import { React, useTLFieldValue, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.i18nString.allLanguages': 'Edit all languages',
  'js.i18nString.ok': 'OK',
  'js.i18nString.cancel': 'Cancel',
};

const { useState } = React;

interface LocaleEntry {
  language: string;
  label: string;
  value: string | null;
}

/**
 * Input control for an {@code I18NString} (multi-locale) attribute.
 *
 * State from server:
 *  - value: string | null    - the current user's locale value
 *  - fallback: string | null - best other-locale value (view display / input placeholder)
 *  - locales: LocaleEntry[]   - {language, label, value} per supported language (for the popup)
 *  - editable / hasError / disabled
 *
 * The inline field edits the current locale (command 'valueChanged'); the icon-button opens an
 * all-languages popup that edits every language explicitly (command 'applyTranslations').
 */
const TLI18NStringInput: React.FC<TLCellProps> = ({ controlId, state }) => {
  const [value, setValue] = useTLFieldValue();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const [open, setOpen] = useState(false);

  const editable = state.editable !== false;
  const fallback = (state.fallback as string | null) ?? null;
  const locales = (state.locales as LocaleEntry[]) ?? [];
  const text = (value as string) ?? '';

  // Immutable: show the current-locale value, or the fallback (greyed) if empty.
  if (!editable) {
    const usingFallback = text === '' && fallback != null;
    return (
      <span
        id={controlId}
        className="tlReactTextInput tlReactTextInput--immutable"
        style={usingFallback ? { color: 'var(--tl-color-text-muted, #888)', fontStyle: 'italic' } : undefined}
      >
        {text !== '' ? text : (fallback ?? '')}
      </span>
    );
  }

  const hasError = state.hasError === true;
  const cls = ['tlReactTextInput', hasError ? 'tlReactTextInput--error' : ''].filter(Boolean).join(' ');

  return (
    <span id={controlId} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', width: '100%' }}>
      <input
        type="text"
        value={text}
        placeholder={fallback ?? undefined}
        onChange={e => setValue(e.target.value)}
        disabled={state.disabled === true}
        className={cls}
        style={{ flex: 1 }}
        aria-invalid={hasError || undefined}
      />
      <button
        type="button"
        onClick={() => setOpen(true)}
        title={i18n['js.i18nString.allLanguages']}
        aria-label={i18n['js.i18nString.allLanguages']}
        style={{ flex: '0 0 auto', cursor: 'pointer' }}
      >
        <i className="bi bi-translate" />
      </button>
      {open && (
        <I18NStringPopup
          locales={locales}
          okLabel={i18n['js.i18nString.ok']}
          cancelLabel={i18n['js.i18nString.cancel']}
          onCancel={() => setOpen(false)}
          onApply={values => {
            setOpen(false);
            sendCommand('applyTranslations', { values });
          }}
        />
      )}
    </span>
  );
};

interface PopupProps {
  locales: LocaleEntry[];
  okLabel: string;
  cancelLabel: string;
  onCancel: () => void;
  onApply: (values: Record<string, string>) => void;
}

const I18NStringPopup: React.FC<PopupProps> = ({ locales, okLabel, cancelLabel, onCancel, onApply }) => {
  const [values, setValues] = useState<Record<string, string>>(() => {
    const init: Record<string, string> = {};
    locales.forEach(l => {
      init[l.language] = l.value ?? '';
    });
    return init;
  });

  const backdrop: React.CSSProperties = {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.3)',
    display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000,
  };
  const box: React.CSSProperties = {
    background: '#fff', padding: '16px', borderRadius: '6px',
    minWidth: '360px', maxHeight: '80vh', overflowY: 'auto',
    boxShadow: '0 4px 16px rgba(0,0,0,0.25)',
  };

  return (
    <div style={backdrop} onClick={onCancel}>
      <div style={box} onClick={e => e.stopPropagation()}>
        {locales.map(l => (
          <div key={l.language} style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
            <label style={{ flex: '0 0 8rem' }}>{l.label}</label>
            <input
              type="text"
              className="tlReactTextInput"
              style={{ flex: 1 }}
              value={values[l.language] ?? ''}
              onChange={e => {
                const v = e.target.value;
                setValues(prev => ({ ...prev, [l.language]: v }));
              }}
            />
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '12px' }}>
          <button type="button" onClick={onCancel}>{cancelLabel}</button>
          <button type="button" onClick={() => onApply(values)}>{okLabel}</button>
        </div>
      </div>
    </div>
  );
};

export default TLI18NStringInput;
