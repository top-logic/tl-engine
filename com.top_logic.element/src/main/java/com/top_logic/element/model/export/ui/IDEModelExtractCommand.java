/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.export.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.generate.MessagesGenerator;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.admin.component.TLServiceUtils;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tools.resources.ResourceFile;
import com.top_logic.util.Resources;

/**
 * {@link CommandHandler} storing a selected module in the model editor back to the development
 * environment in which the app is running.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDEModelExtractCommand extends AbstractCommandHandler {

	private static final String IN_APP_CONFIG_TEMPLATE = "inAppConfig.template.config.xml";

	private static final String MODEL_RESOURCE_PREFIX = ModuleLayoutConstants.WEB_INF_RESOURCE_PREFIX + "/model";

	private static final ExecutabilityRule ONLY_MODULE = new ExecutabilityRule() {
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			return (model instanceof TLModelPart) && ((TLModelPart) model).getModelKind() == ModelKind.MODULE
				? ExecutableState.EXECUTABLE
				: ExecutableState.NOT_EXEC_HIDDEN;
		}
	};

	/**
	 * Creates a {@link IDEModelExtractCommand}.
	 */
	public IDEModelExtractCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		try {
			TLModule module = (TLModule) model;
			BinaryData data = ModelExportCommand.toXML(module);

			String modelResource = MODEL_RESOURCE_PREFIX + '/' + module.getName() + ".model.xml";
			File targetFile = FileManager.getInstance().getIDEFile(modelResource);
			File targetApp = Workspace.topLevelWebapp();

			boolean isNew = !targetFile.exists();
			if (isNew) {
				targetFile = new File(targetApp, "." + modelResource);
				targetFile.getParentFile().mkdirs();
			}
			
			try (InputStream in = data.getStream()) {
				try (FileOutputStream out = new FileOutputStream(targetFile)) {
					StreamUtilities.copyStreamContents(in, out);
				}
			}

			if (isNew) {
				createApplicationConfigFragment(targetApp, modelResource, module);
			}

			if (isNew) {
				File resourceDir = new File(targetApp, ModuleLayoutConstants.RESOURCES_PATH);
				resourceDir.mkdirs();
			}
			ResourcesModule resourcesModule = ResourcesModule.getInstance();
			try (ResourceTransaction tx = resourcesModule.startResourceTransaction()) {
				for (Locale locale : resourcesModule.getSupportedLocales()) {
					String resourceName = TLModelNamingConvention.resourcesFileName(module, locale.getLanguage());
					File messagesFile = FileManager.getInstance()
						.getIDEFile(ModuleLayoutConstants.RESOURCES_RESOURCE_PREFIX + "/" + resourceName);

					File resourceDir = messagesFile.getParentFile();
					resourceDir.mkdirs();

					I18NBundle bundle = resourcesModule.getBundle(locale);
					MessagesGenerator generator = new MessagesGenerator(resourceDir, locale.getLanguage(), module) {
						@Override
						protected boolean hasValue(String key) {
							return getValue(key) != null;
						}

						@Override
						protected String getValue(String key) {
							ResKey base = ResKey.internalCreate(key);

							// Also extract optional description values.
							keepOptionalKey(base.tooltip());

							return lookup(base);
						}

						private void keepOptionalKey(ResKey infoKey) {
							String infoValue = lookup(infoKey);
							if (infoValue != null) {
								getOldValues().put(infoKey.getKey(), infoValue);
							}
						}

						private String lookup(ResKey infoKey) {
							String result = bundle.getString(infoKey, null);

							// Remove key from dynamic resources.
							tx.saveI18N(locale, infoKey, null);

							return result;
						}
					};

					generator.generate(messagesFile);

					// Normalize resources.
					new ResourceFile(messagesFile).save();
				}

				tx.commit();
			}

			patchModelBaseLine(module);
	
			if (isNew) {
				Resources resources = Resources.getInstance();
				String title = resources.getString(I18NConstants.NEW_MODULE_CONFIGURATION.tooltip());
				String message = resources.getString(I18NConstants.NEW_MODULE_CONFIGURATION);
				return MessageBox.confirm(aContext.getWindowScope(), MessageType.INFO, title, message,
					MessageBox.button(ButtonType.OK, this::restart));
			} else {
				return HandlerResult.DEFAULT_RESULT;
			}
		} catch (XMLStreamException | SQLException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Updates the stored model baseline to include the exported module in the state it was
	 * exported.
	 * 
	 * <p>
	 * The is required to allow updating the module with potential changed made in the IDE during
	 * next reboot. </p
	 */
	private void patchModelBaseLine(TLModule module) throws SQLException, XMLStreamException {
		String moduleName = module.getName();

		ConnectionPool pool = KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase());
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			String oldModelXml = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
				DynamicModelService.APPLICATION_MODEL_PROPERTY);
			if (StringServices.isEmpty(oldModelXml)) {
				return;
			}

			Document modelBaseLine = DOMUtil.parse(oldModelXml);

			// Remove old version.
			for (Element moduleElement : DOMUtil.elementsNS(modelBaseLine.getDocumentElement(), null,
				ModelConfig.MODULE)) {
				if (moduleName.equals(moduleElement.getAttribute(ModuleConfig.NAME))) {
					moduleElement.getParentNode().removeChild(moduleElement);
					break;
				}
			}

			// Add module configuration to baseline.
			{
				// Note: The config must be exported again without namespace, because the baseline
				// does not use namespaces for historical reasons.
				StringWriter buffer = new StringWriter();
				ModelExportCommand.serializeModuleOrType(buffer, module, null);

				// Add module to baseline.
				Document moduleDoc = DOMUtil.parse(buffer.toString());

				for (Element moduleElement : DOMUtil.elementsNS(moduleDoc.getDocumentElement(), null,
					ModelConfig.MODULE)) {
					modelBaseLine.getDocumentElement().appendChild(modelBaseLine.importNode(moduleElement, true));
				}
			}

			String newModelXml = DOMUtil.toString(modelBaseLine, true, true);

			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
				DynamicModelService.APPLICATION_MODEL_PROPERTY, newModelXml);

			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	private HandlerResult restart(DisplayContext displaycontext) {
		// Make sure, the new configuration fragment is read and the new resource bundle is
		// found.
		TLServiceUtils.reloadConfigurations();

		try {
			ModuleUtil.INSTANCE.restart(ResourcesModule.Module.INSTANCE);
		} catch (RestartException ex) {
			throw new RuntimeException(ex);
		}

		displaycontext.getWindowScope().getTopLevelFrameScope().addClientAction(MainLayout.createFullReload());

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Installs an <code>autoconf</code> fragment referencing the exported model.
	 *
	 * @param targetApp
	 *        Web application root of the target application to export to.
	 * @param modelResource
	 *        Application-local path to the model file to create.
	 * @param module
	 *        {@link TLModule} to export.
	 */
	private void createApplicationConfigFragment(File targetApp, String modelResource, TLModule module)
			throws IOException {

		String template;
		try (InputStream configTemplate = getClass().getResourceAsStream(IN_APP_CONFIG_TEMPLATE)) {
			if (configTemplate == null) {
				throw new IOException("Configuration template file '" + IN_APP_CONFIG_TEMPLATE + "' not found.");
			}
			template = StreamUtilities.readAllFromStream(configTemplate, StringServices.UTF8);
		}
		
		String resourceRef = TLModelNamingConvention.getBundleName(module);
		String withBundle = template.replace("${bundleName}", resourceRef);
		String result = withBundle.replace("${modelPath}", modelResource);
		
		
		File autoConf = new File(targetApp, ModuleLayoutConstants.AUTOCONF_PATH);
		autoConf.mkdirs();
		File fragmentFile = new File(autoConf, module.getName() + ".config.xml");
		
		try (Writer out = new FileWriter(fragmentFile)) {
			out.write(result);
		}
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), ONLY_MODULE);
	}

}
