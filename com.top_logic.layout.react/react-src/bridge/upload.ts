import { useCallback, useEffect, useState } from 'react';
import { useTLState, useTLUpload } from './tl-react-bridge';

/**
 * State key of the upload status a server control reports: `received` once the file has arrived.
 * Controls without such a status (the file chips) report the arrival by their new content instead.
 */
const STATUS = 'status';

/** Value of {@link STATUS} once the uploaded file has arrived at the server. */
const STATUS_RECEIVED = 'received';

/** State key of the text of the error of the last upload, written by the server control. */
const ERROR = 'error';

/** The state of an upload as {@link useUpload} reports it. */
export interface UploadState {
  /** Whether an upload of this control is under way. */
  uploading: boolean;

  /** The text of the error of the last upload, as the server reports it; `null` if there is none. */
  error: string | null;
}

/** One part of an upload: a file or blob under the name of its multipart part. */
export interface UploadPart {
  /** The name of the multipart part, which tells the server what arrives (`file`, `audio`, `photo`). */
  name: string;

  /** The data. */
  file: Blob;

  /** The file name the data is sent under. */
  fileName: string;
}

/**
 * The upload of the surrounding control: sends files over the upload channel of the bridge (see
 * `useTLUpload`, which refuses an upload larger than the server's limit) and reports whether an
 * upload is under way and the error of the last one.
 *
 * <p>An upload is under way from the call of `upload` until the upload channel has handed it over;
 * a server control that reports a status ends it earlier, when it reports the file as received.</p>
 */
export function useUpload(): UploadState & { upload: (parts: UploadPart[]) => Promise<void> } {
  const state = useTLState();
  const send = useTLUpload();
  const [uploading, setUploading] = useState(false);

  const status = state[STATUS];
  const error = typeof state[ERROR] === 'string' ? state[ERROR] as string : null;

  useEffect(() => {
    if (status === STATUS_RECEIVED) {
      setUploading(false);
    }
  }, [status]);

  const upload = useCallback(async (parts: UploadPart[]) => {
    if (parts.length === 0) {
      return;
    }
    setUploading(true);
    try {
      const formData = new FormData();
      for (const part of parts) {
        formData.append(part.name, part.file, part.fileName);
      }
      await send(formData);
    } finally {
      setUploading(false);
    }
  }, [send]);

  return { uploading, error, upload };
}
