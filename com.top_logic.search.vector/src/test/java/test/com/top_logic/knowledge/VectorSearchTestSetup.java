/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import junit.framework.Test;

import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.indexing.vector.VectorIndex;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Setup for search test cases.
 * 
 * @author    Dieter Rothbächer
 */
public class VectorSearchTestSetup {
	
	public static Test createSearchTestSetup(Test innerTest) {
		Test withCustomConfig = ServiceTestSetup.createSetup(innerTest, VectorIndex.Module.INSTANCE,
			DefaultAnalyzeService.Module.INSTANCE, PersistencyLayer.Module.INSTANCE);
		String customConfig = CustomPropertiesDecorator.createFileName(VectorSearchTestSetup.class);
		Test withKBSetup = new CustomPropertiesSetup(withCustomConfig, customConfig, true);
		return KBSetup.getSingleKBTest(withKBSetup);
	}

}
