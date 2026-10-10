import { React } from 'tl-react-bridge'
import { kindIcon, sourceOrigin } from './citations'
import type { bundle } from './i18n'
import type { ChatMessage, Citation } from './types'

const { useMemo, useState } = React

type Labels = ReturnType<typeof bundle>

/** One source this session cited, and every marker that points at it. */
export interface CitedSource {
  /** Kind and id together: the same source cited twice is one row. */
  key: string
  kind: string
  id: string
  label: string
  url: string
  /** Where it comes from: the web host for a URL, the host kind otherwise. */
  origin: string
  citation: Citation
  markers: { index: number; messageId: string }[]
}

/**
 * Every source cited in this chat, in the order they were first cited.
 *
 * The rail's list is session-wide, so the same page cited in three answers is one row with three
 * marker numbers rather than three rows.
 */
export function citedSources(messages: ChatMessage[]): CitedSource[] {
  const byKey = new Map<string, CitedSource>()
  for (const message of messages) {
    if (message.type !== 'assistant_text' || !message.citations?.length) continue
    for (const citation of message.citations) {
      const key = `${citation.kind}::${citation.id}`
      const known = byKey.get(key)
      if (known) {
        known.markers.push({ index: citation.index, messageId: message.id })
        if (!known.citation.excerpt && citation.excerpt) known.citation = citation
        continue
      }
      byKey.set(key, {
        key,
        kind: citation.kind,
        id: citation.id,
        label: citation.label || citation.id,
        url: citation.url,
        origin: sourceOrigin(citation),
        citation,
        markers: [{ index: citation.index, messageId: message.id }],
      })
    }
  }
  return [...byKey.values()]
}

/**
 * The sources of the newest answer - what the section's count badge reports.
 *
 * The list itself is the conversation's: a page cited in three answers stays one row for the whole
 * chat, and nothing a reader has seen is taken away from them. A *badge* of that list would be a
 * running total that only ever grows, sits on the Quellen icon for the rest of the chat and cannot
 * be dismissed (#495). What a badge is for is "look here now", so it counts what the answer in
 * front of the reader rests on: an answer that cited nothing carries no badge, and an answer that
 * is still streaming carries none either - its citations arrive with the finished text.
 */
export function latestAnswerSources(messages: ChatMessage[]): CitedSource[] {
  for (let at = messages.length - 1; at >= 0; at--) {
    const message = messages[at]
    if (message.type !== 'assistant_text') continue
    return message.citations?.length ? citedSources([message]) : []
  }
  return []
}

/** Above this many rows the list gets a filter field. */
const FILTER_FROM = 6

/**
 * The framework's `Quellen` section: what this chat has cited, with the markers that point at it.
 *
 * A host may hang its own list beside it as a further tab - the sources are the framework's, the
 * documents a host produces are not.
 */
export function SourcesSection({
  sources,
  labels,
  onOpen,
  onGoTo,
  tabs,
}: {
  sources: CitedSource[]
  labels: Labels
  onOpen: (source: CitedSource) => void
  onGoTo: (messageId: string) => void
  tabs?: { id: string; label: string; render: () => React.ReactNode }[]
}) {
  const [tab, setTab] = useState('sources')
  const [filter, setFilter] = useState('')
  const wanted = filter.trim().toLocaleLowerCase()
  const shown = useMemo(
    () =>
      wanted
        ? sources.filter(
            (source) =>
              source.label.toLocaleLowerCase().includes(wanted) ||
              source.origin.toLocaleLowerCase().includes(wanted),
          )
        : sources,
    [sources, wanted],
  )

  const hostTab = tabs?.find((item) => item.id === tab)
  return (
    <div className="sources">
      {tabs && tabs.length > 0 ? (
        <div className="sources-tabs" role="tablist">
          <button
            type="button"
            role="tab"
            aria-selected={tab === 'sources'}
            className={`sources-tab${tab === 'sources' ? ' is-active' : ''}`}
            onClick={() => setTab('sources')}
          >
            {labels.sourcesTitle}
          </button>
          {tabs.map((item) => (
            <button
              type="button"
              key={item.id}
              role="tab"
              aria-selected={tab === item.id}
              className={`sources-tab${tab === item.id ? ' is-active' : ''}`}
              onClick={() => setTab(item.id)}
            >
              {item.label}
            </button>
          ))}
        </div>
      ) : null}

      {hostTab ? (
        hostTab.render()
      ) : sources.length === 0 ? (
        <p className="side-empty">{labels.sourcesEmpty}</p>
      ) : (
        <>
          {sources.length >= FILTER_FROM ? (
            <input
              type="search"
              className="sources-filter"
              value={filter}
              placeholder={labels.sourcesFilter}
              aria-label={labels.sourcesFilter}
              onChange={(event) => setFilter(event.currentTarget.value)}
            />
          ) : null}
          <ul className="sources-list">
            {shown.map((source) => (
              <li key={source.key}>
                <SourceRow source={source} labels={labels} onOpen={onOpen} onGoTo={onGoTo} />
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  )
}

/** One source: what it is, where it comes from, and which markers point at it. */
export function SourceRow({
  source,
  labels,
  onOpen,
  onGoTo,
}: {
  source: CitedSource
  labels: Labels
  onOpen: (source: CitedSource) => void
  onGoTo?: (messageId: string) => void
}) {
  return (
    <div className="source-row">
      <button type="button" className="source-head" onClick={() => onOpen(source)} title={labels.openSource}>
        <span className="source-icon" aria-hidden="true">
          {kindIcon(source.kind)}
        </span>
        <span className="source-text">
          <span className="source-label">{source.label}</span>
          <span className="source-origin">{source.origin}</span>
        </span>
      </button>
      {source.citation.archiveUrl ? (
        <a className="source-archive" href={source.citation.archiveUrl} target="_blank" rel="noopener noreferrer">
          {labels.archivedCopy(source.citation.archivedAt ?? '')}
        </a>
      ) : null}
      {onGoTo ? (
        <div className="source-markers">
          {source.markers.map((marker, index) => (
            <button
              type="button"
              key={`${marker.messageId}-${marker.index}-${index}`}
              className="source-marker"
              aria-label={labels.goToAnswer(marker.index)}
              onClick={() => onGoTo(marker.messageId)}
            >
              {marker.index}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  )
}

/** The collapsed pill under an answer: how many sources it cited, expandable in place. */
export function AnswerSources({
  sources,
  labels,
  onOpen,
}: {
  sources: CitedSource[]
  labels: Labels
  onOpen: (source: CitedSource) => void
}) {
  const [open, setOpen] = useState(false)
  if (sources.length === 0) return null
  return (
    <div className="answer-sources">
      <button
        type="button"
        className="answer-sources-pill"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
      >
        <span className="source-icon" aria-hidden="true">
          {kindIcon(sources[0].kind)}
        </span>
        {labels.sourcesPill(sources.length)}
        <span className={`answer-sources-caret${open ? ' is-open' : ''}`} aria-hidden="true" />
      </button>
      {open ? (
        <ul className="sources-list">
          {sources.map((source) => (
            <li key={source.key}>
              <SourceRow source={source} labels={labels} onOpen={onOpen} />
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  )
}

/** The icon of the Quellen section on the 56 px strip. */
export const SourcesIcon = (
  <svg viewBox="0 0 16 16" aria-hidden="true">
    <path d="M3 4.2c1.6-.9 3.4-.9 5 0v8c-1.6-.9-3.4-.9-5 0zM13 4.2c-1.6-.9-3.4-.9-5 0v8c1.6-.9 3.4-.9 5 0z" />
  </svg>
)
