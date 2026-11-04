/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueListeners;
import com.top_logic.model.search.providers.ValueListenerByExpression;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for a {@link ValueListenerByExpression} annotated with {@link TLValueListeners} in a form
 * built by the {@link DefaultAttributeFormFactory}.
 */
@SuppressWarnings("javadoc")
public class TestValueListenerByExpression extends AbstractSearchExpressionTest {

	private static final String TYPE = "TestValueListenerByExpression:Project";

	private static final String REPOSITORY = "repository";

	private static final String BRANCH = "branch";

	private static final String LOG = "log";

	public void testFieldChangeResetsOtherAttribute() {
		AttributeFormContext form = new AttributeFormContext(ResPrefix.forTest("TestValueListenerByExpression"));
		TLFormObject project = form.createObject(type(), null);

		FormField repositoryField = field(form, project, REPOSITORY);
		FormField branchField = field(form, project, BRANCH);
		repositoryField.setValue("repo-a");
		branchField.setValue("main");
		assertEquals("main", project.tValue(part(BRANCH)));

		repositoryField.setValue("repo-b");

		// A string field displays the empty value as empty string.
		assertTrue("Listener resets the branch in the overlay.",
			StringServices.isEmpty(project.tValue(part(BRANCH))));
		assertTrue("Reset is shown in the field of the branch.", StringServices.isEmpty(branchField.getValue()));
		assertEquals("Listener receives old value, new value and attribute.", "repo-a->repo-b:repository",
			project.tValue(part(LOG)));
	}

	private static FormField field(AttributeFormContext form, TLFormObject overlay, String name) {
		AttributeUpdate update = overlay.newCreateUpdate(part(name));
		return (FormField) AttributeFormFactory.getInstance().toFormMember(update, form.getAttributeUpdateContainer());
	}

	private static TLStructuredTypePart part(String name) {
		return type().getPart(name);
	}

	private static TLClass type() {
		return (TLClass) TLModelUtil.findType(TYPE);
	}

	public static Test suite() {
		return suite(TestValueListenerByExpression.class);
	}

}
