/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundRole;

/**
 * {@link RoleRule} to assign roles to a well-known item.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingletonRule extends RoleRule {

	private final TLObject _singleton;

	private final TLClass _singletonType;

	/**
	 * This constructor creates a new {@link SingletonRule}.
	 * 
	 * @param singleton
	 *        The singleton to assign roles to.
	 * @param aRole
	 *        See {@link #getRole()}.
	 * @param aPath
	 *        See {@link #getPath()}.
	 * @param aResourceKey
	 *        See {@link #getResourceKey()}.
	 */
	public SingletonRule(TLObject singleton, BoundRole aRole, List<PathElement> aPath, ResKey aResourceKey,
			String id) {
		super(aRole, aPath, aResourceKey, id);
		_singleton = singleton;
		_singletonType = getType(singleton);
	}

	private static TLClass getType(TLObject singleton) {
		return (TLClass) singleton.tType();
	}

	@Override
	public Type getType() {
		return Type.reference;
	}

	@Override
	public BoundRole getSourceRole() {
		return getRole();
	}

	@Override
	public boolean matches(TLObject itemWrapper) {
		if (_singleton.tValid()) {
			return itemWrapper == _singleton;
		}
		return false;
	}

	@Override
	public TLClass getMetaElement() {
		return _singletonType;
	}

	@Override
	public TLClass getSourceMetaElement() {
		return null;
	}

	@Override
	public boolean isInherit() {
		return false;
	}

}

