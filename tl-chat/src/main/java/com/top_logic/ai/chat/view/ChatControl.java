/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.api.AccountRuns;
import com.top_logic.ai.chat.api.ApprovalChoice;
import com.top_logic.ai.chat.api.ChatPreferencesStore;
import com.top_logic.ai.chat.api.ChatSSEHandler;
import com.top_logic.ai.chat.api.ChatTranscripts;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.RunStateFrame;
import com.top_logic.ai.chat.api.SendRefusal;
import com.top_logic.ai.chat.api.messages.ChatPreferencesDto;
import com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse;
import com.top_logic.ai.chat.api.messages.DraftAttachmentDto;
import com.top_logic.ai.chat.api.messages.PromptImprovedEvent;
import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatState;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor;
import com.top_logic.ai.chat.layout.PromptImprovementProcessor.PromptImprovementException;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.limits.RunLimits;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

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
 * For the same reason every one of them answers the commands the browser sends about the
 * conversation: opening and deleting a chat of the account ({@link #CMD_SELECT_SESSION},
 * {@link #CMD_DELETE_SESSION}), sending ({@link #CMD_SEND}, {@link #CMD_EDIT_SEND},
 * {@link #CMD_REGENERATE}), answering and stopping runs and subagents ({@link #CMD_RESPOND},
 * {@link #CMD_CANCEL}, {@link #CMD_MESSAGE_SUBAGENT}, {@link #CMD_CANCEL_SUBAGENT}), the
 * auto-approve switch ({@link #CMD_SET_APPROVAL_MODE}), ratings ({@link #CMD_RATE}), the prompt
 * improver ({@link #CMD_IMPROVE_PROMPT}) and the layout ({@link #CMD_SAVE_LAYOUT}); and it serves
 * the transcripts the
 * browser reads ({@link #getDownloadData(String)}): a chat's transcript is read from the
 * {@code /react-api/data} endpoint of the control, under a key {@link #DATA_SESSION} or
 * {@link #DATA_SUBAGENT}. Every one of them is checked against the acting account: a chat of
 * another account and an id that names nothing are answered alike.
 * </p>
 */
public abstract class ChatControl extends ReactControl implements DataProvider, UploadHandler {

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

	/**
	 * Command that sends a message in the conversation: into the chat {@link #ARG_SESSION_ID} names,
	 * or into a chat the send creates when it names none.
	 *
	 * <p>
	 * Takes {@link #ARG_TEXT} and the turn settings {@link #ARG_AGENT}, {@link #ARG_MODEL},
	 * {@link #ARG_MCP_OVERRIDE}, {@link #ARG_MCP_SERVERS}, {@link #ARG_EFFORT} and
	 * {@link #ARG_APPROVAL_MODE}; a send that creates its chat titles it {@link #ARG_TITLE}. The
	 * message carries the files attached to it ({@link #handleUpload(DisplayContext, Collection)})
	 * and is told what the page states about the situation ({@link ChatViewBinding#facts()}).
	 * </p>
	 *
	 * <p>
	 * Answered with {@link #FIELD_STATUS}, {@link #FIELD_REASON} for a refusal, and for a message that
	 * went with the chat it went to ({@link #FIELD_SESSION_ID}) and what that chat supports
	 * ({@link #FIELD_FEATURES}). A chat the send created and could not send into is deleted again.
	 * </p>
	 */
	public static final String CMD_SEND = "send";

	/**
	 * Command that drops a turn of a chat and everything after it, and sends the edited message in
	 * its place - in one command, so that no other command of the window can come between the two.
	 *
	 * <p>
	 * Takes {@link #ARG_SESSION_ID}, {@link #ARG_FROM_TURN} and the arguments of {@link #CMD_SEND}.
	 * Nothing is dropped from a send that is refused. Answered like {@link #CMD_SEND}, plus
	 * {@link #FIELD_REMOVED_TURNS}.
	 * </p>
	 */
	public static final String CMD_EDIT_SEND = "editSend";

	/**
	 * Command that answers the last message of a chat again: the last turn is dropped and its message
	 * sent once more.
	 *
	 * <p>
	 * Takes {@link #ARG_SESSION_ID} and the turn settings of {@link #CMD_SEND}. Answered with
	 * {@link #FIELD_STATUS}, {@link #FIELD_REASON}, {@link #FIELD_REMOVED_TURNS} and the message sent
	 * again ({@link #FIELD_TEXT}). A regenerate that is refused drops nothing.
	 * </p>
	 */
	public static final String CMD_REGENERATE = "regenerate";

	/**
	 * Command that takes one file off the message being written; takes {@link #ARG_ATTACHMENT_ID}.
	 */
	public static final String CMD_REMOVE_ATTACHMENT = "removeAttachment";

	/** Argument of {@link #CMD_SEND}: the message. */
	public static final String ARG_TEXT = "text";

	/** Argument of {@link #CMD_SEND}: the title of a chat the send creates. */
	public static final String ARG_TITLE = "title";

	/** Argument of {@link #CMD_SEND}: the agent of the turn; empty runs the account's entry point. */
	public static final String ARG_AGENT = "agent";

	/** Argument of {@link #CMD_SEND}: the model of the turn; empty keeps the agent's. */
	public static final String ARG_MODEL = "model";

	/**
	 * Argument of {@link #CMD_SEND}: whether {@link #ARG_MCP_SERVERS} replace the agent's configured
	 * MCP servers for the turn.
	 */
	public static final String ARG_MCP_OVERRIDE = "mcpOverride";

	/** Argument of {@link #CMD_SEND}: the MCP servers of the turn, see {@link #ARG_MCP_OVERRIDE}. */
	public static final String ARG_MCP_SERVERS = "mcpServers";

	/** Argument of {@link #CMD_SEND}: how hard the model may think; empty keeps the agent's. */
	public static final String ARG_EFFORT = "effort";

	/**
	 * Argument of {@link #CMD_SEND}: the position of the auto-approve switch at the send, an
	 * {@link ApprovalMode} name; the turn runs with it. Empty keeps the chat's stored mode.
	 */
	public static final String ARG_APPROVAL_MODE = "approvalMode";

	/** Argument of {@link #CMD_EDIT_SEND}: the zero-based index of the first turn to drop. */
	public static final String ARG_FROM_TURN = "fromTurn";

	/** Argument of {@link #CMD_REMOVE_ATTACHMENT}: the id of the file. */
	public static final String ARG_ATTACHMENT_ID = "attachmentId";

	/**
	 * Field of the answer to {@link #CMD_SEND}, {@link #CMD_EDIT_SEND} and {@link #CMD_REGENERATE}:
	 * what became of the request, one of {@link #STATUS_PROCESSING}, {@link #STATUS_BUSY},
	 * {@link #STATUS_FORBIDDEN}, {@link #STATUS_REJECTED}, {@link #STATUS_NOT_FOUND} and
	 * {@link #STATUS_ERROR}.
	 */
	public static final String FIELD_STATUS = "status";

	/**
	 * Field of the answer: why the request was refused, a sentence in the user's language; absent
	 * unless it was. In the answer to an upload: why files were not attached, one sentence each.
	 */
	public static final String FIELD_REASON = "reason";

	/** Field of the answer to {@link #CMD_SEND}: the chat the message went to. */
	public static final String FIELD_SESSION_ID = "sessionId";

	/** Field of the answer to {@link #CMD_SEND}: what the chat the message went to supports. */
	public static final String FIELD_FEATURES = "features";

	/** Field of the answer to {@link #CMD_EDIT_SEND} and {@link #CMD_REGENERATE}: how many turns went. */
	public static final String FIELD_REMOVED_TURNS = "removedTurns";

	/** Field of the answer to {@link #CMD_REGENERATE}: the message sent again. */
	public static final String FIELD_TEXT = "text";

	/** {@link #FIELD_STATUS}: the message went, a run answers it. */
	public static final String STATUS_PROCESSING = "processing";

	/** {@link #FIELD_STATUS}: the chat is answering something else. */
	public static final String STATUS_BUSY = "busy";

	/**
	 * {@link #FIELD_STATUS}: the acting account may not make this request, or the chat's answerer
	 * does not support it; {@link #FIELD_REASON} says which.
	 */
	public static final String STATUS_FORBIDDEN = "forbidden";

	/**
	 * {@link #FIELD_STATUS}: this installation has no room for the run right now;
	 * {@link #FIELD_REASON} says so. Distinct from {@link #STATUS_BUSY}, which is about this chat,
	 * and from {@link #STATUS_ERROR}: the user may try again in a moment.
	 */
	public static final String STATUS_REJECTED = "rejected";

	/**
	 * {@link #FIELD_STATUS}: the request names something there is none of - a chat of another
	 * account answered exactly like one that does not exist, or an agent of a name no agent has.
	 */
	public static final String STATUS_NOT_FOUND = "notFound";

	/** {@link #FIELD_STATUS}: nothing was sent - an empty message, nothing to answer again. */
	public static final String STATUS_ERROR = "error";

	/**
	 * State key of the files attached to the message being written, a list of
	 * {@link DraftAttachmentDto}.
	 */
	public static final String STATE_DRAFT_ATTACHMENTS = "draftAttachments";

	/**
	 * Command that answers an approval or a question a run of the account waits for, whichever of
	 * the account's windows started the run (#393).
	 *
	 * <p>
	 * Takes {@link #ARG_REQUEST_ID}, {@link #ARG_RESPONSE}, and for an approval {@link #ARG_TOOL_NAME}
	 * and {@link #ARG_COMMENT}, for a choice question {@link #ARG_OWN_WORDS}. Answered with
	 * {@link #FIELD_STATUS}: {@link #STATUS_OK}, or {@link #STATUS_IGNORED} when no run of the
	 * account waits for the request - which a request of another account is answered with too.
	 * </p>
	 *
	 * @see AccountRuns#respond(Person, String, String, String, String, String)
	 */
	public static final String CMD_RESPOND = "respond";

	/**
	 * Command that stops the run of a chat of the account, whichever of its windows started it
	 * (#465).
	 *
	 * <p>
	 * Takes {@link #ARG_SESSION_ID}; without one, stops what this conversation runs. Answered with
	 * {@link #FIELD_CANCELLED}.
	 * </p>
	 *
	 * @see AccountRuns#cancel(TaskProgressTracker, Person, String)
	 */
	public static final String CMD_CANCEL = "cancel";

	/**
	 * Command that turns "auto-approve" on or off: for the chat {@link #ARG_SESSION_ID} names, and as
	 * the person's last choice, which a new chat starts with (#658, #948).
	 *
	 * <p>
	 * Takes {@link #ARG_MODE}, an {@link ApprovalMode} name. A command without a chat comes from a
	 * conversation that has no chat yet: it only stores the person's choice, and the send that
	 * creates the chat names the mode ({@link #ARG_APPROVAL_MODE}). A run of the chat that is working
	 * takes the new mode with its next approval, whichever window started it (#943). Answered with
	 * {@link #FIELD_STATUS}: {@link #STATUS_OK}, {@link #STATUS_NOT_FOUND} for a chat of another
	 * account, {@link #STATUS_ERROR} for a mode there is none of.
	 * </p>
	 */
	public static final String CMD_SET_APPROVAL_MODE = "setApprovalMode";

	/**
	 * Command that records what the user thought of one answer of a chat, in the chat's metadata.
	 *
	 * <p>
	 * Takes {@link #ARG_SESSION_ID}, {@link #ARG_TURN_INDEX}, {@link #ARG_RATING} and
	 * {@link #ARG_NOTE}; the chat the conversation shows stays shown. Answered with
	 * {@link #FIELD_STATUS}: {@link #STATUS_OK}, or {@link #STATUS_NOT_FOUND} for a chat of another
	 * account.
	 * </p>
	 */
	public static final String CMD_RATE = "rate";

	/**
	 * Command that starts improving the draft in the composer, with the prompt improver configured
	 * for the conversation.
	 *
	 * <p>
	 * Takes {@link #ARG_DRAFT} and {@link #ARG_REQUEST_ID}, and is answered at once - the improvement
	 * is a call to a model and runs on a chat worker, so the window's other commands are not held up
	 * behind it. Its result arrives as a {@link ChatSSEHandler#PROMPT_IMPROVED} frame on the
	 * conversation's stream, naming the request id. Answered with {@link #FIELD_STATUS}:
	 * {@link #STATUS_STARTED}; {@link #STATUS_ERROR} for a request or configuration that cannot be
	 * run; {@link #STATUS_REJECTED} with {@link #FIELD_REASON} when there is no room for the run.
	 * </p>
	 */
	public static final String CMD_IMPROVE_PROMPT = "improvePrompt";

	/**
	 * Command that cancels the improvement {@link #ARG_REQUEST_ID} names; its frame says
	 * {@code cancelled}. Answered with {@link #FIELD_CANCELLED}.
	 */
	public static final String CMD_CANCEL_PROMPT_IMPROVEMENT = "cancelPromptImprovement";

	/**
	 * Command that writes to one background subagent of a chat of the account, from its card.
	 *
	 * <p>
	 * Takes {@link #ARG_SUBAGENT_ID}, {@link #ARG_SESSION_ID}, {@link #ARG_PARENT_TOOL_CALL_ID} and
	 * {@link #ARG_TEXT}. Answered with {@link #FIELD_STATUS} - one of the
	 * {@link AccountRuns.SubagentMessageStatus} codes - and {@link #FIELD_MESSAGE_ID}.
	 * </p>
	 *
	 * @see AccountRuns#messageSubagent(TaskProgressTracker, Person, String, String, String, String)
	 */
	public static final String CMD_MESSAGE_SUBAGENT = "messageSubagent";

	/**
	 * Command that stops one background subagent of a chat of the account; takes the arguments of
	 * {@link #CMD_MESSAGE_SUBAGENT} but the text, answered with {@link #FIELD_CANCELLED}.
	 */
	public static final String CMD_CANCEL_SUBAGENT = "cancelSubagent";

	/**
	 * Technical command that stores the user's layout of one chat - rail, pane width, sections - in
	 * the personal configuration ({@link ChatPreferencesStore}).
	 *
	 * <p>
	 * Takes {@link #ARG_LAYOUT_KEY}, the key the layout is stored under - one layout per
	 * {@code chat-id} - and {@link #ARG_LAYOUT}, a {@link ChatPreferencesDto} holding what the user
	 * decided: a fact they never touched is absent.
	 * </p>
	 */
	public static final String CMD_SAVE_LAYOUT = "saveLayout";

	/** Argument of {@link #CMD_RESPOND} and the prompt improvement: the id of the request. */
	public static final String ARG_REQUEST_ID = "requestId";

	/** Argument of {@link #CMD_RESPOND}: the decision or the answer. */
	public static final String ARG_RESPONSE = "response";

	/** Argument of {@link #CMD_RESPOND}: the tool an approval is about. */
	public static final String ARG_TOOL_NAME = "toolName";

	/** Argument of {@link #CMD_RESPOND}: why a tool call was denied. */
	public static final String ARG_COMMENT = "comment";

	/** Argument of {@link #CMD_RESPOND}: what the user typed for a choice question. */
	public static final String ARG_OWN_WORDS = "ownWords";

	/** Argument of {@link #CMD_SET_APPROVAL_MODE}: an {@link ApprovalMode} name. */
	public static final String ARG_MODE = "mode";

	/** Argument of {@link #CMD_RATE}: the zero-based index of the turn whose answer is rated. */
	public static final String ARG_TURN_INDEX = "turnIndex";

	/** Argument of {@link #CMD_RATE}: {@code up}, {@code down}, or empty to withdraw a rating. */
	public static final String ARG_RATING = "rating";

	/** Argument of {@link #CMD_RATE}: free text the user added. */
	public static final String ARG_NOTE = "note";

	/** Argument of {@link #CMD_IMPROVE_PROMPT}: the draft to improve. */
	public static final String ARG_DRAFT = "draft";

	/** Argument of {@link #CMD_MESSAGE_SUBAGENT} and {@link #CMD_CANCEL_SUBAGENT}: the subagent's id. */
	public static final String ARG_SUBAGENT_ID = "subagentId";

	/**
	 * Argument of {@link #CMD_MESSAGE_SUBAGENT} and {@link #CMD_CANCEL_SUBAGENT}: the lead's tool call
	 * that started the subagent, which tells it from a namesake.
	 */
	public static final String ARG_PARENT_TOOL_CALL_ID = "parentToolCallId";

	/** Argument of {@link #CMD_SAVE_LAYOUT}: the key the layout is stored under. */
	public static final String ARG_LAYOUT_KEY = "layoutKey";

	/** Argument of {@link #CMD_SAVE_LAYOUT}: the layout, as a {@link ChatPreferencesDto}. */
	public static final String ARG_LAYOUT = "layout";

	/**
	 * Field of the answer to {@link #CMD_CANCEL}, {@link #CMD_CANCEL_PROMPT_IMPROVEMENT} and
	 * {@link #CMD_CANCEL_SUBAGENT}: whether something was stopped.
	 */
	public static final String FIELD_CANCELLED = "cancelled";

	/** Field of the answer to {@link #CMD_MESSAGE_SUBAGENT}: the id of the message sent. */
	public static final String FIELD_MESSAGE_ID = "messageId";

	/** {@link #FIELD_STATUS}: done. */
	public static final String STATUS_OK = "ok";

	/** {@link #FIELD_STATUS} of {@link #CMD_RESPOND}: no run of the account waits for the request. */
	public static final String STATUS_IGNORED = "ignored";

	/** {@link #FIELD_STATUS} of {@link #CMD_IMPROVE_PROMPT}: the improvement runs. */
	public static final String STATUS_STARTED = "started";

	/** The title of a chat a send creates when the browser names none. */
	private static final String DEFAULT_TITLE = "New Chat";

	private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

	/** The name of an uploaded file the browser named none for. */
	private static final String UPLOAD_FILE_NAME = "upload.bin";

	/** The name a message goes under when the account has none. */
	private static final String ANONYMOUS_NAME = "Anonymous";

	private final ChatViewBinding _binding;

	/** Writes the attached files into this control's state; listens while the control is displayed. */
	private final Runnable _publishAttachments = this::publishAttachments;

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

		// The browser refuses a larger file before it is transmitted.
		putState(UploadSupport.MAX_UPLOAD_SIZE, Long.valueOf(maxUploadSize()));
		publishAttachments();
		ChatAttachments attachments = _binding.attachments();
		Runnable unlisten = () -> attachments.removeListener(_publishAttachments);
		addAttachListener(() -> {
			attachments.addListener(_publishAttachments);
			publishAttachments();
		});
		addDetachListener(unlisten);
		addCleanupAction(unlisten);
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
		publishAttachments();
	}

	private void publishAttachments() {
		putState(STATE_DRAFT_ATTACHMENTS, _binding.attachments().list());
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
	 * Sends a message, see {@link #CMD_SEND}.
	 */
	@ReactCommandHandler(value = CMD_SEND, params = {
		@ReactParam(name = ARG_TEXT, type = "string", description = "The message."),
		@ReactParam(name = ARG_SESSION_ID, type = "string",
			description = "The chat to send into; empty creates a chat."),
		@ReactParam(name = ARG_TITLE, type = "string", description = "The title of a chat the send creates."),
		@ReactParam(name = ARG_AGENT, type = "string", description = "The agent of the turn."),
		@ReactParam(name = ARG_MODEL, type = "string", description = "The model of the turn."),
		@ReactParam(name = ARG_MCP_OVERRIDE, type = "boolean",
			description = "Whether the named MCP servers replace the agent's own."),
		@ReactParam(name = ARG_MCP_SERVERS, type = "array", description = "The MCP servers of the turn."),
		@ReactParam(name = ARG_EFFORT, type = "string", description = "How hard the model may think."),
		@ReactParam(name = ARG_APPROVAL_MODE, type = "string",
			description = "The position of the auto-approve switch at the send.")
	})
	public HandlerResult send(Map<String, Object> args) {
		return sendTurn(args, null).toResult();
	}

	/**
	 * Drops a turn and sends the edited message in its place, see {@link #CMD_EDIT_SEND}.
	 */
	@ReactCommandHandler(value = CMD_EDIT_SEND, params = {
		@ReactParam(name = ARG_SESSION_ID, type = "string", description = "The chat whose turn is edited."),
		@ReactParam(name = ARG_FROM_TURN, type = "number", description = "The first turn to drop."),
		@ReactParam(name = ARG_TEXT, type = "string", description = "The edited message."),
		@ReactParam(name = ARG_AGENT, type = "string", description = "The agent of the turn."),
		@ReactParam(name = ARG_MODEL, type = "string", description = "The model of the turn."),
		@ReactParam(name = ARG_MCP_OVERRIDE, type = "boolean",
			description = "Whether the named MCP servers replace the agent's own."),
		@ReactParam(name = ARG_MCP_SERVERS, type = "array", description = "The MCP servers of the turn."),
		@ReactParam(name = ARG_EFFORT, type = "string", description = "How hard the model may think."),
		@ReactParam(name = ARG_APPROVAL_MODE, type = "string",
			description = "The position of the auto-approve switch at the send.")
	})
	public HandlerResult editSend(Map<String, Object> args) {
		Person person = actingPerson();
		if (person == null) {
			return status(STATUS_FORBIDDEN).toResult();
		}
		String sessionId = blankToNull(stringArg(args, ARG_SESSION_ID));
		Number fromTurn = args == null ? null : (args.get(ARG_FROM_TURN) instanceof Number n ? n : null);
		if (sessionId == null || fromTurn == null) {
			return status(STATUS_ERROR).toResult();
		}
		TLObject session = ChatSessionIds.ownedSession(sessionId, person);
		if (session == null) {
			return status(STATUS_NOT_FOUND).toResult();
		}
		// Busy means a run of that chat is writing into the turns that would go; a run of another
		// chat does not touch them.
		if (_binding.tracker().hasActiveProcessingStateFor(sessionId)) {
			return status(STATUS_BUSY).toResult();
		}
		ConversationBackend backend = ChatTranscripts.backendOf(session);
		SendRefusal refusal = backend == null ? null
			: SendRefusal.unsupported(backend.features().editMessages(),
				com.top_logic.ai.chat.backend.I18NConstants.EDIT_NOT_SUPPORTED);
		if (refusal != null) {
			return refused(refusal).toResult();
		}
		return sendTurn(args, Integer.valueOf(fromTurn.intValue())).toResult();
	}

	/**
	 * Sends a message, after dropping the turns from {@code fromTurn} on.
	 *
	 * @param fromTurn
	 *        The first turn to drop, <code>null</code> for a plain send. An edited message carries no
	 *        files: the files attached to the message being written stay for it.
	 */
	private ReactCommandReply sendTurn(Map<String, Object> args, Integer fromTurn) {
		Person person = actingPerson();
		if (person == null) {
			return status(STATUS_FORBIDDEN);
		}
		boolean edit = fromTurn != null;
		ChatAttachments draft = _binding.attachments();
		List<ChatAttachments.Entry> attached = edit ? List.of() : draft.entries();
		String text = stringArg(args, ARG_TEXT);
		String message = text == null ? "" : text.trim();
		if (message.isEmpty() && attached.isEmpty()) {
			return status(STATUS_ERROR);
		}
		ApprovalMode shown;
		try {
			String mode = blankToNull(stringArg(args, ARG_APPROVAL_MODE));
			shown = mode == null ? null : ApprovalMode.valueOf(mode);
		} catch (IllegalArgumentException ex) {
			return status(STATUS_ERROR);
		}

		TaskProgressTracker tracker = _binding.tracker();
		String sessionId = blankToNull(stringArg(args, ARG_SESSION_ID));
		TLObject created = null;
		if (sessionId == null) {
			// The first send is what brings a chat into being, titled after what was said, with the
			// mode the user set on it before it existed, about what the page shows now.
			created = ChatHistoryService.createSession(blankToDefault(stringArg(args, ARG_TITLE), DEFAULT_TITLE),
				person, shown == null ? ApprovalMode.MANUAL : shown, _binding.subject());
			if (created == null) {
				return status(STATUS_ERROR);
			}
			// The chat was written on the store's writer thread, at a revision newer than this
			// thread's view of the knowledge base.
			HistoryUtils.updateSessionRevision();
			sessionId = ChatSessionIds.idOf(created);
		} else if (ChatSessionIds.ownedSession(sessionId, person) == null) {
			// The id arrives from the browser, so without this it names whichever chat the caller
			// cares to type.
			return status(STATUS_NOT_FOUND);
		}
		boolean sent = false;
		try {
			if (!tracker.setActiveSessionId(sessionId, person)) {
				return status(STATUS_NOT_FOUND);
			}
			ReactCommandReply reply = sendInto(tracker, person, sessionId, message, attached, shown, args, fromTurn);
			sent = STATUS_PROCESSING.equals(reply.getFields().get(FIELD_STATUS));
			if (sent) {
				draft.removeAll(attached);
			}
			return reply;
		} finally {
			if (created != null && !sent) {
				// A send that went nowhere has produced nothing to keep, and an empty chat in the
				// list is a row the user would have to tidy away.
				ChatHistoryService.deleteSession(created);
			}
		}
	}

	/** Sends a message into the chat the conversation has just been switched to. */
	private ReactCommandReply sendInto(TaskProgressTracker tracker, Person person, String sessionId,
			String message, List<ChatAttachments.Entry> attached, ApprovalMode shown, Map<String, Object> args,
			Integer fromTurn) {
		// The turn runs with the mode the switch shows at the send, whatever reached the server
		// before: a switch still on its way must not make the turn ask while the browser answers by
		// itself, or approve while the person sees the switch off.
		if (shown != null) {
			if (tracker.getApprovalMode() != shown) {
				ApprovalChoice.applyToChat(tracker, person, sessionId, shown);
			}
			ApprovalChoice.save(shown);
		}
		String effort = blankToNull(stringArg(args, ARG_EFFORT));
		ChatProcessor.ChatRunOptions options = runOptions(args, effort);
		ConversationBackend backend = ChatTranscripts.backendOf(ChatSessionIds.ownedSession(sessionId, person));
		// A conversation a backend answers runs no agent of this application, so what the agent policy
		// says about the account's agents does not apply to it; what the backend supports does.
		SendRefusal refusal = backend == null
			? SendRefusal.forRun(person, options)
			: (attached.isEmpty() ? null : SendRefusal.unsupported(backend.features().attachments(),
				com.top_logic.ai.chat.backend.I18NConstants.ATTACHMENTS_NOT_SUPPORTED));
		if (refusal != null) {
			return refused(refusal);
		}
		// One reading of the account's spend for the pre-check, the send and the run (#835).
		RunLimits.SpendReading spendReading = RunLimits.spendReading(tracker.getSessionOwner());
		int removedTurns = 0;
		if (fromTurn != null) {
			// Before the turns are dropped: an edit refused afterwards would have removed what the
			// user still has and given them nothing in its place.
			String noRoom = ChatProcessor.noRoomReason(tracker, spendReading);
			if (noRoom != null) {
				return status(STATUS_REJECTED).put(FIELD_REASON, noRoom);
			}
			ChatState chat = tracker.getSessionManager().getActiveChatState();
			removedTurns = removeTurnsFrom(tracker, chat, person, fromTurn.intValue()).turns();
		}
		tracker.setEffort(effort);
		ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker, TLContextManager.getSubSession(),
			userName(tracker, person), message, ChatAttachments.contentOf(attached), options, _binding.facts(),
			spendReading);
		ReactCommandReply reply = sendResult(result);
		if (fromTurn != null) {
			reply.put(FIELD_REMOVED_TURNS, Integer.valueOf(removedTurns));
		}
		if (result.getTaskId() != null) {
			reply.put(FIELD_SESSION_ID, sessionId).put(FIELD_FEATURES, ChatTranscripts.featuresDto(backend));
		}
		return reply;
	}

	/**
	 * Answers the last message again, see {@link #CMD_REGENERATE}.
	 *
	 * <p>
	 * The previous answer is not kept beside the new one: the user asked for that turn to be done
	 * differently, so the turn is removed - from the session's memory and from its recorded
	 * transcript - and the same user message is sent once more. What the browser then shows is the
	 * replacement, in the place the old answer occupied.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_REGENERATE, params = {
		@ReactParam(name = ARG_SESSION_ID, type = "string", description = "The chat whose last answer is redone."),
		@ReactParam(name = ARG_AGENT, type = "string", description = "The agent of the new run."),
		@ReactParam(name = ARG_MODEL, type = "string", description = "The model of the new run."),
		@ReactParam(name = ARG_MCP_OVERRIDE, type = "boolean",
			description = "Whether the named MCP servers replace the agent's own."),
		@ReactParam(name = ARG_MCP_SERVERS, type = "array", description = "The MCP servers of the new run."),
		@ReactParam(name = ARG_EFFORT, type = "string", description = "How hard the model may think.")
	})
	public HandlerResult regenerate(Map<String, Object> args) {
		Person person = actingPerson();
		if (person == null) {
			return status(STATUS_FORBIDDEN).toResult();
		}
		String sessionId = blankToNull(stringArg(args, ARG_SESSION_ID));
		TLObject session = sessionId == null ? null : ChatSessionIds.ownedSession(sessionId, person);
		if (session == null) {
			return status(STATUS_NOT_FOUND).toResult();
		}
		TaskProgressTracker tracker = _binding.tracker();
		// Switches the conversation to the chat, as a send does: it starts a run, and a run is sent
		// into the chat the conversation shows. The regenerate button sits under an answer of the chat
		// on screen, so this is the chat it shows already.
		tracker.setActiveSessionId(sessionId, person);
		if (tracker.hasActiveProcessingState()) {
			return status(STATUS_BUSY).toResult();
		}
		String effort = blankToNull(stringArg(args, ARG_EFFORT));
		ChatProcessor.ChatRunOptions options = runOptions(args, effort);
		// Before the turn is removed, not after: a regenerate that is refused must leave the answer it
		// was going to replace where it is.
		ConversationBackend backend = ChatTranscripts.backendOf(session);
		SendRefusal refusal = backend == null
			? SendRefusal.forRun(person, options)
			: SendRefusal.unsupported(backend.features().regenerate(),
				com.top_logic.ai.chat.backend.I18NConstants.REGENERATE_NOT_SUPPORTED);
		if (refusal != null) {
			return refused(refusal).toResult();
		}
		// One reading of the account's spend for the pre-check here, the send's and the run's (#835).
		RunLimits.SpendReading spendReading = RunLimits.spendReading(tracker.getSessionOwner());
		String noRoom = ChatProcessor.noRoomReason(tracker, spendReading);
		if (noRoom != null) {
			return status(STATUS_REJECTED).put(FIELD_REASON, noRoom).toResult();
		}
		ChatState chat = tracker.getSessionManager().getActiveChatState();
		int turnCount = chat.log().turnCount();
		if (turnCount == 0) {
			return status(STATUS_ERROR).toResult();
		}
		Removed removed = removeTurnsFrom(tracker, chat, person, turnCount - 1);
		if (removed.text().isEmpty()) {
			return status(STATUS_ERROR).toResult();
		}
		tracker.setEffort(effort);
		ChatProcessor.SendResult result = ChatProcessor.sendMessage(tracker, TLContextManager.getSubSession(),
			userName(tracker, person), removed.text(), List.of(), options, _binding.facts(), spendReading);
		return sendResult(result)
			.put(FIELD_REMOVED_TURNS, Integer.valueOf(removed.turns()))
			.put(FIELD_TEXT, removed.text())
			.toResult();
	}

	/**
	 * Takes a file off the message being written, see {@link #CMD_REMOVE_ATTACHMENT}.
	 */
	@ReactCommandHandler(value = CMD_REMOVE_ATTACHMENT, params = {
		@ReactParam(name = ARG_ATTACHMENT_ID, type = "string", description = "The id of the file.")
	})
	public void removeAttachment(Map<String, Object> args) {
		String id = stringArg(args, ARG_ATTACHMENT_ID);
		if (id != null) {
			_binding.attachments().remove(id);
		}
	}

	/**
	 * Answers what a run of the account asks, see {@link #CMD_RESPOND}.
	 */
	@ReactCommandHandler(value = CMD_RESPOND, params = {
		@ReactParam(name = ARG_REQUEST_ID, type = "string", required = true,
			description = "The id of the approval or question."),
		@ReactParam(name = ARG_RESPONSE, type = "string", required = true,
			description = "APPROVED, APPROVED_FOR_SESSION, REJECTED, or the answer to a question."),
		@ReactParam(name = ARG_TOOL_NAME, type = "string", description = "The tool an approval is about."),
		@ReactParam(name = ARG_COMMENT, type = "string", description = "Why a tool call was denied."),
		@ReactParam(name = ARG_OWN_WORDS, type = "string",
			description = "What the user typed for a choice question.")
	})
	public HandlerResult respond(Map<String, Object> args) {
		Person person = actingPerson();
		boolean answered = person != null && AccountRuns.respond(person, stringArg(args, ARG_REQUEST_ID),
			stringArg(args, ARG_RESPONSE), stringArg(args, ARG_TOOL_NAME), stringArg(args, ARG_COMMENT),
			stringArg(args, ARG_OWN_WORDS));
		return status(answered ? STATUS_OK : STATUS_IGNORED).toResult();
	}

	/**
	 * Stops the run of a chat of the account, see {@link #CMD_CANCEL}.
	 */
	@ReactCommandHandler(value = CMD_CANCEL, params = {
		@ReactParam(name = ARG_SESSION_ID, type = "string",
			description = "The chat whose run is stopped; empty stops what this conversation runs.")
	})
	public HandlerResult cancel(Map<String, Object> args) {
		Person person = actingPerson();
		boolean cancelled = person != null
			&& AccountRuns.cancel(_binding.tracker(), person, blankToNull(stringArg(args, ARG_SESSION_ID)));
		return ReactCommandReply.create().put(FIELD_CANCELLED, Boolean.valueOf(cancelled)).toResult();
	}

	/**
	 * Turns "auto-approve" on or off, see {@link #CMD_SET_APPROVAL_MODE}.
	 */
	@ReactCommandHandler(value = CMD_SET_APPROVAL_MODE, params = {
		@ReactParam(name = ARG_MODE, type = "string", required = true, description = "AUTO_APPROVE or MANUAL."),
		@ReactParam(name = ARG_SESSION_ID, type = "string",
			description = "The chat switched; empty for a conversation that has no chat yet.")
	})
	public HandlerResult setApprovalMode(Map<String, Object> args) {
		Person person = actingPerson();
		if (person == null) {
			return status(STATUS_FORBIDDEN).toResult();
		}
		ApprovalMode mode;
		try {
			String name = blankToNull(stringArg(args, ARG_MODE));
			if (name == null) {
				return status(STATUS_ERROR).toResult();
			}
			mode = ApprovalMode.valueOf(name);
		} catch (IllegalArgumentException ex) {
			return status(STATUS_ERROR).toResult();
		}
		String sessionId = blankToNull(stringArg(args, ARG_SESSION_ID));
		if (sessionId != null) {
			if (ChatSessionIds.ownedSession(sessionId, person) == null) {
				return status(STATUS_NOT_FOUND).toResult();
			}
			ApprovalChoice.applyToChat(_binding.tracker(), person, sessionId, mode);
		}
		ApprovalChoice.save(mode);
		return status(STATUS_OK).toResult();
	}

	/**
	 * Records what the user thought of one answer, see {@link #CMD_RATE}.
	 *
	 * <p>
	 * The rating lands in the chat's metadata bag rather than in the transcript: a transcript is
	 * written per run and a rating arrives after it, while the metadata is the chat's own and is
	 * rewritten whole on every persist. That is also where an evaluation reads the ratings back from,
	 * keyed by the turn they belong to.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_RATE, params = {
		@ReactParam(name = ARG_SESSION_ID, type = "string", required = true,
			description = "The chat the rated answer belongs to."),
		@ReactParam(name = ARG_TURN_INDEX, type = "number", required = true,
			description = "The zero-based index of the turn whose answer is rated."),
		@ReactParam(name = ARG_RATING, type = "string", description = "up, down, or empty to withdraw."),
		@ReactParam(name = ARG_NOTE, type = "string", description = "Free text the user added.")
	})
	public HandlerResult rate(Map<String, Object> args) {
		Person person = actingPerson();
		if (person == null) {
			return status(STATUS_FORBIDDEN).toResult();
		}
		String sessionId = blankToNull(stringArg(args, ARG_SESSION_ID));
		Number turnIndex = args == null ? null : (args.get(ARG_TURN_INDEX) instanceof Number n ? n : null);
		if (sessionId == null || turnIndex == null) {
			return status(STATUS_ERROR).toResult();
		}
		if (ChatSessionIds.ownedSession(sessionId, person) == null) {
			return status(STATUS_NOT_FOUND).toResult();
		}
		TaskProgressTracker tracker = _binding.tracker();
		// The rated chat, not the one the conversation shows: a rating is no reason to switch it.
		ChatState chat = tracker.getSessionManager().chatStateFor(sessionId, person);
		String note = stringArg(args, ARG_NOTE);
		chat.setFeedback(turnIndex.intValue(), blankToNull(stringArg(args, ARG_RATING)), note == null ? "" : note);
		tracker.getSessionManager().persist(chat);
		return status(STATUS_OK).toResult();
	}

	/**
	 * Starts improving the draft in the composer, see {@link #CMD_IMPROVE_PROMPT}.
	 */
	@ReactCommandHandler(value = CMD_IMPROVE_PROMPT, params = {
		@ReactParam(name = ARG_DRAFT, type = "string", required = true, description = "The draft to improve."),
		@ReactParam(name = ARG_REQUEST_ID, type = "string", required = true,
			description = "The id the result frame names.")
	})
	public HandlerResult improvePrompt(Map<String, Object> args) {
		if (actingPerson() == null) {
			return status(STATUS_FORBIDDEN).toResult();
		}
		ChatViewBinding binding = _binding;
		try {
			PromptImprovementProcessor.start(binding.tracker(), stringArg(args, ARG_REQUEST_ID),
				stringArg(args, ARG_DRAFT), TLContextManager.getSubSession(),
				result -> binding.sendPromptImproved(PromptImprovedEvent.create()
					.setRequestId(result.requestId())
					.setImprovedText(result.improvedText())
					.setStatus(result.status())
					.setReason(result.reason())));
		} catch (PromptImprovementException ex) {
			if (ex.getFailure() == PromptImprovementProcessor.Failure.NO_ROOM) {
				Logger.info("Prompt improvement refused: " + ex.getMessage(), ChatControl.class);
				return status(STATUS_REJECTED)
					.put(FIELD_REASON,
						Resources.getInstance().getString(com.top_logic.tl.ai.core.limits.I18NConstants.TOO_MANY_RUNS_IN_PROGRESS))
					.toResult();
			}
			Logger.info("Prompt improvement not started: " + ex.getMessage(), ChatControl.class);
			return status(STATUS_ERROR).toResult();
		}
		return status(STATUS_STARTED).toResult();
	}

	/**
	 * Cancels a prompt improvement, see {@link #CMD_CANCEL_PROMPT_IMPROVEMENT}.
	 */
	@ReactCommandHandler(value = CMD_CANCEL_PROMPT_IMPROVEMENT, params = {
		@ReactParam(name = ARG_REQUEST_ID, type = "string", required = true,
			description = "The id of the improvement to cancel.")
	})
	public HandlerResult cancelPromptImprovement(Map<String, Object> args) {
		String requestId = blankToNull(stringArg(args, ARG_REQUEST_ID));
		boolean cancelled = requestId != null && _binding.tracker().cancelPromptImprovement(requestId);
		return ReactCommandReply.create().put(FIELD_CANCELLED, Boolean.valueOf(cancelled)).toResult();
	}

	/**
	 * Writes to a background subagent, see {@link #CMD_MESSAGE_SUBAGENT}.
	 */
	@ReactCommandHandler(value = CMD_MESSAGE_SUBAGENT, params = {
		@ReactParam(name = ARG_SUBAGENT_ID, type = "string", required = true, description = "The subagent's id."),
		@ReactParam(name = ARG_SESSION_ID, type = "string",
			description = "The chat whose lead started it; empty for the chat this conversation shows."),
		@ReactParam(name = ARG_PARENT_TOOL_CALL_ID, type = "string",
			description = "The lead's tool call that started it."),
		@ReactParam(name = ARG_TEXT, type = "string", required = true, description = "What the user writes.")
	})
	public HandlerResult messageSubagent(Map<String, Object> args) {
		AccountRuns.SubagentMessage answer = AccountRuns.messageSubagent(_binding.tracker(), actingPerson(),
			stringArg(args, ARG_SESSION_ID), stringArg(args, ARG_SUBAGENT_ID),
			stringArg(args, ARG_PARENT_TOOL_CALL_ID), stringArg(args, ARG_TEXT));
		return status(answer.status().code()).put(FIELD_MESSAGE_ID, answer.messageId()).toResult();
	}

	/**
	 * Stops a background subagent, see {@link #CMD_CANCEL_SUBAGENT}.
	 */
	@ReactCommandHandler(value = CMD_CANCEL_SUBAGENT, params = {
		@ReactParam(name = ARG_SUBAGENT_ID, type = "string", required = true, description = "The subagent's id."),
		@ReactParam(name = ARG_SESSION_ID, type = "string",
			description = "The chat whose lead started it; empty for the chat this conversation shows."),
		@ReactParam(name = ARG_PARENT_TOOL_CALL_ID, type = "string",
			description = "The lead's tool call that started it.")
	})
	public HandlerResult cancelSubagent(Map<String, Object> args) {
		boolean cancelled = AccountRuns.cancelSubagent(_binding.tracker(), actingPerson(),
			stringArg(args, ARG_SESSION_ID), stringArg(args, ARG_SUBAGENT_ID),
			stringArg(args, ARG_PARENT_TOOL_CALL_ID));
		return ReactCommandReply.create().put(FIELD_CANCELLED, Boolean.valueOf(cancelled)).toResult();
	}

	/**
	 * Stores the user's layout of a chat, see {@link #CMD_SAVE_LAYOUT}.
	 */
	@ReactCommandHandler(value = CMD_SAVE_LAYOUT, technical = true, params = {
		@ReactParam(name = ARG_LAYOUT_KEY, type = "string", description = "The key the layout is stored under."),
		@ReactParam(name = ARG_LAYOUT, type = "object", required = true, description = "The layout.")
	})
	public void saveLayout(Map<String, Object> args) {
		if (actingPerson() == null) {
			return;
		}
		Object layout = args == null ? null : args.get(ARG_LAYOUT);
		if (!(layout instanceof Map<?, ?>)) {
			return;
		}
		ChatPreferencesDto preferences;
		try {
			preferences = ChatPreferencesDto.readChatPreferencesDto(new JsonReader(new StringR(JSON.toString(layout))));
		} catch (IOException | RuntimeException ex) {
			Logger.info("Ignoring an unreadable chat layout: " + ex.getMessage(), ChatControl.class);
			return;
		}
		ChatPreferencesStore.save(stringArg(args, ARG_LAYOUT_KEY), preferences);
	}

	/**
	 * Attaches the uploaded files to the message being written in this conversation.
	 *
	 * <p>
	 * A file the message cannot carry - of another type than an image or PDF, one file more than
	 * {@link ChatAttachments#MAX_ATTACHMENTS} - is not attached; the answer says why under
	 * {@link #FIELD_REASON}, one sentence per file. The files attached are in this control's state
	 * ({@link #STATE_DRAFT_ATTACHMENTS}), and in that of every other displayed chat control of the
	 * conversation.
	 * </p>
	 */
	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<jakarta.servlet.http.Part> parts) {
		if (actingPerson() == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		ChatAttachments attachments = _binding.attachments();
		List<String> refused = new ArrayList<>();
		for (jakarta.servlet.http.Part part : parts) {
			if (!UploadSupport.FILE_PART.equals(part.getName())) {
				continue;
			}
			String fileName = blankToDefault(part.getSubmittedFileName(), UPLOAD_FILE_NAME);
			byte[] content;
			try (InputStream in = part.getInputStream()) {
				content = in.readAllBytes();
			} catch (IOException ex) {
				throw new UncheckedIOException("Failed to read the attachment '" + fileName + "'.", ex);
			}
			ResKey refusal = attachments.add(fileName, part.getContentType(), content);
			if (refusal != null) {
				refused.add(Resources.getInstance().getString(refusal));
			}
		}
		ReactCommandReply reply = ReactCommandReply.create();
		if (!refused.isEmpty()) {
			reply.put(FIELD_REASON, refused);
		}
		return reply.toResult();
	}

	/**
	 * The size limit of one attached file: {@link ChatAttachments#MAX_ATTACHMENT_SIZE}, or the
	 * application's own limit where that is smaller.
	 */
	@Override
	public long maxUploadSize() {
		return UploadSupport.effectiveMaxUploadSize(ChatAttachments.MAX_ATTACHMENT_SIZE);
	}

	/** The answer to a send that went to {@link ChatProcessor#sendMessage}. */
	private static ReactCommandReply sendResult(ChatProcessor.SendResult result) {
		if (result.isRejected()) {
			// Neither forbidden nor broken: this installation has no room for the run right now, and
			// the browser shows the sentence rather than a failure.
			return status(STATUS_REJECTED).put(FIELD_REASON, result.getReason());
		}
		if (result.getTaskId() == null) {
			return status(result.isBusy() ? STATUS_BUSY : STATUS_ERROR);
		}
		return status(STATUS_PROCESSING);
	}

	private static ReactCommandReply status(String status) {
		return ReactCommandReply.create().put(FIELD_STATUS, status);
	}

	private static ReactCommandReply refused(SendRefusal refusal) {
		return status(refusal.status()).put(FIELD_REASON, refusal.reason());
	}

	/** The turn settings a send, an edit or a regenerate names. */
	private static ChatProcessor.ChatRunOptions runOptions(Map<String, Object> args, String effort) {
		return new ChatProcessor.ChatRunOptions(
			blankToNull(stringArg(args, ARG_AGENT)),
			blankToNull(stringArg(args, ARG_MODEL)),
			Boolean.TRUE.equals(args == null ? null : args.get(ARG_MCP_OVERRIDE)),
			stringsArg(args, ARG_MCP_SERVERS),
			effort);
	}

	/** The display name of the acting account, resolved once and remembered on the tracker. */
	private static String userName(TaskProgressTracker tracker, Person person) {
		String stored = tracker.getUserName();
		if (stored != null) {
			return stored;
		}
		String name = person.getName();
		if (name == null) {
			return ANONYMOUS_NAME;
		}
		tracker.setUserName(name);
		return name;
	}

	/** What a truncation took away. */
	private record Removed(int turns, String text) {
		// Pure data.
	}

	/**
	 * Removes turn {@code fromTurn} and everything after it from the live session and from what is
	 * recorded for it.
	 *
	 * <p>
	 * Three things hold a copy of a conversation and all three have to forget it: the conversation
	 * memory the model is given, the conversation log the UI is rebuilt from, and the per-run
	 * transcripts in the knowledge base. Ratings of removed turns go too - they describe answers
	 * that no longer exist.
	 * </p>
	 *
	 * @param chat
	 *        The chat to remove the turns from.
	 * @return how many turns went, and the user text of the first of them
	 */
	private static Removed removeTurnsFrom(TaskProgressTracker tracker, ChatState chat, Person person,
			int fromTurn) {
		ConversationLog log = chat.log();
		List<Turn> turns = log.turns();
		if (fromTurn < 0 || fromTurn >= turns.size()) {
			return new Removed(0, "");
		}
		String text = userTextOf(turns.get(fromTurn));
		int removed = log.removeFrom(fromTurn);

		ConversationMemory memory = chat.memory();
		if (memory != null) {
			ConversationLogReplay.rebuildMemory(log, memory.conversation());
		}
		chat.dropFeedbackFrom(fromTurn);

		String sessionId = chat.sessionId();
		if (sessionId != null) {
			// Guarded again here, not only in the handler: this is the call that deletes, and it must
			// be impossible to reach it through a path that forgot to ask whose session it is.
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, person);
			if (session != null) {
				ChatHistoryService.truncateConversation(session, fromTurn);
			}
		}
		tracker.getSessionManager().persist(chat);
		return new Removed(removed, text);
	}

	/** The text the user contributed to a turn, joined over its parts. */
	private static String userTextOf(Turn turn) {
		StringBuilder text = new StringBuilder();
		for (Part part : turn.userParts()) {
			if (part instanceof Part.TextPart textPart) {
				if (text.length() > 0) {
					text.append('\n');
				}
				text.append(textPart.text());
			}
		}
		return text.toString();
	}

	/**
	 * The signed-in account acting, <code>null</code> for a window nobody is signed in to: the chat
	 * has no anonymous mode.
	 */
	private static Person actingPerson() {
		if (TLContext.isAnonymous()) {
			return null;
		}
		return currentPerson();
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

	private static List<String> stringsArg(Map<String, Object> args, String key) {
		Object value = args == null ? null : args.get(key);
		if (!(value instanceof Collection<?> values)) {
			return List.of();
		}
		List<String> result = new ArrayList<>(values.size());
		for (Object element : values) {
			if (element instanceof String text) {
				result.add(text);
			}
		}
		return result;
	}

	/** The given value trimmed, or <code>null</code> for a value that is empty or only blanks. */
	protected static String blankToNull(String value) {
		return blankToDefault(value, null);
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
