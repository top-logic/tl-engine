import type React from 'react';
import type { ComponentType } from 'react';
import type { ButtonAppearance } from './button-defaults';
import { menuItemProps } from './button-defaults';
import type { ButtonStateJson } from '../state/control-state';
import type { ReorderItemProps } from './list-reorder';
import { TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED } from './tooltip-host';

// The parts (atoms) of the React UI: the presentational elements the controls draw themselves with
// - a button, the trigger of a menu, the toggle disclosing content, a checkbox, a text box, the value
// of a field, a chip, a badge, a divider, a progress bar, a spinner, a list to pick from, a button copying a text. A part has a typed props contract and no server state: the control
// maps its state to the props of the part, sends its commands and handles keyboard bindings and
// navigation, and the part only draws. The controls bundle registers a default for every part (registerPart); a customer
// component library replaces a part (replacePart) and so changes how the part looks wherever a
// control draws it - as a control of its own, in a toolbar or in a table.
//
// A part is added by a props interface, an entry in PartProps, a default registered by the controls
// bundle and, where a library draws it, an adapter of that library.
//
// A site places a part through an element of its own around it or through its container (a flex or
// grid row giving the part its track), never through the className of the part: an implementation
// styles the root of the part itself, and the rules of a component library win against the
// stylesheets of TopLogic (cascade layers `tl, mui`), so margin, width, flex, position or display
// set through the className would be lost. The className of a part only marks it.

/**
 * Attributes a part passes on to the element it draws as they are: the `aria-*` attributes naming
 * or describing the element where the props of the part do not, and `data-*` attributes another
 * component finds the element by.
 */
export interface PartAttributes {
  [attribute: `aria-${string}`]: string | number | boolean | undefined;
  [attribute: `data-${string}`]: string | number | boolean | undefined;
}

/**
 * The ref of a part: receives the element the part draws. A callback or a ref object, as React
 * takes them, stated without the types of React, so that an adapter module type-checking against
 * its own React types passes it to the components of its library.
 */
export type PartRef<T> = ((instance: T | null) => void) | { current: T | null } | null;

/**
 * What a button part shows of its {@link ButtonPartProps.image} and its
 * {@link ButtonPartProps.label}: the display mode a button control resolves from its state and its
 * container.
 */
export type ButtonDisplay = ButtonStateJson.DisplayMode;

/**
 * The props of the part {@link PartProps.Button}: a button, or an entry of a menu.
 *
 * <p>All values are resolved: the control has already applied the defaults of its container
 * ({@link ButtonAppearance} `menu-item` inside a menu, `icon-only` in a compact toolbar), so the
 * part draws exactly what it is given.</p>
 *
 * <p>A button with {@link checked} is a toggle: it always has a pressed state. Outside a menu it is
 * marked pressed while checked, inside a menu it is a checkbox entry. A button with {@link active}
 * is the alternative in force (e.g. the active theme): outside a menu it is marked pressed, inside
 * a menu it is the current entry.</p>
 *
 * <p>A button with {@link href} is a link: pressing it navigates to the address, as a link does,
 * so the button is drawn as an anchor with the look of a button.</p>
 *
 * <p>{@link buttonPartAttributes} gives the attributes every implementation sets on its element:
 * the accessible name, the pressed, current or checked state, the role and roving tabindex of a
 * menu entry, the tooltip. {@link buttonLinkAttributes} gives the attributes of the anchor of a
 * link.</p>
 */
export interface ButtonPartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /**
   * Receives the drawn element, e.g. as anchor of a popover: the button, or the anchor of a link
   * ({@link href}).
   */
  ref?: PartRef<HTMLElement>;

  /**
   * The address the button navigates to. A button with an address is a link, drawn as an anchor
   * with the look of the button. A disabled link has no address and is marked `aria-disabled`.
   */
  href?: string;

  /**
   * The browsing context a link ({@link href}) opens its address in, e.g. `_blank` for a window
   * of its own; absent: the current one. A link into a window of its own gets no access to the
   * page that opened it (`rel="noopener noreferrer"`).
   */
  target?: string;

  /**
   * The label. It is the accessible name of the button, also when the button does not show it,
   * and its tooltip wherever the button does not read it out.
   */
  label?: string;

  /** The icon, a theme image in its encoded form (see `ThemeIcon`); absent: none. */
  image?: string;

  /**
   * What the button shows. A button without {@link image} shows its label in every mode. Inside a
   * menu, an entry with an icon shows icon and label.
   */
  display: ButtonDisplay;

  /** How the button is drawn; `menu-item` draws an entry of the menu around it. */
  appearance: ButtonAppearance;

  /** Whether the button triggers a destructive action. A `link` shows no tone. */
  danger?: boolean;

  /** Whether the button is drawn small; applies to a button showing its icon alone. */
  small?: boolean;

  /** Whether the button stands for the alternative currently in force. */
  active?: boolean;

  /** The pressed state of a toggle; absent for a button that is no toggle. */
  checked?: boolean;

  /** Inside a menu: the toggle is one alternative of a choice ({@link ButtonDefaultsValue.radio}), a radio entry. */
  radio?: boolean;

  /** Whether the button cannot be pressed. */
  disabled?: boolean;

  /**
   * The tab index of the button; `-1` keeps it out of the tab order, e.g. the remove button of a
   * chip in a field that offers removal by its own keys. Absent: the button is in the tab order.
   */
  tabIndex?: number;

  /**
   * An explicit tooltip, shown whatever the button shows. Absent: the label is the tooltip wherever
   * the button does not read it out.
   */
  tooltip?: string;

  /**
   * Classes added to the drawn element. They mark the part and carry no layout: the element
   * around the part places it.
   */
  className?: string;

  /** Called when the button is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /** Called when a mouse button goes down on the button, e.g. to keep the focus where it is. */
  onMouseDown?: (event: React.MouseEvent<HTMLElement>) => void;

  /** Called when a pointer goes down on the button, e.g. to arm the drag of the item it is the handle of. */
  onPointerDown?: (event: React.PointerEvent<HTMLElement>) => void;

  /** Called for a key pressed on the focused button, e.g. to move the item it is the handle of. */
  onKeyDown?: (event: React.KeyboardEvent<HTMLElement>) => void;
}

/**
 * The kind of surface a {@link MenuTriggerPartProps menu trigger} opens, the value of its
 * `aria-haspopup`: a menu, a list of options to choose from (`listbox`), a tree, a grid, or a
 * dialog-like surface of arbitrary content (`dialog`), e.g. a popover.
 */
export type MenuTriggerPopup = 'menu' | 'listbox' | 'tree' | 'grid' | 'dialog';

/**
 * The props of the part {@link PartProps.MenuTrigger}: the button that opens a menu or another
 * surface anchored to it.
 *
 * <p>Its content is, in this order of precedence: the given {@link children}; the {@link icon}
 * alone, with the label as accessible name and tooltip; or the {@link label} followed by a chevron
 * pointing at the surface. Every implementation marks the trigger with the kind of surface it opens
 * ({@link haspopup}, `aria-haspopup`) and whether that surface is open ({@link expanded},
 * `aria-expanded`).</p>
 */
export interface MenuTriggerPartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /** Receives the drawn element, the anchor of the surface and the inside of its outside press. */
  ref?: PartRef<HTMLButtonElement>;

  /** How the trigger is drawn, like the buttons beside it. */
  appearance: ButtonAppearance;

  /** Whether the surface of the trigger is open. */
  expanded: boolean;

  /** The kind of surface the trigger opens, `menu` if not given. */
  haspopup?: MenuTriggerPopup;

  /** The label; the accessible name of an icon-only trigger. */
  label?: string;

  /** The icon of an icon-only trigger, a theme image in its encoded form (see `ThemeIcon`). */
  icon?: string;

  /** Whether the trigger is drawn small; applies to a trigger showing its icon alone, like the part `Button`. */
  small?: boolean;

  /** Free content of the trigger, drawn instead of label and icon. */
  children?: React.ReactNode;

  /**
   * Classes added to the drawn element. They mark the part and carry no layout: the element
   * around the part places it.
   */
  className?: string;

  /** Inline style of the drawn element. */
  style?: React.CSSProperties;

  /** Called when the trigger is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /** Called when a mouse button goes down on the trigger, e.g. to keep the focus where it is. */
  onMouseDown?: (event: React.MouseEvent<HTMLElement>) => void;
}

/**
 * The direction the content of a {@link DisclosureTogglePartProps disclosure toggle} extends in
 * from the toggle when it is shown:
 *
 * <ul>
 * <li>`below`: the content opens below the toggle - a section, a node of a tree, a row of a tree
 *     table. The chevron points to the end of the line while the content is hidden, and down
 *     while it is shown.</li>
 * <li>`end`: the content opens toward the end of the line - a sidebar extending into the page.
 *     The chevron points to the end while the content is hidden, and back to the start while it is
 *     shown.</li>
 * </ul>
 */
export type DisclosureDirection = 'below' | 'end';

/**
 * The props of the part {@link PartProps.DisclosureToggle}: the button that shows or hides content
 * belonging to it - the body of a section, the children of a node in a tree, a sidebar.
 *
 * <p>The toggle shows a chevron pointing where the content opens ({@link opens}) and is named by its
 * {@link label}, which is also its tooltip. Every implementation marks whether the content is shown
 * ({@link expanded}, `aria-expanded`); the element holding the content is named by `aria-controls`
 * where the control has one.</p>
 */
export interface DisclosureTogglePartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /** Receives the drawn element. */
  ref?: PartRef<HTMLButtonElement>;

  /** Whether the content of the toggle is shown. */
  expanded: boolean;

  /**
   * The accessible name and tooltip of the toggle: what pressing it does, e.g. "Expand" or
   * "Collapse".
   */
  label: string;

  /** Where the content opens from the toggle; absent: `below`. */
  opens?: DisclosureDirection;

  /** Whether the toggle cannot be pressed. */
  disabled?: boolean;

  /**
   * The tab index of the toggle; `-1` keeps it out of the tab order, e.g. in a tree, which handles
   * the keyboard for its nodes itself. Absent: the toggle is in the tab order.
   */
  tabIndex?: number;

  /**
   * Classes added to the drawn element. They mark the part and carry no layout: the element
   * around the part places it.
   */
  className?: string;

  /** Called when the toggle is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /**
   * Called when a mouse button goes down on the toggle, e.g. to keep the focus on the tree it
   * belongs to.
   */
  onMouseDown?: (event: React.MouseEvent<HTMLElement>) => void;
}

/**
 * The shape of a {@link CheckboxPartProps checkbox}:
 *
 * <ul>
 * <li>`checkbox`: a box that is ticked;</li>
 * <li>`switch`: a toggle sliding between its two states, with the role `switch`. A switch has no
 *     indeterminate state.</li>
 * </ul>
 */
export type CheckboxDisplay = 'checkbox' | 'switch';

/**
 * The state of the value an input part ({@link CheckboxPartProps checkbox},
 * {@link TextInputPartProps text box}) stands for, as the checks of its form field judge it:
 *
 * <ul>
 * <li>`error`: the value is invalid; the input is marked `aria-invalid`.</li>
 * <li>`warning`: the value is questionable without being invalid.</li>
 * </ul>
 */
export type FieldValidation = 'error' | 'warning';

/**
 * The props of the part {@link PartProps.Checkbox}: a box that is ticked, or a switch that is
 * flipped - the input of a boolean field, the selection of a row, the choice whether a column is
 * shown.
 *
 * <p>The checkbox is controlled: it shows {@link checked} and {@link indeterminate} as given, and a
 * press asks for the new state through {@link onChange}; it shows the new state only once the
 * control passes it.</p>
 *
 * <p>Every implementation draws an indeterminate box as indeterminate and states the mixed state to
 * assistive technology - a native checkbox through its `indeterminate` property, an input that does
 * not carry it by `aria-checked="mixed"` -, marks a read-only box `aria-readonly`, an invalid one `aria-invalid` and a
 * required one `aria-required`, and puts {@link id}, {@link ref}, {@link name}, {@link tabIndex} and
 * the passed-through attributes on the focusable input. A checkbox without {@link label} is named by
 * the control: by an `aria-label`, or by `aria-labelledby` naming the label of its form field.</p>
 */
export interface CheckboxPartProps extends PartAttributes {
  /** The ID of the input. */
  id?: string;

  /** Receives the input. */
  ref?: PartRef<HTMLInputElement>;

  /** Whether the box is ticked, the switch on. */
  checked: boolean;

  /**
   * Whether the box shows that it is neither ticked nor clear: the "no value" of a tri-state field,
   * the selection of some of the rows of a table. It is drawn instead of {@link checked}. Ignored
   * for a switch.
   */
  indeterminate?: boolean;

  /** The shape; absent: `checkbox`. */
  display?: CheckboxDisplay;

  /**
   * The label shown beside the box: a text, or content such as the icon and text of an option. It
   * names the box, and pressing it presses the box. Absent: the box is drawn alone.
   */
  label?: React.ReactNode;

  /** Whether the box is drawn small, e.g. in the narrow selection column of a table. */
  small?: boolean;

  /**
   * Whether a box drawn without {@link label} starts at the start edge of the space it is given, in
   * line with the content above and below it, e.g. the box of a boolean field below its label. A
   * library drawing a padded touch area around the box lets that padding hang over the start edge.
   * A box with a label is always drawn so.
   */
  alignStart?: boolean;

  /** Whether the checkbox cannot be pressed; it is drawn inactive. */
  disabled?: boolean;

  /**
   * Whether the checkbox shows its state only: it is drawn as an active one, but a press changes
   * nothing and calls no {@link onChange}.
   */
  readOnly?: boolean;

  /** Whether the value is required. */
  required?: boolean;

  /** The state of the value; absent: valid. */
  validation?: FieldValidation;

  /** The name of the input. */
  name?: string;

  /**
   * The tab index of the input; `-1` keeps it out of the tab order, e.g. in the row of a table,
   * which handles the keyboard for its rows itself. Absent: the input is in the tab order.
   */
  tabIndex?: number;

  /**
   * Classes added to the outermost drawn element: the label around the box, else the box. They
   * mark the part and carry no layout: the element around the part places it.
   */
  className?: string;

  /**
   * Called when the checkbox is pressed, with the state it asks for: the opposite of
   * {@link checked}, `true` for an indeterminate box. Not called while {@link disabled} or
   * {@link readOnly}.
   */
  onChange?: (checked: boolean) => void;
}

/**
 * What a {@link TextInputPartProps text box} holds, the `type` of a single-line input: plain
 * `text`, a `search` term, a web address (`url`), an e-mail address (`email`), a telephone number
 * (`tel`), a `password`, which the box masks, or a `number`, which the browser restricts to what it
 * reads as a number.
 */
export type TextInputType = 'text' | 'search' | 'url' | 'email' | 'tel' | 'password' | 'number';

/**
 * The on-screen keyboard a {@link TextInputPartProps text box} asks for, the `inputmode` of the
 * input.
 */
export type TextInputMode = 'text' | 'numeric' | 'decimal' | 'search' | 'email' | 'tel' | 'url' | 'none';

/**
 * The props of the part {@link PartProps.TextInput}: a box text is typed into - the input of a text,
 * password or number field, the search of a table, the filter of a list.
 *
 * <p>The box is controlled: it shows {@link value} as given, and each keystroke reports the text
 * typed through {@link onChange}; it shows the new text only once the control passes it.</p>
 *
 * <p>A single-line box may show an {@link icon} ahead of the text - the magnifier of a search -
 * and an {@link action} at its end - the button emptying it, the link opening what it holds. The
 * box keeps the input in place while icon and action come and go, so that typing keeps the focus.
 * A multi-line box ({@link multiline}) draws neither.</p>
 *
 * <p>Every implementation marks an invalid box `aria-invalid` and a required one
 * `aria-required`, and puts {@link id}, {@link ref}, {@link role}, {@link name}, the key, focus
 * and blur handlers and the passed-through attributes on the input. The box has no label of its own: the
 * control names it, by an `aria-label`, or by `aria-labelledby` naming the label of its form
 * field.</p>
 */
export interface TextInputPartProps extends PartAttributes {
  /** The ID of the input. */
  id?: string;

  /**
   * Receives the input, e.g. to give it the focus, or to read and place the caret
   * (`selectionStart`, `setSelectionRange`).
   */
  ref?: PartRef<HTMLInputElement | HTMLTextAreaElement>;

  /**
   * The role of the input: `combobox` for a box controlling a list of suggestions (part
   * {@link PartProps.Listbox}), which the passed-through `aria-autocomplete`, `aria-expanded`,
   * `aria-controls` and `aria-activedescendant` attributes describe. Absent: the native role of
   * the input.
   */
  role?: 'combobox';

  /** The text shown. */
  value: string;

  /** What the box holds; absent: `text`. Ignored for a multi-line box. */
  type?: TextInputType;

  /** The on-screen keyboard the input asks for; absent: the one of the {@link type}. */
  inputMode?: TextInputMode;

  /** The text shown while the box is empty. */
  placeholder?: string;

  /** Whether the box is a text area of several lines, in which Enter is part of the text. */
  multiline?: boolean;

  /** The number of visible lines of a {@link multiline} box; absent: 3. */
  rows?: number;

  /**
   * Whether a {@link multiline} box shows its text as it is written: in a font of fixed width, each
   * line without wrapping it, a line wider than the box scrolling horizontally. Ignored for a
   * single-line box.
   */
  preformatted?: boolean;

  /**
   * The icon shown inside a single-line box ahead of the text, a theme image in its encoded form
   * (see `ThemeIcon`); absent: none. It is decoration, hidden from assistive technology.
   */
  icon?: string;

  /**
   * The action at the end of a single-line box, inside its frame: a part `Button` drawn
   * `icon-only`, `ghost` and `small`, e.g. the button emptying the box. It replaces the browser's own
   * clear button of a `search` box, which the box then does not show. Absent: none.
   */
  action?: React.ReactNode;

  /** Whether the box cannot be edited; it is drawn inactive. */
  disabled?: boolean;

  /** Whether the text can be selected and copied but not edited. */
  readOnly?: boolean;

  /** Whether a value is required. */
  required?: boolean;

  /** The state of the value; absent: valid. */
  validation?: FieldValidation;

  /** The tooltip of the input, e.g. the message of an invalid value; absent: none. */
  tooltip?: string;

  /** Whether the input takes the focus when it is drawn. */
  autoFocus?: boolean;

  /** The name of the input. */
  name?: string;

  /** The maximum number of characters the box takes. */
  maxLength?: number;

  /** The least value of a `number` box. */
  min?: number;

  /** The greatest value of a `number` box. */
  max?: number;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout: the
   * element around the part places it.
   */
  className?: string;

  /**
   * Called with the text of the box after each change. Not called while {@link disabled} or
   * {@link readOnly}.
   */
  onChange?: (value: string) => void;

  /**
   * Called for a key pressed in the box, with the event of the input, e.g. to finish an edit with
   * Enter or to move through a list with the arrow keys. In a multi-line box, where Enter is part of
   * the text, a handler that does not prevent the default action of the key lets it type.
   */
  onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => void;

  /** Called when the input takes the focus. */
  onFocus?: (event: React.FocusEvent<HTMLElement>) => void;

  /** Called when the input loses the focus. */
  onBlur?: (event: React.FocusEvent<HTMLElement>) => void;
}

/**
 * The props of the part {@link PartProps.FieldValue}: the value of a form field that is not
 * editable, shown as text instead of an input - the text of a text or number field, the mask of a
 * password, the label of the chosen option, the written date, the label of a slider's value.
 *
 * <p>A single-line value stays on one line and ends with an ellipsis where it is cut off. A
 * {@link multiline} value keeps its line breaks; an empty line keeps its height. A value with an
 * {@link href} is a link opening the address in a window of its own, which gets no access to the
 * page that opened it. An empty text shows nothing: the value has none. A value the model gives a
 * color role ({@link colorRole}) is drawn as a pill of that role, where it has a text.</p>
 *
 * <p>The value sets no type of its own: it takes the type of the place it stands in - the input
 * area of a form field, the body of a table, the cells of a group row.</p>
 *
 * <p>Every implementation puts {@link id}, {@link className} and the passed-through attributes on
 * its outermost element.</p>
 */
export interface FieldValuePartProps extends PartAttributes {
  /** The ID of the drawn element, the control ID of the field. */
  id?: string;

  /** The value as text. */
  text: string;

  /**
   * The address the value points at; absent: the value is no link. Ignored for a multi-line
   * value.
   */
  href?: string;

  /** Whether the text is shown with its line breaks. */
  multiline?: boolean;

  /**
   * Whether a {@link multiline} value shows its text as it is written: in a font of fixed width,
   * each line without wrapping it, a line wider than the value scrolling horizontally. Ignored for
   * a single-line value.
   */
  preformatted?: boolean;

  /**
   * The icon shown ahead of the text, a theme image in its encoded form (see `ThemeIcon`), e.g. the
   * image of a chosen option; absent: none. Ignored for a multi-line value.
   */
  icon?: string;

  /**
   * Called when the value is pressed; the value is then a link that leads somewhere within the
   * application, e.g. to the place the chosen object is displayed at. Ignored for a value with an
   * {@link href} and for a multi-line value.
   */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /** The color role of the value; absent or `neutral`: plain. Ignored for a multi-line value. */
  colorRole?: ColorRole;

  /**
   * The action offered for a multi-line value, e.g. the part `CopyButton` copying it. It stands at
   * the top end of the value, beside the text, which keeps room for it, so that it never covers a
   * line. Absent: none. Ignored for a single-line value.
   */
  action?: React.ReactNode;

  /**
   * Classes added to the drawn element. They mark the part and carry no layout: the element
   * around the part places it.
   */
  className?: string;
}

/**
 * The color role of a value ({@link ChipPartProps chip}, {@link FieldValuePartProps read-only value}),
 * from a closed list: `neutral`; `brand`; the
 * meanings `error`, `warning`, `success` and `info`; and the categories without meaning
 * `category-1` to `category-8`. The role a model gives a value arrives by this name; how a role
 * looks is up to the implementation.
 */
export type ColorRole = 'neutral' | 'brand' | 'error' | 'warning' | 'success' | 'info'
  | 'category-1' | 'category-2' | 'category-3' | 'category-4'
  | 'category-5' | 'category-6' | 'category-7' | 'category-8';

/**
 * The props of the part {@link PartProps.Chip}: a compact, rounded value - a value the model gives
 * a color role, the chosen value of a field choosing several, an option that is switched on and off,
 * a named filter of a table, an attached file.
 *
 * <p>A chip comes in these forms, which combine:</p>
 * <ul>
 * <li>A chip with neither {@link pressed} nor {@link onClick} only shows its value.</li>
 * <li>A chip with {@link pressed} is a toggle: it is a button marked `aria-pressed`, drawn as
 *     switched on while pressed.</li>
 * <li>A chip with {@link onClick} and without {@link pressed} is a button running an action, e.g.
 *     downloading the file it stands for.</li>
 * <li>A chip with {@link onRemove} offers to remove its value, named by {@link removeLabel}.</li>
 * <li>A chip with a {@link handle} can be dragged by it to another place in its list, e.g. a chosen
 *     value of a field keeping its values in order; {@link dragSource} makes the chip the element
 *     that is dragged.</li>
 * </ul>
 *
 * <p>Every implementation puts {@link id}, {@link ref}, {@link className} and the passed-through
 * attributes on its outermost element.</p>
 */
export interface ChipPartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /** Receives the outermost drawn element. */
  ref?: PartRef<HTMLElement>;

  /**
   * The label: a text, or content such as the label of an option with the part matching a search
   * set off.
   */
  label: React.ReactNode;

  /**
   * A second, less prominent text after the label, e.g. the size of a file; absent: none.
   */
  detail?: string;

  /** The icon ahead of the label, a theme image in its encoded form (see `ThemeIcon`); absent: none. */
  icon?: string;

  /** The color role; absent: none, the chip is drawn in the plain look of its form. */
  colorRole?: ColorRole;

  /** The state of a toggle; absent for a chip that is no toggle. */
  pressed?: boolean;

  /**
   * The state of the value of the field a toggle belongs to, as the checks of the field judge it;
   * absent: valid.
   */
  validation?: FieldValidation;

  /** Whether the chip cannot be pressed and its value cannot be removed. */
  disabled?: boolean;

  /**
   * The tab index of what takes the focus in the chip - the chip, or its toggle and its remove
   * button; `-1` keeps them out of the tab order, e.g. the chosen values of a field whose own keys
   * move between and remove them. Absent: they are in the tab order.
   */
  tabIndex?: number;

  /** The tooltip; absent: none. */
  tooltip?: string;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout: the
   * element around the part places it.
   */
  className?: string;

  /**
   * The handle the chip is dragged by, drawn inside the chip ahead of its label: a part `Button`
   * drawn `icon-only`, `ghost` and `small`, carrying the keyboard reordering. Absent: none.
   */
  handle?: React.ReactNode;

  /**
   * Makes the outermost drawn element the element dragged when the chip is reordered by its
   * {@link handle}: its `draggable` and drag handlers, as the list reordering hands them out.
   */
  dragSource?: ReorderItemProps;

  /** Called when a toggle or a chip running an action is pressed. */
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;

  /**
   * Called when the value of the chip is to be removed: by a press of the remove button, or, where
   * the implementation offers it, by a key. Absent: the value cannot be removed.
   */
  onRemove?: (event: React.SyntheticEvent<HTMLElement>) => void;

  /**
   * The accessible name and tooltip of the means removing the value, e.g. "Remove Alpha"; required
   * with {@link onRemove}.
   */
  removeLabel?: string;
}

/**
 * The props of the part {@link PartProps.Badge}: a small marker carrying a count or a short status
 * beside an icon or a label, e.g. the number of new entries behind an item of the navigation.
 *
 * <p>A badge carries digits or an abbreviation only; a text belongs in a {@link ChipPartProps chip}.
 * Where it stands - beside a label or over the corner of an icon - is up to the element around
 * it, e.g. an element of its own over the corner of the icon. Every implementation puts
 * {@link id}, {@link className} and the passed-through attributes on its outermost element.</p>
 */
export interface BadgePartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /** What the badge shows: a count, or a short status. */
  content: string;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout: the
   * element around the part places it.
   */
  className?: string;
}

/**
 * The direction of a {@link DividerPartProps divider}:
 *
 * <ul>
 * <li>`horizontal`: a line across, between entries stacked one above the other - the sections of a
 *     menu, the items of a navigation.</li>
 * <li>`vertical`: a line standing in a row and stretching to its height - between the groups of a
 *     toolbar.</li>
 * </ul>
 */
export type DividerOrientation = 'horizontal' | 'vertical';

/**
 * The props of the part {@link PartProps.Divider}: a thin line separating the content before it from
 * the content after it.
 *
 * <p>A divider only separates; it carries no content and takes no focus. Every implementation draws a
 * separator (an `hr`, or an element of the role `separator`), marks a vertical one
 * `aria-orientation="vertical"` and puts {@link className} and the passed-through attributes on it.
 * The space around the line is up to the element around it, which sets it through the
 * {@link className}.</p>
 */
export interface DividerPartProps extends PartAttributes {
  /** The direction of the line; absent: `horizontal`. */
  orientation?: DividerOrientation;

  /**
   * Classes added to the drawn element. They mark the part and carry no layout: the element
   * around the part places it.
   */
  className?: string;
}

/**
 * The props of the part {@link PartProps.ProgressBar}: a bar showing how much of a task is done,
 * with an optional label beside it - the progress of a job, the loading of the options of a field.
 *
 * <p>A bar with a {@link value} is determinate: it fills that share of its track. A bar without a
 * value is indeterminate: it shows that something is going on without telling how much of it is
 * done, and is marked `aria-busy`.</p>
 *
 * <p>Every implementation draws an element of the role `progressbar` with the share in percent as
 * `aria-valuenow` (absent for an indeterminate bar), and puts {@link id}, {@link className} and the
 * passed-through attributes on its outermost element. The bar states its value through
 * `aria-valuenow` alone. It is named by its {@link label}; where the label only shows the value - a
 * percentage -, the element saying what progresses names the bar ({@link labelledBy}), and the label
 * is hidden from assistive technology. A bar passed `aria-hidden`, inside an element that carries the
 * name of what loads itself, has no name of its own.</p>
 */
export interface ProgressBarPartProps extends PartAttributes {
  /** The ID of the drawn element. */
  id?: string;

  /**
   * The share of the task that is done, from 0 (nothing) to 1 (all); absent: the bar is
   * indeterminate.
   */
  value?: number;

  /** The text shown beside the bar, e.g. "Loading…" or "3 / 4"; absent: none. */
  label?: string;

  /**
   * The ID of the element naming the bar, e.g. the state of a job above it; the {@link label} then
   * only shows the value. Absent: the label names the bar.
   */
  labelledBy?: string;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout: the
   * element around the part places it.
   */
  className?: string;
}

/**
 * The size of a {@link SpinnerPartProps spinner}:
 *
 * <ul>
 * <li>`small`: the size of a small icon, standing in for an icon. It takes the color of the text
 *     around it.</li>
 * <li>`medium`: a busy indicator standing on its own, e.g. in place of a list being loaded.</li>
 * </ul>
 */
export type SpinnerSize = 'small' | 'medium';

/**
 * The props of the part {@link PartProps.Spinner}: a turning ring showing that something is going
 * on without telling how much of it is done.
 *
 * <p>A spinner with a {@link label} is a busy indicator of its own: every implementation draws it
 * as an element of the role `progressbar` named by the label. A spinner without a label is
 * decoration, hidden from assistive technology: the element around it states that it is busy, e.g.
 * by `aria-busy`. Every implementation puts {@link className} and the passed-through attributes on
 * its outermost element.</p>
 */
export interface SpinnerPartProps extends PartAttributes {
  /** The size; absent: `medium`. */
  size?: SpinnerSize;

  /** The accessible name, e.g. "Loading…"; absent: the spinner is decoration. */
  label?: string;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout: the
   * element around the part places it.
   */
  className?: string;
}

/** An entry of a {@link ListboxPartProps list}. */
export interface ListboxEntry {
  /** The ID of the drawn entry, which `aria-activedescendant` of the controlling element names. */
  id: string;

  /** The text the entry shows. */
  label: string;
}

/**
 * The props of the part {@link PartProps.Listbox}: a list of entries floating at an element, one of
 * which may be active, from which the user picks one - the suggestions under a text box.
 *
 * <p>The list is drawn while it is open: the control draws the part only then. It stands below its
 * {@link anchor}, at least as wide as the anchor, flipped above it where there is no room below,
 * outside the flow of the page (in a portal), and is an anchored overlay (see
 * `anchoredOverlayProps`), so that a dialog around the anchor does not take a press into it for a
 * press outside.</p>
 *
 * <p>The list never takes the focus: the focus stays in the element controlling it, which moves the
 * active entry with the keyboard and names it by `aria-activedescendant`. A press on an entry
 * therefore leaves the focus where it is, and picks the entry ({@link onPick}).</p>
 *
 * <p>Every implementation draws an element of the role `listbox` carrying {@link id},
 * {@link className} and the passed-through attributes (e.g. `aria-labelledby` naming the label of
 * the field), and each entry as an element of the role `option` with the {@link ListboxEntry.id}
 * of the entry, `aria-selected="true"` on the active entry and `aria-selected="false"` on every
 * other. The active entry is highlighted and scrolled into view.</p>
 */
export interface ListboxPartProps extends PartAttributes {
  /** The ID of the list, which `aria-controls` of the controlling element names. */
  id: string;

  /** The element the list stands at, e.g. the text box it offers suggestions for. */
  anchor: Element | null;

  /** The entries, in the order shown. */
  entries: readonly ListboxEntry[];

  /** The position of the active entry in {@link entries}; -1 or absent: none. */
  activeIndex?: number;

  /** Called with the position of the entry the user picks with the pointer. */
  onPick: (index: number) => void;

  /** Called with the position of the entry the pointer rests on, e.g. to make it the active one. */
  onActivate?: (index: number) => void;

  /** Called for a press outside the list and its {@link anchor}, which closes the list. */
  onDismiss?: () => void;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout.
   */
  className?: string;
}

/**
 * The props of the part {@link PartProps.CopyButton}: a button copying a text to the clipboard, e.g.
 * the action of a read-only value (see {@link FieldValuePartProps.action}).
 *
 * <p>Every implementation guarantees:</p>
 * <ul>
 * <li>The button is named by {@link label}, which also is its tooltip.</li>
 * <li>Pressing it copies {@link text} to the clipboard (see `useCopyToClipboard` of
 *     'tl-react-bridge'). After a successful copy, the button shows for a short while that it did,
 *     and {@link copiedLabel} is announced politely (`role="status"`). Where the clipboard is
 *     unavailable or refuses the text, nothing changes and no error surfaces.</li>
 * </ul>
 */
export interface CopyButtonPartProps extends PartAttributes {
  /** The text copied. */
  text: string;

  /** The name of the button, e.g. "Copy to clipboard". */
  label: string;

  /** What is announced after a successful copy, e.g. "Copied". */
  copiedLabel: string;

  /**
   * Classes added to the outermost drawn element. They mark the part and carry no layout.
   */
  className?: string;
}

/**
 * The parts of the React UI by name, each with its props. The name is what {@link registerPart},
 * {@link replacePart} and {@link usePart} take; the props are the contract every implementation of
 * the part fulfils.
 */
export interface PartProps {
  /** A button, or an entry of a menu: {@link ButtonPartProps}. */
  Button: ButtonPartProps;

  /** The button that opens a menu or another anchored surface: {@link MenuTriggerPartProps}. */
  MenuTrigger: MenuTriggerPartProps;

  /** The button that shows or hides content: {@link DisclosureTogglePartProps}. */
  DisclosureToggle: DisclosureTogglePartProps;

  /** A box that is ticked, or a switch: {@link CheckboxPartProps}. */
  Checkbox: CheckboxPartProps;

  /** A box text is typed into: {@link TextInputPartProps}. */
  TextInput: TextInputPartProps;

  /** The value of a field that is not editable: {@link FieldValuePartProps}. */
  FieldValue: FieldValuePartProps;

  /** A compact, rounded value, a toggle or a removable value: {@link ChipPartProps}. */
  Chip: ChipPartProps;

  /** A small marker carrying a count: {@link BadgePartProps}. */
  Badge: BadgePartProps;

  /** A thin line separating content: {@link DividerPartProps}. */
  Divider: DividerPartProps;

  /** A bar showing how much of a task is done: {@link ProgressBarPartProps}. */
  ProgressBar: ProgressBarPartProps;

  /** A turning ring showing that something is going on: {@link SpinnerPartProps}. */
  Spinner: SpinnerPartProps;

  /** A list of entries floating at an element, one to pick: {@link ListboxPartProps}. */
  Listbox: ListboxPartProps;

  /** A button copying a text to the clipboard: {@link CopyButtonPartProps}. */
  CopyButton: CopyButtonPartProps;
}

/** The name of a part, see {@link PartProps}. */
export type PartName = keyof PartProps;

/** A component drawing the part of the given name. */
export type PartComponent<K extends PartName> = ComponentType<PartProps[K]>;

/** Components registered with {@link registerPart}, by part name. */
const _parts = new Map<PartName, ComponentType<never>>();

/** Components registered with {@link replacePart}, by part name. Consulted before {@link _parts}. */
const _partReplacements = new Map<PartName, ComponentType<never>>();

/**
 * Registers the default component drawing the given part.
 *
 * <p>The controls bundle registers one for every part. Registering a part a second time overrides
 * the first registration and logs a warning. A component registered with {@link replacePart} for
 * the same part takes precedence.</p>
 */
export function registerPart<K extends PartName>(name: K, component: PartComponent<K>): void {
  if (_parts.has(name)) {
    console.warn('[TLReact] Part registered twice, the later registration wins:', name);
  }
  _parts.set(name, component as ComponentType<never>);
}

/**
 * Replaces the component drawing the given part.
 *
 * <p>This is the hook for a customer component library: an adapter module draws a part with a
 * component of the library, wherever a control draws that part. The replacement takes precedence
 * over the component registered with {@link registerPart}, independent of the order in which the
 * scripts are loaded. Replacing a part a second time overrides the first replacement and logs a
 * warning.</p>
 *
 * <p>A control looks up its parts each time it renders; an adapter module registers its
 * replacements when its script loads, before the page's controls are mounted.</p>
 */
export function replacePart<K extends PartName>(name: K, component: PartComponent<K>): void {
  if (_partReplacements.has(name)) {
    console.warn('[TLReact] Part replaced twice, the later replacement wins:', name);
  }
  _partReplacements.set(name, component as ComponentType<never>);
}

/**
 * The component drawing the given part: the one registered with {@link replacePart}, else the one
 * registered with {@link registerPart}.
 *
 * @throws Error if no component is registered for the part.
 */
export function usePart<K extends PartName>(name: K): PartComponent<K> {
  const component = _partReplacements.get(name) ?? _parts.get(name);
  if (component === undefined) {
    throw new Error(`[TLReact] No component registered for the part: ${name}`);
  }
  return component as unknown as PartComponent<K>;
}

/** The relation of a link opening its address in a window of its own to the page it is on. */
const REL_NEW_WINDOW = 'noopener noreferrer';

/** The browsing context of a window of its own. */
const TARGET_NEW_WINDOW = '_blank';

/**
 * The attributes of the anchor an implementation of {@link ButtonPartProps} draws for a link
 * ({@link ButtonPartProps.href}): the address - none while disabled, which is then marked
 * `aria-disabled` -, the target and, for a window of its own, the relation that keeps the opened
 * page from reaching the page it was opened from.
 */
export function buttonLinkAttributes(props: ButtonPartProps): {
  href?: string; target?: string; rel?: string; 'aria-disabled'?: boolean;
} {
  const { href, target, disabled } = props;
  return {
    href: disabled ? undefined : href,
    target,
    rel: target === TARGET_NEW_WINDOW ? REL_NEW_WINDOW : undefined,
    'aria-disabled': disabled ? true : undefined,
  };
}

/**
 * The attributes every implementation of {@link ButtonPartProps} sets on the element it draws:
 *
 * <ul>
 * <li>the label as `aria-label` for a button with an icon or one showing its icon alone;</li>
 * <li>outside a menu `aria-pressed` for an active button or a checked toggle;</li>
 * <li>inside a menu the role and roving tabindex of an entry ({@link menuItemProps}),
 *     `aria-current` for the active entry and `aria-checked` for a toggle (a checkbox entry, or a radio entry, {@link ButtonPartProps.radio});</li>
 * <li>the tooltip: the explicit one, else the label - always for a button showing its icon alone,
 *     otherwise only while the label is clipped.</li>
 * </ul>
 */
export function buttonPartAttributes(props: ButtonPartProps): Record<string, string | number | boolean | undefined> {
  const { label, image, display, appearance, active, checked, radio, tooltip } = props;
  const asMenuItem = appearance === 'menu-item';
  const iconOnly = display === 'icon-only';
  const result: Record<string, string | number | boolean | undefined> = {
    ...menuItemProps({ appearance, radio }, checked),
    // A toggle states its state either way; a button marked active only while it is.
    'aria-pressed': asMenuItem ? undefined : checked !== undefined ? checked : active ? true : undefined,
    'aria-current': asMenuItem && active ? 'true' : undefined,
    'aria-checked': asMenuItem && checked !== undefined ? checked : undefined,
    'aria-label': image || iconOnly ? label : undefined,
  };
  const tooltipText = tooltip ?? label;
  if (tooltipText) {
    result[TOOLTIP_ATTR] = `text:${tooltipText}`;
    if (!tooltip && !iconOnly) {
      result[TOOLTIP_WHEN_ATTR] = WHEN_TRUNCATED;
    }
  }
  return result;
}
