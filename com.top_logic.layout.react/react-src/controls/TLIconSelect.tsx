import { React, useTLCommand, useTLFieldValue, useI18N, pressClosedSurface, rootClassName, stateAttr, tooltipProps, useFieldLabelProps, fieldInputId, ThemeIcon, ICON_CHOOSE_ICON } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { fieldStateAttrs, showsValueOnly } from './form/fieldState';
import IconSelectPopup from './icon/IconSelectPopup';
import type { IconEntry } from './icon/IconSelectPopup';

const I18N_KEYS = {
  'js.iconSelect.noIcon': 'No icon',
};

const { useState, useCallback, useRef } = React;

/** A name for an icon the list does not know (yet): its last class without the font prefix, hyphens as spaces. */
function classLabel(encoded: string): string {
  const tokens = encoded.replace(/^(css|colored):/, '').trim().split(/\s+/);
  const last = tokens[tokens.length - 1] ?? '';
  return last.replace(/^(fa|bi)-/, '').replace(/-/g, ' ') || last;
}

/**
 * An icon select field rendered via React.
 *
 * <p>The field is one swatch of the design system (tl-swatch): a button that opens the icon picker, a plain span
 * while the field is read-only, an inactive button while it is disabled. It carries the field state like every
 * other field (error, warning, mandatory) and the state word `empty` while it holds no icon, when it shows the
 * glyph that says "choose an icon".</p>
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

  // What the swatch says of itself: the label of its icon if the list knows it, else a name made of its class
  // (until the list has loaded), or that it has none. That it opens a dialog is aria-haspopup's to say.
  const known = value == null ? undefined : icons.find(icon => icon.variants.some(variant => variant.encoded === value));
  const name = value == null ? i18n['js.iconSelect.noIcon'] : (known?.label ?? classLabel(value));

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

  // Read-only: the icon alone, as a swatch that is no button
  if (showsValueOnly(state)) {
    return (
      <span
        id={controlId}
        className={rootClassName(state, 'tl-swatch')}
        data-tl-state={stateAttr(value == null && 'empty')}
        role="img"
        aria-label={name}
        {...tooltipProps(name)}
      >
        <ThemeIcon encoded={value ?? undefined} className="tl-icon-md" />
      </span>
    );
  }

  const fieldState = fieldStateAttrs(state);

  return (
    <span id={controlId} className={rootClassName(state) || undefined}>
      <button
        ref={swatchRef}
        type="button"
        className="tl-swatch"
        onClick={handleClick}
        disabled={disabled}
        aria-label={name}
        aria-haspopup="dialog"
        aria-expanded={open}
        {...fieldState}
        data-tl-state={stateAttr(value == null && 'empty', fieldState['data-tl-state'])}
        {...tooltipProps(name)}
        id={inputId}
        {...labelProps}
        // Inside a form field the label names the field; the swatch adds its own value (aria-label) to it.
        aria-labelledby={labelProps['aria-labelledby'] === undefined ? undefined : `${labelProps['aria-labelledby']} ${inputId}`}
      >
        <ThemeIcon encoded={value ?? ICON_CHOOSE_ICON} className="tl-icon-md" />
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
