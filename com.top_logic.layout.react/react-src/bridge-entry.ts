// Bridge API
export { register, getComponent } from './bridge/registry';
export { connect, subscribe, unsubscribe } from './bridge/sse-client';
export {
  mount,
  mountField,
  unmount,
  discoverAndMount,
  isMountedControl,
  useTLState,
  useTLCommand,
  useTLUpload,
  useTLDataUrl,
  useTLFieldValue,
  useTLSubmitOnEnter,
  createChildContext,
  TLControlContext,
  KeyboardScopeProvider,
  useKeyboardBinding,
  useStandaloneKeyboardScope,
  useFocusTrap,
} from './bridge/tl-react-bridge';
export { ANCHORED_OVERLAY_ATTR, anchoredOverlayProps } from './bridge/focus-trap';
export { CMD_SUBMIT, CMD_VALUE_CHANGED } from './bridge/command-channel';
export { writeDragPayload, readDragPayload, dragTypeAccepted, dropPositionAt } from './bridge/drag-drop';
export type { TLDragPayload, TLDropPosition } from './bridge/drag-drop';
export { startPointerDrag, DRAG_SHIELD_CLASS } from './bridge/pointer-drag';
export type { PointerDragOptions } from './bridge/pointer-drag';
export { useCloseOnOutsidePress, pressClosedSurface } from './bridge/outside-press';
export type { InsideRef } from './bridge/outside-press';
export { useListReorder } from './bridge/list-reorder';
export type {
  ListReorder,
  ListReorderOptions,
  ReorderAxis,
  ReorderContainerProps,
  ReorderDropTarget,
  ReorderHandleProps,
  ReorderItemProps,
  ReorderItemState,
  ReorderSide,
} from './bridge/list-reorder';
export type { TLCellProps } from './bridge/types';
export { useI18N } from './bridge/i18n';
export { scrollToAnchor } from './bridge/scroll';
export { FILL_CLASS, useFill, useFillHost, FillProvider, FillBarrier } from './bridge/fill';
export type { FillHost } from './bridge/fill';
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
// (e.g. TLReact.mount(...) from ReactControl) and GWT-compiled code
// (e.g. ReactBridge.subscribe()) can call them.
import { mount, mountField, discoverAndMount } from './bridge/tl-react-bridge';
import { subscribe as sseSubscribe, unsubscribe as sseUnsubscribe } from './bridge/sse-client';
import { scrollToAnchor } from './bridge/scroll';
(window as any).TLReact = {
  mount, mountField, discoverAndMount, subscribe: sseSubscribe, unsubscribe: sseUnsubscribe, scrollToAnchor,
};

// Initialize window self-close notification for multi-window support.
import { initSelfCloseNotification } from './bridge/window-manager';
initSelfCloseNotification();

// Initialize client-side route synchronization (pushState/popstate handling).
import { initRouteSync } from './bridge/route-sync';
initRouteSync();

// Initialize per-window tooltip host (delegate handler + popover portal root).
import { initTooltipHost } from './bridge/tooltip-host';
if (document.readyState === 'loading') {
  window.addEventListener('DOMContentLoaded', () => initTooltipHost(), { once: true });
} else {
  initTooltipHost();
}

// Install the single document-level keyboard-gesture dispatcher.
import { initKeyboardDispatcher } from './bridge/keyboard-dispatcher';
initKeyboardDispatcher();

// Initialize the "select view" picker (cross-window pick mode for the View Designer).
import { initElementPicker } from './bridge/element-picker';
initElementPicker();

// Install the single document-level focus-trap listener (confines focus to modal surfaces).
import { initFocusTrap } from './bridge/focus-trap';
initFocusTrap();

// Install the document listener that marks the start of a press gesture, ahead of the listeners
// with which the open surfaces close themselves on an outside press.
import { initOutsidePress } from './bridge/outside-press';
initOutsidePress();
