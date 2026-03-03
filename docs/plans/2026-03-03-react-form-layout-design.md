# React Form Layout Design

## Overview

A comprehensive React-based form layout system for TopLogic, replacing the legacy server-rendered form display with three composable React controls: TLFormLayout, TLFormGroup, and TLFormField.

## Architecture: Composable Component Tree

The server assembles the control tree (Java `ReactControl` instances). The client handles responsive layout based on viewport/container width.

```
TLFormLayout
  TLFormGroup (collapsible, bordered, header/actions)
    TLFormField (label + chrome + input control)
    TLFormField
    TLFormGroup (nested)
      TLFormField
      TLFormField
  TLFormGroup
    TLFormField
    TLFormField
```

## Components

### TLFormLayout

Top-level grid container for a form.

**State shape:**
- `maxColumns: number` - upper bound on columns (e.g. 3)
- `labelPosition: "side" | "top" | "auto"` - "auto" = side when space allows, top on narrow
- `readOnly: boolean` - propagated to all descendants via React context
- `children: ChildDescriptor[]` - TLFormGroup or TLFormField controls

**Behavior:**
- CSS Grid with `repeat(auto-fit, minmax(var(--form-col-min-width), 1fr))` up to `maxColumns`
- When `labelPosition` is `"auto"`, uses a container query on the field slot to switch to `"top"` when a column is narrower than ~20rem
- Provides `FormLayoutContext` (React context) carrying `readOnly` and resolved `labelPosition`

### TLFormGroup

Nestable section within a form. Participates as a grid item in the parent, uses CSS subgrid internally.

**State shape:**
- `header: string | null` - group heading text
- `headerActions: ChildDescriptor[]` - action buttons in header
- `collapsible: boolean` - can the user collapse this group?
- `collapsed: boolean` - current collapsed state (server-managed)
- `border: "none" | "subtle" | "outlined"` - visual border style
- `fullLine: boolean` - span all parent columns?
- `children: ChildDescriptor[]` - TLFormField or nested TLFormGroup

**Commands:**
- `toggleCollapse` - no arguments, server flips `collapsed`

**Behavior:**
- Full-line: `grid-column: 1 / -1`, defines its own responsive grid
- Non-full-line: `grid-template-columns: subgrid`, inherits parent column tracks
- Header renders when `header` is set, `headerActions` is non-empty, or `collapsible` is true
- When collapsed: `.tlFormGroup__body` gets `display: none`, group retains grid position

**DOM structure:**
```html
<div class="tlFormGroup tlFormGroup--border-subtle">
  <div class="tlFormGroup__header">
    <button class="tlFormGroup__collapseToggle">...</button>
    <span class="tlFormGroup__title">Contact Info</span>
    <div class="tlFormGroup__actions"><!-- action buttons --></div>
  </div>
  <div class="tlFormGroup__body">
    <!-- children -->
  </div>
</div>
```

### TLFormField

Wraps any field input control with the full anatomy chrome.

**State shape:**
- `label: string` - field label text
- `required: boolean` - show required indicator
- `error: string | null` - error message (null = no error)
- `helpText: string | null` - help/description text
- `dirty: boolean` - changed-from-original indicator
- `labelPosition: "side" | "top" | null` - per-field override (null = inherit from context)
- `fullLine: boolean` - span all parent columns?
- `visible: boolean` - false = removed from flow (parent grid reflows)
- `field: ChildDescriptor` - the actual input control

No commands - TLFormField is pure chrome. All interaction goes through the wrapped field control.

**Behavior:**
- `visible: false` renders nothing (grid reflows)
- Reads `readOnly` and `resolvedLabelPosition` from `FormLayoutContext`
- Read-only mode: hides help text, error, dirty indicator. Compact layout.
- Error state: adds `.tlFormField--error` modifier, field controls use ancestor selector for red borders
- Dirty state: shows accent bar on left edge

**Field anatomy - side-by-side labels:**
```
Label * (i)    | [  input control          ]
               |  (!) Error message
               |  Help text
```

**Field anatomy - stacked labels:**
```
Label * (i)
[  input control              ]
(!) Error message
Help text
```

**Field anatomy - read-only:**
```
Label          | Value text
```

## Layout Grid Model

### Column Calculation

```css
.tlFormLayout {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(var(--form-col-min-width), 1fr));
  gap: var(--form-field-gap);
}
```

Server sets `maxColumns`, browser determines actual column count. No breakpoint media queries needed.

### Label Position Switching

When `labelPosition` is `"auto"`, uses CSS container query on the field slot (reacts to actual column width, not viewport width). Below ~20rem threshold, switches from side-by-side to stacked.

### Subgrid Alignment

Multiple groups side-by-side align their fields via CSS subgrid:

```
+-- Parent Grid (3 columns) ---------------------------+
| +-- Group A (2 cols) ----------+ +-- Group B (1 col) -+
| | [label] [input]             | | [label] [input]    |
| | [label] [input] [input]     | | [label] [input]    |
| +------------------------------+ +--------------------+
+-------------------------------------------------------+
```

When parent shrinks, groups wrap naturally.

### Dynamic Reflow

Hidden fields (`visible: false`) return null from React. CSS Grid auto-placement fills gaps - remaining fields shift up/left.

### Read-Only Compact Mode

When `readOnly` is true:
- TLFormField suppresses help text, error area, dirty indicator
- Only renders label + value display
- Reduced vertical height, more fields visible

## CSS Classes

### TLFormLayout
```
.tlFormLayout
.tlFormLayout--readonly
```

### TLFormGroup
```
.tlFormGroup
.tlFormGroup--border-none
.tlFormGroup--border-subtle
.tlFormGroup--border-outlined
.tlFormGroup--fullLine
.tlFormGroup--collapsed
.tlFormGroup__header
.tlFormGroup__collapseToggle
.tlFormGroup__title
.tlFormGroup__actions
.tlFormGroup__body
```

### TLFormField
```
.tlFormField
.tlFormField--side
.tlFormField--top
.tlFormField--readonly
.tlFormField--fullLine
.tlFormField--error
.tlFormField--dirty
.tlFormField__label
.tlFormField__required
.tlFormField__helpIcon
.tlFormField__input
.tlFormField__error
.tlFormField__helpText
.tlFormField__dirtyBar
```

## Design Tokens

```css
--form-col-min-width: 20rem;
--form-label-width: 8rem;
--form-field-gap: var(--spacing-03);
--form-group-gap: var(--spacing-04);
--form-dirty-color: var(--layer-accent);
--form-error-color: var(--text-error, #da1e28);
```

All other spacing, typography, and color tokens reuse the existing design token system.

## FormLayoutContext (React Context)

Client-side only, not part of server state. Derived from TLFormLayout state and container measurements:

```typescript
interface FormLayoutContext {
  readOnly: boolean;
  resolvedLabelPosition: "side" | "top";
}
```

Consumed by TLFormField. Updated when container width crosses the label-position threshold.

## Design Decisions

- **Character count** is out of scope for the form layout - it belongs to individual input controls.
- **Tab/focus order** follows DOM order, no special handling in this phase.
- **Field sizing** is binary: normal (1 column) or full-line. No partial spans.
- **Form scrolling**: the form always scrolls as a whole.
- **Field ordering**: source order preserved, no mobile-specific reordering.
- **Collapse behavior**: collapsed groups retain their grid position (header stays visible).
- **Error display**: layout positions error messages, field controls show visual indicators (red borders).
- **Form-level loading states**: postponed to a later phase.
