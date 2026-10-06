// The glyphs of the title-bar buttons of a window, drawn with the paths of the Material icons.

import { React } from 'tl-react-bridge';
import SvgIcon from '@mui/material/SvgIcon';
import type { SvgIconProps } from '@mui/material/SvgIcon';

/** The Material icon `Close`. */
const CLOSE_PATH = 'M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z';

/** The Material icon `CropSquare`: a window that can be maximized. */
const MAXIMIZE_PATH = 'M18 4H6c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 14H6V6h12v12z';

/** The Material icon `FilterNone`: a maximized window that can be restored. */
const RESTORE_PATH = 'M3 5H1v16c0 1.1.9 2 2 2h16v-2H3V5zm18-4H7c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V3c0-1.1-.9-2-2-2zm0 16H7V3h14v14z';

/** The glyph of a button closing a window or dismissing a message. */
export function CloseIcon(props: SvgIconProps) {
  return <SvgIcon fontSize="small" {...props}><path d={CLOSE_PATH} /></SvgIcon>;
}

/** The glyph of the button maximizing a window. */
export function MaximizeIcon(props: SvgIconProps) {
  return <SvgIcon fontSize="small" {...props}><path d={MAXIMIZE_PATH} /></SvgIcon>;
}

/** The glyph of the button giving a maximized window its bounds back. */
export function RestoreIcon(props: SvgIconProps) {
  return <SvgIcon fontSize="small" {...props}><path d={RESTORE_PATH} /></SvgIcon>;
}
