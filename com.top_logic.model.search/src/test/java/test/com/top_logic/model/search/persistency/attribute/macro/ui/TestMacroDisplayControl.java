/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.persistency.attribute.macro.ui;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.macro.ui.MacroDisplayControl;
import com.top_logic.model.search.persistency.attribute.tempate.TemplateStorageMapping;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Test case for {@link MacroDisplayControl}.
 *
 * <p>
 * A macro is a rendering search expression producing HTML output as evaluation side-effect. When
 * called as a function value (as {@link MacroDisplayControl} does), the evaluation context looses its
 * output writer and the macro therefore returns an {@link com.top_logic.base.services.simpleajax.HTMLFragment}
 * instead of writing directly. {@link MacroDisplayControl} must render this returned fragment,
 * otherwise no output is produced at all.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMacroDisplayControl extends TestControl {

	private Person _user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
			.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("testMacroDisplayControl");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx =
			_user.tKnowledgeBase().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	public void testMacroWithModelAccess() {
		TLModel applicationModel = ModelService.getApplicationModel();
		Collection<TLModule> modules = applicationModel.getModules();
		assertTrue(!modules.isEmpty());

		SearchExpression macro = createMacro("<b>name: {$model.get(`tl.model:TLModule#name`)}</b>");

		becomeUser(PersonManager.getManager().getRoot());

		for (TLModule module : modules) {
			MacroDisplayControl control = createControl(module, macro);

			String output = writeControl(control);
			assertContains("<b>name: " + module.getName() + "</b>", output);
		}

		becomeUser(_user);

		for (TLModule module : modules) {
			MacroDisplayControl control = createControl(module, macro);

			String output = writeControl(control);

			assertContains("User has no right to see name of module.", "<b>name: " + "" + "</b>", output);
		}

	}

	/**
	 * Establishes a (non-system) person context for the given user.
	 *
	 * <p>
	 * Setting the person automatically derives a {@link SessionContext#PERSON_ID_PREFIX person}
	 * context id, so {@link ThreadContext#isSystemContext()} is {@code false} and the security
	 * check is not bypassed.
	 * </p>
	 */
	private static void becomeUser(Person person) {
		TLContext.getContext().setCurrentPerson(person);
	}

	public void testMacroOutput() {
		MacroDisplayControl control = createControl("<b>value: {1 + 2}</b>");

		String output = writeControl(control);

		assertContains("<b>value: 3</b>", output);
	}

	public void testEmptyMacro() {
		HiddenField field = FormFactory.newHiddenField("macro");
		MacroDisplayControl control = new MacroDisplayControl(null, field);

		// A macro without value must not fail and must produce the surrounding element only.
		String output = writeControl(control);

		assertContains("cMacro", output);
	}

	private MacroDisplayControl createControl(String macroSource) {
		return createControl(null, createMacro(macroSource));
	}

	private SearchExpression createMacro(String macroSource) {
		return TemplateStorageMapping.INSTANCE.getBusinessObject(macroSource);
	}

	private MacroDisplayControl createControl(TLObject self, SearchExpression macro) {
		HiddenField field = FormFactory.newHiddenField("macro", macro);
		return new MacroDisplayControl(self, field);
	}

	public static Test suite() {
		Test test = new TestSuite(TestMacroDisplayControl.class);
		test = ServiceTestSetup.createSetup(test, PersonManager.Module.INSTANCE);
		return TestControl.suite(test);
	}

}
