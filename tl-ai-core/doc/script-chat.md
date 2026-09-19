# What `aiChat` asks a model for

The TL-Script function `aiChat(messages, model, responseFormat, tools)` runs one exchange with a
model. Its `responseFormat` argument was a promise the code did not keep, fixed in Gitea #388.

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
