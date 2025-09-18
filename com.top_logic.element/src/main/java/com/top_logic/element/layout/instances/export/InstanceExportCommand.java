/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.export;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} exporting the selected objects to XML for later re-import.
 * 
 * @see XMLInstanceExporter
 */
@InApp
public class InstanceExportCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link InstanceExportCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {
		/**
		 * Additional setup actions that customize the export process.
		 */
		List<PolymorphicConfiguration<? extends ExportCustomization>> getCustomizations();
	}

	private final List<ExportCustomization> _customizations;

	/**
	 * Creates a {@link InstanceExportCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InstanceExportCommand(InstantiationContext context, Config config) {
		super(context, config);

		_customizations = TypedConfiguration.getInstanceList(context, config.getCustomizations());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		Collection<?> values = CollectionUtil.asList(model);
		if (values.isEmpty()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		XMLInstanceExporter exporter = new XMLInstanceExporter();
		for (ExportCustomization customization : _customizations) {
			customization.perform(exporter);
		}
		BinaryDataSource instanceData =
			exporter.exportInstances(CollectionUtil.dynamicCastView(TLObject.class, values));
		aContext.getWindowScope().deliverContent(instanceData);

		return HandlerResult.DEFAULT_RESULT;
	}

}
