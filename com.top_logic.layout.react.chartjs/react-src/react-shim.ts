// Shim that re-exports React from tl-react-bridge so that third-party libraries
// (react-chartjs-2, chartjs-plugin-zoom) that `import React from 'react'` get the
// shared React instance instead of bundling their own copy.
import { React, ReactDOM } from 'tl-react-bridge';
export default React;
export { ReactDOM };
// Re-export all named hooks so `import { useRef } from 'react'` works.
export const {
  useState, useRef, useEffect, useCallback, useMemo,
  forwardRef, createRef, createElement, createContext,
  useContext, useReducer, useImperativeHandle, useLayoutEffect,
  memo, Fragment, Children, isValidElement, cloneElement
} = React;
