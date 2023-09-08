/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.structured.StructuredElement;

/**
 * {@link MetaElementFactory} that is able to handle {@link StructuredElement} as
 * {@link MetaElementHolder} and object.
 * 
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class KBBasedSEMetaElementFactory extends KBBasedMetaElementFactory {
    
	/**
	 * Creates a {@link KBBasedSEMetaElementFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public KBBasedSEMetaElementFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

}
