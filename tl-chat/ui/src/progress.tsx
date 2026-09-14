import { React } from 'tl-react-bridge'
import type { bundle } from './i18n'
import { progressSteps, type ProgressStatus } from './progress-model'
import type { ChatMessage } from './types'

const { useMemo } = React

type Labels = ReturnType<typeof bundle>

export function ProgressSection({
  messages,
  labels,
}: {
  messages: ChatMessage[]
  labels: Labels
}) {
  const steps = useMemo(() => progressSteps(messages), [messages])
  if (steps.length === 0) {
    return <p className="side-empty">{labels.progressEmpty}</p>
  }
  return (
    <ol className="progress-list">
      {steps.map((step) => (
        <li key={step.id} className={`progress-step is-${step.status}`}>
          <span className="progress-marker" aria-hidden="true">
            {marker(step.status)}
          </span>
          <span className="progress-label" title={step.name}>
            {step.label}
          </span>
          {step.status === 'waiting' ? (
            <span className="progress-caption">{labels.progressWaiting}</span>
          ) : null}
        </li>
      ))}
    </ol>
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
