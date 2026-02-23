/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo {@link LayoutComponent} that renders a React-based counter control.
 *
 * <p>
 * Demonstrates the React integration by mounting a {@code TLCounter} React component with
 * server-managed state. Increment and decrement commands are dispatched from the React client to the
 * server, and state updates are pushed back via SSE.
 * </p>
 */
public class DemoReactCounterComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactCounterComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private DemoCounterControl _counterControl;

	/**
	 * Creates a new {@link DemoReactCounterComponent}.
	 */
	public DemoReactCounterComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_counterControl == null) {
			_counterControl = new DemoCounterControl();
		}

		_counterControl.write(displayContext, out);
	}

	/**
	 * A {@link ReactControl} that renders a counter with increment and decrement commands.
	 */
	public static class DemoCounterControl extends ReactControl {

		/** State key for the current counter value. */
		private static final String COUNT = "count";

		private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
			new IncrementCommand(),
			new DecrementCommand());

		/** Creates a new {@link DemoCounterControl}. */
		public DemoCounterControl() {
			super(null, "TLCounter", COMMANDS);
			getReactState().put(COUNT, 0);
		}

		/**
		 * Increments the counter.
		 */
		public static class IncrementCommand extends ControlCommand {

			static final String COMMAND = "increment";

			/** Creates an {@link IncrementCommand}. */
			public IncrementCommand() {
				super(COMMAND);
			}

			@Override
			public ResKey getI18NKey() {
				return ResKey.legacy("demo.react.counter.increment");
			}

			@Override
			protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
				ReactControl reactControl = (ReactControl) control;
				int count = ((Number) reactControl.getReactState().get(COUNT)).intValue();
				reactControl.patchReactState(Collections.singletonMap(COUNT, count + 1));
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		/**
		 * Decrements the counter.
		 */
		public static class DecrementCommand extends ControlCommand {

			static final String COMMAND = "decrement";

			/** Creates a {@link DecrementCommand}. */
			public DecrementCommand() {
				super(COMMAND);
			}

			@Override
			public ResKey getI18NKey() {
				return ResKey.legacy("demo.react.counter.decrement");
			}

			@Override
			protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
				ReactControl reactControl = (ReactControl) control;
				int count = ((Number) reactControl.getReactState().get(COUNT)).intValue();
				reactControl.patchReactState(Collections.singletonMap(COUNT, count - 1));
				return HandlerResult.DEFAULT_RESULT;
			}
		}
	}

}
