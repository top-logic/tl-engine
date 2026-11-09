import { afterEach, describe, expect, it, vi } from 'vitest';
import { Editor } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import { Slice } from '@tiptap/pm/model';
import { imageFiles } from './ImageFiles';

/** A pasted or dropped file of the given type. */
function file(name: string, type: string): File {
  return new File(['x'], name, { type });
}

let editor: Editor | null = null;

/** An editor holding the text "Hello world", whose image files go to the given upload. */
function createEditor(upload: (files: File[]) => void): Editor {
  editor = new Editor({
    extensions: [StarterKit, Image.configure({ inline: true }), imageFiles(upload)],
    content: '<p>Hello world</p>',
  });
  return editor;
}

/** Pastes the given files and HTML into the editor; whether the paste was taken. */
function paste(ed: Editor, files: File[], html = ''): boolean {
  const event = {
    clipboardData: { files, getData: (type: string) => (type === 'text/html' ? html : '') },
    preventDefault: () => {},
    stopPropagation: () => {},
  } as unknown as ClipboardEvent;
  return ed.view.someProp('handlePaste', f => f(ed.view, event, Slice.empty)) === true;
}

/** Drops the given files onto the editor at the given document position. */
function drop(ed: Editor, files: File[], pos: number): boolean {
  // The test DOM has no layout, so the position of the pointer is the one given.
  ed.view.posAtCoords = () => ({ pos, inside: -1 });
  const event = {
    dataTransfer: { types: ['Files'], files },
    clientX: 0,
    clientY: 0,
    preventDefault: () => {},
    stopPropagation: () => {},
  } as unknown as DragEvent;
  return ed.view.someProp('handleDrop', f => f(ed.view, event, Slice.empty, false)) === true;
}

afterEach(() => {
  editor?.destroy();
  editor = null;
});

describe('image files', () => {
  it('uploads a pasted image file once', () => {
    const upload = vi.fn();
    const image = file('shot.png', 'image/png');

    expect(paste(createEditor(upload), [image])).toBe(true);

    expect(upload).toHaveBeenCalledTimes(1);
    expect(upload).toHaveBeenCalledWith([image]);
  });

  it('leaves files that are no image to the editor', () => {
    const upload = vi.fn();

    expect(paste(createEditor(upload), [file('drawing.svg', 'image/svg+xml'), file('a.txt', 'text/plain')]))
      .toBe(false);

    expect(upload).not.toHaveBeenCalled();
  });

  it('pastes HTML holding an image as HTML, without uploading the file beside it', () => {
    const upload = vi.fn();

    const taken = paste(createEditor(upload), [file('image.png', 'image/png')],
      '<meta charset="utf-8"><img src="https://example.com/picture.png">');

    expect(taken).toBe(false);
    expect(upload).not.toHaveBeenCalled();
  });

  it('pastes HTML holding text as HTML, without uploading its rendering', () => {
    const upload = vi.fn();

    paste(createEditor(upload), [file('image.png', 'image/png')], '<p>Formatted <b>text</b></p>');

    expect(upload).not.toHaveBeenCalled();
  });

  it('uploads the file beside HTML without content', () => {
    const upload = vi.fn();

    paste(createEditor(upload), [file('image.png', 'image/png')], '<meta charset="utf-8">');

    expect(upload).toHaveBeenCalledTimes(1);
  });

  it('moves the cursor to where image files are dropped and uploads them', () => {
    const upload = vi.fn();
    const ed = createEditor(upload);
    const images = [file('a.png', 'image/png'), file('b.jpg', 'image/jpeg')];

    expect(drop(ed, images, 6)).toBe(true);

    expect(ed.state.selection.from).toBe(6);
    expect(upload).toHaveBeenCalledTimes(1);
    expect(upload).toHaveBeenCalledWith(images);
  });
});
