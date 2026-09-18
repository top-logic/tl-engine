import { React, useTLState, useTLCommand, TLChild, useFillHost, FillProvider } from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

const { useCallback, useEffect, useLayoutEffect, useRef, useState } = React;

/**
 * How long a step wrapper keeps its transition class when no animation runs at all - a theme
 * without one, or a viewer who asked for reduced motion. The class is display state, so it must not
 * survive the transition it describes even where nothing animates.
 */
const TRANSITION_FALLBACK_MS = 1000;

interface StepInfo {
  key: string;
  label: string;
  icon?: string;
}

/**
 * The position of the current step against the number of steps, e.g. "02 — 05".
 *
 * Both numbers are padded to the same width, so the counter keeps its size while the user walks
 * through the wizard instead of shifting whatever sits beside it.
 */
function counterText(index: number, total: number): string {
  const width = String(total).length;
  return String(index + 1).padStart(width, '0') + ' — ' + String(total).padStart(width, '0');
}

/**
 * The state a step is in relative to the step displayed.
 */
function stepState(position: number, activeIndex: number): string {
  if (position < activeIndex) {
    return 'done';
  }
  return position === activeIndex ? 'current' : 'upcoming';
}

/**
 * Renders a multi-step flow: an indicator saying where in the sequence the user is, above the
 * content of the step displayed.
 *
 * State:
 * - steps: StepInfo[] - one entry per step, in the order the wizard walks them
 * - activeIndex: number - position of the step displayed, -1 for a wizard without steps
 * - activeChild: ChildDescriptor | null - the content of that step
 * - counter: boolean - whether the indicator counts the step against the number of steps
 * - progress: ChildDescriptor | null - the bar showing how far through the wizard the step is
 * - stepList: boolean - whether the indicator lists the steps by name
 * - direction: "forward" | "backward" - which way the display last moved
 * - autoAdvance: number | null - how long this step stays before the wizard moves on by itself
 *
 * A step already done is a button that jumps back to it; a step still ahead is not, because the way
 * there leads through the steps in between.
 *
 * The step change is a transition the stylesheet owns: this component only says what is happening -
 * the direction as a modifier on the root, `--entering` on the step arriving and, over it, an inert
 * copy of the step leaving marked `--exiting`. The copy is DOM, not a control: the control of the
 * step left behind is disposed on the server, so there is nothing left to render, and a snapshot is
 * what can still be animated out. It is hidden from assistive technology and takes no input.
 *
 * Takes part in the fill contract as a container: a wizard whose step content fills fills its own
 * container, so the content spans the available height instead of the wizard collapsing to it.
 */
const TLWizard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const [fillClass, fillHost] = useFillHost();

  const steps = (state.steps as StepInfo[]) ?? [];
  const activeIndex = typeof state.activeIndex === 'number' ? state.activeIndex : -1;
  const activeChild = state.activeChild as ChildDescriptor | undefined;
  const progress = state.progress as ChildDescriptor | undefined;
  const showCounter = state.counter === true && steps.length > 0;
  const showStepList = state.stepList === true && steps.length > 0;
  const direction = state.direction === 'backward' ? 'backward' : 'forward';
  const autoAdvance = typeof state.autoAdvance === 'number' ? state.autoAdvance : null;
  const activeStepId = activeIndex >= 0 && steps[activeIndex] ? steps[activeIndex].key : null;

  const goTo = useCallback((stepId: string) => {
    sendCommand('gotoStep', { stepId });
  }, [sendCommand]);

  // --- The step change as a transition ------------------------------------------------------

  const bodyRef = useRef<HTMLDivElement | null>(null);
  const stepRef = useRef<HTMLDivElement | null>(null);
  const displayedRef = useRef<string | null>(null);
  const exitingRef = useRef<HTMLDivElement | null>(null);
  const [entering, setEntering] = useState(false);

  /** Drops the copy of the step left behind, whenever it has finished leaving. */
  const dropExiting = useCallback((event?: AnimationEvent) => {
    const exiting = exitingRef.current;
    if (event && event.target !== exiting) {
      // An animation of something inside the copy, not the copy leaving.
      return;
    }
    exitingRef.current = null;
    if (exiting && exiting.parentNode) {
      exiting.parentNode.removeChild(exiting);
    }
  }, []);

  // Runs before the browser paints the step that has arrived, so the copy of the one left behind is
  // in place by the time either of them is seen.
  useLayoutEffect(() => {
    if (displayedRef.current === activeStepId) {
      return;
    }
    const previous = stepRef.current;
    const body = bodyRef.current;
    const hadStep = displayedRef.current !== null;
    displayedRef.current = activeStepId;

    // One copy at a time: a second change while the first is still leaving replaces it.
    dropExiting();

    if (hadStep && previous && body) {
      const copy = previous.cloneNode(true) as HTMLDivElement;
      // The copy is a picture, not a control: nothing may address it by the ids of what it copied.
      copy.removeAttribute('id');
      copy.querySelectorAll('[id]').forEach((element) => element.removeAttribute('id'));
      copy.classList.remove('tlWizard__step--entering');
      copy.classList.add('tlWizard__step--exiting');
      copy.setAttribute('aria-hidden', 'true');
      copy.setAttribute('inert', '');
      copy.addEventListener('animationend', dropExiting);
      exitingRef.current = copy;
      body.appendChild(copy);
    }

    // The step a wizard opens on was not moved to, so it arrives without a transition.
    setEntering(hadStep && activeStepId !== null);
  }, [activeStepId, dropExiting]);

  // The transition classes are display state: they go once the transition is over, and equally once
  // it is clear that none is running.
  useEffect(() => {
    if (!entering && !exitingRef.current) {
      return undefined;
    }
    const timer = window.setTimeout(() => {
      setEntering(false);
      dropExiting();
    }, TRANSITION_FALLBACK_MS);
    return () => window.clearTimeout(timer);
  }, [entering, activeStepId, dropExiting]);

  useEffect(() => () => dropExiting(), [dropExiting]);

  // --- A step that moves on by itself ---------------------------------------------------------

  useEffect(() => {
    if (autoAdvance === null || activeStepId === null) {
      return undefined;
    }
    const timer = window.setTimeout(() => {
      sendCommand('advanceStep', { stepId: activeStepId });
    }, autoAdvance);
    return () => window.clearTimeout(timer);
  }, [autoAdvance, activeStepId, sendCommand]);

  const hasHeader = showCounter || progress || showStepList;
  const rootClass = ['tlWizard', 'tlWizard--' + direction, fillClass].filter(Boolean).join(' ');
  const stepClass = 'tlWizard__step' + (entering ? ' tlWizard__step--entering' : '');

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={rootClass}>
        {hasHeader && (
          <div className="tlWizard__header">
            {showCounter && (
              <span className="tlWizard__counter">{counterText(activeIndex, steps.length)}</span>
            )}
            {progress && (
              <div className="tlWizard__progress">
                <TLChild control={progress} />
              </div>
            )}
            {showStepList && (
              <ol className="tlWizard__steps">
                {steps.map((step, position) => {
                  const status = stepState(position, activeIndex);
                  const className = 'tlWizard__stepItem tlWizard__stepItem--' + status;
                  return (
                    <li key={step.key} className={className} aria-current={status === 'current' ? 'step' : undefined}>
                      {status === 'done' ? (
                        <button type="button" className="tlWizard__stepButton" onClick={() => goTo(step.key)}>
                          {step.icon && <ThemeIcon encoded={step.icon} className="tlWizard__stepIcon" />}
                          {step.label}
                        </button>
                      ) : (
                        <span className="tlWizard__stepName">
                          {step.icon && <ThemeIcon encoded={step.icon} className="tlWizard__stepIcon" />}
                          {step.label}
                        </span>
                      )}
                    </li>
                  );
                })}
              </ol>
            )}
          </div>
        )}
        <div className="tlWizard__body" ref={bodyRef}>
          <div
            key={activeStepId ?? ''}
            className={stepClass}
            ref={stepRef}
            onAnimationEnd={(event) => {
              if (event.target === event.currentTarget) {
                setEntering(false);
              }
            }}
          >
            {activeChild && <TLChild control={activeChild} />}
          </div>
        </div>
      </div>
    </FillProvider>
  );
};

export default TLWizard;
