import { React, useTLState, useTLCommand, useI18N } from 'tl-react-bridge';
import { createPortal } from 'react-dom';
import type { TLCellProps } from 'tl-react-bridge';

const { useState, useCallback, useRef, useEffect, useMemo } = React;

// -- Types --

interface OptionDescriptor {
  value: string;
  label: string;
  image?: string;
}

// -- Sub-components --

/** Renders an option's image (URL or CSS class) */
function OptionImage({ image }: { image?: string }) {
  if (!image) return null;
  if (image.startsWith('/')) {
    return <img src={image} alt="" className="tlDropdownSelect__optionImage" />;
  }
  // Strip "css:" or "colored:" prefix from ThemeImage.toEncodedForm() output.
  const cssClass = image.startsWith('css:') ? image.substring(4)
    : image.startsWith('colored:') ? image.substring(8)
    : image;
  return <span className={`tlDropdownSelect__optionIcon ${cssClass}`} />;
}

/** Renders a selected value as a chip/tag */
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
  dragClassName,
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
  dragClassName?: string;
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
      className={'tlDropdownSelect__chip' + (dragClassName ? ' ' + dragClassName : '')}
      draggable={draggable || undefined}
      onDragStart={onDragStart}
      onDragOver={onDragOver}
      onDrop={onDrop}
      onDragEnd={onDragEnd}
    >
      {draggable && (
        <span className="tlDropdownSelect__dragHandle" aria-hidden="true">&#8942;&#8942;</span>
      )}
      <OptionImage image={option.image} />
      <span className="tlDropdownSelect__chipLabel">{option.label}</span>
      {removable && (
        <button
          type="button"
          className="tlDropdownSelect__chipRemove"
          onClick={handleRemove}
          aria-label={removeLabel}
        >
          &times;
        </button>
      )}
    </span>
  );
}

/** Renders a single option row in the dropdown, with match highlighting */
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
    return (
      <>
        {option.label.substring(0, idx)}
        <strong>{option.label.substring(idx, idx + searchTerm.length)}</strong>
        {option.label.substring(idx + searchTerm.length)}
      </>
    );
  }, [option.label, searchTerm]);

  return (
    <div
      id={id}
      role="option"
      aria-selected={highlighted}
      className={
        'tlDropdownSelect__option' +
        (highlighted ? ' tlDropdownSelect__option--highlighted' : '')
      }
      onClick={handleClick}
      onMouseEnter={onMouseEnter}
    >
      <OptionImage image={option.image} />
      <span className="tlDropdownSelect__optionLabel">{labelContent}</span>
    </div>
  );
}

// -- Main component --

const TLDropdownSelect: React.FC<TLCellProps> = ({ controlId, state }) => {
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
  const emptyOptionLabel = (state.emptyOptionLabel ?? '') as string;

  // Drag-and-drop is enabled only for custom-order multi-select editable fields
  // and only after options are loaded (so the server option index can resolve IDs).
  const dragEnabled = customOrder && multiSelect && !disabled && editable && optionsLoaded;

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
      '.tlDropdownSelect__chipRemove'
    );
    if (buttons && buttons.length > 0) {
      buttons[Math.min(idx, buttons.length - 1)].focus();
    } else {
      containerRef.current?.focus();
    }
  }, [value]);

  // Close dropdown on outside click
  useEffect(() => {
    if (!isOpen) return;
    const handleOutsideClick = (e: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node) &&
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node)
      ) {
        setIsOpen(false);
        setSearchTerm('');
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, [isOpen]);

  // Position the dropdown when it opens
  useEffect(() => {
    if (!isOpen || !containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const maxHeight = 300; // list max-height + search field
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
      sendCommand('valueChanged', { value: newValue.map((v) => v.value) });

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
      sendCommand('valueChanged', { value: newValue.map((v) => v.value) });
    },
    [sendCommand]
  );

  const clearAll = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      sendCommand('valueChanged', { value: [] });
      closeDropdown();
    },
    [sendCommand, closeDropdown]
  );

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  }, []);

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
      sendCommand('valueChanged', { value: reordered.map((v) => v.value) });

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

  // -- Immutable (read-only) rendering --

  if (!editable) {
    return (
      <div id={controlId} className="tlDropdownSelect tlDropdownSelect--immutable">
        {value.length === 0 ? (
          <span className="tlDropdownSelect__empty">{emptyOptionLabel}</span>
        ) : (
          value.map((v) => (
            <span key={v.value} className="tlDropdownSelect__readonlyValue">
              <OptionImage image={v.image} />
              <span>{v.label}</span>
            </span>
          ))
        )}
      </div>
    );
  }

  // -- Editable rendering --

  const showClearButton = !mandatory && value.length > 0 && !disabled;

  const dropdownContent = isOpen ? (
    <div
      ref={dropdownRef}
      className="tlDropdownSelect__dropdown"
      style={dropdownStyle}
    >
      {/* Search field - shown when options are loaded */}
      {(optionsLoaded || loadError) && (
        <div className="tlDropdownSelect__searchWrapper">
          <span className="tlDropdownSelect__searchIcon" aria-hidden="true">
            &#128269;
          </span>
          <input
            ref={searchRef}
            type="text"
            className="tlDropdownSelect__search"
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
            aria-controls={`${controlId}-listbox`}
          />
        </div>
      )}

      {/* Option list or loading/error/empty states */}
      <div
        id={`${controlId}-listbox`}
        role="listbox"
        className="tlDropdownSelect__list"
      >
        {!optionsLoaded && !loadError && (
          <div className="tlDropdownSelect__loading">
            <span className="tlDropdownSelect__spinner" />
          </div>
        )}
        {loadError && (
          <div className="tlDropdownSelect__error">
            <a href="#" onClick={handleRetry}>
              {i18n['js.dropdownSelect.error']}
            </a>
          </div>
        )}
        {optionsLoaded && filteredOptions.length === 0 && (
          <div className="tlDropdownSelect__noResults">
            {nothingFoundLabel}
          </div>
        )}
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
        ref={containerRef}
        className={
          'tlDropdownSelect' +
          (isOpen ? ' tlDropdownSelect--open' : '') +
          (disabled ? ' tlDropdownSelect--disabled' : '')
        }
        role="combobox"
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        aria-owns={isOpen ? `${controlId}-listbox` : undefined}
        tabIndex={disabled ? -1 : 0}
        onClick={!isOpen ? openDropdown : undefined}
        onKeyDown={handleKeyDown}
      >
        <div className="tlDropdownSelect__chips">
          {value.length === 0 ? (
            <span className="tlDropdownSelect__placeholder">{emptyOptionLabel}</span>
          ) : (
            value.map((v, idx) => {
              let dragClass = '';
              if (dragIndex === idx) {
                dragClass = 'tlDropdownSelect__chip--dragging';
              } else if (dropTargetIndex === idx && dropPosition === 'before') {
                dragClass = 'tlDropdownSelect__chip--dropBefore';
              } else if (dropTargetIndex === idx && dropPosition === 'after') {
                dragClass = 'tlDropdownSelect__chip--dropAfter';
              }
              return (
                <Chip
                  key={v.value}
                  option={v}
                  removable={!disabled && (multiSelect || !mandatory)}
                  onRemove={removeOption}
                  removeLabel={removeChipLabel(v.label)}
                  draggable={dragEnabled}
                  onDragStart={dragEnabled ? (e) => handleChipDragStart(idx, e) : undefined}
                  onDragOver={dragEnabled ? (e) => handleChipDragOver(idx, e) : undefined}
                  onDrop={dragEnabled ? handleChipDrop : undefined}
                  onDragEnd={dragEnabled ? handleChipDragEnd : undefined}
                  dragClassName={dragEnabled ? dragClass : undefined}
                />
              );
            })
          )}
        </div>
        <div className="tlDropdownSelect__controls">
          {showClearButton && (
            <button
              type="button"
              className="tlDropdownSelect__clearAll"
              onClick={clearAll}
              aria-label={i18n['js.dropdownSelect.clear']}
            >
              &times;
            </button>
          )}
          <span className="tlDropdownSelect__arrow" aria-hidden="true">
            {isOpen ? '\u25B2' : '\u25BC'}
          </span>
        </div>
      </div>

      {dropdownContent && createPortal(dropdownContent, document.body)}
    </>
  );
};

export default TLDropdownSelect;
