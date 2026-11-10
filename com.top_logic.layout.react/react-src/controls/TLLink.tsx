import { React, useTLState, rootClassName, usePart, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps, LinkStateJson } from 'tl-react-bridge';
import { linkHref } from './link-href';

/** The kind of an address without one in the state: a web address. */
const DEFAULT_KIND = 'url';

/**
 * A text shown as a link opening an address: a web page, an e-mail to write, a number to call. The
 * link is the part `FieldValue`, so that a theme or a component library draws it as any read-only
 * value pointing somewhere.
 *
 * The state is described by LinkStateJson: the shown text (the address where it is empty), the
 * address, its kind turning it into the link a read-only text field of that kind opens, whether the
 * link opens in the window of the page, and a plain-text tooltip. Without an address the text is
 * shown without a link. The component sends no commands.
 */
const TLLink: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<LinkStateJson>>();
  const FieldValue = usePart('FieldValue');
  const address = state.address ?? '';
  const text = state.text || address;
  const href = linkHref(state.kind ?? DEFAULT_KIND, address);
  return (
    <FieldValue id={controlId} className={rootClassName(state) || undefined} text={text}
      href={href ?? undefined} sameWindow={state.sameWindow === true} {...tooltipProps(state.tooltip)} />
  );
};

export default TLLink;
