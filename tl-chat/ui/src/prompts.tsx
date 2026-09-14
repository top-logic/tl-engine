import { React } from 'tl-react-bridge'
import { respond } from './api'
import type { bundle } from './i18n'
import { toolLabel } from './models'
import type { ChatAction } from './reducer'
import type { ApprovalRequest, InteractionRequest } from './types'

const { useEffect, useState } = React

type Labels = ReturnType<typeof bundle>

const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

export function PromptStack({
  approvals,
  questions,
  windowId,
  labels,
  dispatch,
}: {
  approvals: ApprovalRequest[]
  questions: InteractionRequest[]
  windowId: string
  labels: Labels
  dispatch: React.Dispatch<ChatAction>
}) {
  if (approvals.length === 0 && questions.length === 0) return null
  return (
    <div className="prompt-stack">
      {approvals.map((item) => (
        <ApprovalCard key={item.id} item={item} windowId={windowId} labels={labels} dispatch={dispatch} />
      ))}
      {questions.map((item) => (
        <QuestionCard key={item.id} item={item} windowId={windowId} labels={labels} dispatch={dispatch} />
      ))}
    </div>
  )
}

function formatCountdown(ms: number): string {
  const total = Math.max(0, Math.round(ms / 1000))
  const minutes = Math.floor(total / 60)
  const seconds = total % 60
  return minutes ? `${minutes}:${String(seconds).padStart(2, '0')}` : `${seconds}s`
}

/**
 * How long the user still has to answer.
 *
 * The deadline is the server's: the event says how much was left when it was written, and the local
 * clock only measures the time since. Nothing is shown for a request that carries no deadline, and
 * a request whose time has run out says so rather than counting into negative numbers.
 */
function Countdown({
  remainingMs,
  receivedAt,
  labels,
}: {
  remainingMs?: number
  receivedAt?: number
  labels: Labels
}) {
  const deadline = remainingMs && receivedAt ? receivedAt + remainingMs : 0
  const [left, setLeft] = useState(() => (deadline ? deadline - Date.now() : 0))

  useEffect(() => {
    if (!deadline) return
    const tick = () => setLeft(deadline - Date.now())
    tick()
    const timer = window.setInterval(tick, 1000)
    return () => window.clearInterval(timer)
  }, [deadline])

  if (!deadline) return null
  if (left <= 0) {
    return <span className="prompt-countdown is-over">{labels.timeIsUp}</span>
  }
  const urgent = left < 30_000
  return (
    <span className={`prompt-countdown${urgent ? ' is-urgent' : ''}`}>
      <span className="prompt-clock" aria-hidden="true" />
      <time dateTime={`PT${Math.round(left / 1000)}S`}>{formatCountdown(left)}</time>
      <span className="sr-only">{labels.leftToAnswer}</span>
    </span>
  )
}

function OverlayCard({
  kind,
  title,
  titleTooltip,
  countdown,
  labels,
  children,
  footer,
}: {
  kind: string
  title: string
  titleTooltip?: string
  countdown: React.ReactNode
  labels: Labels
  children: React.ReactNode
  footer: React.ReactNode
}) {
  const [open, setOpen] = useState(true)
  return (
    <section className={`prompt-card${open ? '' : ' is-collapsed'}`}>
      <header className="prompt-card-head">
        <strong>{kind}</strong>
        {countdown}
        <button
          type="button"
          className="icon-button tiny"
          aria-expanded={open}
          aria-label={open ? labels.collapse(kind) : labels.expand(kind)}
          onClick={() => setOpen((value) => !value)}
        >
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d={open ? 'M4 9.5 8 5.5l4 4' : 'M4 6.5 8 10.5l4-4'} />
          </svg>
        </button>
      </header>
      {open ? (
        <>
          <h3 className="prompt-card-title" title={titleTooltip}>
            {title}
          </h3>
          {children}
          <footer className="prompt-card-footer">{footer}</footer>
        </>
      ) : (
        <button type="button" className="prompt-card-bar" onClick={() => setOpen(true)}>
          {title}
        </button>
      )}
    </section>
  )
}

/**
 * Why the approval is being asked, in the reader's language.
 *
 * Phrased from the tool's mode rather than from the server's `displayReason`: the reason is written
 * once, in English, by the security context, and this is the one moment the user is asked to take
 * responsibility for something. An event from before the mode was carried falls back to the wording
 * it does have.
 */
function approvalReason(item: ApprovalRequest, labels: Labels): string {
  switch (item.toolMode) {
    case 'USER_SCOPED':
      return labels.approvalUserScoped
    case 'DATA_WRITE':
      return labels.approvalDataWrite
    case 'STRUCTURAL':
      return labels.approvalStructural
    case 'EXTERNAL':
      return labels.approvalExternal
    default:
      return item.displayReason || labels.approvalGeneric
  }
}

function ApprovalCard({
  item,
  windowId,
  labels,
  dispatch,
}: {
  item: ApprovalRequest
  windowId: string
  labels: Labels
  dispatch: React.Dispatch<ChatAction>
}) {
  const send = async (response: 'APPROVED' | 'APPROVED_FOR_SESSION' | 'REJECTED') => {
    await respond(windowId, {
      requestId: item.approvalRequestId,
      response,
      toolName: item.toolName,
      argsJson: item.argsJson,
    })
    dispatch({ type: 'RESOLVE_APPROVAL', approvalRequestId: item.approvalRequestId, outcome: response })
  }
  return (
    <OverlayCard
      kind={labels.approval}
      title={toolLabel(item.toolName)}
      titleTooltip={item.toolName}
      labels={labels}
      countdown={<Countdown remainingMs={item.remainingMs} receivedAt={item.receivedAt} labels={labels} />}
      footer={
        <>
          <button type="button" className="text-button is-danger" onClick={() => void send('REJECTED')}>
            {labels.deny}
          </button>
          <button type="button" className="text-button" onClick={() => void send('APPROVED_FOR_SESSION')}>
            {labels.allowThisChat}
          </button>
          <button type="button" className="prompt-continue" onClick={() => void send('APPROVED')}>
            {labels.allowOnce}
          </button>
        </>
      }
    >
      <p className="prompt-card-body">{approvalReason(item, labels)}</p>
    </OverlayCard>
  )
}

function QuestionCard({
  item,
  windowId,
  labels,
  dispatch,
}: {
  item: InteractionRequest
  windowId: string
  labels: Labels
  dispatch: React.Dispatch<ChatAction>
}) {
  const [text, setText] = useState('')
  const [picked, setPicked] = useState<string[]>([])
  const submit = async (answer: string) => {
    await respond(windowId, { requestId: item.requestId, response: answer })
    dispatch({ type: 'RESOLVE_INTERACTION', requestId: item.requestId })
  }
  const toggle = (label: string) => {
    setPicked((prev) => {
      if (item.multiSelect) {
        return prev.includes(label) ? prev.filter((value) => value !== label) : [...prev, label]
      }
      return [label]
    })
  }
  const canContinue = item.interactionType === 'choice' ? picked.length > 0 : Boolean(text.trim())
  return (
    <OverlayCard
      kind={labels.question}
      title={item.header || item.question}
      labels={labels}
      countdown={<Countdown remainingMs={item.remainingMs} receivedAt={item.receivedAt} labels={labels} />}
      footer={
        <>
          <button type="button" className="text-button" onClick={() => void submit('skipped')}>
            {labels.skip}
          </button>
          <button
            type="button"
            className="prompt-continue"
            disabled={!canContinue}
            onClick={() => void submit(item.interactionType === 'choice' ? picked.join(', ') : text.trim())}
          >
            {labels.continueLabel}
          </button>
        </>
      }
    >
      {item.header && item.question !== item.header ? <p className="prompt-card-body">{item.question}</p> : null}
      {item.interactionType === 'choice' && item.options ? (
        <div className="prompt-options" role={item.multiSelect ? 'group' : 'radiogroup'}>
          {item.options.map((option, index) => {
            const letter = LETTERS[index] || String(index + 1)
            const selected = picked.includes(option.label)
            return (
              <button
                key={option.label}
                type="button"
                className={`prompt-option${selected ? ' is-on' : ''}`}
                onClick={() => toggle(option.label)}
              >
                <span className="prompt-letter">{letter}</span>
                <span>
                  {option.label}
                  {option.description ? <em>{option.description}</em> : null}
                </span>
              </button>
            )
          })}
        </div>
      ) : (
        <input
          className="prompt-input"
          value={text}
          onChange={(event) => setText(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter' && text.trim()) {
              event.preventDefault()
              void submit(text.trim())
            }
          }}
          placeholder={labels.yourAnswer}
          autoFocus
        />
      )}
    </OverlayCard>
  )
}
