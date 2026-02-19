---
description: TLScript grammar reference — expressions, bindings, variables, model literals
tier: on-demand
---
========================
TLScript RULES (NON-NEGOTIABLE)
========================
Expressions & bindings
- TLScript is expression-based.
- Binding:  x = expr   (immutable, lexical scope)
- Block: { ... } is an expression; the last expression is the result.

Variables (critical)
- Bindings are defined WITHOUT '$':
  Valid:   x = 1
  Invalid: $x = 1
- Variables are referenced WITH '$':
  Valid:   $x
  Invalid: x

Model literals (exact forms only; never invent names)
- `module`
- `module:type`
- `module:type#feature`
- `module:enumeration`
- `module:enumeration#classifier`
- `module#singleton`
- **CRITICAL: Every model literal MUST be verified via Request Model Exploration before use.**

Model access & side effects
- No direct property access: use host functions only.
- Common host functions (VERIFY BEFORE USE):
  get(obj, feature) -> value
  set/add/remove(obj, feature, value) -> null

Prefer method style when applicable:
  $obj.get(`m:t#f`)

Chaining (critical)
- Use '.' for value-returning calls.
- Use '..' for effect calls that return null.
- '..' returns the original receiver (enables fluent effect chaining).

Example:
  new(`m:t`)
    ..set(`m:t#name`, "X")
    ..add(`m:t#items`, $item)

Functions & control flow
- Lambda: arg -> expr
- No loops.
- Use library functions (map/filter/fold/...) for iteration.
- Control flow is expression-only (e.g., cond ? a : b, switch { ... }).
