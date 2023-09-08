/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.google.common.collect.Iterables;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CommandListenerRegistry;
import com.top_logic.layout.CompositeContentHandler;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.action.ModelAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.mig.html.layout.ComponentContentHandler;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Specialization of {@link ComponentAction} that uses a table.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TableActionOp<T extends ModelAction> extends AbstractApplicationActionOp<T> {

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TableActionOp}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public TableActionOp(InstantiationContext context, T config) {
		super(context, config);
	}

	/**
	 * Retrieves the {@link LayoutComponent} in which the given {@link TableControl} is displayed.
	 * 
	 * @param table
	 *        The table control to retrieve {@link LayoutComponent} for.
	 * 
	 * @return May be <code>null</code>.
	 */
	public static LayoutComponent getComponent(TableControl table) {
		FrameScope frameScope = table.getFrameScope();
		LayoutComponent component;
		if (frameScope instanceof ComponentContentHandler) {
			component = ((ComponentContentHandler) frameScope).getComponent();
		} else {
			component = null;
		}
		return component;
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		ModelName tableDataName = config.getModelName();
		TableData tableData = (TableData) ModelResolver.locateModel(context, tableDataName);

		TableControl control = findTable(rootScope(context), tableData);
		if (control == null) {
			throw ApplicationAssertions.fail(config, "Table not found.");
		}
		internalProcess(context, control, argument);
		return argument;
	}

	/**
	 * Find the root {@link FrameScope} to search controls in.
	 */
	protected static FrameScope rootScope(ActionContext context) {
		return context.getMainLayout().getControlScope().getFrameScope();
	}

	private TableControl findTable(ContentHandler scope, TableData model) {
		if (scope instanceof CommandListenerRegistry) {
			for (CommandListener listener : ((CommandListenerRegistry) scope).getCommandListener()) {
				if (listener instanceof TableControl) {
					TableControl control = (TableControl) listener;
					if (control.getTableData() == model) {
						return control;
					}
				}
			}
		}

		if (scope instanceof CompositeContentHandler) {
			for (ContentHandler sub : ((CompositeContentHandler) scope).inspectSubHandlers()) {
				TableControl result = findTable(sub, model);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	/**
	 * Looks up the {@link ButtonControl} with the given {@link CommandModel} from the given scope.
	 */
	protected static AbstractControlBase findButton(ContentHandler scope, CommandModel model) {
		if (scope instanceof CommandListenerRegistry) {
			for (CommandListener listener : ((CommandListenerRegistry) scope).getCommandListener()) {
				if (listener instanceof ButtonControl) {
					ButtonControl control = (ButtonControl) listener;
					if (control.getModel() == model) {
						return control;
					}
				}
				else if (listener instanceof PopupMenuButtonControl) {
					PopupMenuButtonControl control = (PopupMenuButtonControl) listener;
					Menu menu = control.getModel().getMenu();
					if (Iterables.contains(menu.buttons(), model)) {
						return control;
					}
				}
			}
		}

		if (scope instanceof CompositeContentHandler) {
			for (ContentHandler sub : ((CompositeContentHandler) scope).inspectSubHandlers()) {
				AbstractControlBase result = findButton(sub, model);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	/**
	 * Executes the action in context of the given component and the given table.
	 */
	protected abstract void internalProcess(ActionContext context, TableControl table, Object argument);

	/**
	 * Throws an {@link ApplicationAssertion} if the column does not exist.
	 * <p>
	 * Neither of the parameters is allowed to be null.
	 * </p>
	 */
	protected void checkColumnExists(TableData tableData, String columnName) {
		if (!hasColumn(tableData, columnName)) {
			List<String> columnNames = tableData.getTableModel().getColumnNames();
			fail("No such column '" + columnName + "'. Columns: " + columnNames);
		}
	}

	private boolean hasColumn(TableData tableData, String columnName) {
		return tableData.getTableModel().getColumnIndex(columnName) != TableModel.NO_ROW;
	}

	/**
	 * Throws an {@link ApplicationAssertion} if the column is not visible.
	 * <p>
	 * Neither of the parameters is allowed to be null.
	 * </p>
	 */
	protected void checkColumnIsVisible(TableData tableData, String columnName) {
		if (!isColumnVisible(tableData, columnName)) {
			List<String> columnNames = tableData.getViewModel().getColumnNames();
			fail("Column '" + columnName + "' is not visible. Visible columns: " + columnNames);
		}
	}

	private boolean isColumnVisible(TableData tableData, String columnName) {
		return tableData.getViewModel().getColumnIndex(columnName) != TableModel.NO_ROW;
	}

}

