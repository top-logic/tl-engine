import * as bridge from 'tl-react-bridge'
import { attachChatStream, detachChatStream, type ChatAddress } from './api'
import { randomId } from './ids'

/**
 * One frame as the store's handlers read it: its payload as the window's stream delivered it,
 * already parsed - turning it back into text only for every handler to parse it again cost two
 * passes over each frame.
 */
export interface StreamFrame {
  data: unknown
}

/** What the store listens to: the frames by name, `open` once attached, and a failed attach. */
export interface ChatStream {
  addEventListener(name: string, listener: (frame: StreamFrame) => void): void
  /** Called with the reason when the server refused or did not answer the attach; the stream is closed then. */
  onerror: ((error: unknown) => void) | null
  close(): void
}

/** Prefix of the listener id, as `ChatSSEHandler` on the server writes it. */
const LISTENER_PREFIX = 'tl-chat-stream:'

/**
 * Opens the chat's frames on the event stream the browser window already holds.
 *
 * A browser opens at most six connections to one server over HTTP/1.1, for all its tabs together,
 * and every page of the React layer holds one of them for the window's own event stream. The chat
 * used to open a second one, an `EventSource` on `/chat/events`: three tabs then held all six, and
 * no request of any tab reached the server any more - no click, no navigation, no message. The
 * frames now come as patches on the window's stream, so a page holds one connection.
 *
 * The listener is subscribed before the server is asked, so no frame is missed, and its id is new
 * for every stream, so the frames of a stream this one replaces reach nobody.
 */
export function openChatStream(at: ChatAddress): ChatStream {
  return new WindowChatStream(at)
}

/**
 * Tells the window's event stream that the server is writing frames for this chat now, so that it
 * checks they arrive.
 *
 * A proxy that forwards the window's stream only in blocks of some kilobytes lets the start through
 * and holds back the small patches that follow; the stream then changes to polling, but only when
 * it learns that events are due. A React command tells it that by itself; a chat message goes
 * through `/chat/send` instead. An engine that polls by itself offers `expectEvents`; for one that
 * does not, `script/tl-window-stream.js` puts `TLWindowStream` in place (#940, temporary).
 */
export function expectFrames(): void {
  if (bridge.expectEvents) {
    bridge.expectEvents()
  } else {
    ;(window as unknown as { TLWindowStream?: { expect(): void } }).TLWindowStream?.expect()
  }
}

class WindowChatStream implements ChatStream {
  onerror: ((error: unknown) => void) | null = null

  private readonly token = randomId()

  private readonly listenerId = LISTENER_PREFIX + this.token

  private readonly listeners = new Map<string, ((frame: StreamFrame) => void)[]>()

  private closed = false

  private attached = false

  constructor(private readonly at: ChatAddress) {
    bridge.subscribe(this.listenerId, this.receive)
    void this.attach()
  }

  addEventListener(name: string, listener: (frame: StreamFrame) => void): void {
    const list = this.listeners.get(name) ?? []
    list.push(listener)
    this.listeners.set(name, list)
  }

  close(): void {
    if (this.closed) return
    this.closed = true
    bridge.unsubscribe(this.listenerId, this.receive)
    if (this.attached) detachChatStream(this.at, this.token)
  }

  private async attach(): Promise<void> {
    try {
      await attachChatStream(this.at, this.token)
    } catch (error) {
      if (this.closed) return
      this.close()
      this.onerror?.(error)
      return
    }
    if (this.closed) {
      // Closed while the server was being asked: take back what it has just started.
      detachChatStream(this.at, this.token)
      return
    }
    this.attached = true
    this.dispatch('open', {})
    // The server has just written this stream's first frames to the window.
    expectFrames()
  }

  /** A patch of the window's stream: the frames of one flush on the server, in order. */
  private readonly receive = (patch: Record<string, unknown>): void => {
    if (this.closed) return
    const frames = patch.frames
    if (!Array.isArray(frames)) return
    for (const frame of frames as { event?: unknown; data?: unknown }[]) {
      if (typeof frame?.event !== 'string') continue
      this.dispatch(frame.event, frame.data ?? {})
      if (this.closed) return
    }
  }

  private dispatch(name: string, data: unknown): void {
    for (const listener of this.listeners.get(name) ?? []) listener({ data })
  }
}
