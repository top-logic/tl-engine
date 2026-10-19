// The files of wave 5b (spec 2026-10-19 §6): one upload hook (useUpload) and one blob download (downloadBlob) shared
// by the file upload, the binary field, the file chips and the download. The test registry has no parts of its own,
// so the parts the controls look up are the MUI ones, as in containers.test.tsx; what is tested here is the behaviour
// of the hook, the function and the controls, not the drawing of the parts.
//
// The recorder, the player and the camera (spec §3, §4) follow: their buttons are the part Button, drawn here by the
// TopLogic drawing of the part, since what is tested of them includes the classes of the design system.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, replacePart, register, useUpload, downloadBlob, TOOLTIP_ATTR, ICON_PRINT } from 'tl-react-bridge';
import type { ButtonPartProps } from 'tl-react-bridge';
import TLDownload from '../../../com.top_logic.layout.react/react-src/controls/TLDownload';
import TLBinaryField from '../../../com.top_logic.layout.react/react-src/controls/TLBinaryField';
import TLFileChips from '../../../com.top_logic.layout.react/react-src/controls/TLFileChips';
import TLAudioRecorder from '../../../com.top_logic.layout.react/react-src/controls/TLAudioRecorder';
import TLAudioPlayer from '../../../com.top_logic.layout.react/react-src/controls/TLAudioPlayer';
import TLPhotoCapture from '../../../com.top_logic.layout.react/react-src/controls/TLPhotoCapture';
import TLWindow from '../../../com.top_logic.layout.react/react-src/controls/TLWindow';
import TLImage from '../../../com.top_logic.layout.react/react-src/controls/TLImage';
import TLPdfViewer from '../../../com.top_logic.layout.react/react-src/controls/TLPdfViewer';
import TLHtml from '../../../com.top_logic.layout.react/react-src/controls/TLHtml';
import TLJobStatus from '../../../com.top_logic.layout.react/react-src/controls/TLJobStatus';
import ButtonPart from '../../../com.top_logic.layout.react/react-src/controls/button/ButtonPart';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiChipAdapter from './MuiChipAdapter';
import MuiProgressBarAdapter from './MuiProgressBarAdapter';
import { CONTROL_ID, childControl, mountAdapter, patchState, settle } from './wire-test-support';

// The download and the binary field draw their buttons, the file chips their chips and their add button. Without the
// parts a control throws ("No component registered for the part …") instead of failing an expectation.
replacePart('Button', MuiButtonAdapter);
replacePart('Chip', MuiChipAdapter);
replacePart('ProgressBar', MuiProgressBarAdapter);

/** The texts of the controls: the keys, as the test stub translates each key to itself. */
const LABEL_NO_FILE = 'js.download.noFile';
const LABEL_ADD_FILE = 'js.fileChips.add';
const LABEL_UPLOADING = 'js.uploading';

/** The name of the multipart part carrying a file. */
const PART_FILE = 'file';

/** The address of the upload channel. */
const UPLOAD_URL = 'react-api/upload';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** A request to the upload channel answered later: {@link release} answers it. */
interface PendingAnswer {
  answer: Promise<Response>;
  release: () => void;
}

function pendingAnswer(): PendingAnswer {
  let release!: () => void;
  const answer = new Promise<Response>(resolve => {
    release = () => resolve(new Response('', { status: 200 }));
  });
  return { answer, release };
}

/**
 * Records the uploads the control sends, on top of the stub of {@link mountAdapter}: an upload is answered with the
 * given answer, every other request by the stub.
 */
function recordUploads(answer?: Promise<Response>): FormData[] {
  const stub = vi.mocked(fetch).getMockImplementation()!;
  const uploads: FormData[] = [];
  vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
    if (String(url).includes(UPLOAD_URL)) {
      uploads.push(init!.body as FormData);
      return answer ?? new Response('', { status: 200 });
    }
    return stub(url, init);
  }));
  return uploads;
}

/** The names of the files sent in the given part of an upload. */
function fileNames(upload: FormData, part: string): string[] {
  return upload.getAll(part).map(entry => (entry as File).name);
}

/** A control using the upload hook: a button uploading one recording, the state of the hook as text. */
function UploadProbe() {
  const { uploading, error, upload } = useUpload();
  const send = () => upload([{ name: 'audio', file: new Blob(['sound']), fileName: 'recording.webm' }]);
  return (
    <div id={CONTROL_ID}>
      <button type="button" onClick={send}>senden</button>
      <span data-testid="uploading">{String(uploading)}</span>
      {error !== null && <span role="alert">{error}</span>}
    </div>
  );
}

describe('the upload hook (useUpload)', () => {
  it('sends the parts as given and is uploading until the call ends', async () => {
    mountAdapter(UploadProbe, { status: 'idle' });
    const pending = pendingAnswer();
    const uploads = recordUploads(pending.answer);

    await userEvent.click(screen.getByRole('button', { name: 'senden' }));
    await settle();

    expect(screen.getByTestId('uploading').textContent).toBe('true');
    expect(uploads).toHaveLength(1);
    expect(fileNames(uploads[0], 'audio')).toEqual(['recording.webm']);

    pending.release();
    await settle();
    expect(screen.getByTestId('uploading').textContent).toBe('false');
  });

  it('is no longer uploading once the server reports the file as received', async () => {
    mountAdapter(UploadProbe, { status: 'idle' });
    const pending = pendingAnswer();
    recordUploads(pending.answer);

    await userEvent.click(screen.getByRole('button', { name: 'senden' }));
    await settle();
    expect(screen.getByTestId('uploading').textContent).toBe('true');

    patchState({ status: 'received' });
    expect(screen.getByTestId('uploading').textContent).toBe('false');

    pending.release();
    await settle();
  });

  it('reports the error of the server', async () => {
    mountAdapter(UploadProbe, { error: 'Datei zu groß' });
    await settle();

    expect(screen.getByRole('alert').textContent).toBe('Datei zu groß');
  });
});

describe('the blob download (downloadBlob)', () => {
  /** What the stubbed anchor saw when it was clicked. */
  interface Click {
    href: string;
    download: string;
    attached: boolean;
  }

  function stubBlobUrls() {
    const created = vi.fn((_blob: Blob) => 'blob:datei');
    const revoked = vi.fn((_url: string) => {});
    class StubURL extends URL {
      static createObjectURL = created;
      static revokeObjectURL = revoked;
    }
    vi.stubGlobal('URL', StubURL);
    const clicks: Click[] = [];
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (this: HTMLAnchorElement) {
      clicks.push({ href: this.getAttribute('href') ?? '', download: this.download, attached: this.isConnected });
    });
    return { created, revoked, clicks };
  }

  it('saves the fetched data under the file name by a temporary anchor and frees the blob address', async () => {
    const fetched = vi.fn(async () => new Response(new Blob(['inhalt']), { status: 200 }));
    vi.stubGlobal('fetch', fetched);
    const { created, revoked, clicks } = stubBlobUrls();

    const saved = await downloadBlob('/data?rev=1', 'bericht.pdf');

    expect(saved).toBe(true);
    expect(fetched).toHaveBeenCalledWith('/data?rev=1');
    expect(created).toHaveBeenCalledTimes(1);
    expect(clicks).toEqual([{ href: 'blob:datei', download: 'bericht.pdf', attached: true }]);
    expect(document.querySelector('a[download]')).toBeNull();
    expect(revoked).toHaveBeenCalledWith('blob:datei');
  });

  it('answers false on an HTTP error, without an anchor', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => new Response('', { status: 404 })));
    vi.spyOn(console, 'error').mockImplementation(() => {});
    const { created, clicks } = stubBlobUrls();

    const saved = await downloadBlob('/data?rev=1', 'bericht.pdf');

    expect(saved).toBe(false);
    expect(created).not.toHaveBeenCalled();
    expect(clicks).toEqual([]);
  });
});

describe('the file row of the download (TLDownload)', () => {
  it('shows an empty file row without data', async () => {
    mountAdapter(TLDownload, { hasData: false });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.tagName).toBe('DIV');
    expect(root.classList).toContain('tl-file');
    expect(root.getAttribute('data-tl-state')).toBe('empty');
    expect(root.textContent).toBe(LABEL_NO_FILE);
  });

  it('shows the name of a file in the name of the row', async () => {
    mountAdapter(TLDownload, { hasData: true, fileName: 'bericht.pdf' });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList).toContain('tl-file');
    expect(root.hasAttribute('data-tl-state')).toBe(false);
    expect(root.querySelector('span.tl-file__name')!.textContent).toBe('bericht.pdf');
  });
});

describe('the binary field (TLBinaryField)', () => {
  it('shows an empty file row while read-only without data, without the old markers', async () => {
    mountAdapter(TLBinaryField, { editable: false, hasData: false });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList).toContain('tl-file');
    expect(root.getAttribute('data-tl-state')).toBe('empty');
    expect(root.textContent).toBe(LABEL_NO_FILE);
    expect(document.querySelector('.tlDownload, .tlBinaryField')).toBeNull();
  });

  it('shows the name of a file in the file row while read-only', async () => {
    mountAdapter(TLBinaryField, { editable: false, hasData: true, fileName: 'bericht.pdf' });
    await settle();

    expect(document.querySelector('.tl-file .tl-file__name')!.textContent).toBe('bericht.pdf');
    expect(document.querySelector('.tlDownload, .tlBinaryField')).toBeNull();
  });
});

describe('the file chips (TLFileChips) as a drop zone', () => {
  const REPORT = { key: 'k1', name: 'report.pdf', size: 2048, hasData: true };

  /** Two files dropped at once. */
  function twoFiles(): File[] {
    return [new File(['a'], 'eins.txt'), new File(['b'], 'zwei.txt')];
  }

  function group() {
    return document.getElementById(CONTROL_ID)!;
  }

  it('is a chip group with the role group', async () => {
    mountAdapter(TLFileChips, { chips: [REPORT], editable: true });
    await settle();

    expect(group().classList).toContain('tl-chip-group');
    expect(group().getAttribute('role')).toBe('group');
  });

  it('marks the group while a file is dragged over it, until the file leaves', async () => {
    mountAdapter(TLFileChips, { chips: [REPORT], editable: true });
    await settle();

    fireEvent.dragOver(group());
    expect(group().getAttribute('data-tl-state')).toBe('dragover');

    fireEvent.dragLeave(group());
    expect(group().hasAttribute('data-tl-state')).toBe(false);
  });

  it('sends all dropped files as parts file in one upload', async () => {
    mountAdapter(TLFileChips, { chips: [REPORT], editable: true });
    await settle();
    const uploads = recordUploads();

    fireEvent.dragOver(group());
    fireEvent.drop(group(), { dataTransfer: { files: twoFiles() } });
    await settle();

    expect(group().hasAttribute('data-tl-state')).toBe(false);
    expect(uploads).toHaveLength(1);
    expect(fileNames(uploads[0], PART_FILE)).toEqual(['eins.txt', 'zwei.txt']);
  });

  it('sends nothing for files dropped while an upload runs, and ends the drag mark', async () => {
    mountAdapter(TLFileChips, { chips: [REPORT], editable: true });
    await settle();
    const pending = pendingAnswer();
    const uploads = recordUploads(pending.answer);

    fireEvent.drop(group(), { dataTransfer: { files: [new File(['c'], 'erste.txt')] } });
    await settle();
    expect(uploads).toHaveLength(1);
    const add = screen.getByRole('button', { name: LABEL_UPLOADING });
    expect(add.getAttribute('aria-busy')).toBe('true');

    fireEvent.dragOver(group());
    fireEvent.drop(group(), { dataTransfer: { files: twoFiles() } });
    await settle();

    expect(uploads).toHaveLength(1);
    expect(group().hasAttribute('data-tl-state')).toBe(false);

    pending.release();
    await settle();
    expect(screen.getByRole('button', { name: LABEL_ADD_FILE }).hasAttribute('aria-busy')).toBe(false);
  });

  it('neither marks nor uploads while not editable', async () => {
    mountAdapter(TLFileChips, { chips: [REPORT], editable: false });
    await settle();
    const uploads = recordUploads();

    fireEvent.dragOver(group());
    expect(group().hasAttribute('data-tl-state')).toBe(false);
    fireEvent.drop(group(), { dataTransfer: { files: twoFiles() } });
    await settle();

    expect(uploads).toHaveLength(0);
  });

  it('announces the error of the last upload', async () => {
    mountAdapter(TLFileChips, { chips: [REPORT], editable: true, error: 'Datei zu groß' });
    await settle();

    const alert = screen.getByRole('alert');
    expect(alert.textContent).toBe('Datei zu groß');
    expect(alert.classList).toContain('tl-file-upload__status');
  });
});

/**
 * Draws the part Button with the given component for the tests of a block. The registry warns about a second
 * replacement; that is the point here, so the warning is kept out of the output.
 */
function drawButtonsWith(component: React.FC<ButtonPartProps>) {
  vi.spyOn(console, 'warn').mockImplementation(() => {});
  replacePart('Button', component);
}

/** Draws the part Button as TopLogic does, for the tests of a block, and gives the MUI drawing back afterwards. */
function withTopLogicButtons() {
  beforeEach(() => drawButtonsWith(ButtonPart));
  afterEach(() => drawButtonsWith(MuiButtonAdapter));
}

/** A microphone or camera stream: its tracks record whether they were stopped. */
interface FakeStream {
  stream: MediaStream;
  tracks: { stop: ReturnType<typeof vi.fn> }[];
}

function fakeStream(): FakeStream {
  const tracks = [{ stop: vi.fn() }];
  return { stream: { getTracks: () => tracks } as unknown as MediaStream, tracks };
}

/** Makes the page a secure context with the given getUserMedia. */
function stubMediaDevices(getUserMedia: () => Promise<MediaStream>) {
  vi.stubGlobal('isSecureContext', true);
  Object.defineProperty(navigator, 'mediaDevices', { configurable: true, value: { getUserMedia: vi.fn(getUserMedia) } });
}

afterEach(() => {
  delete (navigator as { mediaDevices?: unknown }).mediaDevices;
});

/** The recorders created by the control: a fake MediaRecorder handing over one chunk when stopped. */
const recorders: FakeRecorder[] = [];

class FakeRecorder {
  static isTypeSupported = () => true;
  state: 'inactive' | 'recording' = 'inactive';
  mimeType = 'audio/webm';
  ondataavailable: ((event: { data: Blob }) => void) | null = null;
  onstop: (() => void) | null = null;
  constructor(public stream: MediaStream) {
    recorders.push(this);
  }
  start() {
    this.state = 'recording';
  }
  stop() {
    this.state = 'inactive';
    this.ondataavailable?.({ data: new Blob(['sound']) });
    this.onstop?.();
  }
}

describe('the audio recorder (TLAudioRecorder)', () => {
  withTopLogicButtons();

  const LABEL_RECORD = 'js.audioRecorder.record';

  beforeEach(() => {
    recorders.length = 0;
    vi.stubGlobal('MediaRecorder', FakeRecorder);
  });

  function recordButton() {
    return screen.getByRole('button', { name: LABEL_RECORD });
  }

  it('is a secondary icon button in the danger tone, in the upload wrapper, not pressed', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    mountAdapter(TLAudioRecorder, { status: 'idle' });
    await settle();

    const button = recordButton();
    expect(button.matches('button.tl-button--icon.tl-button--secondary.tl-button--danger')).toBe(true);
    expect(button.getAttribute('aria-pressed')).toBe('false');
    expect(document.getElementById(CONTROL_ID)!.classList).toContain('tl-upload');
    expect(document.querySelector('[class*="tlAudioRecorder"]')).toBeNull();
  });

  it('is pressed while recording, keeps its name and says stop in its tooltip; stopping uploads the recording', async () => {
    const { stream } = fakeStream();
    stubMediaDevices(async () => stream);
    mountAdapter(TLAudioRecorder, { status: 'idle' });
    await settle();
    const pending = pendingAnswer();
    const uploads = recordUploads(pending.answer);

    await userEvent.click(recordButton());
    await settle();

    expect(recordButton().getAttribute('aria-pressed')).toBe('true');
    expect(recordButton().getAttribute(TOOLTIP_ATTR)).toBe('text:js.audioRecorder.stop');

    await userEvent.click(recordButton());
    await settle();

    expect(uploads).toHaveLength(1);
    expect(fileNames(uploads[0], 'audio')).toEqual(['recording.webm']);
    expect(recordButton().getAttribute('aria-busy')).toBe('true');
    expect((recordButton() as HTMLButtonElement).disabled).toBe(true);
    expect(recordButton().getAttribute('aria-pressed')).toBe('false');

    pending.release();
    await settle();
    expect(recordButton().hasAttribute('aria-busy')).toBe(false);
    expect((recordButton() as HTMLButtonElement).disabled).toBe(false);
  });

  it('stands ready again and announces the denial when the microphone is refused', async () => {
    stubMediaDevices(async () => { throw new DOMException('denied', 'NotAllowedError'); });
    vi.spyOn(console, 'error').mockImplementation(() => {});
    mountAdapter(TLAudioRecorder, { status: 'idle' });
    await settle();

    await userEvent.click(recordButton());
    await settle();

    expect(recordButton().getAttribute('aria-pressed')).toBe('false');
    const alert = screen.getByRole('alert');
    expect(alert.textContent).toBe('js.audioRecorder.error.denied');
    expect(alert.classList).toContain('tl-file-upload__status');
  });

  it('stops the microphone and sends nothing when unmounted while recording', async () => {
    const { stream, tracks } = fakeStream();
    stubMediaDevices(async () => stream);
    mountAdapter(TLAudioRecorder, { status: 'idle' });
    await settle();
    const uploads = recordUploads();

    await userEvent.click(recordButton());
    await settle();
    expect(recordButton().getAttribute('aria-pressed')).toBe('true');

    cleanup();
    await settle();

    expect(tracks[0].stop).toHaveBeenCalled();
    expect(recorders[0].state).toBe('inactive');
    expect(uploads).toHaveLength(0);
  });

  it('announces the error of the server', async () => {
    mountAdapter(TLAudioRecorder, { status: 'idle', error: 'Datei zu groß' });
    await settle();

    expect(screen.getByRole('alert').textContent).toBe('Datei zu groß');
  });
});

describe('the audio player (TLAudioPlayer)', () => {
  withTopLogicButtons();

  const LABEL_PLAY = 'js.audioPlayer.play';
  const LABEL_PAUSE = 'js.audioPlayer.pause';

  /** The audio elements created by the control. */
  const played: { play: ReturnType<typeof vi.fn>; pause: ReturnType<typeof vi.fn> }[] = [];

  beforeEach(() => {
    played.length = 0;
    class StubURL extends URL {
      static createObjectURL = () => 'blob:audio';
      static revokeObjectURL = () => {};
    }
    vi.stubGlobal('URL', StubURL);
  });

  function stubAudio(play: () => Promise<void>) {
    vi.stubGlobal('Audio', class {
      play = vi.fn(play);
      pause = vi.fn();
      onended: (() => void) | null = null;
      constructor(public src: string) {
        played.push(this);
      }
    });
  }

  /** Answers the request for the audio data later. */
  function holdAudioData(): PendingAnswer {
    const stub = vi.mocked(fetch).getMockImplementation()!;
    const pending = pendingAnswer();
    vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) =>
      String(url).includes('react-api/data') ? pending.answer : stub(url, init)));
    return pending;
  }

  it('is a secondary icon button that is no toggle', async () => {
    mountAdapter(TLAudioPlayer, { hasAudio: true, dataRevision: 0 });
    await settle();

    const button = screen.getByRole('button', { name: LABEL_PLAY });
    expect(button.matches('button.tl-button--icon.tl-button--secondary')).toBe(true);
    expect(button.classList).not.toContain('tl-button--danger');
    expect(button.hasAttribute('aria-pressed')).toBe(false);
    expect(document.querySelector('[class*="tlAudioPlayer"]')).toBeNull();
  });

  it('is busy while loading, then plays and is named pause', async () => {
    stubAudio(async () => {});
    mountAdapter(TLAudioPlayer, { hasAudio: true, dataRevision: 0 });
    await settle();
    const pending = holdAudioData();

    await userEvent.click(screen.getByRole('button', { name: LABEL_PLAY }));
    await settle();
    expect(screen.getByRole('button', { name: LABEL_PLAY }).getAttribute('aria-busy')).toBe('true');

    pending.release();
    await settle();

    const button = screen.getByRole('button', { name: LABEL_PAUSE });
    expect(button.hasAttribute('aria-busy')).toBe(false);
    expect(button.hasAttribute('aria-pressed')).toBe(false);
    expect(played).toHaveLength(1);
    expect(played[0].play).toHaveBeenCalledTimes(1);
  });

  it('stands ready again when the audio refuses to play', async () => {
    stubAudio(async () => { throw new DOMException('refused', 'NotAllowedError'); });
    vi.spyOn(console, 'error').mockImplementation(() => {});
    mountAdapter(TLAudioPlayer, { hasAudio: true, dataRevision: 0 });
    await settle();

    await userEvent.click(screen.getByRole('button', { name: LABEL_PLAY }));
    await settle();

    expect(screen.getByRole('button', { name: LABEL_PLAY }).hasAttribute('aria-busy')).toBe(false);
  });

  it('is disabled without audio and says so in its tooltip', async () => {
    mountAdapter(TLAudioPlayer, { hasAudio: false, dataRevision: 0 });
    await settle();

    const button = screen.getByRole('button', { name: LABEL_PLAY }) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
    expect(button.getAttribute(TOOLTIP_ATTR)).toBe('text:js.audioPlayer.noAudio');
  });
});

describe('the camera (TLPhotoCapture)', () => {
  withTopLogicButtons();

  const LABEL_OPEN = 'js.photoCapture.open';
  const LABEL_CAPTURE = 'js.photoCapture.capture';
  const LABEL_MIRROR = 'js.photoCapture.mirror';
  const LABEL_CLOSE = 'js.photoCapture.close';

  beforeEach(() => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(
      { drawImage: () => {} } as unknown as CanvasRenderingContext2D);
    vi.spyOn(HTMLCanvasElement.prototype, 'toBlob').mockImplementation(callback => callback(new Blob(['bild'])));
    vi.spyOn(HTMLMediaElement.prototype, 'play').mockResolvedValue(undefined);
  });

  function cameraButton() {
    return screen.getByRole('button', { name: LABEL_OPEN });
  }

  /** The surface of the camera dialog, found by its name: inside a window there are two dialogs. */
  function cameraDialog() {
    return screen.queryByRole('dialog', { name: LABEL_OPEN });
  }

  async function openCamera() {
    await userEvent.click(cameraButton());
    await settle();
  }

  it('is a secondary icon button in the upload wrapper', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    mountAdapter(TLPhotoCapture, {});
    await settle();

    expect(cameraButton().matches('button.tl-button--icon.tl-button--secondary')).toBe(true);
    expect(document.getElementById(CONTROL_ID)!.classList).toContain('tl-upload');
    expect(document.querySelector('[class*="tlPhotoCapture"]')).toBeNull();
  });

  it('opens a dialog of the design system under the body, titled and with the focus on capture', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    mountAdapter(TLPhotoCapture, {});
    await settle();

    await openCamera();

    const surface = cameraDialog()!;
    expect(surface).not.toBeNull();
    expect(surface.classList).toContain('tl-dialog__surface');
    expect(surface.getAttribute('aria-modal')).toBe('true');
    const scrim = surface.parentElement!;
    expect(scrim.matches('div.tl-dialog')).toBe(true);
    expect(scrim.parentElement).toBe(document.body);
    expect(scrim.getAttribute('data-tl-state')).toBe('entering');
    const title = document.getElementById(surface.getAttribute('aria-labelledby')!)!;
    expect(title.classList).toContain('tl-titlebar__title');
    expect(title.textContent).toBe(LABEL_OPEN);
    expect(surface.querySelector('.tl-titlebar button[aria-label="' + LABEL_CLOSE + '"]')).not.toBeNull();
    expect(surface.querySelector('.tl-dialog__body > video.tl-media__frame')).not.toBeNull();

    const capture = screen.getByRole('button', { name: LABEL_CAPTURE });
    expect(capture.classList).toContain('tl-button--primary');
    expect(capture.closest('.tl-button-bar')).not.toBeNull();
    expect(document.activeElement).toBe(capture);
  });

  it('mirrors the video by a pressed button and a state of the video', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    mountAdapter(TLPhotoCapture, {});
    await settle();
    await openCamera();

    const mirror = screen.getByRole('button', { name: LABEL_MIRROR });
    const video = cameraDialog()!.querySelector('video')!;
    expect(mirror.getAttribute('aria-pressed')).toBe('false');
    expect(video.hasAttribute('data-tl-state')).toBe(false);

    await userEvent.click(mirror);

    expect(mirror.getAttribute('aria-pressed')).toBe('true');
    expect(video.getAttribute('data-tl-state')).toBe('mirrored');
  });

  it('closes on Escape, stops the camera and gives the focus back to the camera button', async () => {
    const { stream, tracks } = fakeStream();
    stubMediaDevices(async () => stream);
    mountAdapter(TLPhotoCapture, {});
    await settle();
    await openCamera();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(cameraDialog()).toBeNull();
    expect(tracks[0].stop).toHaveBeenCalled();
    expect(document.activeElement).toBe(cameraButton());
  });

  it('closes on a click on the scrim, not on a click on the surface', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    mountAdapter(TLPhotoCapture, {});
    await settle();
    await openCamera();

    fireEvent.click(cameraDialog()!);
    expect(cameraDialog()).not.toBeNull();
    fireEvent.click(cameraDialog()!.parentElement!);
    expect(cameraDialog()).toBeNull();
  });

  it('uploads the captured photo as part photo and is busy meanwhile', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    mountAdapter(TLPhotoCapture, {});
    await settle();
    const pending = pendingAnswer();
    const uploads = recordUploads(pending.answer);
    await openCamera();

    await userEvent.click(screen.getByRole('button', { name: LABEL_CAPTURE }));
    await settle();

    expect(cameraDialog()).toBeNull();
    expect(uploads).toHaveLength(1);
    expect(fileNames(uploads[0], 'photo')).toEqual(['capture.jpg']);
    expect(cameraButton().getAttribute('aria-busy')).toBe('true');

    pending.release();
    await settle();
    expect(cameraButton().hasAttribute('aria-busy')).toBe(false);
  });

  it('announces the denial when the camera is refused', async () => {
    stubMediaDevices(async () => { throw new DOMException('denied', 'NotAllowedError'); });
    vi.spyOn(console, 'error').mockImplementation(() => {});
    mountAdapter(TLPhotoCapture, {});
    await settle();

    await openCamera();

    expect(cameraDialog()).toBeNull();
    const alert = screen.getByRole('alert');
    expect(alert.textContent).toBe('js.photoCapture.error.denied');
    expect(alert.classList).toContain('tl-file-upload__status');
  });

  it('closes only the camera dialog on Escape inside a window, and the focus returns to the camera button', async () => {
    stubMediaDevices(async () => fakeStream().stream);
    register('MediaTestPhotoCapture', TLPhotoCapture);
    const camera = { controlId: 'pc', module: 'MediaTestPhotoCapture', state: {} } as ReturnType<typeof childControl>;
    const sent = mountAdapter(TLWindow, { title: 'Fenster', child: camera, closable: true });
    await settle();
    await openCamera();
    expect(cameraDialog()).not.toBeNull();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(cameraDialog()).toBeNull();
    expect(sent).not.toHaveBeenCalledWith('close', expect.anything());
    expect(screen.getByRole('dialog', { name: 'Fenster' })).toBeTruthy();
    expect(document.activeElement).toBe(cameraButton());

    // Positive control: with the camera dialog gone, the same key reaches the window.
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(sent).toHaveBeenCalledWith('close', {});
  });
});

// Picture, PDF and HTML (spec §5): one block tl-media for embedded content, the HTML document on the white paper of
// the design system with its print button over it, embedded HTML as tl-prose.

/** The class the server configures for a control (state cssClass). */
const SERVER_CLASS = 'appClass';

/** The root element of the mounted control. */
function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

/** How often the class configured by the server stands at the root. */
function serverClassCount(): number {
  return root().className.split(/\s+/).filter(name => name === SERVER_CLASS).length;
}

describe('the picture (TLImage)', () => {
  it('is a tl-media covering its box by default, the picture its frame, the server class once', async () => {
    mountAdapter(TLImage, { url: 'picture.png', cssClass: SERVER_CLASS, width: '120px' });
    await settle();

    expect(root().classList).toContain('tl-media');
    expect(root().classList).toContain('tl-media--cover');
    expect(root().style.width).toBe('120px');
    expect(root().querySelector(':scope > img.tl-media__frame')!.getAttribute('src')).toBe('picture.png');
    expect(serverClassCount()).toBe(1);
    expect(document.querySelector('[class*="tlImage"]')).toBeNull();
  });

  it('fits the picture into its box with fit contain', async () => {
    mountAdapter(TLImage, { url: 'picture.png', fit: 'contain' });
    await settle();

    expect(root().classList).toContain('tl-media--contain');
    expect(root().classList).not.toContain('tl-media--cover');
  });

  it('keeps the space by an empty placeholder while there is no picture', async () => {
    mountAdapter(TLImage, { url: null, height: '80px' });
    await settle();

    const placeholder = root().querySelector(':scope > div.tl-media__placeholder')!;
    expect(placeholder).not.toBeNull();
    expect(placeholder.childNodes).toHaveLength(0);
    expect(root().querySelector('img')).toBeNull();
  });
});

describe('the PDF viewer (TLPdfViewer)', () => {
  it('shows the document in a frame of a tl-media', async () => {
    mountAdapter(TLPdfViewer, { hasPdf: true, dataRevision: 2, cssClass: SERVER_CLASS });
    await settle();

    expect(root().classList).toContain('tl-media');
    const frame = root().querySelector(':scope > iframe.tl-media__frame')!;
    expect(frame.getAttribute('title')).toBe('js.pdfViewer.title');
    expect(serverClassCount()).toBe(1);
    expect(document.querySelector('[class*="tlPdfViewer"]')).toBeNull();
  });

  it('says that there is no document in a status line', async () => {
    mountAdapter(TLPdfViewer, { hasPdf: false });
    await settle();

    const status = root().querySelector('div.tl-status')!;
    expect(status.textContent).toBe('js.pdfViewer.noDocument');
    expect(root().querySelector('iframe')).toBeNull();
  });
});

describe('the HTML (TLHtml)', () => {
  withTopLogicButtons();

  it('shows a document as the base of a filling overlay, on the paper of the design system', async () => {
    mountAdapter(TLHtml, { display: 'document', dataRevision: 1, cssClass: SERVER_CLASS });
    await settle();

    expect(root().classList).toContain('tl-overlay');
    expect(root().classList).toContain('tl-overlay--fill');
    expect(root().classList).toContain('tlFill');
    expect(serverClassCount()).toBe(1);
    const media = root().querySelector(':scope > div.tl-overlay__base > div.tl-media.tl-media--document')!;
    expect(media).not.toBeNull();
    expect(media.querySelector(':scope > iframe.tl-media__frame')).not.toBeNull();
    expect(root().querySelector('.tl-overlay__layer')).toBeNull();
    expect(document.querySelector('[class*="tlHtml"]')).toBeNull();
  });

  it('puts the print button in an action at the top right over the document', async () => {
    mountAdapter(TLHtml, { display: 'document', dataRevision: 1, print: true });
    await settle();

    const button = screen.getByRole('button', { name: 'js.html.print' });
    expect(button.matches('button.tl-button--ghost.tl-button--icon')).toBe(true);
    const action = button.parentElement!;
    expect(action.matches('div.tl-overlay__action')).toBe(true);
    const layer = action.parentElement!;
    expect(layer.matches('div.tl-overlay__layer.tl-overlay__layer--top-right')).toBe(true);
    expect(layer.parentElement).toBe(root());
    const glyph = button.querySelector('.tl-button__icon')!;
    for (const name of ICON_PRINT.substring('css:'.length).split(' ')) {
      expect(glyph.classList).toContain(name);
    }
  });

  it('shows a preview as a tl-media document preview, laid out at its page size and scaled', async () => {
    vi.stubGlobal('ResizeObserver', class { observe() {} disconnect() {} });
    mountAdapter(TLHtml, { display: 'thumbnail', dataRevision: 1, thumbnailWidth: 800, thumbnailHeight: 1000 });
    await settle();

    expect(root().classList).toContain('tl-media');
    expect(root().classList).toContain('tl-media--document');
    expect(root().classList).toContain('tl-media--preview');
    expect(root().style.aspectRatio).toBe('800 / 1000');
    expect(root().style.overflow).toBe('');
    const frame = root().querySelector<HTMLIFrameElement>(':scope > iframe.tl-media__frame')!;
    expect(frame.style.width).toBe('800px');
    expect(frame.style.height).toBe('1000px');
    expect(frame.style.transform).toMatch(/^scale\(/);
    // Only data stands inline; how the page lies in the box is the design system's (tl-media--preview).
    for (const property of ['position', 'top', 'left', 'pointerEvents', 'transformOrigin'] as const) {
      expect(frame.style[property]).toBe('');
    }
  });

  it('inserts embedded HTML as tl-prose', async () => {
    mountAdapter(TLHtml, { display: 'inline', html: '<p>Text</p>', cssClass: SERVER_CLASS });
    await settle();

    expect(root().classList).toContain('tl-prose');
    expect(serverClassCount()).toBe(1);
    expect(root().innerHTML).toBe('<p>Text</p>');
  });

  it('shows an error as an alert of the design system', async () => {
    mountAdapter(TLHtml, { display: 'inline', error: 'Nicht erlaubt' });
    await settle();

    expect(root().classList).toContain('tl-alert');
    expect(screen.getByRole('alert').textContent).toBe('Nicht erlaubt');
  });
});

describe('the job status (TLJobStatus)', () => {
  /** A running job in its second of three steps. */
  const RUNNING = {
    status: 'running',
    phases: [
      { label: 'Daten lesen', status: 'done' },
      { label: 'Prüfen', status: 'active' },
      { label: 'Bericht schreiben', status: 'pending' },
    ],
    fraction: 0.45,
    message: '1204 von 2680 geprüft',
    startedAt: 1000,
    finishedAt: null,
    serverNow: 43000,
    cancelable: true,
  };

  function liveLine(): HTMLElement {
    const lines = root().querySelectorAll<HTMLElement>('[role="status"]');
    expect(lines).toHaveLength(1);
    return lines[0];
  }

  it('is a tl-job-status announcing nothing itself, with one live line for state, current step and message', async () => {
    mountAdapter(TLJobStatus, RUNNING);
    await settle();

    expect(root().matches('div.tl-job-status')).toBe(true);
    expect(root().hasAttribute('role')).toBe(false);
    expect(root().hasAttribute('aria-live')).toBe(false);
    const live = liveLine();
    expect(live.classList).toContain('tl-job-status__live');
    expect(live.textContent).toContain('js.jobStatus.running');
    expect(live.textContent).toContain('js.jobStatus.currentStep');
    expect(live.textContent).toContain('Prüfen');
    expect(live.textContent).toContain('1204 von 2680 geprüft');
    expect(live.querySelector('.tl-visually-hidden')!.textContent).toContain('js.jobStatus.currentStep');
    expect(document.querySelector('[class*="tlJobStatus"]')).toBeNull();
  });

  it('keeps the elapsed time out of every live region, and a change of step changes the text of the live line', async () => {
    mountAdapter(TLJobStatus, RUNNING);
    await settle();

    const elapsed = root().querySelector('.tl-job-status__elapsed')!;
    expect(elapsed.classList).toContain('tl-nums');
    expect(elapsed.closest('[role="status"], [aria-live]')).toBeNull();

    const before = liveLine().textContent;
    patchState({
      phases: [
        { label: 'Daten lesen', status: 'done' },
        { label: 'Prüfen', status: 'done' },
        { label: 'Bericht schreiben', status: 'active' },
      ],
    });
    const after = liveLine().textContent;
    expect(after).not.toBe(before);
    expect(after).toContain('Bericht schreiben');
    expect(after).not.toContain('Prüfen');
  });

  it('lists the steps as vertical tl-steps, done and current as attributes', async () => {
    mountAdapter(TLJobStatus, RUNNING);
    await settle();

    const list = root().querySelector('ol.tl-steps.tl-steps--vertical')!;
    expect(list).not.toBeNull();
    expect(list.closest('[role="status"]')).toBeNull();
    const items = list.querySelectorAll(':scope > li.tl-steps__item');
    expect(items).toHaveLength(3);
    expect(items[0].getAttribute('data-tl-state')).toBe('done');
    expect(items[0].hasAttribute('aria-current')).toBe(false);
    expect(items[1].getAttribute('aria-current')).toBe('step');
    expect(items[1].hasAttribute('data-tl-state')).toBe(false);
    expect(items[2].hasAttribute('data-tl-state')).toBe(false);
    expect(items[2].hasAttribute('aria-current')).toBe(false);
    const disc = items[1].querySelector('.tl-steps__disc')!;
    expect(disc.getAttribute('aria-hidden')).toBe('true');
    expect(disc.textContent).toBe('2');
    expect(items[1].querySelector('.tl-steps__name')!.textContent).toBe('Prüfen');
  });

  it('shows an error as an alert outside the live line', async () => {
    mountAdapter(TLJobStatus, { ...RUNNING, status: 'failed', finishedAt: 5000, error: 'Vorlage fehlt' });
    await settle();

    const error = root().querySelector('div.tl-job-status__error')!;
    expect(error.getAttribute('role')).toBe('alert');
    expect(error.textContent).toBe('Vorlage fehlt');
    expect(error.closest('[role="status"]')).toBeNull();
    expect(liveLine().textContent).not.toContain('Vorlage fehlt');
  });

  it('wraps the part ProgressBar in tl-job-status__bar, without a class on the part', async () => {
    mountAdapter(TLJobStatus, RUNNING);
    await settle();

    const bar = screen.getByRole('progressbar');
    const wrapper = bar.closest('div.tl-job-status__bar')!;
    expect(wrapper).not.toBeNull();
    expect(wrapper.parentElement).toBe(root());
    expect(bar.classList).not.toContain('tl-job-status__bar');
    expect(bar.closest('[role="status"]')).toBeNull();
  });
});
