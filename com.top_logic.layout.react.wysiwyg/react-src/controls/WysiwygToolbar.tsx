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

const I18N = 'class.com.top_logic.layout.react.wysiwyg.I18NConstants.TOOLBAR_';

const ALL_I18N_KEYS: Record<string, string> = {
  [I18N + 'BOLD']: 'Bold',
  [I18N + 'ITALIC']: 'Italic',
  [I18N + 'UNDERLINE']: 'Underline',
  [I18N + 'STRIKETHROUGH']: 'Strikethrough',
  [I18N + 'HEADING']: 'Heading',
  [I18N + 'PARAGRAPH']: 'Paragraph',
  [I18N + 'HEADING_1']: 'Heading 1',
  [I18N + 'HEADING_2']: 'Heading 2',
  [I18N + 'HEADING_3']: 'Heading 3',
  [I18N + 'HEADING_4']: 'Heading 4',
  [I18N + 'HEADING_5']: 'Heading 5',
  [I18N + 'HEADING_6']: 'Heading 6',
  [I18N + 'BULLET_LIST']: 'Bullet List',
  [I18N + 'ORDERED_LIST']: 'Numbered List',
  [I18N + 'LISTS']: 'Lists',
  [I18N + 'BLOCKQUOTE']: 'Blockquote',
  [I18N + 'LINK']: 'Link',
  [I18N + 'LINK_URL']: 'URL',
  [I18N + 'LINK_APPLY']: 'Apply',
  [I18N + 'LINK_REMOVE']: 'Remove link',
  [I18N + 'IMAGE']: 'Image',
  [I18N + 'TABLE']: 'Table',
  [I18N + 'CODE_BLOCK']: 'Code Block',
  [I18N + 'UNDO']: 'Undo',
  [I18N + 'REDO']: 'Redo',
};

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function t(labels: Record<string, string>, suffix: string): string {
  return labels[I18N + suffix] || ALL_I18N_KEYS[I18N + suffix] || suffix;
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
          className={'tlToolbar__btn' + (active ? ' tlToolbar__btn--active' : '')}
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
        <Tooltip.Content className="tlToolbar__tooltip" sideOffset={6}>
          {tooltip}
          <Tooltip.Arrow className="tlToolbar__tooltipArrow" />
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
    { label: t(labels, 'PARAGRAPH'), level: null, icon: 'ri-paragraph' },
    { label: t(labels, 'HEADING_1'), level: 1, icon: 'ri-h-1' },
    { label: t(labels, 'HEADING_2'), level: 2, icon: 'ri-h-2' },
    { label: t(labels, 'HEADING_3'), level: 3, icon: 'ri-h-3' },
    { label: t(labels, 'HEADING_4'), level: 4, icon: 'ri-h-4' },
    { label: t(labels, 'HEADING_5'), level: 5, icon: 'ri-h-5' },
    { label: t(labels, 'HEADING_6'), level: 6, icon: 'ri-h-6' },
  ];

  // Determine current heading level
  let currentIcon = 'ri-paragraph';
  let currentLabel = t(labels, 'PARAGRAPH');
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
            <button type="button" className="tlToolbar__btn tlToolbar__btn--dropdown" aria-label={t(labels, 'HEADING')}>
              <i className={currentIcon} />
              <i className="ri-arrow-down-s-line tlToolbar__chevron" />
            </button>
          </DropdownMenu.Trigger>
        </Tooltip.Trigger>
        <Tooltip.Portal>
          <Tooltip.Content className="tlToolbar__tooltip" sideOffset={6}>
            {currentLabel}
            <Tooltip.Arrow className="tlToolbar__tooltipArrow" />
          </Tooltip.Content>
        </Tooltip.Portal>
      </Tooltip.Root>
      <DropdownMenu.Portal>
        <DropdownMenu.Content className="tlToolbar__dropdown" sideOffset={4} align="start">
          {levels.map((h) => {
            const isActive = h.level === null
              ? !editor.isActive('heading')
              : editor.isActive('heading', { level: h.level });
            return (
              <DropdownMenu.Item
                key={h.level ?? 'p'}
                className={'tlToolbar__dropdownItem' + (isActive ? ' tlToolbar__dropdownItem--active' : '')}
                onSelect={() => {
                  if (h.level === null) {
                    editor.chain().focus().setParagraph().run();
                  } else {
                    editor.chain().focus().toggleHeading({ level: h.level as 1|2|3|4|5|6 }).run();
                  }
                }}
              >
                <i className={h.icon + ' tlToolbar__dropdownIcon'} />
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
              className={'tlToolbar__btn tlToolbar__btn--dropdown' + ((isBullet || isOrdered) ? ' tlToolbar__btn--active' : '')}
              aria-label={t(labels, 'LISTS')}
            >
              <i className={currentIcon} />
              <i className="ri-arrow-down-s-line tlToolbar__chevron" />
            </button>
          </DropdownMenu.Trigger>
        </Tooltip.Trigger>
        <Tooltip.Portal>
          <Tooltip.Content className="tlToolbar__tooltip" sideOffset={6}>
            {t(labels, 'LISTS')}
            <Tooltip.Arrow className="tlToolbar__tooltipArrow" />
          </Tooltip.Content>
        </Tooltip.Portal>
      </Tooltip.Root>
      <DropdownMenu.Portal>
        <DropdownMenu.Content className="tlToolbar__dropdown" sideOffset={4} align="start">
          <DropdownMenu.Item
            className={'tlToolbar__dropdownItem' + (isBullet ? ' tlToolbar__dropdownItem--active' : '')}
            onSelect={() => editor.chain().focus().toggleBulletList().run()}
          >
            <i className="ri-list-unordered tlToolbar__dropdownIcon" />
            <span>{t(labels, 'BULLET_LIST')}</span>
          </DropdownMenu.Item>
          <DropdownMenu.Item
            className={'tlToolbar__dropdownItem' + (isOrdered ? ' tlToolbar__dropdownItem--active' : '')}
            onSelect={() => editor.chain().focus().toggleOrderedList().run()}
          >
            <i className="ri-list-ordered tlToolbar__dropdownIcon" />
            <span>{t(labels, 'ORDERED_LIST')}</span>
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
              className={'tlToolbar__btn' + (isActive ? ' tlToolbar__btn--active' : '')}
              aria-label={t(labels, 'LINK')}
            >
              <i className="ri-link" />
            </button>
          </Popover.Trigger>
        </Tooltip.Trigger>
        <Tooltip.Portal>
          <Tooltip.Content className="tlToolbar__tooltip" sideOffset={6}>
            {t(labels, 'LINK')}
            <Tooltip.Arrow className="tlToolbar__tooltipArrow" />
          </Tooltip.Content>
        </Tooltip.Portal>
      </Tooltip.Root>
      <Popover.Portal>
        <Popover.Content className="tlToolbar__linkPopover" sideOffset={6} align="start">
          <div className="tlToolbar__linkForm">
            <label className="tlToolbar__linkLabel">{t(labels, 'LINK_URL')}</label>
            <input
              ref={inputRef}
              type="url"
              className="tlToolbar__linkInput"
              placeholder="https://..."
              value={url}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setUrl(e.target.value)}
              onKeyDown={handleKeyDown}
              autoFocus
            />
            <div className="tlToolbar__linkActions">
              <button
                type="button"
                className="tlToolbar__linkApply"
                onClick={applyLink}
                disabled={!url.trim()}
              >
                {t(labels, 'LINK_APPLY')}
              </button>
              {isActive && (
                <button
                  type="button"
                  className="tlToolbar__linkRemove"
                  onClick={removeLink}
                >
                  {t(labels, 'LINK_REMOVE')}
                </button>
              )}
            </div>
          </div>
          <Popover.Arrow className="tlToolbar__popoverArrow" />
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
      <div className="tlToolbar" role="toolbar" aria-label="Editor toolbar">
        {/* Text formatting */}
        <ToolbarButton
          icon="ri-bold"
          tooltip={t(labels, 'BOLD')}
          active={editor.isActive('bold')}
          onClick={() => editor.chain().focus().toggleBold().run()}
        />
        <ToolbarButton
          icon="ri-italic"
          tooltip={t(labels, 'ITALIC')}
          active={editor.isActive('italic')}
          onClick={() => editor.chain().focus().toggleItalic().run()}
        />
        <ToolbarButton
          icon="ri-underline"
          tooltip={t(labels, 'UNDERLINE')}
          active={editor.isActive('underline')}
          onClick={() => editor.chain().focus().toggleUnderline().run()}
        />
        <ToolbarButton
          icon="ri-strikethrough"
          tooltip={t(labels, 'STRIKETHROUGH')}
          active={editor.isActive('strike')}
          onClick={() => editor.chain().focus().toggleStrike().run()}
        />

        <Separator.Root className="tlToolbar__sep" orientation="vertical" decorative />

        {/* Heading dropdown */}
        <HeadingDropdown editor={editor} labels={labels} />

        {/* List dropdown */}
        <ListDropdown editor={editor} labels={labels} />

        <Separator.Root className="tlToolbar__sep" orientation="vertical" decorative />

        {/* Block elements */}
        <ToolbarButton
          icon="ri-double-quotes-l"
          tooltip={t(labels, 'BLOCKQUOTE')}
          active={editor.isActive('blockquote')}
          onClick={() => editor.chain().focus().toggleBlockquote().run()}
        />
        <ToolbarButton
          icon="ri-code-s-slash-line"
          tooltip={t(labels, 'CODE_BLOCK')}
          active={editor.isActive('codeBlock')}
          onClick={() => editor.chain().focus().toggleCodeBlock().run()}
        />

        <Separator.Root className="tlToolbar__sep" orientation="vertical" decorative />

        {/* Link, Image, Table */}
        <LinkPopover editor={editor} labels={labels} />
        <ToolbarButton
          icon="ri-image-line"
          tooltip={t(labels, 'IMAGE')}
          onClick={onImageUpload}
        />
        <ToolbarButton
          icon="ri-table-line"
          tooltip={t(labels, 'TABLE')}
          onClick={() => editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()}
        />

        <Separator.Root className="tlToolbar__sep" orientation="vertical" decorative />

        {/* History */}
        <ToolbarButton
          icon="ri-arrow-go-back-line"
          tooltip={t(labels, 'UNDO')}
          disabled={!editor.can().undo()}
          onClick={() => editor.chain().focus().undo().run()}
        />
        <ToolbarButton
          icon="ri-arrow-go-forward-line"
          tooltip={t(labels, 'REDO')}
          disabled={!editor.can().redo()}
          onClick={() => editor.chain().focus().redo().run()}
        />
      </div>
    </Tooltip.Provider>
  );
};

export default WysiwygToolbar;
