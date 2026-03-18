/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactFieldListControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A demo command that opens a modal dialog showing a table with N rows where N is the
 * numeric metadata value from a chart click event.
 *
 * <p>
 * Demonstrates how chart click handlers can receive numeric metadata and display
 * dynamic content based on the clicked bar's data value.
 * </p>
 */
public class DemoChartClickCommand implements ViewCommand {

	/**
	 * Configuration for {@link DemoChartClickCommand}.
	 */
	@TagName("demo-chart-click")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(DemoChartClickCommand.class)
		Class<? extends ViewCommand> getImplementationClass();
	}

	/**
	 * Creates a new {@link DemoChartClickCommand}.
	 */
	@CalledByReflection
	public DemoChartClickCommand(InstantiationContext context, Config config) {
		// No additional config needed.
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		int count = 0;
		if (input instanceof Number) {
			count = ((Number) input).intValue();
		}

		DialogManager mgr = context.getDialogManager();
		if (mgr == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ReactWindowControl window = new ReactWindowControl(context,
			"Details (" + count + " Eintr\u00e4ge)",
			DisplayDimension.px(420), () -> mgr.closeTopDialog(DialogResult.cancelled()));

		List<ReactButtonControl> rows = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			rows.add(new ReactButtonControl(context, "Wert " + i,
				ctx -> HandlerResult.DEFAULT_RESULT));
		}
		window.setChild(new ReactFieldListControl(context, rows));

		ReactButtonControl okBtn = new ReactButtonControl(context, "OK", ctx -> {
			mgr.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		});
		window.setActions(List.of(okBtn));

		mgr.openDialog(true, window, result -> {
			// Result handling is a no-op for this demo.
		});

		return HandlerResult.DEFAULT_RESULT;
	}
}
