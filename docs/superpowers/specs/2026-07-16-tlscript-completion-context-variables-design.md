# TL-Script completion: context (top-level) variables

## Goal

Let a TL-Script editor offer additional variables at the top level of `$`-completion — variables that are not written in the editor text but are woven into the script's context by the surrounding infrastructure.

The motivating case is `ServiceMethodBuilderByExpression`: at runtime the configured `Config.getOperation()` expression is wrapped with the OpenAPI operation's parameters (`Define.create(param, expr)` per parameter → `param1 -> … -> paramN -> <operation>`), so those parameters are top-level `$param` variables when the script runs. They should therefore be offered by completion while editing the operation, even though they never appear in the edited text.

## Background

- The `Expr` type carries `@ControlProvider(TLScriptCodeEditorControl.PlainCP.class)`, so every `Expr` field is edited with the TL-Script code editor including completion — independent of any `@PropertyEditor` override (e.g. `getOperation()` uses `@PropertyEditor(PlainEditor.class)` only to skip the `ModelReferenceChecker` validation; completion is still active).
- The `$`-variable completion (ticket #29389) computes in-scope variables from the editor text via `TLScriptVariableScope.inScopeVariables(textToCursor)`, dispatched by `TLScriptAutoCompletionCommand` → `TLScriptCompletionService.computeCompletions(...)`.
- The command runs server-side with the `TLScriptCodeEditorControl` (and thus its `FormField`) in hand.
- `EditorFactory.getValueModel(FormMember)` is public and returns the field's `ValueModel`; `ValueModel.getModel()` is the edited `ConfigurationItem` and `ValueModel.getProperty()` its `PropertyDescriptor`. This is the link from a field back to its configuration and annotations.
- TopLogic option providers (e.g. `@Options(fun=…)`) read the surrounding configuration via `DeclarativeFormOptions`; this design uses the simpler `ValueModel` link, available at completion time.

## Requirements

- A generic, reusable way for any `Expr` property to declare context variables that are always in scope for completion.
- OpenAPI's `getOperation()` uses it to expose the operation's parameters.
- Context variables are offered top-level (and everywhere), filtered by the typed prefix like ordinary `$`-variables, and de-duplicated against text-derived variables of the same name.
- Purely server-side: no client/protocol change.
- Completion must never break: any failure to compute context variables degrades to "no context variables".
- No change to validation; the `PlainEditor` workaround stays as-is (out of scope).

## Design

### 1. Generic SPI (`com.top_logic.model.search.ui`)

- **Provider interface** `ScriptContextVariables`:
  ```java
  List<String> getVariables(ValueModel valueModel);
  ```
  Given the edited config item and property (via `ValueModel`), returns the variable names (without `$`) that are in scope for the whole script. Implementations are stateless and have a public no-arg constructor.

- **Annotation** `@ScriptContextVariables` (target: method; retention: runtime) on an `Expr` getter:
  ```java
  @ScriptContextVariables(OperationParameterVariables.class)
  Expr getOperation();
  ```
  Its value is the `Class<? extends ScriptContextVariables>` provider.

### 2. Plumbing (editor-independent, server-side)

- `TLScriptCodeEditorControl` gains `List<String> getContextVariableNames()`:
  1. `ValueModel vm = EditorFactory.getValueModel(getFieldModel())` — may be `null` (field not built by the declarative form, e.g. the expert-search editor).
  2. If `vm != null`, read `@ScriptContextVariables` from `vm.getProperty()`.
  3. If present, instantiate the provider (cached per provider class) and call `getVariables(vm)`.
  4. Return the result, or an empty list on `null`/missing annotation/exception (exceptions are logged, not propagated).
  Computed fresh per call so newly added sibling parameters appear immediately.

- `TLScriptAutoCompletionCommand.execute` reads `scriptCodeControl.getContextVariableNames()` and passes it to the service.

### 3. Completion integration (`TLScriptCompletionService` / `TLScriptVariableScope`)

- Add a `computeCompletions(context, line, prefix, textToCursor, contextVariables, caseSensitive)` overload; the existing overloads delegate with `contextVariables = Collections.emptyList()`.
- In the variable-completion branch, the candidate set is `TLScriptVariableScope.inScopeVariables(textToCursor)` unioned with `contextVariables` (context variables are always in scope). Union is de-duplicated (a text lambda param and a context variable of the same name collapse to one). Prefix filtering, `$`-prefixing, ordering and scoring are unchanged.
- `matchingVariables(textToCursor, prefix, caseSensitive)` grows a context-variables parameter (or a sibling helper) so the union happens before prefix filtering.

### 4. OpenAPI provider (`com.top_logic.service.openapi.server`)

- `OperationParameterVariables implements ScriptContextVariables`: from `valueModel.getModel()` (the operation config) collect the operation's own parameters plus the enclosing path-item and global parameters — navigating the config `container()` chain, mirroring `OpenApiServer.buildHandlers` — and flat-map `ConcreteRequestParameter.getScriptParameterNames()`.
- Annotate `ServiceMethodBuilderByExpression.Config.getOperation()` with `@ScriptContextVariables(OperationParameterVariables.class)`.
- If the container chain is not fully available at edit time, the provider returns what it can (at least the operation's own declared parameters); it never throws.

## Testing

- `model.search` unit test: given `textToCursor` and a `contextVariables` list, `TLScriptCompletionService`/`TLScriptVariableScope` union yields the context variables top-level, keeps them in nested scopes, filters by prefix, and de-duplicates a same-named lambda parameter.
- OpenAPI unit test: an operation `Config` with declared parameters (and, where feasible, path/global parameters) yields the expected variable names from `OperationParameterVariables`.
- Manual (Playwright): in the OpenAPI operation editor, typing `$` offers the operation's parameters at the top level.

## Out of scope

- Validation of the operation expression / replacing the `PlainEditor` with a context-aware `TLScriptPropertyEditor`.
- Client-side / CodeMirror (`TLScriptEditorReactControl`) support — the legacy `computeCompletions` overload keeps `contextVariables` empty there.
- Type information or documentation for context variables.
