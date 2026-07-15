import { React, useTLState, useTLCommand, useTLUpload, useTLDataUrl } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useEditor, EditorContent } from '@tiptap/react';
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

const TLWysiwygEditor: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const uploadFile = useTLUpload();
  const dataUrl = useTLDataUrl();

  const value: string = (state.value as string) || '';
  const editable: boolean = state.editable !== false;
  const hasError: boolean = !!state.hasError;
  const imageUrl: string | null = (state.imageUrl as string) || null;

  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
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
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
      debounceRef.current = setTimeout(() => {
        sendCommand('valueChanged', { value: ed.getHTML() });
      }, 300);
    },
    onBlur: ({ editor: ed }) => {
      // Flush pending debounced value immediately on blur so the server has
      // the latest content before any save command executes.
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
        debounceRef.current = null;
      }
      sendCommand('valueChanged', { value: ed.getHTML() });
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
          dangerouslySetInnerHTML={{ __html: value }}
        />
      </div>
    );
  }

  const cssClass = 'tlWysiwygEditor' + (hasError ? ' tlWysiwygEditor--error' : '');

  return (
    <div className={cssClass}>
      <WysiwygToolbar editor={editor} onImageUpload={handleImageUpload} />
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
