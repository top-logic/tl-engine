/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.layout.form.I18NConstants;

import com.top_logic.layout.form.constraints.AbstractDependency;
import com.top_logic.layout.form.constraints.AtMostOneFilledFieldDependency;
import com.top_logic.layout.form.constraints.OneFilledFieldDependency;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;

/**
 * Test {@link AbstractDependency}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDependencies extends BasicTestCase {

	public void testOneFilledFieldDependency() {
		StringField s1 = FormFactory.newStringField("s1");
		StringField s2 = FormFactory.newStringField("s2");
		StringField s3 = FormFactory.newStringField("s3");

		new OneFilledFieldDependency(s1, s2, s3).attach();

		FormContext ctx = new FormContext("name", I18NConstants.RESOURCE_PREFIX);

		ctx.addMember(s1);
		ctx.addMember(s2);
		ctx.addMember(s3);

		ctx.checkAll();
		assertTrue(s1.hasError());
		assertTrue(s2.hasError());
		assertTrue(s3.hasError());

		s1.setValue("v1");
		s2.setValue("v2");
		ctx.checkAll();
		assertTrue(!s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(!s3.hasError());

		s2.setValue(null);
		ctx.checkAll();
		assertTrue(!s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(!s3.hasError());

		s1.setValue(null);
		s3.setValue("v3");
		ctx.checkAll();
		assertTrue(!s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(!s3.hasError());

		s3.setValue(null);
		ctx.checkAll();
		assertTrue(s1.hasError());
		assertTrue(s2.hasError());
		assertTrue(s3.hasError());
	}

	public void testAtMostOneFilledFieldDependency() {
		StringField s1 = FormFactory.newStringField("s1");
		StringField s2 = FormFactory.newStringField("s2");
		StringField s3 = FormFactory.newStringField("s3");

		new AtMostOneFilledFieldDependency(s1, s2, s3).attach();

		FormContext ctx = new FormContext("name", I18NConstants.RESOURCE_PREFIX);

		ctx.addMember(s1);
		ctx.addMember(s2);
		ctx.addMember(s3);

		ctx.checkAll();
		assertTrue(!s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(!s3.hasError());

		s1.setValue("v1");
		ctx.checkAll();
		assertTrue(!s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(!s3.hasError());

		s2.setValue("v2");
		ctx.checkAll();
		assertTrue(s1.hasError());
		assertTrue(s2.hasError());
		assertTrue(!s3.hasError());

		s3.setValue("v3");
		ctx.checkAll();
		assertTrue(s1.hasError());
		assertTrue(s2.hasError());
		assertTrue(s3.hasError());

		s2.setValue(null);
		ctx.checkAll();
		assertTrue(s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(s3.hasError());

		s1.setValue(null);
		ctx.checkAll();
		assertTrue(!s1.hasError());
		assertTrue(!s2.hasError());
		assertTrue(!s3.hasError());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDependencies.class);
	}

}
