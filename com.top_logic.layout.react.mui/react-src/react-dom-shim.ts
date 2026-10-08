// Shim for react-dom that re-exports the react-dom instance of tl-react-bridge, both as default
// export and by name (Material UI reads e.g. `ReactDOM.createPortal` and `ReactDOM.flushSync` from
// a namespace import).
import { ReactDOM } from 'tl-react-bridge';
export default ReactDOM;
export const {
  createPortal, flushSync, preconnect, prefetchDNS, preinit, preinitModule, preload,
  preloadModule, requestFormReset, unstable_batchedUpdates, useFormState, useFormStatus, version,
} = ReactDOM as typeof ReactDOM & Record<string, any>;
