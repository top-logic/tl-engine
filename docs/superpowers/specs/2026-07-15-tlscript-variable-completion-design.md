# TL-Script editor: `$`-triggered variable completion

## Goal

Extend the code completion of `TLScriptCodeEditorControl` so that typing `$`
(and continuing to type an identifier) opens the completion popup listing the
variables that are in scope at the cursor position — analogous to how typing
`` ` `` already offers model elements and typing `.` offers functions.

## Background

The existing completion mechanism has three cooperating parts:

1. **Trigger** — `TLScriptCodeEditorControl.enableCustomEditorAutoComplete`
   passes a regex to the client (`tlscripteditor.js`). After every keystroke the
   client tests the line-prefix against this regex; on a match it triggers Ace's
   `startAutocomplete`. The regex is assembled in
   `createTLScriptAutoCompletionRegex()` from three branches: model-constant
   (`` ` ``), function (`.`), and default (word).

2. **Client → server request** — the Ace completer in `mode-tlscript.js`
   (`tlScriptCompleter.getCompletions`) sends a `tlScriptAutoCompletion` control
   command to the server carrying `prefix` and the current `line` (line text up
   to the cursor).

3. **Server computation** — `TLScriptAutoCompletionCommand.createCompletions`
   inspects the line, selects a mode (model-part / text / default-function), and
   returns a list of `CodeCompletion`s which are shipped back to the client via
   `services.tlscriptsearch.showCompletions`.

### How TL-Script introduces variables

The only construct that binds a variable is a **lambda**:

```
name -> body
```

`name` is bound inside `body` and referenced as `$name`. There are no
predefined/global variables; the control is a generic `Expr` editor. A variable
is therefore "in scope" at the cursor exactly when the cursor is inside the body
of the lambda that bound it.

The lexer (`SearchExpressionParser.jj`) tokenizes `$name` as a single `VAR`
token; a bare `$` is a lexical error. Whitespace and comments are `SKIP`, so the
token stream contains only meaningful tokens. The generated lexer classes
(`SearchExpressionParserTokenManager`, `SimpleCharStream`,
`SearchExpressionParserConstants`, `Token`) are checked into source and can be
reused.

## Requirements

- Typing `$` opens the popup; typing further identifier characters filters it.
- Only variables whose enclosing lambda body contains the cursor are offered
  (accurate lexical scope, including cross-line and nested/chained lambdas).
- At top level (no enclosing lambda) nothing is offered.
- No suggestions inside string literals or model constants.
- Filtering / ordering / rendering consistent with existing completion modes.

## Design

### 1. Trigger regex (`TLScriptCodeEditorControl`)

Add a fourth branch to `createTLScriptAutoCompletionRegex()` matching a trailing
`$` optionally followed by identifier characters at end of line, e.g. a
`createVariableExpression()` built with `TLRegexBuilder`. This only decides
*whether* to ask the server; the server does the authoritative mode detection.

### 2. Request payload (`mode-tlscript.js` + command)

Variable scope can span lines above the cursor, so the current `line`-only
payload is insufficient. Extend `tlScriptCompleter.getCompletions` to also send:

- `text` — the full editor value (`session.getValue()`), and
- `row` / `column` — the cursor position (`pos.row`, `pos.column`).

The model-part and function modes keep using `line`/`prefix` unchanged; only the
new variable mode consumes `text` + position. `TLScriptAutoCompletionCommand`
reads the new arguments.

### 3. Mode selection (`TLScriptAutoCompletionCommand.createCompletions`)

Add a variable-completion branch. It is selected when the line-prefix ends with
`$` + optional identifier and the cursor is not inside a string
(`inTextMode`) or model constant (`inTLModelPartCompletionMode`). It delegates
scope computation to the new analyzer, filters by the typed prefix (reusing the
existing `startsWith` / case-sensitivity logic), and builds `CodeCompletion`s:

- `name` / `value` = `$<varname>` (so Ace replaces the typed `$` prefix
  correctly — Ace's `identifierRegexps` includes `$`, so the prefix being
  replaced already contains the `$`),
- no `docHTML` (variables carry no documentation),
- ordered with the existing `CompletionByNameComparator`.

### 4. Scope analyzer (new class `TLScriptVariableScope`)

Location: `com.top_logic.model.search/.../ui/TLScriptVariableScope.java`.

Input: full text + cursor offset (offset computed from `row`/`column` + text).
Output: ordered set of in-scope variable names (without `$`).

Algorithm (single pass over the token stream, reusing the JavaCC lexer):

1. Compute the cursor offset. Tokenize the text region **before** the incomplete
   trailing `$…` being typed, so the lexer never sees a bare `$`.
2. Maintain a stack of "bracket frames"; start with one top-level frame. Each
   frame holds the variable names bound in its current comma-segment.
   - `(`, `[`, `{` → push a new frame.
   - `)`, `]`, `}` → pop the current frame.
   - `,` → clear the current frame's bindings (a comma terminates the current
     lambda body at this bracket level).
   - `NAME` immediately followed by `->` → bind `NAME` in the current frame.
3. The result is the **union of all frames still on the stack** at the cursor —
   i.e. every lambda whose body still encloses the cursor.
4. Wrap the scan in a try/catch on `TokenMgrError`; on a mid-edit lexical error,
   stop and return whatever was collected so far.

Why lexer-based and not AST-based: the compiled `Lambda` AST has no source
positions, and the partial expression at the cursor generally does not parse.
The token stream is the robust source of truth and tolerates unbalanced brackets
during editing.

#### Worked example

```
count(all(`M:T`), x -> $x.foo(y -> $‸))
```

- `count(` push f1; `all(` push f2; `` `M:T` `` type token; `)` pop f2.
- `,` clears f1 (nothing bound yet).
- `x ->` binds `x` in f1.
- `$x.foo(` push f2'; `y ->` binds `y` in f2'.
- Cursor `‸`: stack = [f1{x}, f2'{y}] → union `{x, y}` → offers `$x`, `$y`. ✅

At the top level after everything closes, the stack is empty → offers nothing. ✅

## Testing

JUnit test for `TLScriptVariableScope` covering:

- single lambda body, top level (empty), variable before its own `->` (empty).
- nested lambdas (the worked example) → both variables.
- chained `list.filter(x -> $x).map(y -> $‸)` → only `y` (no leakage across `.`).
- comma-separated args `foo(x -> $x, $‸)` → `x` not offered after the comma.
- multi-line: lambda opened on an earlier line, cursor on a later line.
- mid-edit unbalanced brackets → graceful partial result, no exception.
- prefix filtering (`$fo` narrows to matching names).

Manual verification with Playwright in `com.top_logic.demo` (expert search
editor) per the project's UI verification convention.

## Out of scope

- Predefined / context-provided variables (none exist for this control).
- Type-aware suggestions or variable documentation.
- Changes to the model-constant or function completion modes.
