/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Collections;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMLProperties.XMLPropertiesConfig;

/**
 * {@link TestSetupDecorator} that runs the
 * {@link test.com.top_logic.basic.TestSetupDecorator.SetupAction} with a custom
 * {@link XMLProperties} configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CustomConfigurationDecorator implements TestSetupDecorator {

	private XMLPropertiesConfig before;
	private FileManager oldFileManager;

	@Override
	public void setup(SetupAction innerSetup) throws Exception {
		if (XMLProperties.exists()) {
			before = XMLProperties.Module.INSTANCE.config();
			oldFileManager = FileManager.getInstance();
		} else {
			before = null;
		}
		installConfiguration();
		AliasManager.getInstance().setBaseAliases(Collections.emptyMap());

		innerSetup.setUpDecorated();
	}

	@Override
	public void tearDown(SetupAction innerSetup) throws Exception {
		innerSetup.tearDownDecorated();

		if (before != null) {
			FileManager.setInstance(oldFileManager);
			XMLProperties.restartXMLProperties(before);

			// TODO: Unclear how to revert to original state.
			AliasManager.getInstance().setBaseAliases(Collections.emptyMap());
		}
	}

	protected abstract void installConfiguration() throws Exception;

}
