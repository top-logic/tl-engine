import React from 'react';
import { isInteractiveTarget } from './interactive';
import { tooltipProps } from './tooltip-host';
import { usePart } from './parts';
import { ICON_OPEN } from './icons';

/**
 * Elements the user opens: a tile of a dashboard with an action, an item of an activatable list.
 *
 * What an element offers to be opened is its {@link ActivationAction action}, which the server
 * publishes per element (the state of `ItemAction`): the name the element is announced under,
 * whether opening it is currently refused, and the text explaining it. How the element is opened
 * is its {@link ActivationMode mode}:
 *
 * - `click` - an element nothing else answers a click on (a tile, an item of a list that is not
 *   selectable): a click or tap anywhere on the element opens it, and so do `Enter` and `Space`.
 *   The keyboard reaches the element through a {@link ActivationButton button} out of sight but in
 *   the tab order, which names the element; the element shows the focus ring for it. A click on
 *   interactive content of the element (a button, a link, a field) is that content's, and a click
 *   that ends a text selection in the element only selects the text.
 * - `double` - an element a click selects (an option of a list box): a double-click and `Enter`
 *   open it, and so does its {@link ActivationOpenButton open button}, since a double-click is
 *   neither visible nor available on a touch screen. `Enter` on an element whose action is refused
 *   is left to the selection, which then selects the element.
 *
 * An element without action cannot be opened; one whose action is disabled shows it and does
 * nothing when opened.
 */

/**
 * What opening an element offers, as the server publishes it (`ItemAction`).
 */
export interface ActivationAction {
  /** The name the element is announced under, the name of its open button. */
  label: string;

  /** Whether opening the element is currently refused. */
  disabled: boolean;

  /** The text explaining the action, for a disabled one why it is refused. */
  tooltip?: string | null;
}

/** How an element is opened, see the description of this module. */
export type ActivationMode = 'click' | 'double';

/**
 * CSS class of an element a click opens: pointer and highlight on hover, the focus ring while its
 * {@link ACTIVATION_ACTION_CLASS button} has the keyboard focus (layout.css, base.css).
 */
export const ACTIVATABLE_CLASS = 'tl-activatable';

/** CSS class of the button out of sight naming an element a click opens. */
export const ACTIVATION_ACTION_CLASS = 'tl-activatable__action';

/** Attributes and handlers an element spreads onto itself to be opened. */
export interface ActivationProps {
  /** {@link ACTIVATABLE_CLASS} for an element a click opens, while it can be opened. */
  className?: string;
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;
  onDoubleClick?: (event: React.MouseEvent<HTMLElement>) => void;
  onMouseDown?: (event: React.MouseEvent<HTMLElement>) => void;
  onKeyDown?: (event: React.KeyboardEvent<HTMLElement>) => void;
  [tooltipAttr: string]: unknown;
}

/**
 * Whether a click on the given element ends a text selection within it: the user then marked text
 * rather than pressed the element.
 */
export function endsTextSelection(element: Element): boolean {
  const selection = typeof window !== 'undefined' ? window.getSelection() : null;
  if (!selection || selection.isCollapsed || selection.rangeCount === 0) {
    return false;
  }
  const range = selection.getRangeAt(0);
  return element.contains(range.commonAncestorContainer);
}

/**
 * The attributes and handlers making an element openable.
 *
 * @param action What opening the element offers; `null` or `undefined` for an element that cannot
 *        be opened, which then gets nothing.
 * @param activate Opens the element - the command of the control.
 * @param mode How the element is opened.
 * @param enabled Whether the element can be opened now; a dashboard being rearranged opens nothing.
 */
export function activationProps(
  action: ActivationAction | null | undefined,
  activate: () => void,
  mode: ActivationMode,
  enabled: boolean = true,
): ActivationProps {
  if (!action) {
    return {};
  }
  if (!enabled) {
    // The action is not offered now, but the element still says what it would do.
    return { ...tooltipProps(action.tooltip) };
  }
  const accepts = (event: React.SyntheticEvent<HTMLElement>) => !action.disabled && !isInteractiveTarget(event.target);
  if (mode === 'click') {
    return {
      className: ACTIVATABLE_CLASS,
      ...tooltipProps(action.tooltip),
      // The element's own button has sent the activation already when the press landed on it: it is
      // interactive, so the element is activated exactly once however it was pressed.
      onClick: event => {
        if (accepts(event) && !endsTextSelection(event.currentTarget)) {
          activate();
        }
      },
    };
  }
  return {
    ...tooltipProps(action.tooltip),
    onMouseDown: event => {
      // The second press of a double-click opens the element, it does not select a word.
      if (event.detail > 1 && !isInteractiveTarget(event.target)) {
        event.preventDefault();
      }
    },
    onDoubleClick: event => {
      if (accepts(event)) {
        activate();
      }
    },
    onKeyDown: event => {
      // Enter opens the element instead of selecting it; the server selects it first. A refused
      // element is only selected, by the handler of the selection, which skips a handled key.
      if (event.key === 'Enter' && event.target === event.currentTarget && !action.disabled
        && !event.ctrlKey && !event.metaKey && !event.shiftKey && !event.altKey) {
        event.preventDefault();
        activate();
      }
    },
  };
}

/** Properties of {@link ActivationButton}. */
export interface ActivationButtonProps {
  /** What opening the element offers. */
  action: ActivationAction;

  /** Opens the element. */
  activate: () => void;

  /** Whether the button is in the tab order; a dashboard being rearranged takes it out. */
  focusable?: boolean;
}

/**
 * The name and the keyboard operation of an element a click opens: a button of its own, out of
 * sight but in the tab order, operated by `Enter` and `Space` as any button is. It is a direct
 * child of the element, which shows the focus ring for it.
 */
export const ActivationButton: React.FC<ActivationButtonProps> = ({ action, activate, focusable = true }) => (
  <button
    type="button"
    className={`${ACTIVATION_ACTION_CLASS} tl-visually-hidden`}
    disabled={action.disabled}
    tabIndex={focusable ? 0 : -1}
    onClick={activate}
  >
    {action.label}
  </button>
);

/** Properties of {@link ActivationOpenButton}. */
export interface ActivationOpenButtonProps {
  /** What opening the element offers. */
  action: ActivationAction;

  /** Opens the element. */
  activate: () => void;

  /** Content placed beside the button in the corner, e.g. the check mark of a selected option. */
  children?: React.ReactNode;
}

/**
 * The open button of an element a click selects: a chevron in the top right corner of the element,
 * floating above its content (`tl-overlay__action` in a `tl-overlay__layer--top-right`). The element
 * must be positioned.
 */
export const ActivationOpenButton: React.FC<ActivationOpenButtonProps> = ({ action, activate, children }) => {
  const Button = usePart('Button');
  return (
    <div className="tl-overlay__layer tl-overlay__layer--top-right">
      {children}
      <div className="tl-overlay__action">
        <Button
          label={action.label}
          tooltip={action.tooltip ?? undefined}
          image={ICON_OPEN}
          display="icon-only"
          appearance="ghost"
          small
          disabled={action.disabled}
          onClick={() => activate()}
        />
      </div>
    </div>
  );
};
