# AGENTS.md - RSProx (rsprox-master)

## Build & Test Commands

| Command | Description |
|---|---|
| `./gradlew test` | Run unit tests |
| `./gradlew proxy` | Run the proxy tool GUI application |
| `./gradlew launcher:shadowJar` | Build the launcher SHAred JAR |
| `./gradlew :gui:build` | Build the GUI module |
| `./gradlew publish` | Publish Maven artifacts to S3 |
| `./gradlew uploadJarsToS3` | Upload project jars to S3 Maven repo |
| `./gradlew download` | Run the client download command |
| `./gradlew tostring` | Run binary-to-string command |
| `./gradlew transcribe` | Run transcription command |
| `./gradlew index` | Run the indexer command |
| `./gradlew patch` | Run the client patcher command |

## Requirements

- **Java 11** (Temurin) for most tasks; some CI uses Java 11 or 17
- Gradle wrapper at `gradlew`
- For launcher build: needs native toolchain (Detours, dropt, sajson) on Windows

## Key Project Structure (subprojects)

- `proxy` - Core proxy logic
- `processor` - Packet processing
- `protocol` - Protocol definitions/decoders
- `patch` - Client patcher
- `gui` - Graphical user interface (ProxyToolGuiKt)
- `transcriber` - Live transcriber
- `cache` - Binary cache handling
- `shared` - Shared utilities
- `launcher` - Launcher build (produces `rsprox-launcher.jar`)
- `runelite` - RuneLite integration

## CI Notes

- `proxy-gui.yml`: Runs tests on ubuntu/macOS/windows, then publishes to S3 on master branch
- `launcher.yml`: Builds launcher JAR and Windows/Linux installers on tag push
- Tests use JUnit 5 platform
- Publishing requires AWS credentials (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
- `uploadJarsToS3` uploads all runtimeClasspath jars and generates `bootstrap.json`

## Custom Gradle Tasks

- `proxy` - Runs `net.rsprox.gui.ProxyToolGuiKt` (GUI entry point)
- `download` - Runs `net.rsprox.proxy.cli.ClientDownloadCommandKt`
- `tostring` - Runs `net.rsprox.proxy.cli.BinaryToStringCommandKt`
- `transcribe` - Runs `net.rsprox.proxy.cli.TranscribeCommandKt`
- `index` - Runs `net.rsprox.proxy.cli.IndexerCommandKt`
- `patch` - Runs `net.rsprox.proxy.cli.ClientPatcherCommandKt`