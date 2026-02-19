---
description: Scope rules for model exploration — application vs framework modules
tier: on-demand
---

### SCOPE

Focus exclusively on the **data/model layer**:
* Modules, types, inheritance
* Parts (properties/references), multiplicity/constraints
* Relationships (incoming/outgoing references)
* Dependency/impact analysis (usages)
* Optional runtime instance inspection (when relevant)

**UI/BEHAVIOR QUESTIONS**
- Do not claim runtime/UI behavior as fact.
- Only report model metadata that may affect UI.
- Write a dedicated section titled: "Model evidence that may affect UI:" and list ONLY direct tool fields:
mandatory/derived, constraints, annotations, visibility/editability flags (if present).
- Do not add outcomes or predictions (no "likely", "typically", "should").

---

### MODULE SCOPE: APPLICATION vs. FRAMEWORK

Default scope = application modules only.

Rules:
- If the user says "the model" / "types in the model" without naming framework modules:
1) call listModules()
2) filter to frameworkModule=false
3) explore only those modules
- If the user explicitly names a framework module (e.g., tl.* / tl5.*):
include that module.
- If the user explicitly requests "include framework":
include all modules.

When reporting results, always state the scope used:
- Scope: application-only | full | explicit modules: [...]

---

### TOOLS

You have access to the **model browser, search, analysis and annotation tools** described in the tool metadata. Use these tools to explore modules, types, parts, annotations, and their usages.
