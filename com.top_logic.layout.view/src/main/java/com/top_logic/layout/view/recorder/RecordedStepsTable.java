/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.recorder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.headless.RecordedStep;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Read-only table of {@link RecordedStep captured recorder steps}, one row per step with its
 * 1-based number, target address, command and arguments. The rows come from an input channel fed by
 * {@link RecorderAction}, so the table updates when recording is started, refreshed or stopped.
 *
 * <p>
 * App-specific widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * </p>
 */
public class RecordedStepsTable implements UIElement {

	/**
	 * A captured step paired with its 1-based position, so the table can show and key by the step
	 * number independent of the {@link RecordedStep} value (two identical steps stay distinct rows).
	 *
	 * @param number
	 *        The 1-based step number.
	 * @param step
	 *        The captured step.
	 */
	private record NumberedStep(int number, RecordedStep step) {
		// Pure data.
	}

	/**
	 * Configuration for {@link RecordedStepsTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(RecordedStepsTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the captured steps ({@code List<RecordedStep>}).
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link RecordedStepsTable} from configuration.
	 */
	@CalledByReflection
	public RecordedStepsTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<NumberedStep, ?>> columns = new ArrayList<>();
		columns.add(textColumn("index", I18NConstants.COLUMN_INDEX, r -> Integer.toString(r.number()), 60));
		columns.add(textColumn("address", I18NConstants.COLUMN_ADDRESS, r -> nullSafe(r.step().address()), 460));
		columns.add(textColumn("command", I18NConstants.COLUMN_COMMAND, r -> r.step().command(), 160));
		columns.add(textColumn("arguments", I18NConstants.COLUMN_ARGUMENTS, r -> JSON.toString(r.step().arguments()), 320));

		ViewChannel channel = context.resolveChannel(_inputRef);
		ListRowSource<NumberedStep> source =
			new ListRowSource<>(rows(channel.get()), columns, r -> Integer.toString(r.number()));
		DefaultTableView<NumberedStep> view = DefaultTableView.create(columns, source);
		TableViewControl<NumberedStep> control = new TableViewControl<>(context, view, false);

		ChannelListener listener = (sender, oldValue, newValue) -> {
			source.setElements(rows(newValue));
			control.refreshData();
		};
		channel.addListener(listener);
		control.addCleanupAction(() -> channel.removeListener(listener));

		return control;
	}

	/**
	 * The channel value as a list of {@link NumberedStep}s (1-based), or an empty list when nothing is
	 * captured.
	 */
	private static List<NumberedStep> rows(Object value) {
		List<NumberedStep> result = new ArrayList<>();
		if (value instanceof List<?> list) {
			int number = 1;
			for (Object element : list) {
				if (element instanceof RecordedStep step) {
					result.add(new NumberedStep(number++, step));
				}
			}
		}
		return result;
	}

	/**
	 * A text column reading one {@link String} of a {@link NumberedStep}.
	 */
	private static Column<NumberedStep, String> textColumn(String id, ResKey label,
			Function<? super NumberedStep, String> value, int width) {
		return DefaultColumn.<NumberedStep, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.width(width)
			.build();
	}

	/**
	 * Maps {@code null} to the empty string for display.
	 */
	private static String nullSafe(String value) {
		return value == null ? "" : value;
	}
}
