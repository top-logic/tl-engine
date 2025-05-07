/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.importer;

import java.util.Map;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.model.ModelService;

/**
 * {@link CommandHandler} importing objects from an uploaded XML file.
 * 
 * @see XMLInstanceImporter
 */
public class InstanceImportCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link InstanceImportCommand}.
	 */
	public InstanceImportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		UploadForm form = (UploadForm) EditorFactory.getModel(((FormHandler) aComponent).getFormContext());
		BinaryData data = form.getData();

		aComponent.closeDialog();

		return new ProgressDialog(I18NConstants.IMPORT_PROGRESS, DisplayDimension.px(500),
			DisplayDimension.px(300)) {
			@Override
			protected void run(I18NLog log) throws AbortExecutionException {
				TLModel applicationModel = ModelService.getApplicationModel();

				XMLInstanceImporter importer =
					new XMLInstanceImporter(applicationModel, ModelService.getInstance().getFactory());
				importer.setLog(log.asLog());

				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
				try (Transaction tx = kb.beginTransaction(I18NConstants.IMPORTED_OBJECTS)) {
					try {
						log.info(I18NConstants.PARSING_DATA);
						ObjectsConf objects = XMLInstanceImporter.loadConfig(data);

						log.info(I18NConstants.IMPORTING_OBJECTS);
						importer.importInstances(objects);

						log.info(I18NConstants.COMMITTING_CHANGES);
						tx.commit();

						log.info(I18NConstants.IMPORT_FINISHED);
					} catch (ConfigurationException ex) {
						log.error(ex.getErrorKey());
						Logger.error("Import failed.", ex);
					}
				}
			}
		}.open(aContext);
	}

}
