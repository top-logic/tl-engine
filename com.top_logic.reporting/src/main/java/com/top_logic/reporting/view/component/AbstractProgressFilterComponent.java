/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;

import org.apache.poi.ss.formula.eval.NotImplementedException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.report.view.component.AbstractFilterComponent;
import com.top_logic.reporting.view.component.progress.ApplyFilterProgressCommandHandler;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.export.ExportAware;

/**
 * Component that uses {@link FilterVO} based filters and provides a progress dialog when filtering.
 * 
 * @author <a href="mailto:olb@top-logic.com">olb</a>
 */
public abstract class AbstractProgressFilterComponent<T extends FilterVO> extends AbstractFilterComponent {

	/**
	 * Configuration for the {@link AbstractProgressFilterComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractFilterComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AbstractFilterComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(ApplyFilterProgressCommandHandler.COMMAND_ID);
		}

		@Override
		default void addUpdateCommand(CommandRegistry registry) {
			registry.registerButton(ApplyFilterCommandHandler.COMMAND_ID);
		}

	}

	private T filter;

	/**
	 * Creates a {@link AbstractProgressFilterComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractProgressFilterComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}
	


	@Override
	protected SecurityObjectProvider getDefaultSecurityObjectProvider() {
		return FilterSecurityObjectProvider.INSTANCE;
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		throw new NotImplementedException("Must be implemented?");
	}

	@Override
	final protected void fill(FormContext aContext) {
		addFiltersFields(aContext);
	}
	
	@Override
	protected final void fill(ChartContext aChartContext) {
		// Do not need this
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void handleEvent() {
		Object theModel = this.getModel();
		// TODO OLB Really need this test? We should always have a FilterVO.
		ChartContext theContext = this.createChartContext(theModel);
		if (theContext instanceof FilterVO) {
			setFilter((T)theContext);
		}
		else {
			throw new IllegalArgumentException("The context has to be an implementation of " + FilterVO.class.getName() + " but is " + theContext.getClass().getName());
		}

		if (this.supportsInternalModel(theModel)) {
			this.fill(theContext);
		}
	}

	@Override
	public void invalidate() {
	    super.invalidate();
	    this.getSlaveComponent().invalidate();
	}
	
	@Override
	public boolean validateModel(DisplayContext context) {
        if (super.validateModel(context)) {
            // Trigger refresh button and show initial result.
            CommandDispatcher.getInstance().dispatchCommand(getApplyFilterProgressCommandHandler(), context, this, ApplyFilterCommandHandler.prepareParametersForCommandHandler(this));
            return true;
        }
        return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object aChangedBy) {
		if (aModel instanceof FilterVO) {
			setFilter((T)aModel);
			invalidate();
		}
		
		return true;
	}
	
	/**
	 * Add the specifics filters fields of the application to the given {@code aContext}
	 */
	protected void addFiltersFields(FormContext aContext) {
		FilteringFilterVO<?> theConf = (FilteringFilterVO<?>) this.getFilter();
		for (FilterProperty property : theConf.getProperties()) {
			aContext.addMember(property.getFormMember());
		}
	}

	public void fireEventToSlave() {
		setSelected(getFilter());
	}
	
	/** 
     * Return the slave component belonging to this filter component.
     * 
     * The slave component will normally be a {@link TableComponent table component},
     * which is {@link ExportAware}.
     * 
     * @return    The requested slave component, may be <code>null</code>.
     */
    public final LayoutComponent getSlaveComponent() {
		Collection<? extends LayoutComponent> slaves = getSlaves();

		if (slaves.isEmpty() || slaves.size() > 1) {
			throw new IllegalStateException("The component '" + getName()
				+ "' must have exactly one slave. Configured slaves: '" + slaves + "'.");
		}
		return slaves.iterator().next();
    }

    /**
	 * Used by the {@link ApplyFilterCommandHandler} to check, if a progress dialog has to be
	 * displayed.
	 * 
	 * @param aSearchBaseCount
	 *        Value read by {@link #getSearchBaseCount()}.
	 * @return <code>true</code> when a dialog has to be displayed.
	 * @see #getSearchBaseCount()
	 */
	public boolean needProgressDialog(int aSearchBaseCount) {
		return aSearchBaseCount > 100;
    }

	/**
	 * Return the number of objects to be used for finding the search base.
	 * 
	 * This method is needed by the {@link ApplyFilterCommandHandler} to display an information to
	 * the user.
	 * 
	 * @return The number of object to be used for filling the filter afterwards.
	 * @see #needProgressDialog(int)
	 */
	public int getSearchBaseCount() {
		return 100;
	}

    public T getFilter() {
		return (filter);
	}

	public void setFilter(T filter) {
		this.filter = filter;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean resetFormContext(Object oldModel, Object newModel) {
		T filter2 = this.getFilter();
		if (filter2 == null) {
			ChartContext createChartContext = this.createChartContext(newModel);
			this.setFilter((T) createChartContext);
		}
		else {
			filter2.setModel(newModel);

			FilteringFilterVO<?> theConf = (FilteringFilterVO<?>) filter2;
			for (FilterProperty property : theConf.getProperties()) {
				property.resetFormMember();
			}
		}

		return true;
	}

	protected String getApplyFilterProgressCommandHandlerID(){
		return ApplyFilterProgressCommandHandler.COMMAND_ID;
	}
	
	protected ApplyFilterProgressCommandHandler getApplyFilterProgressCommandHandler(){
		return (ApplyFilterProgressCommandHandler) getCommandById(getApplyFilterProgressCommandHandlerID());
	}

	/**
	 * Extract the model from the filter and use it as security object if possible.
	 * 
	 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	protected static class FilterSecurityObjectProvider implements SecurityObjectProvider {

		/** Avoid multiple instances. */
		public static final FilterSecurityObjectProvider INSTANCE = new FilterSecurityObjectProvider();

		@Override
		public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
			if (aChecker instanceof AbstractProgressFilterComponent) {
				FilterVO filter = ((AbstractProgressFilterComponent<?>) aChecker).getFilter();
				if (filter != null) {
					Object filterModel = filter.getModel();
					if (filterModel instanceof BoundObject) {
						return (BoundObject) filterModel;
					}
				}
			}

			return null;
		}

	}

}
