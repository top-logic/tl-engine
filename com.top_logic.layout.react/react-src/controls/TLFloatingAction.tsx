import {
  React, useTLState, TLChild, ButtonDefaults, usePart, rootClassName, stateAttr, createPortal, tokenPx,
  useChildStates, useFloatingAnchor, useTransientOverlayRects, useCloseOnOutsidePress, useStandaloneKeyboardScope,
} from 'tl-react-bridge';
import type { TLCellProps, FloatingActionStateJson, ChildControlJson, FloatingEdge } from 'tl-react-bridge';

const { useCallback, useEffect, useLayoutEffect, useRef, useState } = React;

/** The corner of an absent {@link FloatingActionStateJson.position}. */
const DEFAULT_POSITION: FloatingActionStateJson.Position = 'bottom-end';

/** The space kept between the actions and a transient overlay they move away from. */
const CLEARANCE_TOKEN = '--tl-space-sm';

/** The space of {@link CLEARANCE_TOKEN} without the design system. */
const CLEARANCE_FALLBACK_PX = 12;

/** What the actions can be pressed with: an enabled button, or a link. */
const PRESSABLE = 'button:not(:disabled), a[href]';

/** The part of a box the clearance computation reads: its vertical and horizontal extent. */
export interface Box {
  readonly top: number;
  readonly bottom: number;
  readonly left: number;
  readonly right: number;
}

/**
 * The vertical shift moving a box at the given edge of its frame out of the way of the given
 * overlays: a box at the bottom edge moves up (a negative shift) until it stands the given gap above
 * every overlay it overlaps, a box at the top edge moves down below them. A box overlapping no
 * overlay keeps its place (zero). An overlay of no extent, one not rendered, is ignored.
 *
 * @param box The box at its place, unshifted.
 * @param overlays The areas of the transient overlays.
 * @param edge The edge of the frame the box is placed against.
 * @param gap The space kept to an overlay moved away from.
 */
export function clearance(box: Box, overlays: readonly Box[], edge: FloatingEdge, gap: number): number {
  let shift = 0;
  for (const overlay of overlays) {
    if (overlay.bottom <= overlay.top || overlay.right <= overlay.left) continue;
    const across = box.left < overlay.right && box.right > overlay.left;
    const along = box.top < overlay.bottom && box.bottom > overlay.top;
    if (!across || !along) continue;
    shift = edge === 'bottom'
      ? Math.min(shift, overlay.top - gap - box.bottom)
      : Math.max(shift, overlay.bottom + gap - box.top);
  }
  return shift;
}

/**
 * Actions floating over a corner of the frame they stand in (FloatingActionStateJson).
 *
 * <p>The actions are rendered into the rail of the enclosing frame ({@code useFloatingAnchor}): the
 * body of the panel or the window around them, so that they stay in place while the body scrolls;
 * outside of a frame, they float over the corner of the browser window. A transient overlay they
 * would cover - a snackbar - moves them aside ({@code useTransientOverlayRects}, {@link clearance}).</p>
 *
 * <p>The actions are button controls rendered with {@link TLChild}; their states decide on the
 * presentation ({@code useChildStates}): no visible action renders nothing, one is shown as a round
 * floating action button (with its label, if its display mode asks for it), several as a speed
 * dial. The trigger of the dial is drawn by the part {@code Button}, marked with
 * {@code aria-expanded}; the dial closes on Escape, on a press outside of it and after an action
 * was pressed. Opened from the keyboard, it moves the focus to its first action; closing it while
 * the focus is on an action brings the focus back to the trigger.</p>
 */
const TLFloatingAction: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<FloatingActionStateJson>>();
  const actions: ChildControlJson[] = state.actions ?? [];
  const position = state.position ?? DEFAULT_POSITION;
  const edge: FloatingEdge = position === 'top-start' || position === 'top-end' ? 'top' : 'bottom';

  const actionStates = useChildStates(actions);
  const hidden = actionStates.map(actionState => actionState.hidden === true);
  const visibleCount = hidden.filter(isHidden => !isHidden).length;
  const dial = visibleCount > 1;

  const anchor = useFloatingAnchor(edge);
  const overlays = useTransientOverlayRects();

  const [open, setOpen] = useState(false);
  const expanded = open && dial;
  const rootRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLElement | null>(null);
  const actionsRef = useRef<HTMLDivElement>(null);

  // Opened from the keyboard, the dial moves the focus to its first action once it is shown.
  const focusOnOpen = useRef(false);

  const close = useCallback(() => {
    const actionsElement = actionsRef.current;
    if (actionsElement != null && actionsElement.contains(document.activeElement)) {
      triggerRef.current?.focus();
    }
    setOpen(false);
  }, []);

  const toggle = useCallback((event: React.MouseEvent<HTMLElement>) => {
    if (open) {
      close();
      return;
    }
    // A click without a pointer press (detail 0) is the keyboard activation of the trigger.
    focusOnOpen.current = event.detail === 0;
    setOpen(true);
  }, [open, close]);

  useEffect(() => {
    if (!expanded || !focusOnOpen.current) return;
    focusOnOpen.current = false;
    actionsRef.current?.querySelector<HTMLElement>(PRESSABLE)?.focus();
  }, [expanded]);

  // A dial falling back to a single action (the others hidden) is closed when it comes back.
  useEffect(() => {
    if (!dial) setOpen(false);
  }, [dial]);

  useStandaloneKeyboardScope(expanded, {
    ESCAPE: () => {
      triggerRef.current?.focus();
      close();
      return true;
    },
  });

  useCloseOnOutsidePress(expanded, [rootRef], close);

  // An action pressed closes the dial; the action's own click handler has sent its command already.
  const handleActionsClick = useCallback((event: React.MouseEvent<HTMLElement>) => {
    if (event.target instanceof Element && event.target.closest(PRESSABLE)) {
      close();
    }
  }, [close]);

  // The vertical shift keeping the actions clear of the transient overlays, measured against their
  // unshifted place.
  const [shift, setShift] = useState(0);
  const shiftRef = useRef(0);
  shiftRef.current = shift;
  useLayoutEffect(() => {
    const root = rootRef.current;
    if (root == null) return;
    const rect = root.getBoundingClientRect();
    const unshifted: Box = {
      top: rect.top - shiftRef.current,
      bottom: rect.bottom - shiftRef.current,
      left: rect.left,
      right: rect.right,
    };
    const next = clearance(unshifted, overlays, edge, tokenPx(root, CLEARANCE_TOKEN, CLEARANCE_FALLBACK_PX));
    if (next !== shiftRef.current) {
      setShift(next);
    }
  }, [overlays, edge, anchor, visibleCount, expanded]);

  const Button = usePart('Button');

  if (anchor == null || visibleCount === 0) {
    return null;
  }

  const actionsId = `${controlId}-actions`;
  const content = (
    <div
      id={controlId}
      ref={rootRef}
      className={rootClassName(state, 'tl-fab')}
      data-tl-position={position}
      data-tl-anchor={anchor.viewport ? 'viewport' : undefined}
      data-tl-state={stateAttr(expanded && 'open')}
      style={shift !== 0 ? { transform: `translateY(${shift}px)` } : undefined}
    >
      {dial ? (
        <>
          <div className="tl-fab__main">
            <Button
              ref={triggerRef}
              label={state.triggerLabel}
              image={state.triggerImage}
              display="icon-only"
              appearance="primary"
              aria-expanded={expanded}
              aria-controls={actionsId}
              onClick={toggle}
            />
          </div>
          <div id={actionsId} ref={actionsRef} className="tl-fab__actions" hidden={!expanded}
            onClick={handleActionsClick}>
            <ButtonDefaults appearance="secondary" iconOnly>
              {actions.map((action, index) => (
                <div key={action.controlId} className="tl-fab__action" hidden={hidden[index]}>
                  <TLChild control={action} />
                </div>
              ))}
            </ButtonDefaults>
          </div>
        </>
      ) : (
        <ButtonDefaults appearance="primary">
          {actions.map((action, index) => (
            <div key={action.controlId} className="tl-fab__main" hidden={hidden[index]}>
              <TLChild control={action} />
            </div>
          ))}
        </ButtonDefaults>
      )}
    </div>
  );
  return createPortal(content, anchor.container);
};

export default TLFloatingAction;
