/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.layout.react.control.layout.LabelPosition;

/**
 * The layout of a grid of form fields.
 *
 * <p>
 * A form grid insets its fields from the container border, distributes them over as many columns as
 * the available width carries, and places each label beside its input or above it depending on how
 * wide the column it landed in is. What the configuration fixes is not the number of columns but
 * the greatest number of them wanted, and where the labels stand.
 * </p>
 */
public interface FormLayoutOptions extends ConfigurationItem {

	/** Configuration name for {@link #getMaxColumns()}. */
	String MAX_COLUMNS = "max-columns";

	/** Configuration name for {@link #getLabelPosition()}. */
	String LABEL_POSITION = "label-position";

	/**
	 * The greatest number of columns the fields are laid out in.
	 *
	 * <p>
	 * How many of them are actually filled follows the available width: a narrow display shows
	 * fewer columns, and on a phone a single one.
	 * </p>
	 */
	@Name(MAX_COLUMNS)
	@IntDefault(3)
	@Bound(comparison = Comparision.GREATER_OR_EQUAL, value = 1)
	int getMaxColumns();

	/**
	 * Where the fields render their labels relative to their inputs.
	 *
	 * <p>
	 * With {@link LabelPosition#AUTO}, each label stands beside its input where the column is wide
	 * enough for both and moves above it where it is not. {@link LabelPosition#SIDE} and
	 * {@link LabelPosition#TOP} fix the position whatever the width.
	 * </p>
	 *
	 * <p>
	 * A field stating a position of its own keeps it.
	 * </p>
	 */
	@Name(LABEL_POSITION)
	@ComplexDefault(LabelPosition.AutoDefault.class)
	LabelPosition getLabelPosition();

	/**
	 * The given {@link LabelPosition} if a grid of fields can take it, and the responsive
	 * {@link LabelPosition#AUTO} with an error reported if it is one only a single field can take.
	 *
	 * @param context
	 *        The context the rejection of a field-level position is reported to.
	 * @param position
	 *        The position stated by the configuration.
	 * @return The position the grid is laid out with.
	 */
	static LabelPosition layoutPosition(InstantiationContext context, LabelPosition position) {
		switch (position) {
			case SIDE:
			case TOP:
			case AUTO:
				return position;
			default:
				context.error("The label position '" + position.getExternalName()
					+ "' is one a single field takes, not one a grid of fields lays its fields out in. Allowed are '"
					+ LabelPosition.SIDE.getExternalName() + "', '" + LabelPosition.TOP.getExternalName() + "' and '"
					+ LabelPosition.AUTO.getExternalName() + "'.");
				return LabelPosition.AUTO;
		}
	}

}
