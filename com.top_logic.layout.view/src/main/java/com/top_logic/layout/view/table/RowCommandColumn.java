/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Table columns offering a command on every row: one icon-only button per row, running the command
 * with that row as its input.
 *
 * <p>
 * Such a column is {@link Column#pinnedEnd() pinned} to the end of the table - it acts on a row
 * instead of showing what the row is, so it stays at the right edge, out of the arrangement the
 * user makes. Each button follows the command's executability for its own row: a row the rules
 * reject shows a disabled button, a row they hide shows none.
 * </p>
 *
 * <p>
 * The columns are built from {@link ViewCommandModel}s, so any place holding configured commands
 * can offer them per row - a table's activation command, a row action, a row-level workflow step.
 * </p>
 */
public class RowCommandColumn {

	/**
	 * Prefix of the generated {@link Column#name() column names}, one column per command.
	 *
	 * <p>
	 * The leading underscore keeps a generated name apart from the attribute names the data columns
	 * carry.
	 * </p>
	 */
	private static final String COLUMN_NAME_PREFIX = "_command-";

	/**
	 * The display width of a command column in pixels: room for one icon-only button, the width the
	 * action columns of an editable table use.
	 */
	public static final int WIDTH = 48;

	/**
	 * A command offered on every row of a table.
	 *
	 * @param model
	 *        Runs the command with a row as its input and decides the button's state for that row.
	 * @param image
	 *        The button's icon for a command whose configuration declares none.
	 * @param label
	 *        The button's tooltip for a command whose configuration declares no label.
	 */
	public record RowCommand(ViewCommandModel model, ThemeImage image, ResKey label) {
		// Pure value type.
	}

	/**
	 * Builds one column per given command, in the given order.
	 *
	 * <p>
	 * A cell holds a single button, so each command becomes a column of its own: the buttons then
	 * line up in columns the user recognizes, and a command hidden for a row leaves an empty cell
	 * instead of shifting the buttons beside it.
	 * </p>
	 *
	 * @param context
	 *        The context the commands execute in - the context of the element hosting them, as for
	 *        every other way of running a configured command.
	 * @param commands
	 *        The commands to offer, in display order.
	 * @return The columns, to be appended to the table's columns.
	 */
	public static <R> List<Column<R, R>> columns(ReactContext context, List<RowCommand> commands) {
		List<Column<R, R>> result = new ArrayList<>(commands.size());
		for (int n = 0, cnt = commands.size(); n < cnt; n++) {
			result.add(column(context, COLUMN_NAME_PREFIX + n, commands.get(n)));
		}
		return result;
	}

	/**
	 * Builds the column offering a single command.
	 *
	 * @param context
	 *        The context the command executes in.
	 * @param name
	 *        The {@link Column#name() name} of the column.
	 * @param command
	 *        The command to offer on every row.
	 * @return The column holding one button per row.
	 * @see #columns(ReactContext, List)
	 */
	public static <R> Column<R, R> column(ReactContext context, String name, RowCommand command) {
		return DefaultColumn.<R, R> builder(name, row -> row)
			// A button column carries no header label - it would not fit the narrow width, and the
			// button in the cell names the command itself.
			.label(ResKey.text(""))
			.renderer(row -> renderCell(context, command, row))
			.width(WIDTH)
			.pinnedEnd(true)
			.build();
	}

	/**
	 * The cell content for one row: the button, or nothing where the command is hidden for that
	 * row.
	 */
	private static CellContent renderCell(ReactContext context, RowCommand command, Object row) {
		ExecutableState state = command.model().executability(row);
		if (state.isHidden()) {
			return CellContent.EMPTY;
		}
		return new CellContent.Raw(
			(CellControlFactory) cellContext -> button(cellContext, context, command, row, state));
	}

	/**
	 * The button of one cell.
	 *
	 * @param cellContext
	 *        The context the cell control is created in.
	 * @param context
	 *        The context the command executes in.
	 * @param state
	 *        The command's executability for {@code row}, already known to be visible.
	 */
	private static ReactButtonControl button(ReactContext cellContext, ReactContext context, RowCommand command,
			Object row, ExecutableState state) {
		ViewCommandModel model = command.model();
		String label = label(command);
		ReactButtonControl button =
			new ReactButtonControl(cellContext, label, unused -> model.execute(context, row));
		ThemeImage image = model.getImage();
		button.setImage(image != null ? image : command.image());
		button.setTooltip(label);
		button.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
		button.setDisabled(!state.isExecutable());
		return button;
	}

	/**
	 * The button's label: the command's own, or the fallback the caller supplied for a command
	 * declaring none.
	 */
	private static String label(RowCommand command) {
		ResKey key = command.model().getLabelKey();
		return Resources.getInstance().getString(key != null ? key : command.label());
	}

}
