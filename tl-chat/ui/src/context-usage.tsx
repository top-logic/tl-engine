import { React } from 'tl-react-bridge'
import type { bundle } from './i18n'
import { effortLabel, modelLabel } from './models'
import { runStore, type RunStore } from './store'
import { answerTokens, type ContextComposition, type RunSnapshot } from './types'

const { useEffect, useRef, useState, useSyncExternalStore } = React

type Labels = ReturnType<typeof bundle>

export function formatTokens(n: number): string {
  if (!n) return '0'
  if (n < 1000) return String(n)
  if (n < 10_000) return `${(n / 1000).toFixed(1)}K`
  if (n < 1_000_000) return `${Math.round(n / 1000)}K`
  const millions = n / 1_000_000
  return `${millions < 10 ? millions.toFixed(1).replace(/\.0$/, '') : Math.round(millions)}M`
}

/**
 * How full the window is, in words a reader can act on.
 *
 * A million-token window makes the first turns round to zero, and "0% full" reads as a broken
 * gauge rather than as an empty one, so anything below one percent says so instead.
 */
function formatPercent(ratio: number): string {
  if (ratio <= 0) return '0%'
  if (ratio < 0.01) return '<1%'
  return `${Math.round(ratio * 100)}%`
}

interface Slice {
  key: string
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
  /** Ready to print, e.g. "78%" or "<1%". */
  percent: string
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
    percent: formatPercent(ratio),
    slices: slicesOf(used, snapshot.composition, estimated),
    answer: answerTokens(snapshot.lastUsage),
  }
}

/**
 * How the measured total splits.
 *
 * The system prompt and the tool definitions are the loop's estimates, subtracted from the measured
 * total; what is left is the conversation. Of that remainder, the tool results are shown on their
 * own line - they are inside the prompt like everything else, and they are the part that grows
 * without the user writing anything - and are marked as an estimate because their figure, unlike
 * the remainder they come out of, is one.
 */
function slicesOf(used: number, composition: ContextComposition | undefined, estimated: boolean): Slice[] {
  if (!composition || used <= 0) return []
  const system = Math.min(composition.systemPromptTokens, used)
  const tools = Math.min(composition.toolDefinitionTokens, Math.max(0, used - system))
  const rest = Math.max(0, used - system - tools)
  const toolResults = Math.min(composition.toolResultTokens, rest)
  const conversation = rest - toolResults
  const slices: Slice[] = []
  if (system > 0) {
    slices.push({ key: 'system', tokens: system, estimated: true })
  }
  if (tools > 0) {
    slices.push({
      key: 'tools',
      tokens: tools,
      estimated: true,
      note: composition.toolCount ? String(composition.toolCount) : undefined,
    })
  }
  if (conversation > 0) {
    slices.push({ key: 'conversation', tokens: conversation, estimated })
  }
  if (toolResults > 0) {
    slices.push({ key: 'toolResults', tokens: toolResults, estimated: true })
  }
  return slices
}

/** The name of a slice, in the reader's language. */
function sliceLabel(slice: Slice, labels: Labels): string {
  switch (slice.key) {
    case 'system':
      return labels.systemPrompt
    case 'tools':
      return labels.toolDefinitions
    case 'toolResults':
      return labels.toolResults
    default:
      return labels.conversation
  }
}

const RADIUS = 13

/**
 * Length of the drawn arc.
 *
 * A fraction of a percent of a million-token window would be a sub-pixel arc, and a ring with no
 * arc at all reads as a spinner that never finishes. Any non-zero usage therefore draws a short
 * visible tick; the exact figure is one hover away.
 */
function arcLength(ratio: number, circumference: number): number {
  if (ratio <= 0) return 0
  return Math.max(circumference * 0.04, circumference * ratio)
}

/**
 * The ring reads the run store of the chat it belongs to.
 *
 * It defaults to the module-level one, which is what a host composing its own chat still gets;
 * every element of this bundle passes the store of the chat it joined, because a window holding
 * two conversations has two runs and the module store only ever holds the last writer's.
 */
export const ContextUsage = React.memo(function ContextUsage({
  labels,
  runs = runStore,
}: {
  labels: Labels
  runs?: RunStore
}) {
  const snapshot = useSyncExternalStore(runs.subscribe, runs.get, runs.get)
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

  // Nothing measured, nothing estimated: an empty ring beside the send button reads as a spinner
  // that never finishes. The gauge appears with the first turn, which is when it has something to
  // say; until then the window is visible in the model menu.
  if (!reading.window || reading.used <= 0) return null

  const label = `${prefix}${formatTokens(reading.used)} / ${formatTokens(reading.window)} ${labels.tokens}`

  return (
    <div className="context-wrap" ref={wrap}>
      <button
        type="button"
        className={`context-gauge tone-${tone}${open ? ' is-open' : ''}`}
        aria-expanded={open}
        aria-haspopup="dialog"
        aria-label={`${labels.contextUsage}: ${label}`}
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
            strokeDasharray={`${arcLength(reading.ratio, circumference)} ${circumference}`}
            transform="rotate(-90 16 16)"
          />
        </svg>
      </button>
      {open ? (
        <ContextPopover reading={reading} model={snapshot.model} effort={snapshot.effort} labels={labels} />
      ) : null}
    </div>
  )
})

function ContextPopover({
  reading,
  model,
  effort,
  labels,
}: {
  reading: ContextReading
  model: string
  effort: string
  labels: Labels
}) {
  const prefix = reading.estimated ? '~' : ''
  // What the turn actually ran with: the model, and the effort when one is in force.
  const modelLine = model ? (effort ? `${modelLabel(model)} · ${effortLabel(effort, labels)}` : modelLabel(model)) : ''
  return (
    <div className="context-popover" role="dialog" aria-label={labels.contextUsage}>
      <div className="context-popover-head">
        <strong>{labels.contextUsage}</strong>
        <span>
          {prefix}
          {formatTokens(reading.used)} / {formatTokens(reading.window)} {labels.tokens}
        </span>
      </div>
      <div className="context-popover-meta">
        <span>{labels.contextFull(reading.percent)}</span>
        {modelLine ? <span className="context-model" title={model}>{modelLine}</span> : null}
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
          <i className="slice-conversation" style={{ width: `${Math.max(1, reading.ratio * 100)}%` }} />
        )}
      </div>
      {reading.slices.length ? (
        <dl className="context-rows">
          {reading.slices.map((slice) => (
            <div key={slice.key}>
              <dt>
                <span className={`context-key slice-${slice.key}`} aria-hidden="true" />
                {sliceLabel(slice, labels)}
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
          <span>{labels.lastAnswer}</span>
          <span>{labels.tokensWritten(formatTokens(reading.answer))}</span>
        </div>
      ) : null}
      <p className="context-note">
        {reading.estimated ? labels.contextEstimated : labels.contextMeasured}
      </p>
    </div>
  )
}
