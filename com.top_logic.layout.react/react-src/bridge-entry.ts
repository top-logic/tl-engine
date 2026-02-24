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
