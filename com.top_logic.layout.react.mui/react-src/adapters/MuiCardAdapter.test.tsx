// The wire contract of TLCard, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import type { CardStateJson } from 'tl-react-bridge';
import MuiCardAdapter from './MuiCardAdapter';
import { CONTROL_ID, childControl, mountAdapter } from './wire-test-support';

function mountCard(state: Partial<CardStateJson>) {
  return mountAdapter(MuiCardAdapter, { child: childControl('content', 'Inhalt'), ...state });
}

/** One unit of the MUI spacing, the CSS variable the theme publishes it in. */
const SPACING = 'var(--mui-spacing)';

/** The top padding the MUI styles declare for the given element, without resolving variables. */
function paddingTop(element: Element): string {
  let declared = '';
  for (const sheet of Array.from(document.styleSheets)) {
    for (const rule of Array.from(sheet.cssRules)) {
      if (rule instanceof CSSStyleRule && element.matches(rule.selectorText)) {
        declared = rule.style.getPropertyValue('padding-top') || rule.style.getPropertyValue('padding') || declared;
      }
    }
  }
  return declared;
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLCard as MUI Card', () => {
  it('renders an outlined MUI card with header, actions and content', () => {
    mountCard({
      title: 'Kontakt', cssClass: 'my-card',
      headerActions: [childControl('edit', 'Bearbeiten'), childControl('remove', 'Entfernen')],
    });

    const card = root();
    expect(card.classList).toContain('MuiCard-root');
    expect(card.classList).toContain('MuiPaper-outlined');
    expect(card.classList).toContain('my-card');
    expect(card.querySelector('.MuiCardHeader-title')!.textContent).toBe('Kontakt');
    const action = card.querySelector('.MuiCardHeader-action')!;
    expect(action.querySelector('#edit')!.textContent).toBe('Bearbeiten');
    expect(action.querySelector('#remove')!.textContent).toBe('Entfernen');
    expect(card.querySelector('.MuiCardContent-root #content')!.textContent).toBe('Inhalt');
  });

  it('raises an elevated card by a shadow instead of a border', () => {
    mountCard({ variant: 'elevated' });

    expect(root().classList).toContain('MuiPaper-elevation1');
    expect(root().classList).not.toContain('MuiPaper-outlined');
  });

  it('shows no header without title and actions', () => {
    mountCard({});

    expect(root().querySelector('.MuiCardHeader-root')).toBeNull();
  });

  it('shows the header for actions alone', () => {
    mountCard({ headerActions: [childControl('edit', 'Bearbeiten')] });

    expect(root().querySelector('.MuiCardHeader-root #edit')).not.toBeNull();
  });

  it('takes the padding of its content from the state', () => {
    const expected: [CardStateJson.Padding | undefined, string][] = [
      ['none', '0'], ['compact', SPACING], [undefined, `calc(2 * ${SPACING})`],
    ];
    for (const [padding, declared] of expected) {
      mountCard(padding === undefined ? {} : { padding });
      expect(paddingTop(root().querySelector('.MuiCardContent-root')!)).toBe(declared);
      cleanup();
    }
  });

  it('renders nothing while hidden', () => {
    mountCard({ hidden: true });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
