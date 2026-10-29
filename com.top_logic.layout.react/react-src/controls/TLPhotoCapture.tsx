import {
  React, useTLState, useUpload, useI18N, usePart, useStandaloneKeyboardScope, useFocusTrap, createPortal,
  anchoredOverlayProps, rootClassName, ICON_CAMERA, ICON_MIRROR, ICON_CLOSE,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.photoCapture.open': 'Open camera',
  'js.photoCapture.close': 'Close camera',
  'js.photoCapture.capture': 'Capture photo',
  'js.photoCapture.mirror': 'Mirror camera',
  'js.photoCapture.error.denied': 'Camera access denied or unavailable.',
};

/** The name of the multipart part carrying the photo. */
const PART_PHOTO = 'photo';

/** The file name a captured photo is sent under. */
const CAPTURE_FILE_NAME = 'capture.jpg';

/** The type and the quality a captured photo is encoded with. */
const CAPTURE_TYPE = 'image/jpeg';
const CAPTURE_QUALITY = 0.85;

/** Releases the camera. */
function stopTracks(stream: MediaStream | null) {
  stream?.getTracks().forEach(track => track.stop());
}

/**
 * Takes a photo with the camera and uploads it (part `photo`).
 *
 * <p>The camera button opens a dialog of the design system (`tl-dialog`), portaled to the body: the
 * camera picture, a mirror toggle and the capture button, which has the focus, so Enter takes the
 * photo. Escape, a click on the scrim and the close button close the dialog; the focus returns to the
 * camera button. The dialog is purely a client one: its Escape scope is registered when it opens, so
 * inside a window it closes first, and the window stays.</p>
 *
 * <p>Without camera access in the browser (no secure context), the button opens the file selection of
 * the device instead. While the photo is uploaded, the camera button is busy and disabled. A device
 * error and the error of the server are announced in the status line.</p>
 */
const TLPhotoCapture: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const { uploading, error, upload } = useUpload();
  const Button = usePart('Button');

  const [open, setOpen] = React.useState(false);
  const [localError, setLocalError] = React.useState<string | null>(null);
  const [mirrored, setMirrored] = React.useState(false);
  const videoRef = React.useRef<HTMLVideoElement | null>(null);
  const streamRef = React.useRef<MediaStream | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement | null>(null);
  const surfaceRef = React.useRef<HTMLDivElement | null>(null);
  const cameraButtonRef = React.useRef<HTMLElement | null>(null);
  const captureButtonRef = React.useRef<HTMLElement | null>(null);
  const mountedRef = React.useRef(true);

  const hasGetUserMedia = React.useMemo(
    () => !!(window.isSecureContext && navigator.mediaDevices?.getUserMedia),
    []
  );

  const stopCamera = React.useCallback(() => {
    stopTracks(streamRef.current);
    streamRef.current = null;
    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }
  }, []);

  const closeDialog = React.useCallback(() => {
    stopCamera();
    setOpen(false);
  }, [stopCamera]);

  const handleCameraClick = React.useCallback(async () => {
    if (uploading || open) {
      return;
    }

    setLocalError(null);

    if (!hasGetUserMedia) {
      // Fallback: the file selection of the device.
      fileInputRef.current?.click();
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }
      });
      if (!mountedRef.current) {
        // Unmounted while the browser asked for the camera.
        stopTracks(stream);
        return;
      }
      streamRef.current = stream;
      setOpen(true);
    } catch (err) {
      console.error('[TLPhotoCapture] Camera access denied or unavailable:', err);
      setLocalError('js.photoCapture.error.denied');
    }
  }, [uploading, open, hasGetUserMedia]);

  const handleCapture = React.useCallback(() => {
    const video = videoRef.current;
    if (!video) {
      return;
    }

    const canvas = document.createElement('canvas');
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }
    ctx.drawImage(video, 0, 0);

    closeDialog();

    canvas.toBlob((blob) => {
      if (blob) {
        upload([{ name: PART_PHOTO, file: blob, fileName: CAPTURE_FILE_NAME }]);
      }
    }, CAPTURE_TYPE, CAPTURE_QUALITY);
  }, [upload, closeDialog]);

  const handleFallbackFile = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    // Reset so picking the same file again still fires a change event.
    e.target.value = '';
    if (file) {
      upload([{ name: PART_PHOTO, file, fileName: file.name }]);
    }
  }, [upload]);

  // Wire the stream to the video when the dialog opens.
  React.useEffect(() => {
    if (open && videoRef.current && streamRef.current) {
      videoRef.current.srcObject = streamRef.current;
    }
  }, [open]);

  // The dialog is modal: the focus moves to the capture button, stays in the surface, and returns to the
  // element that had it. Escape closes the dialog through the shared keyboard dispatcher; its scope is
  // registered when the dialog opens, so it is the newest one and wins over a window around the control.
  useFocusTrap(open, surfaceRef, () => captureButtonRef.current);
  useStandaloneKeyboardScope(open, { ESCAPE: () => { closeDialog(); return true; } });

  // The trap returns the focus to the element that had it; if that is gone, the camera button takes it.
  React.useEffect(() => {
    if (!open) {
      return;
    }
    return () => {
      const anchor = cameraButtonRef.current;
      queueMicrotask(() => {
        if (anchor?.isConnected && (document.activeElement === document.body || document.activeElement === null)) {
          anchor.focus();
        }
      });
    };
  }, [open]);

  // Unmounted: release the camera.
  React.useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      stopTracks(streamRef.current);
      streamRef.current = null;
    };
  }, []);

  const t = useI18N(I18N_KEYS);
  const titleId = controlId + '-camera-title';

  return (
    <div id={controlId} className={rootClassName(state, 'tl-upload')}>
      <Button
        ref={cameraButtonRef}
        label={t['js.photoCapture.open']}
        image={ICON_CAMERA}
        display="icon-only"
        appearance="secondary"
        disabled={uploading}
        aria-busy={uploading ? true : undefined}
        onClick={handleCameraClick}
      />

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

      {open && createPortal(
        <div
          className="tl-dialog"
          data-tl-state="entering"
          onClick={(e) => { if (e.target === e.currentTarget) closeDialog(); }}
          {...anchoredOverlayProps}
        >
          <div
            ref={surfaceRef}
            className="tl-dialog__surface"
            role="dialog"
            aria-modal="true"
            aria-labelledby={titleId}
          >
            <div className="tl-titlebar">
              <h2 className="tl-titlebar__title tl-type-heading-sm" id={titleId}>{t['js.photoCapture.open']}</h2>
              <Button
                label={t['js.photoCapture.close']}
                image={ICON_CLOSE}
                display="icon-only"
                appearance="ghost"
                small
                onClick={closeDialog}
              />
            </div>
            <div className="tl-dialog__body">
              <video
                ref={videoRef}
                className="tl-media__frame"
                data-tl-state={mirrored ? 'mirrored' : undefined}
                autoPlay
                muted
                playsInline
              />
            </div>
            <div className="tl-button-bar">
              <Button
                label={t['js.photoCapture.mirror']}
                image={ICON_MIRROR}
                display="icon-label"
                appearance="secondary"
                checked={mirrored}
                onClick={() => setMirrored(m => !m)}
              />
              <Button
                ref={captureButtonRef}
                label={t['js.photoCapture.capture']}
                image={ICON_CAMERA}
                display="icon-label"
                appearance="primary"
                onClick={handleCapture}
              />
            </div>
          </div>
        </div>,
        document.body,
      )}

      {localError && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{t[localError]}</span>
      )}
      {error && (
        <span className="tl-file-upload__status tl-type-label" role="alert">{error}</span>
      )}
    </div>
  );
};

export default TLPhotoCapture;
