import { React, useTLState, useImageSrc, useI18N, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.image.alt': 'Image',
};

/**
 * Displays a picture.
 *
 * The box of the picture takes the size options; the picture fills it and is cropped or fitted
 * according to `fit`. Without any size option the box is as large as the picture, limited to the
 * width available. While there is no picture, an empty box keeps the space a size option reserves.
 * While the picture of a box with a height or an aspect ratio loads, a skeleton (part `Skeleton`) fills the box and
 * the box is `aria-busy`; it disappears when the picture has loaded or failed to load.
 *
 * State:
 * - url: string | null
 * - hasData: boolean
 * - dataRevision: number
 * - alt: string | null
 * - fit: "cover" | "contain"
 * - aspectRatio: string | null
 * - width: string | null
 * - height: string | null
 * - lazy: boolean
 */
const TLImage: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const src = useImageSrc();
  const t = useI18N(I18N_KEYS);

  const fit = (state.fit as string) ?? 'cover';
  const Skeleton = usePart('Skeleton');

  // The address whose picture has loaded or failed to load; a new address loads again.
  const [doneSrc, setDoneSrc] = React.useState<string | null>(null);
  const imgRef = React.useRef<HTMLImageElement>(null);
  const finish = React.useCallback(() => setDoneSrc(src), [src]);

  // A picture the browser has at hand is complete before an event could report it.
  React.useLayoutEffect(() => {
    if (imgRef.current?.complete) {
      finish();
    }
  }, [finish]);

  const style: React.CSSProperties = {};
  if (state.aspectRatio) {
    style.aspectRatio = state.aspectRatio as string;
  }
  if (state.width) {
    style.width = state.width as string;
  }
  if (state.height) {
    style.height = state.height as string;
  }

  // Only a box with a height or proportions knows the shape of the picture before it is there; a box
  // with a width alone is as high as the picture, which it does not know yet.
  const sized = Boolean(state.aspectRatio || state.height);
  const loading = sized && src !== null && doneSrc !== src;

  return (
    <div id={controlId} className={rootClassName(state, 'tl-media', `tl-media--${fit}`)} style={style}
      aria-busy={loading || undefined}>
      {src ? (
        <img
          ref={imgRef}
          className="tl-media__frame"
          src={src}
          onLoad={finish}
          onError={finish}
          loading={state.lazy ? 'lazy' : undefined}
          alt={(state.alt as string | undefined) || t['js.image.alt']}
        />
      ) : (
        <div className="tl-media__placeholder" />
      )}
      {loading && (
        <div className="tl-media__loading">
          <Skeleton shape="rect" />
        </div>
      )}
    </div>
  );
};

export default TLImage;
