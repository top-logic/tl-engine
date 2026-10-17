import { React, useTLCommand, useTLFieldValue, useI18N, pressClosedSurface, rootClassName, stateAttr, tooltipProps, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import ColorPopup from './color/ColorPopup';

const I18N_KEYS = {
  'js.colorInput.chooseColor': 'Choose color',
  'js.colorInput.noColor': 'No color',
};

const { useState, useCallback, useRef } = React;

/**
 * A color input field rendered via React.
 *
 * <p>The field is one swatch of the design system (tl-swatch tl-swatch--color): a button that opens the
 * color picker, a plain span while the field is read-only, an inactive button while it is disabled. It carries
 * the field state like every other field (error, warning, mandatory) and the state word `empty` while it
 * holds no color; the color itself is inline, as data.</p>
 *
 * State from server:
 *  - value: string | null       - Current hex color ('#RRGGBB') or null
 *  - editable: boolean          - Whether the field is editable
 *  - disabled: boolean          - Whether the field that is not editable shows an inactive swatch
 *                                 button that opens no popup (see showsValueOnly) instead of the color
 *  - palette: (string | null)[] - Palette colors (flat, row-major)
 *  - paletteColumns: number     - Number of palette columns
 *  - defaultPalette: (string | null)[] - Default palette for reset
 */
const TLColorInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [fieldValue, setValue] = useTLFieldValue();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const [open, setOpen] = useState(false);
  const swatchRef = useRef<HTMLButtonElement>(null);

  const value = fieldValue as string | null;
  const editable = state.editable !== false;
  const palette = (state.palette as (string | null)[]) ?? [];
  const paletteColumns = (state.paletteColumns as number) ?? 6;
  const defaultPalette = (state.defaultPalette as (string | null)[]) ?? palette;
  const name = value == null ? i18n['js.colorInput.noColor'] : i18n['js.colorInput.chooseColor'];

  const handleClick = useCallback(() => {
    // The press of this click has closed the popup this swatch opens: leave it closed.
    if (pressClosedSurface()) {
      return;
    }
    if (editable) setOpen(true);
  }, [editable]);

  const handleConfirm = useCallback(
    (hex: string | null) => {
      setOpen(false);
      setValue(hex);
    },
    [setValue]
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

  // Read-only: the color alone, as a swatch that is no button
  if (showsValueOnly(state)) {
    return (
      <span
        id={controlId}
        className={rootClassName(state, 'tl-swatch tl-swatch--color')}
        data-tl-state={stateAttr(value == null && 'empty')}
        style={value != null ? { backgroundColor: value } : undefined}
        role="img"
        aria-label={value ?? name}
        {...tooltipProps(value ?? name)}
      />
    );
  }

  const fieldState = fieldStateAttrs(state);

  return (
    <span id={controlId} className={rootClassName(state) || undefined}>
      <button
        ref={swatchRef}
        type="button"
        className="tl-swatch tl-swatch--color"
        style={value != null ? { backgroundColor: value } : undefined}
        onClick={handleClick}
        disabled={state.disabled === true}
        aria-label={name}
        aria-haspopup="dialog"
        aria-expanded={open}
        {...fieldState}
        data-tl-state={stateAttr(value == null && 'empty', fieldState['data-tl-state'])}
        {...tooltipProps(value ?? name)}
        id={inputId}
        {...labelProps}
      />

      {open && (
        <ColorPopup
          anchorRef={swatchRef}
          currentColor={value}
          palette={palette}
          paletteColumns={paletteColumns}
          defaultPalette={defaultPalette}
          canReset={state.canReset !== false}
          onConfirm={handleConfirm}
          onCancel={handleCancel}
          onPaletteChange={handlePaletteChange}
        />
      )}
    </span>
  );
};

export default TLColorInput;
