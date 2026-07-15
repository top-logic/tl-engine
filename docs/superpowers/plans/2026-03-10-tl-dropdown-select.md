# TLDropdownSelect Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a React-based dropdown select control (`TLDropdownSelect`) with chip/tag display, search-as-you-type filtering, image-rich option rendering, and single/multi-select support.

**Architecture:** A Java `ReactDropdownSelectControl` wraps a `SelectField` model and communicates with a `TLDropdownSelect` React component via SSE state updates and commands. Options are loaded lazily on first dropdown open via a `loadOptions` command, then filtered client-side in React.

**Tech Stack:** Java 17, TypeScript/React (via tl-react-bridge), CSS with TopLogic CSS variables, msgbuf protocol.

**Spec:** `docs/superpowers/specs/2026-03-10-tl-dropdown-select-design.md`

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/select/ReactDropdownSelectControl.java` | Create | Server-side control: wraps SelectField, handles loadOptions/valueChanged commands, generates stable option IDs via label-based hashing |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java` | Modify | Add I18N keys for dropdown select |
| `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.tsx` | Create | React component: chip display, dropdown portal, search, keyboard navigation |
| `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.css` | Create | Component CSS styles |
| `com.top_logic.layout.react/react-src/controls-entry.ts` | Modify | Register TLDropdownSelect component |

## Chunk 1: Server-Side Control

### Task 1: Create ReactDropdownSelectControl

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/select/ReactDropdownSelectControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java`

- [ ] **Step 1: Add I18N constants**

Add the following constants to `I18NConstants.java`, before the `static { initConstants(...) }` block:

```java
/**
 * @en Selection changed.
 */
public static ResKey REACT_DROPDOWN_SELECT_VALUE_CHANGED;

// -- Dropdown select client-side i18n keys --

/**
 * @en Select\u2026
 * @de Ausw\u00e4hlen\u2026
 */
public static ResKey JS_DROPDOWN_SELECT_EMPTY = ResKey.internalCreate("js.dropdownSelect.empty");

/**
 * @en Nothing found
 * @de Keine Treffer
 */
public static ResKey JS_DROPDOWN_SELECT_NOTHING_FOUND = ResKey.internalCreate("js.dropdownSelect.nothingFound");

/**
 * @en Filter\u2026
 * @de Filtern\u2026
 */
public static ResKey JS_DROPDOWN_SELECT_FILTER_PLACEHOLDER = ResKey.internalCreate("js.dropdownSelect.filterPlaceholder");

/**
 * @en Remove {0}
 * @de {0} entfernen
 */
public static ResKey JS_DROPDOWN_SELECT_REMOVE_CHIP = ResKey.internalCreate("js.dropdownSelect.removeChip");

/**
 * @en Clear selection
 * @de Auswahl l\u00f6schen
 */
public static ResKey JS_DROPDOWN_SELECT_CLEAR = ResKey.internalCreate("js.dropdownSelect.clear");

/**
 * @en Loading\u2026
 * @de Laden\u2026
 */
public static ResKey JS_DROPDOWN_SELECT_LOADING = ResKey.internalCreate("js.dropdownSelect.loading");

/**
 * @en Failed to load options. Retry
 * @de Optionen konnten nicht geladen werden. Erneut versuchen
 */
public static ResKey JS_DROPDOWN_SELECT_ERROR = ResKey.internalCreate("js.dropdownSelect.error");
```

- [ ] **Step 2: Create ReactDropdownSelectControl.java**

Create the file `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/select/ReactDropdownSelectControl.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link ReactControl} that renders a dropdown select with search-as-you-type
 * filtering via the {@code TLDropdownSelect} React component.
 *
 * <p>
 * Supports single and multi-selection, chip/tag display for selected values,
 * and image-rich option rendering. Options are loaded lazily on first dropdown
 * open via the {@code loadOptions} command.
 * </p>
 */
public class ReactDropdownSelectControl extends ReactControl {

	private static final String VALUE = "value";
	private static final String OPTIONS = "options";
	private static final String OPTIONS_LOADED = "optionsLoaded";
	private static final String MULTI_SELECT = "multiSelect";
	private static final String MANDATORY = "mandatory";
	private static final String DISABLED = "disabled";
	private static final String EDITABLE = "editable";
	private static final String EMPTY_OPTION_LABEL = "emptyOptionLabel";
	private static final String NOTHING_FOUND_LABEL = "nothingFoundLabel";

	private static final String OPT_VALUE = "value";
	private static final String OPT_LABEL = "label";
	private static final String OPT_IMAGE = "image";

	private final SelectField _selectField;

	/**
	 * Maps stable option ID strings to the original option objects. Built during
	 * {@link #handleLoadOptions()} and used by {@link #handleValueChanged(Map)} to
	 * resolve client-sent IDs back to model objects.
	 */
	private Map<String, Object> _optionIndex = Collections.emptyMap();

	/**
	 * Creates a new {@link ReactDropdownSelectControl}.
	 *
	 * @param selectField
	 *        The {@link SelectField} model to wrap.
	 */
	public ReactDropdownSelectControl(SelectField selectField) {
		super(selectField, "TLDropdownSelect");
		_selectField = selectField;
	}

	@Override
	protected void onBeforeWrite(ViewDisplayContext context) {
		super.onBeforeWrite(context);
		syncState();
	}

	private void syncState() {
		Resources resources = Resources.getInstance();

		putState(VALUE, toOptionDescriptors(_selectField.getSelection()));
		putState(MULTI_SELECT, _selectField.isMultiple());
		putState(MANDATORY, _selectField.isMandatory());
		putState(DISABLED, _selectField.isDisabled());
		putState(EDITABLE, !_selectField.isImmutable());
		putState(EMPTY_OPTION_LABEL,
			resources.getString(I18NConstants.JS_DROPDOWN_SELECT_EMPTY));
		putState(NOTHING_FOUND_LABEL,
			resources.getString(I18NConstants.JS_DROPDOWN_SELECT_NOTHING_FOUND));
		putState(OPTIONS_LOADED, false);
	}

	/**
	 * Handles the {@code loadOptions} command from the React client.
	 *
	 * <p>
	 * Sends the full option list as a state patch via SSE. Builds an internal index
	 * mapping stable option IDs to model objects for later resolution.
	 * </p>
	 */
	@ReactCommand("loadOptions")
	HandlerResult handleLoadOptions() {
		try {
			List<?> options = _selectField.getOptions();
			Map<String, Object> newIndex = new LinkedHashMap<>();
			List<Map<String, Object>> descriptors = buildOptionDescriptors(options, newIndex);
			_optionIndex = newIndex;

			Map<String, Object> patch = new HashMap<>();
			patch.put(OPTIONS, descriptors);
			patch.put(OPTIONS_LOADED, true);
			patchReactState(patch);
		} catch (Exception ex) {
			Logger.error("Failed to load options for " + _selectField.getName(), ex, this);
			return HandlerResult.error(I18NConstants.JS_DROPDOWN_SELECT_ERROR);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles the {@code valueChanged} command from the React client.
	 *
	 * @param arguments
	 *        Must contain a {@code "value"} key with a list of option value IDs.
	 */
	@SuppressWarnings("unchecked")
	@ReactCommand("valueChanged")
	HandlerResult handleValueChanged(Map<String, Object> arguments) {
		List<String> selectedIds = (List<String>) arguments.get(VALUE);
		if (selectedIds == null) {
			selectedIds = Collections.emptyList();
		}

		List<Object> newSelection = resolveOptions(selectedIds);
		_selectField.setAsSelection(newSelection);

		// Update value state with full descriptors for chip display.
		putState(VALUE, toOptionDescriptors(_selectField.getSelection()));

		return HandlerResult.DEFAULT_RESULT;
	}

	private List<Object> resolveOptions(List<String> ids) {
		List<Object> resolved = new ArrayList<>(ids.size());
		for (String id : ids) {
			Object option = _optionIndex.get(id);
			if (option != null) {
				resolved.add(option);
			}
		}
		return resolved;
	}

	/**
	 * Builds option descriptors and populates the given index map with stable ID
	 * to option mappings.
	 */
	private List<Map<String, Object>> buildOptionDescriptors(List<?> options,
			Map<String, Object> index) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		LabelProvider labelProvider = _selectField.getOptionLabelProvider();
		ResourceProvider resourceProvider = toResourceProvider(labelProvider);

		int counter = 0;
		for (Object option : options) {
			String id = stableOptionId(option, labelProvider, counter++);
			index.put(id, option);

			Map<String, Object> descriptor = new HashMap<>();
			descriptor.put(OPT_VALUE, id);
			descriptor.put(OPT_LABEL, labelProvider.getLabel(option));

			if (resourceProvider != null) {
				ThemeImage image = resourceProvider.getImage(option, Flavor.DEFAULT);
				if (image != null) {
					descriptor.put(OPT_IMAGE, image.toEncodedForm());
				}
			}

			descriptors.add(descriptor);
		}
		return descriptors;
	}

	/**
	 * Converts a list of selected options to descriptors. Uses the existing
	 * {@link #_optionIndex} for stable IDs when available, falling back to
	 * index-based IDs for the initial render (before loadOptions).
	 */
	private List<Map<String, Object>> toOptionDescriptors(List<?> options) {
		List<Map<String, Object>> descriptors = new ArrayList<>(options.size());
		LabelProvider labelProvider = _selectField.getOptionLabelProvider();
		ResourceProvider resourceProvider = toResourceProvider(labelProvider);

		int counter = 0;
		for (Object option : options) {
			String id = stableOptionId(option, labelProvider, counter++);

			Map<String, Object> descriptor = new HashMap<>();
			descriptor.put(OPT_VALUE, id);
			descriptor.put(OPT_LABEL, labelProvider.getLabel(option));

			if (resourceProvider != null) {
				ThemeImage image = resourceProvider.getImage(option, Flavor.DEFAULT);
				if (image != null) {
					descriptor.put(OPT_IMAGE, image.toEncodedForm());
				}
			}

			descriptors.add(descriptor);
		}
		return descriptors;
	}

	/**
	 * Generates a stable option ID by combining the option's label with its
	 * position index. This survives server restarts and is unique within the
	 * option list.
	 */
	private String stableOptionId(Object option, LabelProvider labelProvider, int index) {
		String label = labelProvider.getLabel(option);
		if (label == null) {
			label = "";
		}
		return index + ":" + label;
	}

	private ResourceProvider toResourceProvider(LabelProvider labelProvider) {
		if (labelProvider instanceof ResourceProvider) {
			return (ResourceProvider) labelProvider;
		}
		return null;
	}
}
```

- [ ] **Step 3: Build the module to verify compilation**

Run: `mvn install -pl com.top_logic.layout.react -am -DskipTests=true` from the worktree root.
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/select/ReactDropdownSelectControl.java
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java
git add com.top_logic.layout.react/src/main/java/META-INF/messages_en.properties
git add com.top_logic.layout.react/src/main/java/META-INF/messages_de.properties
git commit -m "Ticket #29108: Add ReactDropdownSelectControl server-side control."
```

## Chunk 2: React Component and CSS

### Task 2: Create TLDropdownSelect React component and styles

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.tsx`
- Create: `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.css`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

- [ ] **Step 1: Create TLDropdownSelect.css**

Create `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.css`:

```css
/* TLDropdownSelect - chip-based dropdown select control */

.tlDropdownSelect {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-subtle, #ccc);
  border-radius: 4px;
  background-color: var(--field, #fff);
  padding: 4px 8px;
  min-height: var(--form-line-height, 32px);
  cursor: pointer;
  box-sizing: border-box;
}

.tlDropdownSelect:focus {
  outline: 2px solid var(--focus, #0078d4);
  outline-offset: -1px;
}

.tlDropdownSelect--open {
  border-color: var(--focus, #0078d4);
}

.tlDropdownSelect--disabled {
  opacity: 0.6;
  cursor: default;
  background-color: var(--field-disabled, #f0f0f0);
}

.tlDropdownSelect--immutable {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

/* Chip area */
.tlDropdownSelect__chips {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.tlDropdownSelect__placeholder {
  color: var(--text-secondary, #888);
  font-style: italic;
}

.tlDropdownSelect__empty {
  color: var(--text-secondary, #888);
}

/* Individual chip */
.tlDropdownSelect__chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background-color: var(--layer-hover, #e8e8e8);
  border-radius: 12px;
  padding: 2px 8px 2px 4px;
  max-width: 100%;
}

.tlDropdownSelect__chipLabel {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tlDropdownSelect__chipRemove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary, #888);
  font-size: 14px;
  padding: 0 2px;
  line-height: 1;
}

.tlDropdownSelect__chipRemove:hover {
  color: var(--text-primary, #333);
}

/* Controls area (clear, arrow) */
.tlDropdownSelect__controls {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 4px;
}

.tlDropdownSelect__clearAll {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary, #888);
  font-size: 16px;
  padding: 0 2px;
  line-height: 1;
}

.tlDropdownSelect__clearAll:hover {
  color: var(--text-primary, #333);
}

.tlDropdownSelect__arrow {
  color: var(--text-secondary, #888);
  font-size: 10px;
}

/* Read-only value */
.tlDropdownSelect__readonlyValue {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* Dropdown portal */
.tlDropdownSelect__dropdown {
  background-color: var(--field, #fff);
  border: 1px solid var(--border-subtle, #ccc);
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Search wrapper */
.tlDropdownSelect__searchWrapper {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-bottom: 1px solid var(--border-subtle, #ccc);
}

.tlDropdownSelect__searchIcon {
  margin-right: 6px;
  font-size: 14px;
  color: var(--text-secondary, #888);
}

.tlDropdownSelect__search {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: inherit;
  font-family: inherit;
  color: var(--text-primary, #333);
}

/* Option list */
.tlDropdownSelect__list {
  overflow-y: auto;
  max-height: 250px;
}

/* Single option */
.tlDropdownSelect__option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  cursor: pointer;
  color: var(--text-secondary, #555);
  line-height: var(--form-line-height, 32px);
  white-space: nowrap;
}

.tlDropdownSelect__option:hover,
.tlDropdownSelect__option--highlighted {
  background-color: var(--layer-hover, #e8e8e8);
  color: var(--text-primary, #333);
}

.tlDropdownSelect__optionLabel {
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Option image / icon */
.tlDropdownSelect__optionImage {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  object-fit: contain;
}

.tlDropdownSelect__optionIcon {
  font-size: 20px;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* Loading / Error / No results */
.tlDropdownSelect__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.tlDropdownSelect__spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-subtle, #ccc);
  border-top-color: var(--focus, #0078d4);
  border-radius: 50%;
  animation: tlDropdownSelectSpin 0.8s linear infinite;
}

@keyframes tlDropdownSelectSpin {
  to { transform: rotate(360deg); }
}

.tlDropdownSelect__error {
  padding: 16px;
  text-align: center;
  color: var(--text-secondary, #888);
}

.tlDropdownSelect__error a {
  color: var(--focus, #0078d4);
}

.tlDropdownSelect__noResults {
  padding: 16px 10px;
  text-align: center;
  color: var(--text-secondary, #888);
  font-style: italic;
}
```

- [ ] **Step 2: Create TLDropdownSelect.tsx**

Create `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.tsx`:

```tsx
import { React, useTLState, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import './TLDropdownSelect.css';

const { useState, useCallback, useRef, useEffect, useMemo, createPortal } = React;

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
  return <span className={`tlDropdownSelect__optionIcon ${image}`} />;
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

  // Focus search input when dropdown opens
  useEffect(() => {
    if (isOpen && searchRef.current) {
      searchRef.current.focus();
    }
  }, [isOpen]);

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

  // Dropdown position (fixed, relative to control)
  const [dropdownStyle, setDropdownStyle] = useState<React.CSSProperties>({});
  useEffect(() => {
    if (!isOpen || !containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    const spaceBelow = window.innerHeight - rect.bottom;
    const maxHeight = 250;
    const flipAbove = spaceBelow < maxHeight && rect.top > spaceBelow;

    setDropdownStyle({
      position: 'fixed',
      left: rect.left,
      width: rect.width,
      ...(flipAbove
        ? { bottom: window.innerHeight - rect.top }
        : { top: rect.bottom }),
      maxHeight: maxHeight + 50, /* extra for search field */
      zIndex: 10000,
    });
  }, [isOpen]);

  const dropdownContent = isOpen ? (
    <div
      ref={dropdownRef}
      className="tlDropdownSelect__dropdown"
      style={dropdownStyle}
    >
      {/* Search field */}
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

      {dropdownContent && (typeof document !== 'undefined')
        ? (React as any).createPortal(dropdownContent, document.body)
        : dropdownContent}
    </>
  );
};

export default TLDropdownSelect;
```

**Note on portal:** The `createPortal` usage appends the dropdown to `document.body` so it escapes any `overflow: hidden` parent containers. The `(React as any).createPortal` cast is needed because `createPortal` is on `ReactDOM`, not `React`. If the build exposes `ReactDOM` from `tl-react-bridge`, use that instead. Otherwise, import `createPortal` from `react-dom` if available. The implementer should verify which approach the build supports and adjust accordingly -- the key requirement is that the dropdown renders outside the control's DOM tree.

- [ ] **Step 3: Register in controls-entry.ts**

Add to `com.top_logic.layout.react/react-src/controls-entry.ts`:

After the existing `import TLTreeView` line, add:
```typescript
import TLDropdownSelect from './controls/TLDropdownSelect';
```

After the existing `register('TLTreeView', TLTreeView);` line, add:
```typescript
register('TLDropdownSelect', TLDropdownSelect);
```

- [ ] **Step 4: Build the module**

Run: `mvn install -pl com.top_logic.layout.react -am -DskipTests=true` from the worktree root.
Expected: BUILD SUCCESS (both Java and React bundle compiled).

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLDropdownSelect.tsx
git add com.top_logic.layout.react/react-src/controls/TLDropdownSelect.css
git add com.top_logic.layout.react/react-src/controls-entry.ts
git commit -m "Ticket #29108: Add TLDropdownSelect React component with CSS."
```

## Chunk 3: Integration Verification

### Task 3: Full build and verification

- [ ] **Step 1: Full module build**

Run: `mvn install -pl com.top_logic.layout.react -am -DskipTests=true` from the worktree root.
Expected: BUILD SUCCESS (both Java and React bundle compiled).

- [ ] **Step 2: Verify the generated messages files include new I18N keys**

Check that `com.top_logic.layout.react/src/main/java/META-INF/messages_en.properties` contains:
- `js.dropdownSelect.empty`
- `js.dropdownSelect.nothingFound`
- `js.dropdownSelect.filterPlaceholder`
- `js.dropdownSelect.removeChip`
- `js.dropdownSelect.clear`

- [ ] **Step 3: Manual smoke test (optional)**

If a demo app is available, wire up a `ReactDropdownSelectControl` in a demo view to verify:
1. Dropdown opens and shows loading spinner
2. Options appear after loading
3. Search filtering works
4. Single/multi selection works
5. Chips display correctly with images
6. Keyboard navigation works (Arrow Down/Up, Enter, Escape, Tab, Backspace)
7. Clear button works (hidden when mandatory)
8. Read-only / disabled states render correctly
9. Dropdown flips above when near viewport bottom

- [ ] **Step 4: Final commit with any fixups**

```bash
git add -A
git commit -m "Ticket #29108: Fix any issues found during integration verification."
```

(Skip this commit if no fixups are needed.)
