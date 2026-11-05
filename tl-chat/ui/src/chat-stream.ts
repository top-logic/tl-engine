import * as bridge from 'tl-react-bridge'
import type { CommandOptions, CommandResponse } from 'tl-react-bridge'
import { randomId } from './ids'

/**
 * A command sent to a chat control of this conversation - one the store reaches through any of its
 * mounted elements (`useTLCommand`).
 */
export type ChatCommand = (
  command: string,
  args?: Record<string, unknown>,
  options?: CommandOptions,
) => Promise<CommandResponse | undefined>

/** Command that starts the conversation's frames. Must match `ChatControl#CMD_ATTACH_STREAM`. */
export const CMD_ATTACH_STREAM = 'attachStream'

/** Command that stops the frames of one attach. Must match `ChatControl#CMD_DETACH_STREAM`. */
export const CMD_DETACH_STREAM = 'detachStream'

/**
 * Command that sends the conversation's `run_state` frame again, on the stream. Must match
 * `ChatControl#CMD_REFRESH_RUN_STATE`.
 */
export const CMD_REFRESH_RUN_STATE = 'refreshRunState'

/** Argument naming the browser's part of the listener id. Must match `ChatControl#ARG_STREAM`. */
export const ARG_STREAM = 'stream'

/**
 * Field of the attach's answer: whether the stream was started. Must match
 * `StreamAttachReply.attached` (chat `messages.proto`).
 */
export const FIELD_ATTACHED = 'attached'

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

/** Prefix of the listener id. Must match `ChatSSEHandler#LISTENER_PREFIX`. */
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
 * for every stream, so the frames of a stream this one replaces reach nobody. The attach and the
 * detach are commands of a chat control ({@link CMD_ATTACH_STREAM}, {@link CMD_DETACH_STREAM}),
 * so they travel the window's command channel in order with everything else the page sends; the
 * answer of the attach is what tells the window's stream that the first frames are due.
 *
 * A page that is left sends no detach: the server stops the stream itself once the window's chat
 * elements are no longer displayed.
 *
 * @param command Sends a command to a chat control of the conversation.
 */
export function openChatStream(command: ChatCommand): ChatStream {
  return new WindowChatStream(command)
}

class WindowChatStream implements ChatStream {
  onerror: ((error: unknown) => void) | null = null

  private readonly token = randomId()

  private readonly listenerId = LISTENER_PREFIX + this.token

  private readonly listeners = new Map<string, ((frame: StreamFrame) => void)[]>()

  private closed = false

  private attached = false

  constructor(private readonly command: ChatCommand) {
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
    if (this.attached) this.detach()
  }

  private async attach(): Promise<void> {
    const answer = await this.command(CMD_ATTACH_STREAM, { [ARG_STREAM]: this.token })
    const started = answer?.[FIELD_ATTACHED] === true
    if (!started) {
      if (this.closed) return
      this.close()
      this.onerror?.(new Error('attach refused'))
      return
    }
    if (this.closed) {
      // Closed while the server was being asked: take back what it has just started.
      this.detach()
      return
    }
    this.attached = true
    this.dispatch('open', {})
  }

  /**
   * Stops the frames of this attach. A detach only tidies up: when the server no longer knows the
   * page - a classic tab holding the chat was left, and its controls were disposed with it - there
   * is nothing left to stop, and the page must not be loaded again for it.
   */
  private detach(): void {
    void this.command(CMD_DETACH_STREAM, { [ARG_STREAM]: this.token }, { ignoreStaleUi: true })
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
