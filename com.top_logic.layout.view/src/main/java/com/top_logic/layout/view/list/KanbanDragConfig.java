/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.dnd.DragConfig;

/**
 * Configuration of the {@code <drag>} of a {@link KanbanBoardElement}: that its cards may be
 * dragged, the {@link #getKind() kind} of such a drag, and when they may be dragged.
 *
 * <p>
 * The {@link #getExecutability() executability} rules decide for the board as a whole over the
 * value of the {@link #getInput() input} channel, and are followed live: while they refuse, no card
 * can be dragged. The {@link #getCardExecutability() card executability} rules decide for each card
 * separately, with the card's object as their input.
 * </p>
 */
public interface KanbanDragConfig extends DragConfig {

	/** Configuration name for {@link #getCardExecutability()}. */
	String CARD_EXECUTABILITY = "card-executability";

	/**
	 * Rules deciding which cards may be dragged, each card's object being the input they decide
	 * over.
	 *
	 * <p>
	 * A card the rules refuse offers no drag. Empty (default) lets every card be dragged while the
	 * board-wide {@link #getExecutability() executability} allows dragging at all.
	 * </p>
	 */
	@Name(CARD_EXECUTABILITY)
	@EntryTag(RULE)
	List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getCardExecutability();

}
