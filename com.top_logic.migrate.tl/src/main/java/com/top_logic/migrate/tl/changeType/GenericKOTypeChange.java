/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.changeType;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link SimpleKOTypeChange} for all objects whose event matches a given
 * {@link GenericKOTypeChange.Matcher}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericKOTypeChange extends SimpleKOTypeChange {

	/**
	 * Description of the objects to rewrite.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Matcher {

		/**
		 * Checks whether the object with the given name and the given type moved to target system.
		 * 
		 * @param objectType
		 *        Type of the object to check. This is one of the given source types.
		 * @param objectName
		 *        Name of the object to check.
		 * @param creationValues
		 *        attributes of the object during creation type.
		 * 
		 * @return Whether the object needs the new target type.
		 */
		boolean matches(MetaObject objectType, Object objectName, Map<String, Object> creationValues);

	}

	/**
	 * {@link GenericKOTypeChange.Matcher} that matches all objects.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class TrueMatcher implements Matcher {

		/** Singleton {@link GenericKOTypeChange.TrueMatcher} instance. */
		public static final TrueMatcher INSTANCE = new TrueMatcher();

		private TrueMatcher() {
			// singleton instance
		}

		@Override
		public boolean matches(MetaObject objectType, Object objectName, Map<String, Object> creationValues) {
			return true;
		}

	}

	private final Matcher _matcher;

	/**
	 * Creates a new {@link GenericKOTypeChange}.
	 */
	public GenericKOTypeChange(Protocol log, Iterable<String> srcTypes, MetaObject targetType, boolean newTargetType,
			GenericKOTypeChange.Matcher matcher) {
		super(log, srcTypes, targetType, newTargetType);
		_matcher = matcher;
	}

	@Override
	protected boolean matches(MetaObject objectType, Object objectName, Map<String, Object> creationValues) {
		return _matcher.matches(objectType, objectName, creationValues);
	}

	/**
	 * Creates an {@link RewritingEventVisitor} that renames each elements of the source type to
	 * destination type.
	 */
	public static RewritingEventVisitor renameType(Protocol log, KnowledgeBase srcKB, String sourceType,
			KnowledgeBase destKB, String destType) {
		MetaObject srcMO = resolveTypeOrNull(srcKB, sourceType);
		if (srcMO == null) {
			throw new IllegalArgumentException("Unknown source type: " + sourceType);
		}
		Iterable<String> srcTypes = Collections.singletonList(sourceType);
		MetaObject targetType = resolveTypeOrNull(destKB, destType);
		if (targetType == null) {
			throw new IllegalArgumentException("Unknown target type: " + sourceType);
		}
		boolean newTargetType = resolveTypeOrNull(srcKB, destType) == null;
		return new GenericKOTypeChange(log, srcTypes, targetType, newTargetType, TrueMatcher.INSTANCE);
	}

	private static MetaObject resolveTypeOrNull(KnowledgeBase kb, String type) {
		try {
			return kb.getMORepository().getMetaObject(type);
		} catch (UnknownTypeException ex) {
			return null;
		}
	}

}
