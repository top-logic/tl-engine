# View Designer: Structural Editing in the Design Tree

**Ticket:** #29429
**Date:** 2026-07-29
**Status:** Implemented (browser verification outstanding)
**Related:** `2026-03-27-view-designer-design.md`, `2026-07-15-view-designer-select-view-design.md`

## Problem

The design tree's context menu (*Add Child*, *Remove*, *Move Up*, *Move Down*) does not
edit the view. All three commands mutate only the `DesignTreeNode` mirror tree:

| Command | What it does | What is missing |
|---|---|---|
| `AddChildCommand` | `new StackElement.Config`, appends a node to `parent.getChildren()` | The config is never inserted into a property of the parent config |
| `RemoveElementCommand` | `parent.getChildren().remove(node)` | The config is never removed from its owning property |
| `MoveElementCommand` | reorders `parent.getChildren()` | The owning property's list order is unchanged |

`SaveDesignCommand` then serializes the *unmodified* configs, so *Apply* persists nothing.
The tree looks editable and silently is not.

Two further gaps:

- **No type choice when adding.** `AddChildCommand` hard-codes `StackElement.Config`.
  `ConfigListEditorControl.addElement()` (the form-side *Add* button) takes
  `_choices.options().get(0)` and relies on the user correcting the type afterwards through
  the per-element type selector.
- **Duplicated list mutation.** `ConfigListEditorControl` implements add/remove/move-up/
  move-down against the config list inline (its own `addElement`, `removeElement`, `moveUp`,
  `moveDown`). The designer needs the same operations. Writing them a second time in the
  designer would be two implementations of one thing.

## Two further blockers found while implementing

The context menu did not merely act on the wrong model — it never appeared:

- **Nothing renders the tree's menu.** `ReactTreeControl.openContextMenu(...)` pushed the items,
  position and open flag as control state, but `TLTreeView` reads only `nodes`, `selectionMode`,
  `dragEnabled`, `dropEnabled` and the drop indicators. The state was write-only.
  The framework already has the right mechanism: `ReactMenuControl` / `TLMenu`, which positions
  itself at viewport coordinates, plus `ContextMenuOpener` / `ContextMenuContribution` which compose
  a menu from `CommandModel`s and honour visibility, executability and cliques. So the fix is to
  *use* it and delete the dead tree-local machinery, not to teach `TLTreeView` to render menus.
- **A view without an app shell has no menu overlay.** Only `AppShellElement` creates a
  `ReactMenuControl` and publishes a `ContextMenuOpener` (via `ViewContext.withContextMenuOpener`).
  `designer.view.xml` is rooted in a `<panel>`, so `getContextMenuOpener()` was `null` there and no
  overlay existed to render into. `ViewServlet` already solves the same problem for notifications
  with a *window-level* snackbar, for "windows whose view does not embed an app shell". A menu
  overlay gets the same treatment: one per browser window, since `TLMenu` positions itself
  viewport-fixed anyway.

## Root cause

A `DesignTreeNode` does not record *which property of which configuration* holds it.
`VirtualDesignTreeNode` carries a `PropertyDescriptor`, but `DesignTreeBuilder` inlines
children when a config has exactly one `@TreeProperty` (`inline = treeProperties.size() == 1`),
and in that case no group node exists — so for the most common containers
(`ContainerElement.getChildren()`, `ViewElement.getContent()`) the owning property is lost.

## Solution

### 1. `ConfigChildren` — the shared write-through abstraction

A new class in `com.top_logic.layout.configedit` addressing one structural property of one
configuration item:

```java
public final class ConfigChildren {
    static ConfigChildren create(ConfigurationItem owner, PropertyDescriptor property);

    boolean isList();                              // LIST vs. ITEM kind
    List<ConfigurationItem> elements();            // ITEM renders as 0..1 elements
    int indexOf(ConfigurationItem child);
    boolean canAdd();                              // LIST: always; ITEM: only when unset
    void add(ConfigurationItem child);             // LIST: append; ITEM: set
    boolean remove(ConfigurationItem child);       // LIST: remove; ITEM: reset to null
    boolean move(ConfigurationItem child, int delta);   // LIST only
    ConfigurationItem newElement(Object typeOption);    // via the property's OptionMapping
    PolymorphicOptions.Choices allowedTypes();     // PolymorphicOptions.compute(owner, property)
}
```

It lives in `configedit`, not in `designer`, because it is not designer-specific: it is
"edit a structural property of a config". `com.top_logic.layout.view` already depends on
`tl-layout-configedit`.

`ConfigListEditorControl`'s four mutation methods are re-expressed on top of it, so there is
one implementation of list mutation, used by both the form and the tree.

### 2. Node-to-property binding in the tree

`DesignTreeNode` gains an optional **child container** — the `ConfigChildren` its child
*config* nodes are stored in:

- `VirtualDesignTreeNode` → `(parent config, its property)`
- `ConfigDesignTreeNode` in the inline case → `(own config, the single tree property)`
- otherwise absent

The binding is set by `DesignTreeBuilder`, which is the only place that knows the inline rule,
so the rule is not restated anywhere else.

The origin of a node is then *derived*, not stored a second time:
`node.getParent().getChildContainer()`, valid only when that container actually contains the
node's config (`indexOf(...) >= 0`). This makes the two non-editable cases fall out
automatically rather than needing special cases:

- a **`<view-ref>`-resolved** child (its config comes from another file and is not an element
  of the referencing node's property),
- an **`ErrorDesignTreeNode`** (carries no config at all).

`canExecute` for *Remove* / *Move* is exactly this check, so the context menu stops offering
operations that would silently do nothing.

### 3. Add with type choice

*Add* on a container node offers the types the target property actually allows, from
`ConfigChildren.allowedTypes()` — i.e. `PolymorphicOptions.compute(owner, property)`, the same
option list and `OptionMapping` the form's type selector already uses. No separate registry of
element types, and `@Options`-annotated properties keep working.

A container property of UI elements accepts on the order of 59 element types (the configuration
interfaces derived from `UIElement.Config`), so the types cannot be listed as sibling entries of the
context menu — the original plan of "one *Add ‹type›* entry per type" would produce an unusable
menu. Instead:

- `ConfigTypeChoice.of(children)` yields the choices in label order, and reports
  `isUnique()` when there is nothing to ask about (the common case for a property whose element type
  has no subtypes, e.g. `SidebarElement.getItems()`).
- Context menu: a single *Add element…* entry. When the target property accepts one type, it adds
  directly; otherwise it opens a second menu of the types at the same position.
- The form's *Add* button uses the same `ConfigTypeChoice` and opens a `ReactMenuControl` anchored to
  the button, instead of taking `options().get(0)`.

A filterable dialog would serve a long type list better than a menu. Both call sites go through
`ConfigTypeChoice`, so that can replace the menu later without touching them.

Resolving a property's options loads every candidate implementation class, so `ConfigChildren`
resolves `allowedTypes()` lazily: building the design tree binds a container per node and must not
pay for options that only the *Add* command needs.

After a successful add the new node is selected, so the config editor immediately shows the
new element's properties.

### 4. `@TreeProperty` coverage

Which polymorphic structural properties belong in the tree rather than in the form is a
per-property judgement, and the mechanism for it already exists (`@TreeProperty` plus
`ConfigEditorControl(…, skipTreeProperties)`). 26 properties are annotated today, all in
`com.top_logic.layout.view`. The candidate list of not-yet-annotated structural properties is
to be reviewed separately, after the write-through works — annotating a property before the
tree can edit it only moves the property out of the form and into a read-only tree.

## Out of scope

- Drag & drop in the tree (context menu only, as today).
- Undo/redo (*Revert* remains all-or-nothing).
- Moving an element across containers or across `.view.xml` files.
- Editing inside a `<view-ref>`-resolved subtree (its own file is edited by designing that view).
- XML formatting preservation on save (separate, pre-existing issue).

## Testing

- `ConfigChildren` (JUnit, `com.top_logic.layout.configedit`): add/remove/move on a LIST
  property; add/remove on an ITEM property incl. `canAdd()` when already set; `indexOf` miss;
  `newElement` through the `OptionMapping` for a polymorphic and an `@Options` property.
- Designer (JUnit, `com.top_logic.layout.view`): over a built design tree, assert the child
  container is bound for both the inline and the virtual-group case; assert *Remove* / *Move*
  are not executable for a `<view-ref>` child and an error node; assert that add/remove/move
  are visible in the owning config afterwards, i.e. that a subsequent serialization differs.
- Manual (Playwright, `tl-demo-react`): add an element of a chosen type, remove one, reorder
  one, press *Apply*, and confirm the `.view.xml` on disk changed accordingly and the running
  app reflects it.
