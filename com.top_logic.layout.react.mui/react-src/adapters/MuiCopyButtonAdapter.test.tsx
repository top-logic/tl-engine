// The part CopyButton drawn with Material UI: a small MUI IconButton copying its text.

import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import type { CopyButtonPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiCopyButtonAdapter from './MuiCopyButtonAdapter';
import { describeCopyButton } from './copy-button-test-support';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: CopyButtonPartProps) {
  render(<MuiRoot><MuiCopyButtonAdapter {...props} /></MuiRoot>);
}

afterEach(() => {
  cleanup();
});

describe('the part CopyButton as MUI IconButton', () => {
  it('draws a small MUI icon button, the className and attributes on the element around it', () => {
    draw({ text: 'a', label: 'Kopieren', copiedLabel: 'Kopiert', failedLabel: 'Fehler', className: 'x',
      'data-testid': 'c' });

    expect(screen.getByRole('button', { name: 'Kopieren' }).classList).toContain('MuiIconButton-sizeSmall');
    expect(screen.getByTestId('c').classList).toContain('x');
  });
});

describeCopyButton('MUI IconButton', draw);
