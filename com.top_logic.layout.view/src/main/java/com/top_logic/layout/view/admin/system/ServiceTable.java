/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewCommands;
import com.top_logic.layout.view.element.Icons;
import com.top_logic.layout.view.table.RowCommandColumn;
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
 * The status tells whether the service is running and, for a running service whose stored
 * configuration has changed since it was started (see
 * {@link BasicRuntimeModule#isConfigurationChanged()}), that it must be restarted to apply it.
 * </p>
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The table shows a live snapshot of the known modules when opened and rebuilds from the
 * {@link ServiceSnapshot} on the configured {@link Config#getInput() input channel} whenever a new
 * one is written, e.g. after a lifecycle command. The selected module is written
 * to the {@link Config#getSelection() selection channel} (so a command can act on it) and its running
 * status to the {@link Config#getState() state channel} as {@link #STATE_ACTIVE} / {@link #STATE_INACTIVE}
 * (so start vs. stop / restart can be shown mutually exclusively); both are cleared to {@code null}
 * when the selection is empty. Activating a row - a double-click, {@code Enter} on the cursor row,
 * or the row's button - runs the {@link Config#getOnActivate() activation command} with the module.
 * </p>
 *
 * @implNote The module list comes from {@link ModuleUtil#getAllModules()}; the running status from
 *           {@link BasicRuntimeModule#isActive()}; the labels from {@link ServiceSnapshot#label(BasicRuntimeModule)}.
 */
public class ServiceTable implements UIElement {

	/** State channel value for a running module. */
	public static final String STATE_ACTIVE = "ACTIVE";

	/** State channel value for a stopped module. */
	public static final String STATE_INACTIVE = "INACTIVE";

	/**
	 * Configuration for {@link ServiceTable}.
	 */
	public interface Config extends UIElement.Config, ActionScopeDefinition {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getState()}. */
		String STATE = "state";

		/** Configuration name for {@link #getOnActivate()}. */
		String ON_ACTIVATE = "on-activate";

		/** Configuration name for {@link #getOnActivateAs()}. */
		String ACTIVATE_AS = "activate-as";

		/** Default value of {@link #getOnActivateAs()}. */
		String DEFAULT_ACTIVATE_AS = "row";

		@Override
		@ClassDefault(ServiceTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the {@link ServiceSnapshot} to display; while it holds none, the table
		 * shows a live snapshot taken at render time.
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

		/**
		 * The command a row activation runs.
		 *
		 * <p>
		 * The activated row becomes the table's selection first, then the command runs with its
		 * module as main argument, named by {@link #getOnActivateAs()}. Without a command,
		 * activating a row only selects it.
		 * </p>
		 */
		@Name(ON_ACTIVATE)
		@Nullable
		@Options(fun = AllInAppImplementations.class)
		PolymorphicConfiguration<? extends ViewCommand> getOnActivate();

		/**
		 * The name of the argument holding the module of the activated row, the main argument of the
		 * {@link #getOnActivate() command}.
		 */
		@Name(ACTIVATE_AS)
		@StringDefault(DEFAULT_ACTIVATE_AS)
		String getOnActivateAs();

		/**
		 * The {@link #getOnActivate() command} runs with the activated module as argument named by
		 * {@link #getOnActivateAs()}.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (part != null && part == getOnActivate()) {
				return ActionScope.single(getOnActivateAs());
			}
			return outer;
		}
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	private final ChannelRef _stateRef;

	private final String _cssClass;

	/** The instantiated {@link Config#getOnActivate()} command, {@code null} without one. */
	private final ViewCommand _onActivate;

	/** The configuration {@link #_onActivate} was instantiated from, {@code null} without one. */
	private final ViewCommand.Config _onActivateConfig;

	private final String _activateAs;

	/**
	 * Creates a new {@link ServiceTable} from configuration.
	 */
	@CalledByReflection
	public ServiceTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
		_stateRef = config.getState();
		_cssClass = config.getCssClass();
		_activateAs = config.getOnActivateAs();
		PolymorphicConfiguration<? extends ViewCommand> onActivate = config.getOnActivate();
		_onActivateConfig = onActivate instanceof ViewCommand.Config activateConfig ? activateConfig : null;
		_onActivate = ViewCommands.instantiate(context, onActivate, _activateAs);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		Resources resources = Resources.getInstance();
		String active = resources.getString(I18NConstants.SERVICE_STATE_ACTIVE);
		String restartRequired = resources.getString(I18NConstants.SERVICE_STATE_RESTART_REQUIRED);
		String inactive = resources.getString(I18NConstants.SERVICE_STATE_INACTIVE);

		List<Column<BasicRuntimeModule<?>, ?>> columns = new ArrayList<>();
		columns.add(textColumn("status", I18NConstants.SERVICE_COLUMN_STATUS,
			module -> module.isActive() ? (module.isConfigurationChanged() ? restartRequired : active) : inactive,
			200));
		columns.add(textColumn("name", I18NConstants.SERVICE_COLUMN_NAME, ServiceSnapshot::label, 320));
		columns.add(textColumn("technicalName", I18NConstants.SERVICE_COLUMN_TECHNICAL_NAME,
			module -> module.getImplementation().getSimpleName(), 320));

		ViewCommandModel activation = _onActivate == null || _onActivateConfig == null
			? null
			: ViewCommandModel.forCommand(context, _onActivate, _onActivateConfig, _activateAs);
		// The activation button activates its row through the table control, which is built from
		// the view over these very columns - the button reaches it once it exists.
		AtomicReference<TableViewControl<BasicRuntimeModule<?>>> controlRef = new AtomicReference<>();
		if (activation != null) {
			columns.addAll(RowCommandColumn.<BasicRuntimeModule<?>> columns(List.of(
				new RowCommandColumn.RowCommand<BasicRuntimeModule<?>>(activation,
					row -> controlRef.get().activate(row), Icons.TABLE_ACTIVATE_ROW,
					com.top_logic.layout.view.element.I18NConstants.TABLE_ACTIVATE_ROW))));
		}

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;

		// Modules are singletons, so the default identity row key is stable across rebuilds; no
		// explicit key function is needed.
		ListRowSource<BasicRuntimeModule<?>> source =
			new ListRowSource<>(rows(dataChannel == null ? null : dataChannel.get()), columns);
		DefaultTableView<BasicRuntimeModule<?>> view = DefaultTableView.create(columns, source);
		TableViewControl<BasicRuntimeModule<?>> control = new TableViewControl<>(context, view, false);
		controlRef.set(control);
		if (activation != null) {
			control.setActivationHandler(row -> activation.execute(context, row));
		}

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(rows(newValue));
				control.refreshData();
			};
			Registration registration = dataChannel.addListener(listener);
			control.addCleanupAction(registration::dispose);
		}

		ViewChannel selection = _selectionRef != null ? context.resolveChannel(_selectionRef) : null;
		ViewChannel state = _stateRef != null ? context.resolveChannel(_stateRef) : null;
		if (selection != null || state != null) {
			control.addSelectionListener(keys -> {
				BasicRuntimeModule<?> module = keys.size() == 1 ? cast(keys.iterator().next()) : null;
				if (selection != null) {
					selection.set(module);
				}
				if (state != null) {
					state.set(module == null ? null : (module.isActive() ? STATE_ACTIVE : STATE_INACTIVE));
				}
			});
		}

		control.setCssClass(_cssClass);
		return control;
	}

	/**
	 * The modules of the snapshot held by the channel value, or of a live snapshot when the channel
	 * holds none.
	 */
	private static List<BasicRuntimeModule<?>> rows(Object value) {
		ServiceSnapshot snapshot = value instanceof ServiceSnapshot given ? given : ServiceSnapshot.current();
		return new ArrayList<>(snapshot.getModules());
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
