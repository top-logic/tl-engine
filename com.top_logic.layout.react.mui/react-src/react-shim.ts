// Shim that re-exports React from tl-react-bridge, so that Material UI and emotion, which import
// React from 'react' like any third-party library, get the shared React instance instead of
// bundling a copy of their own.
//
// The libraries import React as a namespace (`import * as React from 'react'`) and read the API
// from it, some of it computed (emotion looks up `useInsertionEffect` by a composed name). The shim
// therefore re-exports the complete public API of React 19 by name, not just the hooks of one
// library.
import { React } from 'tl-react-bridge';
export default React;
export const {
  Activity, Children, Component, Fragment, Profiler, PureComponent, StrictMode, Suspense,
  addTransitionType, cache, cacheSignal, cloneElement, createContext, createElement, createRef,
  forwardRef, isValidElement, lazy, memo, startTransition, use, useActionState, useCallback,
  useContext, useDebugValue, useDeferredValue, useEffect, useEffectEvent, useId,
  useImperativeHandle, useInsertionEffect, useLayoutEffect, useMemo, useOptimistic, useReducer,
  useRef, useState, useSyncExternalStore, useTransition, version,
} = React as typeof React & Record<string, any>;
