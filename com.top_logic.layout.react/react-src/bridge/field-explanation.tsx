import React from 'react';
import { usePart } from './parts';
import { useI18N } from './i18n';
import { ThemeIcon } from './ThemeIcon';
import { TOOLTIP_ATTR } from './tooltip-host';

const I18N_KEYS = {
  'js.formField.help': 'Help',
};

/** The icon of an explanation beside the label it explains. */
export const EXPLANATION_HELP_ICON = 'css:fa-regular fa-circle-question';

/** The icon of an explanation standing in for a label that is not shown. */
export const EXPLANATION_INFO_ICON = 'css:bi bi-info-circle';

/** The value of {@link TOOLTIP_ATTR} fetching the rich explanation the server holds for the control. */
const RICH_EXPLANATION = 'key:tooltip';

/** The value of {@link TOOLTIP_ATTR} declaring a plain text, followed by the text. */
const TEXT_PREFIX = 'text:';

/**
 * The tooltip declaration (a value of {@link TOOLTIP_ATTR}) of an explanation, `null` for none.
 *
 * @param rich Whether the server holds a rich explanation under the key `tooltip` for the enclosing
 *        control; it takes precedence over the texts.
 * @param texts Plain texts explaining the same thing, the first that is not empty is taken.
 */
export function explanationTooltip(rich: boolean, ...texts: (string | null | undefined)[]): string | null {
  if (rich) {
    return RICH_EXPLANATION;
  }
  const text = texts.find(candidate => !!candidate);
  return text ? TEXT_PREFIX + text : null;
}

/** The props of {@link FieldExplanation}. */
export interface FieldExplanationProps {
  /** What the icon explains, as tooltip declaration ({@link explanationTooltip}). */
  tooltip: string;

  /** The icon, a theme image in its encoded form; {@link EXPLANATION_HELP_ICON} or {@link EXPLANATION_INFO_ICON}. */
  image: string;

  /**
   * The ID of the element holding the help text the icon shows and hides. Absent: the icon explains
   * through its tooltip only, e.g. while a form shows values.
   */
  helpId?: string;

  /** Whether the help text {@link helpId} names is shown. */
  helpVisible?: boolean;

  /** Called when the icon is pressed to show or hide the help text {@link helpId} names. */
  onToggleHelp?: () => void;

  /** Classes marking the icon at the site it is placed; they carry no layout. */
  className?: string;
}

/**
 * The icon explaining a label, a field without a visible label or a title: the explanation is its
 * tooltip, and its name for assistive technology is "Help".
 *
 * <p>With a help text ({@link FieldExplanationProps.helpId}), the icon is a button of the part
 * `Button` that shows and hides it (`aria-expanded`, `aria-controls`); the tooltip declaration is
 * passed to the part as attribute, so that the part shows the explanation instead of its label.
 * Without one, the icon is an image carrying the tooltip.</p>
 */
export function FieldExplanation(props: FieldExplanationProps) {
  const { tooltip, image, helpId, helpVisible, onToggleHelp, className } = props;
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  const name = i18n['js.formField.help'];
  const declaration = { [TOOLTIP_ATTR]: tooltip };

  if (helpId !== undefined) {
    return (
      <Button label={name} image={image} display="icon-only" appearance="ghost" small className={className}
        onClick={onToggleHelp} aria-expanded={helpVisible === true} aria-controls={helpId} {...declaration} />
    );
  }
  return (
    <span role="img" aria-label={name} className={className} {...declaration}>
      <ThemeIcon encoded={image} className="tl-icon-sm" />
    </span>
  );
}
