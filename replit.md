# VoxelAI

A Minecraft-inspired voxel engine with JavaFX graphics and a Gemini AI-powered command system.

## Project Structure

- `game/` - Java/JavaFX game source and assets
  - `src/` - Java source files
  - `out/` - Compiled Java classes
  - `lib/json.jar` - JSON parsing library
  - `lib/javafx-sdk/` - JavaFX 19 SDK JARs (downloaded from OpenJFX)
  - `lib/javafx-patched/` - JavaFX native libs patched for Replit's NixOS glibc-2.37 compatibility
  - `lib/javafx17/` - JavaFX 17 JARs (backup, not currently used)
  - `tiles/` - Block texture images
  - `Player male/`, `Player female/`, `Gnome/` - Character sprite images
- `ai/` - Python FastAPI AI server
  - `ai_server.py` - FastAPI app using Google Gemini
  - `ai_memory.json` - Conversation history
- `build.sh` - Java compilation script
- `run_game.sh` - JavaFX game launch script
- `run_ai_server.sh` - AI server launch script

## Architecture

- **Game (Java/JavaFX)**: Desktop voxel game running via VNC in the browser
- **AI Server (Python/FastAPI)**: Runs on `localhost:8000`, receives natural language commands and translates them to game commands via Gemini AI
- **Communication**: Java `AIClient.java` sends HTTP POST requests to the AI server

## Workflows

- **Start application** (VNC): Runs `bash run_game.sh` - launches the JavaFX game
- **AI Server** (console): Runs `bash run_ai_server.sh` - starts the FastAPI server on port 8000

## Environment Variables / Secrets

- `GEMINI_API_KEY` - Google Gemini AI API key (required for AI commands)

## Java/JavaFX Setup

- **Java**: GraalVM CE 22.3.1 (Java 19) at `/nix/store/c8hr2f0b0dm685yx1dkp6bw24bpx495n-graalvm19-ce-22.3.1`
- **JavaFX**: Version 19, downloaded from OpenJFX, JARs in `game/lib/javafx-sdk/`
- **Native libs**: Patched in `game/lib/javafx-patched/` to use older X11 libs (libX11-1.7.2, glibc-2.34 compat) since the Replit VNC environment uses NixOS glibc-2.37

### Key Patching Notes

The Replit VNC environment uses NixOS glibc-2.37. The standard JavaFX native libs link against newer libX11 (≥1.8.x) which requires glibc-2.38. The solution:
1. Use libX11-1.7.2 (only needs glibc-2.34)
2. Use matching libxcb-1.14, libXtst-1.2.3, libXext-1.3.4, libXi-1.8
3. Patch RPATH on `libglass.so` and `libglassgtk3.so` to point to `game/lib/javafx-patched/`
4. Unset `LD_AUDIT` / `REPLIT_LD_AUDIT` in `run_game.sh` to bypass the Replit runtime linker interception

## Python Dependencies

Managed via `uv` in `.pythonlibs/`:
- fastapi, uvicorn, python-dotenv, google-genai, pydantic
