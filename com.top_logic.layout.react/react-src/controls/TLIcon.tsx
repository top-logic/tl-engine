import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

/**
 * A theme icon rendered on its own, in one of the four size steps of the design system
 * (`tl-icon-sm|md|lg|glyph`, base.css). The icon has no stylesheet of its own.
 *
 * State:
 * - image: string - the encoded theme image, e.g. {@code css:bi bi-chevron-right}
 * - size: "sm" | "md" | "lg" | "glyph" - the step by role; sm (beside a label) when absent
 * - tooltip: string - optional text shown on hover, which also names the icon
 * - cssClasses: string - optional additional CSS classes appended to the size class
 *
 * An icon without a tooltip decorates what it sits beside and is hidden from assistive technology;
 * one with a tooltip carries that text as its accessible name.
 */
const SIZES = new Set(['sm', 'md', 'lg', 'glyph']);

const TLIcon: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const image = state.image as string | undefined;
  const tooltip = state.tooltip as string | undefined;
  const size = SIZES.has(state.size as string) ? (state.size as string) : 'sm';
  const extra = (state.cssClasses as string) ?? '';

  return (
    <span
      id={controlId}
      className={rootClassName(state, ['tl-icon-' + size, extra].filter(Boolean).join(' '))}
      data-tooltip={tooltip ? `text:${tooltip}` : undefined}
      role={tooltip ? 'img' : undefined}
      aria-label={tooltip}
      aria-hidden={tooltip ? undefined : true}
    >
      {image && <ThemeIcon encoded={image} />}
    </span>
  );
};

export default TLIcon;
