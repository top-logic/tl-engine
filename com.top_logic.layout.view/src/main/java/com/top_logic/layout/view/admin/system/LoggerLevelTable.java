/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.logging.LogConfigurator;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Table of the configured loggers, one row per logger with its name and an inline level selector that
 * changes the logger's level at runtime; the selected logger name is written to the configured
 * {@link Config#getSelection() selection channel} so it can be removed.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element). The
 * loggers and their current levels come from the {@link LogConfigurator} facade; changing a row's level
 * applies immediately via {@link LogConfigurator#setLoggerLevel(String, String)}. A live snapshot is taken
 * when the view opens and rebuilt from the configured {@link Config#getInput() input channel} after an
 * add / remove command.
 * </p>
 */
public class LoggerLevelTable implements UIElement {

	/** Display name used for the root logger, whose technical name is the empty string. */
	private static final String ROOT_DISPLAY = "<root>";

	/**
	 * Configuration for {@link LoggerLevelTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(LoggerLevelTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the logger list ({@code List<Map.Entry<String, String>>}) to display; when unset
		 * or holding a non-list, a live snapshot is taken at render time.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel the selected logger name is written to (or {@code null} when the selection is cleared).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link LoggerLevelTable} from configuration.
	 */
	@CalledByReflection
	public LoggerLevelTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<Map.Entry<String, String>, ?>> columns = new ArrayList<>();
		columns.add(textColumn("logger", I18NConstants.LOGGER_COLUMN_NAME,
			entry -> displayName(entry.getKey()), 460));
		columns.add(DefaultColumn.<Map.Entry<String, String>, Map.Entry<String, String>> builder("level",
			Function.identity())
			.label(I18NConstants.LOGGER_COLUMN_LEVEL)
			.renderer(entry -> new CellContent.Raw((CellControlFactory) ctx -> levelControl(ctx, entry)))
			.width(160)
			.build());

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		ListRowSource<Map.Entry<String, String>> source =
			new ListRowSource<>(rows(dataChannel == null ? null : dataChannel.get()), columns);
		DefaultTableView<Map.Entry<String, String>> view = DefaultTableView.create(columns, source);
		TableViewControl<Map.Entry<String, String>> control = new TableViewControl<>(context, view, false);

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(rows(newValue));
				control.refreshData();
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}

		ViewChannel selection = _selectionRef != null ? context.resolveChannel(_selectionRef) : null;
		if (selection != null) {
			control.setSelectionListener(keys -> {
				Object key = keys.size() == 1 ? keys.iterator().next() : null;
				selection.set(key instanceof Map.Entry<?, ?> entry ? entry.getKey() : null);
			});
		}
		return control;
	}

	/**
	 * The logger list held by the channel value, or a live snapshot when the channel is unset or empty.
	 */
	@SuppressWarnings("unchecked")
	private static List<Map.Entry<String, String>> rows(Object value) {
		if (value instanceof List<?> list) {
			return (List<Map.Entry<String, String>>) list;
		}
		return new ArrayList<>(LogConfigurator.getInstance().getLoggerLevels().entrySet());
	}

	/**
	 * Builds the inline level selector for the given logger row, applying a chosen level immediately. The
	 * selector is not clearable - a logger always has a level.
	 */
	private static ReactControl levelControl(ReactContext context, Map.Entry<String, String> row) {
		List<String> levels = LogConfigurator.getInstance().getLevelNames();
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(row.getValue(), levels, false);
		model.setMandatory(true);
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				String level = selectedLevel(newValue);
				if (level != null) {
					LogConfigurator.getInstance().setLoggerLevel(row.getKey(), level);
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// No reaction needed.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// No reaction needed.
			}
		});
		return new ReactDropdownSelectControl(context, model, MetaLabelProvider.INSTANCE, null, false);
	}

	/**
	 * The selected level name extracted from a select model value (a single-element selection list).
	 */
	private static String selectedLevel(Object value) {
		if (value instanceof List<?> list) {
			return list.isEmpty() ? null : String.valueOf(list.get(0));
		}
		return value == null ? null : String.valueOf(value);
	}

	/**
	 * The display name of a logger, mapping the empty root-logger name to {@link #ROOT_DISPLAY}.
	 */
	private static String displayName(String loggerName) {
		return loggerName == null || loggerName.isEmpty() ? ROOT_DISPLAY : loggerName;
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a logger row.
	 */
	private static Column<Map.Entry<String, String>, String> textColumn(String id, ResKey label,
			Function<? super Map.Entry<String, String>, String> value, int width) {
		return DefaultColumn.<Map.Entry<String, String>, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
