/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.WrapperValueFilter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command recomputing the result of a {@link AbstractReportFilterComponent} from its filter
 * settings.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractRefreshReportHandler extends AbstractCommandHandler {

    /** 
     * Create a new instance of this class.
     * 
     */
	public AbstractRefreshReportHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * Creates the report filter from the values of the given form.
     *
     * @param aModel
     *        The model of the filter component.
     * @param aContext
     *        The form of the filter component.
     * @return The new report filter.
     */
    protected abstract ReportDescription createReportFilter(Object aModel, FormContext aContext);

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        HandlerResult theResult = new HandlerResult();

        if (aComponent instanceof AbstractReportFilterComponent) {
            AbstractReportFilterComponent theComp    = (AbstractReportFilterComponent) aComponent;
            FormContext          theContext = theComp.getFormContext();

            if (theContext.checkAll()) {
				if (!theComp.refreshResult(this.createReportFilter(model, theContext))) {
					theResult.addErrorText("Result cannot be refreshed!");
                }
            }
        }
        else {
			theResult.addErrorText("Component is no SupplierReportFilter!");
        }

        return (theResult);
    }

    /**
     * The value of the given field, a list only if more than one value is selected.
     *
     * @return The single selected value, the list of selected values, or {@code null} if nothing is
     *         selected.
     */
    protected Object getSingleValueFromField(SelectField aField) {
        Object theValue = aField.getValue();

        if (theValue instanceof List) {
            List theList = (List) theValue;
            int  theSize = theList.size();

            if (theSize == 1) {
                theValue = theList.get(0);
            }
            else if (theSize > 1) {
                theValue = theList;
            }
            else {
                theValue = null;
            }
        }

        return (theValue);
    }

    /**
     * Adds a {@link WrapperValueFilter} for the value of the given field to the given filter list,
     * if the field has a value.
     *
     * @param aFilterList
     *        The list to add the filter to.
     * @param aContext
     *        The form containing the field.
     * @param aField
     *        The name of the field.
     * @param anAttr
     *        The name of the attribute to filter.
     */
    protected void addValueFilter(List aFilterList, FormContext aContext, String aField, String anAttr) {
        SelectField theField = (SelectField) aContext.getMember(aField);
        Object      theValue = theField.hasValue() ? this.getSingleValueFromField(theField) : null;

        if (theValue != null) {
            aFilterList.add(new WrapperValueFilter(anAttr, theValue));
        }
    }
}