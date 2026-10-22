import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createDraftReporter, DRAFT_PAUSE } from './draft'

/**
 * When the composer's draft reaches the host.
 *
 * The whole point of the reporter is that typing does not become one channel write per keystroke,
 * while the two moments a host acts on - the field losing the focus and the send - are not late.
 */
describe('the draft reporter', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('reports once for a burst of typing, with what was typed last', () => {
    const published: string[] = []
    const reporter = createDraftReporter((text) => published.push(text))

    reporter.report('W')
    reporter.report('Wa')
    reporter.report('Was')
    expect(published).toEqual([])

    vi.advanceTimersByTime(DRAFT_PAUSE)
    expect(published).toEqual(['Was'])
  })

  it('reports again once the typing goes on after a pause', () => {
    const published: string[] = []
    const reporter = createDraftReporter((text) => published.push(text))

    reporter.report('Was')
    vi.advanceTimersByTime(DRAFT_PAUSE)
    reporter.report('Was kostet das?')
    vi.advanceTimersByTime(DRAFT_PAUSE)

    expect(published).toEqual(['Was', 'Was kostet das?'])
  })

  it('does not wait on blur or before a send, and drops the pending report', () => {
    const published: string[] = []
    const reporter = createDraftReporter((text) => published.push(text))

    reporter.report('Was kos')
    reporter.flush('Was kostet das?')
    expect(published).toEqual(['Was kostet das?'])

    // The pending report is gone rather than arriving late with the older text.
    vi.advanceTimersByTime(DRAFT_PAUSE * 2)
    expect(published).toEqual(['Was kostet das?'])
  })

  it('never publishes the same text twice in a row', () => {
    const published: string[] = []
    const reporter = createDraftReporter((text) => published.push(text))

    reporter.flush('Was kostet das?')
    reporter.flush('Was kostet das?')
    reporter.report('Was kostet das?')
    vi.advanceTimersByTime(DRAFT_PAUSE)

    expect(published).toEqual(['Was kostet das?'])
  })

  it('reports the empty draft a send leaves behind', () => {
    const published: string[] = []
    const reporter = createDraftReporter((text) => published.push(text))

    reporter.flush('Was kostet das?')
    reporter.report('')
    vi.advanceTimersByTime(DRAFT_PAUSE)

    expect(published).toEqual(['Was kostet das?', ''])
  })

  it('sends nothing more once it is cancelled - the composer went away', () => {
    const published: string[] = []
    const reporter = createDraftReporter((text) => published.push(text))

    reporter.report('Was')
    reporter.cancel()
    vi.advanceTimersByTime(DRAFT_PAUSE * 2)

    expect(published).toEqual([])
  })
})
