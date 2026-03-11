# TL-Script Editor with CodeMirror 6 — Design

**Ticket:** #29108
**Date:** 2026-03-11
**Status:** PoC exploration

## Goal

Replace the Ace-based TL-Script code editor with a CodeMirror 6-based React
form control that provides proper syntax highlighting (via a Lezer grammar),
model-aware completions, hover tooltips, and real-time diagnostics — all
communicating over the existing React control protocol (no new WebSockets or
transport layers).

## Architecture

```
+---------------------------------------------------+
|  Browser: CodeMirror 6 + TL-Script Extensions     |
|  - CM6 EditorView as React component              |
|  - Lezer grammar for TL-Script syntax highlighting|
|  - Completion extension -> @ReactCommand("complete"|
|  - Hover extension     -> @ReactCommand("hover")  |
|  - Lint extension      <- SSE PatchEvent diagnostics|
|  - valueChanged        -> @ReactCommand on blur    |
+---------------------------------------------------+
|  Transport: Existing React Control Protocol        |
|  - POST /react/command  (client -> server)         |
|  - SSE PatchEvent       (server -> client)         |
+---------------------------------------------------+
|  Server: TLScriptEditorReactControl                |
|  - Form field control (ReactControl subclass)      |
|  - @ReactCommand("complete") -> completion list    |
|  - @ReactCommand("hover")    -> type/doc info      |
|  - @ReactCommand("validate") -> diagnostics        |
|  - Reuses existing completion/parsing logic        |
+---------------------------------------------------+
```

### Why not LSP over WebSocket?

A TopLogic page may display many TL-Script editors simultaneously (e.g. in a
form with multiple script attributes). Full LSP would require per-editor
WebSocket connections or a multiplexing layer. Instead, each editor is a
ReactControl with its own control ID, sharing the existing SSE connection and
HTTP session. No additional transport overhead.

### Why CodeMirror 6 over Ace?

- Lezer parser gives tree-structured syntax highlighting (vs Ace's regex tokenizer)
- Modular extension system — completions, hover, lint are pluggable
- Modern, actively maintained, lightweight
- Better accessibility and mobile support

## Server-Side Design

### TLScriptEditorReactControl

A `ReactControl` that acts as a form field control. Receives a value callback
from its parent form element.

**State model** (JSON sent to client):

```json
{
  "value": "..tl-script text..",
  "readOnly": false,
  "diagnostics": [
    {"line": 2, "col": 5, "severity": "error", "message": "Unexpected token"}
  ]
}
```

**Commands:**

#### `@ReactCommand("complete")`

Receives:
```json
{
  "text": "full editor content",
  "pos": 42,
  "line": "current line text",
  "prefix": "typed prefix"
}
```

Returns completion list via response or state patch. Delegates to the existing
`TLScriptAutoCompletionCommand` logic — model-aware completions for modules
(backtick prefix), types, attributes, and functions (dot prefix).

#### `@ReactCommand("hover")`

Receives:
```json
{
  "text": "full editor content",
  "pos": 42,
  "token": "hovered token text"
}
```

Returns type information and documentation HTML. Reuses the same model
introspection infrastructure used by completions.

#### `@ReactCommand("validate")`

Receives:
```json
{
  "text": "full editor content"
}
```

Parses via `SearchExpressionParser`, collects parse errors, and pushes
diagnostics back via `PatchEvent`:

```json
{
  "diagnostics": [
    {"line": 1, "col": 10, "severity": "error", "message": "Expected ')'"}
  ]
}
```

Validation can also be triggered proactively on the server side (e.g. after
model changes) and pushed via SSE without a client request.

#### `@ReactCommand("valueChanged")`

Receives:
```json
{
  "value": "new tl-script text"
}
```

Updates the form field value via the registered callback. Triggered on blur
or debounced typing.

### Reuse of Existing Logic

The completion and parsing logic currently lives in `com.top_logic.model.search`,
coupled to the Ace-based `TLScriptCodeEditorControl`. The relevant logic needs
to be accessible from the new React control:

- **Completions:** `TLScriptAutoCompletionCommand` computes model-aware
  completions. The core logic (model element lookup, function matching) should
  be extractable into a shared utility.
- **Parsing:** `SearchExpressionParser` already produces parse errors with
  line/column information.
- **CodeCompletion DTO:** The existing `CodeCompletion` class (name, value,
  snippet, docHTML, score) maps directly to CM6's `Completion` interface.

## Client-Side Design

### TLScriptEditor React Component

Location: `com.top_logic.layout.react` module, TypeScript side.

A React component that:
1. Creates a CM6 `EditorView` on mount
2. Configures extensions for syntax highlighting, completion, hover, lint
3. Syncs value changes back to the server via `valueChanged` command
4. Reads `diagnostics` from React state (pushed via SSE) and maps to CM6 lint

Imports React from `tl-react-bridge` (never from `react` directly).

### Lezer Grammar for TL-Script

A `.grammar` file compiled at build time via `@lezer/generator`. Covers:

- **Literals:** strings (`"..."`, `'...'`), numbers, booleans, `null`
- **Model references:** `` `module:Type#attribute` ``
- **Operators:** arithmetic, comparison, logical, string concatenation
- **Keywords:** `if`, `else`, `true`, `false`, `null`, function names
- **Function calls:** `name(args)`
- **Block expressions:** `{ ... }`
- **Lambdas:** `x -> expr`
- **Comments:** `/* ... */`, `// ...`

The grammar enables tree-structured highlighting entirely client-side —
no server round-trip needed for syntax coloring.

### Completion Extension

Registers a CM6 `CompletionSource` that:
1. Detects trigger context (after `.`, after `` ` ``, on identifier typing)
2. Sends `complete` command to server with cursor position and context
3. Maps server response to CM6 `CompletionResult` with label, detail, info, type

### Hover Extension

Registers a CM6 `hoverTooltip` that:
1. On pointer hover, identifies the token under cursor
2. Sends `hover` command to server
3. Displays returned HTML as a tooltip DOM element

### Diagnostics (Lint) Extension

Uses CM6's `@codemirror/lint` extension:
1. Reads `diagnostics` array from React state
2. Maps each `{line, col, severity, message}` to a CM6 `Diagnostic`
3. Updates on every state patch from server

### Build Integration

CM6, Lezer, and the grammar compile via the existing `frontend-maven-plugin`
Vite build in `com.top_logic.layout.react`. New npm dependencies:

- `@codemirror/view`, `@codemirror/state` — core CM6
- `@codemirror/language` — language support infrastructure
- `@codemirror/autocomplete` — completion extension
- `@codemirror/lint` — diagnostics extension
- `@lezer/lr`, `@lezer/generator` — Lezer parser runtime and build tool

## Integration with Forms

The editor is a **form field control** — it does not own channels or
view-level plumbing. A `FormElement` (or existing form infrastructure) creates
a `TLScriptEditorReactControl`, wiring it to the appropriate model attribute.

- The editor receives the field's current value and editability state
- `valueChanged` updates the field value via callback
- Read-only mode disables editing and completions; syntax highlighting and
  hover remain active

## Scope

### In scope (PoC)

1. `TLScriptEditorReactControl` — Java ReactControl with `complete`, `hover`,
   `validate`, `valueChanged` commands
2. `TLScriptEditor.tsx` — CM6 React component in `com.top_logic.layout.react`
3. Lezer grammar for TL-Script syntax highlighting
4. Completion integration reusing existing completion logic
5. Real-time diagnostics via `SearchExpressionParser` + SSE push
6. Hover info for model references and functions

### Out of scope

- Go-to-definition, find-references, rename/refactor
- Signature help (parameter hints)
- Replacing existing Ace-based editors (coexistence, not migration)
- Multi-cursor, collaborative editing
