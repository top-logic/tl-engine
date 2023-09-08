/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;


import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.MultiProperties;

/**
 * {@link TestSetup} that installs a per test custom configuration from a
 * given <code>metaConf</code> file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomMetaConfSetup extends SimpleDecoratedTestSetup {

	/**
	 * Creates a {@link CustomMetaConfSetup}.
	 * 
	 * @param test
	 *        The test to wrap.
	 * @param metaConfResource
	 *        The file to read the {@link MultiProperties} from.
	 */
	public CustomMetaConfSetup(Test test, String metaConfResource) {
		super(new CustomMetaConfDecorator(metaConfResource), test);
	}
	
}