/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * What an agent may remember about the accounts of this installation.
 *
 * <p>
 * A memory is written only with the user's agreement in the very turn it is written - see
 * {@code MemoryConsent} - and the newest of them are read back into the environment block of every
 * following conversation. This section is where an administrator switches the whole feature off,
 * caps how much of it reaches a model, and adjusts the words the agreement check looks for.
 * </p>
 *
 * @see UserMemorySettings
 * @see UserMemories
 */
@TagName("user-memory")
@DisplayOrder({
	UserMemoryConfig.ENABLED,
	UserMemoryConfig.MAX_LOADED,
	UserMemoryConfig.MAX_LENGTH,
	UserMemoryConfig.REQUEST_WORDS,
	UserMemoryConfig.AFFIRMATIVE_WORDS,
	UserMemoryConfig.NEGATIVE_WORDS,
})
public interface UserMemoryConfig extends ConfigPart {

	/** Configuration property name for {@link #getEnabled()}. */
	String ENABLED = "enabled";

	/** Configuration property name for {@link #getMaxLoaded()}. */
	String MAX_LOADED = "max-loaded";

	/** Configuration property name for {@link #getMaxLength()}. */
	String MAX_LENGTH = "max-length";

	/** Configuration property name for {@link #getRequestWords()}. */
	String REQUEST_WORDS = "request-words";

	/** Configuration property name for {@link #getAffirmativeWords()}. */
	String AFFIRMATIVE_WORDS = "affirmative-words";

	/** Configuration property name for {@link #getNegativeWords()}. */
	String NEGATIVE_WORDS = "negative-words";

	/**
	 * Whether this installation keeps memories about its users at all.
	 *
	 * <p>
	 * Switched off, the {@code remember} and {@code recall} tools are not offered to any agent and
	 * refuse a call that reaches them anyway, and no memory is rendered into any environment block.
	 * Existing memories are kept - this is a switch about what is written and sent, not about what
	 * is stored - so switching it back on restores them.
	 * </p>
	 *
	 * @see #ENABLED
	 */
	@Name(ENABLED)
	@Label("Enabled")
	@BooleanDefault(true)
	boolean getEnabled();

	/**
	 * How many of an account's newest memories are read into the environment block.
	 *
	 * <p>
	 * They sit in front of every request of the conversation, so their length is paid for on every
	 * turn. What does not fit is still stored and still findable with the {@code recall} tool.
	 * </p>
	 *
	 * @see #MAX_LOADED
	 */
	@Name(MAX_LOADED)
	@Label("Maximum memories loaded")
	@IntDefault(20)
	int getMaxLoaded();

	/**
	 * How long one memory's text may be, in characters.
	 *
	 * <p>
	 * A longer text is refused when the agent tries to write it, with a message naming the limit. A
	 * memory is a sentence about the user, not a document.
	 * </p>
	 *
	 * @see #MAX_LENGTH
	 */
	@Name(MAX_LENGTH)
	@Label("Maximum memory length")
	@IntDefault(500)
	int getMaxLength();

	/**
	 * The words that make a sentence a request to remember something.
	 *
	 * <p>
	 * The agreement check is a check on what the <em>user</em> typed, so these are matched against
	 * the user's own words, in whatever language they wrote them. The shipped list covers the two
	 * languages this product ships; an installation whose users write a third adds its words here.
	 * Matching ignores case and diacritics and is on word boundaries, so "merken" does not fire on
	 * "anmerkenswert".
	 * </p>
	 *
	 * @see #REQUEST_WORDS
	 */
	@Name(REQUEST_WORDS)
	@Label("Request words")
	@Format(CommaSeparatedStrings.class)
	@FormattedDefault("remember,memorize,memorise,keep in mind,note that,make a note,"
		+ "don't forget,do not forget,save that,merk dir,merke dir,merk es dir,merken,"
		+ "gemerkt,behalte,behalten,speichere,speicher dir,notiere,vergiss nicht,denk dran,"
		+ "denke daran")
	List<String> getRequestWords();

	/**
	 * The words with which a user agrees to a question the agent asked.
	 *
	 * <p>
	 * Read together with the question: an agreement counts only when the question the agent asked
	 * was itself about remembering something, so a "yes" to some other question grants nothing.
	 * Matched like {@link #getRequestWords()}.
	 * </p>
	 *
	 * @see #AFFIRMATIVE_WORDS
	 */
	@Name(AFFIRMATIVE_WORDS)
	@Label("Affirmative words")
	@Format(CommaSeparatedStrings.class)
	@FormattedDefault("yes,yep,yeah,sure,ok,okay,please do,go ahead,agreed,agree,do it,"
		+ "ja,jau,klar,gerne,gern,sicher,einverstanden,mach das,mache das,bitte,"
		+ "in ordnung,passt")
	List<String> getAffirmativeWords();

	/**
	 * The words with which a user refuses.
	 *
	 * <p>
	 * They win over {@link #getAffirmativeWords()}: an answer that carries a negation grants
	 * nothing, even when it also carries a word that would otherwise read as agreement - "no, that
	 * is fine" is a refusal. Refusing is the safe direction, so the ambiguous case goes that way.
	 * </p>
	 *
	 * @see #NEGATIVE_WORDS
	 */
	@Name(NEGATIVE_WORDS)
	@Label("Negative words")
	@Format(CommaSeparatedStrings.class)
	@FormattedDefault("no,nope,never,rather not,don't,do not,"
		+ "nein,nee,nicht,kein,keine,keinesfalls,lieber nicht,auf keinen fall")
	List<String> getNegativeWords();
}
