/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.ImplOptionMapping;
import com.top_logic.layout.form.values.edit.TargetTypeInAppImplementations;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link TargetTypeInAppImplementations}.
 */
public class TestTargetTypeInAppImplementations extends TestCase {

	/**
	 * Base type of the implementations offered in this test.
	 */
	public interface Plugin {
		// Pure marker.
	}

	/**
	 * {@link Plugin} without a {@link TargetType} restriction.
	 */
	@InApp
	public static class UnrestrictedPlugin implements Plugin {
		// Pure marker.
	}

	/**
	 * {@link Plugin} for {@link TLTypeKind#STRING} values.
	 */
	@InApp(priority = 10)
	@TargetType(TLTypeKind.STRING)
	public static class StringPlugin implements Plugin {
		// Pure marker.
	}

	/**
	 * {@link Plugin} for {@link TLTypeKind#INT} values.
	 */
	@InApp
	@TargetType(TLTypeKind.INT)
	public static class IntPlugin implements Plugin {
		// Pure marker.
	}

	/**
	 * {@link Plugin} for references to {@link #BASE_TYPE} and its specializations only.
	 */
	@InApp
	@TargetType(value = TLTypeKind.REF, name = BASE_TYPE)
	public static class BaseRefPlugin implements Plugin {
		// Pure marker.
	}

	/**
	 * {@link Plugin} that is not {@link InApp}.
	 */
	public static class InternalPlugin implements Plugin {
		// Pure marker.
	}

	/**
	 * Configuration with a property selecting a {@link Plugin}.
	 */
	public interface PluginConfig extends ConfigurationItem {

		/**
		 * Property name for {@link #getImpl()}.
		 */
		String IMPL = "impl";

		/**
		 * The selected {@link Plugin}.
		 */
		@Name(IMPL)
		PolymorphicConfiguration<? extends Plugin> getImpl();

	}

	private static final String TEST_MODULE = "test.targettype";

	private static final String BASE = "Base";

	private static final String SUB = "Sub";

	private static final String OTHER = "Other";

	/**
	 * Qualified name of the type {@link BaseRefPlugin} is restricted to.
	 */
	static final String BASE_TYPE = TEST_MODULE + ":" + BASE;

	private TargetTypeInAppImplementations _options;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_options = new TargetTypeInAppImplementations(new TestOptions(
			TypedConfiguration.getConfigurationDescriptor(PluginConfig.class).getProperty(PluginConfig.IMPL)));
	}

	/**
	 * Only the implementations of the given kind and the unrestricted ones are offered, never a
	 * non-{@link InApp} one.
	 */
	public void testKindOnly() {
		assertOptions(apply(null, TLTypeKind.STRING), UnrestrictedPlugin.class, StringPlugin.class);
		assertOptions(apply(null, TLTypeKind.INT), UnrestrictedPlugin.class, IntPlugin.class);
	}

	/**
	 * An implementation restricted to named types is not offered without a concrete type.
	 */
	public void testNameRestrictionWithoutType() {
		assertOptions(apply(null, TLTypeKind.REF), UnrestrictedPlugin.class);
	}

	/**
	 * Without a kind, only the unrestricted implementations are offered.
	 */
	public void testUnknownKind() {
		assertOptions(apply(null, null), UnrestrictedPlugin.class);
	}

	/**
	 * An implementation restricted to named types is offered only for a compatible type, and only
	 * if the kind is known.
	 */
	public void testNameRestrictionWithType() {
		inTestModule(module -> {
			assertOptions(apply(module.getType(BASE), null), UnrestrictedPlugin.class);
			assertOptions(apply(module.getType(BASE), TLTypeKind.REF), UnrestrictedPlugin.class, BaseRefPlugin.class);
			assertOptions(apply(module.getType(SUB), TLTypeKind.REF), UnrestrictedPlugin.class, BaseRefPlugin.class);
			assertOptions(apply(module.getType(OTHER), TLTypeKind.REF), UnrestrictedPlugin.class);
		});
	}

	/**
	 * The implementation with the highest priority is the default.
	 */
	public void testDefault() {
		assertEquals(StringPlugin.class, apply(null, TLTypeKind.STRING).getDefaultValue());
	}

	/**
	 * The options are implementation classes of a {@link PolymorphicConfiguration}.
	 */
	public void testOptionMapping() {
		assertSame(ImplOptionMapping.INSTANCE, _options.getOptionMapping());
	}

	private ListOptionModel<Class<?>> apply(TLType type, TLTypeKind kind) {
		return _options.apply(type, kind);
	}

	/**
	 * Runs the given test with the module {@link #TEST_MODULE} in the application model, containing
	 * the classes {@link #BASE}, its specialization {@link #SUB}, and the unrelated {@link #OTHER},
	 * in a transaction that is rolled back afterwards.
	 */
	private static void inTestModule(Consumer<TLModule> test) {
		ThreadContext.inSystemContext(TestTargetTypeInAppImplementations.class, () -> {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				TLModule module = TLModelUtil.addModule(ModelService.getApplicationModel(), TEST_MODULE);
				TLClass base = TLModelUtil.addClass(module, BASE);
				TLClass sub = TLModelUtil.addClass(module, SUB);
				sub.getGeneralizations().add(base);
				TLModelUtil.addClass(module, OTHER);
				test.accept(module);
				tx.rollback();
			}
		});
	}

	private static void assertOptions(ListOptionModel<Class<?>> options, Class<?>... expected) {
		List<? extends Class<?>> actual = options.getBaseModel();
		assertEquals(Set.of(expected), new HashSet<>(actual));
		assertEquals("Duplicate options.", expected.length, actual.size());
	}

	private static class TestOptions extends LazyTypedAnnotatable implements DeclarativeFormOptions {

		private static final AnnotationCustomizations NO_CUSTOMIZATIONS = new ConfiguredAnnotationCustomizations();

		private final PropertyDescriptor _property;

		TestOptions(PropertyDescriptor property) {
			_property = property;
		}

		@Override
		public PropertyDescriptor getProperty() {
			return _property;
		}

		@Override
		public AnnotationCustomizations getCustomizations() {
			return NO_CUSTOMIZATIONS;
		}

	}

	/**
	 * Suite with the {@link TypeIndex} for looking up implementations and the
	 * {@link ModelService} for resolving model types.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTargetTypeInAppImplementations.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
