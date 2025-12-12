package com.top_logic.chat.streaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.chat.model.Chat;
import com.top_logic.chat.model.ChatMessage;

/**
 * Mock implementation of {@link ChatStreamingService} with simulated streaming responses.
 *
 * <p>
 * This implementation provides hardcoded responses split into chunks with artificial delays,
 * simulating a real streaming AI service without requiring external dependencies.
 * </p>
 */
public class MockChatStreamingService implements ChatStreamingService {

	/**
	 * Singleton instance.
	 */
	public static final MockChatStreamingService INSTANCE = new MockChatStreamingService();

	private static final String[] MOCK_RESPONSES = {
		"This is a mock response from the chat assistant. In a real implementation, this would call an external AI service. The response would be generated based on the user's input and context from previous messages in the conversation.",
		"Hello! I'm a simulated assistant responding to your message. This mock implementation demonstrates the streaming capability by breaking the response into smaller chunks. Each chunk is delivered progressively to create a smooth user experience.",
		"That's an interesting question! Here's a mock answer that shows how the streaming feature works. The text appears gradually, chunk by chunk, as if it were being generated in real-time by an AI model. This provides immediate feedback to the user.",
		"I understand your request. Let me provide a comprehensive mock response that will be streamed to you piece by piece. This simulation helps test the UI components without requiring integration with an actual AI service during development.",
		"Thank you for your message! This simulated response demonstrates the progressive rendering of assistant messages. In production, you would replace this mock service with a real integration to services like OpenAI or Anthropic Claude."
	};

	private static final int CHUNK_SIZE_WORDS = 3;
	private static final long INITIAL_DELAY_MS = 100;

	private final Map<String, MockStreamingSession> activeSessions = new ConcurrentHashMap<>();
	private final Random random = new Random();

	private MockChatStreamingService() {
		// Singleton
	}

	@Override
	public String startStreaming(Chat chat, ChatMessage userMessage) {
		String sessionId = UUID.randomUUID().toString();

		// Select random response
		String response = MOCK_RESPONSES[random.nextInt(MOCK_RESPONSES.length)];

		// Split into word chunks
		List<String> chunks = splitIntoChunks(response, CHUNK_SIZE_WORDS);

		// Create session
		MockStreamingSession session = new MockStreamingSession(
			sessionId,
			chat,
			userMessage,
			chunks
		);

		activeSessions.put(sessionId, session);

		return sessionId;
	}

	@Override
	public StreamingChunk getNextChunk(String sessionId) {
		MockStreamingSession session = activeSessions.get(sessionId);
		if (session == null) {
			return new StreamingChunk("", false);
		}

		// Update last access time
		session.lastAccessTime = System.currentTimeMillis();

		// Get next chunk
		if (session.currentChunkIndex < session.responseChunks.size()) {
			String chunk = session.responseChunks.get(session.currentChunkIndex);
			session.currentChunkIndex++;

			boolean hasMore = session.currentChunkIndex < session.responseChunks.size();
			return new StreamingChunk(chunk, hasMore);
		}

		return new StreamingChunk("", false);
	}

	@Override
	public boolean isComplete(String sessionId) {
		MockStreamingSession session = activeSessions.get(sessionId);
		if (session == null) {
			return true;
		}
		return session.currentChunkIndex >= session.responseChunks.size();
	}

	@Override
	public void cleanup(String sessionId) {
		activeSessions.remove(sessionId);
	}

	/**
	 * Clean up sessions that have been idle for more than 5 minutes.
	 */
	public void cleanupIdleSessions() {
		long currentTime = System.currentTimeMillis();
		long timeoutThreshold = 5 * 60 * 1000; // 5 minutes

		List<String> toRemove = new ArrayList<>();
		for (Map.Entry<String, MockStreamingSession> entry : activeSessions.entrySet()) {
			if (currentTime - entry.getValue().lastAccessTime > timeoutThreshold) {
				toRemove.add(entry.getKey());
			}
		}

		for (String sessionId : toRemove) {
			cleanup(sessionId);
		}
	}

	private List<String> splitIntoChunks(String text, int wordsPerChunk) {
		List<String> chunks = new ArrayList<>();
		String[] words = text.split("\\s+");

		StringBuilder currentChunk = new StringBuilder();
		int wordCount = 0;

		for (String word : words) {
			if (currentChunk.length() > 0) {
				currentChunk.append(" ");
			}
			currentChunk.append(word);
			wordCount++;

			if (wordCount >= wordsPerChunk) {
				chunks.add(currentChunk.toString());
				currentChunk = new StringBuilder();
				wordCount = 0;
			}
		}

		// Add remaining words
		if (currentChunk.length() > 0) {
			chunks.add(currentChunk.toString());
		}

		return chunks;
	}

	/**
	 * Internal session state for mock streaming.
	 */
	private static class MockStreamingSession {
		final String sessionId;
		final Chat chat;
		final ChatMessage userMessage;
		final List<String> responseChunks;
		int currentChunkIndex;
		long lastAccessTime;

		MockStreamingSession(String sessionId, Chat chat, ChatMessage userMessage, List<String> responseChunks) {
			this.sessionId = sessionId;
			this.chat = chat;
			this.userMessage = userMessage;
			this.responseChunks = responseChunks;
			this.currentChunkIndex = 0;
			this.lastAccessTime = System.currentTimeMillis();
		}
	}
}
