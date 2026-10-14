// The translations of the bridge (useI18N): a key is requested from the server once while its answer
// is under way, and an answer that changes nothing renders nothing again. Every answer re-renders
// all components using translations; requesting a key anew on each render before its answer came
// made a page of many controls request the same keys dozens of times, and the stream of answers
// re-rendered the page until React gave up with "Maximum update depth exceeded" (#185).

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { act, cleanup, render, screen } from '@testing-library/react';

/** The key of the translation the probe shows. */
const KEY = 'js.test.greeting';

/** What the probe shows before the translation has arrived. */
const FALLBACK = 'Hello';

/** The translation the server answers. */
const TRANSLATION = 'Hallo';

/** An answer of the server, sent when the test resolves it. */
interface PendingAnswer {
  resolve: (translations: Record<string, string>) => void;
}

/** Replaces fetch by a server whose answers the test sends one by one. */
function fakeServer(): { answers: PendingAnswer[]; fetch: ReturnType<typeof vi.fn> } {
  const answers: PendingAnswer[] = [];
  const fetch = vi.fn(() => new Promise<Response>(resolveResponse => {
    answers.push({
      resolve: translations => resolveResponse({ ok: true, json: async () => translations } as Response),
    });
  }));
  vi.stubGlobal('fetch', fetch);
  return { answers, fetch };
}

/** The bridge, loaded afresh for each test: the translations are cached for the page. */
async function load() {
  vi.resetModules();
  return await import('tl-react-bridge');
}

/** Lets the scheduled request go out and the answers be processed. */
async function settle() {
  await act(async () => {
    await new Promise(resolve => setTimeout(resolve, 0));
  });
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('useI18N', () => {
  it('requests a key once while its answer is under way', async () => {
    const { answers, fetch } = fakeServer();
    const { useI18N } = await load();
    let renders = 0;
    function Probe({ round }: { round: number }) {
      renders++;
      const i18n = useI18N({ [KEY]: FALLBACK });
      return <span data-testid="text" data-round={round}>{i18n[KEY]}</span>;
    }

    const { rerender } = render(<Probe round={0} />);
    await settle();
    for (let round = 1; round <= 5; round++) {
      rerender(<Probe round={round} />);
      await settle();
    }
    expect(fetch).toHaveBeenCalledTimes(1);
    expect(screen.getByTestId('text').textContent).toBe(FALLBACK);

    await act(async () => answers[0].resolve({ [KEY]: TRANSLATION }));
    await settle();
    expect(screen.getByTestId('text').textContent).toBe(TRANSLATION);

    const rendersAfterAnswer = renders;
    rerender(<Probe round={6} />);
    await settle();
    expect(fetch).toHaveBeenCalledTimes(1);
    expect(renders).toBe(rendersAfterAnswer + 1);
  });

  it('renders nothing again for an answer that changes no translation', async () => {
    const { answers, fetch } = fakeServer();
    const { useI18N } = await load();
    let renders = 0;
    function Probe() {
      renders++;
      const i18n = useI18N({ [KEY]: FALLBACK });
      return <span data-testid="text">{i18n[KEY]}</span>;
    }

    render(<Probe />);
    await settle();
    expect(fetch).toHaveBeenCalledTimes(1);
    const rendersBeforeAnswer = renders;

    // The server knows no translation of the key: the fallback stays, and nothing renders again.
    await act(async () => answers[0].resolve({}));
    await settle();
    expect(renders).toBe(rendersBeforeAnswer);
    expect(screen.getByTestId('text').textContent).toBe(FALLBACK);
  });
});
