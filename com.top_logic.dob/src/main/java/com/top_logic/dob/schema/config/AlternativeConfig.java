/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import java.util.List;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.MOAlternative;

/**
 * Configuration of an {@link MOAlternative}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(MetaObjectsConfig.ALTERNATIVE_TAG_NAME)
public interface AlternativeConfig extends MetaObjectName {

	/**
	 * Configurations of the types which are a specialization of the {@link MOAlternative}.
	 * 
	 * @see MOAlternative#getSpecialisations()
	 */
	@Key(TypeChoice.NAME_ATTRIBUTE)
	@DefaultContainer
	List<TypeChoice> getSpecialisations();

	/**
	 * Reference to a potential alternative type of an {@link AlternativeConfig}.
	 */
	interface TypeChoice extends NamedConfigMandatory {
		// Pure marker interface.
	}

}

