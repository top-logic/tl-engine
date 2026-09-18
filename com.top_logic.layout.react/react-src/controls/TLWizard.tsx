import { React, useTLState, useTLCommand, TLChild, useFillHost, FillProvider } from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

const { useCallback } = React;

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
 *
 * A step already done is a button that jumps back to it; a step still ahead is not, because the way
 * there leads through the steps in between.
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

  const goTo = useCallback((stepId: string) => {
    sendCommand('gotoStep', { stepId });
  }, [sendCommand]);

  const hasHeader = showCounter || progress || showStepList;

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={fillClass ? 'tlWizard ' + fillClass : 'tlWizard'}>
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
        <div className="tlWizard__body">
          <div className="tlWizard__step">
            {activeChild && <TLChild control={activeChild} />}
          </div>
        </div>
      </div>
    </FillProvider>
  );
};

export default TLWizard;
