/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;

/**
 * Annotation saying the annotated {@link TLModelPart} is an imported part.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(TLImported.DEFAULT_TAG_NAME)
@DefaultStrategy(Strategy.NONE)
@InApp
public interface TLImported extends TLSynced {

	/** Default tag name of {@link TLImported}. */
	String DEFAULT_TAG_NAME = "imported";

	/** @see #getSource() */
	String VALUE = "source";

	/**
	 * Value of this annotation.
	 */
	@Name(VALUE)
	String getSource();

	/**
	 * Setter of {@link #getSource()}
	 */
	void setSource(String value);

}

