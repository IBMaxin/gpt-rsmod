# RSMod + RSProx Setup Guide

This guide walks you through setting up RSMod (game server) and RSProx (client proxy) for local development and testing.

## Prerequisites

- **Java 21** (Temurin/Adoptium recommended)
- **OSRS cache zip** (revision 233) in the project root
- **Internet connection** for downloading xteas

## Project Structure

```
gpt-rsmod/
├── rsmod-main/          # RSMod game server
├── rsprox-master/       # RSProx proxy tool
└── cache-oldschool-live-en-b233-*.zip   # OSRS cache archive
```

---

## Step 1: Build Both Projects

```sh
# Build RSMod
cd rsmod-main
.\gradlew.bat spotlessApply    # Fix any formatting issues first
.\gradlew.bat build

# Build RSProx
cd ..\rsprox-master
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat build
```

---

## Step 2: Extract Cache

RSMod expects cache files in `.data/cache/vanilla/` (not `cache/vanilla/`).

```powershell
# Create directory
New-Item -ItemType Directory -Force -Path ".data\cache\vanilla"

# Extract cache
Expand-Archive -Path "..\cache-oldschool-live-en-b233-*.zip" `
  -DestinationPath ".data\cache\vanilla" -Force

# Flatten nested directory (zip contains a 'cache/' subfolder)
Move-Item -Path ".data\cache\vanilla\cache\*" -Destination ".data\cache\vanilla\" -Force
Remove-Item -Path ".data\cache\vanilla\cache" -Force -ErrorAction SilentlyContinue
```

Verify files are in place:
```powershell
Get-ChildItem ".data\cache\vanilla" -Name
# Should show: main_file_cache.dat2, main_file_cache.idx0, etc.
```

---

## Step 3: Download XTEAs

```powershell
Invoke-WebRequest -Uri "https://archive.openrs2.org/caches/runescape/2293/keys.json" `
  -OutFile ".data\cache\xteas.json" -UseBasicParsing
```

---

## Step 4: Pack Cache

```sh
.\gradlew.bat packCache --console=plain
```

This enriches the vanilla cache with game-specific data.

---

## Step 5: Generate RSA Keys

```sh
.\gradlew.bat generateRsa --console=plain
```

This outputs:
- **Private key**: `.data/game.key`
- **Public modulus**: `.data/client.key`
- **Modulus** (printed to console) — you need this for RSProx config

Save the printed modulus value (long hex string).

---

## Step 6: Configure RSProx Proxy Target

Create the RSProx config directory:
```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\.rsprox"
```

Create `~/.rsprox/proxy-targets.yaml`:
```yaml
config:
  - name: RS Mod Local
    jav_config_url: https://client.blurite.io/jav_local_233.ws
    revision: 233.1
    modulus: <PASTE_RSA_MODULUS_HERE>
    game_server_port: 43594
```

---

## Running

### Terminal 1 — RSMod Server
```sh
cd rsmod-main
.\gradlew.bat run --console=plain
```

Wait for:
```
Bound to ports: 43594
Revision: 233
```

### Terminal 2 — RSProx Proxy
```sh
cd rsprox-master
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat proxy
```

### Connect
1. In RSProx GUI, select **"Default"** Jagex Account mode
2. Select **"Native"** or **"RuneLite"** client type
3. Select **"RS Mod Local"** proxy target
4. Press **Launch**

The client downloads, patches, and connects to your local server on port 43594.

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Spotless formatting errors | `.\gradlew.bat spotlessApply` |
| Invalid javaHome error | Set `$env:JAVA_HOME` to correct JDK path, then `.\gradlew.bat --stop` |
| "Cache not found" during packCache | Ensure files are in `.data/cache/vanilla/` (not nested) |
| Stale zip in cache dirs | Delete `.data/cache/game/*` and `.data/cache/vanilla/disk*.zip` |
| RSProx can't find config | Check `~/.rsprox/proxy-targets.yaml` exists and has correct modulus |

---

## Key Paths

| Item | Location |
|---|---|
| RSMod server entry | `server/app/.../GameServer.kt` |
| Server port | Hardcoded `43594` in `api/net/.../NetworkFactory.kt` |
| RSA private key | `rsmod-main/.data/game.key` |
| RSA public modulus | `rsmod-main/.data/client.key` |
| Server config | `rsmod-main/.data/server.toml` |
| Game cache | `rsmod-main/.data/cache/game/` |
| RSProx config | `~/.rsprox/proxy-targets.yaml` |

---

## Server Config

Auto-created at `.data/server.toml` on first run:
```toml
realm = "dev"
world = 1
```

The `dev` realm enables:
- No password required for login
- Auto-assigned display names
- 150x base XP rate
- Dev mode enabled
