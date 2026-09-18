/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.common.ReactProgressControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ProgressElement;
import com.top_logic.layout.view.element.ProgressElement.Progress;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Tests what a {@link ProgressElement} states: the fraction its two counts are the ratio of, the
 * label it derives from them, and which combinations of its expressions are a statement at all.
 *
 * <p>
 * The expressions are not written as TL-Script here: evaluating one needs the application services
 * (a {@code PersistencyLayer} among them), so what the element computes <em>from</em> the values of
 * its expressions is tested directly, and a bar following a channel is driven by an expression
 * written in Java.
 * </p>
 */
public class TestProgressElement extends TestCase {

	/** Name of the channel a bar over a channel is bound to. */
	private static final String VALUE = "value";

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

	/** A fraction that answers a number is the share of the bar. */
	public void testAFractionIsTheShareItAnswers() {
		assertEquals(Double.valueOf(0.5), ProgressElement.fraction(Double.valueOf(0.5)));
	}

	/** A fraction that answers nothing at all is no share: the bar has no length to fill. */
	public void testAFractionThatAnswersNothingIsNoShare() {
		assertNull(ProgressElement.fraction(null));
	}

	/** ...and so is a fraction that answers something that is no number. */
	public void testAFractionThatIsNoNumberIsNoShare() {
		assertNull(ProgressElement.fraction("half"));
	}

	/** The two counts always state a share, so a counted bar is never one without a length. */
	public void testTwoCountsAlwaysStateAShare() {
		assertNotNull(ProgressElement.counted(0, 0, null).fraction());
	}

	/**
	 * Tests that a bar over a channel follows it in both directions: from a share to none when the
	 * value stops stating one, and back to a share when it states one again.
	 */
	public void testAChannelUpdateSwitchesBetweenAShareAndNone() {
		ViewContext context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		ViewChannel value = new DefaultViewChannel(VALUE);
		context.registerChannel(VALUE, value);
		value.set(Double.valueOf(0.25));

		ProgressElement element =
			new ProgressElement(new ChannelRef(VALUE), self(), null, null, null, List.of());
		ReactProgressControl control = (ReactProgressControl) element.createControl(context);

		assertEquals(Double.valueOf(0.25), displayedFraction(control));

		value.set(null);
		assertNull("A value stating no share leaves the bar without one.", displayedFraction(control));

		value.set(Double.valueOf(0.75));
		assertEquals("The bar takes up a share again as soon as the value states one.",
			Double.valueOf(0.75), displayedFraction(control));
	}

	/** A label of its own replaces the one the counts would give. */
	public void testAGivenLabelReplacesTheCounts() {
		assertEquals("almost there", ProgressElement.counted(3, 7, "almost there").label());
	}

	/**
	 * Tests that an internationalized label is displayed as the text it stands for, not as the
	 * literal a script writes it as.
	 */
	public void testAnInternationalizedLabelIsResolved() {
		ProgressElement element = new ProgressElement(null, constant(Double.valueOf(0.5)), null, null,
			constant(ResKey.text("Working")), List.of());

		assertEquals("Working", element.progressOf(null).label());
	}

	/** A label that is a text already is displayed as it is written. */
	public void testATextLabelIsDisplayedAsItIs() {
		ProgressElement element = new ProgressElement(null, constant(Double.valueOf(0.5)), null, null,
			constant("3 of 7 files"), List.of());

		assertEquals("3 of 7 files", element.progressOf(null).label());
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

	/** The share the given control displays, {@code null} for the bar without one. */
	private static Double displayedFraction(ReactProgressControl control) {
		Object state;
		try {
			state = JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + control.stateAsJSON(), ex);
		}
		Object fraction = ((Map<?, ?>) state).get(ReactProgressControl.FRACTION);
		return fraction == null ? null : Double.valueOf(((Number) fraction).doubleValue());
	}

	/** An expression answering the given value, whatever it is called with. */
	private static QueryExecutor constant(Object value) {
		return new QueryExecutor() {
			@Override
			protected Object internalExecuteWith(EvalContext definitions, Args args) {
				return value;
			}

			@Override
			public SearchExpression getSearch() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected KnowledgeBase getKnowledgeBase() {
				return null;
			}

			@Override
			protected TLModel getTLModel() {
				return null;
			}

			@Override
			protected void internalDisableSecurity() {
				// Nothing to switch off, the expression accesses no data.
			}
		};
	}

	/** An expression answering the value it is called with. */
	private static QueryExecutor self() {
		return new QueryExecutor() {
			@Override
			protected Object internalExecuteWith(EvalContext definitions, Args args) {
				return args.value();
			}

			@Override
			public SearchExpression getSearch() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected KnowledgeBase getKnowledgeBase() {
				return null;
			}

			@Override
			protected TLModel getTLModel() {
				return null;
			}

			@Override
			protected void internalDisableSecurity() {
				// Nothing to switch off, the expression accesses no data.
			}
		};
	}

	/** The given TL-Script source as the configuration reads it. */
	private static Expr expr(String source) {
		try {
			return ExprFormat.INSTANCE.getValue("expr", source);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a TL-Script expression: " + source, ex);
		}
	}

	/**
	 * Suite requiring the {@link TypeIndex} the TL-Script compiler resolves against and the resource
	 * bundles a label is resolved with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestProgressElement.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
