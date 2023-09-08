/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.layout;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.form.boxes.border.HSpaceProvider;
import com.top_logic.layout.form.boxes.model.AbstractBox;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.BoxFactory;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.Table;

/**
 * {@link BoxLayout} positioning subsequent boxes vertically but using a fixed numner of super
 * columns.
 * 
 * <p>
 * The contents is spread among the super columns so that the total rows being used of the container
 * is kept to a (not strictly mathematical) minimum.
 * </p>
 * 
 * <p>
 * <b>Note:</b> This {@link BoxLayout} is stateful and must not be used in more than one {@link Box}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColumnsLayout implements BoxLayout {

	/**
	 * Configuration options for {@link ColumnsLayout}.
	 */
	public interface Config extends PolymorphicConfiguration<ColumnsLayout> {

		/**
		 * The number of columns in which to layout contents.
		 */
		@IntDefault(2)
		int getColumns();

		/**
		 * The {@link BoxFactory} to create separator boxes to be placed between columns.
		 */
		@InstanceFormat
		@InstanceDefault(HSpaceProvider.class)
		BoxFactory getSeparatorFactory();

	}

	private BoxFactory _separatorFactory;

	private final int _superColumns;

	private final int[] _subColumns;

	private int _containerRows;

	/**
	 * Creates a {@link ColumnsLayout} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnsLayout(InstantiationContext context, Config config) {
		this(config.getColumns(), config.getSeparatorFactory());
	}

	/**
	 * Creates a {@link ColumnsLayout}.
	 * 
	 * @param superColumns
	 *        See {@link Config#getColumns()}.
	 * @param separatorFactory
	 *        See {@link Config#getSeparatorFactory()}.
	 */
	public ColumnsLayout(int superColumns, BoxFactory separatorFactory) {
		_superColumns = superColumns;
		_subColumns = new int[_superColumns];

		_separatorFactory = separatorFactory;
	}

	@Override
	public boolean isHorizontal() {
		return false;
	}

	@Override
	public void layout(AbstractBox container, List<Box> boxes) {
		int totalRows = 0;
		for (int n = 0, cnt = boxes.size(); n < cnt; n++) {
			Box box = boxes.get(n);
			box.layout();
			totalRows += box.getRows();
		}

		int avgRows = (totalRows + _superColumns - 1) / _superColumns;

		_containerRows = 0;
		for (int n = 0, cnt = boxes.size(), currentRow = 0; n < cnt; n++) {
			Box box = boxes.get(n);
			int boxRows = box.getRows();
			int nextRow = currentRow + boxRows;
			if (nextRow >= avgRows) {
				_containerRows = Math.max(_containerRows, nextRow);
				nextRow = 0;
			}
			currentRow = nextRow;
		}

		Arrays.fill(_subColumns, 0);
		for (int n = 0, cnt = boxes.size(), currentRow = 0, superColumn = 0; n < cnt; n++) {
			Box box = boxes.get(n);
			int boxRows = box.getRows();
			int nextRow = currentRow + boxRows;
			if (nextRow > _containerRows) {
				// Box does not fit into the current super column anymore.
				superColumn++;
				nextRow = boxRows;
			}
			_subColumns[superColumn] = Math.max(_subColumns[superColumn], box.getColumns());
			currentRow = nextRow;
		}

		int borderColumns;
		Box probe = _separatorFactory.newBox();
		probe.layout();
		borderColumns = probe.getColumns() * (_superColumns - 1);
		int containerColumns = sum(_subColumns) + borderColumns;
		container.setDimension(containerColumns, _containerRows);
	}

	@Override
	public void enter(AbstractBox container, Table<ContentBox> table, int x, int y, List<Box> boxes) {
		int cnt = boxes.size();
		int lastIndex = cnt - 1;
		for (int n = 0, row = 0, column = 0, superColumn = 0; n < cnt; n++) {
			Box box = boxes.get(n);
			int boxRows = box.getRows();

			int nextRow = row + boxRows;
			if (nextRow > _containerRows) {
				// Box does not fit into the current super column anymore.
				column += _subColumns[superColumn];
				superColumn++;
				row = 0;
				nextRow = boxRows;
				
				Box separator = _separatorFactory.newBox();
				separator.layout();
				separator.enter(x + column, y, separator.getColumns(), _containerRows, table);
				column += separator.getColumns();
			}

			boolean lastInRow = n == lastIndex || nextRow + boxes.get(n + 1).getRows() > _containerRows;

			int availableRows = lastInRow ? _containerRows - row : boxRows;
			box.enter(x + column, y + row, _subColumns[superColumn], availableRows, table);

			row = nextRow;
		}
	}

	private static int sum(int[] values) {
		int result = 0;
		for (int value : values) {
			result += value;
		}
		return result;
	}

}
