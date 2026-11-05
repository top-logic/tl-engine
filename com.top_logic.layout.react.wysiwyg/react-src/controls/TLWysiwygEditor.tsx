import {
  React, CMD_COMMIT, useTLState, useTLCommand, useTLUpload, useTLFieldValue, useUncontrolledFieldSync,
  rootClassName, fieldInputId, useFieldLabelProps,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useEditor, EditorContent } from '@tiptap/react';
import type { Editor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import type { ImageOptions } from '@tiptap/extension-image';
import { Table, TableRow, TableCell, TableHeader } from '@tiptap/extension-table';
import { TextStyle, Color } from '@tiptap/extension-text-style';
import WysiwygToolbar from './WysiwygToolbar';
import { HtmlPassthrough, restoreHtmlSources } from './HtmlPassthrough';
import {
  ObjectMention, MentionList, useMentions, mentionConfigs, CMD_MENTION, ARG_TRIGGER, ARG_QUERY, STATE_MENTIONS,
  STATE_MENTION_OPTIONS,
} from './mentions';
import { FollowObjectLinks, ObjectLink, objectLinkAt } from './ObjectLink';
import { IMAGE_MIME_TYPES, imageFiles } from './ImageFiles';
import type { MentionAnswer } from './mentions';
import './TLWysiwygEditor.css';

/** Command sent when the user follows an object link in displayed content. */
const CMD_SHOW_OBJECT_LINK = 'showObjectLink';

/** The CMD_SHOW_OBJECT_LINK argument naming the object to display. */
const ARG_HREF = 'href';

/** State holding the toolbar of the commands the editor was configured with. */
const STATE_TOOLBAR = 'toolbar';

/** State asking for markup to be inserted at the cursor. */
const STATE_INSERT = 'insert';

/** State telling whether the editor offers uploading images, absent for an editor that does. */
const STATE_IMAGES = 'images';

/** The multipart field name of an uploaded image, see `UploadSupport#FILE_PART`. */
const FILE_PART = 'file';

/** The types the file chooser for an image offers. */
const IMAGE_ACCEPT = IMAGE_MIME_TYPES.join(',');

/** State telling whether the editor offers switching to the HTML source of the text. */
const STATE_SOURCE_EDITING = 'sourceEditing';

/**
 * How an image is resized in the editor: by the handles at its corners, keeping its proportions,
 * down to a width still to be grabbed. The size reached is stored as width and height of the image.
 */
const IMAGE_RESIZE: ImageOptions['resize'] = {
  enabled: true,
  directions: ['top-left', 'top-right', 'bottom-left', 'bottom-right'],
  minWidth: 48,
  alwaysPreserveAspectRatio: true,
};

/** Options of the command asking for the objects of a mention: it reads nothing but its arguments. */
const MENTION_OPTIONS = { flushFields: false } as const;

/** The class of the design system for text from the server: the writing surface, and the view. */
const PROSE_CLASS = 'tl-prose';

/** The classes of the frame of the editor: a field of the design system, holding toolbar and text. */
const FRAME_CLASS = 'tl-field tl-rich-text';

/** The classes of the HTML source, shown in place of the writing surface: monospaced, inside the frame. */
const SOURCE_CLASS = 'tl-rich-text__source tl-type-code';

/** What the server asks to be inserted at the cursor. */
interface InsertRequest {
  /** Counts the insertions, so that inserting the same markup twice is two requests. */
  seq: number;

  /** The markup to insert. */
  html: string;
}

/** The text of the given editor, with the markup the editor keeps without understanding it. */
function editorHtml(editor: Editor): string {
  return restoreHtmlSources(editor.getHTML());
}

/**
 * Replaces the content of the given editor by the given text, without reporting it as an edit. The
 * selection stays at its positions, as far as the new text reaches.
 */
function replaceContent(editor: Editor, html: string): void {
  if (editorHtml(editor) === html) {
    return;
  }
  const { from, to } = editor.state.selection;
  editor.commands.setContent(html, { emitUpdate: false });
  const size = editor.state.doc.content.size;
  editor.commands.setTextSelection({ from: Math.min(from, size), to: Math.min(to, size) });
}

/** A selection of a text area. */
interface TextSelection {
  start: number;
  end: number;
}

const TLWysiwygEditor: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const [fieldValue, setFieldValue] = useTLFieldValue();
  const uploadFile = useTLUpload();

  // Whether the HTML source is shown in place of the formatted text.
  const [sourceMode, setSourceMode] = React.useState(false);

  const value: string = (fieldValue as string) || '';
  const editable: boolean = state.editable !== false;
  const hasError: boolean = !!state.hasError;

  // The editable area is the element a form field's label names and focuses: the formatted text,
  // or the source while it is shown. The attributes of the formatted text reach the editor through
  // the editor options: given when the editor is created, and handed to it again by the effect below
  // whenever they change, so that a change of the label association or of the error updates the
  // element without re-creating the editor. The area is the writing surface of the library
  // (.ProseMirror, a textbox); the text in it is prose of the design system. An error is announced
  // at the textbox, which assistive technology reads; the frame shows it.
  const inputId = fieldInputId(controlId);
  const labelledBy = useFieldLabelProps(controlId, inputId)['aria-labelledby'];

  // A mention is detected only while the text is edited. The objects of a mention are asked for
  // with the trigger and the text typed after it alone.
  const mentionsValue: unknown = state[STATE_MENTIONS];
  const mentionList = React.useMemo(
    () => editable ? mentionConfigs(mentionsValue) : [],
    [editable, mentionsValue]);
  const mentions = useMentions({
    id: inputId,
    mentions: mentionList,
    answer: (state[STATE_MENTION_OPTIONS] as MentionAnswer | undefined) || null,
    request: (trigger, query) => {
      void sendCommand(CMD_MENTION, { [ARG_TRIGGER]: trigger, [ARG_QUERY]: query }, MENTION_OPTIONS);
    },
  });
  const mentionAttributes = mentions.ariaAttributes;
  const dismissMention = mentions.dismiss;

  const editorProps = React.useMemo(() => {
    const attributes: Record<string, string> = sourceMode
      ? { class: PROSE_CLASS }
      : { id: inputId, class: PROSE_CLASS, ...mentionAttributes };
    if (labelledBy !== undefined) {
      attributes['aria-labelledby'] = labelledBy;
    }
    if (hasError) {
      attributes['aria-invalid'] = 'true';
    }
    return { attributes };
  }, [inputId, labelledBy, hasError, sourceMode, mentionAttributes]);

  const commitEdits: boolean = state.commitEdits === true;
  const toolbar: unknown = state[STATE_TOOLBAR] || null;
  const insertRequest: InsertRequest | null = (state[STATE_INSERT] as InsertRequest) || null;
  const images: boolean = state[STATE_IMAGES] !== false;
  const sourceEditing: boolean = state[STATE_SOURCE_EDITING] === true;

  // The source shown while the source is edited.
  const [source, setSource] = React.useState(value);

  const sourceRef = React.useRef<HTMLTextAreaElement>(null);

  /** The selection of the source to restore after a value of the server replaced the source. */
  const sourceSelectionRef = React.useRef<TextSelection | null>(null);

  /** The editor, for showing a value of the server: the editor is created after the synchronization. */
  const editorRef = React.useRef<Editor | null>(null);

  // A value of the server replaces the formatted text and the source alike, whichever is shown and
  // whether it has the focus or not.
  const sync = useUncontrolledFieldSync({
    value,
    send: setFieldValue,
    apply: (html: string) => {
      const ed = editorRef.current;
      if (ed && !ed.isDestroyed) {
        replaceContent(ed, html);
      }
      if (html === source) {
        return;
      }
      const area = sourceRef.current;
      if (area !== null) {
        sourceSelectionRef.current = { start: area.selectionStart, end: area.selectionEnd };
      }
      setSource(html);
    },
  });

  const dirtyRef = React.useRef(false);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  /** The sequence number of the insertion carried out last, so that none is carried out twice. */
  const insertedSeqRef = React.useRef(0);

  // Leads to the object an object link names, in displayed content and, with Ctrl (Cmd) held, in
  // the edited text. The editor reaches it through a reference, as it is created once.
  const followLink = React.useCallback((href: string) => {
    sendCommand(CMD_SHOW_OBJECT_LINK, { [ARG_HREF]: href });
  }, [sendCommand]);
  const followLinkRef = React.useRef(followLink);
  followLinkRef.current = followLink;

  // Uploads image files; the server answers with the request to insert them at the cursor. The
  // editor reaches it through a reference, as it is created once.
  const uploadImages = React.useCallback((files: File[]) => {
    if (files.length === 0) {
      return;
    }
    const formData = new FormData();
    for (const file of files) {
      formData.append(FILE_PART, file, file.name);
    }
    void uploadFile(formData);
  }, [uploadFile]);
  const uploadImagesRef = React.useRef(uploadImages);
  uploadImagesRef.current = uploadImages;

  const editor = useEditor({
    extensions: [
      // First, so that an element of unknown type encloses the formatting of its content.
      HtmlPassthrough,
      StarterKit.configure({
        // The link is the one writing object links as they are stored, see below.
        link: false,
        // Opening existing content must not change its stored markup, so no empty paragraph is
        // appended after a trailing table or code block.
        trailingNode: false,
      }),
      // The link extension carries the CSS class of an anchor, which is what tells an object link
      // from an ordinary one, through parsing and rendering alike.
      ObjectLink.configure({ openOnClick: false }),
      FollowObjectLinks.configure({ follow: (href: string) => followLinkRef.current(href) }),
      ObjectMention,
      ...mentions.extensions,
      Image.configure({ allowBase64: true, inline: true, resize: IMAGE_RESIZE }),
      // Image files pasted or dropped are uploaded, where the editor offers images.
      ...(images ? [imageFiles((files: File[]) => uploadImagesRef.current(files))] : []),
      Table.configure({ resizable: true }),
      TableRow,
      TableCell,
      TableHeader,
      TextStyle,
      Color,
    ],
    content: value,
    editable,
    editorProps,
    onUpdate: ({ editor: ed }) => {
      dirtyRef.current = true;
      sync.edited(() => editorHtml(ed));
    },
    onBlur: () => {
      // A mention left unfinished stays as typed.
      dismissMention();
      // The text is reported when the editor is left, so that the server has it before any
      // command that follows.
      void sync.flush();
      // Commands are dispatched FIFO, so the commit runs after the value is applied
      // server-side. Only an actual edit commits: focusing and leaving does nothing.
      if (commitEdits && dirtyRef.current) {
        dirtyRef.current = false;
        sendCommand(CMD_COMMIT);
      }
    },
  }, [editable, images, mentions.extensions]);

  // The editor takes its options again on a render only while it has no dependencies; with the
  // dependency above it keeps the attributes it was created with. Changed attributes are handed to
  // it here, and the editor applies them to its element.
  React.useEffect(() => {
    if (editor && !editor.isDestroyed) {
      editor.setOptions({ editorProps });
    }
  }, [editor, editorProps]);

  // A change of the edit mode builds the editor anew, with the value at hand as its content: in the
  // render that brings the change, the editor at hand is the old one, already destroyed, and the
  // next render brings the new one - every effect below leaves a destroyed editor alone.
  editorRef.current = editor;

  // The source replaced by a value of the server keeps its selection, as far as the new text reaches.
  React.useLayoutEffect(() => {
    const selection = sourceSelectionRef.current;
    const area = sourceRef.current;
    sourceSelectionRef.current = null;
    if (selection !== null && area !== null) {
      area.setSelectionRange(Math.min(selection.start, source.length), Math.min(selection.end, source.length));
    }
  }, [source]);

  // The markup the server asks for goes in at the cursor, once per request. Reporting the text
  // it produced is what tells the server the request is answered.
  React.useEffect(() => {
    if (!editor || editor.isDestroyed || !insertRequest || insertRequest.seq <= insertedSeqRef.current) {
      return;
    }
    insertedSeqRef.current = insertRequest.seq;
    editor.chain().focus().insertContent(insertRequest.html).run();
    void sync.flush();
  }, [insertRequest, editor, sync]);

  // Switches between the formatted text and its source. Pending changes are reported first, so
  // that the other view starts from the text the server has.
  const toggleSourceMode = React.useCallback(() => {
    if (!editor) {
      return;
    }
    void sync.flush();
    if (sourceMode) {
      editor.commands.setContent(source, { emitUpdate: false });
      setSourceMode(false);
    } else {
      setSource(sync.current());
      setSourceMode(true);
    }
  }, [editor, sourceMode, source, sync]);

  const handleSourceChange = React.useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const html = e.target.value;
    setSource(html);
    dirtyRef.current = true;
    sync.edited(() => html);
  }, [sync]);

  const handleSourceBlur = React.useCallback(() => {
    void sync.flush();
    if (commitEdits && dirtyRef.current) {
      dirtyRef.current = false;
      sendCommand(CMD_COMMIT);
    }
  }, [sync, commitEdits, sendCommand]);

  const handleImageUpload = React.useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const handleFileSelected = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    uploadImages(Array.from(e.target.files ?? []));
    // Reset so the same file can be uploaded again.
    e.target.value = '';
  }, [uploadImages]);

  // Object links in displayed content lead to the object they name; every other anchor keeps
  // its normal behavior.
  const handleContentClick = React.useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    const anchor = objectLinkAt(e.currentTarget, e.nativeEvent);
    if (anchor === null) {
      return;
    }
    e.preventDefault();
    followLink(anchor.getAttribute('href') || '');
  }, [followLink]);

  // The view is the text alone, without frame.
  if (!editable) {
    return (
      <div
        id={controlId}
        className={rootClassName(state, PROSE_CLASS)}
        onClick={handleContentClick}
        dangerouslySetInnerHTML={{ __html: value }}
      />
    );
  }

  // The frame shows an error of the field (the border of tl-field); the textbox announces it.
  return (
    <div id={controlId} className={rootClassName(state, FRAME_CLASS)} aria-invalid={hasError ? true : undefined}>
      <WysiwygToolbar
        editor={editor}
        onImageUpload={images ? handleImageUpload : undefined}
        toolbar={toolbar}
        sourceMode={sourceMode}
        onToggleSource={sourceEditing ? toggleSourceMode : undefined}
      />
      {sourceMode && (
        <textarea
          ref={sourceRef}
          id={inputId}
          aria-labelledby={labelledBy}
          aria-invalid={hasError ? true : undefined}
          className={SOURCE_CLASS}
          value={source}
          spellCheck={false}
          onChange={handleSourceChange}
          onBlur={handleSourceBlur}
        />
      )}
      <EditorContent editor={editor} hidden={sourceMode} />
      {mentions.list !== null && !sourceMode && (
        <MentionList {...mentions.list} aria-labelledby={labelledBy} />
      )}
      <input
        ref={fileInputRef}
        type="file"
        accept={IMAGE_ACCEPT}
        multiple
        style={{ display: 'none' }}
        onChange={handleFileSelected}
      />
    </div>
  );
};

export default TLWysiwygEditor;
