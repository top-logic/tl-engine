---
description: Best practices for prompting the ModelExplorer agent via requestModelExploration
---
# How to Prompt the Model Explorer

The `requestModelExploration` tool delegates to the ModelExplorer agent which has
read-only access to the TopLogic application model. It takes three fields:

- **goal** — One-line intent (what you want to achieve)
- **task** — Concrete question or instruction (what the explorer should do)
- **context** — Optional hints: module names, type names, part names

## Golden Rules

1. **Never paste the user message verbatim.** Distill it into goal + task + context.
2. **Be specific.** "List all types in module sales" beats "Tell me about sales."
3. **Name entities when known.** If you already know a module or type name, put it in context.
4. **One question per call.** Ask "What parts does Order have?" separately from "Where is Order used?"
5. **Chain calls.** Use the result of one call to refine the next (e.g., resolve a type first, then list its parts).

## Available Explorer Tools (what it can do)

| Tool | Use when you need |
|------|-------------------|
| `listModules` | Overview of all modules (names, type counts) |
| `listTypes` | All types in a specific module |
| `listTypeParts` | Properties and references of a type |
| `listClassifiers` | Enum values of an enumeration |
| `searchModel` | Find a type/part by name (fuzzy search) |
| `findTypeUsages` | Where a type is used as property type or reference target |
| `findPartUsages` | Where a specific part is defined and inherited |
| `listAnnotations` | Annotations on a type, part, or classifier |
| `describeAnnotation` | Schema of an annotation config interface |
| `listInstances` | Paginated instance data for a type |
| `getInstance` | Single instance details by TID |

## Prompting Patterns

### Pattern 1: Resolve before inspecting

When the user mentions a type name without a module prefix, ask the explorer
to search first, then inspect.

**Call 1 — resolve:**
- goal: "Find type Order in application modules"
- task: "Search for type named 'Order' in application modules (not framework)"
- context: ""

**Call 2 — inspect (using result from call 1):**
- goal: "List all parts of Order"
- task: "List all properties and references of type Order, including inherited"
- context: "module: sales, type: Order"

### Pattern 2: Module overview

- goal: "List application modules"
- task: "List all modules that are not framework modules, with their type counts"
- context: ""

### Pattern 3: Type usage / impact analysis

- goal: "Find where Customer is used"
- task: "Find all usages of type Customer — which types reference it, which types extend it"
- context: "sales:Customer"

### Pattern 4: Enum values

- goal: "List enum values of Priority"
- task: "List all classifiers of the Priority enumeration"
- context: "module: sales, enum: Priority"

### Pattern 5: Annotations and scripts

- goal: "Find TLScript on Order#total"
- task: "List all annotations on the 'total' part of type Order"
- context: "sales:Order#total"

## Anti-Patterns (avoid these)

- **Vague goal:** "Look at the model" → Too broad, explorer won't know what to focus on.
- **Full user prompt dump:** Copying the entire user message into task → Noisy, confusing.
- **Multiple questions in one call:** "List types and also find usages of Order" → Split into two calls.
- **Guessing qualified names:** Don't invent "sales:Order" if you haven't confirmed it — let the explorer search first.
- **Asking for TLScript interpretation:** The explorer returns code verbatim but does not explain it. Delegate TLScript explanation to the TLScript specialist.
