import { React, useTLState, useTLCommand, useI18N, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, BreadcrumbStateJson } from 'tl-react-bridge';
import Breadcrumbs from '@mui/material/Breadcrumbs';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';

const { useCallback } = React;

/** Command jumping back to an item of the trail. */
const CMD_NAVIGATE = 'navigate';

/** Argument of {@link CMD_NAVIGATE}: the ID of the item. */
const ARG_ITEM_ID = 'itemId';

/** The name of the trail for assistive technology, with its English default (TLBreadcrumb's). */
const I18N_KEYS = {
  'js.breadcrumb.label': 'Breadcrumb',
};

/**
 * Renders the state of a TopLogic breadcrumb (module name `TLBreadcrumb`) with the MUI
 * `Breadcrumbs`.
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>items → one entry per item: every item but the last is a `Link` rendered as a button,
 *     whose click sends `navigate` with the argument `itemId` naming the item; the last item, the
 *     current location, is a `Typography` marked `aria-current="page"`;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `Breadcrumbs`.</li>
 * </ul>
 *
 * <p>The separator is the one of MUI. Like TLBreadcrumb, the trail shows every item: MUI's
 * collapsing of a long trail is switched off.</p>
 */
const MuiBreadcrumbAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<BreadcrumbStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const navigate = useCallback((itemId: string) => {
    sendCommand(CMD_NAVIGATE, { [ARG_ITEM_ID]: itemId });
  }, [sendCommand]);

  if (state.hidden === true) {
    return null;
  }

  const items = state.items ?? [];

  return (
    <Breadcrumbs id={controlId} className={rootClassName(state)} maxItems={Math.max(items.length, 1)} aria-label={i18n['js.breadcrumb.label']}>
      {items.map((item, index) => (index === items.length - 1
        ? (
          <Typography key={item.id} variant="body2" color="textPrimary" aria-current="page">
            {item.label}
          </Typography>
        )
        : (
          <Link key={item.id} component="button" type="button" variant="body2" underline="hover"
            color="inherit" onClick={() => navigate(item.id)}>
            {item.label}
          </Link>
        )))}
    </Breadcrumbs>
  );
};

export default MuiBreadcrumbAdapter;
