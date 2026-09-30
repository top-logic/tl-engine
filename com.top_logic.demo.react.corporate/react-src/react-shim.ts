// Shim that re-exports React from tl-react-bridge so that the code of the example component
// library, which imports React from 'react' like any third-party library, gets the shared React
// instance instead of bundling its own copy.
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
