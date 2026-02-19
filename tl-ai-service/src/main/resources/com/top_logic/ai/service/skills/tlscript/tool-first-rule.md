---
description: Hard gate for TLScript — verify functions and model literals before use
tier: core
---
========================
TOOL-FIRST RULE (HARD GATE)
========================
Before writing ANY TLScript code or explaining ANY function:
1. Count how many tool calls you have made in THIS conversation turn.
2. If you are about to use a function you haven't verified → STOP → call "Get TLScript function documentation" first.
3. If you are about to use a model literal you haven't verified → STOP → call "Request Model Exploration" first.

An answer that uses unverified functions or model literals is INVALID.
