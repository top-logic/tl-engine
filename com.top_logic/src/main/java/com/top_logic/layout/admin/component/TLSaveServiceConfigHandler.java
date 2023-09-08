/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * Stores custom service configurations in a separate configuration file in the autoconf folder of
 * the toplevel module.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLSaveServiceConfigHandler extends AbstractApplyCommandHandler {

	private static final String APPLICATION_CONFIG_ROOT_TAG_NAME = "application";

	/**
	 * Creates a {@link TLSaveServiceConfigHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLSaveServiceConfigHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean storeChanges(LayoutComponent component, FormContext context, Object model) {
		BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) model;

		if (model instanceof BasicRuntimeModule) {
			ServiceConfiguration<?> serviceConfiguration = getServiceConfiguration(component);
			checkConstraints(serviceConfiguration);
			boolean serviceConfig = storeNewServiceConfiguration(serviceConfiguration, module);

			TLServiceUtils.reloadConfigurations();
			repaintEditor((EditComponent) component);

			InfoService.showInfo(I18NConstants.CONFIGURATION_SAVED_INFO_MESSAGE);

			return serviceConfig;
		}

		return false;
	}

	private void checkConstraints(ServiceConfiguration<?> serviceConfiguration) {
		BufferingI18NLog checkingProtocol = new BufferingI18NLog();
		new ConstraintChecker().check(checkingProtocol, serviceConfiguration);
		ResKey[] errorKeys =
			checkingProtocol.getEntries()
				.stream()
				.filter(event -> event.getLevel() == Level.ERROR)
				.map(BufferingI18NLog.Entry::getMessage)
				.toArray(ResKey[]::new);
		if (errorKeys.length > 0) {
			throw new TopLogicException(I18NConstants.SERVICE_CONFIGURATION_INVALID__ERRORS.fill(errorKeys));
		}
	}

	private void repaintEditor(EditComponent editor) {
		editor.removeFormContext();
		editor.invalidate();
	}

	private ServiceConfiguration<?> getServiceConfiguration(LayoutComponent component) {
		TLServiceConfigEditorFormBuilder.EditModel editModel = getEditModel(component);

		return editModel.getServiceConfiguration();
	}

	private TLServiceConfigEditorFormBuilder.EditModel getEditModel(LayoutComponent component) {
		FormContext formContext = getFormContext(component);

		return (TLServiceConfigEditorFormBuilder.EditModel) EditorFactory.getModel(formContext);
	}

	private FormContext getFormContext(LayoutComponent component) {
		EditComponent editor = (EditComponent) component;

		return editor.getFormContext();
	}

	private boolean storeNewServiceConfiguration(ServiceConfiguration<?> instance,
			BasicRuntimeModule<?> module) {

		try {
			File inAppServiceConfigFile = createInAppServiceConfigFile(module);
			ApplicationConfig.Config applicationConfig = getApplicationConfig(inAppServiceConfigFile);

			addServiceConfig(applicationConfig, module.getImplementation(), instance);

			writeCustomServiceConfigTo(inAppServiceConfigFile, applicationConfig);
			Logger.info(inAppServiceConfigFile.getAbsolutePath() + " successfully written.",
				TLSaveServiceConfigHandler.class);
		} catch (Exception exception) {
			throw new TopLogicException(I18NConstants.SERVICE_CONFIG_SAVE_ERROR, exception);
		}

		return true;
	}

	private void writeCustomServiceConfigTo(File file, ApplicationConfig.Config config) throws IOException {
		String configuration;
		try (StringWriter stringWriter = new StringWriter()) {
			configuration = createConfigText(config, stringWriter);
		} catch (XMLStreamException exception) {
			Logger.error("Service configuration could not be written into the filesystem.", exception,
				TLSaveServiceConfigHandler.class);
			throw new RuntimeException(exception);
		}
		FileUtilities.writeStringToFile(configuration, file, StringServices.UTF8);
	}

	private String createConfigText(ApplicationConfig.Config config, StringWriter innerWriter) throws XMLStreamException {
		OverrideConfigurationWriter configWriter = new OverrideConfigurationWriter(innerWriter, getOverrideConfigs());

		TypedConfiguration.serialize(APPLICATION_CONFIG_ROOT_TAG_NAME, config, configWriter);

		return TypedConfiguration.prettyPrint(innerWriter.toString(), getPrinterConfig());
	}

	private Set<Class<?>> getOverrideConfigs() {
		return Collections.singleton(TypedRuntimeModule.ModuleConfiguration.class);
	}

	private com.top_logic.basic.xml.XMLPrettyPrinter.Config getPrinterConfig() {
		return ThemeUtil.THEME_PRINTER_CONFIG;
	}

	private File createInAppServiceConfigFile(BasicRuntimeModule<?> moduleImplementation) throws IOException {
		File inAppServiceConfigFile = inAppServiceConfiguration(moduleImplementation);
		FileUtilities.enforceDirectory(inAppServiceConfigFile.getParentFile());
		return inAppServiceConfigFile;
	}

	static File inAppServiceConfiguration(BasicRuntimeModule<?> model) {
		return new File(XMLProperties.Setting.resolveAutoconfFolder(), serviceFileName(model));
	}

	private static String serviceFileName(BasicRuntimeModule<?> model) {
		return model.getImplementation().getName() + ".config.xml";
	}

	private void addServiceConfig(ApplicationConfig.Config appConfig, Class<? extends ManagedClass> serviceClass,
			ServiceConfiguration<?> instance) {
		appConfig.getServices().put(serviceClass, createModuleConfiguration(serviceClass, instance));
	}

	private ModuleConfiguration createModuleConfiguration(Class<? extends ManagedClass> serviceClass,
			ServiceConfiguration<?> instance) {
		ModuleConfiguration moduleConfiguration = TypedConfiguration.newConfigItem(ModuleConfiguration.class);

		moduleConfiguration.setServiceClass(serviceClass);
		moduleConfiguration.setInstance(instance);

		return moduleConfiguration;
	}

	private ApplicationConfig.Config getApplicationConfig(File file) throws ConfigurationException {
		if (!file.exists()) {
			return TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		} else {
			return readCustomServiceConfig(file);
		}
	}

	private ApplicationConfig.Config readCustomServiceConfig(File file) throws ConfigurationException {
		ConfigurationReader reader = createAppConfigReader();

		reader.setSource(getConfigContent(file));

		return (ApplicationConfig.Config) reader.read();
	}

	private ConfigurationReader createAppConfigReader() {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

		return new ConfigurationReader(context, getDescriptorByGlobalName());
	}

	private Map<String, ConfigurationDescriptor> getDescriptorByGlobalName() {
		Class<ApplicationConfig.Config> appConfigClass = ApplicationConfig.Config.class;
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(appConfigClass);
		
		return Collections.singletonMap(APPLICATION_CONFIG_ROOT_TAG_NAME, descriptor);
	}

	private Content getConfigContent(File inAppServiceConfigFile) {
		return BinaryDataFactory.createBinaryData(inAppServiceConfigFile);
	}

}
