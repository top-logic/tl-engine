/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * The class {@link TestTypeLookup} tests {@link TLModelUtil#findType(String)}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeLookup extends BasicTestCase {

	public void testTypeOfRoot() throws ConfigurationException {
		TLType expectedRootType = TLModelUtil.findType(TLModelUtil.qualifiedName("module1", "Class1"));
		assertNotNull(expectedRootType);

		TLModule module1 = DynamicModelService.getApplicationModel().getModule("module1");
		assertNotNull(module1);

		TLObject root = module1.getSingleton(TLModule.DEFAULT_SINGLETON_NAME);
		assertNotNull(root);

		TLType actualRootType = root.tType();
		assertEquals(expectedRootType, actualRootType);

		String tlObjectName = TLModelUtil.qualifiedName(TlModelFactory.TL_MODEL_STRUCTURE, TLObject.TL_OBJECT_TYPE);
		TLType tlObjectType = TLModelUtil.findType(tlObjectName);
		assertNotNull(tlObjectType);

		assertTrue(tlObjectName + " is automatically added to all types.",
			TLModelUtil.isGeneralization((TLClass) tlObjectType, (TLClass) expectedRootType));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTypeLookup}.
	 */
	public static Test suite() {
		return DynamicModelServiceSetup.suite(TestTypeLookup.class);
	}

}

