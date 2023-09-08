/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.MultiProperties;
import com.top_logic.basic.XMLProperties;

/**
 * {@link TestSetupDecorator} that installs a custom configuration from a given
 * <code>metaConf</code> file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CustomMetaConfDecorator extends CustomConfigurationDecorator {

	private String metaConfResource;

	/**
	 * Creates a {@link CustomMetaConfDecorator}.
	 * 
	 * @param metaConfResource
	 *        The file to read the {@link MultiProperties} from.
	 */
	public CustomMetaConfDecorator(String metaConfResource) {
		this.metaConfResource = metaConfResource;
	}
	
	@Override
	protected void installConfiguration() throws Exception {
		XMLProperties.startWithMetaConf(metaConfResource);
	}
	

}

