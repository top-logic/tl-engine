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

### Element Tree in XML

Because each XML element maps to a `PolymorphicConfiguration<? extends UIElement>`, the
view XML is a tree of typed configurations. Container elements have child properties of
type `List<PolymorphicConfiguration<? extends UIElement>>`:

```xml
<vbox>
  <heading text="Customer Details" />
  <form model="channel:selectedCustomer">
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

### The Naming Problem

In the current system, LayoutComponents have explicit names. When components are created
by templates, names must be parameterized. This leads to concatenated names mixed from
constants and template parameters, making it nearly impossible to find where a
component/channel is defined.

### Solution: File Path as Identity

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

### Cross-View Channel References

Remote channels are referenced directly by file path and channel name, using the syntax
`channel:path/to/view.view.xml#channelName`. No intermediate binding declarations or
direction annotations are needed:

```xml
<!-- File: customers/masterDetail.view.xml -->
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <split direction="horizontal" ratio="30:70">
    <!-- Read a channel from another view by its file path -->
    <table model="channel:organization/selector.view.xml#currentOrganization"
           selection="channel:selectedCustomer">
      <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
        elements="model -> $model.get(`tl.customers:Organization#customers`)"
        supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
      />
    </table>

    <form model="channel:selectedCustomer"
          visible="channel:selectedCustomer != null">
      <field attribute="name" />
      <field attribute="email" />
      <field attribute="phone" />
    </form>
  </split>
</view>
```

### Advantages

- **No naming conflicts with templates**: Templates never need to generate view names.
  The file path is fixed and unique regardless of how many times a template is used.
- **Searchable**: `grep -r "organization/selector.view.xml"` instantly finds all
  references to that view's channels.
- **No indirection**: No need for `direction="in"`, `source="viewName.channelName"`,
  or local alias channels. A remote channel is used directly where it is needed.
- **Consistent with current system**: The current system already uses `*.layout.xml`
  file names as component name scopes. This simplifies and extends that pattern.

### Channel Reference Syntax

```
channel:<channelName>                              -- local channel in the same view
channel:<path/to/view.view.xml>#<channelName>      -- remote channel in another view
```

When an expression references a channel (e.g., in `visible` attributes), the same syntax
applies within TL-Script expressions:

```xml
<panel visible="channel:selectedCustomer != null">
```

## 3. Channels: Unified Reactive Data Flow

There is a single concept for reactive data flow: **channels**. Channels serve both for
intra-view communication and for inter-view communication. There is no separate "state"
concept - a channel *is* the reactive state.

### Channel Declaration

Channels are declared at the view level:

```xml
<view>
  <!-- A channel holding the currently selected customer -->
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <!-- A derived channel computed from other channels -->
  <channel name="hasSelection" expr="$selectedCustomer != null" />
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

### Channel Bindings on Elements

Elements reference channels in their configuration attributes. The binding direction
depends on the attribute semantics:

- **`model="channel:selectedCustomer"`** - The element *reads* from this channel (input).
  When the channel value changes, the element receives a new model.
- **`selection="channel:selectedCustomer"`** - The element *writes* to this channel
  (output). When the user selects something, the channel is updated.
- Some attributes are bidirectional (both read and write).

Local channels (same view) use the short form `channel:channelName`. Remote channels
(other view) use the full path form `channel:path/to/view.view.xml#channelName`.

### Derived Channels

Channels can be computed from expressions over other channels:

```xml
<channel name="hasSelection" expr="$selectedCustomer != null" />
<channel name="canDelete"
         expr="$selectedCustomer != null
               && $currentUser.hasRole(`admin`)" />
```

These are read-only channels whose values update reactively when their dependencies
change. The expressions use TL-Script. Within channel expressions, `$channelName`
references sibling channels in the same view.

## 4. Model Builders: Complex Model Derivation

For complex UI elements like tables and trees, a static value from a channel is not
sufficient. The element needs logic to:

- Derive row/node objects from an input model object
- React to model changes (object creation/deletion) with incremental updates
- Determine whether a given object belongs to the element's data set

This is exactly what the existing `ModelBuilder` / `ListModelBuilder` /
`TreeModelBuilder` infrastructure already provides. The new system reuses these directly
as `PolymorphicConfiguration` children of the element:

```xml
<table model="channel:selectedOrganization" selection="channel:selectedCustomer">
  <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
    elements="model -> $model.get(`tl.customers:Organization#customers`)"
    supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
  />
  <columns>
    <column attribute="name" />
    <column attribute="email" />
    <column attribute="status" />
  </columns>
</table>
```

Here:

- **`model="channel:selectedOrganization"`** - The channel provides the *input model*
  (a single Organization object) that is fed into the model builder.
- **`<model-builder>`** - A `PolymorphicConfiguration<? extends ListModelBuilder>` that
  derives table rows from the input model. This is the existing `ListModelByExpression`
  used unchanged.
- **`selection="channel:selectedCustomer"`** - The table's selection output is bound to
  a channel for consumption by other elements.

### Why Reuse Existing Model Builders?

The existing model builders (`ListModelByExpression`, `TreeModelByExpression`, etc.)
already solve the hard problems:

- Incremental update detection (via `supportsElement`, `ElementUpdate`)
- Expression-based declarative configuration
- Support for both simple and complex derivation logic
- Extensibility via `PolymorphicConfiguration`

Reusing them avoids reinventing this and provides immediate access to the full existing
library of model builder implementations.

### Elements Without Model Builders

Simple elements that just display a channel value directly do not need a model builder:

```xml
<!-- Simple text display - no model builder needed -->
<text value="channel:selectedCustomer.get(`tl.customers:Customer#name`)" />

<!-- Form bound to a channel value - no model builder needed -->
<form model="channel:selectedCustomer">
  <field attribute="name" />
  <field attribute="email" />
</form>
```

## 5. Built-in UIElement Types

### Layout Elements

These are container UIElements that arrange their children using CSS layout:

| Element     | CSS Model    | Description                              |
|-------------|--------------|------------------------------------------|
| `<vbox>`    | Flexbox col  | Vertical flex container                  |
| `<hbox>`    | Flexbox row  | Horizontal flex container                |
| `<grid>`    | CSS Grid     | Grid layout with rows/columns            |
| `<stack>`   | Stacking     | Overlapping layers (tabs, cards)         |
| `<split>`   | Flex + drag  | Resizable split (like today, but opt-in) |
| `<scroll>`  | Overflow     | Scrollable region                        |
| `<panel>`   | Block        | Collapsible panel with header            |

Each of these is a `PolymorphicConfiguration<? extends ContainerElement>` where
`ContainerElement extends UIElement` and has a `List<PolymorphicConfiguration<UIElement>>`
property for children.

### Content Elements

| Element       | Description                    |
|---------------|--------------------------------|
| `<heading>`   | Text heading (h1-h6)          |
| `<text>`      | Static or channel-bound text   |
| `<icon>`      | Theme icon display             |
| `<image>`     | Image display                  |
| `<separator>` | Visual separator               |
| `<spacer>`    | Flexible space                 |

### Data Elements

Complex elements that use model builders to derive data from channel values:

| Element    | Model Builder Type  | Description                           |
|------------|---------------------|---------------------------------------|
| `<table>`  | ListModelBuilder    | Table with columns, sorting, filters  |
| `<tree>`   | TreeModelBuilder    | Tree with expand/collapse             |
| `<form>`   | (direct binding)    | Form fields bound to a model object   |

### Interaction Elements

| Element     | Description                                 |
|-------------|---------------------------------------------|
| `<button>`  | Action trigger, references a command         |
| `<link>`    | Clickable link                               |
| `<menu>`    | Dropdown menu                                |

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
      <table model="channel:organization" selection="channel:selectedCustomer">
        <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
          elements="model -> all(`tl.customers:Customer`)"
          supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
        />
      </table>
    </masterContent>
    <detailContent>
      <form model="channel:selectedCustomer">
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

        /** Command definitions scoped to this view. */
        @Name("commands")
        List<PolymorphicConfiguration<CommandHandler>> getCommands();
    }
}
```

Note: No `getName()` - the identity comes from the file system.

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
channel:customers/masterDetail.view.xml#selectedCustomer
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
- It resolves cross-view channel references
  (`channel:other/view.view.xml#channelName`) by locating the target view's runtime
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
    <text value="channel:selectedCustomer.get(`tl.customers:Customer#name`)" />
  </hbox>
</vbox>
```

- Common layout properties (`padding`, `gap`, `margin`, `width`, `height`) are direct
  configuration attributes on layout elements.
- Visual styling uses `css-class` to reference existing theme CSS classes.
- This avoids inventing a parallel styling system - the existing theme infrastructure
  is reused.

## 9. Commands and Actions

Commands are declared at the view level and referenced from interaction elements:

```xml
<!-- File: customers/edit.view.xml -->
<view>
  <commands>
    <command name="save"
             class="com.example.customers.SaveCustomerHandler" />
    <command name="delete"
             class="com.example.customers.DeleteCustomerHandler"
             confirm="true" />
  </commands>

  <form model="channel:selectedCustomer">
    <field attribute="name" />
    <field attribute="email" />
    <hbox>
      <button command="save" />
      <button command="delete" />
    </hbox>
  </form>
</view>
```

Command handlers are `PolymorphicConfiguration<? extends CommandHandler>`, reusing the
existing command infrastructure. The view's `ViewContext` resolves command references and
provides the execution environment.

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
|  - Commands                                                      |
+------------------------------------------------------------------+
        |                                    |
        | createControl()                    | model-builder
        v                                    v
+------------------------+    +--------------------------------+
|  Control (existing)    |    |  ModelBuilder (existing)        |
|  - Renders HTML        |    |  - ListModelByExpression        |
|  - Handles updates     |    |  - TreeModelByExpression        |
|  - Processes events    |    |  - Custom implementations       |
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
| Model binding          | Mandatory ModelBuilder    | Opt-in `<model-builder>` child         |
| Data flow              | ComponentChannel wiring   | Channels (local + path#name refs)      |
| Security               | Per-component mandatory   | Per-element opt-in `secure` attr       |
| Commands               | Per-component             | Per-view, invoked from any element     |
| Composition            | *.template.xml            | *.view.template.xml (same mechanism)   |
| Extensibility          | LayoutComponent subclass  | New UIElement + Config pair (modular)   |
| Backward compat        | N/A                       | ViewHostComponent / legacy-component   |

## Complete Example

Three views collaborating via cross-view channel references:

```xml
<!-- File: organization/selector.view.xml -->
<view>
  <channel name="currentOrganization" type="tl.customers:Organization" />

  <tree selection="channel:currentOrganization">
    <model-builder class="com.top_logic.model.search.providers.TreeModelByExpression"
      rootNode="all(`tl.customers:Organization`).filter(o -> $o.get(`parent`) == null)"
    />
  </tree>
</view>
```

```xml
<!-- File: customers/list.view.xml -->
<view>
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <table model="channel:organization/selector.view.xml#currentOrganization"
         selection="channel:selectedCustomer">
    <model-builder class="com.top_logic.model.search.providers.ListModelByExpression"
      elements="model -> $model.get(`tl.customers:Organization#customers`)"
      supportsElement="element -> $element.instanceOf(`tl.customers:Customer`)"
    />
    <columns>
      <column attribute="name" />
      <column attribute="email" />
      <column attribute="status" />
    </columns>
  </table>
</view>
```

```xml
<!-- File: customers/detail.view.xml -->
<view>
  <channel name="canEdit"
           expr="channel:customers/list.view.xml#selectedCustomer != null" />

  <form model="channel:customers/list.view.xml#selectedCustomer"
        visible="channel:canEdit">
    <field attribute="name" />
    <field attribute="email" />
    <field attribute="phone" />
    <field attribute="status" />
  </form>
</view>
```

All three views reference each other's channels by file path. No names to invent, no
conflicts possible, and `grep` finds all usages instantly.

## Open Questions

1. **ViewContext API**: What exactly should the `ViewContext` provide to
   `UIElement.createControl()`? At minimum: resolved channel values, command registry,
   security context, theme/locale. Should it also carry a reference to the enclosing
   `LayoutComponent` (for bridge scenarios)?

2. **Form integration**: How deep should the form system integrate? Should `<form>` be a
   UIElement that internally creates a `FormContext` / `FormGroup`, or should individual
   `<field>` elements be standalone UIElements?

3. **Incremental rendering**: When a channel value changes, should the entire Control tree
   be rebuilt, or should individual Controls update incrementally? The existing Control
   infrastructure supports incremental updates via `ClientAction`, but the question is
   how channel changes propagate to the right Controls.

4. **Personalization**: The current system stores user-specific layout sizes. How should
   personalization work for the new flexible layout elements?

5. **Toolbar integration**: LayoutComponents participate in the toolbar system
   (`ToolBarOwner`). How should view-level commands appear in the application toolbar?

6. **Channel resolution at runtime**: When view A references
   `channel:viewB.view.xml#foo`, how is the runtime instance of view B located? The
   application must maintain a registry of active view instances indexed by file path.
   What happens if the same `*.view.xml` is instantiated multiple times (e.g., in
   different tabs)?

## Additional Ideas

### View Variants

Conditional element selection based on device/context:

```xml
<responsive>
  <when max-width="768px">
    <vbox><!-- stacked mobile layout --></vbox>
  </when>
  <otherwise>
    <hbox><!-- side-by-side desktop layout --></hbox>
  </otherwise>
</responsive>
```

### List Rendering

Declarative iteration over a collection channel:

```xml
<for-each items="channel:customer.get(`tl.customers:Customer#orders`)" var="order">
  <card>
    <text value="channel:order.get(`tl.orders:Order#description`)" />
    <text value="channel:order.get(`tl.orders:Order#amount`)" />
  </card>
</for-each>
```

### Event Handlers

Lightweight inline actions for simple state manipulation:

```xml
<button label="Clear" on-click="set:selectedCustomer = null" />
```
