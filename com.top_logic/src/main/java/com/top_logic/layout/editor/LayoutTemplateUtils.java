/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.editor.commands.AdditionalComponentDefinition;
import com.top_logic.layout.editor.config.DialogTemplateParameters;
import com.top_logic.layout.editor.scripting.CreateComponentButtonAction;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.ResolveFailure;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.LayoutResourcesCollector;
import com.top_logic.mig.html.layout.LayoutScopeMapper;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutTemplateCall;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.LayoutVariables;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for layout templates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutTemplateUtils {

	/**
	 * Property set on the {@link DisplayContext} to get stable identifiers for
	 * {@link LayoutComponent}s during tests.
	 */
	public static final Property<List<String>> COMPONENT_IDENTIFIERS =
		TypedAnnotatable.propertyList("COMPONENT_IDENTIFIERS");

	/**
	 * Property set on the {@link DisplayContext} to get stable {@link UUID}s during tests.
	 */
	public static final Property<List<String>> UUIDS = TypedAnnotatable.propertyList("UUIDS");

	private static final String ROOT_TEMPLATE_ARGUMENTS_TAG_NAME = "arguments";

	private static final String COMPONENTS_TEMPLATE_PROPERTY = "components";

	/**
	 * XML pretty printer configuration to display template arguments on the gui.
	 */
	public static final XMLPrettyPrinter.Config TEMPLATE_PRINTER_CONFIG = printerConfig();

	/**
	 * Instantiate the given layout template to create a
	 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
	 * 
	 * @param templateCall
	 *        Layout template definition.
	 * 
	 * @return Instantiated layout template.
	 * 
	 * @throws ConfigurationException
	 *         When the arguments of given {@link LayoutTemplateCall} can not be parsed.
	 * 
	 * @see #getInstantiatedLayoutTemplate(String, ConfigurationItem, String)
	 */
	public static LayoutComponent.Config getInstantiatedTemplateCall(LayoutTemplateCall templateCall,
			String layoutKey) throws ConfigurationException {
		return getInstantiatedLayoutTemplate(templateCall.getTemplateName(), templateCall.getArguments(),
			layoutKey);
	}

	/**
	 * Instantiate the given layout template to create a
	 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
	 * 
	 * @param template
	 *        Relative template path.
	 * @param arguments
	 *        Typed layout template parameters.
	 * 
	 * @return Instantiated layout template.
	 */
	public static LayoutComponent.Config getInstantiatedLayoutTemplate(String template, ConfigurationItem arguments,
			String layoutKey) throws ConfigurationException {
		Config configuration = createUnqualifiedComponentConfig(template, arguments, layoutKey);

		if (configuration != null) {
			LayoutUtils.qualifyComponentNames(layoutKey, configuration);
		}

		return configuration;
	}

	/**
	 * Instantiate the layout template given by the template name with the given arguments to create
	 * a unqualified {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
	 */
	public static LayoutComponent.Config createUnqualifiedComponentConfig(String template, ConfigurationItem arguments,
			String layoutKey) throws ConfigurationException {
		Optional<DynamicComponentDefinition> definition = getTemplateComponentDefinition(template);

		if (definition.isPresent()) {
			DynamicComponentDefinition componentDefinition = definition.get();
			ConfigBuilder builder = TypedConfiguration.createConfigBuilder(componentDefinition.descriptor());
			ConfigCopier.fillDeepCopy(arguments, builder, getInstantiationContext());

			Protocol protocol = createProtocol();
			Config result = componentDefinition.createComponentConfig(protocol,
				builder.createConfig(getInstantiationContext()), layoutKey);
			protocol.checkErrors();
			return result;
		}

		return null;
	}

	private static Protocol createProtocol() {
		return new LogProtocol(LayoutTemplateUtils.class);
	}

	private static InstantiationContext getInstantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	/**
	 * Computes the {@link Element} node containing the typed layout arguments.
	 * 
	 * It is assumed that the first child of typed layout templates is the arguments object.
	 */
	public static Element getTemplateArgumentElement(Element node) {
		return getFirstChild(node);
	}

	static String getArgumentContentName(String templateName) {
		return templateName + "#" + LayoutModelConstants.TEMPLATE_CALL_ARGUMENTS_ELEMENT;
	}

	/**
	 * Shortcut to access the attribute with name "template" for the given {@link Element} node.
	 * 
	 * @return Instantiated template name.
	 */
	public static String getTemplateName(Element node) {
		String templateName = node.getAttribute(LayoutModelConstants.TEMPLATE_CALL_TEMPLATE_ATTR);

		if (StringServices.isEmpty(templateName)) {
			throw new TopLogicException(I18NConstants.NODE_HAS_NO_TEMPLATE_NAME);
		}

		return templateName;
	}

	/**
	 * Whether the given "template call" node is flagged as final, i.e. whether no overlays must be
	 * applied to the corresponding arguments.
	 * 
	 * @param templateCall
	 *        Root note of a template call.
	 * 
	 * @see LayoutModelConstants#TEMPLATE_CALL_FINAL_ATTR
	 * @see LayoutModelConstants#TEMPLATE_CALL_ELEMENT
	 */
	public static boolean isFinal(Element templateCall) throws ConfigurationException {
		String property = LayoutModelConstants.TEMPLATE_CALL_FINAL_ATTR;
		Boolean finalAnnotation = ConfigUtil.getBoolean(property, templateCall.getAttribute(property));
		if (finalAnnotation == null) {
			return false;
		}
		return finalAnnotation.booleanValue();
	}

	private static Element getFirstChild(Element node) {
		return DOMUtil.elements(node).iterator().next();
	}

	/**
	 * Root element of the parsed layout definition document.
	 */
	public static Optional<Element> getLayoutDocumentOnFilesystem(String layoutKey) {
		Protocol protocol = createProtocol();
		LayoutResolver resolver = LayoutResolver.newRuntimeResolver(protocol);
		protocol.checkErrors();

		return getLayoutOnFilesystem(layoutKey, resolver).map(layout -> layout.getLayoutDocument().getDocumentElement());
	}

	private static Optional<ConstantLayout> getLayoutOnFilesystem(String layoutKey, LayoutResolver resolver) {
		return getLayoutFileOnFilesystem(layoutKey)
			.map(filesystemLayout -> {
				try {
					return resolver.getLayout(layoutKey, filesystemLayout);
				} catch (ResolveFailure exception) {
					throw new TopLogicException(I18NConstants.LAYOUT_RESOLVE_ERROR.fill(layoutKey));
				}
			});
	}

	/**
	 * Computes the layout {@link File} specified by the given relative path to the root layout
	 * directory.
	 * 
	 * @return An empty {@link Optional} if the layout file does not exist.
	 */
	public static Optional<BinaryData> getLayoutFileOnFilesystem(String relativePath) {
		String path = LayoutConstants.LAYOUT_BASE_RESOURCE + "/" + relativePath;

		return Optional.ofNullable(FileManager.getInstance().getDataOrNull(path));
	}

	/**
	 * Lookup in all {@link DynamicComponentDefinition}s for the given template.
	 */
	public static Optional<DynamicComponentDefinition> getTemplateComponentDefinition(String template) {
		return Optional.ofNullable(DynamicComponentService.getInstance().getComponentDefinition(template));
	}

	/**
	 * Lookup in all {@link DynamicComponentDefinition}s for the given template.
	 */
	public static DynamicComponentDefinition getComponentDefinition(String template) {
		DynamicComponentDefinition definition = DynamicComponentService.getInstance().getComponentDefinition(template);

		if (definition == null) {
			throw new TopLogicException(I18NConstants.DEFINITION_NOT_FOUND__TEMPLATE.fill(template));
		}

		return definition;
	}

	/**
	 * Serializes the given arguments from {@link ConfigurationItem} to String.
	 * 
	 * @param arguments
	 *        Layout template arguments {@link ConfigurationItem}.
	 * 
	 * @return String serialized template arguments.
	 */
	public static String getSerializedTemplateArguments(ConfigurationItem arguments) {
		TypedConfiguration.minimize(arguments);
		
		try (StringWriter stringWriter = new StringWriter()) {
			try (ConfigurationWriter configWriter = new ConfigurationWriter(stringWriter)) {
				writeTemplateArguments(configWriter, arguments);
			}
			return stringWriter.toString();
		} catch (XMLStreamException | IOException exception) {
			throw new IOError(exception);
		}
	}

	/**
	 * Deserializes the given template arguments from String to {@link ConfigurationItem}.
	 * 
	 * @param layoutKey
	 *        Layout identifier.
	 * 
	 * @param templateName
	 *        Template file name.
	 * 
	 * @param arguments
	 *        Values for the template properties.
	 * 
	 * @return Deserialized template arguments.
	 * 
	 * @throws ConfigurationException
	 *         When the arguments of given {@link LayoutTemplateCall} can not be parsed.
	 */
	public static ConfigurationItem getDeserializedTemplateArguments(String layoutKey, String templateName,
			String arguments)
			throws ConfigurationException {
		Optional<DynamicComponentDefinition> componentDef = getTemplateComponentDefinition(templateName);
		if (componentDef.isPresent()) {
			return deserializeTemplateArguments(Collections.emptyList(), layoutKey, componentDef.get(), arguments);
		} else {
			return null;
		}
	}

	/**
	 * Deserializes the given template arguments from String to {@link ConfigurationItem}.
	 */
	public static ConfigurationItem deserializeTemplateArguments(List<Content> overlays, String layoutKey,
			DynamicComponentDefinition definition, String baseArguments) throws ConfigurationException {
		String prettyBaseArguments = XMLPrettyPrinter.prettyPrint(TEMPLATE_PRINTER_CONFIG, baseArguments);
		CharacterContent templateArgContent = CharacterContents.newContent(prettyBaseArguments, "layout:" + layoutKey);
		List<Content> contents = new ListBuilder<Content>().add(templateArgContent).addAll(overlays).toList();

		return deserializeTemplateArgumentsInternal(contents, layoutKey, definition, baseArguments);
	}

	static ConfigurationItem deserializeTemplateArgumentsInternal(List<Content> contents, String layoutKey,
			DynamicComponentDefinition definition, String baseArguments) throws ConfigurationException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		ConfigurationReader reader = new ConfigurationReader(context, createComponentDefinitionDescriptor(definition));
		reader.setVariableExpander(LayoutVariables.layoutExpander(ThemeFactory.getTheme(), layoutKey));
		reader.setSources(contents);

		try {
			return reader.read();
		} catch (ConfigurationException exception) {
			String template = definition.definitionFile();
			throw new ConfigurationException(
				I18NConstants.TEMPLATE_DESERIALIZATION_ERROR__ARGUMENTS_TEMPLATE_KEY.fill(baseArguments, template, layoutKey),
				exception.getPropertyName(),
				exception.getPropertyValue(), exception);
		}
	}

	private static XMLPrettyPrinter.Config printerConfig() {
		XMLPrettyPrinter.Config config = XMLPrettyPrinter.newConfiguration();

		config.setIndentChar(' ');
		config.setIndentStep(2);

		return config;
	}

	private static Map<String, ConfigurationDescriptor> createComponentDefinitionDescriptor(
			DynamicComponentDefinition componentDefinition) {
		return Collections.singletonMap(ROOT_TEMPLATE_ARGUMENTS_TAG_NAME, componentDefinition.descriptor());
	}

	private static void writeTemplateArguments(ConfigurationWriter writer, ConfigurationItem arguments)
			throws XMLStreamException {
		String argumentsLocalName = LayoutModelConstants.TEMPLATE_CALL_ARGUMENTS_ELEMENT;

		writer.writeRootElement(argumentsLocalName, arguments.descriptor(), arguments);
	}

	/**
	 * Returns the layout defined by the given identifier.
	 * 
	 * @param layoutKey
	 *        Layout identifier
	 * 
	 * @return null if the layout could not be created.
	 */
	public static TLLayout getOrCreateLayout(String layoutKey) {
		return LayoutStorage.getInstance().getOrCreateLayout(layoutKey);
	}

	/**
	 * Checks if the layout defined by the given key origins from a template.
	 */
	public static boolean isLayoutFromTemplate(String layoutKey) {
		TLLayout layout = LayoutStorage.getInstance().getLayout(layoutKey);

		if (layout != null) {
			return layout.hasTemplate();
		}

		return false;
	}

	/**
	 * Name scope (layout.xml file) where the component is defined.
	 * 
	 * @param component
	 *        Layout component.
	 * 
	 * @return Layout key for the layout definition file of the given component in the current
	 *         {@link MainLayout}, see {@link MainLayout#getComponentForLayoutKey(String)}.
	 */
	public static String getNonNullNameScope(LayoutComponent component) {
		String nameScope = getDirectNameScope(component);

		if (nameScope == null) {
			return getNonNullNameScope(component.getParent());
		}

		return LayoutUtils.normalizeLayoutKey(nameScope);
	}

	/**
	 * Short-cut to access the {@link ComponentName#scope()} of the {@link LayoutComponent#getName()
	 * name} of the given {@link LayoutComponent}.
	 */
	public static String getDirectNameScope(LayoutComponent component) {
		String scope = component.getName().scope();
		if (MainLayout.UNIQUE_PREFIX.equals(scope)) {
			// treat components with synthetic name as components with local name.
			scope = null;
		}
		return scope;
	}

	/**
	 * Getter to access the configuration property with name "components".
	 * 
	 * @return Components property for the given {@link ConfigurationItem}
	 */
	@SuppressWarnings("unchecked")
	public static List<LayoutComponent.Config> getComponentsProperty(ConfigurationItem config) {
		return (List<LayoutComponent.Config>) TypedConfigUtil.getProperty(config, COMPONENTS_TEMPLATE_PROPERTY);
	}

	/**
	 * Getter to access the configuration property with name "dialogs".
	 * 
	 * @return Dialogs property for the given {@link ConfigurationItem}
	 */
	@SuppressWarnings("unchecked")
	public static List<LayoutComponent.Config> getDialogsProperty(ConfigurationItem config) {
		return (List<LayoutComponent.Config>) TypedConfigUtil.getProperty(config, DialogTemplateParameters.DIALOGS);
	}

	/**
	 * Setter for the components property of a given {@link ConfigurationItem}.
	 * 
	 * @see #getComponentsProperty(ConfigurationItem)
	 */
	public static void setComponentsProperty(ConfigurationItem config,
			List<? extends LayoutComponent.Config> componentConfigs) {
		TypedConfigUtil.setProperty(config, COMPONENTS_TEMPLATE_PROPERTY, componentConfigs);
	}

	/**
	 * Creates a {@link LayoutReference} with the given layout key as resource.
	 * 
	 * @param layoutKey
	 *        Identifier of the layout to create a reference to.
	 * 
	 * @return New {@link LayoutReference} instance.
	 */
	public static LayoutReference createLayoutReference(String layoutKey) {
		LayoutReference newConfigItem = TypedConfiguration.newConfigItem(LayoutReference.class);

		newConfigItem.setResource(layoutKey);

		return newConfigItem;
	}

	/**
	 * Creates a new random unused component layout key.
	 */
	public static String createNewComponentLayoutKey() {
		if (ScriptingRecorder.isEnabled()) {
			List<String> identifiers = DefaultDisplayContext.getDisplayContext().get(COMPONENT_IDENTIFIERS);
			if (!identifiers.isEmpty()) {
				return ((LinkedList<String>) identifiers).removeFirst();
			}
		}

		return StringServices.randomUUID() + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;
	}

	/**
	 * Fetch a formerly recorded identifier provided by {@link CreateComponentButtonAction}.
	 * 
	 * @return A "well-known" UUIDs or <code>null</code> is there is none.
	 */
	public static String wellKnownUUIDs() {
		if (ScriptingRecorder.isEnabled()) {
			List<String> identifiers = DefaultDisplayContext.getDisplayContext().get(UUIDS);
			if (!identifiers.isEmpty()) {
				return ((LinkedList<String>) identifiers).removeFirst();
			}
		}
		return null;
	}

	/**
	 * Computes the relative path of the given file to the root layout directory.
	 * 
	 * @param template
	 *        Layout template file.
	 * 
	 * @return The relativized layout template path.
	 */
	public static Optional<Path> getRelativeTemplatePath(File template) {
		return FileUtilities.getFirstParent(template, ModuleLayoutConstants.LAYOUTS_DIR_NAME)
			.map(parent -> parent.toPath().relativize(template.toPath()));
	}

	/**
	 * Deletes the persistent layout template corresponding to the given layout key.
	 * 
	 * @param layoutKey
	 *        Identifier of the layout to delete.
	 */
	public static boolean deletePersistentTemplateLayout(String layoutKey) {
		Person person = TLContext.getContext().getCurrentPersonWrapper();
		return LayoutStorage.deleteLayoutFromDatabase(person, layoutKey);
	}

	/**
	 * Returns all referenced layout keys of the layout for the given layout key and person.
	 */
	public static Collection<String> getReferencedLayoutKeys(Person person, String layoutKey) throws ConfigurationException {
		Theme theme = ThemeFactory.getTheme();
		Config layoutConfig = LayoutStorage.getInstance().getOrCreateLayoutConfig(theme, person, layoutKey);
		return getAllReferencesLayoutKeys(layoutConfig);
	}

	private static Collection<String> getAllReferencesLayoutKeys(Config layoutConfiguration) {
		return new LayoutResourcesCollector().collectAll(layoutConfiguration);
	}

	/**
	 * Computes a {@link ResPrefix} dependent of the relative path of a given typed layout template.
	 * 
	 * @see I18NConstants#DYNAMIC_COMPONENT
	 */
	public static ResPrefix getPrefixResKey(String relativeTemplatePath) {
		String resPrefix = FileUtilities.replaceSeparator(relativeTemplatePath, '.');
		if (resPrefix.endsWith(FileUtilities.XML_FILE_ENDING)) {
			resPrefix = resPrefix.substring(0, resPrefix.length() - FileUtilities.XML_FILE_ENDING.length());
		}
		return I18NConstants.DYNAMIC_COMPONENT.append(resPrefix);
	}

	private static void writeTemplateInternal(XMLStreamWriter out, String template, ConfigurationItem arguments, boolean isFinal) {
		try {
			out.setPrefix(ConfigurationSchemaConstants.CONFIG_NS_PREFIX, ConfigurationSchemaConstants.CONFIG_NS);
			out.writeStartElement(ConfigurationSchemaConstants.CONFIG_NS, LayoutModelConstants.TEMPLATE_CALL_ELEMENT);
			out.writeAttribute(LayoutModelConstants.TEMPLATE_CALL_TEMPLATE_ATTR, template);
			out.writeAttribute(LayoutModelConstants.TEMPLATE_CALL_FINAL_ATTR, Boolean.toString(isFinal));
			out.writeNamespace(ConfigurationSchemaConstants.CONFIG_NS_PREFIX, ConfigurationSchemaConstants.CONFIG_NS);

			ConfigurationWriter configWriter = new ConfigurationWriter(out);
			configWriter.setNamespaceWriting(false);
			configWriter.writeRootElement(LayoutModelConstants.TEMPLATE_CALL_ARGUMENTS_ELEMENT,
				arguments.descriptor(), arguments);
			
			out.writeEndElement();
		} catch (XMLStreamException exception) {
			Logger.error("Template call could not be written.", exception);
		}
	}

	/**
	 * Writes the template to the given File.
	 * 
	 * @param isFinal
	 *        Whether the layout must not be enhanced with overlays, when it is de-serialized.
	 * 
	 * 
	 * @throws ConfigurationException
	 *         When the arguments of the given {@link LayoutTemplateCall} could not be parsed.
	 */
	public static void writeTemplate(File file, TLLayout layout, boolean isFinal) throws ConfigurationException {
			FileUtilities.ensureFileExisting(file);

			try (OutputStream outputStream = new FileOutputStream(file)) {
				layout.writeTo(outputStream, isFinal);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			try {
				XMLPrettyPrinter.normalizeFile(file);
			} catch (IOException | SAXException exception) {
				throw new IOError(exception);
			}
	}

	/**
	 * Writes a call for the given template with the given arguments into the given output stream.
	 * 
	 * @param stream
	 *        To write into.
	 * @param templateName
	 *        Template name.
	 * @param arguments
	 *        Arguments of a template call.
	 * @param isFinal
	 *        Whether the written template call is final, i.e. whether no overlays must be applied
	 *        when it is read.
	 * @throws IOException
	 *         Template configuration could not be written into the given stream.
	 */
	public static void writeLayoutTemplateCall(OutputStream stream, String templateName, ConfigurationItem arguments,
			boolean isFinal) throws IOException {
		String utf8 = StringServices.UTF8;
		try {
			XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(stream, utf8);
			try {
				out.writeStartDocument(utf8, "1.0");
				LayoutTemplateUtils.writeTemplateInternal(out, templateName, arguments, isFinal);
			} finally {
				out.close();
			}
		} catch (XMLStreamException exception) {
			throw new IOException(exception);
		}
	}

	/**
	 * Resolves the top-level component of the layout file that defines the given
	 * {@link LayoutComponent}.
	 */
	public static LayoutComponent getLayoutContextComponent(LayoutComponent component) {
		String scope = LayoutTemplateUtils.getNonNullNameScope(component);
		return component.getMainLayout().getComponentForLayoutKey(scope);
	}

	/**
	 * When replaying the action, the identifiers are stored in a property
	 * {@link LayoutTemplateUtils#COMPONENT_IDENTIFIERS} at the {@link DisplayContext} from where they
	 * are used by {@link LayoutTemplateUtils#createNewComponentLayoutKey} when creating new
	 * components.
	 */
	public static void recordComponentCreation(ResKey label, Identifiers identifiers, LayoutComponent component) {
		if (ScriptingRecorder.isRecordingActive()) {
			ComponentName name = component.getMainLayout().getName();
			ScriptingRecorder.recordAction(ActionFactory.createComponentButtonAction(identifiers, name, label));
		}
	}

	/**
	 * Updates the transient cards of a given {@link TabComponent} and stores it for the given
	 * layout key into the database.
	 * 
	 * @param newCards
	 *        New {@link TabComponent} cards.
	 * @param arguments
	 *        Typed template arguments for a tabbar containing all tabs in the components property.
	 * @param tabbar
	 *        {@link TabComponent} to be updated.
	 * @param layoutKey
	 *        Key which is used to store the template into the database.
	 * @param templateName
	 *        Name of the used typed template.
	 * 
	 * @see TabComponent#adaptTabs(List)
	 */
	public static void updateTabbarCards(List<? extends Card> newCards, ConfigurationItem arguments,
			TabComponent tabbar, String layoutKey, String templateName) throws ConfigurationException {
		List<LayoutReference> newComponentConfigs = createNewComponentConfigs(newCards);

		setComponentsProperty(arguments, newComponentConfigs);
		LayoutTemplateUtils.storeLayout(layoutKey, templateName, arguments);

		tabbar.adaptTabs(newCards);
	}

	/**
	 * Replaces {@link LayoutTemplate}s inside the given {@link ConfigurationItem} with
	 * {@link LayoutReference}s returning the used {@link LayoutReference#getResource()}.
	 */
	public static Identifiers replaceInnerTemplates(ConfigurationItem arguments) {
		LayoutTemplateConfigurationVisitor templateVisitor = new LayoutTemplateConfigurationVisitor();

		templateVisitor.replaceAndStore(arguments);

		return templateVisitor.getIdentifiers();
	}

	/**
	 * Replaces the component identified by the given layout key.
	 */
	public static void replaceComponent(String layoutKey, LayoutComponent component) {
		try {
			Config persistentLayout = LayoutStorage.getInstance().getOrCreateLayoutConfig(ThemeFactory.getTheme(),
				TLContext.getContext().getPerson(), layoutKey);

			MainLayout mainLayout = component.getMainLayout();
			if (mainLayout != null) {
				mainLayout.replaceComponent(layoutKey, persistentLayout);
			}
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.REPLACE_COMPONENT_ERROR__LAYOUT_KEY.fill(layoutKey), exception);
		}
	}

	/**
	 * Stores the template into the database for the given layout key.
	 */
	public static void storeLayout(String layoutKey, String template, ConfigurationItem arguments) {
		LayoutStorage.storeLayout(TLContext.getContext().getPerson(), layoutKey, template, arguments);
	}

	private static List<LayoutReference> createNewComponentConfigs(List<? extends Card> newCards) {
		return newCards.stream().map(card -> {
			Object content = card.getContent();
			String layoutKey = null;
			if (content instanceof LayoutComponent) {
				layoutKey = LayoutTemplateUtils.getNonNullNameScope((LayoutComponent) content);
			} else if (content instanceof LayoutReference) {
				layoutKey = ((LayoutReference) content).getResource();
			}
			return LayoutTemplateUtils.createLayoutReference(layoutKey);
		}).collect(Collectors.toList());
	}

	/**
	 * Creates a {@link TemplateComponentContext} object for the given {@link ComponentName}.
	 */
	public static TemplateComponentContext createTemplateComponentContext(TemplateContext context, ComponentName name) {
		TemplateComponentContext componentContext = new DefaultTemplateComponentContext();

		componentContext.setName(name);
		componentContext.setTemplate(context.getTemplate());
		componentContext.setTemplateArguments(context.getTemplateArguments());

		return componentContext;
	}

	/**
	 * Collect all templates belonging to one of the given template groups and usable in the given
	 * context component.
	 */
	public static List<DynamicComponentDefinition> getTemplates(Collection<String> groups,
			LayoutComponent contextComponent) {
		return getComponentDefinitions().stream()
			.filter(componentDefinition -> {
				Collection<String> belongingGroup = componentDefinition.getBelongingGroups();
				boolean groupMatches = false;
				for (String group : groups) {
					if (belongingGroup.contains(group)) {
						groupMatches = true;
						break;
					}
				}
				return groupMatches && componentDefinition.usableInContext(contextComponent);
			})
			.sorted(createTemplateLabelComparator())
			.collect(Collectors.toList());
	}

	private static Comparator<DynamicComponentDefinition> createTemplateLabelComparator() {
		return Comparator.comparing(definition -> Resources.getInstance().getString(definition.label()));
	}

	private static Collection<DynamicComponentDefinition> getComponentDefinitions() {
		return DynamicComponentService.Module.INSTANCE
			.getImplementationInstance()
			.getComponentDefinitions();
	}

	/**
	 * Replaces the given component with a new component instantiated from the given template
	 * definition and arguments.
	 */
	public static void createComponent(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem arguments, List<AdditionalComponentDefinition> additionals) {
		String nameScope = LayoutTemplateUtils.getNonNullNameScope(component);
		
		Map<String, String> _mapping = new HashMap<>();
		_mapping.put(LayoutModelConstants.TEMPLATE_CALL_ENCLOSING_LAYOUT_SCOPE_VALUE, nameScope);
		for (AdditionalComponentDefinition additional : additionals) {
			String configuredScope = additional.getConfiguredNameScope();
			if (configuredScope != null) {
				_mapping.put(configuredScope, additional.getLayoutKey());
			}
		}
		
		/* References to the configured name scopes must be replaced by the actual layout scopes.
		 * The configured scopes must not be used, because they would lead to clashes when the
		 * template is instantiated twice. */
		LayoutScopeMapper scopeMapper = new LayoutScopeMapper(_mapping);

		scopeMapper.map(arguments);
		Identifiers baseIdentifiers = LayoutTemplateUtils.replaceInnerTemplates(arguments);

		/* The layout keys in the additional are created first. */
		baseIdentifiers.prependComponentKeys(
			additionals.stream()
				.map(AdditionalComponentDefinition::getLayoutKey)
				.collect(Collectors.toList()));

		additionals.forEach(x -> {
			ConfigurationItem arg = x.getArguments();
			scopeMapper.map(arg);
			Identifiers innerIdentifiers = LayoutTemplateUtils.replaceInnerTemplates(arg);
			baseIdentifiers.append(innerIdentifiers);
		});

		LayoutTemplateUtils.recordComponentCreation(ButtonType.OK.getButtonLabelKey(), baseIdentifiers, component);

		LayoutTemplateUtils.storeLayout(nameScope, definition.definitionFile(), arguments);

		additionals.forEach(x -> {
			LayoutTemplateUtils.storeLayout(x.getLayoutKey(), x.getDefinition().definitionFile(), x.getArguments());
		});

		LayoutTemplateUtils.replaceComponent(nameScope, component);
	}

	/**
	 * Serialize and then deserialize the arguments from the given {@link TLLayout}.
	 */
	public static ConfigurationItem getReparsedArguments(String layoutKey, TLLayout layout)
			throws ConfigurationException {
		String arguments = LayoutTemplateUtils.getSerializedTemplateArguments(layout.getArguments());

		return LayoutTemplateUtils.getDeserializedTemplateArguments(layoutKey, layout.getTemplateName(), arguments);
	}

}
