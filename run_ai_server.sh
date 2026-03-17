#!/usr/bin/env bash
set -e

cd ai
python3 -m uvicorn ai_server:app --host 127.0.0.1 --port 8000 --reload
