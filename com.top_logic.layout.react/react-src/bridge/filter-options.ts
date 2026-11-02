/**
 * The one way a list of options is narrowed by a typed term: the select popup and the column
 * selection (and, from wave 5, the icon select) show the options whose label contains the term,
 * ignoring case and surrounding blanks.
 */

/**
 * The options whose label contains the needle.
 *
 * @param options The options in their order.
 * @param needle The typed term; blank or empty keeps every option.
 * @returns The matching options in their order, a new array.
 */
export function filterOptions<T extends { label: string }>(options: readonly T[], needle: string): T[] {
  const term = needle.trim().toLowerCase();
  if (term === '') {
    return options.slice();
  }
  return options.filter((option) => option.label.toLowerCase().includes(term));
}

/**
 * Whether the given option is the one a typed text names: its label equals the text, ignoring case
 * and the blanks around the text. The server matches an entered text against the options the same
 * way (`ReactDropdownSelectControl`, command `enterText`).
 *
 * @param option The option.
 * @param text The typed text.
 */
export function labelMatches(option: { label: string }, text: string): boolean {
  const term = text.trim().toLowerCase();
  return term !== '' && option.label.toLowerCase() === term;
}
