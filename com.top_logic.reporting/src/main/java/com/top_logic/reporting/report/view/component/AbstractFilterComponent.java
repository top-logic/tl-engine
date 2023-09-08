/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.Updatable;
import com.top_logic.layout.component.UpdateCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.layout.meta.search.ReportingAttributedSearchResultSet;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.util.Utils;

/**
 * This abstract filter component works together with the {@link AbstractFilterChartComponent}.
 * Use this component as master of one subclass of {@link AbstractFilterChartComponent}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class AbstractFilterComponent extends FormComponent implements Updatable, Selectable {

	/**
	 * Configuration for the {@link AbstractFilterComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			addUpdateCommand(registry);
		}

		default void addUpdateCommand(CommandRegistry registry) {
			registry.registerButton(UpdateCommandHandler.COMMAND_ID);
		}

	}

    /** Sticky flag indicating that model was changed */
    protected boolean changed;

	private Object _oldModel;

    /** 
     * Creates a {@link AbstractFilterComponent} from XML.
     */
    public AbstractFilterComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        changed = true;
        // userOverlib = false; // Do we need this?
    }

    @Override
	protected
	abstract boolean supportsInternalModel(Object anObject);

    /**
     * This method fills the given form context with the necessary filter
     * fields.
     * 
     * @param aContext
     *        The form context to fill. The form context is never
     *        <code>null</code>.
     */
    abstract protected void fill(FormContext aContext);

    /**
     * This method fills the given filter value object with the data from the
     * form context. The given object is never <code>null</code>. The model
     * is already set by the current model of this component.
     */
    protected abstract void fill(ChartContext aChartContext);

    @Override
	public FormContext createFormContext() {
        FormContext context = new FormContext("FilterFormContext", getResPrefix());
        fill(context);
        return context;
    }
    
    @Override
	public boolean isModelValid() {
		return !this.changed && super.isModelValid();
    }

    @Override
	public boolean validateModel(DisplayContext context) {
    	boolean result = super.validateModel(context);
    	if (this.changed) {
			// Reset the form context
			Object model = getModel();
			if (resetFormContext(_oldModel, model)) {
				removeFormContext();
				invalidate();
			}
			if (model != null) {
				modifyFormContext(_oldModel, model, getFormContext());
			}
			_oldModel = null;

    		handleEvent();
    		this.changed = false;
    		
    		// Events have been sent.
    		result = true;
    	}
    	return result; 
    }
    
    @Override
	protected boolean receiveModelChangedEvent(Object aModel, Object aChangedBy) {
        if (Utils.equals(getModel(), aModel)) {
            if (isVisible()) {
                handleEvent();
            } else {
                this.changed = true;
            }
            return true;
        }
        return super.receiveModelChangedEvent(aModel, aChangedBy);
    }

    /** This method handles the event sending. */
	protected void handleEvent() {
		Object theModel = this.getModel();
		if (!(theModel instanceof Wrapper) || ((Wrapper) theModel).tValid()) {
			ChartContext theContext = this.createChartContext(theModel);
			if (this.supportsInternalModel(theModel)) {
				this.fill(theContext);
				setSelected(theContext);
			}
        }
    }

    /** 
     * Create a ChartContext based on aModel;
     * 
     * @return always a FilterVO.
     */
    protected ChartContext createChartContext(Object aModel) {
        ChartContext theContect =  new FilterVO(aModel);
        if (aModel instanceof ReportingAttributedSearchResultSet) {
            theContect.setReportConfiguration(((ReportingAttributedSearchResultSet) aModel).getReportConfiguration());
        }
        return theContect;
    }

    @Override
	public void update() {
        handleEvent();
    }
    
    final protected void registerUpdateChartCommandHandlerHook() {
        /** Just to track usage */
    }

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		if (!changed) {
			_oldModel = oldModel;
			changed = true;
		}
		fireSecurityChanged(newModel);
	}
    
    /**
	 * This method is a hook for sub classes to modify the form context after
	 * the new model is set but before the events are send.
	 * 
	 * @param aFormContext
	 *            The current form context must NOT be <code>null</code>.
	 */
    protected void modifyFormContext(Object oldModel, Object newModel, FormContext aFormContext) {
    	// default is empty
	}
    
    /**
	 * This method returns <code>true</code> if the form context must be reset
	 * for the given model. This method is normally called if a new model is set
	 * to the filter component. But for some filter configurations it isn't
	 * necessary to reset the form context.
	 */
    protected boolean resetFormContext(Object oldModel, Object newModel) {
        return true;
	}

    /**
     * <p>
     * Change handling is inactive by default, because filters like this do not
     * have any persistent representation.
     * </p>
     * <p>
     * If change handling is needed by specialized subclasses, the option 
     * {@link LayoutComponent#ATT_USE_CHANGE_HANDLING} can be configured via XML.
     * </p> 
     */
    @Override
    protected final boolean isChangeHandlingDefault() {
        return false;
    }

    /**
     * The UpdateChartCommandHandler updates the corresponding chart component. 
     *
     * @deprecated replaced by {@link UpdateCommandHandler}.
     */
    @Deprecated
	public static class UpdateChartCommandHandler  {
        
        /** 
         * Left here for compatibility of exiting JSPs
         * 
         * @deprecated replaced by {@link UpdateCommandHandler#COMMAND_ID}. 
         */
        @Deprecated
		public static final String COMMAND_ID = UpdateCommandHandler.COMMAND_ID;

    }
    
}
