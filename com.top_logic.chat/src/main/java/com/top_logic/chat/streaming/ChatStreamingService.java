package com.top_logic.chat.streaming;

import com.top_logic.chat.model.Chat;
import com.top_logic.chat.model.ChatMessage;

/**
 * Service for streaming chat responses.
 *
 * <p>
 * This interface defines the contract for backend streaming handlers.
 * Implementations can provide mock responses or integrate with real AI services.
 * </p>
 */
public interface ChatStreamingService {

	/**
	 * Start streaming a response for the given user message.
	 *
	 * @param chat
	 *        The chat context.
	 * @param userMessage
	 *        The user's message to respond to.
	 * @return A unique session ID for this streaming session.
	 */
	String startStreaming(Chat chat, ChatMessage userMessage);

	/**
	 * Get the next chunk of the streaming response.
	 *
	 * @param sessionId
	 *        The streaming session ID.
	 * @return The next chunk with text delta and continuation flag.
	 */
	StreamingChunk getNextChunk(String sessionId);

	/**
	 * Check if streaming is complete for the given session.
	 *
	 * @param sessionId
	 *        The streaming session ID.
	 * @return True if streaming is complete, false otherwise.
	 */
	boolean isComplete(String sessionId);

	/**
	 * Clean up resources for a streaming session.
	 *
	 * @param sessionId
	 *        The streaming session ID to cleanup.
	 */
	void cleanup(String sessionId);
}
