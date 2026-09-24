import { React } from 'tl-react-bridge'
import type { bundle } from './i18n'
import { ToolId } from './tool-id'
import {
  progressSteps,
  type AgentTask,
  type PendingApproval,
  type ProgressStatus,
} from './progress-model'

const { useMemo } = React

type Labels = ReturnType<typeof bundle>

export function ProgressSection({
  tasks,
  approvals,
  labels,
}: {
  tasks: AgentTask[]
  approvals?: PendingApproval[]
  labels: Labels
}) {
  const pending = approvals ?? []
  const steps = useMemo(() => progressSteps(tasks, pending), [tasks, pending])
  if (steps.length === 0) {
    return <p className="side-empty">{labels.progressEmpty}</p>
  }
  return (
    <ol className="progress-list">
      {steps.map((step) => {
        const tools = step.tools?.join(', ')
        return (
          <li key={step.id} className={`progress-step is-${step.status}`}>
            <span className="progress-marker" aria-hidden="true">
              {marker(step.status)}
            </span>
            <span className="progress-label" title={step.label || tools}>
              {step.label || <ToolIds ids={step.tools ?? []} />}
            </span>
            {step.status === 'approval' ? (
              <span className="progress-caption" title={tools}>
                {labels.progressWaiting}
              </span>
            ) : null}
          </li>
        )
      })}
    </ol>
  )
}

/** The tools an approval that came before any task is open for, each set as code. */
function ToolIds({ ids }: { ids: string[] }) {
  return (
    <>
      {ids.map((id, index) => (
        <React.Fragment key={index}>
          {index > 0 ? ', ' : null}
          <ToolId id={id} />
        </React.Fragment>
      ))}
    </>
  )
}

function marker(status: ProgressStatus): React.ReactNode {
  if (status === 'done') {
    return (
      <svg viewBox="0 0 16 16">
        <path d="m3.5 8.5 3 3 6-7" />
      </svg>
    )
  }
  if (status === 'waiting') {
    return (
      <svg viewBox="0 0 16 16">
        <circle cx="8" cy="8" r="5.5" />
      </svg>
    )
  }
  if (status === 'approval') {
    return (
      <svg viewBox="0 0 16 16">
        <circle cx="8" cy="8" r="5.5" />
        <path d="M8 5v3.4l2 1.4" />
      </svg>
    )
  }
  if (status === 'error') {
    return (
      <svg viewBox="0 0 16 16">
        <path d="m4 4 8 8M12 4l-8 8" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 16 16">
      <circle cx="8" cy="8" r="5.5" />
      <circle cx="8" cy="8" r="2.5" />
    </svg>
  )
}

/** The icon of the framework's own Fortschritt section on the 56 px strip. */
export const ProgressIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="m2 4.5 1.6 1.6L6.5 3" />
    <path d="m2 11.5 1.6 1.6L6.5 10" />
    <path d="M8.5 5h5.5M8.5 12H14" />
  </svg>
)

/**
 * What the agent receives with every turn.
 *
 * The framework knows four of those facts itself (the language it speaks, the agent, the model and
 * the session); a host adds its own through `contextFacts`.
 */
export function ContextSection({
  facts,
  labels,
}: {
  facts: { id: string; label: string; value: string }[]
  labels: Labels
}) {
  if (facts.length === 0) {
    return <p className="side-empty">{labels.railEmpty}</p>
  }
  return (
    <dl className="context-list">
      {facts.map((fact) => (
        <div key={fact.id} className="context-row">
          <dt>{fact.label}</dt>
          <dd title={fact.value}>{fact.value}</dd>
        </div>
      ))}
    </dl>
  )
}

/** The icon of the Kontext section on the 56 px strip. */
export const ContextIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <circle cx="8" cy="8" r="6" />
    <path d="M8 7.2v4M8 4.6v.9" />
  </svg>
)
