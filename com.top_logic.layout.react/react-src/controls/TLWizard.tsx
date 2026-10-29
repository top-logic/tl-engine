import {
  React, useTLState, useTLCommand, useI18N, TLChild, useFillHost, FillProvider, rootClassName, stateAttr,
  useKeyedTransition,
  ThemeIcon,
} from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';

const I18N_KEYS = { 'js.wizard.steps': 'Steps' };

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
 * the direction as `data-tl-state~="backward"` on the root - and leaves the marking of the step arriving and the
 * copy of the step left behind to {@link useKeyedTransition}. The design system package draws the default slide.
 *
 * Takes part in the fill contract as a container: a wizard whose step content fills fills its own
 * container, so the content spans the available height instead of the wizard collapsing to it.
 */
const TLWizard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
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
    state: true,
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

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={rootClassName(state, 'tl-wizard', fillClass)}
        data-tl-state={stateAttr(direction === 'backward' && 'backward')}>
        {hasHeader && (
          <div className="tl-wizard__header">
            {showCounter && (
              // While the list stands, it says with aria-current where the user is and the counter repeats it for the
              // eye only; without the list the counter is the only place that says it, so it is read.
              <span className="tl-wizard__counter tl-type-label tl-nums" aria-hidden={showStepList ? 'true' : undefined}>
                {counterText(activeIndex, steps.length)}
              </span>
            )}
            {showStepList && (
              <ol className="tl-steps" aria-label={i18n['js.wizard.steps']}>
                {steps.map((step, position) => {
                  const status = stepState(position, activeIndex);
                  return (
                    <li
                      key={step.key}
                      className="tl-steps__item"
                      data-tl-state={stateAttr(status === 'done' && 'done')}
                      aria-current={status === 'current' ? 'step' : undefined}
                    >
                      <span className="tl-steps__disc tl-type-label tl-nums" aria-hidden="true">{position + 1}</span>
                      {status === 'done' ? (
                        <button type="button" className={buttonClassName({ appearance: 'link' })} onClick={() => goTo(step.key)}>
                          {step.icon && <ThemeIcon encoded={step.icon} className="tl-button__icon tl-icon-sm" />}
                          <span className="tl-button__label">{step.label}</span>
                        </button>
                      ) : (
                        <span className={'tl-steps__name ' + (status === 'current' ? 'tl-type-body-strong' : 'tl-type-body')}>
                          {step.icon && <ThemeIcon encoded={step.icon} className="tl-icon-sm" />}
                          {step.label}
                        </span>
                      )}
                    </li>
                  );
                })}
              </ol>
            )}
            {progress && (
              <div className="tl-wizard__progress">
                <TLChild control={progress} />
              </div>
            )}
          </div>
        )}
        <div className="tl-wizard__body" ref={bodyRef}>
          <div key={activeStepId ?? ''} className="tl-wizard__step" ref={stepRef}>
            {activeChild && <TLChild control={activeChild} />}
          </div>
        </div>
      </div>
    </FillProvider>
  );
};

export default TLWizard;
