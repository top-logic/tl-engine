import { React, useTLCommand, useI18N, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.sidebar.openDrawer': 'Open navigation',
};

/** Icon of the button opening the drawer. */
const MENU_ICON = 'css:fa-solid fa-bars';

/**
 * Hamburger button that toggles a target sidebar's mobile drawer by sending the
 * {@code toggle} command. Rendered into whichever slot the sidebar is configured to
 * address via {@code drawer-open-slot-name} (typically the app bar's leading slot). The
 * button is hidden at desktop breakpoints by CSS so it only appears when the sidebar is in
 * mobile drawer mode.
 */
const TLDrawerToggle: React.FC<TLCellProps> = ({ controlId, state }) => {
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const Button = usePart('Button');

  return (
    <Button
      id={controlId}
      label={i18n['js.sidebar.openDrawer']}
      image={MENU_ICON}
      display="icon-only"
      appearance="ghost"
      className={rootClassName(state, 'tlDrawerToggle')}
      onClick={() => sendCommand('toggle', {})}
    />
  );
};

export default TLDrawerToggle;
