/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.layout;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.layout.form.boxes.model.AbstractBox;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.Table;

/**
 * {@link BoxLayout} that positions boxes in a single row or column.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SharedInstance
public class LinearLayout implements BoxLayout {

	/**
	 * Configuration options of {@link LinearLayout}.
	 */
	public interface Config extends PolymorphicConfiguration<BoxLayout> {

		/**
		 * Whether contents is layouted horizontally (vertical is default).
		 */
		boolean isHorizontal();

	}

	private static void enter(AbstractBox container, Table<ContentBox> table, int x, int y, boolean horizontal,
			List<? extends Box> content) {
		int containerColumns = container.getColumns();
		int containerRows = container.getRows();
	
		int end = horizontal ? x + containerColumns : y + containerRows;
		for (int n = 0, cnt = content.size(); n < cnt; n++) {
			Box box = content.get(n);
			boolean last = n == cnt - 1;
	
			int availableEntryColumns = horizontal ? (last ? end - x : box.getColumns()) : containerColumns;
			int availableEntryRows = horizontal ? containerRows : (last ? end - y : box.getRows());
			box.enter(x, y, availableEntryColumns, availableEntryRows, table);
	
			if (horizontal) {
				x += box.getColumns();
			} else {
				y += box.getRows();
			}
		}
	}

	private static void layoutHorizontally(AbstractBox container, List<? extends Box> content) {
		LinearLayout.layoutHorizontally(container, 0, content);
	}

	private static void layoutHorizontally(AbstractBox container, int separatorSize, List<? extends Box> content) {
		int xPos = 0;
		int maxRows = 0;
		for (Box box : content) {
			box.layout();
			maxRows = Math.max(maxRows, box.getRows());
		
			xPos += box.getColumns();
			xPos += separatorSize;
		}
		container.setDimension(xPos - separatorSize, maxRows);
	}

	private static void layoutVertically(AbstractBox container, List<? extends Box> content) {
		LinearLayout.layoutVertically(container, 0, content);
	}

	private static void layoutVertically(AbstractBox container, int separatorSize, List<? extends Box> content) {
		int maxColumns = 0;
		int yPos = 0;
		for (Box box : content) {
			box.layout();
			maxColumns = Math.max(maxColumns, box.getColumns());
	
			yPos += box.getRows();
			yPos += separatorSize;
		}
		container.setDimension(maxColumns, yPos - separatorSize);
	}

	private final boolean _horizontal;

	/**
	 * Creates a {@link LinearLayout} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LinearLayout(InstantiationContext context, Config config) {
		this(config.isHorizontal());
	}

	/**
	 * Creates a {@link LinearLayout}.
	 * 
	 * @param horizontal
	 *        See {@link #isHorizontal()}.
	 */
	public LinearLayout(boolean horizontal) {
		_horizontal = horizontal;
	}

	@Override
	public void layout(AbstractBox container, List<Box> boxes) {
		if (_horizontal) {
			LinearLayout.layoutHorizontally(container, boxes);
		} else {
			LinearLayout.layoutVertically(container, boxes);
		}
	}

	@Override
	public void enter(AbstractBox container, Table<ContentBox> table, int x, int y, List<Box> boxes) {
		LinearLayout.enter(container, table, x, y, _horizontal, boxes);
	}

	@Override
	public boolean isHorizontal() {
		return _horizontal;
	}

}
