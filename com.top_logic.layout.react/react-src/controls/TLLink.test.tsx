import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// A text shown as a link opening an address, drawn with the part FieldValue.

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLLink from './TLLink';
import FieldValuePart from './form/FieldValuePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'link';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  registerPart('FieldValue', FieldValuePart);
  register('TLLink', TLLink);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Mounts a link with the given state and answers the element the link is drawn as. */
function mountLink(state: Record<string, unknown>): HTMLElement {
  const element = document.createElement('div');
  element.id = CONTROL_ID;
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLLink', state, 'w', '/ctx'));
  return element.querySelector('.tl-field-value')!;
}

describe('link', () => {
  it('opens a web address in a window of its own, showing the address', () => {
    const link = mountLink({ address: 'https://www.top-logic.com/' });

    expect(link.tagName).toBe('A');
    expect(link.getAttribute('href')).toBe('https://www.top-logic.com/');
    expect(link.getAttribute('target')).toBe('_blank');
    expect(link.getAttribute('rel')).toBe('noopener noreferrer');
    expect(link.textContent).toBe('https://www.top-logic.com/');
  });

  it('shows its text instead of the address', () => {
    const link = mountLink({ text: 'TopLogic', address: 'https://www.top-logic.com/' });

    expect(link.textContent).toBe('TopLogic');
    expect(link.getAttribute('href')).toBe('https://www.top-logic.com/');
  });

  it('writes an e-mail to an e-mail address', () => {
    const link = mountLink({ address: 'info@top-logic.com', kind: 'email' });

    expect(link.getAttribute('href')).toBe('mailto:info@top-logic.com');
  });

  it('calls a telephone number, without its spaces', () => {
    const link = mountLink({ address: '+49 721 123', kind: 'tel' });

    expect(link.getAttribute('href')).toBe('tel:+49721123');
    expect(link.textContent).toBe('+49 721 123');
  });

  it('opens its address in the window of the page', () => {
    const link = mountLink({ address: 'https://www.top-logic.com/', sameWindow: true });

    expect(link.getAttribute('target')).toBeNull();
    expect(link.getAttribute('rel')).toBeNull();
  });

  it('shows a text without an address without a link', () => {
    const link = mountLink({ text: 'No site', address: '' });

    expect(link.tagName).toBe('SPAN');
    expect(link.textContent).toBe('No site');
  });

  it('shows an address running script without a link', () => {
    const link = mountLink({ address: 'javascript:alert(1)' });

    expect(link.tagName).toBe('SPAN');
    expect(link.textContent).toBe('javascript:alert(1)');
  });

  it('declares its tooltip', () => {
    const link = mountLink({ address: 'https://www.top-logic.com/', tooltip: 'Open' });

    expect(link.getAttribute('data-tooltip')).toBe('text:Open');
  });
});
