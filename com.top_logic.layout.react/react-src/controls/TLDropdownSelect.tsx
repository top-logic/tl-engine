import { React, useTLState, useTLCommand, useI18N, anchoredOverlayProps, useCloseOnOutsidePress, CMD_VALUE_CHANGED, rootClassName, tooltipProps, createPortal, useFieldLabelProps, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, DropdownSelectStateJson } from 'tl-react-bridge';
import {
  ARG_OPTION,
  CMD_GOTO,
  OptionContent,
  OptionImage,
  ReadonlyValues,
} from './selectOptions';
import type { OptionDescriptor } from './selectOptions';
import { pillClassName } from './pill/TLPill';
import { ProgressBar } from './TLProgress';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';

const { useState, useCallback, useRef, useEffect, useMemo } = React;

// -- Sub-components --

/** How a chip takes part in reordering by drag and drop: the design system's data-tl-state word. */
type ChipDragState = 'dragging' | 'drop-before' | 'drop-after';

/**
 * Renders a selected value of a multi-valued field as a chip: a pill of the value's color role
 * (neutral without one) holding the drag handle, the label and the button removing the value.
 */
function Chip({
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
  const handleRemove = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      onRemove(option.value);
    },
    [onRemove, option.value]
  );

  return (
    <span
      className={pillClassName(option.colorRole, 'tl-type-label tl-select__chip')}
      data-tl-state={dragState}
      draggable={draggable || undefined}
      onDragStart={onDragStart}
      onDragOver={onDragOver}
      onDrop={onDrop}
      onDragEnd={onDragEnd}
    >
      {draggable && (
        <span className="tl-select__drag-handle" aria-hidden="true">
          <ThemeIcon encoded="css:fa-solid fa-grip-vertical" className="tl-icon-sm" />
        </span>
      )}
      <OptionImage image={option.image} />
      <span className="tl-pill__label">{option.label}</span>
      {removable && (
        <button
          type="button"
          className="tl-select__chip-remove"
          onClick={handleRemove}
          aria-label={removeLabel}
          {...tooltipProps(removeLabel)}
        >
          <ThemeIcon encoded="css:fa-solid fa-xmark" className="tl-icon-sm" />
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
 * chosen value with a color role is a pill of that role; the values of a multi-valued field are
 * chips. The list floats in `tl-select__popup`, positioned from the field, its layer the design
 * system's. The focus stays on the field or the search field; the keyboard highlight is
 * `data-tl-state="highlighted"` on the option and `aria-activedescendant` on the search field. A
 * read-only field shows its values in `tl-select__values`. A disabled field renders the field as
 * an inactive one (`aria-disabled`, out of the tab order) that opens no list and offers neither the
 * clear button nor the removal and reordering of chips (see showsValueOnly).
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
  const [dropdownStyle, setDropdownStyle] = useState<React.CSSProperties>({});

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

  // Focus search input when dropdown is open and options are loaded.
  // Re-runs on value changes to restore focus after SSE state updates.
  useEffect(() => {
    if (isOpen && optionsLoaded && searchRef.current) {
      searchRef.current.focus();
    }
  }, [isOpen, optionsLoaded, value]);

  // After a chip is removed, focus the next remove button (or previous, or container).
  useEffect(() => {
    if (removalIndexRef.current < 0) return;
    const idx = removalIndexRef.current;
    removalIndexRef.current = -1;

    const buttons = containerRef.current?.querySelectorAll<HTMLElement>(
      '.tl-select__chip-remove'
    );
    if (buttons && buttons.length > 0) {
      buttons[Math.min(idx, buttons.length - 1)].focus();
    } else {
      containerRef.current?.focus();
    }
  }, [value]);

  // Close the dropdown on a press outside the control and its (portalled) list.
  useCloseOnOutsidePress(isOpen, [containerRef, dropdownRef], () => {
    setIsOpen(false);
    setSearchTerm('');
  });

  // Position the dropdown when it opens
  useEffect(() => {
    if (!isOpen || !containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    // The height the popup may grow to is the design system's (tl-select__popup max-height: eight
    // rows, the search field and the padding); it follows the density, so it is read from the
    // rendered popup rather than repeated here. Should it not resolve, the popup's current height
    // decides.
    const popup = dropdownRef.current;
    const cssMaxHeight = popup ? parseFloat(getComputedStyle(popup).maxHeight) : NaN;
    const maxHeight = Number.isFinite(cssMaxHeight) ? cssMaxHeight : (popup?.offsetHeight ?? 0);
    const flipAbove = spaceBelow < maxHeight && rect.top > spaceBelow;

    setDropdownStyle({
      left: rect.left,
      width: rect.width,
      ...(flipAbove
        ? { bottom: window.innerHeight - rect.top }
        : { top: rect.bottom }),
    });
  }, [isOpen]);

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

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
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
    return <ReadonlyValues id={controlId} className={rootClassName(state)} value={value} onGoto={goto} />;
  }

  // -- Editable or disabled rendering --

  const showClearButton = !mandatory && value.length > 0 && !disabled;
  const listboxId = `${controlId}-listbox`;

  // The position is data measured from the field (see above); layer, surface and shadow are the
  // design system's (tl-select__popup).
  const dropdownContent = isOpen ? (
    <div
      ref={dropdownRef}
      className="tl-select__popup"
      style={dropdownStyle}
      {...anchoredOverlayProps}
    >
      {/* Search field - shown when options are loaded */}
      {(optionsLoaded || loadError) && (
        <span className="tl-field-group tl-select__search-group">
          <span className="tl-field-group__icon" aria-hidden="true">
            <ThemeIcon encoded="css:fa-solid fa-magnifying-glass" className="tl-icon-sm" />
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
        <div className="tl-select__status tl-type-body">
          <ProgressBar fraction={null} label={i18n['js.dropdownSelect.loading']} />
        </div>
      )}
      {loadError && (
        <div className="tl-select__status tl-type-body" role="alert">
          <button type="button" className="tl-button tl-button--link tl-type-label" onClick={handleRetry}>
            {i18n['js.dropdownSelect.error']}
          </button>
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
        aria-disabled={disabled || undefined}
        tabIndex={disabled ? -1 : 0}
        onClick={!isOpen ? openDropdown : undefined}
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
                <Chip
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
            <button
              type="button"
              className="tl-select__clear"
              onClick={clearAll}
              aria-label={i18n['js.dropdownSelect.clear']}
              {...tooltipProps(i18n['js.dropdownSelect.clear'])}
            >
              <ThemeIcon encoded="css:fa-solid fa-xmark" className="tl-icon-sm" />
            </button>
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
