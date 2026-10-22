/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.Presentation;

/**
 * How a page's current selection is stated to the agent.
 *
 * <p>
 * One line, the value of the per-turn {@code Selected object} fact that
 * {@code <chat-conversation><selection/>} contributes to the {@code <turn-context>} block:
 * </p>
 *
 * <pre>
 * Selected object: KI im Mittelstand 2026 [type:demo.events:Event] [id:GenericObject:12345]
 * </pre>
 *
 * <p>
 * <b>Three things, not one.</b> The label answers "which object have I selected?"; the qualified
 * type name says what kind of thing it is, which is what a model needs to reason about it and to
 * name it in a search; and the identifier is what makes it <em>actionable</em> - it is the same
 * {@code label [id:TID]} form every tool result writes, so a model that wants to read or change the
 * selected object feeds it straight back into the next tool call and
 * {@code com.top_logic.tl.ai.core.tools.model.TidResolver} reads it. A label alone would leave the
 * agent searching for the object the user is looking at.
 * </p>
 *
 * <p>
 * <b>Bounded.</b> A selection is whatever a table hands over, and "select all" hands over
 * everything: at most {@link #MAX_NAMED} objects are named and the rest is counted, so a stray
 * Ctrl+A cannot put ten thousand identifiers in front of the model.
 * </p>
 *
 * <p>
 * Nothing here is a {@link com.top_logic.basic.util.ResKey}. The text is read by the model, not by
 * the user, and what a run does must not depend on the UI language of whoever started it - see the
 * Coding Rules in {@code CLAUDE.md}. The <em>label</em> of the object is the application's own, and
 * therefore is localized, because it is the name the user sees on their screen.
 * </p>
 *
 * @see ChatConversationElement.Selection
 */
public final class ChatSelection {

	/** What a page calls the fact unless it says otherwise. Read by the model. */
	public static final String DEFAULT_NAME = "Selected object";

	/** What the fact says while the user has selected nothing. */
	public static final String NOTHING = "none";

	/** How many objects are named before the rest is only counted. */
	public static final int MAX_NAMED = 20;

	private ChatSelection() {
		// Static utility.
	}

	/**
	 * States what stands on a selection channel.
	 *
	 * @param selection
	 *        The channel's value: one object, a collection of them, a {@link Presentation} the
	 *        agent put there, or nothing.
	 * @param label
	 *        What to call one object; {@code null} for the application's own label.
	 * @return The value of the fact; never {@code null}, and never empty - a selection of nothing is
	 *         {@link #NOTHING}, which is a statement rather than a gap.
	 */
	public static String describe(Object selection, Function<Object, String> label) {
		List<Object> objects = selected(selection);
		if (objects.isEmpty()) {
			return NOTHING;
		}
		int named = Math.min(objects.size(), MAX_NAMED);
		StringBuilder out = new StringBuilder();
		if (selection instanceof Presentation shown && shown.as() != PresentAs.FORM) {
			// What the agent presented, still on screen: which objects, and that they are a table or
			// a chart rather than one object the user opened.
			out.append("a ").append(shown.as().name().toLowerCase()).append(" of ")
				.append(objects.size()).append(objects.size() == 1 ? " object: " : " objects: ");
		}
		for (int n = 0; n < named; n++) {
			if (n > 0) {
				out.append("; ");
			}
			out.append(one(objects.get(n), label));
		}
		if (objects.size() > named) {
			out.append("; and ").append(objects.size() - named).append(" more");
		}
		return out.toString();
	}

	/** The objects a channel value stands for, in order, without the empty ones. */
	private static List<Object> selected(Object selection) {
		if (selection == null) {
			return List.of();
		}
		if (selection instanceof Presentation shown) {
			return new ArrayList<>(shown.objects());
		}
		Collection<?> many = selection instanceof Collection<?> collection ? collection : List.of(selection);
		List<Object> objects = new ArrayList<>(many.size());
		for (Object object : many) {
			if (object == null || "".equals(object)) {
				continue;
			}
			objects.add(object);
		}
		return objects;
	}

	/** One object, as the model reads it. */
	private static String one(Object object, Function<Object, String> label) {
		String name = name(object, label);
		if (!(object instanceof TLObject model)) {
			// A channel may carry anything - a string, an enum, a value a host computed. It has no
			// type and no identifier to state, so what it says about itself is all there is.
			return name;
		}
		String type = typeName(model);
		return ReadFilter.withId(type == null ? name : name + " [type:" + type + "]",
			ReadFilter.idOf(model));
	}

	/** What to call the object: the configured expression, otherwise the application's own label. */
	private static String name(Object object, Function<Object, String> label) {
		try {
			String name = label == null ? MetaLabelProvider.INSTANCE.getLabel(object) : label.apply(object);
			return name == null ? "" : name.trim();
		} catch (RuntimeException ex) {
			// The same rule a <fact> follows: a selection the page cannot name costs the agent a
			// word, not the page. The identifier below is what the agent actually acts on.
			Logger.warn("The chat could not label a selected object: " + ex.getMessage(), ex,
				ChatSelection.class);
			return "";
		}
	}

	/**
	 * The qualified name of the object's type, or {@code null} when it has none.
	 *
	 * <p>
	 * The qualified name rather than the type's label, because this half is for the model: it is
	 * what a tool takes for a type and what the model definition calls the type, and unlike the
	 * label it is the same string in every language.
	 * </p>
	 */
	private static String typeName(TLObject object) {
		try {
			TLStructuredType type = object.tType();
			return type == null ? null : TLModelUtil.qualifiedName(type);
		} catch (RuntimeException ex) {
			Logger.debug("The chat could not name the type of a selected object.", ex, ChatSelection.class);
			return null;
		}
	}
}
