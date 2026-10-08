// Bridge API
export { register, replace, getComponent } from './bridge/registry';
export { registerRootWrapper, DEFAULT_ROOT_WRAPPER_ORDER } from './bridge/root-wrapper';
export type { RootWrapper, RootWrapperOptions } from './bridge/root-wrapper';
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
  VALUE_DEBOUNCE_MS,
} from './bridge/tl-react-bridge';
export { ANCHORED_OVERLAY_ATTR, anchoredOverlayProps, firstFocusable } from './bridge/focus-trap';
export { TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED, TOOLTIP_WHEN_CLIPPED, tooltipProps } from './bridge/tooltip-host';
export { CMD_SUBMIT, CMD_VALUE_CHANGED } from './bridge/command-channel';
export { pushLocalStep } from './bridge/route-sync';
export { FieldLabelContext, fieldLabel, fieldInputId, useFieldLabelProps, focusFieldInput } from './bridge/field-label';
export type { FieldLabel, FieldLabelProps } from './bridge/field-label';
export { FormLayoutContext, useFormLayout } from './bridge/form-layout';
export type { FormLayout } from './bridge/form-layout';
export { ButtonDefaults, useButtonDefaults, menuItemProps } from './bridge/button-defaults';
export type { ButtonAppearance, ButtonDefaultsValue } from './bridge/button-defaults';
export { writeDragPayload, runningDrag, onDragEnd, readDragPayload, dragKindAccepted, flatZoneSplit, treeZoneSplit, dropZoneAt, DROP_MODE_ORDERED, DROP_MODE_ONTO, DROP_MODE_CONTROL } from './bridge/drag-drop';
export type { TLDragPayload, TLDropZone, TLDropMarker, TLZoneSplit, TLRunningDrag, TLDragStart } from './bridge/drag-drop';
export { startPointerDrag, DRAG_SHIELD_CLASS } from './bridge/pointer-drag';
export { INTERACTIVE_SELECTOR, isInteractiveWithin } from './bridge/interactive';
export { ATTR_LONG_PRESS, LONG_PRESS_EVENT } from './bridge/touch-drag';
export type { PointerDragOptions } from './bridge/pointer-drag';
export { useCloseOnOutsidePress, pressClosedSurface } from './bridge/outside-press';
export type { InsideRef } from './bridge/outside-press';
export { usePopover } from './bridge/popover';
export type { PopoverAnchor, PopoverOptions } from './bridge/popover';
// A popover surface needs both its own ref and the hook's setFloating on one element; controls
// must not import floating-ui themselves (it would bundle a second React), so the merge comes from here.
export { useMergeRefs } from '@floating-ui/react';
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
// The state contract of the replaceable components, generated from state.proto: one message per
// component (e.g. ButtonStateJson for TLButton), read with useTLState<Partial<ButtonStateJson>>().
export type {
  ControlStateJson,
  FieldStateJson,
  TypingFieldStateJson,
  ChildControlJson,
  ButtonStateJson,
  ToggleButtonStateJson,
  CheckboxStateJson,
  TextInputStateJson,
  PasswordInputStateJson,
  NumberInputStateJson,
  DatePickerStateJson,
  SelectStateJson,
  DropdownSelectStateJson,
  TabBarStateJson,
  AccordionStateJson,
  WindowStateJson,
  DialogStateJson,
  MenuStateJson,
  SnackbarStateJson,
  AlertStateJson,
  FormFieldStateJson,
  TextStateJson,
  CardStateJson,
  AppBarStateJson,
  BreadcrumbStateJson,
  ProgressStateJson,
  SliderStateJson,
} from './state/control-state';
export { useI18N } from './bridge/i18n';
export { scrollToAnchor } from './bridge/scroll';
export { lockMode, THEME_CLIENT_API, LOCK_MODE_FUNCTION } from './bridge/theme-mode';
export type { ThemeMode } from './bridge/theme-mode';
export { rootClassName } from './bridge/css';
export { useKeyedTransition, TRANSITION_FALLBACK_MS } from './bridge/transition';
export type { KeyedTransitionOptions } from './bridge/transition';
export { FILL_CLASS, useFill, useFillHost, FillProvider, FillBarrier } from './bridge/fill';
export type { FillHost } from './bridge/fill';
export { default as TLChild } from './bridge/TLChild';
export { ThemeIcon } from './bridge/ThemeIcon';
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

// Re-export the react-dom entry points that controls legitimately need. Without these, a control
// has no rule-conforming way to create a portal and is forced to `import { createPortal } from
// 'react-dom'` -- which drags a second copy of React *and* react-dom into tl-react-controls.js,
// exactly what the note above forbids.
export const createPortal = ReactDOM.createPortal;
export const flushSync = ReactDOM.flushSync;

// Re-export the automatic JSX runtime of the same React instance. A module that bundles a library
// compiled with the automatic runtime (e.g. Material UI, whose prebuilt code calls
// `jsx(type, props, key)` from 'react/jsx-runtime') aliases 'react/jsx-runtime' to a shim
// re-exporting these:
//
//   export { jsx, jsxs, Fragment } from 'tl-react-bridge';
//
// `React.createElement` is no substitute: it reads its third argument as children, so a keyed
// `jsx` call would replace the children passed in the props.
export { jsx, jsxs, Fragment } from 'react/jsx-runtime';

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
