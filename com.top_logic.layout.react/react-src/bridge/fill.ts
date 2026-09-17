// The fill contract: which controls take the height their container offers, and how that request
// propagates up through nested containers.
//
// A control that fills carries FILL_CLASS on its root element, which the layout CSS turns into
// `flex: 1; min-height: 0`. That alone bounds a control only as long as its container has a
// definite height itself, so a container hosting a filling child has to fill in turn - up to the
// first box that is bounded anyway (the viewport-high mount point, a tab-content region, a
// scrolling panel body). The chain is therefore not a property of a single control but of the path
// through the control tree, which is why it is decided here rather than in a CSS selector
// enumerating the child types allowed to grow.

import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react';

/**
 * CSS class of a control that fills the space its flex container offers, instead of growing with
 * its content.
 */
export const FILL_CLASS = 'tlFill';

/**
 * The container a control reports its fill request to.
 *
 * Provided by every container taking part in the contract (see {@link useFillHost}) and reached by
 * its children through {@link useFill}.
 */
export interface FillHost {

  /**
   * Reports whether the child holding the given token fills.
   *
   * The token identifies the reporting child for the lifetime of its component, so that a
   * container with several children fills while any one of them does.
   */
  setChildFill(token: object, fills: boolean): void;
}

/**
 * The innermost enclosing {@link FillHost}, or `null` at a barrier (see {@link FillBarrier}).
 */
const FillContext = createContext<FillHost | null>(null);

/**
 * Reports the given fill decision to the enclosing container while this component is mounted.
 */
function useFillReport(fills: boolean): void {
  const host = useContext(FillContext);
  const token = useMemo(() => ({}), []);
  useEffect(() => {
    if (host === null) {
      return undefined;
    }
    host.setChildFill(token, fills);
    return () => host.setChildFill(token, false);
  }, [host, fills, token]);
}

/**
 * Takes part in the fill contract with a fixed decision: reports it to the enclosing container and
 * returns the CSS class for this control's root element.
 *
 * For a control that either fills or does not by its own configuration - a `<panel fill="true">`,
 * a control that always spans its container - rather than depending on what it contains. A
 * container whose fill follows its children uses {@link useFillHost} instead.
 *
 * @param fills
 *        Whether this control fills the space its container offers.
 * @returns {@link FILL_CLASS} when filling, the empty string otherwise.
 */
export function useFill(fills: boolean): string {
  useFillReport(fills);
  return fills ? FILL_CLASS : '';
}

/**
 * Takes part in the fill contract as a container: fills while it always does or while one of its
 * children reports filling, and reports that on to its own container.
 *
 * The returned {@link FillHost} has to be provided to the children whose request should propagate
 * (see {@link FillProvider}); children behind a {@link FillBarrier} report to no container at all.
 *
 * @param alwaysFills
 *        Whether this container spans its container's height regardless of its content (a split
 *        panel, whose panes are sized proportionally; a tab bar, whose strip stays pinned while
 *        the tab content scrolls).
 * @returns The CSS class for this container's root element, and the host to provide to its
 *          children.
 */
export function useFillHost(alwaysFills = false): [string, FillHost] {
  const fillingChildren = useRef<Set<object> | null>(null);
  const [childFills, setChildFills] = useState(false);

  const host = useMemo<FillHost>(() => ({
    setChildFill(token: object, fills: boolean) {
      let filling = fillingChildren.current;
      if (filling === null) {
        filling = new Set<object>();
        fillingChildren.current = filling;
      }
      if (fills) {
        filling.add(token);
      } else {
        filling.delete(token);
      }
      setChildFills(filling.size > 0);
    },
  }), []);

  const fills = alwaysFills || childFills;
  useFillReport(fills);
  return [fills ? FILL_CLASS : '', host];
}

/**
 * Provides the given {@link FillHost} to the children rendered inside, so that a filling child
 * makes its container fill.
 */
export const FillProvider: React.FC<{ host: FillHost; children?: React.ReactNode }> =
  ({ host, children }) => React.createElement(FillContext.Provider, { value: host }, children);

/**
 * Ends the fill chain for the children rendered inside: their fill request reaches no container,
 * so it neither grows this control nor leaks to one further out.
 *
 * For a region that is bounded on its own and scrolls what does not fit (a panel body, a tab
 * content area) and for a surface that is laid out apart from the page (a dialog, a window, a
 * drawer): a filling control inside it resolves its height against that region, and the page
 * behind must not react to it.
 */
export const FillBarrier: React.FC<{ children?: React.ReactNode }> =
  ({ children }) => React.createElement(FillContext.Provider, { value: null }, children);
