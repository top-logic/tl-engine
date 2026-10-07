import { React, useI18N, TLChild, tooltipProps, anchoredOverlayProps } from 'tl-react-bridge';
import { useEditorState } from '@tiptap/react';
import type { Editor, EditorStateSnapshot } from '@tiptap/react';
import * as DropdownMenu from '@radix-ui/react-dropdown-menu';
import * as Popover from '@radix-ui/react-popover';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface ToolbarProps {
  editor: Editor | null;
  onImageUpload: () => void;

  /**
   * The toolbar of the commands the editor is configured with, as the server describes it, or
   * null where the editor carries none.
   */
  toolbar: unknown;
}

/** The formatting at the cursor and the history, as far as the toolbar shows them. */
interface ToolbarState {
  bold: boolean;
  italic: boolean;
  underline: boolean;
  strike: boolean;
  blockquote: boolean;
  codeBlock: boolean;

  /** The level of the heading at the cursor, or null in a paragraph. */
  headingLevel: number | null;

  bulletList: boolean;
  orderedList: boolean;
  link: boolean;
  canUndo: boolean;
  canRedo: boolean;
}

/** The levels a heading can have. */
const HEADING_LEVELS = [1, 2, 3, 4, 5, 6] as const;

/**
 * Reads the toolbar state from the editor. The toolbar renders from this selection only, so it
 * re-renders exactly when one of its flags changes, not on every transaction of the editor.
 */
function selectToolbarState({ editor }: EditorStateSnapshot<Editor | null>): ToolbarState | null {
  if (!editor) {
    return null;
  }
  return {
    bold: editor.isActive('bold'),
    italic: editor.isActive('italic'),
    underline: editor.isActive('underline'),
    strike: editor.isActive('strike'),
    blockquote: editor.isActive('blockquote'),
    codeBlock: editor.isActive('codeBlock'),
    headingLevel: HEADING_LEVELS.find((level) => editor.isActive('heading', { level })) ?? null,
    bulletList: editor.isActive('bulletList'),
    orderedList: editor.isActive('orderedList'),
    link: editor.isActive('link'),
    canUndo: editor.can().undo(),
    canRedo: editor.can().redo(),
  };
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

/**
 * Handler for the closing of a toolbar popup that puts the caret back into the text at its
 * previous selection, instead of onto the button that opened the popup.
 */
function useFocusEditorOnClose(editor: Editor): (e: Event) => void {
  return React.useCallback((e: Event) => {
    e.preventDefault();
    editor.commands.focus();
  }, [editor]);
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
    <button
      type="button"
      className={'tlWysiwygToolbar__btn' + (active ? ' tlWysiwygToolbar__btn--active' : '')}
      disabled={disabled}
      onMouseDown={(e: React.MouseEvent) => {
        e.preventDefault();
        onClick();
      }}
      aria-label={tooltip}
      {...tooltipProps(tooltip)}
    >
      <i className={icon} />
    </button>
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

const HeadingDropdown: React.FC<{
  editor: Editor;
  labels: Record<string, string>;
  activeLevel: number | null;
}> = ({ editor, labels, activeLevel }) => {
  const levels: HeadingLevel[] = [
    { label: t(labels, 'paragraph'), level: null, icon: 'ri-paragraph' },
    { label: t(labels, 'heading1'), level: 1, icon: 'ri-h-1' },
    { label: t(labels, 'heading2'), level: 2, icon: 'ri-h-2' },
    { label: t(labels, 'heading3'), level: 3, icon: 'ri-h-3' },
    { label: t(labels, 'heading4'), level: 4, icon: 'ri-h-4' },
    { label: t(labels, 'heading5'), level: 5, icon: 'ri-h-5' },
    { label: t(labels, 'heading6'), level: 6, icon: 'ri-h-6' },
  ];

  const focusEditor = useFocusEditorOnClose(editor);
  const currentIcon = activeLevel === null ? 'ri-paragraph' : 'ri-h-' + activeLevel;
  const currentLabel = activeLevel === null ? t(labels, 'paragraph') : t(labels, 'heading' + activeLevel);

  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger asChild>
        <button type="button" className="tlWysiwygToolbar__btn tlWysiwygToolbar__btn--dropdown"
          aria-label={t(labels, 'heading')} {...tooltipProps(currentLabel)}>
          <i className={currentIcon} />
          <i className="ri-arrow-down-s-line tlWysiwygToolbar__chevron" />
        </button>
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content
          className="tlWysiwygToolbar__dropdown"
          sideOffset={4}
          align="start"
          onCloseAutoFocus={focusEditor}
          {...anchoredOverlayProps}
        >
          {levels.map((h) => {
            const isActive = h.level === activeLevel;
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

const ListDropdown: React.FC<{
  editor: Editor;
  labels: Record<string, string>;
  isBullet: boolean;
  isOrdered: boolean;
}> = ({ editor, labels, isBullet, isOrdered }) => {
  const focusEditor = useFocusEditorOnClose(editor);
  const currentIcon = isOrdered ? 'ri-list-ordered' : 'ri-list-unordered';
  const listsLabel = t(labels, 'lists');

  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger asChild>
        <button
          type="button"
          className={'tlWysiwygToolbar__btn tlWysiwygToolbar__btn--dropdown' + ((isBullet || isOrdered) ? ' tlWysiwygToolbar__btn--active' : '')}
          aria-label={listsLabel}
          {...tooltipProps(listsLabel)}
        >
          <i className={currentIcon} />
          <i className="ri-arrow-down-s-line tlWysiwygToolbar__chevron" />
        </button>
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content
          className="tlWysiwygToolbar__dropdown"
          sideOffset={4}
          align="start"
          onCloseAutoFocus={focusEditor}
          {...anchoredOverlayProps}
        >
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

const LinkPopover: React.FC<{
  editor: Editor;
  labels: Record<string, string>;
  isActive: boolean;
}> = ({ editor, labels, isActive }) => {
  const [open, setOpen] = React.useState(false);
  const [url, setUrl] = React.useState('');
  const inputRef = React.useRef<HTMLInputElement>(null);

  const linkLabel = t(labels, 'link');
  const focusEditor = useFocusEditorOnClose(editor);

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
      <Popover.Trigger asChild>
        <button
          type="button"
          className={'tlWysiwygToolbar__btn' + (isActive ? ' tlWysiwygToolbar__btn--active' : '')}
          aria-label={linkLabel}
          {...tooltipProps(linkLabel)}
        >
          <i className="ri-link" />
        </button>
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content
          className="tlWysiwygToolbar__linkPopover"
          sideOffset={6}
          align="start"
          onCloseAutoFocus={focusEditor}
          {...anchoredOverlayProps}
        >
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

const WysiwygToolbar: React.FC<ToolbarProps> = ({ editor, onImageUpload, toolbar }) => {
  const labels = useI18N(ALL_I18N_KEYS);
  const state = useEditorState({ editor, selector: selectToolbarState });

  if (!editor || !state) return null;

  return (
    <div className="tlWysiwygToolbar" role="toolbar" aria-label="Editor toolbar">
      <div className="tlWysiwygToolbar__groups">
        {/* Text formatting */}
        <div className="tlWysiwygToolbar__group">
          <ToolbarButton
            icon="ri-bold"
            tooltip={t(labels, 'bold')}
            active={state.bold}
            onClick={() => editor.chain().focus().toggleBold().run()}
          />
          <ToolbarButton
            icon="ri-italic"
            tooltip={t(labels, 'italic')}
            active={state.italic}
            onClick={() => editor.chain().focus().toggleItalic().run()}
          />
          <ToolbarButton
            icon="ri-underline"
            tooltip={t(labels, 'underline')}
            active={state.underline}
            onClick={() => editor.chain().focus().toggleUnderline().run()}
          />
          <ToolbarButton
            icon="ri-strikethrough"
            tooltip={t(labels, 'strikethrough')}
            active={state.strike}
            onClick={() => editor.chain().focus().toggleStrike().run()}
          />
        </div>

        {/* Heading and list dropdowns */}
        <div className="tlWysiwygToolbar__group">
          <HeadingDropdown editor={editor} labels={labels} activeLevel={state.headingLevel} />
          <ListDropdown
            editor={editor}
            labels={labels}
            isBullet={state.bulletList}
            isOrdered={state.orderedList}
          />
        </div>

        {/* Block elements */}
        <div className="tlWysiwygToolbar__group">
          <ToolbarButton
            icon="ri-double-quotes-l"
            tooltip={t(labels, 'blockquote')}
            active={state.blockquote}
            onClick={() => editor.chain().focus().toggleBlockquote().run()}
          />
          <ToolbarButton
            icon="ri-code-s-slash-line"
            tooltip={t(labels, 'codeBlock')}
            active={state.codeBlock}
            onClick={() => editor.chain().focus().toggleCodeBlock().run()}
          />
        </div>

        {/* Link, Image, Table */}
        <div className="tlWysiwygToolbar__group">
          <LinkPopover editor={editor} labels={labels} isActive={state.link} />
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
        </div>

        {/* The commands the editor is configured with, in a toolbar of their own. */}
        {toolbar != null && (
          <div className="tlWysiwygToolbar__group tlWysiwygToolbar__group--commands">
            <TLChild control={toolbar} />
          </div>
        )}

        {/* History */}
        <div className="tlWysiwygToolbar__group">
          <ToolbarButton
            icon="ri-arrow-go-back-line"
            tooltip={t(labels, 'undo')}
            disabled={!state.canUndo}
            onClick={() => editor.chain().focus().undo().run()}
          />
          <ToolbarButton
            icon="ri-arrow-go-forward-line"
            tooltip={t(labels, 'redo')}
            disabled={!state.canRedo}
            onClick={() => editor.chain().focus().redo().run()}
          />
        </div>
      </div>
    </div>
  );
};

export default WysiwygToolbar;
