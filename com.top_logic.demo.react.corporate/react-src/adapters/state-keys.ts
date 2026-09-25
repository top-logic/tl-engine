// Names of the state keys and commands through which the TopLogic controls talk to the components
// the adapters replace. They are the wire contract of the server-side controls
// (ReactButtonControl, ReactCheckboxControl and their base classes).

/** Label of a button. */
export const STATE_LABEL = 'label';

/** Whether a button or field is disabled. */
export const STATE_DISABLED = 'disabled';

/** Whether the control is hidden. */
export const STATE_HIDDEN = 'hidden';

/** Explicit tooltip text of a button. */
export const STATE_TOOLTIP = 'tooltip';

/** Whether a button's command is the alternative in force (a pressed toggle). */
export const STATE_ACTIVE = 'active';

/** Appearance of a button (`primary`, `secondary`, …). */
export const STATE_APPEARANCE = 'appearance';

/** Tone of a button; {@link TONE_DANGER} marks a destructive action. */
export const STATE_TONE = 'tone';

/** Additional CSS classes declared on the command a button renders. */
export const STATE_CSS_CLASSES = 'cssClasses';

/** URL a button navigates to directly instead of sending {@link CMD_CLICK}. */
export const STATE_NAVIGATE_URL = 'navigateUrl';

/** Whether {@link STATE_NAVIGATE_URL} opens in a window of its own. */
export const STATE_NAVIGATE_NEW_WINDOW = 'navigateNewWindow';

/** Keyboard gesture that triggers a button within its keyboard scope. */
export const STATE_KEY_GESTURE = 'keyGesture';

/** Whether a field accepts input; `false` for a field shown in view mode. */
export const STATE_EDITABLE = 'editable';

/** Whether a field's value has an error. */
export const STATE_HAS_ERROR = 'hasError';

/** Value of {@link STATE_APPEARANCE} for the emphasized button. */
export const APPEARANCE_PRIMARY = 'primary';

/** Value of {@link STATE_TONE} for a destructive action. */
export const TONE_DANGER = 'danger';

/** Command a button sends when it is clicked. */
export const CMD_CLICK = 'click';
