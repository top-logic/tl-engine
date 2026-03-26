import { React } from 'tl-react-bridge';
import type { Editor } from '@tiptap/react';

interface ToolbarProps {
  editor: Editor | null;
  items: string[];
  onImageUpload: () => void;
}

interface ToolbarButtonDef {
  label: string;
  action: (editor: Editor) => void;
  isActive?: (editor: Editor) => boolean;
}

const BUTTON_DEFS: Record<string, ToolbarButtonDef> = {
  bold: {
    label: 'B',
    action: (e) => e.chain().focus().toggleBold().run(),
    isActive: (e) => e.isActive('bold'),
  },
  italic: {
    label: 'I',
    action: (e) => e.chain().focus().toggleItalic().run(),
    isActive: (e) => e.isActive('italic'),
  },
  underline: {
    label: 'U',
    action: (e) => e.chain().focus().toggleUnderline().run(),
    isActive: (e) => e.isActive('underline'),
  },
  strike: {
    label: 'S',
    action: (e) => e.chain().focus().toggleStrike().run(),
    isActive: (e) => e.isActive('strike'),
  },
  heading: {
    label: 'H',
    action: (e) => e.chain().focus().toggleHeading({ level: 2 }).run(),
    isActive: (e) => e.isActive('heading'),
  },
  bulletList: {
    label: '\u2022',
    action: (e) => e.chain().focus().toggleBulletList().run(),
    isActive: (e) => e.isActive('bulletList'),
  },
  orderedList: {
    label: '1.',
    action: (e) => e.chain().focus().toggleOrderedList().run(),
    isActive: (e) => e.isActive('orderedList'),
  },
  blockquote: {
    label: '\u201C',
    action: (e) => e.chain().focus().toggleBlockquote().run(),
    isActive: (e) => e.isActive('blockquote'),
  },
  link: {
    label: '\uD83D\uDD17',
    action: (e) => {
      const url = window.prompt('URL:');
      if (url) {
        e.chain().focus().setLink({ href: url }).run();
      }
    },
    isActive: (e) => e.isActive('link'),
  },
  image: {
    label: '\uD83D\uDDBC',
    action: () => { /* handled via onImageUpload */ },
  },
  table: {
    label: '\u2637',
    action: (e) => e.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run(),
  },
  codeBlock: {
    label: '</>',
    action: (e) => e.chain().focus().toggleCodeBlock().run(),
    isActive: (e) => e.isActive('codeBlock'),
  },
  color: {
    label: 'A',
    action: (e) => {
      const color = window.prompt('Color (hex):', '#000000');
      if (color) {
        e.chain().focus().setColor(color).run();
      }
    },
  },
  undo: {
    label: '\u21B6',
    action: (e) => e.chain().focus().undo().run(),
  },
  redo: {
    label: '\u21B7',
    action: (e) => e.chain().focus().redo().run(),
  },
};

const WysiwygToolbar: React.FC<ToolbarProps> = ({ editor, items, onImageUpload }) => {
  if (!editor) return null;

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
            onMouseDown={(e) => { e.preventDefault(); onClick(); }}
            title={item}
          >
            {def.label}
          </button>
        );
      })}
    </div>
  );
};

export default WysiwygToolbar;
