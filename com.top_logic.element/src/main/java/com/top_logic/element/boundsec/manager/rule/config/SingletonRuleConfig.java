/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule.config;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link RoleRuleConfig} to assign roles to a singleton.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("singleton-rule")
@Label("Role rule for a singleton")
@DisplayInherited(DisplayStrategy.APPEND)
public interface SingletonRuleConfig extends RoleRuleConfig {

	/**
	 * The singleton to which the rule must be applied.
	 *
	 * @see TLModelUtil#resolveQualifiedName(String)
	 */
	@Mandatory
	String getTarget();

	@Hidden
	@Override
	String getMetaElement();

}
