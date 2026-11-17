import {
  React, useTLState, useTLCommand, useI18N, usePart, useFocusTrap, useStandaloneKeyboardScope, TLChild, rootClassName,
  stateAttr, ICON_ROW_MENU,
} from 'tl-react-bridge';
import type { TLCellProps, DataListStateJson, ChildControlJson } from 'tl-react-bridge';
import { SearchField } from './SearchField';

const { useEffect, useLayoutEffect, useMemo, useRef, useState } = React;

/** The command selecting a row, see DataListStateJson. */
export const CMD_SELECT = 'select';

/** The command opening the commands of a row, see DataListStateJson. */
export const CMD_ROW_MENU = 'rowMenu';

/** Argument of {@link CMD_SELECT} and {@link CMD_ROW_MENU}: the ID of the row. */
export const ARG_ID = 'id';

/** Argument of {@link CMD_ROW_MENU}: the client x coordinate to open the menu at. */
export const ARG_X = 'x';

/** Argument of {@link CMD_ROW_MENU}: the client y coordinate to open the menu at. */
export const ARG_Y = 'y';

const I18N_KEYS = {
  'js.dataList.collapse': 'Collapse list',
  'js.dataList.expand': 'Expand list',
  'js.dataList.search': 'Search',
  'js.dataList.empty': 'Nothing to show.',
  'js.dataList.noMatch': 'No matching entries.',
  'js.dataList.rowActions': 'Actions',
};

type Labels = Record<keyof typeof I18N_KEYS, string>;

type Row = DataListStateJson.Row;
type Line = DataListStateJson.Line;

interface RowGroup {
  label: string;
  rows: Row[];
}

/** Buckets rows by their server-computed group label, groups in first-appearance order. */
function groupRows(rows: Row[]): RowGroup[] {
  const order: string[] = [];
  const groups = new Map<string, Row[]>();
  for (const row of rows) {
    const label = row.group ?? '';
    let bucket = groups.get(label);
    if (!bucket) {
      bucket = [];
      groups.set(label, bucket);
      order.push(label);
    }
    bucket.push(row);
  }
  return order.map((label) => ({ label, rows: groups.get(label)! }));
}

/** The text of the first line of a row, its name in tooltips and in the name of its trigger. */
function firstText(row: Row): string {
  return row.lines?.[0]?.text ?? '';
}

/** The root font size in px, which is what the `drawerBelow` rem value is measured in. */
function remInPx(): number {
  return parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
}

/** The ID of the heading of a group, unique in the page for the given prefix. */
function groupId(prefix: string, label: string): string {
  return `${prefix}-group-${label}`;
}

/** What one rendering of the list (the column, the strip, the list in the open drawer) needs. */
interface ListPartsProps {
  /** Prefix of the IDs inside, unique per rendering. */
  idPrefix: string;
  labels: Labels;
  title: string;
  collapsible: boolean;
  /** Whether the toggle says the list is shown. */
  expanded: boolean;
  onToggle: () => void;
  toggleRef?: React.Ref<HTMLButtonElement>;
  /** Whether to render the commands of the list here. */
  showButtons: boolean;
  buttons: ChildControlJson[];
  onCommand: () => void;
  /** Whether to render search and rows; the strip of a drawer has neither. */
  showContent: boolean;
  search: boolean;
  query: string;
  onQuery: (query: string) => void;
  groups: RowGroup[];
  emptyText: string;
  selectedId: string;
  onSelect: (row: Row) => void;
  showRowMenu: boolean;
  revealRowMenu: boolean;
  onRowMenu: (row: Row, event: React.MouseEvent<HTMLElement>) => void;
  triggerRef: (row: Row, node: HTMLButtonElement | null) => void;
  bodyRef?: React.Ref<HTMLDivElement>;
}

/** Header, commands, search and body of the list (design system: tl-list). */
function ListParts(props: ListPartsProps) {
  const DisclosureToggle = usePart('DisclosureToggle');
  const MenuTrigger = usePart('MenuTrigger');
  const { labels, title, query } = props;

  return (
    <>
      {/* A header with neither a title nor a toggle is an empty bar; leave it out. */}
      <div className="tl-list__header" hidden={!title && !props.collapsible}>
        {title ? <span className="tl-list__title tl-type-heading-sm">{title}</span> : null}
        {props.collapsible ? (
          <DisclosureToggle
            ref={props.toggleRef}
            expanded={props.expanded}
            opens="end"
            label={props.expanded ? labels['js.dataList.collapse'] : labels['js.dataList.expand']}
            onClick={props.onToggle}
          />
        ) : null}
      </div>

      {/*
        * A command is the engine's command button: it shows its command's state - hidden, disabled
        * with the reason as tooltip - and runs the command itself. A click on it closes an open drawer.
        */}
      {props.showButtons && props.buttons.length > 0 ? (
        <div className="tl-list__commands" onClick={props.onCommand}>
          {props.buttons.map((button) => (
            <TLChild key={button.controlId} control={button} />
          ))}
        </div>
      ) : null}

      {props.showContent && props.search ? (
        <SearchField
          className="tl-list__search"
          value={query}
          onChange={props.onQuery}
          label={labels['js.dataList.search']}
          placeholder={labels['js.dataList.search']}
        />
      ) : null}

      {props.showContent ? (
        <div className="tl-list__body" aria-live="polite" tabIndex={-1} ref={props.bodyRef}>
          {props.groups.length === 0 ? (
            <p className="tl-list__message tl-type-label">
              {query ? labels['js.dataList.noMatch'] : props.emptyText}
            </p>
          ) : null}
          {props.groups.map((group) => (
            <section
              className="tl-list__group"
              key={group.label}
              aria-labelledby={group.label ? groupId(props.idPrefix, group.label) : undefined}
              aria-label={group.label ? undefined : title || undefined}
            >
              {group.label ? (
                <h2 className="tl-list__subheader tl-type-label-strong" id={groupId(props.idPrefix, group.label)}>
                  {group.label}
                </h2>
              ) : null}
              <ul className="tl-list__items">
                {group.rows.map((row) => (
                  <li key={row.id} className={['tl-list__item', row.cssClass].filter(Boolean).join(' ')}>
                    <button
                      type="button"
                      className="tl-list__main"
                      aria-current={row.id === props.selectedId ? 'true' : undefined}
                      title={firstText(row)}
                      onClick={() => props.onSelect(row)}
                    >
                      <span className="tl-list__text">
                        {(row.lines ?? []).map((line: Line, index: number) => {
                          // A line whose value is empty for this row - a state the row is not in - is
                          // not drawn, so it leaves no gap.
                          if (index > 0 && !line.text) return null;
                          const emphasis = line.emphasis ?? (index === 0 ? 'primary' : 'secondary');
                          return emphasis === 'primary' ? (
                            <span key={index} className="tl-list__primary tl-type-body">{line.text}</span>
                          ) : (
                            <span key={index} className="tl-list__secondary tl-type-label">{line.text}</span>
                          );
                        })}
                      </span>
                    </button>
                    {props.showRowMenu ? (
                      <MenuTrigger
                        appearance="ghost"
                        small
                        expanded={false}
                        icon={ICON_ROW_MENU}
                        label={`${labels['js.dataList.rowActions']}: ${firstText(row)}`}
                        className={props.revealRowMenu ? 'tl-reveal-on-hover' : undefined}
                        ref={(node: HTMLButtonElement | null) => props.triggerRef(row, node)}
                        onClick={(event) => props.onRowMenu(row, event)}
                      />
                    ) : null}
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      ) : null}
    </>
  );
}

/**
 * A selectable, groupable, searchable list of objects in the style of a contact or message list
 * (design system: tl-list), the client of the element `<data-list>`.
 *
 * The state is described by DataListStateJson. The rows come from the server; search and collapse
 * are client-side, a row click sends {@link CMD_SELECT}, the trigger of a row {@link CMD_ROW_MENU}.
 *
 * Below `drawerBelow` rem of width of its host, a collapsible list keeps only its strip in the flow,
 * and its toggle opens the list as a modal drawer over its neighbours (tl-drawer--container with
 * tl-drawer__scrim): focus moves to the selected row and stays in the drawer; Escape, the scrim, the
 * toggle, a row and a command close it and hand the focus back to the toggle of the strip.
 */
const TLDataList: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DataListStateJson>>();
  const command = useTLCommand();
  const labels = useI18N(I18N_KEYS) as Labels;

  const rows = useMemo(() => (Array.isArray(state.rows) ? state.rows : []), [state.rows]);
  const buttons = useMemo(() => (Array.isArray(state.buttons) ? state.buttons : []), [state.buttons]);
  const selectedId = state.selectedId ?? '';
  const search = state.search === true;
  const collapsible = state.collapsible === true;
  const title = state.title ?? '';
  const emptyText = state.emptyText || labels['js.dataList.empty'];
  /*
   * Row actions. The server says whether there is a menu to open at all - it is off without row
   * commands, and off where the frame has no menu to open (a list outside an `<app-shell>`) - and
   * when the trigger is shown. `hover` is the reveal of tl-reveal-on-hover; on a touch device, where
   * there is no hover, the same rule shows it permanently.
   */
  const rowActions = state.rowActions ?? 'hover';
  const showRowMenu = state.hasRowMenu === true && rowActions !== 'none';

  const [query, setQuery] = useState('');
  // The first-load state. Not remembered: a `<data-list>` has no stored layout of its own.
  const [collapsed, setCollapsed] = useState(state.collapsed === true);

  /*
   * The drawer, where the host has no room for a column of this list. `collapsed` - the user's
   * choice for the column - is neither read nor written while the list is a drawer, so it is what the
   * list shows again once the host is wide enough. A list that cannot collapse has no strip to fall
   * back to and is never a drawer. Measured before paint, so a narrow host does not first lay out
   * the column it cannot hold.
   */
  const drawerBelow = typeof state.drawerBelow === 'number' ? state.drawerBelow : 0;
  const root = useRef<HTMLDivElement | null>(null);
  const stripToggle = useRef<HTMLButtonElement | null>(null);
  const aside = useRef<HTMLElement | null>(null);
  const [drawer, setDrawer] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);

  useLayoutEffect(() => {
    const host = root.current?.parentElement;
    if (!collapsible || drawerBelow <= 0 || !host) {
      setDrawer(false);
      return;
    }
    const measure = () => setDrawer(host.clientWidth < drawerBelow * remInPx());
    measure();
    const observer = new ResizeObserver(measure);
    observer.observe(host);
    return () => observer.disconnect();
  }, [collapsible, drawerBelow]);

  // A host that grows wide again takes the list back as a column; nothing is left open behind it.
  useEffect(() => {
    if (!drawer) setDrawerOpen(false);
  }, [drawer]);

  const open = drawer && drawerOpen;

  // Opening moves the focus to the selected row, else the first one, and keeps it in the drawer;
  // closing gives it back to the toggle of the strip.
  useFocusTrap(open, aside, (container) =>
    container.querySelector<HTMLElement>('.tl-list__main[aria-current="true"]')
    ?? container.querySelector<HTMLElement>('.tl-list__main'),
  { restoreTo: () => stripToggle.current });

  // Escape closes the drawer - unless it is clearing a search field that has text in it, which is
  // what Escape does there first.
  useStandaloneKeyboardScope(open, {
    ESCAPE: () => {
      const active = document.activeElement as HTMLInputElement | null;
      if (active?.type === 'search' && active.value) return false;
      setDrawerOpen(false);
      return true;
    },
  });

  const closeDrawer = () => setDrawerOpen(false);

  const groups = useMemo(() => {
    const normalized = search ? query.trim().toLocaleLowerCase() : '';
    const visible = normalized
      ? rows.filter((row) => (row.lines ?? []).some((line) => (line.text ?? '').toLocaleLowerCase().includes(normalized)))
      : rows;
    return groupRows(visible);
  }, [query, rows, search]);

  /*
   * Where focus goes when the row whose menu was open disappears - which is what a Delete in that
   * menu does. Left alone, focus falls back to <body> and a keyboard user loses their place in the
   * list; it moves to the neighbouring row's trigger instead (the one before, else the one after,
   * else the list itself).
   */
  const triggers = useRef(new Map<string, HTMLButtonElement>());
  const menuRow = useRef('');
  const previousIds = useRef<string[]>([]);
  const body = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const ids = rows.map((row) => row.id);
    const before = previousIds.current;
    previousIds.current = ids;
    const opened = menuRow.current;
    if (!opened || ids.includes(opened)) return;
    menuRow.current = '';
    const gone = before.indexOf(opened);
    if (gone < 0) return;
    const neighbour =
      [...before.slice(0, gone)].reverse().find((id) => ids.includes(id)) ??
      before.slice(gone + 1).find((id) => ids.includes(id));
    const target = neighbour ? triggers.current.get(neighbour) : null;
    if (target) target.focus();
    else body.current?.focus();
  }, [rows]);

  const shared = {
    labels,
    title,
    collapsible,
    buttons,
    onCommand: closeDrawer,
    search,
    query,
    onQuery: setQuery,
    groups,
    emptyText,
    selectedId,
    onSelect: (row: Row) => {
      closeDrawer();
      void command(CMD_SELECT, { [ARG_ID]: row.id });
    },
    showRowMenu,
    revealRowMenu: rowActions !== 'always',
    onRowMenu: (row: Row, event: React.MouseEvent<HTMLElement>) => {
      menuRow.current = row.id;
      void command(CMD_ROW_MENU, {
        [ARG_ID]: row.id,
        [ARG_X]: Math.round(event.clientX),
        [ARG_Y]: Math.round(event.clientY),
      });
    },
    triggerRef: (row: Row, node: HTMLButtonElement | null) => {
      if (node) triggers.current.set(row.id, node);
      else triggers.current.delete(row.id);
    },
  };

  const isCollapsed = collapsible && (drawer || collapsed);
  return (
    <div
      id={controlId}
      ref={root}
      className={rootClassName(state, 'tl-list')}
      data-tl-state={stateAttr(isCollapsed && 'collapsed')}
      aria-label={title || undefined}
    >
      <ListParts
        {...shared}
        idPrefix={controlId}
        expanded={!isCollapsed}
        onToggle={() => (drawer ? setDrawerOpen(true) : setCollapsed((value) => !value))}
        toggleRef={stripToggle}
        showButtons={!open}
        showContent={!drawer}
        bodyRef={drawer ? undefined : body}
      />
      {open ? (
        <>
          {/* A pointer's way out. Keyboard and screen reader users have the toggle and Escape. */}
          <div className="tl-drawer__scrim" aria-hidden="true" onClick={closeDrawer} />
          <aside
            ref={aside}
            className="tl-drawer tl-drawer--start tl-drawer--narrow tl-drawer--container"
            data-tl-state="open"
            role="dialog"
            aria-modal="true"
            aria-label={title || undefined}
            tabIndex={-1}
          >
            <div className="tl-list">
              <ListParts
                {...shared}
                idPrefix={`${controlId}-drawer`}
                expanded
                onToggle={closeDrawer}
                showButtons
                showContent
                bodyRef={body}
              />
            </div>
          </aside>
        </>
      ) : null}
    </div>
  );
};

export default TLDataList;
