import React from 'react';
import { useI18N } from './i18n';
import { ThemeIcon } from './ThemeIcon';
import { ICON_SCROLL_END, ICON_SCROLL_START } from './icons';
import { tooltipProps } from './tooltip-host';
import { EDGE_END, EDGE_START, useScrollStrip } from './scroll-strip';
import type { StripEdge } from './scroll-strip';

/** I18N key of the label of the button scrolling to the tabs before the visible ones. */
export const SCROLL_START_KEY = 'js.tabs.scrollStart';

/** I18N key of the label of the button scrolling to the tabs after the visible ones. */
export const SCROLL_END_KEY = 'js.tabs.scrollEnd';

/** Texts of the scroll buttons; the server translates them (I18NConstants). */
const I18N_KEYS = {
  [SCROLL_START_KEY]: 'Show previous tabs',
  [SCROLL_END_KEY]: 'Show next tabs',
};

/** The classes of a scroll button: a small ghost icon button of the design system, marked as such. */
const SCROLL_BUTTON_CLASS = 'tl-button tl-button--icon tl-button--sm tl-button--ghost tl-tabs__scroll';

/** The selected tab inside the tab list. */
function selectedTab(list: HTMLElement): Element | null {
  return list.querySelector('[role="tab"][aria-selected="true"]');
}

/** Props of {@link TabStrip}. */
export interface TabStripProps {
  /** The ID of the selected tab; each change scrolls the selected tab into view inside the strip. */
  selected: string;

  /** The key handler of the tab list, see {@link useTabList}. */
  onKeyDown: (e: React.KeyboardEvent) => void;

  /** The accessible name of the tab list. */
  'aria-label'?: string;

  /** The tab buttons. */
  children: React.ReactNode;
}

/** Keeps the focus where it is: a scroll button is a pointer affordance, never a focus target. */
function keepFocus(e: React.MouseEvent): void {
  e.preventDefault();
}

/**
 * The strip of a tab bar (tl-tabs__strip): the tab list between two scroll buttons.
 *
 * <p>The tab list scrolls horizontally by itself when the tabs are wider than the strip. While they
 * are, a scroll button stands outside the list at each end and keeps its space; a button with no
 * tabs hidden in its direction is disabled, which the design system draws invisible
 * ({@link useScrollStrip}). Without overflow both buttons are hidden. The selected tab is kept in
 * view inside the list only.</p>
 */
export function TabStrip({ selected, onKeyDown, 'aria-label': ariaLabel, children }: TabStripProps) {
  const i18n = useI18N(I18N_KEYS);
  const strip = useScrollStrip({ revealKey: selected, target: selectedTab });

  const button = (edge: StripEdge, canScroll: boolean, labelKey: string, icon: string) => (
    <button
      type="button"
      className={SCROLL_BUTTON_CLASS}
      data-tl-edge={edge}
      tabIndex={-1}
      aria-label={i18n[labelKey]}
      {...tooltipProps(i18n[labelKey])}
      hidden={!strip.overflows}
      disabled={!canScroll}
      onMouseDown={keepFocus}
      onClick={() => strip.scrollPage(edge)}
    >
      <ThemeIcon encoded={icon} className="tl-icon-sm" />
    </button>
  );

  return (
    <div className="tl-tabs__strip">
      {button(EDGE_START, strip.canScrollStart, SCROLL_START_KEY, ICON_SCROLL_START)}
      <div ref={strip.listRef} className="tl-tabs__list" role="tablist" aria-label={ariaLabel} onKeyDown={onKeyDown}>
        {children}
      </div>
      {button(EDGE_END, strip.canScrollEnd, SCROLL_END_KEY, ICON_SCROLL_END)}
    </div>
  );
}
