import { React } from 'tl-react-bridge'
import { runStore } from './store'
import { answerTokens, type ContextComposition, type RunSnapshot } from './types'

const { useEffect, useRef, useState, useSyncExternalStore } = React

export function formatTokens(n: number): string {
  if (!n) return '0'
  if (n < 1000) return String(n)
  if (n < 10_000) return `${(n / 1000).toFixed(1)}K`
  return `${Math.round(n / 1000)}K`
}

interface Slice {
  key: string
  label: string
  tokens: number
  /** True when the figure comes from the agent loop's estimator rather than from the provider. */
  estimated: boolean
  note?: string
}

export interface ContextReading {
  /** Tokens of the last prompt, measured when possible. */
  used: number
  window: number
  /** True when `used` is the agent loop's estimate because no turn has completed yet. */
  estimated: boolean
  ratio: number
  percent: number
  slices: Slice[]
  /** Tokens the model wrote in the last turn - not part of the prompt, so never inside the bar. */
  answer: number
}

/**
 * Turns a run snapshot into the numbers the ring and its popover show.
 *
 * How each number is derived:
 *
 * - `used` is `inputTokens + cacheReadTokens + cacheWriteTokens` of the last `turn_completed`. That
 *   sum is exactly the prompt the provider billed, cached parts included, and therefore exactly the
 *   context the model saw. Only before a session's first completed turn does the agent loop's
 *   estimate stand in, and then `estimated` says so.
 * - `window` is the context window of the selected model, from `/chat/capabilities` before a run
 *   and from the run's own events once one has started.
 * - The slices split `used`. System prompt and tool definitions are the agent loop's estimates
 *   (`context_composition`) - the provider reports one total and never splits it - and are marked
 *   as estimates. Conversation is then the remainder of the measured total, which is why it is not
 *   marked: it inherits the measurement, minus two estimates.
 * - `answer` is `outputTokens + thinkingTokens` of the last turn. It is what the model wrote, not
 *   what it read, so it is reported beside the bar and never inside it.
 */
export function readContext(snapshot: RunSnapshot): ContextReading {
  const measured = snapshot.measuredTokens
  const used = measured || snapshot.estimatedTokens || 0
  const estimated = measured <= 0
  const windowSize = snapshot.contextWindow || 0
  const ratio = windowSize > 0 ? Math.min(1, used / windowSize) : 0
  return {
    used,
    window: windowSize,
    estimated,
    ratio,
    percent: Math.round(ratio * 100),
    slices: slicesOf(used, snapshot.composition, estimated),
    answer: answerTokens(snapshot.lastUsage),
  }
}

function slicesOf(used: number, composition: ContextComposition | undefined, estimated: boolean): Slice[] {
  if (!composition || used <= 0) return []
  const system = Math.min(composition.systemPromptTokens, used)
  const tools = Math.min(composition.toolDefinitionTokens, Math.max(0, used - system))
  const conversation = Math.max(0, used - system - tools)
  const slices: Slice[] = []
  if (system > 0) {
    slices.push({ key: 'system', label: 'System prompt', tokens: system, estimated: true })
  }
  if (tools > 0) {
    slices.push({
      key: 'tools',
      label: 'Tool definitions',
      tokens: tools,
      estimated: true,
      note: composition.toolCount ? `${composition.toolCount} tools` : undefined,
    })
  }
  if (conversation > 0) {
    slices.push({ key: 'conversation', label: 'Conversation', tokens: conversation, estimated })
  }
  return slices
}

const RADIUS = 13

export const ContextUsage = React.memo(function ContextUsage() {
  const snapshot = useSyncExternalStore(runStore.subscribe, runStore.get, runStore.get)
  const [open, setOpen] = useState(false)
  const wrap = useRef<HTMLDivElement>(null)
  const reading = readContext(snapshot)
  const tone = reading.ratio > 0.9 ? 'ember' : reading.ratio > 0.7 ? 'warn' : 'ok'
  const circumference = 2 * Math.PI * RADIUS
  const prefix = reading.estimated ? '~' : ''

  useEffect(() => {
    if (!open) return
    const onDoc = (event: MouseEvent) => {
      if (!wrap.current?.contains(event.target as Node)) setOpen(false)
    }
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onDoc)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onDoc)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  if (!reading.window) return null

  const label = `${prefix}${formatTokens(reading.used)} of ${formatTokens(reading.window)} tokens used`

  return (
    <div className="context-wrap" ref={wrap}>
      <button
        type="button"
        className={`context-gauge tone-${tone}${open ? ' is-open' : ''}`}
        aria-expanded={open}
        aria-haspopup="dialog"
        aria-label={`Context usage: ${label}`}
        title={label}
        onClick={() => setOpen((value) => !value)}
      >
        <svg viewBox="0 0 32 32" aria-hidden="true">
          <circle className="gauge-track" cx="16" cy="16" r={RADIUS} />
          <circle
            className="gauge-arc"
            cx="16"
            cy="16"
            r={RADIUS}
            strokeDasharray={`${circumference * reading.ratio} ${circumference}`}
            transform="rotate(-90 16 16)"
          />
        </svg>
      </button>
      {open ? <ContextPopover reading={reading} model={snapshot.model} /> : null}
    </div>
  )
})

function ContextPopover({ reading, model }: { reading: ContextReading; model: string }) {
  const prefix = reading.estimated ? '~' : ''
  return (
    <div className="context-popover" role="dialog" aria-label="Context usage">
      <div className="context-popover-head">
        <strong>Context usage</strong>
        <span>
          {prefix}
          {formatTokens(reading.used)} / {formatTokens(reading.window)} tokens
        </span>
      </div>
      <div className="context-popover-meta">
        <span>{reading.percent}% full</span>
        {model ? <span className="context-model">{model}</span> : null}
      </div>
      <div className="context-bar" aria-hidden="true">
        {reading.slices.length ? (
          reading.slices.map((slice) => (
            <i
              key={slice.key}
              className={`slice-${slice.key}`}
              style={{ width: `${(slice.tokens / reading.window) * 100}%` }}
            />
          ))
        ) : (
          <i className="slice-conversation" style={{ width: `${Math.max(1, reading.percent)}%` }} />
        )}
      </div>
      {reading.slices.length ? (
        <dl className="context-rows">
          {reading.slices.map((slice) => (
            <div key={slice.key}>
              <dt>
                <span className={`context-key slice-${slice.key}`} aria-hidden="true" />
                {slice.label}
                {slice.note ? <em>{slice.note}</em> : null}
              </dt>
              <dd>
                {slice.estimated ? '~' : ''}
                {formatTokens(slice.tokens)}
              </dd>
            </div>
          ))}
        </dl>
      ) : null}
      {reading.answer > 0 ? (
        <div className="context-aside">
          <span>Last answer</span>
          <span>{formatTokens(reading.answer)} tokens written</span>
        </div>
      ) : null}
      <p className="context-note">
        {reading.estimated
          ? 'Estimated until the first answer of this chat; the next turn reports the measured size.'
          : 'Measured by the provider for the last request. Rows marked ~ are estimates of how that total splits.'}
      </p>
    </div>
  )
}
