/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

/**
 * Graph structure that can associate an user object to each node and edge.
 *
 * @param <V>
 *        Type for the node user object.
 * @param <E>
 *        Type for the edge user object.
 * 
 * @see ExplicitGraph
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UserObjectGraph<V, E>
		extends ExplicitGraph<UserObjectGraph<V, E>.UserObjectNode, UserObjectGraph<V, E>.UserObjectEdge> {
	/**
	 * Node containing a user object of generic type V.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public class UserObjectNode extends ExplicitGraph<UserObjectNode, UserObjectEdge>.Node {
		private V _userObject;

		/**
		 * Creates an Node with no user object set.
		 */
		public UserObjectNode() {
			_userObject = null;
		}

		/**
		 * @param userObject
		 *        The user object associated with this {@link UserObjectNode}.
		 */
		public UserObjectNode(V userObject) {
			_userObject = userObject;
		}

		/**
		 * The user object associated with this {@link UserObjectNode}.
		 */
		public V getUserObject() {
			return _userObject;
		}
	}

	/**
	 * Edge containing a user object of generic type E.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public class UserObjectEdge extends ExplicitGraph<UserObjectNode, UserObjectEdge>.Edge {
		private E _userObject;

		/**
		 * @param source
		 *        Source {@link UserObjectNode} for this {@link UserObjectEdge}.
		 * @param target
		 *        Target {@link UserObjectNode} for this {@link UserObjectEdge}.
		 */
		protected UserObjectEdge(UserObjectGraph<V, E>.UserObjectNode source,
				UserObjectGraph<V, E>.UserObjectNode target) {
			super(source, target);
		}

		/**
		 * The user object associated with this {@link UserObjectEdge}.
		 */
		public E getUserObject() {
			return _userObject;
		}

		/**
		 * @param userObject
		 *        The user object associated with this {@link UserObjectEdge}.
		 */
		public void setUserObject(E userObject) {
			_userObject = userObject;
		}
	}

	/**
	 * @param userObject
	 *        The user object associated with this {@link UserObjectNode}.
	 * @return {@link UserObjectNode} containing the userObject.
	 */
	public UserObjectNode add(V userObject) {
		return add(new UserObjectNode(userObject));
	}

	/**
	 * @param source
	 *        Source {@link UserObjectNode}.
	 * @param target
	 *        Target {@link UserObjectNode}.
	 * @param userObject
	 *        The user object associated with this {@link UserObjectEdge}.
	 */
	public void connect(final UserObjectNode source, final UserObjectNode target, final E userObject) {
		UserObjectGraph<V, E>.UserObjectEdge edge = connect(source, target);
		edge.setUserObject(userObject);
	}

	@Override
	protected UserObjectEdge newEdge(UserObjectNode source, UserObjectNode target) {
		return new UserObjectEdge(source, target);
	}

	@Override
	public UserObjectNode newNode() {
		return new UserObjectNode();
	}

}
