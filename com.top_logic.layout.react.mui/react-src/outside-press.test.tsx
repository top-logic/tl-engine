// The outside press of tl-react-bridge (useCloseOnOutsidePress) with stacked surfaces: a sub-surface
// portaled to the body and marked as anchored overlay belongs to the surfaces below it.

import React from 'react';
import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render, fireEvent } from '@testing-library/react';
import { useCloseOnOutsidePress, anchoredOverlayProps, createPortal } from 'tl-react-bridge';

/** A surface closing on an outside press, optionally marked as anchored overlay and portaled. */
const Surface: React.FC<{
  name: string; closed: string[]; portaled?: boolean; children?: React.ReactNode;
}> = ({ name, closed, portaled, children }) => {
  const ref = React.useRef<HTMLDivElement>(null);
  useCloseOnOutsidePress(true, [ref], () => closed.push(name));
  const element = (
    <div ref={ref} data-testid={name} {...(portaled ? anchoredOverlayProps : {})}>{children}</div>
  );
  return portaled ? createPortal(element, document.body) : element;
};

/** Mounts a popover holding a select popup that is portaled to the body. */
function mountStack(closed: string[], selectOpen = true): ReturnType<typeof render> {
  const Stack: React.FC<{ selectOpen: boolean }> = ({ selectOpen: open }) => (
    <div>
      <div data-testid="page">page</div>
      <Surface name="popover" closed={closed} portaled>
        content
        {open && <Surface name="select" closed={closed} portaled>option</Surface>}
      </Surface>
    </div>
  );
  return render(<Stack selectOpen={selectOpen} />);
}

afterEach(cleanup);

describe('the outside press of stacked surfaces', () => {
  it('keeps both surfaces open on a press in the sub-surface portaled out of the outer one', () => {
    const closed: string[] = [];
    const { getByTestId } = mountStack(closed);

    fireEvent.pointerDown(getByTestId('select'));

    expect(closed).toEqual([]);
  });

  it('closes only the sub-surface on a press in the outer surface', () => {
    const closed: string[] = [];
    const { getByTestId } = mountStack(closed);

    fireEvent.pointerDown(getByTestId('popover'));

    expect(closed).toEqual(['select']);
  });

  it('closes both surfaces on a press outside of both', () => {
    const closed: string[] = [];
    const { getByTestId } = mountStack(closed);

    fireEvent.pointerDown(getByTestId('page'));

    expect(closed.sort()).toEqual(['popover', 'select']);
  });

  it('closes a surface on a press in an anchored overlay stacked below it', () => {
    const closed: string[] = [];
    const Below: React.FC<{ above: boolean }> = ({ above }) => (
      <>
        <Surface name="menu" closed={closed} portaled>entry</Surface>
        {above && <Surface name="popover" closed={closed} portaled>content</Surface>}
      </>
    );
    const { getByTestId, rerender } = render(<Below above={false} />);
    // The popover becomes active after the menu, and so stands above it.
    rerender(<Below above={true} />);

    fireEvent.pointerDown(getByTestId('menu'));

    expect(closed).toEqual(['popover']);
  });

  it('keeps a surface open on a press in an anchored overlay that is no surface of the hook', () => {
    const closed: string[] = [];
    const { getByTestId } = render(
      <>
        <Surface name="popover" closed={closed} portaled>content</Surface>
        {createPortal(<div data-testid="library-popup" {...anchoredOverlayProps}>option</div>, document.body)}
      </>
    );

    fireEvent.pointerDown(getByTestId('library-popup'));

    expect(closed).toEqual([]);
  });
});
