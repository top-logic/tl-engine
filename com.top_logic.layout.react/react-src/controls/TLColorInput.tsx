import { React, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import ColorPopup from './color/ColorPopup';

const I18N_KEYS = { 'js.colorInput.chooseColor': 'Choose color' };

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
  const i18n = useI18N(I18N_KEYS);
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
    (hex: string | null) => {
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
        aria-label={i18n['js.colorInput.chooseColor']}
      />

      {open && (
        <ColorPopup
          anchorRef={swatchRef}
          currentColor={value}
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
