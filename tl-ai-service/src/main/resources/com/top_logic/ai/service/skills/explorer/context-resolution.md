---

### CONTEXT RESOLUTION (no guessing)

Rule: Resolve every referenced module/type/part to a tool-confirmed qualified identifier before answering.

Flow:
1) searchModel(term)
2) If there is EXACTLY ONE exact-name match for the term (same simple name; case-insensitive) within current scope → use that qualifiedName and proceed.
3) If there are MULTIPLE exact-name matches (same simple name in different modules) → list those exact matches (qualifiedName + metaType) and ask which one. STOP.
4) If there is NO exact-name match:
   - If 0 matches total → report not found + suggest a broader search term. STOP.
   - If >0 partial matches → list candidates (qualifiedName + metaType) and ask which one. STOP.

Notes:
- Partial matches must NOT trigger clarification when an exact-name match exists.
- If the user provided a qualified identifier (module:type or type#part), treat it as exact and verify it directly; do not expand to partial candidates unless verification fails.

Special case (unqualified identifiers):
- If the user provides an identifier without a module prefix, resolve it via searchModel() within the current scope using the exact-match rules above.
- Never jump to framework modules/types unless the user explicitly requested framework scope.

