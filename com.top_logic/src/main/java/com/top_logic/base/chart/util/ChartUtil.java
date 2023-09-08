/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.Range;

import com.top_logic.base.chart.DefaultImageData;
import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.chart.ImageData;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * The ChartUtil contains useful static methods for charts.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ChartUtil {

    private static final String IMAGE_TYPE_PNG = ImageComponent.IMAGE_TYPE_PNG;

    private ChartUtil() {
        // No public constructor is needed. 
        // This class has only static methods.
    }

    /**
     * This method creates an image (png) in the image tmp directory and returns
     * the source path of the image file. If the info objects is unlike
     * <code>null</code>, this method fills it with rendering information.
     * 
     * @param aChart
     *        The chart. Must not be <code>null</code>.
     * @param aDimension
     *        The dimension of the image. Must not be <code>null</code>.
     * @param aInfo
     *        The empty rendering info. The rendering info is filled with
     *        information during the image generation. The rendering info can be
     *        used to create an image map. The info can be <code>null</code>
     *        if no rendering info is needed.
     * @return The source path of the generated image file.
	 * 
	 * @see #getChartAsPng(JFreeChart, Dimension, ChartRenderingInfo)
	 */
    public static String writeChartAsPng(JFreeChart aChart, Dimension aDimension, ChartRenderingInfo aInfo) throws IOException {
        return getFilePath(writePathAsPngFile(aChart, aDimension, aInfo));
    }

	/**
	 * This method creates an image (png) in memory and returns it as {@link BinaryData}. If the
	 * info objects is unlike <code>null</code>, this method fills it with rendering information.
	 * 
	 * @param info
	 *        The empty rendering info. The rendering info is filled with information during the
	 *        image generation. The rendering info can be used to create an image map. The info can
	 *        be <code>null</code> if no rendering info is needed.
	 * 
	 * @return A {@link BinaryData} containing the image.
	 * 
	 * @see #writeChartAsPng(JFreeChart, Dimension, ChartRenderingInfo)
	 */
	public static BinaryData getChartAsPng(JFreeChart chart, Dimension dimension, ChartRenderingInfo info)
			throws IOException {
		InMemoryBinaryData data = new InMemoryBinaryData(ImageComponent.MIME_PNG);
		try {
			ChartUtils.writeChartAsPNG(data, chart, dimension.width, dimension.height, info);
		} finally {
			data.close();
		}
		return data;
	}

	/**
	 * This method creates an image (png) in the image tmp directory and returns the the image file.
	 * If the info objects is unlike <code>null</code>, this method fills it with rendering
	 * information.
	 * 
	 * @param aChart
	 *        The chart. Must not be <code>null</code>.
	 * @param aDimension
	 *        The dimension of the image. Must not be <code>null</code>.
	 * @param aInfo
	 *        The empty rendering info. The rendering info is filled with information during the
	 *        image generation. The rendering info can be used to create an image map. The info can
	 *        be <code>null</code> if no rendering info is needed.
	 * @return The generated image file.
	 */
	public static File writePathAsPngFile(JFreeChart aChart, Dimension aDimension, ChartRenderingInfo aInfo)
			throws IOException, FileNotFoundException {
		if (aChart == null) {
            throw new IllegalArgumentException("The chart must NOT be null!");
        }
        if (aDimension == null) {
            throw new IllegalArgumentException("The dimension must NOT be null!");
        }
        
        File             theFile = getImageFile(IMAGE_TYPE_PNG);
        FileOutputStream theOut  = new FileOutputStream(theFile);
		try {
			writeChartAsPng(theOut, aChart, aDimension, aInfo);
		} finally {
			StreamUtilities.close(theOut);
		}
		return theFile;
	}

    /**
	 * This method creates an image (png) in the image tmp directory and returns the source path of
	 * the image file. If the info objects is unlike <code>null</code>, this method fills it with
	 * rendering information.
	 * 
	 * @param aChart
	 *        The chart. Must not be <code>null</code>.
	 * @param aSize
	 *        The size of the image (>0).
	 * @param aInfo
	 *        The empty rendering info. The rendering info is filled with information during the
	 *        image generation. The rendering info can be used to create an image map. The info can
	 *        be <code>null</code> if no rendering info is needed.
	 * @return The source path of the generated image file.
	 * 
	 * @see #getSquareChart(JFreeChart, int, ChartRenderingInfo)
	 */
    public static String writeSquareChart(JFreeChart aChart, int aSize, ChartRenderingInfo aInfo) throws IOException {
		return getFilePath(writeSquareChartToFile(aChart, aSize, aInfo));
    }

	/**
	 * /** This method creates an image (png) in the image tmp directory and returns it. If the info
	 * objects is unlike <code>null</code>, this method fills it with rendering information.
	 * 
	 * @param aChart
	 *        The chart. Must not be <code>null</code>.
	 * @param aSize
	 *        The size of the image (>0).
	 * @param aInfo
	 *        The empty rendering info. The rendering info is filled with information during the
	 *        image generation. The rendering info can be used to create an image map. The info can
	 *        be <code>null</code> if no rendering info is needed.
	 * @return The generated image file.
	 */
	public static File writeSquareChartToFile(JFreeChart aChart, int aSize, ChartRenderingInfo aInfo)
			throws IOException, FileNotFoundException {
		Dimension dim = getSquareChartDimension(aChart, aSize);
		File writePathAsPngFile = ChartUtil.writePathAsPngFile(aChart, dim, aInfo);
		return writePathAsPngFile;
	}

	/**
	 * Equal to {@link #writeChartAsPng(OutputStream, JFreeChart, Dimension, ChartRenderingInfo)}
	 * but does not write the chart to a temporary file but returns a {@link BinaryData} holding the
	 * data.
	 * 
	 * @see #writeSquareChart(JFreeChart, int, ChartRenderingInfo)
	 */
	public static BinaryData getSquareChart(JFreeChart aChart, int aSize, ChartRenderingInfo aInfo) throws IOException {
		Dimension dim = getSquareChartDimension(aChart, aSize);
		return ChartUtil.getChartAsPng(aChart, dim, aInfo);
	}

	private static Dimension getSquareChartDimension(JFreeChart chart, int size) throws IOException {
		LegendTitle legend = chart.getLegend();
        if (legend == null) {
			return new Dimension(size, size);
        } else {
            // We must write the chart first that the legend width and height is
            // calculated. 
			ChartUtil.writeChartAsPng(new ByteArrayOutputStream(), chart, new Dimension(size, size), null);

			legend = chart.getLegend();
            RectangleEdge position = legend.getPosition();
            if ( (position.equals(ChartConstants.LEGEND_TOP)) || (position.equals(ChartConstants.LEGEND_BOTTOM))) {
				return new Dimension(size, size + (int) legend.getHeight());
            } else {
				return new Dimension(size + (int) legend.getWidth(), size - 10);
            }
        }
	}
    
    /**
     * This method writes the chart into the output stream. If the info objects
     * is unlike <code>null</code>, this method fills it with rendering
     * information.
     * 
     * @param anOut
     *        A {@link OutputStream}. Must not be <code>null</code>, will be closed when done.
     * @param aChart
     *        The chart. Must not be <code>null</code>.
     * @param aDimension
     *        The dimension of the image. Must not be <code>null</code>.
     * @param aInfo
     *        The empty rendering info. The rendering info is filled with
     *        information during the image generation. The rendering info can be
     *        used to create an image map. The info can be <code>null</code>
     *        if no rendering info is needed.
     */
    public static void writeChartAsPng(OutputStream anOut, JFreeChart aChart, Dimension aDimension, ChartRenderingInfo aInfo) throws IOException {
        if (anOut == null) {
            throw new IllegalArgumentException("the output stream must NOT be null!");
        }
        if (aChart == null) {
            throw new IllegalArgumentException("The chart must NOT be null!");
        }
        if (aDimension == null) {
            throw new IllegalArgumentException("The dimension must NOT be null!");
        }
        
		ChartUtils.writeChartAsPNG(anOut, aChart, aDimension.width, aDimension.height, aInfo);
		StreamUtilities.close(anOut);
    }
    
    /** 
     * Utility method to stream some data into a file located in the web app area.
     * 
     * @param    anInput    The stream to be read, must not be <code>null</code>.
     * @param    aFileType    The type of the image to be written, must not be <code>null</code>.
     * @return   The URL part describing the destination file in the web app.
     * @throws   IOException    If copying file failed for a reason.
     */
    public static String writeImage(InputStream anInput, String aFileType) throws IOException {
        File theFile = ChartUtil.getImageFile(aFileType);

        FileUtilities.copyFile(anInput, theFile);
        
        return ChartUtil.getFilePath(theFile);
    }

	/**
	 * This method write the image into a {@link BinaryData}.
	 * 
	 * @param image
	 *        The image to draw. Must NOT be <code>null</code>.
	 * @see #writeImageAsPng(BufferedImage)
	 */
	public static BinaryData getImageAsPng(BufferedImage image) throws IOException {
		InMemoryBinaryData data = new InMemoryBinaryData(ImageComponent.MIME_PNG);
		try {
			ImageIO.write(image, "PNG", data);
		} finally {
			data.close();
		}
		return data;
	}

    /**
	 * This method write the image into a tmp file and returns it.
	 * 
	 * @param image
	 *            The image to draw. Must NOT be <code>null</code>.
	 * @see #getImageAsPng(BufferedImage)
	 */
    public static File writeImageAsPng(BufferedImage image) throws IOException {
    	File             theFile = getImageFile(IMAGE_TYPE_PNG);
        FileOutputStream theOut  = new FileOutputStream(theFile);
        try {
        	ImageIO.write(image, "PNG", theOut);
			 
        } finally {
        	StreamUtilities.close(theOut);        	
        }
        
        return theFile;
    }
    
    /**
	 * This method write the image into a tmp file and returns the relative path
	 * without the context name.
	 * 
	 * @param image
	 *            The image to draw. Must NOT be <code>null</code>.
	 */
    public static String writeImageAsPngPath(BufferedImage image) throws IOException {
    	return getFilePath(writeImageAsPng(image));
    }
    
    
    /**
	 * This method returns for the given rendering info the image map as {@link HTMLFragment}.
	 * 
	 * @param aMapName
	 *        A map name. Must not be <code>null</code>.
	 * @param anInfo
	 *        An {@link ChartRenderingInfo}. Must not be <code>null</code>.
	 * @return Returns a image map as {@link HTMLFragment}.
	 */
    public static HTMLFragment getImageMap(String aMapName, ChartRenderingInfo anInfo) {
        return Fragments.htmlSource(getImageMapAsString(aMapName, anInfo));
    }

	/**
	 * This method returns for the given rendering info the image map as {@link String}.
	 * 
	 * @param aMapName
	 *        A map name. Must not be <code>null</code>.
	 * @param anInfo
	 *        An {@link ChartRenderingInfo}. Must not be <code>null</code>.
	 * @return Returns a image map as {@link String}.
	 */
	public static String getImageMapAsString(String aMapName, ChartRenderingInfo anInfo) {
		if (aMapName == null) {
            throw new IllegalArgumentException("The map name must NOT be null.");
        }
        if (anInfo == null) {
            throw new IllegalArgumentException("The chart rendering info must NOT be null.");
        }
        
		return ChartUtils.getImageMap(aMapName, anInfo, OverlibTooltipFragmentGenerator.INSTANCE,
			OnClickURLTagFragmentGenerator.INSTANCE);
    }
    
	public static File getImageFile(String anImageType) throws IOException {
		File theTmpDir = Settings.getInstance().getImageTempDir();
        File theTmpFile = File.createTempFile(StringServices.getRandomString(20), anImageType, theTmpDir);
        
        return theTmpFile;
    }
    
    public static String getFilePath(File aFile) {
		return (Settings.getInstance().getImageTempDirName() + '/' + aFile.getName());
    }
    
    /**
	 * Takes the plot and sets the font for the ticklabels to the defined font.
	 * The default size 10 seems to be to small
	 * 
	 * @param plot the plot to configure.
	 */
    public static void configurePlotForExport(Plot plot, int fontSize) {
        /** the font used for exporting charts */
        final Font TICK_FONT = new Font("SansSerif", Font.PLAIN, fontSize);
        
    	if(plot instanceof XYPlot) {
    		XYPlot thePlot = (XYPlot)plot;
    		thePlot.getDomainAxis().setTickLabelFont(TICK_FONT);
    		thePlot.getRangeAxis().setTickLabelFont(TICK_FONT);
    	}
    	else if(plot instanceof CategoryPlot) {
    		CategoryPlot thePlot = (CategoryPlot)plot;
    		thePlot.getDomainAxis().setTickLabelFont(TICK_FONT);
    		thePlot.getRangeAxis().setTickLabelFont(TICK_FONT);
    	}
    	else if(plot instanceof SpiderWebPlot) {
    		SpiderWebPlot thePlot = (SpiderWebPlot) plot;
    		thePlot.setLabelFont(TICK_FONT);
    	}
    	else if (plot instanceof PiePlot) {
    		((PiePlot) plot).setLabelFont(TICK_FONT);
    	}
    	else {
    		Logger.info("Configuration of plottype '" + plot.getPlotType() + "' is not yet supported", ChartUtil.class);
    	}
    }

	/**
	 * Checks and normalizes the given range. In Particular returns a range with a length != 0,
	 * because this could result in server freeze in cause of a division by zero in JFreeChart
	 * components.
	 * 
	 * @param range
	 *        the range to check
	 * @return the normalized range with not equal bounds
	 */
	public static Range normalizeRange(Range range) {
		if (range == null || (range.getLowerBound() == 0.0 && range.getUpperBound() == 0.0)) {
			return new Range(0.0, 0.1);
		}
		double bound = range.getLowerBound();
		if (bound == range.getUpperBound()) {
			if (bound > 0) {
				/* if there is only one bound, subtract 10 % from the lower bound and add 10 % to
				 * the upper bound */
				return new Range(bound * 0.9, bound * 1.1);
			} else {
				/* if there is only one bound, add 10 % to the lower bound and subtract 10 % from
				 * the upper bound */
				return new Range(bound * 1.1, bound * 0.9);
			}

		}
		return range;
	}

	/**
	 * @see #normalizeRange(Range)
	 */
	public static Range normalizeRange(double lowerBound, double upperBound) {
		return normalizeRange(new Range(lowerBound, upperBound));
	}

	/**
	 * Encodes the given {@link BufferedImage} into an {@link ImageData} for delivery.
	 * 
	 * @param useTempFile
	 *        Whether the image data should be dumped to disk instead of being cached in-memory.
	 * @param image
	 *        The pixel data to encode.
	 * @return The {@link ImageData} to deliver to the client.
	 */
	public static ImageData encode(boolean useTempFile, BufferedImage image) throws IOException {
		return encode(useTempFile, image, 0, 0);
	}

	/**
	 * Encodes the given {@link BufferedImage} into an {@link ImageData} for delivery.
	 * 
	 * @param useTempFile
	 *        Whether the image data should be dumped to disk instead of being cached in-memory.
	 * @param image
	 *        The pixel data to encode.
	 * @param headerHeight
	 *        See {@link ImageData#getHeaderHeight()}.
	 * @param descriptionWidth
	 *        See {@link ImageData#getDescriptionWidth()}.
	 * @return The {@link ImageData} to deliver to the client.
	 */
	public static ImageData encode(boolean useTempFile, BufferedImage image, int headerHeight, int descriptionWidth)
			throws IOException {
		BinaryData bytes;
		if (!useTempFile) {
			bytes = getImageAsPng(image);
		} else {
			bytes = BinaryDataFactory.createBinaryData(writeImageAsPng(image));
		}
		return new DefaultImageData(new Dimension(image.getWidth(), image.getHeight()), bytes, headerHeight,
			descriptionWidth);
	}

	/**
	 * Encodes the given {@link JFreeChart} into an {@link ImageData} for delivery.
	 * 
	 * @param useTempFile
	 *        Whether the image data should be dumped to disk instead of being cached in-memory.
	 * @param dimension
	 *        The total dimension in pixels of the resulting image.
	 * @param chart
	 *        The chart data to render and encode.
	 * @return The {@link ImageData} to deliver to the client.
	 */
	public static ImageData encode(boolean useTempFile, Dimension dimension, JFreeChart chart) throws IOException {
		return encode(useTempFile, dimension, chart, null);
	}

	/**
	 * Encodes the given {@link JFreeChart} into an {@link ImageData} for delivery.
	 * 
	 * @param useTempFile
	 *        Whether the image data should be dumped to disk instead of being cached in-memory.
	 * @param dimension
	 *        The total dimension in pixels of the resulting image.
	 * @param chart
	 *        The chart data to render and encode.
	 * @param renderingInfo
	 *        Storage of {@link ChartRenderingInfo} to be filled by the rendering process.
	 * @return The {@link ImageData} to deliver to the client.
	 */
	public static ImageData encode(boolean useTempFile, Dimension dimension, JFreeChart chart,
			ChartRenderingInfo renderingInfo) throws IOException {
		return encode(useTempFile, dimension, chart, renderingInfo, 0, 0);
	}

	/**
	 * Encodes the given {@link JFreeChart} into an {@link ImageData} for delivery.
	 * 
	 * @param useTempFile
	 *        Whether the image data should be dumped to disk instead of being cached in-memory.
	 * @param dimension
	 *        The total dimension in pixels of the resulting image.
	 * @param chart
	 *        The chart data to render and encode.
	 * @param renderingInfo
	 *        Storage of {@link ChartRenderingInfo} to be filled by the rendering process.
	 * @param headerHeight
	 *        See {@link ImageData#getHeaderHeight()}.
	 * @param descriptionWidth
	 *        See {@link ImageData#getDescriptionWidth()}.
	 * @return The {@link ImageData} to deliver to the client.
	 */
	public static ImageData encode(boolean useTempFile, Dimension dimension, JFreeChart chart,
			ChartRenderingInfo renderingInfo, int headerHeight, int descriptionWidth) throws IOException {
		BinaryData bytes;
		if (!useTempFile) {
			bytes = getChartAsPng(chart, dimension, renderingInfo);
		} else {
			bytes = BinaryDataFactory.createBinaryData(writePathAsPngFile(chart, dimension, renderingInfo));
		}
		return new DefaultImageData(dimension, bytes, headerHeight, descriptionWidth);
	}

}
