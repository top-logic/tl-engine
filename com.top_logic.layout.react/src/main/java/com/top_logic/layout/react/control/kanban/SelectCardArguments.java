/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.kanban;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Typed arguments of the {@link ReactKanbanBoardControl#CMD_SELECT_CARD} command: the card the user
 * clicked.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans. A card selection is recorded as a
 * {@link SelectCardByKeyArguments} naming the card's business object, because the card key is
 * allocated per session.
 * </p>
 */
@Label("Select card {card}")
public interface SelectCardArguments extends SelectCardModifiers {

	/** @see #getCard() */
	String CARD = "card";

	/**
	 * The key of the selected card, as the board published it in its card descriptors.
	 */
	@Name(CARD)
	@Mandatory
	String getCard();

}
