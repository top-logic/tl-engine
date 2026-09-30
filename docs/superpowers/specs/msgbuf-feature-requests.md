# msgbuf Feature Requests

Three feature requests for [msgbuf](https://github.com/msgbuf/msgbuf) needed for the TopLogic Model Editor React migration.

---

## Issue 1: Support cross-file protocol extension (extends across .proto files)

### Problem

Currently, all message types in a protocol must be defined in a single `.proto` file. It is not possible to define a subtype of an abstract message in a different `.proto` file located in a different module/package.

### Use Case

We have a server-sent events (SSE) protocol defined in a base module:

```protobuf
// In module A: sse.proto
abstract message SSEEvent {}
message PatchEvent extends SSEEvent { ... }
message StateEvent extends SSEEvent { ... }
```

A downstream module needs to add domain-specific event types:

```protobuf
// In module B: graph-sse.proto
import "sse.proto";

message GraphPatchEvent extends SSEEvent {
    string controlId;
    string creates;
    string updates;
    string deletes;
}
```

This is currently not supported. The only workaround is to add all message types to the single base `.proto` file, which creates an undesirable dependency from the base module to all domain-specific modules.

### Proposed Solution

Allow `extends` to reference abstract messages defined in other `.proto` files via an `import` statement. The generated code should:

1. Correctly register the subtype with the base type's deserialization dispatch.
2. Support polymorphic deserialization - a reader for `SSEEvent` should be able to deserialize a `GraphPatchEvent` without the base module knowing about it at compile time.

This would enable modular, extensible protocols where each module can define its own message types that participate in a shared type hierarchy.

---

## Issue 2: TypeScript/JavaScript code generation

### Problem

msgbuf currently only generates Java classes from `.proto` files. For web applications using SSE (Server-Sent Events) or WebSocket protocols, the client-side TypeScript/JavaScript deserialization and dispatch code must be written by hand. This is repetitive, error-prone, and defeats the purpose of a code generator.

### Use Case

A server sends SSE events defined in a `.proto` file. The Java server code is generated, but the TypeScript client code must be manually written:

```typescript
// Currently hand-written for each message type:
function dispatchSSEEvent(json: any): void {
    switch (json._type) {
        case "PatchEvent":
            handlePatch(json.controlId, json.patch);
            break;
        case "StateEvent":
            handleState(json.controlId, json.state);
            break;
        // ... every new event type requires manual additions
    }
}
```

### Proposed Solution

Add a TypeScript/JavaScript code generation target to msgbuf. From the same `.proto` file, generate:

1. **TypeScript interfaces/classes** with typed properties
2. **JSON deserialization** (`fromJson` / `readFrom` methods)
3. **JSON serialization** (`toJson` / `writeTo` methods)
4. **Polymorphic dispatch** for abstract message hierarchies (automatic type-tag based deserialization)

Example generated output:

```typescript
// Generated from sse.proto
export abstract class SSEEvent {
    static fromJson(reader: JsonReader): SSEEvent { /* dispatch by _type */ }
}

export class PatchEvent extends SSEEvent {
    controlId: string;
    patch: string;

    static fromJson(reader: JsonReader): PatchEvent { ... }
    toJson(writer: JsonWriter): void { ... }
}
```

This would ensure client and server are always in sync and eliminate an entire class of serialization bugs.

---

## Issue 3: Native JSON value type (avoid string-wrapping of structured data)

### Problem

msgbuf message fields can only hold primitive types or other messages. When a message needs to carry an arbitrary JSON structure (e.g., a dynamically-typed state object, a JSON Merge Patch, or a nested graph model), the JSON value must first be serialized to a `string`, then embedded in the message:

```protobuf
message PatchEvent extends SSEEvent {
    string controlId;
    string patch;  // <-- JSON object forced into a string
}
```

This results in double-serialization on the wire:

```json
{"_type":"PatchEvent","controlId":"c1","patch":"{\"nodes\":{\"n1\":{\"x\":100}}}"}
```

The `patch` value is a JSON string containing escaped JSON. The server must `JSON.stringify()` the patch before writing the message, and the client must `JSON.parse()` it after reading the message. This is:

- **Wasteful**: double serialization/deserialization overhead
- **Fragile**: escaping errors, encoding issues
- **Unreadable**: debugging SSE event streams requires manual un-escaping

### Proposed Solution

Add a `json` field type (or `any` type) to msgbuf that represents an opaque JSON value. The value is written directly into the message's JSON representation without string-wrapping:

```protobuf
message PatchEvent extends SSEEvent {
    string controlId;
    json patch;  // <-- native JSON value, no string wrapping
}
```

Wire format becomes:

```json
{"_type":"PatchEvent","controlId":"c1","patch":{"nodes":{"n1":{"x":100}}}}
```

The generated Java API would use a suitable type (e.g., `JsonValue`, `Object`, or `Map<String, Object>`) and write/read it directly via `JsonWriter`/`JsonReader` without intermediate string conversion.
