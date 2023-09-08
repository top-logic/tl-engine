/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.eva;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.component.SelectableFormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.model.ArrayTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.EVAAssistantController;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler.ImportMessage;
import com.top_logic.util.Resources;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ProcessingComponent extends SelectableFormComponent {

    /** Table field contains information on the validation. */
    public static final String INFO_FIELD = "_infoField";
    
    /** Table field contains errors occurred in the validation. */
    public static final String ERROR_FIELD = "_errorField";

    /** 
     * Creates a {@link ProcessingComponent}.
     */
    public ProcessingComponent(InstantiationContext context, Config someAtts) throws ConfigurationException {
        super(context, someAtts);
    }

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	/**
	 * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
	 */
    @Override
	public FormContext createFormContext() {
        FormContext theContext = new FormContext("ctx", this.getResPrefix());
        List        theResult  = this.getValidationResult();

        if (!CollectionUtil.isEmptyOrNull(theResult)) {
            this.addValidationResult(theContext, theResult);
        }

        return theContext;
    }

	@Override
	protected void becomingInvisible() {
		removeFormContext();
		invalidate();
		super.becomingInvisible();
	}

    /** 
     * Add the validation result to the form context.
     * 
     * @param    context    The form context to be extended, must not be <code>null</code>.
     * @param    importResult     The result of the validation (as list of {@link ImportMessage}), 
     *                       must not be <code>null</code> or empty.
     */
	protected void addValidationResult(FormContext context, List<ImportMessage> importResult) {
        Resources resources = Resources.getInstance();
		List<String[]> infoMessages = new ArrayList<>();
		List<String[]> errorMessages = new ArrayList<>();

		for (Iterator<ImportMessage> messages = importResult.iterator(); messages.hasNext();) {
			ImportMessage message = messages.next();
			List<String[]> theList = message.isError() ? errorMessages : infoMessages;

            theList.add(new String[] {message.getString(resources)});
        }

        if (!infoMessages.isEmpty()) {
            createTableField(context, INFO_FIELD, infoMessages);
        }
		if (!errorMessages.isEmpty()) {
			createTableField(context, ERROR_FIELD, errorMessages);
        }
    }

	private void createTableField(FormContext context, String fieldName, List<String[]> items) {
		TableConfiguration tableConfiguration = TableConfigurationFactory.table();
		tableConfiguration.setShowTitle(false);
		tableConfiguration.setResPrefix(this.getResPrefix().append(fieldName));

		// make sure that the items are displayed in their sequence and not ordered alphabetically
		tableConfiguration.getDefaultColumn().setComparator(null);

		TableField tableField =
			FormFactory.newTableField(fieldName,
				new ArrayTableModel<>(new String[] { "name" }, items, tableConfiguration));
		context.addMember(tableField);
	}

    /** 
     * Return the list of validation results.
     * 
     * @return    The validation results stored in the enclosing assistant component, may be <code>null</code>.
     */
    protected List getValidationResult() {
        return (List) this.getAssistant().getData(EVAAssistantController.VALIDATION_RESULT);
    }

    /** 
     * Return the enclosing assistant component.
     * 
     * @return    The requested enclosing assistant component.
     */
    protected AssistentComponent getAssistant() {
        return AssistentComponent.getEnclosingAssistentComponent(this);
    }
}
