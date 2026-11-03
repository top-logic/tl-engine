/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.top_logic.ai.chat.api.ChatTranscripts;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.RunStateFrame;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContext;

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
 *
 * <p>
 * For the same reason every one of them answers the commands that open and delete a chat of the
 * account ({@link #CMD_SELECT_SESSION}, {@link #CMD_DELETE_SESSION}) and serves the transcripts the
 * browser reads ({@link #getDownloadData(String)}): a chat's transcript is read from the
 * {@code /react-api/data} endpoint of the control, under a key {@link #DATA_SESSION} or
 * {@link #DATA_SUBAGENT}. Every one of them is checked against the acting account: a chat of
 * another account and an id that names nothing are answered alike.
 * </p>
 */
public abstract class ChatControl extends ReactControl implements DataProvider {

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

	/**
	 * Command that opens one chat of the account in this window's conversation: the conversation's
	 * agent runs, approval mode, ratings and effort are that chat's from then on.
	 *
	 * <p>
	 * Takes {@link #ARG_SESSION_ID}; answered with {@link #FIELD_OPENED} and, for a chat that was
	 * opened, with the chat's run state in {@link #FIELD_RUN_STATE}. The transcript is read
	 * afterwards, under {@link #DATA_SESSION}.
	 * </p>
	 */
	public static final String CMD_SELECT_SESSION = "selectSession";

	/**
	 * Command that deletes one chat of the account, with everything recorded in it.
	 *
	 * <p>
	 * Takes {@link #ARG_SESSION_ID}; answered with {@link #FIELD_DELETED}. The chat lists of the
	 * account's windows follow the deletion by themselves, see {@link SessionListWatch}.
	 * </p>
	 */
	public static final String CMD_DELETE_SESSION = "deleteSession";

	/** Argument of {@link #CMD_SELECT_SESSION} and {@link #CMD_DELETE_SESSION}: the chat's id. */
	public static final String ARG_SESSION_ID = "sessionId";

	/** Field of the answer to {@link #CMD_SELECT_SESSION}: whether the chat was opened. */
	public static final String FIELD_OPENED = "opened";

	/**
	 * Field of the answer to {@link #CMD_SELECT_SESSION}: the opened chat's run state, as the
	 * {@code run_state} frame of the stream carries it.
	 *
	 * <p>
	 * In the answer rather than on the stream, because the browser applies it once the transcript
	 * it belongs to is on screen, which is after the command has been answered.
	 * </p>
	 */
	public static final String FIELD_RUN_STATE = "runState";

	/** Field of the answer to {@link #CMD_DELETE_SESSION}: whether the chat was deleted. */
	public static final String FIELD_DELETED = "deleted";

	/**
	 * First part of the data key of a chat's transcript: {@code session/<chat id>}.
	 *
	 * @see #getDownloadData(String)
	 * @see ChatTranscripts#sessionDetail(TaskProgressTracker, Person, String)
	 */
	public static final String DATA_SESSION = "session";

	/**
	 * First part of the data key of a subagent's transcript: {@code subagent/<subagent id>}, or
	 * {@code subagent/<subagent id>/<run id>} for a subagent whose card names its recorded run.
	 *
	 * @see #getDownloadData(String)
	 * @see ChatTranscripts#subagentTranscript(TaskProgressTracker, Person, String, String)
	 */
	public static final String DATA_SUBAGENT = "subagent";

	/**
	 * Separator of the parts of a data key; each part after the first is URL-encoded.
	 */
	public static final String DATA_KEY_SEPARATOR = "/";

	/** State key of the conversation the browser keys its store by. */
	public static final String STATE_CHAT_ID = "chatId";

	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

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

	/**
	 * Opens a chat of the account in this window's conversation, see {@link #CMD_SELECT_SESSION}.
	 */
	@ReactCommandHandler(value = CMD_SELECT_SESSION, params = {
		@ReactParam(name = ARG_SESSION_ID, type = "string", description = "The id of the chat to open.")
	})
	public HandlerResult selectSession(Map<String, Object> args) {
		Person person = currentPerson();
		String id = stringArg(args, ARG_SESSION_ID);
		ReactCommandReply reply = ReactCommandReply.create();
		if (ChatSessionIds.ownedSession(id, person) == null) {
			return reply.put(FIELD_OPENED, Boolean.FALSE).toResult();
		}
		TaskProgressTracker tracker = _binding.tracker();
		boolean opened = tracker.getSessionManager().switchSession(id, person);
		reply.put(FIELD_OPENED, Boolean.valueOf(opened));
		if (opened) {
			reply.put(FIELD_RUN_STATE, RunStateFrame.of(tracker, person));
		}
		return reply.toResult();
	}

	/**
	 * Deletes a chat of the account, see {@link #CMD_DELETE_SESSION}.
	 */
	@ReactCommandHandler(value = CMD_DELETE_SESSION, params = {
		@ReactParam(name = ARG_SESSION_ID, type = "string", description = "The id of the chat to delete.")
	})
	public HandlerResult deleteSession(Map<String, Object> args) {
		TLObject session = ChatSessionIds.ownedSession(stringArg(args, ARG_SESSION_ID), currentPerson());
		if (session != null) {
			ChatHistoryService.deleteSession(session);
		}
		return ReactCommandReply.create().put(FIELD_DELETED, Boolean.valueOf(session != null)).toResult();
	}

	/**
	 * The transcript a data key names, as JSON ({@link ChatSessionDetailResponse}), or
	 * <code>null</code> - answered as not found - for a key that names nothing of the acting
	 * account.
	 *
	 * <p>
	 * A key is {@link #DATA_SESSION} or {@link #DATA_SUBAGENT}, followed by the ids it names, each
	 * after a {@link #DATA_KEY_SEPARATOR}.
	 * </p>
	 */
	@Override
	public BinaryData getDownloadData(String key) {
		if (key == null) {
			return null;
		}
		String[] parts = key.split(DATA_KEY_SEPARATOR, -1);
		advanceSessionRevision();
		Person person = currentPerson();
		TaskProgressTracker tracker = _binding.tracker();
		ChatSessionDetailResponse data;
		try {
			if (DATA_SESSION.equals(parts[0]) && parts.length == 2) {
				data = ChatTranscripts.sessionDetail(tracker, person, decode(parts[1]));
			} else if (DATA_SUBAGENT.equals(parts[0]) && (parts.length == 2 || parts.length == 3)) {
				data = ChatTranscripts.subagentTranscript(tracker, person, decode(parts[1]),
					parts.length == 3 ? decode(parts[2]) : null);
			} else {
				data = null;
			}
		} catch (IOException ex) {
			Logger.error("Failed to read the transcript '" + key + "'.", ex, ChatControl.class);
			return null;
		}
		if (data == null) {
			return null;
		}
		return BinaryDataFactory.createBinaryData(DtoMapper.toJson(data).getBytes(StandardCharsets.UTF_8),
			CONTENT_TYPE_JSON);
	}

	/**
	 * Advances the window's view of the knowledge base to the latest commit before a transcript is
	 * read: a run records its transcript on the writer thread of the {@code AgentExecutionStore}, and
	 * the window's stable view would otherwise not include what it recorded since the window's last
	 * command.
	 *
	 * <p>
	 * The data request is a sanctioned point for that: it runs no command, opens no transaction and
	 * renders nothing.
	 * </p>
	 */
	private static void advanceSessionRevision() {
		if (ThreadContextManager.getInteraction() == null) {
			return;
		}
		try {
			HistoryUtils.updateSessionRevision();
		} catch (RuntimeException ex) {
			Logger.info("Could not advance session revision: " + ex.getMessage(), ChatControl.class);
		}
	}

	private static String decode(String part) {
		return URLDecoder.decode(part, StandardCharsets.UTF_8);
	}

	/** The acting account. */
	protected static Person currentPerson() {
		TLContext context = TLContext.getContext();
		return context == null ? null : context.getPerson();
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
