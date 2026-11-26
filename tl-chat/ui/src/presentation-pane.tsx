import {
  ICON_CLOSE,
  ICON_PREVIOUS,
  React,
  rootClassName,
  TLChild,
  useI18N,
  usePart,
  useTLCommand,
  useTLState,
} from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'

/** State key of the history of the pane; the server's `PresentationPaneControl.STEPS`. */
export const STEPS = 'steps'

/** Command returning to the step below; the server's `PresentationPaneControl.BACK_COMMAND`. */
export const CMD_BACK = 'back'

/** Command emptying the pane; the server's `PresentationPaneControl.CLOSE_COMMAND`. */
export const CMD_CLOSE = 'close'

const I18N_KEYS = {
  'js.presentationPane.back': 'Back',
  'js.presentationPane.close': 'Close',
}

/** How a step of the history shows its objects. */
export type PaneStepKind = 'table' | 'chart' | 'form'

/** One step of the history, as the server serialized it. */
export interface PaneStep {
  /** Unique within the pane's life; what keeps a step's mounted control where it is. */
  id: number
  kind: PaneStepKind
  /** Already localised: the object's label, the agent's title, or the type and the count. */
  title: string
  /** The child control descriptor of the step. */
  control: unknown
}

/** Reads the history of the pane, bottom first. The last step is the one shown. */
export function readPaneSteps(state: Record<string, unknown>): PaneStep[] {
  const raw = state[STEPS]
  if (!Array.isArray(raw)) return []
  const steps: PaneStep[] = []
  for (const entry of raw) {
    if (!entry || typeof entry !== 'object') continue
    const row = entry as Record<string, unknown>
    if (typeof row.id !== 'number' || row.control == null) continue
    const kind = row.kind === 'chart' || row.kind === 'form' ? row.kind : 'table'
    steps.push({
      id: row.id,
      kind,
      title: typeof row.title === 'string' ? row.title : '',
      control: row.control,
    })
  }
  return steps
}

/**
 * `<presentation-pane input="…">`: what stands on a channel, for any type - an object as its form,
 * several as a table, what the agent presented as the table, chart or form it asked for.
 *
 * The server keeps a history of steps: a row clicked in a table opens that object's form on top.
 * Every step stays mounted and only the top one is shown, so "back" ({@link CMD_BACK}) shows the
 * step below exactly as it was left. The close button ({@link CMD_CLOSE}) empties the channel. An
 * empty history renders nothing.
 */
export function TLPresentationPane({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const i18n = useI18N(I18N_KEYS)
  const Button = usePart('Button')

  const steps = readPaneSteps(state)
  const top = steps.length > 0 ? steps[steps.length - 1] : null

  return (
    <div id={controlId} className={rootClassName(state, 'tl-chat-presentation')} hidden={top == null}>
      {top != null ? (
        <header className="tl-chat-presentation__bar">
          {steps.length > 1 ? (
            <Button
              label={i18n['js.presentationPane.back']}
              image={ICON_PREVIOUS}
              display="icon-only"
              appearance="ghost"
              onClick={() => void command(CMD_BACK)}
            />
          ) : null}
          <h2 className="tl-chat-presentation__title" title={top.title}>
            {top.title}
          </h2>
          <Button
            label={i18n['js.presentationPane.close']}
            image={ICON_CLOSE}
            display="icon-only"
            appearance="ghost"
            onClick={() => void command(CMD_CLOSE)}
          />
        </header>
      ) : null}
      <div className="tl-chat-presentation__body">
        {steps.map((step) => {
          const shown = step === top
          return (
            <div
              key={step.id}
              className={`pane-step is-${step.kind}${shown ? '' : ' is-stashed'}`}
              inert={!shown}
              aria-hidden={!shown}
            >
              <TLChild control={step.control} />
            </div>
          )
        })}
      </div>
    </div>
  )
}
