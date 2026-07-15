# TreeElement Design

## Overview

Add a declarative `<tree>` UIElement to the view system that wraps `ReactTreeControl`.
The element replaces the legacy `TreeComponent`+`TreeModelBuilder` pattern by using
ViewChannels as input sources and TL-Script expressions for tree structure derivation.

## Configuration

`TreeElement` implements `UIElement` with `@TagName("tree")`.

### Config Properties

| Property        | Type                                            | Required | Description                                                        |
|-----------------|-------------------------------------------------|----------|--------------------------------------------------------------------|
| `inputs`        | `List<ChannelRef>`                              | no       | ViewChannels whose values become positional args to expressions    |
| `root`          | `Expr`                                          | yes      | TL-Script function returning the root business object              |
| `children`      | `Expr`                                          | yes      | TL-Script function returning child nodes for a given parent        |
| `isLeaf`        | `Expr`                                          | no       | `(input0, ..., inputN, node) -> boolean`                           |
| `supportsNode`  | `Expr`                                          | no       | `(input0, ..., inputN, node) -> boolean`                           |
| `modelForNode`  | `Expr`                                          | no       | `(input0, ..., inputN, node) -> Object` reverse root lookup        |
| `parents`       | `Expr`                                          | no       | `(input0, ..., inputN, node) -> Collection<?>`                     |
| `nodesToUpdate` | `Expr`                                          | no       | `(input0, ..., inputN, object) -> Collection<?>`                   |
| `canExpandAll`  | `boolean`                                       | no       | Whether expand-all is safe (default: `true`)                       |
| `selection`     | `ChannelRef`                                    | no       | Channel to write selected node(s) to                               |
| `nodeContent`   | `PolymorphicConfiguration<ReactControlProvider>`| no       | Custom node content renderer                                       |

### Example XML

```xml
<tree selection="selectedItem">
  <inputs>
    <input channel="rootOrganization" />
  </inputs>
  <root>org -> $org</root>
  <children>org -> parent -> $parent.get(`tl.core:StructuredElement#children`)</children>
  <isLeaf>org -> node -> $node.get(`tl.core:StructuredElement#children`).isEmpty()</isLeaf>
  <nodeContent class="...SomeReactControlProvider" />
</tree>
```

## Expression Argument Binding

The `inputs` list defines positional arguments to all expressions:

- **`root`**: `(input0, input1, ..., inputN) -> Object`
- **`children`**: `(input0, input1, ..., inputN, parent) -> Collection<?>`
- **`isLeaf`**: `(input0, input1, ..., inputN, node) -> boolean`
- **`supportsNode`**: `(input0, input1, ..., inputN, node) -> boolean`
- **`modelForNode`**: `(input0, input1, ..., inputN, node) -> Object`
- **`parents`**: `(input0, input1, ..., inputN, node) -> Collection<?>`
- **`nodesToUpdate`**: `(input0, input1, ..., inputN, object) -> Collection<?>`

Input channel values always come first, followed by any node-specific parameter
(the parent/node/object depending on the expression).

## Runtime Lifecycle

### Constructor (shared, stateless)

`TreeElement(InstantiationContext context, Config config)`:

1. Compile `root` Expr into `QueryExecutor _rootExecutor` (mandatory).
2. Compile `children` Expr into `QueryExecutor _childrenExecutor` (mandatory).
3. Compile optional expressions (`isLeaf`, `supportsNode`, `modelForNode`,
   `parents`, `nodesToUpdate`).
4. Store `ChannelRef` list, `canExpandAll` flag, and `nodeContent` provider.

QueryExecutors are compiled once via `QueryExecutor.compile(Expr)` (single-arg,
returns `DeferredQueryExecutor` if services are not yet active) and shared across
all sessions.

### createControl(ViewContext) (per-session)

1. **Resolve input channels**: For each `ChannelRef` in `inputs`, call
   `context.resolveChannel(ref)` to get the `ViewChannel`.
2. **Execute root query**: Read current values from all input channels, pass as
   positional args to `_rootExecutor.execute(...)` to get the root business object.
3. **Build custom TreeBuilder**: Create an anonymous `TreeBuilder<DefaultTreeUINode>`
   that delegates:
   - `createChildList(node)` -> execute `_childrenExecutor` with
     `(channelValues..., node.getBusinessObject())`, then wrap each child in a
     new `DefaultTreeUINode` via `createNode()`.
   - `createNode(model, parent, userObject)` -> standard `DefaultTreeUINode` creation.
   - `isFinite()` -> return `_canExpandAll`.
4. **Create `DefaultTreeUINodeModel`**: From root business object + custom TreeBuilder.
5. **Create `SelectionModel`**: `DefaultSingleSelectionModel` (or multi-select in future).
6. **Create `ReactControlProvider`**: Instantiate configured `nodeContent` provider,
   or fall back to a default text provider using `MetaLabelProvider`.
7. **Create `ReactTreeControl`**: Pass `TreeUIModel` + `SelectionModel` + content provider.
8. **Wire selection channel** (if configured): Listen to `SelectionModel` changes,
   write selected node's business object to the resolved selection channel.
9. **Wire input channel listeners**: Register `ChannelListener` on each input channel.
   On any input change:
   - Re-execute `root` expression to get new root
   - Rebuild `DefaultTreeUINodeModel` with new root
   - Update `ReactTreeControl` with new model (or recreate control)

## Selection Channel

When `selection` is configured:

- Resolve the `ChannelRef` to a `ViewChannel` via `ViewContext.resolveChannel()`.
- Listen to `SelectionModel` changes (via `SelectionModelListener`).
- Single selection: write the selected node's business object (or `null` if deselected).
- The selection channel is output-only from the tree's perspective.

## Node Content Rendering

The `nodeContent` property accepts a `PolymorphicConfiguration<ReactControlProvider>`.
If configured, the provider is instantiated and passed to `ReactTreeControl`.
If not configured, a default provider renders each node as a text label using
`MetaLabelProvider.INSTANCE.getLabel(node.getBusinessObject())`.

## Module Placement

- **Package**: `com.top_logic.layout.view.element`
- **Module**: `com.top_logic.layout.view` (artifact `tl-layout-view`)
- **No new dependencies**: `tl-core` and `tl-model-search` already added for `TableElement`

## Relationship to Legacy System

| Legacy                                             | New (TreeElement)                                           |
|----------------------------------------------------|-------------------------------------------------------------|
| `ModelBuilder.getModel(bm, lc)`                    | `_rootExecutor.execute(channelValues)`                      |
| `TreeBuilderBase.getChildIterator(lc, node)`       | `_childrenExecutor.execute(channelValues..., node)`         |
| `TreeBuilderBase.isLeaf(lc, node)`                 | `_isLeafExecutor.execute(channelValues..., node)`           |
| `TreeBuilderBase.supportsNode(lc, node)`           | `_supportsNodeExecutor.execute(channelValues..., node)`     |
| `TreeModelBuilder.retrieveModelFromNode(lc, node)` | `_modelForNodeExecutor.execute(channelValues..., node)`     |
| `TreeModelBuilder.getParents(lc, node)`            | `_parentsExecutor.execute(channelValues..., node)`          |
| `TreeModelBuilder.getNodesToUpdate(lc, obj)`       | `_nodesToUpdateExecutor.execute(channelValues..., obj)`     |
| `TreeModelBuilder.canExpandAll()`                   | `_canExpandAll` boolean config property                     |
| `LayoutComponent.getModel()`                        | `ViewChannel.get()` (per input channel)                     |
| `TreeComponent` orchestration                       | `TreeElement.createControl()` wiring                        |
| `ReactControlProvider` (for node content)           | Reused as-is via `nodeContent` config                       |
