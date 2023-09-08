/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model;

import java.util.Collection;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ThreadContextDecorator;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for the {@link DynamicModelService}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestDynamicModelService extends BasicTestCase {

	public void testConcreteTypesHaveCommonSuperType() {
		TLClass commonSuperClass = TLModelUtil.tlObjectType(getModel());
		Collection<TLClass> allClasses = TLModelUtil.getAllGlobalClasses(getModel());
		for (TLClass tlClass : allClasses) {
			if (tlClass == commonSuperClass) {
				// common super class does not inherit from itself
				return;
			}
			if (TLModuleSingleton.TL_MODULE_SINGLETONS_TYPE.equals(tlClass.getName())
				&& TlModelFactory.TL_MODEL_STRUCTURE.equals(tlClass.getModule().getName())) {
				// Singletons are defined but no TLObject-Type
				continue;
			}
			String message = "TLClass " + tlClass + " does not inherit from the common super-class "
				+ commonSuperClass + ". Actual supertypes: " + getSuperClasses(commonSuperClass);
			assertTrue(message, TLModelUtil.isGeneralization(commonSuperClass, tlClass));
		}
	}

	public void testNoFinalAbstractClass() {
		for (TLClass tlClass : TLModelUtil.getAllGlobalClasses(getModel())) {
			assertFalse("TLClass is final and abstract. TLClass: " + tlClass,
				tlClass.isFinal() && tlClass.isAbstract());
		}
	}

	private Set<TLClass> getSuperClasses(TLClass tlClass) {
		return TLModelUtil.getTransitiveGeneralizations(tlClass);
	}

	private TLModel getModel() {
		return ModelService.getInstance().getModel();
	}

	public static Test suite() {
		TestSuite innerSuite = new TestSuite(TestDynamicModelService.class);
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(ThreadContextDecorator.INSTANCE, innerSuite,
				DynamicModelService.Module.INSTANCE));
	}

}
