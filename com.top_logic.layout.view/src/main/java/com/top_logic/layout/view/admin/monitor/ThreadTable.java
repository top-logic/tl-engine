/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

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
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Read-only table of the live JVM threads (a snapshot taken at render time), one row per
 * {@link Thread} with its name, state, priority, kind and thread group.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * When a {@link Config#getSelection() selection channel} is configured, the selected thread is
 * written to it so a companion {@link ThreadStackTable} can show its current stack trace.
 * </p>
 */
public class ThreadTable implements UIElement {

	/**
	 * Configuration for {@link ThreadTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(ThreadTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel the selected {@link Thread} is written to (or {@code null} when the selection is
		 * cleared).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link ThreadTable} from configuration.
	 */
	@CalledByReflection
	public ThreadTable(InstantiationContext context, Config config) {
		_selectionRef = config.getSelection();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		Resources resources = Resources.getInstance();
		String daemon = resources.getString(I18NConstants.THREAD_KIND_DAEMON);
		String normal = resources.getString(I18NConstants.THREAD_KIND_NORMAL);

		List<Thread> rows = new ArrayList<>(Thread.getAllStackTraces().keySet());

		List<Column<Thread, ?>> columns = new ArrayList<>();
		columns.add(textColumn("name", I18NConstants.THREAD_COLUMN_NAME, Thread::getName, 280));
		columns.add(textColumn("state", I18NConstants.THREAD_COLUMN_STATE, t -> t.getState().toString(), 140));
		columns.add(textColumn("priority", I18NConstants.THREAD_COLUMN_PRIORITY,
			t -> Integer.toString(t.getPriority()), 90));
		columns.add(textColumn("kind", I18NConstants.THREAD_COLUMN_KIND, t -> t.isDaemon() ? daemon : normal, 100));
		columns.add(textColumn("group", I18NConstants.THREAD_COLUMN_GROUP, ThreadTable::groupName, 160));

		// Thread instances are distinct (identity equality), so the default identity row key is
		// unique; no explicit key function is needed.
		ListRowSource<Thread> source = new ListRowSource<>(rows, columns);
		DefaultTableView<Thread> view = DefaultTableView.create(columns, source);
		TableViewControl<Thread> control = new TableViewControl<>(context, view, false);

		if (_selectionRef != null) {
			ViewChannel selection = context.resolveChannel(_selectionRef);
			control.setSelectionListener(keys -> selection.set(keys.size() == 1 ? keys.iterator().next() : null));
		}

		return control;
	}

	/**
	 * The thread group name, or the empty string when the thread has no group.
	 */
	private static String groupName(Thread thread) {
		ThreadGroup group = thread.getThreadGroup();
		return group != null ? group.getName() : "";
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a {@link Thread}.
	 */
	private static Column<Thread, String> textColumn(String id, ResKey label,
			Function<? super Thread, String> value, int width) {
		return DefaultColumn.<Thread, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
