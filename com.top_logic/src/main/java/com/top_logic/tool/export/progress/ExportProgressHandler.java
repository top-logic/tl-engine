/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.progress;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.thread.InContextThread;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.AbstractProgressComponent.AbstractProgressCommandHandler;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;

/**
 * Download handler which prepares the download in a separate thread.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExportProgressHandler extends AbstractProgressCommandHandler {

	/**
	 * Configuration for {@link ExportProgressHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractProgressCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	private AbstractDownloadHandler _exportHandler;

	private static class ExportJob implements Runnable {
		private final LayoutComponent _component;

		private final AbstractDownloadHandler _exportHandler;

		private final Map<String, Object> _exportArgs;

		private final DefaultProgressInfo _progressInfo;

		ExportJob(LayoutComponent component, AbstractDownloadHandler exportHandler,
				Map<String, Object> exportArgs, DefaultProgressInfo progressInfo) {
			_component = component;
			_exportHandler = exportHandler;
			_exportArgs = exportArgs;
			_progressInfo = progressInfo;
		}

		@Override
		public void run() {
		    try {
				boolean cacheInstalled = BoundComponent.installRequestCacheIfNotExisting();
				try {
					BinaryDataSource download = _exportHandler.getDownloadData(_component, _progressInfo, _exportArgs);
					_progressInfo.setResult(download);
				} finally {
					if (cacheInstalled) {
						BoundComponent.uninstallRequestCache();
					}
				}
		    }
		    catch (Throwable ex) {
		        _progressInfo.setResult(ex);
		        Logger.error("Failed to execute export.", ex, ExportProgressHandler.class);
		    }
		    finally {
		        _progressInfo.setFinished(true); // Set late so Exception will be shown
		    }
		}
	}

	public ExportProgressHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	public static ExportProgressHandler newInstance(AbstractDownloadHandler exportHandler) {
		ExportProgressHandler result = newInstance(ExportProgressHandler.class, null);
		result._exportHandler = exportHandler;
		return result;
    }

    @Override
	protected ProgressInfo startThread(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map someArguments) {
		final DefaultProgressInfo progressInfo = new DefaultProgressInfo();
		final TLContext userContext = TLContext.getContext();

		Runnable exportJob = new ExportJob(aComponent, _exportHandler, someArguments, progressInfo);

		boolean showFile = _exportHandler.showFile();
		long joinTime = _exportHandler.getMaxDirectDownloadTime();
		boolean infiniteWait = joinTime == 0L;
		if (showFile || infiniteWait) {
			exportJob.run();
		} else {
			String threadName = "export-" + aComponent.getName() + "-" + userContext.getCurrentUserName();
			Thread thread = new InContextThread(userContext, exportJob, threadName);
			thread.start();

			boolean alwaysShowProgress = joinTime < 0;
			if (alwaysShowProgress) {
				// Unconditionally show the progress dialog.
				return progressInfo;
			} else {
				try {
					thread.join(joinTime);
				} catch (InterruptedException ex) {
					// Ignore.
				}

				if (thread.isAlive()) {
					// Show the progress dialog only if the export is long-running.
					return progressInfo;
				}
			}
		}

		// Directly push the contents to the client.
		BinaryDataSource dataItem = getExportData(progressInfo);
		if (dataItem != null) {
			aContext.getWindowScope().deliverContent(dataItem, showFile);
		}
		return null;
    }

    @Override
    protected ComponentName getProgressComponent() {
		return ComponentName.newName("export/exportDialog.layout.xml", "ProgressExportView");
    }

	/**
	 * Retrieve the export data from the export thread.
	 * 
	 * @return May be <code>null</code> in case the given {@link ProgressInfo} was requested to
	 *         {@link ProgressInfo#signalStop() stop}.
	 */
	public static BinaryDataSource getExportData(DefaultProgressInfo progressInfo) throws Error {
		Object progressResult = progressInfo.getResult();
		if (progressResult instanceof RuntimeException) {
			throw (RuntimeException) progressResult;
		}
		else if (progressResult instanceof Error) {
			throw (Error) progressResult;
		}
		else if (progressResult instanceof Throwable) {
			throw new RuntimeException((Throwable) progressResult);
		}
	
		BinaryDataSource valueHolder = (BinaryDataSource) progressResult;
		return valueHolder;
	}

}