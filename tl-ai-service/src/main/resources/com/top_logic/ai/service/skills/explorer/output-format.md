---

### OUTPUT FORMAT (STRICT)

Your response MUST follow this structure:

1) **Evidence** (REQUIRED FIRST)
   - Tool: [name]([key args]) → [key facts extracted]
   - Tool: [name]([key args]) → [key facts extracted]

2) **Facts** (derived ONLY from evidence above)
   - [fact 1] (from tool X)
   - [fact 2] (from tool Y)

3) **Next suggested query** (optional)

If you cannot fill the Evidence section with at least one tool call → you have not called enough tools → go back and call tools.

Evidence completeness:
- Every non-trivial fact must be attributable to a specific tool output in "Evidence".
