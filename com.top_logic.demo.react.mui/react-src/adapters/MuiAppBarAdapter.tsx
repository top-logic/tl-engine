import { React, useTLState, TLChild, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, AppBarStateJson, ChildControlJson } from 'tl-react-bridge';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
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

/** The title: as wide as its text, truncated when the bar is short of room. */
const TITLE_SX: SxProps<Theme> = { flex: '0 1 auto', minWidth: 0 };

/**
 * Renders the state of a TopLogic app bar (module name `TLAppBar`) with the MUI `AppBar`
 * (`position="static"`, color `default`) and its `Toolbar`.
 *
 * <p>The parts keep the slot elements and classes of the design system's app bar
 * (`tlAppBar__leading`, `__children`, `__actions`, `__trailing`): they decide who gives way when
 * the bar is short of room (the actions toolbar folds its commands into its overflow menu before
 * the title truncates), push the actions and the trailing part to the end of the bar, and drop an
 * actions area that shows no command.</p>
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>title → a `Typography` (`h6`, as heading `h1`), truncated with an ellipsis;</li>
 * <li>leading, children, actions, trailing → their slots, each control rendered through
 *     {@link TLChild};</li>
 * <li>variant `flat` (default) → elevation 0; `elevated` → the elevation of an MUI app bar;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `AppBar`.</li>
 * </ul>
 *
 * <p>Not reproduced: the ghost look TLAppBar gives the buttons of its actions (through the button
 * defaults of TopLogic, which the bridge does not export); they keep the look of a button of
 * their own.</p>
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
          <div className="tlAppBar__leading">
            <TLChild control={state.leading} />
          </div>
        )}
        <Typography variant="h6" component="h1" noWrap sx={TITLE_SX}>{state.title ?? ''}</Typography>
        {children.length > 0 && (
          <div className="tlAppBar__children">
            {children.map(child => <TLChild key={child.controlId} control={child} />)}
          </div>
        )}
        {state.actions != null && (
          <div className="tlAppBar__actions">
            <TLChild control={state.actions} />
          </div>
        )}
        {!!state.trailing && (
          <div className="tlAppBar__trailing">
            <TLChild control={state.trailing} />
          </div>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default MuiAppBarAdapter;
