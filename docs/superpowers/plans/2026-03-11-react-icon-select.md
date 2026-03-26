# React Icon Select Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a React-based icon select control (`TLIconSelect`) that lets users browse, search, and select icons from the TopLogic icon library, with an advanced mode for editing the encoded CSS class directly.

**Architecture:** Server-side `ReactIconSelectControl` extends `ReactFormFieldControl`, lazily loads icon bundles via a `loadIcons` command, and sends icon metadata to the React frontend. The React `TLIconSelect` component shows a swatch trigger button and a popup with two tabs: Simple (click-to-select) and Advanced (edit encoded form with live preview).

**Tech Stack:** Java 17 (server control), TypeScript/React (frontend components), CSS (styling), SSE (state sync).

**Spec:** `docs/superpowers/specs/2026-03-11-react-icon-select-design.md`

**Prototype:** `docs/prototypes/icon-select-prototype.html`

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactIconSelectControl.java` | Create | Server-side control: manages state, loads icon bundles, handles commands |
| `com.top_logic.layout.react/react-src/controls/TLIconSelect.tsx` | Create | Main React component: swatch trigger button, open/close popup |
| `com.top_logic.layout.react/react-src/controls/icon/IconSelectPopup.tsx` | Create | Popup sub-component: tabs, search, icon grid, advanced editing |
| `com.top_logic.layout.react/react-src/controls-entry.ts` | Modify | Register `TLIconSelect` |
| `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` | Modify | Append icon select CSS styles |

---

## Chunk 1: Server-Side Control

### Task 1: Create ReactIconSelectControl

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactIconSelectControl.java`

**Reference files (read before implementing):**
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactFormFieldControl.java` - base class pattern
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java` - `putState`, `@ReactCommand`
- `com.top_logic/src/main/java/com/top_logic/layout/form/control/IconInputControl.java` - `parseResources()` logic to reuse
- `com.top_logic/src/main/java/com/top_logic/layout/form/control/IconBundle.java` - icon data container
- `com.top_logic/src/main/java/com/top_logic/layout/form/control/IconDescription.java` - icon metadata

- [ ] **Step 1: Create ReactIconSelectControl.java**

The control extends `ReactFormFieldControl` and adds lazy icon loading. The value is an encoded ThemeImage string (e.g. `"css:fa-solid fa-home"`). It overrides `valueChanged` handling because the value comes as an encoded string, not a raw form input.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.common.json.adapt.ReaderR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.IconBundle;
import com.top_logic.layout.form.control.IconDescription;
import com.top_logic.layout.form.control.IconInputControl;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.util.error.TopLogicException;

/**
 * Icon select control that renders via the {@code TLIconSelect} React component.
 *
 * <p>
 * Displays the currently selected icon as a preview swatch and opens a popup picker to browse and
 * search icons from the configured icon libraries (e.g. Font Awesome).
 * </p>
 *
 * <p>
 * Icons are loaded lazily on first popup open via the {@code loadIcons} command to avoid sending
 * the full icon library on page load.
 * </p>
 */
public class ReactIconSelectControl extends ReactFormFieldControl {

	private static final String ICONS = "icons";

	private static final String ICONS_LOADED = "iconsLoaded";

	/**
	 * Creates a new {@link ReactIconSelectControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The form field whose value is an encoded {@link ThemeImage} string.
	 */
	public ReactIconSelectControl(ReactContext context, FormField model) {
		super(context, model, "TLIconSelect");
		putState(ICONS_LOADED, Boolean.FALSE);
	}

	@Override
	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		FormField field = getFieldModel();
		Object rawValue = arguments.get(VALUE);
		String encodedIcon = rawValue != null ? rawValue.toString() : null;

		try {
			FormFieldInternals.updateFieldNoClientUpdate(field, encodedIcon);
		} catch (VetoException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_COMMAND_FAILED__MSG.fill(ex.getMessage()), ex);
		}
	}

	/**
	 * Lazily loads icon metadata from configured icon libraries and sends it to the client.
	 */
	@ReactCommand("loadIcons")
	void handleLoadIcons() {
		List<Map<String, Object>> iconData = buildIconData();
		putState(ICONS, iconData);
		putState(ICONS_LOADED, Boolean.TRUE);
	}

	private List<Map<String, Object>> buildIconData() {
		List<IconBundle> bundles = loadIconBundles();
		List<Map<String, Object>> result = new ArrayList<>();

		for (IconBundle bundle : bundles) {
			for (IconDescription icon : bundle.getIcons()) {
				List<Map<String, Object>> variants = new ArrayList<>();
				for (ThemeImage img : icon.getImages()) {
					Map<String, Object> variant = new HashMap<>();
					variant.put("encoded", img.toEncodedForm());
					variants.add(variant);
				}

				if (!variants.isEmpty()) {
					Map<String, Object> entry = new HashMap<>();
					entry.put("prefix", icon.getPrefix());
					entry.put("label", icon.getLabel());
					entry.put("variants", variants);
					result.add(entry);
				}
			}
		}

		return result;
	}

	/**
	 * Loads icon bundles from the configured icon library resources.
	 *
	 * <p>
	 * Reuses the same {@link IconInputControl.Config} configuration that the legacy icon chooser
	 * uses, reading JSON metadata files and applying style-to-CSS mappings.
	 * </p>
	 */
	static List<IconBundle> loadIconBundles() {
		List<IconBundle> resourceList = new ArrayList<>();
		FileManager fm = FileManager.getInstance();

		IconInputControl.Config config =
			ApplicationConfig.getInstance().getConfig(IconInputControl.Config.class);
		List<IconInputControl.ThemeImageMetaData> resources = config.getResources();

		for (IconInputControl.ThemeImageMetaData resource : resources) {
			Map<String, String> prefixMapping = resource.getIconMappings();
			String resourceName = resource.getResource();
			try {
				try (InputStream stream = fm.getStream(resourceName)) {
					Reader reader = new InputStreamReader(stream, StringServices.UTF8);
					JsonReader json = new JsonReader(new ReaderR(reader));
					resourceList.add(IconBundle.read(json, prefixMapping));
				}
			} catch (IOException ex) {
				Logger.warn(
					"Cannot read icon definition '" + resourceName + "'.",
					ex, ReactIconSelectControl.class);
			}
		}

		return resourceList;
	}

}
```

- [ ] **Step 2: Build to verify compilation**

Run from the `com.top_logic.layout.react` module directory:
```bash
cd com.top_logic.layout.react && mvn compile -DskipTests=true
```
Expected: BUILD SUCCESS (no compilation errors).

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ReactIconSelectControl.java
git commit -m "Ticket #29108: Add ReactIconSelectControl for React-based icon selection."
```

---

## Chunk 2: React Components

### Task 2: Create IconSelectPopup sub-component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/icon/IconSelectPopup.tsx`

**Reference files (read before implementing):**
- `com.top_logic.layout.react/react-src/controls/color/ColorPopup.tsx` - popup pattern (positioning, outside-click, escape, tabs)
- `com.top_logic.layout.react/react-src/controls/TLDropdownSelect.tsx` - search/filter pattern, loading state, keyboard navigation
- `com.top_logic.layout.react/react-src/bridge/tl-react-bridge.ts` - hooks: `useTLCommand`, `useI18N`
- `docs/prototypes/icon-select-prototype.html` - visual design reference

- [ ] **Step 1: Create IconSelectPopup.tsx**

The popup component handles:
- Two tabs: Simple and Advanced
- Search input for filtering icons by prefix, label
- Scrollable icon grid with all style variants
- Advanced tab: editable class input + live preview
- Simple tab: click icon to confirm immediately
- Advanced tab: OK/Cancel buttons to confirm
- Reset/clear button
- Escape to close, click-outside to close
- Positioning relative to anchor element

```typescript
import { React, useI18N } from 'tl-react-bridge';

const { useState, useCallback, useEffect, useRef, useLayoutEffect, useMemo } = React;

const I18N_KEYS = {
  'js.iconSelect.simpleTab': 'Simple',
  'js.iconSelect.advancedTab': 'Advanced',
  'js.iconSelect.filterPlaceholder': 'Filter icons\u2026',
  'js.iconSelect.noResults': 'No icons found',
  'js.iconSelect.loading': 'Loading\u2026',
  'js.iconSelect.classLabel': 'Class',
  'js.iconSelect.previewLabel': 'Preview',
  'js.iconSelect.cancel': 'Cancel',
  'js.iconSelect.ok': 'OK',
  'js.iconSelect.clear': 'Clear icon',
};

/** One style variant of an icon. */
interface IconVariant {
  encoded: string;
}

/** An icon entry from the server. */
export interface IconEntry {
  prefix: string;
  label: string;
  variants: IconVariant[];
}

type Tab = 'simple' | 'advanced';

interface IconSelectPopupProps {
  anchorRef: React.RefObject<HTMLButtonElement>;
  currentValue: string | null;
  icons: IconEntry[];
  iconsLoaded: boolean;
  onSelect: (encoded: string | null) => void;
  onCancel: () => void;
  onLoadIcons: () => Promise<void>;
}

/** Renders a single icon from its encoded form as an <i> or <img> element. */
function IconPreview({ encoded, className }: { encoded: string; className?: string }) {
  if (encoded.startsWith('css:')) {
    const cssClass = encoded.substring(4);
    return <i className={cssClass + (className ? ' ' + className : '')} />;
  }
  if (encoded.startsWith('colored:')) {
    const cssClass = encoded.substring(8);
    return <i className={cssClass + (className ? ' ' + className : '')} />;
  }
  if (encoded.startsWith('/') || encoded.startsWith('theme:')) {
    return <img src={encoded} alt="" className={className} style={{ width: '1em', height: '1em' }} />;
  }
  // Fallback: try as CSS class
  return <i className={encoded + (className ? ' ' + className : '')} />;
}

const IconSelectPopup: React.FC<IconSelectPopupProps> = ({
  anchorRef,
  currentValue,
  icons,
  iconsLoaded,
  onSelect,
  onCancel,
  onLoadIcons,
}) => {
  const i18n = useI18N(I18N_KEYS);
  const [tab, setTab] = useState<Tab>('simple');
  const [searchTerm, setSearchTerm] = useState('');
  const [advancedInput, setAdvancedInput] = useState(currentValue ?? '');
  const [loadError, setLoadError] = useState(false);
  const [position, setPosition] = useState<{ top: number; left: number } | null>(null);
  const popupRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);

  // Position popup relative to anchor
  useLayoutEffect(() => {
    if (!anchorRef.current || !popupRef.current) return;
    const anchorRect = anchorRef.current.getBoundingClientRect();
    const popupRect = popupRef.current.getBoundingClientRect();
    let top = anchorRect.bottom + 4;
    let left = anchorRect.left;
    if (top + popupRect.height > window.innerHeight) {
      top = anchorRect.top - popupRect.height - 4;
    }
    if (left + popupRect.width > window.innerWidth) {
      left = Math.max(0, anchorRect.right - popupRect.width);
    }
    setPosition({ top, left });
  }, [anchorRef]);

  // Load icons if not loaded
  useEffect(() => {
    if (!iconsLoaded && !loadError) {
      onLoadIcons().catch(() => setLoadError(true));
    }
  }, [iconsLoaded, loadError, onLoadIcons]);

  // Focus search on open
  useEffect(() => {
    if (iconsLoaded && searchRef.current) {
      searchRef.current.focus();
    }
  }, [iconsLoaded]);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel();
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [onCancel]);

  // Close on click outside
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (popupRef.current && !popupRef.current.contains(e.target as Node)) {
        onCancel();
      }
    };
    const timer = setTimeout(() => document.addEventListener('mousedown', handler), 0);
    return () => {
      clearTimeout(timer);
      document.removeEventListener('mousedown', handler);
    };
  }, [onCancel]);

  // Filter icons by search term
  const filteredIcons = useMemo(() => {
    if (!searchTerm) return icons;
    const lower = searchTerm.toLowerCase();
    return icons.filter(
      (icon) =>
        icon.prefix.toLowerCase().includes(lower) ||
        icon.label.toLowerCase().includes(lower)
    );
  }, [icons, searchTerm]);

  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  }, []);

  const handleSimpleSelect = useCallback(
    (encoded: string) => {
      onSelect(encoded);
    },
    [onSelect]
  );

  const handleAdvancedIconClick = useCallback((encoded: string) => {
    setAdvancedInput(encoded);
  }, []);

  const handleAdvancedInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setAdvancedInput(e.target.value);
  }, []);

  const handleOk = useCallback(() => {
    onSelect(advancedInput || null);
  }, [advancedInput, onSelect]);

  const handleClear = useCallback(() => {
    onSelect(null);
  }, [onSelect]);

  const handleRetry = useCallback(async () => {
    setLoadError(false);
    try {
      await onLoadIcons();
    } catch {
      setLoadError(true);
    }
  }, [onLoadIcons]);

  return (
    <div
      className="tlIconSelect__popup"
      ref={popupRef}
      style={
        position
          ? { top: position.top, left: position.left, visibility: 'visible' }
          : { visibility: 'hidden' }
      }
    >
      {/* Tab bar */}
      <div className="tlIconSelect__tabs">
        <button
          className={
            'tlIconSelect__tab' + (tab === 'simple' ? ' tlIconSelect__tab--active' : '')
          }
          onClick={() => setTab('simple')}
        >
          {i18n['js.iconSelect.simpleTab']}
        </button>
        <button
          className={
            'tlIconSelect__tab' + (tab === 'advanced' ? ' tlIconSelect__tab--active' : '')
          }
          onClick={() => setTab('advanced')}
        >
          {i18n['js.iconSelect.advancedTab']}
        </button>
      </div>

      {/* Search */}
      <div className="tlIconSelect__searchWrapper">
        <span className="tlIconSelect__searchIcon" aria-hidden="true">
          <i className="fa-solid fa-magnifying-glass" />
        </span>
        <input
          ref={searchRef}
          type="text"
          className="tlIconSelect__search"
          value={searchTerm}
          onChange={handleSearchChange}
          placeholder={i18n['js.iconSelect.filterPlaceholder']}
          aria-label={i18n['js.iconSelect.filterPlaceholder']}
        />
        {currentValue && (
          <button
            className="tlIconSelect__resetBtn"
            onClick={handleClear}
            title={i18n['js.iconSelect.clear']}
          >
            &times;
          </button>
        )}
      </div>

      {/* Icon grid */}
      <div
        className="tlIconSelect__grid"
        style={tab === 'advanced' ? { maxHeight: '160px' } : undefined}
      >
        {!iconsLoaded && !loadError && (
          <div className="tlIconSelect__loading">
            <span className="tlIconSelect__spinner" />
          </div>
        )}
        {loadError && (
          <div className="tlIconSelect__noResults">
            <a href="#" onClick={handleRetry}>
              {i18n['js.iconSelect.loading']}
            </a>
          </div>
        )}
        {iconsLoaded && filteredIcons.length === 0 && (
          <div className="tlIconSelect__noResults">{i18n['js.iconSelect.noResults']}</div>
        )}
        {iconsLoaded &&
          filteredIcons.map((icon) =>
            icon.variants.map((variant) => (
              <div
                key={variant.encoded}
                className={
                  'tlIconSelect__iconCell' +
                  (variant.encoded === currentValue
                    ? ' tlIconSelect__iconCell--selected'
                    : '')
                }
                title={icon.label}
                onClick={() =>
                  tab === 'simple'
                    ? handleSimpleSelect(variant.encoded)
                    : handleAdvancedIconClick(variant.encoded)
                }
              >
                <IconPreview encoded={variant.encoded} />
              </div>
            ))
          )}
      </div>

      {/* Advanced: edit area */}
      {tab === 'advanced' && (
        <div className="tlIconSelect__advancedArea">
          <div className="tlIconSelect__editRow">
            <span className="tlIconSelect__editLabel">
              {i18n['js.iconSelect.classLabel']}
            </span>
            <input
              className="tlIconSelect__editInput"
              type="text"
              value={advancedInput}
              onChange={handleAdvancedInputChange}
            />
          </div>
          <div className="tlIconSelect__previewArea">
            <span className="tlIconSelect__editLabel">
              {i18n['js.iconSelect.previewLabel']}
            </span>
            <div className="tlIconSelect__previewIcon">
              {advancedInput && <IconPreview encoded={advancedInput} />}
            </div>
            <span className="tlIconSelect__previewLabel">
              {advancedInput
                ? advancedInput.startsWith('css:')
                  ? advancedInput.substring(4)
                  : advancedInput
                : ''}
            </span>
          </div>
        </div>
      )}

      {/* Advanced: action buttons */}
      {tab === 'advanced' && (
        <div className="tlIconSelect__actions">
          <button className="tlIconSelect__btn tlIconSelect__btn--cancel" onClick={onCancel}>
            {i18n['js.iconSelect.cancel']}
          </button>
          <button className="tlIconSelect__btn tlIconSelect__btn--ok" onClick={handleOk}>
            {i18n['js.iconSelect.ok']}
          </button>
        </div>
      )}
    </div>
  );
};

export default IconSelectPopup;
export { IconPreview };
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/icon/IconSelectPopup.tsx
git commit -m "Ticket #29108: Add IconSelectPopup component for icon picker popup."
```

---

### Task 3: Create TLIconSelect main component

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLIconSelect.tsx`

**Reference files (read before implementing):**
- `com.top_logic.layout.react/react-src/controls/TLColorInput.tsx` - swatch trigger + popup pattern
- `com.top_logic.layout.react/react-src/controls/icon/IconSelectPopup.tsx` - the popup (created in Task 2)

- [ ] **Step 1: Create TLIconSelect.tsx**

The main component renders a swatch button showing the current icon. Clicking opens the `IconSelectPopup`. Similar pattern to `TLColorInput`.

```typescript
import { React, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import IconSelectPopup, { IconPreview } from './icon/IconSelectPopup';
import type { IconEntry } from './icon/IconSelectPopup';

const I18N_KEYS = { 'js.iconSelect.chooseIcon': 'Choose icon' };

const { useState, useCallback, useRef } = React;

/**
 * An icon select field rendered via React.
 *
 * State from server:
 *  - value: string | null     - Encoded ThemeImage (e.g. "css:fa-solid fa-home")
 *  - editable: boolean        - Whether the field is editable
 *  - icons: IconEntry[]       - Icon metadata (populated on loadIcons)
 *  - iconsLoaded: boolean     - Whether icons have been loaded
 */
const TLIconSelect: React.FC<TLCellProps> = ({ controlId, state }) => {
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const [open, setOpen] = useState(false);
  const swatchRef = useRef<HTMLButtonElement>(null);

  const value = state.value as string | null;
  const editable = state.editable !== false;
  const disabled = state.disabled === true;
  const icons = (state.icons as IconEntry[]) ?? [];
  const iconsLoaded = state.iconsLoaded === true;

  const handleClick = useCallback(() => {
    if (editable && !disabled) setOpen(true);
  }, [editable, disabled]);

  const handleSelect = useCallback(
    (encoded: string | null) => {
      setOpen(false);
      sendCommand('valueChanged', { value: encoded });
    },
    [sendCommand]
  );

  const handleCancel = useCallback(() => {
    setOpen(false);
  }, []);

  const handleLoadIcons = useCallback(async () => {
    await sendCommand('loadIcons');
  }, [sendCommand]);

  // Immutable rendering
  if (!editable) {
    return (
      <span id={controlId} className="tlIconSelect tlIconSelect--immutable">
        <span className="tlIconSelect__swatch">
          {value ? <IconPreview encoded={value} /> : null}
        </span>
      </span>
    );
  }

  return (
    <span id={controlId} className="tlIconSelect">
      <button
        ref={swatchRef}
        className={
          'tlIconSelect__swatch' + (value == null ? ' tlIconSelect__swatch--empty' : '')
        }
        onClick={handleClick}
        disabled={disabled}
        title={value ?? ''}
        aria-label={i18n['js.iconSelect.chooseIcon']}
      >
        {value ? (
          <IconPreview encoded={value} />
        ) : (
          <i className="fa-solid fa-icons" />
        )}
      </button>

      {open && (
        <IconSelectPopup
          anchorRef={swatchRef}
          currentValue={value}
          icons={icons}
          iconsLoaded={iconsLoaded}
          onSelect={handleSelect}
          onCancel={handleCancel}
          onLoadIcons={handleLoadIcons}
        />
      )}
    </span>
  );
};

export default TLIconSelect;
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLIconSelect.tsx
git commit -m "Ticket #29108: Add TLIconSelect main React component."
```

---

### Task 4: Register TLIconSelect in controls-entry.ts

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

- [ ] **Step 1: Add import and registration**

Add after the `TLColorInput` import (line 53):
```typescript
import TLIconSelect from './controls/TLIconSelect';
```

Add after the `TLColorInput` registration (line 96):
```typescript
register('TLIconSelect', TLIconSelect);
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls-entry.ts
git commit -m "Ticket #29108: Register TLIconSelect in controls entry."
```

---

## Chunk 3: Styling

### Task 5: Add CSS styles for TLIconSelect

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` (append after line 3113)

**Reference:** `docs/prototypes/icon-select-prototype.html` for the visual design.

- [ ] **Step 1: Append CSS**

Append the following CSS at the end of `tlReactControls.css`:

```css

/* --- TLIconSelect ------------------------------------------------------ */

.tlIconSelect {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tlIconSelect__swatch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--corner-radius);
  background: var(--layer-02, #fff);
  cursor: pointer;
  font-size: 18px;
  color: var(--text-primary);
  transition: box-shadow 0.15s, border-color 0.15s;
  padding: 0;
}

.tlIconSelect__swatch:hover {
  box-shadow: 0 0 0 2px var(--focus);
  border-color: var(--focus);
}

.tlIconSelect__swatch:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: 2px;
}

.tlIconSelect__swatch--empty {
  color: var(--text-placeholder, #a8a8a8);
  font-size: 14px;
}

.tlIconSelect__swatch:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.tlIconSelect__swatch:disabled:hover {
  box-shadow: none;
  border-color: var(--border-subtle);
}

.tlIconSelect--immutable .tlIconSelect__swatch {
  cursor: default;
  border-color: transparent;
  background: transparent;
}

.tlIconSelect--immutable .tlIconSelect__swatch:hover {
  box-shadow: none;
}

/* Popup */

.tlIconSelect__popup {
  position: fixed;
  z-index: 9000;
  background: var(--layer-02, #fff);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  width: 420px;
  overflow: hidden;
}

.tlIconSelect__tabs {
  display: flex;
  border-bottom: 1px solid var(--border-subtle);
  background: var(--layer-01, #f4f4f4);
}

.tlIconSelect__tab {
  flex: 1;
  padding: var(--spacing-03) var(--spacing-04);
  border: none;
  border-bottom: 2px solid transparent;
  background: none;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}

.tlIconSelect__tab:hover {
  color: var(--text-primary);
  background: var(--layer-hover);
}

.tlIconSelect__tab--active {
  color: var(--text-primary);
  font-weight: 600;
  border-bottom-color: var(--focus);
}

/* Search */

.tlIconSelect__searchWrapper {
  display: flex;
  align-items: center;
  padding: var(--spacing-03);
  border-bottom: 1px solid var(--border-subtle);
  gap: var(--spacing-03);
}

.tlIconSelect__searchIcon {
  color: var(--text-placeholder, #a8a8a8);
  font-size: 14px;
  flex-shrink: 0;
}

.tlIconSelect__search {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
  color: var(--text-primary);
}

.tlIconSelect__search::placeholder {
  color: var(--text-placeholder, #a8a8a8);
}

.tlIconSelect__resetBtn {
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 16px;
  padding: 2px 4px;
  border-radius: 3px;
  line-height: 1;
}

.tlIconSelect__resetBtn:hover {
  color: var(--support-error, #da1e28);
  background: var(--layer-hover);
}

/* Icon grid */

.tlIconSelect__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 2px;
  padding: var(--spacing-03);
  max-height: 260px;
  overflow-y: auto;
}

.tlIconSelect__iconCell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--corner-radius);
  cursor: pointer;
  font-size: 18px;
  color: var(--text-primary);
  transition: background 0.1s;
}

.tlIconSelect__iconCell:hover {
  background: var(--layer-hover);
}

.tlIconSelect__iconCell--selected {
  background: #d0e2ff;
  outline: 2px solid var(--focus);
  outline-offset: -2px;
}

.tlIconSelect__noResults {
  padding: 24px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
  grid-column: 1 / -1;
}

.tlIconSelect__loading {
  padding: 32px;
  text-align: center;
  grid-column: 1 / -1;
}

.tlIconSelect__spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid var(--border-subtle);
  border-top-color: var(--focus);
  border-radius: 50%;
  animation: tlIconSelectSpin 0.8s linear infinite;
}

@keyframes tlIconSelectSpin {
  to { transform: rotate(360deg); }
}

/* Advanced tab: edit area */

.tlIconSelect__advancedArea {
  padding: var(--spacing-03);
  border-top: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-03);
}

.tlIconSelect__editRow {
  display: flex;
  align-items: center;
  gap: var(--spacing-03);
}

.tlIconSelect__editLabel {
  font-size: var(--label-01-font-size, 12px);
  color: var(--text-secondary);
  flex-shrink: 0;
  min-width: 50px;
}

.tlIconSelect__editInput {
  flex: 1;
  min-width: 0;
  height: 28px;
  padding: 0 var(--spacing-03);
  border: 1px solid var(--border-subtle);
  border-radius: var(--corner-radius);
  font-size: 13px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  color: var(--text-primary);
  background: var(--layer-02, #fff);
  transition: border-color 0.15s;
}

.tlIconSelect__editInput:focus {
  border-color: var(--focus);
  box-shadow: 0 0 0 1px var(--focus);
  outline: none;
}

.tlIconSelect__previewArea {
  display: flex;
  align-items: center;
  gap: var(--spacing-04, 12px);
}

.tlIconSelect__previewIcon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--corner-radius);
  font-size: 24px;
  background: var(--layer-01, #f4f4f4);
}

.tlIconSelect__previewLabel {
  font-size: var(--label-01-font-size, 12px);
  color: var(--text-secondary);
}

/* Actions */

.tlIconSelect__actions {
  display: flex;
  gap: var(--spacing-03);
  justify-content: flex-end;
  padding: var(--spacing-03) var(--spacing-04, 12px) var(--spacing-04, 12px);
  border-top: 1px solid var(--border-subtle);
}

.tlIconSelect__btn {
  height: 28px;
  padding: 0 var(--spacing-04, 12px);
  border: none;
  border-radius: var(--corner-radius);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
}

.tlIconSelect__btn--ok {
  background: var(--button-primary, #0f62fe);
  color: var(--text-on-color, #fff);
}

.tlIconSelect__btn--ok:hover {
  background: var(--button-primary-hover, #0353e9);
}

.tlIconSelect__btn--cancel {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-subtle);
}

.tlIconSelect__btn--cancel:hover {
  background: var(--layer-hover);
}
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css
git commit -m "Ticket #29108: Add CSS styles for TLIconSelect control."
```

---

## Chunk 4: Build Verification

### Task 6: Build the React module and verify

- [ ] **Step 1: Run npm build to verify TypeScript compilation**

```bash
cd com.top_logic.layout.react && npm run build
```
Expected: No TypeScript errors. Output bundles generated in `src/main/webapp/script/`.

- [ ] **Step 2: Run Maven build for the full module**

```bash
cd com.top_logic.layout.react && mvn clean install -DskipTests=true
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Verify all files are in place**

Check that the built JAR contains the new Java class and the React bundle includes the new component:
```bash
jar tf target/tl-layout-react-*.jar | grep -i iconselect
grep TLIconSelect src/main/webapp/script/tl-react-controls.js
```
Expected: `ReactIconSelectControl.class` in JAR, `TLIconSelect` referenced in JS bundle.

- [ ] **Step 4: Commit any build-generated changes (if any)**

If `npm run build` regenerated bundle files that are tracked, commit them.
