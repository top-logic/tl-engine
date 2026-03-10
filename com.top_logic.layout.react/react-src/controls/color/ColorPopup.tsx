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
