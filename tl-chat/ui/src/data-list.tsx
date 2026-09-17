import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { bundle, toLanguage } from './i18n'

const { useMemo, useState } = React

/** One display line of a row: text plus an optional emphasis ('primary' | 'secondary'). */
interface DataLine {
  text: string
  emphasis?: string
}

/** One row pushed by {@code DataListControl}: {id, lines[], group?}. */
interface DataRow {
  id: string
  lines: DataLine[]
  group?: string
}

interface RowGroup {
  label: string
  rows: DataRow[]
}

function readLines(value: unknown): DataLine[] {
  if (!Array.isArray(value)) return []
  const out: DataLine[] = []
  for (const raw of value) {
    if (!raw || typeof raw !== 'object') continue
    const line = raw as Record<string, unknown>
    out.push({
      text: typeof line.text === 'string' ? line.text : '',
      emphasis: typeof line.emphasis === 'string' ? line.emphasis : undefined,
    })
  }
  return out
}

function readRows(value: unknown): DataRow[] {
  if (!Array.isArray(value)) return []
  const out: DataRow[] = []
  for (const raw of value) {
    if (!raw || typeof raw !== 'object') continue
    const row = raw as Record<string, unknown>
    if (typeof row.id !== 'string') continue
    out.push({
      id: row.id,
      lines: readLines(row.lines),
      group: typeof row.group === 'string' ? row.group : undefined,
    })
  }
  return out
}

/** Buckets rows by their server-computed group label, groups in first-appearance order. */
function groupRows(rows: DataRow[]): RowGroup[] {
  const order: string[] = []
  const groups = new Map<string, DataRow[]>()
  for (const row of rows) {
    const label = row.group ?? ''
    let bucket = groups.get(label)
    if (!bucket) {
      bucket = []
      groups.set(label, bucket)
      order.push(label)
    }
    bucket.push(row)
  }
  return order.map((label) => ({ label, rows: groups.get(label)! }))
}

/**
 * Generic, data-bound, styled, grouped, searchable, selectable list - the `<data-list>` element's
 * React side. Reuses the session-rail presentation (the `.rail*` classes) but renders whatever rows
 * the Java control pushes, with client-side search and collapse; a row click sends the `select`
 * command back with the row id.
 */
export function TLDataList({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const labels = useMemo(() => bundle(toLanguage(state.language)), [state.language])
  const rows = useMemo(() => readRows(state.rows), [state.rows])
  const selectedId = typeof state.selectedId === 'string' ? state.selectedId : ''
  const search = state.search !== false
  const collapsible = state.collapsible !== false
  const create = state.create === true
  const createLabel = typeof state.createLabel === 'string' && state.createLabel ? state.createLabel : labels.newChat
  const title = typeof state.title === 'string' && state.title ? state.title : labels.chats
  const emptyText = typeof state.emptyText === 'string' && state.emptyText ? state.emptyText : labels.noChats

  const [query, setQuery] = useState('')
  const [collapsed, setCollapsed] = useState(false)

  const groups = useMemo(() => {
    const normalized = search ? query.trim().toLocaleLowerCase() : ''
    const visible = normalized
      ? rows.filter((row) => row.lines.some((line) => line.text.toLocaleLowerCase().includes(normalized)))
      : rows
    return groupRows(visible)
  }, [query, rows, search])

  const isCollapsed = collapsible && collapsed
  return (
    <div id={controlId} className={`tl-data-list${isCollapsed ? ' is-collapsed' : ''}`}>
      <aside
        className={`rail${isCollapsed ? ' is-collapsed' : ''}`}
        aria-label={title}
      >
        <div className="rail-header">
          <strong>{title}</strong>
          {collapsible ? (
            <button
              type="button"
              className="icon-button rail-collapse"
              aria-label={collapsed ? labels.expandChatHistory : labels.collapseChatHistory}
              aria-expanded={!collapsed}
              onClick={() => setCollapsed((value) => !value)}
            >
              <svg viewBox="0 0 16 16" aria-hidden="true">
                <path d={collapsed ? 'm6.5 3.5 4.5 4.5-4.5 4.5' : 'M9.5 3.5 5 8l4.5 4.5'} />
              </svg>
            </button>
          ) : null}
        </div>

        {create ? (
          <button type="button" className="rail-new" onClick={() => void command('create')}>
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="M8 3.5v9M3.5 8h9" />
            </svg>
            <span>{createLabel}</span>
          </button>
        ) : null}

        {search ? (
          <label className="rail-search">
            <span className="sr-only">{labels.searchChats}</span>
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <circle cx="7.2" cy="7.2" r="4" />
              <path d="m10.3 10.3 3 3" />
            </svg>
            <input
              type="search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder={labels.searchChats}
            />
          </label>
        ) : null}

        <div className="rail-list" aria-live="polite">
          {groups.length === 0 ? (
            <div className="rail-message">{query ? labels.noMatchingChats : emptyText}</div>
          ) : null}
          {groups.map((group) => (
            <section
              className="rail-group"
              key={group.label || 'all'}
              aria-labelledby={group.label ? `data-group-${group.label}` : undefined}
              aria-label={group.label ? undefined : title}
            >
              {group.label ? <h2 id={`data-group-${group.label}`}>{group.label}</h2> : null}
              {group.rows.map((row) => (
                <div key={row.id} className={`rail-item${row.id === selectedId ? ' is-active' : ''}`}>
                  <button
                    type="button"
                    className="rail-open"
                    aria-current={row.id === selectedId ? 'page' : undefined}
                    title={row.lines[0]?.text}
                    onClick={() => void command('select', { id: row.id })}
                  >
                    <span className="rail-item-mark" aria-hidden="true" />
                    <span className="rail-open-text">
                      {row.lines.map((line, index) => {
                        const emphasis = line.emphasis ?? (index === 0 ? 'primary' : 'secondary')
                        return (
                          <span key={index} className={`rail-line rail-line-${emphasis}`}>
                            {line.text}
                          </span>
                        )
                      })}
                    </span>
                  </button>
                </div>
              ))}
            </section>
          ))}
        </div>
      </aside>
    </div>
  )
}
