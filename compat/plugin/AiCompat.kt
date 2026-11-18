package plugin

import pumpkin.Server
import pumpkin.World

/**
 * Small custom-AI goal registry. It only registers behaviour for an existing
 * mob; it does not alter world generation or chunk generation.
 */
interface AiGoal {
    fun canStart(server: Server.Server, entity: World.Entity): Boolean = true

    fun shouldContinue(server: Server.Server, entity: World.Entity): Boolean = true

    fun start(server: Server.Server, entity: World.Entity) {}

    fun tick(server: Server.Server, entity: World.Entity) {}

    fun stop(server: Server.Server, entity: World.Entity) {}
}

class AiGoalRegistry {
    private var nextId = 0u
    private val goals = mutableMapOf<UInt, AiGoal>()

    fun register(mob: World.Mob, priority: Int, goal: AiGoal) {
        val id = nextId++
        goals[id] = goal
        mob.addCustomAiGoal(priority.toUByte(), id)
    }

    internal fun canStart(id: UInt, server: Server.Server, entity: World.Entity): Boolean =
        goals[id]?.canStart(server, entity) ?: false

    internal fun shouldContinue(id: UInt, server: Server.Server, entity: World.Entity): Boolean =
        goals[id]?.shouldContinue(server, entity) ?: false

    internal fun start(id: UInt, server: Server.Server, entity: World.Entity) {
        goals[id]?.start(server, entity)
    }

    internal fun tick(id: UInt, server: Server.Server, entity: World.Entity) {
        goals[id]?.tick(server, entity)
    }

    internal fun stop(id: UInt, server: Server.Server, entity: World.Entity) {
        goals[id]?.stop(server, entity)
    }
}
