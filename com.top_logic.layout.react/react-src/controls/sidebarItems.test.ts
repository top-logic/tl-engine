import { describe, expect, it } from 'vitest';
import { drawnItems, flyoutEntries, hasDividerBefore } from './sidebarItems';
import type { SidebarItemData } from './sidebarItems';

function nav(id: string, hidden?: boolean): SidebarItemData {
  return { id, type: 'nav', label: id, hidden };
}

function section(id: string, children: SidebarItemData[], label?: string): SidebarItemData {
  return { id, type: 'section', label, children };
}

function group(id: string, children: SidebarItemData[]): SidebarItemData {
  return { id, type: 'group', label: id, expanded: true, children };
}

/** The entries of a list, a divider written as '|' in front of each entry that gets one. */
function dividedIds(entries: SidebarItemData[]): string[] {
  const result: string[] = [];
  entries.forEach((item, index) => {
    if (hasDividerBefore(entries, index)) result.push('|');
    result.push(item.id);
  });
  return result;
}

/** The drawn entries of a list, a divider written as '|' in front of each entry that gets one. */
function layout(items: SidebarItemData[]): string[] {
  return dividedIds(drawnItems(items));
}

/** The flyout entries of a group with the given children. */
function flyout(children: SidebarItemData[]): SidebarItemData[] {
  const drawn = drawnItems([group('G', children)]);
  return flyoutEntries((drawn[0] as { children: SidebarItemData[] }).children);
}


describe('sidebar sections', () => {
  it('leaves out a section whose only item is hidden, and the divider in front of it', () => {
    expect(layout([section('A', [nav('a')]), section('B', [nav('b', true)])])).toEqual(['A']);
  });

  it('draws exactly one divider between two sections that show something', () => {
    expect(layout([section('A', [nav('a')]), section('B', [nav('b')])])).toEqual(['A', '|', 'B']);
  });

  it('draws one divider where an empty section stood between two others', () => {
    expect(layout([section('A', [nav('a')]), section('E', [nav('e', true)]), section('B', [nav('b')])]))
      .toEqual(['A', '|', 'B']);
  });

  it('drops the heading together with a section that shows nothing', () => {
    const drawn = drawnItems([nav('x'), section('S', [nav('s', true)], 'Heading')]);
    expect(drawn.map(item => item.id)).toEqual(['x']);
  });

  it('keeps a labelled section that shows something, with its heading', () => {
    const drawn = drawnItems([section('S', [nav('s'), nav('t', true)], 'Heading')]);
    expect(drawn).toEqual([section('S', [nav('s')], 'Heading')]);
  });

  it('divides the sections within a group', () => {
    const drawn = drawnItems([group('G', [section('A', [nav('a')]), section('B', [nav('b')]), section('C', [])])]);
    expect(drawn).toHaveLength(1);
    expect(layout((drawn[0] as { children: SidebarItemData[] }).children)).toEqual(['A', '|', 'B']);
  });

  it('divides a plain item from the section next to it, on either side', () => {
    expect(layout([nav('x'), section('S', [nav('s')]), nav('y')])).toEqual(['x', '|', 'S', '|', 'y']);
  });

  it('draws no divider between plain items, nor at the start or end of a list', () => {
    expect(layout([section('E', [nav('e', true)]), nav('x'), nav('y'), section('F', [])])).toEqual(['x', 'y']);
  });

  it('keeps a section whose only item is a group with nothing drawn in it', () => {
    expect(layout([nav('x'), section('S', [group('G', [nav('g', true)])])])).toEqual(['x', '|', 'S']);
  });

  it('leaves out a section that is itself hidden', () => {
    const hidden: SidebarItemData = { ...section('S', [nav('s')]), hidden: true };
    expect(layout([nav('x'), hidden])).toEqual(['x']);
  });

  it('leaves out a section holding only sections that show nothing', () => {
    expect(layout([nav('x'), section('S', [section('T', [nav('t', true)])])])).toEqual(['x']);
  });
});

describe('sidebar group flyout', () => {
  it('shows a nested group under its heading, with a divider in front of it', () => {
    const entries = flyout([nav('x'), group('N', [nav('n1'), nav('n2', true)])]);
    expect(dividedIds(entries)).toEqual(['x', '|', 'N']);
    expect(entries[1]).toEqual(section('N', [nav('n1')], 'N'));
  });

  it('leaves out a section whose only child is a nested group with nothing drawn in it', () => {
    expect(dividedIds(flyout([nav('x'), section('S', [group('N', [nav('n', true)])], 'Heading')])))
      .toEqual(['x']);
  });

  it('shows a group nested in a section within the section', () => {
    const entries = flyout([section('S', [group('N', [nav('n')])])]);
    expect(entries).toEqual([section('S', [section('N', [nav('n')], 'N')])]);
  });
});
