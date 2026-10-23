import { React, useTLState, useTLCommand, rootClassName, useButtonDefaults, usePart } from 'tl-react-bridge';
import type { TLCellProps, ToggleButtonStateJson, ButtonPartProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * Props accepted when TLToggleButton is used as a sub-component inside a composite control.
 *
 * All props are optional -- when omitted, the corresponding value is read from the
 * control state (for standalone usage via ReactToggleButtonControl).
 */
export interface TLToggleButtonProps {
  /** The command name to send on click.  Defaults to {@code "click"}. */
  command?: string;
  /** The button label.  Defaults to {@code state.label}. */
  label?: string;
  /** Whether the button is active.  Defaults to {@code state.active}. */
  active?: boolean;
  /** Whether the button is disabled.  Defaults to {@code false}. */
  disabled?: boolean;
}

/**
 * A toggle button rendered via React that sends a command to the server.
 *
 * <p>When mounted standalone (via {@code ReactToggleButtonControl}), it reads its label
 * and active state from the control state and sends the {@code "click"} command.</p>
 *
 * <p>When composed inside another React component, the parent passes {@code command},
 * {@code label}, {@code active}, and {@code disabled} as props to customise behaviour.</p>
 *
 * <p>Inside a menu ({@code ButtonDefaults.appearance} {@code menu-item}) it is a checkbox entry of
 * that menu ({@code menuitemcheckbox} with {@code aria-checked}; under {@code ButtonDefaults.radio} a {@code menuitemradio}). It shows no icon, so a compact
 * toolbar leaves it as it is.</p>
 *
 * <p>A hidden toggle button renders nothing.</p>
 *
 * <p>The toggle button is drawn by the part {@code Button} ({@link usePart}) as a toggle: a button
 * with a pressed state ({@link ButtonPartProps.checked}).</p>
 */
const TLToggleButton: React.FC<TLCellProps & TLToggleButtonProps> = ({ controlId, command, label, active, disabled }) => {
  const state = useTLState<Partial<ToggleButtonStateJson>>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? state.label;
  const resolvedActive = active ?? state.active === true;
  const resolvedDisabled = disabled ?? false;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  const Button = usePart('Button');

  if (state.hidden === true) {
    return null;
  }

  return (
    <Button
      id={controlId}
      label={resolvedLabel}
      display="label-only"
      appearance={defaults.appearance ?? 'secondary'}
      checked={resolvedActive}
      radio={defaults.radio}
      disabled={resolvedDisabled}
      className={rootClassName(state)}
      onClick={handleClick}
    />
  );
};

export default TLToggleButton;
