/*
 * The link mark of the editor, writing a link to an application object as it is stored.
 *
 * A link to an application object is an anchor with the CSS class {@link TL_OBJECT}. It is followed
 * within the application, not opened in a new window, so it is written without the target and the
 * relation the editor gives every other link; its stored markup stays as it is. Every other link
 * keeps the target and relation the link extension is configured with.
 */
import Link from '@tiptap/extension-link';
import type { DOMOutputSpec } from '@tiptap/pm/model';

/** CSS class marking an anchor as a link to an application object. Must match {@code TLObjectLinkUtil#TL_OBJECT}. */
export const TL_OBJECT = 'tlObject';

/** The attributes of a link that an object link is written without. */
const BROWSER_ATTRIBUTES = ['target', 'rel'];

/** Whether the given value of a class attribute marks an object link. */
export function isObjectLinkClass(value: unknown): boolean {
  return typeof value === 'string' && value.split(/\s+/).includes(TL_OBJECT);
}

/** The link mark writing an object link without target and relation, see the description of this file. */
export const ObjectLink = Link.extend({
  renderHTML(props) {
    const spec = this.parent?.(props) as DOMOutputSpec;
    if (!Array.isArray(spec) || !isObjectLinkClass(props.HTMLAttributes.class)) {
      return spec;
    }
    const [tag, attributes, ...content] = spec as [string, Record<string, unknown>, ...unknown[]];
    const kept = { ...attributes };
    for (const name of BROWSER_ATTRIBUTES) {
      delete kept[name];
    }
    return [tag, kept, ...content] as DOMOutputSpec;
  },
});
