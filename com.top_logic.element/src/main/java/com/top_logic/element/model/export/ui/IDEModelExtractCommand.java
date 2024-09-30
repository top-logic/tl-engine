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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
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
import com.top_logic.element.model.DynamicModelService.Config.DeclarationConfig;
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
import com.top_logic.util.model.ModelService;

/**
 * {@link CommandHandler} storing a selected module in the model editor back to the development
 * environment in which the app is running.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDEModelExtractCommand extends AbstractCommandHandler {

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
							String result = bundle.getStringOptional(infoKey);

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
		// Create configuration fragment.
		ApplicationConfig.Config fragment = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);

		ResourcesModule.Config resourcesConfig = TypedConfiguration.newConfigItem(ResourcesModule.Config.class);
		try {
			ServiceConfiguration<ResourcesModule> currentConfig =
				ApplicationConfig.getInstance().getServiceConfiguration(ResourcesModule.class);
			if (currentConfig != null) {
				resourcesConfig = (ResourcesModule.Config) TypedConfiguration
					.createConfigItemForImplementationClass(currentConfig.getImplementationClass());
			}
		} catch (ConfigurationException ex) {
			// Ignore.
		}
		String resourceRef = TLModelNamingConvention.getBundleName(module);
		resourcesConfig.setBundles(Collections.singletonList(resourceRef));
		ModuleConfiguration resourcesModuleConfig = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		resourcesModuleConfig.setServiceClass(ResourcesModule.class);
		resourcesModuleConfig.setInstance(resourcesConfig);
		fragment.getServices().put(resourcesModuleConfig.getServiceClass(), resourcesModuleConfig);
		
		@SuppressWarnings("unchecked")
		DynamicModelService.Config<DynamicModelService> serviceConfig =
			TypedConfiguration.newConfigItem(DynamicModelService.Config.class);
		try {
			ServiceConfiguration<ModelService> currentConfig =
				ApplicationConfig.getInstance().getServiceConfiguration(ModelService.class);
			if (currentConfig != null) {
				@SuppressWarnings("unchecked")
				DynamicModelService.Config<DynamicModelService> copy = (DynamicModelService.Config<DynamicModelService>) TypedConfiguration
					.createConfigItemForImplementationClass(currentConfig.getImplementationClass());
				serviceConfig = copy;
			}
		} catch (ConfigurationException ex) {
			// Ignore.
		}
		DeclarationConfig modelRef = TypedConfiguration.newConfigItem(DeclarationConfig.class);
		modelRef.setFile(modelResource);
		serviceConfig.getDeclarations().add(modelRef);
		ModuleConfiguration serviceRef = TypedConfiguration.newConfigItem(ModuleConfiguration.class);
		serviceRef.setServiceClass(ModelService.class);
		serviceRef.setInstance(serviceConfig);
		fragment.getServices().put(serviceRef.getServiceClass(), serviceRef);
		
		File autoConf = new File(targetApp, ModuleLayoutConstants.AUTOCONF_PATH);
		autoConf.mkdirs();
		File fragmentFile = new File(autoConf, module.getName() + ".config.xml");
		
		try (Writer out = new FileWriter(fragmentFile)) {
			out.write(
				TypedConfiguration.toString(ApplicationConfig.ROOT_TAG, fragment));
		}
	}

	/**
	 * Locates the base directory relative to which the given file can be resolved using the given
	 * relative path.
	 */
	private static File baseDir(File file, String path) {
		File result = file;
		List<String> pathList = Arrays.asList(path.split("/" + "|" + Pattern.quote(File.separator)));
		Collections.reverse(pathList);
		for (String element : pathList) {
			if (element.isEmpty()) {
				continue;
			}
			assert element.equals(result.getName());
			result = result.getParentFile();
		}
		return result;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), ONLY_MODULE);
	}

}
