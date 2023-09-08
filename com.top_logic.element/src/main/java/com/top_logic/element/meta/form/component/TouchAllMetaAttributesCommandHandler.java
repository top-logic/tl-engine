/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.ValidityCheck;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InEditModeExecutable;

/**
 * Touches all attributes, which have an active validity check and are not read only.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TouchAllMetaAttributesCommandHandler extends AJAXCommandHandler {

    /** Command ID of this handler. */
    public static final String COMMAND_ID = "touchAllMetaAttributes";

	/**
	 * Configuration for {@link TouchAllMetaAttributesCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

    public TouchAllMetaAttributesCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }


    @Override
	public boolean needsConfirm() {
        return true;
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
        return InEditModeExecutable.INSTANCE;
    }



    @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        AttributeFormContext theContext = (AttributeFormContext)((FormComponent)aComponent).getFormContext();
		touchAllFields((FormComponent) aComponent, theContext.getAttributeUpdateContainer().getAllUpdates());
        return HandlerResult.DEFAULT_RESULT;
    }

    /**
     * Touches all values, which have an active validity check and are not read only.
     *
     * @param someUpdates
     *        The holder of meta attributes and their values.
     * @return <code>true</code> when at least one value has been touched.
     */
	protected boolean touchAllFields(FormComponent aComponent, Iterable<AttributeUpdate> someUpdates) {
        boolean theResult = false;
		for (AttributeUpdate theUpdate : someUpdates) {
            ValidityCheck   theValidityCheck = AttributeOperations.getValidityCheck(theUpdate.getAttribute());
            if (theValidityCheck.isActive() && !theValidityCheck.isReadOnly()) {
                if (touchField(aComponent, theUpdate) && updateComponent(aComponent, theUpdate)) {
                    theResult = true;
                }
            }
        }
        return theResult;
    }

    /**
     * Touches the attribute update.
     * @return <code>true</code>, if the touch succeeded, <code>false</code> otherwise
     */
    protected boolean touchField(FormComponent aComponent, AttributeUpdate aUpdate) {
        aUpdate.setTouched(true);
        return true;
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
