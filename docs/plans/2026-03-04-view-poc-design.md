# View System PoC Design

## Goal

Prove the core architecture of the new view configuration layer: a shared UIElement
tree (parsed once from XML config) that produces per-session Control trees wired into
the existing React rendering pipeline.

## Architecture Overview

```
Browser GET /view/ --> ViewServlet (new)
  --> loads .view.xml (TypedConfiguration)
  --> instantiates UIElement tree (Phase 1 - shared, stateless)
  --> calls root.createControl(viewContext) (Phase 2 - per session)
  --> renders bootstrap HTML with React mount points
  --> client establishes SSE to ReactServlet (existing)
  --> all further interaction via ReactServlet
```

### Two-Phase Lifecycle

**Phase 1 - Build the factory tree (once, shared across all sessions):**

```
.view.xml --> TypedConfiguration parser --> Config tree
  --> InstantiationContext.getInstance() --> UIElement tree
```

Each UIElement instantiates its child UIElements from its child configs during
construction. The resulting UIElement tree is stateless and cached.

**Phase 2 - Create control tree (per session):**

```
UIElement tree --> root.createControl(viewContext) --> Control tree
```

Each UIElement creates its ReactControl and recursively creates child controls.
The control tree is per-session, holds dynamic state, and is managed by the
SSEUpdateQueue.

## Core Types

### UIElement

Base interface. A stateless factory for Controls, instantiated from configuration.

```java
public interface UIElement {

    interface Config extends PolymorphicConfiguration<UIElement> {
        // Future: css-class, visibility expression, etc.
    }

    /**
     * Creates the Control that renders this element for a specific session.
     *
     * @param context Hierarchical scope providing session infrastructure.
     *                Container elements may create derived contexts for children.
     * @return A Control (typically ReactControl) for the current session.
     */
    Control createControl(ViewContext context);
}
```

### ViewContext

Hierarchical scope passed during control creation. Minimal for PoC, extensible later.

```java
public interface ViewContext {

    /** Scope for allocating control IDs and registering commands. */
    FrameScope getFrameScope();

    // Future extensions:
    // Object getChannelValue(String channelName);
    // FormContext getFormContext();
    // SecurityContext getSecurityContext();
}
```

Container elements can create derived ViewContexts that add scoped information
for their children (e.g., a form element adding a FormContext). For the PoC,
the context is passed through unchanged.

### ViewElement

The mandatory root element of every `.view.xml` file. Establishes the scope
boundary for the view.

```java
public class ViewElement implements UIElement {

    public interface Config extends UIElement.Config {
        /** The root content of this view. */
        @Name("content")
        @Mandatory
        PolymorphicConfiguration<? extends UIElement> getContent();

        // Future: channel declarations, view-level configuration
    }

    private final UIElement _content;

    public ViewElement(InstantiationContext context, Config config) {
        _content = context.getInstance(config.getContent());
    }

    @Override
    public Control createControl(ViewContext context) {
        return _content.createControl(context);
    }
}
```

### ContainerElement

Base for elements that hold a list of children.

```java
public abstract class ContainerElement implements UIElement {

    public interface Config extends UIElement.Config {
        @Name("children")
        List<PolymorphicConfiguration<? extends UIElement>> getChildren();
    }

    private final List<UIElement> _children;

    protected ContainerElement(InstantiationContext context, Config config) {
        _children = config.getChildren().stream()
            .map(context::getInstance)
            .toList();
    }

    protected List<UIElement> getChildren() {
        return _children;
    }

    protected List<Control> createChildControls(ViewContext context) {
        return _children.stream()
            .map(child -> child.createControl(context))
            .toList();
    }
}
```

## PoC Element Implementations

### AppShellElement

Wraps `ReactAppShellControl`. Defines the top-level app layout with header, content,
and footer regions.

```java
public class AppShellElement implements UIElement {

    public interface Config extends UIElement.Config {
        @Name("header")
        PolymorphicConfiguration<? extends UIElement> getHeader();

        @Name("content")
        @Mandatory
        PolymorphicConfiguration<? extends UIElement> getContent();

        @Name("footer")
        PolymorphicConfiguration<? extends UIElement> getFooter();
    }

    private final UIElement _header;   // nullable
    private final UIElement _content;
    private final UIElement _footer;   // nullable

    public AppShellElement(InstantiationContext context, Config config) {
        _header = optionalInstance(context, config.getHeader());
        _content = context.getInstance(config.getContent());
        _footer = optionalInstance(context, config.getFooter());
    }

    @Override
    public Control createControl(ViewContext context) {
        ReactControl header = _header != null
            ? (ReactControl) _header.createControl(context) : null;
        ReactControl content = (ReactControl) _content.createControl(context);
        ReactControl footer = _footer != null
            ? (ReactControl) _footer.createControl(context) : null;
        return new ReactAppShellControl(header, content, footer);
    }
}
```

### PanelElement

Wraps `ReactPanelControl`. A titled content panel with toolbar header.

```java
public class PanelElement extends ContainerElement {

    public interface Config extends ContainerElement.Config {
        @Name("title")
        String getTitle();
    }

    private final Config _config;

    public PanelElement(InstantiationContext context, Config config) {
        super(context, config);
        _config = config;
    }

    @Override
    public Control createControl(ViewContext context) {
        List<Control> childControls = createChildControls(context);
        ReactControl content = childControls.size() == 1
            ? (ReactControl) childControls.get(0)
            : new ReactStackControl(castToReact(childControls));
        return new ReactPanelControl(_config.getTitle(), content,
            false, false, false);
    }
}
```

### StackElement

Wraps `ReactStackControl`. A flexbox container for arranging children.

```java
public class StackElement extends ContainerElement {

    public interface Config extends ContainerElement.Config {
        @Name("direction")
        @StringDefault("column")
        String getDirection();

        @Name("gap")
        @StringDefault("default")
        String getGap();
    }

    private final Config _config;

    public StackElement(InstantiationContext context, Config config) {
        super(context, config);
        _config = config;
    }

    @Override
    public Control createControl(ViewContext context) {
        return new ReactStackControl(
            _config.getDirection(),
            _config.getGap(),
            "stretch",
            false,
            castToReact(createChildControls(context)));
    }
}
```

## Infrastructure

### ViewServlet

New servlet that bootstraps the view system. Separate from ReactServlet.

**Responsibilities:**
- Serves the initial HTML page for a view
- Resolves view path: URL path parameter or default from application config
- Caches parsed View configs and UIElement trees (shared across sessions)
- Creates per-session control tree via `createControl()`
- Renders HTML bootstrap page with React mount points

**URL mapping:** `/view/*`

**Default view:** Configurable via application configuration (same pattern as
existing LayoutServlet).

**Caching:** The View config + UIElement tree are parsed once per `.view.xml`
file and cached. The cache key is the file path. For development, file
modification timestamps can trigger cache invalidation.

**Session integration:** The ViewServlet needs access to the HTTP session to
create session-scoped control trees and register controls with the SSEUpdateQueue.

### View File Format

View files use `.view.xml` extension. The XML structure is derived from the
Config interfaces via TypedConfiguration:

```xml
<view xmlns="http://www.top-logic.com/ns/view/1.0">
  <content class="com.top_logic...AppShellElement">
    <header class="com.top_logic...StackElement">
      <children>
        <!-- header content -->
      </children>
    </header>
    <content class="com.top_logic...PanelElement">
      <title>Main Content</title>
      <children>
        <stack direction="row" gap="compact">
          <children>
            <!-- content elements -->
          </children>
        </stack>
      </children>
    </content>
  </content>
</view>
```

Note: With proper TypedConfiguration tag-name annotations on the Config
interfaces, the `class` attributes can be replaced with short element names
(e.g., `<app-shell>`, `<panel>`, `<stack>`).

## What This Proves

- UIElement tree is **shared** across sessions (parsed once, stateless)
- Control tree is **per-session** (created fresh, holds dynamic state)
- Composition works hierarchically via Config + InstantiationContext
- Existing ReactControls are reused without modification
- ViewContext is the extension point for future capabilities
- The view system is modular: new UIElement types can be added in any module

## What This Defers

- Channels / reactive data flow
- Model builders / data binding
- Commands on panels
- Security / access control
- Cross-view references
- Tabs, sidebar, navigation elements
- Dirty handling
- Forms and form fields
- Templates
- Dialog lifecycle
