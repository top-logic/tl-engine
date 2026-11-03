/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.providers.LabelProviderByExpression;
import com.top_logic.model.search.providers.OptionsByExpression;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Tests for the {@link OptionsByExpression.Config#getOptionLabels() labels of the options} of an
 * attribute: a text option that is a key is displayed by a label of its own while the value stays
 * the key - in the select field, in its read-only display, and in a table column.
 */
public class TestOptionLabels extends TestCase {

	/** Name of the module the types under test live in. */
	private static final String MODULE = "test.optionLabels";

	/** The key of an option whose label sorts after the label of {@link #GITEA}. */
	private static final String GIT = "git";

	/** The key of an option whose label sorts before the label of {@link #GIT}. */
	private static final String GITEA = "gitea";

	/** The label of {@link #GIT}. */
	private static final String GIT_LABEL = "Plain Git server";

	/** The label of {@link #GITEA}. */
	private static final String GITEA_LABEL = "Gitea";

	/** The expression computing the options of the annotated attributes. */
	private static final String OPTIONS_FUNCTION = "x -> [\"" + GITEA + "\", \"" + GIT + "\"]";

	/** The expression computing the label of an option. */
	private static final String LABEL_FUNCTION =
		"key -> if($key == \"" + GIT + "\", \"" + GIT_LABEL + "\", \"" + GITEA_LABEL + "\")";

	private ReactContext _context;

	private FieldControlService _controls;

	private TLModelImpl _model;

	private TLClass _row;

	private TLPrimitive _text;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		FieldControlService.Config config = TypedConfiguration.newConfigItem(FieldControlService.Config.class);
		config.setImplementationClass(FieldControlService.class);
		_controls = (FieldControlService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);

		_model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(_model, MODULE);
		_row = TLModelUtil.addClass(module, "Row");
		_text = TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
	}

	@Override
	protected void tearDown() throws Exception {
		_text = null;
		_row = null;
		_model = null;
		_controls = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * Runs the test in the system context: an options expression is compiled against the application
	 * model, which reads the knowledge base.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> inSystemContext(TestOptionLabels.class,
			() -> {
				super.runTest();
				return null;
			});
	}

	/** Options without labels of their own are labeled by themselves. */
	public void testOptionsWithoutLabelsAreLabeledByThemselves() throws Exception {
		assertNull(AttributeOptions.labelsFor(attribute("plain", false, true)));
		assertNull(AttributeOptions.labelsFor(TLModelUtil.addProperty(_row, "free", _text)));
	}

	/** The labels of the options label each key. */
	public void testTheOptionLabelsLabelTheKeys() throws Exception {
		LabelProvider labels = AttributeOptions.labelsFor(attribute("provider", true, true));

		assertNotNull(labels);
		assertEquals(GIT_LABEL, labels.getLabel(GIT));
		assertEquals(GITEA_LABEL, labels.getLabel(GITEA));
	}

	/** Options sorted alphabetically are sorted by their labels, not by their keys. */
	@SuppressWarnings("unchecked")
	public void testAlphabeticalOrderSortsByTheLabels() throws Exception {
		Comparator<Object> order = (Comparator<Object>) AttributeOptions.orderFor(attribute("sorted", true, true));

		assertNotNull(order);
		assertTrue("The label of '" + GITEA + "' comes first, although its key comes last.",
			order.compare(GITEA, GIT) < 0);
	}

	/** The read-only display of a value shows the label of the option, not its key. */
	public void testTheReadOnlyDisplayShowsTheLabel() throws Exception {
		TLStructuredTypePart part = attribute("display", true, false);

		assertEquals(List.of(GIT_LABEL), valueLabels(_controls.createDisplayControl(_context, part, GIT)));
	}

	/** The edited field shows its value and its options by their labels. */
	public void testTheEditedFieldShowsTheLabels() throws Exception {
		TLStructuredTypePart part = attribute("edited", true, false);
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(GITEA, List.of(GITEA, GIT), false);

		ReactControl control = _controls.createFieldControl(_context, part, model);

		assertEquals(List.of(GITEA_LABEL), valueLabels(control));
	}

	/** A table column of the attribute shows the label and is searched by it. */
	public void testATableColumnIsSearchedByTheLabel() throws Exception {
		TLStructuredTypePart part = attribute("column", true, false);

		Column<Object, ?> column = ColumnProviderService.getInstance()
			.createColumn("column", ResKey.text("column"), ColumnType.of(part), row -> row);

		assertEquals(GIT_LABEL, column.searchText(GIT));
	}

	/**
	 * The labels of the selected values the given select control hands to its client.
	 */
	private static List<String> valueLabels(ReactControl control) {
		assertTrue("A text chosen from options is displayed by a select control, but is displayed by "
			+ control.getClass(), control instanceof ReactDropdownSelectControl);
		Map<String, Object> state = control.scriptingScalarState();
		List<String> result = new ArrayList<>();
		for (Object option : (List<?>) state.get(FieldState.VALUE__PROP)) {
			result.add((String) ((Map<?, ?>) option).get(DropdownSelectState.Option.LABEL__PROP));
		}
		return result;
	}

	/**
	 * A text attribute of the type under test whose options are computed by
	 * {@link #OPTIONS_FUNCTION}.
	 *
	 * @param labeled
	 *        Whether the options are labeled by {@link #LABEL_FUNCTION}.
	 * @param alphabetical
	 *        Whether the options are sorted alphabetically.
	 */
	private TLStructuredTypePart attribute(String name, boolean labeled, boolean alphabetical)
			throws ConfigurationException {
		TLStructuredTypePart part = TLModelUtil.addProperty(_row, name, _text);
		part.setAnnotation(options(labeled, alphabetical));
		return part;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static TLOptions options(boolean labeled, boolean alphabetical) throws ConfigurationException {
		OptionsByExpression.Config<?> generator = TypedConfiguration.newConfigItem(OptionsByExpression.Config.class);
		generator.update(generator.descriptor().getProperty(OptionsByExpression.Config.FUNCTION),
			ExprFormat.INSTANCE.getValue(OptionsByExpression.Config.FUNCTION, OPTIONS_FUNCTION));
		generator.update(generator.descriptor().getProperty(OptionsByExpression.Config.ALPHABETICAL_ORDER),
			Boolean.valueOf(alphabetical));
		if (labeled) {
			LabelProviderByExpression.Config<?> labels =
				TypedConfiguration.newConfigItem(LabelProviderByExpression.Config.class);
			labels.update(labels.descriptor().getProperty(LabelProviderByExpression.Config.LABEL),
				ExprFormat.INSTANCE.getValue(LabelProviderByExpression.Config.LABEL, LABEL_FUNCTION));
			generator.update(generator.descriptor().getProperty(OptionsByExpression.Config.OPTION_LABELS), labels);
		}
		TLOptions result = TypedConfiguration.newConfigItem(TLOptions.class);
		result.setGenerator((PolymorphicConfiguration) generator);
		return result;
	}

	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite requiring the services a model part consults while answering for an annotation,
	 * the model an expression is compiled against, the {@link LabelProviderService} and the
	 * {@link ModelResolver} of the select control, and the {@link ColumnProviderService} building
	 * the column under test.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestOptionLabels.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
				ModelResolver.Module.INSTANCE, ColumnProviderService.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}

}
