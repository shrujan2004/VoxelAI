from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import json
import os
from dotenv import load_dotenv
from google import genai

# ==================================================
# LOAD ENVIRONMENT (.env FILE)
# ==================================================
load_dotenv()

API_KEY = os.getenv("GEMINI_API_KEY")
if not API_KEY:
    raise RuntimeError("GEMINI_API_KEY environment variable not set")

# ==================================================
# INIT GEMINI CLIENT (NEW OFFICIAL SDK)
# ==================================================
client = genai.Client(api_key=API_KEY)

# ==================================================
# FASTAPI APP
# ==================================================
app = FastAPI()

MEMORY_FILE = "ai_memory.json"
MAX_HISTORY = 6   # last 3 user + AI turns

# ==================================================
# REQUEST MODEL
# ==================================================
class ChatRequest(BaseModel):
    message: str

# ==================================================
# MEMORY HANDLING (BOM SAFE)
# ==================================================
def load_memory():
    if os.path.exists(MEMORY_FILE):
        try:
            with open(MEMORY_FILE, "r", encoding="utf-8-sig") as f:
                return json.load(f)
        except Exception:
            return []
    return []

def save_memory(memory):
    with open(MEMORY_FILE, "w", encoding="utf-8") as f:
        json.dump(memory[-MAX_HISTORY:], f, indent=2)

# ==================================================
# SYSTEM PROMPT (CRITICAL)
# ==================================================
SYSTEM_PROMPT = """
You are an in-game AI assistant inside a voxel sandbox game.

STRICT RULES:
- Respond ONLY with valid JSON
- Do NOT include explanations outside JSON
- You MUST always output a command
- Allowed command: fill
- Allowed blocks: WOOD, STONE, DIRT, GLASS
- Coordinates must be integers
- Max total blocks per command: 100

INTERPRETATION RULES:
- "platform", "floor", "area" → use fill
- "5 by 5" means x=0..4 and z=0..4
- Default y = 1 if not specified
- If unclear, make a reasonable assumption

RESPONSE FORMAT (EXACT):
{
  "command": "fill",
  "params": {
    "x1": 0,
    "y1": 1,
    "z1": 0,
    "x2": 4,
    "y2": 1,
    "z2": 4,
    "block": "WOOD"
  },
  "message": "Short friendly response"
}
"""

# ==================================================
# API ENDPOINT
# ==================================================
@app.post("/ask")
async def ask_ai(request: ChatRequest):
    memory = load_memory()

    # Build prompt with memory
    prompt = SYSTEM_PROMPT.strip() + "\n\n"
    for turn in memory:
        prompt += f"{turn['role']}: {turn['content']}\n"

    prompt += f"user: {request.message}"

    try:
        response = client.models.generate_content(
            model="gemini-2.0-flash",
            contents=prompt
        )

        text = response.text.strip()

        # HARD JSON VALIDATION
        try:
            data = json.loads(text)
        except json.JSONDecodeError:
            # Fallback: NEVER CRASH
            data = {
                "command": "fill",
                "params": {
                    "x1": 0,
                    "y1": 1,
                    "z1": 0,
                    "x2": 1,
                    "y2": 1,
                    "z2": 1,
                    "block": "WOOD"
                },
                "message": "I had trouble understanding, so I placed a small wooden block."
            }

        # Save memory safely
        memory.append({"role": "user", "content": request.message})
        memory.append({"role": "assistant", "content": json.dumps(data)})
        save_memory(memory)

        return data

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=str(e)
        )

# ==================================================
# RUN SERVER
# ==================================================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
