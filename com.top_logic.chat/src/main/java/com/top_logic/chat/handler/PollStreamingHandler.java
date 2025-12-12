package com.top_logic.chat.handler;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.chat.component.ChatDetailComponent;
import com.top_logic.chat.model.Chat;
import com.top_logic.chat.model.ChatMessage;
import com.top_logic.chat.streaming.ChatStreamingService;
import com.top_logic.chat.streaming.MockChatStreamingService;
import com.top_logic.chat.streaming.StreamingChunk;
import com.top_logic.chat.streaming.StreamingState;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command handler for polling streaming updates.
 *
 * <p>
 * This handler:
 * </p>
 * <ul>
 * <li>Checks if streaming is active</li>
 * <li>Fetches the next chunk from the streaming service</li>
 * <li>Appends the chunk to the assistant message</li>
 * <li>Schedules the next poll or finishes streaming</li>
 * </ul>
 */
public class PollStreamingHandler extends AJAXCommandHandler {

	/** Command ID for this handler. */
	public static final String COMMAND_ID = "pollStreaming";

	/**
	 * Creates a {@link PollStreamingHandler} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PollStreamingHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> arguments) {

		if (!(component instanceof ChatDetailComponent)) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ChatDetailComponent detailComp = (ChatDetailComponent) component;

		// Check if streaming is active
		if (detailComp.getStreamingState() != StreamingState.STREAMING) {
			return HandlerResult.DEFAULT_RESULT;
		}

		String sessionId = detailComp.getStreamingSessionId();
		String messageId = detailComp.getStreamingMessageId();

		if (sessionId == null || messageId == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		// Get streaming service
		ChatStreamingService streamingService = MockChatStreamingService.INSTANCE;

		// Get next chunk
		StreamingChunk chunk = streamingService.getNextChunk(sessionId);

		if (StringServices.isEmpty(chunk.getTextDelta())) {
			return HandlerResult.DEFAULT_RESULT;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			// Load assistant message
			ChatMessage assistantMsg = loadMessage(messageId);
			if (assistantMsg == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			// Append chunk to message text
			String currentText = assistantMsg.getText();
			if (currentText == null) {
				currentText = "";
			}
			assistantMsg.setText(currentText + chunk.getTextDelta());

			tx.commit();

			// Create fragment insertion to append chunk to message div
			String messageDivId = "message-" + messageId;
			HTMLFragment fragment = createTextFragment(chunk.getTextDelta());
			detailComp.addUpdate(new FragmentInsertion(messageDivId, FragmentInsertion.POSITION_END_VALUE, fragment));

			if (chunk.hasMore()) {
				// Schedule next poll
				int pollInterval = detailComp.getPollInterval();
				String pollScript = buildPollScript(pollInterval);
				detailComp.addUpdate(new JSSnipplet(pollScript));
			} else {
				// Streaming complete
				Chat chat = (Chat) model;
				if (chat != null) {
					chat.setLastMessageAt(new Date());
				}

				detailComp.setStreamingState(StreamingState.FINISHED, null, null);
				streamingService.cleanup(sessionId);

				// Invalidate chat list to trigger re-sort
				LayoutComponent mainLayout = detailComp.getMainLayout();
				if (mainLayout != null) {
					LayoutComponent chatList = mainLayout.getComponentByName("chatList");
					if (chatList != null) {
						chatList.invalidate();
					}
				}
			}

			return HandlerResult.DEFAULT_RESULT;

		} catch (Exception ex) {
			// On error, cleanup and finish streaming
			detailComp.setStreamingState(StreamingState.FINISHED, null, null);
			streamingService.cleanup(sessionId);
			return HandlerResult.DEFAULT_RESULT;
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Load a {@link ChatMessage} by its ID.
	 */
	private ChatMessage loadMessage(String messageId) {
		try {
			Wrapper wrapper = WrapperFactory.getWrapper(messageId);
			if (wrapper instanceof ChatMessage) {
				return (ChatMessage) wrapper;
			}
		} catch (Exception ex) {
			// Message not found
		}
		return null;
	}

	/**
	 * Create an HTML fragment for text content.
	 */
	private HTMLFragment createTextFragment(String text) {
		return new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.writeText(text);
			}
		};
	}

	/**
	 * Build the JavaScript snippet for scheduling a poll.
	 */
	private String buildPollScript(int intervalMs) {
		return "setTimeout(function() { services.ajax.execute('pollStreaming'); }, " + intervalMs + ");";
	}
}
