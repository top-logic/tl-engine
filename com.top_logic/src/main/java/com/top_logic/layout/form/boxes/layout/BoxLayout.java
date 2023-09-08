/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.layout;

import java.util.List;

import com.top_logic.layout.form.boxes.model.AbstractBox;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;
import com.top_logic.layout.form.boxes.model.Table;

/**
 * Algorithm for layouting {@link DefaultCollectionBox} contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BoxLayout {

	/**
	 * Whether contents is (semantically) layouted horizontally.
	 */
	boolean isHorizontal();

	/**
	 * Container implementation of {@link AbstractBox#localLayout()}
	 */
	void layout(AbstractBox container, List<Box> boxes);

	/**
	 * Container implementation of {@link AbstractBox#enterContent(int, int, int, int, Table)}
	 */
	void enter(AbstractBox container, Table<ContentBox> table, int x, int y, List<Box> boxes);

}
