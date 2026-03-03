import { React, useTLState, useTLUpload, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.photoCapture.open': 'Open camera',
  'js.photoCapture.close': 'Close camera',
  'js.photoCapture.capture': 'Capture photo',
  'js.photoCapture.mirror': 'Mirror camera',
  'js.uploading': 'Uploading\u2026',
  'js.photoCapture.error.denied': 'Camera access denied or unavailable.',
};

type LocalStatus = 'idle' | 'overlayOpen' | 'uploading';

const TLPhotoCapture: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const upload = useTLUpload();

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
  const [localError, setLocalError] = React.useState<string | null>(null);
  const [mirrored, setMirrored] = React.useState(false);
  const videoRef = React.useRef<HTMLVideoElement | null>(null);
  const streamRef = React.useRef<MediaStream | null>(null);
  const canvasRef = React.useRef<HTMLCanvasElement | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);
  const overlayRef = React.useRef<HTMLDivElement | null>(null);

  const serverError = state.error as string | null;

  const hasGetUserMedia = React.useMemo(
    () => !!(window.isSecureContext && navigator.mediaDevices?.getUserMedia),
    []
  );

  const stopCamera = React.useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop());
      streamRef.current = null;
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  }, []);

  const closeOverlay = React.useCallback(() => {
    stopCamera();
    setLocalStatus('idle');
  }, [stopCamera]);

  const handleCameraClick = React.useCallback(async () => {
    if (localStatus === 'uploading') {
      return;
    }

    setLocalError(null);

    if (!hasGetUserMedia) {
      // Fallback: trigger native file input.
      fileInputRef.current?.click();
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });
      streamRef.current = stream;
      setLocalStatus('overlayOpen');
    } catch (err) {
      console.error('[TLPhotoCapture] Camera access denied or unavailable:', err);
      setLocalError('js.photoCapture.error.denied');
      setLocalStatus('idle');
    }
  }, [localStatus, hasGetUserMedia]);

  const handleCapture = React.useCallback(async () => {
    if (localStatus !== 'overlayOpen') {
      return;
    }

    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas) {
      return;
    }

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }
    ctx.drawImage(video, 0, 0);

    stopCamera();
    setLocalStatus('uploading');

    canvas.toBlob(async (blob) => {
      if (!blob) {
        setLocalStatus('idle');
        return;
      }
      const formData = new FormData();
      formData.append('photo', blob, 'capture.jpg');
      await upload(formData);
      setLocalStatus('idle');
    }, 'image/jpeg', 0.85);
  }, [localStatus, upload, stopCamera]);

  const handleFallbackFile = React.useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setLocalStatus('uploading');
    const formData = new FormData();
    formData.append('photo', file, file.name);
    await upload(formData);
    setLocalStatus('idle');

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }, [upload]);

  // Wire stream to video element when overlay opens.
  React.useEffect(() => {
    if (localStatus === 'overlayOpen' && videoRef.current && streamRef.current) {
      videoRef.current.srcObject = streamRef.current;
    }
  }, [localStatus]);

  // Focus overlay and lock body scroll when overlay is open.
  React.useEffect(() => {
    if (localStatus !== 'overlayOpen') return;

    overlayRef.current?.focus();
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    return () => {
      document.body.style.overflow = prev;
    };
  }, [localStatus]);

  // Escape key closes overlay.
  React.useEffect(() => {
    if (localStatus !== 'overlayOpen') return;

    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        closeOverlay();
      }
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [localStatus, closeOverlay]);

  // Cleanup on unmount.
  React.useEffect(() => {
    return () => {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach(t => t.stop());
        streamRef.current = null;
      }
    };
  }, []);

  const t = useI18N(I18N_KEYS);

  const cameraLabel =
    localStatus === 'uploading' ? t['js.uploading'] :
    t['js.photoCapture.open'];

  const cameraBtnClasses = ['tlPhotoCapture__cameraBtn'];
  if (localStatus === 'uploading') cameraBtnClasses.push('tlPhotoCapture__cameraBtn--uploading');

  const videoClasses = ['tlPhotoCapture__overlayVideo'];
  if (mirrored) videoClasses.push('tlPhotoCapture__overlayVideo--mirrored');

  const mirrorBtnClasses = ['tlPhotoCapture__mirrorBtn'];
  if (mirrored) mirrorBtnClasses.push('tlPhotoCapture__mirrorBtn--active');

  return (
    <div id={controlId} className="tlPhotoCapture">
      <div className="tlPhotoCapture__controls">
        <button
          type="button"
          className={cameraBtnClasses.join(' ')}
          onClick={handleCameraClick}
          disabled={localStatus === 'uploading'}
          title={cameraLabel}
          aria-label={cameraLabel}
        >
          <span className="tlPhotoCapture__cameraIcon" />
        </button>
      </div>

      {!hasGetUserMedia && (
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          capture="environment"
          hidden
          onChange={handleFallbackFile}
        />
      )}

      <canvas ref={canvasRef} style={{ display: 'none' }} />

      {localStatus === 'overlayOpen' && (
        <div
          ref={overlayRef}
          className="tlPhotoCapture__overlay"
          role="dialog"
          aria-modal="true"
          tabIndex={-1}
        >
          <div className="tlPhotoCapture__overlayBackdrop" onClick={closeOverlay} />
          <div className="tlPhotoCapture__overlayContent">
            <video
              ref={videoRef}
              className={videoClasses.join(' ')}
              autoPlay
              muted
              playsInline
            />
            <div className="tlPhotoCapture__overlayToolbar">
              <button
                type="button"
                className={mirrorBtnClasses.join(' ')}
                onClick={() => setMirrored(m => !m)}
                title={t['js.photoCapture.mirror']}
                aria-label={t['js.photoCapture.mirror']}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="7 8 3 12 7 16" />
                  <polyline points="17 8 21 12 17 16" />
                  <line x1="12" y1="3" x2="12" y2="21" strokeDasharray="2 2" />
                </svg>
              </button>
              <button
                type="button"
                className="tlPhotoCapture__overlayCaptureBtn"
                onClick={handleCapture}
                title={t['js.photoCapture.capture']}
                aria-label={t['js.photoCapture.capture']}
              >
                <span className="tlPhotoCapture__overlayCaptureIcon" />
              </button>
              <button
                type="button"
                className="tlPhotoCapture__overlayCloseBtn"
                onClick={closeOverlay}
                title={t['js.photoCapture.close']}
                aria-label={t['js.photoCapture.close']}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="6" y1="6" x2="18" y2="18" />
                  <line x1="18" y1="6" x2="6" y2="18" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      )}

      {localError && (
        <span className="tlPhotoCapture__status tlPhotoCapture__status--error">{t[localError]}</span>
      )}
      {serverError && (
        <span className="tlPhotoCapture__status tlPhotoCapture__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLPhotoCapture;
