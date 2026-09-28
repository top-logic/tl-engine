import { React, useTLCommand, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import type { ApprovalRequest, Attachment, ContextFact, InteractionRequest } from './types'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { Composer, plusMenuVisible } from './composer'
import { createDraftReporter } from './draft'
import type { ComposerApi, ReferenceCandidate } from './extensions'
import { bundle } from './i18n'
import type { ReferenceToken } from './reference'
import { RailToggle } from './rail'
import { unseenBadge } from './progress-model'
import {
  registeredAnnotationRenderer,
  registeredReferenceSource,
  resolveMenuItems,
  resolveMessageActions,
} from './registry'
import { ThemeIcon } from './theme-icon'
import { Thread } from './thread'

const { useCallback, useEffect, useMemo, useRef } = React

export interface ThreadConfig {
  citationMarkers: boolean
  copy: boolean
  rate: boolean
  regenerate: boolean
  timestamp: boolean
}

/**
 * Reads the transcript flags a {@code <chat-conversation/>} control put into its state.
 *
 * The trace level is *not* here: it is the store's own axis (seeded from the element's {@code trace}
 * and changed live by the composer's trace picker), so the transcript reads {@code chat.traceMode}.
 */
export function readThreadConfig(state: Record<string, unknown>): ThreadConfig {
  return {
    citationMarkers: state.citations !== false,
    copy: state.copy !== false,
    rate: state.rate !== false,
    regenerate: state.regenerate !== false,
    timestamp: state.timestamp !== false,
  }
}

export interface ComposerControls {
  autoApproveSwitch: boolean
  enterHint: boolean
  agentPicker: boolean
  modelPicker: boolean
  thinking: boolean
  mcp: boolean
  attachmentsEnabled: boolean
  tracePicker: boolean
}

/** Reads the composer flags a {@code <chat-conversation/>} control put into its state. */
export function readComposerControls(state: Record<string, unknown>): ComposerControls {
  return {
    autoApproveSwitch: state.autoApprove !== false,
    enterHint: state.enterHint !== false,
    agentPicker: state.agentPicker !== false,
    modelPicker: state.modelPicker !== false,
    thinking: state.thinking !== false,
    mcp: state.mcp !== false,
    attachmentsEnabled: state.attachments !== false,
    tracePicker: state.tracePicker !== false,
  }
}

/**
 * A synchronous {@code @}-source over the candidates a model-bound {@code <mention>} pushed into
 * state. The composer calls it on each keystroke; it filters the pre-pushed set (label or quoted text
 * contains the query). Returns {@code undefined} when no model binding is present, so the caller can
 * fall back to a registered TypeScript source.
 */
export function modelBoundSource(raw: unknown): ((query: string) => ReferenceCandidate[]) | undefined {
  if (!Array.isArray(raw)) return undefined
  const candidates: ReferenceCandidate[] = []
  for (const item of raw) {
    if (!item || typeof item !== 'object') continue
    const row = item as Record<string, unknown>
    const token = row.token as Record<string, unknown> | undefined
    if (!token || typeof token.id !== 'string') continue
    candidates.push({
      id: String(row.id ?? token.id),
      label: String(row.label ?? token.label ?? ''),
      token: {
        kind: typeof token.kind === 'string' ? token.kind : '',
        id: String(token.id),
        label: String(token.label ?? ''),
        text: typeof token.text === 'string' ? token.text : '',
      },
    })
  }
  return (query: string) => {
    const q = query.trim().toLowerCase()
    if (!q) return candidates
    return candidates.filter(
      (candidate) =>
        candidate.label.toLowerCase().includes(q) || candidate.token.text.toLowerCase().includes(q),
    )
  }
}

/**
 * The facts a `<chat-conversation/>` control computed from its `<context>` configuration.
 *
 * They are already evaluated - on the server, in the acting user's context - so the browser only
 * reads them out of the control state and carries them back with the next message.
 */
export function readContextFacts(raw: unknown): ContextFact[] {
  if (!Array.isArray(raw)) return []
  const facts: ContextFact[] = []
  for (const item of raw) {
    if (!item || typeof item !== 'object') continue
    const row = item as Record<string, unknown>
    const name = typeof row.name === 'string' ? row.name : ''
    if (!name) continue
    facts.push({
      name,
      value: typeof row.value === 'string' ? row.value : '',
      stable: row.stable !== false,
    })
  }
  return facts
}

export interface PromptCard {
  label: string
  icon: string
  text: string
}

/**
 * The prompt cards a `<chat-conversation/>` control computed from its `<prompts>` configuration.
 *
 * Like the context facts, the texts are already evaluated - on the server, in the acting user's
 * context - so clicking a card costs no round trip. A card with nothing to say is dropped here
 * rather than rendered as a button that would empty the composer: a label with no text is an
 * expression that threw, and the user should see one card fewer, not a broken one.
 */
export function readPromptCards(raw: unknown): PromptCard[] {
  if (!Array.isArray(raw)) return []
  const cards: PromptCard[] = []
  for (const item of raw) {
    if (!item || typeof item !== 'object') continue
    const row = item as Record<string, unknown>
    const label = typeof row.label === 'string' ? row.label : ''
    const text = typeof row.text === 'string' ? row.text : ''
    if (!label || !text) continue
    cards.push({ label, icon: typeof row.icon === 'string' ? row.icon : '', text })
  }
  return cards
}

/**
 * Whether the cards are shown right now: on a new chat, and nowhere else.
 *
 * Two conditions, and both are load-bearing. `sessionId` is what "new chat" means - nothing is
 * selected in the session list and the first send is what will bring a conversation into being - so
 * a chat the user opened is never offered openers, whether or not it happens to hold a turn. The
 * empty transcript is the condition the greeting itself is drawn under (`stage.is-empty`, and
 * `Thread`'s `messages.length === 0`), and taking it as well is what keeps the cards from ever
 * standing on a screen that has no greeting over them: they appear under it or not at all.
 *
 * The second one is not redundant. A send on a new chat sets the session id one render before the
 * user's own message is appended, so without it the row would linger for a frame over a screen that
 * is already a conversation.
 */
export function promptCardsVisible(sessionId: unknown, messageCount: number): boolean {
  return !sessionId && messageCount === 0
}

/**
 * What a turn-setting channel said, out of the control state a `<chat-conversation/>` pushed.
 *
 * `undefined` is "the channel says nothing" - it is unwired, empty, or the write was refused - and
 * the composer is then left alone. A string, including the empty one, is an instruction.
 */
export function channelName(raw: unknown): string | undefined {
  return typeof raw === 'string' ? raw : undefined
}

/**
 * What the chat has to do to show what the `input` channel holds.
 *
 * - `select`: the channel names a session. The store decides whether that is news: it ignores the
 *   chat it already shows or is opening, and lets anything else supersede an opening in flight.
 * - `new`: the channel was emptied - by a write, or by the host refusing the session this store
 *   reported (`corrected`) - while a session is open.
 * - `keep`: anything else. In particular an empty channel that has always been empty leaves a
 *   restored session standing: the store reports it, and the host either takes it or corrects it.
 */
function followSelection(
  hasInput: boolean,
  selected: string,
  wasSelected: string,
  corrected: boolean,
  sessionId: string | null,
): 'select' | 'new' | 'keep' {
  if (!hasInput) return 'keep'
  if (selected) return 'select'
  return (wasSelected || corrected) && sessionId ? 'new' : 'keep'
}

/**
 * What a `publishSession` report says: which chat is open here, and whether this window is what
 * brought it into being.
 *
 * `created` is the half the server cannot work out for itself. It sees a session id arrive either
 * way, and a reload re-opens the chat this window had, so "the session has no subject yet" is not
 * the same question: a conversation started elsewhere and opened here must keep the subject it was
 * started with, and one that never had a subject must not be given the page's current one
 * (#508).
 */
export function sessionReport(
  sessionId: string | null,
  createdSessionId: string | null,
): { id: string; created: boolean } {
  const id = sessionId ?? ''
  return { id, created: id !== '' && id === createdSessionId }
}

/**
 * One write of the `reference` or the `quote` channel, out of the control state.
 *
 * Both carry the same thing - a token for the composer to insert at the caret - and differ only in
 * who said what it quotes: the element's own `<mention>` expressions, or the host that showed the
 * passage the reader marked. So one parser serves both state slots; each slot counts its own
 * writes.
 *
 * The `seq` is what makes a write a write: a channel notifies for A, B, A, and the third write
 * carries the same token as the first, which unchanged state could not tell apart from no write at
 * all. `undefined` means the channel is unwired or holds nothing insertable.
 */
export function tokenWrite(raw: unknown): { seq: number; token: ReferenceToken } | undefined {
  if (!raw || typeof raw !== 'object') return undefined
  const row = raw as Record<string, unknown>
  if (typeof row.id !== 'string' || !row.id) return undefined
  const seq = typeof row.seq === 'number' ? row.seq : Number(row.seq)
  if (!Number.isFinite(seq)) return undefined
  return {
    seq,
    token: {
      kind: typeof row.kind === 'string' ? row.kind : '',
      id: row.id,
      label: typeof row.label === 'string' ? row.label : '',
      location: typeof row.location === 'string' ? row.location : '',
      text: typeof row.text === 'string' ? row.text : '',
    },
  }
}

/** The same for the channel that holds a set of names; an empty array attaches no server at all. */
export function channelNames(raw: unknown): string[] | undefined {
  return Array.isArray(raw) ? raw.map(String) : undefined
}

/** Whether two attachment sets name the same servers, order and duplicates aside. */
export function sameServers(a: string[], b: string[]): boolean {
  if (a.length !== b.length) return false
  const left = [...a].sort()
  const right = [...b].sort()
  return left.every((name, index) => name === right[index])
}

/**
 * The row of first questions a page offers, between the greeting and the composer.
 *
 * That is where the classic chat put its own example prompts - under "How can I help you today?",
 * over the field - and it is where they belong: the eye reads the greeting, the offer, and then the
 * place to type. A click fills the composer and nothing else - the user reads the sentence and
 * presses send, which is the same rule the `draft`, `reference` and `quote` channels follow.
 * `setDraft` focuses the field and puts the caret behind the text, so a user who wants to add a word
 * is already there.
 */
function PromptCards({
  cards,
  label,
  onPick,
}: {
  cards: PromptCard[]
  label: string
  onPick: (text: string) => void
}) {
  return (
    <div className="prompt-suggestions" role="group" aria-label={label}>
      {cards.map((card, index) => (
        <button
          key={`${card.label}-${index}`}
          type="button"
          className="prompt-suggestion"
          title={card.text}
          onClick={() => onPick(card.text)}
        >
          {card.icon ? <ThemeIcon encoded={card.icon} className="prompt-suggestion__icon" /> : null}
          <span className="prompt-suggestion__label">{card.label}</span>
        </button>
      ))}
    </div>
  )
}

/**
 * The transcript and the prompt field as one view element.
 *
 * It joins the chat through {@code chat-id}. A configured {@code input} channel selects the
 * session; without one the store is the source. A configured {@code busy} channel is written while
 * the agent works. Every picker can be switched off; sending still works. The pane a source or a
 * host section opens lives in a sibling {@code <chat-rail/>} on the same {@code chat-id}.
 *
 * The four turn settings - agent, model, effort and the attached MCP servers - travel over their own
 * two-way channels: a write from outside is applied as if the user had picked it, and what the user
 * picks is reported back with one {@code publishTurnSettings} command.
 *
 * The {@code draft} channel carries the unsent text the same way, and {@code reference} puts a
 * written model object into it as one atomic token. A host that writes both has put a question into
 * the chat; pressing send is still the user's.
 */
export function TLChatConversation({ controlId }: TLCellProps) {
  const state = useTLState()
  const command = useTLCommand()
  const boot = useMemo(() => readBootstrap(state), [state])
  const chatId = chatIdOf(state, controlId)
  const { chat, actions, runs } = useChat(chatId, boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  // Where this conversation's requests go. Memoised, because the composer is `React.memo`d and a
  // fresh object on every render would defeat that.
  const at = useMemo(() => ({ windowId: chat.windowId, chatId }), [chat.windowId, chatId])
  const config = useMemo(() => readThreadConfig(state), [state])
  const controls = useMemo(() => readComposerControls(state), [state])
  const selected = typeof state.selected === 'string' ? state.selected : ''
  const corrections = typeof state.selectedCorrections === 'number' ? state.selectedCorrections : 0
  const hasInput = state.hasInput === true
  // Whether the element names a subject channel. The server needs the session id of a chat created
  // here even when nothing else on the page follows the selection, so this publishes it too.
  const hasSubject = state.hasSubject === true
  const placeholder = typeof state.placeholder === 'string' ? state.placeholder : ''
  const promptImprovementEnabled = state.promptImprovementEnabled === true
  const extraMenuItems = useMemo(
    () => resolveMenuItems(Array.isArray(state.menuItems) ? state.menuItems.map(String) : []),
    [state.menuItems],
  )
  const referenceCandidates = useMemo(
    () =>
      modelBoundSource(state.referenceCandidates) ??
      registeredReferenceSource(typeof state.referenceSource === 'string' ? state.referenceSource : ''),
    [state.referenceCandidates, state.referenceSource],
  )

  // Tell the chat a conversation is on the page. A `<chat-dock/>` asks for this: it must not open a
  // second copy of the conversation the page is already showing, which would give one draft channel
  // two composers and one remembered layout two rails. Its own nested conversation does not count -
  // `insideDock` says which is which, and comes from the view context rather than from the React tree.
  const insideDock = state.insideDock === true
  useEffect(() => actions.registerConversation(insideDock), [actions, insideDock])

  // The first questions this page offers, shown on a new chat alone. Their texts were evaluated on
  // the server, so a click is purely local: it writes the composer, which then reports the draft
  // onto the `draft` channel the way a typed sentence does.
  const promptCards = useMemo(() => readPromptCards(state.prompts), [state.prompts])
  const onPickPrompt = useCallback((text: string) => actions.setDraft(text), [actions])

  // What this page states about the situation, handed to the store so that it travels with every
  // message. The store compares by value, so re-pushing an unchanged set costs nothing.
  const contextFacts = useMemo(() => readContextFacts(state.contextFacts), [state.contextFacts])
  useEffect(() => {
    actions.setContextFacts(contextFacts)
  }, [contextFacts, actions])

  // Follow the selection channel. A non-empty value selects that session; clearing it (a transition
  // to empty) means "new chat" - a deselect shows a fresh, empty conversation. The transition guard
  // avoids wiping a session that was restored before the write-back below set the channel.
  //
  // A correction is the other way to an empty channel: the host refused the session this store
  // reported - the one it restored from another page's conversation, typically - and left the
  // channel empty, which it already was. `selected` does not change then, so the count is what
  // says the restored session has to go (#576).
  //
  // It runs when the channel moves, never when the store does. The store moves first whenever it
  // opens a chat itself - a pick from the rail, a restore, a first send - and reports it; until the
  // channel answers, `selected` still names the chat that was left. Following it then opened that
  // chat again, the report of it went out, and the channel's answer to the first report asked for
  // the other one: the transcript switched between two conversations every few milliseconds, for as
  // long as the page was open (#576).
  const prevSelected = useRef(selected)
  const prevCorrections = useRef(corrections)
  const sessionId = useRef(chat.sessionId)
  sessionId.current = chat.sessionId
  useEffect(() => {
    const wasSelected = prevSelected.current
    const corrected = prevCorrections.current !== corrections
    prevSelected.current = selected
    prevCorrections.current = corrections
    const follow = followSelection(hasInput, selected, wasSelected, corrected, sessionId.current)
    if (follow === 'select') void actions.selectSession(selected)
    else if (follow === 'new') actions.newChat()
  }, [hasInput, selected, corrections, actions])

  // Write-back: publish the store's own session onto the channel (two-way), so a session list
  // highlights it after the store creates one on first send or restores one on reload.
  //
  // `created` is the other half: the server stamps a conversation with what the page is about only
  // when this window is what brought it into being. It cannot tell that from the id alone - a reload
  // re-opens the chat this window had, and that one must keep the subject it was started with.
  //
  // It starts from "no chat" rather than from nothing. An element mounts with an empty store while
  // the chat its channel names is still loading, and reporting that emptiness cleared the channel
  // the host had just set - the host then answered the clear, and the page showed whatever it
  // answered with until the load came back (#576). An empty report means "the chat was left", so it
  // is only sent after a chat was reported.
  const published = useRef<string | null>('')
  useEffect(() => {
    if (!hasInput && !hasSubject) return
    const report = sessionReport(chat.sessionId, chat.createdSessionId)
    if (published.current === report.id) return
    published.current = report.id
    void command('publishSession', report)
  }, [hasInput, hasSubject, chat.sessionId, chat.createdSessionId, command])

  useEffect(() => {
    void command('setBusy', { busy: chat.busy })
  }, [chat.busy, command])

  // Follow the turn-setting channels. A *write* is what the composer follows, not the value standing
  // on the channel: the composer changes these itself - restoring a session brings back its effort,
  // an agent brings its own model - and the channel only learns of that one round trip later. Acting
  // on the value would undo every such change with the one the channel still holds. The transition
  // guard is the same one the selection channel above uses, and for the same reason.
  const channelAgent = channelName(state.agentSelected)
  const channelModel = channelName(state.modelSelected)
  const channelEffort = channelName(state.effortSelected)
  const channelServers = useMemo(() => channelNames(state.mcpSelected), [state.mcpSelected])
  // Undefined, so a channel that already holds a value when the element mounts counts as written.
  const wroteAgent = useRef<string | undefined>(undefined)
  const wroteModel = useRef<string | undefined>(undefined)
  const wroteEffort = useRef<string | undefined>(undefined)
  const wroteServers = useRef<string[] | undefined>(undefined)
  // The agent goes first, because picking one resets the model and the servers to that agent's own:
  // a host that writes the agent and the model means the model it named, not the one it displaced.
  useEffect(() => {
    const written = wroteAgent.current !== channelAgent
    wroteAgent.current = channelAgent
    if (written && channelAgent !== undefined && channelAgent !== chat.agent) {
      actions.selectAgent(channelAgent)
    }
  }, [channelAgent, chat.agent, actions])
  useEffect(() => {
    const written = wroteModel.current !== channelModel
    wroteModel.current = channelModel
    if (written && channelModel !== undefined && channelModel !== chat.model) {
      actions.selectModel(channelModel)
    }
  }, [channelModel, chat.model, actions])
  useEffect(() => {
    const written = wroteEffort.current !== channelEffort
    wroteEffort.current = channelEffort
    if (written && channelEffort !== undefined && channelEffort !== chat.effort) {
      actions.selectEffort(channelEffort)
    }
  }, [channelEffort, chat.effort, actions])
  useEffect(() => {
    const previous = wroteServers.current
    const written = previous === undefined || channelServers === undefined
      ? previous !== channelServers
      : !sameServers(previous, channelServers)
    wroteServers.current = channelServers
    if (written && channelServers !== undefined && !sameServers(channelServers, chat.mcp)) {
      actions.selectMcpServers(channelServers)
    }
  }, [channelServers, chat.mcp, actions])

  // Write-back, in one command: the composer settles the four together (an agent brings its model and
  // its servers), so reporting them one at a time would put a host through states no user was ever in.
  const publishedTurn = useRef<string | null>(null)
  useEffect(() => {
    const settings = {
      agent: chat.agent,
      model: chat.model,
      effort: chat.effort,
      mcpServers: chat.mcp,
    }
    const fingerprint = JSON.stringify(settings)
    if (publishedTurn.current === fingerprint) return
    publishedTurn.current = fingerprint
    void command('publishTurnSettings', settings)
  }, [chat.agent, chat.model, chat.effort, chat.mcp, command])

  // The draft channel, in both directions.
  //
  // Out: the composer's text, but not on the keystroke - a channel write is a round trip, so an
  // ordinary report waits for the pause, while blur and send report at once (`createDraftReporter`).
  // In: a *write* replaces the draft, focuses the composer and puts the caret at the end. What
  // comes back from the control is mostly this element's own report travelling the loop, so what
  // was last published is remembered and recognised - otherwise an echo that arrives two keystrokes
  // later would put the composer back where it was two keystrokes ago.
  const publishedDraft = useRef<string | null>(null)
  const reporter = useMemo(
    () =>
      createDraftReporter((text) => {
        publishedDraft.current = text
        void command('publishDraft', { text })
      }),
    [command],
  )
  useEffect(() => reporter.cancel, [reporter])
  const onDraft = useCallback(
    (text: string, settled: boolean) => {
      if (settled) reporter.flush(text)
      else reporter.report(text)
    },
    [reporter],
  )
  const channelDraft = channelName(state.draftText)
  const wroteDraft = useRef<string | undefined>(undefined)
  useEffect(() => {
    const written = wroteDraft.current !== channelDraft
    wroteDraft.current = channelDraft
    if (!written || channelDraft === undefined) return
    if (channelDraft === publishedDraft.current) return
    publishedDraft.current = channelDraft
    actions.setDraft(channelDraft)
  }, [channelDraft, actions])

  // The reference channel: an object written to it becomes one atomic token at the caret, exactly
  // as picking it from the "@" menu does. In only - the token is part of the draft the channel
  // above already carries.
  const reference = useMemo(() => tokenWrite(state.referenceWrite), [state.referenceWrite])
  const insertedReference = useRef<number | null>(null)
  useEffect(() => {
    if (!reference || insertedReference.current === reference.seq) return
    insertedReference.current = reference.seq
    actions.insertReference(reference.token)
  }, [reference, actions])

  // The quote channel: the same insert for a passage *of* an object, whose quoted text and
  // location the host wrote. A slot and a counter of its own, so a write of one channel is never
  // lost to a write of the other.
  const quote = useMemo(() => tokenWrite(state.quoteWrite), [state.quoteWrite])
  const insertedQuote = useRef<number | null>(null)
  useEffect(() => {
    if (!quote || insertedQuote.current === quote.seq) return
    insertedQuote.current = quote.seq
    actions.insertReference(quote.token)
  }, [quote, actions])

  const pendingApprovals = useMemo(
    () =>
      chat.messages.filter(
        (message): message is ApprovalRequest =>
          message.type === 'approval_request' && message.outcome === 'pending',
      ),
    [chat.messages],
  )
  const pendingQuestions = useMemo(
    () =>
      chat.messages.filter(
        (message): message is InteractionRequest =>
          message.type === 'interaction_request' && !message.answered,
      ),
    [chat.messages],
  )

  const onEditSend = useCallback(
    (messageId: string, turnIndex: number, text: string): Promise<boolean> =>
      actions.editSend(messageId, turnIndex, text),
    [actions],
  )
  const onSend = useCallback(
    (text: string, attachments: Attachment[]): Promise<boolean> => actions.send(text, attachments),
    [actions],
  )

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-conversation is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-conversation">
      <div className={`stage${chat.messages.length === 0 ? ' is-empty' : ''}`}>
        <Thread
          messages={chat.messages}
          traceMode={chat.traceMode}
          welcomeName={chat.welcomeName || chat.userName}
          busy={chat.busy}
          regenerating={chat.regenerating}
          feedback={chat.feedback}
          labels={labels}
          language={chat.language}
          effort={chat.effort}
          onEditSend={onEditSend}
          onRegenerate={actions.regenerate}
          onRate={actions.rate}
          onOpenRail={() => undefined}
          railCollapsed={false}
          onToggleRail={() => undefined}
          title={chat.sessionTitle}
          sessionListToggle={false}
          canCopy={config.copy}
          canRate={config.rate}
          canRegenerate={config.regenerate}
          citationMarkers={config.citationMarkers}
          showTimestamp={config.timestamp}
          extraActions={resolveMessageActions(
            Array.isArray(state.hostActions) ? state.hostActions.map(String) : [],
          )}
          renderAnnotation={registeredAnnotationRenderer(
            typeof state.citationRenderer === 'string' ? state.citationRenderer : '',
          )}
          openInPane={actions.openPane}
          headerEnd={
            // Derived, not configured: the badge button appears exactly when a `<chat-rail/>` on
            // this `chat-id` is mounted, because with no rail it would toggle nothing visible.
            chat.railMounted ? (
              <RailToggle
                open={chat.sideOpen}
                badge={unseenBadge(chat.badgeTotal, chat.badgeSeen)}
                labels={labels}
                onToggle={() => actions.toggleSide(chat.badgeTotal)}
              />
            ) : null
          }
          onOpenSource={() => undefined}
        />
        {promptCards.length > 0 && promptCardsVisible(chat.sessionId, chat.messages.length) ? (
          <PromptCards cards={promptCards} label={labels.promptCards} onPick={onPickPrompt} />
        ) : null}
        <Composer
          at={at}
          agents={chat.caps.agents}
          models={chat.offeredModels}
          agent={chat.agent}
          model={chat.model}
          onAgent={actions.selectAgent}
          onModel={actions.selectModel}
          mcpServers={chat.caps.mcpServers}
          selectedMcp={chat.mcp}
          onToggleMcp={actions.toggleMcp}
          traceMode={chat.traceMode}
          onTraceMode={actions.selectTraceMode}
          effort={chat.effort}
          effortLevels={chat.effortLevels}
          onEffort={actions.selectEffort}
          labels={labels}
          autoApprove={chat.autoApprove}
          onToggleAutoApprove={() => void actions.toggleAutoApprove()}
          pendingApprovals={pendingApprovals}
          pendingQuestions={pendingQuestions}
          dispatch={actions.dispatch}
          promptImprovementEnabled={promptImprovementEnabled && chat.caps.promptImproverAvailable !== false}
          subagents={chat.subagents}
          onCancelSubagent={actions.cancelSubagent}
          onStopSubagents={actions.stopSubagents}
          onLoadSubagentTranscript={actions.loadSubagentTranscript}
          busy={chat.busy}
          connection={chat.connection}
          notice={chat.notice}
          noticeDetail={chat.noticeDetail}
          noticeTone={chat.noticeTone}
          onDismissNotice={() => actions.showNotice('')}
          onSend={onSend}
          onStop={actions.stop}
          onDraft={onDraft}
          placeholder={placeholder || undefined}
          extraMenuItems={extraMenuItems}
          referenceCandidates={referenceCandidates}
          runs={runs}
          ref={(api: ComposerApi | null) => actions.bindComposer(api)}
          {...controls}
        />
      </div>
    </div>
  )
}

export { plusMenuVisible }
