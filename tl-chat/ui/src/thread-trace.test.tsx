// @vitest-environment jsdom
import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { Thread } from './thread'
import { chatReducer, type ChatAction } from './reducer'
import { bundle, type Language } from './i18n'
import type { ChatMessage } from './types'

/**
 * What the recorded trace says about an approval, once a reader opens it (Gitea #512).
 *
 * The history printed the server's English `displayReason` and the wire word for the outcome, so a
 * German chat recorded "Modifies shared application data." and "approved for session" under a card
 * that had been German throughout. Both now come from the same two functions the live card uses.
 *
 * A live DOM, because the trace panel renders its entries only when it is open and
 * `react-dom/server` cannot open it.
 */

let mounted: { root: Root; container: HTMLElement } | null = null

afterEach(() => {
  mounted?.root.unmount()
  mounted?.container.remove()
  mounted = null
})

function settle(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 20))
}

/** The transcript in the document, with every collapsed trace panel opened. */
async function openTrace(actions: ChatAction[], language: Language): Promise<string> {
  let messages: ChatMessage[] = []
  for (const action of actions) messages = chatReducer(messages, action)

  const container = document.createElement('div')
  document.body.append(container)
  const root = createRoot(container)
  mounted = { root, container }
  root.render(
    React.createElement(Thread, {
      messages,
      traceMode: 'detailed',
      welcomeName: 'Nutzer',
      busy: false,
      regenerating: false,
      feedback: [],
      labels: bundle(language),
      language,
      effort: '',
      onEditSend: async () => true,
      onRegenerate: () => undefined,
      onRate: () => undefined,
      onOpenRail: () => undefined,
      railCollapsed: true,
      onToggleRail: () => undefined,
      title: 'Titel',
    } as never),
  )
  await settle()
  for (const head of container.querySelectorAll('.trace-panel-head, .trace-entry-head')) {
    ;(head as HTMLElement).click()
    await settle()
  }
  return container.innerHTML
}

const APPROVED: ChatAction[] = [
  { type: 'ADD_USER', id: 'u1', text: 'Schreib das auf' },
  { type: 'START_RUN', runId: 'r1' },
  {
    type: 'ADD_APPROVAL',
    approvalRequestId: 'p1',
    toolCallId: 't1',
    toolName: 'consulting.recordStatement',
    argsJson: '{"text":"FACT-004"}',
    displayReason: 'Modifies shared application data.',
    toolMode: 'DATA_WRITE',
  },
  { type: 'RESOLVE_APPROVAL', approvalRequestId: 'p1', outcome: 'APPROVED_FOR_SESSION' },
  { type: 'END_RUN', runId: 'r1' },
]

describe('the recorded approval', () => {
  it('reads in German in a German chat', async () => {
    const html = await openTrace(APPROVED, 'de')
    expect(html).toContain('Ändert gemeinsame Anwendungsdaten.')
    expect(html).toContain('für diesen Chat freigegeben')
    expect(html).not.toContain('Modifies shared application data.')
    expect(html).not.toContain('approved for session')
  })

  it('reads in English in an English chat', async () => {
    const html = await openTrace(APPROVED, 'en')
    expect(html).toContain('Changes shared application data.')
    expect(html).toContain('approved for this chat')
  })

  it('says a denied one was denied', async () => {
    const denied: ChatAction[] = [
      ...APPROVED.slice(0, -2),
      { type: 'RESOLVE_APPROVAL', approvalRequestId: 'p1', outcome: 'REJECTED' },
      { type: 'END_RUN', runId: 'r1' },
    ]
    expect(await openTrace(denied, 'de')).toContain('abgelehnt')
  })
})
