# What `aiChat` asks for, and what it does when there is no answer

The TL-Script function `aiChat(messages, model, responseFormat, tools)` runs one exchange with a
model. Two things about it were promises the code did not keep, fixed in Gitea #388 and #389.

## The response format reaches the provider

`responseFormat` takes the three documented shapes:

| Argument | Meaning |
|---|---|
| `null` | Nothing is asked for; the provider answers as it likes. |
| `"text"` | The provider default, stated explicitly. |
| `"json"` | Some JSON object, with nothing saying which. |
| `{name: ..., schema: ..., strict: ...}` | A JSON object satisfying `schema`. |

The schema map is the shape worth using: a JSON mode still lets the model choose its own field
names, so the caller ends up parsing defensively anyway. `name` is what the provider registers the
compiled schema under (default `response`), `strict` asks the provider to reject its own output when
it does not validate (default `false`, because a strict schema must additionally close every object
with `additionalProperties: false` and list every property in `required`).

An argument that is none of these - `"yaml"`, or a map without a `schema` - is refused where it is
written. An ignored format is worse than a rejected one: a caller that asked for JSON and receives
prose cannot tell that its request was dropped.

The value travels as `AgentParams#responseFormat` into `ProviderRequest#responseFormat`, and each
request builder spells it the way its API does:

| Provider | `"json"` | a schema |
|---|---|---|
| Anthropic Messages | **refused** - the API has no untyped JSON mode | `output_config.format` = `{type: "json_schema", schema}` |
| OpenAI Responses | `text.format` = `{type: "json_object"}` | `text.format` = `{type, name, strict, schema}`, flattened |
| Chat Completions (OpenAI-compatible, Mistral, xAI, local) | `response_format` = `{type: "json_object"}` | `response_format` = `{type: "json_schema", json_schema: {name, strict, schema}}` |
| Bedrock Converse | **refused** | **refused** - the API has no response format at all |

A refusal is a `ProviderException` of kind `INVALID_REQUEST`, raised while the request is built, so
nothing is sent. `TestResponseFormatInRequests` pins every row of that table.

## A failed run is a failure

`aiChat` used to return the empty string whenever the run added no assistant message - a rejected
API key, a stream error, an exhausted turn budget and a model that answered nothing all read the
same to the script (#389). It now throws a `TopLogicException` carrying the provider's own error
kind and message for every stop reason but `COMPLETE`, after the run has been recorded, so the
failed run is both a row an administrator can look at and an error the caller cannot mistake for an
answer. `aiAgentChat` reports the same way.

The asynchronous half is symmetric, because it has a handle rather than a return value:

- `aiAgentRunStatus($handle)` - `RUNNING`, `COMPLETE`, `ERROR`, `CANCELLED`, `REJECTED`.
- `aiAgentRunResult($handle)` - the answer of a `COMPLETE` run, `null` while it is not finished, and
  the same raise as `aiChat` for a run that ended in any other state.
- `aiAgentRunError($handle)` - the recorded message without raising it, for a script that wants to
  store or display the failure rather than be stopped by it.

The documented polling shape never reaches the raise:

```
aiAgentRunStatus($handle) == "COMPLETE" ? aiAgentRunResult($handle) : null
```

## A call with tools gets the turns to use them

`aiChat` without tools is one exchange, as it always was. A call that passes tools now gets the
default turn budget: the answer comes after the tool results, so the one-turn budget stopped the
loop with the tool calls made and nothing said - which, after #389, would be a failure rather than
an empty string.
