/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;

/**
 * Annotation to say that the annotated {@link TLModelPart} is exported to other systems.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(TLExported.DEFAULT_TAG_NAME)
@DefaultStrategy(Strategy.NONE)
@InApp
public interface TLExported extends TLSynced {

	/** Default tag name of {@link TLExported}. */
	String DEFAULT_TAG_NAME = "exported";

}
