/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.task;

import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.TaskWrapper;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.layout.EditTaskComponent;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class EditTaskWrapperComponent extends EditTaskComponent {

	/** Name of the {@link FormGroup} for {@link TaskWrapper} information. */
	public static final String NAME_GROUP_WRAPPER = "wrapper";

	public static final String TIME = "time";

    /**
	 * Creates a {@link EditTaskWrapperComponent}.
	 */
    public EditTaskWrapperComponent(InstantiationContext context, Config aSomeAtts) throws ConfigurationException {
        super(context, aSomeAtts);
    }

    /**
     * @see com.top_logic.util.sched.layout.EditTaskComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        FormContext theContext = super.createFormContext();

        if (this.getModel() instanceof TaskWrapper) {
            TaskWrapper theModel = (TaskWrapper) this.getModel();

            try {
                FormGroup theGroup  = new FormGroup(NAME_GROUP_WRAPPER, this.getResPrefix());

				Date theDate = DateUtil.createDate(0, 0, 0, theModel.getHour(), theModel.getMinute(), 0);

				ComplexField theTime =
					FormFactory.newComplexField(EditTaskWrapperComponent.TIME,
						HTMLFormatter.getInstance().getTimeFormat(), theDate, false);
				theGroup.addMember(theTime);

                theContext.addMember(theGroup);
            }
            catch (Exception ex) {
                Logger.error("Unable to add fields to form context", ex, this);
            }
        }

        return theContext;
    }
}

