# React Resource Cell Design

## Overview

Add a `TLResourceCell` control that displays business objects in the React UI layer — the equivalent of the legacy `ResourceRenderer`. Renders an optional icon, optional label, optional goto-link, and optional CSS class. Covers both "full resource link" (icon + label + link) and "icon-only" display modes via a single component.

## Architecture: Server-Side Resolution

All `ResourceProvider` data is resolved on the server. The React component receives a flat state object and simply renders it. This leverages the existing `LabelProviderService` / `MetaResourceProvider` infrastructure without duplicating it in TypeScript.

## Components

| Layer | Artifact | Role |
|-------|----------|------|
| Java | `ReactResourceCellControl` | Resolves ResourceProvider data into state, handles goto command |
| React | `TLResourceCell.tsx` | Renders icon + label + link from state |

## State Contract

```ts
{
  label?: string,       // Display text (ResourceProvider.getLabel)
  iconCss?: string,     // CSS icon class (for icon-font ThemeImages)
  iconSrc?: string,     // Image URL (for resource-image ThemeImages)
  cssClass?: string,    // Type-specific CSS class (ResourceProvider.getCssClass)
  tooltip?: string,     // Plain text, rendered as title attribute for now
  hasLink: boolean      // Whether clicking triggers goto navigation
}
```

### Rendering Variants

- **Icon + label + link** (common case): `{ label: "John", iconCss: "bi bi-person", hasLink: true }`
- **Icon only**: `{ iconCss: "bi bi-check-circle", hasLink: false }`
- **Label + link, no icon**: `{ label: "Report.pdf", hasLink: true }`
- **Plain text**: `{ label: "some value", hasLink: false }`

## Server-Side Control

`ReactResourceCellControl extends ReactControl`:

- **Constructor**: `(Object value, ResourceProvider provider, boolean useImage, boolean useLabel, boolean useLink)`
- **State population**: Resolves ResourceProvider methods, populates state fields
- **ThemeImage resolution**: Checks resolved image type — CssIcon populates `iconCss`, Img populates `iconSrc`
- **Goto command**: Handles `"goto"` command by dispatching to `GotoHandler` with the stored row object
- **Update method**: `update(Object value)` re-resolves and patches state when cell value changes (viewport scroll)

## React Component

`TLResourceCell.tsx`:

- Icon: `<i className={iconCss}>` for CSS icons, `<img src={iconSrc}>` for image icons
- Label: `<span>` with label text
- Link wrapper: `<a>` with `onClick` sending `"goto"` command via `useTLCommand`
- No link: `<span>` wrapper instead
- Tooltip: `title` attribute on the wrapper element (placeholder — rich tooltips deferred)

## Navigation

Clicking a linked resource cell sends a `"goto"` command to the server. The `ReactResourceCellControl` holds a transient reference to the row object and invokes `GotoHandler` to navigate to the object's detail view. This follows the existing React command pattern (`useTLCommand`).

## Tooltip (Deferred)

Rich tooltip rendering is out of scope. The state carries a `tooltip` field rendered as a native `title` attribute. A proper tooltip component with structured data and incremental fetching will be designed separately.

## Integration

The `ReactCellControlProvider` creates `ReactResourceCellControl` instances for columns that display business objects, replacing `ReactTextCellControl` where resource rendering is needed.
