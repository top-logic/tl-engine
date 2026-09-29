# React Color Input Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a React-based color chooser control (`TLColorInput`) that matches or exceeds the features of the existing non-React `ColorChooserControl` + `ColorChooserSelectionControl`.

**Architecture:** A `ViewColorInputControl` (Java, extends `ReactControl`) holds the current hex color value and the user's personalized palette. On the client, a `TLColorInput` React component renders a color swatch button that opens a client-side popup with two tabs: a color palette grid and an HSV color mixer. The popup is purely client-side (no server round-trip to open). Color selection sends a `valueChanged` command; palette changes send a `paletteChanged` command for server-side persistence. CSS gradients replace the old PNG image dependencies for the HSV mixer.

**Tech Stack:** Java 17, TopLogic `ReactControl` (SSE-based), React (via `tl-react-bridge`), TypeScript, CSS custom properties (design tokens)

**Design document:** See visual prototype at `/tmp/color-control-prototype.html` and the design discussion in the conversation that created this plan.

**Existing code reference:**
- Old control: `com.top_logic/src/main/java/com/top_logic/layout/form/control/ColorChooserControl.java`
- Old selection dialog: `com.top_logic/src/main/java/com/top_logic/layout/form/control/ColorChooserSelectionControl.java`
- Old JS: `com.top_logic/src/main/webapp/script/tl/color.js`
- Pattern to follow: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewTextInputControl.java`

---

## File Structure

### Java (server-side)

| File | Responsibility |
|------|---------------|
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewColorInputControl.java` | **New.** Lean React control for color values. Holds `value` (hex string), `editable`, `palette` (list of hex strings), `paletteColumns`. Handles `valueChanged` and `paletteChanged` commands. Loads/persists palette from `PersonalConfiguration`. |

### TypeScript (client-side)

| File | Responsibility |
|------|---------------|
| `com.top_logic.layout.react/react-src/controls/TLColorInput.tsx` | **New.** Main React component. Renders swatch button, manages popup open/close state, orchestrates sub-components. |
| `com.top_logic.layout.react/react-src/controls/color/ColorPopup.tsx` | **New.** The popup overlay: tab bar, palette tab content, mixer tab content, control panel. |
| `com.top_logic.layout.react/react-src/controls/color/ColorPalette.tsx` | **New.** Grid of palette cells with drag-and-drop swap. |
| `com.top_logic.layout.react/react-src/controls/color/ColorMixer.tsx` | **New.** HSV picker: 2D saturation/value field + vertical hue slider with pointer-event-based drag. |
| `com.top_logic.layout.react/react-src/controls/color/colorUtils.ts` | **New.** Pure functions: RGB<->HSV, RGB<->Hex, validation. |

### CSS

| File | Responsibility |
|------|---------------|
| `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` | **Modify.** Append `TLColorInput` styles at end of file (after line 2451). |

### Registration / Integration

| File | Responsibility |
|------|---------------|
| `com.top_logic.layout.react/react-src/controls-entry.ts` | **Modify.** Add import and `register('TLColorInput', TLColorInput)`. |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java` | **Modify.** Add `CUSTOM` kind branch to create `ViewColorInputControl` for `java.awt.Color` typed attributes. |
| `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java` | **Modify.** Add `ViewColorInputControl` branches in `setupValueCallback()` and `updateInnerControl()`. |

---

## Task 1: Color Utility Functions (TypeScript)

Create the pure color conversion utilities. These have no dependencies and can be tested independently.

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/color/colorUtils.ts`

- [ ] **Step 1: Create `colorUtils.ts` with all conversion functions**

```typescript
/**
 * Pure color-conversion and validation utilities for the TLColorInput control.
 *
 * Color spaces:
 *  - RGB: r, g, b in [0, 255]
 *  - HSV: h in [0, 360), s in [0, 1], v in [0, 1]
 *  - Hex: '#RRGGBB' string
 */

/** Clamps a number to the 0-255 byte range. */
export function clampByte(n: number): number {
  return Math.max(0, Math.min(255, Math.round(n)));
}

/** Returns true if `hex` is a valid 7-char hex color like '#ab12ef'. */
export function isValidHex(hex: string): boolean {
  return /^#[0-9a-fA-F]{6}$/.test(hex);
}

/**
 * Parses a '#RRGGBB' string into [r, g, b] integers.
 * Returns [0,0,0] for invalid input.
 */
export function hexToRgb(hex: string): [number, number, number] {
  if (!isValidHex(hex)) return [0, 0, 0];
  const n = parseInt(hex.slice(1), 16);
  return [(n >> 16) & 0xff, (n >> 8) & 0xff, n & 0xff];
}

/** Formats [r, g, b] integers into a '#RRGGBB' string (uppercase). */
export function rgbToHex(r: number, g: number, b: number): string {
  const toHex = (v: number) => clampByte(v).toString(16).padStart(2, '0');
  return '#' + toHex(r) + toHex(g) + toHex(b);
}

/**
 * Converts RGB to HSV.
 *
 * @returns [h, s, v] where h in [0, 360), s in [0, 1], v in [0, 1].
 */
export function rgbToHsv(r: number, g: number, b: number): [number, number, number] {
  const rn = r / 255;
  const gn = g / 255;
  const bn = b / 255;
  const max = Math.max(rn, gn, bn);
  const min = Math.min(rn, gn, bn);
  const delta = max - min;

  let h = 0;
  if (delta !== 0) {
    if (max === rn) {
      h = ((gn - bn) / delta) % 6;
    } else if (max === gn) {
      h = (bn - rn) / delta + 2;
    } else {
      h = (rn - gn) / delta + 4;
    }
    h *= 60;
    if (h < 0) h += 360;
  }

  const s = max === 0 ? 0 : delta / max;
  return [h, s, max];
}

/**
 * Converts HSV to RGB.
 *
 * @param h Hue in [0, 360)
 * @param s Saturation in [0, 1]
 * @param v Value in [0, 1]
 * @returns [r, g, b] integers in [0, 255].
 */
export function hsvToRgb(h: number, s: number, v: number): [number, number, number] {
  const c = v * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = v - c;

  let rn = 0, gn = 0, bn = 0;
  if (h < 60)       { rn = c; gn = x; bn = 0; }
  else if (h < 120) { rn = x; gn = c; bn = 0; }
  else if (h < 180) { rn = 0; gn = c; bn = x; }
  else if (h < 240) { rn = 0; gn = x; bn = c; }
  else if (h < 300) { rn = x; gn = 0; bn = c; }
  else              { rn = c; gn = 0; bn = x; }

  return [
    Math.round((rn + m) * 255),
    Math.round((gn + m) * 255),
    Math.round((bn + m) * 255),
  ];
}

/** Converts a hex color to HSV. Convenience wrapper. */
export function hexToHsv(hex: string): [number, number, number] {
  return rgbToHsv(...hexToRgb(hex));
}

/** Converts HSV to a hex color. Convenience wrapper. */
export function hsvToHex(h: number, s: number, v: number): string {
  return rgbToHex(...hsvToRgb(h, s, v));
}
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add color conversion utilities for React color input.
```

---

## Task 2: ColorMixer Sub-Component

The HSV color mixer with 2D saturation/value field and vertical hue slider.

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/color/ColorMixer.tsx`

- [ ] **Step 1: Create `ColorMixer.tsx`**

```tsx
import { React } from 'tl-react-bridge';
import { hsvToHex, hexToHsv } from './colorUtils';

const { useState, useCallback, useRef, useEffect } = React;

interface ColorMixerProps {
  /** Current color as '#RRGGBB'. */
  color: string;
  /** Called when the user changes the color by dragging. */
  onColorChange: (hex: string) => void;
}

/**
 * HSV color mixer with a 2D saturation/value field and a vertical hue slider.
 *
 * The SV field uses CSS gradients (no image files):
 *  - Background: pure hue color
 *  - Overlay 1: linear-gradient(to right, white, transparent) for saturation
 *  - Overlay 2: linear-gradient(to bottom, transparent, black) for value
 *
 * The hue slider uses a CSS rainbow gradient.
 * All drag interactions use the Pointer Events API.
 */
const ColorMixer: React.FC<ColorMixerProps> = ({ color, onColorChange }) => {
  const [h, s, v] = hexToHsv(color);

  const svRef = useRef<HTMLDivElement>(null);
  const hueRef = useRef<HTMLDivElement>(null);

  // --- SV field drag ---
  const handleSVPointer = useCallback(
    (clientX: number, clientY: number) => {
      const rect = svRef.current?.getBoundingClientRect();
      if (!rect) return;
      const newS = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width));
      const newV = Math.max(0, Math.min(1, 1 - (clientY - rect.top) / rect.height));
      onColorChange(hsvToHex(h, newS, newV));
    },
    [h, onColorChange]
  );

  const onSVDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      (e.target as HTMLElement).setPointerCapture(e.pointerId);
      handleSVPointer(e.clientX, e.clientY);
    },
    [handleSVPointer]
  );

  const onSVMove = useCallback(
    (e: React.PointerEvent) => {
      if (e.buttons === 0) return;
      handleSVPointer(e.clientX, e.clientY);
    },
    [handleSVPointer]
  );

  // --- Hue slider drag ---
  const handleHuePointer = useCallback(
    (clientY: number) => {
      const rect = hueRef.current?.getBoundingClientRect();
      if (!rect) return;
      const ratio = Math.max(0, Math.min(1, (clientY - rect.top) / rect.height));
      const newH = ratio * 360;
      onColorChange(hsvToHex(newH, s, v));
    },
    [s, v, onColorChange]
  );

  const onHueDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      (e.target as HTMLElement).setPointerCapture(e.pointerId);
      handleHuePointer(e.clientY);
    },
    [handleHuePointer]
  );

  const onHueMove = useCallback(
    (e: React.PointerEvent) => {
      if (e.buttons === 0) return;
      handleHuePointer(e.clientY);
    },
    [handleHuePointer]
  );

  const pureHue = hsvToHex(h, 1, 1);

  return (
    <div className="tlColorInput__mixer">
      {/* SV field */}
      <div
        ref={svRef}
        className="tlColorInput__svField"
        style={{ backgroundColor: pureHue }}
        onPointerDown={onSVDown}
        onPointerMove={onSVMove}
      >
        <div
          className="tlColorInput__svHandle"
          style={{ left: `${s * 100}%`, top: `${(1 - v) * 100}%` }}
        />
      </div>

      {/* Hue slider */}
      <div
        ref={hueRef}
        className="tlColorInput__hueSlider"
        onPointerDown={onHueDown}
        onPointerMove={onHueMove}
      >
        <div
          className="tlColorInput__hueHandle"
          style={{ top: `${(h / 360) * 100}%` }}
        />
      </div>
    </div>
  );
};

export default ColorMixer;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add ColorMixer sub-component with HSV picker.
```

---

## Task 3: ColorPalette Sub-Component

The grid of predefined color cells with drag-and-drop swap and click/double-click selection.

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/color/ColorPalette.tsx`

- [ ] **Step 1: Create `ColorPalette.tsx`**

```tsx
import { React } from 'tl-react-bridge';

const { useCallback, useRef } = React;

interface ColorPaletteProps {
  /** Flat array of hex color strings (row-major). null = empty slot. */
  colors: (string | null)[];
  /** Number of columns in the grid. */
  columns: number;
  /** Called when a color cell is clicked. */
  onSelect: (hex: string) => void;
  /** Called when a color cell is double-clicked (immediate confirm). */
  onConfirm: (hex: string) => void;
  /** Called when two cells are swapped via drag-and-drop. */
  onSwap: (fromIndex: number, toIndex: number) => void;
}

/**
 * A grid of color cells. Supports:
 * - Click to preview a color
 * - Double-click to confirm immediately
 * - Drag-and-drop between cells to swap their colors
 */
const ColorPalette: React.FC<ColorPaletteProps> = ({
  colors,
  columns,
  onSelect,
  onConfirm,
  onSwap,
}) => {
  const dragIndex = useRef<number | null>(null);

  const handleDragStart = useCallback(
    (index: number) => (e: React.DragEvent) => {
      dragIndex.current = index;
      e.dataTransfer.effectAllowed = 'move';
    },
    []
  );

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  }, []);

  const handleDrop = useCallback(
    (index: number) => (e: React.DragEvent) => {
      e.preventDefault();
      if (dragIndex.current !== null && dragIndex.current !== index) {
        onSwap(dragIndex.current, index);
      }
      dragIndex.current = null;
    },
    [onSwap]
  );

  return (
    <div
      className="tlColorInput__palette"
      style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}
    >
      {colors.map((hex, i) => (
        <div
          key={i}
          className={
            'tlColorInput__paletteCell' +
            (hex == null ? ' tlColorInput__paletteCell--empty' : '')
          }
          style={hex != null ? { backgroundColor: hex } : undefined}
          title={hex ?? ''}
          draggable={hex != null}
          onClick={hex != null ? () => onSelect(hex) : undefined}
          onDoubleClick={hex != null ? () => onConfirm(hex) : undefined}
          onDragStart={hex != null ? handleDragStart(i) : undefined}
          onDragOver={handleDragOver}
          onDrop={handleDrop(i)}
        />
      ))}
    </div>
  );
};

export default ColorPalette;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add ColorPalette sub-component with drag-and-drop.
```

---

## Task 4: ColorPopup Sub-Component

The popup overlay with tabs (Palette / Mixer), control panel (preview, RGB/hex inputs, OK/Cancel).

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/color/ColorPopup.tsx`

- [ ] **Step 1: Create `ColorPopup.tsx`**

```tsx
import { React } from 'tl-react-bridge';
import ColorPalette from './ColorPalette';
import ColorMixer from './ColorMixer';
import { hexToRgb, rgbToHex, isValidHex, clampByte } from './colorUtils';

const { useState, useCallback, useEffect, useRef } = React;

interface ColorPopupProps {
  /** The confirmed (original) color. */
  currentColor: string;
  /** Palette colors (flat array, row-major). */
  palette: (string | null)[];
  /** Number of palette columns. */
  paletteColumns: number;
  /** Default palette for reset. */
  defaultPalette: (string | null)[];
  /** Called when user confirms a color (OK or double-click). */
  onConfirm: (hex: string) => void;
  /** Called when user cancels. */
  onCancel: () => void;
  /** Called when palette changes (swap or reset). */
  onPaletteChange: (newPalette: (string | null)[]) => void;
}

type Tab = 'palette' | 'mixer';

/**
 * The color chooser popup with two tabs and a control panel.
 */
const ColorPopup: React.FC<ColorPopupProps> = ({
  currentColor,
  palette,
  paletteColumns,
  defaultPalette,
  onConfirm,
  onCancel,
  onPaletteChange,
}) => {
  const [tab, setTab] = useState<Tab>('palette');
  const [draft, setDraft] = useState(currentColor);
  const popupRef = useRef<HTMLDivElement>(null);

  // RGB derived from draft
  const [r, g, b] = hexToRgb(draft);
  const [hexInput, setHexInput] = useState(draft.toUpperCase());

  // Keep hex input in sync when draft changes via non-hex-field means
  useEffect(() => {
    setHexInput(draft.toUpperCase());
  }, [draft]);

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
    // Delay to avoid catching the opening click
    const timer = setTimeout(() => document.addEventListener('mousedown', handler), 0);
    return () => {
      clearTimeout(timer);
      document.removeEventListener('mousedown', handler);
    };
  }, [onCancel]);

  const handleRgbChange = useCallback(
    (channel: 'r' | 'g' | 'b') => (e: React.ChangeEvent<HTMLInputElement>) => {
      const val = parseInt(e.target.value, 10);
      if (isNaN(val)) return;
      const clamped = clampByte(val);
      const newR = channel === 'r' ? clamped : r;
      const newG = channel === 'g' ? clamped : g;
      const newB = channel === 'b' ? clamped : b;
      setDraft(rgbToHex(newR, newG, newB));
    },
    [r, g, b]
  );

  const handleHexChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setHexInput(val);
    if (isValidHex(val)) {
      setDraft(val);
    }
  }, []);

  const handlePaletteSelect = useCallback((hex: string) => {
    setDraft(hex);
  }, []);

  const handlePaletteConfirm = useCallback(
    (hex: string) => {
      onConfirm(hex);
    },
    [onConfirm]
  );

  const handleSwap = useCallback(
    (from: number, to: number) => {
      const newPalette = [...palette];
      const temp = newPalette[from];
      newPalette[from] = newPalette[to];
      newPalette[to] = temp;
      onPaletteChange(newPalette);
    },
    [palette, onPaletteChange]
  );

  const handleReset = useCallback(() => {
    onPaletteChange([...defaultPalette]);
  }, [defaultPalette, onPaletteChange]);

  const handleOk = useCallback(() => {
    onConfirm(draft);
  }, [draft, onConfirm]);

  return (
    <div className="tlColorInput__popup" ref={popupRef}>
      {/* Tab bar */}
      <div className="tlColorInput__tabs">
        <button
          className={'tlColorInput__tab' + (tab === 'palette' ? ' tlColorInput__tab--active' : '')}
          onClick={() => setTab('palette')}
        >
          Color Palette
        </button>
        <button
          className={'tlColorInput__tab' + (tab === 'mixer' ? ' tlColorInput__tab--active' : '')}
          onClick={() => setTab('mixer')}
        >
          Color Mixer
        </button>
      </div>

      {/* Body: left = tab content, right = control panel */}
      <div className="tlColorInput__body">
        {/* Left: tab content */}
        {tab === 'palette' ? (
          <ColorPalette
            colors={palette}
            columns={paletteColumns}
            onSelect={handlePaletteSelect}
            onConfirm={handlePaletteConfirm}
            onSwap={handleSwap}
          />
        ) : (
          <ColorMixer color={draft} onColorChange={setDraft} />
        )}

        {/* Right: control panel */}
        <div className="tlColorInput__controls">
          <div className="tlColorInput__previewRow">
            <span className="tlColorInput__previewLabel">Current</span>
            <div className="tlColorInput__previewSwatch" style={{ backgroundColor: currentColor }} />
          </div>
          <div className="tlColorInput__previewRow">
            <span className="tlColorInput__previewLabel">New</span>
            <div className="tlColorInput__previewSwatch" style={{ backgroundColor: draft }} />
          </div>

          <div className="tlColorInput__divider" />

          <div className="tlColorInput__inputRow">
            <span className="tlColorInput__inputLabel">Red</span>
            <input
              className="tlColorInput__input"
              type="number"
              min={0}
              max={255}
              value={r}
              onChange={handleRgbChange('r')}
            />
          </div>
          <div className="tlColorInput__inputRow">
            <span className="tlColorInput__inputLabel">Green</span>
            <input
              className="tlColorInput__input"
              type="number"
              min={0}
              max={255}
              value={g}
              onChange={handleRgbChange('g')}
            />
          </div>
          <div className="tlColorInput__inputRow">
            <span className="tlColorInput__inputLabel">Blue</span>
            <input
              className="tlColorInput__input"
              type="number"
              min={0}
              max={255}
              value={b}
              onChange={handleRgbChange('b')}
            />
          </div>
          <div className="tlColorInput__inputRow">
            <span className="tlColorInput__inputLabel">Hex</span>
            <input
              className={'tlColorInput__input' + (!isValidHex(hexInput) ? ' tlColorInput__input--error' : '')}
              type="text"
              value={hexInput}
              onChange={handleHexChange}
            />
          </div>

          <div className="tlColorInput__actions">
            {tab === 'palette' && (
              <button className="tlColorInput__btn tlColorInput__btn--reset" onClick={handleReset}>
                Reset
              </button>
            )}
            <button className="tlColorInput__btn tlColorInput__btn--cancel" onClick={onCancel}>
              Cancel
            </button>
            <button className="tlColorInput__btn tlColorInput__btn--ok" onClick={handleOk}>
              OK
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ColorPopup;
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add ColorPopup sub-component with tabs and control panel.
```

---

## Task 5: TLColorInput Main Component

The top-level React component that renders the swatch button and manages popup lifecycle.

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLColorInput.tsx`
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts` (add import + register)

- [ ] **Step 1: Create `TLColorInput.tsx`**

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import ColorPopup from './color/ColorPopup';

const { useState, useCallback, useRef } = React;

/**
 * A color input field rendered via React.
 *
 * State from server:
 *  - value: string | null       - Current hex color ('#RRGGBB') or null
 *  - editable: boolean          - Whether the field is editable
 *  - palette: (string | null)[] - Palette colors (flat, row-major)
 *  - paletteColumns: number     - Number of palette columns
 *  - defaultPalette: (string | null)[] - Default palette for reset
 */
const TLColorInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const sendCommand = useTLCommand();
  const [open, setOpen] = useState(false);
  const swatchRef = useRef<HTMLButtonElement>(null);

  const value = state.value as string | null;
  const editable = state.editable !== false;
  const palette = (state.palette as (string | null)[]) ?? [];
  const paletteColumns = (state.paletteColumns as number) ?? 6;
  const defaultPalette = (state.defaultPalette as (string | null)[]) ?? palette;

  const handleClick = useCallback(() => {
    if (editable) setOpen(true);
  }, [editable]);

  const handleConfirm = useCallback(
    (hex: string) => {
      setOpen(false);
      sendCommand('valueChanged', { value: hex });
    },
    [sendCommand]
  );

  const handleCancel = useCallback(() => {
    setOpen(false);
  }, []);

  const handlePaletteChange = useCallback(
    (newPalette: (string | null)[]) => {
      sendCommand('paletteChanged', { palette: newPalette });
    },
    [sendCommand]
  );

  // Immutable mode: just a colored span
  if (!editable) {
    return (
      <span
        id={controlId}
        className={
          'tlColorInput tlColorInput--immutable' +
          (value == null ? ' tlColorInput--noColor' : '')
        }
        style={value != null ? { backgroundColor: value } : undefined}
        title={value ?? ''}
      />
    );
  }

  return (
    <span id={controlId} className="tlColorInput">
      <button
        ref={swatchRef}
        className={
          'tlColorInput__swatch' +
          (value == null ? ' tlColorInput__swatch--noColor' : '')
        }
        style={value != null ? { backgroundColor: value } : undefined}
        onClick={handleClick}
        disabled={state.disabled === true}
        title={value ?? ''}
        aria-label="Choose color"
      />

      {open && (
        <ColorPopup
          currentColor={value ?? '#000000'}
          palette={palette}
          paletteColumns={paletteColumns}
          defaultPalette={defaultPalette}
          onConfirm={handleConfirm}
          onCancel={handleCancel}
          onPaletteChange={handlePaletteChange}
        />
      )}
    </span>
  );
};

export default TLColorInput;
```

- [ ] **Step 2: Register in `controls-entry.ts`**

Add after the last existing import (around line 51):

```typescript
import TLColorInput from './controls/TLColorInput';
```

Add after the last existing `register(...)` call (around line 92):

```typescript
register('TLColorInput', TLColorInput);
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add TLColorInput React component with popup.
```

---

## Task 6: CSS Styles for TLColorInput

Append all color control styles to the existing CSS file.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` (append after line 2451)

- [ ] **Step 1: Append CSS**

Append the following CSS block at the end of `tlReactControls.css`:

```css

/* --- TLColorInput ------------------------------------------------------ */

.tlColorInput {
  display: inline-block;
  position: relative;
}

.tlColorInput--immutable {
  display: inline-block;
  width: 1.5rem;
  height: 1.5rem;
  border: 1px solid var(--border-subtle);
  border-radius: var(--corner-radius);
  cursor: default;
}

.tlColorInput--noColor {
  background-image:
    linear-gradient(45deg, #ccc 25%, transparent 25%),
    linear-gradient(-45deg, #ccc 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #ccc 75%),
    linear-gradient(-45deg, transparent 75%, #ccc 75%);
  background-size: 8px 8px;
  background-position: 0 0, 0 4px, 4px -4px, -4px 0;
}

.tlColorInput__swatch {
  width: 2rem;
  height: 2rem;
  padding: 0;
  border: 2px solid var(--border-strong);
  border-radius: var(--corner-radius);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.15s;
}

.tlColorInput__swatch:hover {
  box-shadow: 0 0 0 2px var(--focus);
}

.tlColorInput__swatch:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: 2px;
}

.tlColorInput__swatch:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  border-color: var(--border-subtle);
}

.tlColorInput__swatch:disabled:hover {
  box-shadow: none;
}

.tlColorInput__swatch--noColor {
  background-image:
    linear-gradient(45deg, #ccc 25%, transparent 25%),
    linear-gradient(-45deg, #ccc 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #ccc 75%),
    linear-gradient(-45deg, transparent 75%, #ccc 75%);
  background-size: 8px 8px;
  background-position: 0 0, 0 4px, 4px -4px, -4px 0;
}

.tlColorInput__swatch--noColor::after {
  content: '';
  position: absolute;
  top: -2px;
  left: 50%;
  width: 2px;
  height: calc(100% + 4px);
  background: var(--support-error, #da1e28);
  transform: rotate(45deg);
  transform-origin: center;
}

/* Popup */

.tlColorInput__popup {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  z-index: 9000;
  background: var(--layer-02, #fff);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15), 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 480px;
  overflow: hidden;
}

.tlColorInput__tabs {
  display: flex;
  border-bottom: 1px solid var(--border-subtle);
  background: var(--layer-01, #f4f4f4);
}

.tlColorInput__tab {
  flex: 1;
  padding: 0.625rem 1rem;
  border: none;
  background: transparent;
  font-family: var(--font-family);
  font-size: 0.875rem;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.15s, border-color 0.15s;
}

.tlColorInput__tab:hover {
  color: var(--text-primary);
  background: var(--layer-hover);
}

.tlColorInput__tab--active {
  color: var(--text-primary);
  font-weight: 600;
  border-bottom-color: var(--focus);
}

.tlColorInput__body {
  display: flex;
  padding: 1rem;
  gap: 1rem;
}

/* Palette */

.tlColorInput__palette {
  display: grid;
  gap: 4px;
  flex-shrink: 0;
}

.tlColorInput__paletteCell {
  width: 33px;
  height: 33px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 3px;
  cursor: pointer;
  transition: transform 0.1s, box-shadow 0.1s;
}

.tlColorInput__paletteCell:hover {
  transform: scale(1.15);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  z-index: 1;
  position: relative;
}

.tlColorInput__paletteCell--empty {
  background-image:
    linear-gradient(45deg, #eee 25%, transparent 25%),
    linear-gradient(-45deg, #eee 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #eee 75%),
    linear-gradient(-45deg, transparent 75%, #eee 75%);
  background-size: 6px 6px;
  background-position: 0 0, 0 3px, 3px -3px, -3px 0;
  border: 1px dashed var(--border-subtle);
  cursor: default;
}

.tlColorInput__paletteCell--empty:hover {
  transform: none;
  box-shadow: none;
}

/* Mixer */

.tlColorInput__mixer {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.tlColorInput__svField {
  width: 192px;
  height: 192px;
  position: relative;
  border-radius: 4px;
  cursor: crosshair;
  overflow: hidden;
}

.tlColorInput__svField::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, #fff, transparent);
}

.tlColorInput__svField::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent, #000);
}

.tlColorInput__svHandle {
  position: absolute;
  width: 14px;
  height: 14px;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.3), inset 0 0 0 1px rgba(0, 0, 0, 0.3);
  transform: translate(-50%, -50%);
  z-index: 2;
  pointer-events: none;
}

.tlColorInput__hueSlider {
  width: 24px;
  height: 192px;
  position: relative;
  border-radius: 4px;
  cursor: pointer;
  background: linear-gradient(to bottom,
    #ff0000, #ffff00, #00ff00, #00ffff, #0000ff, #ff00ff, #ff0000);
}

.tlColorInput__hueHandle {
  position: absolute;
  left: -3px;
  width: 30px;
  height: 8px;
  border: 2px solid #fff;
  border-radius: 3px;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.3);
  transform: translateY(-50%);
  pointer-events: none;
  background: transparent;
}

/* Control panel */

.tlColorInput__controls {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  min-width: 170px;
}

.tlColorInput__previewRow {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.tlColorInput__previewLabel {
  font-size: 0.75rem;
  color: var(--text-secondary);
  width: 3.5rem;
  flex-shrink: 0;
}

.tlColorInput__previewSwatch {
  width: 100%;
  height: 28px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--corner-radius);
}

.tlColorInput__divider {
  height: 1px;
  background: var(--border-subtle);
  margin: 0.25rem 0;
}

.tlColorInput__inputRow {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tlColorInput__inputLabel {
  font-size: 0.75rem;
  color: var(--text-secondary);
  width: 3.5rem;
  flex-shrink: 0;
}

.tlColorInput__input {
  flex: 1;
  height: 2rem;
  padding: 0 var(--spacing-03, 0.5rem);
  border: 1px solid var(--border-subtle);
  border-radius: var(--corner-radius);
  font-family: var(--font-family);
  font-size: 0.875rem;
  color: var(--text-primary);
  background: var(--layer-02, #fff);
  outline: none;
  transition: border-color 0.15s;
}

.tlColorInput__input:focus {
  border-color: var(--focus);
  box-shadow: 0 0 0 1px var(--focus);
}

.tlColorInput__input--error {
  border-color: var(--support-error, #da1e28);
}

.tlColorInput__actions {
  display: flex;
  gap: 0.5rem;
  margin-top: auto;
  padding-top: 0.25rem;
}

.tlColorInput__btn {
  flex: 1;
  height: 2rem;
  border: none;
  border-radius: var(--corner-radius);
  font-family: var(--font-family);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.tlColorInput__btn--ok {
  background: var(--button-primary, #0f62fe);
  color: var(--text-on-color, #fff);
}

.tlColorInput__btn--ok:hover {
  background: var(--button-primary-hover, #0353e9);
}

.tlColorInput__btn--cancel {
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-subtle);
}

.tlColorInput__btn--cancel:hover {
  background: var(--layer-hover);
}

.tlColorInput__btn--reset {
  background: transparent;
  color: var(--text-secondary);
  font-size: 0.75rem;
  font-weight: normal;
  border: 1px solid var(--border-subtle);
  flex: none;
  padding: 0 0.75rem;
}

.tlColorInput__btn--reset:hover {
  background: var(--layer-hover);
}
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add CSS styles for TLColorInput.
```

---

## Task 7: ViewColorInputControl (Java Server-Side)

The lean Java control that holds color state and handles commands.

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ViewColorInputControl.java`

- [ ] **Step 1: Create `ViewColorInputControl.java`**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.control.ColorChooserControl;
import com.top_logic.layout.form.control.ColorChooserSelectionControl;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.ViewFieldValueChanged.ValueCallback;

/**
 * Lean color input control that renders via the {@code TLColorInput} React component.
 *
 * <p>
 * Works with plain hex {@link String} values (e.g. {@code "#FF0000"}) instead of
 * {@code FormField} objects. Manages the user's personal color palette for persistence.
 * </p>
 */
public class ViewColorInputControl extends ReactControl {

	private static final String VALUE = "value";

	private static final String EDITABLE = "editable";

	private static final String PALETTE = "palette";

	private static final String PALETTE_COLUMNS = "paletteColumns";

	private static final String DEFAULT_PALETTE = "defaultPalette";

	private static final String COLOR_PALETTE_KEY = "colorPalette";

	private ValueCallback _valueCallback;

	/**
	 * Creates a new {@link ViewColorInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param value
	 *        The initial hex color value (e.g. "#FF0000"), may be {@code null}.
	 * @param editable
	 *        Whether the field is editable.
	 */
	public ViewColorInputControl(ReactContext context, String value, boolean editable) {
		super(context, null, "TLColorInput");
		putState(VALUE, value);
		putState(EDITABLE, Boolean.valueOf(editable));

		List<String> defaultPalette = loadDefaultPalette();
		List<String> personalPalette = loadPersonalPalette();
		List<String> palette = personalPalette != null ? personalPalette : defaultPalette;

		ColorChooserSelectionControl.Config config = getColorConfig();
		putState(PALETTE, palette);
		putState(DEFAULT_PALETTE, defaultPalette);
		putState(PALETTE_COLUMNS, Integer.valueOf(config.getColumns()));
	}

	/**
	 * Sets the hex color value.
	 *
	 * @param value
	 *        The new hex color string, may be {@code null}.
	 */
	public void setValue(String value) {
		putState(VALUE, value);
	}

	/**
	 * Sets whether this field is editable.
	 *
	 * @param editable
	 *        {@code true} for editable, {@code false} for read-only.
	 */
	public void setEditable(boolean editable) {
		putState(EDITABLE, Boolean.valueOf(editable));
	}

	/**
	 * Sets the callback that is invoked when the value changes from the client.
	 *
	 * @param callback
	 *        The callback, or {@code null} to remove.
	 */
	public void setValueCallback(ValueCallback callback) {
		_valueCallback = callback;
	}

	/**
	 * Handles value change events from the client.
	 */
	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		Object rawValue = arguments.get(VALUE);
		String newValue = rawValue != null ? rawValue.toString() : null;
		putState(VALUE, newValue);
		if (_valueCallback != null) {
			_valueCallback.valueChanged(newValue);
		}
	}

	/**
	 * Handles palette change events from the client (drag-drop swap, reset).
	 */
	@ReactCommand("paletteChanged")
	void handlePaletteChanged(Map<String, Object> arguments) {
		@SuppressWarnings("unchecked")
		List<String> newPalette = (List<String>) arguments.get(PALETTE);
		if (newPalette != null) {
			putState(PALETTE, newPalette);
			savePersonalPalette(newPalette);
		}
	}

	private static ColorChooserSelectionControl.Config getColorConfig() {
		ColorChooserSelectionControl.Config config =
			ApplicationConfig.getInstance().getConfig(ColorChooserSelectionControl.Config.class);
		if (config == null) {
			config = TypedConfiguration.newConfigItem(ColorChooserSelectionControl.Config.class);
		}
		return config;
	}

	private static List<String> loadDefaultPalette() {
		ColorChooserSelectionControl.Config config = getColorConfig();
		int rows = config.getRows();
		int columns = config.getColumns();
		List<String> result = new ArrayList<>(rows * columns);
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				String cellName = Character.toString((char) ('A' + column)) + row;
				ColorChooserSelectionControl.Config.ColorItem item = config.getColors().get(cellName);
				result.add(item != null ? item.getHexValue() : null);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<String> loadPersonalPalette() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		return (List<String>) pc.getJSONValue(COLOR_PALETTE_KEY);
	}

	private static void savePersonalPalette(List<String> palette) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setJSONValue(COLOR_PALETTE_KEY, palette);
	}

	/**
	 * Converts a {@link Color} to a hex string for use as control value.
	 *
	 * @param color
	 *        The color, may be {@code null}.
	 * @return Hex string like "#FF0000", or {@code null}.
	 */
	public static String colorToHex(Color color) {
		return ColorChooserControl.toHtmlColor(color);
	}

	/**
	 * Converts a hex string to a {@link Color}.
	 *
	 * @param hex
	 *        The hex string like "#FF0000", may be {@code null}.
	 * @return The color, or {@code null}.
	 */
	public static Color hexToColor(String hex) {
		return ColorChooserSelectionControl.toColor(hex);
	}
}
```

- [ ] **Step 2: Commit**

```
Ticket #29108: Add ViewColorInputControl for React color input.
```

---

## Task 8: Integration into FieldControlFactory and FieldControl

Wire the new control into the form system so that `java.awt.Color` attributes render with the color input.

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControl.java`

- [ ] **Step 1: Update `FieldControlFactory.java`**

In the `createFieldControl` method, add a `CUSTOM` case that checks for `java.awt.Color`. Change the `switch` block (around lines 43-58) — add before the `default` fallthrough:

```java
			case CUSTOM: {
				if (java.awt.Color.class.getName().equals(primitive.getStorageMapping().getApplicationType().getName())) {
					return createColorInput(context, value, editable);
				}
				return createTextInput(context, value, editable);
			}
```

Also add a new factory method at the end of the class (before the closing brace):

```java
	private static ReactControl createColorInput(ReactContext context, Object value, boolean editable) {
		String hex = null;
		if (value instanceof java.awt.Color) {
			hex = ViewColorInputControl.colorToHex((java.awt.Color) value);
		} else if (value instanceof String) {
			hex = (String) value;
		}
		return new ViewColorInputControl(context, hex, editable);
	}
```

- [ ] **Step 2: Update `FieldControl.java` — `setupValueCallback()`**

Add a new `else if` branch after the `ViewSelectControl` branch (around line 201):

```java
		} else if (_innerControl instanceof ViewColorInputControl) {
			((ViewColorInputControl) _innerControl).setValueCallback(this::handleValueChange);
		}
```

- [ ] **Step 3: Update `FieldControl.java` — `updateInnerControl()`**

Add a new `else if` branch after the `ViewSelectControl` branch (around line 236):

```java
		} else if (_innerControl instanceof ViewColorInputControl) {
			ViewColorInputControl color = (ViewColorInputControl) _innerControl;
			String hex = null;
			if (value instanceof java.awt.Color) {
				hex = ViewColorInputControl.colorToHex((java.awt.Color) value);
			} else if (value instanceof String) {
				hex = (String) value;
			}
			color.setValue(hex);
			color.setEditable(editable);
		}
```

- [ ] **Step 4: Add import to `FieldControl.java`**

Add at the top of the file:

```java
import java.awt.Color;
```

(Note: The `Color` reference in `updateInnerControl` uses the fully-qualified `java.awt.Color` to avoid ambiguity, but the `instanceof` check needs the import.)

- [ ] **Step 5: Commit**

```
Ticket #29108: Integrate ViewColorInputControl into form field factory.
```

---

## Task 9: Build and Verify

Build the React bundle and verify the Java module compiles.

**Files:**
- No new files.

- [ ] **Step 1: Build the React controls bundle**

```bash
cd com.top_logic.layout.react && npx vite build --config vite.config.controls.ts
```

Expected: Successful build, `src/main/webapp/script/tl-react-controls.js` updated.

- [ ] **Step 2: Build the Java modules**

```bash
cd com.top_logic.layout.view && mvn install -DskipTests=true
cd ../com.top_logic.layout.react && mvn install -DskipTests=true
```

Expected: `BUILD SUCCESS` for both modules.

- [ ] **Step 3: Commit the built JS bundle (if it changed)**

```
Ticket #29108: Rebuild React controls bundle with TLColorInput.
```

---

## Summary

| Task | Component | Files |
|------|-----------|-------|
| 1 | Color utils (TS) | `colorUtils.ts` |
| 2 | HSV mixer (TSX) | `ColorMixer.tsx` |
| 3 | Color palette (TSX) | `ColorPalette.tsx` |
| 4 | Color popup (TSX) | `ColorPopup.tsx` |
| 5 | Main component (TSX) + registration | `TLColorInput.tsx`, `controls-entry.ts` |
| 6 | CSS styles | `tlReactControls.css` |
| 7 | Java control | `ViewColorInputControl.java` |
| 8 | Form integration | `FieldControlFactory.java`, `FieldControl.java` |
| 9 | Build & verify | — |
