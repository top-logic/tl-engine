import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const leavePageWhenAnswered = vi.fn();
const enqueueCommand = vi.fn((_url: string, _payload: unknown, _options?: unknown) => Promise.resolve(undefined));

vi.mock('./command-channel', () => ({
  BACKGROUND_COMMAND: { background: true },
  leavePageWhenAnswered: (url?: string) => leavePageWhenAnswered(url),
  enqueueCommand: (url: string, payload: unknown, options?: unknown) => enqueueCommand(url, payload, options),
}));

vi.mock('./tl-react-bridge', () => ({ getApiBase: () => '/ctx/' }));

import { ARG_URL, CMD_OPEN_URL_REFUSED, handleOpenUrl } from './open-url';

/** The window the stand-in for {@code window.open} hands out, recording where it is sent. */
let opened: { opener: unknown; location: { href: string } };

let open: ReturnType<typeof vi.spyOn>;

beforeEach(() => {
  leavePageWhenAnswered.mockReset();
  enqueueCommand.mockClear();
  document.body.dataset.windowName = 'w1';
  opened = { opener: window, location: { href: 'about:blank' } };
  open = vi.spyOn(window, 'open').mockImplementation(() => opened as unknown as Window);
  vi.spyOn(console, 'warn').mockImplementation(() => {});
});

afterEach(() => {
  vi.restoreAllMocks();
});

describe('an address opened in a window of its own', () => {
  it('opens a window cut off from the page and sends it to the address', () => {
    handleOpenUrl({ url: 'https://example.com/a?b=c', newWindow: true });

    expect(open).toHaveBeenCalledWith('', '_blank');
    expect(opened.opener).toBeNull();
    expect(opened.location.href).toBe('https://example.com/a?b=c');
    expect(leavePageWhenAnswered).not.toHaveBeenCalled();
  });

  it('opens in a window of its own when the event does not say otherwise', () => {
    handleOpenUrl({ url: 'https://example.com/' });

    expect(open).toHaveBeenCalledTimes(1);
  });

  it('resolves a relative address against the page', () => {
    handleOpenUrl({ url: 'docs/help.html', newWindow: true });

    expect(opened.location.href).toBe(new URL('docs/help.html', document.baseURI).href);
  });

  it('reports a window the browser refuses to the server, in the background', () => {
    open.mockImplementation(() => null);

    handleOpenUrl({ url: 'https://example.com/', newWindow: true });

    expect(enqueueCommand).toHaveBeenCalledWith('/ctx/react-api/command', {
      controlId: '',
      command: CMD_OPEN_URL_REFUSED,
      windowName: 'w1',
      arguments: { [ARG_URL]: 'https://example.com/' },
    }, { background: true });
  });

  it('reports nothing for a window the browser opens', () => {
    handleOpenUrl({ url: 'https://example.com/', newWindow: true });

    expect(enqueueCommand).not.toHaveBeenCalled();
  });
});

describe('an address opened in the window of the application', () => {
  it('leaves the page once the commands are answered', () => {
    handleOpenUrl({ url: 'https://example.com/', newWindow: false });

    expect(leavePageWhenAnswered).toHaveBeenCalledWith('https://example.com/');
    expect(open).not.toHaveBeenCalled();
  });
});

describe('an address another application opens', () => {
  it('is handed to the browser as a clicked link, whatever the window', () => {
    const clicked: string[] = [];
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (this: HTMLAnchorElement) {
      clicked.push(this.href);
    });

    handleOpenUrl({ url: 'mailto:info@example.com', newWindow: true });
    handleOpenUrl({ url: 'tel:+4912345', newWindow: false });

    expect(click).toHaveBeenCalledTimes(2);
    expect(clicked).toEqual(['mailto:info@example.com', 'tel:+4912345']);
    expect(open).not.toHaveBeenCalled();
    expect(leavePageWhenAnswered).not.toHaveBeenCalled();
  });
});

describe('an address not to open', () => {
  it('ignores an empty address', () => {
    handleOpenUrl({ url: '', newWindow: true });
    handleOpenUrl({ url: '', newWindow: false });

    expect(open).not.toHaveBeenCalled();
    expect(leavePageWhenAnswered).not.toHaveBeenCalled();
  });

  it('refuses an address running script', () => {
    handleOpenUrl({ url: 'javascript:alert(1)', newWindow: true });
    handleOpenUrl({ url: ' JavaScript:alert(1)', newWindow: false });
    handleOpenUrl({ url: 'data:text/html,<script>alert(1)</script>', newWindow: true });

    expect(open).not.toHaveBeenCalled();
    expect(leavePageWhenAnswered).not.toHaveBeenCalled();
  });
});
