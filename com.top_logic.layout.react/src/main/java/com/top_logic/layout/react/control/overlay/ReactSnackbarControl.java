/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.SnackbarState;

/**
 * Transient notification message displayed at the bottom of the screen.
 *
 * <p>
 * Auto-dismisses after a configurable duration. Supports four {@link Variant variants} (info,
 * success, warning, error).
 * </p>
 *
 * <p>
 * One message is on screen at a time. A message arriving while one is shown waits until that one is
 * dismissed; messages are shown in arrival order, so a burst of notifications is read one after the
 * other instead of overwriting each other. Each message is shown with its own
 * {@link SnackbarState#GENERATION__PROP}, which restarts the client-side auto-dismiss timer and
 * identifies the message a {@link #DISMISS_COMMAND} refers to.
 * </p>
 */
public class ReactSnackbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLSnackbar";

	/** The {@link ReactCommandHandler} that dismisses a shown snackbar. */
	public static final String DISMISS_COMMAND = "dismiss";

	/**
	 * The visual variant of a snackbar notification.
	 */
	public enum Variant implements ExternallyNamed {

		/** Informational notification. */
		INFO("info"),

		/** Success notification. */
		SUCCESS("success"),

		/** Warning notification. */
		WARNING("warning"),

		/** Error notification. */
		ERROR("error");

		private final String _externalName;

		Variant(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * A message waiting for its turn on the snackbar.
	 *
	 * @param message
	 *        The plain-text message, see {@link SnackbarState#MESSAGE_TEXT__PROP}.
	 * @param content
	 *        The HTML content taking precedence over the message, or <code>null</code> to display
	 *        the message, see {@link SnackbarState#CONTENT__PROP}.
	 * @param variant
	 *        The visual variant to display the message with.
	 */
	private record Message(String message, String content, Variant variant) {
		// Pure value.
	}

	private Runnable _dismissHandler;

	private int _generation;

	private final Deque<Message> _pending = new ArrayDeque<>();

	/**
	 * Creates a snackbar control.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        The visual variant.
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(ReactContext context, String message, Variant variant, Runnable dismissHandler) {
		this(context, message, variant, 5000, dismissHandler);
	}

	/**
	 * Creates a snackbar control with custom duration.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        The visual variant.
	 * @param duration
	 *        Auto-dismiss delay in ms (0 = sticky).
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(ReactContext context, String message, Variant variant, int duration,
			Runnable dismissHandler) {
		super(context, null, REACT_MODULE);
		_dismissHandler = dismissHandler;
		setMessage(message);
		setVariant(variant);
		putState(SnackbarState.DURATION__PROP, duration);
		hide();
		putState(SnackbarState.GENERATION__PROP, 0);
	}

	/**
	 * Sets the notification message.
	 */
	public void setMessage(String message) {
		putState(SnackbarState.MESSAGE_TEXT__PROP, message);
	}

	/**
	 * Sets the visual variant.
	 */
	public void setVariant(Variant variant) {
		putState(SnackbarState.VARIANT__PROP, variant.getExternalName());
	}

	/**
	 * Shows the snackbar with the current message, or queues it behind the message on screen.
	 */
	public void show() {
		enqueue(new Message(currentMessage(), currentContent(), currentVariant()));
	}

	/**
	 * Shows the snackbar with a new message, or queues it behind the message on screen.
	 *
	 * @param message
	 *        The new notification message.
	 */
	public void show(String message) {
		enqueue(new Message(message, null, currentVariant()));
	}

	/**
	 * Shows the snackbar with HTML content and a variant, or queues it behind the message on
	 * screen.
	 *
	 * @param htmlContent
	 *        The HTML content to display.
	 * @param variant
	 *        The visual variant.
	 */
	public void showHtml(String htmlContent, Variant variant) {
		enqueue(new Message(currentMessage(), htmlContent, variant));
	}

	/**
	 * Displays the given message, or appends it to the queue while another one is on screen.
	 */
	private void enqueue(Message message) {
		if (isVisible()) {
			_pending.addLast(message);
		} else {
			display(message);
		}
	}

	/**
	 * Puts the given message on screen under a {@link SnackbarState#GENERATION__PROP} of its own.
	 */
	private void display(Message message) {
		_generation++;
		Object tx = beginUpdate();
		putState(SnackbarState.MESSAGE_TEXT__PROP, message.message());
		putState(SnackbarState.CONTENT__PROP, message.content());
		putState(SnackbarState.VARIANT__PROP, message.variant().getExternalName());
		setVisible(true);
		putState(SnackbarState.GENERATION__PROP, _generation);
		commitUpdate(tx);
	}

	private String currentMessage() {
		return (String) getState(SnackbarState.MESSAGE_TEXT__PROP);
	}

	private String currentContent() {
		return (String) getState(SnackbarState.CONTENT__PROP);
	}

	private Variant currentVariant() {
		String externalName = (String) getState(SnackbarState.VARIANT__PROP);
		for (Variant variant : Variant.values()) {
			if (variant.getExternalName().equals(externalName)) {
				return variant;
			}
		}
		return Variant.INFO;
	}

	private boolean isVisible() {
		return Boolean.TRUE.equals(getState(SnackbarState.VISIBLE__PROP));
	}

	private void setVisible(boolean visible) {
		putState(SnackbarState.VISIBLE__PROP, visible);
	}

	/**
	 * Hides the snackbar and drops the messages waiting for their turn.
	 *
	 * <p>
	 * Taking the snackbar off the screen ends the current series of notifications: a queued message
	 * only becomes visible when the message before it is dismissed, so a message kept in the queue
	 * of a hidden snackbar would wait for a dismiss that never arrives.
	 * </p>
	 */
	public void hide() {
		_pending.clear();
		setVisible(false);
	}

	/**
	 * Handles the dismiss command sent when the snackbar is dismissed (by timer or user).
	 *
	 * <p>
	 * The {@link DismissArguments#getGeneration() reported generation} prevents a stale dismiss
	 * (from a previous message that timed out) from hiding the message currently on screen.
	 * </p>
	 *
	 * <p>
	 * The message waiting next takes the place of the dismissed one right away, under a
	 * {@link SnackbarState#GENERATION__PROP} of its own. The dismiss handler runs only when no
	 * message is left: it is told that the snackbar has gone off screen, which is not the case while
	 * the queue still feeds it.
	 * </p>
	 */
	@ReactCommandHandler(value = DISMISS_COMMAND, technical = true)
	void handleDismiss(DismissArguments args) {
		Integer reported = args.getGeneration();
		int dismissGeneration = reported != null ? reported.intValue() : -1;
		if (dismissGeneration != _generation) {
			return;
		}
		Message next = _pending.pollFirst();
		if (next != null) {
			display(next);
			return;
		}
		hide();
		_dismissHandler.run();
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), SnackbarState.DURATION__PROP,
			SnackbarState.GENERATION__PROP, SnackbarState.VARIANT__PROP);
	}

	/**
	 * An {@link ErrorSink} that routes messages to this snackbar, mapping the severity to the
	 * corresponding {@link Variant}.
	 */
	public ErrorSink asErrorSink() {
		return new ErrorSink() {
			@Override
			public void showError(HTMLFragment content) {
				showHtml(renderToHtml(content), Variant.ERROR);
			}

			@Override
			public void showWarning(HTMLFragment content) {
				showHtml(renderToHtml(content), Variant.WARNING);
			}

			@Override
			public void showInfo(HTMLFragment content) {
				showHtml(renderToHtml(content), Variant.INFO);
			}
		};
	}

	private static String renderToHtml(HTMLFragment fragment) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		StringWriter buffer = new StringWriter();
		try {
			TagWriter out = new TagWriter(buffer);
			fragment.write(displayContext, out);
		} catch (IOException ex) {
			Logger.error("Failed to render snackbar message.", ex, ReactSnackbarControl.class);
		}
		return buffer.toString();
	}
}
