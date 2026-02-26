import { React, useTLState, useTLUpload } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

type LocalStatus = 'idle' | 'recording' | 'uploading';

const TLAudioRecorder: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const upload = useTLUpload();

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
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
      setLocalStatus('idle');
    }
  }, [localStatus, upload]);

  const buttonLabel =
    effectiveStatus === 'recording' ? 'Stop' :
    effectiveStatus === 'uploading' ? 'Uploading\u2026' :
    'Record';

  const isDisabled = effectiveStatus === 'uploading';

  return (
    <div className="tlAudioRecorder">
      <button
        type="button"
        className={`tlAudioRecorder__button${effectiveStatus === 'recording' ? ' tlAudioRecorder__button--recording' : ''}`}
        onClick={handleToggle}
        disabled={isDisabled}
      >
        {buttonLabel}
      </button>
      {effectiveStatus !== 'idle' && effectiveStatus !== 'recording' && (
        <span className="tlAudioRecorder__status">{effectiveStatus}</span>
      )}
      {serverError && (
        <span className="tlAudioRecorder__status tlAudioRecorder__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLAudioRecorder;
