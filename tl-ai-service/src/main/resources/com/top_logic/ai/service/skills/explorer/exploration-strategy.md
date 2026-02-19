---
description: Step-by-step exploration loop with completion checks and tool recipes
tier: on-demand
---

### EXPLORATION STRATEGY (MODEL EXPLORER LOOP)

For any substantive question, follow this loop:

1) Plan: Identify ALL tool calls that MUST be made before answering.
   Default to MORE tools rather than fewer. If in doubt, call the tool.
2) Acquire: Call tools to gather evidence.
3) Check gaps: If any required entity/field is missing or ambiguous (type/part/annotation/usage), do NOT guess.
   - Run additional tool calls to resolve it, OR ask one clarifying question if multiple candidates remain.
4) Verify completeness: Ensure the relevant scope is enumerated (module/type/parts/usages).
5) Synthesize: Answer with evidence-backed facts and cite which tool produced what.

---

### COMPLETION CHECK (after every tool call)

After receiving each tool result, ask yourself:
□ Have I confirmed ALL entities mentioned in my planned answer via tools?
□ Have I enumerated (not assumed) any collections (types, parts, usages)?
□ Can I cite a specific tool call for EVERY fact I will state?

If ANY checkbox is unchecked → call another tool.
Only when ALL boxes are checked → synthesize answer.

---

### COMMON EXPLORATION FLOWS (tool recipes)

- Model overview:
listModules() → (default: frameworkModule=false) → listTypes(module)

- Type summary:
searchModel(name) → listTypeParts(type, includeInherited=true) → (optional) listAnnotations(type / type#part)

- Attribute / constraints:
listTypeParts(type, includeInherited=true) → for relevant parts: listAnnotations(type#part)

- Where-used (STRICT):
findTypeUsages(type) →
	- Report ONLY the buckets returned by the tool (e.g., Properties / References / Subclasses).
	- If a bucket is empty: "none reported by tool".
	- Do NOT add narratives like "used as value type" unless the tool explicitly says so.
	- If the tool outputs `Type$part`: interpret ONLY as "part <part> on <Type>" (no paths like "self").
	- If you mention a part's datatype/target, verify it via listTypeParts(Type, includeInherited=true).
