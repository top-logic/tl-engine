// Bridge API
export { register, getComponent } from './bridge/registry';
export { connect, subscribe, unsubscribe } from './bridge/sse-client';
export {
  mount,
  mountField,
  unmount,
  useTLState,
  useTLCommand,
  useTLUpload,
  useTLFieldValue,
  createChildContext,
  TLControlContext,
} from './bridge/tl-react-bridge';
export type { TLCellProps } from './bridge/types';
export { default as TLChild } from './bridge/TLChild';
export type { ChildDescriptor } from './bridge/TLChild';

// Re-export React so that control bundles use the SAME React instance.
//
// IMPORTANT: Controls in tl-react-controls.js MUST import React from 'tl-react-bridge',
// NOT from 'react' directly.  Importing from 'react' bundles a second copy of React into
// the controls bundle, which causes "useState is null" errors at runtime because hooks
// only work when called against the same React instance that rendered the component tree.
//
// Correct:   import { React, useTLState } from 'tl-react-bridge';
// WRONG:     import React, { useState } from 'react';   // <-- duplicates React!
import React from 'react';
import ReactDOM from 'react-dom';
export { React, ReactDOM };

// Expose bridge functions on window so that server-generated inline scripts
// (e.g. TLReact.mount(...) from ReactControl) can call them.
import { mount, mountField } from './bridge/tl-react-bridge';
(window as any).TLReact = { mount, mountField };
