# FAQ: Writing a ConfigurationItem to an XML file

## Problem: Configuration XML is malformed, uses wrong encoding, or is unreadable

When serializing a `ConfigurationItem` back to an XML file, incorrect encoding setup or missing formatting causes broken files (encoding mismatch), unreadable output (single-line XML), or application startup failures.

## Standard Pattern

```java
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;

File file = ...;
try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StringServices.CHARSET_UTF_8)) {
    new ConfigurationWriter(out).write("root-tag", MyConfig.class, config);
}
XMLPrettyPrinter.normalizeFile(file);
```

- **`"root-tag"`**: The XML root element name (e.g. `"view"`, `"config"`, `"application"`).
- **`MyConfig.class`**: The static configuration interface type.
- **`config`**: The `ConfigurationItem` instance to serialize.

## Rules

1. **Always use UTF-8** via `StringServices.CHARSET_UTF_8`. Never use `"ISO-8859-1"` or other encodings.

2. **Always call `XMLPrettyPrinter.normalizeFile()`** after writing. `ConfigurationWriter` produces single-line XML without indentation. The pretty-printer reformats the file with proper indentation and line breaks.

3. **Never construct the `XMLStreamWriter` manually.** Use the `ConfigurationWriter(Writer)` constructor — it creates the `XMLStreamWriter` internally and writes the XML header automatically.

## Common Mistakes

### Wrong encoding

```java
// WRONG: ISO-8859-1 encoding causes mismatch with XML declaration
new OutputStreamWriter(new FileOutputStream(file), "ISO-8859-1")

// CORRECT: Always UTF-8
new OutputStreamWriter(new FileOutputStream(file), StringServices.CHARSET_UTF_8)
```

The `ConfigurationWriter` calls `XMLStreamWriter.writeStartDocument()` without an explicit encoding parameter. The `XMLStreamWriter` defaults to UTF-8 in the XML declaration. If the `OutputStreamWriter` uses a different encoding, the declared encoding and the actual byte encoding diverge — causing parse errors for any non-ASCII characters (e.g. German umlauts).

### Missing pretty-printing

```java
// WRONG: Produces a single, unreadable line of XML
try (Writer out = ...) {
    new ConfigurationWriter(out).write("config", MyConfig.class, config);
}
// File is syntactically valid but a single line

// CORRECT: Format after writing
try (Writer out = ...) {
    new ConfigurationWriter(out).write("config", MyConfig.class, config);
}
XMLPrettyPrinter.normalizeFile(file);  // Produces indented, readable XML
```

## Reference implementations

- `LayoutUtils.writeConfiguration()` in `com.top_logic/src/main/java/com/top_logic/mig/html/layout/LayoutUtils.java`
- `MigrationUtil.dumpMigrationConfig()` in `com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/MigrationUtil.java`
