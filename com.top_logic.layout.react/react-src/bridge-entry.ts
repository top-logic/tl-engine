// Bridge API
export { register, getComponent } from './bridge/registry';
export { connect, subscribe, unsubscribe } from './bridge/sse-client';
export {
  mount,
  mountField,
  unmount,
  useTLState,
  useTLCommand,
  useTLFieldValue,
} from './bridge/tl-react-bridge';
export type { TLCellProps } from './bridge/types';

// Expose React for downstream bundles that externalize it.
import React from 'react';
import ReactDOM from 'react-dom';
export { React, ReactDOM };

// Expose bridge functions on window so that server-generated inline scripts
// (e.g. TLReact.mount(...) from ReactControl) can call them.
import { mount, mountField } from './bridge/tl-react-bridge';
(window as any).TLReact = { mount, mountField };
