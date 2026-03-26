import { React, useI18N } from 'tl-react-bridge';
import type { Editor } from '@tiptap/react';

interface ToolbarProps {
  editor: Editor | null;
  items: string[];
  onImageUpload: () => void;
}

interface ToolbarButtonDef {
  icon: string;
  i18nKey: string;
  fallback: string;
  action: (editor: Editor) => void;
  isActive?: (editor: Editor) => boolean;
}

const I18N = 'class.com.top_logic.layout.react.wysiwyg.I18NConstants.TOOLBAR_';

const BUTTON_DEFS: Record<string, ToolbarButtonDef> = {
  bold: {
    icon: 'ri-bold',
    i18nKey: I18N + 'BOLD', fallback: 'Bold',
    action: (e) => e.chain().focus().toggleBold().run(),
    isActive: (e) => e.isActive('bold'),
  },
  italic: {
    icon: 'ri-italic',
    i18nKey: I18N + 'ITALIC', fallback: 'Italic',
    action: (e) => e.chain().focus().toggleItalic().run(),
    isActive: (e) => e.isActive('italic'),
  },
  underline: {
    icon: 'ri-underline',
    i18nKey: I18N + 'UNDERLINE', fallback: 'Underline',
    action: (e) => e.chain().focus().toggleUnderline().run(),
    isActive: (e) => e.isActive('underline'),
  },
  strike: {
    icon: 'ri-strikethrough',
    i18nKey: I18N + 'STRIKETHROUGH', fallback: 'Strikethrough',
    action: (e) => e.chain().focus().toggleStrike().run(),
    isActive: (e) => e.isActive('strike'),
  },
  heading: {
    icon: 'ri-h-2',
    i18nKey: I18N + 'HEADING', fallback: 'Heading',
    action: (e) => e.chain().focus().toggleHeading({ level: 2 }).run(),
    isActive: (e) => e.isActive('heading'),
  },
  bulletList: {
    icon: 'ri-list-unordered',
    i18nKey: I18N + 'BULLET_LIST', fallback: 'Bullet List',
    action: (e) => e.chain().focus().toggleBulletList().run(),
    isActive: (e) => e.isActive('bulletList'),
  },
  orderedList: {
    icon: 'ri-list-ordered',
    i18nKey: I18N + 'ORDERED_LIST', fallback: 'Numbered List',
    action: (e) => e.chain().focus().toggleOrderedList().run(),
    isActive: (e) => e.isActive('orderedList'),
  },
  blockquote: {
    icon: 'ri-double-quotes-l',
    i18nKey: I18N + 'BLOCKQUOTE', fallback: 'Blockquote',
    action: (e) => e.chain().focus().toggleBlockquote().run(),
    isActive: (e) => e.isActive('blockquote'),
  },
  link: {
    icon: 'ri-link',
    i18nKey: I18N + 'LINK', fallback: 'Link',
    action: (e) => {
      const url = window.prompt('URL:');
      if (url) {
        e.chain().focus().setLink({ href: url }).run();
      }
    },
    isActive: (e) => e.isActive('link'),
  },
  image: {
    icon: 'ri-image-line',
    i18nKey: I18N + 'IMAGE', fallback: 'Image',
    action: () => { /* handled via onImageUpload */ },
  },
  table: {
    icon: 'ri-table-line',
    i18nKey: I18N + 'TABLE', fallback: 'Table',
    action: (e) => e.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run(),
  },
  codeBlock: {
    icon: 'ri-code-s-slash-line',
    i18nKey: I18N + 'CODE_BLOCK', fallback: 'Code Block',
    action: (e) => e.chain().focus().toggleCodeBlock().run(),
    isActive: (e) => e.isActive('codeBlock'),
  },
  color: {
    icon: 'ri-font-color',
    i18nKey: I18N + 'COLOR', fallback: 'Text Color',
    action: (e) => {
      const color = window.prompt('Color (hex):', '#000000');
      if (color) {
        e.chain().focus().setColor(color).run();
      }
    },
  },
  undo: {
    icon: 'ri-arrow-go-back-line',
    i18nKey: I18N + 'UNDO', fallback: 'Undo',
    action: (e) => e.chain().focus().undo().run(),
  },
  redo: {
    icon: 'ri-arrow-go-forward-line',
    i18nKey: I18N + 'REDO', fallback: 'Redo',
    action: (e) => e.chain().focus().redo().run(),
  },
};

/** Build the i18n key map for all button definitions used by the current toolbar. */
function buildI18NKeys(items: string[]): Record<string, string> {
  const keys: Record<string, string> = {};
  for (const item of items) {
    const def = BUTTON_DEFS[item];
    if (def) {
      keys[def.i18nKey] = def.fallback;
    }
  }
  return keys;
}

const WysiwygToolbar: React.FC<ToolbarProps> = ({ editor, items, onImageUpload }) => {
  if (!editor) return null;

  const i18nKeys = React.useMemo(() => buildI18NKeys(items), [items]);
  const labels = useI18N(i18nKeys);

  return (
    <div className="tlWysiwygToolbar">
      {items.map((item, index) => {
        if (item === '|') {
          return <span key={index} className="tlWysiwygToolbar__separator" />;
        }
        const def = BUTTON_DEFS[item];
        if (!def) return null;
        const active = def.isActive ? def.isActive(editor) : false;
        const onClick = item === 'image' ? onImageUpload : () => def.action(editor);
        return (
          <button
            key={item}
            type="button"
            className={'tlWysiwygToolbar__button' + (active ? ' tlWysiwygToolbar__button--active' : '')}
            onMouseDown={(e) => {
              if (item !== 'image') {
                e.preventDefault();
              }
              onClick();
            }}
            title={labels[def.i18nKey] || def.fallback}
          >
            <i className={def.icon} />
          </button>
        );
      })}
    </div>
  );
};

export default WysiwygToolbar;
