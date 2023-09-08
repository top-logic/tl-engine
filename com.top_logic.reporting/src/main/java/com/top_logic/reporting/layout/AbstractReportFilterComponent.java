/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ModelEventListener;

/**
 * Abstract Filter component for reports.
 * 
 * This component holds the result, which will be calculated, when the
 * {@link AbstractRefreshReportHandler} command is called. The {@link AbstractReportTableComponent}
 * is requesting the result via {@link #getResultList()} and display that.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractReportFilterComponent extends FormComponent {

	/**
	 * Configuration for the {@link AbstractReportFilterComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Return the ID of the refresh command.
		 * 
		 * @return The ID of the refresh command, never <code>null</code>.
		 */
		@Mandatory
		String getRefreshCommand();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(getRefreshCommand());
		}

	}

    /** The result list of the held filter. */
    private List result = null; // new ArrayList();

    private ReportDescription reportFilter;

    /** 
     * Create a new instance of this class.
     */
    public AbstractReportFilterComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
    }

    /**
	 * Return all objects to be inspected by {@link #refreshResult(ReportDescription)}.
	 * 
	 * @param aFilter
	 *        The filter to search for (later on).
	 * 
	 * @return The requested list, never <code>null</code>.
	 */
    protected abstract List getAllObjects(ReportDescription aFilter);

    @Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		removeFormContext();
		this.fireSecurityChanged(newModel);
    }

    /** 
     * Return the name of the structure to organize the result.
     * 
     * @return    The name of the structure, may be <code>null</code>.
     */
    public ReportDescription getReportFilter() {
        return (this.reportFilter);
    }

    /** 
     * Return the list of suppliers being the result of the filter.
     * 
     * @return    The requested list, never <code>null</code>.
     */
    public List getResultList() {
        if (this.result == null) {
            // TODO: TRI / MGA: Do we need a filled result on first appearence?
            this.result = new ArrayList();
        }

        return (this.result);
    }

    /** 
     * Refresh the suppliers list based on the given report filter.
     * 
     * @param    aFilter    The report filter to be used for building up the new result list.
     * @return   Flag, if refresh succeeds.
     * @see      #getAllObjects(ReportDescription)
     * @see      #addResult(List, Object, Currency)
     */
    public boolean refreshResult(ReportDescription aFilter) {
        Filter   theFilter   = aFilter.getFilter();
        Currency theCurrency = aFilter.getCurrency();
        List     theList     = this.getAllObjects(aFilter);
        List     theResult   = new ArrayList(theList.size());

        this.reportFilter = aFilter;

        for (Iterator theIt = theList.iterator(); theIt.hasNext();) {
            Object theObject = theIt.next();

            if (theFilter.accept(theObject)) {
                this.addResult(theResult, theObject, theCurrency);
            }
        }

        theResult = this.postProcessResult(aFilter, theResult);

        this.result = theResult;

        fireModelEvent(this.result, ModelEventListener.MODEL_MODIFIED);
        this.invalidateButtons();

        return (true);
    }

    /** 
     * Hook method for sub classes to process the result after visiting all objects in the list.
     * 
     * @param    aFilter    The report filter to be used for building up the new result list.
     * @param    aResult    The list created and filled with {@link #addResult(List, Object, Currency)}.
     */
    protected List postProcessResult(ReportDescription aFilter, List aResult) {
        return (aResult);
    }

    /** 
     * Add the given object to the result list.
     * 
     * @param    aList       The list to be used for adding.
     * @param    anObject    The object to be added.
     * @param    aCurrency   The currency the results have to be converted to.
     * @return   <code>true</code>, if adding changed the list.
     * @see      #refreshResult(ReportDescription)
     */
    protected boolean addResult(List aList, Object anObject, Currency aCurrency) {
        return aList.add(anObject);
    }
}
