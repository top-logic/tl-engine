import {
  React, CMD_COMMIT, useTLState, useTLCommand, useTLUpload, useTLDataUrl, rootClassName, fieldInputId, useFieldLabelProps,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useEditor, EditorContent } from '@tiptap/react';
import type { Editor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import { Table, TableRow, TableCell, TableHeader } from '@tiptap/extension-table';
import { TextStyle, Color } from '@tiptap/extension-text-style';
import WysiwygToolbar from './WysiwygToolbar';
import './TLWysiwygEditor.css';

/** Command sent when the user follows an object link in displayed content. */
const CMD_SHOW_OBJECT_LINK = 'showObjectLink';

/** The CMD_SHOW_OBJECT_LINK argument naming the object to display. */
const ARG_HREF = 'href';

/** Command sent when the text of the editor changed. */
const CMD_VALUE_CHANGED = 'valueChanged';

/** The CMD_VALUE_CHANGED argument holding the text. */
const ARG_VALUE = 'value';

/** State holding the toolbar of the commands the editor was configured with. */
const STATE_TOOLBAR = 'toolbar';

/** State asking for markup to be inserted at the cursor. */
const STATE_INSERT = 'insert';

/** CSS class marking an anchor as a link to an application object. */
const TL_OBJECT = 'tlObject';

/** The class of the design system for text from the server: the writing surface, and the view. */
const PROSE_CLASS = 'tl-prose';

/** The classes of the frame of the editor: a field of the design system, holding toolbar and text. */
const FRAME_CLASS = 'tl-field tl-rich-text';

/** What the server asks to be inserted at the cursor. */
interface InsertRequest {
  /** Counts the insertions, so that inserting the same markup twice is two requests. */
  seq: number;

  /** The markup to insert. */
  html: string;
}

const TLWysiwygEditor: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const uploadFile = useTLUpload();
  const dataUrl = useTLDataUrl();

  const value: string = (state.value as string) || '';
  const editable: boolean = state.editable !== false;
  const hasError: boolean = !!state.hasError;

  // The editable area is the element a form field's label names and focuses. Its attributes reach
  // the editor through the editor options, which the editor compares by identity on every render,
  // so a change of the label association or of the error updates the element without re-creating
  // the editor. The area is the writing surface of the library (.ProseMirror, a textbox); the text
  // in it is prose of the design system. An error is announced at the textbox, which assistive
  // technology reads; the frame shows it.
  const inputId = fieldInputId(controlId);
  const labelledBy = useFieldLabelProps(controlId, inputId)['aria-labelledby'];
  const editorProps = React.useMemo(() => {
    const attributes: Record<string, string> = { id: inputId, class: PROSE_CLASS };
    if (labelledBy !== undefined) {
      attributes['aria-labelledby'] = labelledBy;
    }
    if (hasError) {
      attributes['aria-invalid'] = 'true';
    }
    return { attributes };
  }, [inputId, labelledBy, hasError]);

  const imageUrl: string | null = (state.imageUrl as string) || null;
  const commitEdits: boolean = state.commitEdits === true;
  const toolbar: unknown = state[STATE_TOOLBAR] || null;
  const insertRequest: InsertRequest | null = (state[STATE_INSERT] as InsertRequest) || null;

  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | null>(null);
  const dirtyRef = React.useRef(false);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  /** The sequence number of the insertion carried out last, so that none is carried out twice. */
  const insertedSeqRef = React.useRef(0);

  // Reports the text of the editor right away, dropping a report that is still being waited out.
  const flushValue = React.useCallback((ed: Editor) => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
      debounceRef.current = null;
    }
    sendCommand(CMD_VALUE_CHANGED, { [ARG_VALUE]: ed.getHTML() });
  }, [sendCommand]);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        // The link extension carries the CSS class of an anchor, which is what tells an object
        // link from an ordinary one, through parsing and rendering alike.
        link: { openOnClick: false },
        // Opening existing content must not change its stored markup, so no empty paragraph is
        // appended after a trailing table or code block.
        trailingNode: false,
      }),
      Image.configure({ allowBase64: true, inline: true }),
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
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
      debounceRef.current = setTimeout(() => {
        sendCommand(CMD_VALUE_CHANGED, { [ARG_VALUE]: ed.getHTML() });
      }, 300);
    },
    onBlur: ({ editor: ed }) => {
      // Flush pending debounced value immediately on blur so the server has
      // the latest content before any save command executes.
      flushValue(ed);
      // Commands are dispatched FIFO, so the commit runs after the value is applied
      // server-side. Only an actual edit commits: focusing and leaving does nothing.
      if (commitEdits && dirtyRef.current) {
        dirtyRef.current = false;
        sendCommand(CMD_COMMIT);
      }
    },
  }, [editable]);

  // Sync external value changes into the editor.
  React.useEffect(() => {
    if (editor && !editor.isFocused) {
      const currentHtml = editor.getHTML();
      if (currentHtml !== value) {
        editor.commands.setContent(value, { emitUpdate: false });
      }
    }
  }, [value, editor]);

  // Handle one-shot imageUrl from server (after upload).
  React.useEffect(() => {
    if (editor && imageUrl) {
      const imgSrc = dataUrl + '&key=' + encodeURIComponent(imageUrl);
      editor.chain().focus().setImage({ src: imgSrc }).run();
    }
  }, [imageUrl, editor, dataUrl]);

  // The markup the server asks for goes in at the cursor, once per request. Reporting the text
  // it produced is what tells the server the request is answered.
  React.useEffect(() => {
    if (!editor || !insertRequest || insertRequest.seq <= insertedSeqRef.current) {
      return;
    }
    insertedSeqRef.current = insertRequest.seq;
    editor.chain().focus().insertContent(insertRequest.html).run();
    flushValue(editor);
  }, [insertRequest, editor, flushValue]);

  const handleImageUpload = React.useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const handleFileSelected = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      uploadFile(formData);
    }
    // Reset so the same file can be uploaded again.
    e.target.value = '';
  }, [uploadFile]);

  // Object links in displayed content lead to the object they name; every other anchor keeps
  // its normal behavior.
  const handleContentClick = React.useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    const anchor = (e.target as HTMLElement | null)?.closest('a');
    if (!anchor || !e.currentTarget.contains(anchor) || !anchor.classList.contains(TL_OBJECT)) {
      return;
    }
    e.preventDefault();
    sendCommand(CMD_SHOW_OBJECT_LINK, { [ARG_HREF]: anchor.getAttribute('href') || '' });
  }, [sendCommand]);

  // Cleanup debounce on unmount.
  React.useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

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
        onImageUpload={handleImageUpload}
        toolbar={toolbar}
      />
      <EditorContent editor={editor} />
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleFileSelected}
      />
    </div>
  );
};

export default TLWysiwygEditor;
