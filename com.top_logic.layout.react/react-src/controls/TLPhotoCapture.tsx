import { React, useTLState, useTLUpload, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.photoCapture.open': 'Open camera',
  'js.photoCapture.close': 'Close camera',
  'js.photoCapture.capture': 'Capture photo',
  'js.uploading': 'Uploading\u2026',
};

type LocalStatus = 'idle' | 'previewing' | 'uploading';

const TLPhotoCapture: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const upload = useTLUpload();

  const [localStatus, setLocalStatus] = React.useState<LocalStatus>('idle');
  const videoRef = React.useRef<HTMLVideoElement | null>(null);
  const streamRef = React.useRef<MediaStream | null>(null);
  const canvasRef = React.useRef<HTMLCanvasElement | null>(null);

  const serverError = state.error as string | null;

  const stopCamera = React.useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop());
      streamRef.current = null;
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  }, []);

  const handleToggleCamera = React.useCallback(async () => {
    if (localStatus === 'uploading') {
      return; // Ignore clicks while uploading.
    }

    if (localStatus === 'previewing') {
      // Stop camera.
      stopCamera();
      setLocalStatus('idle');
      return;
    }

    // Start camera.
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });
      streamRef.current = stream;
      setLocalStatus('previewing');
    } catch (err) {
      console.error('[TLPhotoCapture] Camera access denied or unavailable:', err);
      setLocalStatus('idle');
    }
  }, [localStatus, stopCamera]);

  const handleCapture = React.useCallback(async () => {
    if (localStatus !== 'previewing') {
      return;
    }

    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas) {
      return;
    }

    // Draw the current video frame onto the canvas at native resolution.
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }
    ctx.drawImage(video, 0, 0);

    // Stop the camera tracks.
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

  // Wire stream to video element after React renders the <video>.
  React.useEffect(() => {
    if (localStatus === 'previewing' && videoRef.current && streamRef.current) {
      videoRef.current.srcObject = streamRef.current;
    }
  }, [localStatus]);

  // Cleanup: stop camera on unmount.
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
    localStatus === 'previewing' ? t['js.photoCapture.close'] :
    localStatus === 'uploading' ? t['js.uploading'] :
    t['js.photoCapture.open'];

  const isCameraDisabled = localStatus === 'uploading';

  const cameraBtnClasses = ['tlPhotoCapture__cameraBtn'];
  if (localStatus === 'previewing') cameraBtnClasses.push('tlPhotoCapture__cameraBtn--active');

  const captureBtnClasses = ['tlPhotoCapture__captureBtn'];
  if (localStatus === 'uploading') captureBtnClasses.push('tlPhotoCapture__captureBtn--uploading');

  return (
    <div className="tlPhotoCapture">
      <div className="tlPhotoCapture__controls">
        <button
          type="button"
          className={cameraBtnClasses.join(' ')}
          onClick={handleToggleCamera}
          disabled={isCameraDisabled}
          title={cameraLabel}
          aria-label={cameraLabel}
        >
          <span className="tlPhotoCapture__cameraIcon" />
        </button>
        {localStatus === 'previewing' && (
          <button
            type="button"
            className={captureBtnClasses.join(' ')}
            onClick={handleCapture}
            title={t['js.photoCapture.capture']}
            aria-label={t['js.photoCapture.capture']}
          >
            <span className="tlPhotoCapture__captureIcon" />
          </button>
        )}
      </div>
      {localStatus === 'previewing' && (
        <video
          ref={videoRef}
          className="tlPhotoCapture__preview"
          autoPlay
          muted
          playsInline
        />
      )}
      <canvas ref={canvasRef} style={{ display: 'none' }} />
      {serverError && (
        <span className="tlPhotoCapture__status tlPhotoCapture__status--error">{serverError}</span>
      )}
    </div>
  );
};

export default TLPhotoCapture;
