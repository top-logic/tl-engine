/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * A {@link CellRenderer} implementation that uniformly uses the same
 * {@link Renderer} for all columns in a row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UniformCellRenderer extends AbstractCellRenderer {

	/**
	 * Configuration of an {@link UniformCellRenderer}
	 */
	public interface Config extends PolymorphicConfiguration<UniformCellRenderer> {

		/** @see #getRenderer() */
		String RENDERER_NAME = "renderer";

		/**
		 * {@link Renderer} to dispatch to.
		 */
		@InstanceFormat
		@Mandatory
		@Name(RENDERER_NAME)
		Renderer<?> getRenderer();
	}

	private Renderer<Object> _valueRenderer;

	/**
	 * Creates a {@link UniformCellRenderer}.
	 * 
	 * @param valueRenderer
	 *        See {@link #getRenderer()}.
	 */
	public UniformCellRenderer(Renderer<Object> valueRenderer) {
		_valueRenderer = valueRenderer;
	}
	
	/**
	 * Creates a new {@link UniformCellRenderer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link UniformCellRenderer}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public UniformCellRenderer(InstantiationContext context, Config config) throws ConfigurationException {
		this(config.getRenderer().generic());
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		_valueRenderer.write(context, out, cell.getValue());
	}
	
	/**
	 * The {@link Renderer} for cell values.
	 * 
	 * @see TableModel#getValueAt(int, int)
	 */
	public Renderer<Object> getRenderer() {
		return _valueRenderer;
	}

	/**
	 * Creates a {@link UniformCellRenderer} for the given column.
	 * 
	 * @return Never null.
	 */
	public static UniformCellRenderer forColumn(ColumnConfiguration column) {
		return new UniformCellRenderer(column.finalRenderer());
	}

}
