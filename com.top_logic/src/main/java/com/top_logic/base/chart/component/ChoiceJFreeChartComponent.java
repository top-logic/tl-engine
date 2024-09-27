/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.ChartChoice;
import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.base.chart.util.ChartType;
import com.top_logic.base.services.simpleajax.AbstractSystemAjaxCommand;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The ChoiceJFreeChartComponent implements the {@link ChartChoice} interface.
 * Subclasses have to implement only the abstract method of the {@link JFreeChartComponent}. 
 * 
 * Do not forget to set the init chart type and supported chart types. 
 * It is not allowed that no chart type is selected or no chart type is supported.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ChoiceJFreeChartComponent extends JFreeChartComponent implements ChartChoice{

	/**
	 * Configuration for the {@link ChoiceJFreeChartComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends JFreeChartComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			JFreeChartComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(ChoiceChartCommand.COMMAND_ID);
		}

	}

    /** {@link ChartChoice#getSupportedChartTypes()} */
	private List<ChartType> supportedChartTypes;
    /** {@link ChartChoice#getSelection()} */
	private ChartType selectedChartType;

    /** 
     * Creates a {@link ChoiceJFreeChartComponent} with the
     * given parameters. The layout framework uses this constructor.
     */
    public ChoiceJFreeChartComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	protected abstract Dataset createDataSet(String anImageId);
    
    @Override
	protected abstract JFreeChart createChart(String anImageId);
    
    /**
     * If this component is visible and a new chart type was selected, this
     * method is called. The subclass has to react and must generate a new chart
     * with the selected chart type. To get the selected chart type call the
     * {@link #getSelection()} method.
     */
    protected abstract void chartTypeSelected();
    
    @Override
	public ChartType getSelection() {
        if (this.selectedChartType == null) {
            this.selectedChartType = loadChartTypeFromPersonalConfig();
            if (this.selectedChartType == null) {
				this.selectedChartType = getSupportedChartTypes().get(0);
            }
        }
        
        return this.selectedChartType;
    }

    @Override
	public boolean setSelection(ChartType aChartType) {
        return (this.setSelection(aChartType, true));
    }

	protected boolean setSelection(ChartType aChartType, boolean store) {
        if (!isChartTypeSupported(aChartType) || aChartType.equals(this.selectedChartType)) { return false; }
        
        this.selectedChartType = aChartType;
        
        if (store) {
            storeChartTypeInPersonalConfig(this.selectedChartType);
        
            if(isVisible()) {
                chartTypeSelected();
            }
        }
        
        return true;
    }

    /**
     * This method returns the chart type from the {@link PersonalConfiguration}
     * of the current person or <code>null</code>.
     */
	protected ChartType loadChartTypeFromPersonalConfig() {
		PersonalConfiguration result = PersonalConfiguration.getPersonalConfiguration();
		if (result == null) {
			return null;
		}
		return (ChartType) result.getValue(getPersonalConfigKey());
    }
    
    /**
     * This method stores the current visible chart type for the person into its
     * {@link PersonalConfiguration}.
     * 
     * @param aChartType
     *        A chart type. Must not be <code>null</code>.
     */
	protected void storeChartTypeInPersonalConfig(ChartType aChartType) {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration != null) {
			personalConfiguration.setValue(getPersonalConfigKey(), aChartType);
        }
    }

    private String getPersonalConfigKey() {
		return this.getName().qualifiedName() + '.' + CHART_TYPE;
    }
    
    @Override
	public List<ChartType> getSupportedChartTypes() {
        if (this.supportedChartTypes == null || this.supportedChartTypes.size() == 0) {
            throw new IllegalStateException("Minimum one chart type must be supported for a ChoiceJFreeChartComponent ('" + this.getName() + "')!");
        }
        
        return this.supportedChartTypes;
    }
    
    @Override
	public boolean isChartTypeSupported(ChartType aChartType) {
        return getSupportedChartTypes().contains(aChartType);
    }

    @Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);
		if (newValue != null) {
			newValue.defineGroup(CommandHandlerFactory.CHART_TYPES_GROUP).addButtons(getCommandModels());
        }
		if (oldValue != null) {
			oldValue.removeGroup(CommandHandlerFactory.CHART_TYPES_GROUP);
		}
    }
    
    /**
	 * This method returns a list of {@link CommandModel}s for the supported chart types of this
	 * component, never <code>null</code>.
	 */
	private List getCommandModels() {
		ArrayList theModels = new ArrayList();
		List<ChartType> theChartTypes = getSupportedChartTypes();
		Iterator<ChartType> theIter = theChartTypes.iterator();
		while (theIter.hasNext()) {
			ChartType theChartType = theIter.next();
			ResKey theLabel = getI18NLabel(theChartType.getName());
			CommandModel model = getCommandModelFor(theChartType.getName());
			model.setLabel(theLabel);
			model.setImage(theChartType.getIcon());
			model.set(ButtonControl.BUTTON_RENDERER, ImageButtonRenderer.INSTANCE);
			theModels.add(model);
		}

		return theModels;
	}

	private ResKey getI18NLabel(String aChartType) {
		return I18NConstants.CHART_TYPE.key(aChartType);
    }

    /**
	 * This method returns a {@link CommandModel} for the chart type.
	 * 
	 * @param aChartType
	 *            A chart type. Must not be <code>null</code>.
	 */
    private CommandModel getCommandModelFor(String aChartType) {
    	Map arguments = Collections.singletonMap(ChartConstants.CHART_TYPE, aChartType);
		CommandHandler command = CommandHandlerFactory.getInstance().getHandler(ChoiceChartCommand.COMMAND_ID);
		CommandModel model = CommandModelFactory.commandModel(command, this, arguments);
        return model;
    }

    /** {@link ChartChoice#getSupportedChartTypes()} */
	public void setSupportedChartTypes(List<ChartType> someChartTypes) {
        this.supportedChartTypes = someChartTypes;
    }

    /**
     * The ChoiceChartCommand sets the selected chart type to a
     * {@link ChartChoice} class.
     */
	public static class ChoiceChartCommand extends AbstractSystemAjaxCommand {

        /** The command identifier of this component. */
        public static final String COMMAND_ID = "ChoiceChartCommand";
        
        /** 
         * Creates a {@link ChoiceChartCommand}.
         */
		public ChoiceChartCommand(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        // Overridden methods from AJAXCommandHandler

        @Override
		public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            ChartChoice theComponent = (ChartChoice) aComponent;
			theComponent.setSelection((ChartType) someArguments.get(ChartConstants.CHART_TYPE));
            
            return HandlerResult.DEFAULT_RESULT;
        }
    }
}

