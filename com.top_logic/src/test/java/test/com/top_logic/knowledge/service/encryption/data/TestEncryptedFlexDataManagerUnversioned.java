/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.data;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.TestFlexUnversionedDataManager;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.db2.SerializingTransformer.Config;
import com.top_logic.knowledge.service.encryption.data.EncryptedFlexDataManager;

/**
 * Test case for {@link EncryptedFlexDataManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestEncryptedFlexDataManagerUnversioned extends TestFlexUnversionedDataManager {

	@Override
	protected FlexDataManager createFlexDataManager() {
		return new EncryptedFlexDataManager(TypedConfiguration.newConfigItem(Config.class),
			super.createFlexDataManager());
	}

	public static Test suite() {
		return TestEncryptedFlexDataManagerVersioned.encryptedSuite(TestEncryptedFlexDataManagerUnversioned.class);
	}

}
