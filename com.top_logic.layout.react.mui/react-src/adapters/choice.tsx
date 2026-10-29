import { React, useTLState, useTLCommand, usePart, CMD_VALUE_CHANGED, ThemeIcon } from 'tl-react-bridge';
import type { ColorRole, DropdownSelectStateJson, OptionDescriptor } from 'tl-react-bridge';
import Stack from '@mui/material/Stack';

export type { OptionDescriptor } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

/** Command sent when the user follows the link of a displayed value. */
export const CMD_GOTO = 'goto';

/** Argument of {@link CMD_GOTO}: the value of the option to display. */
export const ARG_OPTION = 'option';

/**
 * Command entering a text of the user's own as the value of a field that accepts free entry
 * (freeEntry). Must match `EnterTextArguments#COMMAND`.
 */
export const CMD_ENTER_TEXT = 'enterText';

/** Argument of {@link CMD_ENTER_TEXT}: the text entered. Must match `EnterTextArguments#TEXT`. */
export const ARG_TEXT = 'text';

/** Argument of `valueChanged`: the values of the chosen options. */
export const ARG_VALUE = 'value';

/** Size class of the design system for the icon of an option. */
const ICON_CLASS = 'tl-icon-sm';

/**
 * An option as its icon followed by its label; a value with a color role is the part `Chip` of that
 * role, as TopLogic draws it.
 */
export function OptionContent({ option }: { option: OptionDescriptor }) {
  const Chip = usePart('Chip');
  if (option.colorRole) {
    return <Chip colorRole={option.colorRole as ColorRole} icon={option.image} label={option.label} />;
  }
  return (
    <Stack component="span" direction="row" spacing={0.5} sx={{ alignItems: 'center' }}>
      {option.image && <ThemeIcon encoded={option.image} className={ICON_CLASS} />}
      <span>{option.label}</span>
    </Stack>
  );
}

/** The chosen values of a choice field and how to change them, see {@link useChoice}. */
export interface Choice {
  /** The state of the field. */
  state: Partial<DropdownSelectStateJson>;

  /** The chosen options: the last ones sent, until the server answers with its own. */
  value: OptionDescriptor[];

  /** The options that can be chosen. */
  options: OptionDescriptor[];

  /** Sends the given options as the new choice. */
  send: (selection: OptionDescriptor[]) => void;

  /**
   * Presses the given option: toggles it in or out of a field choosing several options; chooses it
   * in a field choosing one, where pressing the chosen option clears the field unless the field is
   * mandatory.
   */
  press: (option: OptionDescriptor) => void;

  /** Leads to the place the given option is displayed at. */
  goto: (optionValue: string) => void;
}

/**
 * The choice of a field with a `DropdownSelectState`, shared by the choice adapters.
 *
 * <p>A change is sent as `valueChanged` with the list of the values of the chosen options (also for
 * a field choosing one). The choice sent is shown at once and is what the next change starts from,
 * so a second click before the server's answer lands on the first; the server's answer replaces it.
 * A disabled field sends nothing.</p>
 */
export function useChoice(): Choice {
  const state = useTLState<Partial<DropdownSelectStateJson>>();
  const sendCommand = useTLCommand();
  const serverValue = state.value as OptionDescriptor[] | undefined;
  const [value, setValue] = useState<OptionDescriptor[]>(serverValue ?? []);
  const valueRef = useRef<OptionDescriptor[]>(serverValue ?? []);
  useEffect(() => {
    valueRef.current = serverValue ?? [];
    setValue(serverValue ?? []);
  }, [serverValue]);

  const disabled = state.disabled === true || state.editable === false;
  const multiSelect = state.multiSelect === true;
  const mandatory = state.mandatory === true;

  const send = useCallback((selection: OptionDescriptor[]) => {
    if (disabled) return;
    valueRef.current = selection;
    setValue(selection);
    sendCommand(CMD_VALUE_CHANGED, { [ARG_VALUE]: selection.map(option => option.value) });
  }, [disabled, sendCommand]);

  const toggle = useCallback((option: OptionDescriptor) => {
    const selection = valueRef.current;
    const selected = selection.some(v => v.value === option.value);
    send(selected ? selection.filter(v => v.value !== option.value) : [...selection, option]);
  }, [send]);

  const choose = useCallback((option: OptionDescriptor) => {
    const selected = valueRef.current.some(v => v.value === option.value);
    if (!selected) {
      send([option]);
    } else if (!mandatory) {
      send([]);
    }
  }, [send, mandatory]);

  const goto = useCallback((optionValue: string) => {
    sendCommand(CMD_GOTO, { [ARG_OPTION]: optionValue });
  }, [sendCommand]);

  return {
    state,
    value,
    options: (state.options ?? []) as OptionDescriptor[],
    send,
    press: multiSelect ? toggle : choose,
    goto,
  };
}
