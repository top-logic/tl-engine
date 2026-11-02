import {
  React, useTLState, TLChild, ButtonDefaults, usePart, rootClassName, stateAttr, createPortal,
  useChildStates, useFloatingAnchor, useOverlayClearance, useCloseOnOutsidePress, useStandaloneKeyboardScope,
} from 'tl-react-bridge';
import type { TLCellProps, FloatingActionStateJson, ChildControlJson, FloatingEdge } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

/** The corner of an absent {@link FloatingActionStateJson.position}. */
const DEFAULT_POSITION: FloatingActionStateJson.Position = 'bottom-end';

/** What the actions can be pressed with: an enabled button, or a link. */
const PRESSABLE = 'button:not(:disabled), a[href]';

/**
 * Actions floating over a corner of the frame they stand in (FloatingActionStateJson).
 *
 * <p>The actions are rendered into the rail of the enclosing frame ({@code useFloatingAnchor}): the
 * body of the panel or the window around them, so that they stay in place while the body scrolls;
 * outside of a frame, they float over the corner of the browser window. A transient overlay they
 * would cover - a snackbar - moves them aside ({@code useOverlayClearance}).</p>
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

  // The vertical shift keeping the actions clear of the transient overlays.
  const shift = useOverlayClearance(rootRef, edge, [anchor, visibleCount, expanded]);

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
