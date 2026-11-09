/**
 * The address a link to a value of the given kind opens, or `null` where the kind has no link or
 * the value is empty.
 *
 * <p>The kinds are the HTML input types of an address: `url` opens the value itself, `email` writes
 * an e-mail to it (`mailto:`), `tel` calls it (`tel:`). Every other kind has no link.</p>
 */
export const linkHref = (kind: string, value: string): string | null => {
  if (value === '') return null;
  switch (kind) {
    case 'url':
      return value;
    case 'email':
      return 'mailto:' + value;
    case 'tel':
      // A phone number is written with spaces for legibility; the dialer takes the digits.
      return 'tel:' + value.replace(/\s+/g, '');
    default:
      return null;
  }
};
