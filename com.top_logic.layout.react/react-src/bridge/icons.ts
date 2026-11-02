/**
 * The glyphs the controls draw, by what they mean, in the encoded form {@link ThemeIcon} renders
 * (Font Awesome Free as an icon font).
 *
 * <p>A control names a glyph by one of these constants, never by a glyph string of its own: the
 * icon library is not settled (a later wave may replace Font Awesome), and a switch then changes
 * this file alone. Two meanings may share a glyph today and part later, so each meaning keeps its
 * own constant.</p>
 */

/** The handle an entry is dragged and moved by (select chip, column selection, value list). */
export const ICON_HANDLE = 'css:fa-solid fa-grip-vertical';

/** Shows a hidden entry (column selection: the eye of a hidden column). */
export const ICON_HIDDEN = 'css:fa-solid fa-eye-slash';

/** Hides a shown entry (column selection: the eye of a shown column). */
export const ICON_VISIBLE = 'css:fa-solid fa-eye';

/** Releases a frozen entry (column selection: the lock of a frozen column). */
export const ICON_LOCK = 'css:fa-solid fa-lock';

/** Freezes an entry (column selection: the lock of a column that is not frozen). */
export const ICON_UNLOCK = 'css:fa-solid fa-lock-open';

/** Removes one entry (select chip, filter chip, value list). */
export const ICON_REMOVE = 'css:fa-solid fa-xmark';

/** Clears an input (search, select). */
export const ICON_CLEAR = 'css:fa-solid fa-xmark';

/** Copies a text to the clipboard. */
export const ICON_COPY = 'css:fa-regular fa-copy';

/** Tells that a text was copied to the clipboard a moment ago. */
export const ICON_COPIED = 'css:fa-solid fa-check';

/** Tells that a text could not be copied to the clipboard. */
export const ICON_COPY_FAILED = 'css:fa-solid fa-triangle-exclamation';

/** Cancels a form without applying it. */
export const ICON_CANCEL = 'css:fa-solid fa-xmark';

/** Confirms a form. */
export const ICON_CONFIRM = 'css:fa-solid fa-check';

/** Adds an entry. */
export const ICON_ADD = 'css:fa-solid fa-plus';

/** The toggle of an expandable row; pointing right while closed, turned down by the stylesheet. */
export const ICON_EXPAND = 'css:fa-solid fa-chevron-right';

/** The arrow of a closed list that opens downwards. */
export const ICON_LIST_CLOSED = 'css:fa-solid fa-chevron-down';

/** The arrow of an open list. */
export const ICON_LIST_OPEN = 'css:fa-solid fa-chevron-up';

/** A search field. */
export const ICON_SEARCH = 'css:fa-solid fa-magnifying-glass';

/** Grouping rows by a column. */
export const ICON_GROUP = 'css:fa-solid fa-layer-group';

/** Ascending sort. */
export const ICON_SORT_ASC = 'css:fa-solid fa-arrow-up';

/** Descending sort. */
export const ICON_SORT_DESC = 'css:fa-solid fa-arrow-down';

/** A column filter. */
export const ICON_FILTER = 'css:fa-solid fa-filter';

/** The column selection of a table. */
export const ICON_COLUMNS = 'css:fa-solid fa-gear';

/** Saving the current criteria under a name. */
export const ICON_SAVE = 'css:fa-regular fa-bookmark';

/** Closes a surface: dialog, window, drawer, snackbar. */
export const ICON_CLOSE = 'css:fa-solid fa-xmark';

/** Maximizes a window. */
export const ICON_MAXIMIZE = 'css:fa-regular fa-window-maximize';

/** Gives a maximized window its size back. */
export const ICON_RESTORE = 'css:fa-regular fa-window-restore';

/** Chooses an icon (the icon field while it holds none). */
export const ICON_CHOOSE_ICON = 'css:fa-solid fa-icons';

/** Uploads a file (the file upload, the binary field). */
export const ICON_UPLOAD = 'css:fa-solid fa-upload';

/** Downloads a file (the download, the binary field). */
export const ICON_DOWNLOAD = 'css:fa-solid fa-download';

/** A file (the chip of an attached file). */
export const ICON_FILE = 'css:fa-regular fa-file';

/** Attaches files (the add button of the file chips). */
export const ICON_ATTACH = 'css:fa-solid fa-paperclip';

/** Starts a recording (the audio recorder). */
export const ICON_RECORD = 'css:fa-solid fa-circle';

/** Stops a recording (the audio recorder while it records). */
export const ICON_STOP = 'css:fa-solid fa-stop';

/** Plays audio (the audio player). */
export const ICON_PLAY = 'css:fa-solid fa-play';

/** Pauses audio (the audio player while it plays). */
export const ICON_PAUSE = 'css:fa-solid fa-pause';

/** Takes a photo (the camera button and the capture button of the camera dialog). */
export const ICON_CAMERA = 'css:fa-solid fa-camera';

/** Mirrors the camera picture (the camera dialog). */
export const ICON_MIRROR = 'css:fa-solid fa-left-right';

/** Prints the document (the print button over an HTML document). */
export const ICON_PRINT = 'css:fa-solid fa-print';

/** The button moving to the previous period, e.g. of a calendar. */
export const ICON_PREVIOUS = 'css:fa-solid fa-chevron-left';

/** The button moving to the next period, e.g. of a calendar. */
export const ICON_NEXT = 'css:fa-solid fa-chevron-right';

/** Scrolls a strip towards its start, showing what is hidden before (tab strip). */
export const ICON_SCROLL_START = 'css:fa-solid fa-chevron-left';

/** Scrolls a strip towards its end, showing what is hidden after (tab strip). */
export const ICON_SCROLL_END = 'css:fa-solid fa-chevron-right';

/** Bold text (the toolbar of the rich text editor). */
export const ICON_BOLD = 'css:fa-solid fa-bold';

/** Italic text (the toolbar of the rich text editor). */
export const ICON_ITALIC = 'css:fa-solid fa-italic';

/** Underlined text (the toolbar of the rich text editor). */
export const ICON_UNDERLINE = 'css:fa-solid fa-underline';

/** Struck-through text (the toolbar of the rich text editor). */
export const ICON_STRIKETHROUGH = 'css:fa-solid fa-strikethrough';

/** A paragraph of text (the paragraph format of the rich text editor). */
export const ICON_PARAGRAPH = 'css:fa-solid fa-paragraph';

/** A heading (the paragraph format of the rich text editor). */
export const ICON_HEADING = 'css:fa-solid fa-heading';

/** A bulleted list (the list kind of the rich text editor). */
export const ICON_BULLET_LIST = 'css:fa-solid fa-list-ul';

/** A numbered list (the list kind of the rich text editor). */
export const ICON_NUMBERED_LIST = 'css:fa-solid fa-list-ol';

/** A quotation (the toolbar of the rich text editor). */
export const ICON_QUOTE = 'css:fa-solid fa-quote-left';

/** A block of code (the toolbar of the rich text editor). */
export const ICON_CODE_BLOCK = 'css:fa-solid fa-code';

/** A link (the toolbar of the rich text editor). */
export const ICON_LINK = 'css:fa-solid fa-link';

/** Inserts an image (the toolbar of the rich text editor). */
export const ICON_IMAGE = 'css:fa-regular fa-image';

/** Inserts a table (the toolbar of the rich text editor). */
export const ICON_TABLE = 'css:fa-solid fa-table';

/** Undoes the last change (the toolbar of the rich text editor). */
export const ICON_UNDO = 'css:fa-solid fa-arrow-rotate-left';

/** Redoes the last undone change (the toolbar of the rich text editor). */
export const ICON_REDO = 'css:fa-solid fa-arrow-rotate-right';

/** Shows the HTML source in place of the formatted text (the toolbar of the rich text editor). */
export const ICON_SOURCE = 'css:fa-solid fa-file-code';
