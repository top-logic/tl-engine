/**
 * Setup of every test file of the module (see vitest.config.ts).
 *
 * Answers the translation requests of the i18n service (bridge/i18n.ts) with no translations, so that
 * the components show their fallback texts. A mount() makes the context path known, and the keys
 * requested by the rendered components go out to the server; without a server to answer them, each
 * such request would fail and log an error. A test that stubs {@link fetch} itself answers these
 * requests on its own.
 */

/** The fetch function of the test environment, used for every request but a translation request. */
const environmentFetch = globalThis.fetch;

globalThis.fetch = (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
  const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
  if (url.includes('react-api/i18n')) {
    return Promise.resolve(new Response('{}', { status: 200 }));
  }
  return environmentFetch(input, init);
};
