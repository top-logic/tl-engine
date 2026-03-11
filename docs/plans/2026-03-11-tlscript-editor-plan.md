# TL-Script CodeMirror 6 Editor Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a CodeMirror 6-based TL-Script editor as a React form control, with server-side completions, hover info, and real-time diagnostics over the existing React control protocol.

**Architecture:** A `TLScriptEditorReactControl` (Java) communicates with a `TLScriptEditor` React component (TypeScript) via `@ReactCommand` handlers and SSE state patches. A Lezer grammar provides client-side syntax highlighting. Completion logic is extracted from the existing `TLScriptAutoCompletionCommand` into a reusable service.

**Tech Stack:** Java 17, CodeMirror 6, Lezer, TypeScript, Vite, React 19 (via tl-react-bridge)

**Design doc:** `docs/plans/2026-03-11-tlscript-editor-design.md`

---

## File Structure

### New Files

| File | Responsibility |
|------|----------------|
| `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCompletionService.java` | Extracted completion logic (shared by Ace + CM6 controls) |
| `com.top_logic.model.search.react/pom.xml` | New bridge module POM |
| `com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorReactControl.java` | Server-side ReactControl with complete/hover/validate commands |
| `com.top_logic.layout.react/react-src/controls/TLScriptEditor.tsx` | CM6 React component |
| `com.top_logic.layout.react/react-src/lang/tlscript.grammar` | Lezer grammar for TL-Script |
| `com.top_logic.layout.react/react-src/lang/tlscript-highlight.ts` | Lezer highlight style tags |
| `com.top_logic.layout.react/react-src/lang/tlscript-lang.ts` | CM6 language support (wraps Lezer grammar + completions + hover + lint) |

### Modified Files

| File | Change |
|------|--------|
| `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java` | Delegate to `TLScriptCompletionService` |
| `com.top_logic.layout.react/react-src/controls-entry.ts` | Register `TLScriptEditor` component |
| `com.top_logic.layout.react/package.json` | Add CM6 + Lezer dependencies |
| `com.top_logic.layout.react/vite.config.controls.ts` | Add `@lezer/generator/rollup` plugin for grammar compilation |
| `tl-parent-engine/pom.xml` | Register new `com.top_logic.model.search.react` module |

### Key Architectural Decisions

**Completion request/response pattern:** CM6's `CompletionSource` is an `async` function
that must **return** a `CompletionResult`. The existing `useTLCommand()` hook is
fire-and-forget (`Promise<void>` — the HTTP response is always `{"success":true}`).
Therefore, completions use a **promise-over-SSE** pattern: the client sends a `complete`
command, the server computes completions and pushes them via SSE `PatchEvent` to
`state.completions`, and the client resolves a pending Promise when the `completions`
state key changes. A per-request ID ensures correct correlation.

**Module structure:** The Java-side `TLScriptEditorReactControl` lives in a new bridge
module `com.top_logic.model.search.react` (artifact `tl-model-search-react`) that depends
on both `tl-model-search` (for parser, completions) and `tl-layout-react` (for
`ReactControl` base class). This keeps `com.top_logic.model.search` free of React
dependencies and `com.top_logic.layout.react` free of TL-Script dependencies.

---

## Chunk 1: Extract Completion Service

### Task 1: Create TLScriptCompletionService

Extract reusable completion logic from `TLScriptAutoCompletionCommand` into a standalone service class that both the legacy Ace control and the new CM6 control can use.

**Files:**
- Create: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCompletionService.java`
- Modify: `com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java`

- [ ] **Step 1: Write the TLScriptCompletionService class**

Create a new utility class with static methods that encapsulate the completion logic currently in `TLScriptAutoCompletionCommand`. The key change is that `DisplayContext` is passed as a parameter (needed by `SearchBuilder.getDocumentation()`).

```java
package com.top_logic.model.search.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.SearchExpressionFactory;

/**
 * Reusable service for computing TL-Script code completions.
 *
 * <p>
 * Extracted from {@link TLScriptAutoCompletionCommand} so that both the legacy
 * Ace editor and the new CodeMirror 6 editor can share the same completion logic.
 * </p>
 */
public class TLScriptCompletionService {

	/**
	 * Computes completions for the given line and prefix.
	 *
	 * @param context
	 *        The display context (needed for locale-aware documentation).
	 * @param line
	 *        The full text of the current line.
	 * @param prefix
	 *        The prefix typed so far (may be empty).
	 * @param caseSensitive
	 *        Whether to match case-sensitively.
	 * @return Sorted list of completions with scores assigned, possibly empty.
	 */
	public static List<CodeCompletion> computeCompletions(DisplayContext context,
			String line, String prefix, boolean caseSensitive) {
		// Move the body of TLScriptAutoCompletionCommand.createCompletions() here.
		// This includes the dispatch logic:
		//   1. Check if line matches TL model part pattern -> createTLModelPartCompletions()
		//   2. Check if line ends with "." -> createFunctionCompletions() (method-style)
		//   3. Otherwise -> createFunctionCompletions() (default functions)
		//
		// Move these private methods from TLScriptAutoCompletionCommand:
		//   - createTLModelPartCompletions(context, line, prefix, caseSensitive)
		//   - createFunctionCompletions(context, prefix, caseSensitive)
		//   - createFunctionCompletionsInternal(context, prefix, caseSensitive, names)
		//   - createCodeCompletion(context, functionName)
		//   - matchModelPart(line)
		//   - getDocHTML(context, name)
		//
		// All methods become static. Instance field _completeCaseSensitive becomes
		// the caseSensitive parameter. The DisplayContext parameter replaces the
		// one that was threaded from ControlCommand.execute().
		//
		// After computing completions, sort with CompletionByNameComparator and
		// assign descending scores.
	}
}
```

- [ ] **Step 2: Update TLScriptAutoCompletionCommand to delegate**

Replace the body of `TLScriptAutoCompletionCommand.createCompletions()` with a delegation call:

```java
List<CodeCompletion> completions = TLScriptCompletionService.computeCompletions(
    context, line, prefix, caseSensitive);
```

Keep the JSON serialization and `sendCompletions()` call in the command — only the computation logic moves.

- [ ] **Step 3: Build and verify compilation**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.model.search
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptCompletionService.java
git add com.top_logic.model.search/src/main/java/com/top_logic/model/search/ui/TLScriptAutoCompletionCommand.java
git commit -m "Ticket #29108: Extract TLScriptCompletionService from TLScriptAutoCompletionCommand."
```

---

## Chunk 2: Lezer Grammar for TL-Script

### Task 2: Add CodeMirror 6 and Lezer Dependencies

**Files:**
- Modify: `com.top_logic.layout.react/package.json`

- [ ] **Step 1: Add npm dependencies**

Use the project-local npm installed by `frontend-maven-plugin`:

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
./node/npm install @codemirror/view @codemirror/state @codemirror/language @codemirror/autocomplete @codemirror/lint @codemirror/commands @lezer/lr @lezer/highlight --save
./node/npm install @lezer/generator --save-dev
```

If `./node/npm` doesn't exist yet, run `mvn generate-resources -DskipTests=true` first to trigger the `install-node-and-npm` execution, then retry.

- [ ] **Step 2: Verify package.json updated**

Read `package.json` and confirm all 8 runtime deps and 1 dev dep are listed.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/package.json com.top_logic.layout.react/package-lock.json
git commit -m "Ticket #29108: Add CodeMirror 6 and Lezer dependencies."
```

### Task 3: Write the Lezer Grammar

**Files:**
- Create: `com.top_logic.layout.react/react-src/lang/tlscript.grammar`
- Create: `com.top_logic.layout.react/react-src/lang/tlscript-highlight.ts`

- [ ] **Step 1: Write the grammar file**

The grammar covers TL-Script syntax based on the JavaCC grammar (`SearchExpressionParser.jj`) and the existing Ace mode. The unary minus operator is handled by making `UnaryExpr` use the literal `"-"` token instead of a separate `MinusOp` token, avoiding ambiguity with the binary `additiveOp`:

```
@top Script { expression }

@skip { whitespace | LineComment | BlockComment }

expression {
  Lambda |
  TernaryExpr
}

Lambda { identifier Arrow expression }

TernaryExpr {
  OrExpr ("?" expression ":" expression)?
}

OrExpr { AndExpr (orOp AndExpr)* }
AndExpr { CompareExpr (andOp CompareExpr)* }
CompareExpr { AdditiveExpr (compareOp AdditiveExpr)? }
AdditiveExpr { MultiplicativeExpr ((Plus | Minus) MultiplicativeExpr)* }
MultiplicativeExpr { UnaryExpr (multiplicativeOp UnaryExpr)* }

UnaryExpr {
  NotOp expression |
  Minus UnaryExpr |
  AccessExpr
}

AccessExpr {
  AtomicExpr (MethodAccess | DescendAccess | CallArgs | ArrayAccess)*
}

MethodAccess { "." identifier CallArgs? }
DescendAccess { ".." identifier CallArgs? }
CallArgs { "(" commaSep<expression>? ")" }
ArrayAccess { "[" expression "]" }

AtomicExpr {
  Number |
  String |
  Boolean |
  Null |
  Variable |
  ModelConstant |
  ResourceKey |
  I18NLiteral |
  TupleExpr |
  SwitchExpr |
  Block |
  HtmlExpr |
  ParenExpr |
  FunctionCall
}

FunctionCall { identifier CallArgs }
ParenExpr { "(" expression ")" }
Block { "{" BlockContent "}" }
BlockContent { Statement (";" Statement)* ";"? }
Statement { (identifier "=" expression) | expression }

SwitchExpr {
  kw<"switch"> ("(" expression ")")? "{" SwitchCase* SwitchDefault? "}"
}
SwitchCase { expression ":" expression ";" }
SwitchDefault { kw<"default"> ":" expression ";"? }

TupleExpr { kw<"tuple"> "(" commaSep<TupleEntry> ")" }
TupleEntry { identifier Arrow expression }

I18NLiteral { "#(" commaSep<I18NEntry> ")" }
I18NEntry { String LanguageTag }

HtmlExpr { "{{{" htmlContent* "}}}" }
htmlContent { htmlText | HtmlEmbed }
HtmlEmbed { "{" expression "}" }

@tokens {
  whitespace { $[ \t\n\r]+ }
  LineComment { "//" ![\n]* }
  BlockComment { "/*" blockCommentContent* "*/" }
  blockCommentContent { ![*] | "*" ![/] }

  Number { ("0" | $[1-9] $[0-9]*) ("." $[0-9]+)? (("e" | "E") ("+" | "-")? $[0-9]+)? }
  String { "'" (![\\'] | "\\" _)* "'" | "\"" (![\\"] | "\\" _)* "\"" | "\"\"\"" _* "\"\"\"" }
  Variable { "$" $[a-zA-Z_] $[a-zA-Z0-9_]* }
  ModelConstant { "`" $[a-zA-Z_] $[a-zA-Z0-9_.]* (":" $[a-zA-Z_] $[a-zA-Z0-9_.]*)? ("#" $[a-zA-Z_] $[a-zA-Z0-9_.]*)? "`" }
  ResourceKey { "#" ("'" (![\\'] | "\\" _)* "'" | "\"" (![\\"] | "\\" _)* "\"") }
  LanguageTag { "@" $[a-zA-Z] $[a-zA-Z\-]* }
  identifier { $[a-zA-Z_] $[a-zA-Z0-9_]* }

  Arrow { "->" }
  orOp { "or" | "||" }
  andOp { "and" | "&&" }
  compareOp { "==" | "!=" | ">=" | "<=" | ">" | "<" }
  Plus { "+" }
  Minus { "-" }
  multiplicativeOp { "*" | "/" | "%" }
  NotOp { "!" }

  @precedence { BlockComment, LineComment, Number, Arrow, identifier, orOp, andOp }
}

kw<term> { @specialize[@name={term}]<identifier, term> }
commaSep<expr> { expr ("," expr)* }

@external propSource highlighting from "./tlscript-highlight"

Boolean { kw<"true"> | kw<"false"> }
Null { kw<"null"> }
```

**Note:** This is a starting draft. The grammar will need iterative refinement when tested against real TL-Script expressions. Edge cases (HTML mode `{{{ }}}`, triple-quoted strings) may need further work.

- [ ] **Step 2: Create the highlight props file**

Create `com.top_logic.layout.react/react-src/lang/tlscript-highlight.ts`:

```typescript
import { styleTags, tags as t } from '@lezer/highlight';

export const highlighting = styleTags({
  Number: t.number,
  String: t.string,
  Boolean: t.bool,
  Null: t.null,
  Variable: t.variableName,
  ModelConstant: t.typeName,
  ResourceKey: t.special(t.string),
  LanguageTag: t.annotation,
  LineComment: t.lineComment,
  BlockComment: t.blockComment,
  'identifier': t.variableName,
  'FunctionCall/identifier': t.function(t.variableName),
  'MethodAccess/identifier': t.function(t.propertyName),
  'Arrow': t.punctuation,
  '"(" ")" "[" "]" "{" "}"': t.paren,
  '"." ".."': t.derefOperator,
  'orOp andOp': t.logicOperator,
  'compareOp': t.compareOperator,
  'Plus Minus multiplicativeOp': t.arithmeticOperator,
  'NotOp': t.logicOperator,
  '"switch" "default" "tuple"': t.keyword,
  '"?" ":"': t.punctuation,
});
```

- [ ] **Step 3: Add Lezer rollup plugin to Vite config**

Modify `com.top_logic.layout.react/vite.config.controls.ts` to compile `.grammar` files at build time:

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { lezer } from '@lezer/generator/rollup';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' }), lezer()],
  // ... rest unchanged ...
});
```

This eliminates the need to manually run `@lezer/generator` CLI — the grammar is compiled during `npm run build`.

- [ ] **Step 4: Build to verify grammar compiles**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
mvn compile -DskipTests=true
```

The `frontend-maven-plugin` runs `npm run build`, which triggers Vite, which invokes the Lezer plugin to compile `tlscript.grammar` into a parser module. Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/react-src/lang/
git add com.top_logic.layout.react/vite.config.controls.ts
git commit -m "Ticket #29108: Add Lezer grammar and highlight tags for TL-Script."
```

### Task 4: Create CM6 Language Support Module

**Files:**
- Create: `com.top_logic.layout.react/react-src/lang/tlscript-lang.ts`

- [ ] **Step 1: Write the language support module**

```typescript
import { LRLanguage, LanguageSupport } from '@codemirror/language';
import { parser } from './tlscript.grammar';
import { highlighting } from './tlscript-highlight';

/**
 * CodeMirror 6 language definition for TL-Script.
 */
export const tlscriptLanguage = LRLanguage.define({
  parser: parser.configure({
    props: [highlighting],
  }),
  languageData: {
    commentTokens: { line: '//', block: { open: '/*', close: '*/' } },
    closeBrackets: { brackets: ['(', '[', '{', "'", '"', '`'] },
  },
});

/**
 * Returns a CM6 LanguageSupport instance for TL-Script.
 */
export function tlscript(): LanguageSupport {
  return new LanguageSupport(tlscriptLanguage);
}
```

Note: with the Lezer rollup plugin, the import `from './tlscript.grammar'` is resolved at build time — the plugin transforms the `.grammar` import into the compiled parser.

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/lang/tlscript-lang.ts
git commit -m "Ticket #29108: Add CM6 language support module for TL-Script."
```

---

## Chunk 3: TLScriptEditor React Component

### Task 5a: Create basic TLScriptEditor with syntax highlighting

The component is built incrementally. This step creates the editor with just syntax highlighting and value sync.

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLScriptEditor.tsx`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

- [ ] **Step 1: Write the basic editor component**

```typescript
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { EditorView, keymap } from '@codemirror/view';
import { EditorState, Compartment } from '@codemirror/state';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';
import { tlscript } from '../lang/tlscript-lang';

const { useRef, useEffect } = React;

/** Debounce delay for value sync and validation (ms). */
const DEBOUNCE_MS = 300;

/**
 * CodeMirror 6-based TL-Script editor control.
 *
 * Communicates with {@code TLScriptEditorReactControl} on the server via
 * {@code useTLCommand()} for value changes, validation, completions, and hover.
 */
const TLScriptEditor: React.FC<TLCellProps> = ({ controlId, state }) => {
  const sendCommand = useTLCommand();
  const editorRef = useRef<HTMLDivElement>(null);
  const viewRef = useRef<EditorView | null>(null);
  const editableComp = useRef(new Compartment());
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const initialValue = (state.value as string) ?? '';
  const readOnly = state.readOnly === true;

  // Create editor on mount
  useEffect(() => {
    if (!editorRef.current) return;

    const view = new EditorView({
      state: EditorState.create({
        doc: initialValue,
        extensions: [
          tlscript(),
          history(),
          keymap.of([...defaultKeymap, ...historyKeymap]),
          editableComp.current.of(EditorView.editable.of(!readOnly)),
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              // Debounced value sync and validation
              if (timerRef.current) clearTimeout(timerRef.current);
              timerRef.current = setTimeout(() => {
                const text = update.state.doc.toString();
                sendCommand('valueChanged', { value: text });
                sendCommand('validate', { text });
              }, DEBOUNCE_MS);
            }
          }),
        ],
      }),
      parent: editorRef.current,
    });

    viewRef.current = view;

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      view.destroy();
      viewRef.current = null;
    };
  }, []); // Mount once

  // Update readOnly when state changes (using Compartment for dynamic reconfiguration)
  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;
    view.dispatch({
      effects: editableComp.current.reconfigure(EditorView.editable.of(!readOnly)),
    });
  }, [readOnly]);

  return <div ref={editorRef} id={controlId} className="tlScriptEditor" />;
};

export default TLScriptEditor;
```

Key design points:
- Uses CM6 `Compartment` for dynamic `readOnly` toggling (not `EditorView.editable.reconfigure()`)
- Debounces `valueChanged` and `validate` commands (300ms)
- Destructures both `controlId` and `state` from `TLCellProps`

- [ ] **Step 2: Register the component**

Add to `com.top_logic.layout.react/react-src/controls-entry.ts`:

```typescript
import TLScriptEditor from './controls/TLScriptEditor';

register('TLScriptEditor', TLScriptEditor);
```

- [ ] **Step 3: Build to verify**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLScriptEditor.tsx
git add com.top_logic.layout.react/react-src/controls-entry.ts
git commit -m "Ticket #29108: Add basic TLScriptEditor component with syntax highlighting and value sync."
```

### Task 5b: Add diagnostics display

- [ ] **Step 1: Add lint extension to TLScriptEditor**

Add to `TLScriptEditor.tsx`:

```typescript
import { setDiagnostics } from '@codemirror/lint';
import type { Diagnostic } from '@codemirror/lint';

interface ServerDiagnostic {
  line: number;
  col: number;
  endLine?: number;
  endCol?: number;
  severity: 'error' | 'warning' | 'info';
  message: string;
}
```

Add a `useEffect` that reads `state.diagnostics` and pushes them to the CM6 editor:

```typescript
  const currentState = useTLState();
  const diagnostics = (currentState.diagnostics as ServerDiagnostic[]) ?? [];

  useEffect(() => {
    const view = viewRef.current;
    if (!view) return;

    const doc = view.state.doc;
    const cmDiags: Diagnostic[] = diagnostics
      .map((d) => {
        const lineNum = Math.max(1, Math.min(d.line, doc.lines));
        const line = doc.line(lineNum);
        const from = line.from + Math.max(0, Math.min(d.col - 1, line.length));
        const to = d.endCol != null && d.endLine != null
          ? doc.line(Math.max(1, Math.min(d.endLine, doc.lines))).from +
            Math.max(0, Math.min(d.endCol - 1, line.length))
          : Math.min(from + 1, line.to);
        return { from, to, severity: d.severity, message: d.message };
      })
      .filter((d) => d.from <= doc.length && d.to <= doc.length);

    view.dispatch(setDiagnostics(view.state, cmDiags));
  }, [diagnostics]);
```

- [ ] **Step 2: Build and commit**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
mvn compile -DskipTests=true
git add com.top_logic.layout.react/react-src/controls/TLScriptEditor.tsx
git commit -m "Ticket #29108: Add server-driven diagnostics display to TLScriptEditor."
```

### Task 5c: Add completion support via promise-over-SSE

The CM6 `CompletionSource` is `async` and must return a `CompletionResult`. Since
`useTLCommand()` is fire-and-forget, we use a **promise-over-SSE** pattern:

1. Client sends `complete` command with a unique `requestId`
2. Server computes completions and pushes them via SSE as `state.completionResponse`
3. Client resolves the pending promise when `completionResponse.requestId` matches

- [ ] **Step 1: Add completion source to TLScriptEditor**

Add to `TLScriptEditor.tsx`:

```typescript
import { autocompletion } from '@codemirror/autocomplete';
import type { CompletionContext, CompletionResult } from '@codemirror/autocomplete';

// Inside the component:
  const pendingCompletion = useRef<{
    requestId: string;
    resolve: (result: CompletionResult | null) => void;
  } | null>(null);

  // Watch for completion responses from server (via SSE state patch)
  const completionResponse = currentState.completionResponse as {
    requestId?: string;
    completions?: Array<{ name: string; value: string; score?: number; docHTML?: string }>;
  } | undefined;

  useEffect(() => {
    const pending = pendingCompletion.current;
    if (!pending || !completionResponse || completionResponse.requestId !== pending.requestId) {
      return;
    }
    const items = completionResponse.completions ?? [];
    pendingCompletion.current = null;

    if (items.length === 0) {
      pending.resolve(null);
      return;
    }
    pending.resolve({
      from: pending.from,
      options: items.map((c) => ({
        label: c.name,
        detail: c.value,
        info: c.docHTML ? () => {
          const div = document.createElement('div');
          div.innerHTML = c.docHTML;
          return div;
        } : undefined,
        boost: c.score ?? 0,
      })),
    });
  }, [completionResponse]);

  const completionSource = useCallback(
    (context: CompletionContext): Promise<CompletionResult | null> => {
      const pos = context.pos;
      const line = context.state.doc.lineAt(pos);
      const prefix = context.matchBefore(/[\w$`.:]+/)?.text ?? '';
      if (!prefix && !context.explicit) return Promise.resolve(null);

      const requestId = String(Date.now()) + Math.random();
      sendCommand('complete', { line: line.text, prefix, requestId });

      return new Promise((resolve) => {
        pendingCompletion.current = { requestId, resolve, from: pos - prefix.length };
        // Timeout: resolve null after 3s if server doesn't respond
        setTimeout(() => {
          if (pendingCompletion.current?.requestId === requestId) {
            pendingCompletion.current = null;
            resolve(null);
          }
        }, 3000);
      });
    },
    [sendCommand]
  );
```

Add `autocompletion({ override: [completionSource] })` to the editor extensions in the creation `useEffect`.

- [ ] **Step 2: Build and commit**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
mvn compile -DskipTests=true
git add com.top_logic.layout.react/react-src/controls/TLScriptEditor.tsx
git commit -m "Ticket #29108: Add completion support via promise-over-SSE pattern."
```

---

## Chunk 4: Server-Side ReactControl

### Task 6: Create bridge module and TLScriptEditorReactControl

**Files:**
- Create: `com.top_logic.model.search.react/pom.xml`
- Create: `com.top_logic.model.search.react/src/main/java/com/top_logic/model/search/react/TLScriptEditorReactControl.java`
- Modify: `tl-parent-engine/pom.xml` (register new module)

- [ ] **Step 1: Create the bridge module POM**

Create `com.top_logic.model.search.react/pom.xml` with parent `tl-parent-core-internal`
and dependencies on `tl-model-search` and `tl-layout-react`.

- [ ] **Step 2: Register module in tl-parent-engine/pom.xml**

Add `<module>../com.top_logic.model.search.react</module>` to the modules list.

- [ ] **Step 3: Write the control class**

The control uses a `java.util.function.Consumer<String>` for value callbacks (standard JDK, no extra dependency).

```java
package com.top_logic.model.search.react;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactDisplayContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.ui.CodeCompletion;
import com.top_logic.model.search.ui.TLScriptCompletionService;

/**
 * React control for editing TL-Script expressions using CodeMirror 6.
 *
 * <p>
 * Communicates with the {@code TLScriptEditor} React component via:
 * </p>
 * <ul>
 * <li>{@code complete} - computes code completions (response via SSE state patch)</li>
 * <li>{@code hover} - returns type/doc info for a token</li>
 * <li>{@code validate} - parses the script and returns diagnostics</li>
 * <li>{@code valueChanged} - syncs the edited value back to the form field</li>
 * </ul>
 */
public class TLScriptEditorReactControl extends ReactControl {

	private static final String VALUE = "value";
	private static final String READ_ONLY = "readOnly";
	private static final String DIAGNOSTICS = "diagnostics";

	private Consumer<String> _valueCallback;

	/**
	 * Creates a new {@link TLScriptEditorReactControl}.
	 *
	 * @param value
	 *        The initial TL-Script text, may be {@code null}.
	 * @param readOnly
	 *        Whether the editor is read-only.
	 */
	public TLScriptEditorReactControl(String value, boolean readOnly) {
		super(null, "TLScriptEditor");
		putState(VALUE, value != null ? value : "");
		putState(READ_ONLY, Boolean.valueOf(readOnly));
	}

	/**
	 * Sets the callback that is invoked when the value changes from the client.
	 *
	 * @param callback
	 *        The callback, or {@code null} to remove.
	 */
	public void setValueCallback(Consumer<String> callback) {
		_valueCallback = callback;
	}

	/** Updates the editor value from the server side. */
	public void setValue(String value) {
		putState(VALUE, value);
	}

	/** Updates the read-only state. */
	public void setReadOnly(boolean readOnly) {
		putState(READ_ONLY, Boolean.valueOf(readOnly));
	}

	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		Object rawValue = arguments.get(VALUE);
		String newValue = rawValue != null ? rawValue.toString() : null;
		if (_valueCallback != null) {
			_valueCallback.accept(newValue);
		}
	}

	@ReactCommand("validate")
	void handleValidate(Map<String, Object> arguments) {
		String text = (String) arguments.get("text");
		List<Map<String, Object>> diagnostics = computeDiagnostics(text);
		putState(DIAGNOSTICS, diagnostics);
	}

	@ReactCommand("complete")
	void handleComplete(ReactDisplayContext context, Map<String, Object> arguments) {
		String line = (String) arguments.get("line");
		String prefix = (String) arguments.get("prefix");
		String requestId = (String) arguments.get("requestId");

		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(context, line, prefix, false);

		List<Map<String, Object>> items = new ArrayList<>();
		for (CodeCompletion c : completions) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("name", c.getName());
			entry.put("value", c.getValue());
			entry.put("score", Integer.valueOf(c.getScore()));
			if (c.getDocHTML() != null) {
				entry.put("docHTML", c.getDocHTML());
			}
			items.add(entry);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("requestId", requestId);
		response.put("completions", items);
		putState("completionResponse", response);
	}

	@ReactCommand("hover")
	void handleHover(Map<String, Object> arguments) {
		String token = (String) arguments.get("token");
		Map<String, Object> hoverInfo = new HashMap<>();
		hoverInfo.put("token", token);
		// TODO: Look up documentation via SearchBuilder or model introspection.
		putState("hoverInfo", hoverInfo);
	}

	private List<Map<String, Object>> computeDiagnostics(String text) {
		if (text == null || text.isBlank()) {
			return Collections.emptyList();
		}
		try {
			SearchExpressionParser parser =
				new SearchExpressionParser(new StringReader(text));
			parser.expr();
			return Collections.emptyList();
		} catch (ParseException ex) {
			Map<String, Object> diag = new HashMap<>();
			if (ex.currentToken != null && ex.currentToken.next != null) {
				diag.put("line", Integer.valueOf(ex.currentToken.next.beginLine));
				diag.put("col", Integer.valueOf(ex.currentToken.next.beginColumn));
			} else {
				diag.put("line", Integer.valueOf(1));
				diag.put("col", Integer.valueOf(1));
			}
			diag.put("severity", "error");
			diag.put("message", ex.getMessage());
			return Collections.singletonList(diag);
		}
	}
}
```

Key design points:
- Uses `Consumer<String>` instead of a module-specific callback interface
- `handleComplete` accepts `ReactDisplayContext` parameter and passes it to `TLScriptCompletionService.computeCompletions()`
- Completions pushed via `putState("completionResponse", ...)` with `requestId` for correlation

- [ ] **Step 4: Install dependencies and build the new module**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.model.search
mvn install -DskipTests=true

cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
mvn install -DskipTests=true

cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.model.search.react
mvn compile -DskipTests=true
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.model.search.react/
git add tl-parent-engine/pom.xml
git commit -m "Ticket #29108: Add com.top_logic.model.search.react module with TLScriptEditorReactControl."
```

---

## Chunk 5: Integration and Full Build

### Task 7: Full Build Verification

- [ ] **Step 1: Build the react module end-to-end (Java + TS)**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a/com.top_logic.layout.react
mvn compile -DskipTests=true
```

This runs the frontend-maven-plugin which executes `npm run build`, compiling both the bridge and controls bundles (including the new `TLScriptEditor` component and Lezer grammar).

Expected: BUILD SUCCESS with both `tl-react-bridge.js` and `tl-react-controls.js` generated in `src/main/webapp/script/`.

- [ ] **Step 2: Verify the built JS bundles contain the new component**

Check that `TLScriptEditor` appears in the controls bundle:

```bash
grep -c "TLScriptEditor" com.top_logic.layout.react/src/main/webapp/script/tl-react-controls.js
```

Expected: At least 1 match.

- [ ] **Step 3: Commit build artifacts**

```bash
git add com.top_logic.layout.react/src/main/webapp/script/
git commit -m "Ticket #29108: Rebuild JS bundles with TLScriptEditor component."
```
