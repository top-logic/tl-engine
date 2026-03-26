import { React, ReactDOM } from 'tl-react-bridge';
export default React;
export { ReactDOM };
export const {
  useState, useRef, useEffect, useCallback, useMemo,
  forwardRef, createRef, createElement, createContext,
  useContext, useReducer, useImperativeHandle, useLayoutEffect,
  memo, Fragment, Children, isValidElement, cloneElement,
  useDebugValue, version,
} = React as any;
