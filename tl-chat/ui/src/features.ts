import type { ComposerControls, ThreadConfig } from './chat-conversation'

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
  /** A message may carry files. */
  attachments: boolean
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
  attachments: true,
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
    attachments: value.attachments !== false,
    approvals: value.approvals !== false,
  }
}

/** Whether two feature sets say the same, so that an unchanged answer writes no state. */
export function sameFeatures(a: ConversationFeatures, b: ConversationFeatures): boolean {
  return (Object.keys(ALL_FEATURES) as (keyof ConversationFeatures)[]).every((key) => a[key] === b[key])
}

/** What the transcript offers: what the element configured, as far as the conversation supports it. */
export function gateThreadConfig(config: ThreadConfig, features: ConversationFeatures): ThreadConfig {
  return { ...config, regenerate: config.regenerate && features.regenerate }
}

/** What the composer offers: what the element configured, as far as the conversation supports it. */
export function gateComposerControls(
  controls: ComposerControls,
  features: ConversationFeatures,
): ComposerControls {
  return {
    ...controls,
    agentPicker: controls.agentPicker && features.agentPicker,
    modelPicker: controls.modelPicker && features.modelPicker,
    thinking: controls.thinking && features.effortPicker,
    mcp: controls.mcp && features.mcpPicker,
    attachmentsEnabled: controls.attachmentsEnabled && features.attachments,
    autoApproveSwitch: controls.autoApproveSwitch && features.approvals,
  }
}
