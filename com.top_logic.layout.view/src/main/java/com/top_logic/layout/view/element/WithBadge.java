/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.common.BadgeRole;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Declaration of an element that carries a badge: a small count or dot at its corner that says
 * something is waiting there.
 *
 * <p>
 * Mixed into the declarations of the elements a badge can stand on, which all display it alike
 * through a {@link BadgeBinding}.
 * </p>
 *
 * @see BadgeBinding
 */
public interface WithBadge extends ConfigurationItem {

	/** Configuration name for {@link #getBadge()}. */
	String BADGE = "badge";

	/** Configuration name for {@link #getBadgeRole()}. */
	String BADGE_ROLE = "badge-role";

	/** Configuration name for {@link #getBadgeLabel()}. */
	String BADGE_LABEL = "badge-label";

	/**
	 * Channel whose value the badge shows - the number of things waiting in the page the element
	 * leads to, say.
	 *
	 * <p>
	 * A number is shown as it is, a number above 99 as "99+", and the number 0 shows no badge. A
	 * short text is shown as it is, {@code true} shows a dot, and {@code null}, {@code false} or an
	 * empty text show no badge. Any other value is shown by its label. The badge follows both a new
	 * value on the channel and a change of the object that value points to, so a count computed
	 * from an edited object is up to date without the channel being written anew. A count to be
	 * computed comes from a derived channel.
	 * </p>
	 */
	@Name(BADGE)
	@Nullable
	@Format(ChannelRefFormat.class)
	ChannelRef getBadge();

	/**
	 * The color role of the badge: the brand color (the default), or the color of an error, a
	 * warning, a success or an information.
	 */
	@Name(BADGE_ROLE)
	BadgeRole getBadgeRole();

	/**
	 * The text naming the badge for a screen reader, where the badge does not say it itself - a dot
	 * ("New entries"), or a count at an element without a visible label.
	 */
	@Name(BADGE_LABEL)
	ResKey getBadgeLabel();

}
