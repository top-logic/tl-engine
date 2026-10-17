import { React, useTLCommand, useI18N, rootClassName, usePart, useDisplayClass } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.sidebar.openDrawer': 'Open navigation',
};

/** Icon of the button opening the drawer. */
const MENU_ICON = 'css:fa-solid fa-bars';

/**
 * Hamburger button that opens and closes the drawer of a target sidebar by sending {@code toggle}. Rendered into the
 * slot the sidebar addresses via {@code drawer-open-slot-name} (typically the app bar's leading slot).
 *
 * The button exists only while the app shell is compact (useDisplayClass): on a wide screen the sidebar is a rail
 * and there is no drawer to open.
 *
 * State:
 * - open: boolean - whether the drawer is open (aria-expanded)
 * - controls: string - the id of the sidebar's navigation (aria-controls, set while the drawer is open; closed, the
 *   navigation is not in the document)
 */
const TLDrawerToggle: React.FC<TLCellProps> = ({ controlId, state }) => {
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  const compact = useDisplayClass() === 'compact';

  if (!compact) {
    return null;
  }
  const open = state.open === true;
  return (
    <Button
      id={controlId}
      className={rootClassName(state) || undefined}
      label={i18n['js.sidebar.openDrawer']}
      image={MENU_ICON}
      display="icon-only"
      appearance="ghost"
      aria-expanded={open}
      aria-controls={open ? (state.controls as string) : undefined}
      onClick={() => sendCommand('toggle', {})}
    />
  );
};

export default TLDrawerToggle;
