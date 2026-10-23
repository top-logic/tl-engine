import {
  React, useI18N, TLChild, usePart, ButtonDefaults, useButtonDefaults, Menu, usePopover,
  useCloseOnOutsidePress, useFocusTrap, useStandaloneKeyboardScope, pressClosedSurface, createPortal,
  anchoredOverlayProps,
  ICON_BOLD, ICON_ITALIC, ICON_UNDERLINE, ICON_STRIKETHROUGH, ICON_PARAGRAPH, ICON_HEADING, ICON_BULLET_LIST,
  ICON_NUMBERED_LIST, ICON_QUOTE, ICON_CODE_BLOCK, ICON_LINK, ICON_IMAGE, ICON_TABLE, ICON_UNDO, ICON_REDO, ICON_SOURCE,
} from 'tl-react-bridge';
import { useEditorState } from '@tiptap/react';
import type { Editor, EditorStateSnapshot } from '@tiptap/react';

const { useCallback, useEffect, useId, useRef, useState } = React;

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface ToolbarProps {
  editor: Editor | null;

  /** Starts uploading an image, undefined where the editor offers no image upload. */
  onImageUpload?: () => void;

  /** Whether the HTML source is shown in place of the formatted text. */
  sourceMode: boolean;

  /**
   * Switches between the formatted text and its HTML source, undefined where the editor offers no
   * source editing.
   */
  onToggleSource?: () => void;

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
  headingLevel: HeadingLevel | null;

  bulletList: boolean;
  orderedList: boolean;
  link: boolean;
  canUndo: boolean;
  canRedo: boolean;
}

/** The levels a heading can have. */
const HEADING_LEVELS = [1, 2, 3, 4, 5, 6] as const;

/** The level of a heading. */
type HeadingLevel = typeof HEADING_LEVELS[number];

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
// I18N
// ---------------------------------------------------------------------------

/** The texts of the toolbar, with their English fallbacks; the server translates them. */
const I18N_KEYS: Record<string, string> = {
  'js.wysiwyg.toolbar': 'Editor toolbar',
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
  'js.wysiwyg.bulletList': 'Bullet list',
  'js.wysiwyg.orderedList': 'Numbered list',
  'js.wysiwyg.lists': 'Lists',
  'js.wysiwyg.blockquote': 'Blockquote',
  'js.wysiwyg.link': 'Link',
  'js.wysiwyg.linkUrl': 'URL',
  'js.wysiwyg.linkApply': 'Apply',
  'js.wysiwyg.linkRemove': 'Remove link',
  'js.wysiwyg.image': 'Image',
  'js.wysiwyg.table': 'Table',
  'js.wysiwyg.codeBlock': 'Code block',
  'js.wysiwyg.undo': 'Undo',
  'js.wysiwyg.redo': 'Redo',
  'js.wysiwyg.source': 'Source code',
};

/** The translated texts of the toolbar. */
type Labels = Record<string, string>;

/** The translated text of the toolbar under the given name (the key without its prefix). */
function t(labels: Labels, key: string): string {
  return labels['js.wysiwyg.' + key];
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Keeps the focus - and with it the selection - in the text when a button of the toolbar is
 * pressed with the mouse. The keyboard reaches the buttons anyway: Enter and Space click the
 * focused button.
 */
function keepFocus(event: React.MouseEvent) {
  event.preventDefault();
}

/**
 * The return target of the surfaces of the toolbar (popover.contract.md, rich-text.contract.md):
 * when a menu or the link popup closes, the focus goes back into the text at its selection, not
 * to the button that opened the surface, so that writing goes on where it stopped.
 *
 * <p>The focus trap of the surface gives the focus back to where it was when the surface opened;
 * this hook then moves it on into the text - only from there, not from a field the user has
 * pressed in meanwhile. Its cleanup runs after the one of the trap: the trap belongs to a child
 * (the menu), whose cleanup runs first, or it is a hook called before this one.</p>
 *
 * @returns The function to call right before the surface opens, noting where the focus is.
 */
function useReturnToText(open: boolean, editor: Editor): () => void {
  const originRef = useRef<Element | null>(null);
  useEffect(() => {
    if (!open) {
      return;
    }
    return () => {
      const current = document.activeElement;
      const given = current === originRef.current || current === null || current === document.body;
      originRef.current = null;
      if (given && !editor.isDestroyed) {
        editor.view.focus();
      }
    };
  }, [open, editor]);
  return useCallback(() => {
    originRef.current = document.activeElement;
  }, []);
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------

interface ToolButtonProps {
  icon: string;
  label: string;

  /** The pressed state of a toggle; absent for a button that is no toggle. */
  checked?: boolean;

  disabled?: boolean;
  onPress: () => void;
}

/**
 * A button of the toolbar: the part `Button` in the appearance the toolbar gives its buttons
 * (ghost, icon alone), drawn small like every button of the toolbar: the toolbar is part of a field.
 * A toggle reports whether it is pressed (`aria-pressed`).
 */
const ToolButton: React.FC<ToolButtonProps> = ({ icon, label, checked, disabled, onPress }) => {
  const Button = usePart('Button');
  const defaults = useButtonDefaults();
  return (
    <Button
      image={icon}
      label={label}
      display={defaults.iconOnly === false ? 'icon-label' : 'icon-only'}
      appearance={defaults.appearance ?? 'ghost'}
      small
      checked={checked}
      disabled={disabled}
      onMouseDown={keepFocus}
      onClick={onPress}
    />
  );
};

/**
 * An alternative of a choice the toolbar offers in a menu. Applying it leaves the focus alone: the
 * menu gives it back to the text when it closes (useReturnToText), at once - a second, deferred
 * focus of the editor would pull it back from where the user has gone meanwhile.
 */
interface Choice {
  label: string;

  /** The icon of the entry; absent: the entry shows its label alone. */
  icon?: string;

  /** Whether the alternative is the one at the cursor. */
  checked: boolean;

  /** Applies the alternative to the text. */
  apply: () => void;
}

interface ChoiceEntryProps extends Omit<Choice, 'apply'> {
  onSelect: () => void;
}

/**
 * An entry of a menu of the toolbar: the part `Button` as entry of the menu. Under the defaults of
 * a choice (`ButtonDefaults` `radio`) it is a `menuitemradio`, otherwise a `menuitemcheckbox`;
 * `aria-checked` is the given state either way.
 */
const ChoiceEntry: React.FC<ChoiceEntryProps> = ({ label, icon, checked, onSelect }) => {
  const Button = usePart('Button');
  const defaults = useButtonDefaults();
  return (
    <Button
      image={icon}
      label={label}
      display={icon ? 'icon-label' : 'label-only'}
      appearance={defaults.appearance ?? 'menu-item'}
      radio={defaults.radio}
      checked={checked}
      onClick={onSelect}
    />
  );
};

// ---------------------------------------------------------------------------
// Menus
// ---------------------------------------------------------------------------

interface ChoiceMenuProps {
  editor: Editor;

  /** The name of the trigger. */
  label: string;

  /** The icon of the trigger: the one of the alternative at the cursor. */
  icon: string;

  /** The alternatives, one entry each; at most one is checked. */
  choices: Choice[];

  /**
   * Whether the entries are the alternatives of a choice that always holds one of them (radio
   * entries), or toggles that may all be off, choosing the checked one switching it off (checkbox
   * entries).
   */
  radio: boolean;
}

/**
 * A menu of the toolbar offering the alternatives of one choice (paragraph format, list kind): the
 * part `MenuTrigger` opening the menu of the bridge, whose entries are radio entries or checkbox
 * entries ({@link ChoiceMenuProps.radio}). The menu closes after an entry is chosen; the focus goes
 * back into the text.
 */
const ChoiceMenu: React.FC<ChoiceMenuProps> = ({ editor, label, icon, choices, radio }) => {
  const MenuTrigger = usePart('MenuTrigger');
  const defaults = useButtonDefaults();
  const [open, setOpen] = useState(false);
  const [anchor, setAnchor] = useState<HTMLButtonElement | null>(null);
  const close = useCallback(() => setOpen(false), []);
  const noteOrigin = useReturnToText(open, editor);

  const toggle = useCallback(() => {
    // The press of this click has closed the menu: leave it closed.
    if (pressClosedSurface()) {
      return;
    }
    if (!open) {
      noteOrigin();
    }
    setOpen(!open);
  }, [open, noteOrigin]);

  return (
    <>
      <MenuTrigger
        ref={setAnchor}
        appearance={defaults.appearance ?? 'ghost'}
        expanded={open}
        label={label}
        icon={icon}
        small
        onMouseDown={keepFocus}
        onClick={toggle}
      />
      <Menu open={open} anchor={anchor} onClose={close}>
        <ButtonDefaults appearance="menu-item" radio={radio} iconOnly={false}>
          {choices.map(choice => (
            <ChoiceEntry
              key={choice.label}
              label={choice.label}
              icon={choice.icon}
              checked={choice.checked}
              onSelect={() => {
                choice.apply();
                close();
              }}
            />
          ))}
        </ButtonDefaults>
      </Menu>
    </>
  );
};

// ---------------------------------------------------------------------------
// Link popup
// ---------------------------------------------------------------------------

/**
 * The button of the toolbar opening the link popup, and the popup: a content popover with the
 * address of the link in a field and the buttons applying and - on a link - removing it. Enter in
 * the field applies, Escape and a press outside close. The focus goes back into the text.
 */
const LinkPopup: React.FC<{ editor: Editor; labels: Labels; isLink: boolean }> = ({ editor, labels, isLink }) => {
  const MenuTrigger = usePart('MenuTrigger');
  const Button = usePart('Button');
  const TextInput = usePart('TextInput');
  const defaults = useButtonDefaults();
  const fieldId = useId();
  const [open, setOpen] = useState(false);
  const [url, setUrl] = useState('');

  // Trigger and surface each carry an own ref and the one of the popover: plain callbacks, as in the
  // menu of the bridge, so that the refs type-check under the React types of this module.
  const triggerRef = useRef<HTMLButtonElement | null>(null);
  const [anchor, setAnchor] = useState<HTMLButtonElement | null>(null);
  const setTriggerRefs = useCallback((element: HTMLButtonElement | null) => {
    triggerRef.current = element;
    setAnchor(element);
  }, []);
  const surfaceRef = useRef<HTMLDivElement | null>(null);
  const { setFloating, style } = usePopover({ open, anchor });
  const setSurfaceRefs = useCallback((element: HTMLDivElement | null) => {
    surfaceRef.current = element;
    setFloating(element);
  }, [setFloating]);

  const close = useCallback(() => setOpen(false), []);
  useStandaloneKeyboardScope(open, { ESCAPE: close });
  useCloseOnOutsidePress(open, [triggerRef, surfaceRef], close);
  useFocusTrap(open, surfaceRef, 'field');
  // After the trap: its cleanup runs first.
  const noteOrigin = useReturnToText(open, editor);

  const toggle = useCallback(() => {
    if (open) {
      close();
      return;
    }
    // Editing a link starts from its address.
    setUrl(editor.getAttributes('link').href || '');
    noteOrigin();
    setOpen(true);
  }, [open, close, editor, noteOrigin]);

  const href = url.trim();

  // The focus goes back to the text when the popup closes (useReturnToText).
  const apply = useCallback(() => {
    if (href) {
      editor.chain().setLink({ href }).run();
    }
    close();
  }, [editor, href, close]);

  const remove = useCallback(() => {
    editor.chain().unsetLink().run();
    close();
  }, [editor, close]);

  const handleKeyDown = useCallback((event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      apply();
    }
  }, [apply]);

  const linkLabel = t(labels, 'link');

  return (
    <>
      <MenuTrigger
        ref={setTriggerRefs}
        appearance={defaults.appearance ?? 'ghost'}
        expanded={open}
        haspopup="dialog"
        label={linkLabel}
        icon={ICON_LINK}
        small
        onMouseDown={keepFocus}
        onClick={toggle}
      />
      {open && createPortal(
        <div
          ref={setSurfaceRefs}
          className="tl-popover tl-popover--content"
          role="dialog"
          aria-label={linkLabel}
          style={style}
          {...anchoredOverlayProps}
        >
          <div className="tl-inset tl-inset--panel">
            <div className="tl-stack tl-stack--column tl-stack--gap-compact">
              <label htmlFor={fieldId} className="tl-type-label">{t(labels, 'linkUrl')}</label>
              <TextInput
                id={fieldId}
                type="url"
                value={url}
                placeholder="https://..."
                onChange={setUrl}
                onKeyDown={handleKeyDown}
              />
              <div className="tl-stack tl-stack--row tl-stack--gap-compact tl-stack--justify-end">
                <Button
                  label={t(labels, 'linkApply')}
                  display="label-only"
                  appearance="primary"
                  disabled={!href}
                  onClick={apply}
                />
                {isLink && (
                  <Button
                    label={t(labels, 'linkRemove')}
                    display="label-only"
                    appearance="secondary"
                    danger
                    onClick={remove}
                  />
                )}
              </div>
            </div>
          </div>
        </div>,
        document.body,
      )}
    </>
  );
};

// ---------------------------------------------------------------------------
// Main Toolbar
// ---------------------------------------------------------------------------

/**
 * The toolbar of the rich text editor (rich-text.contract.md): a wrapping `tl-toolbar` whose
 * groups are divided by vertical dividers. Its buttons are the part `Button`, ghost, icon alone and
 * small, as are its menu triggers; the paragraph format is a menu of radio entries, the list kind a menu of checkbox entries
 * (the checked one switches the list off), the link a popup. The commands the editor is configured
 * with form a toolbar of their own inside. Where the editor offers source editing, a toggle shows the
 * HTML source in place of the formatted text; while it is shown, the toggle is the only button.
 */
const WysiwygToolbar: React.FC<ToolbarProps> = ({ editor, onImageUpload, sourceMode, onToggleSource, toolbar }) => {
  const labels = useI18N(I18N_KEYS);
  const state = useEditorState({ editor, selector: selectToolbarState });
  const Divider = usePart('Divider');

  if (!editor || !state) return null;

  const sourceToggle = onToggleSource && (
    <ToolButton icon={ICON_SOURCE} label={t(labels, 'source')} checked={sourceMode} onPress={onToggleSource} />
  );

  if (sourceMode) {
    // The formatting buttons act on the formatted text, which is not shown.
    return (
      <ButtonDefaults appearance="ghost" iconOnly>
        <div className="tl-toolbar tl-toolbar--wrap" role="toolbar" aria-label={t(labels, 'toolbar')}>
          <div className="tl-toolbar__group">{sourceToggle}</div>
        </div>
      </ButtonDefaults>
    );
  }

  const headingLevel = state.headingLevel;
  const groups: React.ReactNode[] = [
    // Text formatting
    <>
      <ToolButton icon={ICON_BOLD} label={t(labels, 'bold')} checked={state.bold}
        onPress={() => editor.chain().focus().toggleBold().run()} />
      <ToolButton icon={ICON_ITALIC} label={t(labels, 'italic')} checked={state.italic}
        onPress={() => editor.chain().focus().toggleItalic().run()} />
      <ToolButton icon={ICON_UNDERLINE} label={t(labels, 'underline')} checked={state.underline}
        onPress={() => editor.chain().focus().toggleUnderline().run()} />
      <ToolButton icon={ICON_STRIKETHROUGH} label={t(labels, 'strikethrough')} checked={state.strike}
        onPress={() => editor.chain().focus().toggleStrike().run()} />
    </>,

    // Paragraph format and list kind
    <>
      <ChoiceMenu
        editor={editor}
        label={t(labels, 'heading')}
        icon={headingLevel === null ? ICON_PARAGRAPH : ICON_HEADING}
        radio
        choices={[
          {
            label: t(labels, 'paragraph'),
            checked: headingLevel === null,
            apply: () => editor.chain().setParagraph().run(),
          },
          ...HEADING_LEVELS.map(level => ({
            label: t(labels, 'heading' + level),
            checked: headingLevel === level,
            // Choosing the level in force keeps it: a radio entry is no toggle.
            apply: () => editor.chain().setHeading({ level }).run(),
          })),
        ]}
      />
      <ChoiceMenu
        editor={editor}
        label={t(labels, 'lists')}
        icon={state.orderedList ? ICON_NUMBERED_LIST : ICON_BULLET_LIST}
        radio={false}
        choices={[
          {
            label: t(labels, 'bulletList'),
            icon: ICON_BULLET_LIST,
            checked: state.bulletList,
            apply: () => editor.chain().toggleBulletList().run(),
          },
          {
            label: t(labels, 'orderedList'),
            icon: ICON_NUMBERED_LIST,
            checked: state.orderedList,
            apply: () => editor.chain().toggleOrderedList().run(),
          },
        ]}
      />
    </>,

    // Block elements
    <>
      <ToolButton icon={ICON_QUOTE} label={t(labels, 'blockquote')} checked={state.blockquote}
        onPress={() => editor.chain().focus().toggleBlockquote().run()} />
      <ToolButton icon={ICON_CODE_BLOCK} label={t(labels, 'codeBlock')} checked={state.codeBlock}
        onPress={() => editor.chain().focus().toggleCodeBlock().run()} />
    </>,

    // Link, image, table
    <>
      <LinkPopup editor={editor} labels={labels} isLink={state.link} />
      {onImageUpload && <ToolButton icon={ICON_IMAGE} label={t(labels, 'image')} onPress={onImageUpload} />}
      <ToolButton icon={ICON_TABLE} label={t(labels, 'table')}
        onPress={() => editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()} />
    </>,
  ];

  // The commands the editor is configured with, in a toolbar of their own.
  if (toolbar != null) {
    groups.push(<TLChild control={toolbar} />);
  }

  if (sourceToggle) {
    groups.push(sourceToggle);
  }

  // History
  groups.push(
    <>
      <ToolButton icon={ICON_UNDO} label={t(labels, 'undo')} disabled={!state.canUndo}
        onPress={() => editor.chain().focus().undo().run()} />
      <ToolButton icon={ICON_REDO} label={t(labels, 'redo')} disabled={!state.canRedo}
        onPress={() => editor.chain().focus().redo().run()} />
    </>,
  );

  return (
    <ButtonDefaults appearance="ghost" iconOnly>
      <div className="tl-toolbar tl-toolbar--wrap" role="toolbar" aria-label={t(labels, 'toolbar')}>
        {groups.map((group, index) => (
          <React.Fragment key={index}>
            {index > 0 && <Divider orientation="vertical" />}
            <div className="tl-toolbar__group">{group}</div>
          </React.Fragment>
        ))}
      </div>
    </ButtonDefaults>
  );
};

export default WysiwygToolbar;
