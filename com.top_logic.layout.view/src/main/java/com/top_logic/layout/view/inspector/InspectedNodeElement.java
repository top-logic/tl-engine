/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.scripting.ScriptingAction;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.inspector.InspectedNode.StateEntry;
import com.top_logic.layout.view.model.TableSelectionBinding;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Shows what the inspected control holds: its identity, its state entry by entry, and the actions it
 * offers.
 *
 * <p>
 * The state is shown flattened to one row per leaf value, addressed by the dotted path an assertion
 * names it by, so a grouped observation - what a table hid because the user may not read it, for
 * instance - is as selectable as a plain state key. The selected paths are written to the
 * selection channel, from which a command can take what to assert on.
 * </p>
 *
 * <p>
 * The element follows the node channel and updates the controls it has built: the identity texts
 * take new values, the tables take new rows. Nothing is disposed and rebuilt, so a control that
 * listens to the same channel is never torn down while that channel is still notifying.
 * </p>
 *
 * <p>
 * App-specific widget, referenced by {@code class=} in the inspector view rather than claiming a
 * global {@code @TagName}.
 * </p>
 */
public class InspectedNodeElement implements UIElement {

	/**
	 * Number of characters of a state value shown in its cell; the full value is the cell's tooltip.
	 */
	private static final int VALUE_CELL_LENGTH = 120;

	/**
	 * Separator between the parts of the inspected control's description.
	 */
	private static final String DESCRIPTION_SEPARATOR = " · ";

	/**
	 * Display of the address: emphasized, and cut off with an ellipsis instead of overflowing the
	 * window, since an address has no spaces to wrap at.
	 */
	private static final String ADDRESS_CSS_CLASS = "tlText--strong tlText--ellipsis";

	/**
	 * Configuration for {@link InspectedNodeElement}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getNode()}. */
		String NODE = "node";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(InspectedNodeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel holding the {@link InspectedNode} to show.
		 */
		@Name(NODE)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getNode();

		/**
		 * Optional channel holding the selected state paths: one path as that path, several as the
		 * set of them, none as {@code null}.
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final ChannelRef _nodeRef;

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link InspectedNodeElement} from configuration.
	 */
	@CalledByReflection
	public InspectedNodeElement(InstantiationContext context, Config config) {
		_nodeRef = config.getNode();
		_selectionRef = config.getSelection();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel nodeChannel = context.resolveChannel(_nodeRef);
		InspectedNode node = node(nodeChannel);

		ReactTextControl addressText = new ReactTextControl(context, address(node), ADDRESS_CSS_CLASS);
		showFullAddress(addressText, node);
		ReactTextControl descriptionText = new ReactTextControl(context, description(node), null);

		List<Column<StateEntry, ?>> stateColumns = List.of(
			textColumn("path", I18NConstants.COLUMN_PATH, StateEntry::path, 220),
			valueColumn("value", I18NConstants.COLUMN_VALUE, StateEntry::value, 320));
		ListRowSource<StateEntry> stateSource =
			new ListRowSource<>(stateEntries(node), stateColumns, StateEntry::path);
		TableViewControl<StateEntry> stateTable =
			new TableViewControl<>(context, multiSelectView(stateColumns, stateSource), false);

		TableSelectionBinding selectionBinding = _selectionRef == null ? null
			: new TableSelectionBinding(stateTable, context.resolveChannel(_selectionRef));
		if (selectionBinding != null) {
			stateTable.addCleanupAction(selectionBinding::dispose);
		}

		List<Column<ScriptingAction, ?>> actionColumns = List.of(
			textColumn("action", I18NConstants.COLUMN_ACTION, ScriptingAction::command, 160),
			valueColumn("arguments", I18NConstants.COLUMN_ARGUMENTS, InspectedNodeElement::arguments, 320));
		ListRowSource<ScriptingAction> actionSource =
			new ListRowSource<>(actions(node), actionColumns, ScriptingAction::command);
		TableViewControl<ScriptingAction> actionTable =
			new TableViewControl<>(context, DefaultTableView.create(actionColumns, actionSource), false);

		ReactStackControl result = new ReactStackControl(context,
			List.<ReactControl> of(addressText, descriptionText, stateTable, actionTable));

		ChannelListener listener = (sender, oldValue, newValue) -> {
			InspectedNode current = node(nodeChannel);
			addressText.setText(address(current));
			showFullAddress(addressText, current);
			descriptionText.setText(description(current));
			stateSource.setElements(stateEntries(current));
			stateTable.refreshData();
			if (selectionBinding != null) {
				selectionBinding.rowsRefreshed();
			}
			actionSource.setElements(actions(current));
			actionTable.refreshData();
		};
		nodeChannel.addListener(listener);
		result.addCleanupAction(() -> nodeChannel.removeListener(listener));

		return result;
	}

	/**
	 * The table showing the state entries: one row per leaf, any number of them selectable.
	 */
	private static DefaultTableView<StateEntry> multiSelectView(List<Column<StateEntry, ?>> columns,
			ListRowSource<StateEntry> source) {
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, Set.of());
		state.setSelection(Selection.none(SelectionMode.MULTI));
		return new DefaultTableView<>(columns, source, state);
	}

	/**
	 * The node the channel holds, or {@code null} while nothing is inspected.
	 */
	private static InspectedNode node(ViewChannel nodeChannel) {
		return nodeChannel.get() instanceof InspectedNode node ? node : null;
	}

	/**
	 * Offers the whole address - of which the header shows as much as fits - as the tooltip of the
	 * header, where it can be read and copied.
	 */
	private static void showFullAddress(ReactTextControl addressText, InspectedNode node) {
		addressText.setTooltip(node == null ? null : escaped(node.address()), null, true);
	}

	/**
	 * The text as HTML that displays exactly it.
	 */
	private static String escaped(String text) {
		TagWriter out = new TagWriter();
		out.writeText(text);
		return out.toString();
	}

	/**
	 * The inspected control's address, or the hint to pick one while there is none.
	 */
	private static String address(InspectedNode node) {
		return node == null ? Resources.getInstance().getString(I18NConstants.HINT_NO_NODE) : node.address();
	}

	/**
	 * What the inspected control is: its role, its name where it has one, and the React module
	 * rendering it.
	 */
	private static String description(InspectedNode node) {
		if (node == null || node.view() == null) {
			return "";
		}
		StringBuilder result = new StringBuilder(node.view().role());
		if (node.view().name() != null) {
			result.append(" [").append(node.view().name()).append(']');
		}
		if (node.view().module() != null) {
			result.append(DESCRIPTION_SEPARATOR).append(node.view().module());
		}
		return result.toString();
	}

	private static List<StateEntry> stateEntries(InspectedNode node) {
		return node == null ? List.of() : node.stateEntries();
	}

	private static List<ScriptingAction> actions(InspectedNode node) {
		return node == null || node.view() == null ? List.of() : node.view().actions();
	}

	/**
	 * The action's arguments as canonical JSON: its declared schema, or its parameter names where it
	 * declares none.
	 */
	private static String arguments(ScriptingAction action) {
		if (action.argsSchema() != null) {
			return JSON.toString(action.argsSchema());
		}
		return JSON.toString(action.params().stream().map(param -> param.name()).toList());
	}

	/**
	 * A column showing a {@link String} of a row as plain text.
	 */
	private static <R> Column<R, String> textColumn(String id, ResKey label,
			Function<? super R, String> value, int width) {
		return DefaultColumn.<R, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.width(width)
			.build();
	}

	/**
	 * A column showing a technical value shortened to the cell, with the full value as its tooltip.
	 */
	private static <R> Column<R, String> valueColumn(String id, ResKey label,
			Function<? super R, String> value, int width) {
		return DefaultColumn.<R, String> builder(id, value)
			.label(label)
			.renderer(text -> new CellContent.Labeled(shorten(text), text, null, null))
			.width(width)
			.build();
	}

	/**
	 * The value as far as it is shown in a cell.
	 */
	private static String shorten(String value) {
		if (value == null || value.length() <= VALUE_CELL_LENGTH) {
			return value;
		}
		return value.substring(0, VALUE_CELL_LENGTH) + "…";
	}

}
