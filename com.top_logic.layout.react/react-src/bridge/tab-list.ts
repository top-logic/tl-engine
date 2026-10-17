import React from 'react';

/** The attributes of one tab of a tab list, see {@link useTabList}. */
export interface TabProps {
  id: string;
  role: 'tab';
  'aria-selected': boolean;
  'aria-controls': string;
  tabIndex: 0 | -1;
  type: 'button';
  onClick: () => void;
}

/** The attributes of a tab panel, see {@link useTabList}. */
export interface PanelProps {
  id: string;
  role: 'tabpanel';
  'aria-labelledby': string;
  hidden: boolean;
}

/** What {@link useTabList} needs to know about the list. */
export interface TabListOptions {
  /** Prefix of the element IDs: the tabs are `${idPrefix}-tab-${id}`, the panels `${idPrefix}-panel-${id}`. */
  idPrefix: string;

  /** The IDs of the tabs in display order. */
  ids: string[];

  /** The ID of the selected tab; an ID not in the list means no selection. */
  selected: string;

  /** Called when a tab is chosen, by click or by keyboard. */
  onSelect: (id: string) => void;
}

/**
 * The keyboard and the ARIA wiring every tab list shares.
 *
 * <p>Roving tabindex: only the selected tab, or the first while none is selected, is in the tab
 * order. Arrow right and left (cyclic), Home and End move the focus and select the tab reached in
 * one step, through the same {@code onSelect} a click calls. The list puts {@code onListKeyDown} on
 * its {@code role="tablist"} element and spreads {@code tabProps(id)} on each tab button and
 * {@code panelProps(id)} on each panel.</p>
 */
export function useTabList({ idPrefix, ids, selected, onSelect }: TabListOptions) {
  const tabId = (id: string) => `${idPrefix}-tab-${id}`;
  const panelId = (id: string) => `${idPrefix}-panel-${id}`;
  const stop = ids.includes(selected) ? selected : ids[0];

  const onListKeyDown = (e: React.KeyboardEvent) => {
    const origin = (e.target as HTMLElement).closest?.('[role="tab"]');
    let index = ids.findIndex(id => tabId(id) === origin?.id);
    if (index < 0) {
      index = Math.max(0, ids.indexOf(stop));
    }
    let next: number;
    if (e.key === 'ArrowRight') next = index + 1;
    else if (e.key === 'ArrowLeft') next = index - 1;
    else if (e.key === 'Home') next = 0;
    else if (e.key === 'End') next = ids.length - 1;
    else return;
    e.preventDefault();
    if (ids.length === 0) return;
    const id = ids[(next + ids.length) % ids.length];
    (e.currentTarget as HTMLElement).ownerDocument.getElementById(tabId(id))?.focus();
    onSelect(id);
  };

  const tabProps = (id: string, options: { controls?: string } = {}): TabProps => ({
    id: tabId(id),
    role: 'tab',
    'aria-selected': id === selected,
    'aria-controls': options.controls ?? panelId(id),
    tabIndex: id === stop ? 0 : -1,
    type: 'button',
    onClick: () => onSelect(id),
  });

  const panelProps = (id: string): PanelProps => ({
    id: panelId(id),
    role: 'tabpanel',
    'aria-labelledby': tabId(id),
    hidden: id !== selected,
  });

  return { onListKeyDown, tabProps, panelProps };
}
