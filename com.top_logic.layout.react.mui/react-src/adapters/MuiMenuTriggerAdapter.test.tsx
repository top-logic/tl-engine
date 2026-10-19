// The menu triggers of TLToolbar and TLMenuRegion, drawn by the MUI adapter of the part MenuTrigger:
// the controls keep the popover, the focus and the commands, the adapter draws the trigger.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLToolbar from '../../../com.top_logic.layout.react/react-src/controls/TLToolbar';
import TLMenuRegion from '../../../com.top_logic.layout.react/react-src/controls/TLMenuRegion';
import MuiMenuTriggerAdapter from './MuiMenuTriggerAdapter';
import MuiDividerAdapter from './MuiDividerAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

replacePart('MenuTrigger', MuiMenuTriggerAdapter);
// The toolbar draws the lines between its groups and between the sections of its menus.
replacePart('Divider', MuiDividerAdapter);

/** The command of a menu region asking the server to open its menu. */
const CMD_OPEN_MENU = 'openMenu';

/** The argument of {@link CMD_OPEN_MENU} naming the element the menu hangs off. */
const ARG_ANCHOR_ID = 'anchorId';

/** A toolbar with one menu group of two entries. */
function toolbarState(group: Record<string, unknown>) {
  return {
    groups: [{
      name: 'more',
      display: 'menu',
      items: [childControl('i1', 'Kopieren'), childControl('i2', 'Einfügen')],
      ...group,
    }],
  };
}

/** The menu of the toolbar's menu group, portaled to the body. */
function dropdown(): HTMLElement {
  return document.body.querySelector('.tl-menu[role="menu"]') as HTMLElement;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('the menu trigger of a toolbar group as MUI button', () => {
  it('shows the label of the group with a chevron, closed', () => {
    mountAdapter(TLToolbar, toolbarState({ label: 'Bearbeiten' }));

    const trigger = screen.getByRole('button', { name: 'Bearbeiten' });
    expect(trigger.classList).toContain('MuiButton-root');
    // The toolbar's ghost look.
    expect(trigger.classList).toContain('MuiButton-text');
    expect(trigger.classList).not.toContain('tl-button');
    expect(trigger.querySelector('.MuiButton-endIcon i.fa-chevron-down')).not.toBeNull();
    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    expect(dropdown().hidden).toBe(true);
  });

  it('opens the menu on a click and closes it on the next, the trigger being its anchor', async () => {
    mountAdapter(TLToolbar, toolbarState({ label: 'Bearbeiten' }));
    const trigger = screen.getByRole('button', { name: 'Bearbeiten' });

    await userEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
    expect(dropdown().hidden).toBe(false);
    expect(dropdown().textContent).toContain('Kopieren');

    // A press on the trigger is not a press outside the menu (the trigger is the ref the toolbar
    // holds): the click toggles the menu closed instead of the press closing and the click reopening it.
    await userEvent.click(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    expect(dropdown().hidden).toBe(true);
  });

  it('closes the menu on a press outside', async () => {
    mountAdapter(TLToolbar, toolbarState({ label: 'Bearbeiten' }));
    const trigger = screen.getByRole('button', { name: 'Bearbeiten' });

    await userEvent.click(trigger);
    await userEvent.click(document.body);

    expect(trigger.getAttribute('aria-expanded')).toBe('false');
  });

  it('shows an icon-only trigger as MUI IconButton named by the label of the group', () => {
    mountAdapter(TLToolbar, toolbarState({ label: 'Mehr', icon: 'css:bi bi-three-dots' }));

    const trigger = screen.getByRole('button', { name: 'Mehr' });
    expect(trigger.classList).toContain('MuiIconButton-root');
    expect(trigger.querySelector('i.bi-three-dots')).not.toBeNull();
    expect(trigger.textContent).toBe('');
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
  });

  it('draws no trigger for a group of a single entry', () => {
    mountAdapter(TLToolbar, toolbarState({ label: 'Bearbeiten', items: [childControl('i1', 'Kopieren')] }));

    expect(screen.queryByRole('button', { name: 'Bearbeiten' })).toBeNull();
    expect(screen.getByText('Kopieren')).toBeTruthy();
  });
});

describe('the kind of surface a MUI menu trigger opens', () => {
  it('names a menu unless told otherwise, and the given kind otherwise', () => {
    render(<>
      <MuiMenuTriggerAdapter appearance="ghost" expanded={false} label="Menü" />
      <MuiMenuTriggerAdapter appearance="ghost" expanded={true} haspopup="dialog" label="Details" />
    </>);

    expect(screen.getByRole('button', { name: 'Menü' }).getAttribute('aria-haspopup')).toBe('menu');
    const dialogTrigger = screen.getByRole('button', { name: 'Details' });
    expect(dialogTrigger.getAttribute('aria-haspopup')).toBe('dialog');
    expect(dialogTrigger.getAttribute('aria-expanded')).toBe('true');
  });
});

describe('the drop-down trigger of a menu region as MUI button', () => {
  it('wraps the child control and reports whether its menu is open', () => {
    mountAdapter(TLMenuRegion, {
      trigger: 'click', menuOpen: true, cssClass: 'my-class', child: childControl('c2', 'Ansicht'),
    });

    const trigger = screen.getByRole('button', { name: 'Ansicht' });
    expect(trigger.id).toBe(CONTROL_ID);
    expect(trigger.classList).toContain('MuiButton-root');
    expect(trigger.classList).toContain('tl-menu-region');
    expect(trigger.classList).toContain('my-class');
    expect(trigger.getAttribute('data-tl-trigger')).toBe('click');
    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
    expect(trigger.querySelector('#c2')?.textContent).toBe('Ansicht');
    expect(trigger.querySelector('.MuiButton-endIcon')).toBeNull();
  });

  it('asks the server to open the menu at the trigger when it is clicked', async () => {
    const sent = mountAdapter(TLMenuRegion, { trigger: 'click', menuOpen: false, child: childControl('c2', 'Ansicht') });

    const trigger = screen.getByRole('button', { name: 'Ansicht' });
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    await userEvent.click(trigger);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_OPEN_MENU, { [ARG_ANCHOR_ID]: CONTROL_ID }]]);
  });

  it('is hidden by its style while hidden, keeping its child mounted', () => {
    mountAdapter(TLMenuRegion, { trigger: 'click', hidden: true, child: childControl('c2', 'Ansicht') });

    const region = document.getElementById(CONTROL_ID)!;
    expect(region.style.display).toBe('none');
    expect(region.querySelector('#c2')).not.toBeNull();
  });

  it('stays a pass-through region without a trigger for the context menu gesture', () => {
    mountAdapter(TLMenuRegion, { trigger: 'contextmenu', child: childControl('c2', 'Ansicht') });

    expect(screen.queryByRole('button')).toBeNull();
    expect(document.getElementById(CONTROL_ID)?.getAttribute('data-tl-trigger')).toBe('contextmenu');
  });
});

describe('the MUI menu trigger part', () => {
  it('hands the drawn element to the ref, the anchor of the menu', () => {
    const ref = React.createRef<HTMLButtonElement>();

    render(<MuiMenuTriggerAdapter ref={ref} appearance="primary" expanded={false} label="Mehr" />);

    const trigger = screen.getByRole('button', { name: 'Mehr' });
    expect(ref.current).toBe(trigger);
    // The look of a primary button.
    expect(trigger.classList).toContain('MuiButton-contained');
  });
});
