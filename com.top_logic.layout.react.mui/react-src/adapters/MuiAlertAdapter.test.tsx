// The wire contract of TLAlert, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { AlertStateJson } from 'tl-react-bridge';
import MuiAlertAdapter from './MuiAlertAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

/** Command dismissing the alert. */
const CMD_DISMISS = 'dismiss';

/** Argument of {@link CMD_DISMISS}: the generation of the content dismissed. */
const ARG_GENERATION = 'generation';

/** The label of the close button: the key, as the test stub translates each key to itself. */
const LABEL_DISMISS = 'js.alert.dismiss';

/** An icon font image in its encoded form. */
const IMAGE_WARNING = 'css:fas fa-triangle-exclamation';

function mountAlert(state: Partial<AlertStateJson>) {
  return mountAdapter(MuiAlertAdapter, { message: 'Die Frist läuft ab.', generation: 2, ...state });
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLAlert as MUI Alert', () => {
  it('renders an MUI alert of the severity of its variant, with title, message and the icon of the state', () => {
    mountAlert({ variant: 'warning', title: 'Achtung', icon: IMAGE_WARNING, cssClass: 'my-alert' });

    const alert = screen.getByRole('alert');
    expect(alert.id).toBe(CONTROL_ID);
    expect(alert.classList).toContain('MuiAlert-standard');
    expect(alert.classList).toContain('MuiAlert-colorWarning');
    expect(alert.classList).toContain('my-alert');
    expect(alert.querySelector('.MuiAlertTitle-root')?.textContent).toBe('Achtung');
    expect(alert.textContent).toContain('Die Frist läuft ab.');
    expect(alert.querySelector('.MuiAlert-icon i.fa-triangle-exclamation')).not.toBeNull();
  });

  it('announces an information politely, as a status', () => {
    mountAlert({});

    const status = screen.getByRole('status');
    expect(status.classList).toContain('MuiAlert-colorInfo');
    expect(screen.queryByRole('alert')).toBeNull();
  });

  it('renders its actions through the embedded controls', () => {
    mountAlert({ actions: [childControl('renew', 'Verlängern'), childControl('ignore', 'Ignorieren')] });

    const status = screen.getByRole('status');
    expect(status.querySelector('#renew')?.textContent).toBe('Verlängern');
    expect(status.querySelector('#ignore')?.textContent).toBe('Ignorieren');
  });

  it('sends dismiss with the generation of its content for its close button', async () => {
    const sent = mountAlert({ closable: true });

    await userEvent.click(screen.getByRole('button', { name: LABEL_DISMISS }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_DISMISS, { [ARG_GENERATION]: 2 }]]);
  });

  it('offers no close button unless closable', () => {
    mountAlert({});

    expect(screen.queryByRole('button')).toBeNull();
  });

  it('renders nothing while hidden', () => {
    mountAlert({ hidden: true });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
