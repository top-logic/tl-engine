/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.layout.react.control.select.OptionOrder;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.providers.OptionsByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link AttributeOptions#orderFor(TLStructuredTypePart)} - the order the options of an
 * attribute are offered in, decided by the source of the options.
 */
public class TestAttributeOptionsOrder extends TestCase {

	/** Name of the module the types under test live in. */
	private static final String MODULE = "test.order";

	/** The expression computing the options of the annotated attributes. */
	private static final String OPTIONS_FUNCTION = "x -> []";

	private TLModelImpl _model;

	private TLModule _module;

	private TLClass _row;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_model = new TLModelImpl();
		_module = TLModelUtil.addModule(_model, MODULE);
		_row = _model.addClass(_module, _module, "Row");
	}

	@Override
	protected void tearDown() throws Exception {
		_row = null;
		_module = null;
		_model = null;

		super.tearDown();
	}

	/**
	 * Runs the test in an interaction: an options expression is compiled against the application
	 * model, which reads the knowledge base.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContextManager.<Void, Throwable, RuntimeException> inSystemInteraction(TestAttributeOptionsOrder.class,
			() -> {
				super.runTest();
				return null;
			});
	}

	/** The classifiers of an enumeration are offered in their own order. */
	public void testAnEnumerationKeepsTheOrderOfItsClassifiers() {
		TLEnumeration status = _model.addEnumeration(_module, _module, "Status");
		TLModelUtil.addClassifier(status, "open");
		TLModelUtil.addClassifier(status, "closed");

		assertNull(AttributeOptions.orderFor(_model.addClassProperty(_row, "status", status)));
		assertNull(AttributeOptions.orderFor(status));
	}

	/** The instances of a class have no order of their own and are sorted by their labels. */
	public void testAReferenceSortsItsOptionsByLabel() {
		assertNotNull(AttributeOptions.orderFor(_model.addClassProperty(_row, "other", _row)));
		assertNotNull(AttributeOptions.orderFor(_row));
	}

	/** A configured {@link OptionOrder} sorts by label or keeps the order as given. */
	public void testAConfiguredOrderStandsForItsComparator() {
		assertNotNull(AttributeOptions.comparator(OptionOrder.BY_LABEL));
		assertNull(AttributeOptions.comparator(OptionOrder.AS_GIVEN));
	}

	/** An options expression that is not asked to sort keeps the order it computes. */
	public void testAnOptionsExpressionKeepsItsOrderWhereAsked() throws Exception {
		TLStructuredTypePart part = _model.addClassProperty(_row, "computed", _row);
		part.setAnnotation(options(false));

		assertNull(AttributeOptions.orderFor(part));
	}

	/** An options expression sorts its options alphabetically by default. */
	public void testAnOptionsExpressionSortsByDefault() throws Exception {
		TLStructuredTypePart part = _model.addClassProperty(_row, "sorted", _row);
		part.setAnnotation(options(null));

		assertNotNull(AttributeOptions.orderFor(part));
	}

	/**
	 * An options annotation computing the options by {@link #OPTIONS_FUNCTION}.
	 *
	 * @param alphabetical
	 *        Whether the expression is asked to sort its options alphabetically, or {@code null} to
	 *        leave that to its default.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static TLOptions options(Boolean alphabetical) throws ConfigurationException {
		OptionsByExpression.Config<?> generator = TypedConfiguration.newConfigItem(OptionsByExpression.Config.class);
		generator.update(generator.descriptor().getProperty(OptionsByExpression.Config.FUNCTION),
			ExprFormat.INSTANCE.getValue(OptionsByExpression.Config.FUNCTION, OPTIONS_FUNCTION));
		if (alphabetical != null) {
			generator.update(generator.descriptor().getProperty(OptionsByExpression.Config.ALPHABETICAL_ORDER),
				alphabetical);
		}
		TLOptions result = TypedConfiguration.newConfigItem(TLOptions.class);
		result.setGenerator((PolymorphicConfiguration) generator);
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, the services a model part consults while
	 * answering for an annotation, and the model an options expression is compiled against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestAttributeOptionsOrder.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE));
	}

}
