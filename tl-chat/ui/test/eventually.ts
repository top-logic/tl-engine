/**
 * Waiting for the page to reach a state, in the chat's DOM tests.
 *
 * What a test waits for after an action - a fetch and its answer, a store notification, a React
 * commit and the effects behind it - is a chain of tasks whose length in time depends on how busy
 * the machine is. A test therefore polls for the state it asserts next instead of sleeping for a
 * fixed time: `check` is retried until it stops throwing, and its last failure is the test's failure
 * once the timeout has run out. A state that is reached at once costs one call.
 */

import { vi } from 'vitest'

/** How long a state may take to arrive, generous for a loaded CI node. */
const TIMEOUT_MS = 5_000

/** How often the state is looked at again while it has not arrived. */
const INTERVAL_MS = 10

/** Resolves once `check` passes; rejects with its failure once {@link TIMEOUT_MS} has run out. */
export function eventually(check: () => void): Promise<void> {
  return vi.waitFor(check, { timeout: TIMEOUT_MS, interval: INTERVAL_MS })
}
