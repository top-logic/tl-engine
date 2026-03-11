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
