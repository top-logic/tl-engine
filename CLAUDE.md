# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TopLogic is an open-source, model-based, no-code web application development platform. It combines declarative configuration (UML/BPMN modeling, WYSIWYG UI configuration) with extensible Java components. The platform enables both no-code application development and traditional Java/Maven development workflows.

**Version**: 7.10.0-SNAPSHOT
**Java Version**: 17
**Build System**: Maven 3.6.0+
**License**: Dual-licensed (AGPL-3.0 / BOS-TopLogic-1.0)

## Build Commands

### Building the Project

```bash
# Build entire engine from root (tl-parent-all)
mvn clean install

# Build without tests (faster, default configuration)
mvn clean install -DskipTests=true

# Build specific module
cd com.top_logic.basic
mvn clean install
```

### Running Tests

Tests are **skipped by default** (`skipTests=true` in tl-parent-all). To run tests:

```bash
# Run tests in specific module
cd com.top_logic.basic
mvn test -DskipTests=false

# Run single test class
mvn test -DskipTests=false -Dtest=ClassName

# Run specific test method
mvn test -DskipTests=false -Dtest=ClassName#methodName
```

**Important**: Tests require database configuration. Many tests use H2 in-memory databases, but some require specific database drivers (MySQL, Oracle, PostgreSQL, DB2, MS SQL Server).

### Other Useful Commands

```bash
# Check for dependency issues
mvn dependency:tree

# Check for plugin updates (from tl-parent-all)
mvn -N versions:display-plugin-updates

# Run SpotBugs static analysis
mvn clean verify spotbugs:spotbugs

# Check for security vulnerabilities
mvn dependency-check:check

# Generate JavaDoc
mvn javadoc:javadoc
```

## Architecture Overview

TopLogic uses a **layered, modular architecture** with approximately 110+ Maven modules organized into distinct layers:

### Module Naming Conventions

- **`com.top_logic.*`** - Core framework modules (reverse DNS)
- **`tl-*`** - Build tooling and infrastructure
- **`ext.*`** - Packaged external libraries (Bootstrap Icons, CKEditor, Font Awesome, BPMN.js, Ace Editor, etc.)
- **`test.*`** - Test-only modules

### Core Layers

1. **Build Infrastructure** (`tl-parent-*`, `tl-build-*`, `tl-maven-plugin`)
   - Parent POMs define versioning, dependencies, and plugin configuration
   - Custom Maven plugins for TopLogic-specific build tasks
   - Build processors for code generation

2. **Foundation Layer** (`com.top_logic.basic*`)
   - Low-level utilities with no UI dependencies
   - Configuration framework (`basic.config`) - declarative typed configuration system
   - Data structures, I18N, reflection, XML/JSON processing
   - Database abstraction layer (`basic.db`, `basic.db.schema`)
   - Logging facades (Log4j, Logback)

3. **Core Engine** (`com.top_logic` / tl-core)
   - **Knowledge Base** (`knowledge.*`) - Object persistence, versioning, event journal
   - **Type System** (`model.*`) - Dynamic type definitions, forms, data binding
   - **Layout System** (`layout.*`) - Component-based declarative UI framework
   - **Security** - Authentication, authorization, access control

4. **Feature Modules** (various `com.top_logic.*`)
   - **BPE**: Business Process Engine (`bpe`, `bpe.modeler`, `bpe.app`)
   - **Reporting**: Report generation (`reporting`, `reporting.office`, `reporting.flex`)
   - **Search**: Full-text search (`search.base`, `search.lucene`)
   - **Graph/Diagrams**: UML, BPMN support (`graph.*`, `umljs`, `graph.diagramjs.*`)
   - **Office Integration**: Word/Excel/PowerPoint (`office`, `doc`, `template`)
   - **Messaging**: Kafka, JMS integration (`kafka*`, `tl-service-jms*`)
   - **OpenAPI**: REST API support (`service.openapi.*`)

5. **Client Layer** (GWT-compiled JavaScript)
   - `*.ajax.client`, `*.client.diagramjs`, `*.graphic.blocks.client`
   - Compiled from Java to JavaScript for browser execution

### Key Architectural Patterns

- **Fragment Modules**: Most modules are library JARs that compose into applications
- **Parent POMs**: `tl-parent-core` for fragment modules, `tl-parent-app` for runnable applications
- **Configuration-Driven**: Heavy use of XML configuration files in `src/main/webapp/WEB-INF/`
- **Declarative Layouts**: UI defined in `.layout.xml` files under `WEB-INF/layouts/`
- **Type System**: Dynamic type definitions enable model-driven development
- **Knowledge Base**: Custom ORM layer above SQL databases with versioning and auditing

## Module Dependencies

**Typical dependency flow** (bottom-up):

```
com.top_logic.basic (utilities, no dependencies on other TL modules)
  ↓
com.top_logic.basic.db (database layer)
  ↓
com.top_logic.dob, .dsa (data access layer)
  ↓
com.top_logic (core: knowledge base, model system, layout engine)
  ↓
Feature modules (bpe, reporting, search, etc.)
  ↓
Application modules (compose features)
```

**Important**: The `basic.*` layer must remain dependency-free from higher layers to prevent circular dependencies.

## File Locations

### Source Structure

- **Java source**: `src/main/java/com/top_logic/...`
- **Resources**: `src/main/resources/`
- **Webapp files**: `src/main/webapp/` (for web modules)
  - Layouts: `WEB-INF/layouts/`
  - Configuration: `WEB-INF/conf/`
  - Kbase definitions: `WEB-INF/kbase/`
  - Model definitions: `WEB-INF/model/`

### Test Structure

- **Test source**: `src/test/java/test/com/top_logic/...`
  - Note the `test.` prefix in package names
- **Test resources**: `src/test/resources/`

### Configuration Files

- **Module POM**: `pom.xml` (defines dependencies, build configuration)
- **Parent POM reference**: Most modules extend `tl-parent-core-internal`
- **Webapp descriptor**: `WEB-INF/web.xml` (for web modules)
- **TypeDoc**: `*.type.xml` files define model types

## Development Patterns

### Configuration Framework

TopLogic uses a sophisticated typed configuration system. Configuration classes use annotations:

```java
public interface MyConfig extends ConfigurationItem {
    @Name("my-property")
    @Mandatory
    String getMyProperty();
}
```

Configuration is typically loaded from XML files using `InstantiationContext`.

### Knowledge Base Access

Objects are accessed through the Knowledge Base API:

```java
KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
Transaction tx = kb.beginTransaction();
try {
    // Perform operations
    tx.commit();
} finally {
    tx.rollback();
}
```

### Layout Components

UI components extend `LayoutComponent` and are configured declaratively:

```xml
<layout>
    <component class="com.top_logic.layout.form.component.FormComponent">
        <!-- Configuration -->
    </component>
</layout>
```

### Model Definitions

Types are defined in XML files (`*.model.xml`):

```xml
<model xmlns="http://www.top-logic.com/ns/dynamic-types/6.0">
    <module name="my.module">
        <class name="MyType">
            <attributes>
                <property name="myAttribute" type="tl.core:String"/>
            </attributes>
        </class>
    </module>
</model>
```

### Exception Handling

TopLogic uses **`TopLogicException`** for user-visible errors that should be displayed with internationalized messages:

```java
import com.top_logic.util.error.TopLogicException;

// Define I18N constants in I18NConstants.java
public class I18NConstants extends I18NConstantsBase {
    /**
     * @en Failed to generate PDF: {0}
     */
    public static ResKey1 ERROR_PDF_GENERATION_FAILED__MSG;

    static {
        initConstants(I18NConstants.class);
    }
}

// Use TopLogicException with I18N constants
throw new TopLogicException(I18NConstants.ERROR_PDF_GENERATION_FAILED__MSG.fill(ex.getMessage()), ex);
```

**When to use TopLogicException:**
- User-visible errors that should be displayed in the UI
- Errors that need internationalization support
- Validation failures that users need to understand

**When to use RuntimeException:**
- Internal programming errors that should not occur in production
- Errors that indicate bugs rather than user mistakes
- Low-level technical failures that users cannot act upon

**I18N Constant Naming Convention:**
- Format: `ERROR_<DESCRIPTION>__<PARAM1>_<PARAM2>`
- Example: `ERROR_INVALID_PAGE_SIZE__VALUE_VALID` (takes value and valid options as parameters)
- Use `ResKey` (no params), `ResKey1` (1 param), `ResKey2` (2 params), etc.
- JavaDoc comment must start with `@en` for English default text

## Testing Conventions

- Test classes use the `test.` package prefix (e.g., `test.com.top_logic.basic.GenericTest`)
- Many base test classes exist for common scenarios (see `Abstract*Test.java` patterns)
- Tests requiring a knowledge base extend `AbstractDBKnowledgeBaseTest`
- Tests are JUnit 4 based (JUnit 4.13.2)

## Important Notes

### Database Support

The platform supports multiple databases:
- **H2** (default for development/testing)
- **Oracle**, **PostgreSQL**, **MySQL**, **MS SQL Server**, **IBM DB2**

Database-specific drivers are included in test scope for internal modules.

### Encoding

Source files use **ISO-8859-1** encoding (`project.build.sourceEncoding=ISO-8859-1` in parent POM).

### Version Management

- Central version: `7.10.0-SNAPSHOT` defined in `tl-parent-all`
- Use `${tl.version}` property for internal dependencies
- External dependencies versioned via properties (e.g., `${jetty.version}`)

### Layout Normalization

XML layout files can be normalized using:

```bash
mvn exec:java@normalize-layouts
```

This runs `com.top_logic.basic.xml.XMLPrettyPrinter` on layout directories.

### Migration Tools

For layout migrations after API changes:

```bash
# Example: Migrate confirm messages (Ticket #28336)
mvn exec:java@migrate-ticket28336
```

## Repository Structure

- **tl-parent-all/** - Root aggregator POM
- **tl-parent-engine/** - Aggregates all runtime modules (103 modules)
- **com.top_logic/** - Core engine (tl-core artifact)
- **com.top_logic.basic/** - Foundation utilities
- **com.top_logic.element/** - Element management
- **com.top_logic.demo/** - Demo application
- **test-migrate-apps/** - Test applications for migration scenarios
- **bos-settings/** - Build settings and configuration
- **tl-doc/** - Documentation and JavaDoc output

## Git Commit Message Convention

Commit messages in this project must follow a specific format:

- **Format**: `Ticket #<number>: <description>`
- **Example**: `Ticket #28934: Add data URI SVG support to SVGReplacedElementFactory.`
- **Important**: Do NOT include "Generated with Claude Code", "Co-Authored-By: Claude", or any AI-generation attribution lines
- Keep the message plain and focused on describing the change

## Additional Resources

- **Main Repository**: https://github.com/top-logic/tl-engine
- **Developer Guide**: https://dev.top-logic.com/tl/content/docs/DeveloperGuide/
- **Tutorial**: https://dev.top-logic.com/tl/content/docs/Tutorial/
- **Discord Server**: https://discord.gg/XsNq9JXTe9
- **Organization**: Business Operation Systems GmbH (https://top-logic.com)