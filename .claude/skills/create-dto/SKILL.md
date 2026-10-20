---
name: create-dto
description: This skill should be used when creating data transfer objects (DTOs), JSON serialization classes, or response/request objects. Use msgbuf library with .proto files instead of hand-written POJOs.
version: 1.0.0
---

# Creating DTOs with msgbuf

**DO NOT** create hand-written POJO DTOs with getters/setters for JSON serialization. Use the **msgbuf** library (`de.haumacher.msgbuf`) to define message types in `.proto` files and generate data classes automatically.

**DO NOT** use Jackson (`ObjectMapper`) for JSON serialization/deserialization. Use msgbuf's built-in `JsonWriter`/`JsonReader` instead.

## Step 1: Create a `.proto` file

Create a `messages.proto` file in a `messages` subpackage:

```proto
package com.top_logic.ai.<module>.path.messages;

option NoBinary;
option NoVisit;
option NoListener;
option NoInterfaces;
option NoXml;

message MyResponse {
    string name;
    bool active;
    int count;
    repeated MyItem items;
}

message MyItem {
    string id;
    string label;
}
```

## Step 2: Add the msgbuf generator plugin

Add to the module's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>de.haumacher.msgbuf</groupId>
            <artifactId>msgbuf-generator-maven-plugin</artifactId>
            <version>${msgbuf.version}</version>
            <executions>
                <execution>
                    <id>generate-protocols</id>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Step 3: Generate Java classes

Run `mvn compile` to generate Java classes from the `.proto` file.

## Step 4: Add TestComment exclusion

Add exclusion for the generated package in the module's test config (`src/test/webapp/WEB-INF/conf/<module>.test.config.xml`):

```xml
<config config:interface="test.com.top_logic.basic.TestComment$Config">
    <ignore>
        <entry value="com.top_logic.ai.<module>.path.messages" />
    </ignore>
</config>
```

## Using generated classes

```java
// Create with fluent setters
MyResponse response = MyResponse.create()
    .setName("example")
    .setActive(true)
    .setCount(42);

// Serialize to JSON string
StringW buffer = new StringW();
try (JsonWriter json = new JsonWriter(buffer)) {
    response.writeTo(json);
}
String jsonString = buffer.toString();

// Deserialize from JSON string
JsonReader reader = new JsonReader(new StringR(jsonString));
MyResponse parsed = MyResponse.readMyResponse(reader);
```

For tool responses in `tl-ai-core`, use `DataClassSerializer.toJson()` which adds error handling and optional tool call tracking.

## Important constraints

- msgbuf does not support `map<string, Object>`. If you need a map with mixed value types, serialize it to a JSON string field (e.g., `string argsJson`) and parse it on the client side.
- Generated classes have `protected` constructors — always use the static `create()` factory method.
- The `.proto` file must be placed in the same package directory as where the generated classes will live.

## Existing examples

- `tl-ai-core/src/main/java/com/top_logic/tl/ai/core/tools/model/messages/messages.proto` — largest example
- `tl-ai-core/src/main/java/com/top_logic/tl/ai/core/tools/integration/messages/messages.proto` — simple example
- `tl-chat/src/main/java/com/top_logic/ai/chat/api/messages/messages.proto` — chat API DTOs and SSE event payloads
