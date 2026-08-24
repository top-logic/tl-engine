# Design: Generic form element for editing configurations

**Date:** 2026-08-12
**Ticket:** #29462
**Modules:** `com.top_logic.layout.configedit` (core), `com.top_logic.layout.view` (entry points),
`com.top_logic.dev.tools` (first use)

## Problem

A form in the React view layer can only display **model attributes**: `<field attribute="…"/>` binds
through `AttributeFieldControl` to an attribute of the input object, and `FieldControlService` picks
the input control from the `TLStructuredTypePart` and its `TLPrimitive.Kind`.

Much of what makes up a TopLogic application is not a model attribute but a `ConfigurationItem`. The
immediate occasion: what a model element actually says lives in its **annotations** —
`tl.model:TLModelPart#annotations` is a multi-valued attribute of datatype `tl.model:TLAnnotation`
whose values are polymorphic configurations. That is why the React model editor
(`admin/model/model-editor.view.xml` in `tl-dev-tools`) shows nothing but the name in its detail
forms.

### What already exists

The module `com.top_logic.layout.configedit` (created under #29108) is the React counterpart of
`EditorFactory` and already covers the **structure**:

| Class | What it does |
|---|---|
| `ConfigFieldModel` | `AbstractFieldModel` over (`ConfigurationItem`, `PropertyDescriptor`); writes through `config.update(…)`, listens back as a `ConfigurationListener` |
| `ConfigEditorControl` | Builds the form over the properties: `PLAIN`/`REF` as a field with chrome, `ITEM`/`LIST` as collapsible groups; honours `@Hidden` and `@TreeProperty` |
| `ConfigListEditorControl` | List with add, remove, move up/down, and a type selector per entry |
| `PolymorphicItemControl`, `PolymorphicOptions` | Implementation choice with a dependent sub-form |
| `ConfigSelectFieldModel` | Select field model over a configuration property |
| `ConfigFieldDispatch` | Maps a value type to an input control |

Its users are `com.top_logic.layout.view.designer.ConfigEditorElement` (the view designer) and
`com.top_logic.demo.react.view.DemoConfigEditorElement`.

### What is missing

1. **No general entry point.** `ConfigEditorElement` is tied to the view designer's
   `DesignTreeNode`. There is neither an element for an arbitrary configuration taken from a channel
   nor a field control that embeds the editor for a **configuration-valued model attribute** — which
   is exactly what `annotations` needs.
2. **The value-to-control mapping is thin.** `ConfigFieldDispatch` knows `boolean` → checkbox,
   integral and floating-point numbers → number input, enum → select, and **everything else** →
   text input. A `Date`, `ResKey`, `ThemeImage`, `Class` or `TLModelPartRef` property therefore ends
   up in a text field whose input is written back as a raw `String` into a typed property. The
   property's `ConfigurationValueProvider` is used nowhere, and neither are `@Options` option lists —
   only enums get a select.
3. **Property kinds are missing:** `MAP`, `ARRAY` and `COMPLEX` are skipped.
4. **No edit mode.** `ConfigFieldModel` writes every input straight into the item; there is no
   working copy and no connection to the edit/apply/cancel cycle of a `<form>`.
5. **No validation** of the configuration's constraints on apply (mandatory properties are only
   marked as such).
6. **Nothing annotation-specific:** neither the annotation types admissible for a given model
   element nor writing the list back into `tl.model:TLModelPart#annotations`.

This ticket is therefore an **extension, not a new build**. Item 2 is the bulk of the work, not the
structure.

## Goal

A form element that makes an arbitrary `ConfigurationItem` fully editable, with the model editor as
its first use.

## Non-goals

- Unifying control selection with `FieldControlService`. The duplication between "model attribute →
  input control" and "configuration property → input control" has existed in the classic UI for
  years (`FieldProvider`/`AttributeFormFactory` versus `EditorFactory`/`Editor`) and is deliberately
  reproduced here. The two sides only resemble each other in the middle: the attribute side has
  references to persistent objects, access rights and annotations on the type; the configuration
  side has polymorphic implementation choice, nested items, lists and maps.
- `DERIVED` properties. They stay skipped as they are today.
- Changes to the view designer. It keeps using the editor in its write-through mode.

## Design

### 1. Value-to-control mapping: `ConfigFieldDispatch` becomes a service

The fixed `if` chain becomes a configurable service modelled on `FieldControlService`. It has to be
configurable because the TL-Script editor lives in `tl-model-search-react`, above `configedit`, and
must be able to contribute its control from the outside.

Decision order:

1. **An annotation on the property** that names the input control explicitly. This needs a new,
   React-specific annotation in `configedit` — the existing `@PropertyEditor` and `@ControlProvider`
   (in `com.top_logic.layout.form.values.edit.annotation`) name editors and control providers of the
   classic UI and cannot be used here. `TLInputControl` on the attribute side is the model for its
   shape and naming.
2. **A property with an option list** → select control. The options come from
   `Fields.optionProvider(DeclarativeFormOptions)`, which already resolves `@Options` including its
   argument references (`fun`, `mapping`, `args`) as well as the polymorphic implementation lists —
   so not just enums as today.
3. **A configured mapping by Java value type**, taken from the service configuration and extensible
   from the outside.
4. **The built-in fallback by value type.**

The core of the repair is in step 4: when a property has a `ConfigurationValueProvider`, that
provider is used to **parse and format** the value instead of assigning a raw `String`. Invalid input
becomes a field error through the existing input-error mechanism, just as it does for model
attributes. On top of that:

- `Date` values → the date/time picker; which flavour (date, time, timestamp) follows from the
  property's format
- `ResKey` → the I18N control
- `Class` and instance-valued properties → the implementation select (as `PolymorphicOptions`
  already does for items)
- properties annotated `@Encrypted` → a password field

> **How this turned out in implementation** (ticket #29462, commits `1b2067820b`..`b05181761a`).
> Three of the four points above survived as written; the design of the chain changed, and one point
> did not survive at all.
>
> **The ordering follows the classic form's veto, not the type.** `ValueEditor.addField` asks first
> whether a property is *specialized* — it has options, an explicit `@Format`, or a value binding —
> and only an unspecialized property reaches the convenient type branches. `ConfigControlService`
> now mirrors that, with `@Encrypted` as a fourth specialization, because its password control is a
> text control. The consequence is deliberate: a `Date` with its own format is edited as that
> format's text, never in a picker that could not show the value. This also removes the need to
> derive a date sub-kind from the format, which an earlier attempt did by inspecting the value
> provider's class — a distinction the classic config layer never made either, since a formatted
> value has always gone to `PlainEditor`'s text field there.
>
> **`ResKey` does not reach the I18N control, and cannot without changing the chain.**
> `ReactI18NStringInputControl` is on this module's classpath, so the module boundary is not the
> obstacle. The obstacle is the value domain: `createModel` wraps a `ResKey` property in
> `ConfigFormatFieldModel`, which hands out the format's text, while that control expects the raw
> `ResKey`. Wiring it would require `createModel` to consult the resolution chain *before* choosing
> the domain, which is a change to the chain itself. `ResKey` therefore stays on the format text
> path — correct, not pretty. It is moot for the view designer today in any case: `ConfigEditorControl`
> skips `PropertyKind.COMPLEX`, which is what a `ResKey` property is, so it never reaches the service
> (see section 2).
>
> **A known format can claim its own control** — the point that replaced the date sub-kind
> derivation. The service's configured provider map gained a second key, the
> `ConfigurationValueProvider` class, consulted before the generic format text field: two properties
> of the same Java type can carry different providers, so only the provider's class distinguishes a
> plain date from a time of day. `TimeOfDayAsDateValueProvider` is registered onto a time input, and
> the same mechanism is how a module above this one contributes a control. A claimed property gets
> the plain, typed model by construction, since the claimed control edits the typed value; options
> and `@Encrypted` both outrank a claim.
>
> **Enum option labels changed.** The retired `ConfigFieldDispatch` resolved them from a resource key
> with an `@<constantName>` suffix; the service uses `MetaLabelProvider`, keeping it free of a
> `Resources` dependency. Accepted knowingly.

### 2. Missing property kinds

`ConfigEditorControl.isSupportedKind` admits `PLAIN`, `REF`, `ITEM` and `LIST`; everything else is
skipped and never appears in the form.

- `ARRAY` — the same list editor as `LIST`. The one real difference is the value: an array, not a
  `List`, so the editor has to read and write both shapes.
- `COMPLEX` — the value through its `ConfigurationValueProvider`, i.e. the same format field as in
  (1). `ConfigControlService` already accepts a `COMPLEX` property that has a value provider and
  rejects one that has only a binding; only the editor's own filter keeps such a property out. This
  is what a `ResKey` property needs (see the note in section 1).
- `MAP` — entries as groups, **created inline** rather than in a dialog.

**Why the classic side uses a dialog, and what inline has to solve instead.** A map in
TypedConfiguration is keyed the same way a keyed list is: by a property of the entry itself, as in
this ticket's own `@Key(ProviderMapping.TYPE) Map<Class<?>, ProviderMapping> getProviders()`. An
entry without a valid, unique key therefore cannot exist in the collection at all — which is why
`ListEditor` opens an `AddDialog` and `MapFormGroupBuilder` a `MapEntryBuilderDialog`: the dialog is
where the key is obtained *before* insertion. The classic editor then renders the key field
immutable (`EditorFactory.initEditorGroup`, and again in `MapFormGroupBuilder.createKeyField`), so a
key is assigned exactly once.

Inline creation keeps that guarantee but moves the moment: the "+" produces a **pending entry** that
lives in the control, not yet in the configuration. Its key field is editable and its other fields
are shown like any entry's. As soon as the key is non-empty and unique, the entry is inserted into
the configuration and its key field turns immutable, exactly as a classic entry's. An empty or
duplicate key leaves the entry pending and shows a field error on the key field, through the
mechanism section 1 established.

This is deliberately **one** mechanism for two cases: a keyed `LIST` has the identical problem, and
ticket #29462 currently only guards against it — the "+" refuses a second entry while an existing
one has no key (`ConfigListEditorControl.checkKeyAvailable`). The pending entry replaces that guard
rather than sitting beside it.

It is also a small anticipation of section 3: holding a half-finished value outside the configuration
until it is valid is the same question a working copy answers, only scoped to one entry.

### 3. Edit mode, working copy, validation

The working copy becomes a **mode of the editor, not a requirement** — the view designer saves the
view as a whole and keeps needing write-through.

- Entering edit mode creates a copy (`TypedConfiguration.copy`) that all fields work on.
- Apply checks mandatory properties and the configuration's constraints. Violations appear at the
  offending fields and edit mode stays open. Only then does the copy reach its recipient.
- Cancel discards the copy.

### 4. Entry points

- **`<config-form>`** as a `UIElement` in `com.top_logic.layout.view`: its input channel carries a
  `ConfigurationItem`, and edit mode is optional as with `<form withEditMode="true">`. This also
  reaches configurations that hang off no model attribute at all.
- **`ConfigFieldControlProvider implements ReactFieldControlProvider`**, registered in
  `FieldControlService` for configuration-valued types: the item editor for a single-valued
  attribute, the list editor for a multi-valued one. An ordinary `<form>` thereby carries a `<field
  attribute="annotations"/>` without knowing about the configuration editor.

### 5. First use: annotations in the model editor

- Register the provider for `tl.model:TLAnnotation`.
- **Admissible annotation types per element:** the counterpart of `PartAnnotationOptions`, filtered
  by the kind of element (module, class, enumeration, property, reference, classifier) and by
  `@TargetType`. The list's "+" offers only those.
- Add `<field attribute="annotations"/>` to the model editor's detail forms, for the module and for
  the selected part.
- Writing back happens inside the transaction the form already opens on save.

### Presentation

Inline collapsible groups — the presentation `ConfigEditorControl` and `ConfigListEditorControl`
already produce:

```
Details ─────────────────────────────────
Name        [ name                       ]

Annotations                         [ + ]
┌────────────────────────────────────────┐
│ ▾ Visibility                     [ x ] │
│     Value      [ hidden         ▾ ]    │
├────────────────────────────────────────┤
│ ▾ Storage                        [ x ] │
│     Implementation  [ InlineSet.. ▾ ]  │
│     Table      [ hasWrapperAttValue ]  │
│     Column     [ value            ]    │
├────────────────────────────────────────┤
│ ▸ Default value                  [ x ] │
└────────────────────────────────────────┘
```

## Outcome of section 1

Section 1 is implemented and verified in the running application (view designer of
`com.top_logic.demo.react`). What the plan set out to fix is fixed: an ill-formed entry produces an
error **at the field** instead of a raw `String` written into a typed property, values display in
their formatted form, and a property with an option list is edited by selecting rather than by typing.

Two things the verification turned up that the design did not foresee:

- **A number reached the property as a `Double`.** `ReactNumberInputControl` always parses into a
  `Double`, so an `int` property rejected every edit. Not a regression — the retired
  `ConfigFieldDispatch` routed `int` to the same control — but it made the section's promise hollow
  for integral properties. Fixed on the configuration side, in `ConfigFieldModel`, which knows the
  property's exact type; a fractional value for an integral property is rejected with a field error
  rather than truncated.
- **Enum option labels changed**, see the note above.

Still open for later sections, and now known rather than assumed: a `ResKey` property never reaches
the service because `ConfigEditorControl` skips `PropertyKind.COMPLEX` (section 2), and the view
designer rewrites the whole `view.xml` on apply, stripping XML comments — unrelated to this ticket
but worth its own.

## Testing

- **Server tests** in the existing package `test.com.top_logic.layout.configedit`: one synthetic
  configuration interface per property kind; format parsing including the failure case; working copy
  and cancel; option resolution through `@Options`.
- **Manual verification** with Playwright in `com.top_logic.demo.react`. That application depends on
  `tl-dev-tools`, so the model editor is reachable there.

## Order of work

1. Value-to-control mapping as a service, with `ConfigurationValueProvider` and option resolution (1)
2. The missing property kinds (2)
3. Edit mode with working copy and validation (3)
4. Entry points: `<config-form>` and the field control (4)
5. Annotations in the model editor (5)

Step 1 is useful on its own: it also fixes that typed properties end up in a text field in the view
designer today, and are written back from there as a raw string.
