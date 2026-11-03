import { describe, expect, it } from 'vitest';
import { fullCount, fullIndex, shownColumns, shownCount } from './compactColumns';

interface Column {
  name: string;
  hideWhenCompact?: boolean;
}

/** The columns of the given names; a name ending in '*' is a column a phone can do without. */
function columns(...names: string[]): Column[] {
  return names.map(name => name.endsWith('*')
    ? { name: name.slice(0, -1), hideWhenCompact: true }
    : { name });
}

function names(list: Column[]): string[] {
  return list.map(column => column.name);
}

/**
 * The order of all columns after the server took the column out and inserted it at the given position, as
 * TableView.moveColumn does.
 */
function moved(all: Column[], column: Column, target: number): Column[] {
  const order = all.filter(other => other !== column);
  order.splice(target, 0, column);
  return order;
}

describe('the columns a table displays while the window is compact', () => {
  it('are all of them in the regular display', () => {
    const all = columns('a', 'b*', 'c');
    expect(shownColumns(all, false)).toBe(all);
  });

  it('leave out the ones a phone can do without', () => {
    expect(names(shownColumns(columns('a', 'b*', 'c', 'd*'), true))).toEqual(['a', 'c']);
  });
});

describe('the frozen columns of a table in the compact display', () => {
  const all = columns('a', 'b*', 'c', 'd');
  const shown = shownColumns(all, true);

  it('are the displayed ones among the frozen columns the server counts', () => {
    expect(shownCount(all, shown, 0)).toBe(0);
    expect(shownCount(all, shown, 1)).toBe(1);
    expect(shownCount(all, shown, 2)).toBe(1);
    expect(shownCount(all, shown, 3)).toBe(2);
    expect(shownCount(all, shown, 4)).toBe(3);
  });

  it('are sent to the server counting the columns left out before the last of them', () => {
    expect(fullCount(all, shown, 0)).toBe(0);
    expect(fullCount(all, shown, 1)).toBe(1);
    expect(fullCount(all, shown, 2)).toBe(3);
    expect(fullCount(all, shown, 3)).toBe(4);
  });

  it('are counted the same way in the regular display', () => {
    expect(fullCount(all, all, 2)).toBe(2);
    expect(shownCount(all, all, 2)).toBe(2);
  });

  it('survive the way to the server and back', () => {
    for (let count = 0; count <= shown.length; count++) {
      expect(shownCount(all, shown, fullCount(all, shown, count))).toBe(count);
    }
  });
});

describe('a column moved in the compact display', () => {
  const all = columns('a', 'b*', 'c', 'd*', 'e');
  const shown = shownColumns(all, true);

  it('lands at every displayed position it is moved to', () => {
    for (let from = 0; from < shown.length; from++) {
      for (let to = 0; to < shown.length; to++) {
        const column = shown[from];
        const order = moved(all, column, fullIndex(all, shown, to));
        const expected = shown.filter(other => other !== column);
        expected.splice(to, 0, column);
        expect(names(shownColumns(order, true)), `${column.name} to ${to}`).toEqual(names(expected));
      }
    }
  });

  it('keeps the columns left out next to the displayed column they followed', () => {
    // e moved in front of c: d still follows c, b still follows a.
    expect(names(moved(all, all[4], fullIndex(all, shown, 1)))).toEqual(['a', 'b', 'e', 'c', 'd']);
  });

  it('is sent with its displayed position in the regular display', () => {
    expect(fullIndex(all, all, 3)).toBe(3);
  });
});
