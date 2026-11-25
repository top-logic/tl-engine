import type { ComposerControls, ThreadConfig } from './chat-conversation'

/**
 * Which files a message may carry - the server's `AttachmentPolicyDto`, the `AttachmentPolicy` of the
 * answerer of the conversation.
 */
export interface AttachmentPolicy {
  /** The `accept` attribute of the file input; empty when every type is allowed. */
  accept: string
  /**
   * The allowed types: MIME types (`text/csv`), MIME patterns (`text/*`) and file patterns
   * (`*.zip`). Empty, or holding `*\/*` or `*.*`, when every type is allowed.
   */
  types: string[]
  /** How many files one message may carry; 0 when it may carry none. */
  maxFiles: number
  /** The largest file in bytes; 0 for no limit. */
  maxFileSize: number
  /** The largest sum of the sizes of the files of one message in bytes; 0 for no limit. */
  maxMessageSize: number
}

/**
 * Which functions beyond sending a message the answerer of the open conversation supports - the
 * server's `ConversationFeaturesDto`, delivered with the conversation and with every send.
 *
 * The framework's own agent supports all of them. A conversation an external backend answers
 * supports what the backend states, and the chat hides the rest; the server refuses it as well.
 */
export interface ConversationFeatures {
  /** An answer can be produced again for the same message. */
  regenerate: boolean
  /** A sent message can be edited, which drops it and everything after it. */
  editMessages: boolean
  /** The user can choose the agent that answers. */
  agentPicker: boolean
  /** The user can choose the model. */
  modelPicker: boolean
  /** The user can choose the thinking effort. */
  effortPicker: boolean
  /** The user can choose the MCP servers attached to the run. */
  mcpPicker: boolean
  /**
   * Which files a message may carry; `null` where the server stated none - a new chat -, so that the
   * policy the chat control publishes applies (see `effectivePolicy`).
   */
  attachments: AttachmentPolicy | null
  /** The run asks for approvals in the chat, which the auto-approve switch answers. */
  approvals: boolean
}

/** Everything: what the framework's own agent supports, and what a new conversation starts with. */
export const ALL_FEATURES: ConversationFeatures = Object.freeze({
  regenerate: true,
  editMessages: true,
  agentPicker: true,
  modelPicker: true,
  effortPicker: true,
  mcpPicker: true,
  attachments: null,
  approvals: true,
})

/**
 * Reads the features a response carries. Absent - a server that states none, a send that named no
 * conversation - means everything; a field counts as unsupported only when it says `false`.
 */
export function readFeatures(raw: unknown): ConversationFeatures {
  if (!raw || typeof raw !== 'object') return ALL_FEATURES
  const value = raw as Record<string, unknown>
  return {
    regenerate: value.regenerate !== false,
    editMessages: value.editMessages !== false,
    agentPicker: value.agentPicker !== false,
    modelPicker: value.modelPicker !== false,
    effortPicker: value.effortPicker !== false,
    mcpPicker: value.mcpPicker !== false,
    attachments: readAttachmentPolicy(value.attachments),
    approvals: value.approvals !== false,
  }
}

/** Reads an `AttachmentPolicyDto`; `null` for an absent or unreadable one. */
export function readAttachmentPolicy(raw: unknown): AttachmentPolicy | null {
  if (!raw || typeof raw !== 'object') return null
  const value = raw as Record<string, unknown>
  return {
    accept: typeof value.accept === 'string' ? value.accept : '',
    types: Array.isArray(value.types) ? value.types.map((type) => String(type)) : [],
    maxFiles: count(value.maxFiles),
    maxFileSize: count(value.maxFileSize),
    maxMessageSize: count(value.maxMessageSize),
  }
}

function count(raw: unknown): number {
  return typeof raw === 'number' && raw > 0 ? raw : 0
}

/**
 * The policy a message of the open conversation is checked against: the one the conversation stated
 * with its features, or else the one the chat control published; `null` while neither is known, and
 * the server alone decides.
 */
export function effectivePolicy(
  features: ConversationFeatures,
  published: AttachmentPolicy | null,
): AttachmentPolicy | null {
  return features.attachments ?? published
}

/** Whether two feature sets say the same, so that an unchanged answer writes no state. */
export function sameFeatures(a: ConversationFeatures, b: ConversationFeatures): boolean {
  return (Object.keys(ALL_FEATURES) as (keyof ConversationFeatures)[]).every((key) =>
    key === 'attachments' ? samePolicy(a.attachments, b.attachments) : a[key] === b[key],
  )
}

/** Whether two policies say the same. */
export function samePolicy(a: AttachmentPolicy | null, b: AttachmentPolicy | null): boolean {
  return JSON.stringify(a) === JSON.stringify(b)
}

/** What the transcript offers: what the element configured, as far as the conversation supports it. */
export function gateThreadConfig(config: ThreadConfig, features: ConversationFeatures): ThreadConfig {
  return { ...config, regenerate: config.regenerate && features.regenerate }
}

/**
 * What the composer offers: what the element configured, as far as the conversation supports it.
 *
 * @param policy
 *        the files a message may carry (`effectivePolicy`); a policy allowing no file takes the
 *        attachment button away, an unknown one (`null`) leaves it
 */
export function gateComposerControls(
  controls: ComposerControls,
  features: ConversationFeatures,
  policy: AttachmentPolicy | null = features.attachments,
): ComposerControls {
  return {
    ...controls,
    agentPicker: controls.agentPicker && features.agentPicker,
    modelPicker: controls.modelPicker && features.modelPicker,
    thinking: controls.thinking && features.effortPicker,
    mcp: controls.mcp && features.mcpPicker,
    attachmentsEnabled: controls.attachmentsEnabled && (policy === null || policy.maxFiles > 0),
    autoApproveSwitch: controls.autoApproveSwitch && features.approvals,
  }
}
