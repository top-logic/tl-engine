/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control.access;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.ConfiguredTableRenderer;

/**
 * Indirection for rendering a single row.
 * 
 * <p>
 * A {@link RowDisplay} value is expected to be simply written to the output by a
 * {@link TemplateExpression rendering template}. This causes a dispatch to the
 * {@link com.top_logic.layout.table.renderer.ConfiguredTableRenderer.Config#getRowTemplate() row
 * template} and the installation of a
 * {@link TableControl#addRowScope(Renderer, DisplayContext, TagWriter, int) row scope} for the
 * currently rendered table row.
 * </p>
 * 
 * @see RowRef
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RowDisplay implements HTMLFragment, Renderer<Object> {

	private final RowRef _rowView = new RowRef();

	private TableControl _table;

	private Object _rowObject;

	/**
	 * Updates the {@link ColumsCollectionRef} to a new state.
	 *
	 * @param table
	 *        The {@link TableControl} being rendered.
	 * @param rowObject
	 *        The row object of the currently rendered row.
	 * @return This instance for call chaining.
	 */
	public RowDisplay init(TableControl table, Object rowObject) {
		_table = table;
		_rowObject = rowObject;
		return this;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		_table.addRowScope(this, context, out, _table.getViewModel().getRowOfObject(_rowObject));
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		// Note value is null, Renderer interface is only used as indirection.
		((ConfiguredTableRenderer) _table.getRenderer()).renderRow(context, out, _rowView.init(_table, _rowObject));
	}

}
