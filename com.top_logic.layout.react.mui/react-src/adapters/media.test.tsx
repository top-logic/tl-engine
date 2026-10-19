// The files of wave 5b (spec 2026-10-19 §6): one upload hook (useUpload) and one blob download (downloadBlob) shared
// by the file upload, the binary field, the file chips and the download. The test registry has no parts of its own,
// so the parts the controls look up are the MUI ones, as in containers.test.tsx; what is tested here is the behaviour
// of the hook, the function and the controls, not the drawing of the parts.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, replacePart, useUpload, downloadBlob } from 'tl-react-bridge';
import TLDownload from '../../../com.top_logic.layout.react/react-src/controls/TLDownload';
import TLBinaryField from '../../../com.top_logic.layout.react/react-src/controls/TLBinaryField';
import TLFileChips from '../../../com.top_logic.layout.react/react-src/controls/TLFileChips';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiChipAdapter from './MuiChipAdapter';
import MuiProgressBarAdapter from './MuiProgressBarAdapter';
import { CONTROL_ID, mountAdapter, patchState, settle } from './wire-test-support';

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
