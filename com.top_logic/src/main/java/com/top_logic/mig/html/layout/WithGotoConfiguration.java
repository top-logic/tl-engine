/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TypeRefMandatory;

/**
 * Configuration of local Goto targets.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface WithGotoConfiguration extends ConfigurationItem {

	/**
	 * Configuration for local GOTO targets.
	 * 
	 * <p>
	 * The values are pairs of {@link TLType} name's and {@link ComponentName}s. When an object of
	 * the type (or a specialization) is displayed, the configured target component is used as goto
	 * target by default.
	 * </p>
	 */
	@Key(GotoTarget.TYPE_SPEC)
	Map<String, GotoTarget> getGotoTargets();

	/**
	 * Configuration of a local goto target.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GotoTarget extends TypeRefMandatory {

		/** Configuration name of the value {@link #getComponent()}. */
		String COMPONENT = "component";

		/**
		 * Full qualified name of the {@link TLType} for which {@link #getComponent()} must be used
		 * as GOTO target.
		 */
		@Override
		String getTypeSpec();

		/**
		 * Name of the component that is used as GOTO target for objects of type
		 * {@link #getTypeSpec()}.
		 */
		@Mandatory
		@Name(COMPONENT)
		ComponentName getComponent();
	}

}

