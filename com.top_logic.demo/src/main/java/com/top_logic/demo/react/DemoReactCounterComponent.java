/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
import com.top_logic.layout.react.SSEUpdateQueue;
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

	private ReactControl _counterControl;

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
			_counterControl = createCounterControl();
		}

		SSEUpdateQueue queue = SSEUpdateQueue.forSession(request.getSession());
		_counterControl.setSSEQueue(queue);

		_counterControl.write(displayContext, out);

		// Register after write(), because the control ID is only assigned during write.
		queue.registerControl(_counterControl);
	}

	private ReactControl createCounterControl() {
		Map<String, ControlCommand> commands = new HashMap<>();
		commands.put(IncrementCommand.COMMAND, new IncrementCommand());
		commands.put(DecrementCommand.COMMAND, new DecrementCommand());

		ReactControl control = new ReactControl(null, "TLCounter", commands);
		control.getReactState().put("count", 0);

		return control;
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
			int count = ((Number) reactControl.getReactState().get("count")).intValue();
			reactControl.patchReactState(Collections.singletonMap("count", count + 1));
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
			int count = ((Number) reactControl.getReactState().get("count")).intValue();
			reactControl.patchReactState(Collections.singletonMap("count", count - 1));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
