/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import java.util.List;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLClassifierAnnotation;

/**
 * Definition of a {@link TLEnumeration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(EnumConfig.TAG_NAME)
public interface EnumConfig extends TypeConfig {

	/** Default tag for defining enumerations. */
	String TAG_NAME = "enum";

	/**
	 * The literals of the enumeration.
	 */
	@DefaultContainer
	@Key(ClassifierConfig.NAME)
	List<ClassifierConfig> getClassifiers();

	/**
	 * Definition of a {@link TLClassifier}.
	 */
	public interface ClassifierConfig extends PartAspect, AnnotatedConfig<TLClassifierAnnotation> {

		/** @see #isDefault() */
		String DEFAULT = "default";

		/**
		 * Whether this is the default classifier for the defining enumeration.
		 */
		@Name(DEFAULT)
		boolean isDefault();

		/** Sets the default property, {@link #isDefault()} */
		void setDefault(boolean isDefault);

	}

}
