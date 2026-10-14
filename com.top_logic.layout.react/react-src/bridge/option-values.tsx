import React from 'react';
import type { DropdownSelectStateJson } from '../state/control-state';
import type { ChipTone } from './parts';
import { usePart } from './parts';

/**
 * One option of a choice field as the server describes it, a chosen one or one to choose.
 *
 * <p>The same descriptor carries an option of the list to choose from and a chosen value, so a
 * value looks the same wherever a control shows it. The server always sends its value and
 * label.</p>
 */
export type OptionDescriptor = Partial<DropdownSelectStateJson.Option>
  & Required<Pick<DropdownSelectStateJson.Option, 'value' | 'label'>>;

/** Stands between two values shown as plain text, as in the other lists of values on one line. */
const VALUE_SEPARATOR = ', ';

/** The class of the list of the design system. */
const VALUES_CLASS = 'tl-select__values';

/** The class of the separator between two values shown as plain text. */
const SEPARATOR_CLASS = 'tl-select__separator';

/**
 * The chosen values of a choice field that is not editable, in the list of the design system
 * (`tl-select__values`).
 *
 * <p>A value with a color role is the part `Chip` of that role, any other the part `FieldValue`;
 * both show the image of the option ahead of its label. A value the application displays somewhere
 * (`link`) is pressed to lead there: the chip then runs an action, the field value is a link.
 * Two values shown as plain text are separated by a comma, so that they do not run into each other;
 * a chip is set apart by its own outline. An empty choice shows an empty list - the label of the
 * empty option is an affordance of editing and would mislead here.</p>
 *
 * <p>Every control showing the chosen values of a choice field draws them through this list, also
 * in a customer component library, which draws the parts.</p>
 *
 * @param id the ID of the list, the control ID of the field
 * @param className further classes, e.g. the result of rootClassName
 * @param value the chosen values
 * @param onGoto leads to the place the value of the given option is displayed at
 */
export function OptionValues({
  id,
  className,
  value,
  onGoto,
}: {
  id?: string;
  className?: string;
  value: OptionDescriptor[];
  onGoto: (optionValue: string) => void;
}) {
  const Chip = usePart('Chip');
  const FieldValue = usePart('FieldValue');
  return (
    <span id={id} className={className ? VALUES_CLASS + ' ' + className : VALUES_CLASS}>
      {value.map((option, n) => {
        const goto = option.link ? () => onGoto(option.value) : undefined;
        return (
          <React.Fragment key={option.value}>
            {n > 0 && !value[n - 1].colorRole && !option.colorRole
              && <span className={SEPARATOR_CLASS}>{VALUE_SEPARATOR}</span>}
            {option.colorRole
              ? <Chip tone={option.colorRole as ChipTone} icon={option.image} label={option.label} onClick={goto} />
              : <FieldValue text={option.label} icon={option.image} onClick={goto} />}
          </React.Fragment>
        );
      })}
    </span>
  );
}
