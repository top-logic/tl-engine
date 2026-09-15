import { React } from 'tl-react-bridge'
import type { bundle } from './i18n'
import type { SessionSummary } from './types'

const { useEffect, useMemo, useRef, useState } = React

type Labels = ReturnType<typeof bundle>

interface SessionGroup {
  label: string
  sessions: SessionSummary[]
}

function groupSessions(
  sessions: SessionSummary[],
  labels: Labels,
  grouping: 'by-date' | 'flat' = 'by-date',
): SessionGroup[] {
  if (grouping === 'flat') {
    return sessions.length === 0 ? [] : [{ label: '', sessions }]
  }
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const day = 24 * 60 * 60 * 1000
  const groups = new Map<string, SessionSummary[]>()

  for (const session of sessions) {
    const timestamp = session.lastUpdated ? Date.parse(session.lastUpdated) : Number.NaN
    let label = labels.recent
    if (Number.isFinite(timestamp)) {
      if (timestamp >= today) label = labels.today
      else if (timestamp >= today - day) label = labels.yesterday
      else if (timestamp >= today - 7 * day) label = labels.previous7Days
      else label = labels.older
    }
    const entries = groups.get(label) ?? []
    entries.push(session)
    groups.set(label, entries)
  }

  return [labels.today, labels.yesterday, labels.previous7Days, labels.recent, labels.older]
    .filter((label) => groups.has(label))
    .map((label) => ({ label, sessions: groups.get(label)! }))
}

export const SessionRail = React.memo(function SessionRail({
  labels,
  sessions,
  activeId,
  open,
  collapsed,
  loading,
  error,
  onNew,
  onSelect,
  onDelete,
  onClose,
  onToggleCollapsed,
  search = true,
  newChat = true,
  canDelete = true,
  grouping = 'by-date',
  collapsible = true,
}: {
  labels: Labels
  sessions: SessionSummary[]
  activeId: string | null
  open: boolean
  collapsed: boolean
  loading: boolean
  error: string
  onNew: () => void
  onSelect: (id: string) => void
  onDelete: (id: string) => void
  onClose: () => void
  onToggleCollapsed: () => void
  search?: boolean
  newChat?: boolean
  canDelete?: boolean
  grouping?: 'by-date' | 'flat'
  collapsible?: boolean
}) {
  const [query, setQuery] = useState('')
  // Which chat is asking to be deleted; deleting is not undoable, so it is never one click.
  const [confirming, setConfirming] = useState<string | null>(null)
  const confirmRef = useRef<HTMLDivElement>(null)
  const groups = useMemo(() => {
    const normalized = search ? query.trim().toLocaleLowerCase() : ''
    const visible = normalized
      ? sessions.filter((session) => (session.title || labels.untitled).toLocaleLowerCase().includes(normalized))
      : sessions
    return groupSessions(visible, labels, grouping)
  }, [grouping, labels, query, search, sessions])

  useEffect(() => {
    if (!confirming) return
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setConfirming(null)
    }
    const onDoc = (event: MouseEvent) => {
      if (!confirmRef.current?.contains(event.target as Node)) setConfirming(null)
    }
    document.addEventListener('keydown', onKey)
    document.addEventListener('mousedown', onDoc)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.removeEventListener('mousedown', onDoc)
    }
  }, [confirming])

  return (
    <>
      <button
        type="button"
        className={`rail-scrim${open ? ' is-open' : ''}`}
        aria-label={labels.closeChats}
        tabIndex={open ? 0 : -1}
        onClick={onClose}
      />
      <aside
        id="chat-session-rail"
        className={`rail${open ? ' is-open' : ''}${collapsible && collapsed ? ' is-collapsed' : ''}`}
        aria-label={labels.chats}
      >
        <div className="rail-header">
          <strong>{labels.chats}</strong>
          <button type="button" className="icon-button rail-close" aria-label={labels.closeChats} onClick={onClose}>
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="m4 4 8 8M12 4l-8 8" />
            </svg>
          </button>
          {collapsible ? (
            <button
              type="button"
              className="icon-button rail-collapse"
              aria-label={collapsed ? labels.expandChatHistory : labels.collapseChatHistory}
              aria-expanded={!collapsed}
              onClick={onToggleCollapsed}
            >
              <svg viewBox="0 0 16 16" aria-hidden="true">
                <path d={collapsed ? 'm6.5 3.5 4.5 4.5-4.5 4.5' : 'M9.5 3.5 5 8l4.5 4.5'} />
              </svg>
            </button>
          ) : null}
        </div>

        {newChat ? (
          <button type="button" className="rail-new" onClick={onNew}>
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="M8 3.5v9M3.5 8h9" />
            </svg>
            <span>{labels.newChat}</span>
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
          {loading ? (
            <div className="rail-loading" aria-label={labels.loading}>
              <span />
              <span />
              <span />
            </div>
          ) : null}
          {!loading && error ? <div className="rail-message is-error">{error}</div> : null}
          {!loading && !error && groups.length === 0 ? (
            <div className="rail-message">{query ? labels.noMatchingChats : labels.noChats}</div>
          ) : null}
          {groups.map((group) => (
            <section
              className="rail-group"
              key={group.label || 'all'}
              aria-labelledby={group.label ? `chat-group-${group.label}` : undefined}
              aria-label={group.label ? undefined : labels.chats}
            >
              {group.label ? <h2 id={`chat-group-${group.label}`}>{group.label}</h2> : null}
              {group.sessions.map((session) => {
                const title = session.title || labels.untitled
                if (canDelete && confirming === session.id) {
                  return (
                    <div className="rail-item is-confirming" key={session.id} ref={confirmRef}>
                      <span className="rail-confirm-text">{labels.deleteChatQuestion}</span>
                      <span className="rail-confirm-actions">
                        <button type="button" className="text-button" onClick={() => setConfirming(null)}>
                          {labels.cancel}
                        </button>
                        <button
                          type="button"
                          className="text-button is-danger"
                          onClick={() => {
                            setConfirming(null)
                            onDelete(session.id)
                          }}
                        >
                          {labels.delete}
                        </button>
                      </span>
                    </div>
                  )
                }
                return (
                  <div key={session.id} className={`rail-item${session.id === activeId ? ' is-active' : ''}`}>
                    <button
                      type="button"
                      className="rail-open"
                      aria-current={session.id === activeId ? 'page' : undefined}
                      title={title}
                      onClick={() => onSelect(session.id)}
                    >
                      <span className="rail-item-mark" aria-hidden="true" />
                      <span>{title}</span>
                    </button>
                    {canDelete ? (
                      <button
                        type="button"
                        className="icon-button tiny rail-delete"
                        aria-label={`${labels.deleteChat}: ${title}`}
                        title={labels.deleteChat}
                        onClick={() => setConfirming(session.id)}
                      >
                        <svg viewBox="0 0 16 16" aria-hidden="true">
                          <path d="M3.5 4.5h9M6.5 4.5V3h3v1.5M5 4.5l.6 8h4.8l.6-8" />
                        </svg>
                      </button>
                    ) : null}
                  </div>
                )
              })}
            </section>
          ))}
        </div>
      </aside>
    </>
  )
})
