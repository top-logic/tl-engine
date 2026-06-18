# Extensible Column Providers + Data-Backed Demo — Plan

**Status:** Draft for discussion · **Ticket:** #29108 · **Date:** 2026-06-18

Addresses two pieces of review feedback on the green-field `<table-view>`:

1. The demo (`tl.accounts:Person`) only exercises **string** and **boolean** values — not enough to
   validate the type-aware machinery. Replace it with a **data-backed `DemoTypes:A`** that has every
   value kind (string, number, date, boolean, enumeration, reference).
2. `TableViewElement.buildColumn` selects a column's filter/comparator with an
   **`instanceof TLEnumeration` / `instanceof TLPrimitive` + `switch(Kind)` cascade**. That is a
   closed dispatch — wrong for a model-based system where new datatypes can be added at any level.
   It must become an **extensible, model-driven registry**.

---

## 1. Motivation — why the cascade is wrong

The current dispatch (in `TableViewElement`) hardcodes, in Java, every datatype the table knows how
to filter. A new primitive datatype, a custom `StorageMapping`, or an app-specific type cannot get a
sensible filter without editing core view code. TopLogic is model-driven: the mapping from a model
attribute to a UI affordance must be data/config-driven and overridable, not a Java type switch.

## 2. The idiomatic pattern to mirror: `FieldControlService`

`com.top_logic.layout.view.form.FieldControlService` already solves the exact analog for **form
input controls**: given a `TLStructuredTypePart`, pick the right input control. Its shape:

- A **TypedConfiguration service** with a `Config` whose `@Key(...)`-indexed map binds a
  `TLModelPartRef` (type) → a provider. Apps add bindings declaratively; **no core edit**.
- **3-tier resolution** (`FieldControlService.java:143-164`):
  1. **attribute-level annotation** (`@TLInputControl`, a `TLAttributeAnnotation` + `TLTypeAnnotation`
     with `@DefaultStrategy(VALUE_TYPE)`) — explicit per-attribute / per-type override;
  2. **configured provider** for the attribute's `TLType` (registry lookup by qualified name);
  3. **built-in fallback** — a `TLPrimitive.Kind` switch as the *last resort only*.

Key realization: tier 3 is still a `Kind` switch. The model itself bottoms out in primitive kinds,
so a small built-in switch is fine **as the default behind the registry** — what's wrong is the
switch being the *only* mechanism. We adopt the same structure.

## 3. Proposed design — `ColumnProvider` registry

A new model-aware service (working name `ColumnProvider` / `TableColumnService`), living **next to
`FieldControlService` in `com.top_logic.layout.view.form`** (model-coupled view layer; the core
`com.top_logic.table` package stays model-neutral — unchanged).

```
ColumnProvider.buildColumn(TLStructuredTypePart part) : Column<Object,?>
```

resolves the column's **filter + comparator** (the type-dependent parts) via:

1. **Attribute / type annotation** — a new `@TLColumnFilter` (`TLAttributeAnnotation` +
   `TLTypeAnnotation`, `@DefaultStrategy(VALUE_TYPE)`) naming a `ColumnFilterFactory`. Explicit
   override on an attribute or on a whole type. *(Optional in step 1 — see decision B.)*
2. **Configured registry** keyed by the attribute's `TLType` (qualified name), mirroring
   `FieldControlService.Config`. App modules register filters for their own types here.
3. **Built-in fallback** — the existing kind logic (enumeration → options, INT/FLOAT → range,
   DATE → range, BOOLEAN/TRISTATE → boolean, STRING → text, else label-text), moved out of
   `TableViewElement` into the service's default provider. This is the only place a kind switch
   remains, and it is overridable by (1) and (2).

The cell **renderer** stays uniform (`MetaLabelProvider` → localized label), and the **accessor** is
the tolerant typed reader already in place; the provider only supplies the type-specific
**filter + comparator** (decision A).

`TableViewElement.buildColumns` shrinks to: for each part (configured or type-derived), call
`ColumnProvider`. The boolean-label fix (filter options labelled as the cells render them) moves
into the built-in boolean provider.

### Layering / boundary
- `com.top_logic.table.*` (core, model-neutral): **unchanged** — still pure `Column`/`ColumnFilter`.
- `com.top_logic.layout.view.form.ColumnProvider`: the model→column binding (uses `TLType`,
  `TLPrimitive`, `MetaLabelProvider`, the filter library). Sibling to `FieldControlService`.
- `TableViewElement`: delegates to `ColumnProvider`; no type logic of its own.

### Future tie-in (not step 1)
The filter's *value input* widget (number spinner, date picker — §13.C) can later be sourced from
`FieldControlService`, so the filter editor reuses the same datatype→control registry as forms.

## 4. Open decisions (to confirm before implementing)

- **A. Provider output.** Provider yields **filter + comparator** only (renderer/accessor stay
  uniform)? *(Recommended.)* Or the whole `Column` (max flexibility, more surface)?
- **B. Annotation now or later.** Ship **registry + built-in fallback** first (removes the cascade,
  makes it type-extensible), and add the `@TLColumnFilter` per-attribute annotation as a follow-up?
  *(Recommended.)* Or include the annotation in step 1?
- **C. Naming / home.** `ColumnProvider` in `com.top_logic.layout.view.form` beside
  `FieldControlService` — agreed?
- **D. "No instanceof at all"?** Confirm the goal is *extensible registry with kind-switch as the
  overridable default* (matching `FieldControlService`), not literally zero type inspection (the
  model bottoms out in primitive kinds, so a default switch is unavoidable somewhere).

## 5. Data-backed `DemoTypes:A` demo (the test bed)

- Add `com.top_logic.demo/.../WEB-INF/data/W99_DemoTypes_A.objects.xml` with ~8 `DemoTypes:A`
  instances spread across: `name` (string), `classificationSingle` (enum green/yellow/red),
  `boolean`, `long`/`float` (numbers), `date`. Verify the classifier-reference format against an
  existing `objects.xml`.
- Point `table-view-demo.view.xml` at `DemoTypes:A` with those columns (and the detail form).
- **Requires a `tmp/` wipe** — initial data only imports on fresh DB init
  (`InitialDataSetupService`). This destroys the current demo DB + personalization (acceptable for a
  demo). Call this out and get the go-ahead before wiping.
- Live-verify every filter kind: text, options (enum facets), range (number), date range, boolean —
  the coverage we couldn't reach with `Person`.

## 6. Migration steps

1. ~~Seed `DemoTypes:A` data + switch the demo view~~ — **DONE (2026-06-18), via in-app
   create/delete/generate instead of seeded data (no `tmp/` wipe).** The demo now targets
   `DemoTypes:A` with `observed-types` auto-refresh and "Generate samples" / New / Delete commands;
   live-verified: generate → 8 rows; options filter (Grün 3 / Gelb 3 / Rot 2) → Grün → 3 rows;
   numeric-range editor; boolean filter labelled Ja/Nein.
2. **NEXT:** Introduce `ColumnProvider` (built-in providers = the current kind logic, incl. boolean
   labels + enum options + numeric/date ranges) and its TypedConfiguration `Config`.
3. Replace `TableViewElement`'s cascade with a `ColumnProvider` call.
4. Live-verify all filter kinds on the `DemoTypes:A` demo; add unit coverage for the registry
   resolution (type-config → fallback).
5. *(Follow-up)* `@TLColumnFilter` annotation; filter value widgets via `FieldControlService`.

**Decisions confirmed (2026-06-18):** A = provider yields filter + comparator; B = registry +
built-in fallback first, annotation later; D = extensible registry with kind-switch as overridable
default. Per-attribute create handles mandatory fields (name, booleanMandatory, booleanRadioMandatory,
booleanSelectMandatory).

## 7. Out of scope
Query pushdown (#29341); legacy `TableModel` adapter; inline editing; live-data refresh.
