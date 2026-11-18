# TPSBar Java

TPSBar Java adds a per-player TPS, MSPT, and ping boss bar to Pumpkin.

## Compatibility

| Component | Version |
|---|---|
| TPSBar Java | 0.1.0 |
| Pumpkin server tested | 0.2.2+26.3-26.63 |
| Pumpkin Kotlin/Wasm API | 0.1.2 |
| Pumpkin plugin WIT | `pumpkin:plugin@0.1.0` (synchronous) |
| Kotlin | 2.4.0 |
| Java | 21 |
| Gradle | 9.8.1 |

The plugin uses the synchronous `pumpkin:plugin@0.1.0` ABI. The async `pumpkin:plugin@0.2.0` ABI is not supported.

## Installation

1. Download the `.wasm` asset from the latest GitHub release.
2. Rename the downloaded asset to `tpsbar-java.wasm`.
3. Copy it into the server plugin directory:

   ```text
   <pumpkin-server>/plugins/tpsbar-java.wasm
   ```

4. Start or restart the server.
5. Confirm the plugin loaded:

   ```text
   Loaded tpsbar-java (0.1.0)
   ```

6. Grant the command permission or use an operator account:

   ```text
   tpsbar-java:command.toggle
   ```

The permission defaults to OP level 3.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/tpsbar` | `tpsbar-java:command.toggle` | Enable or disable the personal TPS bar. |
| `/tpsbarmspt` | `tpsbar-java:command.toggle` | Use MSPT as the progress metric. |
| `/tpsbartps` | `tpsbar-java:command.toggle` | Use TPS as the progress metric. |
| `/tpsbarping` | `tpsbar-java:command.toggle` | Use ping as the progress metric. |

## Behavior

| Setting | Value |
|---|---|
| Default state | Disabled |
| Default progress metric | MSPT |
| Refresh interval | 20 ticks |
| TPS progress range | 0.0 to 20.0 |
| MSPT progress range | 0.0 to 50.0 |
| Ping progress range | 0 to 200 ms |

The bar title displays TPS, MSPT, and ping. The bar color follows MSPT severity. Command state is stored in memory and is not persisted across server restarts.

## Build

The source is Java. `cleanBuildWasm` translates it to Kotlin and packages the result as a WebAssembly component.

Requirements:

- JDK 21
- Network access to Maven Central and the Gradle Plugin Portal on the first build

Build the WebAssembly artifact:

```bash
./gradlew cleanBuildWasm
```

Output:

```text
target/wasm/tpsbar-java.wasm
```

The build writes translation and packaging traces to:

```text
target/traces/
```

Change the output filename in `gradle.properties`:

```properties
pumpkinWasmName=tpsbar-java
```

The release workflow reads `pumpkinWasmName` and copies the generated file to the versioned release asset name.

## GitHub releases

The release workflow is defined in `.github/workflows/release.yml`. It runs when a tag matching `v*` is pushed.

The workflow:

1. Builds the WebAssembly component with `cleanBuildWasm`.
2. Uploads the generated Wasm file as a GitHub Actions artifact.
3. Downloads that artifact in the release job. The release job does not rebuild the component.
4. Publishes the `.wasm` file and its SHA-256 checksum as GitHub release assets.

Release asset naming:

```text
tpsbar-java-<plugin-version>-pumpkin-<tested-pumpkin-version>-api-<api-version>-wit-<wit-version>.wasm
```

Example for version `0.1.0`:

```text
tpsbar-java-0.1.0-pumpkin-0.2.2+26.3-26.63-api-0.1.2-wit-0.1.0.wasm
```

The workflow verifies that the pushed tag matches the `version` value in `build.gradle.kts`. The Pumpkin and WIT versions are defined in the workflow environment; the API version is read from `target/traces/build-summary.txt`. Release titles use `TPSBar Java <plugin-version> (Pumpkin <tested-pumpkin-version>, API <api-version>, WIT <wit-version>)`.

When changing supported versions, update the compatibility table and the workflow environment together.

### Publishing a release

1. Update `version` in `build.gradle.kts` and `java-plugin/build.gradle.kts`.
2. Update the plugin version in `java-plugin/src/main/java/com/ping/tpsbar/TpsBarPlugin.java`.
3. Commit the changes.
4. Create and push the tag:

   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

The release job creates the GitHub release from the uploaded build artifact. Each release includes a `.sha256` file; verify the download with:

```bash
sha256sum -c tpsbar-java-*.wasm.sha256
```

## Project layout

```text
java-plugin/src/main/java/com/ping/tpsbar/
  TpsBarPlugin.java
  commands/
    TpsBarCommand.java
    ToggleCommand.java
    MetricCommand.java
    MsptCommand.java
    TpsCommand.java
    PingCommand.java
  listeners/
    PlayerLeaveListener.java
  managers/
    TpsBarManager.java
    BarState.java
  models/
    BarMetric.java
    Severity.java
  config/
    TpsBarSettings.java
  permissions/
    TpsBarPermissions.java
  utils/
    MessageUtil.java
    TpsBarDisplay.java
```

`compat/` contains the Kotlin bridge used by the Java-to-Wasm build. `libs/` contains the bundled translator, Pumpkin stub, and Maven artifacts.

## Limitations

- Preferences are not persisted.
- Commands use flat names; nested command trees are not supported by this bridge.
- Messages are English-only.
- The server must provide the synchronous `pumpkin:plugin@0.1.0` ABI.

## License

Apache License 2.0. See `LICENSE`.

TPSBar was designed and published by PING:
https://github.com/PingGai/Pumpkin-TPSBar
