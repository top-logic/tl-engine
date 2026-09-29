/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
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
 * Read-only table showing the current stack trace of the {@link Thread} held by an input channel,
 * one row per stack frame. It updates reactively when the channel selection changes.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element),
 * the detail companion of {@link ThreadTable}.
 * </p>
 */
public class ThreadStackTable implements UIElement {

	/**
	 * Configuration for {@link ThreadStackTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(ThreadStackTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the {@link Thread} whose stack trace is shown.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link ThreadStackTable} from configuration.
	 */
	@CalledByReflection
	public ThreadStackTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String empty = Resources.getInstance().getString(I18NConstants.THREAD_STACK_EMPTY);

		List<Column<Frame, ?>> columns = new ArrayList<>();
		columns.add(DefaultColumn.<Frame, String> builder("frame", Frame::text)
			.label(I18NConstants.THREAD_COLUMN_STACK_FRAME)
			.renderer(CellContent::text)
			.filter(new TextColumnFilter<>(text -> text))
			.width(900)
			.build());

		ViewChannel channel = context.resolveChannel(_inputRef);
		ListRowSource<Frame> source = new ListRowSource<>(frames(channel.get(), empty), columns, Frame::index);
		DefaultTableView<Frame> view = DefaultTableView.create(columns, source);
		TableViewControl<Frame> control = new TableViewControl<>(context, view, false);

		ChannelListener listener = (sender, oldValue, newValue) -> {
			source.setElements(frames(newValue, empty));
			control.refreshData();
		};
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		return control;
	}

	/**
	 * The stack-frame rows for the given selected value: a leading thread header followed by one
	 * row per stack frame, or a single hint row when nothing is selected.
	 */
	private static List<Frame> frames(Object value, String empty) {
		List<Frame> rows = new ArrayList<>();
		if (value instanceof Thread thread) {
			rows.add(new Frame(0, thread.getName() + " — " + thread.getState()));
			StackTraceElement[] stack = thread.getStackTrace();
			for (int i = 0; i < stack.length; i++) {
				rows.add(new Frame(i + 1, "at " + stack[i]));
			}
		} else {
			rows.add(new Frame(0, empty));
		}
		return rows;
	}

	/**
	 * One stack-frame row.
	 *
	 * @param index
	 *        The stable row key (frame position; frame texts may repeat under recursion).
	 * @param text
	 *        The rendered frame text.
	 */
	public record Frame(int index, String text) {
		// Accessors index()/text() are generated.
	}
}
