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

## 2. Channels: Unified Reactive Data Flow

There is a single concept for reactive data flow: **channels**. Channels serve both for
intra-view communication (what the previous draft called "state") and for inter-view
communication. There is no separate "state" concept.

### Channel Declaration

Channels are declared at the view level and are scoped to that view. They can optionally
be exposed for cross-view linking:

```xml
<view name="customerMasterDetail">
  <!-- Internal channel: links table selection to detail form -->
  <channel name="selectedCustomer" type="tl.customers:Customer" />

  <!-- Exposed channel: can be linked from other views -->
  <channel name="currentOrganization" type="tl.customers:Organization"
           direction="in" source="organizationSelector.selection" />

  <split direction="horizontal" ratio="30:70">
    <table model="channel:currentOrganization" selection="channel:selectedCustomer">
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

Elements reference channels via `channel:channelName` in their configuration attributes.
The binding direction depends on the attribute semantics:

- **`model="channel:selectedCustomer"`** - The element *reads* from this channel (input).
  When the channel value changes, the element receives a new model.
- **`selection="channel:selectedCustomer"`** - The element *writes* to this channel
  (output). When the user selects something, the channel is updated.
- Some attributes are bidirectional (both read and write).

### Derived Channels

Channels can be computed from expressions over other channels:

```xml
<channel name="hasSelection" expr="$selectedCustomer != null" />
<channel name="canDelete"
         expr="$selectedCustomer != null
               && $currentUser.hasRole(`admin`)" />
```

These are read-only channels whose values update reactively when their dependencies change.
The expressions use TL-Script.

### Inter-View Channel Linking

When a channel declares `direction="in"` or `direction="out"`, it becomes part of the
view's external interface. Views hosted within the same LayoutComponent tree can link
channels across view boundaries, analogous to how ComponentChannels link today:

```xml
<!-- View A exposes a selection -->
<view name="customerSelector">
  <channel name="selection" type="tl.customers:Customer" direction="out" />
  <table selection="channel:selection">...</table>
</view>

<!-- View B consumes it -->
<view name="customerEditor">
  <channel name="customer" type="tl.customers:Customer" direction="in"
           source="customerSelector.selection" />
  <form model="channel:customer">...</form>
</view>
```

## 3. Model Builders: Complex Model Derivation

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

### Why Not Invent a New Model Abstraction?

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

## 4. Built-in UIElement Types

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

## 5. Templates: Reusing the Existing Template System

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

## 6. View Definition and Hosting

### What Is a View?

A `<view>` is the top-level container that:

1. Declares the channels available to its element tree.
2. Contains a root `UIElement` (its content).
3. Can be hosted inside a `LayoutComponent` for backward compatibility.

```java
/**
 * A view is a self-contained UI definition with its own channel scope.
 */
public interface ViewDefinition {

    interface Config extends ConfigurationItem {

        /** Name of this view (for referencing in channel links). */
        @Name("name")
        String getName();

        /** Channel declarations. */
        @Name("channels")
        List<ViewChannelConfig> getChannels();

        /** The root UI element. */
        @Name("content")
        @Mandatory
        PolymorphicConfiguration<UIElement> getContent();

        /** Command definitions scoped to this view. */
        @Name("commands")
        List<PolymorphicConfiguration<CommandHandler>> getCommands();
    }
}
```

### Hosting in the Existing System

A bridge `LayoutComponent` hosts a view inside the existing component tree:

```xml
<!-- In a classic .layout.xml -->
<component class="com.top_logic.layout.view.ViewHostComponent">
  <view name="customers">
    <channels>
      <channel name="organization" direction="in"
               source="orgSelector.selection" />
      <channel name="selectedCustomer" />
    </channels>
    <content>
      <split direction="horizontal" ratio="30:70">
        <table model="channel:organization"
               selection="channel:selectedCustomer">
          <model-builder
            class="com.top_logic.model.search.providers.ListModelByExpression"
            elements="model -> $model.get(`tl.customers:Organization#customers`)"
          />
        </table>
        <form model="channel:selectedCustomer">
          <field attribute="name" />
          <field attribute="email" />
        </form>
      </split>
    </content>
  </view>
</component>
```

`ViewHostComponent` bridges the two worlds:

- It is a `LayoutComponent` (participates in the existing component tree).
- It instantiates the view's channels and connects `direction="in"` channels to
  the corresponding `ComponentChannel` sources in the LayoutComponent tree.
- It calls `UIElement.createControl(viewContext)` on the root element to produce
  the Control tree for rendering.

### Embedding Legacy Components

Conversely, an existing `LayoutComponent` can be embedded inside a view:

```xml
<view name="mixed">
  <content>
    <vbox>
      <heading text="Overview" />
      <legacy-component
          class="com.top_logic.layout.table.component.TableComponent">
        <!-- Full existing LayoutComponent config -->
      </legacy-component>
    </vbox>
  </content>
</view>
```

`<legacy-component>` is a `UIElement` that wraps an existing `LayoutComponent`,
adapting it into the new element tree.

## 7. Styling

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

## 8. Commands and Actions

Commands are declared at the view level and referenced from interaction elements:

```xml
<view name="customerEdit">
  <commands>
    <command name="save"
             class="com.example.customers.SaveCustomerHandler" />
    <command name="delete"
             class="com.example.customers.DeleteCustomerHandler"
             confirm="true" />
  </commands>
  <content>
    <form model="channel:customer">
      <field attribute="name" />
      <field attribute="email" />
      <hbox>
        <button command="save" />
        <button command="delete" />
      </hbox>
    </form>
  </content>
</view>
```

Command handlers are `PolymorphicConfiguration<? extends CommandHandler>`, reusing the
existing command infrastructure. The view's `ViewContext` resolves command references and
provides the execution environment.

## 9. Security

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
|  View Definition (.view.xml / .view.template.xml)                |
|  - Channels (reactive data flow)                                 |
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
|  - Hosts a View inside a LayoutComponent                         |
|  - Maps ViewChannels <-> ComponentChannels                       |
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
| Rendering              | Every LayoutComponent     | Every UIElement (lightweight)          |
| Layout                 | Fixed splitter panels     | CSS Flex/Grid (flexible)               |
| Model binding          | Mandatory ModelBuilder    | Opt-in `<model-builder>` child         |
| Data flow              | ComponentChannel wiring   | Channels (unified intra/inter-view)    |
| Security               | Per-component mandatory   | Per-element opt-in `secure` attr       |
| Commands               | Per-component             | Per-view, invoked from any element     |
| Composition            | *.template.xml            | *.view.template.xml (same mechanism)   |
| Extensibility          | LayoutComponent subclass  | New UIElement + Config pair (modular)   |
| Backward compat        | N/A                       | ViewHostComponent / legacy-component   |

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
