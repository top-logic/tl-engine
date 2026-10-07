/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.AnnotatedSearchExpression;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.expr.ExprStorageMapping;
import com.top_logic.model.search.react.TLScriptEditorReactControl;
import com.top_logic.model.search.react.TLScriptFieldControlProvider;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests editing an attribute of the model type {@code tl.model.search:Expr} with the
 * {@link TLScriptFieldControlProvider}: the attribute holds the compiled {@link SearchExpression},
 * while the editor works on its source.
 */
public class TestTLScriptAttributeField extends AbstractSearchExpressionTest {

	/** The command the editor reports an edit of the user with. */
	private static final String CMD_VALUE_CHANGED = "valueChanged";

	/** The state key of whether the editor takes no input. */
	private static final String READ_ONLY = "readOnly";

	/** The argument and state key of the edited text. */
	private static final String VALUE = "value";

	private static final String SCRIPT_TYPE = "tl.model.search:Expr";

	private static final String TEMPLATE_TYPE = "tl.model.search:Template";

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		super.tearDown();
	}

	/**
	 * An attribute of the script type is edited in the script editor, in a form and - behind an
	 * opener - in a table cell.
	 */
	public void testScriptAttributeIsEditedAsScript() {
		TLType type = TLModelUtil.findType(SCRIPT_TYPE);

		ReactControl formControl = FieldControlService.getInstance()
			.createFieldControl(_context, type, scriptField(), new AbstractFieldModel(null));
		assertTrue("Got " + formControl.getClass().getName(), formControl instanceof TLScriptEditorReactControl);

		ReactControl cellControl = FieldControlService.getInstance()
			.createFieldControl(_context, type, scriptField().setCompact(true), new AbstractFieldModel(null));
		assertTrue("Got " + cellControl.getClass().getName(), cellControl instanceof ReactCompactFieldControl);
	}

	/**
	 * A template holds a compiled script, too, but is no script: it is not edited in the script
	 * editor.
	 */
	public void testTemplateIsNotEditedAsScript() {
		TLType type = TLModelUtil.findType(TEMPLATE_TYPE);

		ReactControl control = FieldControlService.getInstance()
			.createFieldControl(_context, type, scriptField(), new AbstractFieldModel(null));
		assertFalse(control instanceof TLScriptEditorReactControl);
	}

	/**
	 * The editor shows the source of the stored script, and an entered script is stored the way the
	 * attribute stores it - and comes back from the storage unchanged.
	 */
	public void testRoundTrip() {
		AbstractFieldModel model = new AbstractFieldModel(ExprStorageMapping.INSTANCE.getBusinessObject("1 + 2"));
		ReactControl control = new TLScriptFieldControlProvider().createControl(_context, scriptField(), model);

		assertEquals("1 + 2", control.scriptingScalarState().get(VALUE));

		control.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, "3 * 4"));

		assertFalse(model.hasError());
		Object value = model.getValue();
		assertTrue("Got " + value, value instanceof AnnotatedSearchExpression);
		Object stored = ExprStorageMapping.INSTANCE.getStorageObject(value);
		assertEquals("3 * 4", stored);

		SearchExpression reloaded = ExprStorageMapping.INSTANCE.getBusinessObject(stored);
		ReactControl reopened = new TLScriptFieldControlProvider()
			.createControl(_context, scriptField(), new AbstractFieldModel(reloaded));
		assertEquals("3 * 4", reopened.scriptingScalarState().get(VALUE));
		assertEquals("3 * 4", new TLScriptFieldControlProvider().previewText(scriptField(), reloaded));
	}

	/**
	 * A script that cannot be read is reported and leaves the value untouched.
	 */
	public void testInvalidScriptIsReported() {
		SearchExpression original = ExprStorageMapping.INSTANCE.getBusinessObject("1 + 2");
		AbstractFieldModel model = new AbstractFieldModel(original);
		ReactControl control = new TLScriptFieldControlProvider().createControl(_context, scriptField(), model);

		control.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, "x -> ("));

		assertTrue("The input error keeps a form from saving and a dialog from confirming", model.hasError());
		assertNotNull(model.getError());
		assertSame(original, model.getValue());

		control.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, "x -> $x"));

		assertFalse("A corrected script clears the error", model.hasError());
		assertEquals("x -> $x", ExprStorageMapping.INSTANCE.getStorageObject(model.getValue()));
	}

	/**
	 * A value replacing the one the user could not enter clears the input error: the editor shows
	 * that value now.
	 */
	public void testNewValueClearsInputError() {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = new TLScriptFieldControlProvider().createControl(_context, scriptField(), model);

		control.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, "1 +"));
		assertTrue(model.hasError());

		model.setValue(ExprStorageMapping.INSTANCE.getBusinessObject("2"));
		assertFalse(model.hasError());
		assertEquals("2", control.scriptingScalarState().get(VALUE));
	}

	/**
	 * The editor accepts input only while the field is editable, and follows changes of its
	 * editability.
	 */
	public void testEditorFollowsEditability() {
		SearchExpression original = ExprStorageMapping.INSTANCE.getBusinessObject("1");
		AbstractFieldModel model = new AbstractFieldModel(original);
		model.setEditable(false);
		ReactControl control = new TLScriptFieldControlProvider().createControl(_context, scriptField(), model);
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get(READ_ONLY));

		control.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, "2"));
		assertSame("A read-only editor takes no input", original, model.getValue());

		model.setEditable(true);
		assertEquals(Boolean.FALSE, control.scriptingScalarState().get(READ_ONLY));
		control.executeCommand(CMD_VALUE_CHANGED, Map.of(VALUE, "2"));
		assertEquals("2", ExprStorageMapping.INSTANCE.getStorageObject(model.getValue()));

		model.setEditable(false);
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get(READ_ONLY));
	}

	private static FieldSpec scriptField() {
		return FieldSpec.of(SearchExpression.class, "Script");
	}

	/**
	 * Test suite with the model the scripts are compiled against and the service resolving the
	 * controls.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTLScriptAttributeField.class,
			ServiceTestSetup.createStarterFactoryForModules(getModules(FieldControlService.Module.INSTANCE)));
	}

}
