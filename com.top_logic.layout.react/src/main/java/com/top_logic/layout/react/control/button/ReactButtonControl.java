/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} that renders a button via the {@code TLButton} React component.
 *
 * <p>
 * When the button is clicked on the client, the {@code "click"} command is dispatched to the
 * server, which invokes the {@link ButtonAction} provided at construction time.
 * </p>
 *
 * <p>
 * When constructed with a {@link CommandModel}, the button automatically reads the label and
 * disabled state from the model, listens for state changes, and removes its listener during
 * cleanup.
 * </p>
 */
public class ReactButtonControl extends ReactControl {

	/** State key for the button label. */
	private static final String LABEL = "label";

	/** State key for the disabled flag. */
	private static final String DISABLED = "disabled";

	/** State key for the hidden flag. */
	private static final String HIDDEN = "hidden";

	/** State key for the icon name (renders as icon-only button when set). */
	private static final String ICON = "icon";

	private final ButtonAction _action;

	private CommandModel _model;

	private Runnable _modelChangeHandler;

	/**
	 * Creates a new {@link ReactButtonControl} with a simple action.
	 *
	 * @param label
	 *        The button label.
	 * @param action
	 *        The {@link ButtonAction} to execute when the button is clicked.
	 */
	public ReactButtonControl(ReactContext context, String label, ButtonAction action) {
		super(context, null, "TLButton");
		_action = action;
		putState(LABEL, label);
	}

	/**
	 * Creates a new {@link ReactButtonControl} backed by a {@link CommandModel}.
	 *
	 * <p>
	 * The button reads its label and disabled state from the model and automatically updates when
	 * the model's state changes. The state change listener is removed during
	 * {@link #onCleanup() cleanup}.
	 * </p>
	 *
	 * @param model
	 *        The command model providing label, executability, and execution.
	 */
	public ReactButtonControl(ReactContext context, CommandModel model) {
		super(context, null, "TLButton");
		_model = model;
		_action = model::executeCommand;
		_modelChangeHandler = this::handleModelChange;

		putState(LABEL, model.getLabel());
		putState(DISABLED, Boolean.valueOf(!model.isExecutable()));
		putState(HIDDEN, Boolean.valueOf(!model.isVisible()));
		model.addStateChangeListener(_modelChangeHandler);
	}

	/**
	 * Updates the button label.
	 *
	 * @param label
	 *        The new label text.
	 */
	public void setLabel(String label) {
		putState(LABEL, label);
	}

	/**
	 * Sets the disabled state of the button.
	 *
	 * @param disabled
	 *        Whether the button should be disabled.
	 */
	public void setDisabled(boolean disabled) {
		putState(DISABLED, disabled);
	}

	/**
	 * Sets the hidden state of the button.
	 *
	 * @param hidden
	 *        Whether the button should be hidden ({@code display: none}).
	 */
	public void setHidden(boolean hidden) {
		putState(HIDDEN, hidden);
	}

	/**
	 * Sets the icon name for this button.
	 *
	 * <p>
	 * When set, the button renders as an icon-only button using the named SVG icon on the client.
	 * The label is used as accessible title/tooltip.
	 * </p>
	 *
	 * @param icon
	 *        The icon name (e.g. "detail", "delete"), or {@code null} to remove the icon.
	 */
	public void setIcon(String icon) {
		putState(ICON, icon);
	}

	/**
	 * Handles the click command from the React client.
	 */
	@ReactCommand("click")
	HandlerResult handleClick(ReactContext context) {
		return _action.execute(context);
	}

	@Override
	protected void onCleanup() {
		if (_model != null) {
			_model.removeStateChangeListener(_modelChangeHandler);
		}
	}

	private void handleModelChange() {
		setLabel(_model.getLabel());
		setDisabled(!_model.isExecutable());
		setHidden(!_model.isVisible());
	}

}
