import { React, rootClassName, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { bundle, toLanguage } from './i18n'

const { useEffect, useLayoutEffect, useMemo, useRef, useState } = React

/** One display line of a row: text plus an optional emphasis ('primary' | 'secondary'). */
interface DataLine {
  text: string
  emphasis?: string
}

/** One row pushed by {@code DataListControl}: {id, lines[], group?, cssClass?}. */
interface DataRow {
  id: string
  lines: DataLine[]
  group?: string
  /** Extra classes the view's `css-class` expression put on this row, e.g. a done/running state. */
  cssClass?: string
}

interface RowGroup {
  label: string
  rows: DataRow[]
}

/** One header command button pushed by {@code DataListControl}: {label, icon} (icon is a ThemeImage). */
interface DataButton {
  label: string
  icon: string
}

function readButtons(value: unknown): DataButton[] {
  if (!Array.isArray(value)) return []
  const out: DataButton[] = []
  for (const raw of value) {
    if (!raw || typeof raw !== 'object') continue
    const button = raw as Record<string, unknown>
    out.push({
      label: typeof button.label === 'string' ? button.label : '',
      icon: typeof button.icon === 'string' ? button.icon : '',
    })
  }
  return out
}

/**
 * Renders a command's {@code ThemeImage} icon. A {@code css:<classes>} image (the common case, e.g.
 * a Bootstrap icon) renders as an icon font element; anything else falls back to a generic plus glyph.
 */
function ButtonIcon({ icon }: { icon: string }) {
  if (icon.startsWith('css:')) {
    return <i className={icon.slice('css:'.length)} aria-hidden="true" />
  }
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true">
      <path d="M8 3.5v9M3.5 8h9" />
    </svg>
  )
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
      cssClass: typeof row.cssClass === 'string' && row.cssClass ? row.cssClass : undefined,
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

/** What Tab moves between inside an open drawer; the scrim and the row list itself are left out. */
const FOCUSABLE = 'button:not([disabled]):not([tabindex="-1"]), input:not([disabled]), [tabindex="0"]'

/** The root font size in px, which is what the `drawer-below` rem value is measured in. */
function remInPx(): number {
  return parseFloat(getComputedStyle(document.documentElement).fontSize) || 16
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
  const buttons = useMemo(() => readButtons(state.buttons), [state.buttons])
  /*
   * Row actions. The server says whether there is a menu to open at all - it is off without
   * configured row commands, and off where the frame has no menu to open (a list outside an
   * `<app-shell>`) - and when the trigger is shown. `hover` is the reveal on `:hover` /
   * `:focus-within` the stylesheet does; on a touch device, where there is no hover, the same rule
   * shows it permanently.
   */
  const hasRowMenu = state.hasRowMenu === true
  const rowActions = typeof state.rowActions === 'string' ? state.rowActions : 'hover'
  const showRowMenu = hasRowMenu && rowActions !== 'none'
  /*
   * No fallback to the chat's own words.
   *
   * This element was extracted from the chat's session rail and kept its labels: an unconfigured
   * `<data-list>` of invoices announced itself as "Chats" and said "Noch keine Chats" when it was
   * empty. A generic list has no title unless the view gives it one - inside a rail section the
   * section's own header already names it - and says the neutral thing when it is empty.
   */
  const title = typeof state.title === 'string' ? state.title : ''
  const emptyText =
    typeof state.emptyText === 'string' && state.emptyText ? state.emptyText : labels.railEmpty

  const [query, setQuery] = useState('')
  // The element's first-load state. Not remembered: a `<data-list>` has no stored layout of its
  // own, so this is what every page load starts from.
  const [collapsed, setCollapsed] = useState(state.collapsed === true)

  /*
   * A drawer where the host has no room for a column of this list (#548).
   *
   * The list shares its host with other columns - in a `<chat>` row, the conversation with its
   * #511 floor and the rail's strip - and none of them gives way, so below the width they need
   * together the whole row overflowed into the page's scroller: the composer started off-screen and
   * the chat scrolled in two dimensions. Below `drawerBelow` (rem, measured on the host) a
   * collapsible list therefore keeps only its strip in the flow, and its expand arrow opens the
   * list over its neighbour instead of widening the row. The threshold is the view's to state,
   * because only the view knows what else stands in the row.
   *
   * `collapsed` - the user's choice for the column - is neither read nor written while the list is
   * a drawer, so it is what the list shows again once the host is wide enough. A list that cannot
   * collapse has no strip to fall back to and is never a drawer.
   *
   * Measured before paint, so a narrow page does not first lay out the column it cannot hold.
   */
  const drawerBelow = typeof state.drawerBelow === 'number' ? state.drawerBelow : 0
  const root = useRef<HTMLDivElement | null>(null)
  const toggle = useRef<HTMLButtonElement | null>(null)
  const [drawer, setDrawer] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)

  useLayoutEffect(() => {
    const host = root.current?.parentElement
    if (!collapsible || drawerBelow <= 0 || !host) {
      setDrawer(false)
      return
    }
    const measure = () => setDrawer(host.clientWidth < drawerBelow * remInPx())
    measure()
    const observer = new ResizeObserver(measure)
    observer.observe(host)
    return () => observer.disconnect()
  }, [collapsible, drawerBelow])

  // A host that grows wide again takes the list back as a column; nothing is left open behind it.
  useEffect(() => {
    if (!drawer) setDrawerOpen(false)
  }, [drawer])

  // Opening moves focus into the drawer: to the row that is open, else the first one.
  const aside = useRef<HTMLElement | null>(null)
  useLayoutEffect(() => {
    if (!drawerOpen) return
    const panel = aside.current
    const target =
      panel?.querySelector<HTMLElement>('.rail-open[aria-current="page"]') ??
      panel?.querySelector<HTMLElement>('.rail-open') ??
      listNode.current
    target?.focus()
  }, [drawerOpen])

  /** Closes an open drawer and hands focus back to the arrow that opened it. */
  const closeDrawer = () => {
    if (!drawerOpen) return
    setDrawerOpen(false)
    toggle.current?.focus()
  }

  /*
   * Keys while the drawer is open. Escape closes it - unless it is clearing a search field that has
   * text in it, which is what Escape does there first. Tab wraps inside the drawer, because the
   * scrim says everything behind it is out of reach for now.
   */
  const onDrawerKey = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (!drawerOpen) return
    if (event.key === 'Escape') {
      const target = event.target as HTMLInputElement
      if (target.type === 'search' && target.value) return
      event.preventDefault()
      closeDrawer()
      return
    }
    if (event.key !== 'Tab' || !aside.current) return
    const focusable = [...aside.current.querySelectorAll<HTMLElement>(FOCUSABLE)]
    if (focusable.length === 0) return
    const first = focusable[0]
    const last = focusable[focusable.length - 1]
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault()
      last.focus()
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault()
      first.focus()
    }
  }

  const groups = useMemo(() => {
    const normalized = search ? query.trim().toLocaleLowerCase() : ''
    const visible = normalized
      ? rows.filter((row) => row.lines.some((line) => line.text.toLocaleLowerCase().includes(normalized)))
      : rows
    return groupRows(visible)
  }, [query, rows, search])

  /*
   * Where focus goes when the row whose menu was open disappears - which is what a Delete in that
   * menu does. Left alone, focus falls back to <body> and a keyboard user loses their place in the
   * list; it moves to the neighbouring row's trigger instead (the one before, else the one after,
   * else the list itself).
   */
  const triggers = useRef(new Map<string, HTMLButtonElement>())
  const menuRow = useRef('')
  const previousIds = useRef<string[]>([])
  const listNode = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const ids = rows.map((row) => row.id)
    const before = previousIds.current
    previousIds.current = ids
    const opened = menuRow.current
    if (!opened || ids.includes(opened)) return
    menuRow.current = ''
    const gone = before.indexOf(opened)
    if (gone < 0) return
    const neighbour =
      [...before.slice(0, gone)].reverse().find((id) => ids.includes(id)) ??
      before.slice(gone + 1).find((id) => ids.includes(id))
    const target = neighbour ? triggers.current.get(neighbour) : null
    if (target) target.focus()
    else listNode.current?.focus()
  }, [rows])

  const isCollapsed = collapsible && (drawer ? !drawerOpen : collapsed)
  return (
    <div
      id={controlId}
      ref={root}
      className={rootClassName(
        state,
        'tl-data-list',
        isCollapsed && 'is-collapsed',
        drawer && 'is-drawer',
        drawer && drawerOpen && 'is-drawer-open',
      )}
      onKeyDown={onDrawerKey}
    >
      {drawer && drawerOpen ? (
        // A pointer's way out. Keyboard and screen reader users have the arrow and Escape.
        <div className="data-list-scrim" aria-hidden="true" onClick={closeDrawer} />
      ) : null}
      <aside
        ref={aside}
        className={`rail${isCollapsed ? ' is-collapsed' : ''}`}
        aria-label={title || undefined}
      >
        {/* A header with neither a title nor a collapse button is an empty bar; leave it out. */}
        <div className="rail-header" hidden={!title && !collapsible}>
          <strong>{title}</strong>
          {collapsible ? (
            <button
              type="button"
              className="icon-button rail-collapse"
              ref={toggle}
              aria-label={isCollapsed ? labels.expandChatHistory : labels.collapseChatHistory}
              aria-expanded={!isCollapsed}
              onClick={() => {
                if (!drawer) setCollapsed((value) => !value)
                else if (drawerOpen) closeDrawer()
                else setDrawerOpen(true)
              }}
            >
              <svg viewBox="0 0 16 16" aria-hidden="true">
                <path d={isCollapsed ? 'm6.5 3.5 4.5 4.5-4.5 4.5' : 'M9.5 3.5 5 8l4.5 4.5'} />
              </svg>
            </button>
          ) : null}
        </div>

        {buttons.map((button, index) => (
          <button
            key={index}
            type="button"
            className="rail-new"
            onClick={() => {
              closeDrawer()
              void command('button', { index })
            }}
          >
            <ButtonIcon icon={button.icon} />
            <span>{button.label || labels.newChat}</span>
          </button>
        ))}

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

        <div className="rail-list" aria-live="polite" ref={listNode} tabIndex={-1}>
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
                <div
                  key={row.id}
                  className={`rail-item${row.id === selectedId ? ' is-active' : ''}${
                    row.cssClass ? ` ${row.cssClass}` : ''
                  }`}
                >
                  <button
                    type="button"
                    className="rail-open"
                    aria-current={row.id === selectedId ? 'page' : undefined}
                    title={row.lines[0]?.text}
                    onClick={() => {
                      closeDrawer()
                      void command('select', { id: row.id })
                    }}
                  >
                    <span className="rail-item-mark" aria-hidden="true" />
                    <span className="rail-open-text">
                      {row.lines.map((line, index) => {
                        // A line whose value is empty for this row - a state the row is not in - is
                        // not drawn, so it leaves no gap.
                        if (index > 0 && !line.text) return null
                        const emphasis = line.emphasis ?? (index === 0 ? 'primary' : 'secondary')
                        return (
                          <span key={index} className={`rail-line rail-line-${emphasis}`}>
                            {line.text}
                          </span>
                        )
                      })}
                    </span>
                  </button>
                  {showRowMenu ? (
                    <button
                      type="button"
                      className={`icon-button tiny rail-row-menu${
                        rowActions === 'always' ? ' is-always' : ''
                      }`}
                      aria-haspopup="menu"
                      aria-label={`${labels.rowActions}: ${row.lines[0]?.text ?? ''}`}
                      ref={(node) => {
                        if (node) triggers.current.set(row.id, node)
                        else triggers.current.delete(row.id)
                      }}
                      onClick={(event) => {
                        menuRow.current = row.id
                        void command('rowMenu', {
                          id: row.id,
                          x: Math.round(event.clientX),
                          y: Math.round(event.clientY),
                        })
                      }}
                    >
                      <svg viewBox="0 0 16 16" aria-hidden="true">
                        <circle cx="8" cy="3.5" r="1.1" />
                        <circle cx="8" cy="8" r="1.1" />
                        <circle cx="8" cy="12.5" r="1.1" />
                      </svg>
                    </button>
                  ) : null}
                </div>
              ))}
            </section>
          ))}
        </div>
      </aside>
    </div>
  )
}
