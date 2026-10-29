import { React, useTLState, useTLCommand, useI18N, anchoredOverlayProps, useCloseOnOutsidePress, usePopover, useMergeRefs, CMD_VALUE_CHANGED, rootClassName, createPortal, useFieldLabelProps, ThemeIcon, useListReorder, reorderState, filterOptions, labelMatches, useFocusAfterRemove, usePart, OptionValues, ICON_HANDLE, ICON_CLEAR, ICON_SEARCH, ICON_LIST_OPEN, ICON_LIST_CLOSED, ICON_CONFIRM } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson, ReorderItemProps, ReorderHandleProps, ColorRole } from 'tl-react-bridge';
import {
  ARG_OPTION,
  ARG_TEXT,
  CMD_ENTER_TEXT,
  CMD_GOTO,
  OptionLabel,
} from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { chipFocusTargets } from './chip/chipFocus';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { findTypeAheadMatch, isTypeAheadKey, useTypeAhead } from './typeAhead';

const { useState, useCallback, useRef, useEffect, useMemo } = React;

// -- Sub-components --

/** The attribute marking the chip of a chosen value, by which the focus finds a chip after a removal. */
const VALUE_CHIP_ATTR = 'data-tl-value-chip';

/** Whether the given element is a button, e.g. the remove button of a chip or the clear button. */
function isButton(element: HTMLElement): boolean {
  return element.tagName === 'BUTTON' || element.getAttribute('role') === 'button';
}

/**
 * Renders a selected value of a multi-valued field as the part `Chip` of the value's color role
 * (neutral without one), holding the drag handle - the part `Button` - and removable while the
 * field is editable. The chip is the element that is dragged and carries the design system's state
 * of the reordering in `data-tl-state`.
 */
function ValueChip({
  option,
  removable,
  onRemove,
  removeLabel,
  reorderable,
  itemProps,
  handleProps,
  dragState,
}: {
  option: OptionDescriptor;
  removable: boolean;
  onRemove: (value: string, removed: Element) => void;
  removeLabel: string;
  reorderable: boolean;
  itemProps?: ReorderItemProps;
  handleProps?: ReorderHandleProps;
  dragState?: string;
}) {
  const Chip = usePart('Chip');
  const Button = usePart('Button');
  const handleRemove = useCallback(
    (e: React.SyntheticEvent<HTMLElement>) => {
      e.stopPropagation();
      onRemove(option.value, e.currentTarget);
    },
    [onRemove, option.value]
  );

  return (
    <Chip
      label={option.label}
      icon={option.image}
      colorRole={(option.colorRole || undefined) as ColorRole | undefined}
      onRemove={removable ? handleRemove : undefined}
      removeLabel={removeLabel}
      handle={reorderable && (
        <Button
          label={handleProps?.['aria-label']}
          image={ICON_HANDLE}
          display="icon-only"
          appearance="ghost"
          small
          className="tl-select__drag-handle"
          onClick={(e) => e.stopPropagation()}
          {...handleProps}
        />
      )}
      dragSource={itemProps}
      data-tl-state={dragState}
      {...{ [VALUE_CHIP_ATTR]: true }}
    />
  );
}

/**
 * Renders a single option row in the dropdown, with match highlighting.
 *
 * <p>The chosen option of a single select is `aria-selected="true"` and carries a check mark at
 * the end of the row; all other rows are `aria-selected="false"`. The keyboard position is
 * independent of that: `data-tl-state="highlighted"`, mirrored in the search field's
 * `aria-activedescendant`. The match takes the strong variant of the type style it stands in: the
 * label style inside a pill, the body style otherwise.</p>
 */
function OptionRow({
  option,
  selected,
  highlighted,
  searchTerm,
  onSelect,
  onMouseEnter,
  id,
}: {
  option: OptionDescriptor;
  selected: boolean;
  highlighted: boolean;
  searchTerm: string;
  onSelect: (value: string) => void;
  onMouseEnter: () => void;
  id: string;
}) {
  const handleClick = useCallback(() => onSelect(option.value), [onSelect, option.value]);

  const labelContent = useMemo(() => {
    // The term the filter matches (filterOptions trims it): blanks around it mark nothing.
    const term = searchTerm.trim();
    if (!term) return option.label;
    const idx = option.label.toLowerCase().indexOf(term.toLowerCase());
    if (idx < 0) return option.label;
    const matchClass = option.colorRole ? 'tl-type-label-strong' : 'tl-type-body-strong';
    return (
      <>
        {option.label.substring(0, idx)}
        <span className={matchClass}>{option.label.substring(idx, idx + term.length)}</span>
        {option.label.substring(idx + term.length)}
      </>
    );
  }, [option.label, option.colorRole, searchTerm]);

  return (
    <div
      id={id}
      role="option"
      aria-selected={selected}
      data-tl-state={highlighted ? 'highlighted' : undefined}
      className="tl-select__option"
      onClick={handleClick}
      onMouseEnter={onMouseEnter}
    >
      <OptionLabel option={option} label={labelContent} />
      {selected && <ThemeIcon encoded={ICON_CONFIRM} className="tl-icon-sm tl-select__option-check" />}
    </div>
  );
}

/**
 * Renders the entry of the list taking the typed text as the value of a field accepting free entry,
 * labelled with the text ("Use "xyz""). It is an option of the list like the others: reachable with
 * the arrow keys, highlighted as they are, and activated by a click or Enter.
 */
function EntryRow({
  label,
  highlighted,
  onActivate,
  onMouseEnter,
  id,
}: {
  label: string;
  highlighted: boolean;
  onActivate: () => void;
  onMouseEnter: () => void;
  id: string;
}) {
  return (
    <div
      id={id}
      role="option"
      aria-selected={false}
      data-tl-state={highlighted ? 'highlighted' : undefined}
      className="tl-select__option"
      onClick={onActivate}
      onMouseEnter={onMouseEnter}
    >
      <span>{label}</span>
    </div>
  );
}

// -- Main component --

/**
 * A select field whose options open in a searchable list below it.
 *
 * Design system: the field is `tl-field tl-select` with `role="combobox"`, its state carried as
 * attributes (see fieldStateAttrs), open as `aria-expanded`, switched off as `aria-disabled`. A
 * chosen value with a color role is the part `Chip` of that role; the values of a multi-valued field
 * are chips (part `Chip`), removable while the field is editable. The list floats in `tl-select__popup`, positioned from the field, its layer the design
 * system's. The focus stays on the field or the search field; the keyboard highlight is
 * `data-tl-state="highlighted"` on the option and `aria-activedescendant` on the search field. A
 * read-only field shows its values in `tl-select__values`. A disabled field renders the field as
 * an inactive one (`aria-disabled`, out of the tab order) that opens no list and offers neither the
 * clear button nor the removal and reordering of chips (see showsValueOnly).
 *
 * The list of a single select holds all options, the chosen one checked; opening it puts the
 * keyboard highlight on the chosen option and scrolls it into view, like a native select, and
 * choosing the chosen option again just closes the list. The list of a multi select holds only the
 * options not chosen, the chosen ones being chips in the field.
 *
 * Where the server asks for no filter (`noFilter`), the open list has no search field: the focus
 * stays on the field, which then carries the `aria-activedescendant`, and the list is operated by
 * the keyboard alone. Besides the arrow keys, Enter and Escape, typing the beginning of a label moves
 * to the option it starts (type-ahead, see useTypeAhead); on the closed field, typing opens the list
 * and moves there.
 *
 * A field accepting free entry (`freeEntry`) also takes a text of the user's own, sent with
 * {@link CMD_ENTER_TEXT}; it always has the search field, whatever `noFilter` says, since the text
 * is typed there. While the trimmed text names no option and no chosen value (see labelMatches), the
 * list ends with an entry taking the text ("Use "xyz""), reached with the arrow keys like an option.
 * While searching, the highlight starts on the option the text names exactly, if any, and on no
 * option otherwise - a single option merely containing the text is not highlighted, so that Enter
 * does not replace the text by it. Enter takes the highlighted option, or the text if no option is
 * highlighted. Closing the list by leaving it - a press outside, Tab, a click on the field - takes a
 * non-blank text as well; Escape closes the list and drops the text. Once the text is taken, the
 * search is cleared; a single-valued field closes its list, a multi-valued one keeps it open for
 * the next entry. A text the server refuses (e.g. one that is no valid value) is reported by the
 * server like any failed command, in the window's snackbar.
 */
const TLDropdownSelect: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DropdownSelectStateJson>>();
  const labelProps = useFieldLabelProps(controlId, controlId);
  const sendCommand = useTLCommand();

  // Server state
  const value = (state.value ?? []) as OptionDescriptor[];
  const multiSelect = state.multiSelect === true;
  const customOrder = state.customOrder === true;
  const mandatory = state.mandatory === true;
  const disabled = state.disabled === true;
  const editable = state.editable !== false;
  const optionsLoaded = state.optionsLoaded === true;
  const allOptions = (state.options ?? []) as OptionDescriptor[];
  const emptyOptionLabel = state.emptyOptionLabel ?? '';
  const freeEntry = state.freeEntry === true;
  // The text of a free entry is typed into the search field, so free entry always has one.
  const noFilter = state.noFilter === true && !freeEntry;

  // Drag-and-drop is enabled only for custom-order multi-select editable fields
  const dragEnabled = customOrder && multiSelect && !disabled && editable;

  // I18N for client-side labels
  const i18n = useI18N({
    'js.dropdownSelect.nothingFound': 'Nothing found',
    'js.dropdownSelect.filterPlaceholder': 'Filter\u2026',
    'js.dropdownSelect.clear': 'Clear selection',
    'js.dropdownSelect.removeChip': 'Remove {0}',
    'js.dropdownSelect.loading': 'Loading\u2026',
    'js.dropdownSelect.error': 'Failed to load options. Retry',
    'js.dropdownSelect.useText': 'Use "{0}"',
  });

  const nothingFoundLabel = i18n['js.dropdownSelect.nothingFound'];
  const Button = usePart('Button');
  const TextInput = usePart('TextInput');
  const ProgressBar = usePart('ProgressBar');

  /** Format remove-chip label by replacing {0} with the option label. */
  const removeChipLabel = useCallback(
    (label: string) => i18n['js.dropdownSelect.removeChip'].replace('{0}', label),
    [i18n]
  );

  // Local state
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const [loadError, setLoadError] = useState(false);

  // Refs
  const containerRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Tracks the latest selection to avoid stale closures when the user
  // selects multiple options faster than the SSE round-trip.
  const valueRef = useRef(value);
  valueRef.current = value;

  // Type-ahead in a list without a search field. A prefix typed before the options have arrived
  // waits for them in pendingPrefixRef.
  const typeAhead = useTypeAhead();
  const pendingPrefixRef = useRef<string | null>(null);

  // Derived: selected value IDs for fast lookup
  const selectedIds = useMemo(
    () => new Set(value.map((v) => v.value)),
    [value]
  );

  // Derived: the options of the list, narrowed by the search (filterOptions, shared with the column
  // selection). A single select lists all options, a multi select only those not chosen.
  const filteredOptions = useMemo(
    () => filterOptions(multiSelect ? allOptions.filter((o) => !selectedIds.has(o.value)) : allOptions, searchTerm),
    [allOptions, multiSelect, selectedIds, searchTerm]
  );

  // Derived: the text a free entry takes, and whether the list offers the entry taking it - not for a
  // text naming an option or a chosen value, which the server would choose instead.
  const entryText = freeEntry ? searchTerm.trim() : '';
  const offerEntry = entryText !== '' && optionsLoaded
    && !allOptions.some((o) => labelMatches(o, entryText))
    && !value.some((v) => labelMatches(v, entryText));
  // The entry taking the text follows the options; the keyboard moves through both.
  const entryIndex = filteredOptions.length;
  const rowCount = filteredOptions.length + (offerEntry ? 1 : 0);

  // Derived: the position of the chosen option of a single select in the list, -1 if not listed.
  const chosenIndex = useMemo(
    () => (multiSelect ? -1 : filteredOptions.findIndex((o) => selectedIds.has(o.value))),
    [multiSelect, filteredOptions, selectedIds]
  );

  // The highlight a list starts with: while searching, the single result if there is exactly one -
  // with free entry only the option the text names exactly, so that Enter takes a text that merely
  // occurs in a label as the text; otherwise the chosen option of a single select (none in a multi
  // select). Set on opening, once the options are there, and whenever the search changes.
  const exactIndex = useMemo(
    () => (freeEntry ? filteredOptions.findIndex((o) => labelMatches(o, searchTerm)) : -1),
    [freeEntry, filteredOptions, searchTerm]
  );
  useEffect(() => {
    if (searchTerm) {
      if (freeEntry) {
        setHighlightedIndex(exactIndex);
      } else {
        setHighlightedIndex(filteredOptions.length === 1 ? 0 : -1);
      }
    } else {
      setHighlightedIndex(chosenIndex);
    }
  }, [isOpen, filteredOptions.length, searchTerm, chosenIndex, freeEntry, exactIndex]);

  // A prefix typed while the options were loading moves to its option once they are there.
  useEffect(() => {
    const prefix = pendingPrefixRef.current;
    if (prefix === null || !isOpen || !optionsLoaded) return;
    pendingPrefixRef.current = null;
    const match = findTypeAheadMatch(filteredOptions.map((o) => o.label), prefix, -1);
    if (match >= 0) setHighlightedIndex(match);
  }, [isOpen, optionsLoaded, filteredOptions]);

  // Focus search input when dropdown is open and options are loaded (with free entry at once, its
  // search field being there while the options load).
  // Re-runs on value changes to restore focus after SSE state updates.
  useEffect(() => {
    if (isOpen && (optionsLoaded || freeEntry) && searchRef.current) {
      searchRef.current.focus();
    }
  }, [isOpen, optionsLoaded, freeEntry, value]);

  // A chip vanishes with its focused remove button once the server has dropped its value: the focus
  // goes to the neighbour (same place, else the one before), see chipFocusTargets; without any to the
  // field.
  const noteRemoval = useFocusAfterRemove(
    value,
    () => chipFocusTargets(containerRef.current, VALUE_CHIP_ATTR),
    () => containerRef.current,
  );

  /** Sends the given text as a free entry; a blank one is not sent. */
  const sendText = useCallback(
    (text: string) => {
      const entered = text.trim();
      if (entered === '') return;
      sendCommand(CMD_ENTER_TEXT, { [ARG_TEXT]: entered });
    },
    [sendCommand]
  );

  // Close the dropdown on a press outside the control and its (portalled) list, taking the text of a
  // free entry.
  useCloseOnOutsidePress(isOpen, [containerRef, dropdownRef], () => {
    if (freeEntry) sendText(searchTerm);
    setIsOpen(false);
    setSearchTerm('');
  });

  // The popup is placed at the field by the one popover placement (flipped, shifted, following
  // scroll and resize); its width is the field's.
  const { setFloating, style: popoverStyle } = usePopover({ open: isOpen, anchor: containerRef.current });
  const popupRef = useMergeRefs<HTMLDivElement>([dropdownRef, setFloating]);

  // -- Handlers --

  const openDropdown = useCallback(() => {
    if (disabled || !editable) return;
    setIsOpen(true);
    setSearchTerm('');
    setHighlightedIndex(-1);
    setLoadError(false);
  }, [disabled, editable]);

  // The open list asks for its options while it has none: on opening, and when the server drops
  // them while the list stands open (after a free entry, whose value the options then include).
  useEffect(() => {
    if (!isOpen || optionsLoaded) return;
    sendCommand('loadOptions').catch(() => setLoadError(true));
  }, [isOpen, optionsLoaded, sendCommand]);

  const closeDropdown = useCallback(() => {
    setIsOpen(false);
    setSearchTerm('');
    setHighlightedIndex(-1);
    containerRef.current?.focus();
  }, []);

  const selectOption = useCallback(
    (optionValue: string) => {
      let newValue: OptionDescriptor[];
      if (multiSelect) {
        const opt = allOptions.find((o) => o.value === optionValue);
        if (opt) {
          newValue = [...valueRef.current, opt];
        } else {
          return;
        }
      } else {
        if (valueRef.current.length === 1 && valueRef.current[0].value === optionValue) {
          // The chosen option again: nothing changes.
          closeDropdown();
          return;
        }
        const opt = allOptions.find((o) => o.value === optionValue);
        if (opt) {
          newValue = [opt];
        } else {
          return;
        }
      }

      valueRef.current = newValue;
      sendCommand(CMD_VALUE_CHANGED, { value: newValue.map((v) => v.value) });

      if (!multiSelect) {
        closeDropdown();
      } else {
        setSearchTerm('');
        setHighlightedIndex(-1);
      }
    },
    [multiSelect, allOptions, sendCommand, closeDropdown]
  );

  /**
   * Takes the typed text as a free entry: a single-valued field closes its list, a multi-valued one
   * keeps it open for the next entry, with the search cleared.
   */
  const takeText = useCallback(() => {
    sendText(searchTerm);
    if (!multiSelect) {
      closeDropdown();
    } else {
      setSearchTerm('');
      setHighlightedIndex(-1);
    }
  }, [sendText, searchTerm, multiSelect, closeDropdown]);

  const removeOption = useCallback(
    (optionValue: string, removed: Element | null = null) => {
      noteRemoval(valueRef.current.findIndex((v) => v.value === optionValue), removed);
      const newValue = valueRef.current.filter((v) => v.value !== optionValue);
      valueRef.current = newValue;
      sendCommand(CMD_VALUE_CHANGED, { value: newValue.map((v) => v.value) });
    },
    [sendCommand, noteRemoval]
  );

  // A click anywhere on the field toggles the list. Clicks inside the list bubble up here as well,
  // since React propagates events through the portal; only clicks on the field itself count.
  const handleFieldClick = useCallback(
    (e: React.MouseEvent) => {
      if (!containerRef.current?.contains(e.target as Node)) return;
      if (isOpen) {
        if (freeEntry) sendText(searchTerm);
        closeDropdown();
      } else {
        openDropdown();
      }
    },
    [isOpen, openDropdown, closeDropdown, freeEntry, sendText, searchTerm]
  );

  const clearAll = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      sendCommand(CMD_VALUE_CHANGED, { value: [] });
      closeDropdown();
    },
    [sendCommand, closeDropdown]
  );

  /** Leads to the place the given option is displayed at. */
  const goto = useCallback(
    (optionValue: string) => {
      sendCommand(CMD_GOTO, { [ARG_OPTION]: optionValue });
    },
    [sendCommand]
  );

  /**
   * Takes a typed character into the type-ahead prefix and moves to the option it starts, opening
   * the list first if it is closed.
   */
  const typeAheadKey = useCallback(
    (ch: string) => {
      const prefix = typeAhead.type(ch);
      if (!isOpen || !optionsLoaded) {
        pendingPrefixRef.current = prefix;
        if (!isOpen) openDropdown();
        return;
      }
      const match = findTypeAheadMatch(filteredOptions.map((o) => o.label), prefix, highlightedIndex);
      if (match >= 0) setHighlightedIndex(match);
    },
    [typeAhead, isOpen, optionsLoaded, openDropdown, filteredOptions, highlightedIndex]
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (noFilter && isTypeAheadKey(e, typeAhead.typing()) && !isButton(e.target as HTMLElement)) {
        e.preventDefault();
        e.stopPropagation();
        if (!disabled && editable) typeAheadKey(e.key);
        return;
      }

      if (!isOpen) {
        if (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'Enter' || e.key === ' ') {
          // Let Enter/Space activate a focused child button (chip remove, clear).
          if (isButton(e.target as HTMLElement)) return;
          e.preventDefault();
          e.stopPropagation();
          openDropdown();
        }
        return;
      }

      switch (e.key) {
        case 'ArrowDown':
          e.preventDefault();
          e.stopPropagation();
          setHighlightedIndex((prev) =>
            prev < rowCount - 1 ? prev + 1 : 0
          );
          break;
        case 'ArrowUp':
          e.preventDefault();
          e.stopPropagation();
          setHighlightedIndex((prev) =>
            prev > 0 ? prev - 1 : rowCount - 1
          );
          break;
        case 'Enter':
          e.preventDefault();
          e.stopPropagation();
          if (highlightedIndex >= 0 && highlightedIndex < filteredOptions.length) {
            selectOption(filteredOptions[highlightedIndex].value);
          } else if (entryText !== '') {
            // The entry taking the text, or no option highlighted.
            takeText();
          }
          break;
        case 'Escape':
          e.preventDefault();
          e.stopPropagation();
          closeDropdown();
          break;
        case 'Tab':
          if (freeEntry) sendText(searchTerm);
          closeDropdown();
          // Let Tab propagate naturally for focus management.
          break;
        case 'Backspace':
          if (searchTerm === '' && multiSelect && value.length > 0) {
            removeOption(value[value.length - 1].value);
          }
          break;
      }
    },
    [
      noFilter,
      typeAhead,
      disabled,
      editable,
      typeAheadKey,
      isOpen,
      openDropdown,
      closeDropdown,
      filteredOptions,
      rowCount,
      highlightedIndex,
      selectOption,
      entryText,
      takeText,
      freeEntry,
      sendText,
      searchTerm,
      multiSelect,
      value,
      removeOption,
    ]
  );

  const handleRetry = useCallback(
    async (e: React.MouseEvent) => {
      e.preventDefault();
      setLoadError(false);
      try {
        await sendCommand('loadOptions');
      } catch {
        setLoadError(true);
      }
    },
    [sendCommand]
  );

  // -- Reordering the chips: the common gesture (useListReorder), along the row of chips --

  const moveChip = useCallback(
    (from: number, to: number) => {
      const reordered = [...valueRef.current];
      const [moved] = reordered.splice(from, 1);
      reordered.splice(to, 0, moved);
      valueRef.current = reordered;
      sendCommand(CMD_VALUE_CHANGED, { value: reordered.map((v) => v.value) });
    },
    [sendCommand]
  );

  // The chips are keyed by value: after Alt + arrow the hook gives the focus back to the moved chip's
  // handle once the server's new order has arrived.
  const reorder = useListReorder({
    axis: 'horizontal',
    onMove: moveChip,
    enabled: dragEnabled,
    count: value.length,
    labelOf: (i) => value[i]?.label ?? '',
    keyOf: (i) => value[i]?.value ?? String(i),
  });

  // Scroll highlighted option into view
  useEffect(() => {
    if (highlightedIndex < 0 || !dropdownRef.current) return;
    const optionEl = dropdownRef.current.querySelector(
      `[id="${controlId}-opt-${highlightedIndex}"]`
    );
    if (optionEl) {
      optionEl.scrollIntoView({ block: 'nearest' });
    }
  }, [highlightedIndex, controlId]);

  // -- Read-only rendering: an empty selection renders nothing (the "empty option" label is an edit
  // affordance and would mislead in a read-only display) --

  if (showsValueOnly(state)) {
    return <OptionValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
  }

  // -- Editable or disabled rendering --

  const showClearButton = !mandatory && value.length > 0 && !disabled;
  const listboxId = `${controlId}-listbox`;

  // The position is usePopover's (see above); layer, surface and shadow are the design system's
  // (tl-popover), the inner layout is tl-select__popup's.
  const dropdownContent = isOpen ? (
    <div
      ref={popupRef}
      className="tl-popover tl-select__popup"
      style={{ ...popoverStyle, width: containerRef.current?.getBoundingClientRect().width }}
      {...anchoredOverlayProps}
    >
      {/* Search field - shown when options are loaded (with free entry at once), unless the field
          asks for none */}
      {!noFilter && (optionsLoaded || loadError || freeEntry) && (
        <div className="tl-select__search-group">
          <TextInput
            ref={searchRef}
            icon={ICON_SEARCH}
            value={searchTerm}
            onChange={setSearchTerm}
            onKeyDown={handleKeyDown}
            placeholder={i18n['js.dropdownSelect.filterPlaceholder']}
            aria-label={i18n['js.dropdownSelect.filterPlaceholder']}
            aria-activedescendant={
              highlightedIndex >= 0
                ? `${controlId}-opt-${highlightedIndex}`
                : undefined
            }
            aria-controls={listboxId}
          />
        </div>
      )}

      {/* Loading, error and empty states - a line of the popup, not an option of the list */}
      {!optionsLoaded && !loadError && (
        <div className="tl-status tl-type-body">
          <ProgressBar label={i18n['js.dropdownSelect.loading']} />
        </div>
      )}
      {loadError && (
        <div className="tl-status tl-type-body" role="alert">
          <Button
            label={i18n['js.dropdownSelect.error']}
            display="label-only"
            appearance="link"
            onClick={handleRetry}
          />
        </div>
      )}
      {optionsLoaded && filteredOptions.length === 0 && !offerEntry && (
        <div className="tl-status tl-type-body">
          {nothingFoundLabel}
        </div>
      )}

      {/* Option list, named by the field's label like the field itself */}
      <div
        id={listboxId}
        {...labelProps}
        role="listbox"
        className="tl-select__list tl-type-body"
      >
        {optionsLoaded &&
          filteredOptions.map((opt, idx) => (
            <OptionRow
              key={opt.value}
              id={`${controlId}-opt-${idx}`}
              option={opt}
              selected={!multiSelect && selectedIds.has(opt.value)}
              highlighted={idx === highlightedIndex}
              searchTerm={searchTerm}
              onSelect={selectOption}
              onMouseEnter={() => setHighlightedIndex(idx)}
            />
          ))}
        {offerEntry && (
          <EntryRow
            id={`${controlId}-opt-${entryIndex}`}
            label={i18n['js.dropdownSelect.useText'].replace('{0}', entryText)}
            highlighted={highlightedIndex === entryIndex}
            onActivate={takeText}
            onMouseEnter={() => setHighlightedIndex(entryIndex)}
          />
        )}
      </div>
    </div>
  ) : null;

  return (
    <>
      <div
        id={controlId}
        {...labelProps}
        ref={containerRef}
        className={rootClassName(state, 'tl-field tl-select tl-type-body')}
        role="combobox"
        {...fieldStateAttrs(state)}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        aria-controls={isOpen ? listboxId : undefined}
        aria-activedescendant={
          noFilter && isOpen && highlightedIndex >= 0 ? `${controlId}-opt-${highlightedIndex}` : undefined
        }
        aria-disabled={disabled || undefined}
        tabIndex={disabled ? -1 : 0}
        onClick={handleFieldClick}
        onKeyDown={handleKeyDown}
      >
        <span className="tl-select__values" data-tl-axis={multiSelect ? 'horizontal' : undefined} {...reorder.containerProps}>
          {value.length === 0 ? (
            <span className="tl-select__placeholder">{emptyOptionLabel}</span>
          ) : !multiSelect ? (
            // A single value is shown as it is. A chip sets one entry off from the next and
            // carries the button removing just that one; with a single value there is nothing to
            // set it off from, and removing it is what the clear button beside the arrow does. A
            // value with a color role is a pill of that role, as it is in the list, its text in
            // tl-pill__label; a value without one has its text in tl-field-value__text.
            <span className="tl-select__value">
              <OptionLabel option={value[0]} labelClassName="tl-field-value__text" />
            </span>
          ) : (
            value.map((v, idx) => (
              <ValueChip
                key={v.value}
                option={v}
                removable={!disabled}
                onRemove={removeOption}
                removeLabel={removeChipLabel(v.label)}
                reorderable={dragEnabled}
                itemProps={dragEnabled ? reorder.itemProps(idx) : undefined}
                handleProps={dragEnabled ? reorder.handleProps(idx) : undefined}
                dragState={dragEnabled ? reorderState(reorder.itemState(idx)) : undefined}
              />
            ))
          )}
        </span>
        <span className="tl-select__controls">
          {showClearButton && (
            <Button
              label={i18n['js.dropdownSelect.clear']}
              image={ICON_CLEAR}
              display="icon-only"
              appearance="ghost"
              small
              onClick={clearAll}
            />
          )}
          <span className="tl-select__arrow" aria-hidden="true">
            <ThemeIcon
              encoded={isOpen ? ICON_LIST_OPEN : ICON_LIST_CLOSED}
              className="tl-icon-sm"
            />
          </span>
        </span>
      </div>

      {dragEnabled && reorder.announcer}
      {dropdownContent && createPortal(dropdownContent, document.body)}
    </>
  );
};

export default TLDropdownSelect;
