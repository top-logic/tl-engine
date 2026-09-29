/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.layout.react.control.IReactControl;
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
import com.top_logic.util.Resources;

/**
 * Table of the application services (runtime modules), one row per {@link BasicRuntimeModule} with
 * its running status, label and implementation class name.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The table shows a live snapshot of the known modules when opened and rebuilds from the configured
 * {@link Config#getInput() input channel} after a lifecycle command. The selected module is written
 * to the {@link Config#getSelection() selection channel} (so a command can act on it) and its running
 * status to the {@link Config#getState() state channel} as {@link #STATE_ACTIVE} / {@link #STATE_INACTIVE}
 * (so start vs. stop / restart can be shown mutually exclusively); both are cleared to {@code null}
 * when the selection is empty.
 * </p>
 *
 * @implNote The module list comes from {@link ModuleUtil#getAllModules()}; the running status from
 *           {@link BasicRuntimeModule#isActive()}.
 */
public class ServiceTable implements UIElement {

	/** State channel value for a running module. */
	public static final String STATE_ACTIVE = "ACTIVE";

	/** State channel value for a stopped module. */
	public static final String STATE_INACTIVE = "INACTIVE";

	/**
	 * Configuration for {@link ServiceTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getState()}. */
		String STATE = "state";

		@Override
		@ClassDefault(ServiceTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the module list ({@code List<BasicRuntimeModule>}) to display; when unset,
		 * the table shows a live snapshot taken at render time.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel the selected module is written to (or {@code null} when the selection is cleared).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Channel the selected module's running status is written to ({@link #STATE_ACTIVE} /
		 * {@link #STATE_INACTIVE}, or {@code null} when the selection is cleared).
		 */
		@Name(STATE)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getState();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	private final ChannelRef _stateRef;

	/**
	 * Creates a new {@link ServiceTable} from configuration.
	 */
	@CalledByReflection
	public ServiceTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
		_stateRef = config.getState();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		Resources resources = Resources.getInstance();
		String active = resources.getString(I18NConstants.SERVICE_STATE_ACTIVE);
		String inactive = resources.getString(I18NConstants.SERVICE_STATE_INACTIVE);

		List<Column<BasicRuntimeModule<?>, ?>> columns = new ArrayList<>();
		columns.add(textColumn("status", I18NConstants.SERVICE_COLUMN_STATUS,
			module -> module.isActive() ? active : inactive, 110));
		columns.add(textColumn("name", I18NConstants.SERVICE_COLUMN_NAME, ServiceTable::label, 320));
		columns.add(textColumn("technicalName", I18NConstants.SERVICE_COLUMN_TECHNICAL_NAME,
			module -> module.getImplementation().getSimpleName(), 320));

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;

		// Modules are singletons, so the default identity row key is stable across rebuilds; no
		// explicit key function is needed.
		ListRowSource<BasicRuntimeModule<?>> source =
			new ListRowSource<>(rows(dataChannel == null ? null : dataChannel.get()), columns);
		DefaultTableView<BasicRuntimeModule<?>> view = DefaultTableView.create(columns, source);
		TableViewControl<BasicRuntimeModule<?>> control = new TableViewControl<>(context, view, false);

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(rows(newValue));
				control.refreshData();
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}

		ViewChannel selection = _selectionRef != null ? context.resolveChannel(_selectionRef) : null;
		ViewChannel state = _stateRef != null ? context.resolveChannel(_stateRef) : null;
		if (selection != null || state != null) {
			control.setSelectionListener(keys -> {
				BasicRuntimeModule<?> module = keys.size() == 1 ? cast(keys.iterator().next()) : null;
				if (selection != null) {
					selection.set(module);
				}
				if (state != null) {
					state.set(module == null ? null : (module.isActive() ? STATE_ACTIVE : STATE_INACTIVE));
				}
			});
		}

		return control;
	}

	/**
	 * The module list held by the channel value, or a live snapshot when the channel is unset.
	 */
	@SuppressWarnings("unchecked")
	private static List<BasicRuntimeModule<?>> rows(Object value) {
		if (value instanceof List<?> list) {
			return (List<BasicRuntimeModule<?>>) list;
		}
		List<BasicRuntimeModule<?>> result = new ArrayList<>(ModuleUtil.getAllModules());
		result.sort(Comparator.comparing(ServiceTable::label));
		return result;
	}

	/**
	 * The configured label of the module's implementation class.
	 */
	private static String label(BasicRuntimeModule<?> module) {
		return I18NClassNameProvider.INSTANCE.getLabel(module.getImplementation());
	}

	@SuppressWarnings("unchecked")
	private static BasicRuntimeModule<?> cast(Object key) {
		return (BasicRuntimeModule<?>) key;
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a module.
	 */
	private static Column<BasicRuntimeModule<?>, String> textColumn(String id, ResKey label,
			Function<? super BasicRuntimeModule<?>, String> value, int width) {
		return DefaultColumn.<BasicRuntimeModule<?>, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
