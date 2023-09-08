/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.MultiProperties;
import com.top_logic.basic.XMLProperties;

/**
 * {@link CustomConfigurationDecorator} installing a custom configuration based of a file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FileBasedPropertiesDecorator extends CustomConfigurationDecorator {

	private final String _fileName;

	private final boolean _withDefaultConfig;

	/**
	 * Creates a new {@link FileBasedPropertiesDecorator}.
	 * 
	 * @param fileName
	 *        The name of the file containing the custom configuration.
	 * @param withDefaultConfig
	 *        whether the current existing configuration should be used as default
	 */
	public FileBasedPropertiesDecorator(String fileName, boolean withDefaultConfig) {
		_fileName = fileName;
		_withDefaultConfig = withDefaultConfig;
	}

	@Override
	protected void installConfiguration() throws Exception {
		if (_withDefaultConfig) {
			MultiProperties.restartWithConfig(XMLProperties.getFile(_fileName));
		} else {
			XMLProperties.startWithConfigFile(_fileName);
		}
	}

}

