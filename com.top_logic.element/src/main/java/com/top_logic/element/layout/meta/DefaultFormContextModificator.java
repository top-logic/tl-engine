/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.DefaultTableFieldConfigurator;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.component.TableFieldConfigurator;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/** 
 * Default {@link FormContextModificator} implementation that does no modifications at all.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DefaultFormContextModificator extends DefaultTableFieldConfigurator implements FormContextModificator {

	/** Singleton {@link DefaultFormContextModificator}. */
    public static final FormContextModificator INSTANCE = new DefaultFormContextModificator();

    @Override
	public boolean preModify(LayoutComponent component, TLClass type, TLObject anAttributed) {
        return true;
    }

    @Override
	public void postModify(LayoutComponent component, TLClass type, TLObject anAttributed, AttributeFormContext aContext, FormContainer currentGroup) {
    	// No modification.
    }

    @Override
	public void modify(LayoutComponent component, String aName, FormMember aMember, TLStructuredTypePart aMA, TLClass type, TLObject aAnAttributed, AttributeUpdate aAnUpdate, AttributeFormContext aContext, FormContainer currentGroup) {
    	// No modification.
    }
    
    @Override
	public void clear(LayoutComponent component, TLClass type,
    		TLObject anAttributed, AttributeUpdateContainer aContainer,
    		FormContainer currentGroup) {
    	// Nothing to clear.
    }

	/**
	 * @see FormComponent#makeConfigurable(SelectField)
	 */
	protected final void makeConfigurable(LayoutComponent component, SelectField field) {
		configurator(component).makeConfigurable(field);
	}

	/**
	 * @see FormComponent#makeConfigurable(SelectField, String)
	 */
	protected final void makeConfigurable(LayoutComponent component, SelectField field, final String configName) {
		configurator(component).makeConfigurable(field, configName);
	}

	/**
	 * @see FormComponent#adaptTableConfiguration(String, TableConfiguration)
	 */
	protected final void adaptTableConfiguration(LayoutComponent component, String name, TableConfiguration table) {
		configurator(component).adaptTableConfiguration(name, table);
	}

	/**
	 * @see FormComponent#lookupTableConfigurationBuilder(String)
	 */
	protected final TableConfigurationProvider lookupTableConfigurationBuilder(LayoutComponent component, String name) {
		return configurator(component).lookupTableConfigurationBuilder(name);
	}

	private TableFieldConfigurator configurator(LayoutComponent component) {
		if (component instanceof TableFieldConfigurator) {
			return (TableFieldConfigurator) component;
		} else {
			return this;
		}
	}

}