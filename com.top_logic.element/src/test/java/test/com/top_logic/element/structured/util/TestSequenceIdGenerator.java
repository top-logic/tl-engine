/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.structured.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.element.structured.util.SequenceIdGenerator;
import com.top_logic.element.structured.util.SequenceNameMigrationProcessor;

/**
 * Test for the unified physical sequence naming shared by {@link SequenceIdGenerator} and the
 * <code>generateSequenceId</code>/<code>resetSequence</code> TL-Script functions, and for the
 * {@link SequenceNameMigrationProcessor#toUnifiedName(String) legacy name rewrite}.
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSequenceIdGenerator extends TestCase {

	/**
	 * Tests that a sequence without context is named base name plus technical suffix.
	 */
	public void testSequenceNameWithoutContext() {
		assertEquals("invoice" + SequenceIdGenerator.SEQUENCE_SUFFIX,
			SequenceIdGenerator.sequenceName("invoice", null));
	}

	/**
	 * Tests that the technical suffix is appended last, after the context part.
	 */
	public void testSequenceNameWithContext() {
		assertEquals("invoice_productA" + SequenceIdGenerator.SEQUENCE_SUFFIX,
			SequenceIdGenerator.sequenceName("invoice", "productA"));
	}

	/**
	 * Tests that a legacy name without context is rewritten to the unified name.
	 */
	public void testMigrateLegacyNameWithoutContext() {
		assertEquals("AAA" + SequenceIdGenerator.SEQUENCE_SUFFIX,
			SequenceNameMigrationProcessor.toUnifiedName("AAA_NumberHandler"));
	}

	/**
	 * Tests that a legacy name with context has its technical suffix relocated behind the context.
	 */
	public void testMigrateLegacyNameWithContext() {
		assertEquals("AAA_DemoTypes:12" + SequenceIdGenerator.SEQUENCE_SUFFIX,
			SequenceNameMigrationProcessor.toUnifiedName("AAA_NumberHandler_DemoTypes:12"));
	}

	/**
	 * Tests that a base name containing underscores is preserved (the technical suffix marks the
	 * split, not the base name).
	 */
	public void testMigrateLegacyNameWithUnderscoreInBase() {
		assertEquals("a_b_c_ctx" + SequenceIdGenerator.SEQUENCE_SUFFIX,
			SequenceNameMigrationProcessor.toUnifiedName("a_b_c_NumberHandler_ctx"));
	}

	/**
	 * Tests that a migrated legacy name matches the name the generator now computes for the same
	 * base and context.
	 */
	public void testMigrationMatchesGeneratedName() {
		assertEquals(SequenceIdGenerator.sequenceName("invoice", null),
			SequenceNameMigrationProcessor.toUnifiedName("invoice_NumberHandler"));
	}

	/**
	 * Tests that an already-unified name is not touched.
	 */
	public void testUnifiedNameNotMigrated() {
		assertNull(SequenceNameMigrationProcessor.toUnifiedName("invoice" + SequenceIdGenerator.SEQUENCE_SUFFIX));
	}

	/**
	 * Suite of all tests in this class.
	 */
	public static Test suite() {
		return new TestSuite(TestSequenceIdGenerator.class);
	}

}
