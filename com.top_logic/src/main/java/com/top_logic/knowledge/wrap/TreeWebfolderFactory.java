/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Arrays;
import java.util.Collection;

import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link WebFolderFactory} whose allowed folder types are configured, defaulting to
 * {@link #STANDARD_FOLDER} only.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TreeWebfolderFactory extends WebFolderFactory {
    
	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link TreeWebfolderFactory}.
	 */
	public TreeWebfolderFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
	 * Collection with allowed folder types.
	 * 
	 * @see com.top_logic.knowledge.wrap.WebFolderFactory#createAllowedFolderTypes(Config)
	 */
    @Override
	protected Collection<String> createAllowedFolderTypes(Config config) {
    	if(config.getFolderTypes().isEmpty()) {
			return Arrays.asList(STANDARD_FOLDER);
    	} else {
        	return config.getFolderTypes();	
    	}
    }

}
