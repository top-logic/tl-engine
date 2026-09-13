import { React } from 'tl-react-bridge'
import type { SessionSummary } from './types'

const { useMemo, useState } = React

interface SessionGroup {
  label: string
  sessions: SessionSummary[]
}

function groupSessions(sessions: SessionSummary[]): SessionGroup[] {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const day = 24 * 60 * 60 * 1000
  const groups = new Map<string, SessionSummary[]>()

  for (const session of sessions) {
    const timestamp = session.lastUpdated ? Date.parse(session.lastUpdated) : Number.NaN
    let label = 'Recent'
    if (Number.isFinite(timestamp)) {
      if (timestamp >= today) label = 'Today'
      else if (timestamp >= today - day) label = 'Yesterday'
      else if (timestamp >= today - 7 * day) label = 'Previous 7 days'
      else label = 'Older'
    }
    const entries = groups.get(label) ?? []
    entries.push(session)
    groups.set(label, entries)
  }

  return ['Today', 'Yesterday', 'Previous 7 days', 'Recent', 'Older']
    .filter((label) => groups.has(label))
    .map((label) => ({ label, sessions: groups.get(label)! }))
}

export const SessionRail = React.memo(function SessionRail({
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
}: {
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
}) {
  const [query, setQuery] = useState('')
  const groups = useMemo(() => {
    const normalized = query.trim().toLocaleLowerCase()
    const visible = normalized
      ? sessions.filter((session) => (session.title || 'Untitled').toLocaleLowerCase().includes(normalized))
      : sessions
    return groupSessions(visible)
  }, [query, sessions])

  return (
    <>
      <button
        type="button"
        className={`rail-scrim${open ? ' is-open' : ''}`}
        aria-label="Close chats"
        tabIndex={open ? 0 : -1}
        onClick={onClose}
      />
      <aside
        id="chat-session-rail"
        className={`rail${open ? ' is-open' : ''}${collapsed ? ' is-collapsed' : ''}`}
        aria-label="Chat history"
      >
        <div className="rail-header">
          <strong>Chats</strong>
          <button type="button" className="icon-button rail-close" aria-label="Close chats" onClick={onClose}>
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="m4 4 8 8M12 4l-8 8" />
            </svg>
          </button>
          <button
            type="button"
            className="icon-button rail-collapse"
            aria-label={collapsed ? 'Expand chat history' : 'Collapse chat history'}
            aria-expanded={!collapsed}
            onClick={onToggleCollapsed}
          >
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d={collapsed ? 'm6.5 3.5 4.5 4.5-4.5 4.5' : 'M9.5 3.5 5 8l4.5 4.5'} />
            </svg>
          </button>
        </div>

        <button type="button" className="rail-new" onClick={onNew}>
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M8 3.5v9M3.5 8h9" />
          </svg>
          <span>New chat</span>
        </button>

        <label className="rail-search">
          <span className="sr-only">Search chats</span>
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <circle cx="7.2" cy="7.2" r="4" />
            <path d="m10.3 10.3 3 3" />
          </svg>
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search chats"
          />
        </label>

        <div className="rail-list" aria-live="polite">
          {loading ? (
            <div className="rail-loading" aria-label="Loading chats">
              <span />
              <span />
              <span />
            </div>
          ) : null}
          {!loading && error ? <div className="rail-message is-error">{error}</div> : null}
          {!loading && !error && groups.length === 0 ? (
            <div className="rail-message">{query ? 'No matching chats' : 'No chats yet'}</div>
          ) : null}
          {groups.map((group) => (
            <section className="rail-group" key={group.label} aria-labelledby={`chat-group-${group.label}`}>
              <h2 id={`chat-group-${group.label}`}>{group.label}</h2>
              {group.sessions.map((session) => (
                <div key={session.id} className={`rail-item${session.id === activeId ? ' is-active' : ''}`}>
                  <button
                    type="button"
                    className="rail-open"
                    aria-current={session.id === activeId ? 'page' : undefined}
                    title={session.title || 'Untitled'}
                    onClick={() => onSelect(session.id)}
                  >
                    <span className="rail-item-mark" aria-hidden="true" />
                    <span>{session.title || 'Untitled'}</span>
                  </button>
                  <button
                    type="button"
                    className="icon-button tiny rail-delete"
                    aria-label={`Delete ${session.title || 'Untitled'}`}
                    title="Delete chat"
                    onClick={() => onDelete(session.id)}
                  >
                    <svg viewBox="0 0 16 16" aria-hidden="true">
                      <path d="M3.5 4.5h9M6.5 4.5V3h3v1.5M5 4.5l.6 8h4.8l.6-8" />
                    </svg>
                  </button>
                </div>
              ))}
            </section>
          ))}
        </div>
      </aside>
    </>
  )
})
