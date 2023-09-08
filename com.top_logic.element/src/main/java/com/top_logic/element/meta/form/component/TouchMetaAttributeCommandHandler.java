/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.ValidityCheck;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InEditModeExecutable;
import com.top_logic.tool.execution.NegateRule;

/**
 * Touches an attribute.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TouchMetaAttributeCommandHandler extends AJAXCommandHandler {

    public static final String COMMAND_ID = "touchMetaAttribute";

    public static final String PARAMETER_FIELD = "field";

    public static final String[] PARAMETERS = new String[] {
        PARAMETER_FIELD
    };


    

    public TouchMetaAttributeCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    

    


    @Override
	public String[] getAttributeNames() {
        return PARAMETERS;
    }

    @Override
	public boolean needsConfirm() {
        return true;
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
        return new NegateRule(new NegateRule(InEditModeExecutable.INSTANCE, false), false);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        String theFieldID = LayoutComponent.getParameter(someArguments, PARAMETER_FIELD);
        touchField((FormComponent)aComponent, ((FormComponent)aComponent).getFormContext().getMember(theFieldID));
        return HandlerResult.DEFAULT_RESULT;
    }


    /**
     * Touches the given form member.
     *
     * @param aMember
     *        The field to touch.
     * @return <code>true</code> if touching succeeds, false otherwise.
     */
    protected boolean touchField(FormComponent aComponent, FormMember aMember) {
		AttributeUpdate theUpdate = AttributeFormFactory.getAttributeUpdate(aMember);
        if (theUpdate != null) {
            TLStructuredTypePart theMA = theUpdate.getAttribute();
            ValidityCheck theValidityCheck = AttributeOperations.getValidityCheck(theMA);
            if (theValidityCheck.isActive() && !theValidityCheck.isReadOnly()) {
                theUpdate.setTouched(true);
                return updateComponent(aComponent, theUpdate);
            }
        }
        return false;
    }

    /**
     * Updates the component.
     *
     * @param aComponent
     *        the component to update
     * @param aUpdate
     *        the attribute update
     * @return <code>true</code> if the update succeeded, <code>false</code> otherwise
     */
    protected boolean updateComponent(FormComponent aComponent, AttributeUpdate aUpdate) {
        aComponent.invalidate();
        return true;
    }

}
