/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a counter with increment and decrement commands.
 *
 * <p>
 * Mounts the {@code TLCounter} React component with server-managed state: increment and decrement
 * commands are dispatched from the React client to the server, and state updates are pushed back via
 * SSE.
 * </p>
 */
public class DemoCounterControl extends ReactControl {

	/** State key for the current counter value. */
	private static final String COUNT = "count";

	/** State key for the display label. */
	private static final String LABEL = "label";

	/**
	 * Creates a new {@link DemoCounterControl} with default label.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public DemoCounterControl(ReactContext context) {
		this(context, null);
	}

	/**
	 * Creates a new {@link DemoCounterControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param label
	 *        The display label, or {@code null} for the default.
	 */
	public DemoCounterControl(ReactContext context, String label) {
		super(context, null, "TLCounter");
		setCount(0);
		if (label != null) {
			putState(LABEL, label);
		}
	}

	private int getCount() {
		return ((Number) getState(COUNT)).intValue();
	}

	private void setCount(int count) {
		putState(COUNT, count);
	}

	@ReactCommandHandler("increment")
	void handleIncrement() {
		setCount(getCount() + 1);
	}

	@ReactCommandHandler("decrement")
	void handleDecrement() {
		setCount(getCount() - 1);
	}

}
