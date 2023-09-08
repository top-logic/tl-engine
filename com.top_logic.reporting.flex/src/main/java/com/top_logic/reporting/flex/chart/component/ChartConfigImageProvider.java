/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.DefaultImageData;
import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.chart.ImageControl;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.chart.exception.ChartException;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.model.ImageField;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.form.tag.js.JSValue;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.component.handler.OpenChartDetailsCommand;
import com.top_logic.reporting.flex.chart.config.ChartConfig;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartContext;
import com.top_logic.reporting.flex.chart.config.chartbuilder.ChartData;
import com.top_logic.reporting.flex.chart.config.chartbuilder.JFreeChartBuilder;
import com.top_logic.reporting.flex.chart.config.datasource.ChartDataSource;
import com.top_logic.reporting.flex.chart.config.datasource.ComponentDataContext;
import com.top_logic.reporting.flex.chart.config.model.ChartNode;
import com.top_logic.reporting.flex.chart.config.model.ChartTree;
import com.top_logic.reporting.flex.chart.config.model.ChartTree.DataKey;
import com.top_logic.reporting.flex.chart.config.util.Configs;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Supporting class for creating {@link ImageField}s by using a {@link ChartConfig}.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class ChartConfigImageProvider implements ImageComponent, ControlProvider, ComponentDataContext {

    /** A map of {@link ChartRenderingInfo}s. The keys are image IDs and the values are {@link ChartRenderingInfo}s which represents image maps. */
    protected ChartRenderingInfo info;

	private ChartConfig _config;

	private final LayoutComponent _component;

	ChartControl _control;

	@SuppressWarnings("rawtypes")
	private ChartData _chartData;

	/**
	 * Creates a {@link ChartConfigImageProvider}.
	 * 
	 * @param config
	 *        The chart-config, must not be <code>null</code>.
	 */
	public ChartConfigImageProvider(ChartConfig config, LayoutComponent component) {
		if (config == null) {
			throw new IllegalArgumentException("Given producer must not be null!");
		}
		_component = component;
		_config = config;
	}

	@Override
	public LayoutComponent getComponent() {
		return _component;
	}

	/**
	 * the {@link ChartConfig} that describes the chart created by this
	 *         {@link ImageComponent}
	 */
	public ChartConfig getConfig() {
		return _config;
	}

	/**
	 * Resets the chartData and triggers a redraw with the current config.
	 */
	public void resetChartData() {
		_chartData = null;
		if (_control != null) {
			_control.requestRepaint();
		}
	}

	/**
	 * Resets the chartData, updates the config and triggers a redraw.
	 */
	public void updateChartConfig(ChartConfig config) {
		_chartData = null;
		if (config != null) {
			_config = config;
		}
		if (_control != null) {
			_control.requestRepaint();
		}
	}

	@Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		this.prepareImage(dimension);
	}

	@Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		return (this.info == null) ? null : ChartUtil.getImageMap(mapName, this.info);
	}

	@Override
	public ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException {

		BinaryData data = createImageAsBinaryData(imageId, dimension);
		if (data == null) {
			data = FileManager.getInstance().getData("/images/null.png");
		}
		return new DefaultImageData(dimension, data);
	}

	private BinaryData createImageAsBinaryData(String anImageId, Dimension aDimension) throws ChartException {
		try {
			JFreeChart chart = createChart();

			if (chart == null) {
				return null;
			}
			else {
				info = new ChartRenderingInfo(new StandardEntityCollection());

				return getChartAsBinaryData(chart, aDimension);
			}
		} catch (IOException ex) {
			Logger.error("Could not create chart for the image id: '" + anImageId + "'.", ex,
				ChartConfigImageProvider.class);
			throw new ChartException(ChartConfigImageProvider.class, "", ex);
		}
	}

	/**
	 * Generate an encoded representation of the image for the given chart.
	 * 
	 * @param chart
	 *        The {@link JFreeChart} to generate the image for.
	 * @param dim
	 *        The chart's {@link Dimension}.
	 * @return The {@link BinaryData} containing the encoded image.
	 * @throws IOException
	 *         If an error occurred while generating the chart.
	 */
	protected BinaryData getChartAsBinaryData(JFreeChart chart, Dimension dim) throws IOException {
		return ChartUtil.getChartAsPng(chart, dim, info);
	}

	/** 
	 * Prepare the image by handling the dimension of it.
	 * 
	 * @param    aDimension    The dimension of the managed image, must not be <code>null</code>.
	 * @return   The given dimension (may have been modified), never <code>null</code>.
	 */
	public Dimension prepareImage(Dimension aDimension) {
    	double theWidth  = aDimension.getWidth();
    	double theHeight = aDimension.getHeight(); 

    	if (theWidth < 0) {
    		Logger.info("The width ('" + theWidth + "') of the chart is smaller than zero. That can be happen if a chart window is invisible because of another chart window is maximized.", this);
    		theWidth = 1;
    		aDimension.setSize(theWidth, theHeight);
    	}

    	if (theHeight < 0) {
    		Logger.info("The height ('" + theHeight + "') of the chart is smaller than zero. That can be happen if a chart window is invisible because of another chart window is maximized.", this);
    		theHeight = 1;
    		aDimension.setSize(theWidth, theHeight);
    	}

        return aDimension;
	}
	
	/** 
	 * Create an image and store it on the application server.
	 * 
	 * @param    anImageId    The ID of the requested image, must not be <code>null</code>.
	 * @param	 aDimension	  The dimension of the image, must no be <code>null</code>.
	 * @return   The URL of the create image, may be <code>null</code>.
	 * @throws   ChartException    If storing the image to the application server fails.
	 */
	public String createImage(String anImageId, Dimension aDimension) throws ChartException {
        try {
			JFreeChart theChart = createChart();

            if (theChart == null) {
				return null;
            }
            else {
	            this.info = new ChartRenderingInfo(new StandardEntityCollection());
				return this.getChartURL(theChart, this.info, aDimension);
            }
        }
        catch (IOException ex) {
			Logger.error("Could not create chart for the image id: '" + anImageId + "'.", ex,
				ChartConfigImageProvider.class);
			throw new ChartException(ChartConfigImageProvider.class, "", ex);
        }
	}

	/**
	 * Creates a {@link JFreeChart} according to the {@link ChartConfig} of this
	 * {@link ImageComponent}. The dataset is only created once for performance-reasons.
	 * 
	 * @return a new chart based on the configured data
	 */
	public JFreeChart createChart() {
		JFreeChartBuilder<? extends Dataset> chartBuilder = _config.getChartBuilder();
		if (_chartData == null) {
			ChartDataSource dataSource = _config.getDataSource();
			Collection<Object> rawData = dataSource.getRawData(this);
			ChartTree tree = _config.getModelPreparation().prepare(rawData);
			Dataset dataset = chartBuilder.getDatasetBuilder().getDataset(tree);
			_chartData = new ChartData<>(tree, dataset);
		}
		JFreeChart chart = chartBuilder.createChart(_control, _chartData);
		return chart;
	}

	ChartTree getChartTree() {
		return _chartData.getModel();
	}

	/** 
	 * Create an image out of the given parameters.
	 * 
	 * @param    aChart       The chart to create the image from, must not be <code>null</code>.
	 * @param    anInfo       The additional information object to be filled, must not be <code>null</code>.
	 * @param    aDim         The dimension to create the chart for, must not be <code>null</code>.
	 * @return   The path to the requested image, never <code>null</code>.
	 * @throws   IOException    When creating the image failed.
	 */
	protected String getChartURL(JFreeChart aChart, ChartRenderingInfo anInfo, Dimension aDim) throws IOException {
		return ChartUtil.writeChartAsPng(aChart, aDim, anInfo);
	}

	@Override
	public Control createControl(Object aModel, String aStyle) {
		ImageField imageField = (ImageField) aModel;
		String id = imageField.getQualifiedName();

		_control = new ChartControl(imageField, id, new FormModeModelAdapter(imageField), this);
		return _control;
	}

	/**
	 * Same as {@link #newImageField(String, ChartConfig, Dimension, LayoutComponent)} but
	 * {@link ChartConfig} will be read from given filename.
	 */
	public static ImageField newImageField(String aName, String configFile, Dimension aDim, LayoutComponent component) {
		ChartConfig config = Configs.readChartConfig(configFile);
		return newImageField(aName, config, aDim, component);
	}

    /**
	 * Create an image field for the given parameters.
	 * 
	 * @param aName
	 *        The name of the new created field, must not be <code>null</code>.
	 * @param aConfig
	 *        The chart-config.
	 * @param aDim
	 *        The dimension to create the chart for, must not be <code>null</code>.
	 * @param component
	 *        Used for {@link ComponentDataContext}, may be null if no context is necessary for
	 *        chart.
	 * @return The requested image field, never <code>null</code>.
	 */
	public static ImageField newImageField(String aName, ChartConfig aConfig, Dimension aDim, LayoutComponent component) {
		final ChartConfigImageProvider provider = new ChartConfigImageProvider(aConfig, component);
		ImageField field = new ImageField(aName, provider, aDim);

		field.setControlProvider(provider);

    	return field;
    }

	/**
	 * The generic detail-dialog only works with single objects or wrapper-valued collections. This
	 * interface can be used if the detail-dialog should display objects that does not match these
	 * criteria (e.g. no {@link Wrapper}).
	 */
	public interface DetailDialogAware {

		/**
		 * The {@link DetailDialogAware} component must take care of displaying the given objects.
		 * 
		 * @param field
		 *        the image-field holding the chart for context-information.
		 * @param node
		 *        the node containing the objects to display of providing context-information what
		 *        to display
		 * @param commandContext
		 *        the current command-context
		 * @return the result of the command
		 */
		public HandlerResult displayObjects(ImageField field, ChartNode node, DisplayContext commandContext);

		/**
		 * The {@link DetailDialogAware} must decide if it is responsible of showing details for the
		 * given node. If not, the default-code will be called (see Class-comment).
		 * 
		 * @param field
		 *        the image-field holding the chart for context-information.
		 * @param node
		 *        the node to check if this component is responsible
		 * @return true if the component should handle the displaying of the objects, even if this
		 *         means, there is nothing to show. False to use the default-implementation.
		 */
		public boolean isResponsibleFor(ImageField field, ChartNode node);

	}

	private static class ChartControl extends ImageControl implements ChartContext {

		private static final String OPEN_CHART_DETAILS = "openChartDetails";

		private static final Map<String, ControlCommand> CHART_CONTROL_COMMANDS = createCommandMap(ImageControl.COMMANDS,
			new ControlCommand[] { new ControlCommand(OPEN_CHART_DETAILS) {

				@Override
				protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
					ChartControl chartControl = (ChartControl) control;
					LayoutComponent component = chartControl.getComponent();
					String nodeID = (String) arguments.get(OpenChartDetailsCommand.PARAMETER_ID);
					ChartTree tree = chartControl.getChartTree();
					ImageField field = chartControl.getImageField();
					ChartNode node = tree.getNode(nodeID);
					if (component instanceof DetailDialogAware) {
						if (((DetailDialogAware) component).isResponsibleFor(field, node)) {
							return ((DetailDialogAware) component).displayObjects(field, node, commandContext);
						}
					}
					List<?> objects = node.getObjects();
					if (AbstractChartComponent.showSingleObject(component, objects)) {
						return HandlerResult.DEFAULT_RESULT;
					}
					Object first = CollectionUtil.getFirst(objects);
						if (!(first instanceof TLObject)) {
						return HandlerResult.DEFAULT_RESULT;
					}

					DetailsTableDialog dialog = DetailsTableDialog.getDialog(component);
						TLClass me = (TLClass) ((TLObject) first).tType();
					AttributedSearchResultSet result = toSearchResult(objects, me);
					dialog.setSearchResult(result);
					LayoutComponent topLayout = dialog.getDialogTopLayout();
					DataKey dataKey = tree.getDataKey(nodeID);
					OpenModalDialogCommandHandler.openDialog(topLayout, new ConstantDisplayValue(String.valueOf(dataKey)));
					return HandlerResult.DEFAULT_RESULT;
				}

				private AttributedSearchResultSet toSearchResult(List<?> objects, TLClass me) {
						Collection<TLObject> wrappers =
							CollectionUtil.dynamicCastView(TLObject.class, (Collection<?>) objects);
					List<String> defaultColumns = Collections.singletonList("name");
					AttributedSearchResultSet result = new AttributedSearchResultSet(wrappers, me, defaultColumns, null);
					return result;
				}

				@Override
				public ResKey getI18NKey() {
					return I18NConstants.OPEN_CHART_DETAILS;
				}
			}
		});

		private final ChartConfigImageProvider _provider;

		private final ImageField _field;

		protected ChartControl(ImageField aModel, String aImageId, ModeModel modeModel, ChartConfigImageProvider provider) {
			super(aModel, aImageId, modeModel, CHART_CONTROL_COMMANDS, null);
			_field = aModel;
			_provider = provider;
		}

		protected ImageField getImageField() {
			return _field;
		}

		protected ChartTree getChartTree() {
			return _provider.getChartTree();
		}
		
		protected LayoutComponent getComponent() {
			return _provider.getComponent();
		}

		private ControlCommand getOpenerCommand() {
			return getCommand(OPEN_CHART_DETAILS);
		}

		@Override
		public String getUrl(String id) {
			Map<String, JSValue> args = new HashMap<>();
			args.put(OpenChartDetailsCommand.PARAMETER_ID, new JSString(id));
			StringBuffer result = new StringBuffer();
			try {
				getOpenerCommand().writeInvokeExpression(result, this, new JSObject(args));
			} catch (IOException ex) {
				throw new UnreachableAssertion(ex);
			}
			return result.toString();
		}

	}

}
