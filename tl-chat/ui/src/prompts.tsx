import { React } from 'tl-react-bridge'
import { respond } from './api'
import type { ChatAction } from './reducer'
import type { ApprovalRequest, InteractionRequest } from './types'

const { useState } = React

const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

export function PromptStack({
  approvals,
  questions,
  windowId,
  dispatch,
}: {
  approvals: ApprovalRequest[]
  questions: InteractionRequest[]
  windowId: string
  dispatch: React.Dispatch<ChatAction>
}) {
  if (approvals.length === 0 && questions.length === 0) return null
  return (
    <div className="prompt-stack">
      {approvals.map((item) => (
        <ApprovalCard key={item.id} item={item} windowId={windowId} dispatch={dispatch} />
      ))}
      {questions.map((item) => (
        <QuestionCard key={item.id} item={item} windowId={windowId} dispatch={dispatch} />
      ))}
    </div>
  )
}

function OverlayCard({
  kind,
  title,
  children,
  footer,
}: {
  kind: string
  title: string
  children: React.ReactNode
  footer: React.ReactNode
}) {
  const [open, setOpen] = useState(true)
  return (
    <section className={`prompt-card${open ? '' : ' is-collapsed'}`}>
      <header className="prompt-card-head">
        <strong>{kind}</strong>
        <button
          type="button"
          className="icon-button"
          aria-expanded={open}
          aria-label={open ? `Collapse ${kind}` : `Expand ${kind}`}
          onClick={() => setOpen((value) => !value)}
        >
          {open ? '–' : '+'}
        </button>
      </header>
      {open ? (
        <>
          <h3 className="prompt-card-title">{title}</h3>
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

function ApprovalCard({
  item,
  windowId,
  dispatch,
}: {
  item: ApprovalRequest
  windowId: string
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
      kind="Approval"
      title={item.toolName}
      footer={
        <>
          <button type="button" className="text-button" onClick={() => void send('REJECTED')}>
            Deny
          </button>
          <button type="button" className="text-button" onClick={() => void send('APPROVED_FOR_SESSION')}>
            Allow this chat
          </button>
          <button type="button" className="prompt-continue" onClick={() => void send('APPROVED')}>
            Allow once
          </button>
        </>
      }
    >
      {item.displayReason ? <p className="prompt-card-body">{item.displayReason}</p> : null}
    </OverlayCard>
  )
}

function QuestionCard({
  item,
  windowId,
  dispatch,
}: {
  item: InteractionRequest
  windowId: string
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
      kind="Question"
      title={item.header || item.question}
      footer={
        <>
          <button type="button" className="text-button" onClick={() => void submit('skipped')}>
            Skip
          </button>
          <button
            type="button"
            className="prompt-continue"
            disabled={!canContinue}
            onClick={() => void submit(item.interactionType === 'choice' ? picked.join(', ') : text.trim())}
          >
            Continue
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
          placeholder="Your answer"
        />
      )}
    </OverlayCard>
  )
}
