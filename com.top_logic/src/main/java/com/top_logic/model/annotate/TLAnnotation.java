/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * Base class for {@link TLAnnotation}s.
 * 
 * <p>
 * Note: Concrete annotation classes must be final.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface TLAnnotation extends ConfigPart {

	@Override
	Class<? extends TLAnnotation> getConfigurationInterface();

	/**
	 * @see #getAnnotated()
	 */
	String ANNOTATED = "annotated";

	/**
	 * The annotated configuration.
	 */
	@Name(ANNOTATED)
	@Hidden
	@Container
	AnnotatedConfig<?> getAnnotated();

}
