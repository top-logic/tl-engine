import { React, useTLState, useTLCommand, useI18N, anchoredOverlayProps, useCloseOnOutsidePress, usePopover, useMergeRefs, CMD_VALUE_CHANGED, rootClassName, tooltipProps, createPortal, useFieldLabelProps, ThemeIcon, useListReorder, reorderState, filterOptions, useFocusAfterRemove, ICON_HANDLE, ICON_REMOVE, ICON_CLEAR, ICON_SEARCH, ICON_LIST_OPEN, ICON_LIST_CLOSED } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson, ReorderItemProps, ReorderHandleProps } from 'tl-react-bridge';
import {
  ARG_OPTION,
  CMD_GOTO,
  OptionImage,
  OptionLabel,
  ReadonlyValues,
  optionRoleAttr,
} from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { buttonClassName } from './button/ButtonDefaults';
import { ProgressBar } from './TLProgress';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { findTypeAheadMatch, isTypeAheadKey, useTypeAhead } from './typeAhead';

const { useState, useCallback, useRef, useEffect, useMemo } = React;

// -- Sub-components --

/**
 * Renders a selected value of a multi-valued field as a chip: a pill of the value's color role
 * (neutral without one) holding the drag handle, the label and the button removing the value.
 */
function Chip({
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
  const handleRemove = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      onRemove(option.value, e.currentTarget);
    },
    [onRemove, option.value]
  );

  return (
    <span
      className="tl-pill tl-type-label tl-select__chip"
      data-tl-role={optionRoleAttr(option)}
      data-tl-state={dragState}
      {...itemProps}
    >
      {reorderable && (
        <button
          type="button"
          className={buttonClassName({ appearance: 'ghost', small: true, icon: true, extra: 'tl-select__drag-handle' })}
          onClick={(e) => e.stopPropagation()}
          {...tooltipProps(handleProps?.['aria-label'])}
          {...handleProps}
        >
          <ThemeIcon encoded={ICON_HANDLE} className="tl-button__icon tl-icon-sm" />
        </button>
      )}
      <OptionImage image={option.image} inPill />
      <span className="tl-pill__label">{option.label}</span>
      {removable && (
        <button
          type="button"
          className={buttonClassName({ appearance: 'ghost', small: true, icon: true })}
          onClick={handleRemove}
          aria-label={removeLabel}
          {...tooltipProps(removeLabel)}
        >
          <ThemeIcon encoded={ICON_REMOVE} className="tl-button__icon tl-icon-sm" />
        </button>
      )}
    </span>
  );
}

/**
 * Renders a single option row in the dropdown, with match highlighting.
 *
 * <p>The list only holds options that are not chosen, so every row is `aria-selected="false"`; the
 * keyboard position is `data-tl-state="highlighted"`, mirrored in the search field's
 * `aria-activedescendant`. The match takes the strong variant of the type style it stands in: the
 * label style inside a pill, the body style otherwise.</p>
 */
function OptionRow({
  option,
  highlighted,
  searchTerm,
  onSelect,
  onMouseEnter,
  id,
}: {
  option: OptionDescriptor;
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
      aria-selected={false}
      data-tl-state={highlighted ? 'highlighted' : undefined}
      className="tl-select__option"
      onClick={handleClick}
      onMouseEnter={onMouseEnter}
    >
      <OptionLabel option={option} label={labelContent} />
    </div>
  );
}

// -- Main component --

/**
 * A select field whose options open in a searchable list below it.
 *
 * Design system: the field is `tl-field tl-select` with `role="combobox"`, its state carried as
 * attributes (see fieldStateAttrs), open as `aria-expanded`, switched off as `aria-disabled`. A
 * chosen value with a color role is a pill of that role; the values of a multi-valued field are
 * chips. The list floats in `tl-select__popup`, positioned from the field, its layer the design
 * system's. The focus stays on the field or the search field; the keyboard highlight is
 * `data-tl-state="highlighted"` on the option and `aria-activedescendant` on the search field. A
 * read-only field shows its values in `tl-select__values`. A disabled field renders the field as
 * an inactive one (`aria-disabled`, out of the tab order) that opens no list and offers neither the
 * clear button nor the removal and reordering of chips (see showsValueOnly).
 *
 * Where the server asks for no filter (`noFilter`), the open list has no search field: the focus
 * stays on the field, which then carries the `aria-activedescendant`, and the list is operated by
 * the keyboard alone. Besides the arrow keys, Enter and Escape, typing the beginning of a label moves
 * to the option it starts (type-ahead, see useTypeAhead); on the closed field, typing opens the list
 * and moves there.
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
  const noFilter = state.noFilter === true;

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
  });

  const nothingFoundLabel = i18n['js.dropdownSelect.nothingFound'];

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

  // Derived: the options not chosen, narrowed by the search (filterOptions, shared with the column
  // selection).
  const filteredOptions = useMemo(
    () => filterOptions(allOptions.filter((o) => !selectedIds.has(o.value)), searchTerm),
    [allOptions, selectedIds, searchTerm]
  );

  // Auto-highlight single result when filtering, reset otherwise.
  useEffect(() => {
    if (searchTerm && filteredOptions.length === 1) {
      setHighlightedIndex(0);
    } else {
      setHighlightedIndex(-1);
    }
  }, [filteredOptions.length, searchTerm]);

  // A prefix typed while the options were loading moves to its option once they are there.
  useEffect(() => {
    const prefix = pendingPrefixRef.current;
    if (prefix === null || !isOpen || !optionsLoaded) return;
    pendingPrefixRef.current = null;
    const match = findTypeAheadMatch(filteredOptions.map((o) => o.label), prefix, -1);
    if (match >= 0) setHighlightedIndex(match);
  }, [isOpen, optionsLoaded, filteredOptions]);

  // Focus search input when dropdown is open and options are loaded.
  // Re-runs on value changes to restore focus after SSE state updates.
  useEffect(() => {
    if (isOpen && optionsLoaded && searchRef.current) {
      searchRef.current.focus();
    }
  }, [isOpen, optionsLoaded, value]);

  // A chip vanishes with its focused remove button once the server has dropped its value: the focus
  // goes to the neighbour's remove button (same place, else the one before), without any to the field.
  const noteRemoval = useFocusAfterRemove(
    value,
    () => containerRef.current?.querySelectorAll<HTMLElement>('.tl-select__chip > .tl-button:not(.tl-select__drag-handle)'),
    () => containerRef.current,
  );

  // Close the dropdown on a press outside the control and its (portalled) list.
  useCloseOnOutsidePress(isOpen, [containerRef, dropdownRef], () => {
    setIsOpen(false);
    setSearchTerm('');
  });

  // The popup is placed at the field by the one popover placement (flipped, shifted, following
  // scroll and resize); its width is the field's.
  const { setFloating, style: popoverStyle } = usePopover({ open: isOpen, anchor: containerRef.current });
  const popupRef = useMergeRefs<HTMLDivElement>([dropdownRef, setFloating]);

  // -- Handlers --

  const openDropdown = useCallback(async () => {
    if (disabled || !editable) return;
    setIsOpen(true);
    setSearchTerm('');
    setHighlightedIndex(-1);
    setLoadError(false);

    if (!optionsLoaded) {
      try {
        await sendCommand('loadOptions');
      } catch {
        setLoadError(true);
      }
    }
  }, [disabled, editable, optionsLoaded, sendCommand]);

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
        closeDropdown();
      } else {
        openDropdown();
      }
    },
    [isOpen, openDropdown, closeDropdown]
  );

  const clearAll = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      sendCommand(CMD_VALUE_CHANGED, { value: [] });
      closeDropdown();
    },
    [sendCommand, closeDropdown]
  );

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  }, []);

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
      if (noFilter && isTypeAheadKey(e, typeAhead.typing()) && (e.target as HTMLElement).tagName !== 'BUTTON') {
        e.preventDefault();
        e.stopPropagation();
        if (!disabled && editable) typeAheadKey(e.key);
        return;
      }

      if (!isOpen) {
        if (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'Enter' || e.key === ' ') {
          // Let Enter/Space activate a focused child button (chip remove, clear).
          if ((e.target as HTMLElement).tagName === 'BUTTON') return;
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
            prev < filteredOptions.length - 1 ? prev + 1 : 0
          );
          break;
        case 'ArrowUp':
          e.preventDefault();
          e.stopPropagation();
          setHighlightedIndex((prev) =>
            prev > 0 ? prev - 1 : filteredOptions.length - 1
          );
          break;
        case 'Enter':
          e.preventDefault();
          e.stopPropagation();
          if (highlightedIndex >= 0 && highlightedIndex < filteredOptions.length) {
            selectOption(filteredOptions[highlightedIndex].value);
          }
          break;
        case 'Escape':
          e.preventDefault();
          e.stopPropagation();
          closeDropdown();
          break;
        case 'Tab':
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
      highlightedIndex,
      selectOption,
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
    return <ReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
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
      {/* Search field - shown when options are loaded, unless the field asks for none */}
      {!noFilter && (optionsLoaded || loadError) && (
        <span className="tl-field-group tl-select__search-group">
          <span className="tl-field-group__icon" aria-hidden="true">
            <ThemeIcon encoded={ICON_SEARCH} className="tl-icon-sm" />
          </span>
          <input
            ref={searchRef}
            type="text"
            className="tl-field tl-select__search tl-type-body"
            value={searchTerm}
            onChange={handleSearchChange}
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
        </span>
      )}

      {/* Loading, error and empty states - a line of the popup, not an option of the list */}
      {!optionsLoaded && !loadError && (
        <div className="tl-status tl-type-body">
          <ProgressBar fraction={null} label={i18n['js.dropdownSelect.loading']} />
        </div>
      )}
      {loadError && (
        <div className="tl-status tl-type-body" role="alert">
          <button type="button" className="tl-button tl-button--link tl-type-body" onClick={handleRetry}>
            {i18n['js.dropdownSelect.error']}
          </button>
        </div>
      )}
      {optionsLoaded && filteredOptions.length === 0 && (
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
              highlighted={idx === highlightedIndex}
              searchTerm={searchTerm}
              onSelect={selectOption}
              onMouseEnter={() => setHighlightedIndex(idx)}
            />
          ))}
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
              <Chip
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
            <button
              type="button"
              className={buttonClassName({ appearance: 'ghost', small: true, icon: true })}
              onClick={clearAll}
              aria-label={i18n['js.dropdownSelect.clear']}
              {...tooltipProps(i18n['js.dropdownSelect.clear'])}
            >
              <ThemeIcon encoded={ICON_CLEAR} className="tl-button__icon tl-icon-sm" />
            </button>
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
