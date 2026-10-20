---
description: Read before writing a tool that reads or discloses model objects, or when an agent sees too much or nothing - layer L2: the ModelSecurity facade over ModelAccessRights (canRead/canWrite/canCreate/accessibleTypes, ModelAccessRightsSecurity), the ReadFilter chokepoint, TidResolver and TidDiagnostics, why counts and object labels are not read-scoped (filterSecurity), which tools disclose what, and how tool RBAC on the security root and per-object rights combine.
order: 30
---

# Data authorization

## The `ModelSecurity` facade

Every tool reaches the model access rights through the context it receives:

```java
public interface ModelSecurity {

    boolean canRead(TLObject obj);
    boolean canRead(TLObject obj, TLStructuredTypePart attribute);
    boolean canWrite(TLObject obj);
    boolean canWrite(TLObject obj, TLStructuredTypePart attribute);
    boolean canDelete(TLObject obj);
    boolean canCreate(TLClass type, TLObject context);
    boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute);
    boolean canPerform(TLObject obj, BoundCommandGroup operation);   // custom business operations

    <T extends TLObject> List<T> filterReadable(Collection<T> objects);
    Set<TLClass> accessibleTypes(BoundCommandGroup operation);

    /** Throws ToolSecurityException instead of returning false. */
    void requireRead(TLObject obj);
    void requireWrite(TLObject obj);
}
```

- It is reached as `context.modelSecurity()` from the `ToolExecutionContext`, so tools need no plumbing of their own.
- It is bound to the run's `Person` at construction; there is no per-call `TLContext` read ([identity](doc:ai/security/identity)).
- It delegates straight to `ModelAccessRights.getInstance()` and adds **no policy of its own** - two places deciding "may this be read" is one place too many.
- `ToolSecurityException` carries a message safe to feed back to the model (it names the operation and the type, never the withheld value) and is mapped by the dispatcher to a denied tool result and a log record.

`ModelAccessRightsSecurity` implements it against the engine:

| Facade | Engine call |
|---|---|
| `canRead(obj)` | `isAllowed(person, obj, SimpleBoundCommandGroup.READ)` |
| `canRead(obj, attr)` | `isAllowed(person, obj, attr, SimpleBoundCommandGroup.READ)` |
| `canWrite(obj)` | `isAllowed(person, obj, SimpleBoundCommandGroup.WRITE)` |
| `canDelete(obj)` | `isAllowed(person, obj, SimpleBoundCommandGroup.DELETE)` |
| `canCreate(type, ctx)` | `isAllowedCreate(person, type, ctx)` |
| `canCreate(parent, attr)` | `isAllowedCreate(person, parent, attr)` |
| `canPerform(obj, group)` | `isAllowed(person, obj, group)` |
| `accessibleTypes(group)` | `getAccessibleTypes(person, group)` |
| `filterReadable(coll)` | `isReadAllowed(person, obj)` per element |

`filterReadable` iterates rather than calling `SearchExpression.filterSecurity(person, coll)`: that helper is `Object`-typed and shape-preserving, so a `Collection<T>` routed through it would need an unchecked cast, for no gain. There is no mode in which the facade answers `true` everywhere; `ModelSecurity.UNCHECKED` does, but it is a test seam.

## The `ReadFilter` chokepoint

Every conversion of a `TLObject` into something a model sees goes through **one** class, `ReadFilter`, and an instance of it cannot exist without the `ModelSecurity` of the run being served (`ReadFilter.of(context)` refuses a call that has no agent run to name a principal). Tools do not write serializers of their own, because a serializer of its own is a check one can forget. Two shapes make the unsafe path absent rather than discouraged:

- **`TidResolver.resolve(String, ModelSecurity)` has no single-argument form.** It is the only place a model-supplied TID becomes a live object in `tl-ai-core`, so a tool cannot reach an object the run named without saying whose rights apply. An object the principal may not read is refused with the message of one that does not exist - "exists but hidden" is itself a disclosure.
- **`ExecutionResult.toJson(ReadFilter)` has no argument-less form.** The engine's `all()` leaves securing the result to the caller; this is where the agent framework does.

What `ReadFilter` enforces:

1. An unreadable object contributes **nothing** - no `_tid`, no `_type`, no `_display`, no attribute value - and is *omitted* from a collection rather than replaced by a placeholder, because a placeholder still discloses existence and count.
2. `attributesOf` respects **per-attribute** Read; an attribute the principal may not read is left out and the object still renders.
3. Filtering is recursive, through collections, map keys, map values, arrays and object-valued attributes. The traversal mirrors `SearchExpression.filterSecurity`, down to returning the input unchanged when nothing had to be removed.

**What a refused identifier is told.** "Invalid TID format" is true of every wrong identifier and helps with none of them, so `TidDiagnostics` classifies the string the model wrote and names the remedy: a uuid copied out of a read answer, an object pasted in its printed form, a type name no table has, an abstract table, an id the engine's parser rejects, or none of those. An application adds the one class the framework cannot know - the code its own users speak, `LEAD-014` - as a pattern and a sentence in the `<object-ids>` section of `AgentApplicationService`; a configured hint is tried first. The classification runs on the error path only and asks the type repository about the type name, never the database about the object. Every refusing branch of `resolve` calls the same method with the same string, so an object the principal may not read and an object that does not exist are refused with the same sentence, character for character (`TestTidResolverDiagnostics.testAHiddenObjectIsRefusedLikeAMissingOne`).

### What it does not claim: aggregates

A count computed inside TL-Script is a number by the time it arrives, and no read filter can tell which objects it counted. Cardinality is accepted, not fixed; `TestReadFilter` states that boundary as a test so it is not mistaken for an oversight.

### What labels are

A **label** is not read-scoped either - and that is the engine's own position on what a label *is*:

```
TL-Script  toString($obj)   ->  ToString
           "" + $obj        ->  the same coercion
                            ->  SearchExpression.asString(value)
                            ->  (not a number, boolean or CharSequence)
                            ->  MetaLabelProvider.INSTANCE.getLabel(value)
                            ->  generic Java model access, NOT access-checked
                            ->  a String
```

Only TL-Script's **attribute** access (`AccessLike.lookupValue`) consults `ModelAccessRights`; label rendering deliberately does not. The engine states the rule in the JavaDoc of `FilterSecurity`: access operations in TL-Script return referenced objects unfiltered (consistent with the user interface, which always shows a referenced object by its label and only secures navigation into it); only the access to the *attributes* of an object is denied if the user must not read the object. So by the time the value reaches `ReadFilter` it is a `String`, and there is nothing left for a filter to recognise. Two consequences:

- **Object labels are non-secret by design.** A reference is rendered by its label in every table, tooltip and selector; what is secured is navigating into the object, not naming it.
- **The modeller carries the obligation.** An application must not encode confidential information in the name of an object. If a contract's *name* has to be secret, the secret belongs in an attribute with its own `<part>` grant - which *is* read-scoped - and the label must not repeat it.

A label discloses whatever the model's label provider renders. A type with a `name` attribute is labelled by its name; a type without one gets the generic *"Unnamed object of type Ticket"*, so coercing such objects discloses **how many** exist and nothing else - the cardinality boundary reached by a second route. The two accepted disclosures are one family: a script reduces an object to a plain value before any output filter runs, and an output filter cannot recover what the value was.

**The control is the caller's.** `filterSecurity(...)` - the engine's TL-Script function behind `SearchExpression.filterSecurity` - applies the current user's read permissions to a value, dropping the objects the user may not read, recursively through collections and maps. Applied *before* the coercion, there is no object left to label:

```
filterSecurity(all(`pm:Project`)).map(p -> toString($p))   // scoped to what the user may read
all(`pm:Project`).map(p -> toString($p))                   // every label, by design
```

The cheat sheet of the query language (`QueryLanguage`, which a failing query is answered with and which the skill `TL_writingDataChanges` carries) and that skill's security section say so. A script reaches a reader through `query` and `present`, which are in `data.read`: withholding `script` does not keep a count or a label from an account; only withholding `data.read` does.

## What the tools disclose

| Tool | Disclosure |
|---|---|
| `InstanceBrowserTool.listInstances` | Filters the instance list through `readable(...)` **before** the count is taken and before the page is cut, so `totalCount` is the number of instances the user may see. An unfiltered `totalCount` leaks cardinality; a page cut before the filter leaks it through short pages. |
| `InstanceBrowserTool.getInstance` | Resolves the TID through the read check, then serializes only the attributes `canRead(obj, attr)` allows. The container is another object's identity and is disclosed only when that object is readable too. |
| `QueryTool.query`, `ChangeDataTool.changeData` | The result is disclosed through `ReadFilter`: an unreadable object is left out, so a script whose whole result is one hidden object reports `null`. `changeData`'s report of what changed names an attribute the principal may not read without its values, and an object it may not read without its label. Inside TL-Script the engine enforces the model access rights itself; `changeData` and its dry run refuse a system context. |
| `ScriptToolExecutor` (every TL-Script tool) | The result is rendered through `ReadFilter.text(...)`. Object-typed *arguments* are read-checked where their TID is resolved, so a script body cannot be handed an object the principal may not read. |
| `ViewDocumentTool.viewDocument` | Uses the resolver's check, so "no such object" and "not yours" are one answer. |
| `VectorIndexSearchTool`, `index.search` | Filtered per hit at query time, see [result filtering](doc:ai/security/result-filtering#the-vector-index). |
| The MCP resources `InstanceResource`, `TypeInstancesResource`, `<dynamic-resources>` | Read as the authenticated account and disclosed through the same filter: "not found" for an unreadable object, unreadable attributes left out, listings filtered before they are counted and paginated. |
| `ModelSearchTool.searchModel`, `ModelBrowserTool.listModules` / `listTypes` / `listTypeParts` / `listClassifiers`, `TypeAnalysisTool.*`, the MCP model-metadata resources | Disclose model *structure* - types, attributes, labels - not business data, so the read filter has nothing to filter. `outlineModel`, which hands over the whole model in one call, is bounded by `accessibleTypes(READ)` ([the model digest](doc:ai/security/result-filtering#the-model-digest)); these tools enumerate the same information a type at a time and are not bounded by it. |
| `ModelEditorTool.*`, `ModelAnnotationEditorTool.*` | Structural: gated by the `model.structure` group at L1; L4 backstops the commit. |
| `McpTool` (remote tools), `ScriptToolExecutor` | Declare no group, so they are denied until an administrator classifies them in `<tool-groups>`. |

## How the two layers combine

```
        ┌───────────── L1 RBAC: may this PERSON use this TOOL? ─────────────┐
        │  role grants tool  AND  agent grants tool  AND  mode allows mode  │
        └───────────────────────────────┬───────────────────────────────────┘
                                        │ pass
        ┌───────────────────────────────▼───────────────────────────────────┐
        │  L2: may this PERSON perform this OPERATION on THIS OBJECT?       │
        │  ModelAccessRights.isAllowed(person, instance, commandGroup)      │
        │  = type/module/attribute grant  AND  role held on the instance    │
        └───────────────────────────────┬───────────────────────────────────┘
                                        │ pass, per object
        ┌───────────────────────────────▼───────────────────────────────────┐
        │  L3 pre-filter results  ·  L4 re-assert before commit             │
        └───────────────────────────────────────────────────────────────────┘
```

**The two layers ask about the same role at different objects, and that is what makes one role name do both jobs.** L1 reads roles **on the security root**, because a tool call has no object yet when the gate runs; L2 reads them **on the object being touched**. So a role has to be assigned *twice* to be useful: once on the root, by a `<singleton-rule>` - the *tier*; and once on business objects, by a role rule derived from the data - the *scope*.

Worked example: a clerk holding the role `SalesAssistantUser` asks "list all customers and raise their credit limit". `SalesAssistantUser` is assigned by two rules: a `<singleton-rule>` on `SecurityStructure#ROOT` for the members of the group `salesAssistants`, and a `<rule type="reference">` that derives it on each `Customer` of the clerk's own region.

1. **L1** - the clerk holds `SalesAssistantUser` on the security root through the tier rule, and the policy grants that role `data.read`. `listInstances` passes. `changeData` is in `script` (privileged, not granted) - denied, and the model is told so.
2. **L2** - `listInstances` calls `readable(...)`. `ModelAccessRights` finds the Read grant of `Customer` for `SalesAssistantUser` and evaluates the role *per instance*: through the scope rule the clerk holds it on their own region's customers only. Result: their region's customers.
3. **L3** - the filtered list is serialized; `totalCount` reports the filtered count.
4. **L4** - no write happened, nothing to guard.

**The scope assignment alone is not enough.** A role held only on regional customers is invisible to L1, which looks at the root and nowhere else, so step 1 would deny `listInstances` and step 2 would never run. The tier assignment makes L1 pass; the scope assignment makes L2 narrow. Neither substitutes for the other, and `use-default-security-parent` must stay `false`, or the tier assignment swallows the scope one. The [worked example](doc:ai/security/role-model-worked-example) builds exactly this, in full.

The clerk sees their customers and a clear denial for the write. Neither layer alone gets this right: RBAC alone would return every customer; per-object rights alone would let the clerk run arbitrary TL-Script and merely fail per object, leaking type structure and cardinality in the process.

At boot, `AiToolAccessService` warns about a `<tool-access>` role that no role rule can produce on the security root (`AccessManager.canHaveRole`), and about a role granted `data.read` or `data.write` that appears in no Read, Write or Create grant of `<security-config>` - an account with read tools that see nothing. These are warnings, not errors: a direct runtime assignment can still arrive, and model rights can come from a `<part>` grant or an annotation the check does not read.
