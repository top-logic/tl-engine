// Shim for react/jsx-runtime that delegates to the shared React instance.
import { React } from 'tl-react-bridge';
export const jsx = React.createElement;
export const jsxs = React.createElement;
export const Fragment = React.Fragment;
