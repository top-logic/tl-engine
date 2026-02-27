import { React, useTLState, useTLDataUrl, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.audioPlayer.play': 'Play audio',
  'js.audioPlayer.pause': 'Pause audio',
  'js.audioPlayer.noAudio': 'No audio',
  'js.loading': 'Loading\u2026',
};

type PlayerStatus = 'disabled' | 'idle' | 'loading' | 'playing' | 'paused';

const TLAudioPlayer: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();

  const hasAudio = !!state.hasAudio;
  const dataRevision: number = state.dataRevision ?? 0;

  const [status, setStatus] = React.useState<PlayerStatus>(hasAudio ? 'idle' : 'disabled');
  const audioRef = React.useRef<HTMLAudioElement | null>(null);
  const blobUrlRef = React.useRef<string | null>(null);
  const prevRevisionRef = React.useRef<number>(dataRevision);

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

  // Invalidate cached blob when server-side audio data is replaced.
  React.useEffect(() => {
    if (dataRevision === prevRevisionRef.current) {
      return;
    }
    prevRevisionRef.current = dataRevision;

    // Stop current playback.
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    // Release cached blob.
    if (blobUrlRef.current) {
      URL.revokeObjectURL(blobUrlRef.current);
      blobUrlRef.current = null;
    }
    // Reset to idle (the hasAudio effect will handle disabled if needed).
    if (status === 'playing' || status === 'paused' || status === 'loading') {
      setStatus('idle');
    }
  }, [dataRevision]);

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

  const t = useI18N(I18N_KEYS);
  const ariaLabel =
    status === 'loading' ? t['js.loading'] :
    status === 'playing' ? t['js.audioPlayer.pause'] :
    status === 'disabled' ? t['js.audioPlayer.noAudio'] :
    t['js.audioPlayer.play'];

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
