// The wire contract of TLBreadcrumb, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { BreadcrumbStateJson } from 'tl-react-bridge';
import MuiBreadcrumbAdapter from './MuiBreadcrumbAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

/** Command jumping back to an item of the trail. */
const CMD_NAVIGATE = 'navigate';

/** Argument of {@link CMD_NAVIGATE}: the ID of the item. */
const ARG_ITEM_ID = 'itemId';

/** The name of the trail: the key, as the test stub translates each key to itself. */
const LABEL_TRAIL = 'js.breadcrumb.label';

const ITEMS: BreadcrumbStateJson.Item[] = [
  { id: 'root', label: 'Projekte' },
  { id: 'p1', label: 'Projekt 1' },
  { id: 'm1', label: 'Meilenstein 1' },
];

function mountTrail(state: Partial<BreadcrumbStateJson>) {
  return mountAdapter(MuiBreadcrumbAdapter, { items: ITEMS, ...state });
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLBreadcrumb as MUI Breadcrumbs', () => {
  it('renders MUI breadcrumbs: links to the outer items, the current one as text', async () => {
    mountTrail({ cssClass: 'my-trail' });

    const nav = await screen.findByRole('navigation', { name: LABEL_TRAIL });
    expect(nav.id).toBe(CONTROL_ID);
    expect(nav.classList).toContain('MuiBreadcrumbs-root');
    expect(nav.classList).toContain('my-trail');
    expect(screen.getAllByRole('button').map(button => button.textContent)).toEqual(['Projekte', 'Projekt 1']);
    const current = screen.getByText('Meilenstein 1');
    expect(current.getAttribute('aria-current')).toBe('page');
    expect(current.closest('button')).toBeNull();
  });

  it('sends navigate with the ID of the item clicked', async () => {
    const sent = mountTrail({});

    await userEvent.click(screen.getByRole('button', { name: 'Projekt 1' }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_NAVIGATE, { [ARG_ITEM_ID]: 'p1' }]]);
  });

  it('sends nothing for a click on the current item', async () => {
    const sent = mountTrail({});

    await userEvent.click(screen.getByText('Meilenstein 1'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('renders nothing while hidden', () => {
    mountTrail({ hidden: true });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
