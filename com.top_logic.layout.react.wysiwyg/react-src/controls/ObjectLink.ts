/*
 * The link mark of the editor, writing a link to an application object as it is stored.
 *
 * A link to an application object is an anchor with the CSS class {@link TL_OBJECT}. It is followed
 * within the application, not opened in a new window, so it is written without the target and the
 * relation the editor gives every other link; its stored markup stays as it is. Every other link
 * keeps the target and relation the link extension is configured with.
 *
 * In the edited text, a click with Ctrl (Cmd) held follows an object link, see FollowObjectLinks.
 */
import { Extension } from '@tiptap/react';
import Link from '@tiptap/extension-link';
import type { DOMOutputSpec } from '@tiptap/pm/model';
import { Plugin, PluginKey } from '@tiptap/pm/state';
import type { EditorView } from '@tiptap/pm/view';

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

/** Options of {@link FollowObjectLinks}. */
export interface FollowObjectLinksOptions {
  /** Leads to the object the link with the given destination names. */
  follow: (href: string) => void;
}

/** The object link the given event happened on in the given editor element, `null` for none. */
export function objectLinkAt(root: Element, event: Event): HTMLAnchorElement | null {
  const target = event.target;
  const anchor = target instanceof Element ? target.closest('a') : null;
  return anchor !== null && root.contains(anchor) && anchor.classList.contains(TL_OBJECT) ? anchor : null;
}

/** Whether the given event is a press with the modifier following a link in the edited text. */
function withFollowModifier(event: MouseEvent): boolean {
  return event.ctrlKey || event.metaKey;
}

/**
 * Follows an object link in the edited text on a click with Ctrl (Cmd) held. Such a press is the
 * link's alone: the editor neither places the cursor nor selects the paragraph around the link, as
 * it does for a click with Ctrl held elsewhere. A plain click places the cursor, also on a link.
 */
export const FollowObjectLinks = Extension.create<FollowObjectLinksOptions>({
  name: 'followObjectLinks',

  addOptions() {
    return { follow: () => {} };
  },

  addProseMirrorPlugins() {
    const options = this.options;
    const linkPressed = (view: EditorView, event: MouseEvent) =>
      withFollowModifier(event) ? objectLinkAt(view.dom, event) : null;
    return [new Plugin({
      key: new PluginKey('followObjectLinks'),
      props: {
        handleDOMEvents: {
          mousedown: (view, event) => {
            if (linkPressed(view, event) === null) {
              return false;
            }
            event.preventDefault();
            return true;
          },
          click: (view, event) => {
            const anchor = linkPressed(view, event);
            if (anchor === null) {
              return false;
            }
            event.preventDefault();
            options.follow(anchor.getAttribute('href') || '');
            return true;
          },
        },
      },
    })];
  },
});
