/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class of the controls of the chat elements that join a conversation's browser store - a
 * {@code <chat-conversation>}, a {@code <chat-rail>}, a {@code <chat-dock>}, a rail section.
 *
 * <p>
 * Every such control binds its window's conversation once through {@link ChatViewBinding}, writes
 * the bootstrap the store is built from, and reads the user's stored layout again on every render.
 * The store holds one stream of the conversation's frames for all elements of the conversation and
 * attaches it through whichever of them it can reach, so every one of these controls answers the
 * stream commands: {@link #CMD_ATTACH_STREAM}, {@link #CMD_DETACH_STREAM} and
 * {@link #CMD_REFRESH_RUN_STATE}.
 * </p>
 *
 * <p>
 * The stream belongs to the conversation, not to the control it was attached through: it lives
 * while any chat control of the conversation is displayed in the window (see
 * {@link ChatViewBinding#hidden(ReactControl)}), which is what the browser expects of it - the
 * first chat element to mount opens it, the last to leave closes it.
 * </p>
 */
public abstract class ChatControl extends ReactControl {

	/**
	 * Command that starts sending the conversation's frames to the window's event stream, as
	 * patches to the listener id {@code tl-chat-stream:<token>}.
	 *
	 * <p>
	 * Takes {@link #ARG_STREAM}; answered with {@link #FIELD_ATTACHED}. The first frames -
	 * {@code connected}, the approval mode and the {@code run_state} - are delivered with the
	 * command.
	 * </p>
	 */
	public static final String CMD_ATTACH_STREAM = "attachStream";

	/**
	 * Command that stops the frames a {@link #CMD_ATTACH_STREAM} with the same {@link #ARG_STREAM}
	 * started; a stream a later attach started is left alone.
	 */
	public static final String CMD_DETACH_STREAM = "detachStream";

	/**
	 * Command that sends the conversation's {@code run_state} frame again, on the stream: the
	 * subagents of the run with their costs and budget, its task list, the requests waiting for
	 * the user.
	 */
	public static final String CMD_REFRESH_RUN_STATE = "refreshRunState";

	/**
	 * Argument of {@link #CMD_ATTACH_STREAM} and {@link #CMD_DETACH_STREAM}: the browser's part of
	 * the listener id, new for every attach.
	 */
	public static final String ARG_STREAM = "stream";

	/** Field of the answer to {@link #CMD_ATTACH_STREAM}: whether the stream was started. */
	public static final String FIELD_ATTACHED = "attached";

	/** State key of the conversation the browser keys its store by. */
	public static final String STATE_CHAT_ID = "chatId";

	private final ChatViewBinding _binding;

	/**
	 * Creates a {@link ChatControl}.
	 *
	 * @param context
	 *        The view context of the element.
	 * @param reactModule
	 *        The client component rendering the element.
	 * @param chatId
	 *        The resolved {@code chat-id} of the conversation the element joins.
	 * @param element
	 *        The element's tag, e.g. {@code chat-rail}, for the join diagnostic.
	 */
	protected ChatControl(ReactContext context, String reactModule, String chatId, String element) {
		super(context, null, reactModule);
		_binding = ChatViewBinding.bind(context, chatId, element);
		_binding.writeSharedState(this::putState);
		// The browser keys its store by what is pushed here, so it must be the resolved id.
		putState(STATE_CHAT_ID, chatId);

		Runnable hidden = () -> _binding.hidden(this);
		addAttachListener(() -> _binding.shown(this));
		addDetachListener(hidden);
		addCleanupAction(hidden);
	}

	/** The window's binding of the conversation this element joins. */
	protected final ChatViewBinding binding() {
		return _binding;
	}

	/**
	 * Re-reads the user's stored layout.
	 *
	 * <p>
	 * A browser reload re-renders the control tree the window already holds, so this hook is the
	 * only place that runs again. Writing the layout in the constructor would replay the first
	 * render's layout for the life of the window (Gitea #423).
	 * </p>
	 */
	@Override
	protected void onBeforeWrite() {
		super.onBeforeWrite();
		_binding.writeRenderState(this::putState);
	}

	/**
	 * Starts sending the conversation's frames to this window, see {@link #CMD_ATTACH_STREAM}.
	 */
	@ReactCommandHandler(value = CMD_ATTACH_STREAM, technical = true, params = {
		@ReactParam(name = ARG_STREAM, type = "string",
			description = "The browser's part of the listener id the frames are addressed to.")
	})
	public HandlerResult attachStream(Map<String, Object> args) {
		boolean attached = _binding.attachStream(getReactContext(), stringArg(args, ARG_STREAM));
		return ReactCommandReply.create().put(FIELD_ATTACHED, Boolean.valueOf(attached)).toResult();
	}

	/**
	 * Stops the frames of one attach, see {@link #CMD_DETACH_STREAM}.
	 */
	@ReactCommandHandler(value = CMD_DETACH_STREAM, technical = true, params = {
		@ReactParam(name = ARG_STREAM, type = "string", description = "The listener id part the attach named.")
	})
	public void detachStream(Map<String, Object> args) {
		_binding.detachStream(stringArg(args, ARG_STREAM));
	}

	/**
	 * Sends the conversation's {@code run_state} frame again, see {@link #CMD_REFRESH_RUN_STATE}.
	 */
	@ReactCommandHandler(value = CMD_REFRESH_RUN_STATE, technical = true)
	public void refreshRunState() {
		_binding.sendRunState();
	}

	private static String stringArg(Map<String, Object> args, String key) {
		Object value = args == null ? null : args.get(key);
		return value instanceof String text ? text : null;
	}

	/** The given value, or the fallback for a value that is empty or only blanks. */
	protected static String blankToDefault(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? fallback : trimmed;
	}
}
