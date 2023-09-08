/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

/**
 * An event informing about a change in an {@link ObjectScope}.
 * 
 * @see ScopeListener#handleObjectScopeEvent(ScopeEvent)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ScopeEvent {

	/**
	 * A classification of a {@link ObjectScope} change.
	 */
	public enum Kind {

		/**
		 * Preparing phase.
		 */
		PREPARE,

		/**
		 * Post processing phase.
		 */
		POST_PROCESS,

		/**
		 * An object was created.
		 */
		CREATE,

		/**
		 * An object was deleted.
		 */
		DELETE,

		/**
		 * An object was internally modified (some property of the object has changed).
		 */
		UPDATE;
	}

	private final ObjectScope _scope;

	private final Object _obj;

	/**
	 * Creates a {@link ScopeEvent}.
	 *
	 * @param scope
	 *        See {@link #getScope()}.
	 * @param obj
	 *        See {@link #getObj()}
	 */
	public ScopeEvent(ObjectScope scope, Object obj) {
		_scope = scope;
		_obj = obj;
	}

	/**
	 * The {@link ObjectScope} that was changed.
	 */
	public ObjectScope getScope() {
		return _scope;
	}

	/**
	 * The {@link Object} that was modified.
	 */
	public Object getObj() {
		return _obj;
	}

	/**
	 * A classification of this event.
	 */
	public abstract Kind getKind();

	@Override
	public String toString() {
		return getKind().name() + "(" + getObj() + ")";
	}

	/**
	 * {@link ScopeEvent} informing about the preparing phase.
	 */
	public static class Prepare extends ScopeEvent {

		/**
		 * Creates a {@link Prepare}.
		 *
		 * @param scope
		 *        See {@link #getScope()}.
		 */
		public Prepare(ObjectScope scope) {
			super(scope, null);
		}

		@Override
		public Kind getKind() {
			return Kind.PREPARE;
		}

	}

	/**
	 * {@link ScopeEvent} informing about the post processing.
	 */
	public static class PostProcess extends ScopeEvent {

		/**
		 * Creates a {@link PostProcess}.
		 *
		 * @param scope
		 *        See {@link #getScope()}.
		 */
		public PostProcess(ObjectScope scope) {
			super(scope, null);
		}

		@Override
		public Kind getKind() {
			return Kind.POST_PROCESS;
		}

	}

	/**
	 * {@link ScopeEvent} informing about an object creation.
	 */
	public static class Create extends ScopeEvent {

		/**
		 * Creates a {@link Create}.
		 *
		 * @param scope
		 *        See {@link #getScope()}.
		 * @param obj
		 *        See {@link #getObj()}.
		 */
		public Create(ObjectScope scope, Object obj) {
			super(scope, obj);
		}

		@Override
		public Kind getKind() {
			return Kind.CREATE;
		}

	}

	/**
	 * {@link ScopeEvent} informing about an object deletion.
	 */
	public static class Delete extends ScopeEvent {

		/**
		 * Creates a {@link Delete}.
		 *
		 * @param scope
		 *        See {@link #getScope()}.
		 * @param obj
		 *        See {@link #getObj()}.
		 */
		public Delete(ObjectScope scope, Object obj) {
			super(scope, obj);
		}

		@Override
		public Kind getKind() {
			return Kind.DELETE;
		}

	}

	/**
	 * {@link ScopeEvent} informing about an internal object modification.
	 */
	public static class Update extends ScopeEvent {

		private final String _property;

		/**
		 * Creates a {@link Update}.
		 *
		 * @param scope
		 *        See {@link #getScope()}.
		 * @param obj
		 *        See {@link #getObj()}.
		 * @param property
		 *        See {@link #getProperty()}.
		 */
		public Update(ObjectScope scope, Object obj, String property) {
			super(scope, obj);
			_property = property;
		}

		/**
		 * The property of the object that was modified.
		 * 
		 * @see ObjectData#getData(String)
		 */
		public String getProperty() {
			return _property;
		}

		@Override
		public Kind getKind() {
			return Kind.UPDATE;
		}

		@Override
		public String toString() {
			return getKind().name() + "(" + getObj() + "." + _property + ")";
		}

	}

}
