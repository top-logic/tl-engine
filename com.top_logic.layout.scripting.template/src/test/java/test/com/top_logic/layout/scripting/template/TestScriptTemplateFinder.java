/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.layout.scripting.template.ScriptTemplateFinder;
import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Test for the {@link ScriptTemplateFinder}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestScriptTemplateFinder extends TestCase {

	private static final String MARKER_NO_BUSINESS_OBJECT = "no business object";

	private static final String MARKER_NO_BUSINESS_ACTION = "no business action";

	private static final String NAME_TESTED_CLASS = ScriptTemplateFinder.class.getSimpleName();

	public void testNoObjectAndNoAction() throws IOException {
		String objectName = null;
		String actionName = null;
		String scriptLocation = findScriptFor(objectName, actionName);
		TemplateSource scriptFile = resolve(scriptLocation);
		String scriptContent = load(scriptFile);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			MARKER_NO_BUSINESS_OBJECT, scriptContent);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			MARKER_NO_BUSINESS_ACTION, scriptContent);
	}

	public void testNonExistingObjectAndNoAction() {
		String objectName = "NonExistingObject";
		String actionName = null;
		assertNonExisting(objectName, actionName);
	}

	public void testExistingObjectAndNoAction() throws IOException {
		String objectName = "BusinessObjectA";
		String actionName = null;
		String scriptLocation = findScriptFor(objectName, actionName);
		TemplateSource scriptFile = resolve(scriptLocation);
		String scriptContent = load(scriptFile);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			objectName, scriptContent);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			MARKER_NO_BUSINESS_ACTION, scriptContent);
	}

	public void testNoObjectAndNonExistingAction() {
		String objectName = null;
		String actionName = "NonExistingAction";
		assertNonExisting(objectName, actionName);
	}

	public void testNoObjectAndExistingAction() throws IOException {
		String objectName = null;
		String actionName = "BusinessActionA";
		String scriptLocation = findScriptFor(objectName, actionName);
		TemplateSource scriptFile = resolve(scriptLocation);
		String scriptContent = load(scriptFile);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			MARKER_NO_BUSINESS_OBJECT, scriptContent);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			actionName, scriptContent);
	}

	public void testNonExistingObjectAndNonExistingAction() {
		String objectName = "NonExistingObject";
		String actionName = "NonExistingAction";
		assertNonExisting(objectName, actionName);
	}

	public void testExistingObjectAndNonExistingAction() {
		String objectName = "BusinessObjectA";
		String actionName = "NonExistingAction";
		assertNonExisting(objectName, actionName);
	}

	public void testNonExistingObjectAndExistingAction() {
		String objectName = "NonExistingObject";
		String actionName = "BusinessActionA";
		assertNonExisting(objectName, actionName);
	}

	public void testExistingObjectAndExistingAction() throws IOException {
		String objectName = "BusinessObjectA";
		String actionName = "BusinessActionA";
		String scriptLocation = findScriptFor(objectName, actionName);
		TemplateSource scriptFile = resolve(scriptLocation);
		String scriptContent = load(scriptFile);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			objectName, scriptContent);
		assertContains("The " + NAME_TESTED_CLASS + " returned the wrong script file.",
			actionName, scriptContent);
	}

	/**
	 * @see ScriptTemplateFinder#findScriptResourceFor(String, String)
	 */
	private String findScriptFor(String businessObject, String businessAction) {
		return ScriptTemplateFinder.getInstance().findScriptResourceFor(businessObject, businessAction);
	}

	private void assertNonExisting(String objectName, String actionName) {
		TemplateSource scriptFile;
		try {
			String scriptLocation = findScriptFor(objectName, actionName);
			scriptFile = resolve(scriptLocation);
		} catch (RuntimeException ex) {
			String message = NAME_TESTED_CLASS + " tried to find a non-existing script.";
			Pattern expectation = Pattern.compile("Failed to access the file");
			BasicTestCase.assertErrorMessage(message, expectation, ex);
			return;
		}
		String message = NAME_TESTED_CLASS + " returned an existing file"
			+ " for a script that should not exist: " + scriptFile;
		try {
			scriptFile.getContent();
			fail(message);
		} catch (IOException ex) {
			// Expected.
		}
	}

	private TemplateSource resolve(String scriptLocation) {
		TemplateSource scriptFile = TemplateSourceFactory.getInstance().resolve(scriptLocation);
		return scriptFile;
	}

	private String load(TemplateSource scriptFile) throws IOException, UnsupportedEncodingException {
		String scriptContent = StreamUtilities.readAllFromStream(scriptFile.getContent(), StringServices.CHARSET_UTF_8);
		return scriptContent;
	}

	/** Creates the {@link Test} suite containing all the actual {@link Test}s of this class. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(
			TestScriptTemplateFinder.class, ScriptTemplateFinder.Module.INSTANCE));
	}

}
