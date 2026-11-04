import { React, useTLState, useTLCommand, useI18N, useRovingMenu, filterOptions, stateAttr, firstFocusable } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { SearchField } from '../SearchField';

const { useState, useRef, useCallback } = React;

const I18N_KEYS = {
  'js.table.filterAll': 'All',
  'js.table.filterSearch': 'Search values…',
  'js.table.filterNoMatch': 'No value matches',
  'js.table.filterInvert': 'Invert selection',
  'js.table.clear': 'Remove filter',
};

/** Checks or unchecks the value the `key` argument names. */
const CMD_TOGGLE = 'toggle';

/** Checks every value (`checked` true) or unchecks every value. */
const CMD_ALL = 'all';

/** Turns every check mark. */
const CMD_INVERT = 'invert';

/** Removes the filter: every value is checked again. */
const CMD_CLEAR = 'clear';

/** The key of the row "All" among the keys of the rows of the menu; the values have numbers as keys. */
const ALL_KEY = '*';

/** One value of the list, as the server sends it. */
interface FilterEntry {
  /** What a command names the value by. */
  key: string;
  label: string;
  /** The number of rows the value matches; absent where the table does not count. */
  count?: number;
  /** Whether the value is checked, i.e. its rows are shown. */
  checked: boolean;
}

/**
 * Where the focus goes in the content of a filter popover: its first field - the search of a value list, the first
 * input of a form -, else its first focusable element - the first row of a value list. A help button in the label of a
 * field is passed over: it stands before the field, but nobody opens a filter to read its help.
 */
export function firstFieldOrFocusable(root: HTMLElement): HTMLElement | null {
  return firstFocusable(root, true) ?? firstFocusable(root);
}

/** The state of "All" for the given values: `true` if all are checked, `false` if none is, else `mixed`. */
function allState(entries: readonly FilterEntry[]): boolean | 'mixed' {
  const checked = entries.filter(entry => entry.checked).length;
  return checked === entries.length ? true : checked === 0 ? false : 'mixed';
}

/**
 * The value list of a column filter that chooses among values (design system: tl-filter-list), the content of the
 * filter popover at the funnel of the column: a check row per value, headed by "All", with a search from eight values
 * on, and a footer to remove the filter and to invert the selection.
 *
 * <p>Checked is shown, unchecked is hidden. Every click takes effect at once: the row sends its command, the server
 * filters the table and sends the check marks back (see ReactFilterListControl). "All" checks every value, or, when
 * all are checked, unchecks every one - so that one value can then be checked alone; it is `mixed` while some are
 * checked. A value without matches is quiet but stays a row that clicks.</p>
 *
 * <p>The rows are a menu named after the column, each a `menuitemcheckbox` whose whole width clicks; the box in it is a
 * drawing of the state of the row, no input. One row is in the tab order, the arrows, Home and End move between the
 * rows (useRovingMenu). The search stands above the menu, outside of its roving, and narrows the rows in the client;
 * "All" is left out while it narrows, since it acts on every value, the hidden ones included.</p>
 *
 * <p>The elements are children of the popover around the content, a flex column (tl-picker): the search and the footer
 * stand, the menu between them scrolls (tl-picker__scroll).</p>
 */
const TLFilterList: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const entries = (state.entries as FilterEntry[] | undefined) ?? [];
  const label = (state.label as string | undefined) ?? '';
  const searchable = state.searchable === true;
  const active = state.active === true;

  const [search, setSearch] = useState('');
  const searching = searchable && search.trim() !== '';
  const shown = searching ? filterOptions(entries, search) : entries;

  // The rows are the tab stop one at a time: the one last focused while it is shown, else the first.
  const menuRef = useRef<HTMLDivElement | null>(null);
  const searchRef = useRef<HTMLInputElement | null>(null);
  const [focusKey, setFocusKey] = useState<string | null>(null);
  const keys = [...(searching ? [] : [ALL_KEY]), ...shown.map(entry => entry.key)];
  const tabStop = focusKey !== null && keys.includes(focusKey) ? focusKey : keys[0];
  const { onKeyDown } = useRovingMenu(menuRef, true);

  const all = allState(entries);

  const handleAll = useCallback(() => {
    sendCommand(CMD_ALL, { checked: all !== true });
  }, [all, sendCommand]);

  const handleClear = useCallback(() => {
    // The button is disabled by its own click, and a disabled button drops the focus: it goes to the rows before, or,
    // while the search shows no row, to the search.
    (menuRef.current?.querySelector<HTMLElement>('[tabindex="0"]') ?? searchRef.current)?.focus();
    sendCommand(CMD_CLEAR, {});
  }, [sendCommand]);

  const rowProps = (key: string, checked: boolean | 'mixed') => ({
    type: 'button' as const,
    role: 'menuitemcheckbox',
    'aria-checked': checked,
    tabIndex: key === tabStop ? 0 : -1,
    onFocus: () => setFocusKey(key),
  });

  return (
    <>
      {searchable && (
        <SearchField
          label={i18n['js.table.filterSearch']}
          placeholder={i18n['js.table.filterSearch']}
          value={search}
          onChange={setSearch}
          inputRef={searchRef}
        />
      )}
      {shown.length === 0 && searching ? (
        <div id={controlId} className="tl-status tl-type-body" role="status">{i18n['js.table.filterNoMatch']}</div>
      ) : (
        <div
          ref={menuRef}
          id={controlId}
          className="tl-picker__scroll"
          role="menu"
          aria-label={label}
          onKeyDown={onKeyDown}
        >
          {!searching && (
            <button
              {...rowProps(ALL_KEY, all)}
              className="tl-menu__item tl-menu__item--check tl-filter-list__all tl-type-body"
              onClick={handleAll}
            >
              <span className="tl-checkbox" aria-hidden="true" />
              <span className="tl-menu__label">{i18n['js.table.filterAll']}</span>
            </button>
          )}
          {shown.map(entry => (
            <button
              key={entry.key}
              {...rowProps(entry.key, entry.checked)}
              className="tl-menu__item tl-menu__item--check tl-type-body"
              data-tl-state={stateAttr(entry.count === 0 && 'empty')}
              onClick={() => sendCommand(CMD_TOGGLE, { key: entry.key })}
            >
              <span className="tl-checkbox" aria-hidden="true" />
              <span className="tl-menu__label">{entry.label}</span>
              {entry.count !== undefined && (
                <span className="tl-filter-list__count tl-type-label tl-nums">{entry.count}</span>
              )}
            </button>
          ))}
        </div>
      )}
      <FilterFooter active={active} clearLabel={i18n['js.table.clear']} onClear={handleClear}>
        <button
          type="button"
          className="tl-button tl-button--ghost tl-type-label"
          onClick={() => sendCommand(CMD_INVERT, {})}
        >
          {i18n['js.table.filterInvert']}
        </button>
      </FilterFooter>
    </>
  );
};

/**
 * The footer of a filter popover (tl-filter-list__footer): the action removing the filter of the column - disabled
 * while the column is not filtered - followed by the further actions of the content.
 */
export const FilterFooter: React.FC<{
  /** Whether the column is filtered now. */
  active: boolean;
  clearLabel: string;
  onClear: () => void;
  children?: React.ReactNode;
}> = ({ active, clearLabel, onClear, children }) => (
  <div className="tl-filter-list__footer">
    <button type="button" className="tl-button tl-button--ghost tl-type-label" disabled={!active} onClick={onClear}>
      {clearLabel}
    </button>
    {children}
  </div>
);

export default TLFilterList;
