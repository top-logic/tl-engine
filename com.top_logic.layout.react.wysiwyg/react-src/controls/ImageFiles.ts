import { FileHandler } from '@tiptap/extension-file-handler';
import type { Editor } from '@tiptap/core';

/**
 * The types of the image files the editor takes: the raster formats a browser shows and the server
 * recognizes by their content. An SVG is left out, as it is a document that can run scripts when its
 * download URL is opened. The server accepts the same types, see `ReactWysiwygControl#IMAGE_TYPES`.
 */
export const IMAGE_MIME_TYPES = ['image/png', 'image/jpeg', 'image/gif'];

/** Uploads image files; the server answers with the request to insert them at the cursor. */
export type UploadImages = (files: File[]) => void;

/**
 * Whether pasted HTML holds content of its own: text or an image.
 *
 * A browser copying an image from a page, and an office application copying formatted text, put the
 * HTML on the clipboard together with a picture file of it. The HTML is the content then, and the
 * file only a rendering of it.
 */
export function htmlHasContent(html: string | undefined): boolean {
  if (!html) {
    return false;
  }
  const body = new DOMParser().parseFromString(html, 'text/html').body;
  return body.querySelector('img') !== null || (body.textContent ?? '').trim() !== '';
}

/**
 * Takes image files dropped onto the editor: the cursor moves to where they were dropped, the
 * position the server's insertion lands at, and they are uploaded.
 */
export function dropImages(editor: Editor, files: File[], pos: number, upload: UploadImages): void {
  editor.chain().focus().setTextSelection(pos).run();
  upload(files);
}

/**
 * Takes image files pasted into the editor: they are uploaded, unless the clipboard also holds HTML
 * with content of its own, which the editor pastes as usual.
 */
export function pasteImages(files: File[], html: string | undefined, upload: UploadImages): void {
  if (htmlHasContent(html)) {
    return;
  }
  upload(files);
}

/**
 * The extension taking image files pasted into or dropped onto the editor.
 *
 * @param upload Uploads the files. The extension is created once with its editor, so the function
 *        is the one reaching the current upload of the control.
 */
export function imageFiles(upload: UploadImages) {
  return FileHandler.configure({
    allowedMimeTypes: IMAGE_MIME_TYPES,
    onPaste: (_editor, files, html) => pasteImages(files, html, upload),
    onDrop: (editor, files, pos) => dropImages(editor, files, pos, upload),
  });
}
