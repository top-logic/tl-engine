package com.top_logic.chat.streaming;

/**
 * A chunk of streaming response data.
 */
public class StreamingChunk {

	private final String textDelta;
	private final boolean hasMore;

	public StreamingChunk(String textDelta, boolean hasMore) {
		this.textDelta = textDelta;
		this.hasMore = hasMore;
	}

	/**
	 * The text content for this chunk (incremental delta).
	 */
	public String getTextDelta() {
		return textDelta;
	}

	/**
	 * Whether more chunks are available.
	 */
	public boolean hasMore() {
		return hasMore;
	}
}
