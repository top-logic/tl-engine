/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;

/**
 * Says which kind of wrong identifier a model passed, and what to use instead.
 *
 * <p>
 * {@code "Invalid TID format. Expected 'TypeName:id'"} is true of every wrong identifier and
 * helps with none of them, because the mistakes a model actually makes fall into a few recognisable
 * classes and each has a different remedy: it copies a {@code uuid} attribute it saw in a read answer, it
 * uses the human-facing code it was told to use in the conversation, it pastes the {@code toString}
 * of an object out of a log line, or it writes a type name that no table has. A message that names
 * the class ends the retry loop in one step; the generic one makes the model try the same string
 * again in a different tool.
 * </p>
 *
 * <p>
 * <b>Only on the error path.</b> Nothing here runs while an identifier resolves. {@link TidResolver}
 * asks for a diagnosis when it is already about to throw, so the classification - a handful of
 * regular expressions and one lookup in the type repository's map - is paid for by a call that has
 * failed anyway.
 * </p>
 *
 * <h2>What it may not say</h2>
 *
 * <p>
 * An object the principal may not read and an object that does not exist get the <em>same</em>
 * sentence, down to the byte. The diagnosis therefore never asks whether an object exists: it
 * classifies the string and asks the type repository about the type name, and the caller feeds both
 * cases through the same call. See {@link TidResolver#resolve(String,
 * com.top_logic.tl.ai.core.security.ModelSecurity)}.
 * </p>
 *
 * @see ObjectIdConfig The application's own hints, which come first.
 */
public final class TidDiagnostics {

	/** A uuid in the usual 8-4-4-4-12 hex grouping. */
	private static final Pattern UUID =
		Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

	/**
	 * An object written the way {@code toString} writes one: {@code Person(Karl Meier)}.
	 *
	 * <p>
	 * {@link Pattern#DOTALL} because what was pasted may carry a line break - a log line with the
	 * object's attributes under it is a printed object too.
	 * </p>
	 */
	private static final Pattern PRINTED_OBJECT =
		Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_.$]*\\s*\\(.*", Pattern.DOTALL);

	/** Stands in for a configured pattern that does not compile; matches nothing. */
	private static final Pattern NEVER = Pattern.compile("(?!)");

	/** Configured patterns, compiled once each. */
	private static final Map<String, Pattern> COMPILED = new ConcurrentHashMap<>();

	/** How much of an identifier is quoted back before it is cut off. */
	private static final int QUOTED_LENGTH = 80;

	/** What the framework wants instead, for a string that is recognisably something else. */
	private static final String USE_THE_ONE_PRINTED =
		"Use the id a tool answer printed for that object, which reads 'TypeName:id'.";

	/** What the framework wants, for a string that resembles nothing in particular. */
	private static final String THE_FORM = "An object id reads 'TypeName:id' and comes from a tool answer.";

	private TidDiagnostics() {
		// Static utility.
	}

	/**
	 * What is known about the type name in front of the colon of an identifier.
	 *
	 * @see Types
	 */
	public enum TypeState {

		/** Nobody was asked - there is no type repository at hand. */
		UNDECIDED,

		/** No type of that name exists. */
		UNKNOWN,

		/** The type exists but is abstract, so nothing is ever stored under it. */
		ABSTRACT,

		/** The type exists and has instances. */
		INSTANTIABLE;
	}

	/**
	 * What the diagnosis may ask about a type name.
	 *
	 * <p>
	 * A seam rather than a {@code KnowledgeBase} parameter, so that the classification can be tested
	 * for every type state without booting a persistency layer, and so that a caller with no
	 * repository at hand can answer {@link TypeState#UNDECIDED} instead of passing {@code null}.
	 * </p>
	 */
	@FunctionalInterface
	public interface Types {

		/** Answers {@link TypeState#UNDECIDED} for every type name. */
		Types NONE = typeName -> TypeState.UNDECIDED;

		/**
		 * What is known about the type of that name.
		 *
		 * @param typeName
		 *        The part in front of the colon; never <code>null</code>.
		 * @return Never <code>null</code>.
		 */
		TypeState stateOf(String typeName);
	}

	/**
	 * The sentence a model gets when the identifier it passed cannot be resolved.
	 *
	 * @param identifier
	 *        What the model named, as {@link TidResolver#objectId(String)} extracted it. May be
	 *        <code>null</code> or empty.
	 * @param types
	 *        What may be asked about a type name; {@link Types#NONE} when nothing can be.
	 * @return The whole message, ending in a full stop; never <code>null</code>.
	 */
	public static String explain(String identifier, Types types) {
		String id = identifier == null ? "" : identifier.trim();
		if (id.isEmpty()) {
			return "An object id is required. " + THE_FORM;
		}

		String contributed = hint(id);
		if (contributed != null) {
			return quote(id) + " " + contributed;
		}
		if (UUID.matcher(id).matches()) {
			return quote(id) + " is a uuid, not an object id. " + USE_THE_ONE_PRINTED;
		}
		if (PRINTED_OBJECT.matcher(id).matches()) {
			return quote(id) + " is a printed object, not an object id. " + USE_THE_ONE_PRINTED;
		}

		int colon = id.indexOf(':');
		if (colon < 0) {
			return quote(id) + " is not an object id. " + THE_FORM;
		}
		return explainPair(id, id.substring(0, colon), stripQualifiers(id.substring(colon + 1)), types);
	}

	/**
	 * The sentence for an identifier that has the {@code TypeName:id} shape but still failed.
	 *
	 * @param id
	 *        The whole identifier, for quoting.
	 * @param typeName
	 *        The part in front of the colon.
	 * @param objectName
	 *        The part behind it, without the {@code #branch} / {@code @revision} qualifiers.
	 */
	private static String explainPair(String id, String typeName, String objectName, Types types) {
		if (typeName.isEmpty()) {
			return quote(id) + " has no type in front of the colon. " + THE_FORM;
		}
		switch (types.stateOf(typeName)) {
			case UNKNOWN:
				return "No object type is called " + quote(typeName) + ", so " + quote(id) + " names nothing."
					+ " Copy an id from a tool answer without changing it - the part in front of the colon"
					+ " is the type the answer printed, which is not always the type you have in mind.";
			case ABSTRACT:
				return "The type " + quote(typeName) + " is abstract and has no objects of its own, so "
					+ quote(id) + " names nothing. Copy an id from a tool answer without changing it.";
			case INSTANTIABLE:
			case UNDECIDED:
				break;
		}
		if (!isObjectName(objectName)) {
			return "The part behind the colon of " + quote(id) + " is not an id."
				+ " Copy an id from a tool answer without changing it.";
		}
		return "Object " + quote(id) + " not found. It does not exist, or you may not read it."
			+ " Take a current id from a tool answer.";
	}

	/**
	 * Whether the engine's identifier parser accepts the part behind the colon.
	 *
	 * <p>
	 * Asked rather than pattern-matched, because what an identifier looks like is the engine's
	 * decision: with short ids it is a number, without them it is a string.
	 * </p>
	 */
	private static boolean isObjectName(String objectName) {
		if (objectName.isEmpty()) {
			return false;
		}
		try {
			return IdentifierUtil.fromExternalForm(objectName) != null;
		} catch (RuntimeException ex) {
			return false;
		}
	}

	/**
	 * What the application says about this identifier, or <code>null</code> if it says nothing.
	 *
	 * @see ObjectIdConfig#getHints()
	 */
	private static String hint(String id) {
		for (ObjectIdConfig.HintConfig hint : ObjectIdSettings.hints()) {
			String explanation = hint.getExplanation();
			if (explanation == null || explanation.isBlank()) {
				continue;
			}
			if (pattern(hint.getPattern()).matcher(id).matches()) {
				return explanation.trim();
			}
		}
		return null;
	}

	/**
	 * The configured pattern, compiled once; one that does not compile is reported and then matches
	 * nothing.
	 */
	private static Pattern pattern(String regexp) {
		if (regexp == null || regexp.isEmpty()) {
			return NEVER;
		}
		return COMPILED.computeIfAbsent(regexp, source -> {
			try {
				return Pattern.compile(source);
			} catch (PatternSyntaxException ex) {
				Logger.error("Ignoring the object-id hint '" + source + "': it is no regular expression.",
					ex, TidDiagnostics.class);
				return NEVER;
			}
		});
	}

	/**
	 * The identifier as the message shows it: in quotes, and cut off if the model passed something
	 * long enough to be a document.
	 */
	private static String quote(String id) {
		String shown = id.length() <= QUOTED_LENGTH ? id : id.substring(0, QUOTED_LENGTH) + "...";
		return "'" + shown + "'";
	}

	/**
	 * Cuts off the {@code #branch} / {@code @revision} suffixes of an object key's external form.
	 *
	 * @see TidResolver
	 */
	static String stripQualifiers(String id) {
		int end = id.length();
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (c == '#' || c == '@') {
				end = i;
				break;
			}
		}
		return id.substring(0, end);
	}
}
