/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.layout.scripting.recorder.ref;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.TransientTLObjectNaming;
import com.top_logic.layout.scripting.recorder.ref.TransientTLObjectNaming.Name;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link TransientTLObjectNaming}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestTransientTLObjectNaming extends TestWithModelExtension {

	/**
	 * Basic test for {@link Name}.
	 */
	public void testName() {
		Date dateVal = new Date();
		String stringVal = "string value";
		TLObject aObj = TransientModelFactory.createTransientObject(getAType());
		aObj.tUpdateByName("dateProp", dateVal);
		aObj.tUpdateByName("stringProp", stringVal);
		aObj.tUpdateByName("persistentRef", getAType());

		TLObject bObj1 = TransientModelFactory.createTransientObject(getBType());
		bObj1.tUpdateByName("name", "b1");
		TLObject bObj2 = TransientModelFactory.createTransientObject(getBType());
		bObj2.tUpdateByName("name", "b2");

		aObj.tUpdateByName("transientRef", bObj1);
		aObj.tUpdateByName("multipleTransientRef", set(bObj1, bObj2));

		Maybe<? extends ModelName> modelNameMaybe = ModelResolver.buildModelNameIfAvailable(aObj);
		assertTrue(modelNameMaybe.hasValue());
		ModelName modelName = modelNameMaybe.get();
		assertTrue("This is a test for " + TransientTLObjectNaming.class,
			modelName instanceof TransientTLObjectNaming.Name);

		TLObject located = (TLObject) ModelResolver.locateModel(actionContext(), modelName);

		assertEquals(dateVal, located.tValueByName("dateProp"));
		assertEquals(stringVal, located.tValueByName("stringProp"));
		assertEquals(getAType(), located.tValueByName("persistentRef"));
		TLObject locatedB1 = (TLObject) located.tValueByName("transientRef");
		assertEquals(getBType(), locatedB1.tType());
		assertEquals("b1", locatedB1.tValueByName("name"));
		Set<?> multipleTransientRef = new HashSet<>((Set<?>) located.tValueByName("multipleTransientRef"));
		assertEquals(2, multipleTransientRef.size());
		assertTrue(multipleTransientRef.remove(locatedB1));
		assertEquals("b2", ((TLObject) multipleTransientRef.iterator().next()).tValueByName("name"));
	}

	private LiveActionContext actionContext() {
		return new LiveActionContext(null, null, DefaultDisplayContext.getDisplayContext());
	}

	/**
	 * Basic cycles in {@link TLObject} value hierarchy.
	 */
	public void testCycle() {
		TLObject aObj = TransientModelFactory.createTransientObject(getAType());
		TLObject bObj = TransientModelFactory.createTransientObject(getBType());
		aObj.tUpdateByName("transientRef", bObj);
		bObj.tUpdateByName("name", "b1");
		bObj.tUpdateByName("a", aObj);

		ModelName aModelName = ModelResolver.buildModelName(aObj);
		assertTrue("This is a test for " + TransientTLObjectNaming.class,
			aModelName instanceof TransientTLObjectNaming.Name);
		TLObject locatedA = (TLObject) ModelResolver.locateModel(actionContext(), aModelName);
		TLObject b = (TLObject) locatedA.tValueByName("transientRef");
		assertSame(locatedA, b.tValueByName("a"));

		inThread(new Execution() {

			/**
			 * Running this test in new thread is actually a workaround to get a new
			 * {@link DisplayContext}.
			 * 
			 * <p>
			 * If this is not run in a new interaction, the locally stored {@link Name} will corrupt
			 * the test.
			 * </p>
			 * 
			 * @see test.com.top_logic.basic.BasicTestCase.Execution#run()
			 */
			@Override
			public void run() throws Exception {
				ThreadContextManager.inSystemInteraction(TestTransientTLObjectNaming.class, () -> {
					ModelName bModelName = ModelResolver.buildModelName(bObj);
					assertTrue("This is a test for " + TransientTLObjectNaming.class,
						bModelName instanceof TransientTLObjectNaming.Name);
					TLObject locatedB = (TLObject) ModelResolver.locateModel(actionContext(), bModelName);
					TLObject a = (TLObject) locatedB.tValueByName("a");
					assertSame(locatedB, a.tValueByName("transientRef"));
				});
			}
		});
	}

	private TLClass getAType() {
		return (TLClass) module().getType("A");
	}

	private TLClass getBType() {
		return (TLClass) module().getType("B");
	}

	private TLModule module() {
		String moduleName = "test.com.top_logic.element.layout.scripting.recorder.ref.TestTransientTLObjectNaming";
		return (TLModule) TLModelUtil.resolveModelPart(moduleName);
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestTransientTLObjectNaming}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestTransientTLObjectNaming.class);
		test = new ModelExtensionTestSetup(test, TestTransientTLObjectNaming.class);
		test = ServiceTestSetup.createSetup(test, ModelResolver.Module.INSTANCE);
		test = ServiceTestSetup.createSetup(test, ScriptingRecorder.Module.INSTANCE);
		return suite(test);
	}

}

