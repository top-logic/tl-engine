/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.TLModule;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Tests the label, tooltip and icon a {@link ViewCommandModel} computes from the arguments of its
 * command ({@link ViewCommand.Config#getLabelExpr()}, {@link ViewCommand.Config#getTooltipExpr()},
 * {@link ViewCommand.Config#getImageExpr()}).
 *
 * <p>
 * The computed values follow the input channel and the object it holds, like the executability
 * does; changes of the object are committed to the knowledge base and delivered by a
 * {@link GlobalModelEventForwarder}, which is also the scope the model attaches to.
 * </p>
 */
public class TestViewCommandAppearance extends BasicTestCase {

	/** The fixed label of a command computing its label. */
	private static final ResKey FIXED_LABEL = ResKey.text("Fixed label");

	/** The fixed tooltip of a command computing its tooltip. */
	private static final ResKey FIXED_TOOLTIP = ResKey.text("Fixed tooltip");

	/** The fixed icon of a command computing its icon. */
	private static final String FIXED_IMAGE = "css:bi bi-star";

	/** Name of the module whose name the label of a command shows. */
	private static final String MODULE_NAME = "test.appearance";

	/** Name the module is renamed to. */
	private static final String RENAMED = "test.renamed";

	/** The channel holding the main argument of the command. */
	private ViewChannel _input;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The number of state changes the model reported. */
	private int _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_input = new DefaultViewChannel("input");
		_reported = 0;

		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), kb().getUpdateChain(), relevance);
	}

	@Override
	protected void tearDown() throws Exception {
		_scope = null;
		_input = null;

		super.tearDown();
	}

	/**
	 * Tests that label, tooltip and icon follow a new value of the input channel.
	 */
	public void testFollowsTheChannel() throws Exception {
		_input.set("a");
		ViewCommandModel model = attachedModel(computing("'Label ' + $input", "'Tip ' + $input",
			"'css:bi bi-' + $input"), ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		assertEquals("Label a", model.getLabel());
		assertEquals("Tip a", model.getTooltip());
		assertEquals(icon("css:bi bi-a"), model.getImage());

		_input.set("b");

		assertEquals("Label b", model.getLabel());
		assertEquals("Tip b", model.getTooltip());
		assertEquals(icon("css:bi bi-b"), model.getImage());
		assertEquals("The button was told once that what it shows changed.", 1, _reported);
	}

	/**
	 * Tests that label, tooltip and icon follow an attribute stored on the object the channel
	 * holds, while the channel keeps pointing to the same object.
	 */
	public void testFollowsTheObject() throws Exception {
		TLModule module;
		try (Transaction tx = kb().beginTransaction()) {
			module = TLModelUtil.addModule(ModelService.getApplicationModel(), MODULE_NAME);
			tx.commit();
		}
		try {
			_scope.synthesizeModelEvents();
			_input.set(module);
			String name = "$input.get(`tl.model:TLNamedPart#name`)";
			ViewCommandModel model = attachedModel(computing(name, "'Tip ' + " + name,
				"'css:bi bi-' + " + name), ViewExecutabilityRule.ALWAYS_EXECUTABLE);

			assertEquals(MODULE_NAME, model.getLabel());
			assertEquals("Tip " + MODULE_NAME, model.getTooltip());
			assertEquals(icon("css:bi bi-" + MODULE_NAME), model.getImage());

			try (Transaction tx = kb().beginTransaction()) {
				module.setName(RENAMED);
				tx.commit();
			}
			_scope.synthesizeModelEvents();

			assertEquals(RENAMED, model.getLabel());
			assertEquals("Tip " + RENAMED, model.getTooltip());
			assertEquals(icon("css:bi bi-" + RENAMED), model.getImage());
			assertEquals("The button was told once that what it shows changed.", 1, _reported);
			model.detach();
		} finally {
			try (Transaction tx = kb().beginTransaction()) {
				TLModelUtil.deleteRecursive(module);
				tx.commit();
			}
		}
	}

	/**
	 * Tests that an expression yielding nothing - no value, or an empty text - falls back to the
	 * fixed value.
	 */
	public void testFallsBackToFixedValues() throws Exception {
		ViewCommand.Config config = computing(
			"if($input == 'computed', 'Computed label', if($input == 'empty', '', null))",
			"if($input == 'computed', 'Computed tooltip', if($input == 'empty', '', null))",
			"if($input == 'computed', 'css:bi bi-a', if($input == 'empty', '', null))");
		withFixedValues(config);
		_input.set("computed");
		ViewCommandModel model = attachedModel(config, ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		assertEquals("Computed label", model.getLabel());
		assertEquals("Computed tooltip", model.getTooltip());
		assertEquals(icon("css:bi bi-a"), model.getImage());

		_input.set("none");
		assertFixedValues(model);

		_input.set("empty");
		assertFixedValues(model);
	}

	/**
	 * Tests that a new channel value is not reported while neither state, label, tooltip nor icon
	 * change, but is reported when only the label changes.
	 */
	public void testUnchangedAppearanceIsNotReported() throws Exception {
		_input.set("a1");
		ViewCommandModel model = attachedModel(computing("subString($input, 0, 1)", "'Tip'", "'css:bi bi-a'"),
			ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		_input.set("a2");
		assertEquals("a", model.getLabel());
		assertEquals("Nothing the button shows changed.", 0, _reported);

		_input.set("b1");
		assertEquals("b", model.getLabel());
		assertEquals("A changed label alone is reported.", 1, _reported);
	}

	/**
	 * Tests that a disabled command explains its reason instead of the computed tooltip.
	 */
	public void testDisabledReasonTakesPrecedence() throws Exception {
		ViewCommandModel model =
			attachedModel(computing(null, "'Tip ' + $input", null), NullInputDisabled.INSTANCE);

		assertEquals("A disabled command explains its reason.",
			Resources.getInstance().getString(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey()),
			model.getTooltip());

		_input.set("a");
		assertEquals("An executable command shows its computed tooltip.", "Tip a", model.getTooltip());
	}

	/**
	 * Tests that a UI supplying the main argument itself - a button on every row - gets the label,
	 * tooltip and icon of each row.
	 */
	public void testPerInput() throws Exception {
		ViewCommandModel model = attachedModel(computing("'Row ' + $input", "'Tip ' + $input",
			"'css:bi bi-' + $input"), ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		assertEquals("Row r1", model.label("r1"));
		assertEquals("Row r2", model.label("r2"));
		assertEquals("Tip r1", model.tooltip("r1"));
		assertEquals("Tip r2", model.tooltip("r2"));
		assertEquals(icon("css:bi bi-r1"), model.image("r1"));
		assertEquals(icon("css:bi bi-r2"), model.image("r2"));
	}

	/**
	 * Tests that an icon expression yielding image data shows it, and that a result that is no
	 * icon - a binary that is no image, a number - falls back to the fixed icon.
	 */
	public void testImageDataAndNonIcon() throws Exception {
		ViewCommand.Config config = computing(null, null, "$input");
		withFixedValues(config);
		BinaryData png = BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3 }, "image/png", "icon.png");
		_input.set(png);
		ViewCommandModel model = attachedModel(config, ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		assertEquals("Image data is shown.", ThemeImage.data(png), model.getImage());

		_input.set(BinaryDataFactory.createBinaryData(new byte[] { 4, 5, 6 }, "application/pdf", "doc.pdf"));
		assertEquals("A binary that is no image is no icon.", icon(FIXED_IMAGE), model.getImage());

		_input.set(42);
		assertEquals("A number is no icon.", icon(FIXED_IMAGE), model.getImage());

		_input.set(icon("css:bi bi-a"));
		assertEquals("An icon is shown.", icon("css:bi bi-a"), model.getImage());
	}

	/**
	 * A model of the given configuration and rule over {@link #_input}, attached to {@link #_scope}
	 * and counting the state changes it reports in {@link #_reported} after the attachment.
	 */
	private ViewCommandModel attachedModel(ViewCommand.Config config, ViewExecutabilityRule rule) {
		ViewCommandModel model = new ViewCommandModel(null, (context, input) -> {}, config, _input, rule);
		model.attach(_scope);
		model.addStateChangeListener(() -> _reported++);
		return model;
	}

	/**
	 * A command configuration computing its label, tooltip and icon by the given expressions,
	 * {@code null} for none.
	 */
	private static ViewCommand.Config computing(String label, String tooltip, String image) {
		ViewCommand.Config config = TypedConfiguration.newConfigItem(ViewCommand.Config.class);
		if (label != null) {
			TypedConfigUtil.setProperty(config, ViewCommand.Config.LABEL_EXPR, expr(label));
		}
		if (tooltip != null) {
			TypedConfigUtil.setProperty(config, ViewCommand.Config.TOOLTIP_EXPR, expr(tooltip));
		}
		if (image != null) {
			TypedConfigUtil.setProperty(config, ViewCommand.Config.IMAGE_EXPR, expr(image));
		}
		return config;
	}

	/**
	 * Configures {@link #FIXED_LABEL}, {@link #FIXED_TOOLTIP} and {@link #FIXED_IMAGE}.
	 */
	private static void withFixedValues(ViewCommand.Config config) {
		TypedConfigUtil.setProperty(config, ViewCommand.Config.LABEL, FIXED_LABEL);
		TypedConfigUtil.setProperty(config, ViewCommand.Config.TOOLTIP, FIXED_TOOLTIP);
		TypedConfigUtil.setProperty(config, ViewCommand.Config.IMAGE, icon(FIXED_IMAGE));
	}

	private static void assertFixedValues(ViewCommandModel model) {
		assertEquals(Resources.getInstance().getString(FIXED_LABEL), model.getLabel());
		assertEquals(Resources.getInstance().getString(FIXED_TOOLTIP), model.getTooltip());
		assertEquals(icon(FIXED_IMAGE), model.getImage());
	}

	private static ThemeImage icon(String encoded) {
		return ThemeImage.internalDecode(encoded);
	}

	private static Expr expr(String source) {
		try {
			return ExprFormat.INSTANCE.getValue("expr", source);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a TL-Script expression: " + source, ex);
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The expressions are compiled and evaluated against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestViewCommandAppearance.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
