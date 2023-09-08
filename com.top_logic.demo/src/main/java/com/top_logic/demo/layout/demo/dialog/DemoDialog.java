/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link FormComponent} demonstrating various dialogs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoDialog extends FormComponent {

	/**
	 * Configuration for a {@link DemoDialog}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/**
		 * A {@link DemoDialog} typically does not have a model.
		 */
		@BooleanDefault(true)
		@Override
		boolean getDisplayWithoutModel();

	}

	public DemoDialog(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext("form", getResPrefix());
		
		fc.addMember(FormFactory.newSelectField("select", Arrays.asList(new Object[] {"a", "b", "c"}), false, null, false));
		fc.addMember(FormFactory.newSelectField("selectMulti", Arrays.asList(new Object[] {"a", "b", "c"}), true, null, false));
		
		final List<String> option = Collections.singletonList("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
		for (int i = 0; i < 20; i++) {
			fc.addMember(FormFactory.newSelectField("select_" + i, option, false, option, false));
		}
		
		return fc;
	}

}
