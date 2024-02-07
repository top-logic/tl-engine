/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation.additional;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.util.Resources;

/**
 * {@link FormElementTemplateProvider} displaying the start page of the current person.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@TagName("current-start-page")
public class CurrentStartPage extends PersonTemplateProvider {

	@Override
	protected FormField createField(FormEditorContext context, String fieldName, Person account) {
		LayoutComponent startComponent = getStartPage(account);
		String startPageName = startPageName(startComponent);
		StringField startPageField = FormFactory.newStringField(fieldName, startPageName, FormFactory.IMMUTABLE);
		return startPageField;
	}

	private LayoutComponent getStartPage(Person account) {
		PersonalConfiguration pc = getPersonalConfiguration(account);
		if (pc == null) {
			return null;
		}
		Homepage homepage;
		try {
			homepage = pc.getHomepage(getMainLayout());
		} catch (ConfigurationException ex) {
			// Home page can not be resolved.
			return null;
		}
		if (homepage == null) {
			return null;
		}
		ComponentName startComponentName = homepage.getComponentName();
		if (startComponentName == null) {
			return null;
		}
		return getMainLayout().getComponentByName(startComponentName);
	}

	private MainLayout getMainLayout() {
		return MainLayout.getDefaultMainLayout();
	}

	private String startPageName(LayoutComponent startComponent) {
		if (startComponent == null) {
			return null;
		}
		CompoundSecurityLayout nearestCompoundLayout = CompoundSecurityLayout.getNearestCompoundLayout(startComponent);
		if (nearestCompoundLayout == null) {
			return null;
		}
		return Resources.getInstance().getString(nearestCompoundLayout.getTitleKey());
	}

	@Override
	protected String fieldName() {
		return "startPage";
	}

	@Override
	protected ResKey fieldLabel() {
		return I18NConstants.CURRENT_START_PAGE;
	}

	@Override
	public boolean isVisible(FormEditorContext context) {
		TLObject editedPerson = context.getModel();
		if (editedPerson != null) {
			PersonalConfiguration pc = getPersonalConfiguration((Person) editedPerson);
			if (pc != null) {
				return !pc.getStartPageAutomatism();
			}
		}
		return true;
	}

}

