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
}: {
  option: OptionDescriptor;
  removable: boolean;
  onRemove: (value: string) => void;
  removeLabel: string;
}) {
  const handleRemove = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      onRemove(option.value);
    },
    [onRemove, option.value]
  );

  return (
    <span className="tlDropdownSelect__chip">
      <OptionImage image={option.image} />
      <span className="tlDropdownSelect__chipLabel">{option.label}</span>
      {removable && (
        <button
          type="button"
          className="tlDropdownSelect__chipRemove"
          onClick={handleRemove}
          aria-label={removeLabel}
          tabIndex={-1}
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
  const mandatory = state.mandatory === true;
  const disabled = state.disabled === true;
  const editable = state.editable !== false;
  const optionsLoaded = state.optionsLoaded === true;
  const allOptions = (state.options ?? []) as OptionDescriptor[];
  const emptyOptionLabel = (state.emptyOptionLabel ?? '') as string;
  const nothingFoundLabel = (state.nothingFoundLabel ?? '') as string;

  // I18N for client-side labels
  const i18n = useI18N({
    'js.dropdownSelect.filterPlaceholder': 'Filter\u2026',
    'js.dropdownSelect.clear': 'Clear selection',
    'js.dropdownSelect.removeChip': 'Remove {0}',
    'js.dropdownSelect.loading': 'Loading\u2026',
    'js.dropdownSelect.error': 'Failed to load options. Retry',
  });

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

  // Refs
  const containerRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

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

  // Reset highlight when filtered options change
  useEffect(() => {
    setHighlightedIndex(-1);
  }, [filteredOptions.length]);

  // Focus search input when dropdown opens and options are loaded
  useEffect(() => {
    if (isOpen && optionsLoaded && searchRef.current) {
      searchRef.current.focus();
    }
  }, [isOpen, optionsLoaded]);

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
          newValue = [...value, opt];
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

      sendCommand('valueChanged', { value: newValue.map((v) => v.value) });

      if (!multiSelect) {
        closeDropdown();
      } else {
        setSearchTerm('');
        setHighlightedIndex(-1);
        searchRef.current?.focus();
      }
    },
    [multiSelect, value, allOptions, sendCommand, closeDropdown]
  );

  const removeOption = useCallback(
    (optionValue: string) => {
      const newValue = value.filter((v) => v.value !== optionValue);
      sendCommand('valueChanged', { value: newValue.map((v) => v.value) });
    },
    [value, sendCommand]
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
          e.preventDefault();
          openDropdown();
        }
        return;
      }

      switch (e.key) {
        case 'ArrowDown':
          e.preventDefault();
          setHighlightedIndex((prev) =>
            prev < filteredOptions.length - 1 ? prev + 1 : 0
          );
          break;
        case 'ArrowUp':
          e.preventDefault();
          setHighlightedIndex((prev) =>
            prev > 0 ? prev - 1 : filteredOptions.length - 1
          );
          break;
        case 'Enter':
          e.preventDefault();
          if (highlightedIndex >= 0 && highlightedIndex < filteredOptions.length) {
            selectOption(filteredOptions[highlightedIndex].value);
          }
          break;
        case 'Escape':
          e.preventDefault();
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
            value.map((v) => (
              <Chip
                key={v.value}
                option={v}
                removable={!disabled && (multiSelect || !mandatory)}
                onRemove={removeOption}
                removeLabel={removeChipLabel(v.label)}
              />
            ))
          )}
        </div>
        <div className="tlDropdownSelect__controls">
          {showClearButton && (
            <button
              type="button"
              className="tlDropdownSelect__clearAll"
              onClick={clearAll}
              aria-label={i18n['js.dropdownSelect.clear']}
              tabIndex={-1}
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
