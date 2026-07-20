import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A link that scrolls the browser to the `TLAnchor` marking a given model object.
 *
 * Hidden while `target` is null. Clicking is a purely client-side viewport action: it finds the
 * element carrying the matching `data-tl-anchor` attribute and scrolls it into view.
 *
 * State:
 * - target: string | null (the anchor id to scroll to; null hides the link)
 * - label: string (link text)
 */
const TLScrollLink: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const target = state.target as string | null;
  const label = (state.label as string | null) ?? '';

  if (target == null) {
    return <span id={controlId} className="tlScrollLink tlScrollLink--empty" />;
  }

  const onClick = (event: React.MouseEvent) => {
    event.preventDefault();
    const node = document.querySelector('[data-tl-anchor="' + CSS.escape(target) + '"]');
    if (node) {
      node.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  };

  return (
    <a id={controlId} className="tlScrollLink" href="#" onClick={onClick}>
      {label}
    </a>
  );
};

export default TLScrollLink;
