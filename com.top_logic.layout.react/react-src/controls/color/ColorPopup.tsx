import { React, useI18N } from 'tl-react-bridge';
import ColorPalette from './ColorPalette';
import ColorMixer from './ColorMixer';
import { hexToRgb, rgbToHex, isValidHex, clampByte } from './colorUtils';

const I18N_KEYS = {
  'js.colorInput.paletteTab': 'Color Palette',
  'js.colorInput.mixerTab': 'Color Mixer',
  'js.colorInput.current': 'Current',
  'js.colorInput.new': 'New',
  'js.colorInput.red': 'Red',
  'js.colorInput.green': 'Green',
  'js.colorInput.blue': 'Blue',
  'js.colorInput.hex': 'Hex',
  'js.colorInput.reset': 'Reset',
  'js.colorInput.cancel': 'Cancel',
  'js.colorInput.ok': 'OK',
};

const { useState, useCallback, useEffect, useRef, useLayoutEffect } = React;

interface ColorPopupProps {
  /** Ref to the anchor element for positioning. */
  anchorRef: React.RefObject<HTMLButtonElement>;
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
  anchorRef,
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
  const i18n = useI18N(I18N_KEYS);
  const [position, setPosition] = useState<{ top: number; left: number } | null>(null);

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
    <div
      className="tlColorInput__popup"
      ref={popupRef}
      style={position
        ? { top: position.top, left: position.left, visibility: 'visible' }
        : { visibility: 'hidden' }}
    >
      {/* Tab bar */}
      <div className="tlColorInput__tabs">
        <button
          className={'tlColorInput__tab' + (tab === 'palette' ? ' tlColorInput__tab--active' : '')}
          onClick={() => setTab('palette')}
        >
          {i18n['js.colorInput.paletteTab']}
        </button>
        <button
          className={'tlColorInput__tab' + (tab === 'mixer' ? ' tlColorInput__tab--active' : '')}
          onClick={() => setTab('mixer')}
        >
          {i18n['js.colorInput.mixerTab']}
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
            <span className="tlColorInput__previewLabel">{i18n['js.colorInput.current']}</span>
            <div className="tlColorInput__previewSwatch" style={{ backgroundColor: currentColor }} />
          </div>
          <div className="tlColorInput__previewRow">
            <span className="tlColorInput__previewLabel">{i18n['js.colorInput.new']}</span>
            <div className="tlColorInput__previewSwatch" style={{ backgroundColor: draft }} />
          </div>

          <div className="tlColorInput__divider" />

          <div className="tlColorInput__inputRow">
            <span className="tlColorInput__inputLabel">{i18n['js.colorInput.red']}</span>
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
            <span className="tlColorInput__inputLabel">{i18n['js.colorInput.green']}</span>
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
            <span className="tlColorInput__inputLabel">{i18n['js.colorInput.blue']}</span>
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
            <span className="tlColorInput__inputLabel">{i18n['js.colorInput.hex']}</span>
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
                {i18n['js.colorInput.reset']}
              </button>
            )}
            <button className="tlColorInput__btn tlColorInput__btn--cancel" onClick={onCancel}>
              {i18n['js.colorInput.cancel']}
            </button>
            <button className="tlColorInput__btn tlColorInput__btn--ok" onClick={handleOk}>
              {i18n['js.colorInput.ok']}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ColorPopup;
