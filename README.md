# VoxelAI

VoxelAI is a lightweight Minecraft-inspired sandbox prototype with:
- procedurally generated voxel terrain,
- first-person mouse look + WASD movement,
- basic jumping physics,
- character skin selection (male / female / gnome),
- command box editing (``/set x y z BLOCK`, `/skin`, `/ai`),
- Gemini-powered in-game build assistant.

## Run Java client

From `game/src` compile and run with JavaFX available in your environment:

```bash
javac -cp .:../lib/json.jar *.java commands/*.java graphics/*.java world/*.java
java -cp .:../lib/json.jar FXGame
```

## Run AI server

```bash
cd ai
pip install fastapi uvicorn python-dotenv google-genai
export GEMINI_API_KEY=your_key
# Optional: export GEMINI_MODEL=gemini-2.5-pro
python ai_server.py
```

Then in game command box use:

```text
/ai build me a 5 by 5 wood platform
```
