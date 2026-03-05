# Design: `<view-ref>` Element for View Composition

**Date:** 2026-03-05
**Ticket:** #29108
**Module:** `com.top_logic.layout.view`

## Problem

Views are currently self-contained: all elements must be defined inline in a single `.view.xml` file. There is no way to embed a view defined in another file. This prevents:

- **Reuse** — the same view (e.g., a customer detail panel) cannot be used in multiple places.
- **Decomposition** — large views cannot be broken into smaller, independently maintained files.
- **Recursion** — a view cannot open another instance of itself (e.g., a dialog spawning a sub-dialog).

The old layout system had both *textual inclusion* (inlining another file's content into the including file's scope) and *component references* (embedding an independent component with its own lifecycle). This design addresses the latter — a true component boundary where each reference creates an independent instance with its own channel namespace.

## Design Overview

A new `<view-ref>` element embeds a referenced view as an isolated component. Each instance gets its own `ViewContext` with its own channel namespace. Communication across the boundary uses explicit `<bind>` elements that map the parent's channels to the child's channels by sharing the same channel instance.

```xml
<!-- Parent view -->
<view>
  <channels>
    <channel name="selectedCustomer"/>
  </channels>

  <split-panel orientation="horizontal">
    <pane size="30" unit="%">
      <view-ref view="customers/list.view.xml">
        <bind channel="selection" to="selectedCustomer"/>
      </view-ref>
    </pane>
    <pane size="70" unit="%">
      <view-ref view="customers/detail.view.xml">
        <bind channel="item" to="selectedCustomer"/>
      </view-ref>
    </pane>
  </split-panel>
</view>
```

Here, `customers/list.view.xml` declares a channel named `selection` and `customers/detail.view.xml` declares a channel named `item`. The parent wires both to its own `selectedCustomer` channel. When the list writes to `selection`, the detail's `item` sees the same value instantly — they are the same channel object.

## New Classes

### `ReferenceElement`

**Package:** `com.top_logic.layout.view`

A `UIElement` that embeds another view as an isolated component.

**Config** (`@TagName("view-ref")`):

| Property | Type | Description |
|----------|------|-------------|
| `view` | `String` (`@Mandatory`) | Path to the referenced `.view.xml` file, relative to `/WEB-INF/views/`. |
| `bindings` | `List<ChannelBindingConfig>` (`@DefaultContainer`) | Channel bindings from parent to child. |

**`createControl(ViewContext parentContext)`:**

1. Load the referenced `ViewElement` via `ViewLoader.getOrLoadView(path)`.
2. Create a new `ViewContext` from `parentContext.getDisplayContext()` — fresh, isolated channel map.
3. For each `<bind>` element: resolve the parent channel via `parentContext.resolveChannel(binding.getTo())`, then pre-register it in the child context via `childContext.registerChannel(binding.getChannel(), parentChannel)`.
4. Derive personalization path: use the `personalization-key` config if set, otherwise use the view file name (e.g., `customers/detail.view.xml` becomes segment `customers/detail`).
5. Call `viewElement.createControl(childContext)`.
6. Return the resulting `ViewControl`.

The referenced view is loaded lazily at `createControl()` time (not at config parse time). This naturally supports recursive usage — the config tree is finite, and each `createControl()` call creates a new runtime context.

### `ChannelBindingConfig`

**Package:** `com.top_logic.layout.view.channel`

Configuration for a single channel binding between parent and child view.

**Config** (`@TagName("bind")`):

| Property | Type | Description |
|----------|------|-------------|
| `channel` | `String` (`@Mandatory`) | Name of the channel in the referenced (child) view. |
| `to` | `ChannelRef` (`@Mandatory`, `@Format(ChannelRefFormat.class)`) | Reference to a channel in the parent scope. |

### `ViewLoader`

**Package:** `com.top_logic.layout.view`

Shared utility for loading and caching parsed `ViewElement` instances. Extracted from `ViewServlet`.

| Method | Description |
|--------|-------------|
| `getOrLoadView(String viewPath)` | Returns a cached `ViewElement`, reloading if the file has changed. |
| `loadView(String viewPath)` | Parses a `.view.xml` file into a `ViewElement`. |

Uses a `ConcurrentHashMap<String, CachedView>` with `lastModified` timestamp checking (same logic currently in `ViewServlet`). The `viewPath` parameter is the full path (e.g., `/WEB-INF/views/customers/detail.view.xml`).

The `view` attribute on `<view-ref>` is a relative path (e.g., `customers/detail.view.xml`). `ReferenceElement` prepends the `/WEB-INF/views/` base path before calling `ViewLoader`.

## Modified Classes

### `ViewElement`

**Change:** In `createControl()`, skip channel registration when a channel with that name is already present (pre-bound by a parent `<view-ref>`).

Before:
```java
for (ChannelConfig channelConfig : _channelConfigs) {
    ViewChannel channel = new DefaultInstantiationContext(...).getInstance(channelConfig);
    context.registerChannel(channelConfig.getName(), channel);
}
```

After:
```java
for (ChannelConfig channelConfig : _channelConfigs) {
    String name = channelConfig.getName();
    if (context.hasChannel(name)) {
        // Pre-bound by parent via <view-ref> binding. Skip local instantiation.
        continue;
    }
    ViewChannel channel = new DefaultInstantiationContext(...).getInstance(channelConfig);
    context.registerChannel(name, channel);
}
```

### `ViewContext`

**Addition:** `hasChannel(String name)` method.

```java
/**
 * Whether a channel with the given name is registered.
 */
public boolean hasChannel(String name) {
    return _channels.containsKey(name);
}
```

### `ViewServlet`

**Change:** Replace private `getOrLoadView()` / `loadView()` methods with calls to `ViewLoader`. Remove the instance-level `_viewCache` field.

## Channel Binding Semantics

**Bound channels:** The parent's channel instance is registered directly in the child's context. The child view "sees" the parent's channel as if it were its own. Reads and writes go to the same object. Listeners registered by the child fire when the parent (or anyone) changes the value.

**Unbound channels:** The child view creates its own local channel instance, isolated from the parent. These channels are private to the referenced view instance.

**Validation:** At `createControl()` time, `ReferenceElement` should verify that each bound channel name actually exists in the referenced view's channel declarations. If a `<bind channel="foo" .../>` references a channel `foo` that the referenced view does not declare, log a warning (the binding is silently ignored since the child will create its own `foo` channel that happens to be the parent's — but this likely indicates a misconfiguration).

## Recursive Usage

A view can reference itself:

```xml
<!-- dialog.view.xml -->
<view>
  <channels>
    <channel name="model"/>
  </channels>
  <panel title="Dialog">
    <!-- Content using model channel -->
    <button label="Open Sub-Dialog" action="...">
      <!-- When clicked, dynamically creates: -->
      <!-- <view-ref view="dialog.view.xml">
             <bind channel="model" to="..."/>
           </view-ref> -->
    </button>
  </panel>
</view>
```

This works because:
- The config (`ReferenceElement.Config`) just stores the string path `"dialog.view.xml"` — no eager resolution.
- Each `createControl()` call loads the shared `ViewElement` and creates a new `ViewContext` — each instance gets its own channel map and personalization scope.
- There is no infinite recursion at config parse time. Runtime recursion is controlled by the application logic (e.g., a button click dynamically creating a new reference).

## Personalization

Each `<view-ref>` instance needs a unique personalization scope so that stateful elements (e.g., split panel positions, collapsed states) within the referenced view are stored independently.

The personalization path segment for a `<view-ref>` is derived as follows:
1. If `personalization-key` is explicitly set on the `<view-ref>` config, use that.
2. Otherwise, derive from the view path by stripping the `.view.xml` suffix and replacing `/` with `.` (e.g., `customers/detail.view.xml` becomes `customers.detail`).

If the same view is referenced twice at the same tree level without explicit keys, their personalization paths collide. Users should set explicit `personalization-key` values in that case.

## XML Examples

### Basic embedding
```xml
<view-ref view="shared/toolbar.view.xml"/>
```

No bindings — the toolbar view runs with its own isolated channels.

### Master-detail wiring
```xml
<view>
  <channels>
    <channel name="selection"/>
  </channels>
  <split-panel>
    <pane size="30" unit="%">
      <view-ref view="customers/list.view.xml">
        <bind channel="selection" to="selection"/>
      </view-ref>
    </pane>
    <pane size="70" unit="%">
      <view-ref view="customers/detail.view.xml">
        <bind channel="item" to="selection"/>
      </view-ref>
    </pane>
  </split-panel>
</view>
```

### Multiple instances of the same view
```xml
<stack direction="row">
  <view-ref view="chart/pie.view.xml" personalization-key="revenue-chart">
    <bind channel="data" to="revenueData"/>
  </view-ref>
  <view-ref view="chart/pie.view.xml" personalization-key="cost-chart">
    <bind channel="data" to="costData"/>
  </view-ref>
</stack>
```

## Error Handling

| Condition | Behavior |
|-----------|----------|
| Referenced view file not found | `ConfigurationException` at `createControl()` time, logged as error. |
| `<bind channel="x">` where `x` is not declared in the referenced view | Warning logged. The child creates channel `x` locally and the binding has no effect. |
| `<bind to="y">` where `y` is not in the parent's channel scope | `IllegalArgumentException` from `ViewContext.resolveChannel()` at `createControl()` time. |
| Circular file references at config parse time | Cannot happen — config stores only the path string. |
| Infinite runtime recursion | Controlled by application logic (e.g., button actions). Not a framework concern. |
