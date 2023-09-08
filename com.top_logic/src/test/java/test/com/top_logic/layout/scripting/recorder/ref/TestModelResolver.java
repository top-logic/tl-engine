/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver.Config;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Tests for the {@link ModelResolver}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestModelResolver extends TestCase {

	private static final Object UNNAMABLE_TEST_MODEL = new Object();

	/** Container for types that are used by the test. (And only by the test.) */
	@SuppressWarnings("javadoc")
	public static class ExampleTypes {

		public static interface InterfaceA1A { /* Nothing needed */ }
		public static interface InterfaceA1B { /* Nothing needed */ }
		public static interface InterfaceA1 extends InterfaceA1A, InterfaceA1B { /* Nothing needed */ }

		public static interface InterfaceA2A { /* Nothing needed */ }
		public static interface InterfaceA2B { /* Nothing needed */ }
		public static interface InterfaceA2 extends InterfaceA2A, InterfaceA2B { /* Nothing needed */ }

		public static class ClassA implements InterfaceA1, InterfaceA2 { /* Nothing needed */ }


		public static interface InterfaceB1A { /* Nothing needed */ }
		public static interface InterfaceB1B { /* Nothing needed */ }
		public static interface InterfaceB1 extends InterfaceB1A, InterfaceB1B { /* Nothing needed */ }

		public static interface InterfaceB2A { /* Nothing needed */ }
		public static interface InterfaceB2B { /* Nothing needed */ }
		public static interface InterfaceB2 extends InterfaceB2A, InterfaceB2B { /* Nothing needed */ }

		public static class ClassB extends ClassA implements InterfaceB1, InterfaceB2 { /* Nothing needed */ }


		public static interface InterfaceC1A { /* Nothing needed */ }
		public static interface InterfaceC1B { /* Nothing needed */ }
		public static interface InterfaceC1 extends InterfaceC1A, InterfaceC1B { /* Nothing needed */ }

		public static interface InterfaceC2A { /* Nothing needed */ }
		public static interface InterfaceC2B { /* Nothing needed */ }
		public static interface InterfaceC2 extends InterfaceC2A, InterfaceC2B { /* Nothing needed */ }

		public static class ClassC extends ClassB implements InterfaceC1, InterfaceC2 { /* Nothing needed */ }

	}

	/** An {@link ModelNamingScheme} for tests. */
	public static class InstructableModelNamingScheme extends AbstractModelNamingScheme<Object, InstructableModelNamingScheme.InstructableModelName> {

		/** {@link ModelName} for the {@link InstructableModelNamingScheme}. */
		public interface InstructableModelName extends ModelName {
			/** Nothing needed */
		}

		/** The class for which this {@link ModelNamingScheme} should be responsible. */
		public static Class MODEL_CLASS;

		/** The model that can be identified by this {@link ModelNamingScheme}. */
		public static Object MODEL;

		@Override
		public Class<InstructableModelName> getNameClass() {
			return InstructableModelName.class;
		}

		@Override
		public Class<Object> getModelClass() {
			return MODEL_CLASS;
		}

		@Override
		public Object locateModel(ActionContext context, InstructableModelName name) {
			return MODEL;
		}

		@Override
		protected void initName(InstructableModelName name, Object model) {
			// Nothing to do
		}

		@Override
		protected boolean isCompatibleModel(Object model) {
			return model == MODEL;
		}

	}

	/** A {@link ModelNamingScheme} that identified only a singleton value: {@link #MODEL} */
	public static class SingletonNamingSchemeA extends
			AbstractModelNamingScheme<Object, SingletonNamingSchemeA.SingletonModelNameA> {

		/** {@link ModelName} for the {@link SingletonNamingSchemeA}. */
		public interface SingletonModelNameA extends ModelName {
			/** Nothing needed */
		}

		/** The singleton object identified by the {@link SingletonNamingSchemeA}. */
		public static Object MODEL = new Object();

		@Override
		public Class<SingletonModelNameA> getNameClass() {
			return SingletonModelNameA.class;
		}

		@Override
		public Class<Object> getModelClass() {
			return Object.class;
		}

		@Override
		public Object locateModel(ActionContext context, SingletonModelNameA name) {
			return MODEL;
		}

		@Override
		protected void initName(SingletonModelNameA name, Object model) {
			// Nothing to do
		}

		@Override
		protected boolean isCompatibleModel(Object model) {
			return model == MODEL;
		}

	}

	/** A {@link ModelNamingScheme} that identified only a singleton value: {@link #MODEL} */
	public static class SingletonNamingSchemeB extends
			AbstractModelNamingScheme<Object, SingletonNamingSchemeB.SingletonModelNameB> {

		/** {@link ModelName} for the {@link SingletonNamingSchemeA}. */
		public interface SingletonModelNameB extends ModelName {
			/** Nothing needed */
		}

		/** The singleton object identified by the {@link SingletonNamingSchemeA}. */
		public static Object MODEL = new Object();

		@Override
		public Class<SingletonModelNameB> getNameClass() {
			return SingletonModelNameB.class;
		}

		@Override
		public Class<Object> getModelClass() {
			return Object.class;
		}

		@Override
		public Object locateModel(ActionContext context, SingletonModelNameB name) {
			return MODEL;
		}

		@Override
		protected void initName(SingletonModelNameB name, Object model) {
			// Nothing to do
		}

		@Override
		protected boolean isCompatibleModel(Object model) {
			return model == MODEL;
		}

	}

	/** Tests if the helper methods of this test work. */
	public void testHelperMethods() {
		ModelResolver modelResolver = newModelResolver(config());
		assertFalse(buildModelName(modelResolver, UNNAMABLE_TEST_MODEL).hasValue());
	}

	/**
	 * Checks that the multiple {@link ModelNamingScheme}s can be registered for the same type and
	 * all of them are used if necessary.
	 */
	public void testMultipleNamingSchemes() throws Throwable {
		Config config = config();
		config.getSchemes().add(schemeConfig(SingletonNamingSchemeA.class));
		config.getSchemes().add(schemeConfig(SingletonNamingSchemeB.class));
		ModelResolver modelResolver = newModelResolver(config);
		Maybe<? extends ModelName> aName = buildModelName(modelResolver, SingletonNamingSchemeA.MODEL);
		assertTrue(aName.hasValue());
		Maybe<? extends ModelName> bName = buildModelName(modelResolver, SingletonNamingSchemeB.MODEL);
		assertTrue(bName.hasValue());
		assertFalse(buildModelName(modelResolver, new Object()).hasValue());
		
		assertEquals(SingletonNamingSchemeA.MODEL, modelResolver.locateModelImpl(null, null, aName.get()));
		assertEquals(SingletonNamingSchemeB.MODEL, modelResolver.locateModelImpl(null, null, bName.get()));
	}

	/**
	 * Tests for all types in {@link ExampleTypes} whether the {@link ModelResolver} finds them, if
	 * a {@link ModelNamingScheme} is registered only for them.
	 * <p>
	 * Does not test the priority, i.e. which {@link ModelNamingScheme} is used, if multiple are
	 * registered.
	 * </p>
	 */
	public void testBuildModel() throws Throwable {
		List<Pair<Class<?>, Class<?>>> failures = new ArrayList<>();
		for (Class<?> nameableType : getExampleTypes()) {
			InstructableModelNamingScheme.MODEL_CLASS = nameableType;
			Config config = config();
			config.getSchemes().add(schemeConfig(InstructableModelNamingScheme.class));
			ModelResolver modelResolver = newModelResolver(config);

			for (Class<?> modelType : getExampleClasses()) {
				Object model = modelType.newInstance();
				InstructableModelNamingScheme.MODEL = model;
				Maybe<? extends ModelName> modelName = buildModelName(modelResolver, model);
				if (nameableType.isAssignableFrom(modelType) != modelName.hasValue()) {
					failures.add(new Pair<>(nameableType, modelType));
				}
			}
		}
		if (!failures.isEmpty()) {
			fail("The following pairs of NameableType and ModelType failed: " + failures);
		}
	}

	private Config config() {
		Config result = TypedConfiguration.newConfigItem(ModelResolver.Config.class);
		result.setImplementationClass(ModelResolver.class);
		PropertyDescriptor priorities = result.descriptor().getProperty(Config.PRIORITIES);
		result.update(priorities, list(ModelResolver.DEFAULT_PRIORITY_LEVEL_NAME));
		return result;
	}

	private ModelNamingScheme.Config schemeConfig(Class<? extends ModelNamingScheme<?, ?, ?>> schemeImpl) {
		ModelNamingScheme.Config config = TypedConfiguration.newConfigItem(ModelNamingScheme.Config.class);
		config.setImplementationClass(schemeImpl);
		return config;
	}

	private List<Class<?>> getExampleTypes() {
		return Arrays.asList(ExampleTypes.class.getClasses());
	}

	private List<Class<?>> getExampleClasses() {
		return Arrays.<Class<?>> asList(ExampleTypes.ClassA.class, ExampleTypes.ClassB.class, ExampleTypes.ClassC.class);
	}

	private Maybe<? extends ModelName> buildModelName(ModelResolver modelResolver, Object model) {
		return modelResolver.buildModelNameImpl(null, model);
	}

	private ModelResolver newModelResolver(ModelResolver.Config config) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestModelResolver.class, TypeIndex.Module.INSTANCE));
	}

}
