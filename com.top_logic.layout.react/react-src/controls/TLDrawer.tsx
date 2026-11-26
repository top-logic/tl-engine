import {
  React, useTLState, useTLCommand, TLChild, useI18N, useStandaloneKeyboardScope, FillBarrier,
  rootClassName, useFocusTrap, usePart, ICON_CLOSE, SurfaceProvider,
} from 'tl-react-bridge';
import type { TLCellProps, Surface } from 'tl-react-bridge';

const { useCallback, useRef } = React;

/** What a drawer is for the components inside: a surface floating above the page, with the insets of a page. */
const OVERLAY_SURFACE: Surface = { level: 'overlay', inset: 'page' };

const I18N_KEYS = {
  'js.drawer.close': 'Close',
};

/**
 * A slide-in panel from the edge of the viewport or of the container it is rendered in.
 *
 * Without `modal`, the content the drawer overlays stays scrollable and clickable, so focus is moved
 * into the drawer when it opens and returned to its origin when it closes, but never confined to it.
 * A modal drawer puts a scrim before itself, which covers the area the drawer is anchored in and
 * closes the drawer on a click, is a dialog for assistive technology, and confines the focus while
 * it is open.
 *
 * State:
 * - open: boolean
 * - position: "left" | "right" | "bottom"  (default: "right")
 * - size: "narrow" | "medium" | "wide"  (default: "medium")
 * - anchor: "viewport" | "container"  (default: "viewport")
 * - width: number | null  - explicit extent in pixels, overriding the named size
 * - title: string | null  (null = no header)
 * - modal: boolean  (default: false)
 * - label: string | null  - the accessible name of a drawer without title
 * - child: ChildDescriptor
 */
const TLDrawer: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const rootRef = useRef<HTMLElement | null>(null);

  const open = state.open === true;
  const position = (state.position as string) ?? 'right';
  const size = (state.size as string) ?? 'medium';
  const anchor = (state.anchor as string) ?? 'viewport';
  const width = typeof state.width === 'number' ? state.width : null;
  const title = (state.title as string) ?? null;
  const child = state.child;
  const modal = state.modal === true;
  const label = typeof state.label === 'string' && state.label ? state.label : undefined;

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const Button = usePart('Button');

  // Escape closes the drawer (via the shared keyboard dispatcher) - unless it is clearing a search
  // field that has text in it, which is what Escape does there first.
  useStandaloneKeyboardScope(open, {
    ESCAPE: () => {
      const active = document.activeElement as HTMLInputElement | null;
      if (active?.type === 'search' && active.value) return false;
      handleClose();
      return true;
    },
  });

  // Opening hands the keyboard to the drawer, closing gives it back to wherever it came from (typically
  // the list the drawer details). Without a trap unless modal: what lies underneath stays reachable.
  // Without scrolling: the drawer is still parked outside the clipped box of its area while it slides in.
  useFocusTrap(open, rootRef, 'first', { trap: modal, preventScroll: true });

  const side = position === 'left' ? 'start' : position === 'bottom' ? 'bottom' : 'end';
  const className = [
    'tl-drawer',
    `tl-drawer--${side}`,
    `tl-drawer--${size}`,
    anchor === 'container' ? 'tl-drawer--container' : '',
  ].filter(Boolean).join(' ');
  const titleId = controlId + '-title';

  // A bottom drawer's extent is its height, a side drawer's its width; the stylesheet caps both at
  // the extent of the area the drawer is anchored in.
  const style = width === null ? undefined
    : position === 'bottom' ? { height: `${width}px` } : { width: `${width}px` };

  const drawer = (
    <aside
      id={controlId}
      className={rootClassName(state, className)}
      data-tl-state={open ? 'open' : undefined}
      inert={!open}
      role={modal ? 'dialog' : undefined}
      aria-modal={modal ? true : undefined}
      aria-labelledby={title !== null ? titleId : undefined}
      aria-label={title === null ? label : undefined}
      tabIndex={-1}
      ref={rootRef}
      style={style}
    >
      {title !== null && (
        <div className="tl-titlebar">
          <h2 className="tl-titlebar__title tl-type-heading-sm" id={titleId}>{title}</h2>
          <Button
            label={i18n['js.drawer.close']}
            image={ICON_CLOSE}
            display="icon-only"
            appearance="ghost"
            small
            onClick={handleClose}
          />
        </div>
      )}
      <div className="tl-drawer__body tl-region">
        <SurfaceProvider value={OVERLAY_SURFACE}>
          <FillBarrier>
            {!!child && <TLChild control={child} />}
          </FillBarrier>
        </SurfaceProvider>
      </div>
    </aside>
  );

  // A pointer's way out of a modal drawer. Keyboard and screen reader users have Escape and the close
  // button. Always the first of two children, so that the drawer itself stays mounted when it appears.
  const scrimClass = anchor === 'container' ? 'tl-drawer__scrim' : 'tl-drawer__scrim tl-drawer__scrim--viewport';
  return (
    <>
      {modal && open ? <div className={scrimClass} aria-hidden="true" onClick={handleClose} /> : null}
      {drawer}
    </>
  );
};

export default TLDrawer;
