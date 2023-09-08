/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.BinaryDataSourceProxy;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExtendedNullModelDisabledRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.tool.export.progress.ExportProgressHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Command preparing a custom export for download providing user feedback.
 * 
 * <p>
 * Note: A pure download without any additional user feedback can be triggered by
 * {@link WindowScope#deliverContent(BinaryDataSource, boolean)}.
 * </p>
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class AbstractDownloadHandler extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link ExportProgressHandler}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static interface GlobalConfig extends ConfigurationItem {

		/**
		 * The time in milliseconds to wait for a forked export to deliver the result before opening
		 * a progress dialog.
		 * 
		 * <p>
		 * A value of <code>0</code> means not to show export progress at all, but perform the
		 * export completely within the rendering thread.
		 * </p>
		 * 
		 * <p>
		 * A value less than <code>0</code> means to always show export progress, even if the export
		 * completes immediately.
		 * </p>
		 */
		long getMaxDirectDownloadTime();
	}

	/**
	 * Configuration of the {@link AbstractDownloadHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		CommandGroupReference getGroup();

	}

	private static final ExecutabilityRule EXEC_RULE = CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, ExtendedNullModelDisabledRule.EXTENDED_NULL_RULE);
    
	public AbstractDownloadHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
	
    /**
     * Prepare the download by fetching all the necessary data.
     * dkh: return Object is "something" ... a HandlerResult, a Wrapper, null ...
     *      take care when using it!
     * 
     * @param aComponent the model fetched from the underlying component.
     * 
	 * @param progressInfo
	 *        Callback to report progress of download preparation to.
     * @return the return Object will be given back to you in all following methods,
     *         may be null.
     */
	protected abstract Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo,
			Map<String, Object> arguments) throws Exception;

    /**
	 * This method prepares all information for a client that would be send to a client.
	 * 
	 * @param aComponent
	 *        the model fetched from the underlying component.
	 * @return a {@link BinaryDataSource} that holds all information that are neccessary for a
	 *         download.
	 */
	public final BinaryDataSource getDownloadData(LayoutComponent aComponent, DefaultProgressInfo progressInfo,
			Map<String, Object> arguments) throws Exception {
		progressInfo.setMessageKey(I18NConstants.DEFAULT_DOWNLOAD_PROGRESS_MESSAGE);
		final Object download = prepareDownload(aComponent, progressInfo, arguments);
		if (progressInfo.shouldStop()) {
			return null;
		}
		String fileName = this.getDownloadName(aComponent, download);
		String contentType = this.getContentType(fileName);
		BinaryDataSource contents = getDownloadData(download);
		final Object componentModel = aComponent.getModel();
		
		BinaryDataSource dataItem = new BinaryDataSourceProxy(contents) {
			@Override
			public String getName() {
				return fileName;
			}

			@Override
			public String getContentType() {
				return contentType;
			}

			@Override
			protected void finalize() throws Throwable {
				super.finalize();
				cleanupDownload(componentModel, download);
			}
		};

		return dataItem;
	}

    
	/**
	 * Specifies the name of the file that is offered for download.
	 * 
	 * @param aComponent
	 *        The underlying component.
	 * @param download
	 *        The download data created by
	 *        {@link #prepareDownload(LayoutComponent, DefaultProgressInfo, Map)}.
	 */
    public abstract String getDownloadName(LayoutComponent aComponent, Object download);

    /**
	 * Returns the data that finally is offered for download.
	 * 
	 * @param download
	 *        The download data created by
	 *        {@link #prepareDownload(LayoutComponent, DefaultProgressInfo, Map)}.
	 */
    public abstract BinaryDataSource getDownloadData(Object download) throws Exception;

	/**
	 * You may wish to relase / delete resources in this function
	 * 
	 * @param model
	 *        The model of the underlying component.
	 * @param download
	 *        The download data created by
	 *        {@link #prepareDownload(LayoutComponent, DefaultProgressInfo, Map)}.
	 */
    public abstract void cleanupDownload(Object model, Object download);

	/**
	 * Handle the download based on the abstract method given above.
	 */
	@Override
	public HandlerResult handleCommand(DisplayContext aContext, final LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		try {
			ExportProgressHandler progressHandler = ExportProgressHandler.newInstance(this);
			progressHandler.handleCommand(aContext, aComponent, model, someArguments);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw handleError(ex);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * {@link IOException} is ignored, because it happens, if the user cancels the download.
	 * 
	 * @param ex
	 *        The occurred exception.
	 * @return {@link HandlerResult#DEFAULT_RESULT}.
	 */
	private HandlerResult createResult(IOException ex) {
		Logger.info("Download was canceled.", ex, this);
		return HandlerResult.DEFAULT_RESULT;
	}

	private TopLogicException handleError(Exception ex) {
		throw new TopLogicException(this.getClass(), "error.001", null, ex);
	}

    /**
	 * Return the contenttype delivered to the browser for downlaod.
	 * 
	 * Carefull here. IE sometimes tries to guess the contenttype int his case. There is at least
	 * one problem eith ".pdf" and AdobeReader 5.0 where this results in a failure and a partially
	 * loaded PDF-Document.
	 * 
	 * @param aFileName
	 *        name of file to be downloaded.
	 * @return the mime type based on the given file name..
	 */
    protected String getContentType(String aFileName) {
		return MimeTypes.getInstance().getMimeType(aFileName);
    }

    /**
	 * indicates if the file is shown or saved when downloaded. 
	 *  
     *  True means that the file is shown, false means save dialog is offered
	 */
	public boolean showFile() {
		return false;
	}

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
    	return EXEC_RULE;
    }

	/**
	 * @see GlobalConfig#getMaxDirectDownloadTime()
	 */
	public long getMaxDirectDownloadTime() {
		return globalConfig().getMaxDirectDownloadTime();
	}

	private static GlobalConfig globalConfig() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
	}
}
