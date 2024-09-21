/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.editor.config.ButtonTemplateParameters;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.GenericCommnadActionOp;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link TableConfigurationProvider} that adds a column with custom commands that operate on the
 * model object displayed in a row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Column with buttons")
public class ButtonColumnProvider
		extends AbstractConfiguredInstance<ButtonColumnProvider.Config<?>>
		implements TableConfigurationProvider, CellRenderer {

	/**
	 * Configuration options for {@link ButtonColumnProvider}.
	 */
	@DisplayOrder({
		Config.COLUMN_LABEL,
		Config.WIDTH,
		Config.BUTTONS,
	})
	public interface Config<I extends ButtonColumnProvider>
			extends PolymorphicConfiguration<I>, ButtonTemplateParameters, ColumnProviderConfig {

		/**
		 * @see #getWidth()
		 */
		String WIDTH = "width";

		@Override
		@AcceptableClassifiers("column")
		List<ConfigBase<? extends CommandHandler>> getButtons();

		/**
		 * The width of the declared column.
		 */
		@Label(WIDTH)
		DisplayDimension getWidth();

	}

	private final List<CommandHandler> _buttons;

	private LayoutComponent _component;

	/**
	 * Creates a {@link ButtonColumnProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ButtonColumnProvider(InstantiationContext context, Config<?> config) {
		super(context, config);

		_buttons = TypedConfiguration.getInstanceList(context, config.getButtons());
		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Config<?> config = getConfig();

		String columnName = config.getColumnId();
		ColumnConfiguration column = table.declareColumn(columnName);

		ResKey labelKey = config.getColumnLabel();
		column.setColumnLabelKey(labelKey);

		column.setCellRenderer(this);
		column.setVisibility(DisplayMode.visible);
		column.setAccessor(IdentityAccessor.INSTANCE);
		column.setSortable(false);
		column.setFilterProvider(null);
		column.setClassifiers(Collections.singleton(ColumnConfig.CLASSIFIER_NO_EXPORT));

		DisplayDimension width = config.getWidth();
		if (width != null) {
			column.setDefaultColumnWidth(width.toString());
		}
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		for (CommandHandler button : _buttons) {
			Control control = createControl(button, cell);
			if (control != null) {
				control.write(context, out);
			}
		}
	}

	private Control createControl(CommandHandler button, Cell cell) {
		Object targetModel = cell.getValue();
		CommandModel commandModel = CommandModelFactory.commandModel(button, _component,
			Collections.singletonMap(CommandHandlerUtil.TARGET_MODEL_ARGUMENT, targetModel));

		if (ScriptingRecorder.isEnabled()) {
			// Note: Cell must not be stored until the command is invoked because it changes during
			// rendering.
			TableData tableData = cell.getView().getTableData();
			String columnName = cell.getColumnName();

			commandModel = new CommandModelAdapter(commandModel) {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					if (ScriptingRecorder.isRecordingActive()) {
						GenericCommnadActionOp.Config action =
							TypedConfiguration.newConfigItem(GenericCommnadActionOp.Config.class);

						CommandInTableRowNaming.Name name =
							TypedConfiguration.newConfigItem(CommandInTableRowNaming.Name.class);
						name.setTable(ModelResolver.buildModelName(tableData));
						name.setColumnId(columnName);
						name.setCommandId(button.getID());
						action.setCommand(name);

						AttributeValue value = TypedConfiguration.newConfigItem(AttributeValue.class);
						value.setName(CommandHandlerUtil.TARGET_MODEL_ARGUMENT);
						value.setValue(ModelResolver.buildModelName(targetModel));
						action.setArguments(Collections.singletonMap(value.getName(), value));

						ScriptingRecorder.recordAction(action);
					}

					return super.executeCommand(context);
				}
			};
		}

		return new ButtonControl(commandModel, ButtonRenderer.INSTANCE);
	}

	/**
	 * {@link ModelNamingScheme} for resolving {@link CommandHandler}s defined in tabel columns.
	 */
	public static class CommandInTableRowNaming
			extends AbstractModelNamingScheme<CommandHandler, CommandInTableRowNaming.Name> {

		/**
		 * {@link ModelName} for a {@link CommandHandler} defined in a table column.
		 */
		public interface Name extends ModelName {

			/**
			 * The table to locate the commands in.
			 */
			ModelName getTable();

			/**
			 * @see #getTable()
			 */
			void setTable(ModelName value);

			/**
			 * The technical ID of the column the {@link CommandHandler} is defined in.
			 */
			String getColumnId();

			/**
			 * @see #getColumnId()
			 */
			void setColumnId(String value);

			/**
			 * The technical ID of the {@link CommandHandler}.
			 */
			String getCommandId();

			/**
			 * @see #getCommandId()
			 */
			void setCommandId(String value);

		}

		/**
		 * Creates a {@link ButtonColumnProvider.CommandInTableRowNaming}.
		 */
		public CommandInTableRowNaming() {
			super(CommandHandler.class, Name.class);
		}


		@Override
		public CommandHandler locateModel(ActionContext context, Name name) {
			TableData table = (TableData) ModelResolver.locateModel(context, name.getTable());

			ColumnConfiguration column =
				table.getTableModel().getTableConfiguration().declareColumn(name.getColumnId());
			CellRenderer cellRenderer = column.getCellRenderer();
			if (!(cellRenderer instanceof ButtonColumnProvider)) {
				throw ApplicationAssertions.fail(name, "Not a button column: " + name.getColumnId());
			}
			ButtonColumnProvider provider = (ButtonColumnProvider) cellRenderer;
			List<CommandHandler> buttons = provider._buttons;
			Optional<CommandHandler> search =
				buttons.stream().filter(b -> name.getCommandId().equals(b.getID())).findAny();
			if (search.isEmpty()) {
				throw ApplicationAssertions.fail(name, "Command with ID '' not found, commands available: "
					+ buttons.stream().map(b -> b.getID()).collect(Collectors.joining(", ")));
			}
			return search.get();
		}

		@Override
		protected void initName(Name name, CommandHandler model) {
			// Ignore.
		}

		@Override
		protected boolean isCompatibleModel(CommandHandler model) {
			// Replay only.
			return false;
		}

	}

}
