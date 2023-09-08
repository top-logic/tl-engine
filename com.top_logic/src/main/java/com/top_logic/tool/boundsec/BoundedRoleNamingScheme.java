/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseName;
import com.top_logic.knowledge.wrap.util.WrapperUtil;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.NamedModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.tool.boundsec.BoundedRoleNamingScheme.BoundedRoleName;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * A {@link ModelNamingScheme} for {@link BoundedRole}, which uses {@link NamedModelName} as
 * {@link ModelName}.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class BoundedRoleNamingScheme extends AbstractModelNamingScheme<BoundedRole, BoundedRoleName> {

	/**
	 * {@link ModelName} of a {@link BoundedRoleNamingScheme}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface BoundedRoleName extends NamedModelName, KnowledgeBaseName {

		// sum interface
	}

	@Override
	public Class<BoundedRoleName> getNameClass() {
		return BoundedRoleName.class;
	}

	@Override
	public Class<BoundedRole> getModelClass() {
		return BoundedRole.class;
	}

	@Override
	public BoundedRole locateModel(ActionContext context, BoundedRoleName name) {
		{
			KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getKnowledgeBase(name.getKnowledgeBase());
			return BoundedRole.getRoleByName(kb, name.getName());
		}
	}

	@Override
	protected void initName(BoundedRoleName name, BoundedRole model) {
		name.setName(model.getName());
		name.setKnowledgeBase(WrapperUtil.getKnowledgeBase(model).getName());
	}

}
