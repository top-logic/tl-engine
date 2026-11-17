import React from 'react';
import type { BadgeJson } from '../state/control-state';
import { badgePartProps, usePart } from './parts';

/**
 * Draws the given state of a badge through the part `Badge`, with the props {@link badgePartProps}
 * derives. The part is looked up only where a badge is drawn, so an element that may carry a badge
 * needs the part only while it does.
 */
export const StateBadge: React.FC<{
  /** The state of the badge. */
  badge: Partial<BadgeJson>;

  /**
   * Whether the badge stands alone at an element that is named by its label and the badge, see
   * {@link badgePartProps}.
   */
  alone: boolean;
}> = ({ badge, alone }) => {
  const Badge = usePart('Badge');
  return <Badge {...badgePartProps(badge, alone)} />;
};
