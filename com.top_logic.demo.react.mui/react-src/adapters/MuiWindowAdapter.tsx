import {
  React, useTLState, useTLCommand, useI18N, TLChild, KeyboardScopeProvider, useKeyboardBinding, useFocusTrap,
  FillBarrier, ButtonDefaults, rootClassName, tooltipProps,
} from 'tl-react-bridge';
import type { TLCellProps, WindowStateJson } from 'tl-react-bridge';
import Paper from '@mui/material/Paper';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import IconButton from '@mui/material/IconButton';
import type { SxProps, Theme } from '@mui/material/styles';
import { RESIZE_CURSORS, RESIZE_HANDLES, RESIZE_HANDLE_STYLES, useWindowFrame } from './window-frame';
import { CloseIcon, MaximizeIcon, RestoreIcon } from './window-icons';

const { useCallback } = React;

/** Command closing the window. */
const CMD_CLOSE = 'close';

/** The gesture closing a window that can be closed. */
const GESTURE_ESCAPE = 'ESCAPE';

/** The elevation of a dialog in Material UI. */
const WINDOW_ELEVATION = 24;

const I18N_KEYS = {
  'js.window.close': 'Close',
  'js.window.maximize': 'Maximize',
  'js.window.restore': 'Restore',
};

/**
 * The window: a column of title bar, body and footer, at most as wide as the browser window. The
 * position and the size come from the window frame as inline style.
 */
const WINDOW_SX: SxProps<Theme> = {
  position: 'relative',
  display: 'flex',
  flexDirection: 'column',
  maxWidth: 'calc(100vw - 32px)',
  maxHeight: '80vh',
  overflow: 'hidden',
  outline: 'none',
};

/** The title bar: the title, the toolbar of the title bar and the buttons; the window's grip. */
const TITLE_SX: SxProps<Theme> = {
  display: 'flex',
  alignItems: 'center',
  gap: 1,
  flexShrink: 0,
  cursor: 'grab',
  userSelect: 'none',
  '&:active': { cursor: 'grabbing' },
};

/** The title bar of a maximized window, which cannot be moved. */
const TITLE_MAXIMIZED_SX: SxProps<Theme> = { cursor: 'default', '&:active': { cursor: 'default' } };

/** The title, shortened with an ellipsis where it does not fit. */
const TITLE_TEXT_STYLE: React.CSSProperties = {
  flex: 1,
  minWidth: 0,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
};

/** The toolbar of the title bar. */
const TOOLBAR_STYLE: React.CSSProperties = { display: 'flex', alignItems: 'center' };

/**
 * The body: flush (the content owns its inset), scrolling what does not fit, separated from title
 * bar and footer by lines.
 */
const BODY_SX: SxProps<Theme> = { padding: 0, flex: 1, overflow: 'auto' };

/**
 * The footer: the collapsing toolbar it consists of takes the whole width and gives it up down to
 * its overflow trigger. A toolbar without a command renders nothing, and the footer disappears.
 */
const FOOTER_SX: SxProps<Theme> = { flexShrink: 0, minWidth: 0, '&:empty': { display: 'none' } };

/** A resize handle: an invisible strip over an edge, or a square over a corner. */
const HANDLE_STYLE: React.CSSProperties = { position: 'absolute' };

/**
 * Binds Escape to closing the window in the window's keyboard scope. Mounted before the content,
 * so that an action of the content bound to Escape (mounted later) wins.
 */
const EscapeToClose: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  useKeyboardBinding(GESTURE_ESCAPE, () => {
    onClose();
    return true;
  });
  return null;
};

/**
 * Renders the state of a TopLogic window (module name `TLWindow`) with the surface parts of an MUI
 * dialog: a `Paper` with a `DialogTitle`, a `DialogContent` and `DialogActions`, and `IconButton`s
 * in the title bar.
 *
 * <p>The window stays TopLogic's in everything but its look: the backdrop around it (TLDialog,
 * stacked by the dialog manager) centers it, and the adapter keeps the behaviour of TLWindow with
 * the bridge's means: the modal keyboard scope ({@link KeyboardScopeProvider}) with Escape bound to
 * closing, the focus trap ({@link useFocusTrap}, focus starting at the first field), and moving,
 * resizing and maximizing (see `useWindowFrame`). MUI's `Dialog` is not used: its modal brings a
 * focus trap, an Escape handling, a scroll lock and a stacking of its own, each of which TopLogic
 * already owns.</p>
 *
 * <p>Mapping from the control state to the MUI parts:</p>
 * <ul>
 * <li>title → the text of the `DialogTitle`, which names the window (`aria-labelledby`);</li>
 * <li>toolbar → in the title bar, rendered through {@link TLChild};</li>
 * <li>child → the `DialogContent`, flush and scrolling, rendered through {@link TLChild} behind a
 *     {@link FillBarrier} (the body is bounded by the window);</li>
 * <li>footer → the `DialogActions`, rendered through {@link TLChild}: the toolbar of the actions
 *     and the button-bar commands, whose buttons take the appearance `secondary` as in TLWindow
 *     ({@link ButtonDefaults}). A footer without commands is not shown;</li>
 * <li>width, height, customWidth, customHeight → the size of the window, as in TLWindow: the size
 *     the user gave the window last replaces the configured one while it fits into the browser
 *     window;</li>
 * <li>resizable → resize handles at the edges and corners (a resize sends `resize` with `width` and
 *     `height`, a double click on a handle `resetSize`), and the maximize `IconButton`, also
 *     reached by a double click on the title bar;</li>
 * <li>closable → the close `IconButton` sends `close`, as does Escape; a window that cannot be
 *     closed shows the button disabled and leaves Escape to the scope around it;</li>
 * <li>hidden → the window is not rendered; the configured CSS class → className of the `Paper`.</li>
 * </ul>
 *
 * <p>Not used: toolbarButtons (TLWindow renders no such buttons either).</p>
 */
const MuiWindowAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<WindowStateJson>>();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const frame = useWindowFrame(state, sendCommand);

  const resizable = state.resizable === true;
  const closable = state.closable !== false;
  const hidden = state.hidden === true;

  const handleClose = useCallback(() => {
    sendCommand(CMD_CLOSE);
  }, [sendCommand]);

  useFocusTrap(!hidden, frame.windowRef, 'field');

  if (hidden) {
    return null;
  }

  const titleId = controlId + '-title';
  const maximized = frame.maximized;
  const maximizeLabel = maximized ? i18n['js.window.restore'] : i18n['js.window.maximize'];
  const closeLabel = i18n['js.window.close'];

  return (
    <KeyboardScopeProvider modal>
      {closable && <EscapeToClose onClose={handleClose} />}
      <Paper
        id={controlId}
        ref={frame.windowRef}
        elevation={WINDOW_ELEVATION}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className={rootClassName(state)}
        style={frame.style}
        sx={WINDOW_SX}
      >
        <DialogTitle
          component="div"
          sx={[TITLE_SX, maximized && TITLE_MAXIMIZED_SX].filter(Boolean) as SxProps<Theme>}
          onPointerDown={maximized ? undefined : frame.onTitlePointerDown}
          onDoubleClick={resizable ? frame.toggleMaximize : undefined}
        >
          <span id={titleId} style={TITLE_TEXT_STYLE}>{state.title ?? ''}</span>
          {!!state.toolbar && (
            <div style={TOOLBAR_STYLE}>
              <TLChild control={state.toolbar} />
            </div>
          )}
          {resizable && (
            <IconButton size="small" onClick={frame.toggleMaximize} aria-label={maximizeLabel}
              {...tooltipProps(maximizeLabel)}>
              {maximized ? <RestoreIcon /> : <MaximizeIcon />}
            </IconButton>
          )}
          <IconButton size="small" onClick={handleClose} disabled={!closable} aria-label={closeLabel}
            {...tooltipProps(closeLabel)}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent dividers sx={BODY_SX}>
          <FillBarrier>
            <TLChild control={state.child} />
          </FillBarrier>
        </DialogContent>
        {!!state.footer && (
          <DialogActions sx={FOOTER_SX}>
            <ButtonDefaults appearance="secondary">
              <TLChild control={state.footer} />
            </ButtonDefaults>
          </DialogActions>
        )}
        {resizable && !maximized && RESIZE_HANDLES.map(dir => (
          <div
            key={dir}
            data-resize={dir}
            style={{ ...HANDLE_STYLE, ...RESIZE_HANDLE_STYLES[dir], cursor: RESIZE_CURSORS[dir] }}
            onPointerDown={event => frame.onResizePointerDown(dir, event)}
            onDoubleClick={frame.resetSize}
          />
        ))}
      </Paper>
    </KeyboardScopeProvider>
  );
};

export default MuiWindowAdapter;
