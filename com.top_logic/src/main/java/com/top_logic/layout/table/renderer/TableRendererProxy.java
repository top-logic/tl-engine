/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableUpdateAccumulator.UpdateRequest;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * A {@link TableRenderer} that forwards all calls defined in the interface
 * {@link TableRenderer} to a {@link #implementation renderer implementation object}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TableRendererProxy<C extends TableRendererProxy.Config<?>> extends AbstractTableRenderer<C> {

	public interface Config<I extends TableRendererProxy<?>> extends AbstractTableRenderer.Config<I> {

		/**
		 * Name of {@link #getImplementation()} property.
		 */
		String IMPLEMENTATION_NAME = "tableRenderer";

		/**
		 * The configuration of the actual {@link TableRenderer} to use as dispatch.
		 */
		@Name(IMPLEMENTATION_NAME)
		TableRenderer.Config<?> getImplementation();

		/**
		 * @see #getImplementation()
		 */
		void setImplementation(TableRenderer.Config<?> config);
	}

	protected TableRenderer<?> implementation;
	
	public TableRendererProxy(InstantiationContext context, C config) {
		super(context, config);
		implementation = (TableRenderer<?>) TableRendererUtil.getInstance(context, config.getImplementation());
	}

	public TableRenderer<?> getImplementation() {
		return implementation;
	}

	@Override
	public void write(DisplayContext context, RenderState state, TagWriter out) throws IOException {
		implementation.write(context, state, out);
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TableControl control)
			throws IOException {
		if (implementation instanceof AbstractTableRenderer<?>) {
			((AbstractTableRenderer<?>) implementation).writeControlTagAttributes(context, out, control);
		} else {
			super.writeControlTagAttributes(context, out, control);
		}
	}

	@Override
	public void writeColumn(TagWriter out, DisplayContext context, RenderState state, boolean isSelected, int column,
			int displayedRow, int row, int leftOffset) throws IOException {
		implementation.writeColumn(out, context, state, isSelected, column, displayedRow, row, leftOffset);
	}

	@Override
	public void writeCellContent(DisplayContext context, TagWriter out, RenderState state, boolean isSelected,
			int column, int row) throws IOException {
		implementation.writeCellContent(context, out, state, isSelected, column, row);
	}

	@Override
	public void writeGroupColumnContent(DisplayContext context, TagWriter out, RenderState state, Column group,
			int columnIndex, int groupSpan,
			String label) throws IOException {
		implementation.writeGroupColumnContent(context, out, state, group, columnIndex, groupSpan, label);
	}

	@Override
	public void writeColumnHeader(DisplayContext context, TagWriter out, RenderState state, int row, int column)
			throws IOException {
		implementation.writeColumnHeader(context, out, state, row, column);
	}
	
	@SuppressWarnings("deprecation")
	@Deprecated
	@Override
	public int getNumberHeaderRows() {
		return implementation.getNumberHeaderRows();
	}

	@Override
	public void writeEmptyFooter(TagWriter out, RenderState state, boolean usePagingFooter) throws IOException {
		implementation.writeEmptyFooter(out, state, usePagingFooter);
	}

	@Override
	public void writeFooter(DisplayContext context, TagWriter out, RenderState state, int aNumber) throws IOException {
		implementation.writeFooter(context, out, state, aNumber);
	}

	@Override
	public void writePagingFooter(DisplayContext context, TagWriter out, RenderState state, boolean usePagingFooter) throws IOException {
		implementation.writePagingFooter(context, out, state, usePagingFooter);
	}

	@Override
	public boolean writeTitle(DisplayContext context, TagWriter out, RenderState state) throws IOException {
		return implementation.writeTitle(context, out, state);
	}

	@Override
	public void writeAdditionalHeader(DisplayContext context, TagWriter out, RenderState state, int headerRowNumber,
			int columnNumber) throws IOException {
		implementation.writeAdditionalHeader(context, out, state, headerRowNumber, columnNumber);
	}

	@Override
	public void appendControlCSSClasses(Appendable out, TableControl control) throws IOException {
		implementation.appendControlCSSClasses(out, control);
	}

    @Override
	public void setCustomCssClass(String aValue) {
        implementation.setCustomCssClass(aValue);
    }
    
    @Override
    public void updateRows(TableControl view, List<UpdateRequest> updateRequests, UpdateQueue actions) {
    	implementation.updateRows(view, updateRequests, actions);
    }
    
	@Override
	public void addUpdateActions(UpdateQueue updates, RenderState state, int firstRow, int lastRow) {
		implementation.addUpdateActions(updates, state, firstRow, lastRow);
	}

	@Override
	public Map<Integer, String> getRowTRStyles() {
		return implementation.getRowTRStyles();
	}

	@Override
	public String computeTRTitleClass() {
		return implementation.computeTRTitleClass();
	}

	@Override
	public String computeTRHeaderClass() {
		return implementation.computeTRHeaderClass();
	}

	@Override
	public String computeTRFooterClass() {
		return implementation.computeTRFooterClass();
	}

	@Override
	public String computeTHGroupClass() {
		return implementation.computeTHGroupClass();
	}

	@Override
	public String computeTHClass(Column column) {
		return implementation.computeTHClass(column);
	}

	@Override
	public String computeTDClass(Column column) {
		return implementation.computeTDClass(column);
	}

	@Override
	public String computeTDClassSelected(Column column) {
		return implementation.computeTDClassSelected(column);
	}

	@Override
	public String hookGetColumnWidth(ColumnConfiguration theCD) {
		return implementation.hookGetColumnWidth(theCD);
	}

	@Override
	public String getRowID(TableControl view, int row) {
		return implementation.getRowID(view, row);
	}

	@Override
	public int getRow(String rowId) {
		return implementation.getRow(rowId);
	}

	/**
	 * Finds the innermost renderer implementation of the given proxy.
	 */
	public static TableRenderer<?> getInnermostRenderer(TableRenderer<?> tableRenderer) {
		if (tableRenderer instanceof TableRendererProxy) {
			TableRenderer<?> unwrapped = ((TableRendererProxy<?>) tableRenderer).getImplementation();
			return getInnermostRenderer(unwrapped);
		} else {
			return tableRenderer;
		}
	}
}