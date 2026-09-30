# Input Controls Demo View

## Context

Ticket #29108 added a React-based color input control. A demo view is needed to showcase input controls in the view-based React demo app. The color input is the first control demonstrated; more will follow.

## Design

A new view-based demo showing a DemoTypes.A object tree on the left and an editable form on the right.

### Files

| File | Action |
|------|--------|
| `com.top_logic.demo/.../views/demo/input-controls-demo.view.xml` | Create |
| `com.top_logic.demo/.../views/app.view.xml` | Add nav-item |

### Layout

- Horizontal `<split-panel>` (30% tree / 70% form)
- Left pane: `<tree>` rooted at `DemoTypes#ROOT`, children via `tl.element:StructuredElementContainer#children`, wired to `selectedObject` channel
- Right pane: `<form>` bound to `selectedObject` showing fields: `name`, `color`, `string`, `boolean`, `date`, `float`

### Navigation

New sidebar entry after "Form Demo": id `input-controls-demo`, label "Input Controls", icon `bi bi-input-cursor`.

### No Java code required

The `color` field renders automatically via `ViewColorInputControl` (registered in `FieldControlFactory` for `java.awt.Color` types). All other fields use standard controls.
