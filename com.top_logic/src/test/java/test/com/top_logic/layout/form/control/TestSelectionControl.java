/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.control;

import junit.framework.Test;

import test.com.top_logic.layout.TestControl;

import com.top_logic.layout.Control;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.resources.SimpleResourceView;

/**
 * Test case for {@link SelectionControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSelectionControl extends TestControl {

	public void testOptionLabelProvider() {
		FormContext context = new FormContext("fc", SimpleResourceView.INSTANCE);
		SelectField field = FormFactory.newSelectField("select", list("A", "B", "C"), true, false);
		field.setValue(list("B", "C"));
		field.setOptionLabelProvider(new LabelProvider() {
			@Override
			public String getLabel(Object object) {
				return "#" + object.toString() + "#";
			}

		});
		SelectFieldUtils.setMultiSelectionSeparator(field, ",", ", ");
		SelectFieldUtils.setCollectionSeparator(field, ", ");
		context.addMember(field);

		Control control = new SelectionControl(field);

		String editableDisplay = writeControl(control);
		assertTrue("Ticket #6779: Option label provider was not used: " + editableDisplay,
			editableDisplay.contains("#B#"));
		assertTrue("Ticket #6779: Option label provider was not used: " + editableDisplay,
			editableDisplay.contains("#C#"));

		field.setImmutable(true);

		String immutableDisplay = writeControl(control);
		assertTrue("Ticket #6779: Option label provider was not used: " + immutableDisplay,
			immutableDisplay.contains("#B#"));
		assertTrue("Ticket #6779: Option label provider was not used: " + immutableDisplay,
			immutableDisplay.contains("#C#"));

		SelectFieldUtils.setCollectionSeparator(field, " ;");

		immutableDisplay = writeControl(control);
		assertTrue("Ticket #18557: Separator for immutable mode not used: " + immutableDisplay,
			immutableDisplay.contains("#B#"));
		assertTrue("Ticket #18557: Separator for immutable mode not used: " + immutableDisplay,
			immutableDisplay.contains("#C#"));

	}

	public void testWriteFieldWithoutContext() {
		SelectField field = FormFactory.newSelectField("select", list("A", "B", "C"), true, false);
		SelectionControl control = new SelectionControl(field);

		writeControl(control);

		field.setDisabled(true);
		writeControl(control);

		field.setImmutable(true);
		writeControl(control);
	}

	public static Test suite() {
		return TestControl.suite(TestSelectionControl.class);
	}

}
