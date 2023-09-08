/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.schema;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLScope;

/**
 * Utilities for accessing {@link TLClass} locations in the
 * {@link ElementSchemaConstants element schema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class HolderType {

	/** 
	 * This object is the holder 
	 */
	public static final String THIS = "this";

	/** 
	 * Parent object is the holder 
	 */
	public static final String PARENT = "parent";

	/**
	 * No holder, type is defined globally (must have a unique type name.
	 */
	public static final String GLOBAL = "global";

	/**
	 * Whether the given holder type is {@link HolderType#GLOBAL}.
	 */
	public static boolean isGlobal(String aHolderType) {
		return aHolderType.equals(HolderType.GLOBAL);
	}

	/**
	 * Whether the given holder type is {@link HolderType#THIS}.
	 */
	public static boolean isLocal(String aHolderType) {
		return aHolderType.equals(HolderType.THIS);
	}

	/**
	 * Whether the given holder type is {@link HolderType#PARENT}.
	 */
	public static boolean isParent(String aHolderType) {
		return aHolderType.equals(HolderType.PARENT);
	}
	
	/**
	 * Whether the given holder type matches the given element type, if the given holder type not
	 * {@link #isGlobal(String)} and not {@link #isLocal(String)} and not {@link #isParent(String)}.
	 */
	public static boolean isHolder(String aHolderType, String elementType) {
		return elementType.equals(aHolderType);
	}

	/**
	 * Evaluate the scope reference to find the specified type scope.
	 * 
	 * @param scopeBase
	 *        The start object relative to which the scope reference is evaluated.
	 * @param scopeRef
	 *        The description of the scope.
	 * @return The type scope.
	 */
	public static TLScope findScope(Object scopeBase, String scopeRef)
				throws IllegalArgumentException {
	
		if (isGlobal(scopeRef)) {
			return null;
		}
		else if (isLocal(scopeRef)) {
			if (scopeBase == null) {
				// As long as the module scope is encoded as null, the this reference in module
				// scope must return this scope.
//				throw new IllegalArgumentException("No scope base in lookup of a local scope.");
				return null;
			}
			if (!(scopeBase instanceof TLScope)) {
				throw new IllegalArgumentException("Selected object '" + scopeBase + "' is not a scope.");
			}
			return (TLScope) scopeBase;
		}
		else if (isParent(scopeRef)) {
			if (!hasParent(scopeBase)) {
				throw new IllegalArgumentException("No structure node '" + scopeBase + "' in to resolve parent scope.");
			}
			Object parentScope = getParent(scopeBase);

			if (!(parentScope instanceof TLScope)) {
				throw new IllegalArgumentException("No scope '" + parentScope + "' in parent reference.");
			}
			return (TLScope) parentScope;
		}
		else {
			if (scopeBase == null) {
				throw new IllegalArgumentException("No scope base in search for type scope '" + scopeRef + "'.");
			}
			if (!hasParent(scopeBase)) {
				throw new IllegalArgumentException(
					"No structure node '" + scopeBase + "' in search for type scope '" + scopeRef + "'.");
			}

			Object scope = scopeBase;
			while (scope != null) {
				if (hasType(scope, scopeRef)) {
					return (TLScope) scope;
				}

				scope = getParent(scope);
			}

			throw new IllegalArgumentException("No object of type '" + scopeRef + "' in scope.");
		}
	}

	private static class ParentScopeBase {

		private final StructuredElement _parent;

		public ParentScopeBase(StructuredElement parent) {
			_parent = parent;
		}

		public StructuredElement getParent() {
			return _parent;
		}

	}

	/**
	 * A stub object that only has a parent scope.
	 */
	public static Object parentScopeBase(StructuredElement parent) {
		if (parent == null) {
			return null;
		}
		return new ParentScopeBase(parent);
	}

	private static boolean hasType(Object scope, String scopeRef) {
		if (!(scope instanceof StructuredElement)) {
			return false;
		}
		return isHolder(scopeRef, ((StructuredElement) scope).getElementType());
	}

	private static boolean hasParent(Object scopeBase) {
		return scopeBase instanceof StructuredElement || scopeBase instanceof ParentScopeBase;
	}

	private static Object getParent(Object scopeBase) {
		if (scopeBase instanceof StructuredElement) {
			return ((StructuredElement) scopeBase).getParent();
		}
		return ((ParentScopeBase) scopeBase).getParent();
	}

}
