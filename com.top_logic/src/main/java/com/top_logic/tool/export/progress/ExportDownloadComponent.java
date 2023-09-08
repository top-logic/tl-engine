/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.progress;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.PageComponent;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Download component to be used, when {@link ExportProgressComponent export} has finished his work.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExportDownloadComponent extends PageComponent {

	/**
	 * Configuration for the {@link ExportDownloadComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PageComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			PageComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(Download.COMMAND_ID);
		}

	}

    public ExportDownloadComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

	/**
	 * Wrapped {@link BinaryDataSource} to prevent equals check in component model handling.
	 */
	public static class DownloadData {
		private BinaryDataSource _data;

		/**
		 * Creates a {@link ExportDownloadComponent.Download}.
		 */
		public DownloadData(BinaryDataSource data) {
			_data = data;
		}

		/**
		 * The underlaying data.
		 */
		public BinaryDataSource getData() {
			return _data;
		}
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof DownloadData;
    }
    
	/**
	 * {@link CommandHandler} finally offering the download.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Download extends AbstractSystemCommand {

		public static final String COMMAND_ID = "downloadExportData";

		public Download(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			aContext.getWindowScope().deliverContent(((DownloadData) model).getData());
			
			HandlerResult result = new HandlerResult();
			result.setCloseDialog(true);
			return result;
		}
    	
    }

}

