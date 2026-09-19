import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import { agentLabel, modelLabel, toolLabel } from './models'
import { SessionRail } from './chrome'
import { Composer } from './composer'
import type {
  ChatExtensions,
  ComposerApi,
  RailRenderContext,
  RailSection,
} from './extensions'
import type { ReferenceToken } from './reference'
import { hostRailSections } from './extensions'
import { ContextIcon, ContextSection, ProgressIcon, ProgressSection } from './progress'
import { SourcesIcon, SourcesSection, type CitedSource } from './sources'
import { progressBadge, unseenBadge } from './progress-model'
import { RailToggle, SideRail, SideStrip } from './rail'
import { SHEET_MAX, SidePane } from './side-pane'
import { Thread } from './thread'
import {
  type ApprovalRequest,
  type Attachment,
  type ChatMessage,
  type InteractionRequest,
  type Citation,
} from './types'

const { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } = React

/**
 * The shipped agentic chat: session rail, thread, composer and right rail over one {@link useChat}
 * store.
 *
 * Everything the conversation *is* - the transcript, the run, the session list, the capabilities,
 * the layout preferences and the one `EventSource` - lives in that store, keyed by `chat-id`, so
 * further view elements can show the same chat beside this one. What stays here is what only this
 * element has: its own geometry (the session drawer, the collapsed rail, the sheet breakpoint, the
 * published composer height) and the host's {@link ChatExtensions}.
 */
export function App({ controlId, extensions }: TLCellProps & { extensions?: ChatExtensions }) {
  const state = useTLState()
  const promptImprovementEnabled = state.promptImprovementEnabled === true
  /**
   * The composer's idle invitation, from `<chat-conversation placeholder="…">` when the installation
   * configured one, else from the host's own extensions (#420). The configured text wins: it is
   * the installation's decision, while the seam is the host code's default. Empty falls through to
   * the framework's wording, which is what an application that configures neither gets.
   */
  const placeholder =
    (typeof state.placeholder === 'string' ? state.placeholder : '') ||
    extensions?.composerAddons?.placeholder ||
    ''
  const boot = useMemo(() => readBootstrap(state), [state])
  const chatId = chatIdOf(state, controlId)
  const { chat, actions, runs } = useChat(chatId, boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  // Where this chat's requests go. Memoised, because the composer is `React.memo`d and a fresh
  // object on every render would defeat that.
  const at = useMemo(() => ({ windowId: chat.windowId, chatId }), [chat.windowId, chatId])

  /** Whether the session drawer is open. Below its breakpoint the rail is a drawer, not a column. */
  const [railOpen, setRailOpen] = useState(false)
  const [railCollapsed, setRailCollapsed] = useState(false)
  /**
   * How tall the composer is, published to the stylesheet.
   *
   * A sheet covers the thread, not the controls: below 1280 it ends at the top of the composer, so
   * the model picker, the send button and the Enter hint stay visible and usable while an artefact
   * is open. Only JS knows that height - it grows with the draft.
   */
  const composerBox = useRef<HTMLDivElement | null>(null)

  /** The chat's own root, which is what decides the layout mode - not the window. */
  const cockpit = useRef<HTMLDivElement | null>(null)
  /** True below 1280, where the rail and the pane are sheets over the thread rather than columns. */
  const [sheet, setSheet] = useState(false)

  useEffect(() => {
    const workspace = composerBox.current
    const composer = workspace?.querySelector('.composer')
    if (!workspace || !composer || typeof ResizeObserver === 'undefined') return undefined
    const publish = (height: number) =>
      workspace.style.setProperty('--composer-height', `${Math.round(height)}px`)
    publish(composer.getBoundingClientRect().height)
    const observer = new ResizeObserver(([entry]) => publish(entry.contentRect.height))
    observer.observe(composer)
    return () => observer.disconnect()
  })

  /*
   * Measured, not asked of the window (Issue #427).
   *
   * A chat placed by an application owns the window almost never: a shell's navigation
   * and the page gutters take their share first, so a viewport query answered for a box the chat
   * does not have and the sheet arrived hundreds of pixels late, or never. The stylesheet decides
   * the same thing from the same element, as a `chat-shell` container query on the same number.
   */
  useLayoutEffect(() => {
    const root = cockpit.current
    if (!root) return undefined
    const decide = (width: number) => setSheet(width <= SHEET_MAX)
    decide(root.getBoundingClientRect().width)
    if (typeof ResizeObserver === 'undefined') return undefined
    const observer = new ResizeObserver(([entry]) => decide(entry.contentRect.width))
    observer.observe(root)
    return () => observer.disconnect()
  }, [])

  useEffect(() => {
    if (!railOpen) return
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return
      setRailOpen(false)
      window.setTimeout(() => {
        document.querySelector<HTMLButtonElement>(`#${CSS.escape(controlId)} .rail-toggle`)?.focus()
      })
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [controlId, railOpen])

  const contextTag = extensions?.contextHook?.contextTag

  const onSelectSession = useCallback(
    async (id: string) => {
      setRailOpen(false)
      await actions.selectSession(id)
    },
    [actions],
  )

  const onNew = useCallback(() => {
    setRailOpen(false)
    actions.newChat()
  }, [actions])

  const onSend = useCallback(
    (text: string, attachments: Attachment[], edited?: boolean): Promise<boolean> =>
      actions.send(text, attachments, { edited, contextTag }),
    [actions, contextTag],
  )

  const onEditSend = useCallback(
    (messageId: string, turnIndex: number, text: string): Promise<boolean> =>
      actions.editSend(messageId, turnIndex, text, { contextTag }),
    [actions, contextTag],
  )

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

  // The approval cards the composer shows are also what Fortschritt reports: the run is not merely
  // "running", it is waiting for this user.
  const awaitingApproval = useMemo(
    () =>
      pendingApprovals.map((request) => ({
        id: request.id,
        // The same wording the approval card uses, not the raw tool name.
        toolName: toolLabel(request.toolName),
      })),
    [pendingApprovals],
  )

  const progressSection: RailSection = useMemo(
    () => ({
      id: 'progress',
      title: labels.progressTitle,
      icon: ProgressIcon,
      badge: () => progressBadge(chat.tasks, awaitingApproval),
      count: () => progressBadge(chat.tasks, awaitingApproval),
      render: () => <ProgressSection tasks={chat.tasks} approvals={awaitingApproval} labels={labels} />,
    }),
    [labels, chat.tasks, awaitingApproval],
  )

  /** What the agent receives every turn, plus whatever the host adds. */
  const contextFacts = useMemo(() => {
    const own = [
      { id: 'language', label: labels.contextLanguage, value: labels.languageName },
      { id: 'agent', label: labels.contextAgent, value: agentLabel(chat.agent, chat.caps.agents) },
      { id: 'model', label: labels.contextModel, value: modelLabel(chat.model) },
      { id: 'session', label: labels.contextSession, value: chat.sessionTitle },
    ]
    return [...own, ...(extensions?.contextFacts?.({ messages: chat.messages }) ?? [])]
  }, [labels, chat.caps.agents, chat.agent, chat.model, chat.sessionTitle, extensions, chat.messages])

  const contextSection: RailSection = useMemo(
    () => ({
      id: 'context',
      title: labels.contextTitle,
      icon: ContextIcon,
      render: () => <ContextSection facts={contextFacts} labels={labels} />,
    }),
    [labels, contextFacts],
  )

  const openInPane = actions.openPane

  /**
   * What "open this source" does.
   *
   * The host decides for its own kinds - the demo puts the invoice in the pane - and a web source
   * falls back to its page. One route, so the marker, its popover and the rail all do the same
   * thing.
   */
  const openSource = useCallback(
    (citation: Citation) => {
      const node = extensions?.messageAnnotations?.render?.(
        citation.index,
        citation,
        { type: 'assistant_text', id: '', text: '', isStreaming: false, timestamp: Date.now() } as ChatMessage,
        openInPane,
      )
      if (node) {
        actions.openPane(node, { title: citation.label || citation.id, mode: 'card' })
        return
      }
      const href = citation.url || (/^https?:\/\//i.test(citation.id) ? citation.id : '')
      if (href) window.open(href, '_blank', 'noopener,noreferrer')
    },
    [actions, extensions, openInPane],
  )

  /** Scrolls the thread to the answer a marker belongs to. */
  const goToAnswer = useCallback((messageId: string) => {
    const el = document.querySelector(`[data-message-id="${CSS.escape(messageId)}"]`)
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el?.classList.add('is-pointed')
    window.setTimeout(() => el?.classList.remove('is-pointed'), 1600)
  }, [])

  const sourcesSection: RailSection = useMemo(
    () => ({
      id: 'sources',
      title: labels.sourcesTitle,
      icon: SourcesIcon,
      // The newest answer's sources, not the chat's running total: a badge over the whole list can
      // only grow, and would sit on the Quellen icon for the rest of the conversation (#495).
      count: () => chat.latestSources.length,
      render: (ctx) => (
        <SourcesSection
          sources={chat.sources}
          labels={labels}
          onOpen={(source: CitedSource) => openSource(source.citation)}
          onGoTo={goToAnswer}
          tabs={(extensions?.sourceTabs ?? []).map((tab) => ({
            id: tab.id,
            label: tab.label,
            render: () => tab.render(ctx),
          }))}
        />
      ),
    }),
    [labels, chat.sources, chat.latestSources, openSource, goToAnswer, extensions],
  )

  const railSections = useMemo(
    () => [progressSection, ...hostRailSections(extensions), contextSection, sourcesSection],
    [progressSection, contextSection, sourcesSection, extensions],
  )

  const badgeTotal = useMemo(
    () => railSections.reduce((sum, section) => sum + (section.badge?.() ?? 0), 0),
    [railSections],
  )
  const badge = unseenBadge(badgeTotal, chat.badgeSeen)

  /** What the composer exposes to a host: an imperative handle, because the caret lives in the DOM. */
  const composer = useRef<ComposerApi>(null)
  const insertReference = useCallback((token: ReferenceToken) => {
    composer.current?.insertReference(token)
  }, [])

  const railCtx: RailRenderContext = useMemo(
    () => ({
      sessionId: chat.sessionId,
      messages: chat.messages,
      busy: chat.busy,
      pendingApprovals: awaitingApproval,
      openInPane,
      closePane: actions.closePane,
      insertReference,
    }),
    [chat.sessionId, chat.messages, chat.busy, awaitingApproval, openInPane, actions, insertReference],
  )

  const collapsedIds = useMemo(() => new Set(chat.collapsedSections), [chat.collapsedSections])

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-cockpit is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  const paneOpen = chat.sideOpen && chat.paneContent != null
  const railVisible = chat.sideOpen && !paneOpen
  const documentMode = paneOpen && chat.paneOptions?.mode === 'document'

  return (
    // `has-pane` is gone: it was here to tell the composer that its column had shrunk, which the
    // stage now says itself as a container query (Issue #427) - and says for a composed
    // conversation too, which never had the class. `has-document` stays, for the one rule that is
    // about a document being open rather than about a width.
    <div
      id={controlId}
      ref={cockpit}
      className={
        `tl-cockpit${railCollapsed ? ' is-rail-collapsed' : ''}` +
        `${chat.sideOpen ? ' has-side' : ''}${documentMode ? ' has-document' : ''}`
      }
    >
      <SessionRail
        labels={labels}
        sessions={chat.sessions}
        activeId={chat.sessionId}
        open={railOpen}
        collapsed={railCollapsed}
        loading={chat.sessionsLoading}
        error={chat.sessionsError}
        onNew={onNew}
        onSelect={onSelectSession}
        onDelete={actions.deleteSession}
        onClose={() => setRailOpen(false)}
        onToggleCollapsed={() => setRailCollapsed((value) => !value)}
      />
      <div className="workspace" ref={composerBox}>
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
            onOpenRail={() => setRailOpen(true)}
            railCollapsed={railCollapsed}
            onToggleRail={() => setRailCollapsed((value) => !value)}
            title={chat.sessionTitle}
            headerCaption={extensions?.contextHook?.header?.({ messages: chat.messages })}
            headerEnd={
              <RailToggle
                open={chat.sideOpen}
                badge={badge}
                labels={labels}
                onToggle={() => actions.toggleSide(badgeTotal)}
              />
            }
            renderAnnotation={extensions?.messageAnnotations?.render}
            openInPane={openInPane}
            extraActions={extensions?.messageActions}
            onOpenSource={openSource}
          />
          <Composer
            runs={runs}
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
            promptImprovementEnabled={
              promptImprovementEnabled && chat.caps.promptImproverAvailable !== false
            }
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
            ref={composer}
            placeholder={placeholder || undefined}
            extraMenuItems={extensions?.composerAddons?.extraMenuItems}
            referenceCandidates={extensions?.composerAddons?.referenceCandidates}
          />
        </div>
        {/*
          Always there, one icon per section: the strip is how the rail says what it holds, whether
          it is open, closed or replaced by a pane. Clicking an icon opens that section.
        */}
        <SideStrip
          sections={railSections}
          activeId={paneOpen ? chat.paneOptions?.sectionId : undefined}
          onSelect={(id) => actions.showSection(id, badgeTotal)}
          labels={labels}
        />
        {/*
          A sheet is an overlay, and must read as one: it dims what it covers and a click beside it
          closes it. Without the scrim the state read as a broken split - half a thread, half a
          composer, no way out but the pane's own close.
        */}
        {(railVisible || paneOpen) && sheet ? (
          <div className="side-scrim" onClick={actions.closeSide} aria-hidden="true" />
        ) : null}
        {railVisible ? (
          <aside className="side-column" aria-label={labels.sideRail}>
            <SideRail
              sections={railSections}
              ctx={railCtx}
              collapsedIds={collapsedIds}
              onToggleSection={actions.toggleSection}
              labels={labels}
            />
          </aside>
        ) : null}
        {paneOpen ? (
          <SidePane
            narrow={sheet}
            title={chat.paneOptions?.title ?? ''}
            backLabel={chat.paneOptions?.backLabel}
            tabs={chat.paneOptions?.tabs}
            width={chat.paneWidth}
            onWidth={actions.setPaneWidth}
            onBack={actions.closePane}
            labels={labels}
            mode={chat.paneOptions?.mode}
            version={chat.paneOptions?.version}
            actions={chat.paneOptions?.actions}
          >
            {chat.paneContent}
          </SidePane>
        ) : null}
      </div>
    </div>
  )
}
