package com.top_logic.chat.streaming;

/**
 * State of a chat streaming session.
 */
public enum StreamingState {
	/** No active streaming session. */
	NO_STREAMING,

	/** Actively streaming response chunks. */
	STREAMING,

	/** Streaming completed successfully. */
	FINISHED
}
