import { React, useTLState, useTLCommand, useTLUpload, useTLDataUrl } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useEditor, EditorContent } from '@tiptap/react';
import type { Editor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import Image from '@tiptap/extension-image';
import Table from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import Color from '@tiptap/extension-color';
import TextStyle from '@tiptap/extension-text-style';
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
  const imageUrl: string | null = (state.imageUrl as string) || null;
  const commitOnBlur: boolean = state.commitOnBlur === true;
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
      StarterKit,
      Underline,
      // The link extension carries the CSS class of an anchor, which is what tells an object
      // link from an ordinary one, through parsing and rendering alike.
      Link.configure({ openOnClick: false }),
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
      if (commitOnBlur && dirtyRef.current) {
        dirtyRef.current = false;
        sendCommand('commit');
      }
    },
  }, [editable]);

  // Sync external value changes into the editor.
  React.useEffect(() => {
    if (editor && !editor.isFocused) {
      const currentHtml = editor.getHTML();
      if (currentHtml !== value) {
        editor.commands.setContent(value, false);
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

  if (!editable) {
    return (
      <div className="tlWysiwygEditor tlWysiwygEditor--immutable">
        <div
          className="tlWysiwygEditor__immutableContent ProseMirror"
          onClick={handleContentClick}
          dangerouslySetInnerHTML={{ __html: value }}
        />
      </div>
    );
  }

  const cssClass = 'tlWysiwygEditor' + (hasError ? ' tlWysiwygEditor--error' : '');

  return (
    <div className={cssClass}>
      <WysiwygToolbar
        editor={editor}
        onImageUpload={handleImageUpload}
        toolbar={toolbar}
      />
      <div className="tlWysiwygEditor__content">
        <EditorContent editor={editor} />
      </div>
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
