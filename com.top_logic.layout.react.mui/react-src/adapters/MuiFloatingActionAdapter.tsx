import {
  React, useTLState, useChildStates, useChildCommand, useFloatingAnchor, useOverlayClearance, useKeyboardBinding,
  buttonPartAttributes, rootClassName, createPortal, ThemeIcon, TOOLTIP_ATTR, useCloseOnOutsidePress,
  useStandaloneKeyboardScope,
} from 'tl-react-bridge';
import type { TLCellProps, FloatingActionStateJson, ButtonStateJson, ChildControlJson, FloatingEdge } from 'tl-react-bridge';
import Box from '@mui/material/Box';
import Fab from '@mui/material/Fab';
import SpeedDial, { speedDialClasses } from '@mui/material/SpeedDial';
import type { CloseReason, OpenReason } from '@mui/material/SpeedDial';
import SpeedDialAction from '@mui/material/SpeedDialAction';
import type { SxProps, Theme } from '@mui/material/styles';
import { ICON_ALONE_CLASS, ICON_WITH_LABEL_CLASS } from './button-look';

const { useCallback, useEffect, useRef, useState } = React;

/** The command pressing an action (a button control). */
const CMD_CLICK = 'click';

/** The corner of an absent {@link FloatingActionStateJson.position}. */
const DEFAULT_POSITION: FloatingActionStateJson.Position = 'bottom-end';

/** The value of {@link TOOLTIP_ATTR} declaring a plain text as tooltip, followed by the text. */
const TEXT_TOOLTIP_PREFIX = 'text:';

/** The distance of the actions from the edges of their corner, in units of the MUI spacing. */
const INSET = 2;

/** The reason MUI gives for a click on the button of the dial: the only one opening it. */
const REASON_TOGGLE: OpenReason & CloseReason = 'toggle';

/** The placement of the actions against the corner of the given position. */
function placement(position: FloatingActionStateJson.Position, viewport: boolean): SxProps<Theme> {
  const [vertical, horizontal] = position.split('-');
  return {
    position: viewport ? 'fixed' : 'absolute',
    zIndex: 'speedDial',
    // The box of a speed dial spans its closed actions as well; only the buttons take the pointer.
    pointerEvents: 'none',
    [vertical]: theme => theme.spacing(INSET),
    [horizontal === 'start' ? 'insetInlineStart' : 'insetInlineEnd']: theme => theme.spacing(INSET),
    transition: theme => theme.transitions.create('transform'),
  } as SxProps<Theme>;
}

/** A single action takes the pointer within the box of the actions, which does not. */
const FAB_SX: SxProps<Theme> = { pointerEvents: 'auto' };

/** The icon of an extended action, before its label. */
const EXTENDED_ICON_SX: SxProps<Theme> = { display: 'inline-flex', mr: 1 };

/** The state of an action, as far as the adapter reads it. */
type ActionState = Partial<ButtonStateJson>;

/**
 * Binds the keyboard gesture of an action (ButtonStateJson.keyGesture) in the enclosing keyboard
 * scope, as the button control does where it renders itself: the gesture presses the action, a
 * hidden or disabled action declines it.
 */
function ActionGesture({ state, onPress }: { state: ActionState; onPress: () => void }) {
  useKeyboardBinding(state.keyGesture, () => {
    if (state.disabled === true || state.hidden === true) {
      return false;
    }
    onPress();
    return true;
  });
  return null;
}

/**
 * Renders the state of TopLogic floating actions (module name `TLFloatingAction`) with the MUI
 * `Fab` and `SpeedDial`.
 *
 * <p>The actions are button controls the adapter draws itself: it reads their states with
 * `useChildStates` and presses an action by sending it the command `click` with `useChildCommand`.
 * The keyboard gesture of an action presses it as well.</p>
 *
 * <p>Mapping from the control state and the states of the actions:</p>
 * <ul>
 * <li>no action that is not hidden → nothing is rendered; hidden actions are left out;</li>
 * <li>one action → a `Fab` (color `primary`) showing the icon of the action, named by its label,
 *     with the label as tooltip (or the tooltip of the action); an action whose display mode shows
 *     its label, or one without an icon → the `extended` variant with its label (after its icon);
 *     disabled → `disabled`;</li>
 * <li>several actions → a `SpeedDial`: triggerImage → its icon, triggerLabel → `ariaLabel` and the
 *     tooltip of its button; each action → a `SpeedDialAction` with the icon of the action, its label
 *     as accessible name and as tooltip (or the tooltip of the action), disabled → a disabled `Fab`.
 *     As TLFloatingAction, the dial opens on a click of its button only (not when hovered or
 *     focused) and closes on a further click of its button, on Escape wherever the focus is (bringing the focus
 *     back to its button), on a press outside of it and
 *     after an action was pressed;</li>
 * <li>position → the corner of the frame the actions float over, a top corner opens the dial
 *     downwards; the actions are rendered into the rail of the enclosing frame (`useFloatingAnchor`),
 *     outside of a frame over the corner of the browser window;</li>
 * <li>a transient overlay the actions would cover - a snackbar - moves them aside
 *     (`useOverlayClearance`);</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the box of the actions.</li>
 * </ul>
 */
const MuiFloatingActionAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<FloatingActionStateJson>>();
  const actions: ChildControlJson[] = state.actions ?? [];
  const position = state.position ?? DEFAULT_POSITION;
  const edge: FloatingEdge = position.startsWith('top') ? 'top' : 'bottom';

  const actionStates = useChildStates(actions) as readonly ActionState[];
  const sendToChild = useChildCommand();
  const press = useCallback((actionId: string) => {
    sendToChild(actionId, CMD_CLICK);
  }, [sendToChild]);

  const visible = actions
    .map((action, index) => ({ id: action.controlId, state: actionStates[index] ?? {} }))
    .filter(action => action.state.hidden !== true);
  const dial = visible.length > 1;

  const [open, setOpen] = useState(false);
  // The dial opens on a click of its button only, not when hovered or focused, as TLFloatingAction.
  const handleOpen = useCallback((_event: unknown, reason: OpenReason) => {
    if (reason === REASON_TOGGLE) setOpen(true);
  }, []);
  // It closes on a click of its button; leaving it with the pointer or the focus keeps it open. Escape
  // is handled below, wherever the focus is.
  const handleClose = useCallback((_event: unknown, reason: CloseReason) => {
    if (reason === REASON_TOGGLE) setOpen(false);
  }, []);
  const close = useCallback(() => setOpen(false), []);

  // A dial falling back to a single action (the others hidden) is closed when it comes back.
  useEffect(() => {
    if (!dial) setOpen(false);
  }, [dial]);

  const anchor = useFloatingAnchor(edge);
  const rootRef = useRef<HTMLDivElement>(null);
  const shift = useOverlayClearance(rootRef, edge, [anchor, visible.length]);

  useCloseOnOutsidePress(open && dial, [rootRef], close);

  // Escape closes the open dial wherever the focus is, and brings the focus back to its button.
  useStandaloneKeyboardScope(open && dial, {
    ESCAPE: () => {
      rootRef.current?.querySelector<HTMLElement>('.' + speedDialClasses.fab)?.focus();
      close();
      return true;
    },
  });

  const gestures = actions.map((action, index) => (
    <ActionGesture key={action.controlId} state={actionStates[index] ?? {}} onPress={() => press(action.controlId)} />
  ));

  if (state.hidden === true || anchor == null || visible.length === 0) {
    return <>{gestures}</>;
  }

  let content: React.ReactNode;
  if (dial) {
    const triggerTooltip: Record<string, string> = state.triggerLabel
      ? { [TOOLTIP_ATTR]: TEXT_TOOLTIP_PREFIX + state.triggerLabel }
      : {};
    content = (
      <SpeedDial
        ariaLabel={state.triggerLabel ?? ''}
        icon={<ThemeIcon encoded={state.triggerImage} className={ICON_ALONE_CLASS} />}
        direction={edge === 'top' ? 'down' : 'up'}
        open={open}
        onOpen={handleOpen}
        onClose={handleClose}
        FabProps={{ ...triggerTooltip }}
      >
        {visible.map(action => (
          <SpeedDialAction
            key={action.id}
            icon={<ThemeIcon encoded={action.state.image} className={ICON_ALONE_CLASS} />}
            slotProps={{
              fab: {
                id: action.id,
                disabled: action.state.disabled === true,
                'aria-label': action.state.label,
                onClick: () => {
                  press(action.id);
                  setOpen(false);
                },
              },
              tooltip: { title: action.state.tooltip ?? action.state.label ?? '' },
            }}
          />
        ))}
      </SpeedDial>
    );
  } else {
    const action = visible[0];
    // The display mode of a button: the one of its state, else the label alone (as TLButton).
    const mode = action.state.displayMode ?? 'label-only';
    const showIcon = !!action.state.image && mode !== 'label-only';
    const extended = !showIcon || mode !== 'icon-only';
    const attributes = buttonPartAttributes({
      label: action.state.label,
      image: action.state.image,
      display: showIcon ? mode : 'label-only',
      appearance: 'primary',
      tooltip: action.state.tooltip,
    });
    content = (
      <Fab
        id={action.id}
        color="primary"
        variant={extended ? 'extended' : 'circular'}
        disabled={action.state.disabled === true}
        onClick={() => press(action.id)}
        sx={FAB_SX}
        {...attributes}
      >
        {showIcon && !extended && <ThemeIcon encoded={action.state.image} className={ICON_ALONE_CLASS} />}
        {showIcon && extended && (
          <Box component="span" sx={EXTENDED_ICON_SX}>
            <ThemeIcon encoded={action.state.image} className={ICON_WITH_LABEL_CLASS} />
          </Box>
        )}
        {extended && action.state.label}
      </Fab>
    );
  }

  return (
    <>
      {gestures}
      {createPortal(
        <Box
          id={controlId}
          ref={rootRef}
          className={rootClassName(state)}
          data-tl-position={position}
          data-tl-anchor={anchor.viewport ? 'viewport' : undefined}
          sx={placement(position, anchor.viewport)}
          style={shift !== 0 ? { transform: `translateY(${shift}px)` } : undefined}
        >
          {content}
        </Box>,
        anchor.container,
      )}
    </>
  );
};

export default MuiFloatingActionAdapter;
