// @vitest-environment jsdom
import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { DisplayClassProvider, useDisplayClass } from './display-class';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A media query list whose match a test switches, as a browser does when the window is resized. */
class FakeQuery extends EventTarget {
  matches = false;

  constructor(readonly media: string) {
    super();
  }

  set(matches: boolean) {
    this.matches = matches;
    this.dispatchEvent(Object.assign(new Event('change'), { matches }));
  }
}

const Probe: React.FC<{ id: string }> = ({ id }) => <span id={id}>{useDisplayClass()}</span>;

describe('useDisplayClass', () => {
  let fake: FakeQuery;
  let container: HTMLDivElement;
  let root: Root;

  beforeEach(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
    fake = new FakeQuery('');
    window.matchMedia = ((media: string) => {
      expect(media).toBe('(max-width: 768px)');
      return fake;
    }) as unknown as typeof window.matchMedia;
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  const text = (id: string) => container.querySelector('#' + id)?.textContent;

  it('gives a component outside the app shell, such as a dialog, the class of the window', () => {
    fake.matches = true;
    act(() => root.render(<Probe id="dialog" />));

    expect(text('dialog')).toBe('compact');
  });

  it('follows the window across the breakpoint', () => {
    act(() => root.render(<Probe id="dialog" />));
    expect(text('dialog')).toBe('regular');

    act(() => fake.set(true));
    expect(text('dialog')).toBe('compact');

    act(() => fake.set(false));
    expect(text('dialog')).toBe('regular');
  });

  it('lets a provider fix the class of the components inside', () => {
    act(() => root.render(
      <>
        <DisplayClassProvider value="compact"><Probe id="inside" /></DisplayClassProvider>
        <Probe id="beside" />
      </>));

    expect(text('inside')).toBe('compact');
    expect(text('beside')).toBe('regular');
  });
});
