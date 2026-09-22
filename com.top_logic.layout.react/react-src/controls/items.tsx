import { React, TLChild } from 'tl-react-bridge';

/**
 * Rendering of the children of a container that repeats them.
 *
 * A container that displays a sequence of equally built children - a list of cards, a grid of
 * tiles - can wrap each of them in an element of its own, so that a stylesheet reaches the
 * individual item without the item's own control having to know it is repeated. The wrapper
 * carries the position of the item, which is what a staggered entrance animation needs; the
 * engine ships the position, not the animation.
 */

/**
 * CSS custom property on an item wrapper holding the 0-based position of the item within its
 * container.
 *
 * An application stylesheet composes a per-item value from it, e.g.
 * `animation-delay: calc(var(--tl-item-index) * 60ms)`.
 */
export const ITEM_INDEX_PROPERTY = '--tl-item-index';

/**
 * CSS class every item wrapper carries, next to the class the container is configured with.
 *
 * The layout stylesheet uses it to keep the wrapper transparent for the layout of the container:
 * the wrapper passes the space it is given on to the item, and it fills while the item fills.
 */
export const ITEM_CLASS = 'tlItem';

/**
 * Renders the children of a container, each wrapped in an element carrying the given class and
 * {@link ITEM_INDEX_PROPERTY}.
 *
 * @param children
 *        The child descriptors to render, in display order.
 * @param itemClass
 *        The CSS class of the wrapper around each child, or `null` to render the children
 *        directly, without a wrapper.
 */
export function renderItems(children: unknown[], itemClass: string | null): React.ReactNode {
  if (!itemClass) {
    return children.map((child, i) => <TLChild key={i} control={child} />);
  }

  return children.map((child, i) => (
    <div
      key={i}
      className={`${ITEM_CLASS} ${itemClass}`}
      style={{ [ITEM_INDEX_PROPERTY as string]: i } as React.CSSProperties}
    >
      <TLChild control={child} />
    </div>
  ));
}
