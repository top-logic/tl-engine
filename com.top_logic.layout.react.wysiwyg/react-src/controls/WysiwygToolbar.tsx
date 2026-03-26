import { React, useI18N } from 'tl-react-bridge';
import type { Editor } from '@tiptap/react';
import * as DropdownMenu from '@radix-ui/react-dropdown-menu';
import * as Popover from '@radix-ui/react-popover';
import * as Tooltip from '@radix-ui/react-tooltip';
import * as Separator from '@radix-ui/react-separator';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface ToolbarProps {
  editor: Editor | null;
  onImageUpload: () => void;
}

// ---------------------------------------------------------------------------
// I18N key prefix
// ---------------------------------------------------------------------------

const ALL_I18N_KEYS: Record<string, string> = {
  'js.wysiwyg.bold': 'Bold',
  'js.wysiwyg.italic': 'Italic',
  'js.wysiwyg.underline': 'Underline',
  'js.wysiwyg.strikethrough': 'Strikethrough',
  'js.wysiwyg.heading': 'Heading',
  'js.wysiwyg.paragraph': 'Paragraph',
  'js.wysiwyg.heading1': 'Heading 1',
  'js.wysiwyg.heading2': 'Heading 2',
  'js.wysiwyg.heading3': 'Heading 3',
  'js.wysiwyg.heading4': 'Heading 4',
  'js.wysiwyg.heading5': 'Heading 5',
  'js.wysiwyg.heading6': 'Heading 6',
  'js.wysiwyg.bulletList': 'Bullet List',
  'js.wysiwyg.orderedList': 'Numbered List',
  'js.wysiwyg.lists': 'Lists',
  'js.wysiwyg.blockquote': 'Blockquote',
  'js.wysiwyg.link': 'Link',
  'js.wysiwyg.linkUrl': 'URL',
  'js.wysiwyg.linkApply': 'Apply',
  'js.wysiwyg.linkRemove': 'Remove link',
  'js.wysiwyg.image': 'Image',
  'js.wysiwyg.table': 'Table',
  'js.wysiwyg.codeBlock': 'Code Block',
  'js.wysiwyg.undo': 'Undo',
  'js.wysiwyg.redo': 'Redo',
};

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function t(labels: Record<string, string>, key: string): string {
  return labels['js.wysiwyg.' + key] || ALL_I18N_KEYS['js.wysiwyg.' + key] || key;
}

// ---------------------------------------------------------------------------
// ToolbarButton -- simple icon button with tooltip
// ---------------------------------------------------------------------------

interface BtnProps {
  icon: string;
  tooltip: string;
  active?: boolean;
  disabled?: boolean;
  onClick: () => void;
}

const ToolbarButton: React.FC<BtnProps> = ({ icon, tooltip, active, disabled, onClick }) => {
  return (
    <Tooltip.Root delayDuration={400}>
      <Tooltip.Trigger asChild>
        <button
          type="button"
          className={'tlWysiwygToolbar__btn' + (active ? ' tlWysiwygToolbar__btn--active' : '')}
          disabled={disabled}
          onMouseDown={(e: React.MouseEvent) => {
            e.preventDefault();
            onClick();
          }}
          aria-label={tooltip}
        >
          <i className={icon} />
        </button>
      </Tooltip.Trigger>
      <Tooltip.Portal>
        <Tooltip.Content className="tlWysiwygToolbar__tooltip" sideOffset={6}>
          {tooltip}
          <Tooltip.Arrow className="tlWysiwygToolbar__tooltipArrow" />
        </Tooltip.Content>
      </Tooltip.Portal>
    </Tooltip.Root>
  );
};

// ---------------------------------------------------------------------------
// HeadingDropdown
// ---------------------------------------------------------------------------

interface HeadingLevel {
  label: string;
  level: number | null; // null = paragraph
  icon: string;
}

const HeadingDropdown: React.FC<{ editor: Editor; labels: Record<string, string> }> = ({ editor, labels }) => {
  const levels: HeadingLevel[] = [
    { label: t(labels, 'paragraph'), level: null, icon: 'ri-paragraph' },
    { label: t(labels, 'heading1'), level: 1, icon: 'ri-h-1' },
    { label: t(labels, 'heading2'), level: 2, icon: 'ri-h-2' },
    { label: t(labels, 'heading3'), level: 3, icon: 'ri-h-3' },
    { label: t(labels, 'heading4'), level: 4, icon: 'ri-h-4' },
    { label: t(labels, 'heading5'), level: 5, icon: 'ri-h-5' },
    { label: t(labels, 'heading6'), level: 6, icon: 'ri-h-6' },
  ];

  // Determine current heading level
  let currentIcon = 'ri-paragraph';
  let currentLabel = t(labels, 'paragraph');
  for (let lvl = 1; lvl <= 6; lvl++) {
    if (editor.isActive('heading', { level: lvl })) {
      currentIcon = 'ri-h-' + lvl;
      currentLabel = t(labels, 'HEADING_' + lvl);
      break;
    }
  }

  return (
    <DropdownMenu.Root>
      <Tooltip.Root delayDuration={400}>
        <Tooltip.Trigger asChild>
          <DropdownMenu.Trigger asChild>
            <button type="button" className="tlWysiwygToolbar__btn tlWysiwygToolbar__btn--dropdown" aria-label={t(labels, 'heading')}>
              <i className={currentIcon} />
              <i className="ri-arrow-down-s-line tlWysiwygToolbar__chevron" />
            </button>
          </DropdownMenu.Trigger>
        </Tooltip.Trigger>
        <Tooltip.Portal>
          <Tooltip.Content className="tlWysiwygToolbar__tooltip" sideOffset={6}>
            {currentLabel}
            <Tooltip.Arrow className="tlWysiwygToolbar__tooltipArrow" />
          </Tooltip.Content>
        </Tooltip.Portal>
      </Tooltip.Root>
      <DropdownMenu.Portal>
        <DropdownMenu.Content className="tlWysiwygToolbar__dropdown" sideOffset={4} align="start">
          {levels.map((h) => {
            const isActive = h.level === null
              ? !editor.isActive('heading')
              : editor.isActive('heading', { level: h.level });
            return (
              <DropdownMenu.Item
                key={h.level ?? 'p'}
                className={'tlWysiwygToolbar__dropdownItem' + (isActive ? ' tlWysiwygToolbar__dropdownItem--active' : '')}
                onSelect={() => {
                  if (h.level === null) {
                    editor.chain().focus().setParagraph().run();
                  } else {
                    editor.chain().focus().toggleHeading({ level: h.level as 1|2|3|4|5|6 }).run();
                  }
                }}
              >
                <i className={h.icon + ' tlWysiwygToolbar__dropdownIcon'} />
                <span>{h.label}</span>
              </DropdownMenu.Item>
            );
          })}
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
};

// ---------------------------------------------------------------------------
// ListDropdown
// ---------------------------------------------------------------------------

const ListDropdown: React.FC<{ editor: Editor; labels: Record<string, string> }> = ({ editor, labels }) => {
  const isBullet = editor.isActive('bulletList');
  const isOrdered = editor.isActive('orderedList');
  const currentIcon = isOrdered ? 'ri-list-ordered' : 'ri-list-unordered';

  return (
    <DropdownMenu.Root>
      <Tooltip.Root delayDuration={400}>
        <Tooltip.Trigger asChild>
          <DropdownMenu.Trigger asChild>
            <button
              type="button"
              className={'tlWysiwygToolbar__btn tlWysiwygToolbar__btn--dropdown' + ((isBullet || isOrdered) ? ' tlWysiwygToolbar__btn--active' : '')}
              aria-label={t(labels, 'lists')}
            >
              <i className={currentIcon} />
              <i className="ri-arrow-down-s-line tlWysiwygToolbar__chevron" />
            </button>
          </DropdownMenu.Trigger>
        </Tooltip.Trigger>
        <Tooltip.Portal>
          <Tooltip.Content className="tlWysiwygToolbar__tooltip" sideOffset={6}>
            {t(labels, 'lists')}
            <Tooltip.Arrow className="tlWysiwygToolbar__tooltipArrow" />
          </Tooltip.Content>
        </Tooltip.Portal>
      </Tooltip.Root>
      <DropdownMenu.Portal>
        <DropdownMenu.Content className="tlWysiwygToolbar__dropdown" sideOffset={4} align="start">
          <DropdownMenu.Item
            className={'tlWysiwygToolbar__dropdownItem' + (isBullet ? ' tlWysiwygToolbar__dropdownItem--active' : '')}
            onSelect={() => editor.chain().focus().toggleBulletList().run()}
          >
            <i className="ri-list-unordered tlWysiwygToolbar__dropdownIcon" />
            <span>{t(labels, 'bulletList')}</span>
          </DropdownMenu.Item>
          <DropdownMenu.Item
            className={'tlWysiwygToolbar__dropdownItem' + (isOrdered ? ' tlWysiwygToolbar__dropdownItem--active' : '')}
            onSelect={() => editor.chain().focus().toggleOrderedList().run()}
          >
            <i className="ri-list-ordered tlWysiwygToolbar__dropdownIcon" />
            <span>{t(labels, 'orderedList')}</span>
          </DropdownMenu.Item>
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
};

// ---------------------------------------------------------------------------
// LinkPopover
// ---------------------------------------------------------------------------

const LinkPopover: React.FC<{ editor: Editor; labels: Record<string, string> }> = ({ editor, labels }) => {
  const [open, setOpen] = React.useState(false);
  const [url, setUrl] = React.useState('');
  const inputRef = React.useRef<HTMLInputElement>(null);

  const isActive = editor.isActive('link');

  const handleOpen = React.useCallback((nextOpen: boolean) => {
    if (nextOpen) {
      // Pre-fill with current link if editing
      const attrs = editor.getAttributes('link');
      setUrl(attrs.href || '');
    }
    setOpen(nextOpen);
  }, [editor]);

  const applyLink = React.useCallback(() => {
    if (url.trim()) {
      editor.chain().focus().setLink({ href: url.trim() }).run();
    }
    setOpen(false);
  }, [editor, url]);

  const removeLink = React.useCallback(() => {
    editor.chain().focus().unsetLink().run();
    setOpen(false);
  }, [editor]);

  const handleKeyDown = React.useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      applyLink();
    }
  }, [applyLink]);

  return (
    <Popover.Root open={open} onOpenChange={handleOpen}>
      <Tooltip.Root delayDuration={400}>
        <Tooltip.Trigger asChild>
          <Popover.Trigger asChild>
            <button
              type="button"
              className={'tlWysiwygToolbar__btn' + (isActive ? ' tlWysiwygToolbar__btn--active' : '')}
              aria-label={t(labels, 'link')}
            >
              <i className="ri-link" />
            </button>
          </Popover.Trigger>
        </Tooltip.Trigger>
        <Tooltip.Portal>
          <Tooltip.Content className="tlWysiwygToolbar__tooltip" sideOffset={6}>
            {t(labels, 'link')}
            <Tooltip.Arrow className="tlWysiwygToolbar__tooltipArrow" />
          </Tooltip.Content>
        </Tooltip.Portal>
      </Tooltip.Root>
      <Popover.Portal>
        <Popover.Content className="tlWysiwygToolbar__linkPopover" sideOffset={6} align="start">
          <div className="tlWysiwygToolbar__linkForm">
            <label className="tlWysiwygToolbar__linkLabel">{t(labels, 'linkUrl')}</label>
            <input
              ref={inputRef}
              type="url"
              className="tlWysiwygToolbar__linkInput"
              placeholder="https://..."
              value={url}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setUrl(e.target.value)}
              onKeyDown={handleKeyDown}
              autoFocus
            />
            <div className="tlWysiwygToolbar__linkActions">
              <button
                type="button"
                className="tlWysiwygToolbar__linkApply"
                onClick={applyLink}
                disabled={!url.trim()}
              >
                {t(labels, 'linkApply')}
              </button>
              {isActive && (
                <button
                  type="button"
                  className="tlWysiwygToolbar__linkRemove"
                  onClick={removeLink}
                >
                  {t(labels, 'linkRemove')}
                </button>
              )}
            </div>
          </div>
          <Popover.Arrow className="tlWysiwygToolbar__popoverArrow" />
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
};

// ---------------------------------------------------------------------------
// Main Toolbar
// ---------------------------------------------------------------------------

const WysiwygToolbar: React.FC<ToolbarProps> = ({ editor, onImageUpload }) => {
  const labels = useI18N(ALL_I18N_KEYS);

  if (!editor) return null;

  return (
    <Tooltip.Provider delayDuration={400}>
      <div className="tlWysiwygToolbar" role="toolbar" aria-label="Editor toolbar">
        {/* Text formatting */}
        <ToolbarButton
          icon="ri-bold"
          tooltip={t(labels, 'bold')}
          active={editor.isActive('bold')}
          onClick={() => editor.chain().focus().toggleBold().run()}
        />
        <ToolbarButton
          icon="ri-italic"
          tooltip={t(labels, 'italic')}
          active={editor.isActive('italic')}
          onClick={() => editor.chain().focus().toggleItalic().run()}
        />
        <ToolbarButton
          icon="ri-underline"
          tooltip={t(labels, 'underline')}
          active={editor.isActive('underline')}
          onClick={() => editor.chain().focus().toggleUnderline().run()}
        />
        <ToolbarButton
          icon="ri-strikethrough"
          tooltip={t(labels, 'strikethrough')}
          active={editor.isActive('strike')}
          onClick={() => editor.chain().focus().toggleStrike().run()}
        />

        <Separator.Root className="tlWysiwygToolbar__sep" orientation="vertical" decorative />

        {/* Heading dropdown */}
        <HeadingDropdown editor={editor} labels={labels} />

        {/* List dropdown */}
        <ListDropdown editor={editor} labels={labels} />

        <Separator.Root className="tlWysiwygToolbar__sep" orientation="vertical" decorative />

        {/* Block elements */}
        <ToolbarButton
          icon="ri-double-quotes-l"
          tooltip={t(labels, 'blockquote')}
          active={editor.isActive('blockquote')}
          onClick={() => editor.chain().focus().toggleBlockquote().run()}
        />
        <ToolbarButton
          icon="ri-code-s-slash-line"
          tooltip={t(labels, 'codeBlock')}
          active={editor.isActive('codeBlock')}
          onClick={() => editor.chain().focus().toggleCodeBlock().run()}
        />

        <Separator.Root className="tlWysiwygToolbar__sep" orientation="vertical" decorative />

        {/* Link, Image, Table */}
        <LinkPopover editor={editor} labels={labels} />
        <ToolbarButton
          icon="ri-image-line"
          tooltip={t(labels, 'image')}
          onClick={onImageUpload}
        />
        <ToolbarButton
          icon="ri-table-line"
          tooltip={t(labels, 'table')}
          onClick={() => editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()}
        />

        <Separator.Root className="tlWysiwygToolbar__sep" orientation="vertical" decorative />

        {/* History */}
        <ToolbarButton
          icon="ri-arrow-go-back-line"
          tooltip={t(labels, 'undo')}
          disabled={!editor.can().undo()}
          onClick={() => editor.chain().focus().undo().run()}
        />
        <ToolbarButton
          icon="ri-arrow-go-forward-line"
          tooltip={t(labels, 'redo')}
          disabled={!editor.can().redo()}
          onClick={() => editor.chain().focus().redo().run()}
        />
      </div>
    </Tooltip.Provider>
  );
};

export default WysiwygToolbar;
