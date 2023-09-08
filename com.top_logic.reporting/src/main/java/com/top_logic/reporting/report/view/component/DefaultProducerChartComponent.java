/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.base.chart.util.ChartConstants;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.control.producer.ChartProducer;
import com.top_logic.reporting.report.control.size.ChartSizeManipulator;
import com.top_logic.reporting.report.control.size.NoChangeManipulator;
import com.top_logic.reporting.report.model.FilterVO;
import com.top_logic.reporting.report.util.ValueUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExportAware;
import com.top_logic.tool.export.PowerpointExportHandler;
import com.top_logic.tool.export.WordExportHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * This component delegates the creation of a JFreeChart to the related {@link ChartProducer}
 * which is configured in the layout xml (see {@link #LAYOUT_ATTRIBUTE_PRODUCER_CLASS_NAME}).
 * The {@link ChartProducer} can be used without a component to create {@link JFreeChart}s.
 * In the past it was a problem if the component contains the creation code for {@link JFreeChart}s
 * because if someone else want to export the chart he need the component. Must set the model to 
 * the component,... Now all (the component and the export) can use the {@link ChartProducer}
 * without these problems.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DefaultProducerChartComponent extends AbstractFilterChartComponent implements ExportAware {

    public interface Config extends AbstractFilterChartComponent.Config {
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(LAYOUT_ATTRIBUTE_PRODUCER_CLASS_NAME)
		@Mandatory
		@InstanceFormat
		ChartProducer getChartProducer();

		@Name(LAYOUT_ATTRIBUTE_CHART_SIZE_CLASS_NAME)
		@InstanceFormat
		@InstanceDefault(NoChangeManipulator.class)
		ChartSizeManipulator getChartSizeClassName();

		@Name(LAYOUT_ATTRIBUTE_EXPORT_CONTEXT_CLASS_NAME)
		@InstanceFormat
		ChartContext getExportContextClassName();

		@Name(LAYOUT_ATTRIBUTE_EXPORT_AS_WINDOW_BUTTON)
		@BooleanDefault(true)
		boolean getExportAsWindowButton();

		@Name(LAYOUT_ATTRIBUTE_EXPORT_HANDLER_ID)
		String getExportHandlerId();

		@Name(LAYOUT_ATTRIBUTE_TEMPLATE_FILE)
		String getTemplateFile();

		@Name(LAYOUT_ATTRIBUTE_DOWNLOAD_NAME_KEY)
		@InstanceFormat
		ResKey getDownloadNameKey();

		@Name(LAYOUT_ATTRIBUTE_CHART_WIDTH)
		@IntDefault(900)
		int getChartWidth();

		@Name(LAYOUT_ATTRIBUTE_CHART_HEIGHT)
		@IntDefault(560)
		int getChartHeight();

		@Name(LAYOUT_ATTRIBUTE_BG_COLOR)
		@FormattedDefault("white")
		@Format(ColorConfigFormat.class)
		Color getBgColor();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AbstractFilterChartComponent.Config.super.modifyIntrinsicCommands(registry);
			if (!StringServices.isEmpty(getExportHandlerId())) {
				if (getExportAsWindowButton()) {
					registry.registerCommand(getExportHandlerId());
				} else {
					registry.registerButton(getExportHandlerId());
				}
			}
		}

	}

	/** This attribute contains the class name of the {@link ChartProducer} for
     *  this chart component. See {@link ChartProducer}. */
    public static final String LAYOUT_ATTRIBUTE_PRODUCER_CLASS_NAME       = "chartProducer";
    /** The attribute contains the id of the export command handler. */
    public static final String LAYOUT_ATTRIBUTE_EXPORT_HANDLER_ID         = "exportHandlerId";
    /** The attribute contains the template file (the base directory is
     *  WEB-INF/reportTemplates/).  */
    public static final String LAYOUT_ATTRIBUTE_TEMPLATE_FILE             = "templateFile";
    /** The attribute contains the resource key that is used for the name 
     *  of the download file. The translation doesn't end with a file suffix (e.g. .doc).  */
    public static final String LAYOUT_ATTRIBUTE_DOWNLOAD_NAME_KEY         = "downloadNameKey";
    /** The attribute contains the chart image width for the export.
     *  See {@link #LAYOUT_ATTRIBUTE_CHART_SIZE_CLASS_NAME}. */
    public static final String LAYOUT_ATTRIBUTE_CHART_WIDTH               = "chartWidth";
    /** The attribute contains the chart image height for the export. 
     *  See {@link #LAYOUT_ATTRIBUTE_CHART_SIZE_CLASS_NAME}. */
    public static final String LAYOUT_ATTRIBUTE_CHART_HEIGHT              = "chartHeight";
    /** The attribute contains the chart background for the export image. */
    public static final String LAYOUT_ATTRIBUTE_BG_COLOR                  = "bgColor";
    /** The attribute contains the class name of the export chart context.
     *  The export chart context contains additional values. The export 
     *  context based on the component chart context. If both contexts 
     *  contains the same key, the export context value is used. */
    public static final String LAYOUT_ATTRIBUTE_EXPORT_CONTEXT_CLASS_NAME = "exportContextClassName";
    /** The {@link ChartSizeManipulator} can manipulate the chart size. Normally the chart fill
     *  the complete frame. The manipulator can change the dimension of the chart. 
     */
    public static final String LAYOUT_ATTRIBUTE_CHART_SIZE_CLASS_NAME     = "chartSizeClassName";
	public static final String LAYOUT_ATTRIBUTE_EXPORT_AS_WINDOW_BUTTON = "exportAsWindowButton";

	private final ChartProducer chartProducer;
    private String               exportHandlerId;
    private String               templatePath;
    private ResKey               downloadNameKey;
    private int                  chartWidth;
    private int                  chartHeight;
    private Color                bgColor;
    private ChartContext         exportChartContext;
    private ChartSizeManipulator chartSizeManipulator;
	private boolean exportAsWindowButton;

    /** Creates a {@link DefaultProducerChartComponent}. */
    public DefaultProducerChartComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
		this.chartProducer = atts.getChartProducer();
		this.chartSizeManipulator = atts.getChartSizeClassName();
		this.exportChartContext = atts.getExportContextClassName();
        
		this.exportAsWindowButton = atts.getExportAsWindowButton();
        this.exportHandlerId = StringServices.nonEmpty(atts.getExportHandlerId());
        this.templatePath    = StringServices.nonEmpty(atts.getTemplateFile());
		this.downloadNameKey = atts.getDownloadNameKey();
        this.chartWidth      = atts.getChartWidth();
        this.chartHeight     = atts.getChartHeight();
		this.bgColor = atts.getBgColor();
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return (anObject instanceof ChartContext)
            && this.chartProducer.supports((ChartContext) anObject);
    }

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);
		if (exportAsWindowButton) {
			if (oldValue != null) {
				oldValue.removeGroup(CommandHandlerFactory.EXPORT_BUTTONS_GROUP);
			}
			if (newValue != null && !StringServices.isEmpty(exportHandlerId)) {
				newValue.defineGroup(CommandHandlerFactory.EXPORT_BUTTONS_GROUP).addButton(
					getExportCommandModel(getCommandById(exportHandlerId)));
			}
		}
	}

    private CommandModel getExportCommandModel(CommandHandler exportHandler) {
		CommandModel commandModel =
			CommandModelFactory.commandModel(exportHandler, this, Collections.<String, Object> emptyMap());
		commandModel.setImage(exportHandler.getImage(this));
		commandModel.set(ButtonControl.BUTTON_RENDERER, ImageButtonRenderer.INSTANCE);
		return commandModel;
    }

    @Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		this.chartSizeManipulator.processChartSize(getModelAsChartContext(), dimension);
		super.prepareImage(context, imageId, dimension);
    }
    
    @Override
	protected JFreeChart createChart() {
        return createChart(getModelAsChartContext());
    }
    
    protected JFreeChart createChart(ChartContext chartContext) {
    	return this.getChartProducer().produceChart(chartContext);
    }
    
    @Override
	protected Dataset createDataSet(String anImageId) {
        // The chart is produced completely through the ChartProducer.
        // This method is unused.
        return null;
    }

    public ChartProducer getChartProducer() {
        return this.chartProducer;
    }

    @Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments) {
        checkLayoutAttributes();

        ChartContext chartContext = getExportChartContext();
        
        // Store the current background color and set the export background
        // color to the chart context. The color is reset after export is done
        // (see finally block).
        Paint exportBgColor = (Paint) chartContext.getValue(ChartProducer.VALUE_PAINT_BACKGROUND);
        chartContext.setValue(ChartProducer.VALUE_PAINT_BACKGROUND, this.bgColor);
        try {
            String     imageId = ValueUtil.getStringValue(chartContext.getValue(VALUE_IMAGE_ID), JFreeChartComponent.DEFAULT_IMAGE_ID);
            JFreeChart chart   = createChart(chartContext);

            if (chart != null) {
                this.getPostHandler().handle(this, chartContext, chart, false);
                ChartUtil.configurePlotForExport(chart.getPlot(), 12);
				Object image;
                try {
                    Dimension dimension = getExportDimension(imageId);
                    // The chart can be rendered as square chart. That means that the chart is square but
                    // without the legend. If a chart should be rendered as square chart the width is used
                    // as size. 
                    if (ValueUtil.getBooleanValue(chartContext.getValue(ChartConstants.VALUE_WRITE_SQUARE_CHART), false)) {
						if (!isWriteToTempFile()) {
							image = ChartUtil.getSquareChart(chart, dimension.width, null);
						} else {
							image = ChartUtil.writeSquareChartToFile(chart, dimension.width, null);
						}
                    } else {
						if (!isWriteToTempFile()) {
							image = ChartUtil.getChartAsPng(chart, dimension, null);
						} else {
							image = ChartUtil.writePathAsPngFile(chart, dimension, null);
						}
                    }
                    
                }
                catch (IOException ioe) {
                    throw new TopLogicException(this.getClass(), "Couldn't create the chart for detailed information see the exception message.", ioe);
                }
                
				if (image != null) {
                    if (WordExportHandler.COMMAND_ID.equals(this.exportHandlerId) || PowerpointExportHandler.COMMAND.equals(this.exportHandlerId)) {
                        HashMap<String, Object> valueMap = new HashMap<>();
						valueMap.put("PICTURE_" + imageId.toUpperCase(), image);
                        exportAdditionalValues(valueMap, arguments);

                        String theDownload = this.getDownloadLabel();
						String theTemplate = this.getTemplatePath();
						String theExt      = this.getExtension(theTemplate);

						return new OfficeExportValueHolder(theTemplate, theDownload + theExt, valueMap, false);
                    }
                    
                } else {
                    throw new TopLogicException(this.getClass(), "Could not export the chart because the image file was null.");
                }
            } 
            
            throw new TopLogicException(this.getClass(), "Could not export the chart because the chart producer ('" + getChartProducer().getClass().getName() + "') returned null.");
        } finally {
            // Reset the export background paint to the previous paint.
            chartContext.setValue(ChartProducer.VALUE_PAINT_BACKGROUND, exportBgColor);
        }
    }

	private String getExtension(String filename) {
		if (filename == null) return null;
		int index = filename.lastIndexOf('.');
		return index > 0 ? filename.substring(index) : StringServices.EMPTY_STRING;
	}

	/**
	 * Gets the download file name how to show the user.
	 */
	protected String getDownloadLabel() {
		ResKey downloadKey = getDownloadNameKey();
		if (downloadKey == null) {
			return Resources.getInstance().getString(getResPrefix().key("export.title"));
		}
		else {
			return Resources.getInstance().getString(downloadKey);
		}
	}

	/**
     * Exports additional context information.
     *
     * @param valueMap the map to put export replacements
     */
    protected void exportAdditionalValues(Map<String, Object> valueMap, Map arguments) {
		Resources resources = Resources.getInstance();
		valueMap.put("VALUE_LABEL_TITLE", resources.getString(I18NConstants.TITLE_LABEL));
		valueMap.put("VALUE_LABEL_DATE_OF_EXPORT", resources.getString(I18NConstants.DATE_LABEL));
		valueMap.put("VALUE_LABEL_CURRENT_USER", resources.getString(I18NConstants.USER_LABEL));

		valueMap.put("VALUE_TITLE", resources.getString(getResPrefix().key("export.title"), null));
        valueMap.put("VALUE_DATE_OF_EXPORT", HTMLFormatter.getInstance().getDateFormat().format(new Date()));
        valueMap.put("VALUE_CURRENT_USER", MetaLabelProvider.INSTANCE.getLabel(TLContext.getContext().getCurrentPersonWrapper()));
    }

	/** 
     * This method returns a new export chart context. 
     * 
     * 1) create a new chart context
     * 2) copy all values from the current model (component chart context)
     * 3) copy all values from the configured export chart context
     */
    protected ChartContext getExportChartContext() {
        if (this.exportChartContext == null) {
            return getModelAsChartContext();
        } else {
            ChartContext chartContext  = getModelAsChartContext(); 
            ChartContext exportContext = new FilterVO(chartContext.getModel(), chartContext.getFilteredObjects());
            copy(chartContext, exportContext);
            copy(this.exportChartContext, exportContext);
            return exportContext;
        }
    }

    private void copy(ChartContext from, ChartContext to) {
        for (Iterator iterator = from.getAllKeys(); iterator.hasNext();) {
            Object key   = iterator.next();
            Object value = from.getValue(key);
            to.setValue(key, value);
        }
        to.setReportConfiguration(from.getReportConfiguration());
    }
    
    private Dimension getExportDimension(String anImageId) {
        Dimension chartDimension = (Dimension) this.dimensions.get(anImageId);

        int width = chartDimension.width;
        if (this.chartWidth > 0) {
            width = this.chartWidth;
        }
        
        int height = chartDimension.height;
        if (this.chartHeight > 0) {
            height = this.chartHeight;
        }
        
        return new Dimension(width, height);
    }
    
    private void checkLayoutAttributes() {
        checkStringLayoutAttribute(LAYOUT_ATTRIBUTE_EXPORT_HANDLER_ID, this.exportHandlerId);
        checkStringLayoutAttribute(LAYOUT_ATTRIBUTE_TEMPLATE_FILE,     this.templatePath);
    }
    
    private void checkStringLayoutAttribute(String anLayoutAttribute, String aValue) {
        if (StringServices.isEmpty(aValue)) {
            throw new TopLogicException(this.getClass(), "The layout attribute ('" + anLayoutAttribute + "') is missing for component ('" + this.getName() + "').");
        }
    }

	/**
     * Returns the exportHandlerId.
     */
    public String getExportHandlerId() {
    	return (exportHandlerId);
    }

	/** 
	 * Return the template file for the export.
	 * 
	 * @return   The requested template name, never <code>null</code>.
	 */
	public String getTemplatePath() {
		return templatePath;
	}

	public final ResKey getDownloadNameKey() {
		return downloadNameKey;
	}

	public final int getChartWidth() {
		return chartWidth;
	}

	public final int getChartHeight() {
		return chartHeight;
	}

}

