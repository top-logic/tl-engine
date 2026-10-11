import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { openChatStream, type StreamFrame } from './chat-stream'
import { emitPatch, isSubscribed, resetTLBridge } from '../test/tl-react-bridge'

/**
 * The chat's frames on the window's event stream: the browser half of `ChatSSEHandler`.
 */

/** Every request the stream made: method and URL. */
let calls: { method: string; url: string }[] = []

/** What the next `POST /chat/stream` is answered with. */
let attachAnswer: () => Promise<unknown> = async () => ({ ok: true, status: 200, json: async () => ({}) })

const AT = { windowId: 'w1', chatId: 'chat' }

/** Lets the pending `await`s run. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise((resolve) => setTimeout(resolve, 0))
  }
}

/** The listener id the stream subscribed to, from the token its attach named. */
function listenerOf(url: string): string {
  return 'tl-chat-stream:' + new URL(url, 'http://host').searchParams.get('stream')
}

beforeEach(() => {
  calls = []
  attachAnswer = async () => ({ ok: true, status: 200, json: async () => ({}) })
  resetTLBridge()
  const globals = globalThis as unknown as Record<string, unknown>
  globals.window = { location: { pathname: '/app/view/chat' } }
  globals.fetch = vi.fn(async (url: string, init?: { method?: string }) => {
    calls.push({ method: init?.method ?? 'GET', url })
    if (init?.method === 'POST') return attachAnswer()
    return { ok: true, status: 200, json: async () => ({}) }
  })
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('the chat stream', () => {
  it('asks for the frames of its window and conversation, under a listener id of its own', async () => {
    const stream = openChatStream(AT)
    await settle()

    expect(calls).toHaveLength(1)
    expect(calls[0].method).toBe('POST')
    expect(calls[0].url).toContain('/chat/stream?stream=')
    expect(calls[0].url).toContain('windowId=w1')
    expect(calls[0].url).toContain('conversation=chat')
    expect(isSubscribed(listenerOf(calls[0].url))).toBe(true)

    // A second stream - the one that replaces this after a silence - listens under another id, so
    // the frames of the replaced one reach nobody.
    const second = openChatStream(AT)
    await settle()
    expect(listenerOf(calls[1].url)).not.toBe(listenerOf(calls[0].url))

    stream.close()
    second.close()
  })

  it('hands the frames of one patch to their listeners in order, after it says it is open', async () => {
    const stream = openChatStream(AT)
    const seen: string[] = []
    for (const name of ['open', 'text_delta', 'run_completed']) {
      stream.addEventListener(name, (frame: StreamFrame) => seen.push(`${name} ${frame.data}`))
    }
    await settle()
    emitPatch(listenerOf(calls[0].url), {
      frames: [
        { event: 'text_delta', data: { text: 'Hallo', end: 5 } },
        { event: 'run_completed', data: { stopReason: 'COMPLETE' } },
      ],
    })

    expect(seen).toEqual([
      'open ',
      'text_delta {"text":"Hallo","end":5}',
      'run_completed {"stopReason":"COMPLETE"}',
    ])

    stream.close()
  })

  it('stops listening when closed and tells the server, with the same id', async () => {
    const stream = openChatStream(AT)
    const seen: string[] = []
    stream.addEventListener('text_delta', (frame) => seen.push(frame.data))
    await settle()
    const listener = listenerOf(calls[0].url)

    stream.close()
    emitPatch(listener, { frames: [{ event: 'text_delta', data: { text: 'zu spät' } }] })

    expect(isSubscribed(listener)).toBe(false)
    expect(seen).toEqual([])
    expect(calls.map((call) => call.method)).toEqual(['POST', 'DELETE'])
    expect(listenerOf(calls[1].url)).toBe(listener)
  })

  it('takes back a stream it no longer wants when the server starts it after all', async () => {
    let answer: (value: unknown) => void = () => undefined
    attachAnswer = () => new Promise((resolve) => (answer = resolve))
    const stream = openChatStream(AT)
    await settle()

    stream.close()
    answer({ ok: true, status: 200, json: async () => ({}) })
    await settle()

    expect(calls.map((call) => call.method)).toEqual(['POST', 'DELETE'])
  })

  it('reports an attach the server refused, and listens no more', async () => {
    attachAnswer = async () => ({ ok: false, status: 503, json: async () => ({}), clone: () => ({ json: async () => ({}) }) })
    const stream = openChatStream(AT)
    const errors: unknown[] = []
    stream.onerror = (error) => errors.push(error)
    await settle()

    expect(errors).toHaveLength(1)
    expect(isSubscribed(listenerOf(calls[0].url))).toBe(false)
    // Nothing was started, so nothing is taken back.
    expect(calls.map((call) => call.method)).toEqual(['POST'])
  })
})
