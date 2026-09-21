import { React, useTLState, useI18N, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useImageSrc } from './imageSource';

const I18N_KEYS = {
  'js.image.alt': 'Image',
};

/**
 * Displays a picture.
 *
 * The box of the picture takes the size options; the picture fills it and is cropped or fitted
 * according to `fit`. Without any size option the box is as large as the picture, limited to the
 * width available. While there is no picture, an empty box keeps the space a size option reserves.
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
  const className = rootClassName(state, 'tlImage', `tlImage--fit-${fit}`);

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

  return (
    <div id={controlId} className={className} style={style}>
      {src ? (
        <img
          className="tlImage__image"
          src={src}
          loading={state.lazy ? 'lazy' : undefined}
          alt={(state.alt as string | undefined) || t['js.image.alt']}
        />
      ) : (
        <div className="tlImage__placeholder" />
      )}
    </div>
  );
};

export default TLImage;
