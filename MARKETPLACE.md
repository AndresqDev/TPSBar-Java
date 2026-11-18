# TPSBar Java (Experimental)

## Short description

A personal TPS, MSPT, and ping boss bar for Pumpkin, written in Java and built to WebAssembly.

## Long description

TPSBar Java adds a compact per-player performance boss bar to Pumpkin. Players can enable
their own bar and choose whether its progress follows MSPT, TPS, or ping. TPS, MSPT, and ping
values are colored by health, while the bar color follows server load. The plugin refreshes once
per second, requires no client mod, and only affects players who enable it.

This is an experimental Java port of the original Rust TPSBar plugin. It is built with the
j2kt-pumpkin Java-to-Kotlin-to-Wasm pipeline and Pumpkin Kotlin/Wasm API 0.1.2. It does not read
a TOML file, does not persist preferences across restarts, and uses flat command names.

## Compatibility

- Built with Pumpkin Kotlin/Wasm API 0.1.2 and synchronous WIT v0.1.
- Tested with Pumpkin nightly `0.2.2+26.3-26.63` (tag `87f8822`). Official 0.2.1+ and
  0.2.2+ servers expose the same v0.1 compatibility ABI.
- Strict Pumpkin API 0.2.0 is async; the current Kotlin binding generator does not
  support async exports. This plugin targets the synchronous v0.1 ABI.
- Kotlin 2.4.0, Pumpkin Gradle plugin 0.1.2, JDK 21.

## Reference implementation

The source is organized as a conventional Java plugin example: a thin plugin entry
point, one class per command, a dedicated listener, a manager for per-player boss bar
state, and separate model/config/permission/util packages. See the README for the full
directory map. This makes the project suitable as the reference example for the
Java-to-Kotlin-to-Wasm toolchain.

## Commands and permissions

| Command | Permission | Description |
|---|---|---|
| `/tpsbar` | `tpsbar-java:command.toggle` | Toggle your personal TPSBar. |
| `/tpsbarmspt` | `tpsbar-java:command.toggle` | Use MSPT for your TPSBar progress. |
| `/tpsbartps` | `tpsbar-java:command.toggle` | Use TPS for your TPSBar progress. |
| `/tpsbarping` | `tpsbar-java:command.toggle` | Use ping for your TPSBar progress. |

Permission `tpsbar-java:command.toggle` defaults to OP level 3.

## Experimental limitations

- Preferences reset when the plugin or server restarts.
- English-only messages.
- Flat commands instead of the upstream nested `/tpsbar by ...` command tree.
- No config file.

## Attribution

Inspired by TPSBar by PING: https://github.com/PingGai/Pumpkin-TPSBar
