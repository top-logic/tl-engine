/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ValueNamingSchemeRegistry;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableNaming.GlobalVariableName;
import com.top_logic.layout.scripting.recorder.ref.value.object.GlobalVariableRef;
import com.top_logic.layout.scripting.recorder.ref.value.object.NamedValue;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.model.TransientObject;

/**
 * {@link TestCase}s for the {@link ReferenceFactory}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestReferenceFactory extends BasicTestCase {

	final class FakeWrapper extends TransientObject implements Wrapper {

		@Override
		public final KnowledgeObject tHandle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int compareTo(Wrapper o) {
			throw new UnsupportedOperationException();
		}

		/**
		 * TODO #2829: Delete TL 6 deprecation
		 * 
		 * @deprecated Use {@link #tHandle()} or one of the other short-cuts for accessing
		 *             underlying data, e.g. {@link #tGetData(String)}.
		 * @deprecated Use {@link #tHandle()} instead
		 */
		@Override
		public final KnowledgeObject getWrappedObject() {
			return (KnowledgeObject) tHandle();
		}

		/**
		 * TODO #2829: Delete TL 6 deprecation.
		 * 
		 * @deprecated Use {@link #tId()} instead
		 */
		@Deprecated
		@Override
		public final ObjectKey getObjectKey() {
			return tId();
		}

	}

	private static final String REF_FACTORY_NAME = ReferenceFactory.class.getSimpleName();

	/**
	 * Tests that trying to reference a fake {@link Wrapper} does not cause an error to be thrown,
	 * but {@link Maybe#none()} to be returned. (When no model or value naming scheme is given.)
	 */
	public void testReferenceFakeWrapper() {
		try {
			Maybe<? extends ModelName> fakeWrapperReference = ModelResolver.buildModelNameIfAvailable(new FakeWrapper());
			assertEquals(Maybe.none(), fakeWrapperReference);
		} catch (NullPointerException ex) {
			String message = "A NPE is thrown when a fake wrapper should be referenced: " + ex.getMessage();
			BasicTestCase.fail(message, ex);
		}
	}

	/**
	 * Tests that if a reference should be build to a value which is already stored in a global
	 * variable, a {@link GlobalVariableName} is build.
	 */
	public void testReferenceGlobalVariable() {
		String variableName = "test variable";
		try {
			FakeWrapper value = new FakeWrapper();
			GlobalVariableStore.getElseCreateFromSession().set(variableName, value);
			Maybe<? extends ModelName> actualValue = ModelResolver.buildModelNameIfAvailable(value);
			String message = REF_FACTORY_NAME + " does not build " + GlobalVariableName.class.getSimpleName() + "!";
			assertTrue(message, actualValue.hasValue());
			GlobalVariableName globalVariableName = globalVariableName(variableName);
			assertEquals(message, globalVariableName, actualValue.get());
		} finally {
			GlobalVariableStore.getElseCreateFromSession().del(variableName);
		}
	}

	private GlobalVariableName globalVariableName(String variableName) {
		GlobalVariableName globalVariableName = newConfigItem(GlobalVariableName.class);
		globalVariableName.setName(variableName);
		return globalVariableName;
	}

	/**
	 * Tests that the {@link ReferenceFactory} does not build a {@link GlobalVariableRef} for a
	 * primitive.
	 */
	public void testReferenceGlobalVariableNotForPrimitives() {
		int primitiveValue = 0;
		String variableName = "test variable";
		try {
			GlobalVariableStore.getElseCreateFromSession().set(variableName, primitiveValue);
			ModelName reference = ModelResolver.buildModelNameIfAvailable((Object) primitiveValue).getElseError();
			String message = REF_FACTORY_NAME + " builds " + GlobalVariableRef.class.getSimpleName()
				+ " for primitives!";
			assertFalse(message, reference instanceof GlobalVariableRef);
		} finally {
			GlobalVariableStore.getElseCreateFromSession().del(variableName);
		}
	}

	/**
	 * Tests if the {@link FakeValueNamingScheme} is used correctly. This is needed to ensure the
	 * following tests will actually test something.
	 */
	public void testFakeValueNamingScheme() {
		Object value = new Object();
		FakeValueNamingScheme.setNamedValue(value);
		List<Object> valueContext = Collections.singletonList(value);
		Maybe<? extends ModelName> valueRef = ModelResolver.buildModelNameIfAvailable(valueContext, value);
		assertTrue(FakeValueNamingScheme.class.getSimpleName() + " is not used!", valueRef.hasValue());
	}

	/**
	 * Tests if the {@link FakeModelNamingScheme} is used correctly. This is needed to ensure the
	 * following tests will actually test something.
	 */
	public void testFakeModelNamingScheme() {
		Object value = new Object();
		FakeModelNamingScheme.setNamedModel(value);
		Maybe<? extends ModelName> valueRef = ModelResolver.buildModelNameIfAvailable(value);
		assertTrue(FakeModelNamingScheme.class.getSimpleName() + " is not used!", valueRef.hasValue());
	}

	/**
	 * Tests that a {@link GlobalVariableName} will be produced if both it and a
	 * {@link ValueNamingScheme} can be used to identify an object.
	 */
	public void testGlobalReferenceWinsOverValueNaming() {
		Object value = new Object();
		FakeValueNamingScheme.setNamedValue(value);
		String variableName = "test variable";
		try {
			GlobalVariableStore.getElseCreateFromSession().set(variableName, value);
			List<Object> valueContext = Collections.singletonList(value);
			ModelName valueRef = ModelResolver.buildModelNameIfAvailable(valueContext, value).getElseError();
			String message = REF_FACTORY_NAME + " prefers " + FakeValueNamingScheme.class.getSimpleName() + " over "
				+ GlobalVariableName.class.getSimpleName() + "!";
			assertTrue(message, valueRef instanceof GlobalVariableName);
		} finally {
			GlobalVariableStore.getElseCreateFromSession().del(variableName);
		}
	}

	/**
	 * Tests that a {@link GlobalVariableRef} will be produced if both it and a
	 * {@link ModelNamingScheme} can be used to identify an object.
	 */
	public void testGlobalReferenceWinsOverModelNaming() {
		Object value = new Object();
		FakeModelNamingScheme.setNamedModel(value);
		String variableName = "test variable";
		try {
			GlobalVariableStore.getElseCreateFromSession().set(variableName, value);
			ModelName valueRef = ReferenceFactory.legacyTryReferenceValue(value, null).getElseError();
			String message = REF_FACTORY_NAME + " prefers " + FakeModelNamingScheme.class.getSimpleName() + " over "
				+ GlobalVariableRef.class.getSimpleName() + "!";
			assertTrue(message, valueRef instanceof GlobalVariableRef);
		} finally {
			GlobalVariableStore.getElseCreateFromSession().del(variableName);
		}
	}

	/**
	 * Tests that a {@link ValueNamingScheme} will be used if both it and a
	 * {@link ModelNamingScheme} can be used to identify an object.
	 */
	public void testValueNamingWinsOverModelNaming() {
		Object value = new Object();
		FakeValueNamingScheme.setNamedValue(value);
		FakeModelNamingScheme.setNamedModel(value);
		List<Object> valueContext = Collections.singletonList(value);
		ModelName valueRef = ReferenceFactory.legacyTryReferenceValue(value, valueContext).getElseError();
		String message = REF_FACTORY_NAME + " prefers " + FakeModelNamingScheme.class.getSimpleName() + " over "
			+ FakeValueNamingScheme.class.getSimpleName() + "!";
		assertTrue(message, valueRef instanceof NamedValue);
	}

	/**
	 * Tests getting reference for {@link Map} value.
	 */
	public void testMapValue() {
		HashMap<Object, Object> value = new HashMap<>();
		value.put("key", "value");
		value.put(15, new Date());
		value.put(Boolean.TRUE, null);
		value.put(new HashMap<>(), TimeUnit.DAYS);

		ModelName valueRef = ModelResolver.buildModelNameIfAvailable(value).getElseError();

		assertEquals(value, ModelResolver.locateModel(null, valueRef));

		value.remove("key");
		assertNotEquals("Unstable value ref.", value, ModelResolver.locateModel(null, valueRef));
	}

	/**
	 * Creates a {@link Test} suite that takes care of setup and teardown of the dependencies of
	 * these tests.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestReferenceFactory.class,
			ModelResolver.Module.INSTANCE, ValueNamingSchemeRegistry.Module.INSTANCE));
	}

}
