package plugin

import pumpkin.Command.Command
import pumpkin.Command.CommandNode
import pumpkin.Command.CommandSender
import pumpkin.Command.CommandSuggestions
import pumpkin.Command.ConsumedArgs
import pumpkin.Command.SuggestionRequest
import pumpkin.Context.Context
import pumpkin.Event
import pumpkin.Logging
import pumpkin.Server.Server

/**
 * Small compatibility layer for the `events.listen` / `commands.register`
 * helpers documented by Pumpkin but not present in every generated
 * `pumpkin-api-kt` release.
 *
 * Add this file to the plugin's `src/wasmWasiMain/kotlin` source set (or a
 * shared source set) and make your plugin extend [PumpkinCompatPlugin].
 */
abstract class PumpkinCompatPlugin : PumpkinPlugin() {
    private val eventRegistry = EventRegistry()
    private val commandRegistry = CommandRegistry()
    val ipc = IpcRegistry()
    val compatTasks = CompatTaskScheduler()
    val aiGoals = AiGoalRegistry()

    val events: EventRegistry get() = eventRegistry
    val commands: CommandRegistry get() = commandRegistry

    override fun handleEvent(
        eventId: UInt,
        server: Server,
        event: Event.Event,
    ): Event.Event = eventRegistry.dispatch(eventId, server, event)

    override fun handleCommand(
        commandId: UInt,
        sender: CommandSender,
        server: Server,
        args: ConsumedArgs,
    ): Result<Int> = commandRegistry.dispatch(commandId, sender, server, args)

    override fun handleCommandSuggestion(
        handlerId: UInt,
        sender: CommandSender,
        server: Server,
        request: SuggestionRequest,
    ): CommandSuggestions = commandRegistry.dispatchSuggestion(handlerId, sender, server, request)

    override fun handleIpcMessage(sender: String, message: List<UByte>): Result<List<UByte>> =
        ipc.handle(sender, message)

    override fun handleTask(handlerId: UInt, server: Server) {
        if (!compatTasks.handle(handlerId, server)) {
            super.handleTask(handlerId, server)
        }
    }

    override fun handleAiGoalCanStart(goalId: UInt, server: Server, entity: pumpkin.World.Entity): Boolean =
        aiGoals.canStart(goalId, server, entity)

    override fun handleAiGoalShouldContinue(goalId: UInt, server: Server, entity: pumpkin.World.Entity): Boolean =
        aiGoals.shouldContinue(goalId, server, entity)

    override fun handleAiGoalStart(goalId: UInt, server: Server, entity: pumpkin.World.Entity) =
        aiGoals.start(goalId, server, entity)

    override fun handleAiGoalTick(goalId: UInt, server: Server, entity: pumpkin.World.Entity) =
        aiGoals.tick(goalId, server, entity)

    override fun handleAiGoalStop(goalId: UInt, server: Server, entity: pumpkin.World.Entity) =
        aiGoals.stop(goalId, server, entity)
}

class CompatTaskScheduler {
    private var nextId = 0u
    private val handlers = mutableMapOf<UInt, (Server) -> Unit>()
    private val active = mutableMapOf<UInt, ScheduledTask>()

    fun afterTicks(delayTicks: ULong, action: (Server) -> Unit): ScheduledTask =
        schedule(false, delayTicks, 0u, action)

    fun everyTicks(delayTicks: ULong, periodTicks: ULong, action: (Server) -> Unit): ScheduledTask =
        schedule(true, delayTicks, periodTicks, action)

    private fun schedule(
        repeating: Boolean,
        delayTicks: ULong,
        periodTicks: ULong,
        action: (Server) -> Unit,
    ): ScheduledTask {
        val id = nextId++
        handlers[id] = action
        val taskId = if (repeating) {
            pumpkin.Scheduler.scheduleRepeatingTask(id, delayTicks, periodTicks)
        } else {
            pumpkin.Scheduler.scheduleDelayedTask(id, delayTicks)
        }
        val task = ScheduledTask {
            handlers.remove(id)
            active.remove(id)
            pumpkin.Scheduler.cancelTask(taskId)
        }
        active[id] = task
        return task
    }

    internal fun handle(handlerId: UInt, server: Server): Boolean {
        val handler = handlers[handlerId] ?: return false
        handler(server)
        return true
    }
}

class IpcRegistry {
    private var handler: ((String, ByteArray) -> ByteArray)? = null

    fun onMessage(handler: (String, ByteArray) -> ByteArray) {
        this.handler = handler
    }

    fun sendIpcMessage(recipient: String, message: ByteArray): ByteArray {
        val result = pumpkin.Ipc.sendIpcMessage(recipient, message.map { it.toUByte() })
        return result.getOrNull()?.getOrNull()?.map { it.toByte() }?.toByteArray() ?: ByteArray(0)
    }

    internal fun handle(sender: String, message: List<UByte>): Result<List<UByte>> {
        val callback = handler ?: return Result.failure(IllegalStateException("No IPC handler"))
        return runCatching { callback(sender, message.map { it.toByte() }.toByteArray()).map { it.toUByte() } }
    }
}

class EventRegistry {
    private var nextId = 0u
    private val handlers = mutableMapOf<UInt, (Server, Event.Event) -> Event.Event>()

    fun listen(
        context: Context,
        type: Event.EventType,
        priority: Event.EventPriority,
        blocking: Boolean,
        listener: (Server, Event.Event) -> Event.Event,
    ): EventSubscription {
        val id = nextId++
        handlers[id] = listener
        context.registerEvent(id, type, priority, blocking)
        return EventSubscription { handlers.remove(id) }
    }

    internal fun dispatch(eventId: UInt, server: Server, event: Event.Event): Event.Event =
        handlers[eventId]?.invoke(server, event) ?: event
}

class EventSubscription internal constructor(
    private val onUnregister: () -> Unit,
) {
    private var active = true

    fun unregister(): Boolean {
        if (!active) {
            return false
        }
        active = false
        onUnregister()
        return true
    }
}

class CommandRegistry {
    private var nextId = 0u
    private var nextSuggestionId = 0u
    private val handlers = mutableMapOf<
        UInt,
        (CommandSender, Server, ConsumedArgs) -> Result<Int>
        >()
    private val suggestionHandlers = mutableMapOf<
        UInt,
        (CommandSender, Server, SuggestionRequest) -> CommandSuggestions
        >()

    fun register(
        context: Context,
        command: Command,
        permission: String,
        handler: (CommandSender, Server, ConsumedArgs) -> Result<Int>,
    ) {
        val id = nextId++
        handlers[id] = handler
        // The host only registers an executable command node when a handler ID
        // has been attached to the command resource. Without this call the
        // command is absent from the dispatcher and clients receive
        // "Unknown command" even though the plugin loaded successfully.
        try {
            Logging.log(Logging.Level.INFO, "Registering command handler $id")
            command.executeWithHandlerId(id)
            context.registerCommand(command, permission)
            Logging.log(Logging.Level.INFO, "Registered command handler $id")
        } catch (failure: Throwable) {
            Logging.log(Logging.Level.ERROR, "Command registration failed: ${failure.message}")
            throw failure
        }
    }

    fun onSuggestions(
        node: CommandNode,
        handler: (CommandSender, Server, SuggestionRequest) -> CommandSuggestions,
    ) {
        val id = nextSuggestionId++
        suggestionHandlers[id] = handler
        node.suggestWithHandlerId(id)
    }

    internal fun dispatch(
        commandId: UInt,
        sender: CommandSender,
        server: Server,
        args: ConsumedArgs,
    ): Result<Int> = handlers[commandId]?.invoke(sender, server, args)
        ?: Result.failure(IllegalStateException("No command handler for id $commandId"))

    internal fun dispatchSuggestion(
        handlerId: UInt,
        sender: CommandSender,
        server: Server,
        request: SuggestionRequest,
    ): CommandSuggestions = suggestionHandlers[handlerId]?.invoke(sender, server, request)
        ?: CommandSuggestions(0u, 0u, emptyList())
}
