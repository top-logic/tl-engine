import { React, useTLState, useTLCommand, useI18N, anchoredOverlayProps, useCloseOnOutsidePress, usePopover, useMergeRefs, CMD_VALUE_CHANGED, rootClassName, createPortal, useFieldLabelProps, ThemeIcon, usePart, OptionValues } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson, ChipTone } from 'tl-react-bridge';
import {
  ARG_OPTION,
  CMD_GOTO,
  OptionContent,
} from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import { SEARCH_ICON } from './form/fieldIcons';
import { findTypeAheadMatch, isTypeAheadKey, useTypeAhead } from './typeAhead';

const { useState, useCallback, useRef, useEffect, useMemo } = React;

/** Icon of the button clearing the selection. */
const CLEAR_ICON = 'css:fa-solid fa-xmark';

// -- Sub-components --

/** How a chip takes part in reordering by drag and drop: the design system's data-tl-state word. */
type ChipDragState = 'dragging' | 'drop-before' | 'drop-after';

/** The icon of the handle a chip is dragged by. */
const DRAG_HANDLE_ICON = 'css:fa-solid fa-grip-vertical';

/** The attribute marking the chip of a chosen value, by which the focus finds a chip after a removal. */
const VALUE_CHIP_ATTR = 'data-tl-value-chip';

/**
 * What takes the focus of a chip: the chip itself where it is focusable, else its first button, e.g.
 * its remove button.
 */
const CHIP_FOCUS_TARGET = 'button:not([disabled]), [tabindex]';

/** Whether the given element is a button, e.g. the remove button of a chip or the clear button. */
function isButton(element: HTMLElement): boolean {
  return element.tagName === 'BUTTON' || element.getAttribute('role') === 'button';
}

/**
 * Renders a selected value of a multi-valued field as the part `Chip`, of the value's color role,
 * removable while the field is editable.
 *
 * <p>A chip that can be reordered stands in a drag source (tlDropdownSelect__chipDrag) holding the
 * drag handle before the chip; the chip carries the design system's state of the reordering in
 * `data-tl-state`.</p>
 */
function ValueChip({
  option,
  removable,
  onRemove,
  removeLabel,
  draggable,
  onDragStart,
  onDragOver,
  onDrop,
  onDragEnd,
  dragState,
}: {
  option: OptionDescriptor;
  removable: boolean;
  onRemove: (value: string) => void;
  removeLabel: string;
  draggable?: boolean;
  onDragStart?: (e: React.DragEvent) => void;
  onDragOver?: (e: React.DragEvent) => void;
  onDrop?: (e: React.DragEvent) => void;
  onDragEnd?: (e: React.DragEvent) => void;
  dragState?: ChipDragState;
}) {
  const Chip = usePart('Chip');
  const handleRemove = useCallback(
    (e: React.SyntheticEvent) => {
      e.stopPropagation();
      onRemove(option.value);
    },
    [onRemove, option.value]
  );

  const chip = (
    <Chip
      label={option.label}
      icon={option.image}
      tone={(option.colorRole || undefined) as ChipTone | undefined}
      onRemove={removable ? handleRemove : undefined}
      removeLabel={removeLabel}
      data-tl-state={dragState}
      {...{ [VALUE_CHIP_ATTR]: true }}
    />
  );
  if (!draggable) {
    return chip;
  }
  return (
    <span
      className="tlDropdownSelect__chipDrag"
      draggable
      onDragStart={onDragStart}
      onDragOver={onDragOver}
      onDrop={onDrop}
      onDragEnd={onDragEnd}
    >
      <span className="tl-select__drag-handle" aria-hidden="true">
        <ThemeIcon encoded={DRAG_HANDLE_ICON} className="tl-icon-sm" />
      </span>
      {chip}
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
    if (!searchTerm) return option.label;
    const idx = option.label.toLowerCase().indexOf(searchTerm.toLowerCase());
    if (idx < 0) return option.label;
    const matchClass = option.colorRole ? 'tl-type-label-strong' : 'tl-type-body-strong';
    return (
      <>
        {option.label.substring(0, idx)}
        <span className={matchClass}>{option.label.substring(idx, idx + searchTerm.length)}</span>
        {option.label.substring(idx + searchTerm.length)}
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
      <OptionContent option={option} label={labelContent} />
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

  // Drag-and-drop state for chip reordering
  const [dragIndex, setDragIndex] = useState<number | null>(null);
  const [dropTargetIndex, setDropTargetIndex] = useState<number | null>(null);
  const [dropPosition, setDropPosition] = useState<'before' | 'after' | null>(null);

  // Refs
  const containerRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Tracks the latest selection to avoid stale closures when the user
  // selects multiple options faster than the SSE round-trip.
  const valueRef = useRef(value);
  valueRef.current = value;

  // Index of the last removed chip, used to restore focus after SSE update.
  const removalIndexRef = useRef(-1);

  // Type-ahead in a list without a search field. A prefix typed before the options have arrived
  // waits for them in pendingPrefixRef.
  const typeAhead = useTypeAhead();
  const pendingPrefixRef = useRef<string | null>(null);

  // Derived: selected value IDs for fast lookup
  const selectedIds = useMemo(
    () => new Set(value.map((v) => v.value)),
    [value]
  );

  // Derived: filtered options (exclude selected, apply search)
  const filteredOptions = useMemo(() => {
    let opts = allOptions.filter((o) => !selectedIds.has(o.value));
    if (searchTerm) {
      const lower = searchTerm.toLowerCase();
      opts = opts.filter((o) => o.label.toLowerCase().includes(lower));
    }
    return opts;
  }, [allOptions, selectedIds, searchTerm]);

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

  // After a chip is removed, focus the next chip (or previous, or container), see CHIP_FOCUS_TARGET.
  useEffect(() => {
    if (removalIndexRef.current < 0) return;
    const idx = removalIndexRef.current;
    removalIndexRef.current = -1;

    const chips = containerRef.current?.querySelectorAll<HTMLElement>(`[${VALUE_CHIP_ATTR}]`);
    const chip = chips && chips.length > 0 ? chips[Math.min(idx, chips.length - 1)] : null;
    const target = chip && (chip.matches(CHIP_FOCUS_TARGET) ? chip : chip.querySelector<HTMLElement>(CHIP_FOCUS_TARGET));
    (target ?? containerRef.current)?.focus();
  }, [value]);

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
    (optionValue: string) => {
      removalIndexRef.current = valueRef.current.findIndex((v) => v.value === optionValue);
      const newValue = valueRef.current.filter((v) => v.value !== optionValue);
      valueRef.current = newValue;
      sendCommand(CMD_VALUE_CHANGED, { value: newValue.map((v) => v.value) });
    },
    [sendCommand]
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

  // -- Drag-and-drop handlers for chip reordering --

  const handleChipDragStart = useCallback(
    (index: number, e: React.DragEvent) => {
      setDragIndex(index);
      e.dataTransfer.effectAllowed = 'move';
      e.dataTransfer.setData('text/plain', String(index));
    },
    []
  );

  const handleChipDragOver = useCallback(
    (index: number, e: React.DragEvent) => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      if (dragIndex === null || dragIndex === index) {
        setDropTargetIndex(null);
        setDropPosition(null);
        return;
      }
      const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
      const midX = rect.left + rect.width / 2;
      const pos = e.clientX < midX ? 'before' : 'after';
      setDropTargetIndex(index);
      setDropPosition(pos);
    },
    [dragIndex]
  );

  const handleChipDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      if (dragIndex === null || dropTargetIndex === null || dropPosition === null) return;
      if (dragIndex === dropTargetIndex) return;

      const reordered = [...valueRef.current];
      const [moved] = reordered.splice(dragIndex, 1);
      let insertAt = dropTargetIndex;
      // Adjust insertion index since the item was removed first
      if (dragIndex < dropTargetIndex) {
        insertAt = dropPosition === 'before' ? insertAt - 1 : insertAt;
      } else {
        insertAt = dropPosition === 'before' ? insertAt : insertAt + 1;
      }
      reordered.splice(insertAt, 0, moved);

      valueRef.current = reordered;
      sendCommand(CMD_VALUE_CHANGED, { value: reordered.map((v) => v.value) });

      setDragIndex(null);
      setDropTargetIndex(null);
      setDropPosition(null);
    },
    [dragIndex, dropTargetIndex, dropPosition, sendCommand]
  );

  const handleChipDragEnd = useCallback(() => {
    setDragIndex(null);
    setDropTargetIndex(null);
    setDropPosition(null);
  }, []);

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
      {/* Search field - shown when options are loaded, unless the field asks for none */}
      {!noFilter && (optionsLoaded || loadError) && (
        <TextInput
          ref={searchRef}
          className="tl-select__search-group"
          icon={SEARCH_ICON}
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
      )}

      {/* Loading, error and empty states - a line of the popup, not an option of the list */}
      {!optionsLoaded && !loadError && (
        <div className="tl-select__status tl-type-body">
          <ProgressBar label={i18n['js.dropdownSelect.loading']} />
        </div>
      )}
      {loadError && (
        <div className="tl-select__status tl-type-body" role="alert">
          <Button
            label={i18n['js.dropdownSelect.error']}
            display="label-only"
            appearance="link"
            onClick={handleRetry}
          />
        </div>
      )}
      {optionsLoaded && filteredOptions.length === 0 && (
        <div className="tl-select__status tl-type-body">
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
        <span className="tl-select__values">
          {value.length === 0 ? (
            <span className="tl-select__placeholder">{emptyOptionLabel}</span>
          ) : !multiSelect ? (
            // A single value is shown as it is. A chip sets one entry off from the next and
            // carries the button removing just that one; with a single value there is nothing to
            // set it off from, and removing it is what the clear button beside the arrow does. A
            // value with a color role is a pill of that role, as it is in the list.
            <span className="tl-select__value">
              <OptionContent option={value[0]} labelClassName="tl-field-value__text" />
            </span>
          ) : (
            value.map((v, idx) => {
              // A chip is either the one dragged or the one dropped beside, never both.
              let dragState: ChipDragState | undefined;
              if (dragIndex === idx) {
                dragState = 'dragging';
              } else if (dropTargetIndex === idx && dropPosition === 'before') {
                dragState = 'drop-before';
              } else if (dropTargetIndex === idx && dropPosition === 'after') {
                dragState = 'drop-after';
              }
              return (
                <ValueChip
                  key={v.value}
                  option={v}
                  removable={!disabled}
                  onRemove={removeOption}
                  removeLabel={removeChipLabel(v.label)}
                  draggable={dragEnabled}
                  onDragStart={dragEnabled ? (e) => handleChipDragStart(idx, e) : undefined}
                  onDragOver={dragEnabled ? (e) => handleChipDragOver(idx, e) : undefined}
                  onDrop={dragEnabled ? handleChipDrop : undefined}
                  onDragEnd={dragEnabled ? handleChipDragEnd : undefined}
                  dragState={dragEnabled ? dragState : undefined}
                />
              );
            })
          )}
        </span>
        <span className="tl-select__controls">
          {showClearButton && (
            <Button
              label={i18n['js.dropdownSelect.clear']}
              image={CLEAR_ICON}
              display="icon-only"
              appearance="ghost"
              small
              onClick={clearAll}
            />
          )}
          <span className="tl-select__arrow" aria-hidden="true">
            <ThemeIcon
              encoded={isOpen ? 'css:fa-solid fa-chevron-up' : 'css:fa-solid fa-chevron-down'}
              className="tl-icon-sm"
            />
          </span>
        </span>
      </div>

      {dropdownContent && createPortal(dropdownContent, document.body)}
    </>
  );
};

export default TLDropdownSelect;
