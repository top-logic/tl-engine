/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.PowerpointExportHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Configurable default-implementation of {@link ExportManager}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public abstract class AbstractExportManager<C extends AbstractExportManager.Config> implements ExportManager,
		ConfiguredInstance<C> {

	/** Suffix for {@link LayoutComponent#getResPrefix()} to create the title of the export. */
	private static final String EXPORT_TITLE = "export.title";

	/**
	 * Config-interface for {@link AbstractExportManager}.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractExportManager<?>> {

		/**
		 * The template-file for the export.
		 * 
		 * @see ExportManager#getTemplatePath()
		 */
		public String getTemplatePath();

		/**
		 * See {@link #getTemplatePath()}
		 */
		public void setTemplatePath(String path);

		/**
		 * The download-name of the exported file.
		 * 
		 * @see ExportManager#getDownloadLabel(LayoutComponent)
		 */
		public ResKey getDownloadKey();

		/**
		 * See {@link #getDownloadKey()}
		 */
		public void setDownloadKey(ResKey labelKey);

		/**
		 * The ID of the export-handler, or <code>null</code> to disable export.
		 * 
		 * @see ExportManager#getExportHandler()
		 */
		@StringDefault(PowerpointExportHandler.COMMAND)
		@Nullable
		public String getExportHandler();

		/**
		 * See {@link #getExportHandler()}
		 */
		public void setExportHandler(String handler);

	}

	private final C _config;

	/**
	 * Config-Constructor for {@link AbstractExportManager}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public AbstractExportManager(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public String getTemplatePath() {
		return StringServices.nonNull(_config.getTemplatePath());
	}

	@Override
	public String getDownloadLabel(LayoutComponent component) {
		ResKey downloadKey = _config.getDownloadKey();
		if (downloadKey == null) {
			downloadKey = ResKey.fallback(exportTitleKey(component), I18NConstants.DOWNLOAD_FILE_NAME);
		}
		return Resources.getInstance().getString(downloadKey);
	}

	private ResKey exportTitleKey(LayoutComponent component) {
		return component.getResPrefix().key(EXPORT_TITLE);
	}

	@Override
	public String getExportHandler() {
		return _config.getExportHandler();
	}

	/**
	 * Gets the file extension from the given filename.
	 */
	protected String getExtension(String filename) {
		if (filename == null)
			return null;
		int index = filename.lastIndexOf('.');
		return index > 0 ? filename.substring(index) : StringServices.EMPTY_STRING;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public OfficeExportValueHolder getExportValues(DefaultProgressInfo progressInfo, Map arguments, LayoutComponent caller) {
		HashMap<String, Object> valueMap = new HashMap<>();
		exportDefaultValues(caller, valueMap);
		exportAdditionalValues(caller, valueMap, arguments);

		String download = getDownloadLabel(caller);
		String template = getTemplatePath();
		String ext = getExtension(template);

		return new OfficeExportValueHolder(template, download + ext, valueMap, false);
	}

	/**
	 * Initializes the value-map with default-values like current user, date of export and title.
	 */
	protected void exportDefaultValues(LayoutComponent caller, Map<String, Object> valueMap) {
		Resources resources = Resources.getInstance();
		valueMap.put("VALUE_LABEL_TITLE", resources.getString(I18NConstants.TITLE_LABEL));
		valueMap.put("VALUE_LABEL_DATE_OF_EXPORT",
			resources.getString(I18NConstants.DATE_LABEL));
		valueMap.put("VALUE_LABEL_CURRENT_USER",
			resources.getString(I18NConstants.USER_LABEL));

		valueMap.put("VALUE_TITLE", resources.getString(exportTitleKey(caller), null));
		valueMap.put("VALUE_DATE_OF_EXPORT", HTMLFormatter.getInstance().getDateFormat().format(new Date()));
		valueMap.put("VALUE_CURRENT_USER", MetaLabelProvider.INSTANCE.getLabel(TLContext.getContext().getCurrentPersonWrapper()));
	}
	
	/**
	 * Hook for subclasses to add additional values to the exported data.
	 * 
	 * @param caller
	 *        the caller-component
	 * @param valueMap
	 *        the value-map to add additional values
	 * @param arguments
	 *        additional params
	 */
	@SuppressWarnings("rawtypes")
	protected abstract void exportAdditionalValues(LayoutComponent caller, Map<String, Object> valueMap, Map arguments);

	@Override
	public void registerExportCommand(CommandRegistry registry) {
		String exportHandlerId = getExportHandler();
		if (!StringServices.isEmpty(exportHandlerId)) {
			registry.registerButton(exportHandlerId);
		}
	}

}
