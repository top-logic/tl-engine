/**
 * The items of the sidebar as the server sends them, and which of them are drawn.
 *
 * The sidebar is divided into sections: a section shows its items as if they stood at its own
 * level, set off from its neighbours by a divider and headed by its label where it has one. Which
 * dividers and headings appear follows from the sections that currently show something.
 */

export interface SidebarItemBase {
  id: string;
  type: string;
  label?: string;
  icon?: string;
  /** Not drawn, together with what it contains; an open navigation item stays open nevertheless. */
  hidden?: boolean;
}

export interface NavItem extends SidebarItemBase {
  type: 'nav';
  badge?: string;
}

export interface CommandItemInfo extends SidebarItemBase {
  type: 'command';
  /** Shown greyed out, not activatable and skipped by keyboard navigation. */
  disabled?: boolean;
  /** Explaining text, the reason for the disabling where a rule gave one. */
  tooltip?: string;
}

export interface GroupItemInfo extends SidebarItemBase {
  type: 'group';
  expanded: boolean;
  children: SidebarItemData[];
}

/** Items shown at the level of the section, under its heading where it has a label; it does not fold. */
export interface SectionItemInfo extends SidebarItemBase {
  type: 'section';
  children: SidebarItemData[];
}

export type SidebarItemData =
  NavItem | CommandItemInfo | GroupItemInfo | SectionItemInfo;

/** The items the given item holds: those of a group or a section, none for any other item. */
export function childrenOf(item: SidebarItemData): SidebarItemData[] {
  return item.type === 'group' || item.type === 'section' ? item.children : [];
}

/**
 * The items to draw: an item marked hidden is left out, within a group or section as well, and so
 * is a section none of whose items is drawn. A group stays even when nothing in it is drawn: its
 * button is an entry of its own.
 */
export function drawnItems(items: SidebarItemData[]): SidebarItemData[] {
  const result: SidebarItemData[] = [];
  for (const item of items) {
    if (item.hidden) continue;
    if (item.type === 'group') {
      result.push({ ...item, children: drawnItems(item.children) });
    } else if (item.type === 'section') {
      const children = drawnItems(item.children);
      if (children.length > 0) {
        result.push({ ...item, children });
      }
    } else {
      result.push(item);
    }
  }
  return result;
}

/**
 * Whether a divider is drawn in front of the entry at the given index of a list of drawn entries
 * (see {@link drawnItems}): between two neighbours of which at least one is a section, never at the
 * start of the list.
 */
export function hasDividerBefore(items: SidebarItemData[], index: number): boolean {
  return index > 0 && (items[index].type === 'section' || items[index - 1].type === 'section');
}

/**
 * The entries of a group's flyout, from the drawn children of the group (see {@link drawnItems}).
 *
 * The flyout is a menu, which has no folding groups: a nested group is shown like a section, under
 * its label and set off by dividers, so its items stay reachable. A section or nested group that is
 * left with nothing in it disappears together with its heading.
 */
export function flyoutEntries(children: SidebarItemData[]): SidebarItemData[] {
  const result: SidebarItemData[] = [];
  for (const child of children) {
    if (child.type === 'group' || child.type === 'section') {
      const entries = flyoutEntries(child.children);
      if (entries.length > 0) {
        result.push({ id: child.id, type: 'section', label: child.label, icon: child.icon, children: entries });
      }
    } else {
      result.push(child);
    }
  }
  return result;
}
