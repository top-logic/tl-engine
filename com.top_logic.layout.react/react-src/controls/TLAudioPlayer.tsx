import { React, useTLState, useTLDataUrl, useI18N, usePart, rootClassName, ICON_PLAY, ICON_PAUSE } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.audioPlayer.play': 'Play audio',
  'js.audioPlayer.pause': 'Pause audio',
  'js.audioPlayer.noAudio': 'No audio',
};

type PlayerStatus = 'disabled' | 'idle' | 'loading' | 'playing' | 'paused';

/**
 * Plays the audio of the server control.
 *
 * <p>The button shows play or, while playing, pause, and is named as it shows; it is no toggle. While
 * the audio is loaded, it is busy. Without audio it is disabled and says so in its tooltip.</p>
 */
const TLAudioPlayer: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const dataUrl = useTLDataUrl();
  const Button = usePart('Button');

  const hasAudio = !!state.hasAudio;
  const dataRevision: number = (state.dataRevision as number | undefined) ?? 0;

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

  // The browser may refuse to play (no permission to autoplay, a format it cannot decode): the player stands ready.
  const play = React.useCallback((audio: HTMLAudioElement) => {
    setStatus('playing');
    audio.play().catch((e: unknown) => {
      console.error('[TLAudioPlayer] Playback failed:', e);
      if (audioRef.current === audio) {
        audioRef.current = null;
        setStatus('idle');
      }
    });
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
      play(audioRef.current);
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
    play(audio);
  }, [status, dataUrl, play]);

  const t = useI18N(I18N_KEYS);
  const playing = status === 'playing';

  return (
    <span id={controlId} className={rootClassName(state) || undefined}>
      <Button
        label={playing ? t['js.audioPlayer.pause'] : t['js.audioPlayer.play']}
        tooltip={status === 'disabled' ? t['js.audioPlayer.noAudio'] : undefined}
        image={playing ? ICON_PAUSE : ICON_PLAY}
        display="icon-only"
        appearance="secondary"
        disabled={status === 'disabled' || status === 'loading'}
        aria-busy={status === 'loading' ? true : undefined}
        onClick={handleClick}
      />
    </span>
  );
};

export default TLAudioPlayer;
