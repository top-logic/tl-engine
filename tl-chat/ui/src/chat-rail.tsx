import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { chatIdOf, readBootstrap, useChat } from './chat-store'
import { bundle } from './i18n'
import { agentLabel, modelLabel, toolLabel } from './models'
import type { RailRenderContext, RailSection } from './extensions'
import type { ReferenceToken } from './reference'
import { ContextIcon, ContextSection, ProgressIcon, ProgressSection } from './progress'
import { SourcesIcon, SourcesSection, type CitedSource } from './sources'
import { progressBadge } from './progress-model'
import { SideRail, SideStrip } from './rail'
import { SidePane } from './side-pane'
import {
  collapsedFromConfig,
  paneIsOpen,
  readRailSections,
  resolveRailSections,
} from './rail-order'
import { registeredRailSection, registeredRailSectionNames } from './registry'
import type { ApprovalRequest, Citation } from './types'

const { useCallback, useEffect, useMemo } = React

/**
 * The right rail and its pane as their own view element.
 *
 * It joins the chat through {@code chat-id}. A configured {@code input} channel selects the
 * session; without one the store is the source. {@code openInPane} from a section still opens the
 * pane here, because the pane is this element's other face, not a fifth tag.
 */
export function TLChatRail({ controlId }: TLCellProps) {
  const state = useTLState()
  const boot = useMemo(() => readBootstrap(state), [state])
  const { chat, actions } = useChat(chatIdOf(state, controlId), boot)
  const labels = useMemo(() => bundle(chat.language), [chat.language])
  const configured = useMemo(() => readRailSections(state), [state])
  const strip = state.strip !== false
  const paneMode = state.paneMode === 'document' ? 'document' : 'card'
  const selected = typeof state.selected === 'string' ? state.selected : ''

  useEffect(() => {
    if (!selected || selected === chat.sessionId) return
    void actions.selectSession(selected)
  }, [actions, chat.sessionId, selected])

  const seed = useMemo(() => collapsedFromConfig(configured), [configured])
  useEffect(() => {
    if (!seed) return
    actions.seedCollapsedOnce(seed)
  }, [actions, seed])

  const awaitingApproval = useMemo(
    () =>
      chat.messages
        .filter(
          (message): message is ApprovalRequest =>
            message.type === 'approval_request' && message.outcome === 'pending',
        )
        .map((request) => ({
          id: request.id,
          toolName: toolLabel(request.toolName),
        })),
    [chat.messages],
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

  const contextFacts = useMemo(
    () => [
      { id: 'language', label: labels.contextLanguage, value: labels.languageName },
      { id: 'agent', label: labels.contextAgent, value: agentLabel(chat.agent, chat.caps.agents) },
      { id: 'model', label: labels.contextModel, value: modelLabel(chat.model) },
      { id: 'session', label: labels.contextSession, value: chat.sessionTitle },
    ],
    [labels, chat.caps.agents, chat.agent, chat.model, chat.sessionTitle],
  )

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

  const openSource = useCallback(
    (citation: Citation) => {
      const href = citation.url || (/^https?:\/\//i.test(citation.id) ? citation.id : '')
      if (href) window.open(href, '_blank', 'noopener,noreferrer')
    },
    [],
  )

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
      count: () => chat.sources.length,
      render: () => (
        <SourcesSection
          sources={chat.sources}
          labels={labels}
          onOpen={(source: CitedSource) => openSource(source.citation)}
          onGoTo={goToAnswer}
        />
      ),
    }),
    [labels, chat.sources, openSource, goToAnswer],
  )

  const insertReference = useCallback(
    (token: ReferenceToken) => actions.insertReference(token),
    [actions],
  )

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

  const registered = useMemo(() => {
    const sections: RailSection[] = []
    for (const name of registeredRailSectionNames()) {
      const section = registeredRailSection(name, railCtx)
      if (section) sections.push(section)
    }
    return sections
  }, [railCtx])

  const { sections } = useMemo(
    () =>
      resolveRailSections(configured, {
        progress: progressSection,
        context: contextSection,
        sources: sourcesSection,
      }, registered),
    [configured, progressSection, contextSection, sourcesSection, registered],
  )

  const badgeTotal = useMemo(
    () => sections.reduce((sum, section) => sum + (section.badge?.() ?? 0), 0),
    [sections],
  )
  const collapsedIds = useMemo(() => new Set(chat.collapsedSections), [chat.collapsedSections])
  const paneOpen = paneIsOpen(chat.sideOpen, chat.paneContent)
  const railVisible = chat.sideOpen && !paneOpen

  if (!chat.authenticated) {
    return (
      <div id={controlId} className="tl-chat-rail is-signed-out">
        <div className="signed-out">
          <h2>{labels.signInHeading}</h2>
          <p>{labels.signInBody}</p>
        </div>
      </div>
    )
  }

  return (
    <div id={controlId} className="tl-chat-rail">
      {strip ? (
        <SideStrip
          sections={sections}
          activeId={paneOpen ? chat.paneOptions?.sectionId : undefined}
          onSelect={(id) => actions.showSection(id, badgeTotal)}
          labels={labels}
        />
      ) : null}
      {railVisible ? (
        <aside className="side-column" aria-label={labels.sideRail}>
          <SideRail
            sections={sections}
            ctx={railCtx}
            collapsedIds={collapsedIds}
            onToggleSection={actions.toggleSection}
            labels={labels}
          />
        </aside>
      ) : null}
      {paneOpen ? (
        <SidePane
          title={chat.paneOptions?.title ?? ''}
          backLabel={chat.paneOptions?.backLabel}
          tabs={chat.paneOptions?.tabs}
          width={chat.paneWidth}
          onWidth={actions.setPaneWidth}
          onBack={actions.closePane}
          labels={labels}
          mode={chat.paneOptions?.mode ?? paneMode}
          version={chat.paneOptions?.version}
          actions={chat.paneOptions?.actions}
        >
          {chat.paneContent}
        </SidePane>
      ) : null}
    </div>
  )
}

export { collapsedFromConfig, paneIsOpen, readRailSections, resolveRailSections }
