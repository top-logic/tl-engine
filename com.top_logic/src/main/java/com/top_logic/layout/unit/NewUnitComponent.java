/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.unit;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.wrap.unit.Unit;
import com.top_logic.knowledge.wrap.unit.UnitWrapper;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link LayoutComponent} to create a {@link Unit} instance. It has form fields for
 * the name, format, sort order.
 * 
 * @author     <a href="mailto:TEH@top-logic.com">Tobias Ehrler</a>
 */
public class NewUnitComponent extends AbstractCreateComponent {

	/**
	 * Configuration for the {@link NewUnitComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		@StringDefault(NewUnitCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

	}

	public NewUnitComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }
	
	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext("NewUnitFormContext", getResPrefix());
        
		Constraint theNoDuplicates = new Constraint() {
            @Override
			public boolean check(Object aValue) throws CheckException {
				Unit theUnit = UnitWrapper.getInstance((String) aValue);

                if (theUnit != null) {
					throw new CheckException(Resources.getInstance().getString(getResPrefix().key("error.exists")));
                }
                
                return true;
            }
        
        };
		StringField theNameField = FormFactory.newStringField(UnitWrapper.NAME_ATTRIBUTE, "", MANDATORY, !IMMUTABLE, theNoDuplicates);
		formContext.addMember(theNameField);
        
		StringField theFormatField =
			FormFactory.newStringField(UnitWrapper.FORMAT, "#0.00", MANDATORY, !IMMUTABLE, null);
		formContext.addMember(theFormatField);
		
        formContext.addMember(FormFactory.newIntField(UnitWrapper.SORT_ORDER, null, MANDATORY, !IMMUTABLE, null));

        return formContext;
	}

	/** 
	 * Overridden to only support units.
	 * 
	 * @return Returns true if the argument is an instance of {@link Unit}.
	 */
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof Unit;
	}

}
