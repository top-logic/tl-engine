/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule;

import java.util.List;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.tool.boundsec.BoundRole;

/**
 * {@link RoleRule} applying rules to all elements of a given {@link TLClass}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultRoleRule extends RoleRule {

	/** The meta element type of objects the role applies to */
	private TLClass metaElement;

	/** Indicates that the role is to be applied to sub types too */
	private boolean inherit;

	/** the type of the rule */
	private Type type;

	/** The meta element type of objects an inheritance rule may inherit from */
	private TLClass sourceMetaElement;

	/** the role to request in case of an inheritance rule that maps one role to another one */
	private BoundRole sourceRole;

	/**
	 * Creates a new {@link DefaultRoleRule}.
	 * 
	 * @param aME
	 *        See {@link #getMetaElement()}.
	 * @param isInherit
	 *        See {@link #isInherit()}.
	 * @param aRole
	 *        See {@link #getRole()}.
	 * @param aPath
	 *        See {@link #getPath()}.
	 * @param aResourceKey
	 *        See {@link #getResourceKey()}.
	 */
	public DefaultRoleRule(TLClass aME, TLClass aSourceME, boolean isInherit, BoundRole aRole, BoundRole aSourceRole,
			List<PathElement> aPath, Type aType, ResKey aResourceKey, String id) {
		super(aRole, aPath, aResourceKey, id);
		this.metaElement = Objects.requireNonNull(aME);
		this.sourceMetaElement = aSourceME;
		this.inherit = isInherit;
		this.sourceRole = aSourceRole;
		this.type = aType;
	}

	@Override
	public boolean matches(TLObject itemWrapper) {
		if (!itemWrapper.tValid())
			return false;

		TLStructuredType itemType = itemWrapper.tType();
		if (itemType == null) {
			return false;
		}
		if (this.inherit) {
			return MetaElementUtil.hasGeneralization(itemType, getMetaElement());
		} else {
			return getMetaElement().equals(itemType);
		}
	}

	@Override
	public TLClass getMetaElement() {
		return (metaElement);
	}

	@Override
	public TLClass getSourceMetaElement() {
		return (sourceMetaElement);
	}

	@Override
	public boolean isInherit() {
		return (inherit);
	}

	@Override
	public BoundRole getSourceRole() {
		return this.sourceRole == null ? getRole() : this.sourceRole;
	}

	@Override
	public Type getType() {
		return (this.type);
	}

}

