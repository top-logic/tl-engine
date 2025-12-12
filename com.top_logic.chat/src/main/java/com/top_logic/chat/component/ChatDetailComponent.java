package com.top_logic.chat.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.chat.model.Chat;
import com.top_logic.chat.streaming.StreamingState;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Component displaying the detail view of a selected chat with message history and input form.
 *
 * <p>
 * This component manages:
 * </p>
 * <ul>
 * <li>Display of chat messages</li>
 * <li>Form with message input field</li>
 * <li>Streaming state during assistant response generation</li>
 * <li>Poll-based progressive updates</li>
 * </ul>
 */
public class ChatDetailComponent extends FormComponent {

	/**
	 * Configuration for {@link ChatDetailComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/** Configuration name for {@link #getPollInterval()}. */
		String POLL_INTERVAL = "pollInterval";

		/** Configuration name for {@link #getPage()}. */
		String PAGE = "page";

		/**
		 * Polling interval in milliseconds for streaming updates.
		 */
		@Name(POLL_INTERVAL)
		@IntDefault(500)
		int getPollInterval();

		/**
		 * JSP page for rendering the component.
		 */
		@Override
		@Name(PAGE)
		@StringDefault("chat/chatDetail.jsp")
		String getPage();

		@Override
		default void modifyIntrinsicCommands(CommandGroupRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerGroup(SimpleBoundCommandGroup.WRITE);
		}
	}

	/** Form field name for message input. */
	public static final String MESSAGE_INPUT_FIELD = "messageInput";

	private final int _pollInterval;

	private StreamingState _streamingState = StreamingState.NO_STREAMING;
	private String _streamingSessionId;
	private String _streamingMessageId;

	/**
	 * Creates a {@link ChatDetailComponent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChatDetailComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_pollInterval = config.getPollInterval();
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext("chatForm", getResPrefix());

		// Create multi-line input field for message
		StringField messageInput = FormFactory.newStringField(MESSAGE_INPUT_FIELD, "", true);
		messageInput.setMultiLine(true);
		messageInput.setRows(3);

		formContext.addMember(messageInput);

		return formContext;
	}

	@Override
	protected void receiveModelChanged(Object oldModel, Object newModel) {
		super.receiveModelChanged(oldModel, newModel);

		// When chat selection changes, cleanup any active streaming and reset form
		cleanupStreaming();
		removeFormContext();
	}

	@Override
	protected void becomingInvisible() {
		// Cleanup streaming session when component becomes invisible
		cleanupStreaming();
		super.becomingInvisible();
	}

	/**
	 * Get the current streaming state.
	 */
	public StreamingState getStreamingState() {
		return _streamingState;
	}

	/**
	 * Get the current streaming session ID.
	 */
	public String getStreamingSessionId() {
		return _streamingSessionId;
	}

	/**
	 * Get the ID of the message currently being streamed.
	 */
	public String getStreamingMessageId() {
		return _streamingMessageId;
	}

	/**
	 * Get the configured poll interval in milliseconds.
	 */
	public int getPollInterval() {
		return _pollInterval;
	}

	/**
	 * Set the streaming state.
	 *
	 * @param state
	 *        The new streaming state.
	 * @param sessionId
	 *        The streaming session ID (null if not streaming).
	 * @param messageId
	 *        The ID of the message being streamed (null if not streaming).
	 */
	public void setStreamingState(StreamingState state, String sessionId, String messageId) {
		this._streamingState = state;
		this._streamingSessionId = sessionId;
		this._streamingMessageId = messageId;
	}

	/**
	 * Clean up any active streaming session.
	 */
	public void cleanupStreaming() {
		if (_streamingState == StreamingState.STREAMING && _streamingSessionId != null) {
			// Cleanup will be handled by PollStreamingHandler
			_streamingState = StreamingState.FINISHED;
			_streamingSessionId = null;
			_streamingMessageId = null;
		}
	}

	/**
	 * Get the currently selected chat from the model.
	 */
	public Chat getChat() {
		Object model = getModel();
		if (model instanceof Chat) {
			return (Chat) model;
		}
		return null;
	}
}
