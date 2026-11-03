import { beforeEach, describe, expect, it } from 'vitest'
import {
  ARG_STREAM,
  CMD_ATTACH_STREAM,
  CMD_DETACH_STREAM,
  FIELD_ATTACHED,
  openChatStream,
  type StreamFrame,
} from './chat-stream'
import {
  answerCommands,
  emitPatch,
  expectEventsCalls,
  isSubscribed,
  resetTLBridge,
  tlCommand,
  tlCommands,
  type CommandResponse,
} from '../test/tl-react-bridge'

/**
 * The chat's frames on the window's event stream: the browser half of `ChatSSEHandler`, attached and
 * detached by commands of a chat control (`ChatControl`).
 */

/** What the next attach is answered with. */
let attachAnswer: () => Promise<CommandResponse | undefined>

/** Lets the pending `await`s run. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise((resolve) => setTimeout(resolve, 0))
  }
}

/** The commands sent so far: name and the stream they name. */
function sent(): { name: string; stream: unknown }[] {
  return tlCommands().map((command) => ({ name: command.name, stream: command.args?.[ARG_STREAM] }))
}

/** The listener id the stream subscribed to, from the token its attach named. */
function listenerOf(index: number): string {
  return 'tl-chat-stream:' + String(tlCommands()[index].args?.[ARG_STREAM])
}

beforeEach(() => {
  resetTLBridge()
  attachAnswer = async () => ({ success: true, [FIELD_ATTACHED]: true })
  answerCommands(async (name) => (name === CMD_ATTACH_STREAM ? attachAnswer() : { success: true }))
})

describe('the chat stream', () => {
  it('leaves checking that the first frames arrive to the attach command itself', async () => {
    // A proxy that forwards the window's stream in blocks of kilobytes holds back the small chat
    // frames; the window changes to polling only when it is told that events are due - which the
    // answer of every command tells it, the attach among them.
    openChatStream(tlCommand)
    await settle()

    expect(expectEventsCalls()).toBe(0)
  })

  it('asks for the frames of its conversation, under a listener id of its own', async () => {
    const stream = openChatStream(tlCommand)
    await settle()

    expect(sent()).toHaveLength(1)
    expect(sent()[0].name).toBe(CMD_ATTACH_STREAM)
    expect(typeof sent()[0].stream).toBe('string')
    expect(isSubscribed(listenerOf(0))).toBe(true)

    // A second stream - the one that replaces this after a silence - listens under another id, so
    // the frames of the replaced one reach nobody.
    const second = openChatStream(tlCommand)
    await settle()
    expect(listenerOf(1)).not.toBe(listenerOf(0))

    stream.close()
    second.close()
  })

  it('hands the frames of one patch to their listeners in order, after it says it is open', async () => {
    const stream = openChatStream(tlCommand)
    const seen: string[] = []
    for (const name of ['open', 'text_delta', 'run_completed']) {
      stream.addEventListener(name, (frame: StreamFrame) => seen.push(`${name} ${JSON.stringify(frame.data)}`))
    }
    await settle()
    emitPatch(listenerOf(0), {
      frames: [
        { event: 'text_delta', data: { text: 'Hallo', end: 5 } },
        { event: 'run_completed', data: { stopReason: 'COMPLETE' } },
      ],
    })

    expect(seen).toEqual([
      'open {}',
      'text_delta {"text":"Hallo","end":5}',
      'run_completed {"stopReason":"COMPLETE"}',
    ])

    stream.close()
  })

  it('stops listening when closed and tells the server, with the same id', async () => {
    const stream = openChatStream(tlCommand)
    const seen: unknown[] = []
    stream.addEventListener('text_delta', (frame) => seen.push(frame.data))
    await settle()
    const listener = listenerOf(0)

    stream.close()
    emitPatch(listener, { frames: [{ event: 'text_delta', data: { text: 'zu spät' } }] })

    expect(isSubscribed(listener)).toBe(false)
    expect(seen).toEqual([])
    expect(sent().map((command) => command.name)).toEqual([CMD_ATTACH_STREAM, CMD_DETACH_STREAM])
    expect(listenerOf(1)).toBe(listener)
  })

  it('takes back a stream it no longer wants when the server starts it after all', async () => {
    let answer: (value: CommandResponse | undefined) => void = () => undefined
    attachAnswer = () => new Promise((resolve) => (answer = resolve))
    const stream = openChatStream(tlCommand)
    await settle()

    stream.close()
    answer({ success: true, [FIELD_ATTACHED]: true })
    await settle()

    expect(sent().map((command) => command.name)).toEqual([CMD_ATTACH_STREAM, CMD_DETACH_STREAM])
  })

  it('reports an attach the server refused or did not answer, and listens no more', async () => {
    for (const refusal of [{ success: true, [FIELD_ATTACHED]: false }, undefined]) {
      resetTLBridge()
      answerCommands(async () => refusal)
      const stream = openChatStream(tlCommand)
      const errors: unknown[] = []
      stream.onerror = (error) => errors.push(error)
      await settle()

      expect(errors).toHaveLength(1)
      expect(isSubscribed(listenerOf(0))).toBe(false)
      // Nothing was started, so nothing is taken back.
      expect(sent().map((command) => command.name)).toEqual([CMD_ATTACH_STREAM])
    }
  })
})
