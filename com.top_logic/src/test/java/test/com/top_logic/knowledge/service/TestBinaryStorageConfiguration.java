/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.knowledge.service.BinaryStorageSettings;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.model.annotate.TLBinaryStorage;

/**
 * Test of the binary storage configurations: defaults and constraints on the thresholds.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBinaryStorageConfiguration extends BasicTestCase {

	private static final long TOO_LARGE = BlobUpload.MAX_BUFFERED_THRESHOLD + 1L;

	public void testFactoryDefaults() throws ConfigurationException {
		FlexDataManagerFactory.Config defaultConfig =
			TypedConfiguration.newConfigItem(FlexDataManagerFactory.Config.class);
		BinaryStorageSettings defaults = newFactory(defaultConfig).getBinaryDefaults();
		assertEquals(BinaryAttributeKind.HYBRID, defaults.kind());
		assertNull(defaults.storeName());
		assertEquals(FlexDataManagerFactory.Config.DEFAULT_BINARY_THRESHOLD, defaults.threshold());

		FlexDataManagerFactory.Config inlineConfig = TypedConfiguration.parse("config",
			FlexDataManagerFactory.Config.class, CharacterContents.newContent(
				"<config " + FlexDataManagerFactory.Config.BINARY_KIND + "='"
					+ BinaryAttributeKind.INLINE.getExternalName() + "'/>"));
		assertEquals(BinaryAttributeKind.INLINE, newFactory(inlineConfig).getBinaryDefaults().kind());
	}

	private static FlexDataManagerFactory newFactory(FlexDataManagerFactory.Config config) {
		return new FlexDataManagerFactory(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	public void testAnnotationThreshold() throws ConfigurationException {
		TLBinaryStorage annotation = TypedConfiguration.newConfigItem(TLBinaryStorage.class);
		assertNull("No kind means the configured default kind.", annotation.getKind());
		assertNoFailure(annotation);

		annotation.setThreshold(0L);
		assertNoFailure(annotation);

		annotation.setThreshold(Long.valueOf(BlobUpload.MAX_BUFFERED_THRESHOLD));
		assertNoFailure(annotation);

		annotation.setThreshold(-1L);
		assertFailure(annotation, TLBinaryStorage.THRESHOLD);

		annotation.setThreshold(TOO_LARGE);
		assertFailure(annotation, TLBinaryStorage.THRESHOLD);
	}

	public void testAnnotationThresholdFromXml() throws ConfigurationException {
		TLBinaryStorage annotation = TypedConfiguration.parse(TLBinaryStorage.TAG_NAME, TLBinaryStorage.class,
			CharacterContents.newContent(
				"<" + TLBinaryStorage.TAG_NAME + " " + TLBinaryStorage.THRESHOLD + "='3GB'/>"));
		assertEquals(Long.valueOf(3L * 1024 * 1024 * 1024), annotation.getThreshold());

		BufferingProtocol log = new BufferingProtocol();
		new ConstraintChecker().check(log, annotation);
		assertTrue("Too large threshold not reported.", log.hasErrors());
	}

	public void testFactoryThreshold() throws ConfigurationException {
		FlexDataManagerFactory.Config config = TypedConfiguration.newConfigItem(FlexDataManagerFactory.Config.class);
		assertNoFailure(config);

		setValue(config, FlexDataManagerFactory.Config.BINARY_THRESHOLD, 0L);
		assertNoFailure(config);

		setValue(config, FlexDataManagerFactory.Config.BINARY_THRESHOLD, TOO_LARGE);
		assertFailure(config, FlexDataManagerFactory.Config.BINARY_THRESHOLD);
	}

	public void testHybridAttributeThreshold() throws ConfigurationException {
		HybridBinaryAttribute.Config config = TypedConfiguration.newConfigItem(HybridBinaryAttribute.Config.class);
		config.setAttributeName("data");
		config.setThreshold(1);
		assertNoFailure(config);

		config.setThreshold(0);
		assertFailure(config, HybridBinaryAttribute.Config.THRESHOLD);

		config.setThreshold(TOO_LARGE);
		assertFailure(config, HybridBinaryAttribute.Config.THRESHOLD);
	}

	private static void setValue(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	private static void assertNoFailure(ConfigurationItem config) throws ConfigurationException {
		assertEquals(List.of(), failures(config));
	}

	private static void assertFailure(ConfigurationItem config, String property) throws ConfigurationException {
		List<ConstraintFailure> failures = failures(config);
		assertEquals(failures.toString(), 1, failures.size());
		assertEquals(property, failures.get(0).getContextProperty().getPropertyName());
	}

	private static List<ConstraintFailure> failures(ConfigurationItem config) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);
		return checker.getFailures();
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(TestBinaryStorageConfiguration.class);
	}

}
