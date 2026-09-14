/**
 * The chat's own strings, in the language the application runs in.
 *
 * The language comes from the user's TopLogic locale, pushed into the bootstrap state by
 * `AgentChatControl`, so the chat speaks whatever the app bar and the rest of the view layer speak.
 * Anything the framework renders is already localized by the framework; only the strings this
 * bundle owns live here.
 */
export type Language = 'en' | 'de'

const EN = {
  chats: 'Chats',
  newChat: 'New chat',
  searchChats: 'Search chats',
  noChats: 'No chats yet',
  noMatchingChats: 'No matching chats',
  chatsLoadFailed: 'Chats could not be loaded.',
  chatsRefreshFailed: 'Chats could not be refreshed.',
  today: 'Today',
  yesterday: 'Yesterday',
  previous7Days: 'Previous 7 days',
  recent: 'Recent',
  older: 'Older',
  untitled: 'Untitled',
  deleteChat: 'Delete chat',
  deleteChatQuestion: 'Delete this chat?',
  delete: 'Delete',
  cancel: 'Cancel',
  showChats: 'Show chats',
  hideChats: 'Hide chats',
  closeChats: 'Close chats',
  expandChatHistory: 'Expand chat history',
  collapseChatHistory: 'Collapse chat history',

  greeting: (name: string) => (name ? `Hello ${name}.` : 'Hello.'),
  howCanIHelp: 'What can I help you with?',
  askAnything: 'Ask anything…',
  typeNext: 'Type your next message…',
  enterSends: 'Enter sends · Shift+Enter adds a line',
  enterSendsShort: 'Enter sends',
  autoApprove: 'Auto-approve',
  send: 'Send',
  stopRun: 'Stop the run',
  generating: 'Generating a response',
  jumpToLatest: 'Jump to latest',

  workedFor: (duration: string) => `Worked for ${duration}`,
  working: 'Working…',
  toolCalls: (count: number) => `${count} tool call${count === 1 ? '' : 's'}`,
  skillLabel: 'skill',
  editAndSendAgain: 'Edit and send again',
  editedMarker: 'edited',
  signInHeading: 'Sign in to use the chat',
  signInBody: 'The agent works with your data and with your permissions, so it needs to know who you are.',
  edited: 'edited',
  laterTurnsRemoved: (count: number) =>
    `${count} later turn${count === 1 ? '' : 's'} removed. Change the message and send it again.`,
  copyAnswer: 'Copy answer',
  copied: 'Copied',
  copyCode: 'Copy code',
  copyShort: 'Copy',
  goodAnswer: 'Good answer',
  badAnswer: 'Bad answer',
  regenerate: 'Answer again',
  regenerating: 'Answering again…',
  feedbackNotePlaceholder: 'What was wrong with it? (optional)',
  feedbackNoteSave: 'Save',
  feedbackNoteSkip: 'Skip',

  uploadFile: 'Upload file',
  uploadFileHint: 'Images or PDF, up to 5 files',
  agent: 'Agent',
  agentHint: 'Who answers and which tools it has',
  model: 'Model',
  modelHint: 'The model this chat runs on',
  effort: 'Effort',
  effortHint: 'How long the model may think before answering',
  effortLow: 'Low',
  effortMedium: 'Medium',
  effortHigh: 'High',
  effortMax: 'Maximum',
  mcpServers: 'MCP servers',
  mcpHint: 'External tool servers for this turn',
  noMcpServers: 'No MCP server is configured',
  traceLevel: 'Trace level',
  traceHint: "How much of the agent's work is shown",
  traceNone: 'None',
  traceNoneHint: 'Only the answer',
  traceOverview: 'Overview',
  traceOverviewHint: 'One line naming the tools that ran',
  traceDetailed: 'Detailed',
  traceDetailedHint: 'Every step with its arguments and result',
  searchMenu: 'Search agents, models, settings…',
  searchMenuLabel: 'Search agents, models and settings',
  nothingMatches: (query: string) => `Nothing matches “${query}”`,
  defaultValue: 'Default',
  none: 'None',
  on: 'On',
  off: 'Off',
  connected: 'Connected',
  notConnected: 'Not connected',
  enabledCount: (count: number) => `${count} on`,
  plusMenuLabel: 'Attach a file or change the agent, model or trace level',

  improvePrompt: 'Improve prompt',
  improving: 'Improving…',
  improveFailed: 'The prompt could not be improved.',
  improveNoChanges: 'Nothing to improve — the draft is fine as it is.',
  undoImprovement: 'Undo improvement',
  retry: 'Retry',
  reconnecting: 'Reconnecting…',
  dropHint: 'Drop images or PDFs to attach',
  /** Stands in for the message text of a send that is nothing but an attachment. */
  attachmentOnly: 'Attachment',
  dismiss: 'Dismiss',
  busyHint:
    'The agent is still working on the previous message. Your text is kept — send it again when the run has finished.',
  sendFailed: 'The message could not be sent.',
  /** Shown when the server refused a request but sent no reason of its own. */
  notAllowed: 'This agent, model or MCP server is not available to you.',
  onlyImagesAndPdf: 'only images and PDF can be attached',
  largerThan10Mb: 'larger than 10 MB',
  atMostFiles: (max: number) => `at most ${max} files`,
  removeFile: (name: string) => `Remove ${name}`,

  contextUsage: 'Context usage',
  contextFull: (percent: string) => `${percent} full`,
  systemPrompt: 'System prompt',
  toolDefinitions: 'Tool definitions',
  conversation: 'Conversation',
  toolResults: 'Tool results',
  toolsCount: (count: number) => `${count} tools`,
  lastAnswer: 'Last answer',
  tokensWritten: (tokens: string) => `${tokens} tokens written`,
  contextMeasured:
    'Measured by the provider for the last request - everything the model read, tool results included. Rows marked ~ are estimates of how that total splits.',
  contextEstimated:
    'Estimated until the first answer of this chat; the next turn reports the measured size.',
  tokens: 'tokens',

  approval: 'Approval',
  question: 'Question',
  approvalUserScoped: 'Changes your personal settings.',
  approvalDataWrite: 'Changes shared application data.',
  approvalStructural: 'Changes the structure of the application.',
  approvalExternal: 'Sends data to a system outside this application.',
  approvalGeneric: 'Needs your approval before it runs.',
  deny: 'Deny',
  allowThisChat: 'Allow this chat',
  allowOnce: 'Allow once',
  skip: 'Skip',
  continueLabel: 'Continue',
  yourAnswer: 'Your answer',
  timeIsUp: 'Time is up',
  leftToAnswer: ' left to answer',
  collapse: (kind: string) => `Collapse ${kind}`,
  expand: (kind: string) => `Expand ${kind}`,

  trace: 'Trace',
  toolsUsed: (count: number) => `${count} tool${count === 1 ? '' : 's'} used`,
  stepsUsed: (count: number) => `${count} step${count === 1 ? '' : 's'}`,
  thinking: 'Thinking',
  output: 'Output',
  argumentsLabel: 'Arguments',
  result: 'Result',
  error: 'Error',
  reason: 'Reason',
  tool: 'Tool',
  running: 'running',
  waiting: 'waiting',
  answered: 'answered',
  showLess: 'Show less',
  showMore: (chars: number) => `Show ${chars} more characters`,

  working_plural: 'Working',
  tasks: 'Tasks',
  stopAll: 'Stop all',
  stop: 'Stop',
  closeActivity: 'Close activity',
  viewTranscript: 'View transcript',
  loading: 'Loading…',
  noDetails: 'No details available.',
  transcriptFailed: 'The subagent transcript could not be loaded.',
  subagentStopFailed: 'The subagent could not be stopped.',
  subagentsStopFailed: 'The subagents could not be stopped.',
  you: 'You',
  agentLabel: 'Agent',
  providerError: 'The model returned an error. The attempt was discarded; you can send again.',
  providerErrorRateLimited: 'The model is being asked too often just now. Try again in a moment.',
  providerErrorBilling: 'The model account cannot be billed. Please tell your administrator.',
  providerErrorOverloaded: 'The model is overloaded just now. Try again in a moment.',
  providerErrorContext: 'The conversation has grown past what this model can read. Start a new chat, or switch to a model with a larger context.',
  providerErrorAuth: 'The model rejected the access key. Please tell your administrator.',
  providerErrorInvalid: 'The model refused the request. The attempt was discarded; you can send again.',
  errorDetails: 'Technical details',
  streamLost: 'Lost the event stream.',
  cancelled: 'Stopped.',

  sideRail: 'Rail',
  sideRailBadge: (count: number) => `${count} new`,
  sideBack: 'Back',
  sideSplitter: 'Resize the side pane',
  railEmpty: 'Nothing to show.',
  progressTitle: 'Progress',
  progressContributor: 'tl-ai',
  progressEmpty: 'No tasks in this run yet.',
  progressWaiting: 'Waiting for approval',
  contextTitle: 'Context',
  citationChip: (n: number) => `Citation ${n}`,
  citationKind: 'Kind',
  citationSource: 'Source',
  removeChip: (label: string) => `Remove ${label}`,
}

type Bundle = typeof EN

const DE: Bundle = {
  chats: 'Chats',
  newChat: 'Neuer Chat',
  searchChats: 'Chats durchsuchen',
  noChats: 'Noch keine Chats',
  noMatchingChats: 'Keine passenden Chats',
  chatsLoadFailed: 'Die Chats konnten nicht geladen werden.',
  chatsRefreshFailed: 'Die Chats konnten nicht aktualisiert werden.',
  today: 'Heute',
  yesterday: 'Gestern',
  previous7Days: 'Letzte 7 Tage',
  recent: 'Kürzlich',
  older: 'Älter',
  untitled: 'Ohne Titel',
  deleteChat: 'Chat löschen',
  deleteChatQuestion: 'Diesen Chat löschen?',
  delete: 'Löschen',
  cancel: 'Abbrechen',
  showChats: 'Chats einblenden',
  hideChats: 'Chats ausblenden',
  closeChats: 'Chats schließen',
  expandChatHistory: 'Chat-Verlauf ausklappen',
  collapseChatHistory: 'Chat-Verlauf einklappen',

  greeting: (name: string) => (name ? `Hallo ${name}.` : 'Hallo.'),
  howCanIHelp: 'Womit kann ich helfen?',
  askAnything: 'Frag mich etwas …',
  typeNext: 'Nächste Nachricht schreiben …',
  enterSends: 'Enter sendet · Umschalt+Enter macht einen Zeilenumbruch',
  enterSendsShort: 'Enter sendet',
  autoApprove: 'Automatisch freigeben',
  send: 'Senden',
  stopRun: 'Lauf abbrechen',
  generating: 'Antwort wird erzeugt',
  jumpToLatest: 'Zum Ende springen',

  workedFor: (duration: string) => `${duration} gearbeitet`,
  working: 'Arbeitet …',
  toolCalls: (count: number) => `${count} Werkzeugaufruf${count === 1 ? '' : 'e'}`,
  skillLabel: 'Skill',
  editAndSendAgain: 'Bearbeiten und erneut senden',
  editedMarker: 'bearbeitet',
  signInHeading: 'Melde dich an, um den Chat zu nutzen',
  signInBody: 'Der Agent arbeitet mit deinen Daten und mit deinen Rechten - dafür muss er wissen, wer du bist.',
  edited: 'bearbeitet',
  laterTurnsRemoved: (count: number) =>
    `${count} spätere ${count === 1 ? 'Antwort wurde' : 'Antworten wurden'} entfernt. Ändere die Nachricht und sende sie erneut.`,
  copyAnswer: 'Antwort kopieren',
  copied: 'Kopiert',
  copyCode: 'Code kopieren',
  copyShort: 'Kopieren',
  goodAnswer: 'Gute Antwort',
  badAnswer: 'Schlechte Antwort',
  regenerate: 'Neu beantworten',
  regenerating: 'Wird neu beantwortet …',
  feedbackNotePlaceholder: 'Was war daran falsch? (optional)',
  feedbackNoteSave: 'Speichern',
  feedbackNoteSkip: 'Überspringen',

  uploadFile: 'Datei hochladen',
  uploadFileHint: 'Bilder oder PDF, bis zu 5 Dateien',
  agent: 'Agent',
  agentHint: 'Wer antwortet und welche Werkzeuge er hat',
  model: 'Modell',
  modelHint: 'Das Modell, mit dem dieser Chat läuft',
  effort: 'Nachdenken',
  effortHint: 'Wie lange das Modell vor der Antwort nachdenken darf',
  effortLow: 'Kurz',
  effortMedium: 'Mittel',
  effortHigh: 'Lang',
  effortMax: 'Maximal',
  mcpServers: 'MCP-Server',
  mcpHint: 'Externe Werkzeug-Server für diesen Zug',
  noMcpServers: 'Es ist kein MCP-Server konfiguriert',
  traceLevel: 'Detailgrad',
  traceHint: 'Wie viel von der Arbeit des Agenten gezeigt wird',
  traceNone: 'Keiner',
  traceNoneHint: 'Nur die Antwort',
  traceOverview: 'Überblick',
  traceOverviewHint: 'Eine Zeile mit den benutzten Werkzeugen',
  traceDetailed: 'Ausführlich',
  traceDetailedHint: 'Jeder Schritt mit Argumenten und Ergebnis',
  searchMenu: 'Agenten, Modelle, Einstellungen …',
  searchMenuLabel: 'Agenten, Modelle und Einstellungen durchsuchen',
  nothingMatches: (query: string) => `Nichts passt zu „${query}“`,
  defaultValue: 'Standard',
  none: 'Keine',
  on: 'An',
  off: 'Aus',
  connected: 'Verbunden',
  notConnected: 'Nicht verbunden',
  enabledCount: (count: number) => `${count} aktiv`,
  plusMenuLabel: 'Datei anhängen oder Agent, Modell und Detailgrad ändern',

  improvePrompt: 'Prompt verbessern',
  improving: 'Wird verbessert …',
  improveFailed: 'Der Prompt konnte nicht verbessert werden.',
  improveNoChanges: 'Nichts zu verbessern — der Entwurf passt so.',
  undoImprovement: 'Verbesserung rückgängig',
  retry: 'Erneut',
  reconnecting: 'Verbindung wird wiederhergestellt …',
  dropHint: 'Bilder oder PDFs zum Anhängen hier ablegen',
  attachmentOnly: 'Anhang',
  dismiss: 'Schließen',
  busyHint:
    'Der Agent arbeitet noch an der vorherigen Nachricht. Dein Text bleibt erhalten — sende ihn erneut, wenn der Lauf fertig ist.',
  sendFailed: 'Die Nachricht konnte nicht gesendet werden.',
  notAllowed: 'Dieser Agent, dieses Modell oder dieser MCP-Server steht Ihnen nicht zur Verfügung.',
  onlyImagesAndPdf: 'nur Bilder und PDF können angehängt werden',
  largerThan10Mb: 'größer als 10 MB',
  atMostFiles: (max: number) => `höchstens ${max} Dateien`,
  removeFile: (name: string) => `${name} entfernen`,

  contextUsage: 'Kontextnutzung',
  contextFull: (percent: string) => `${percent} belegt`,
  systemPrompt: 'System-Prompt',
  toolDefinitions: 'Werkzeugdefinitionen',
  conversation: 'Konversation',
  toolResults: 'Werkzeugergebnisse',
  toolsCount: (count: number) => `${count} Werkzeuge`,
  lastAnswer: 'Letzte Antwort',
  tokensWritten: (tokens: string) => `${tokens} Token geschrieben`,
  contextMeasured:
    'Vom Anbieter für die letzte Anfrage gemessen - alles, was das Modell gelesen hat, Werkzeugergebnisse eingeschlossen. Mit ~ markierte Zeilen schätzen die Aufteilung.',
  contextEstimated:
    'Geschätzt bis zur ersten Antwort dieses Chats; der nächste Zug meldet die gemessene Größe.',
  tokens: 'Token',

  approval: 'Freigabe',
  question: 'Rückfrage',
  approvalUserScoped: 'Ändert deine persönlichen Einstellungen.',
  approvalDataWrite: 'Ändert gemeinsame Anwendungsdaten.',
  approvalStructural: 'Ändert die Struktur der Anwendung.',
  approvalExternal: 'Sendet Daten an ein System außerhalb dieser Anwendung.',
  approvalGeneric: 'Braucht deine Freigabe, bevor es ausgeführt wird.',
  deny: 'Ablehnen',
  allowThisChat: 'In diesem Chat erlauben',
  allowOnce: 'Einmal erlauben',
  skip: 'Überspringen',
  continueLabel: 'Weiter',
  yourAnswer: 'Deine Antwort',
  timeIsUp: 'Zeit abgelaufen',
  leftToAnswer: ' verbleiben zum Antworten',
  collapse: (kind: string) => `${kind} einklappen`,
  expand: (kind: string) => `${kind} ausklappen`,

  trace: 'Verlauf',
  toolsUsed: (count: number) => `${count} Werkzeug${count === 1 ? '' : 'e'} benutzt`,
  stepsUsed: (count: number) => `${count} Schritt${count === 1 ? '' : 'e'}`,
  thinking: 'Nachdenken',
  output: 'Ausgabe',
  argumentsLabel: 'Argumente',
  result: 'Ergebnis',
  error: 'Fehler',
  reason: 'Grund',
  tool: 'Werkzeug',
  running: 'läuft',
  waiting: 'wartet',
  answered: 'beantwortet',
  showLess: 'Weniger anzeigen',
  showMore: (chars: number) => `${chars} weitere Zeichen anzeigen`,

  working_plural: 'Arbeitet',
  tasks: 'Aufgaben',
  stopAll: 'Alle abbrechen',
  stop: 'Abbrechen',
  closeActivity: 'Aktivität schließen',
  viewTranscript: 'Protokoll ansehen',
  loading: 'Wird geladen …',
  noDetails: 'Keine Details vorhanden.',
  transcriptFailed: 'Das Protokoll des Subagenten konnte nicht geladen werden.',
  subagentStopFailed: 'Der Subagent konnte nicht abgebrochen werden.',
  subagentsStopFailed: 'Die Subagenten konnten nicht abgebrochen werden.',
  you: 'Du',
  agentLabel: 'Agent',
  providerError: 'Das Modell hat einen Fehler gemeldet. Der Versuch wurde verworfen, du kannst erneut senden.',
  providerErrorRateLimited: 'Das Modell wird gerade zu oft angefragt. Versuche es gleich noch einmal.',
  providerErrorBilling: 'Das Modellkonto kann nicht abgerechnet werden. Bitte wende dich an deine Administration.',
  providerErrorOverloaded: 'Das Modell ist gerade überlastet. Versuche es gleich noch einmal.',
  providerErrorContext: 'Das Gespräch ist länger, als dieses Modell lesen kann. Beginne einen neuen Chat oder wechsle zu einem Modell mit größerem Kontext.',
  providerErrorAuth: 'Das Modell hat den Zugangsschlüssel abgelehnt. Bitte wende dich an deine Administration.',
  providerErrorInvalid: 'Das Modell hat die Anfrage abgelehnt. Der Versuch wurde verworfen, du kannst erneut senden.',
  errorDetails: 'Technische Details',
  streamLost: 'Die Ereignisverbindung ist abgerissen.',
  cancelled: 'Abgebrochen.',

  sideRail: 'Leiste',
  sideRailBadge: (count: number) => `${count} neu`,
  sideBack: 'Zurück',
  sideSplitter: 'Seitenbereich in der Breite ändern',
  railEmpty: 'Nichts anzuzeigen.',
  progressTitle: 'Fortschritt',
  progressContributor: 'tl-ai',
  progressEmpty: 'In diesem Lauf gibt es noch keine Aufgaben.',
  progressWaiting: 'Wartet auf Freigabe',
  contextTitle: 'Kontext',
  citationChip: (n: number) => `Beleg ${n}`,
  citationKind: 'Art',
  citationSource: 'Quelle',
  removeChip: (label: string) => `${label} entfernen`,
}

const BUNDLES: Record<Language, Bundle> = { en: EN, de: DE }

/** The language of a TopLogic locale tag such as `de_DE`; anything unknown falls back to English. */
export function toLanguage(locale: unknown): Language {
  const tag = typeof locale === 'string' ? locale.toLowerCase() : ''
  return tag.startsWith('de') ? 'de' : 'en'
}

/** The strings of the given language. */
export function bundle(language: Language): Bundle {
  return BUNDLES[language] ?? EN
}

/**
 * Formats a timestamp the way the user's locale writes it: the time alone for today, the date and
 * the time otherwise, because a time without a date is ambiguous the moment it is not today.
 */
export function formatTimestamp(timestamp: number, language: Language): string {
  const date = new Date(timestamp)
  const locale = language === 'de' ? 'de-DE' : 'en-GB'
  const now = new Date()
  const sameDay =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  if (sameDay) {
    return date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleString(locale, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * What a failed run says to the reader, in their language.
 *
 * The provider's own message names a field of a JSON request and is written for whoever wrote that
 * request, not for whoever was typing. The `kind` is the part that means something to a reader -
 * that the model was busy, that the conversation outgrew it, that a key is wrong - so the strip
 * says that, and the provider's wording is kept for the tooltip.
 */
export function providerErrorText(kind: string | undefined, language: Language): string {
  const labels = bundle(language)
  switch ((kind ?? '').toUpperCase()) {
    case 'RATE_LIMITED':
      return labels.providerErrorRateLimited
    case 'BILLING':
      return labels.providerErrorBilling
    case 'OVERLOADED':
      return labels.providerErrorOverloaded
    case 'CONTEXT_WINDOW_EXCEEDED':
      return labels.providerErrorContext
    case 'AUTHENTICATION':
      return labels.providerErrorAuth
    case 'INVALID_REQUEST':
      return labels.providerErrorInvalid
    default:
      return labels.providerError
  }
}
