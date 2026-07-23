# FAQ: msgbuf Library

The project uses the [msgbuf](https://github.com/msgbuf/msgbuf) library for type-safe protocol message generation from `.proto` files.

## Key pitfall: writer types

`de.haumacher.msgbuf.json.JsonWriter` takes `de.haumacher.msgbuf.io.Writer` — **not** `java.io.Writer`. The compiler error "StringWriter cannot be converted to Writer" is misleading because `StringWriter` IS a `java.io.Writer`, but the constructor expects the msgbuf `Writer` interface. The error omits the package, making it look like a standard Java type.

**Correct usage:**

```java
// For in-memory string serialization:
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

StringW out = new StringW();
try (JsonWriter writer = new JsonWriter(out)) {
    myMessage.writeTo(writer);
}
String json = out.toString();

// To wrap a java.io.Writer (server-side only):
import de.haumacher.msgbuf.server.io.WriterAdapter;

try (JsonWriter writer = new JsonWriter(new WriterAdapter(javaIoWriter))) {
    myMessage.writeTo(writer);
}
```

## Generator plugin

The msgbuf Maven plugin generates Java classes from `.proto` files. Its default lifecycle phase is **not** `generate-sources`, so `mvn generate-sources` alone won't trigger it — it runs during `mvn compile`. To run it in isolation, invoke its `generate` goal by plugin prefix, targeting a module that declares the plugin (`com.top_logic.basic`, `com.top_logic.graphic.blocks`, or `tl-tools-resources`):

```bash
mvn msgbuf-generator:generate -pl com.top_logic.basic
```

The prefix is `msgbuf-generator` (not `msgbuf`); no version is needed — Maven resolves it from the target module's POM (`${msgbuf.version}`).
