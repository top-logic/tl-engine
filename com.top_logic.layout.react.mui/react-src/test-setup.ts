// Setup of the tests of this module (see vitest.config.ts).
//
// Loading tl-react-bridge starts its document observer, which auto-mounts controls added to the
// page. jsdom still delivers the observer the removals of its own teardown, after the DOM globals
// are gone, and the observer then fails. The observers are disconnected before that.

import { afterAll } from 'vitest';

const observers: MutationObserver[] = [];
const NativeMutationObserver = globalThis.MutationObserver;

globalThis.MutationObserver = class extends NativeMutationObserver {
  constructor(callback: MutationCallback) {
    super(callback);
    observers.push(this);
  }
};

afterAll(() => {
  observers.forEach(observer => observer.disconnect());
});

// jsdom has no CSS.escape, which user-event uses to find the radio button an arrow key moves to.
const globalCss = (globalThis as { CSS?: { escape?: (value: string) => string } });
globalCss.CSS ??= {};
globalCss.CSS.escape ??= (value: string) => value.replace(/[^a-zA-Z0-9_-]/g, ch => '\\' + ch);
