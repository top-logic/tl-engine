# TL Views - New UI Definition Layer Specification

## Problem Summary

The current `LayoutComponent` system conflates several concerns into one heavy abstraction:

- **Layout** (splitter-based positioning)
- **Model binding** (ModelBuilder, Knowledge Base access)
- **Data flow** (ComponentChannel linking)
- **Security** (per-component access control)
- **Commands/Actions** (buttons, handlers)
- **Lifecycle** (validation, invalidation, visibility)
- **Rendering** (Control tree generation)

Every configurable UI piece must be a full `LayoutComponent`, even if it only needs one of
these capabilities. And layout is constrained to horizontal/vertical splitter panels.

## Core Design Principle: Progressive Capability

The base element is nearly zero-cost. Capabilities are added declaratively, only when needed.

```
UIElement (base - just renders)
  + model-builder   -> adds model binding (opt-in)
  + channel(...)    -> adds reactive data flow (opt-in)
  + secure(...)     -> adds access control (opt-in)
  + commands(...)   -> adds action handling (opt-in)
```

## 1. UIElement: The Foundation

### Configuration Model

Every element in the new view system is a `PolymorphicConfiguration<? extends UIElement>`.
This means the system is fully modular: new element types (configuration + implementation
pairs) can be defined in any module without modifying the core framework.

```java
/**
 * Base interface for all lightweight UI elements.
 *
 * <p>A UIElement is a factory for {@link Control}. Given a render context (including
 * resolved channel values), it produces a Control that is responsible for actual
 * rendering and incremental updates.</p>
 */
public interface UIElement {

    /**
     * Configuration interface for UIElement.
     *
     * <p>Every concrete UIElement has a corresponding Config sub-interface that is a
     * {@link PolymorphicConfiguration}. This makes all view element declarations
     * extensible: new element types can be registered in any module.</p>
     */
    interface Config extends PolymorphicConfiguration<UIElement> {
        // Common attributes shared by all elements (visibility, css-class, etc.)
    }

    /**
     * Creates the {@link Control} that renders this element.
     *
     * @param context The view context providing access to resolved channels,
     *                the enclosing view's state, and the rendering environment.
     * @return A Control responsible for writing HTML and handling incremental updates.
     */
    Control createControl(ViewContext context);
}
```

### Relationship to Control

`UIElement` is a **factory for `Control`**. The separation of concerns is:

| Concern         | UIElement                              | Control                              |
|-----------------|----------------------------------------|--------------------------------------|
| Lifecycle       | Configuration-time (static)            | Runtime (attached to browser page)   |
| Responsibility  | Produce a Control from configuration   | Render HTML, handle updates/events   |
| State           | Stateless (configured once)            | Stateful (tracks model, DOM state)   |
| Extensibility   | New types via PolymorphicConfiguration | Existing Control infrastructure      |

The existing `Control` interface (`com.top_logic.layout.Control`) is reused as-is. The
new layer sits above it: where today a `LayoutComponent` creates Controls internally, a
`UIElement` *is* the thing that creates the Control - nothing more.

### ViewContext: Hierarchical Scoping

The `ViewContext` passed to `createControl()` is not a flat, global object. It is an
**immutable, hierarchical scope** that container elements can enrich for their children.
This solves the problem of contextual dependencies (e.g., `<field>` needing to know
which form model to resolve its attribute against) without breaking the uniform
`UIElement` interface.

```java
/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>Container elements create derived contexts that add information for their
 * descendants. Child contexts inherit everything from parent contexts. Scoped
 * values (channels, form group, form model) are set by creating a new derived
 * instance - the context itself is not mutated for these.</p>
 *
 * <p>The one exception is {@link #registerCommand(CommandHandler)}, which is a
 * collection point: child elements contribute commands upward to the nearest
 * enclosing command scope (panel or dialog).</p>
 */
public interface ViewContext {

    /** Resolve a local or remote channel reference to its runtime channel. */
    ViewChannel resolveChannel(ChannelRef ref);

    /** Look up a command by name (from the enclosing panel/dialog scope). */
    CommandHandler getCommand(String name);

    /**
     * Register a command with the nearest enclosing command scope (panel or dialog).
     *
     * <p>Used by elements that contribute implicit commands (e.g., a table contributes
     * "search" and "reset filters" commands to the enclosing panel's toolbar). The
     * panel collects these during control creation and displays them alongside its
     * explicitly declared commands.</p>
     */
    void registerCommand(CommandHandler command);

    /** Security context for access checks. */
    SecurityContext getSecurityContext();

    /** Locale and theme for rendering. */
    RenderContext getRenderContext();

    /** The form group set by an enclosing {@code <form>} element, or null. */
    FormGroup getFormGroup();

    /** The form model object set by an enclosing {@code <form>} element, or null. */
    Object getFormModel();

    /**
     * Create a derived context with additional scoped information.
     * The derived context inherits all values from this context.
     */
    ViewContext derive();
}
```

**How scoping works**: When a container element processes its children, it can create a
derived context that carries additional information:

```java
// Inside Form.createControl(ViewContext parentContext):

// 1. Resolve the model from the channel
Object model = parentContext.resolveChannel(getConfig().getModel()).get();

// 2. Create a FormGroup for field state (dirty tracking, validation)
FormGroup formGroup = createFormGroup(model);

// 3. Create an enriched child context
ViewContext formContext = parentContext.derive()
    .withFormModel(model)
    .withFormGroup(formGroup);

// 4. Children see the form model through their context
for (UIElement child : getChildren()) {
    child.createControl(formContext);
}
```

A `<field>` element then queries the form context:

```java
// Inside Field.createControl(ViewContext context):

FormGroup formGroup = context.getFormGroup();
Object model = context.getFormModel();
String attribute = getConfig().getAttribute();

// Resolve attribute against the model, create and register form field
FormField field = formGroup.createField(model, attribute);
return field.createControl();
```

**This pattern is not form-specific.** Any container element can enrich the context for
its children:

- `<form>` pushes the form model and form group
- `<for-each>` could push the current iteration variable
- `<switch>` could push the matched case value
- A hypothetical `<with-model>` could scope a model for any subtree

**Validation**: Elements that depend on context (like `<field>` requiring an enclosing
`<form>`) should be validated at configuration parse time. If a `<field>` appears outside
a `<form>`, this is a configuration error detected early, even though the `UIElement` type
signature does not enforce it.

### Element Tree in XML

Because each XML element maps to a `PolymorphicConfiguration<? extends UIElement>`, the
view XML is a tree of typed configurations. Container elements have child properties of
type `List<PolymorphicConfiguration<? extends UIElement>>`:

```xml
<vbox>
  <heading text="Customer Details" />
  <form model="selectedCustomer">
    <field attribute="name" />
    <field attribute="email" />
  </form>
  <hbox gap="8px">
    <button label="Save" command="save" />
    <button label="Cancel" command="cancel" />
  </hbox>
</vbox>
```

Here `<vbox>` is `PolymorphicConfiguration<VBox>`, `<heading>` is
`PolymorphicConfiguration<Heading>`, etc. Each implementation class (`VBox`, `Heading`,
`Button`, ...) implements `UIElement` and produces the corresponding `Control`.

## 2. View Identity: File Path, Not Names

A view has **no explicit name**. Each view is defined in a `*.view.xml` file, and the
file path (relative to the application's well-known layout base folder) *is* the view's
identity. This makes views directly searchable in the filesystem.

```xml
<!-- File: customers/masterDetail.view.xml -->
<!-- The view's identity is "customers/masterDetail.view.xml" -->
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <split direction="horizontal" ratio="30:70">
    ...
  </split>
</view>
```

### Why Not Explicit Names?

In the current system, LayoutComponents have explicit names. When components are created
by templates, names must be parameterized. This leads to concatenated names mixed from
constants and template parameters, making it nearly impossible to find where a
component/channel is defined. File-path-as-identity eliminates this entire class of
problems:

- **No naming conflicts with templates**: Templates never need to generate view names.
  The file path is fixed and unique regardless of how many times a template is used.
- **Searchable**: `grep -r "organization/selector.view.xml"` instantly finds all
  references to that view's channels.
- **Consistent with current system**: The current system already uses `*.layout.xml`
  file names as component name scopes. This simplifies and extends that pattern.

### Cross-View Channel References

Remote channels are referenced directly by file path and channel name:

```
path/to/view.view.xml#channelName
```

No intermediate binding declarations or direction annotations are needed. See the
channel section below for details.

## 3. Channels: Unified Reactive Data Flow

There is a single concept for reactive data flow: **channels**. Channels serve both for
intra-view communication and for inter-view communication. There is no separate "state"
concept - a channel *is* the reactive state.

### Channel Declaration

Channels are declared at the view level. There are two kinds:

**Value channels** hold a mutable value, typically written by a UI element:

```xml
<channel name="selectedCustomer" type="tl.customers:Customer" />
```

**Derived channels** are read-only, computed from other channels via an expression.
They follow the same `inputs` + expression pattern as model builders - inputs are
declared explicitly and map 1:1 to expression parameters:

```xml
<derived-channel name="detailTitle"
    inputs="customers/list.view.xml#selectedCustomer"
    expr="customer -> if($customer != null,
             $customer.get(`tl.customers:Customer#name`),
             `No selection`)" />
```

Multi-input derived channels work naturally:

```xml
<derived-channel name="orgMismatch"
    inputs="organization/selector.view.xml#currentOrganization,
            customers/list.view.xml#selectedCustomer"
    expr="orga -> customer ->
        $customer != null
        && $customer.get(`tl.customers:Customer#organization`) != $orga" />
```

Example usage:

```xml
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <derived-channel name="detailTitle"
      inputs="selectedCustomer"
      expr="customer -> if($customer != null,
               $customer.get(`tl.customers:Customer#name`),
               `No selection`)" />

  ...
  <heading text="detailTitle" />
  ...
</view>
```

### Channel Semantics

A channel is a named, typed, observable value holder within a view:

```java
/**
 * A named reactive value within a view.
 *
 * <p>Channels hold a current value and notify listeners when it changes.
 * UI elements bind to channels to receive input and propagate output.</p>
 */
public interface ViewChannel {

    /** Current value. */
    Object get();

    /** Update value, notifying all listeners. Returns true if value changed. */
    boolean set(Object newValue);

    /** Listen for value changes. */
    void addListener(ChannelListener listener);

    void removeListener(ChannelListener listener);
}
```

### Channel References: No Prefix

Configuration attributes that expect channel references are typed as such in the
configuration interface. There is no `channel:` prefix - the attribute type makes it
unambiguous that a channel reference is expected:

```xml
<!-- "selectedCustomer" is a channel reference -->
<table selection="selectedCustomer">

<!-- Remote channel reference -->
<form model="customers/list.view.xml#selectedCustomer">
```

The syntax is:

```
<channelName>                              -- local channel in the same view
<path/to/view.view.xml>#<channelName>      -- remote channel in another view
```

This is unambiguous because:

- Attributes that accept channel references are declared with a channel-reference type in
  their configuration interface (e.g., `ChannelRef` rather than `String`).
- Channel references and TL-Script expressions are never mixed in the same attribute.
  An attribute is either a channel reference *or* an expression, determined by its type.

### Channel Bindings on Elements

Channel references on UI elements are primarily for **output** - writing user interaction
results back to a channel:

- **`selection="selectedCustomer"`** - The element *writes* to this channel. When the
  user selects something in a table or tree, the channel is updated.

**Input** to complex elements (tables, trees) comes through the model builder's `inputs`
property, not through a channel attribute on the element itself (see Section 4).

Simple elements like `<form>` that don't use a model builder still accept a `model`
channel reference for direct binding to a single channel value:

```xml
<form model="customers/list.view.xml#selectedCustomer">
```

This is the exception, not the rule - most data derivation goes through model builders.

### Derived Channels

Derived channels are read-only channels whose values are computed from other channels.
They use the same explicit `inputs` + expression pattern as model builders:

```xml
<derived-channel name="detailTitle"
    inputs="selectedCustomer"
    expr="customer -> if($customer != null,
             $customer.get(`tl.customers:Customer#name`),
             `No selection`)" />
```

The `inputs` property lists channel references (local or remote). Each input maps
positionally to a function parameter in the expression. The expression uses TL-Script.

This avoids embedding implicit variable references (like `$selectedCustomer`) in the
expression, which would not work well with remote channel references that have complex
path-based names. Instead, the inputs are declared separately and the expression uses
short, meaningful parameter names.

## 4. Model Builders: Complex Model Derivation

For complex UI elements like tables and trees, a static value from a channel is not
sufficient. The element needs logic to:

- Derive row/node objects from an input model object
- React to model changes (object creation/deletion) with incremental updates
- Determine whether a given object belongs to the element's data set

### The Multi-Input Problem

In the current LayoutComponent system, a model builder receives a single "model" value
from its component's model channel. When a builder needs multiple inputs (e.g., data from
two different channels), the workaround is a "composite" channel that merges multiple
values into a list/pair. The builder then has to decompose `$model[0]` and `$model[1]`
instead of working with named parameters. This is cumbersome and error-prone.

### Solution: Model Builders Declare Their Own Inputs

In the new system, the model builder itself declares which channels it reads from via an
`inputs` property. The expression function parameters map 1:1 to these inputs:

```xml
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />
  <derived-channel name="hasSelection"
      inputs="selectedCustomer"
      expr="c -> $c != null" />

  <split direction="horizontal" ratio="30:70">
    <table selection="selectedCustomer">
      <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
        inputs="organization/selector.view.xml#currentOrganization,
                organization/division.view.xml#selection"
        elements="orga -> division ->
            $orga.get(`tl.customers:Organization#customers`)
                 .filter(c -> $c.get(`tl.customers:Customer#division`) == $division)"
        supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
      />
    </table>

    <form model="selectedCustomer"
          visible="hasSelection">
      <field attribute="name" />
      <field attribute="email" />
      <field attribute="phone" />
    </form>
  </split>
</view>
```

Here:

- **`inputs="..., ..."`** - A list of channel references (local or remote) that feed the
  model builder. Each input maps to a function parameter in order.
- **`elements="orga -> division -> ..."`** - The first parameter `orga` receives the
  value of the first input (currentOrganization), `division` receives the second input.
  The expression works with named, meaningful parameters instead of indexed composites.
- **`selection="selectedCustomer"`** - The table's selection output is bound to a channel.
  This is on the table element itself, not the model builder, because selection is a UI
  concern independent of model derivation.

### Comparison with the Current System

| Aspect           | Old: LayoutComponent model builders     | New: View model builders                |
|------------------|-----------------------------------------|-----------------------------------------|
| Multiple inputs  | Composite channel workaround            | Direct: `inputs="channelA, channelB"`   |
| Expression args  | `$model[0]`, `$model[1]`               | Named: `orga -> division -> ...`        |
| Coupling         | Builder coupled to LayoutComponent      | Builder depends only on channels        |
| Input binding    | Implicit (component's model channel)    | Explicit (`inputs` property)            |
| Configuration    | Proven patterns (reuse design)          | Same property names and semantics       |
| Implementation   | Tied to LayoutComponent API             | New impl against channel-based API      |

### Relationship to Existing Model Builders

The existing model builder implementations (`ListModelByExpression`,
`TreeModelByExpression`, etc.) are tightly coupled to `LayoutComponent` and cannot be
reused directly. However, their **configuration patterns** and **design concepts** are
proven and should be carried over into the new system:

- Expression-based declarative configuration (`elements`, `supportsElement`, etc.)
- Incremental update detection (via `supportsElement`, `ElementUpdate`)
- The `PolymorphicConfiguration` extensibility pattern

The new model builder implementations will be written from scratch against a new
interface that takes channel inputs instead of a `LayoutComponent` reference. The
configuration interfaces can largely mirror the existing ones (same property names,
same expression semantics), making migration straightforward for users familiar with
the current system.

The `inputs` property is the key new addition: it replaces the implicit single-model
dependency on the component with an explicit list of channel references.

### Elements Without Model Builders

Not all data elements need model builders. A `<form>` binds directly to a channel value
via its `model` attribute - the channel provides the object whose attributes are
displayed. No derivation is needed:

```xml
<form model="selectedCustomer">
  <field attribute="name" />
  <field attribute="email" />
</form>
```

Model builders are for elements that need to **derive** a collection or tree from an
input (e.g., a table deriving rows from an organization object). Forms display attributes
of a single object directly.

## 5. Built-in UIElement Types

All built-in elements are `PolymorphicConfiguration<? extends UIElement>`. Custom modules
can add new element types by providing new configuration + implementation pairs.

### Layout Elements

Container UIElements that arrange their children using CSS layout:

| Element     | CSS Model    | Description                              |
|-------------|--------------|------------------------------------------|
| `<vbox>`    | Flexbox col  | Vertical flex container                  |
| `<hbox>`    | Flexbox row  | Horizontal flex container                |
| `<grid>`    | CSS Grid     | Grid layout with rows/columns            |
| `<split>`   | Flex + drag  | Resizable split (like today, but opt-in) |
| `<scroll>`  | Overflow     | Scrollable region                        |
| `<panel>`   | Block        | Display panel - the command-defining element |

Each of these is a `PolymorphicConfiguration<? extends ContainerElement>` where
`ContainerElement extends UIElement` and has a `List<PolymorphicConfiguration<UIElement>>`
property for children.

#### `<panel>` - Command Scope and Display Region

`<panel>` is the primary command-defining element. A panel represents a self-contained
display region with optional header, toolbar, and button bar. Commands are always defined
on a panel because the panel provides the display areas (toolbar, button bar) where
command buttons appear.

```java
public interface PanelElement extends ContainerElement {

    interface Config extends ContainerElement.Config {

        /** Panel title (i18n). */
        @Name("title")
        ResKey getTitle();

        /** Whether a toolbar area is rendered above the content. */
        @Name("toolbar")
        boolean getToolbar();

        /** Whether a button bar is rendered below the content. */
        @Name("button-bar")
        boolean getButtonBar();

        /** Whether the panel is collapsible. */
        @Name("collapsible")
        boolean getCollapsible();

        /** Local clique definitions for application-specific command grouping. */
        @Name("cliques")
        List<LocalCliqueConfig> getCliques();

        /** Commands scoped to this panel. */
        @Name("commands")
        List<PolymorphicConfiguration<CommandHandler>> getCommands();
    }
}
```

Commands on a panel declare a **placement** that determines where they appear:

```xml
<panel title="i18n:customers.detail" toolbar="true" button-bar="true">
  <commands>
    <!-- placement defaults from command type, can be overridden -->
    <command class="com.example.EditCommandHandler" />       <!-- toolbar (default) -->
    <command class="com.example.DeleteCommandHandler" />     <!-- toolbar (default) -->
    <command class="com.example.SaveCommandHandler" />       <!-- button-bar (default) -->
    <command class="com.example.CancelCommandHandler" />     <!-- button-bar (default) -->
    <command class="com.example.ExportCommandHandler" />       <!-- clique: export (menu) -->
  </commands>

  <form model="selectedCustomer">
    <field attribute="name" />
    <field attribute="email" />
  </form>
</panel>
```

**Placement** determines which display area the command's button appears in:

| Placement      | Location                                   | Default for                    |
|----------------|--------------------------------------------|--------------------------------|
| `toolbar`      | Horizontal bar above panel content         | Most action commands (edit, delete, create) |
| `button-bar`   | Horizontal bar below panel content         | Commit/cancel commands (save, apply, cancel) |
| `context-menu` | Right-click menu on data elements          | Row-specific actions           |

Each command type/implementation defines its own default placement. The `placement`
attribute on a command declaration overrides this default. This way, standard commands
work out of the box, but any command can be placed anywhere.

Note: There is no `burger-menu` placement. What was traditionally an "overflow" or
"hamburger" menu is simply a menu clique (see Command Cliques) within the `toolbar`
placement. Commands in a menu clique are collapsed into a single dropdown button.

**Context menu commands**: Commands with `placement="context-menu"` are collected by
data elements (`<table>`, `<tree>`) within the panel. When the user right-clicks a row,
the table/tree builds a context menu from all context-menu-placed commands of its
enclosing panel.

**Explicit `<button>` override**: A `<button>` element anywhere in the panel content
can explicitly render any command, regardless of its declared placement. This is
the escape hatch for placing a command in a non-standard location:

```xml
<panel toolbar="true">
  <commands>
    <command name="specialAction" class="..." placement="toolbar" />
  </commands>

  <form model="selectedCustomer">
    <field attribute="name" />
    <!-- Also render the command as an inline button inside the form -->
    <button command="specialAction" label="i18n:do.special" />
  </form>
</panel>
```

**Implicit commands from children**: In addition to explicitly declared commands, a panel
collects implicit commands contributed by its child elements via
`ViewContext.registerCommand()`. For example, a `<table>` inside a panel contributes
search/filter/export commands to the panel's toolbar. See Section 9
(Implicit Commands) for details.

**Panels without explicit commands**: A `<panel>` without `<commands>` can still display
implicit commands from its children (e.g., a panel containing only a table will show
the table's search/filter buttons). A panel with neither explicit nor implicit commands
is purely structural (collapsible section, titled region).

### Navigation Elements

These elements present a set of named children where one (or more) are visible at a time.
They share the same underlying mechanics (a list of children, a selection determining
which is active) but differ in visual presentation and configuration:

| Element      | Current Equivalent  | Description                                  |
|--------------|---------------------|----------------------------------------------|
| `<tabs>`     | TabComponent        | Tab bar with tab pages                       |
| `<sidebar>`  | Sidebar layout      | Side navigation panel selecting content area |
| `<tiles>`    | TileListComponent   | Card/tile grid, selecting one tile for detail |

All three follow the same pattern: they contain named children, one of which is active.
The active child can be written to a channel. However, each has its own configuration
because the visual requirements differ significantly.

Like `<panel>`, navigation elements can also define commands. The display area depends
on the element type:

- **`<tabs>`**: Commands appear as buttons at the right side of the tab bar.
- **`<sidebar>`**: Commands appear as additional entries below the navigation entries.

**`<tabs>`** - Tab bar with labeled pages:

```xml
<tabs selection="activeTab">
  <commands>
    <command class="com.example.AddTabHandler" />        <!-- tab-bar button -->
    <command class="com.example.CloseAllTabsHandler" />  <!-- tab-bar button -->
  </commands>

  <tab label="i18n:customers.overview" icon="people">
    <include view="customers/overview.view.xml" />
  </tab>
  <tab label="i18n:customers.orders" icon="shopping-cart">
    <include view="customers/orders.view.xml" />
  </tab>
  <tab label="i18n:customers.history" icon="clock">
    <include view="customers/history.view.xml" />
  </tab>
</tabs>
```

Configuration: tab labels (i18n), icons, closable tabs, lazy loading, dynamic
tab visibility. Commands on `<tabs>` render as buttons at the right end of the tab bar.

**`<sidebar>`** - Side navigation selecting a content area:

```xml
<sidebar selection="activePage" position="left" width="250px">
  <commands>
    <command class="com.example.CreateProjectHandler" />  <!-- sidebar entry -->
    <command class="com.example.SettingsHandler" />       <!-- sidebar entry -->
  </commands>

  <entry label="i18n:nav.customers" icon="people">
    <include view="customers/list.view.xml" />
  </entry>
  <entry label="i18n:nav.orders" icon="shopping-cart">
    <include view="orders/list.view.xml" />
  </entry>
  <group label="i18n:nav.admin">
    <entry label="i18n:nav.users" icon="person-gear">
      <include view="admin/users.view.xml" />
    </entry>
  </group>
</sidebar>
```

Configuration: position (left/right), width, collapsible, grouped entries. Commands
on `<sidebar>` render as additional entries below the navigation entries.

**`<tiles>`** - Card grid with model-builder-driven content:

```xml
<tiles selection="selectedProject" columns="3">
  <model-builder class="..."
    inputs="organization/selector.view.xml#currentOrganization"
    elements="orga -> $orga.get(`tl.projects:Organization#projects`)"
  />
  <tile-preview>
    <vbox padding="16px">
      <heading text="tile:name" />
      <text value="tile:description" />
      <text css-class="status" value="tile:status" />
    </vbox>
  </tile-preview>
</tiles>
```

Configuration: column count, tile preview template, model builder for tile objects.
Unlike `<tabs>` and `<sidebar>` which have statically configured children, `<tiles>`
derives its entries from a model builder (like `<table>` derives rows).

### Conditional Elements

| Element        | Description                                              |
|----------------|----------------------------------------------------------|
| `<switch>`     | Selects one child based on a channel-driven condition    |
| `<responsive>` | Selects one child based on viewport size                 |

Both are conditional elements that render exactly one child from a set of candidates.
They differ in what drives the condition: `<switch>` evaluates channel values,
`<responsive>` evaluates the client viewport.

**`<switch>`** - Channel-driven conditional:

```xml
<switch>
  <case>
    <condition input="customers/list.view.xml#selectedCustomer"
               expr="c -> $c.instanceOf(`tl.customers:SpecialCustomer`)" />
    <content>
      <form model="customers/list.view.xml#selectedCustomer">
        <field attribute="specialDiscount" />
        <field attribute="vipLevel" />
        <field attribute="name" />
        <field attribute="email" />
      </form>
    </content>
  </case>
  <default>
    <form model="customers/list.view.xml#selectedCustomer">
      <field attribute="name" />
      <field attribute="email" />
    </form>
  </default>
</switch>
```

The `<condition>` element follows the same `input`/`inputs` + expression pattern as
derived channels and model builders.

**`<responsive>`** - Viewport-driven conditional:

```xml
<responsive>
  <when max-width="768px">
    <!-- Mobile: stacked layout -->
    <vbox>
      <include view="customers/selector.view.xml" />
      <include view="customers/detail.view.xml" />
    </vbox>
  </when>
  <when max-width="1200px">
    <!-- Tablet: narrow split -->
    <split direction="horizontal" ratio="40:60">
      <include view="customers/selector.view.xml" />
      <include view="customers/detail.view.xml" />
    </split>
  </when>
  <otherwise>
    <!-- Desktop: wide split with sidebar -->
    <sidebar position="left" width="250px">
      <entry label="i18n:nav.customers">
        <split direction="horizontal" ratio="30:70">
          <include view="customers/selector.view.xml" />
          <include view="customers/detail.view.xml" />
        </split>
      </entry>
    </sidebar>
  </otherwise>
</responsive>
```

`<responsive>` evaluates on the client side and re-evaluates when the viewport is
resized. The `<when>` conditions are checked in order; the first match wins.
`<otherwise>` is the fallback. This enables the same view definition to serve mobile,
tablet, and desktop layouts without separate view files.

### Composition Elements

| Element      | Description                                            |
|--------------|--------------------------------------------------------|
| `<include>`  | Include another `*.view.xml` inline                    |
| `<dialog>`   | Defines modal dialog content, opened by commands       |

**`<include>`** embeds another view file inline. This is the primary composition
mechanism beyond templates:

```xml
<split direction="horizontal" ratio="30:70">
  <include view="organization/selector.view.xml" />
  <include view="customers/detail.view.xml" />
</split>
```

The included view's channels become reachable via the included file's path, as with
any cross-view channel reference.

**`<dialog>`** defines modal dialog content within a view. Like `<panel>`, a dialog is
a command-defining element - it has its own `<commands>` section and a button bar where
commit/cancel buttons appear. The dialog is not rendered inline - it is opened by a
command and closed explicitly:

```xml
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <dialog name="editDialog" title="i18n:customers.edit.title">
    <commands>
      <command class="com.example.SaveCustomerHandler" />     <!-- button-bar (default) -->
      <command class="com.example.CancelDialogHandler" />     <!-- button-bar (default) -->
    </commands>

    <form model="selectedCustomer">
      <field attribute="name" />
      <field attribute="email" />
    </form>
  </dialog>

  <panel toolbar="true">
    <commands>
      <command name="openEdit" class="com.example.OpenEditHandler"
               opens-dialog="editDialog" />
    </commands>

    <table selection="selectedCustomer">
      ...
    </table>
  </panel>
</view>
```

A dialog always renders a button bar for its commands (the dialog's commit/cancel
buttons). No explicit `button-bar="true"` is needed - this is inherent to dialogs.

### Data Elements

Complex elements that use model builders to derive data from channel values:

| Element        | Model Builder Type   | Description                                |
|----------------|----------------------|--------------------------------------------|
| `<table>`      | ListModelBuilder     | Table with columns, sorting, filters       |
| `<tree>`       | TreeModelBuilder     | Tree with expand/collapse, single/multi-select |
| `<tree-table>` | TreeModelBuilder     | Tree with table columns (hierarchical grid)|
| `<selector>`   | ListModelBuilder     | Single-select dropdown from derived options|

**`<tree>`** - Hierarchical navigation with selection:

```xml
<tree selection="selectedNode">
  <model-builder class="..."
    inputs="organization/selector.view.xml#currentOrganization"
    rootNode="orga -> $orga"
    children="node -> $node.get(`tl.org:OrgUnit#children`)"
  />
</tree>
```

Configuration: root visibility, selection mode (single/multi), expand depth,
drag-drop support, context menu.

**`<tree-table>`** - Combines tree hierarchy with table columns:

```xml
<tree-table selection="selectedNode">
  <model-builder class="..."
    inputs="rootChannel"
    rootNode="root -> $root"
    children="node -> $node.get(`tl.org:OrgUnit#children`)"
  />
  <columns>
    <column attribute="name" />
    <column attribute="budget" />
    <column attribute="headcount" />
  </columns>
</tree-table>
```

**`<selector>`** - Lightweight single-select dropdown:

```xml
<selector selection="selectedStatus">
  <model-builder class="..."
    inputs="..."
    elements="model -> all(`tl.core:Status`)"
  />
</selector>
```

Unlike `<table>`, a `<selector>` renders as a compact dropdown, not a full grid. It
is the appropriate element for picking one value from a small option set.

#### Forms

`<form>` is a container UIElement that displays and edits attributes of a model object.
It is more complex than a simple field list:

**Basic form**: Explicit fields in explicit layout.

```xml
<form model="selectedCustomer">
  <vbox>
    <heading text="Contact Information" />
    <grid columns="2">
      <field attribute="name" />
      <field attribute="email" />
      <field attribute="phone" />
      <field attribute="address" />
    </grid>
  </vbox>
</form>
```

Forms are containers: they can embed arbitrary UIElements (layout elements, headings,
tables, etc.) alongside `<field>` elements. A `<field>` resolves its attribute against
the form's model object.

**Form with dynamic attributes**: Explicit fields for known base type attributes plus
a catch-all group for additional attributes defined by the concrete subtype.

```xml
<form model="selectedCustomer">
  <vbox>
    <grid columns="2">
      <!-- Explicitly laid out base type fields -->
      <field attribute="name" />
      <field attribute="email" />
      <field attribute="phone" />
    </grid>

    <!-- All remaining attributes of the concrete subtype -->
    <dynamic-attributes />
  </vbox>
</form>
```

`<dynamic-attributes>` renders all attributes of the model object's concrete type that
are not already displayed by an explicit `<field>` element in the same form. This handles
the common pattern of a stable base layout plus whatever extras a subtype adds, without
enumerating all possible subtypes.

**Form with embedded complex structure**:

```xml
<form model="selectedCustomer">
  <vbox>
    <grid columns="2">
      <field attribute="name" />
      <field attribute="email" />
    </grid>

    <heading text="Orders" />
    <table selection="selectedOrder">
      <model-builder class="..."
        inputs="selectedCustomer"
        elements="customer -> $customer.get(`tl.customers:Customer#orders`)"
      />
      <columns>
        <column attribute="date" />
        <column attribute="amount" />
      </columns>
    </table>
  </vbox>
</form>
```

**Polymorphic forms** are handled by the generic `<switch>` element (see above), not
by a form-specific mechanism. Use `<switch>` when completely different layouts are needed
per type. Use `<dynamic-attributes>` when the base layout is stable and subtypes only
add fields.

**Edit/view mode**: The current TopLogic system has an EditComponent with explicit mode
switching (view mode shows read-only text, edit mode shows input fields). How edit/view
mode works in the new system is an open question - it could be a configuration property
on `<form>` (e.g., `mode="channel:editMode"`), a mode in the ViewContext, or a separate
mechanism. See Open Questions.

### Interaction Elements

| Element      | Description                                 |
|--------------|---------------------------------------------|
| `<button>`   | Action trigger, references a command         |
| `<link>`     | Clickable link                               |
| `<menu>`     | Dropdown menu                                |

Note: There is no standalone `<toolbar>` element. Toolbars and button bars are display
areas of `<panel>` (and `<dialog>`), automatically populated from the panel's commands
based on their placement. An explicit `<button>` can place any command anywhere as an
override (see `<panel>` in Layout Elements).

### Wizard Elements

| Element      | Current Equivalent        | Description                          |
|--------------|---------------------------|--------------------------------------|
| `<wizard>`   | AssistentComponent        | Multi-step workflow with progress    |

```xml
<wizard selection="currentStep">
  <step label="i18n:wizard.step1" icon="1">
    <form model="wizardData">
      <field attribute="name" />
      <field attribute="type" />
    </form>
  </step>
  <step label="i18n:wizard.step2" icon="2">
    <form model="wizardData">
      <field attribute="details" />
      <field attribute="options" />
    </form>
  </step>
  <step label="i18n:wizard.step3" icon="3">
    <vbox>
      <heading text="Summary" />
      <text value="..." />
    </vbox>
  </step>
</wizard>
```

Configuration: step labels, step validation, linear vs. free navigation, progress
display.

### Content Elements

| Element       | Description                    |
|---------------|--------------------------------|
| `<heading>`   | Text heading (h1-h6)          |
| `<text>`      | Static or channel-bound text   |
| `<icon>`      | Theme icon display             |
| `<image>`     | Image display                  |
| `<separator>` | Visual separator               |
| `<spacer>`    | Flexible space                 |
| `<breadcrumb>`| Navigation trail               |

**`<breadcrumb>`** displays a navigation trail based on the current view/selection
hierarchy:

```xml
<breadcrumb input="selectedNode" />
```

### Extension Elements (from feature modules)

These elements are not part of the core framework but provided by optional modules:

| Element    | Module               | Description                           |
|------------|----------------------|---------------------------------------|
| `<chart>`  | com.top_logic.reporting | Data visualization (bar, line, pie) |

Extension modules register their element types via `PolymorphicConfiguration`, making
them available in view XML like any built-in element.

## 6. Templates: Reusing the Existing Template System

The template system follows the existing `*.template.xml` pattern closely. A view
template defines:

1. A `<properties>` block with typed configuration parameters.
2. A body that produces a `PolymorphicConfiguration<? extends UIElement>` (instead of
   `PolymorphicConfiguration<? extends LayoutComponent>` in the old system).

### Template Definition

```xml
<!-- masterDetail.view.template.xml -->
<config:template
    xmlns:config="http://www.top-logic.com/ns/config/6.0"
    groups="commons"
>
  <properties>
    <property name="title" type="ResKey">
      <mandatory />
    </property>
    <property name="masterRatio" type="String">
      <default value="30:70" />
    </property>
    <property name="masterContent"
              type="PolymorphicConfiguration&lt;UIElement&gt;">
      <mandatory />
    </property>
    <property name="detailContent"
              type="PolymorphicConfiguration&lt;UIElement&gt;">
      <mandatory />
    </property>
  </properties>

  <vbox>
    <heading text="${title}" />
    <split direction="horizontal" ratio="${masterRatio}">
      ${masterContent}
      ${detailContent}
    </split>
  </vbox>
</config:template>
```

### Template Call

```xml
<config:template-call template="masterDetail.view.template.xml">
  <arguments title="i18n:customers.title" masterRatio="40:60">
    <masterContent>
      <table selection="selectedCustomer">
        <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
          inputs="organization"
          elements="model -> all(`tl.customers:Customer`)"
          supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
        />
      </table>
    </masterContent>
    <detailContent>
      <form model="selectedCustomer">
        <field attribute="name" />
        <field attribute="email" />
      </form>
    </detailContent>
  </arguments>
</config:template-call>
```

### Key Design Points

- Template parameters are typed configurations, validated at parse time.
- Parameters can be primitive values (`String`, `ResKey`, `int`) or complex
  configurations (`PolymorphicConfiguration<UIElement>`), allowing entire sub-trees
  to be passed as parameters.
- The `${variable}` substitution mechanism from the existing template system is reused.
- Templates are discovered by `DynamicComponentService` (extended to scan
  `*.view.template.xml` alongside `*.template.xml`).
- Assistant templates work the same way: a parameterized wrapper around a base template.
- **Templates never generate view names**: Since view identity comes from the file path,
  templates produce UIElement configurations that are embedded into a `*.view.xml` file.
  The file path provides the identity - the template content is anonymous.

## 7. View Definition and Hosting

### What Is a View?

A `<view>` is the top-level container defined in a `*.view.xml` file. It:

1. Declares the channels available to its element tree.
2. Contains a root `UIElement` (its content).
3. Is identified by its file path (no explicit `name` attribute).
4. Can be hosted inside a `LayoutComponent` for backward compatibility.

```java
/**
 * A view is a self-contained UI definition with its own channel scope.
 *
 * <p>The view's identity is its file path relative to the application's layout
 * base folder (e.g., "customers/masterDetail.view.xml"). This identity is used
 * for cross-view channel references.</p>
 */
public interface ViewDefinition {

    interface Config extends ConfigurationItem {

        /** Channel declarations. */
        @Name("channels")
        List<ViewChannelConfig> getChannels();

        /** The root UI element tree. */
        @Name("content")
        @Mandatory
        PolymorphicConfiguration<UIElement> getContent();
    }
}
```

Note: No `getCommands()` - commands are defined on `<panel>` and `<dialog>` elements
within the content tree, not at the view level. Each panel is a self-contained command
scope with its own toolbar/button-bar display areas.

No `getName()` - the identity comes from the file system.

### File Structure

```
WEB-INF/views/                          -- well-known base folder
  customers/
    masterDetail.view.xml               -- identity: customers/masterDetail.view.xml
    selector.view.xml                   -- identity: customers/selector.view.xml
  organization/
    overview.view.xml                   -- identity: organization/overview.view.xml
```

A channel in `customers/masterDetail.view.xml` named `selectedCustomer` is referenced
from anywhere as:

```
customers/masterDetail.view.xml#selectedCustomer
```

### Hosting in the Existing System

A bridge `LayoutComponent` hosts a view inside the existing component tree:

```xml
<!-- In a classic .layout.xml -->
<component class="com.top_logic.layout.view.ViewHostComponent">
  <view ref="customers/masterDetail.view.xml" />
</component>
```

`ViewHostComponent` bridges the two worlds:

- It is a `LayoutComponent` (participates in the existing component tree).
- It loads and instantiates the referenced `*.view.xml` file.
- It resolves cross-view channel references by locating the target view's runtime
  channel instance within the same application.
- It calls `UIElement.createControl(viewContext)` on the root element to produce
  the Control tree for rendering.

### Embedding Legacy Components

Conversely, an existing `LayoutComponent` can be embedded inside a view:

```xml
<!-- File: mixed/overview.view.xml -->
<view>
  <vbox>
    <heading text="Overview" />
    <legacy-component
        class="com.top_logic.layout.table.component.TableComponent">
      <!-- Full existing LayoutComponent config -->
    </legacy-component>
  </vbox>
</view>
```

`<legacy-component>` is a `UIElement` that wraps an existing `LayoutComponent`,
adapting it into the new element tree.

## 8. Styling

Elements support styling through direct attributes and CSS class references:

```xml
<vbox padding="16px" gap="8px">
  <text css-class="tlHeadingLarge">Title</text>
  <hbox gap="4px" align="center">
    <icon name="person" />
    <text css-class="tlBody">Some text</text>
  </hbox>
</vbox>
```

- Common layout properties (`padding`, `gap`, `margin`, `width`, `height`) are direct
  configuration attributes on layout elements.
- Visual styling uses `css-class` to reference existing theme CSS classes.
- This avoids inventing a parallel styling system - the existing theme infrastructure
  is reused.

## 9. Commands and Actions

### Command-Defining Elements

Commands are defined on elements that have display areas for them, not at the view level.
The primary command-defining elements are:

| Element      | Display Areas                                              |
|--------------|------------------------------------------------------------|
| `<panel>`    | Toolbar (above content), button bar (below content)        |
| `<dialog>`   | Button bar (always present)                                |
| `<tabs>`     | Buttons at the right side of the tab bar                   |
| `<sidebar>`  | Additional entries below the navigation entries            |

Each command-defining element is a self-contained command scope: it owns the commands
and provides the display areas where they appear. The most common case is `<panel>`:

```xml
<!-- File: customers/detail.view.xml -->
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <panel title="i18n:customers.detail" toolbar="true" button-bar="true">
    <commands>
      <command class="com.example.EditCommandHandler" />
      <command class="com.example.DeleteCommandHandler" />
      <command class="com.example.SaveCommandHandler" />
      <command class="com.example.CancelCommandHandler" />
    </commands>

    <form model="selectedCustomer">
      <field attribute="name" />
      <field attribute="email" />
    </form>
  </panel>
</view>
```

This design solves the master-detail problem: in a view with two panels side by side,
each panel has its own commands displayed in its own toolbar/button-bar. There is no
ambiguity about which commands belong where.

### Command Placement

Each command declares where it should appear via the `placement` property. Every command
type/implementation defines a **default placement** so that standard commands work out
of the box without explicit placement configuration:

```java
public interface CommandHandler {

    interface Config extends PolymorphicConfiguration<CommandHandler> {

        /** Where this command's button appears. Default is defined by the implementation. */
        @Name("placement")
        CommandPlacement getPlacement();

        /** Which clique this command belongs to (for ordering and visual grouping). */
        @Name("clique")
        CommandClique getClique();
    }
}

public enum CommandPlacement {
    /** Toolbar above panel content (default for most action commands). */
    TOOLBAR,
    /** Button bar below panel content (default for commit/cancel commands). */
    BUTTON_BAR,
    /** Context menu on data elements (default for row-specific actions). */
    CONTEXT_MENU
}
```

The default placement and clique are part of the command's implementation:

```java
public class SaveCommandHandler extends AbstractCommandHandler {

    public interface Config extends AbstractCommandHandler.Config {
        @Override
        default CommandPlacement getPlacement() {
            return CommandPlacement.BUTTON_BAR;
        }

        @Override
        default CommandClique getClique() {
            return CommandClique.COMMIT;
        }
    }
}
```

A command's placement and clique can always be overridden in the panel's `<commands>`
declaration:

```xml
<panel toolbar="true">
  <commands>
    <!-- Override clique for a specific command -->
    <command class="com.example.DuplicateHandler" clique="create" />
    <!-- ExportHandler defaults to 'export' clique (menu display) -->
    <command class="com.example.ExportHandler" />
  </commands>
  ...
</panel>
```

### Command Cliques

Commands within a placement area (toolbar, button bar, context menu) are ordered and
visually grouped by **cliques**. A clique defines a semantic category for a command.
Commands in the same clique appear together; different cliques are separated visually
(e.g., by a separator line or spacing).

#### Clique Display Mode

Each clique has a **display mode** that controls how its commands are presented:

- **`inline`** (default): Commands are shown directly in the display area, separated
  from adjacent cliques by visual separators.
- **`menu`**: Commands are collapsed into a single button/entry that opens a submenu
  or dropdown. The clique defines a `label` and `icon` for the trigger.

This generalizes what was traditionally a "burger menu" or "overflow menu". Any clique
can be a menu clique - there is no special `burger-menu` placement.

#### Standard Cliques

A small, well-known set of standard cliques covers most business application needs.
Each command type defines a default clique, so developers rarely need to specify one
explicitly:

| Clique      | Typical Commands                         | Display  | Order |
|-------------|------------------------------------------|----------|-------|
| `create`    | New, import, duplicate                   | `inline` | 1     |
| `edit`      | Edit, lock, unlock                       | `inline` | 2     |
| `delete`    | Delete, remove                           | `inline` | 3     |
| `commit`    | Save, apply, cancel                      | `inline` | 4     |
| `navigate`  | Open, goto, back                         | `inline` | 5     |
| `view`      | Search, reset filters, column config     | `menu`   | 6     |
| `export`    | Excel, PDF, print                        | `menu`   | 7     |
| `more`      | Settings, help, about                    | `menu`   | 8     |

The clique order determines the display order within a placement area. Commands within
the same clique appear in their declaration order (explicit commands) or registration
order (implicit commands). Inline cliques are separated visually; menu cliques are
rendered as dropdown buttons.

```
Toolbar:  [New] [Import]  |  [Edit] [Lock]  |  [Delete]  |  [View ▾]  [Export ▾]
           ---- create ---    --- edit ---      - delete -    - view -    - export -
```

Menu cliques like `view` and `export` collapse their commands into dropdowns. If a
menu clique contains only a single command, it can be rendered as a direct button
instead of a dropdown (implementation choice).

#### Nested Cliques

Menu cliques can contain **sub-cliques** to organize their dropdown content into
visually separated groups:

```xml
<panel toolbar="true">
  <cliques>
    <clique name="export">
      <!-- Sub-cliques within the 'export' menu -->
      <clique name="export.document" />
      <clique name="export.labels" />
    </clique>
  </cliques>

  <commands>
    <command class="com.example.ExportExcelHandler" clique="export.document" />
    <command class="com.example.ExportPdfHandler" clique="export.document" />
    <command class="com.example.PrintLabelsHandler" clique="export.labels" />
  </commands>
  ...
</panel>
```

```
Toolbar:  ...  [Export ▾]
                ├─ Excel
                ├─ PDF
                ├───────── (separator)
                └─ Print labels
```

Sub-cliques within a menu create separator-divided groups in the dropdown. This works
recursively - a sub-clique can itself be a menu clique, producing nested submenus:

```
Context menu:  Edit
               Delete
               ─────────
               Export  ►  ├─ Excel
                          ├─ PDF
                          ├─────────
                          └─ Print labels
```

This eliminates the need for a dedicated "burger menu" concept. What was traditionally
an overflow menu is simply the last menu clique(s) in the toolbar (e.g., `view`,
`export`, `more`).

#### Default Cliques

Like placement, the default clique is defined by the command's implementation:

```java
public class DeleteCommandHandler extends AbstractCommandHandler {

    public interface Config extends AbstractCommandHandler.Config {
        @Override
        default CommandClique getClique() {
            return CommandClique.DELETE;
        }
    }
}
```

Implicit commands from child elements (e.g., table search, tree expand-all) also
declare their clique. Standard implicit commands default to `view`.

#### Local Cliques

Panels can define **local cliques** for application-specific groupings that don't
warrant a global standard clique:

```xml
<panel toolbar="true">
  <cliques>
    <!-- Local clique, ordered between 'edit' and 'delete' -->
    <clique name="workflow" after="edit" />
  </cliques>

  <commands>
    <command class="com.example.ApproveHandler" clique="workflow" />
    <command class="com.example.RejectHandler" clique="workflow" />
    <command class="com.example.EscalateHandler" clique="workflow" />
    <command class="com.example.EditHandler" />     <!-- clique: edit (default) -->
    <command class="com.example.DeleteHandler" />   <!-- clique: delete (default) -->
  </commands>
  ...
</panel>
```

```
Toolbar:  [Edit]  |  [Approve] [Reject] [Escalate]  |  [Delete]
          - edit -   ---------- workflow -----------    - delete -
```

Local cliques are scoped to the panel that defines them. They use `after` (or `before`)
to specify their position relative to standard cliques. This avoids polluting the
global list while still allowing custom grouping where needed.

Local cliques can also use `display="menu"` to collapse their commands into a dropdown:

```xml
<panel toolbar="true">
  <cliques>
    <clique name="workflow" after="edit" display="menu"
            label="i18n:workflow" icon="workflow" />
  </cliques>

  <commands>
    <command class="com.example.ApproveHandler" clique="workflow" />
    <command class="com.example.RejectHandler" clique="workflow" />
    <command class="com.example.EscalateHandler" clique="workflow" />
  </commands>
  ...
</panel>
```

```
Toolbar:  [Edit]  |  [Workflow ▾]  |  [Delete]
                     ├─ Approve
                     ├─ Reject
                     └─ Escalate
```

### Context Menu Commands

Commands with `placement="context-menu"` are not rendered in the toolbar or button bar.
Instead, data elements (`<table>`, `<tree>`) within the panel collect these commands
and present them when the user right-clicks a row:

```xml
<panel toolbar="true">
  <commands>
    <command class="com.example.EditRowHandler" placement="context-menu" />
    <command class="com.example.DeleteRowHandler" placement="context-menu" />
    <command class="com.example.CreateHandler" />  <!-- toolbar (default) -->
  </commands>

  <table selection="selectedCustomer">
    <model-builder class="..." inputs="..." elements="..." />
    <columns>
      <column attribute="name" />
      <column attribute="email" />
    </columns>
  </table>
</panel>
```

The table automatically builds its context menu from the enclosing panel's context-menu
commands. The command's executability rule determines which entries are enabled/visible
for the right-clicked row.

### Explicit Button Override

An explicit `<button>` anywhere in the panel content can render any command, regardless
of its declared placement. This is the escape hatch for non-standard layouts:

```xml
<panel toolbar="true">
  <commands>
    <command name="approve" class="com.example.ApproveHandler" />
  </commands>

  <form model="selectedCustomer">
    <field attribute="name" />
    <!-- Render the command inline, in addition to its toolbar button -->
    <button command="approve" label="i18n:approve.now" />
  </form>
</panel>
```

### Dialog Commands

Dialogs are also command-defining elements. A dialog always renders a button bar for
its commit/cancel commands:

```xml
<dialog name="confirmDelete" title="i18n:confirm.delete">
  <commands>
    <command class="com.example.ConfirmDeleteHandler" />   <!-- button-bar -->
    <command class="com.example.CancelDialogHandler" />    <!-- button-bar -->
  </commands>

  <text value="i18n:confirm.delete.message" />
</dialog>
```

### Implicit Commands

Not all commands are explicitly declared in `<commands>`. Some elements contribute
**implicit commands** - commands that are inherent to the element's functionality and
need to appear in the nearest enclosing command scope (panel or dialog).

**Panel and dialog implicit commands**:
- `<panel collapsible="true">` contributes a collapse/expand toggle command to its own
  toolbar.
- `<dialog>` contributes a close/cancel command to its own button bar.

These are self-contained: the element contributes commands to itself.

**Child element implicit commands**:
Elements within a panel can contribute commands that bubble up to the enclosing panel's
display areas. Examples:

| Element    | Implicit Commands                                | Default Placement | Default Clique |
|------------|--------------------------------------------------|-------------------|----------------|
| `<table>`  | Search, reset filters, column config, export     | `toolbar`         | `view`/`export`|
| `<tree>`   | Expand all, collapse all                         | `toolbar`         | `view`         |
| `<form>`   | (potentially) reset form                         | `toolbar`         | `view`         |

These elements call `ViewContext.registerCommand()` during control creation. The
enclosing panel collects all registered commands and displays them according to their
placement, alongside the panel's explicitly declared commands.

```java
// Inside TableElement.createControl(ViewContext context):

// Register implicit table commands with the enclosing panel
context.registerCommand(new TableSearchCommand(tableModel));
context.registerCommand(new ResetFiltersCommand(tableModel));
context.registerCommand(new ColumnConfigCommand(tableModel));
```

This mechanism generalizes the current system where `TableComponent` adds toolbar
buttons for search/filter/export. The difference is that the element does not need to
know *where* or *how* the commands are displayed - it just registers them, and the
enclosing panel handles placement.

**Ordering**: Both explicit and implicit commands are ordered by their clique (see
Command Cliques). Within a clique, explicit commands appear in declaration order,
followed by implicit commands in registration order. Placement determines the display
area; clique determines position and grouping within that area.

### Command Resolution

Command handlers are `PolymorphicConfiguration<? extends CommandHandler>`, reusing the
existing command infrastructure. The enclosing element's `ViewContext` resolves command
references and provides the execution environment. A `<button command="name">` is
resolved against the commands of the nearest enclosing command-defining element (panel,
dialog, tabs, or sidebar) - both explicit and implicit.

## 10. Security

Security is opt-in per element:

```xml
<panel secure="role:admin">
  <text>Admin-only content</text>
</panel>

<button command="delete" secure="permission:customers.delete" />
```

Elements without a `secure` attribute are always visible (subject to their parent's
visibility). This replaces the current system where every `LayoutComponent` participates
in security evaluation regardless of need.

## Summary: Architecture Layers

```
+------------------------------------------------------------------+
|  View Definition (*.view.xml / *.view.template.xml)              |
|  - Identity = file path (no explicit names)                      |
|  - Channels (reactive data flow, locally and cross-view)         |
|  - UIElement tree (PolymorphicConfiguration<UIElement>)           |
|  - Commands (scoped to <panel> and <dialog>)                      |
+------------------------------------------------------------------+
        |                                    |
        | createControl()                    | model-builder (owns its inputs)
        v                                    v
+------------------------+    +--------------------------------+
|  Control (existing)    |    |  ModelBuilder (new impl)         |
|  - Renders HTML        |    |  - inputs = channel ref list    |
|  - Handles updates     |    |  - expressions map 1:1 to      |
|  - Processes events    |    |    input parameters             |
|                        |    |  - config patterns from existing |
+------------------------+    +--------------------------------+
        |
        v
+------------------------------------------------------------------+
|  ViewHostComponent (bridge to existing LayoutComponent tree)     |
|  - Hosts a *.view.xml inside a LayoutComponent                   |
|  - Resolves cross-view channel references                        |
+------------------------------------------------------------------+
        |
        v
+------------------------------------------------------------------+
|  Existing LayoutComponent tree                                   |
+------------------------------------------------------------------+
```

## Element Capability Matrix

| Capability             | Current System            | New System                             |
|------------------------|---------------------------|----------------------------------------|
| Identity               | Component name (in config)| File path (*.view.xml)                 |
| Rendering              | Every LayoutComponent     | Every UIElement (lightweight)          |
| Layout                 | Fixed splitter panels     | CSS Flex/Grid (flexible)               |
| Model binding          | Mandatory ModelBuilder    | Opt-in, builder declares own inputs    |
| Data flow              | ComponentChannel wiring   | Channels (local + path#name refs)      |
| Multi-input models     | Composite channel hack    | Builder `inputs` with named params     |
| Security               | Per-component mandatory   | Per-element opt-in `secure` attr       |
| Commands               | Per-component             | Per-panel, with placement-based display |
| Composition            | *.template.xml            | *.view.template.xml (same mechanism)   |
| Extensibility          | LayoutComponent subclass  | New UIElement + Config pair (modular)   |
| Backward compat        | N/A                       | ViewHostComponent / legacy-component   |

## Complete Example

Three views collaborating via cross-view channel references, with panel-scoped commands:

```xml
<!-- File: organization/selector.view.xml -->
<view>
  <channel name="currentOrganization" type="tl.customers:Organization" />

  <panel title="i18n:organizations" toolbar="true">
    <commands>
      <command class="com.example.CreateOrganizationHandler" />
    </commands>

    <tree selection="currentOrganization">
      <model-builder class="com.top_logic.model.search.providers.TreeModelByExpression"
        rootNode="all(`tl.customers:Organization`).filter(o -> $o.get(`parent`) == null)"
      />
    </tree>
  </panel>
</view>
```

```xml
<!-- File: customers/list.view.xml -->
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <panel title="i18n:customers" toolbar="true">
    <commands>
      <command class="com.example.CreateCustomerHandler" />
      <command class="com.example.EditCustomerHandler" placement="context-menu" />
      <command class="com.example.DeleteCustomerHandler" placement="context-menu" />
    </commands>

    <table selection="selectedCustomer">
      <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
        inputs="organization/selector.view.xml#currentOrganization"
        elements="orga -> $orga.get(`tl.customers:Organization#customers`)"
        supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
      />
      <columns>
        <column attribute="name" />
        <column attribute="email" />
        <column attribute="status" />
      </columns>
    </table>
  </panel>
</view>
```

```xml
<!-- File: customers/detail.view.xml -->
<view>
  <derived-channel name="hasSelection"
      inputs="customers/list.view.xml#selectedCustomer"
      expr="customer -> $customer != null" />

  <panel title="i18n:customer.detail" toolbar="true" button-bar="true"
         visible="hasSelection">
    <commands>
      <command class="com.example.EditCommandHandler" />
      <command class="com.example.SaveCommandHandler" />
      <command class="com.example.CancelCommandHandler" />
    </commands>

    <form model="customers/list.view.xml#selectedCustomer">
      <field attribute="name" />
      <field attribute="email" />
      <field attribute="phone" />
      <field attribute="status" />
    </form>
  </panel>
</view>
```

All three views reference each other's channels by file path. No names to invent, no
conflicts possible, and `grep` finds all usages instantly. Each panel owns its commands
and displays them in its own toolbar/button-bar - there is no ambiguity about which
commands belong to which part of the UI.

## Configuration Plug-in Interface Migration

The current LayoutComponent/CommandHandler system uses numerous plug-in interfaces
(`PolymorphicConfiguration<? extends SomeInterface>`) for customization. Many of these
take `LayoutComponent` as a parameter and cannot be reused in the new view system. This
section inventories all such interfaces and proposes migration strategies.

### Interfaces Dependent on LayoutComponent (need new equivalents)

These interfaces have `LayoutComponent` in their method signatures and require new
view-based counterparts.

#### 1. ExecutabilityRule

- **Current**: `isExecutable(LayoutComponent component, Object model, Map args)`
- **Used in**: `CommandHandler.Config` (`getExecutability()` property)
- **Purpose**: Determines if a command is executable (enabled/disabled/hidden)
- **Implementations**: AlwaysExecutable, NullModelDisabled, InEditModeExecutable,
  HistoricDataExecRule, CombinedExecutabilityRule, DelegateRule, RuleReference, etc.
- **New equivalent**: `isExecutable(ViewContext context, Map args)` - The model comes
  from channels (accessible through ViewContext), not from the component. Most existing
  rules check `component.getModel()` or component state (edit mode, visibility) - both
  available through ViewContext.

#### 2. CommandConfirmation

- **Current**: `getConfirmation(LayoutComponent component, ResKey label, Object model, Map args)`
- **Used in**: `CommandHandler.Config` (`getConfirmation()` property)
- **Purpose**: Provides confirmation message before command execution
- **Implementations**: DefaultConfirmation, DefaultDeleteConfirmation,
  CustomConfirmation, ScriptedConfirmation
- **New equivalent**: `getConfirmation(ViewContext context, ResKey label, Map args)` -
  Same pattern, ViewContext replaces LayoutComponent.

#### 3. CheckScopeProvider

- **Current**: `getCheckScope(LayoutComponent component)`
- **Used in**: `CommandHandler.Config` (`getCheckScopeProvider()` property)
- **Purpose**: Identifies components to check for unsaved changes before execution
- **New equivalent**: Needs rethinking. The current concept navigates the LayoutComponent
  tree to find dirty forms. In the new system, forms could track their own dirty state
  via channels, and the view could aggregate dirty state in a derived channel. The
  command's check could then be a simple channel-value check rather than a tree walk.

#### 4. ModelBuilder / ListModelBuilder / TreeModelBuilder

- **Current**: `getModel(Object businessModel, LayoutComponent component)`
- **Used in**: `BuilderComponent.Config`, `TableComponent.Config`, `TreeComponent.Config`
- **Purpose**: Derives GUI model (table rows, tree nodes) from a business model object
- **New equivalent**: Already covered in the spec (Section 4). New implementations
  against a channel-based API with explicit `inputs`. Configuration patterns
  (property names, expression semantics) carried over; implementations rewritten.

#### 5. LayoutControlProvider

- **Current**: Creates LayoutControl for a LayoutComponent
- **Used in**: `LayoutComponent.Config` (`getComponentControlProvider()` property)
- **Purpose**: Produces the visual representation of a component
- **New equivalent**: Replaced entirely by `UIElement.createControl(ViewContext)`.
  The UIElement *is* the control provider. This interface has no counterpart in the
  new system.

#### 6. ComponentResolver

- **Current**: Resolves/initializes components during loading
- **Used in**: `LayoutComponent.Config`
- **Purpose**: Post-processing of component tree after instantiation
- **New equivalent**: Likely not needed. Views are simpler (no component resolution
  phase). If needed, a view-level initialization hook could be added.

#### 7. PostCreateAction

- **Current**: `handleNew(LayoutComponent component, Object newModel)`
- **Used in**: `OpenModalDialogCommandHandler.Config` (`getPostCreateActions()`)
- **Purpose**: Actions after object creation (set model, close dialog, navigate, etc.)
- **Implementations**: SetModel, SetEditMode, CloseDialog, ShowComponent,
  UpdateComponent, DeliverAsDownload, InTransaction
- **New equivalent**: `handleNew(ViewContext context, Object newModel)` - Most actions
  translate directly: SetModel becomes a channel write, CloseDialog stays the same,
  ShowComponent becomes view navigation. The InTransaction wrapper works unchanged.

#### 8. ValueTransformation

- **Current**: `transform(LayoutComponent component, Object value)`
- **Used in**: `PostCreateAction.WithTransform.Config` (`getInput()` property)
- **Purpose**: Transforms command result before passing to PostCreateActions
- **New equivalent**: Could be simplified to `Function<Object, Object>` since most
  transforms don't actually need the component. If context is needed:
  `transform(ViewContext context, Object value)`.

#### 9. TitleProvider

- **Current**: `createTitle(LayoutComponent component)`
- **Used in**: DialogInfo configuration
- **Purpose**: Computes dialog/component title
- **New equivalent**: Could be a derived channel or a TL-Script expression. A title is
  just a computed value from model data - no dedicated interface needed if the view
  system supports expression-based attribute values.

#### 10. SecurityPath

- **Current**: `nextComponent(LayoutComponent, int, int)` and
  `getModel(LayoutComponent, Object, BoundCommandGroup, int, int)`
- **Used in**: `PathSecurityObjectProvider.Config` (`getPath()` property)
- **Purpose**: Navigation steps (Master, Parent, Opener, Selection, etc.) through the
  LayoutComponent tree to find the security-relevant object
- **Implementations**: Component, Model, Selection, Master, Parent, Opener, Window,
  SelectableMaster, CurrentObject
- **New equivalent**: Most of this navigation becomes unnecessary. In the new system,
  the security object is typically the value of a channel (reachable directly by channel
  reference). The SecurityObjectProvider on a command can reference a channel instead of
  walking a component tree. Complex tree navigation (Parent, Opener, Window) is a
  LayoutComponent concept that has no direct equivalent in views.

#### 11. ContextMenuFactory

- **Current**: Creates context menus tied to component context
- **Used in**: `TreeComponent.Config`, `WithContextMenuFactory`
- **Purpose**: Provides context menu entries for tree/table elements
- **New equivalent**: Replaced by the panel-scoped command model. Context menus on data
  elements (`<table>`, `<tree>`) are built automatically from commands with
  `placement="context-menu"` on the enclosing `<panel>`. No dedicated factory interface
  is needed - the panel's command list and each command's executability rule determine
  the context menu entries.

### Interfaces Independent of LayoutComponent (reusable as-is)

These interfaces do not reference `LayoutComponent` in their signatures and can be
reused in the new view system without changes.

| Interface                  | Used In            | Purpose                                  |
|----------------------------|--------------------|------------------------------------------|
| **SecurityObjectProvider** | CommandHandler     | Determines security check target          |
| **BoundCommandGroup**      | CommandHandler     | Security classification (READ/WRITE/...)  |
| **TableFilterProvider**    | Column config      | Filter implementations for columns        |
| **TableConfigurationProvider** | Table config   | Dynamic table configuration               |
| **TableDragSource**        | Table/Tree config  | Drag source for rows                      |
| **TableDropTarget**        | Table/Tree config  | Drop target for rows                      |
| **ResourceProvider**       | Column/Tree config | Labels, images, tooltips for objects      |
| **LabelProvider**          | Column/filter      | Text labels for values                    |
| **Renderer**               | Column config      | Generic value rendering                   |
| **CellRenderer**           | Column config      | Table cell rendering                      |
| **ControlProvider**        | Column/form config | Creates controls for form fields          |
| **FieldProvider**          | Column config      | Creates form fields for values            |
| **Accessor**               | Column config      | Accesses parts of row objects             |
| **Comparator**             | Column config      | Sorting                                   |
| **CellExistenceTester**    | Column config      | Whether a cell should render              |
| **ExcelCellRenderer**      | Column config      | Export rendering (Excel)                  |
| **PDFRenderer**            | Column config      | Export rendering (PDF)                    |
| **RowClassProvider**       | Table config       | Dynamic CSS classes for rows              |
| **ColumnConfigurator**     | Column config      | Additional column config logic            |
| **TableCommandProvider**   | Table config       | Commands for table elements               |
| **TableDataExport**        | Table config       | Export functionality                      |

Note: `SecurityObjectProvider` uses `BoundChecker` (which wraps LayoutComponent), so it
may need a thin adapter but the core interface is not LayoutComponent-specific.

### Migration Strategy Summary

The general pattern for migrating LayoutComponent-dependent interfaces:

1. **Replace `LayoutComponent` parameter with `ViewContext`** in the method signature.
   ViewContext provides access to channels, commands, security context, and locale.

2. **Replace component model access with channel access**. Instead of
   `component.getModel()`, read from a named channel via ViewContext.

3. **Replace component tree navigation with channel references**. Instead of walking
   Parent/Master/Opener, reference channels directly by `path/to/view.view.xml#name`.

4. **Eliminate interfaces that become unnecessary**. LayoutControlProvider,
   ComponentResolver, and TitleProvider have no direct equivalents because their
   concerns are handled differently in the new architecture.

5. **Carry over configuration patterns**. Property names, expression semantics, and
   PolymorphicConfiguration extensibility remain the same. Users familiar with
   `ListModelByExpression` configuration will recognize the new equivalents immediately.

## Open Questions

1. **ViewContext completeness**: The ViewContext API is sketched in Section 1 (channel
   resolution, commands, security, rendering, form scoping). Should it also carry a
   reference to the enclosing `LayoutComponent` (for bridge scenarios via
   ViewHostComponent)?

2. **Form integration**: How deep should the form system integrate? Should `<form>` be a
   UIElement that internally creates a `FormContext` / `FormGroup`, or should individual
   `<field>` elements be standalone UIElements?

3. **Incremental rendering**: When a channel value changes, should the entire Control tree
   be rebuilt, or should individual Controls update incrementally? The existing Control
   infrastructure supports incremental updates via `ClientAction`, but the question is
   how channel changes propagate to the right Controls.

4. **Personalization**: The current system stores user-specific layout sizes. How should
   personalization work for the new flexible layout elements?

5. **Edit/view mode**: The current system has EditComponent with explicit mode
   switching. How should this work in views? Options: (a) a `mode` attribute on
   `<form>` bound to a channel, (b) a mode in ViewContext that `<field>` elements
   respect, (c) a separate `<editor>` wrapper element. This also affects dirty
   tracking and save/cancel command patterns.

6. **Application-level toolbar**: LayoutComponents participate in the toolbar system
   (`ToolBarOwner`). In the new system, commands are scoped to `<panel>` elements. When
   a view is hosted in a ViewHostComponent, how does the panel's toolbar interact with
   the application-level toolbar? Does the topmost panel's toolbar merge into the
   application toolbar, or is it always rendered independently?

7. **Dialog lifecycle**: `<dialog>` declares content, but the full lifecycle (opening
   with a model, passing results back, stacking multiple dialogs) needs definition.
   How does the dialog interact with channels? Does opening a dialog push a
   ViewContext scope?

8. **Channel resolution at runtime**: When view A references
   `viewB.view.xml#foo`, how is the runtime instance of view B located? The application
   must maintain a registry of active view instances indexed by file path. What happens
   if the same `*.view.xml` is instantiated multiple times (e.g., in different tabs)?

9. **Model builder input type**: The `inputs` property on model builders is a list of
   channel references. Should this be `List<ChannelRef>` in the configuration, or a
   comma-separated string for conciseness in XML? The comma-separated form is more
   readable but less type-safe.

## Additional Ideas

### List Rendering

Declarative iteration over a collection channel:

```xml
<for-each items="customer.get(`tl.customers:Customer#orders`)" var="order">
  <card>
    <text value="$order.get(`tl.orders:Order#description`)" />
    <text value="$order.get(`tl.orders:Order#amount`)" />
  </card>
</for-each>
```

### Event Handlers

Lightweight inline actions for simple state manipulation:

```xml
<button label="Clear" on-click="set:selectedCustomer = null" />
```
