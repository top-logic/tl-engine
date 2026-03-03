import { React, useTLState, useTLUpload, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.audioRecorder.record': 'Record audio',
  'js.audioRecorder.stop': 'Stop recording',
  'js.uploading': 'Uploading\u2026',
  'js.audioRecorder.error.insecure': 'Microphone requires a secure connection (HTTPS).',
  'js.audioRecorder.error.denied': 'Microphone access denied or unavailable.',
};

type LocalStatus = 'idle' | 'recording' | 'uploading';

const TLAudioRecorder: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const upload = useTLUpload();

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
  const [localError, setLocalError] = React.useState<string | null>(null);
  const mediaRecorderRef = React.useRef<MediaRecorder | null>(null);
  const chunksRef = React.useRef<Blob[]>([]);
  const streamRef = React.useRef<MediaStream | null>(null);

  const serverStatus = (state.status as string) ?? 'idle';
  const serverError = state.error as string | null;

  // Effective status: server status takes precedence when relevant.
  const effectiveStatus = serverStatus === 'received' ? 'idle' : (localStatus !== 'idle' ? localStatus : serverStatus);

  const handleToggle = React.useCallback(async () => {
    if (localStatus === 'recording') {
      // Stop recording.
      const recorder = mediaRecorderRef.current;
      if (recorder && recorder.state !== 'inactive') {
        recorder.stop();
      }
      return;
    }

    if (localStatus === 'uploading') {
      return; // Already uploading, ignore clicks.
    }

    // Start recording.
    setLocalError(null);

    if (!window.isSecureContext || !navigator.mediaDevices) {
      setLocalError('js.audioRecorder.error.insecure');
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;
      chunksRef.current = [];

      const mimeType = MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : '';
      const recorder = new MediaRecorder(stream, mimeType ? { mimeType } : undefined);
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = (e: BlobEvent) => {
        if (e.data.size > 0) {
          chunksRef.current.push(e.data);
        }
      };

      recorder.onstop = async () => {
        // Stop all tracks to release microphone.
        stream.getTracks().forEach(t => t.stop());
        streamRef.current = null;

        const blob = new Blob(chunksRef.current, { type: recorder.mimeType || 'audio/webm' });
        chunksRef.current = [];

        if (blob.size === 0) {
          setLocalStatus('idle');
          return;
        }

        setLocalStatus('uploading');
        const formData = new FormData();
        formData.append('audio', blob, 'recording.webm');
        await upload(formData);
        setLocalStatus('idle');
      };

      recorder.start();
      setLocalStatus('recording');
    } catch (err) {
      console.error('[TLAudioRecorder] Microphone access denied or unavailable:', err);
      setLocalError('js.audioRecorder.error.denied');
      setLocalStatus('idle');
    }
  }, [localStatus, upload]);

  const t = useI18N(I18N_KEYS);
  const ariaLabel =
    effectiveStatus === 'recording' ? t['js.audioRecorder.stop'] :
    effectiveStatus === 'uploading' ? t['js.uploading'] :
    t['js.audioRecorder.record'];

  const isDisabled = effectiveStatus === 'uploading';

  const buttonClasses = ['tlAudioRecorder__button'];
  if (effectiveStatus === 'recording') buttonClasses.push('tlAudioRecorder__button--recording');
  if (effectiveStatus === 'uploading') buttonClasses.push('tlAudioRecorder__button--uploading');

  return (
    <div id={controlId} className="tlAudioRecorder">
      <button
        type="button"
        className={buttonClasses.join(' ')}
        onClick={handleToggle}
        disabled={isDisabled}
        title={ariaLabel}
        aria-label={ariaLabel}
      >
        <span className={`tlAudioRecorder__icon${effectiveStatus === 'recording' ? ' tlAudioRecorder__icon--stop' : ''}`} />
      </button>
      {localError && (
        <span className="tlAudioRecorder__status tlAudioRecorder__status--error">{t[localError]}</span>
      )}
      {serverError && (
        <span className="tlAudioRecorder__status tlAudioRecorder__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLAudioRecorder;
