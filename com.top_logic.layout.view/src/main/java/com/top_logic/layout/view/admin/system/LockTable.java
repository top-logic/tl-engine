/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
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

/**
 * Table of the currently-held cluster locks, one row per {@link LockInfo} with its owner, the
 * triggering operation, the locked objects and aspects, the timeout and the acquiring cluster node.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The table shows a live snapshot of the held locks when opened. When an
 * {@link Config#getInput() input channel} is configured, a written lock list replaces the rows, so a
 * release / refresh command can update the table. The selected lock is written to the configured
 * {@link Config#getSelection() selection channel} so a release command can act on it.
 * </p>
 *
 * @implNote The live snapshot is read via {@link TokenService#getAllLocks()}.
 */
public class LockTable implements UIElement {

	/**
	 * Configuration for {@link LockTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(LockTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the lock list ({@code List<LockInfo>}) to display; when unset, the table
		 * shows a live snapshot taken at render time.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel the selected {@link LockInfo} is written to (or {@code null} when the selection is
		 * cleared).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link LockTable} from configuration.
	 */
	@CalledByReflection
	public LockTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_selectionRef = config.getSelection();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<LockInfo, ?>> columns = new ArrayList<>();
		columns.add(textColumn("owner", I18NConstants.LOCK_COLUMN_OWNER,
			lock -> label(lock.getOwner()), 200));
		columns.add(textColumn("operation", I18NConstants.LOCK_COLUMN_OPERATION,
			lock -> nonNull(lock.getOperation()), 200));
		columns.add(textColumn("objects", I18NConstants.LOCK_COLUMN_OBJECTS,
			LockTable::objects, 260));
		columns.add(textColumn("aspects", I18NConstants.LOCK_COLUMN_ASPECTS,
			lock -> String.join(", ", lock.getAspects()), 200));
		columns.add(DefaultColumn.<LockInfo, Date> builder("timeout", LockInfo::getTimeout)
			.label(I18NConstants.LOCK_COLUMN_TIMEOUT)
			.renderer(timeout -> CellContent.text(label(timeout)))
			.sort(() -> Comparator.nullsFirst(Comparator.<Date> naturalOrder()))
			.width(160)
			.build());
		columns.add(textColumn("node", I18NConstants.LOCK_COLUMN_NODE,
			lock -> lock.getClusterNodeId() == null ? "" : lock.getClusterNodeId().toString(), 110));

		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;

		// LockInfo instances are distinct (identity equality), so the default identity row key is
		// unique; no explicit key function is needed.
		ListRowSource<LockInfo> source =
			new ListRowSource<>(rows(dataChannel == null ? null : dataChannel.get()), columns);
		DefaultTableView<LockInfo> view = DefaultTableView.create(columns, source);
		TableViewControl<LockInfo> control = new TableViewControl<>(context, view, false);

		if (dataChannel != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> {
				source.setElements(rows(newValue));
				control.refreshData();
			};
			dataChannel.addListener(listener);
			control.addCleanupAction(() -> dataChannel.removeListener(listener));
		}

		if (_selectionRef != null) {
			ViewChannel selection = context.resolveChannel(_selectionRef);
			control.setSelectionListener(keys -> selection.set(keys.size() == 1 ? keys.iterator().next() : null));
		}

		return control;
	}

	/**
	 * The lock list held by the channel value, or a live snapshot when the channel is unset.
	 *
	 * <p>
	 * A written (possibly empty) list is shown verbatim; an unset channel ({@code null}) falls back to
	 * the current {@link TokenService#getAllLocks()} snapshot for the initial display.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private static List<LockInfo> rows(Object value) {
		if (value instanceof List<?> list) {
			return (List<LockInfo>) list;
		}
		return new ArrayList<>(TokenService.getInstance().getAllLocks());
	}

	/**
	 * The labels of the lock's exclusively locked objects, joined by {@code ", "}.
	 */
	private static String objects(LockInfo lock) {
		return lock.getObjects().stream()
			.map(LockTable::label)
			.collect(Collectors.joining(", "));
	}

	/**
	 * The label of the given object (owner, locked object or timeout), or the empty string for
	 * {@code null}.
	 */
	private static String label(Object object) {
		return object == null ? "" : MetaLabelProvider.INSTANCE.getLabel(object);
	}

	/**
	 * The given string, or the empty string for {@code null}.
	 */
	private static String nonNull(String value) {
		return value == null ? "" : value;
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a {@link LockInfo}.
	 */
	private static Column<LockInfo, String> textColumn(String id, ResKey label,
			Function<? super LockInfo, String> value, int width) {
		return DefaultColumn.<LockInfo, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
