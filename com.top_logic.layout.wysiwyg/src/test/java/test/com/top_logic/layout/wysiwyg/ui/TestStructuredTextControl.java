/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.html.SafeHTML;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControl;

/**
 * Test for {@link StructuredTextControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestStructuredTextControl extends TestControl {

	/**
	 * Tests that HTML entities are resolved, such that valid XML is contained in the field.
	 */
	public void testValidXMLFromHTML() {
		StringField field = FormFactory.newStringField("field");
		StructuredTextControl control = new StructuredTextControl(field);

		writeControl(control);
		executeControlCommand(control, "valueChanged",
			Collections.singletonMap("value",
				"<p>HTML entities: '&uuml;' '&euro;' XML entity: &amp; &quot;</p>"));

		assertEquals("<p>HTML entities: '\u00FC' '\u20AC' XML entity: &amp; \"</p>", field.getValue());
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestStructuredTextControl}.
	 */
	public static Test suite() {
		return suite(ServiceTestSetup.createSetup(TestStructuredTextControl.class,
			StructuredTextConfigService.Module.INSTANCE,
			SafeHTML.Module.INSTANCE));
	}

}

