/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.component;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.data.general.Dataset;

import com.top_logic.base.chart.DefaultImageData;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.chart.exception.ChartException;
import com.top_logic.base.chart.util.ChartUtil;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * A (Form)Component to generate Charts.
 * A JFreeChartComponent can handle more than one image.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class JFreeChartComponent extends AbstractImageComponent {

	/**
     * Constant for the default image id. The constant is useful if no image id 
     * (<code>null</code>) is available.
     * 
     * Is used to sore Object in HTTPRequest, too
     */
    public static final String DEFAULT_IMAGE_ID = "default";

	/**
	 * Configuration for the {@link JFreeChartComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractImageComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AbstractImageComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(GotoHandler.COMMAND);
		}

	}

    /**
     * A map of {@link Dataset}s. The keys are image ids and the values are
     * {@link Dataset}s.
     * 
     * TODO TDI/KHA drop these when becoming invible
     */
    protected HashMap datasets   = new HashMap(5);
    
    /**
     * A map of {@link JFreeChart}s. The keys are image ids and the values are
     * {@link JFreeChart}s.
     */
    protected HashMap charts     = new HashMap(5);
    
    /**
     * A map of {@link Dimension}s. The keys are image ids and the values are
     * {@link Dimension}s.
     */
    protected HashMap dimensions = new HashMap(5);
    
    /**
	 * A map of {@link HTMLFragment}s. The keys are image ids and the values are
	 * {@link HTMLFragment}s which represents image maps.
	 */
	protected HashMap<String, HTMLFragment> _imageMaps = new HashMap<>(5);
    
    /**
     * A map of {@link ChartRenderingInfo}s. The keys are image ids and the
     * values are {@link ChartRenderingInfo}s which represents image maps.
     */
    protected HashMap infoMap  = new HashMap(5);
    
    /** 
     * This boolean indicates whether the charts are cached.   
     * 
     * TODO TDI/KHA create Maps onnly when useChartCache is true.
     */
    private boolean useChartCache = false; /* Default */

    /**
     * This constructor is used by the layout framework to create a new
     * JFreeChartComponent with the given attributes.
     */
    public JFreeChartComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    /**
     * <p>
     * This method creates a {@link Dataset} for a chart. But a
     * JFreeChartComponent can handle more than one image, in this case this
     * method differentiates the data sets by the given image id.
     * </p>
     * <p>
     * Note, that the given image id can be <code>null</code> if only one
     * image is in use and that the data set fits to the chart!
     * </p>
     * 
     * @param  anImageId An image id.
     * @return Returns a {@link Dataset} for a chart.
     */
    protected abstract Dataset createDataSet(String anImageId);
    
    /**
     * <p>
     * This method creates a {@link JFreeChart} which based on a {@link Dataset}.
     * But a JFreeChartComponent can handle more than one image, in this case
     * this method differentiates the charts by the given image id.
     * </p>
     * <p>
     * Note, that the given image id can be <code>null</code> if only one
     * image is in use and that the chart use the correct data set!
     * </p>
     * 
     * @param  anImageId An image id.
     * @return Returns a {@link JFreeChart}.
     */
    protected abstract JFreeChart createChart(String anImageId);

    /**
     * Generic change handling, reset data set and chart for the default image
     * on any change of master.
     * 
     * @param aModel    The new model to be seen.
     * @param changedBy The modifier called this method.
     * @return Returns <code>true</code> to indicate that this part (or
     *         subparts) have become invalid.
     */
    @Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
        if (!dimensions.isEmpty()
         && supportsInternalModel(aModel)) {
            resetCaches();
            removeFormContext();
            this.invalidate();
            return true;
        }
        return super.receiveModelChangedEvent(aModel, changedBy);
    }

    /**
	 * If you overwrite this method note that you have to put the returned dimension into the
	 * {@link #dimensions} map.
	 * 
	 * @see com.top_logic.base.chart.ImageComponent#prepareImage(com.top_logic.layout.DisplayContext,
	 *      java.lang.String, java.awt.Dimension)
	 */
    @Override
	public void prepareImage(DisplayContext context, String imageId, Dimension dimension) throws IOException {
		double width = dimension.getWidth();
		double height = dimension.getHeight();
    	
		if (width <= 0) {
			Logger.info("Chart width " + width + " is zero or negative. " +
				"This can happen if a chart window is invisible because of another chart window is maximized.",
				this);
    		width = 1;
			dimension.setSize(width, height);
    	}
    	
		if (height <= 0) {
			Logger.info("Chart height " + height + " is is zero or negative. " +
				"This can happen if a chart window is invisible because of another chart window is maximized.",
				this);
    		height = 1;
			dimension.setSize(width, height);
    	}

		dimensions.put(getKey(imageId), dimension);
    }

    /**
	 * <p>
	 * This method creates an png image in the image tmp directory and returns the source for the
	 * layout image tag.
	 * </p>
	 * <p>
	 * If the caching is enabled, this method tries to get the chart from the cache. If is the chart
	 * not in the cache a new chart will be created and stored in the cache.
	 * </p>
	 * 
	 * @see com.top_logic.base.chart.ImageComponent#createImage(DisplayContext, java.lang.String,
	 *      String, Dimension)
	 */
    @Override
	public ImageData createImage(DisplayContext context, String imageId, String imageType, Dimension dimension)
			throws IOException {
        try {
            JFreeChart theChart;
			Dimension theDimension = (Dimension) dimensions.get(getKey(imageId));
			if (theDimension == null) {
				// In a unit test (without UI), there is no control providing the client dimensions.
				// Incremental image updates cannot be rendered.
				return null;
			}
            if(useChartCache){
				theChart = getChart(imageId);
            }
            else{
				theChart = createChart(imageId);
            }
            if (theChart == null) {
				BinaryData nullGif = FileManager.getInstance().getData("/images/null.png");
				return new DefaultImageData(new Dimension(1, 1), nullGif);
            }
            
			customizeChart(imageId, theChart);
        
            ChartRenderingInfo renderingInfo = new ChartRenderingInfo(new StandardEntityCollection());
            
			ImageData result = ChartUtil.encode(isWriteToTempFile(), theDimension, theChart, renderingInfo);
			this.infoMap.put(getKey(imageId), renderingInfo);
			return result;
        }
        catch (Exception e) {
			Logger.error("Could not create chart for the image id: '" + imageId + "'.", e, this.getClass());
            throw new ChartException(this.getClass(), "", e);
        }
    }

    /**
	 * <p>
	 * This method returns the image map for the given image id.
	 * </p>
	 * <p>
	 * If the caching is enabled, this method tries to get the image map from the cache. If is the
	 * image map not in the cache a new image map will be created and stored in the cache.
	 * </p>
	 * 
	 * @see com.top_logic.base.chart.ImageComponent#getImageMap(java.lang.String, java.lang.String,
	 *      Dimension)
	 */
    @Override
	public HTMLFragment getImageMap(String imageId, String mapName, Dimension dimension)
			throws IOException {
		ChartRenderingInfo chartInfo = (ChartRenderingInfo) this.infoMap.get(getKey(imageId));
        if (chartInfo == null) return null;
		if(useChartCache){
			HTMLFragment theImageMap = _imageMaps.get(getKey(imageId));
            if(StringServices.isEmpty(theImageMap)){
				theImageMap = ChartUtil.getImageMap(mapName, chartInfo);
				_imageMaps.put(getKey(imageId), theImageMap);
            }
            return theImageMap;
        }
        else{
			return ChartUtil.getImageMap(mapName, chartInfo);
        }
    }

    /**
     * This method is a hook for subclasses to customize the chart with the
     * given image id. The default implementation sets only the chart background
     * to the current theme background.
     * @param anImageId An image id.
     * @param aChart    A {@link JFreeChart}.
     */
    protected void customizeChart(String anImageId, JFreeChart aChart) {
        if (null != aChart) {
			aChart.setBackgroundPaint(JFreeChartComponent.getThemeBackgroundColor());
			aChart.getPlot().setBackgroundPaint(JFreeChartComponent.getThemeChartBackgroundColor());
        }
    }

    /** 
     * This method returns the current theme background color.
     * 
     * @return Returns the current theme background color.
     */
    public static Color getThemeBackgroundColor(){
		return getThemeBackgroundColor(Icons.BODY_BACK, Icons.BODY_BACK, true);
	}

	/**
	 * This method returns the current theme background color for the content of the chart himself.
	 * 
	 * @return Returns the current theme background color for the chart content.
	 */
	public static Color getThemeChartBackgroundColor() {
		return getThemeBackgroundColor(Icons.CHART_BACK, Icons.BODY_BACK, false);
	}

	/**
	 * Return the requested color defined in the theme.
	 * 
	 * @param key
	 *        The requested theme key for the color, must not be <code>null</code>.
	 * @param defaultKey
	 *        The theme key for the backup color, must not be <code>null</code>.
	 * @param transparent
	 *        Flag, if color should be transparent (for {@link #getThemeBackgroundColor()}.
	 * @return The requested color, never <code>null</code>.
	 */
	public static Color getThemeBackgroundColor(ThemeVar<Color> key, ThemeVar<Color> defaultKey, boolean transparent) {
		Theme theme = ThemeFactory.getInstance().getCurrentTheme();
		Color colorValue = theme.getValue(key);

		if (colorValue == null) {
			colorValue = theme.getValue(defaultKey);
		}

		Color resultColor;
		if (colorValue != null) {
			resultColor = colorValue;

			if (transparent) {
				resultColor = new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), 0);
			}
		} else {
			resultColor = Color.WHITE;
		}

		return resultColor;
	}
    
    /**
     * If only one image is in use, this method returns the {@link Dataset} in a
     * lazy way. If no {@link Dataset} exists this method creates one.
     * 
     * @return Returns the {@link Dataset} in a lazy way. If no {@link Dataset}
     *         exists this method creates one.
     */
    public Dataset getDataSet() {
        return getDataSet(null);
    }
    
    /**
     * This method returns the {@link Dataset} for the given image id in a lazy
     * way. If no {@link Dataset} exists this method creates one.
     * 
     * @param  anImageId An image id.
     * @return Returns the {@link Dataset} for the given image id in a lazy way.
     *         If no {@link Dataset} exists it will be create one.
     */
    public Dataset getDataSet(String anImageId){
        anImageId = anImageId == null? DEFAULT_IMAGE_ID : anImageId;
        Dataset theDataset = (Dataset)datasets.get(anImageId);
        if(theDataset == null){
            theDataset = createDataSet(anImageId);
            datasets.put(anImageId, theDataset);
        }
        return theDataset;
    }
    
    /**
     * If only one image is in use, this method sets the given {@link Dataset}
     * to the default image.
     * 
     * @param aDataset A {@link Dataset}.
     */
    public void setDataSet(Dataset aDataset){
        setDataSet(aDataset, null);
    }
    
    /**
     * This method sets a {@link Dataset} for the given image id.
     * 
     * @param aDataset  A {@link Dataset}.
     * @param anImageId A image id.
     */
    public void setDataSet(Dataset aDataset, String anImageId){
        anImageId = anImageId == null? DEFAULT_IMAGE_ID : anImageId;
        datasets.put(anImageId, aDataset);
    }
    
    /**
     * If only one image is in use, this method returns the {@link JFreeChart}
     * in a lazy way. If no {@link JFreeChart} exists this method creates one.
     * 
     * @return Returns the {@link JFreeChart} in a lazy way. If no
     *         {@link JFreeChart} exists this method creates one.
     */
    public JFreeChart getChart() {
        return getChart(null);
    }
    
    /**
     * This method returns the {@link JFreeChart} for the given image id in a
     * lazy way. If no {@link JFreeChart} exists this method creates one.
     * 
     * @param  anImageId An image id.
     * @return Returns the {@link JFreeChart} for the given image id in a lazy
     *         way. If no {@link JFreeChart}exists it will be create one.
     */
    public JFreeChart getChart(String anImageId){
        anImageId = anImageId == null? DEFAULT_IMAGE_ID : anImageId;
        JFreeChart theChart;
        if (useChartCache) {
            theChart = (JFreeChart)charts.get(anImageId);
            if(theChart == null){
                theChart = createChart(anImageId);
                charts.put(anImageId, theChart);
            }
        } else { // do not use cache
            theChart = createChart(anImageId);
        }
        return theChart;
    }
    
    /**
     * If only one image is in use, this method sets the given
     * {@link JFreeChart} to the default image.
     * 
     * @param aChart A {@link JFreeChart}.
     */
    public void setChart(JFreeChart aChart){
        setChart(aChart, null);
    }
    
    /**
     * This method sets a {@link JFreeChart} for the given image id.
     * 
     * @param aChart    A {@link JFreeChart}.
     * @param anImageId A image id.
     */
    public void setChart(JFreeChart aChart, String anImageId){
        anImageId = anImageId == null? DEFAULT_IMAGE_ID : anImageId;
        charts.put(anImageId, aChart);
    }

    /**
     * Reset Caches, use when model changes ... 
     */
    protected void resetCaches() {
        dimensions.clear();
        _imageMaps .clear();
        datasets  .clear();
        infoMap   .clear();
        if (useChartCache) {
            charts.clear();
        }
    }
    
    /**
     * This method returns for the given image identifier the key. The key can
     * be used for the internal caches.
     * 
     * @param anImageId
     *        A image identifier.
     * @return A key {@link String}.
     */
    protected String getKey(String anImageId) {
        return anImageId != null ? anImageId : DEFAULT_IMAGE_ID;
    }
    
    /**
     * When true charts and datasets are cached in some maps.
     * 
     * @return Returns whether the chart cache is used.
     */
    public boolean isUseChartCache(){
        return useChartCache;
    }
    
    /** 
     * When true charts and datasets are cached in some maps.
     */
    public void setUseChartCache(boolean aFlag){
        useChartCache = aFlag;
    }

    /** 
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        return null;
    }

}
