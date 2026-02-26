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

  const ariaLabel =
    status === 'loading' ? 'Loading\u2026' :
    status === 'playing' ? 'Pause audio' :
    status === 'disabled' ? 'No audio' :
    'Play audio';

  const isDisabled = status === 'disabled' || status === 'loading';

  const buttonClasses = ['tlAudioPlayer__button'];
  if (status === 'playing') buttonClasses.push('tlAudioPlayer__button--playing');
  if (status === 'loading') buttonClasses.push('tlAudioPlayer__button--loading');

  return (
    <div className="tlAudioPlayer">
      <button
        type="button"
        className={buttonClasses.join(' ')}
        onClick={handleClick}
        disabled={isDisabled}
        title={ariaLabel}
        aria-label={ariaLabel}
      >
        <span className={`tlAudioPlayer__icon${status === 'playing' ? ' tlAudioPlayer__icon--pause' : ''}`} />
      </button>
    </div>
  );
};

export default TLAudioPlayer;
