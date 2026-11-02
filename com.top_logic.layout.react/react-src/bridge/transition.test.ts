import { describe, expect, it } from 'vitest';
import { inertCopy } from './transition';

/**
 * A detached element as a control renders it, the way the copy is made of it once the control left
 * the document: here a dialog showing a picture, a frame and a video.
 */
function leftElement(): HTMLElement {
  const root = document.createElement('div');
  root.id = 'v1';
  root.innerHTML = `
    <div id="v2" class="body">
      <img id="v3" src="/react-api/data?controlId=v3&rev=1" alt="QR">
      <picture><source srcset="/pictures/wide.png"><img src="/pictures/narrow.png" alt="Picture"></picture>
      <iframe src="/react-api/data?controlId=v4&rev=1"></iframe>
      <video src="/react-api/data?controlId=v5&rev=1" poster="/poster.png"><source src="/clip.mp4"></video>
    </div>`;
  return root;
}

describe('inertCopy', () => {
  it('takes the loaded pictures over instead of copying them', () => {
    const left = leftElement();
    const picture = left.querySelector('img')!;
    const pictureElement = left.querySelector('picture')!;

    const copy = inertCopy(left);

    expect(copy).not.toBe(left);
    const pictures = copy.querySelectorAll('img');
    expect(pictures[0]).toBe(picture);
    expect(copy.querySelector('picture')).toBe(pictureElement);
    expect(picture.getAttribute('src')).toBe('/react-api/data?controlId=v3&rev=1');
  });

  it('copies frames and media without the address they load', () => {
    const copy = inertCopy(leftElement());

    const frame = copy.querySelector('iframe')!;
    expect(frame.hasAttribute('src')).toBe(false);
    const video = copy.querySelector('video')!;
    expect(video.hasAttribute('src')).toBe(false);
    expect(video.hasAttribute('poster')).toBe(false);
    expect(video.querySelector('source')!.hasAttribute('src')).toBe(false);
  });

  it('keeps what does not load anything, without ids and inert', () => {
    const copy = inertCopy(leftElement());

    expect(copy.hasAttribute('id')).toBe(false);
    expect(copy.querySelectorAll('[id]').length).toBe(0);
    expect(copy.querySelector('.body')).not.toBeNull();
    expect(copy.getAttribute('aria-hidden')).toBe('true');
    expect(copy.hasAttribute('inert')).toBe(true);
  });

  it('copies the pictures of an element still in the document', () => {
    const shown = leftElement();
    document.body.appendChild(shown);
    try {
      const picture = shown.querySelector('img')!;

      const copy = inertCopy(shown);

      expect(copy.querySelector('img')).not.toBe(picture);
      expect(shown.contains(picture)).toBe(true);
    } finally {
      shown.remove();
    }
  });
});
