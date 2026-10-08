/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAction;
import com.top_logic.layout.react.control.overlay.DismissArguments;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.state.AlertState;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A highlighted message standing in the content of a page: an icon, an optional title, the message
 * text and buttons offering what to do about it, rendered by the {@code TLAlert} component.
 *
 * <p>
 * The {@link Variant} of the message selects its color and its icon; the icons are theme images
 * (see {@link Icons}), so a theme can replace them.
 * </p>
 *
 * <p>
 * A {@link #setClosable(boolean) closable} alert offers the user to dismiss it. Dismissing
 * {@link #hide() hides} the alert and runs the {@link #setDismissHandler(ButtonAction) dismiss
 * handler}. Each content the alert {@link #show() shows} is shown under an
 * {@link AlertState#GENERATION__PROP} of its own; a setter given the value the alert already shows
 * changes nothing, neither the state nor the generation; a {@link #DISMISS_COMMAND} reporting another
 * generation refers to a content that was replaced between rendering and the click, and is
 * ignored.
 * </p>
 */
public class ReactAlertControl extends ReactControl {

	private static final String REACT_MODULE = "TLAlert";

	/** The {@link ReactCommandHandler} dismissing a shown alert. */
	public static final String DISMISS_COMMAND = "dismiss";

	private ButtonAction _dismissHandler;

	private int _generation;

	/**
	 * Creates a hidden {@link ReactAlertControl} giving an information.
	 *
	 * <p>
	 * The alert becomes visible through {@link #show()}.
	 * </p>
	 */
	public ReactAlertControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		putState(AlertState.VARIANT__PROP, Variant.INFO.getExternalName());
		putState(AlertState.ICON__PROP, encoded(icon(Variant.INFO)));
		putState(AlertState.TITLE__PROP, null);
		putState(AlertState.MESSAGE_TEXT__PROP, "");
		putState(AlertState.CLOSABLE__PROP, Boolean.FALSE);
		putState(AlertState.ACTIONS__PROP, List.of());
		putState(AlertState.GENERATION__PROP, Integer.valueOf(_generation));
		setHidden(true);
	}

	/**
	 * The kind of the message.
	 */
	public Variant getVariant() {
		String externalName = (String) getState(AlertState.VARIANT__PROP);
		for (Variant variant : Variant.values()) {
			if (variant.getExternalName().equals(externalName)) {
				return variant;
			}
		}
		return Variant.INFO;
	}

	/**
	 * Sets the kind of the message, together with the icon of that kind.
	 *
	 * @param variant
	 *        The kind of the message, {@code null} for {@link Variant#INFO}.
	 */
	public void setVariant(Variant variant) {
		Variant effective = variant == null ? Variant.INFO : variant;
		if (effective == getVariant()) {
			return;
		}
		Object tx = beginUpdate();
		putState(AlertState.VARIANT__PROP, effective.getExternalName());
		putState(AlertState.ICON__PROP, encoded(icon(effective)));
		contentChanged();
		commitUpdate(tx);
	}

	/**
	 * The title of the message, or {@code null} for none.
	 */
	public String getTitle() {
		return (String) getState(AlertState.TITLE__PROP);
	}

	/**
	 * Sets the title of the message.
	 *
	 * @param title
	 *        The title, plain text, or {@code null} for none.
	 */
	public void setTitle(String title) {
		if (Objects.equals(title, getTitle())) {
			return;
		}
		Object tx = beginUpdate();
		putState(AlertState.TITLE__PROP, title);
		contentChanged();
		commitUpdate(tx);
	}

	/**
	 * The message text.
	 */
	public String getMessage() {
		return (String) getState(AlertState.MESSAGE_TEXT__PROP);
	}

	/**
	 * Sets the message text.
	 *
	 * @param message
	 *        The message, plain text.
	 */
	public void setMessage(String message) {
		String effective = message == null ? "" : message;
		if (effective.equals(getMessage())) {
			return;
		}
		Object tx = beginUpdate();
		putState(AlertState.MESSAGE_TEXT__PROP, effective);
		contentChanged();
		commitUpdate(tx);
	}

	/**
	 * Whether the user can dismiss the alert.
	 */
	public boolean isClosable() {
		return Boolean.TRUE.equals(getState(AlertState.CLOSABLE__PROP));
	}

	/**
	 * Sets whether the user can dismiss the alert.
	 */
	public void setClosable(boolean closable) {
		putState(AlertState.CLOSABLE__PROP, Boolean.valueOf(closable));
	}

	/**
	 * Sets the buttons offering what to do about the message.
	 *
	 * <p>
	 * The alert owns the given controls: replacing them disposes the ones shown before, and
	 * disposing the alert disposes them.
	 * </p>
	 *
	 * @param actions
	 *        The controls to show, in display order.
	 */
	public void setActions(List<? extends ReactControl> actions) {
		List<ReactControl> previous = actions();
		List<ReactControl> current = List.copyOf(actions);
		putState(AlertState.ACTIONS__PROP, current);
		for (ReactControl old : previous) {
			if (!current.contains(old)) {
				old.cleanupTree();
			}
		}
	}

	/**
	 * The buttons offering what to do about the message.
	 *
	 * @see #setActions(List)
	 */
	@SuppressWarnings("unchecked")
	public List<ReactControl> actions() {
		return (List<ReactControl>) getState(AlertState.ACTIONS__PROP);
	}

	/**
	 * Sets what happens after the user dismissed the alert.
	 *
	 * @param handler
	 *        Runs after the dismissed alert is hidden, {@code null} for nothing.
	 */
	public void setDismissHandler(ButtonAction handler) {
		_dismissHandler = handler;
	}

	/**
	 * Shows the alert with the given kind, title and message.
	 *
	 * <p>
	 * All properties are sent as a single patch under a new {@link AlertState#GENERATION__PROP},
	 * so the client never renders the old title with the new message.
	 * </p>
	 *
	 * @param variant
	 *        The kind of the message, see {@link #setVariant(Variant)}.
	 * @param title
	 *        The title, see {@link #setTitle(String)}.
	 * @param message
	 *        The message, see {@link #setMessage(String)}.
	 */
	public void show(Variant variant, String title, String message) {
		Object tx = beginUpdate();
		setVariant(variant);
		setTitle(title);
		setMessage(message);
		show();
		commitUpdate(tx);
	}

	/**
	 * Shows the alert with its current content.
	 *
	 * <p>
	 * An alert that is hidden is shown under a new {@link AlertState#GENERATION__PROP}; showing an
	 * alert that is already visible changes nothing.
	 * </p>
	 */
	public void show() {
		if (!isHidden()) {
			return;
		}
		Object tx = beginUpdate();
		setHidden(false);
		nextGeneration();
		commitUpdate(tx);
	}

	/**
	 * Hides the alert.
	 */
	public void hide() {
		setHidden(true);
	}

	/**
	 * The {@link AlertState#GENERATION__PROP} of the content shown.
	 */
	public int getGeneration() {
		return _generation;
	}

	/**
	 * Dismisses the alert on behalf of the user: {@link #hide() hides} it and runs the
	 * {@link #setDismissHandler(ButtonAction) dismiss handler}.
	 *
	 * <p>
	 * The command is refused for an alert that does not offer it - one that is hidden or not
	 * {@link #setClosable(boolean) closable} - and ignored when its
	 * {@link DismissArguments#getGeneration() generation} names a content that is no longer shown.
	 * </p>
	 */
	@ReactCommandHandler(DISMISS_COMMAND)
	HandlerResult handleDismiss(ReactContext context, DismissArguments args) {
		if (isHidden() || !isClosable()) {
			return HandlerResult.DEFAULT_RESULT;
		}
		Integer reported = args.getGeneration();
		if (reported == null || reported.intValue() != _generation) {
			return HandlerResult.DEFAULT_RESULT;
		}
		hide();
		if (_dismissHandler == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return _dismissHandler.execute(context);
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), AlertState.ICON__PROP,
			AlertState.GENERATION__PROP);
	}

	/**
	 * Accounts for a change of the content shown: a visible alert shows the changed content under a
	 * new {@link AlertState#GENERATION__PROP}.
	 */
	private void contentChanged() {
		if (!isHidden()) {
			nextGeneration();
		}
	}

	private void nextGeneration() {
		_generation++;
		putState(AlertState.GENERATION__PROP, Integer.valueOf(_generation));
	}

	/**
	 * The icon of the given kind of message.
	 */
	private static ThemeImage icon(Variant variant) {
		switch (variant) {
			case SUCCESS:
				return Icons.ALERT_SUCCESS;
			case WARNING:
				return Icons.ALERT_WARNING;
			case ERROR:
				return Icons.ALERT_ERROR;
			case INFO:
			default:
				return Icons.ALERT_INFO;
		}
	}

	private String encoded(ThemeImage image) {
		return ReactImages.encode(getReactContext(), image);
	}
}
