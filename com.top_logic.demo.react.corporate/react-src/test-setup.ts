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
