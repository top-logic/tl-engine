import { React, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.sidebar.openDrawer': 'Open navigation',
};

/**
 * Hamburger button that toggles a target sidebar's mobile drawer by sending the
 * {@code toggle} command. Rendered into whichever slot the sidebar is configured to
 * address via {@code drawer-open-slot-name} (typically the app bar's leading slot). The
 * button is hidden at desktop breakpoints by CSS so it only appears when the sidebar is in
 * mobile drawer mode.
 */
const TLDrawerToggle: React.FC<TLCellProps> = ({ controlId }) => {
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  return (
    <button
      id={controlId}
      type="button"
      className="tlDrawerToggle"
      aria-label={i18n['js.sidebar.openDrawer']}
      onClick={() => sendCommand('toggle', {})}
    >
      <svg viewBox="0 0 16 16" width="20" height="20" aria-hidden="true">
        <path d="M2 4h12M2 8h12M2 12h12" fill="none" stroke="currentColor" strokeWidth="2"
          strokeLinecap="round" />
      </svg>
    </button>
  );
};

export default TLDrawerToggle;
