import { React, useTLState, TLChild, ButtonDefaults, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, AppBarStateJson, ChildControlJson } from 'tl-react-bridge';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import type { SxProps, Theme } from '@mui/material/styles';

/** The elevation of an elevated bar, the one MUI gives an app bar by default. */
const ELEVATION = 4;

/**
 * The color of the bar: the neutral surface of MUI with the primary text color. On it the
 * controls placed in the bar - the buttons with their primary color, the texts - keep the colors
 * they have on the page; on the MUI default `primary` they would stand blue on blue.
 */
const COLOR = 'default';

/** The space between the parts of the bar, in units of the MUI spacing. */
const TOOLBAR_SX: SxProps<Theme> = { gap: 1 };

/** The slots of the bar, laid out by MUI: the actions take the room left and give way first, the title last. */
const LEADING_SX: SxProps<Theme> = { display: 'flex', flex: 'none', alignItems: 'center' };
const CHILDREN_SX: SxProps<Theme> = { display: 'flex', flex: '0 1 auto', alignItems: 'center', gap: 1, minWidth: 0 };
const ACTIONS_SX: SxProps<Theme> = { display: 'flex', flex: '1 1 0', justifyContent: 'flex-end', minWidth: 0, '&:empty': { display: 'none' } };
const TRAILING_SX: SxProps<Theme> = { display: 'flex', flex: 'none', alignItems: 'center', gap: 1, ml: 'auto' };

/** The title: as wide as its text, truncated when the bar is short of room. */
const TITLE_SX: SxProps<Theme> = { flex: '0 1 auto', minWidth: 0 };

/**
 * Renders the state of a TopLogic app bar (module name `TLAppBar`) with the MUI `AppBar`
 * (`position="static"`, color `default`) and its `Toolbar`.
 *
 * <p>The adapter lays out its slots with MUI; it follows the state contract (state.proto), not the stylesheets of
 * TopLogic.</p>
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>title → a `Typography` (`h6`, as heading `h1`), truncated with an ellipsis;</li>
 * <li>leading, children, actions, trailing → their slots, each control rendered through
 *     {@link TLChild}; the buttons of the actions take the appearance `ghost` as in TLAppBar
 *     ({@link ButtonDefaults});</li>
 * <li>variant `flat` (default) → elevation 0; `elevated` → the elevation of an MUI app bar;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `AppBar`.</li>
 * </ul>
 *
 */
const MuiAppBarAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<AppBarStateJson>>();

  if (state.hidden === true) {
    return null;
  }

  const children: ChildControlJson[] = state.children ?? [];

  return (
    <AppBar id={controlId} position="static" color={COLOR} className={rootClassName(state)}
      elevation={state.variant === 'elevated' ? ELEVATION : 0}>
      <Toolbar sx={TOOLBAR_SX}>
        {!!state.leading && (
          <Box data-slot="leading" sx={LEADING_SX}>
            <TLChild control={state.leading} />
          </Box>
        )}
        <Typography variant="h6" component="h1" noWrap sx={TITLE_SX}>{state.title ?? ''}</Typography>
        {children.length > 0 && (
          <Box data-slot="children" sx={CHILDREN_SX}>
            {children.map(child => <TLChild key={child.controlId} control={child} />)}
          </Box>
        )}
        {state.actions != null && (
          <ButtonDefaults appearance="ghost">
            <Box data-slot="actions" sx={ACTIONS_SX}>
              <TLChild control={state.actions} />
            </Box>
          </ButtonDefaults>
        )}
        {!!state.trailing && (
          <Box data-slot="trailing" sx={TRAILING_SX}>
            <TLChild control={state.trailing} />
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default MuiAppBarAdapter;
