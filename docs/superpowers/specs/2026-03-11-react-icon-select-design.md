# React Icon Select Control Design

## Overview

Replace the legacy `IconInputControl` / `IconChooserControl` with a modern React-based icon select control (`TLIconSelect`). The control allows selecting icons from the TopLogic icon library (e.g. Font Awesome) with search/filter and an advanced mode for editing the encoded CSS class directly.

## Trigger Button

- 32px square swatch button showing the currently selected icon.
- Placeholder icon (`fa-solid fa-icons`, dimmed) when no icon is set.
- States: selected, empty, disabled, immutable.
- Clicking opens the popup picker.

## Popup

Two tabs: **Simple** and **Advanced**.

### Simple Tab (default)

- Search input at the top for filtering icons by name, label, or search terms.
- Reset button (x) in the search bar to clear the current icon selection.
- Scrollable icon grid (40px cells, auto-fill columns).
- All style variants (solid, regular, brands) shown as separate cells.
- Clicking an icon selects it immediately and closes the popup.
- Tooltip on hover shows the icon label.

### Advanced Tab

- Same search input and icon grid as Simple tab (with reduced max-height).
- Below the grid: an editable "Class" text field showing the encoded form (e.g. `css:fa-solid fa-home`).
- Clicking an icon in the grid populates the text field (does not close popup).
- User can edit the text to add extra CSS classes (e.g. `tl-danger`).
- Live preview area shows the resulting icon.
- OK/Cancel buttons to confirm or discard.

## Data Loading

Icons are loaded lazily on first popup open via a `loadIcons` server command, matching the `loadOptions` pattern in `TLDropdownSelect`. The server reads icon metadata from configured `IconBundle`s using the existing `IconInputControl.Config` infrastructure.

## Server State

| Key | Type | Description |
|-----|------|-------------|
| `value` | `string \| null` | Encoded ThemeImage (e.g. `"css:fa-solid fa-home"`) |
| `editable` | `boolean` | Whether the field is editable |
| `icons` | `IconEntry[]` | Icon metadata (populated on `loadIcons`) |
| `iconsLoaded` | `boolean` | Whether icons have been loaded |

Each `IconEntry`:
```
{ prefix: string, label: string, variants: [{ encoded: string }] }
```

## Commands

- `valueChanged({ value: string | null })` - Client sends selected icon value.
- `loadIcons()` - Client requests icon library data.

## Files

| File | Module | Description |
|------|--------|-------------|
| `ReactIconSelectControl.java` | `com.top_logic.layout.react` | Server-side control, extends `ReactFormFieldControl` |
| `TLIconSelect.tsx` | `react-src/controls/` | Main React component |
| `IconSelectPopup.tsx` | `react-src/controls/icon/` | Popup sub-component |
| `controls-entry.ts` | `react-src/` | Registration (add import + register) |
| `tlReactControls.css` | `style/` | CSS styles (append) |

## Visual Prototype

See `docs/prototypes/icon-select-prototype.html` for the interactive mockup.
