import {
  React, useTLState, useTLCommand, useI18N, usePart, TLChild, rootClassName, ThemeIcon, ICON_ROW_MENU,
  isInteractiveTarget,
} from 'tl-react-bridge';
import type { TLCellProps, DataListStateJson, ChildControlJson } from 'tl-react-bridge';
import { SearchField } from './SearchField';

const { useEffect, useMemo, useRef, useState } = React;

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

/**
 * Options of a focus moved by the list itself to where the user's place in it is now: shown with
 * its ring whatever device moved it there (the `focusVisible` option of the HTML standard, which the
 * DOM types of TypeScript do not know yet).
 */
const FOCUS_VISIBLE = { focusVisible: true } as FocusOptions;

/** The ID of the heading of a group, unique in the page for the given prefix. */
function groupId(prefix: string, label: string): string {
  return `${prefix}-group-${label}`;
}

/** What the parts of the list need. */
interface ListPartsProps {
  /** Prefix of the IDs inside, unique per list. */
  idPrefix: string;
  labels: Labels;
  title: string;
  buttons: ChildControlJson[];
  search: boolean;
  query: string;
  onQuery: (query: string) => void;
  groups: RowGroup[];
  emptyText: string;
  selectedId: string;
  onSelect: (row: Row) => void;
  /** A right click on a row, before the event goes on to a context menu region around the list. */
  onRowContextMenu: (row: Row) => void;
  showRowMenu: boolean;
  revealRowMenu: boolean;
  onRowMenu: (row: Row, event: React.MouseEvent<HTMLElement>) => void;
  triggerRef: (row: Row, node: HTMLButtonElement | null) => void;
  bodyRef?: React.Ref<HTMLDivElement>;
}

/** Header, commands, search and body of the list (design system: tl-list). */
function ListParts(props: ListPartsProps) {
  const MenuTrigger = usePart('MenuTrigger');
  const Avatar = usePart('Avatar');
  const Badge = usePart('Badge');
  const { labels, title, query } = props;

  return (
    <>
      {/* A header without a title is an empty bar; leave it out. */}
      <div className="tl-list__header" hidden={!title}>
        {title ? <span className="tl-list__title tl-type-heading-sm">{title}</span> : null}
      </div>

      {/*
        * A command is the engine's command button: it shows its command's state - hidden, disabled
        * with the reason as tooltip - and runs the command itself.
        */}
      {props.buttons.length > 0 ? (
        <div className="tl-list__commands">
          {props.buttons.map((button) => (
            <TLChild key={button.controlId} control={button} />
          ))}
        </div>
      ) : null}

      {props.search ? (
        <SearchField
          className="tl-list__search"
          value={query}
          onChange={props.onQuery}
          label={labels['js.dataList.search']}
          placeholder={labels['js.dataList.search']}
        />
      ) : null}

      {
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
                  <li
                    key={row.id}
                    className={['tl-list__item', row.cssClass].filter(Boolean).join(' ')}
                    onContextMenu={() => props.onRowContextMenu(row)}
                  >
                    <button
                      type="button"
                      className="tl-list__main"
                      aria-current={row.id === props.selectedId ? 'true' : undefined}
                      title={firstText(row)}
                      onClick={() => props.onSelect(row)}
                    >
                      {row.leading ? (
                        <span className="tl-list__leading">
                          {row.leading.icon ? (
                            <ThemeIcon encoded={row.leading.icon} className="tl-icon-md" />
                          ) : (
                            // The name of the avatar is what the lines say; the button reads it once.
                            <Avatar name={row.leading.name || null} src={row.leading.image || null} decorative />
                          )}
                        </span>
                      ) : null}
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
                      {row.trailing && !row.trailing.control ? (
                        row.trailing.badge ? (
                          <span className="tl-list__trailing"><Badge content={row.trailing.text} /></span>
                        ) : (
                          <span className="tl-list__trailing tl-type-label">{row.trailing.text}</span>
                        )
                      ) : null}
                    </button>
                    {row.trailing?.control ? (
                      // Content of the application's template: beside the button of the entry, not
                      // in it, since it may hold interactive elements of its own. A click on such an
                      // element is left to it; a click on anything else selects the entry, like a
                      // click on the button.
                      <span
                        className="tl-list__trailing"
                        onClick={(event) => {
                          if (!isInteractiveTarget(event.target)) props.onSelect(row);
                        }}
                      >
                        <TLChild control={row.trailing.control} />
                      </span>
                    ) : null}
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
      }
    </>
  );
}

/**
 * A selectable, groupable, searchable list of objects in the style of a contact or message list
 * (design system: tl-list), the client of the element `<data-list>`.
 *
 * The state is described by DataListStateJson. The rows come from the server; search is
 * client-side, a row click sends {@link CMD_SELECT}, the trigger of a row {@link CMD_ROW_MENU}.
 * In front of the lines a row shows an icon or an avatar (part `Avatar`), after them metadata text, a
 * badge (part `Badge`), or the control of the application's template for the row, beside the button of
 * the entry. A right click on a row outside the selection selects it and lets the event
 * go on to a context menu region (`<context-menu>`) around the list.
 */
const TLDataList: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DataListStateJson>>();
  const command = useTLCommand();
  const labels = useI18N(I18N_KEYS) as Labels;

  const rows = useMemo(() => (Array.isArray(state.rows) ? state.rows : []), [state.rows]);
  const buttons = useMemo(() => (Array.isArray(state.buttons) ? state.buttons : []), [state.buttons]);
  const selectedId = state.selectedId ?? '';
  const search = state.search === true;
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
    // The trigger is revealed only while it shows the focus ring (tl-reveal-on-hover): moved here by
    // code after a Delete chosen with the mouse, the focus would otherwise land on an invisible button.
    const target = neighbour ? triggers.current.get(neighbour) : null;
    if (target) target.focus(FOCUS_VISIBLE);
    else body.current?.focus(FOCUS_VISIBLE);
  }, [rows]);

  const shared = {
    labels,
    title,
    buttons,
    search,
    query,
    onQuery: setQuery,
    groups,
    emptyText,
    selectedId,
    onSelect: (row: Row) => {
      void command(CMD_SELECT, { [ARG_ID]: row.id });
    },
    /*
     * The rule of a context menu around the list: a right click on a row outside the selection
     * selects the row, on the selected row it keeps the selection. The event is left alone, so that
     * it reaches the menu region; the commands of the page go through one FIFO channel, so the
     * selection reaches the server before the region asks for its menu.
     */
    onRowContextMenu: (row: Row) => {
      if (row.id !== selectedId) {
        void command(CMD_SELECT, { [ARG_ID]: row.id });
      }
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

  return (
    <div
      id={controlId}
      className={rootClassName(state, 'tl-list')}
      aria-label={title || undefined}
    >
      <ListParts {...shared} idPrefix={controlId} bodyRef={body} />
    </div>
  );
};

export default TLDataList;
