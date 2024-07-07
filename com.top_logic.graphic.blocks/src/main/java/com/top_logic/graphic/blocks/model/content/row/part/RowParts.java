/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content.row.part;

/**
 * Well-known names of {@link RowPartType}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RowParts {

	/**
	 * A {@link LabelDisplay}.
	 */
	String LABEL_KIND = "label";

	/**
	 * A {@link SelectInput}.
	 */
	String SELECT_KIND = "select";

	/**
	 * A {@link TextInput}.
	 */
	String TEXT_KIND = "text";

}
