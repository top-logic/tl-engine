import { React, useI18N, usePart, tooltipProps, useTabList, TabStrip } from 'tl-react-bridge';
import ColorPalette, { COLOR_DRAG_TYPE } from './ColorPalette';
import ColorMixer from './ColorMixer';
import { PickerPopover } from '../picker/PickerPopover';
import { hexToRgb, rgbToHex, isValidHex, clampByte } from './colorUtils';

/** Checks if a hex color already exists in the palette (case-insensitive). */
function paletteContains(palette: (string | null)[], hex: string): boolean {
  const upper = hex.toUpperCase();
  return palette.some(c => c != null && c.toUpperCase() === upper);
}

const I18N_KEYS = {
  'js.colorInput.chooseColor': 'Choose color',
  'js.colorInput.paletteTab': 'Color Palette',
  'js.colorInput.mixerTab': 'Color Mixer',
  'js.colorInput.current': 'Current',
  'js.colorInput.new': 'New',
  'js.colorInput.red': 'Red',
  'js.colorInput.green': 'Green',
  'js.colorInput.blue': 'Blue',
  'js.colorInput.hex': 'Hex',
  'js.colorInput.noColor': 'No color',
  'js.colorInput.clear': 'Clear',
  'js.colorInput.reset': 'Reset',
  'js.colorInput.cancel': 'Cancel',
  'js.colorInput.ok': 'OK',
};

const { useState, useCallback, useEffect, useId } = React;

interface ColorPopupProps {
  /** The element the popup hangs under and gives the focus back to. */
  anchorRef: React.RefObject<HTMLElement | null>;
  /** The confirmed (original) color, or null if no color is set. */
  currentColor: string | null;
  /** Palette colors (flat array, row-major). */
  palette: (string | null)[];
  /** Number of palette columns. */
  paletteColumns: number;
  /** Default palette for reset. */
  defaultPalette: (string | null)[];
  /** Whether the user can clear the color to null. */
  canReset: boolean;
  /** Called when user confirms a color (OK, Enter, double-click), null means clear. */
  onConfirm: (hex: string | null) => void;
  /** Called when user cancels (Cancel, Escape, a press outside). Must close the popup at once. */
  onCancel: () => void;
  /** Called when palette changes (swap or reset). */
  onPaletteChange: (newPalette: (string | null)[]) => void;
}

type Tab = 'palette' | 'mixer';

const TABS: Tab[] = ['palette', 'mixer'];

/** The focus starts on the selected tab. */
function selectedTab(root: HTMLElement): HTMLElement | null {
  return root.querySelector<HTMLElement>('[role="tab"][aria-selected="true"]');
}

/**
 * The color chooser popup (the picker shell) with two tabs, the preview of the color and the inputs of its
 * channels. Everything in it changes only the draft "New"; OK and Enter in an input confirm it.
 */
const ColorPopup: React.FC<ColorPopupProps> = ({
  anchorRef,
  currentColor,
  palette,
  paletteColumns,
  defaultPalette,
  canReset,
  onConfirm,
  onCancel,
  onPaletteChange,
}) => {
  const [tab, setTab] = useState<Tab>('palette');
  const [draft, setDraft] = useState<string | null>(currentColor);
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  const TextInput = usePart('TextInput');
  const Divider = usePart('Divider');
  const list = useTabList({ idPrefix: 'tl-color-' + useId(), ids: TABS, selected: tab, onSelect: id => setTab(id as Tab) });
  // One panel serves both tabs: the inputs and the preview beside the content are the same for each.
  const panel = list.panelProps(tab);

  // RGB derived from draft (defaults to [0,0,0] when no color)
  const hasColor = draft != null;
  const [r, g, b] = hasColor ? hexToRgb(draft) : [0, 0, 0];
  const [hexInput, setHexInput] = useState(draft?.toUpperCase() ?? '');

  // Keep hex input in sync when draft changes via non-hex-field means
  useEffect(() => {
    setHexInput(draft?.toUpperCase() ?? '');
  }, [draft]);

  const handleRgbChange = useCallback(
    (channel: 'r' | 'g' | 'b') => (text: string) => {
      const val = parseInt(text, 10);
      if (isNaN(val)) return;
      const clamped = clampByte(val);
      const newR = channel === 'r' ? clamped : r;
      const newG = channel === 'g' ? clamped : g;
      const newB = channel === 'b' ? clamped : b;
      setDraft(rgbToHex(newR, newG, newB));
    },
    [r, g, b]
  );

  const handleNewSwatchDragStart = useCallback(
    (e: React.DragEvent) => {
      if (draft != null) {
        e.dataTransfer.setData(COLOR_DRAG_TYPE, draft.toUpperCase());
        e.dataTransfer.effectAllowed = 'move';
        // The drag image is a swatch like the cells it is dropped on
        const ghost = document.createElement('div');
        ghost.className = 'tl-swatch tl-swatch--color';
        ghost.style.backgroundColor = draft;
        ghost.style.position = 'absolute';
        ghost.style.top = '-9999px';
        document.body.appendChild(ghost);
        e.dataTransfer.setDragImage(ghost, ghost.offsetWidth / 2, ghost.offsetHeight / 2);
        requestAnimationFrame(() => document.body.removeChild(ghost));
      }
    },
    [draft]
  );

  const handleHexChange = useCallback((val: string) => {
    setHexInput(val);
    if (isValidHex(val)) {
      setDraft(val);
    }
  }, []);

  const handleClearDraft = useCallback(() => {
    setDraft(null);
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

  const handleReplace = useCallback(
    (index: number, hex: string) => {
      const newPalette = [...palette];
      newPalette[index] = hex;
      onPaletteChange(newPalette);
    },
    [palette, onPaletteChange]
  );

  const handleReset = useCallback(() => {
    onPaletteChange([...defaultPalette]);
  }, [defaultPalette, onPaletteChange]);

  const autoAddToPalette = useCallback(
    (hex: string) => {
      if (paletteContains(palette, hex)) return;
      const emptyIndex = palette.indexOf(null);
      if (emptyIndex < 0) return;
      const newPalette = [...palette];
      newPalette[emptyIndex] = hex.toUpperCase();
      onPaletteChange(newPalette);
    },
    [palette, onPaletteChange]
  );

  /** Confirms a color; the first free cell of the palette takes a new one. */
  const confirmColor = useCallback(
    (hex: string | null) => {
      if (hex != null) {
        autoAddToPalette(hex);
      }
      onConfirm(hex);
    },
    [onConfirm, autoAddToPalette]
  );

  // Enter in an input finishes the edit like OK, while the hex field holds a color.
  const handleInputKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key !== 'Enter' || !isValidHex(hexInput)) return;
      e.preventDefault();
      confirmColor(draft);
    },
    [hexInput, draft, confirmColor]
  );

  const noColor = i18n['js.colorInput.noColor'];

  return (
    <PickerPopover
      anchorRef={anchorRef}
      label={i18n['js.colorInput.chooseColor']}
      initialFocus={selectedTab}
      onClose={onCancel}
    >
      <div className="tl-tabs tl-picker__tabs">
        <TabStrip selected={tab} onKeyDown={list.onListKeyDown}>
          <button {...list.tabProps('palette', { controls: panel.id })} className="tl-tabs__tab tl-type-body">
            {i18n['js.colorInput.paletteTab']}
          </button>
          <button {...list.tabProps('mixer', { controls: panel.id })} className="tl-tabs__tab tl-type-body">
            {i18n['js.colorInput.mixerTab']}
          </button>
        </TabStrip>
        <div {...panel} hidden={false} className="tl-tabs__panel">
          <div className="tl-stack tl-stack--row tl-stack--gap-default">
            {/* Left: the content of the tab; both stay mounted, so the mixer keeps its hue */}
            <div className="tl-stack tl-stack--column tl-stack--gap-compact" hidden={tab !== 'palette'}>
              <ColorPalette
                colors={palette}
                columns={paletteColumns}
                selected={draft}
                onSelect={handlePaletteSelect}
                onConfirm={handlePaletteConfirm}
                onSwap={handleSwap}
                onReplace={handleReplace}
              />
              <div>
                <Button
                  label={i18n['js.colorInput.reset']}
                  display="label-only"
                  appearance="link"
                  onClick={handleReset}
                />
              </div>
            </div>
            <ColorMixer color={draft ?? '#000000'} onColorChange={setDraft} hidden={tab !== 'mixer'} />

            {/* Right: preview and inputs */}
            <div className="tl-stack tl-stack--column tl-stack--gap-compact">
              <div className="tl-color-picker__preview">
                <span className="tl-type-label">{i18n['js.colorInput.current']}</span>
                <div
                  className="tl-swatch tl-swatch--color tl-swatch--wide"
                  role="img"
                  aria-label={`${i18n['js.colorInput.current']}: ${currentColor?.toUpperCase() ?? noColor}`}
                  data-tl-state={currentColor == null ? 'empty' : undefined}
                  style={currentColor != null ? { backgroundColor: currentColor } : undefined}
                  {...tooltipProps(currentColor ?? noColor)}
                />
                <span className="tl-type-label">{i18n['js.colorInput.new']}</span>
                <div
                  className="tl-swatch tl-swatch--color tl-swatch--wide"
                  role="img"
                  aria-label={`${i18n['js.colorInput.new']}: ${draft?.toUpperCase() ?? noColor}`}
                  data-tl-state={!hasColor ? 'empty' : undefined}
                  style={hasColor ? { backgroundColor: draft } : undefined}
                  {...tooltipProps(draft ?? noColor)}
                  draggable={hasColor}
                  onDragStart={hasColor ? handleNewSwatchDragStart : undefined}
                />
              </div>

              <Divider />

              <div className="tl-color-picker__inputs">
                <label className="tl-type-label">
                  {i18n['js.colorInput.red']}
                  <TextInput
                    type="number"
                    min={0}
                    max={255}
                    aria-label={i18n['js.colorInput.red']}
                    value={hasColor ? String(r) : ''}
                    onChange={handleRgbChange('r')}
                    onKeyDown={handleInputKeyDown}
                  />
                </label>
                <label className="tl-type-label">
                  {i18n['js.colorInput.green']}
                  <TextInput
                    type="number"
                    min={0}
                    max={255}
                    aria-label={i18n['js.colorInput.green']}
                    value={hasColor ? String(g) : ''}
                    onChange={handleRgbChange('g')}
                    onKeyDown={handleInputKeyDown}
                  />
                </label>
                <label className="tl-type-label">
                  {i18n['js.colorInput.blue']}
                  <TextInput
                    type="number"
                    min={0}
                    max={255}
                    aria-label={i18n['js.colorInput.blue']}
                    value={hasColor ? String(b) : ''}
                    onChange={handleRgbChange('b')}
                    onKeyDown={handleInputKeyDown}
                  />
                </label>
                <label className="tl-type-label">
                  {i18n['js.colorInput.hex']}
                  <TextInput
                    aria-label={i18n['js.colorInput.hex']}
                    validation={hexInput !== '' && !isValidHex(hexInput) ? 'error' : undefined}
                    value={hexInput}
                    onChange={handleHexChange}
                    onKeyDown={handleInputKeyDown}
                  />
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="tl-button-bar tl-picker__actions">
        {canReset && (
          <Button
            label={i18n['js.colorInput.clear']}
            display="label-only"
            appearance="ghost"
            danger
            onClick={handleClearDraft}
          />
        )}
        <Button label={i18n['js.colorInput.cancel']} display="label-only" appearance="secondary" onClick={onCancel} />
        <Button
          label={i18n['js.colorInput.ok']}
          display="label-only"
          appearance="primary"
          onClick={() => confirmColor(draft)}
        />
      </div>
    </PickerPopover>
  );
};

export default ColorPopup;
