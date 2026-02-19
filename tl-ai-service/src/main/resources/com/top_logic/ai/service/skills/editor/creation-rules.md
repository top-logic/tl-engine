---
description: Idempotent creation, mandatory creation order, naming conventions, i18n
tier: core
---
### IDEMPOTENT CREATION

All creation tools are **idempotent**: if an element already exists, the tool returns it without creating a duplicate. You do NOT need to check what exists before creating. Just call the creation tools directly — they handle deduplication.

---

### AVAILABLE TOOLS

**Creation tools** (for building model elements):
- `createModule` — Create a new module or get an existing one
- `createClass` — Create a new class in a module
- `createProperty` — Create a primitive property on a class (STRING, INT, BOOLEAN, FLOAT, DATE, TRISTATE, BINARY, CUSTOM)
- `createReference` — Create a forward reference from one class to another
- `createBackwardReference` — Create a backward reference to an existing forward reference
- `createEnumeration` — Create an enumeration with classifiers
- `updateModule`, `updateClass`, `updateProperty`, `updateEnumeration` — Update existing elements

**Delegation tools** (for calling specialist agents):
- `requestTLScriptHelp` — Delegate to the **TLScriptAgent** for TLScript syntax, function docs, script validation, or code generation.

---

### MANDATORY CREATION ORDER

You MUST create elements in this strict order:

1. **Modules first** — Create the target module (returns existing if it already exists)
2. **Enumerations** — Create any enumerations before classes that reference them
3. **Classes** — Create all classes before adding properties/references
4. **Properties** — Add primitive properties to classes
5. **Forward references** — Create references between classes
6. **Backward references** — Only if explicitly requested

Never create a property or reference before its owning class exists.
Never create a reference before its target class exists.

---

### NAMING CONVENTIONS

- **Module names**: lowercase, may contain dots (e.g., `test`, `myapp.core`)
- **Class names**: PascalCase, start with uppercase (e.g., `TestClass`, `OrderItem`)
- **Property names**: camelCase or lowercase (e.g., `name`, `orderDate`, `totalPrice`)
- **Reference names**: camelCase (e.g., `items`, `assignedTo`, `category`)
- **Enumeration names**: PascalCase (e.g., `Priority`, `OrderStatus`)

---

### I18N (INTERNATIONALIZATION)

For all created elements, provide:
- `labelEn` — English label: humanized name (split camelCase, e.g., "Order Item")
- `labelDe` — German label: same as English unless you know the German translation
- `descEn` / `descDe` — Description if provided in requirements, otherwise empty string
