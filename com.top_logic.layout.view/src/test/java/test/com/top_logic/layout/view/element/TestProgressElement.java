/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.element.ProgressElement;
import com.top_logic.layout.view.element.ProgressElement.Progress;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Tests what a {@link ProgressElement} states: the fraction its two counts are the ratio of, the
 * label it derives from them, and which combinations of its expressions are a statement at all.
 *
 * <p>
 * The expressions themselves are not evaluated here: TL-Script evaluation needs the application
 * services (a {@code PersistencyLayer} among them), so what the element computes <em>from</em> the
 * values is tested directly, and the live recomputation of a bar over a channel is exercised in the
 * running application.
 * </p>
 */
public class TestProgressElement extends TestCase {

	/** The fraction is the ratio of the two counts. */
	public void testTheFractionIsTheRatioOfTheTwoCounts() {
		assertEquals(0.75, ProgressElement.counted(3, 4, null).fraction(), 0.0);
	}

	/** ...and they are the label, so the reader sees what the ratio is of. */
	public void testTheTwoCountsAreTheLabel() {
		assertEquals("3 / 7", ProgressElement.counted(3, 7, null).label());
	}

	/** A count is written as the whole number it is, not as the double it arrives as. */
	public void testACountIsWrittenAsAWholeNumber() {
		assertEquals("10 / 100", ProgressElement.counted(10d, 100d, null).label());
	}

	/** Nothing in all leaves an empty bar rather than a division by it. */
	public void testNothingInAllIsAnEmptyBar() {
		Progress progress = ProgressElement.counted(0, 0, null);

		assertEquals(0.0, progress.fraction(), 0.0);
		assertEquals("0 / 0", progress.label());
	}

	/** A label of its own replaces the one the counts would give. */
	public void testAGivenLabelReplacesTheCounts() {
		assertEquals("almost there", ProgressElement.counted(3, 7, "almost there").label());
	}

	/** Stating the fraction and the counts it would be the ratio of states it twice. */
	public void testStatingBothFormsIsAConfigurationError() {
		assertRejected(
			ProgressElement.Config.FRACTION, expr("x -> 0.5"),
			ProgressElement.Config.DONE, expr("x -> 1"),
			ProgressElement.Config.TOTAL, expr("x -> 2"));
	}

	/** Neither form is no statement at all. */
	public void testStatingNeitherFormIsAConfigurationError() {
		assertRejected();
	}

	/** One of the two counts alone is half a statement. */
	public void testOneCountAloneIsAConfigurationError() {
		assertRejected(ProgressElement.Config.DONE, expr("x -> 1"));
		assertRejected(ProgressElement.Config.TOTAL, expr("x -> 2"));
	}

	/** A fraction alone, and two counts alone, are each accepted. */
	public void testEitherFormAloneIsAccepted() throws ConfigurationException {
		accept(ProgressElement.Config.FRACTION, expr("x -> 0.5"));
		accept(ProgressElement.Config.DONE, expr("x -> 1"), ProgressElement.Config.TOTAL, expr("x -> 2"));
	}

	private static void assertRejected(Object... propertyValuePairs) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestProgressElement.class);
		context.getInstance(config(propertyValuePairs));
		try {
			context.checkErrors();
		} catch (AbortExecutionException | ConfigurationException expected) {
			// The configuration was rejected, as it must be.
			return;
		}
		fail("The configuration must be rejected.");
	}

	private static void accept(Object... propertyValuePairs) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestProgressElement.class);
		Object element = context.getInstance(config(propertyValuePairs));
		context.checkErrors();
		assertTrue("The configuration must build an element.", element instanceof ProgressElement);
	}

	private static ProgressElement.Config config(Object... propertyValuePairs) {
		ProgressElement.Config config = TypedConfiguration.newConfigItem(ProgressElement.Config.class);
		for (int n = 0; n < propertyValuePairs.length; n += 2) {
			PropertyDescriptor property = config.descriptor().getProperty((String) propertyValuePairs[n]);
			config.update(property, propertyValuePairs[n + 1]);
		}
		return config;
	}

	/** The given TL-Script source as the configuration reads it. */
	private static Expr expr(String source) {
		try {
			return ExprFormat.INSTANCE.getValue("expr", source);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a TL-Script expression: " + source, ex);
		}
	}

	/** Suite requiring the {@link TypeIndex} the TL-Script compiler resolves against. */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestProgressElement.class, TypeIndex.Module.INSTANCE);
	}

}
