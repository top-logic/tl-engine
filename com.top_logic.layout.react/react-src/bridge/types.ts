/**
 * Props passed to every TopLogic React cell/field component.
 */
export interface TLCellProps {
  /** The server-side control ID. */
  controlId: string;
  /** The current component state from the server. */
  state: Record<string, unknown>;
  /** Optional component configuration. */
  config?: Record<string, unknown>;
}

/** Type codes matching the msgbuf SSEEvent hierarchy. */
export const SSE_TYPE = {
  StateEvent: 'StateEvent',
  PatchEvent: 'PatchEvent',
  ContentReplacement: 'ContentReplacement',
  ElementReplacement: 'ElementReplacement',
  PropertyUpdate: 'PropertyUpdate',
  CssClassUpdate: 'CssClassUpdate',
  FragmentInsertion: 'FragmentInsertion',
  RangeReplacement: 'RangeReplacement',
  JSSnipplet: 'JSSnipplet',
  FunctionCall: 'FunctionCall',
} as const;

export interface StateEventData {
  controlId: string;
  state: string;
}

export interface PatchEventData {
  controlId: string;
  patch: string;
}

export interface ContentReplacementData {
  elementId: string;
  html: string;
}

export interface ElementReplacementData {
  elementId: string;
  html: string;
}

export interface PropertyUpdateData {
  elementId: string;
  properties: Array<{ name: string; value: string }>;
}

export interface CssClassUpdateData {
  elementId: string;
  cssClass: string;
}

export interface FragmentInsertionData {
  elementId: string;
  position: string;
  html: string;
}

export interface RangeReplacementData {
  startId: string;
  stopId: string;
  html: string;
}

export interface JSSnippletData {
  code: string;
}

/** I18N cache invalidation has no payload fields. */
export type I18NCacheInvalidationData = Record<string, never>;

export interface FunctionCallData {
  elementId: string;
  functionRef: string;
  functionName: string;
  arguments: string;
}
