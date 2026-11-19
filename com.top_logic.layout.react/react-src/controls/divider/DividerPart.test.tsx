import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The control TLDivider and the TopLogic drawing of the part Divider it draws.

vi.mock('../../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../../bridge/tl-react-bridge';
import { register, registerPart } from 'tl-react-bridge';
import TLDivider from '../TLDivider';
import DividerPart from './DividerPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'divider';

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  registerPart('Divider', DividerPart);
  register('TLDivider', TLDivider);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Mounts a divider with the given state and answers the element the divider is drawn as. */
function mountDivider(state: Record<string, unknown>): HTMLElement {
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLDivider', state, 'w', '/ctx'));
  return element.querySelector('.tl-divider')!;
}

describe('divider', () => {
  it('draws a plain horizontal line as an hr', () => {
    const divider = mountDivider({ orientation: 'horizontal', labelPosition: 'center', inset: 'none' });

    expect(divider.tagName).toBe('HR');
    expect(divider.id).toBe(CONTROL_ID);
    expect(divider.getAttribute('aria-orientation')).toBeNull();
    expect(divider.className).toBe('tl-divider');
    expect(divider.textContent).toBe('');
  });

  it('draws a line without state as a plain horizontal hr', () => {
    const divider = mountDivider({});

    expect(divider.tagName).toBe('HR');
    expect(divider.className).toBe('tl-divider');
  });

  it('marks a vertical line with its orientation', () => {
    const divider = mountDivider({ orientation: 'vertical' });

    expect(divider.tagName).toBe('HR');
    expect(divider.classList).toContain('tl-divider--vertical');
    expect(divider.getAttribute('aria-orientation')).toBe('vertical');
  });

  it('draws a line with a text as a separator holding the text in the middle', () => {
    const divider = mountDivider({ orientation: 'horizontal', label: 'or', labelPosition: 'center' });

    expect(divider.tagName).toBe('DIV');
    expect(divider.getAttribute('role')).toBe('separator');
    expect(divider.classList).toContain('tl-divider--labeled');
    expect(divider.classList).not.toContain('tl-divider--label-start');
    expect(divider.classList).not.toContain('tl-divider--label-end');
    const label = divider.querySelector('.tl-divider__label')!;
    expect(label.textContent).toBe('or');
    expect(label.classList).toContain('tl-type-label');
  });

  it('places the text at the start of the line', () => {
    const divider = mountDivider({ label: 'Address', labelPosition: 'start' });

    expect(divider.classList).toContain('tl-divider--label-start');
    expect(divider.classList).not.toContain('tl-divider--label-end');
  });

  it('places the text at the end of the line', () => {
    const divider = mountDivider({ label: 'End', labelPosition: 'end' });

    expect(divider.classList).toContain('tl-divider--label-end');
    expect(divider.classList).not.toContain('tl-divider--label-start');
  });

  it('shows no text in a vertical line', () => {
    const divider = mountDivider({ orientation: 'vertical', label: 'ignored' });

    expect(divider.tagName).toBe('HR');
    expect(divider.querySelector('.tl-divider__label')).toBeNull();
    expect(divider.classList).not.toContain('tl-divider--labeled');
  });

  it('shortens an inset line at its start', () => {
    expect(mountDivider({ inset: 'start' }).classList).toContain('tl-divider--inset');
  });

  it('shortens an inset line with a text at its start', () => {
    const divider = mountDivider({ inset: 'start', label: 'Section' });

    expect(divider.classList).toContain('tl-divider--inset');
    expect(divider.classList).toContain('tl-divider--labeled');
  });

  it('carries the configured class of the control', () => {
    const divider = mountDivider({ cssClass: 'my-divider' });

    expect(divider.classList).toContain('tl-divider');
    expect(divider.classList).toContain('my-divider');
  });

  it('renders nothing while hidden', () => {
    expect(mountDivider({ hidden: true })).toBeNull();
  });
});
