# Anchored text edits

`TextEdits` changes a named passage of a text and leaves the rest of it alone. `TextEditTool` is the
agent-facing pair of tools built on it: `readText` hands out one Text or String attribute exactly as
it is stored, `editText` replaces a passage of it.

This page is the contract. Gitea #168 is the ticket; `TestTextEdits`, `TestTextGutter` and
`TestTextEditTool` are where every rule below is pinned.

## Why anchored rather than whole-value

Before this, the only generic way to change stored instance data was a TL-Script `set` with
`withCommit=true`, which writes the **whole** value. For a document body or a system prompt that has
two costs:

- **Output tokens.** Changing one sentence of an 8,000-token document costs 8,000 output tokens,
  every time.
- **Drift.** Every untouched paragraph goes back through the model, where it can come back subtly
  different: reflowed whitespace, a rephrased heading, a Markdown table that lost a column. Prompt
  discipline ("copy every out-of-scope section character for character") holds for a while and then
  does not.

An anchored edit costs the changed snippet and **cannot** alter anything outside it, because nothing
outside it is ever re-emitted. That is a structural guarantee, not a prompt.

## The applier: `com.top_logic.tl.ai.core.text.TextEdits`

`String` in, `Result` out. No knowledge base, no model, no services - an application that addresses
its own text its own way (by heading, by section, by line range) reuses the applier without taking on
the generic tool.

```java
TextEdits.Result result = TextEdits.replace(current, "See the manual.", "See the handbook.");
if (result.applied()) {
    store(result.text());
} else {
    reportToTheCaller(result.message());   // says what to do next
}
```

| Operation | What it does |
|---|---|
| `replace(source, anchor, replacement)` | The anchor must occur **once**. |
| `replaceAll(source, anchor, replacement)` | Every occurrence, left to right, non-overlapping. |
| `insertAfter(source, anchor, insertion)` | Keeps the anchor, puts the text behind it. Unique anchor. |
| `insertBefore(source, anchor, insertion)` | Keeps the anchor, puts the text in front of it. Unique anchor. |
| `lineOf(text, offset)` | The 1-based line an offset sits on. |

Each takes an optional `Matching`: `TOLERANT` (the default, see the cascade) or `STRICT` (the anchor
must occur character for character).

### The three rules

**1. Byte-for-byte.** The anchor is matched against the stored characters as they stand, and the
replacement is stored as it stands. Nothing is canonicalised on the way in or on the way out. This is
what makes read-then-edit work: a caller that quoted a passage of a verbatim read gets a match, and a
caller that invented one gets a refusal rather than a guess.

**2. A unique anchor, or nothing happens.** Two occurrences are an `AMBIGUOUS` failure that names the
count and the line of every one of them. Editing the first match instead is the single failure mode
this class exists to prevent, and it is not recoverable afterwards - the caller believes the right
passage changed. `replaceAll` is how a caller says it does not need to tell them apart.

**3. A failure says what to do next.** "Not found" alone costs a whole round trip to learn nothing, so
a miss reports the **longest prefix of the anchor that does occur**, the line it occurs on, and how
the text continues there - which is exactly where the caller's idea of the text and the text itself
parted company:

```
The anchor does not occur in the text. It agrees with the text up to 'The importer now skips '
(line 3) and differs after that: there the text continues with 'empty rows. The importer now
skips empty rows. See the manua...'. Read the value and copy the passage to change out of it.
```

### The match cascade

`TOLERANT` tries three levels in order and stops at the first that finds anything. Which level
answered is always reported (`Result.match()`), and the message says so in words, because a fallback
that matched means the caller is wrong about what is stored.

| Level | Reads the same |
|---|---|
| `EXACT` | nothing is normalised |
| `LINE_ENDINGS` | `\r\n` and `\r` are read as `\n`, on both sides |
| `WHITESPACE` | every run of whitespace is read as one space, on both sides, and the anchor's own leading and trailing whitespace is dropped |

`WHITESPACE` counts the space characters Java does **not** call whitespace - the non-breaking space,
the thin space, the ideographic space - because a text that went through a word processor is full of
them and none of them is a difference the author intended.

**A level is taken as a whole.** If `EXACT` finds two occurrences, that is an ambiguity, not a reason
to try the next level.

**A fallback decides where, never what.** Every level compares a normalised anchor with a normalised
text and then maps the hit back onto the offsets of the text *as stored*, so what gets replaced is
always a range of the original. An anchor that names a line with the wrong indentation therefore finds
that line and replaces the line, leaving the stored indentation as it was. To change an indentation,
the anchor has to be exact - and then the first level of the cascade applies it.

### What is deliberately *not* normalised

The ticket proposed NFKC folding and mapping smart quotes and dashes to ASCII. That is not here, and
the omission is the design:

- A curly quote is not a straight one and an en dash is not a hyphen. Both are characters an author
  chose, so a text that differs from the anchor there is not the text the caller meant.
- Folding them would break rule 1. A caller could no longer predict what a given anchor hits, which is
  the property the whole design rests on.

Whitespace is different: it is the one class where the difference is reliably *not* the author's
intent, which is why it is the one class tolerated. If a real miss class turns up later, it is a
fourth `Match` value and one more level in the cascade; nothing else has to move.

### Empty anchors

An empty anchor occurs everywhere and therefore addresses nothing - against a text that is not empty
it is an `EMPTY_ANCHOR` failure. Against the **empty** text it addresses the one position that exists,
and writes the first content. That is what lets a caller holding only these operations fill a value
that was never set.

An anchor of nothing but whitespace is a legitimate *exact* anchor (a blank line). It has no tolerant
reading, because at the whitespace level it would match every gap in the text.

## The tools: `text-edit.readText` / `text-edit.editText`

```
readText(objectId, attribute[, fromLine, toLine])
editText(objectId, attribute, oldText, newText[, replaceAll])
```

`objectId` is the `"TypeName:id"` form every tool answer prints, resolved by `TidResolver`.
`attribute` is the part name.

### Read first

`readText` renders the value with a line number in front of every line:

```
systemPrompt, all 8 lines, exactly as stored:
<untrusted_model_data source="AIAgent:1952#systemPrompt">
  1| # Release notes
  2| 
  3| The importer now skips empty rows.
</untrusted_model_data>
```

The `NNN| ` gutter is the tool's own framing and **not part of the value**; the tool description says
so, and the whitespace level of the cascade absorbs a caller that got the indentation slightly wrong
anyway. The line numbers earn their place by making an ambiguous anchor fixable in one step ("occurs
on lines 3 and 5") rather than several.

Rendering stops on a **line boundary** before `MAX_READ_CHARS` (16,000), which is below the agent
loop's own `MAX_TOOL_RESULT_CHARS` (20,000). A result the framework truncated would end in the middle
of a line, and a caller that anchored on what it was shown would quote half a sentence as if it were
whole. The answer says where it stopped and which `fromLine` continues it.

### Which attributes

A **single-valued property** whose type is a `TLPrimitive` of kind `STRING` whose storage mapping
yields a Java `String`. That admits `tl.core:String` and `tl.core:Text` together with an application's
own datatypes built on them, and refuses everything else **by name**:

| Refused | Because |
|---|---|
| a reference | it names other objects, not a text |
| a multi-valued property | it holds a list, not one text |
| a date, number, boolean, binary, enumeration | not a text |
| an attribute the type does not have | a typo; the answer says where to look |

A model told only "not a text attribute" cannot tell a typo from a reference from a multi-valued
property, and retries all three.

Rich text (`StructuredText`) is **out of scope**, as the ticket scoped it: byte symmetry does not
survive an HTML/Markdown round trip, so it needs a structure-aware, lossy contract of its own.

### Security

- The object is resolved through `TidResolver`, so one the principal may not read is refused **exactly
  like one that does not exist**.
- The attribute is checked for Read before it is disclosed and for Write before it is changed - both
  the object and the attribute, because model access is attribute-granular and a principal may hold
  the object and lack one property of it.
- The refusal names the operation and the type, never the withheld value.
- The check happens in the tool as well as in the `PreCommitGuard`. Not because the guard is
  insufficient, but because a refusal the model reads *now* costs one tool call, while the same
  refusal at commit time rolls the whole turn back.

### The transaction

`editText` is `ToolMode.DATA_WRITE`. It takes the standard `editValues` lock through
`context.transaction()`, which bridges with the interactive UI's edit lock, so an object a user has
open is not written behind their back. It opens **no transaction of its own**: the agent loop begins
one before the first write tool of a turn and commits it after the turn, which is the path on which a
constraint that vetoes the commit is reported back to the model (#410).

An edit that would produce the value that is already stored is reported and **not written**, so a
no-op does not open a transaction.

## Reusing the applier without the tool

An application that addresses its text by section, heading or line range builds that addressing on
top and calls `TextEdits` for the edit itself. It gets rules 1 to 3 and the cascade for free and keeps
its own idea of what a "section" is:

```java
String section = sectionOf(document, "## Findings");
TextEdits.Result edited = TextEdits.replace(document, section, rewritten, Matching.STRICT);
```

`STRICT` is the honest setting there: the anchor was produced by the application itself, so a miss is
a bug in the addressing layer, and matching something almost-equal instead would hide it.
