import { describe, expect, it } from 'vitest';
import { applyColumnMove } from './columnMove';
import type { ColumnEntry } from './columnMove';

// The cases of TestReactColumnSelect (columnMove): the client predicts the list the server will push, so both must
// come to the same order, the same locks and the same eyes.

/** Five shown columns a to e, the given ones frozen as the block at the start, the given ones hidden. */
function columns(locked: string[], hidden: string[] = []): ColumnEntry[] {
  const names = ['a', 'b', 'c', 'd', 'e'];
  const entry = (name: string): ColumnEntry => ({
    name, label: name.toUpperCase(), visible: !hidden.includes(name), locked: locked.includes(name), grouped: false,
  });
  return [...names.filter(name => locked.includes(name)), ...names.filter(name => !locked.includes(name))].map(entry);
}

function order(entries: readonly ColumnEntry[]): string[] {
  return entries.map(entry => entry.name);
}

function lockedColumns(entries: readonly ColumnEntry[]): string[] {
  return entries.filter(entry => entry.locked && entry.visible).map(entry => entry.name);
}

function visibleColumns(entries: readonly ColumnEntry[]): string[] {
  return entries.filter(entry => entry.visible).map(entry => entry.name);
}

function move(entries: readonly ColumnEntry[], column: string, targetIndex: number, locked: boolean, visible: boolean) {
  return applyColumnMove(entries, { column, targetIndex, locked, visible });
}

describe('the predicted list of a column move', () => {
  it('keeps a movable column released where the target is at the end of the block', () => {
    const result = move(columns(['a', 'b']), 'd', 1, false, true);

    expect(order(result)).toEqual(['a', 'b', 'd', 'c', 'e']);
    expect(lockedColumns(result)).toEqual(['a', 'b']);
  });

  it('keeps a frozen column at the end of the block where the target lies behind it', () => {
    const result = move(columns(['a', 'b']), 'a', 3, true, true);

    expect(order(result)).toEqual(['b', 'a', 'c', 'd', 'e']);
    expect(lockedColumns(result)).toEqual(['b', 'a']);
  });

  it('moves a frozen column within the block', () => {
    const result = move(columns(['a', 'b', 'c']), 'a', 2, true, true);

    expect(order(result)).toEqual(['b', 'c', 'a', 'd', 'e']);
    expect(lockedColumns(result)).toEqual(['b', 'c', 'a']);
  });

  it('keeps the state at the line: a frozen column ends the block, a movable one starts the rest', () => {
    expect(lockedColumns(move(columns(['a', 'b']), 'a', 1, true, true))).toEqual(['b', 'a']);

    const unlocked = move(columns(['a', 'b']), 'e', 2, false, true);
    expect(lockedColumns(unlocked)).toEqual(['a', 'b']);
    expect(order(unlocked)).toEqual(['a', 'b', 'e', 'c', 'd']);
  });

  it('freezes a movable column dropped into the block, at the dropped place', () => {
    const result = move(columns(['a', 'b']), 'c', 1, true, true);

    expect(order(result)).toEqual(['a', 'c', 'b', 'd', 'e']);
    expect(lockedColumns(result)).toEqual(['a', 'c', 'b']);
  });

  it('releases a frozen column dropped behind the block, at the dropped place', () => {
    const result = move(columns(['a', 'b', 'c']), 'a', 4, false, true);

    expect(order(result)).toEqual(['b', 'c', 'd', 'e', 'a']);
    expect(lockedColumns(result)).toEqual(['b', 'c']);
  });

  it('shows a hidden column at the target index', () => {
    const before = columns(['a'], ['b', 'd']);
    expect(visibleColumns(before)).toEqual(['a', 'c', 'e']);

    const result = move(before, 'd', 2, false, true);

    expect(order(result)).toEqual(['a', 'b', 'd', 'c', 'e']);
    expect(visibleColumns(result)).toEqual(['a', 'd', 'c', 'e']);
    expect(lockedColumns(result)).toEqual(['a']);
  });

  it('shows and freezes a hidden column dropped into the block', () => {
    const result = move(columns(['a', 'b'], ['d']), 'd', 0, true, true);

    expect(order(result)).toEqual(['d', 'a', 'b', 'c', 'e']);
    expect(lockedColumns(result)).toEqual(['d', 'a', 'b']);
    expect(visibleColumns(result)).toContain('d');
  });

  it('hides a column in its place and ignores the target index', () => {
    const result = move(columns(['a']), 'c', 0, false, false);

    expect(order(result)).toEqual(['a', 'b', 'c', 'd', 'e']);
    expect(visibleColumns(result)).toEqual(['a', 'b', 'd', 'e']);
    expect(lockedColumns(result)).toEqual(['a']);
  });

  it('releases a frozen column it hides, which then rests directly behind the block', () => {
    const result = move(columns(['a', 'b', 'c']), 'a', 0, true, false);

    expect(order(result)).toEqual(['b', 'c', 'a', 'd', 'e']);
    expect(lockedColumns(result)).toEqual(['b', 'c']);
    expect(visibleColumns(result)).not.toContain('a');
    expect(result.find(entry => entry.name === 'a')!.locked).toBe(false);
  });

  it('does not hide the last shown column: the list stays as it is', () => {
    const before: ColumnEntry[] = [
      { name: 'a', label: 'A', visible: true, locked: false, grouped: false },
      { name: 'b', label: 'B', visible: false, locked: false, grouped: false },
    ];

    expect(move(before, 'a', 1, false, false)).toBe(before);
  });

  it('clamps a target index beyond the list', () => {
    const first = move(columns(['a']), 'b', 99, false, true);
    expect(order(first)).toEqual(['a', 'c', 'd', 'e', 'b']);

    const second = move(first, 'e', -5, true, true);
    expect(order(second)).toEqual(['e', 'a', 'c', 'd', 'b']);
    expect(lockedColumns(second)).toEqual(['e', 'a']);
  });

  it('changes nothing for an unknown column', () => {
    const before = columns(['a']);

    expect(move(before, 'x', 0, false, true)).toBe(before);
  });

  it('keeps the order for a drop where the column already is', () => {
    const result = move(columns(['a']), 'c', 2, false, true);

    expect(order(result)).toEqual(['a', 'b', 'c', 'd', 'e']);
    expect(lockedColumns(result)).toEqual(['a']);
  });

  it('leaves the given list as it is', () => {
    const before = columns(['a', 'b']);
    const copy = before.map(entry => ({ ...entry }));

    move(before, 'a', 4, false, true);
    move(before, 'b', 0, false, false);

    expect(before).toEqual(copy);
  });
});
