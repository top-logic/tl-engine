import { React, useTLState, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A theme icon rendered on its own, as a {@code <span>} around the glyph or picture.
 *
 * State:
 * - image: string - the encoded theme image, e.g. {@code css:bi bi-chevron-right}
 * - tooltip: string - optional text shown on hover, which also names the icon
 * - cssClasses: string - optional additional CSS classes appended to the default {@code tlIcon} class
 *
 * An icon without a tooltip decorates what it sits beside and is hidden from assistive technology;
 * one with a tooltip carries that text as its accessible name.
 */
const TLIcon: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const image = state.image as string | undefined;
  const tooltip = state.tooltip as string | undefined;
  const extra = (state.cssClasses as string) ?? '';
  const className = extra ? `tlIcon ${extra}` : 'tlIcon';

  return (
    <span
      id={controlId}
      className={className}
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
