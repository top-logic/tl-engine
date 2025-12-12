package com.top_logic.chat.handler;

import java.util.Date;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.chat.component.ChatDetailComponent;
import com.top_logic.chat.model.Chat;
import com.top_logic.chat.model.ChatMessage;
import com.top_logic.chat.streaming.ChatStreamingService;
import com.top_logic.chat.streaming.MockChatStreamingService;
import com.top_logic.chat.streaming.StreamingState;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * Command handler for sending a chat message.
 *
 * <p>
 * This handler:
 * </p>
 * <ul>
 * <li>Creates a user message from the form input</li>
 * <li>Creates an empty assistant message placeholder</li>
 * <li>Starts the streaming session</li>
 * <li>Schedules the first poll</li>
 * </ul>
 */
public class SendMessageHandler extends AbstractApplyCommandHandler {

	/** Command ID for this handler. */
	public static final String COMMAND_ID = "sendMessage";

	/** Role constant for user messages. */
	private static final String ROLE_USER = "user";

	/** Role constant for assistant messages. */
	private static final String ROLE_ASSISTANT = "assistant";

	/**
	 * Creates a {@link SendMessageHandler} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SendMessageHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
		if (!(component instanceof ChatDetailComponent)) {
			return false;
		}

		ChatDetailComponent detailComp = (ChatDetailComponent) component;
		Chat chat = (Chat) model;

		if (chat == null) {
			return false;
		}

		// Check if already streaming
		if (detailComp.getStreamingState() == StreamingState.STREAMING) {
			return false;
		}

		// Get message text from form
		FormField messageField = formContext.getField(ChatDetailComponent.MESSAGE_INPUT_FIELD);
		String messageText = (String) messageField.getValue();

		if (messageText == null || messageText.trim().isEmpty()) {
			return false;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			// Create user message
			ChatMessage userMsg = createMessage(ROLE_USER, messageText.trim(), new Date());
			chat.getMessages().add(userMsg);
			chat.setLastMessageAt(new Date());

			// Create empty assistant placeholder
			ChatMessage assistantMsg = createMessage(ROLE_ASSISTANT, "", new Date());
			chat.getMessages().add(assistantMsg);

			tx.commit();

			// Get streaming service
			ChatStreamingService streamingService = MockChatStreamingService.INSTANCE;

			// Start streaming
			String sessionId = streamingService.startStreaming(chat, userMsg);

			// Get assistant message ID
			String assistantMsgId = ((Wrapper) assistantMsg).tId().toString();

			// Update component state
			detailComp.setStreamingState(StreamingState.STREAMING, sessionId, assistantMsgId);

			// Clear input field
			messageField.setValue("");

			// Schedule first poll
			int pollInterval = detailComp.getPollInterval();
			String pollScript = buildPollScript(pollInterval);
			detailComp.addUpdate(new JSSnipplet(pollScript));

			// Invalidate component to refresh message display
			detailComp.invalidate();

			return true;

		} catch (Exception ex) {
			return false;
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Create a new {@link ChatMessage}.
	 */
	private ChatMessage createMessage(String role, String text, Date timestamp) {
		TLClass messageType = getChatMessageType();
		ChatMessage message = (ChatMessage) TLModelUtil.createObject(messageType);
		message.setRole(role);
		message.setText(text);
		message.setTimestamp(timestamp);
		return message;
	}

	/**
	 * Get the {@link ChatMessage} TLClass.
	 */
	private TLClass getChatMessageType() {
		TLModel model = ModelService.getApplicationModel();
		return (TLClass) model.getType("Chat:ChatMessage");
	}

	/**
	 * Build the JavaScript snippet for scheduling a poll.
	 */
	private String buildPollScript(int intervalMs) {
		return "setTimeout(function() { services.ajax.execute('pollStreaming'); }, " + intervalMs + ");";
	}
}
