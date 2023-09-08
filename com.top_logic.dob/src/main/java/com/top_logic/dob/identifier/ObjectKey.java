/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.identifier;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.TypeContext;

/**
 * Identifier of an {@link IdentifiedObject}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ObjectKey {
	
	private static final char ID_SEP = ':';

	private static final char REV_SEP = '@';

	private static final char LEGACY_REV_SEP = '-';

	private static final char BRANCH_SEP = '#';

	private static final int TYPE_GROUP = 1;

	private static final int LEGACY_ID_GROUP = 2;

	private static final int ID_GROUP = 3;

	private static final int BRANCH_GROUP = 4;

	private static final int REV_GROUP = 5;

	private static final Pattern KEY_PATTERN = Pattern.compile(
		"([^" + ID_SEP + "]+)" +
			ID_SEP +
			"(?:" +
				"(?:ID\\(" + "([^\\)]+)" + "\\)" + ")" + 
				"|" +
				"([^" + LEGACY_REV_SEP + BRANCH_SEP + REV_SEP + "]+)" + 
			")" +
			"(?:" + BRANCH_SEP + "([^" + LEGACY_REV_SEP + REV_SEP + "]+)" + ")?" +
			"(?:" + "(?:" + REV_SEP + "|" + LEGACY_REV_SEP + ")" + "(.+)" + ")?");

	/**
	 * {@link Mapping} to the {@link #getBranchContext()} of the argument key.
	 */
	public static final Mapping<Object, Long> GET_BRANCH = new Mapping<>() {
		@Override
		public Long map(Object key) {
			return Long.valueOf(((ObjectKey) key).getBranchContext());
		}
	
		@Override
		public String toString() {
			return getClass().getName() + "[ObjectKey->branch]";
		}
	};

	/**
	 * {@link Mapping} to the {@link #getObjectType() type name} of the argument key.
	 */
	public static final Mapping<Object, String> GET_TYPE_NAME = new Mapping<>() {
		@Override
		public String map(Object key) {
			return ((ObjectKey) key).getObjectType().getName();
		}
	
		@Override
		public String toString() {
			return getClass().getName() + "[ObjectKey->typeName]";
		}
	};

	/**
	 * {@link Mapping} to the {@link #getObjectName()} of the argument key.
	 */
	public static final Mapping<Object, Object> GET_OBJECT_NAME_MAPPING = new Mapping<>() {
		@Override
		public Object map(Object key) {
			return key == null ? null : ((ObjectKey) key).getObjectName();
		}

		@Override
		public String toString() {
			return getClass().getName() + "[ObjectKey->identifier]";
		}
	};

	/**
	 * {@link Mapping} to the {@link #getHistoryContext()} of the argument key.
	 */
	public static final Mapping<Object, Long> GET_HISTORY_CONTEXT = new Mapping<>() {
		@Override
		public Long map(Object key) {
			return Long.valueOf(((ObjectKey) key).getHistoryContext());
		}
	
		@Override
		public String toString() {
			return getClass().getName() + "[ObjectKey->historyContext]";
		}
	};

	/**
	 * Returns the ID of the branch of the represented object.
	 */
	public abstract long getBranchContext();

	/**
	 * Returns the revision of the represented object.
	 */
	public abstract long getHistoryContext();

	/**
	 * Returns the name of the represented object.
	 */
	public abstract TLID getObjectName();

	/**
	 * Returns the object type of the represented object.
	 */
	public abstract MetaObject getObjectType();
	
	@Override
	public final boolean equals(Object other) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
		return equalsObjectKey(this, other);
	}
	
	@Override
	public final int hashCode() {
		return hashCodeObjectKey(this);
	}
	
	/**
	 * A string representation of this identifier.
	 */
	public String asString() {
		return ObjectKey.toStringObjectKey(this);
	}

	/**
	 * External form of this identifier as string.
	 */
	@Override
	public final String toString() {
		return asString();
	}

	static boolean equalsObjectKey(ObjectKey self, Object other) {
		if (other == self) {
			return true;
		}
		
		if (! (other instanceof ObjectKey)) {
			return false;
		}

		ObjectKey otherKey = (ObjectKey) other;
		
		boolean result = 
			self.getObjectType().equals(otherKey.getObjectType()) && 
			self.getObjectName().equals(otherKey.getObjectName()) && 
				self.getBranchContext() == otherKey.getBranchContext() &&
				self.getHistoryContext() == otherKey.getHistoryContext();

		return result;
	}
	
	static int hashCodeObjectKey(ObjectKey self) {
		int result = self.getObjectType().hashCode();
		result += 16661 * self.getObjectName().hashCode();
		result += 44641 * self.getBranchContext();
		result += 75079 * self.getHistoryContext();
		return result;
	}

	static String toStringObjectKey(ObjectKey self) {
		StringBuilder toString = new StringBuilder();
		toString.append(self.getObjectType().getName().toString());
		toString.append(ID_SEP);
		try {
			self.getObjectName().appendExternalForm(toString);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		long branch = self.getBranchContext();
		if (branch != 1L) {
			toString.append(BRANCH_SEP);
			toString.append(branch);
		}
		long rev = self.getHistoryContext();
		if (rev != Long.MAX_VALUE) {
			toString.append(REV_SEP);
			toString.append(rev);
		}
		return toString.toString();
	}

	/**
	 * Creates an {@link ObjectKey} from its string representation (see {@link #asString()}).
	 * 
	 * @param typeSystem
	 *        The {@link TypeContext} to resolve type references in.
	 * @param key
	 *        The string representation of the key.
	 * @return The parsed {@link ObjectKey}.
	 * @throws UnknownTypeException
	 *         If a type encoded in the key's string representation is not present in the given
	 *         {@link TypeContext}.
	 * @throws IllegalArgumentException
	 *         If the given key is has invalid syntax.
	 */
	public static ObjectKey fromStringObjectKey(TypeContext typeSystem, String key)
			throws UnknownTypeException, IllegalArgumentException {
		Matcher matcher = KEY_PATTERN.matcher(key);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid object identifier '" + key +  "', expecting format '" + KEY_PATTERN +  ".");
		}
		
		MetaObject objectType = typeSystem.getType(matcher.group(TYPE_GROUP));
		
		try {
			TLID objectName =
				IdentifierUtil.fromExternalForm(oneOf(matcher.group(LEGACY_ID_GROUP), matcher.group(ID_GROUP)));

			long branchContext;
			String branchId = matcher.group(BRANCH_GROUP);
			if (branchId == null) {
				branchContext = 1L;
			} else {
				branchContext = Long.parseLong(branchId);
			}
			
			long historyContext;
			String revId = matcher.group(REV_GROUP);
			if (revId == null) {
				historyContext = Long.MAX_VALUE;
			} else {
				historyContext = Long.parseLong(revId);
			}

			return new DefaultObjectKey(branchContext, historyContext, objectType, objectName);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid object identifier: " + key, ex);
		}
	}

	private static String oneOf(String s1, String s2) {
		return (s1 == null) ? s2 : s1;
	}
}

