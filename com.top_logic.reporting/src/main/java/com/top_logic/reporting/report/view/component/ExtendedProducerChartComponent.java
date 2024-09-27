/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;

import com.top_logic.base.chart.ChartChoice;
import com.top_logic.base.chart.component.ChoiceJFreeChartComponent.ChoiceChartCommand;
import com.top_logic.base.chart.util.ChartType;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.basic.CommandHandlerCommand;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.structure.IFrameLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.control.producer.ChartProducer;
import com.top_logic.reporting.report.view.producer.AbstractClassificationChartProducer;
import com.top_logic.reporting.report.view.producer.ExtendedChartProducer;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * Chart component providing a handling of held business objects to the {@link JFreeChart} via the {@link ChartData}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExtendedProducerChartComponent extends DefaultProducerChartComponent implements CategoryURLGenerator, PieURLGenerator, XYURLGenerator, ChartChoice {

    public interface Config extends DefaultProducerChartComponent.Config {
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(XML_CONFIG_DETAIL_COMP_NAME)
		@Mandatory
		ComponentName getDetailComponent();

		@Override
		@ItemDefault(IFrameLayoutControlProvider.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			DefaultProducerChartComponent.Config.super.modifyIntrinsicCommands(registry);
			if (supportedChartTypes(getChartProducer()).size() > 1) {
				registry.registerCommand(ChoiceChartCommand.COMMAND_ID);
			}
		}

	}

	public static final List<ChartType> DEFAULT_CHART_TYPE = Collections.singletonList(ChartType.DEFAULT_CHART);

    public static final String XML_CONFIG_DETAIL_COMP_NAME = "detailComponent";

    /** The container for JFreeChart and <i>TopLogic</i> data. */ 
    private ChartData chartData;

    /** Name of the detail component to be used for displaying the table of selected objects. */
	private ComponentName detailCompName;

    /** 
     * Creates a {@link ExtendedProducerChartComponent}.
     */
    public ExtendedProducerChartComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
        
        this.detailCompName = someAttrs.getDetailComponent();
    }

    @Override
	public String generateURL(CategoryDataset aDataset, int aSeries, int aCategory) {
        return this.createURL(this.chartData.getID(aSeries, aCategory));
    }

    @Override
	public String generateURL(PieDataset aDataset, Comparable aKey, int aPieIndex) {
        if (aDataset.getValue(aKey).intValue() != 0) {
            return this.createURL(this.chartData.getID(aDataset.getIndex(aKey), 0));
        }
        else { 
            return null;
        }
    }

    @Override
	public String generateURL(XYDataset aDataset, int aSeries, int anItem) {
        return this.createURL(this.chartData.getID(aSeries, anItem));
    }

    @Override
	public boolean setSelection(ChartType aChartType) {
        ChartChoice theChoice = this.getChartChoice();

        if (theChoice != null) {
            boolean theResult = theChoice.setSelection(aChartType);

            if (theResult && this.isVisible()) {
                this.chartData = null;

                this.invalidate();
            }

            return theResult;
        }

        return false;
    }

    @Override
	public ChartType getSelection() {
        ChartChoice theChoice = this.getChartChoice();

        return (theChoice != null) ? theChoice.getSelection() : DEFAULT_CHART_TYPE.get(0);
    }

    @Override
	public boolean isChartTypeSupported(ChartType aChartType) {
        ChartChoice theChoice = this.getChartChoice();

        return (theChoice != null) ? theChoice.isChartTypeSupported(aChartType) : DEFAULT_CHART_TYPE.get(0).equals(aChartType);
    }

    @Override
	public final List<ChartType> getSupportedChartTypes() {
		ChartProducer producer = this.getChartProducer();

		return supportedChartTypes(producer);
    }

	static List<ChartType> supportedChartTypes(ChartProducer producer) {
		if (producer instanceof ChartChoice)
			return ((ChartChoice) producer).getSupportedChartTypes();
		else
			return DEFAULT_CHART_TYPE;
	}

    @Override
    protected JFreeChart createChart() {
        ChartProducer theProducer = this.getChartProducer();
        ChartContext  theContext  = this.getModelAsChartContext();

        if (theProducer instanceof ExtendedChartProducer) {
            this.chartData = ((ExtendedChartProducer) theProducer).produceChartData(theContext, this);
        }
        else {
            this.chartData = new ChartData(theProducer.produceChart(theContext), Object.class);
        }
        return this.chartData.getChart();
    }

    @Override
    protected JFreeChart createChart(ChartContext chartContext) {
        ChartProducer theProducer = this.getChartProducer();
        if (theProducer instanceof ExtendedChartProducer) {
            return ((ExtendedChartProducer)theProducer).produceChartData(chartContext, null).getChart();
        }
    	return super.createChart(chartContext);
    }

    @Override
    protected void customizeChart(String anImageId, JFreeChart aChart) {
        // Nothing in here, will be done by AbstractChartProducer
    }

	@Override
	protected boolean supportsInternalModel(Object anObject) {
        ChartProducer theProducer = this.getChartProducer();

        if ((theProducer instanceof ExtendedChartProducer) && !(anObject instanceof ChartContext)) {
            return ((ExtendedChartProducer) theProducer).supportsObject(anObject);
        }
        else {
            return super.supportsInternalModel(anObject);
        }
    }

    @Override
    protected void becomingInvisible() {
    	super.becomingInvisible();
    	invalidate();
    }

    @Override
    public void invalidate() {
    	super.invalidate();
    	this.chartData=null;
    }

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);

		if (newValue != null && this.getSupportedChartTypes().size() > 1) {
			newValue.defineGroup(CommandHandlerFactory.CHART_TYPES_GROUP).addButtons(getChartSelectionModels());
		}

		if (oldValue != null) {
			oldValue.removeGroup(CommandHandlerFactory.CHART_TYPES_GROUP);
		}
	}

    @Override
    public boolean isModelValid() {
		return this.chartData != null && super.isModelValid();
    }

    /** 
     * Return the values for the given ID
     * 
     * @param    anID    The string representation of the requested position, must not be <code>null</code>
     * @return   The requested set of items, may be <code>null</code>.
     * @see      ChartData#getItems(String)
     */
    public Collection<?> getValues(String anID) {
        return this.chartData.getItems(anID);
    }

	/**
	 * Return the chart data object used by this component.
	 * 
	 * @return    The requested chart data object, never <code>null</code>.
	 */
	public ChartData<?> getChartData() {
		return this.chartData;
	}

    /** 
     * Return the table component to be used in the opened dialog.
     * 
     * @return    The requested table component, never <code>null</code>.
     */
    protected TableComponent getDetailComponent() {
        return (TableComponent) this.getMainLayout().getComponentByName(this.detailCompName);
    }

    /** 
     * Create an URL for the given ID.
     * 
     * @param    anID    The ID to generate an URL for, must not be <code>null</code>.
     * @return   The requested URL for opening a dialog, may be <code>null</code>.
     */
    protected String createURL(String anID) {
		CommandHandler theHandler = getCommandById(ChartDisplayDetailCommandHandler.COMMAND_ID);

        if (theHandler != null) {
            if (!StringServices.isEmpty(anID) && !CollectionUtil.isEmptyOrNull(this.getValues(anID))) {
				CommandHandlerCommand command = new CommandHandlerCommand(theHandler, this,
					Collections.singletonMap(ChartDisplayDetailCommandHandler.PARAMETER_ID, anID));

				return LinkGenerator.createLink(DefaultDisplayContext.getDisplayContext(), command);
            }
        }

        return null;
    }

    /** 
     * Return the inner manager for the chart choice handling.
     * 
     * This will be one by inspecting the {@link ChartProducer} if it is a {@link ChartChoice}.
     * 
     * @return    The requested chart choice handler, may be <code>null</code>.
     */
    protected ChartChoice getChartChoice() {
        ChartProducer theProducer = this.getChartProducer();

        return ((theProducer instanceof ChartChoice) ? (ChartChoice) theProducer : null);
    }
    
    /**
     * This method returns a list of {@link CommandModel}s for the supported chart types of this
     * component, never <code>null</code>.
     * 
     * @return    The list of command models to be appended to the parents button list.
     */
    protected List<CommandModel> getChartSelectionModels() {
        List<CommandModel> theModels  = new ArrayList<>();
        CommandHandler     theCommand = CommandHandlerFactory.getInstance().getHandler(ChoiceChartCommand.COMMAND_ID);

		for (Iterator<ChartType> theIter = this.getSupportedChartTypes().iterator(); theIter.hasNext();) {
			ChartType theType = theIter.next();
			ResKey theLabel = this.getI18NLabel(theType.getName());
			CommandModel theModel = this.getCommandModelFor(theCommand, theType.getName());

            theModel.setLabel(theLabel);
			theModel.setImage(theType.getIcon());
			theModel.set(ButtonControl.BUTTON_RENDERER, ImageButtonRenderer.INSTANCE);
            theModels.add(theModel);
        }

        return theModels;
    }

    /**
	 * Return the I18N label for the command model in {@link #getChartSelectionModels()}.
	 * 
	 * @param aChartType
	 *        The requested chart type, must not be <code>null</code>.
	 * 
	 * @return The requested I18N for the given chart type.
	 * @see #getChartSelectionModels()
	 */
	protected ResKey getI18NLabel(String aChartType) {
		return I18NConstants.TL.key(aChartType);
    }

    /**
     * This method returns a {@link CommandModel} for the chart type.
     * 
     * @param    aCommand      The command handler for execution, must not be <code>null</code>.
     * @param    aChartType    A chart type, must not be <code>null</code>.
     */
    protected CommandModel getCommandModelFor(CommandHandler aCommand, String aChartType) {
        final Map<String, Object> arguments = Collections.<String, Object>singletonMap(AbstractClassificationChartProducer.CHART_TYPE, aChartType);
		return CommandModelFactory.commandModel(aCommand, this, arguments);
    }

    /**
	 * Opener for the detail information to the values.
	 * 
	 * <p>
	 * When there is only one object in the {@link ExtendedProducerChartComponent#getValues(String)
	 * target value set}, the handler will try a goto to that object. If this fails or the returned
	 * result contains more objects the configured table will be opened in a dialog.
	 * </p>
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public static class ChartDisplayDetailCommandHandler extends OpenModalDialogCommandHandler {

        // Constants

        protected static String COMMAND_ID = "openChartDetails";

        public static final String PARAMETER_ID = "_id";

        public ChartDisplayDetailCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        // Overridden methods from OpenModalDialogCommandHandler 
 
        @Override
        public String[] getAttributeNames() {
            return new String[] { ChartDisplayDetailCommandHandler.PARAMETER_ID };
        }

        @Override
        public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            boolean       isSuccess = false;
            HandlerResult theResult = HandlerResult.DEFAULT_RESULT; 

            if (aComponent instanceof ExtendedProducerChartComponent) {
                ExtendedProducerChartComponent theComp     = (ExtendedProducerChartComponent) aComponent;
                String                         theID       = (String) someArguments.get(ChartDisplayDetailCommandHandler.PARAMETER_ID);
                Collection<?>                  theValues   = theComp.getValues(theID);

                if (!CollectionUtil.isEmptyOrNull(theValues)) {
                    if (theValues.size() == 1) {
                        Object theObject = CollectionUtil.getSingleValueFromCollection(theValues);

                        if (GotoHandler.canShow(theObject)) {
							isSuccess = aComponent.gotoTarget(theObject);
                        }
                    }

                    if (!isSuccess) {
                        theResult = super.handleCommand(aContext, aComponent, model, someArguments);
						theComp.getDetailComponent().setModel(theValues);
						isSuccess = true;
                    }
                }
                else {
                    return theResult;
                }
            }

            if (!isSuccess) {
                theResult = new HandlerResult();
				theResult.addErrorText("Failed to get objects for display.");
            }

            return theResult;
        }
    }

    /**
     * Special model builder for returning the components model in
     * {@link ExtendedProducerChartModelBuilder#getModel(Object, LayoutComponent)}.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class ExtendedProducerChartModelBuilder implements ListModelBuilder {

		/**
		 * Singleton {@link ExtendedProducerChartModelBuilder} instance.
		 */
		public static final ExtendedProducerChartModelBuilder INSTANCE = new ExtendedProducerChartModelBuilder();

		private ExtendedProducerChartModelBuilder() {
			// Singleton constructor.
		}

        @Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
			return (Collection<?>) businessModel;
        }

        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return aModel == null || aModel instanceof Collection;
        }

        @Override
		public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
            return true;
        }

        @Override
		public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
            return null;
        }
    }
}

