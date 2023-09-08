/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * Instances of this class represent a type of a {@link FilterConfiguration}
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class FilterConfigurationType {
	
	private Class<? extends FilterConfiguration> configurationRawType;
	private Class<? extends JSONTransformer> jsonTransformerType;
	
	/**
	 * Create a new FilterConfigurationType
	 * 
	 * @param configurationRawType - the raw type of the configuration
	 * @param jsonTransformerType - the JSON-Transformer type
	 */
	public FilterConfigurationType(Class<? extends FilterConfiguration> configurationRawType,
									  Class<? extends JSONTransformer> jsonTransformerType) {
		assert configurationRawType != null : "Configuration type must not be null!";
		
		this.configurationRawType = configurationRawType;
		this.jsonTransformerType = jsonTransformerType;
	}
	
	/**
	 * the raw configuration type.
	 */
	public Class<? extends FilterConfiguration> getConfigurationRawType() {
		return configurationRawType;
	}
	
	/**
	 * the json transformer type of the configuration, maybe null.
	 */
	public Class<? extends JSONTransformer> getJsonTransformerType() {
		return jsonTransformerType;
	}
}
