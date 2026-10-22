package com.top_logic.layout.react.state;

/**
 * A suggestion offered for a text being typed: a replacement for a range of that text.
 *
 * Picking the suggestion replaces the characters {@link #getFrom()} (inclusive) to {@link #getTo()}
 * (exclusive) of the text it was computed for by {@link #getText()}, and puts the caret behind the
 * inserted text. With {@link #getFrom()} equal to {@link #getTo()}, the suggestion inserts its text at that
 * position.
 */
public interface SuggestionEntry extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.SuggestionEntry} instance.
	 */
	static com.top_logic.layout.react.state.SuggestionEntry create() {
		return new com.top_logic.layout.react.state.impl.SuggestionEntry_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.SuggestionEntry} type in JSON format. */
	String SUGGESTION_ENTRY__TYPE = "SuggestionEntry";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getText() */
	String TEXT__PROP = "text";

	/** @see #getFrom() */
	String FROM__PROP = "from";

	/** @see #getTo() */
	String TO__PROP = "to";

	/**
	 * What the list of suggestions shows for this suggestion.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.SuggestionEntry setLabel(String value);

	/**
	 * The text replacing the range {@link #getFrom()} to {@link #getTo()}.
	 */
	String getText();

	/**
	 * @see #getText()
	 */
	com.top_logic.layout.react.state.SuggestionEntry setText(String value);

	/**
	 * The position of the first character replaced, a character offset in the text the suggestion
	 * was computed for.
	 */
	int getFrom();

	/**
	 * @see #getFrom()
	 */
	com.top_logic.layout.react.state.SuggestionEntry setFrom(int value);

	/**
	 * The position behind the last character replaced, a character offset in the text the
	 * suggestion was computed for.
	 */
	int getTo();

	/**
	 * @see #getTo()
	 */
	com.top_logic.layout.react.state.SuggestionEntry setTo(int value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.SuggestionEntry readSuggestionEntry(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.SuggestionEntry_Impl result = new com.top_logic.layout.react.state.impl.SuggestionEntry_Impl();
		result.readContent(in);
		return result;
	}

}
