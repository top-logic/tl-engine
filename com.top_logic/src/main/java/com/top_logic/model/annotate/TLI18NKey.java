/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * Annotation of a resource key under which a localized name of the annotated
 * model element is stored.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("i18n-key")
@DefaultStrategy(Strategy.NONE)
public interface TLI18NKey extends TLAttributeAnnotation, TLModuleAnnotation, TLClassifierAnnotation {

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

	/**
	 * The resource key that identifies the user visible name of the annotated model element.
	 */
	@Name(VALUE)
	@Mandatory
	ResKey getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(ResKey value);

}
