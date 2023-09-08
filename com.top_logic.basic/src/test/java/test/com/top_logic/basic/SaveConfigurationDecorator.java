/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;


/**
 * A {@link CustomConfigurationDecorator} which can be used if no special configuration is
 * necessary.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SaveConfigurationDecorator extends CustomConfigurationDecorator {
	
	public SaveConfigurationDecorator() {
		
	}

	@Override
	protected void installConfiguration() throws Exception {
		// does nothing. only saving of configuration is used
	}

}
