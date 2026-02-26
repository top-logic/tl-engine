/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.test.agent.judge.messages;

/**
 * Result from AI judge scoring ModelExplorer agent output.
 */
public class JudgeResult extends de.haumacher.msgbuf.data.AbstractDataObject
		implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult} instance.
	 */
	public static com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult create() {
		return new com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult();
	}

	/**
	 * Identifier for the {@link com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult} type in
	 * JSON format.
	 */
	public static final String JUDGE_RESULT__TYPE = "JudgeResult";

	/** @see #getScore() */
	public static final String SCORE__PROP = "score";

	/** @see #getMissingFacts() */
	public static final String MISSING_FACTS__PROP = "missingFacts";

	/** @see #getIncorrectClaims() */
	public static final String INCORRECT_CLAIMS__PROP = "incorrectClaims";

	/** @see #getScopeViolations() */
	public static final String SCOPE_VIOLATIONS__PROP = "scopeViolations";

	/** @see #getHallucinations() */
	public static final String HALLUCINATIONS__PROP = "hallucinations";

	/** @see #getNotFetched() */
	public static final String NOT_FETCHED__PROP = "notFetched";

	/** @see #getFormatViolations() */
	public static final String FORMAT_VIOLATIONS__PROP = "formatViolations";

	private int _score = 0;

	private final java.util.List<String> _missingFacts = new java.util.ArrayList<>();

	private final java.util.List<String> _incorrectClaims = new java.util.ArrayList<>();

	private final java.util.List<String> _scopeViolations = new java.util.ArrayList<>();

	private final java.util.List<String> _hallucinations = new java.util.ArrayList<>();

	private final java.util.List<String> _notFetched = new java.util.ArrayList<>();

	private final java.util.List<String> _formatViolations = new java.util.ArrayList<>();

	/**
	 * Creates a {@link JudgeResult} instance.
	 *
	 * @see com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult#create()
	 */
	protected JudgeResult() {
		super();
	}

	/**
	 * The score assigned by the AI judge (0-10).
	 */
	public final int getScore() {
		return _score;
	}

	/**
	 * @see #getScore()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setScore(int value) {
		internalSetScore(value);
		return this;
	}

	/** Internal setter for {@link #getScore()} without chain call utility. */
	protected final void internalSetScore(int value) {
		_score = value;
	}

	/**
	 * Facts that were expected but not included in the answer.
	 */
	public final java.util.List<String> getMissingFacts() {
		return _missingFacts;
	}

	/**
	 * @see #getMissingFacts()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setMissingFacts(
			java.util.List<? extends String> value) {
		internalSetMissingFacts(value);
		return this;
	}

	/** Internal setter for {@link #getMissingFacts()} without chain call utility. */
	protected final void internalSetMissingFacts(java.util.List<? extends String> value) {
		_missingFacts.clear();
		_missingFacts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMissingFacts()} list.
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult addMissingFact(String value) {
		internalAddMissingFact(value);
		return this;
	}

	/** Implementation of {@link #addMissingFact(String)} without chain call utility. */
	protected final void internalAddMissingFact(String value) {
		_missingFacts.add(value);
	}

	/**
	 * Removes a value from the {@link #getMissingFacts()} list.
	 */
	public final void removeMissingFact(String value) {
		_missingFacts.remove(value);
	}

	/**
	 * Claims in the answer that contradict the expected facts.
	 */
	public final java.util.List<String> getIncorrectClaims() {
		return _incorrectClaims;
	}

	/**
	 * @see #getIncorrectClaims()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setIncorrectClaims(
			java.util.List<? extends String> value) {
		internalSetIncorrectClaims(value);
		return this;
	}

	/** Internal setter for {@link #getIncorrectClaims()} without chain call utility. */
	protected final void internalSetIncorrectClaims(java.util.List<? extends String> value) {
		_incorrectClaims.clear();
		_incorrectClaims.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getIncorrectClaims()} list.
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult addIncorrectClaim(String value) {
		internalAddIncorrectClaim(value);
		return this;
	}

	/** Implementation of {@link #addIncorrectClaim(String)} without chain call utility. */
	protected final void internalAddIncorrectClaim(String value) {
		_incorrectClaims.add(value);
	}

	/**
	 * Removes a value from the {@link #getIncorrectClaims()} list.
	 */
	public final void removeIncorrectClaim(String value) {
		_incorrectClaims.remove(value);
	}

	/**
	 * Mentions of modules/types outside the allowed scope.
	 */
	public final java.util.List<String> getScopeViolations() {
		return _scopeViolations;
	}

	/**
	 * @see #getScopeViolations()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setScopeViolations(
			java.util.List<? extends String> value) {
		internalSetScopeViolations(value);
		return this;
	}

	/** Internal setter for {@link #getScopeViolations()} without chain call utility. */
	protected final void internalSetScopeViolations(java.util.List<? extends String> value) {
		_scopeViolations.clear();
		_scopeViolations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getScopeViolations()} list.
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult addScopeViolation(String value) {
		internalAddScopeViolation(value);
		return this;
	}

	/** Implementation of {@link #addScopeViolation(String)} without chain call utility. */
	protected final void internalAddScopeViolation(String value) {
		_scopeViolations.add(value);
	}

	/**
	 * Removes a value from the {@link #getScopeViolations()} list.
	 */
	public final void removeScopeViolation(String value) {
		_scopeViolations.remove(value);
	}

	/**
	 * Claims about types/attributes not supported by expected facts.
	 */
	public final java.util.List<String> getHallucinations() {
		return _hallucinations;
	}

	/**
	 * @see #getHallucinations()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setHallucinations(
			java.util.List<? extends String> value) {
		internalSetHallucinations(value);
		return this;
	}

	/** Internal setter for {@link #getHallucinations()} without chain call utility. */
	protected final void internalSetHallucinations(java.util.List<? extends String> value) {
		_hallucinations.clear();
		_hallucinations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getHallucinations()} list.
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult addHallucination(String value) {
		internalAddHallucination(value);
		return this;
	}

	/** Implementation of {@link #addHallucination(String)} without chain call utility. */
	protected final void internalAddHallucination(String value) {
		_hallucinations.add(value);
	}

	/**
	 * Removes a value from the {@link #getHallucinations()} list.
	 */
	public final void removeHallucination(String value) {
		_hallucinations.remove(value);
	}

	/**
	 * Claims that exist in available facts but weren't fetched with required tools. Examples: enum
	 * literals stated without calling listClassifiers, TLScript logic explained without calling
	 * listAnnotations.
	 */
	public final java.util.List<String> getNotFetched() {
		return _notFetched;
	}

	/**
	 * @see #getNotFetched()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setNotFetched(
			java.util.List<? extends String> value) {
		internalSetNotFetched(value);
		return this;
	}

	/** Internal setter for {@link #getNotFetched()} without chain call utility. */
	protected final void internalSetNotFetched(java.util.List<? extends String> value) {
		_notFetched.clear();
		_notFetched.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getNotFetched()} list.
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult addNotFetched(String value) {
		internalAddNotFetched(value);
		return this;
	}

	/** Implementation of {@link #addNotFetched(String)} without chain call utility. */
	protected final void internalAddNotFetched(String value) {
		_notFetched.add(value);
	}

	/**
	 * Removes a value from the {@link #getNotFetched()} list.
	 */
	public final void removeNotFetched(String value) {
		_notFetched.remove(value);
	}

	/**
	 * Formatting issues in the answer.
	 */
	public final java.util.List<String> getFormatViolations() {
		return _formatViolations;
	}

	/**
	 * @see #getFormatViolations()
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult setFormatViolations(
			java.util.List<? extends String> value) {
		internalSetFormatViolations(value);
		return this;
	}

	/** Internal setter for {@link #getFormatViolations()} without chain call utility. */
	protected final void internalSetFormatViolations(java.util.List<? extends String> value) {
		_formatViolations.clear();
		_formatViolations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getFormatViolations()} list.
	 */
	public com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult addFormatViolation(String value) {
		internalAddFormatViolation(value);
		return this;
	}

	/** Implementation of {@link #addFormatViolation(String)} without chain call utility. */
	protected final void internalAddFormatViolation(String value) {
		_formatViolations.add(value);
	}

	/**
	 * Removes a value from the {@link #getFormatViolations()} list.
	 */
	public final void removeFormatViolation(String value) {
		_formatViolations.remove(value);
	}

	@Override
	public String jsonType() {
		return JUDGE_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SCORE__PROP,
			MISSING_FACTS__PROP,
			INCORRECT_CLAIMS__PROP,
			SCOPE_VIOLATIONS__PROP,
			HALLUCINATIONS__PROP,
			NOT_FETCHED__PROP,
			FORMAT_VIOLATIONS__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList());
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case SCORE__PROP:
				return getScore();
			case MISSING_FACTS__PROP:
				return getMissingFacts();
			case INCORRECT_CLAIMS__PROP:
				return getIncorrectClaims();
			case SCOPE_VIOLATIONS__PROP:
				return getScopeViolations();
			case HALLUCINATIONS__PROP:
				return getHallucinations();
			case NOT_FETCHED__PROP:
				return getNotFetched();
			case FORMAT_VIOLATIONS__PROP:
				return getFormatViolations();
			default:
				return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SCORE__PROP:
				internalSetScore((int) value);
				break;
			case MISSING_FACTS__PROP:
				internalSetMissingFacts(de.haumacher.msgbuf.util.Conversions.asList(String.class, value));
				break;
			case INCORRECT_CLAIMS__PROP:
				internalSetIncorrectClaims(de.haumacher.msgbuf.util.Conversions.asList(String.class, value));
				break;
			case SCOPE_VIOLATIONS__PROP:
				internalSetScopeViolations(de.haumacher.msgbuf.util.Conversions.asList(String.class, value));
				break;
			case HALLUCINATIONS__PROP:
				internalSetHallucinations(de.haumacher.msgbuf.util.Conversions.asList(String.class, value));
				break;
			case NOT_FETCHED__PROP:
				internalSetNotFetched(de.haumacher.msgbuf.util.Conversions.asList(String.class, value));
				break;
			case FORMAT_VIOLATIONS__PROP:
				internalSetFormatViolations(de.haumacher.msgbuf.util.Conversions.asList(String.class, value));
				break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult readJudgeResult(
			de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult result =
			new com.top_logic.ai.demo.test.agent.judge.messages.JudgeResult();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SCORE__PROP);
		out.value(getScore());
		out.name(MISSING_FACTS__PROP);
		out.beginArray();
		for (String x : getMissingFacts()) {
			out.value(x);
		}
		out.endArray();
		out.name(INCORRECT_CLAIMS__PROP);
		out.beginArray();
		for (String x : getIncorrectClaims()) {
			out.value(x);
		}
		out.endArray();
		out.name(SCOPE_VIOLATIONS__PROP);
		out.beginArray();
		for (String x : getScopeViolations()) {
			out.value(x);
		}
		out.endArray();
		out.name(HALLUCINATIONS__PROP);
		out.beginArray();
		for (String x : getHallucinations()) {
			out.value(x);
		}
		out.endArray();
		out.name(NOT_FETCHED__PROP);
		out.beginArray();
		for (String x : getNotFetched()) {
			out.value(x);
		}
		out.endArray();
		out.name(FORMAT_VIOLATIONS__PROP);
		out.beginArray();
		for (String x : getFormatViolations()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SCORE__PROP:
				setScore(in.nextInt());
				break;
			case MISSING_FACTS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setMissingFacts(newValue);
			}
				break;
			case INCORRECT_CLAIMS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setIncorrectClaims(newValue);
			}
				break;
			case SCOPE_VIOLATIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setScopeViolations(newValue);
			}
				break;
			case HALLUCINATIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setHallucinations(newValue);
			}
				break;
			case NOT_FETCHED__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setNotFetched(newValue);
			}
				break;
			case FORMAT_VIOLATIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setFormatViolations(newValue);
			}
				break;
			default:
				super.readField(in, field);
		}
	}

}
