import { React, useTLState, useTLDataUrl } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

type PlayerStatus = 'disabled' | 'idle' | 'loading' | 'playing' | 'paused';

const TLAudioPlayer: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  const hasAudio = !!state.hasAudio;

  const [status, setStatus] = React.useState<PlayerStatus>(hasAudio ? 'idle' : 'disabled');
  const audioRef = React.useRef<HTMLAudioElement | null>(null);
  const blobUrlRef = React.useRef<string | null>(null);

  // Sync disabled state when server-side hasAudio changes.
  React.useEffect(() => {
    if (!hasAudio) {
      // Stop playback and release resources.
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      if (blobUrlRef.current) {
        URL.revokeObjectURL(blobUrlRef.current);
        blobUrlRef.current = null;
      }
      setStatus('disabled');
    } else if (status === 'disabled') {
      setStatus('idle');
    }
  }, [hasAudio]);

  // Cleanup blob URL on unmount.
  React.useEffect(() => {
    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
      if (blobUrlRef.current) {
        URL.revokeObjectURL(blobUrlRef.current);
        blobUrlRef.current = null;
      }
    };
  }, []);

  const handleClick = React.useCallback(async () => {
    if (status === 'disabled' || status === 'loading') {
      return;
    }

    // Pause if currently playing.
    if (status === 'playing') {
      if (audioRef.current) {
        audioRef.current.pause();
      }
      setStatus('paused');
      return;
    }

    // Resume if paused and we still have an Audio object.
    if (status === 'paused' && audioRef.current) {
      audioRef.current.play();
      setStatus('playing');
      return;
    }

    // Fetch audio data if not yet cached.
    if (!blobUrlRef.current) {
      setStatus('loading');
      try {
        const resp = await fetch(dataUrl);
        if (!resp.ok) {
          console.error('[TLAudioPlayer] Failed to fetch audio:', resp.status);
          setStatus('idle');
          return;
        }
        const blob = await resp.blob();
        blobUrlRef.current = URL.createObjectURL(blob);
      } catch (e) {
        console.error('[TLAudioPlayer] Fetch error:', e);
        setStatus('idle');
        return;
      }
    }

    // Create Audio element and play.
    const audio = new Audio(blobUrlRef.current);
    audioRef.current = audio;
    audio.onended = () => {
      setStatus('idle');
    };
    audio.play();
    setStatus('playing');
  }, [status, dataUrl]);

  const buttonLabel =
    status === 'loading' ? 'Loading\u2026' :
    status === 'playing' ? 'Pause' :
    'Play';

  const isDisabled = status === 'disabled' || status === 'loading';

  return (
    <div className="tlAudioPlayer">
      <button
        type="button"
        className={`tlAudioPlayer__button${status === 'playing' ? ' tlAudioPlayer__button--playing' : ''}`}
        onClick={handleClick}
        disabled={isDisabled}
      >
        {buttonLabel}
      </button>
      {status === 'loading' && (
        <span className="tlAudioPlayer__status">Loading\u2026</span>
      )}
    </div>
  );
};

export default TLAudioPlayer;
