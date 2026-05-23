# Test Dimension

A NeoForge 1.21.1 mod for creating a dedicated test dimension with isolated player state.

The mod is designed for server operators and mod developers who want a safe sandbox dimension for testing blocks, commands, builds, and player setups without polluting normal survival progression.

## Environment

- Minecraft: `1.21.1`
- Loader: `NeoForge`
- Java: `21`
- Build tool: `Gradle`

## Features

- Registers a test dimension at `testdim:test`
- Adds short commands:
  - `/td enter`
  - `/td leave`
- Keeps `/testdim enter` and `/testdim leave` as compatibility aliases
- Isolates player state between normal gameplay and the test dimension:
  - inventory
  - ender chest
  - experience
  - health
  - hunger
  - saturation
  - game mode
  - saved position
- Stores player state using NeoForge player data attachments
- Initializes the first test-dimension profile as an empty creative profile
- Uses a flat test dimension with:
  - 64 layers of `minecraft:iron_block`
  - no structures
  - no lakes
  - no decoration features
  - `minecraft:the_void` biome

## Current Scope

Implemented:

- staged dimension switching workflow
- symmetric normal/test player profile storage
- profile restore on entering and leaving the test dimension
- basic login/logout/clone lifecycle handling

Not implemented yet:

- advancement isolation or advancement snapshot restore
- deeper edge-case handling for all death/respawn flows on dedicated servers

## Commands

```mcfunction
/td enter
/td leave
```

Compatibility aliases:

```mcfunction
/testdim enter
/testdim leave
```

## Project Structure

```text
src/main/java/com/awfufu/testdimension/
  TestDimensionMod.java
  TestDimensionKeys.java

src/main/java/com/awfufu/testdimension/attachment/
  ModAttachments.java
  PlayerDimensionData.java

src/main/java/com/awfufu/testdimension/command/
  TestDimensionCommands.java

src/main/java/com/awfufu/testdimension/event/
  TestDimensionEvents.java

src/main/java/com/awfufu/testdimension/player/
  PlayerStateManager.java
  PlayerStateProfile.java
  SavedPosition.java

src/main/resources/
  META-INF/neoforge.mods.toml
  pack.mcmeta
  data/testdim/dimension/test.json
  data/testdim/dimension_type/test_type.json
```

## Development

### IntelliJ IDEA

1. Open the project as a Gradle project.
2. Make sure IntelliJ uses Java `21`.
3. Run the Gradle task `runClient` or `runServer`.

### Terminal

Run the client dev environment:

```bash
./gradlew runClient
```

Run the dedicated server dev environment:

```bash
./gradlew runServer
```

For local dedicated server testing:

1. Accept the EULA in `run/server/eula.txt`
2. Set `online-mode=false` in `run/server/server.properties`

## Build

```bash
./gradlew build
```

The built jar is written to `build/libs/`.

## Server Compatibility Notes

- The mod is written to be server-authoritative.
- `displayTest="IGNORE_SERVER_VERSION"` is set in `neoforge.mods.toml` to reduce strict mod-presence matching in supported NeoForge handshake scenarios.
- A pure vanilla client still cannot join a NeoForge server just because this mod is server-side only. That limitation comes from the loader environment, not from this mod itself.

## Persistence Model

- Normal gameplay state is stored in a `normalProfile`
- Test dimension state is stored in a `testProfile`
- The mod uses NeoForge player attachments rather than separate custom save files
- Both profiles store their own position, so returning to either side restores the correct location

## Notes

- Entering the test dimension for the first time starts at `0 64 0` with an empty creative profile.
- Important actions log through the standard NeoForge/Minecraft log output to simplify debugging.
- Advancement isolation is intentionally deferred because it is more fragile than inventory/state isolation.
