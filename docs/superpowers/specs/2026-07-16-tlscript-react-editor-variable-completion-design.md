# Variable completion in the React (CodeMirror) TL-Script editor

## Goal

Give the CodeMirror-based React TL-Script editor (`TLScriptEditorReactControl`, used via the
`<tlscript-editor>` view element) the same `$`-variable code-completion the classic Ace editor has:
offer the in-scope variables (lambda parameters + local assignments) when typing `$`, plus optional
context variables declared on the editor element.

## Background

- The classic Ace editor (`TLScriptCodeEditorControl`) already offers `$`-variable completion,
  including context variables via the `@ScriptContextVariables` SPI, all computed server-side by
  `TLScriptCompletionService.computeCompletions(context, line, prefix, textToCursor, contextVariables, caseSensitive)`.
- The React editor is a separate stack:
  - Server: `com.top_logic.model.search.react.TLScriptEditorReactControl` (a `ReactControl`, NOT a
    form-field control — it holds a plain `String` value + a value callback, no `FormField`/`ValueModel`).
    Its `complete` command handler currently reads only `line` and `prefix` and calls the legacy
    `computeCompletions(null, line, prefix, false)` overload — so `textToCursor` is `null` and no
    context variables are passed. Result: variable completion never fires there.
  - View element: `com.top_logic.model.search.react.TLScriptEditorElement` (`<tlscript-editor>`) binds
    a plain `value` / `value-channel`, not an annotated config property. There is therefore no
    `@ScriptContextVariables` annotation to derive context variables from.
  - Client: TypeScript in `com.top_logic.model.search.react/react-src/` built with `vite` (run via
    `frontend-maven-plugin` during `mvn compile`) into `src/main/webapp/script/tl-script-editor.js`.
    The CodeMirror completion source sends `line` (current line prefix) and `prefix` (`[\w]+`, without
    `$`), and computes the completion `from` from a `[\w]+` match — so a leading `$` is not part of the
    replaced range.

## Requirements

- Typing `$` in the React editor offers the in-scope lambda parameters and local assignment variables
  (parity with the Ace editor's text-derived completion).
- Inserting a variable completion must not double the `$` (the server returns `$name` values).
- The `<tlscript-editor>` element may declare context variables (names) that are always offered at the
  top level; declaration is explicit (no annotated config property exists here).
- Existing model-element (`` ` ``) and function (`.`) completion in the React editor keep working.
- Server-side reuse: the shared `TLScriptCompletionService` is used unchanged (no per-client value
  variants).

## Design

### 1. Client — send text-to-cursor and make the range `$`-aware

In the React module's TypeScript completion source (`react-src/`, the file defining the CodeMirror
language/completion; built to `tl-script-editor.js` via `mvn compile` — do NOT edit the built JS):

- Compute the full text up to the cursor and send it: `textToCursor = context.state.sliceDoc(0, context.pos)`,
  added to the `complete` command arguments alongside the existing `line`/`prefix`/`requestId`.
- Change the `from`/`prefix` match from `/[\w]+/` to `/\$?[\w]*/` so an optional leading `$` is part of
  the matched range:
  - `$fo` → `from` at the `$`; the server value `$foo` replaces `$fo` → `$foo` (no doubling).
  - `fo` → `from` at `f`; a function value `foo` replaces `fo` → `foo` (unchanged behavior).
  - The `prefix` sent to the server then includes the `$` in variable mode; the server already strips a
    leading `$` (`matchingVariables`) and detects variable mode from the line, so this is compatible.
- The trigger predicate (`matchBefore(/[\w$`.:]+/)`) is unchanged.

### 2. Server — pass text-to-cursor and context variables

`TLScriptEditorReactControl`:
- Hold the declared context variables (`List<String> _contextVariables`, default empty), set from the
  element.
- In the `complete` handler, read `textToCursor` from the arguments and call the context-aware overload
  `TLScriptCompletionService.computeCompletions(null, line, prefix, textToCursor, _contextVariables, false)`.

### 3. Context variables — explicit declaration on the element

`TLScriptEditorElement.Config` gains a `context-variables` option: a list of variable names (e.g.
comma-separated, `String`-list format) the author declares for the surrounding infrastructure's
implicit bindings:

```xml
<tlscript-editor value="..." context-variables="topic, key, message" />
```

`TLScriptEditorElement.createControl` passes the configured names to `TLScriptEditorReactControl`
(`_contextVariables`). An empty/absent list means only text-derived variables are offered. These names
are unioned (always in scope, de-duplicated) with the text-derived variables by the existing service.

### Data flow

```
$ typed → CodeMirror completion source
  → complete command { line, prefix (incl. optional $), textToCursor, requestId }
  → TLScriptEditorReactControl.complete
  → TLScriptCompletionService.computeCompletions(null, line, prefix, textToCursor, _contextVariables, false)
  → variables (text-scope ∪ declared context vars), $-prefixed
  → SSE state patch → CodeMirror applies with $-aware `from`
```

## Testing

- **Server:** a unit test exercising the completion path used by the React handler — that
  `TLScriptCompletionService.computeCompletions(null, line, prefix, textToCursor, contextVariables, false)`
  yields the in-scope variables for a `$`-ending line and includes the declared context variables. (The
  core service is already unit-tested; this pins the overload/arguments the React handler relies on.)
- **Client:** the TypeScript completion source has no unit-test harness in this module; verify via
  manual Playwright testing.
- **Manual (Playwright):** in `com.top_logic.demo.react`, a `<tlscript-editor>` with a lambda
  (`x -> $…` offers `$x`) and with `context-variables` declared (those names offered at top level);
  confirm no `$$` doubling and that `` ` `` / `.` completion still work.

## Out of scope

- Annotation-based (`@ScriptContextVariables`) context-variable derivation in the React editor — not
  applicable, as `<tlscript-editor>` is not bound to an annotated config property; context variables are
  declared explicitly on the element instead.
- Case-sensitive completion in the React editor (kept `false`, unchanged).
