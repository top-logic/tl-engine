/*
 * Keeps HTML the editor's schema has no node, mark, or attribute for.
 *
 * The editor parses a text into the nodes and marks of its schema and writes the text anew from
 * them. Whatever the schema has no place for would get lost on the way: an element of unknown
 * type, an attribute of a known element. This extension gives all of it a place, so that a text
 * keeps its markup when it is edited in the formatted view:
 *
 * - Every attribute of a known element that the element's own extension does not handle (a class,
 *   a style, a data attribute, an id, ...) is kept with the node or mark and written again.
 * - An element of unknown type stays an element of that type: a block that holds blocks (e.g. a
 *   <div> around paragraphs) or a block that holds text (e.g. a <div> with text), each with
 *   editable content, and an inline element with text (e.g. a <span class> or a <sup>) as a mark
 *   on the text.
 * - What cannot be edited safely or without changing its structure is kept as an opaque piece of
 *   source: an element that would act in the page (a script, an embedded frame, an event handler
 *   attribute, a script address), an element whose content mixes text with blocks, and an empty
 *   inline element. The editor shows its source and writes it back unchanged.
 *
 * Content pasted from the clipboard is parsed without this extension, so that pasting from another
 * application brings formatting the editor offers, not the markup of that application.
 */
import { Extension, Mark, Node, getExtensionField } from '@tiptap/react';
import type { AnyExtension } from '@tiptap/react';
import { DOMParser as PMDOMParser } from '@tiptap/pm/model';
import type { Schema, TagParseRule } from '@tiptap/pm/model';
import { Plugin, PluginKey } from '@tiptap/pm/state';

/** Name of the attribute holding the kept HTML attributes of a node or mark. */
export const HTML_ATTRIBUTES = 'htmlAttributes';

/** Name of the attribute holding the element type of an element of unknown type. */
const TAG = 'tag';

/** Name of the attribute holding the source of an opaque piece of HTML. */
const SOURCE = 'source';

/** Node holding an element of unknown type whose content is blocks. */
export const HTML_BLOCK = 'htmlBlock';

/** Node holding an element of unknown type whose content is text. */
export const HTML_TEXT_BLOCK = 'htmlTextBlock';

/** Mark for an inline element of unknown type around text. */
export const HTML_INLINE = 'htmlInline';

/** Node holding an opaque piece of HTML in place of a block. */
export const HTML_SOURCE_BLOCK = 'htmlSourceBlock';

/** Node holding an opaque piece of HTML within text. */
export const HTML_SOURCE_INLINE = 'htmlSourceInline';

/**
 * Element standing for an opaque piece of HTML in the text the editor writes, replaced by the
 * source it holds in {@link restoreHtmlSources}. It also carries such a piece through the clipboard
 * when it is copied within the editor.
 */
const SOURCE_ELEMENT = 'tl-html-source';

/** Attribute of a {@link SOURCE_ELEMENT} holding the source. */
const SOURCE_ATTRIBUTE = 'data-source';

/** Attribute of a {@link SOURCE_ELEMENT} marking a piece of HTML within text. */
const INLINE_ATTRIBUTE = 'data-inline';

/** Priority of the rule taking an element that acts in the page as opaque source, before all others. */
const SOURCE_PRIORITY = 1000;

/** Priority of the rules for elements of unknown type, after all rules of known elements. */
const UNKNOWN_PRIORITY = 1;

/**
 * Elements that act in the page when displayed - run a script, load a document, play media, take
 * input - and are therefore never displayed in the editor, but kept as opaque source.
 */
const ACTIVE_TAGS = new Set([
  'script', 'noscript', 'style', 'link', 'meta', 'base', 'template', 'iframe', 'frame', 'frameset',
  'object', 'embed', 'applet', 'portal', 'form', 'input', 'button', 'select', 'option', 'optgroup',
  'textarea', 'video', 'audio', 'source', 'track', 'canvas', 'svg', 'math',
]);

/**
 * Elements that only structure a known element and are written by it again, e.g. the body of a
 * table. They are not taken for elements of unknown type.
 */
const STRUCTURE_TAGS = new Set([
  'html', 'head', 'body', 'title', 'tbody', 'thead', 'tfoot', 'colgroup', 'col', SOURCE_ELEMENT,
]);

/** Elements that are blocks, i.e. that do not stand within a line of text. */
const BLOCK_TAGS = new Set([
  'address', 'article', 'aside', 'blockquote', 'center', 'dd', 'details', 'dialog', 'dir', 'div',
  'dl', 'dt', 'fieldset', 'figcaption', 'figure', 'footer', 'form', 'h1', 'h2', 'h3', 'h4', 'h5',
  'h6', 'header', 'hgroup', 'hr', 'li', 'main', 'menu', 'nav', 'ol', 'p', 'pre', 'section',
  'summary', 'table', 'ul', 'iframe', 'video', 'audio', 'canvas', 'object', 'embed', 'script',
  'style', 'noscript', 'template', 'svg', 'math',
]);

/** Elements whose content may be blocks, so that a block element within them is a block. */
const CONTAINER_TAGS = new Set([
  'body', 'dl', 'address', 'article', 'aside', 'blockquote', 'center', 'dd', 'details', 'dialog', 'div',
  'fieldset', 'figure', 'footer', 'form', 'header', 'li', 'main', 'nav', 'section', 'td', 'th',
]);

/** Element type names that can be written as an element of their own. */
const TAG_NAME = /^[a-z][a-z0-9-]*$/;

/** Attributes holding an address, which must not name a script. */
const SCRIPT_URL = /^\s*(javascript|vbscript|data\s*:\s*text\/html)\s*:?/i;

/**
 * Style properties the formatting marks of the editor take from any element (bold, italic,
 * underline, strike). They are formatting of the content then, not a style the element keeps.
 */
const FORMATTING_STYLES: Record<string, (value: string) => boolean> = {
  'font-weight': (value) => /^(bold(er)?|normal|[4-9]\d{2,})$/.test(value),
  'font-style': (value) => /^(italic|normal)$/.test(value),
  'text-decoration': (value) => /underline|line-through/.test(value),
  'text-decoration-line': (value) => /underline|line-through/.test(value),
};

/**
 * Style properties a node or mark takes as attributes of its own, by node or mark type, e.g. the
 * text color of a styled span.
 */
const OWN_STYLES: Record<string, string[]> = {
  textStyle: ['color', 'background-color', 'font-family', 'font-size', 'line-height'],
};

/** Whether the given attribute would act in the page: an event handler or a script address. */
function isActiveAttribute(name: string, value: string): boolean {
  return name.startsWith('on') || name === 'srcdoc' || SCRIPT_URL.test(value)
    || (name === 'style' && /javascript:|expression\s*\(/i.test(value));
}

/** Whether the given element acts in the page and must not be displayed in the editor. */
function isActive(element: HTMLElement): boolean {
  if (ACTIVE_TAGS.has(element.localName)) {
    return true;
  }
  for (const attribute of Array.from(element.attributes)) {
    if (isActiveAttribute(attribute.name.toLowerCase(), attribute.value)) {
      return true;
    }
  }
  return false;
}

/** Whether the given element is a block. */
function isBlock(element: Element): boolean {
  if (BLOCK_TAGS.has(element.localName)) {
    return true;
  }
  return Array.from(element.children).some(isBlock);
}

/** Elements whose content is text, so that an element within them stands within a line. */
const TEXT_TAGS = new Set(['p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'pre']);

/** Whether the given element stands where a block may stand. */
function inBlockContext(element: Element): boolean {
  const parent = element.parentElement;
  if (parent == null || CONTAINER_TAGS.has(parent.localName)) {
    return true;
  }
  return !TEXT_TAGS.has(parent.localName) && Array.from(parent.children).some(isBlock);
}

/** Whether the content of the given element mixes text with blocks. */
function mixesTextWithBlocks(element: Element): boolean {
  const hasBlocks = Array.from(element.children).some(isBlock);
  return hasBlocks && Array.from(element.childNodes).some(
    (child) => child.nodeType === 3 && (child.textContent || '').trim() !== '');
}

/** Whether the given element is of unknown type and can be written as an element of its own. */
function isUnknown(element: HTMLElement): boolean {
  return !STRUCTURE_TAGS.has(element.localName) && TAG_NAME.test(element.localName) && !isActive(element);
}

/**
 * The given style without the properties the node or mark of the given type takes as formatting or
 * as attributes of its own, or null if nothing is left.
 */
function keptStyle(type: string, style: string): string | null {
  const own = OWN_STYLES[type] || [];
  const kept = style.split(';').map((declaration) => declaration.trim()).filter((declaration) => {
    if (declaration === '') {
      return false;
    }
    const colon = declaration.indexOf(':');
    if (colon < 0) {
      return true;
    }
    const property = declaration.substring(0, colon).trim().toLowerCase();
    const value = declaration.substring(colon + 1).trim().toLowerCase();
    if (own.includes(property)) {
      return false;
    }
    const formatting = FORMATTING_STYLES[property];
    return !(formatting && formatting(value));
  });
  return kept.length === 0 ? null : kept.join('; ');
}

/**
 * The attributes of the given element that the node or mark of the given type does not handle
 * itself, or null if there are none.
 */
function keptAttributes(type: string, element: HTMLElement, handled: Set<string>): Record<string, string> | null {
  const result: Record<string, string> = {};
  let any = false;
  for (const attribute of Array.from(element.attributes)) {
    const name = attribute.name;
    if (handled.has(name) || isActiveAttribute(name.toLowerCase(), attribute.value)) {
      continue;
    }
    const value = name === 'style' ? keptStyle(type, attribute.value) : attribute.value;
    if (value == null) {
      continue;
    }
    result[name] = value;
    any = true;
  }
  return any ? result : null;
}

/** The attribute definitions of a node or mark holding the kept attributes of its element. */
function keptAttributesSpec(type: string, handled: Set<string>) {
  return {
    [HTML_ATTRIBUTES]: {
      default: null,
      keepOnSplit: true,
      parseHTML: (element: HTMLElement) => keptAttributes(type, element, handled),
      renderHTML: (attributes: Record<string, unknown>) =>
        (attributes[HTML_ATTRIBUTES] as Record<string, string> | null) || {},
    },
  };
}

/** The attribute definitions of an element of unknown type. */
function unknownElementAttributes(type: string) {
  return {
    [TAG]: {
      default: 'div',
      rendered: false,
      parseHTML: (element: HTMLElement) => element.localName,
    },
    [HTML_ATTRIBUTES]: {
      default: null,
      rendered: false,
      parseHTML: (element: HTMLElement) => keptAttributes(type, element, new Set()),
    },
  };
}

/** The attribute definitions of an opaque piece of HTML. */
function sourceAttributes() {
  return {
    [SOURCE]: {
      default: '',
      rendered: false,
      parseHTML: (element: HTMLElement) => element.localName === SOURCE_ELEMENT
        ? element.getAttribute(SOURCE_ATTRIBUTE) || ''
        : element.outerHTML,
    },
  };
}

/** Rule reading a {@link SOURCE_ELEMENT}, which stands for an opaque piece of HTML. */
function sourceElementRule(inline: boolean): TagParseRule {
  return {
    tag: SOURCE_ELEMENT,
    priority: SOURCE_PRIORITY,
    getAttrs: (element: HTMLElement) => element.hasAttribute(INLINE_ATTRIBUTE) === inline ? {} : false,
  };
}

/** Writes an opaque piece of HTML as the {@link SOURCE_ELEMENT} standing for it. */
function renderSource(source: string, inline: boolean) {
  const attributes: Record<string, string> = { [SOURCE_ATTRIBUTE]: source };
  if (inline) {
    attributes[INLINE_ATTRIBUTE] = '';
  }
  return [SOURCE_ELEMENT, attributes] as [string, Record<string, string>];
}

/** Displays an opaque piece of HTML in the editor as its source. */
function sourceView(inline: boolean) {
  return ({ node }: { node: { attrs: Record<string, unknown> } }) => {
    const dom = document.createElement(inline ? 'span' : 'div');
    dom.className = 'tlWysiwygEditor__htmlSource' + (inline ? ' tlWysiwygEditor__htmlSource--inline' : '');
    dom.contentEditable = 'false';
    dom.textContent = String(node.attrs[SOURCE] || '');
    return { dom };
  };
}

/**
 * Replaces each element standing for an opaque piece of HTML in the given text by the source it
 * holds.
 */
export function restoreHtmlSources(html: string): string {
  if (html.indexOf(SOURCE_ELEMENT) < 0) {
    return html;
  }
  const pattern = new RegExp('<' + SOURCE_ELEMENT + ' ' + SOURCE_ATTRIBUTE + '="([^"]*)"[^>]*></' + SOURCE_ELEMENT + '>', 'g');
  return html.replace(pattern, (_match, source: string) => source
    .replace(/&quot;/g, '"')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&'));
}

/** Block element of unknown type holding blocks, e.g. a <div> around paragraphs. */
const HtmlBlock = Node.create({
  name: HTML_BLOCK,
  group: 'block',
  content: 'block+',
  defining: true,

  addAttributes() {
    return unknownElementAttributes(HTML_BLOCK);
  },

  parseHTML() {
    return [{
      tag: '*',
      priority: UNKNOWN_PRIORITY,
      getAttrs: (element: HTMLElement) => isUnknown(element) && isBlock(element) && inBlockContext(element)
        && Array.from(element.children).some(isBlock) && !mixesTextWithBlocks(element) ? {} : false,
    }];
  },

  renderHTML({ node }) {
    return [node.attrs[TAG], node.attrs[HTML_ATTRIBUTES] || {}, 0];
  },
});

/** Block element of unknown type holding text, e.g. a <div> with text. */
const HtmlTextBlock = Node.create({
  name: HTML_TEXT_BLOCK,
  group: 'block',
  content: 'inline*',
  defining: true,

  addAttributes() {
    return unknownElementAttributes(HTML_TEXT_BLOCK);
  },

  parseHTML() {
    return [{
      tag: '*',
      priority: UNKNOWN_PRIORITY,
      getAttrs: (element: HTMLElement) => isUnknown(element) && BLOCK_TAGS.has(element.localName)
        && inBlockContext(element) && !Array.from(element.children).some(isBlock) ? {} : false,
    }];
  },

  renderHTML({ node }) {
    return [node.attrs[TAG], node.attrs[HTML_ATTRIBUTES] || {}, 0];
  },
});

/** Inline element of unknown type around text, e.g. a <span class> or a <sup>. */
const HtmlInline = Mark.create({
  name: HTML_INLINE,

  // Several such elements may wrap the same text.
  excludes: '',

  addAttributes() {
    return unknownElementAttributes(HTML_INLINE);
  },

  parseHTML() {
    return [{
      tag: '*',
      priority: UNKNOWN_PRIORITY,
      getAttrs: (element: HTMLElement) => isUnknown(element) && !isBlock(element)
        // A span with a style is the styled text of the editor, see TextStyle.
        && !(element.localName === 'span' && element.hasAttribute('style'))
        && (element.textContent || '') !== '' ? {} : false,
    }];
  },

  renderHTML({ mark }) {
    return [mark.attrs[TAG], mark.attrs[HTML_ATTRIBUTES] || {}, 0];
  },
});

/**
 * Opaque piece of HTML in place of a block: an element that acts in the page, or an element of
 * unknown type whose content mixes text with blocks.
 */
const HtmlSourceBlock = Node.create({
  name: HTML_SOURCE_BLOCK,
  group: 'block',
  atom: true,
  selectable: true,

  addAttributes() {
    return sourceAttributes();
  },

  parseHTML() {
    return [
      sourceElementRule(false),
      {
        tag: '*',
        priority: SOURCE_PRIORITY,
        getAttrs: (element: HTMLElement) => !STRUCTURE_TAGS.has(element.localName) && isActive(element)
          && isBlock(element) && inBlockContext(element) ? {} : false,
      },
      {
        tag: '*',
        priority: UNKNOWN_PRIORITY,
        getAttrs: (element: HTMLElement) => isUnknown(element) && isBlock(element) && inBlockContext(element)
          && mixesTextWithBlocks(element) ? {} : false,
      },
    ];
  },

  renderHTML({ node }) {
    return renderSource(String(node.attrs[SOURCE]), false);
  },

  addNodeView() {
    return sourceView(false);
  },
});

/**
 * Opaque piece of HTML within text: an element that acts in the page, an empty inline element of
 * unknown type, or an element of unknown type that cannot be written as an element of its own.
 */
const HtmlSourceInline = Node.create({
  name: HTML_SOURCE_INLINE,
  group: 'inline',
  inline: true,
  atom: true,
  selectable: true,

  addAttributes() {
    return sourceAttributes();
  },

  parseHTML() {
    return [
      sourceElementRule(true),
      {
        tag: '*',
        priority: SOURCE_PRIORITY,
        getAttrs: (element: HTMLElement) => !STRUCTURE_TAGS.has(element.localName) && isActive(element)
          && !(isBlock(element) && inBlockContext(element)) ? {} : false,
      },
      {
        tag: '*',
        priority: UNKNOWN_PRIORITY,
        getAttrs: (element: HTMLElement) => !STRUCTURE_TAGS.has(element.localName) && !isActive(element)
          && ((!TAG_NAME.test(element.localName)) || (!isBlock(element) && (element.textContent || '') === ''))
          ? {} : false,
      },
    ];
  },

  renderHTML({ node }) {
    return renderSource(String(node.attrs[SOURCE]), true);
  },

  addNodeView() {
    return sourceView(true);
  },
});

/** The node and mark types of this extension, which hold their element's attributes themselves. */
const OWN_TYPES = new Set([HTML_BLOCK, HTML_TEXT_BLOCK, HTML_INLINE, HTML_SOURCE_BLOCK, HTML_SOURCE_INLINE]);

/** Types without an element of their own. */
const ELEMENTLESS_TYPES = new Set(['doc', 'text']);

/** Whether the given rule is one of this extension's rules for elements of unknown type. */
function isPassthroughRule(rule: { tag?: string }): boolean {
  return rule.tag === '*';
}

/**
 * The parser for content pasted from the clipboard: the parser of the schema without the rules for
 * elements of unknown type and without keeping attributes of known elements.
 */
function clipboardParser(schema: Schema): PMDOMParser {
  const rules = PMDOMParser.fromSchema(schema).rules
    .filter((rule) => !isPassthroughRule(rule as { tag?: string }))
    .map((rule) => {
      const tagRule = rule as TagParseRule;
      if (!tagRule.tag || !tagRule.getAttrs) {
        return rule;
      }
      const getAttrs = tagRule.getAttrs;
      return {
        ...tagRule,
        getAttrs: (element: HTMLElement) => {
          const attrs = getAttrs(element);
          if (attrs && typeof attrs === 'object' && HTML_ATTRIBUTES in attrs) {
            const { [HTML_ATTRIBUTES]: _dropped, ...rest } = attrs;
            return rest;
          }
          return attrs;
        },
      };
    });
  return new PMDOMParser(schema, rules);
}

/** The names of the attributes the given node or mark extension defines itself. */
function ownAttributeNames(extension: AnyExtension): Set<string> {
  const addAttributes = getExtensionField<() => Record<string, unknown>>(extension, 'addAttributes', {
    name: extension.name,
    options: extension.options,
    storage: extension.storage,
  });
  return new Set(addAttributes ? Object.keys(addAttributes() || {}) : []);
}

/**
 * Keeps the HTML the editor's schema has no place for, see the description of this file.
 *
 * Must come before the extensions of the formatting it is combined with, so that an element of
 * unknown type encloses the formatting of its content.
 */
export const HtmlPassthrough = Extension.create({
  name: 'htmlPassthrough',

  addExtensions() {
    return [HtmlBlock, HtmlTextBlock, HtmlInline, HtmlSourceBlock, HtmlSourceInline];
  },

  addGlobalAttributes() {
    // An attribute of the element of a known node or mark is kept unless the node or mark
    // handles it itself. Global attributes are written before the node's own, so that what the
    // node writes itself (e.g. a style property) wins.
    return (this.extensions as AnyExtension[])
      .filter((extension) => !OWN_TYPES.has(extension.name) && !ELEMENTLESS_TYPES.has(extension.name))
      .map((extension) => ({
        types: [extension.name],
        attributes: keptAttributesSpec(extension.name, ownAttributeNames(extension)),
      }));
  },

  addProseMirrorPlugins() {
    return [new Plugin({
      key: new PluginKey('htmlPassthroughClipboard'),
      props: {
        clipboardParser: clipboardParser(this.editor.schema),
      },
    })];
  },
});
