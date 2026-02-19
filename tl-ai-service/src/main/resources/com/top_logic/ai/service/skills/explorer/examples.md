---
description: Example exploration interactions demonstrating correct tool-first behavior
tier: on-demand
---

### Examples (Generic, rule-demonstrating; fictional app.* only)

NOTE: Examples are illustrative placeholders. Always resolve real names via tools.

----------------------------------------------------------------
Example A — Unqualified identifier → ambiguity hard-stop
----------------------------------------------------------------
Prompt:
Where is `Status` used?

Bad (FORBIDDEN - no tool calls):
- "You probably mean app.orders:Status …" (assumption without tools)

Good (tool-first):
[First call searchModel(term="Status")]
[Tool returns multiple matches]

Evidence
- Tool: listModules() → scope set to frameworkModule=false
- Tool: searchModel(term="Status") → 3 matches: app.orders:OrderStatus, app.billing:PaymentStatus, app.support:Status

Facts
- Scope: application-only
- `Status` does not resolve to a single exact match.

Next suggested query
- Which one did you mean?
	- `app.orders:OrderStatus` (metaType=enumeration)
	- `app.billing:PaymentStatus` (metaType=enumeration)
	- `app.support:Status` (metaType=class)

(Stop here. No further claims.)

----------------------------------------------------------------
Example B — Not found → hard-stop, no invented types
----------------------------------------------------------------
Prompt:
Show parts of `app.inventory:WarehouseLocation`.

Bad (FORBIDDEN - guessing without tools):
- "Maybe it's called Location …" (guessing)
- "WarehouseLocation typically has fields like..." (inventing)

Good (tool-first):
[First call searchModel(term="app.inventory:WarehouseLocation")]
[Tool returns 0 matches]

Evidence
- Tool: searchModel(term="app.inventory:WarehouseLocation") → 0 matches

Facts
- Scope: explicit modules: `app.inventory`
- Not found in current scope: `app.inventory:WarehouseLocation`.

Next suggested query
- Try a broader search term: searchModel("Location") or confirm the module prefix.

(Stop here.)

----------------------------------------------------------------
Example C — Where-used STRICT → buckets only + Type$part parsing
----------------------------------------------------------------
Prompt:
Where is `app.catalog:Item` used?

Bad (FORBIDDEN):
- "Item is used as a value type …" (narrative not in tool output)
- "property self …" (invented path)

Good (tool-first):
[First call searchModel(term="Item")]
[Then call findTypeUsages(type="app.catalog:Item")]

Evidence
- Tool: searchModel(term="Item") → selected qualifiedName="app.catalog:Item"
- Tool: findTypeUsages(type="app.catalog:Item") → References: app.orders:OrderLine$item, app.pricing:PriceEntry$item; Properties: none; Subclasses: none

Facts
- Scope: explicit modules: `app.catalog`
- Type resolved: `app.catalog:Item`
- Usages (tool buckets only):
	- Properties: none reported by tool
	- References: `app.orders:OrderLine$item`, `app.pricing:PriceEntry$item`
	- Subclasses: none reported by tool

Next suggested query
- Show the definition of `app.orders:OrderLine#item` (target, multiplicity, mandatory).

----------------------------------------------------------------
Example D — UI/behavior question → evidence-only phrasing
----------------------------------------------------------------
Prompt:
Why can't I edit `discount` on `app.pricing:PriceEntry`?

Bad (FORBIDDEN - asserting behavior):
- "The UI disables it because it's computed." (asserting runtime/UI behavior without tools)

Good (tool-first):
[First call listTypeParts(type="app.pricing:PriceEntry", includeInherited=true)]
[Then call listAnnotations(targetRef="app.pricing:PriceEntry#discount")]

Evidence
- Tool: listTypeParts(type="app.pricing:PriceEntry", includeInherited=true) → discount: derived=true, mandatory=false
- Tool: listAnnotations(targetRef="app.pricing:PriceEntry#discount") → [annotation fields]

Facts
- Scope: explicit modules: `app.pricing`
- Part: `app.pricing:PriceEntry#discount`

Model evidence that may affect UI
- derived=true (from listTypeParts)
- mandatory=false (from listTypeParts)
- annotations=[verbatim from listAnnotations]

(No claims about what the UI "will" do; only model evidence.)

----------------------------------------------------------------
Example E — Script boundary → verbatim only + strict "no script found"
----------------------------------------------------------------
Prompt:
What script computes `app.analytics:Report#total`?

Bad (FORBIDDEN - interpreting logic):
- "It sums the line items …" (interpreting logic without seeing code)

Good (tool-first):
[First call listTypeParts(type="app.analytics:Report", includeInherited=true)]
[Then call listAnnotations(targetRef="app.analytics:Report#total")]

Evidence
- Tool: listTypeParts(type="app.analytics:Report", includeInherited=true) → total: derived=true, type=Float
- Tool: listAnnotations(targetRef="app.analytics:Report#total") → payload: { ... }

Facts
- Scope: explicit modules: `app.analytics`
- Part metadata: derived=true, type=Float (from listTypeParts)
- Annotation payload (verbatim):
	```tlscript
	{ /* verbatim payload from listAnnotations(...) */ }
	```

If listAnnotations returns empty:
- No script found (listAnnotations(targetRef="app.analytics:Report#total") returned empty in this run).

----------------------------------------------------------------
Example F — Lazy exploration (FORBIDDEN)
----------------------------------------------------------------
Prompt:
What properties does Order have?

Bad (FORBIDDEN - no tool calls):
- "Order typically has properties like id, date, total, customer..."
→ This is hallucination. You called no tools.

Bad (FORBIDDEN - assumed without enumeration):
- "Order has 3 properties: id, date, total"
→ You did not call listTypeParts(). This is invalid.

Good (tool-first):
[First call searchModel(term="Order") to resolve the type]
[Then call listTypeParts(type="resolved:Order", includeInherited=true)]

Evidence
- Tool: searchModel(term="Order") → app.orders:Order (class)
- Tool: listTypeParts(type="app.orders:Order", includeInherited=true) → id, orderDate, total, customer, lines (5 parts)

Facts
- Type: app.orders:Order
- Parts (from listTypeParts):
  - id (property, type=String)
  - orderDate (property, type=Date)
  - total (property, type=Float, derived=true)
  - customer (reference, target=app.customers:Customer)
  - lines (reference, target=app.orders:OrderLine, multiple=true)
