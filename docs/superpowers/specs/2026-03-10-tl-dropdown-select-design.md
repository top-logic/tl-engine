# TLDropdownSelect - React Select Control Design

## Overview

A React-based select control (`TLDropdownSelect`) that replaces the legacy `SelectionControl`'s core selection capabilities with a modern chip/tag-based UI. Supports both single and multi-selection, search-as-you-type filtering, custom option rendering with images, and keyboard navigation.

## Scope

**In scope:**
- Auto-completion / search-as-you-type filtering (client-side)
- Custom option rendering with images (URL or CSS class icons)
- Clear button to reset selection
- Keyboard navigation (arrow keys, enter, escape, tab, backspace)
- Single-select and multi-select modes (configurable)
- Lazy option loading on first dropdown open

**Out of scope (future work):**
- Popup dialog opener (separate component)
- Drag-and-drop support
- Tree/table-based selection dialogs

## Architecture

### Component Names

- **React module:** `TLDropdownSelect`
- **Java control:** `ReactDropdownSelectControl extends ReactControl`
- **React file:** `react-src/controls/TLDropdownSelect.tsx`

### Server-Side (Java)

`ReactDropdownSelectControl` wraps a `SelectField` model and exposes it as a React control.

**Initial state sent on render (includes selected option details but not the full option list):**

```json
{
  "value": [
    { "value": "obj-123", "label": "John Smith", "image": "/icons/person.png" }
  ],
  "multiSelect": false,
  "mandatory": false,
  "disabled": false,
  "editable": true,
  "emptyOptionLabel": "Select...",
  "nothingFoundLabel": "Nothing found",
  "optionsLoaded": false
}
```

The `value` field is always an array of option descriptors (`{ value, label, image? }`), not raw IDs. This allows the component to render chips with image + label immediately, even before the full option list has been loaded. For single-select mode, the array contains 0 or 1 elements.

**Commands:**

| Command | Direction | Payload | Description |
|---------|-----------|---------|-------------|
| `loadOptions` | client -> server | none | Requests the full option list. Server responds by patching `options` and `optionsLoaded` into state via SSE. |
| `valueChanged` | client -> server | `{ "value": ["obj-123", "obj-456"] }` | Sends updated selection as an array of option value IDs. Server maps IDs back to model objects. |

**Options payload (sent via SSE PatchEvent after `loadOptions`):**

```json
{
  "options": [
    { "value": "obj-123", "label": "John Smith", "image": "/icons/person.png" },
    { "value": "obj-456", "label": "Jane Doe", "image": "bi bi-person-fill" },
    { "value": "obj-789", "label": "Johanna Lee" }
  ],
  "optionsLoaded": true
}
```

The `image` field is optional. When present: if it starts with `/`, it is treated as an image URL; otherwise as a CSS class name (e.g., Bootstrap Icons, Font Awesome).

**Option invalidation:** The server can push a patch with `"optionsLoaded": false` to force a reload on next open (e.g., when options depend on another field's value). The client clears its cached options and shows the loading spinner on next open.

**I18N:** Both i18n strings are sent as resolved text in the initial state:
- `emptyOptionLabel` - placeholder text when nothing is selected (server-provided with i18n default, e.g., "Select..." / "Auswahl...")
- `nothingFoundLabel` - message when search filter yields no matches (e.g., "Nothing found" / "Keine Treffer")

### Client-Side (React)

`TLDropdownSelect.tsx` is a self-contained React component. It uses `useTLState()` to read server state and `useTLCommand()` to send commands. It does **not** use `useTLFieldValue()` because the value semantics differ from simple scalar fields (value is always an array of descriptors, valueChanged sends an array of IDs).

**Internal state (React-local, not server state):**
- `isOpen: boolean` - dropdown visibility
- `searchTerm: string` - current filter text
- `highlightedIndex: number` - keyboard-navigated option index, initially -1 (nothing highlighted); first Arrow Down sets it to 0

**Data flow:**

1. Form renders. Server sends initial state with `value` (array of option descriptors for selected items), `emptyOptionLabel`, `nothingFoundLabel`, `multiSelect`, `mandatory`, etc. No full option list yet (`optionsLoaded: false`).
2. User clicks anywhere on the control surface (chips, arrow, empty area). If `optionsLoaded` is false, React sends `loadOptions` command. Dropdown opens with a loading spinner.
3. Server responds with `PatchEvent` containing `options` array and `optionsLoaded: true`. Spinner is replaced by search field + option list. Focus moves to the search input.
4. On subsequent opens, cached options display instantly (no spinner, no server round-trip). Focus moves to search input.
5. User types in search field. React filters the cached options client-side (case-insensitive substring match on label).
6. User selects an option (click or Enter). React updates `value` optimistically, sends `valueChanged` command with the array of selected option value IDs.
7. Server can push option list updates via SSE `PatchEvent` at any time. Setting `optionsLoaded: false` forces a reload on next open.

**Error handling for `loadOptions`:** If the command fails (network error, server error), the dropdown shows an i18n error message with a "Retry" link. Clicking the link re-sends `loadOptions`.

## UI States

### 1. Empty (no selection)
Shows `emptyOptionLabel` as placeholder text. Dropdown arrow on the right.

### 2. Single selection (multiSelect=false)
Selected value displayed as a chip with image + label. Clear-all button (x) and dropdown arrow on the right. Clicking anywhere on the control surface (chip, empty space, arrow) opens the dropdown. Selecting a new option replaces the current one and closes the dropdown.

The clear-all button is hidden when `mandatory` is true (selection cannot be empty).

### 3. Multi selection (multiSelect=true)
Selected values displayed as chips with image + label, each with an individual remove button (x). Chips wrap to multiple lines. Clear-all button removes all selections. Dropdown stays open after selecting an option to allow further picks. The clear-all button is hidden when `mandatory` is true.

### 4. Disabled
Greyed out, no interaction possible. No dropdown arrow or clear button.

### 5. Immutable (read-only, editable=false)
Shows selected value(s) as plain image + label text, no border, no buttons. No interaction. Takes precedence over `disabled` (if `editable` is false, `disabled` is irrelevant).

### 6. Dropdown open
- **Top area:** Chip display (selected values) + clear-all + up-arrow
- **Search field:** Text input with magnifying glass icon and "Filter..." placeholder. Receives focus when dropdown opens.
- **Option list:** Scrollable list (max-height ~250px) of options with image + label
- **Highlighted option:** Background highlight for keyboard-navigated item (initially nothing highlighted, -1)
- **Already-selected options** are hidden from the list
- **Match highlighting:** Matched substring in option labels shown in bold
- **Positioning:** Dropdown renders as a portal appended to `document.body`, positioned absolutely below the control. If there is insufficient space below the viewport, it flips to appear above.

### 7. Loading
Shown on first dropdown open while waiting for `loadOptions` response. Spinner centered in dropdown body. Replaced by search + options once data arrives.

### 8. No results
When search filter matches no options, shows `nothingFoundLabel` message centered in dropdown body.

### 9. Error
Shown if `loadOptions` command fails. Displays an error message with a "Retry" link.

## Option Rendering

Each option displays:
- **Image** (optional): `<img>` for URL paths (starting with `/`), `<span className={image}>` for CSS class icons
- **Label**: Text label, with matched search substring highlighted in bold

The same rendering is used for chips (selected values) and dropdown options.

## Keyboard Navigation

| Key | Behavior |
|-----|----------|
| Arrow Down | Open dropdown (if closed). Move highlight to next option (wraps). First press sets highlight to index 0. |
| Arrow Up | Move highlight to previous option (wraps). |
| Enter | Select highlighted option. Close dropdown (single-select) / keep open (multi-select). If no option is highlighted, no-op. |
| Escape | Close dropdown without changing selection. Focus returns to the control. |
| Tab | Close dropdown without selecting the highlighted option. Move focus to next form field. |
| Backspace | In search: delete text. If search is empty and multi-select: remove last chip. |

**Focus management:**
- Opening the dropdown moves focus to the search input field.
- Closing the dropdown (Escape) returns focus to the control's outer container.
- Selecting in single-select mode closes the dropdown and returns focus to the outer container.
- Selecting in multi-select mode keeps focus in the search input.

## Accessibility

- The control outer element uses `role="combobox"` with `aria-expanded`, `aria-haspopup="listbox"`.
- The option list uses `role="listbox"`, individual options use `role="option"` with `aria-selected`.
- The highlighted option is linked via `aria-activedescendant` on the search input.
- Search input has `aria-label` for screen reader context.
- Chips include a visually hidden label for the remove button (e.g., "Remove John Smith").

## CSS

The component uses TopLogic CSS variables for theme integration (`--border-subtle`, `--field`, `--layer-hover`, `--text-primary`, `--text-secondary`, `--spacing-*`, `--form-line-height`, `--focus`).

CSS class prefix: `tlDropdownSelect` (BEM-style, e.g., `tlDropdownSelect`, `tlDropdownSelect__chip`, `tlDropdownSelect__dropdown`, `tlDropdownSelect__option`, `tlDropdownSelect__search`).

## File Structure

```
com.top_logic.layout.react/
  src/main/java/com/top_logic/layout/react/control/
    select/
      ReactDropdownSelectControl.java    # Server-side control (new package)
  react-src/
    controls/
      TLDropdownSelect.tsx               # React component
  src/main/webapp/style/
    tl-dropdown-select.css               # Component CSS
```

The component is registered in `react-src/controls-entry.ts` via `register('TLDropdownSelect', TLDropdownSelect)`.
