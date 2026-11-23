/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.control.table.TableViewPreferences;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * The commands offering the user how tables look: striped rows and lines between columns.
 *
 * <p>
 * Each is a switch that is on or off, stored per person in {@link TableViewPreferences} and put
 * into effect at once in every table of the page, without a reload. The density of a table is not
 * chosen here: tables follow the density of the application, see {@link DensityCommands}.
 * </p>
 *
 * @implNote The switch position is the command's {@link CommandModel#isActive() active} state; the
 *           menu draws a {@link CommandModel#isToggle() toggle} as a switch.
 */
@InApp
public class TableViewCommands implements ViewCommandSource {

	/**
	 * Configuration for {@link TableViewCommands}.
	 */
	@TagName("table-view-commands")
	public interface Config extends ViewCommandSource.Config<TableViewCommands> {

		@Override
		@ClassDefault(TableViewCommands.class)
		Class<? extends TableViewCommands> getImplementationClass();
	}

	private final ResKey _label;

	/**
	 * Creates a new {@link TableViewCommands} from configuration.
	 */
	@CalledByReflection
	public TableViewCommands(InstantiationContext context, Config config) {
		ResKey label = config.getLabel();
		_label = label == null ? I18NConstants.TABLES_GROUP : label;
	}

	@Override
	public ResKey getLabel() {
		return _label;
	}

	@Override
	public List<CommandModel> getCommands(ViewContext context) {
		return List.of(
			command("striped", I18NConstants.TABLE_STRIPED_ROWS, TableViewPreferences::striped,
				TableViewPreferences::setStriped, TableViewPreferences.ATTR_STRIPED),
			command("columnLines", I18NConstants.TABLE_COLUMN_LINES, TableViewPreferences::columnLines,
				TableViewPreferences::setColumnLines, TableViewPreferences.ATTR_COLUMN_LINES));
	}

	private static CommandModel command(String name, ResKey label, BooleanSupplier current,
			Consumer<Boolean> store, String attribute) {
		return SimpleCommandModel
			.create(name, Resources.getInstance().getString(label), ctx -> toggle(ctx, current, store, attribute))
			.setToggle(true)
			// Read on every display, as the menu is built once while the setting changes under it.
			.setActive(current);
	}

	private static void toggle(ReactContext context, BooleanSupplier current, Consumer<Boolean> store,
			String attribute) {
		boolean on = !current.getAsBoolean();
		store.accept(Boolean.valueOf(on));

		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create().setCode(clientCode(attribute, on)));
		}
	}

	/**
	 * The script setting or removing the given attribute of the element carrying the table view
	 * in the browser. Without such an element it does nothing.
	 *
	 * @param attribute
	 *        The attribute of the table view, see {@link TableViewPreferences#ATTR_STRIPED}.
	 * @param on
	 *        Whether the attribute is present afterwards.
	 */
	public static String clientCode(String attribute, boolean on) {
		StringBuilder code = new StringBuilder();
		code.append("(function(e){if(e)e.toggleAttribute(");
		TagUtil.writeJsString(code, attribute);
		code.append(",");
		code.append(on);
		code.append(");})(document.querySelector('[");
		code.append(TableViewPreferences.ATTR_MARK);
		code.append("]'));");
		return code.toString();
	}
}
