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
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.headless.RecordedStep;
import com.top_logic.layout.react.headless.ScriptRecorder;
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
 * Read-only table of {@link RecordedStep captured recorder steps}, one row per step with its 1-based
 * number, target address, command and arguments.
 *
 * <p>
 * The table reflects the {@link ScriptRecorder} of the window that opened this side-window (resolved
 * through {@link RecorderAccess}) and {@link ScriptRecorder#addListener(ScriptRecorder.Listener)
 * listens} to it, so a step captured in the main window appears here live over the side-window's SSE
 * connection — no manual refresh.
 * </p>
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

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(RecordedStepsTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Optional channel synchronised with the selected step's 1-based row key: the table writes it
		 * when the user selects a row, and selects the row named by it when it changes (so a step
		 * action can advance the selection).
		 */
		@Name(SELECTION)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();
	}

	private final ChannelRef _selectionRef;

	/**
	 * Creates a new {@link RecordedStepsTable} from configuration.
	 */
	@CalledByReflection
	public RecordedStepsTable(InstantiationContext context, Config config) {
		_selectionRef = config.getSelection();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Column<NumberedStep, ?>> columns = new ArrayList<>();
		columns.add(textColumn("index", I18NConstants.COLUMN_INDEX, r -> Integer.toString(r.number()), 60));
		columns.add(textColumn("address", I18NConstants.COLUMN_ADDRESS, r -> nullSafe(r.step().address()), 460));
		columns.add(textColumn("command", I18NConstants.COLUMN_COMMAND, r -> r.step().command(), 160));
		columns.add(textColumn("arguments", I18NConstants.COLUMN_ARGUMENTS, r -> JSON.toString(r.step().arguments()), 320));

		ScriptRecorder recorder = RecorderAccess.openerRecorder(context);
		ListRowSource<NumberedStep> source =
			new ListRowSource<>(rows(recorder), columns, r -> Integer.toString(r.number()));
		DefaultTableView<NumberedStep> view = DefaultTableView.create(columns, source);
		TableViewControl<NumberedStep> control = new TableViewControl<>(context, view, false);

		if (recorder != null) {
			ScriptRecorder.Listener listener = changed -> {
				source.setElements(rows(changed));
				control.refreshData();
			};
			recorder.addListener(listener);
			control.addCleanupAction(() -> recorder.removeListener(listener));
		}

		bindSelection(context, control);

		return control;
	}

	/**
	 * Synchronises the table's single-row selection with the {@link Config#getSelection() selection}
	 * channel in both directions, guarded against the resulting feedback so a user click and a
	 * channel-driven advance do not loop.
	 */
	private void bindSelection(ViewContext context, TableViewControl<NumberedStep> control) {
		if (_selectionRef == null) {
			return;
		}
		ViewChannel selection = context.resolveChannel(_selectionRef);
		boolean[] updating = { false };
		control.setSelectionListener(selectedKeys -> {
			if (updating[0]) {
				return;
			}
			updating[0] = true;
			try {
				selection.set(selectedKeys.size() == 1 ? selectedKeys.iterator().next() : null);
			} finally {
				updating[0] = false;
			}
		});
		ChannelListener selectionListener = (sender, oldValue, newValue) -> {
			if (updating[0]) {
				return;
			}
			updating[0] = true;
			try {
				control.selectRow(newValue);
			} finally {
				updating[0] = false;
			}
		};
		selection.addListener(selectionListener);
		control.addCleanupAction(() -> selection.removeListener(selectionListener));
	}

	/**
	 * The recorder's captured steps as {@link NumberedStep}s (1-based), or an empty list when there is
	 * no recorder.
	 */
	private static List<NumberedStep> rows(ScriptRecorder recorder) {
		List<NumberedStep> result = new ArrayList<>();
		if (recorder != null) {
			int number = 1;
			for (RecordedStep step : recorder.steps()) {
				result.add(new NumberedStep(number++, step));
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
