import { React, useTLCommand, useTLFieldValue, useI18N, pressClosedSurface, rootClassName, tooltipProps, useFieldLabelProps, fieldInputId } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { showsValueOnly } from './form/fieldState';
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
 *  - disabled: boolean        - Whether the field that is not editable shows an inactive swatch
 *                               button that opens no popup (see showsValueOnly) instead of the icon
 *  - icons: IconEntry[]       - Icon metadata (populated on loadIcons)
 *  - iconsLoaded: boolean     - Whether icons have been loaded
 */
const TLIconSelect: React.FC<TLCellProps> = ({ controlId, state }) => {
  const inputId = fieldInputId(controlId);
  const labelProps = useFieldLabelProps(controlId, inputId);
  const [fieldValue, setValue] = useTLFieldValue();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const [open, setOpen] = useState(false);
  const swatchRef = useRef<HTMLButtonElement>(null);

  const value = fieldValue as string | null;
  const editable = state.editable !== false;
  const disabled = state.disabled === true;
  const icons = (state.icons as IconEntry[]) ?? [];
  const iconsLoaded = state.iconsLoaded === true;

  const handleClick = useCallback(() => {
    // The press of this click has closed the popup this swatch opens: leave it closed.
    if (pressClosedSurface()) {
      return;
    }
    if (editable && !disabled) setOpen(true);
  }, [editable, disabled]);

  const handleSelect = useCallback(
    (encoded: string | null) => {
      setOpen(false);
      setValue(encoded);
    },
    [setValue]
  );

  const handleCancel = useCallback(() => {
    setOpen(false);
  }, []);

  const handleLoadIcons = useCallback(async () => {
    await sendCommand('loadIcons');
  }, [sendCommand]);

  // Read-only rendering
  if (showsValueOnly(state)) {
    return (
      <span id={controlId} className={rootClassName(state, 'tlIconSelect tlIconSelect--immutable')}>
        <span className="tlIconSelect__swatch">
          {value ? <IconPreview encoded={value} /> : null}
        </span>
      </span>
    );
  }

  return (
    <span id={controlId} className={rootClassName(state, 'tlIconSelect')}>
      <button
        ref={swatchRef}
        className={
          'tlIconSelect__swatch' + (value == null ? ' tlIconSelect__swatch--empty' : '')
        }
        onClick={handleClick}
        disabled={disabled}
        aria-label={i18n['js.iconSelect.chooseIcon']}
        {...tooltipProps(value ?? i18n['js.iconSelect.chooseIcon'])}
        id={inputId}
        {...labelProps}
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
