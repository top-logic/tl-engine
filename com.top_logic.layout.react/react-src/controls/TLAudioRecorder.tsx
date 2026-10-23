import { React, useTLState, useUpload, useI18N, usePart, rootClassName, ICON_RECORD, ICON_STOP } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.audioRecorder.record': 'Record audio',
  'js.audioRecorder.stop': 'Stop recording',
  'js.audioRecorder.error.insecure': 'Microphone requires a secure connection (HTTPS).',
  'js.audioRecorder.error.denied': 'Microphone access denied or unavailable.',
};

/** The name of the multipart part carrying the recording. */
const PART_AUDIO = 'audio';

/** The file name the recording is sent under. */
const RECORDING_FILE_NAME = 'recording.webm';

/** The type of the recording, where the browser records it. */
const RECORDING_TYPE = 'audio/webm';

/** Releases the microphone. */
function stopTracks(stream: MediaStream | null) {
  stream?.getTracks().forEach(track => track.stop());
}

/**
 * Records audio from the microphone and uploads the recording (part `audio`).
 *
 * <p>The button is a toggle in the danger tone: pressed while recording, with the stop icon and the
 * tooltip saying stop; its name stays "record", as the name of a toggle does. While the recording is
 * uploaded, it is busy and disabled. A device error and the error of the server are announced in the
 * status line.</p>
 *
 * <p>Unmounted while recording, the recorder stops without uploading and releases the microphone.</p>
 */
const TLAudioRecorder: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const { uploading, error, upload } = useUpload();
  const Button = usePart('Button');

  const [recording, setRecording] = React.useState(false);
  const [localError, setLocalError] = React.useState<string | null>(null);
  const mediaRecorderRef = React.useRef<MediaRecorder | null>(null);
  const chunksRef = React.useRef<Blob[]>([]);
  const streamRef = React.useRef<MediaStream | null>(null);
  const mountedRef = React.useRef(true);

  const handleToggle = React.useCallback(async () => {
    if (recording) {
      const recorder = mediaRecorderRef.current;
      if (recorder && recorder.state !== 'inactive') {
        recorder.stop();
      }
      return;
    }

    if (uploading) {
      return;
    }

    setLocalError(null);

    if (!window.isSecureContext || !navigator.mediaDevices) {
      setLocalError('js.audioRecorder.error.insecure');
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      if (!mountedRef.current) {
        // Unmounted while the browser asked for the microphone.
        stopTracks(stream);
        return;
      }
      streamRef.current = stream;
      chunksRef.current = [];

      const mimeType = MediaRecorder.isTypeSupported(RECORDING_TYPE) ? RECORDING_TYPE : '';
      const recorder = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = (e: BlobEvent) => {
        if (e.data.size > 0) {
          chunksRef.current.push(e.data);
        }
      };

      recorder.onstop = async () => {
        stopTracks(stream);
        streamRef.current = null;
        mediaRecorderRef.current = null;
        setRecording(false);

        const blob = new Blob(chunksRef.current, { type: recorder.mimeType || RECORDING_TYPE });
        chunksRef.current = [];
        if (blob.size === 0) {
          return;
        }
        await upload([{ name: PART_AUDIO, file: blob, fileName: RECORDING_FILE_NAME }]);
      };

      recorder.start();
      setRecording(true);
    } catch (err) {
      console.error('[TLAudioRecorder] Microphone access denied or unavailable:', err);
      stopTracks(streamRef.current);
      streamRef.current = null;
      setLocalError('js.audioRecorder.error.denied');
      setRecording(false);
    }
  }, [recording, uploading, upload]);

  // Unmounted while recording: stop without uploading, release the microphone.
  React.useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      const recorder = mediaRecorderRef.current;
      if (recorder) {
        recorder.ondataavailable = null;
        recorder.onstop = null;
        if (recorder.state !== 'inactive') {
          recorder.stop();
        }
        mediaRecorderRef.current = null;
      }
      stopTracks(streamRef.current);
      streamRef.current = null;
    };
  }, []);

  const t = useI18N(I18N_KEYS);

  return (
    <div id={controlId} className={rootClassName(state, 'tl-upload')}>
      <Button
        label={t['js.audioRecorder.record']}
        tooltip={recording ? t['js.audioRecorder.stop'] : undefined}
        image={recording ? ICON_STOP : ICON_RECORD}
        display="icon-only"
        appearance="secondary"
        danger
        checked={recording}
        disabled={uploading}
        aria-busy={uploading ? true : undefined}
        onClick={handleToggle}
      />
      {localError && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{t[localError]}</span>
      )}
      {error && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
      )}
    </div>
  );
};

export default TLAudioRecorder;
