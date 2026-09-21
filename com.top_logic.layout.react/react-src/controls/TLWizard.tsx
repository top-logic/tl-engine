import {
  React, useTLState, useTLCommand, TLChild, useFillHost, FillProvider, rootClassName,
  useKeyedTransition,
} from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

const { useCallback, useEffect, useRef } = React;

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
 * the direction as a modifier on the root - and leaves the marking of the step arriving and the
 * copy of the step left behind to {@link useKeyedTransition}, which the engine draws a default
 * animation for.
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

  useKeyedTransition({
    keys: activeStepId === null ? [] : [activeStepId],
    node: () => stepRef.current,
    container: () => bodyRef.current,
    enterClass: 'tlWizard__step--entering',
    exitClass: 'tlWizard__step--exiting',
  });

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

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={rootClassName(state, rootClass)}>
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
          <div key={activeStepId ?? ''} className="tlWizard__step" ref={stepRef}>
            {activeChild && <TLChild control={activeChild} />}
          </div>
        </div>
      </div>
    </FillProvider>
  );
};

export default TLWizard;
