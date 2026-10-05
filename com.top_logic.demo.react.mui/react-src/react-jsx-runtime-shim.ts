// Shim for react/jsx-runtime that creates elements with the shared React instance of
// tl-react-bridge.
//
// The prebuilt code of Material UI calls the automatic JSX runtime: `jsx(type, props, key)`, with
// the children inside `props` and the key as a separate argument. `createElement` takes the key
// inside the props and children as further arguments, so the key is moved into the props and no
// children argument is passed: `createElement` then keeps `props.children` as it is.
import { React } from 'tl-react-bridge';

type ElementType = Parameters<typeof React.createElement>[0];

export function jsx(type: ElementType, props: Record<string, unknown>, key?: React.Key) {
  return React.createElement(type, key === undefined ? props : { ...props, key });
}

export const jsxs = jsx;
export const Fragment = React.Fragment;
