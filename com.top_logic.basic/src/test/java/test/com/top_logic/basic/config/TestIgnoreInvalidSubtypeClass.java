/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test case that asserts that deserializing a {@link ConfigurationItem} is possible, even if some
 * other classes in some way related to the deserialized type cannot be loaded.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DeactivatedTest("Does not work in regular build, since the build process loads configuration classes, before this test has a chance to delete class files.")
@SuppressWarnings("javadoc")
public class TestIgnoreInvalidSubtypeClass extends AbstractTypedConfigurationTestCase {

	public interface A {
		@DefaultContainer
		@InstanceFormat
		X getX();

		C getC();
	}

	public interface X {
		// Marker.
	}

	@Abstract
	public interface C extends ConfigurationItem {
		// Marker.
	}

	@TagName("c1")
	public interface C1 extends C {
		// Marker.
	}

	@TagName("c2")
	public interface C2 extends C {
		@InstanceFormat
		DoesNotExist getInvalid();
	}

	@TagName("c3")
	public interface C3 extends C, DoesNotExist {
		// Marker
	}

	public static class Y extends AbstractConfiguredInstance<Y.Config> implements X {
		public Y(InstantiationContext context, Config config) {
			super(context, config);
		}

		@TagName("y")
		public interface Config extends PolymorphicConfiguration<Y> {
			// Marker.
		}
	}

	public static class Z extends AbstractConfiguredInstance<Z.Config> implements X {
		@TagName("z")
		public interface Config extends PolymorphicConfiguration<Z> {
			// Marker
		}

		public Z(InstantiationContext context, Config config) {
			super(context, config);
		}
	}

	public static class Z1 extends AbstractConfiguredInstance<Z1.Config> implements X, DoesNotExist {
		@TagName("z1")
		public interface Config extends PolymorphicConfiguration<Z1> {
			// Marker
		}

		public Z1(InstantiationContext context, Config config) {
			super(context, config);
		}
	}

	public static class Z2 extends AbstractConfiguredInstance<Z2.Config> implements X {
		@TagName("z2")
		public interface Config extends PolymorphicConfiguration<Z2> {
			@InstanceFormat
			DoesNotExist getInvalid();
		}

		public Z2(InstantiationContext context, Config config) {
			super(context, config);
		}
	}

	public static class Z3 extends AbstractConfiguredInstance<Z3.Config> implements X {
		@TagName("z3")
		public interface Config extends PolymorphicConfiguration<Z2>, DoesNotExist {
			// Marker
		}

		public Z3(InstantiationContext context, Config config) {
			super(context, config);
		}
	}

	public static interface DoesNotExist {
		// Compiled class deleted before test.
	}

	public void testLoad() throws ConfigurationException {
		try {
			assertDoesNotExist();
			doTest();
		} finally {
			restoreClasses();
		}
	}

	private static void deleteClasses() {
		deleteClass("Z");
		deleteClass("DoesNotExist");
	}

	private static void restoreClasses() {
		restoreClass("Z");
		restoreClass("DoesNotExist");
	}

	private static void deleteClass(String className) {
		File classFile = classFile(className);
		File backupFile = backupFile(classFile);
		if (classFile.exists()) {
			System.out.println("Exists: " + classFile);
			backupFile.delete();
		}
		boolean ok = classFile.renameTo(backupFile);
		if (ok) {
			System.out.println("Deleted: " + classFile);
		}
		assertFalse("Class file could not be deleted.", classFile.exists());
	}

	private static File backupFile(File classFile) {
		return new File(classFile.getParentFile(), classFile.getName() + ".bak");
	}

	private static void restoreClass(String className) {
		File classFile = classFile(className);
		File backupFile = backupFile(classFile);
		backupFile.renameTo(classFile);
	}

	private static File classFile(String className) {
		return new File(ModuleLayoutConstants.CLASSES_LOC
			+ "/" + TestIgnoreInvalidSubtypeClass.class.getName().replace('.', '/') + "$" + className
			+ ".class");
	}

	private void assertDoesNotExist() {
		try {
			assertEquals("Z", Z.class.getSimpleName());
			fail("Z must not be loaded.");
		} catch (NoClassDefFoundError ex) {
			// Expected.
		}
		try {
			assertEquals("DoesNotExist", DoesNotExist.class.getSimpleName());
			fail("DoesNotExist must not be loaded.");
		} catch (NoClassDefFoundError ex) {
			// Expected.
		}
	}

	private void doTest() throws ConfigurationException {
		A item = (A) TypedConfiguration.fromString(
			"<config xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "' config:interface='"
				+ A.class.getName() + "'><y/></config>");
		BasicTestCase.assertInstanceof(item.getX(), Y.class);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		throw new UnsupportedOperationException();
	}

	public static Test suite() {
		deleteClasses();
		return suite(TestIgnoreInvalidSubtypeClass.class);
	}
}
