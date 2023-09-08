/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.SelectionModel;

/**
 * {@link Renderer} that writes the technical column of a {@link GridComponent}
 * with marker and edit buttons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TechnicalColumnRenderer implements Renderer<FormGroup> {

	private final GridComponent _grid;

	private final boolean _drawDisabledCheckboxes;

	/**
	 * Creates a {@link TechnicalColumnRenderer}.
	 * 
	 * @param grid
	 *        The context component.
	 */
	public TechnicalColumnRenderer(GridComponent grid) {
		this(grid, false);
	}

	/**
	 * Creates a {@link TechnicalColumnRenderer}.
	 * 
	 * @param grid
	 *        The context component.
	 * @param drawDisabledCheckboxes
	 *        if <code>true</code>, a disabled checkbox is drawn for not selectable rows, otherwise
	 *        nothing is drawn.
	 */
	public TechnicalColumnRenderer(GridComponent grid, boolean drawDisabledCheckboxes) {
		_grid = grid;
		_drawDisabledCheckboxes = drawDisabledCheckboxes;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, FormGroup value) throws IOException {
		FormGroup row = value;
		
		if (getGrid().showDetailOpener()) {
			writeGotos(context, out, row);
		}
	}

	/**
	 * Write the check box command to the given writer.
	 * 
	 * @param context
	 *        The context to get the I18N from, must not be <code>null</code>.
	 * @param out
	 *        The writer to write the buttons to, must not be <code>null</code>.
	 */
	protected void writeCheckbox(DisplayContext context, TagWriter out, FormGroup row) throws IOException {
		GridComponent grid = getGrid();
		SelectionModel selectionModel = grid.getSelectionModel();
		Object tableRow = grid.getHandler().getFirstTableRow(row);

		SelectionPartControl partControl = new SelectionPartControl(selectionModel, tableRow);
		grid.getTableField(grid.getFormContext()).getSelectionVetoListeners()
			.forEach(partControl::addSelectionVetoListener);
		if (isDrawDisabledCheckboxes() || selectionModel.isSelectable(tableRow)) {
			partControl.write(context, out);
		} else {
			// Render the selection part also in case it must not be visible. Otherwise it is quite
			// hard to ensure that the following detail openers are aligned above.
			out.beginBeginTag(SPAN);
			out.writeAttribute(STYLE_ATTR, "visibility:hidden;");
			out.endBeginTag();
			partControl.write(context, out);
			out.endTag(SPAN);
		}
	}

	/**
	 * Write the goto commands to the given writer.
	 * 
	 * @param context
	 *        The context to get the I18N from, must not be <code>null</code>.
	 * @param out
	 *        The writer to write the buttons to, must not be <code>null</code>.
	 */
	protected void writeGotos(DisplayContext context, TagWriter out, FormGroup row) throws IOException {
		CommandModel command = this.createOpenCommand(context, row);

		new ButtonControl(command).write(context, out);
	}

	/**
	 * Creates the {@link CommandModel} that opens the given (internal) grid row.
	 */
	protected CommandModel createOpenCommand(DisplayContext context, FormGroup row) {
		OpenDetailDialog commandModel = new OpenDetailDialog(getGrid(), row);
		commandModel.initialize(context);
		return commandModel;
	}

	/**
	 * Returns the {@link GridComponent} this {@link TechnicalColumnRenderer} renders.
	 */
	protected final GridComponent getGrid() {
		return _grid;
	}

	/**
	 * Returns the {@link #_drawDisabledCheckboxes} flag.
	 */
	protected final boolean isDrawDisabledCheckboxes() {
		return _drawDisabledCheckboxes;
	}

}
