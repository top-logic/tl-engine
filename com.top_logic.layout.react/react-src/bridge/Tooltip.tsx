import React from 'react';

export type TooltipSpec =
  | { text: string }
  | { html: string }
  | { key: string }
  | { dynamic: true };

export interface TooltipProps {
  spec: TooltipSpec;
  children: React.ReactElement;
}

/**
 * Attaches a single data-tooltip attribute to its child. Prefer setting the attribute directly
 * when the JSX already has it at hand; this wrapper exists for ergonomics.
 */
export function Tooltip({ spec, children }: TooltipProps): React.ReactElement {
  let v: string;
  if ('text' in spec) v = 'text:' + spec.text;
  else if ('html' in spec) v = 'html:' + spec.html;
  else if ('key' in spec) v = 'key:' + spec.key;
  else v = 'dynamic';
  return React.cloneElement(children, { 'data-tooltip': v } as React.Attributes);
}
