---
description: Read when a component library is to draw the atomic elements of the React UI - buttons, menu triggers, disclosure toggles, checkboxes, text inputs, read-only values, chips, badges, dividers, progress bars, spinners - or when a control draws one of them. Covers the part registry of tl-react-bridge (registerPart, replacePart, usePart, PartProps), the list of parts and where they are drawn, a part adapter, and adding a part.
order: 38
---

# Parts: the atoms of the React UI

The React UI is drawn in two layers:

- **Parts** (atoms) only draw: a button, a checkbox, a text box, a chip, … A part has a typed props
  contract and no server state; it knows no control, sends no command and binds no keyboard
  shortcut.
- **Controls** map the state the server sends to the props of their parts and send the commands:
  they handle the keyboard bindings, the navigation, the `hidden` flag and the defaults of their
  container, and draw the result with parts.

Every place that shows a button draws the part `Button`: the button control `TLButton`, a toggle
button, an entry of a menu, the action buttons of a toolbar, a panel, a table row or a dialog. So
the elements look the same everywhere, and a component library that replaces a part reaches every
one of these places with one replacement. How a component library attaches as a whole — theme,
root wrappers, replacements of whole controls — is described in
[Rendering the React UI with a customer's component library](doc:view-layer/customer-component-library).

## The registry

`tl-react-bridge` keeps the parts in a registry of their own beside the registry of the controls
(`com.top_logic.layout.react/react-src/bridge/parts.ts`):

- **`PartProps`** maps each part name to its props interface (`PartProps['Button']` is
  `ButtonPartProps`). `PartName` is the union of the names, `PartComponent<K>` the component type
  drawing the part `K`. The props interfaces are exported from `tl-react-bridge` and document every
  prop and what every implementation of the part guarantees (attributes, roles, ARIA states).
- **`registerPart(name, component)`** registers the TopLogic drawing of a part. The controls bundle
  registers one for every part (`react-src/controls-entry.ts`).
- **`replacePart(name, component)`** draws the part with the given component instead. A
  replacement wins over the registered default, whatever the order the scripts load in. A second
  `registerPart` or a second `replacePart` of the same name logs a console warning; the later one
  wins.
- **`usePart(name)`** returns the component drawing the part: the replacement, else the default.
  It throws for a part nothing is registered for. A control looks up its parts each time it
  renders; a library registers its replacements when its script loads, before the controls of the
  page are mounted.

All names and props are typed: `replacePart('Checkbox', C)` accepts only a component taking
`CheckboxPartProps`.

## The parts

| Part | Props | What it is | Drawn by |
|---|---|---|---|
| `Button` | `ButtonPartProps` | a button, an icon-only button, a toggle (`checked`), an entry of a menu (appearance `menu-item`), a link drawn as a button (`href`) | `TLButton` and `TLToggleButton` (also as buttons of a toolbar and entries of a menu), the action buttons of panels, drawers, tables, alerts, snackbars, form fields, dropdowns, file chips and the calendar, the clear button of a text input, the drag handles of the chosen values of a dropdown and of the column selection |
| `MenuTrigger` | `MenuTriggerPartProps` | the button opening a menu or another surface anchored to it, with its chevron or an icon alone; `haspopup` names the kind of surface for `aria-haspopup` (`menu` unless given; `listbox`, `tree`, `grid`, `dialog`) | the menu of a toolbar, a menu region |
| `DisclosureToggle` | `DisclosureTogglePartProps` | the chevron showing or hiding content below it or toward the end of the line | form groups, nodes of a tree, rows of a tree table, the sidebar |
| `Checkbox` | `CheckboxPartProps` | a box that is ticked, or a switch | `TLCheckbox`, choice groups, the selection column of a table, the column selection |
| `TextInput` | `TextInputPartProps` | a text box, single-line (text, password, number, search, …) with optional icon and end action, or multi-line | `TLTextInput`, `TLPasswordInput`, `TLNumberInput`, the search field (`SearchField`) of a table and of its column selection, the name of a filter to save, the filters of a dropdown and of the icon and color popups |
| `FieldValue` | `FieldValuePartProps` | the one read-only value, as text or link, a value of a color role as a pill (part `Chip`) within it | every text, password, number, select, date and slider field that is not editable; the chosen values of a choice field (`OptionValues`); the object in a table cell (`TLResourceCell`) |
| `Chip` | `ChipPartProps` | a compact value: with a color role, a toggle, removable, reorderable by a handle, or running an action | the toggles of `TLOptionChips`, the chosen values of a dropdown, the named filters of a table, the files of a file chip field, the options of a color role in a list, read-only values of a color role (inside `FieldValue`), texts drawn as a pill |
| `Badge` | `BadgePartProps` | a small marker carrying a count | the counts of the sidebar and the bottom bar |
| `Divider` | `DividerPartProps` | a line between content, horizontal or vertical | between the groups of a toolbar, the sections of a toolbar menu, the items of the sidebar |
| `ProgressBar` | `ProgressBarPartProps` | a determinate or indeterminate bar with an optional label naming it, or named by another element | `TLProgress`, the status of a job, the loading options of a dropdown, the loading children of a tree node |
| `Spinner` | `SpinnerPartProps` | a turning ring: small in place of an icon, or a busy indicator of its own | the icon popup loading its icons |

A part may draw another part through `usePart`: the `FieldValue` of a color role holds a `Chip`, a
removable `Chip` draws its remove button with the `Button`, the `TextInput` takes a `Button` as its
end action. A replacement of the inner part then appears inside the outer one, whichever of them is
replaced.

The shared component `OptionValues` of `tl-react-bridge` shows the chosen values of a choice field
that is not editable with the part `FieldValue`; a replacement of a choice control uses it to show
such values the way the TopLogic controls do.

## Part or control?

A component library has two ways in (see
[Rendering the React UI with a customer's component library](doc:view-layer/customer-component-library#parts-and-controls)):

- **Replace a part** when the library has a counterpart for an atom: a button, a checkbox, a text
  box, a chip. The replacement draws the atom wherever any control draws it — as a control of its
  own and inline in a toolbar, a table, a tree or a menu. It receives resolved props; the control
  keeps the server state, the commands and the keyboard.
- **Replace a control** (`replace`) when the library has a counterpart for a whole composite: a
  window, a dialog, a menu, a select, a tab bar, the frame of a form field. The replacement renders
  the complete state of the control and sends its commands.

The atomic controls `TLButton`, `TLToggleButton`, `TLCheckbox`, `TLTextInput`,
`TLPasswordInput`, `TLNumberInput`, `TLOptionChips` and `TLProgress` draw their look through parts
only. Replace their parts, not the controls: a replaced part also reaches the inline places the
control never covers.

## A part adapter

A part adapter is a plain React component taking the props of the part; it reads no control state
and sends no command (shortened from
`com.top_logic.layout.react.mui/react-src/adapters/MuiCheckboxAdapter.tsx`, which in addition
draws the switch and a label):

```tsx
import { React } from 'tl-react-bridge';
import type { CheckboxPartProps } from 'tl-react-bridge';
import Checkbox from '@mui/material/Checkbox';

const MuiCheckboxAdapter: React.FC<CheckboxPartProps> = ({
  id, ref, checked, indeterminate, small, disabled, readOnly, required, validation, name, tabIndex,
  className, onChange, ...attributes
}) => {
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (readOnly || disabled) return;
    onChange?.(event.target.checked);
  };
  return (
    <Checkbox id={id} name={name} checked={checked} indeterminate={indeterminate}
      onChange={handleChange} disabled={disabled} readOnly={readOnly}
      size={small ? 'small' : undefined}
      color={validation ?? 'primary'}
      className={className}
      slotProps={{ input: {
        ...attributes, ref, tabIndex,
        'aria-readonly': readOnly || undefined,
        'aria-checked': indeterminate ? 'mixed' : undefined,
        'aria-invalid': validation === 'error' ? true : undefined,
        'aria-required': required || undefined,
      } }} />
  );
};
```

The library registers it under the part name:

```ts
import { replacePart } from 'tl-react-bridge';

replacePart('Checkbox', MuiCheckboxAdapter);
```

What an adapter has to keep:

- **The guarantees of the props interface.** Each interface states the attributes every
  implementation sets — ARIA states, roles, where the `id`, the `ref` and the passed-through
  `aria-*` / `data-*` attributes (`PartAttributes`) go. Controls rely on them: the label of a form
  field names its input through the `id`, a popover anchors at the `ref`, a menu finds its entries
  by their role.
- **Controlled drawing.** A checkbox, a text box and a toggle show exactly the value they are given
  and report a change through their callback; the new value arrives with the next render.
- **The helpers of the button.** `buttonPartAttributes(props)` returns the attributes an
  implementation of `Button` sets (accessible name, pressed / current / checked state, role and
  roving tabindex of a menu entry, tooltip), `buttonLinkAttributes(props)` those of the anchor of a
  link. `MuiButtonAdapter.tsx` spreads both.

The MUI adapter of every part is in `com.top_logic.layout.react.mui/react-src/adapters/` with its
test; tests mounting the TopLogic controls with the MUI parts (`checkboxes.test.tsx`,
`chips.test.tsx`, `inline-buttons.test.tsx`, …) check that one replacement reaches every place
the part is drawn.

## Drawing a part in a control

A control obtains the part with `usePart` and maps its state to the props
(`com.top_logic.layout.react/react-src/controls/TLCheckbox.tsx`, shortened):

```tsx
const TLCheckbox: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CheckboxStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const [value, setValue] = useTLFieldValue();
  const Checkbox = usePart('Checkbox');
  if (state.hidden === true) return null;
  return (
    <Checkbox id={controlId} {...labelProps}
      checked={value === true}
      onChange={checked => setValue(checked)}
      disabled={state.disabled === true}
      readOnly={showsValueOnly(state)}
      validation={validationOf(state)}
      className={rootClassName(state) || undefined} />
  );
};
```

A control or composition never draws an atom with its own markup where a part exists: a hand-drawn
`<button>` keeps the TopLogic look under every component library.

## Placing a part

A site places a part through an element of its own around it, or through its container — never
through the `className` of the part:

```tsx
// The search of the popup of a dropdown stands above the list of options.
<div className="tl-select__search-group">
  <TextInput icon={ICON_SEARCH} value={searchTerm} onChange={setSearchTerm} />
</div>
```

```css
.tl-select__search-group {
  flex: 0 0 auto;
  margin-bottom: var(--tl-space-xs);
}
```

An implementation of a part styles its root itself, and the rules of a component library win
against the stylesheets of TopLogic: the page orders them in the cascade layers `tl, mui` (see
[Where customer CSS goes](doc:view-layer/mui-customer-theme#where-customer-css-goes)), whatever the
specificity. A margin, width, `min-width`, `flex`, `position` or `display` set on the part through
its `className` is lost as soon as the library sets the same property on its root — MUI sets the
margin of a text field to 0, the position of a badge to `relative`, the display of a button to
`inline-flex`. So:

- **Layout belongs to the site.** An element around the part takes the margin, the size, the flex
  or grid placement, the absolute position over a corner, the visibility at a breakpoint. A
  `display: grid` wrapper lets the part fill it.
- **Or to the container.** A row that gives a part the rest of its width is a grid
  (`grid-template-columns: auto minmax(0, 1fr)`) rather than a flex row with `flex: 1` on the part.
- **The `className` of a part only marks it** — for tests, for the measuring of a control (the
  width of the dividers of a toolbar), for a look the TopLogic drawing reads (the funnel of a
  column, the drag handle of a list). A look rule on such a class reaches the TopLogic drawing only; a library
  draws the part in its own look.

## Adding a part (engine developers)

1. In `react-src/bridge/parts.ts`, add the props interface (extending `PartAttributes`, every prop
   documented, together with what every implementation guarantees) and its entry in `PartProps`.
   Export the interface and its value types from `react-src/bridge-entry.ts`.
2. Write the TopLogic drawing (e.g. `react-src/controls/chip/ChipPart.tsx`) and register it in
   `react-src/controls-entry.ts` with `registerPart`.
3. Draw the atom through `usePart` in every control and composition that shows it.
4. Write the adapter of each library module: in `com.top_logic.layout.react.mui`, an
   `adapters/Mui<Part>Adapter.tsx`, its entry in `PART_ADAPTERS` of `react-src/install.ts`, which
   adds the name to `COMPONENT_NAMES`.
5. Test the adapter (`Mui<Part>Adapter.test.tsx`: the props drawn as the interface states) and the
   controls with the replaced part (a test mounting the controls below the MUI root, as
   `chips.test.tsx` does).
