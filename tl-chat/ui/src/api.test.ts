import { describe, expect, it } from 'vitest'

import { turnsJsonToMessages } from './api'

/**
 * What a re-opened chat makes of the recorded turns.
 *
 * The rollback is the case this suite was written for (#410 reporting): the live notice is gone
 * the moment the page is reloaded, so the turn has to carry it in the transcript too - which it
 * does, as the `ROLLED_BACK` transaction part `ConversationLogListener` writes.
 */
describe('a persisted turn', () => {
  /** One turn, with the assistant parts the test at hand cares about. */
  const transcript = (parts: Record<string, unknown>[]): string =>
    JSON.stringify([
      {
        id: 't1',
        turnNumber: 1,
        timestamp: 1000,
        userParts: [{ id: 'u1', type: 'text', timestamp: 1000, textData: { text: 'Bitte ändern' } }],
        assistantParts: parts,
      },
    ])

  it('restores a rolled-back transaction as a notice of that turn', () => {
    const messages = turnsJsonToMessages(
      transcript([
        { id: 'a1', type: 'text', timestamp: 1100, textData: { text: 'Erledigt.' } },
        {
          id: 'tx1',
          type: 'transaction',
          timestamp: 1200,
          transactionData: {
            type: 'ROLLED_BACK',
            revisionNumber: -1,
            reason: 'Pre-commit security check failed',
          },
        },
      ]),
    )

    const notice = messages.find((m) => m.type === 'notice')
    expect(notice).toBeDefined()
    expect(notice && 'kind' in notice ? notice.kind : '').toBe('transaction_rolled_back')
    expect(notice && 'detail' in notice ? notice.detail : '').toBe('Pre-commit security check failed')
    // In the turn, after the answer it contradicts - not appended to the end of the conversation.
    expect(messages.indexOf(notice!)).toBeGreaterThan(
      messages.findIndex((m) => m.type === 'assistant_text'),
    )
  })

  /** Begin and commit are the normal course of a turn that wrote something; the chat draws neither. */
  it('draws nothing for a transaction that began and committed', () => {
    const messages = turnsJsonToMessages(
      transcript([
        { id: 'tx0', type: 'transaction', timestamp: 1050, transactionData: { type: 'BEGAN', revisionNumber: -1 } },
        { id: 'a1', type: 'text', timestamp: 1100, textData: { text: 'Erledigt.' } },
        {
          id: 'tx1',
          type: 'transaction',
          timestamp: 1200,
          transactionData: { type: 'COMMITTED', revisionNumber: 42 },
        },
      ]),
    )

    expect(messages.some((m) => m.type === 'notice')).toBe(false)
  })
})
