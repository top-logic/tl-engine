/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.basic.ThemeImage;
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

	/** State key for the ThemeImage encoded form (e.g. "css:fas fa-edit", "/icons/foo.png"). */
	private static final String IMAGE = "image";

	/** State key for an explicit button tooltip (plain text). */
	private static final String TOOLTIP = "tooltip";

	/** State key for the button display mode (see {@link ButtonDisplayMode}). */
	private static final String DISPLAY_MODE = "displayMode";

	/** State key for the {@link ButtonAppearance appearance}. */
	private static final String APPEARANCE = "appearance";

	/** State key for the {@link ButtonSize size}. */
	private static final String SIZE = "size";

	/** State key for a direct client-side navigation target (bypasses the server command). */
	private static final String NAVIGATE_URL = "navigateUrl";

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
		setLabel(label);
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
		this(context, model, "TLButton");
	}

	/**
	 * Creates a {@link ReactButtonControl} backed by a {@link CommandModel} but rendered by the given
	 * React component, for subclasses that need a specialized client component (e.g. an upload
	 * button).
	 *
	 * @param reactModule
	 *        The name of the React component to render this control.
	 */
	protected ReactButtonControl(ReactContext context, CommandModel model, String reactModule) {
		super(context, null, reactModule);
		_model = model;
		_action = model::executeCommand;
		_modelChangeHandler = this::handleModelChange;

		setLabel(model.getLabel());
		setDisabled(!model.isExecutable());
		setHidden(!model.isVisible());
		setImage(model.getImage());
		setTooltip(model.getTooltip());
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
	 * Sets the image for this button.
	 *
	 * @param image
	 *        The theme image, or {@code null} to remove.
	 */
	public void setImage(ThemeImage image) {
		putImageState(image);
	}

	/**
	 * Sets the button tooltip text. {@code null} or empty clears the tooltip.
	 */
	public void setTooltip(String tooltip) {
		putState(TOOLTIP, (tooltip == null || tooltip.isEmpty()) ? null : tooltip);
	}

	/**
	 * Sets the display mode.
	 *
	 * @param displayMode
	 *        The display mode, or {@code null} to fall back to the React component default.
	 */
	public void setDisplayMode(ButtonDisplayMode displayMode) {
		putState(DISPLAY_MODE, displayMode == null ? null : displayMode.getExternalName());
	}

	/**
	 * Sets the button {@link ButtonAppearance appearance} (e.g. {@link ButtonAppearance#LINK} to
	 * render the button as an inline text link).
	 */
	public void setAppearance(ButtonAppearance appearance) {
		putState(APPEARANCE,
			appearance == null || appearance == ButtonAppearance.DEFAULT ? null : appearance.getExternalName());
	}

	/**
	 * Sets the button {@link ButtonSize size}.
	 */
	public void setSize(ButtonSize size) {
		putState(SIZE, size == null || size == ButtonSize.DEFAULT ? null : size.getExternalName());
	}

	/**
	 * Makes the button navigate the browser directly to the given URL on click instead of dispatching
	 * a server command (e.g. an external SSO redirect). {@code null} clears it.
	 */
	public void setNavigateUrl(String url) {
		putState(NAVIGATE_URL, url);
	}

	private void putImageState(ThemeImage image) {
		if (image != null) {
			putState(IMAGE, image.resolve().toEncodedForm());
		} else {
			putState(IMAGE, null);
		}
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
		setTooltip(_model.getTooltip());
	}

}
