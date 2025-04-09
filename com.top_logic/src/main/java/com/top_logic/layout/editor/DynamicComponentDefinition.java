/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.io.binary.NoBinaryData;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.gui.Theme;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.editor.commands.AdditionalComponentDefinition;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.ElementInAttributeContextError;
import com.top_logic.layout.processor.Expansion;
import com.top_logic.layout.processor.Expansion.Buffer;
import com.top_logic.layout.processor.LayoutInline;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.ParameterValue;
import com.top_logic.layout.processor.RangeValue;
import com.top_logic.layout.processor.ResolveFailure;
import com.top_logic.layout.processor.StringValue;
import com.top_logic.layout.processor.TemplateCallProcessor;
import com.top_logic.layout.processor.TemplateLayout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.Resources;

/**
 * Definition for a layout component that can dynamically be instantiated.
 * 
 * <p>
 * A {@link DynamicComponentDefinition} is loaded from layout templates by the
 * {@link DynamicComponentService}.
 * </p>
 * 
 * @see DynamicComponentService#getComponentDefinition(String)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicComponentDefinition {

	/**
	 * {@link TemplateCallProcessor} replacing a template call by a {@link LayoutReference}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class LayoutReferenceReplacer implements TemplateCallProcessor {
		private final String _layoutName;

		private final List<AdditionalComponentDefinition> _components;

		private final Protocol _log;

		/**
		 * Creates a new {@link LayoutReferenceReplacer}.
		 */
		private LayoutReferenceReplacer(String layoutName, List<AdditionalComponentDefinition> components,
				Protocol log) {
			_layoutName = layoutName;
			_components = components;
			_log = log;
		}

		@Override
		public List<? extends Node> expandElement(Expansion expansion, Element templateCall, Buffer out)
				throws ElementInAttributeContextError {
			Optional<DynamicComponentDefinition> dynamicDefinition =
				expansion.getTemplateComponentDefinition(templateCall);
			if (dynamicDefinition.isEmpty()) {
				return Collections.emptyList();
			}
			DynamicComponentDefinition definition = dynamicDefinition.get();
			Optional<ConfigurationItem> innerArgs = expansion.createArguments(templateCall, definition);

			if (innerArgs.isEmpty()) {
				return Collections.emptyList();
			}

			Optional<String> assistentFor = definition.getAssistentFor();
			String layoutKey;
			if (assistentFor.isPresent()) {
				DynamicComponentDefinition generalDefinition =
					DynamicComponentService.getInstance().getComponentDefinition(assistentFor.get());
				ConfigurationItem body;
				try {
					body = definition.createTemplateBody(_log, innerArgs.get(), _components, _layoutName);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
				layoutKey = addAdditional(templateCall, generalDefinition, body);
			} else {
				layoutKey = addAdditional(templateCall, definition, innerArgs.get());
			}

			Element newReference = out.appendElement(null, LayoutReference.TAG_NAME);
			newReference.setAttribute(LayoutReference.RESOURCE_ATTRIBUTE, layoutKey);
			return Collections.singletonList(newReference);
		}

		private String addAdditional(Element templateCall, DynamicComponentDefinition definition,
				ConfigurationItem args) {
			String newLayoutKey = LayoutTemplateUtils.createNewComponentLayoutKey();
			String configuredScope = templateCall.getAttribute(LayoutModelConstants.TEMPLATE_CALL_LAYOUT_SCOPE_ATTR);
			_components.add(new AdditionalComponentDefinition(newLayoutKey, configuredScope, definition, args));
			return newLayoutKey;
		}

	}

	/**
	 * {@link LabelProvider} for a {@link DynamicComponentDefinition}.
	 * 
	 * <p>
	 * This {@link LabelProvider} returns the internationalized
	 * {@link DynamicComponentDefinition#label()}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Labels extends AbstractResourceProvider {

		@Override
		public String getLabel(Object object) {
			if (object == null) {
				return null;
			}
			return Resources.getInstance().getString(resourceKey(object));
		}

		@Override
		public String getTooltip(Object object) {
			if (object == null) {
				return null;
			}
			return Resources.getInstance().getString(resourceKey(object).tooltipOptional());
		}

		private ResKey resourceKey(Object object) {
			return ((DynamicComponentDefinition) object).label();
		}

	}

	/**
	 * Exception that is thrown when the format of the base data are not supported.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class UnsupportedFormatException extends Exception {
		// marker Exception
	}

	/**
	 * Exception that is thrown when the format of the underlying date is unknown.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class UnknownFormatException extends UnsupportedFormatException {
		// marker Exception
	}

	private final ConfigurationDescriptor _descriptor;

	private final Element _layoutConfig;

	private ResKey _labelKey = I18NConstants.DEFAULT_DYNAMIC_COMPONENT_LABEL;

	private String _dataDefinition = StringServices.EMPTY_STRING;

	private Collection<String> _belongingGroups;

	private Collection<String> _usableInGroups;

	private Optional<String> _assistantFor;

	private Predicate<? super LayoutComponent> _usableInContext = component -> true;

	/**
	 * Creates a new {@link DynamicComponentDefinition}.
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} describing the necessary properties to create the
	 *        layout configuration.
	 * @param layoutTemplate
	 *        The template that is used to create the {@link LayoutComponent} configuration.
	 * @param belongingGroups
	 *        Groups where the template belongs to. Group of definitions it belongs to.
	 * @param usableInGroups
	 *        Groups where the template is usable in.
	 * @param assistantFor
	 *        If this template is an assistant template, this parameter holds the name of the
	 *        template for which this is the template.
	 * @param usableInContext
	 *        Function to decide whether this {@link DynamicComponentDefinition} can be used in
	 *        context of a given {@link LayoutComponent}.
	 */
	public DynamicComponentDefinition(ConfigurationDescriptor descriptor, Element layoutTemplate,
			Collection<String> belongingGroups, Collection<String> usableInGroups, Optional<String> assistantFor,
			Predicate<? super LayoutComponent> usableInContext) {
		_belongingGroups = belongingGroups;
		_descriptor = descriptor;
		_layoutConfig = layoutTemplate;
		_usableInGroups = usableInGroups;
		_assistantFor = assistantFor;
		_usableInContext = usableInContext;
	}

	/**
	 * Returns a {@link ResKey} that can be used as label for this
	 * {@link DynamicComponentDefinition}.
	 */
	public ResKey label() {
		return _labelKey;
	}

	/**
	 * Setter for {@link #label()}.
	 */
	public void setLabelKey(ResKey labelKey) {
		_labelKey = labelKey;
	}

	/**
	 * Set of groups where this component definition belongs to.
	 */
	public Collection<String> getBelongingGroups() {
		return _belongingGroups;
	}

	/**
	 * Set of group names where the definition is usable.
	 */
	public Collection<String> getUsableGroups() {
		return _usableInGroups;
	}

	/**
	 * A name identifying the source where this {@link DynamicComponentDefinition} was created from.
	 */
	public String definitionFile() {
		return _dataDefinition;
	}

	/**
	 * Setter for {@link #definitionFile()}.
	 */
	public void setDefinitionFile(String definition) {
		_dataDefinition = Objects.requireNonNull(definition);
		
	}

	/**
	 * Returns the definition for which this definition is an assistant for.
	 */
	public Optional<String> getAssistentFor() {
		return _assistantFor;
	}

	/**
	 * The {@link ConfigurationDescriptor} describing the parameters that are needed to create the
	 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
	 */
	public ConfigurationDescriptor descriptor() {
		return _descriptor;
	}

	/**
	 * Whether this {@link DynamicComponentDefinition} is usable in context of the given
	 * {@link LayoutComponent}.
	 * 
	 * @param context
	 *        {@link LayoutComponent} in whose context this definition is to be used.
	 */
	public boolean usableInContext(LayoutComponent context) {
		return _usableInContext.test(context);
	}

	private ConfigurationItem createTemplateBody(Protocol log, ConfigurationItem parameters,
			List<AdditionalComponentDefinition> components, String layoutName)
			throws ConfigurationException {
		Map<String, ParameterValue> arguments = new HashMap<>();
		Document document = createComponentTemplate(log, parameters, arguments);
		if (document == null) {
			return null;
		}

		LayoutResolver layoutResolver = createLayoutResolver(log);
		ConstantLayout layout = createLayout(document, layoutResolver, layoutName);
		
		if (getAssistentFor().isPresent()) {
			InteractionContext interaction = ThreadContextManager.getInteraction();
			if (interaction.get(Expansion.PROCESSOR) == null) {
				interaction.set(Expansion.PROCESSOR, new LayoutReferenceReplacer(layoutName, components, log));
			}
		}
		inlineLayout(arguments, layoutResolver, layout);

		return createBodyConfiguration(log, layoutResolver, layout);
	}

	private LayoutResolver createLayoutResolver(Protocol log) {
		return LayoutResolver.newRuntimeResolver(log);
	}

	private ConstantLayout createLayout(Document document, LayoutResolver resolver, String layoutName) {
		return new ConstantLayout(resolver, layoutName,
			new NoBinaryData("Dynamically generated template: " + layoutName), document);
	}

	private void inlineLayout(Map<String, ParameterValue> arguments, LayoutResolver resolver, ConstantLayout layout) {
		LayoutInline inliner = new LayoutInline(resolver);

		try {
			inliner.inline(layout, arguments);
		} catch (ResolveFailure exception) {
			throw new RuntimeException("Unable to inline layout.", exception);
		}
	}

	private BinaryContent getContent(ConstantLayout layout) {
		return DOMUtil.toBinaryContent(layout.getLayoutDocument(), "layout:" + layout.getLayoutName());
	}

	private ConfigurationItem createBodyConfiguration(Protocol log, LayoutResolver resolver, ConstantLayout layout)
			throws ConfigurationException {
		try {
			String layoutName = layout.getLayoutName();

			return createBody(log, resolver.getTheme(), layoutName, getContent(layout));
		} catch (ConfigurationException exception) {
			throw new ConfigurationException(
				I18NConstants.INVALID_LAYOUT_TEMPLATE_BODY_DETAILS__TEMPLATE.fill(_dataDefinition),
				exception.getPropertyName(), exception.getPropertyValue(), exception);
		}
	}

	private ConfigurationItem createBody(Protocol log, Theme theme, String layoutScope, BinaryContent content)	throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);

		return LayoutUtils.createConfiguration(context, theme, layoutScope, content, getGlobalDescriptorByName());
	}

	private Map<String, ConfigurationDescriptor> getGlobalDescriptorByName() {
		if (_assistantFor.isPresent()) {
			return Collections.singletonMap("config", getDefinition(_assistantFor.get()).descriptor());
		} else {
			return MainLayout.COMPONENT_DESCRIPTORS;
		}
	}

	private DynamicComponentDefinition getDefinition(String assistantFor) {
		return DynamicComponentService.getInstance().getComponentDefinition(assistantFor);
	}

	/**
	 * Creates a new {@link com.top_logic.mig.html.layout.LayoutComponent.Config} for this
	 * {@link DynamicComponentDefinition} with parameters fetched from the given
	 * {@link ConfigurationItem}.
	 * 
	 * @param log
	 *        Log to writer problems to.
	 * @param parameters
	 *        A {@link ConfigurationItem} appropriate for {@link #descriptor()} with the parameters
	 *        for the result {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
	 * 
	 * @return May be <code>null</code> in case of errors. In such case an error was logged to the
	 *         given log.
	 */
	public LayoutComponent.Config createComponentConfig(Protocol log, ConfigurationItem parameters, String nameScope)
			throws ConfigurationException {
		ConfigurationItem templateBody = createTemplateBody(log, parameters, new ArrayList<>(), nameScope);

		if(templateBody instanceof LayoutComponent.Config) {
			return LayoutUtils.transformLayoutConfig((Config) templateBody);
		} else {
			log.error(createInvalidTemplateBodyTypeErrorMessage());
			
			return null;
		}
	}

	private String createInvalidTemplateBodyTypeErrorMessage() {
		return "Body of template " + definitionFile() + " is not a valid layout configuration.";
	}

	/**
	 * Creates a {@link ConfigurationItem} for the body part of the template given by this
	 * {@link DynamicComponentDefinition} with parameters fetched from the given
	 * {@link ConfigurationItem}.
	 * 
	 * @param parameters
	 *        Typed template parameters.
	 * 
	 */
	public ConfigurationItem createTemplateBody(ConfigurationItem parameters,
			List<AdditionalComponentDefinition> components) throws ConfigurationException {
		return createTemplateBody(new LogProtocol(DynamicComponentDefinition.class), parameters, components,
			_dataDefinition);
	}

	/**
	 * Creates a {@link Document} which represents a {@link LayoutModelConstants#TEMPLATE_ELEMENT}
	 * which can be read and interpreted by the layout reader.
	 * 
	 * @see TemplateLayout
	 * @see LayoutInline
	 * 
	 * @param log
	 *        {@link Log} to write problems to.
	 * @param properties
	 *        Arguments for the instantiation of the template. Must be compatible with
	 *        {@link #descriptor()}.
	 * @param inlineArguments
	 *        Output parameter to be filled with the arguments for the template invocation.
	 */
	public Document createComponentTemplate(Log log, ConfigurationItem properties,
			Map<String, ParameterValue> inlineArguments) {
		if (properties.descriptor() != descriptor()) {
			throw new IllegalArgumentException(
				"Descriptor of parameters '" + properties.descriptor()
					+ "' is not the descriptor of component definition: " + descriptor());
		}
		Document doc = DOMUtil.newDocument();

		Element rootElement = newElement(doc, LayoutModelConstants.TEMPLATE_ELEMENT);
		doc.appendChild(rootElement);
		rootElement.appendChild(createParamsElement(log, doc, properties, inlineArguments));

		String layoutConfigTagName = _layoutConfig.getLocalName();
		if (!_assistantFor.isPresent()) {
			if (MainLayout.COMPONENT_DESCRIPTORS.get(layoutConfigTagName) == null
				&& !LayoutModelConstants.INCLUDE_ELEMENT.equals(layoutConfigTagName)) {
				log.error("Tagname '" + layoutConfigTagName + "' in '" + _dataDefinition + "' is not a valid tag for "
					+ LayoutComponent.class.getName());
				return null;
			}
		}
		if (_layoutConfig.getNamespaceURI() != null) {
			log.error("Layout configuration in '" + _dataDefinition + "' must not have a namespace but has "
				+ _layoutConfig.getNamespaceURI());
			return null;
		}
		Node layoutNode = doc.importNode(_layoutConfig, true);
		rootElement.appendChild(layoutNode);
		return doc;
	}

	private Element createParamsElement(Log log, Document doc, ConfigurationItem parameters,
			Map<String, ParameterValue> inlineArguments) {
		Element params = newElement(doc, LayoutModelConstants.PARAMS_ELEMENT);
		parameters.descriptor()
			.getProperties()
			.stream()
			.map(property -> createParam(log, doc, parameters, property, inlineArguments))
			.filter(Objects::nonNull)
			.forEach(params::appendChild);
		return params;
	}

	private Element createParam(Log log, Document doc, ConfigurationItem parameters, PropertyDescriptor property,
			Map<String, ParameterValue> inlineArguments) {
		Element param = newElement(doc, LayoutModelConstants.PARAM_ELEMENT);
		String propertyName = property.getPropertyName();
		addAttribute(param, LayoutModelConstants.PARAM_NAME, propertyName);

		ConfigurationValueProvider valueProvider = property.getValueProvider();
		if (valueProvider != null) {
			Object value = parameters.value(property);
			if (valueProvider.isLegalValue(value)) {
				String formattedValue = valueProvider.getSpecification(value);
				if (ConfigurationWriter.containsAttributeSpecial(formattedValue)) {
					Element propertyNode = newElement(doc, propertyName);
					propertyNode.appendChild(doc.createTextNode(formattedValue));
					inlineArguments.put(propertyName, RangeValue.createArgumentValue(propertyName, propertyNode));
				} else {
					inlineArguments.put(propertyName, new StringValue(propertyName, formattedValue));
				}
				return param;
			}
		}

		switch (property.kind()) {
			case DERIVED: {
				Object value = parameters.value(property);
				String stringValue;
				if (value == null) {
					stringValue = "";
				} else {
					// value may have primitive type for which a ConfigurationValueProvider may
					// exist.
					ConfigurationValueProvider primitiveValueProvider =
						BuiltInFormats.getPrimitiveValueProvider(value.getClass());
					if (primitiveValueProvider != null) {
						stringValue = primitiveValueProvider.getSpecification(value);
					} else {
						stringValue = value.toString();
					}
				}
				inlineArguments.put(propertyName, new StringValue(propertyName, stringValue));
				break;
			}
			case PLAIN: {
				if (valueProvider == null) {
					log.error("No value provider vor plain property '" + property + "' given.");
				} else {
					Object value = parameters.value(property);
					if (!valueProvider.isLegalValue(value)) {
						log.error("Value '" + value + "' is not a legal value for property '" + property
							+ "' with value provider '" + valueProvider + "'.");
					} else {
						assert false : "Plain property with ValueProvider and legal value is handled above.";
					}
				}
				return null;
			}
			case LIST:
			case ARRAY:
			case MAP:
			case ITEM:
			case COMPLEX: {
				try (ByteArrayStream cache = new ByteArrayStream()) {
					try {
						if (property.kind() == PropertyKind.ITEM) {
							/* Can not use default method to serialise property: If the property is
							 * of type PolymorphicConfiguration of X and the property is of the same
							 * type, then the configuration writer will suppress the implementation
							 * class. */
							serializeItemValue(cache, parameters, property);
						} else {
							serializePropertyValue(cache, parameters, property);
						}
						if (cache.size() > 0) {
							// Do not try parse empty input stream.
							inlineArguments.put(propertyName,
								RangeValue.createArgumentValue(propertyName, parse(cache)));
						} else {
							// Mark as non-mandatory.
							param.appendChild(doc.createComment("Empty value"));
						}
					} catch (SAXException | XMLStreamException ex) {
						log.error("Unable to write parameters.", ex);
						return null;
					}
				} catch (IOException ex) {
					throw new UnreachableAssertion("ByteArrayStream does not throw IOException.");
				}
				break;

			}
			default:
				break;

		}
		return param;
	}

	private Element parse(BinaryContent childContent)
			throws SAXException, IOException {
		InputSource source = new InputSource(childContent.getStream());
		return DOMUtil.getDocumentBuilder().parse(source).getDocumentElement();
	}

	private void serializeItemValue(OutputStream outStream, ConfigurationItem parameters, PropertyDescriptor property)
			throws XMLStreamException {
		ConfigurationItem itemValue = property.getConfigurationAccess().getConfig(parameters.value(property));
		if (itemValue == null) {
			return;
		}
		XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(outStream, "utf-8");
		try (ConfigurationWriter configurationWriter = new ConfigurationWriter(out)) {
			configurationWriter.write(property.getPropertyName(), ConfigurationItem.class, itemValue);
		} finally {
			out.close();
		}
	}

	private void serializePropertyValue(OutputStream outStream, ConfigurationItem parameters,
			PropertyDescriptor property) throws XMLStreamException {
		XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(outStream, "utf-8");
		try (ConfigurationWriter configurationWriter = new ConfigurationWriter(out)) {
			// Write also empty lists, and maps
			configurationWriter.writeNonPlainProperty(parameters, property, true);
		} finally {
			out.close();
		}
	}

	private void addAttribute(Element node, String name, String value) {
		node.setAttributeNS(null, name, value);
	}

	private Element newElement(Document doc, String tagName) {
		return doc.createElementNS(null, tagName);
	}

	/**
	 * Creates a new {@link ConfigurationItem} representing the arguments given by this
	 * {@link DynamicComponentDefinition} with all values set to their defaults.
	 * 
	 * @return An empty typed layout argument object.
	 */
	public ConfigurationItem createArgumentsConfig() {
		return descriptor().factory().createNew();
	}

	/**
	 * Creates an empty layout template for this {@link DynamicComponentDefinition}.
	 * 
	 * @return A {@link LayoutTemplate} with all default arguments.
	 */
	public LayoutTemplate createTemplate() {
		LayoutTemplate layoutTemplateConfig = TypedConfiguration.newConfigItem(LayoutTemplate.class);
	
		layoutTemplateConfig.setTemplateArguments(createArgumentsConfig());
		layoutTemplateConfig.setTemplatePath(definitionFile());
	
		return layoutTemplateConfig;
	}

	private static String getPathRelativToLayoutDirectory(File template) {
		return LayoutTemplateUtils.getRelativeTemplatePath(template)
			.map(templatePath -> templatePath.toString().replace("\\", "/")).orElse(template.getName());
	}

	/**
	 * Parses the given template stream to a {@link DynamicComponentDefinition}.
	 * 
	 * @throws UnsupportedFormatException
	 *         Iff the given XML file uses an invalid format.
	 * 
	 * @return The {@link DynamicComponentDefinition} parsed from the given template. May be
	 *         <code>null</code> in cases of errors. In such case an error is logged to the given
	 *         {@link Protocol}.
	 */
	public static DynamicComponentDefinition parseTemplate(Log log, BinaryData templateData)
			throws IOException, UnsupportedFormatException {
		try (InputStream input = templateData.getStream()) {
			DynamicComponentDefinition template = parseTemplate(log, input, templateData.getName());

			if (template != null) {
				template.setDefinitionFile(LayoutUtils.getLayoutKey(templateData.getName()));
			}

			return template;
		}
	}

	private static DynamicComponentDefinition parseTemplate(Log log, InputStream in, String templateName)
			throws IOException, UnsupportedFormatException {
		Document document;
		try {
			document = DOMUtil.newDocumentBuilderNamespaceAware().parse(in);
		} catch (SAXException | ParserConfigurationException ex) {
			log.info("Invalid XML content in '" + templateName + "': " + ex.getMessage(), Log.WARN);
			return null;
		}
		Element documentElement = document.getDocumentElement();
		if (!ConfigurationSchemaConstants.CONFIG_NS.equals(documentElement.getNamespaceURI())) {
			throw new UnknownFormatException();
		}
		if (!"template".equals(documentElement.getLocalName())) {
			throw new UnknownFormatException();
		}
		return parseVersion1(log, documentElement, templateName);
	}

	private static DynamicComponentDefinition parseVersion1(Log log, Element documentElement, String templateName) {
		Iterator<Element> elements = DOMUtil.elements(documentElement).iterator();

		if (!elements.hasNext()) {
			log.error("No elements in the template " + templateName);
			return null;
		}

		Element configNode = elements.next();

		Protocol descriptorLog = new LogProtocol(DynamicComponentDefinition.class);
		ConfigurationItem config = readDescriptor(descriptorLog, configNode, templateName);
		try {
			descriptorLog.checkErrors();
		} catch (RuntimeException ex) {
			log.error("First element '" + configNode + "' in '" + templateName
				+ "' is not a valid configuration descriptor configuration.", ex);
			return null;
		}

		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(descriptorLog, (ConfigurationDescriptorConfig) config);
		try {
			descriptorLog.checkErrors();
		} catch (RuntimeException ex) {
			log.error("First element '" + configNode + "' in '" + templateName
				+ "' is not a valid configuration descriptor configuration.", ex);
			return null;
		}

		if (!elements.hasNext()) {
			log.error("No layout configuration in " + templateName);
			return null;
		}
		Element layoutConfig = elements.next();
		if (elements.hasNext()) {
			log.info("Elements '" + StringServices.toString(elements, ",") + "' in file '' are not used.",
				Protocol.WARN);
		}
		List<String> templateGroups = getBelongingTemplateGroups(documentElement);
		Collection<String> usableTemplateGroupNames =
			new AllAvailableTemplateGroupNames().getGroupNames(config);
		Optional<String> assistentFor = getAssistentFor(documentElement);
		Predicate<? super LayoutComponent> usableInContext;
		try {
			usableInContext = getUsableInContext(documentElement);
		} catch (ConfigurationException ex) {
			log.error("Unable to get function to determine usability in context in template " + templateName, ex);
			usableInContext = component -> true;
		}
		return new DynamicComponentDefinition(descriptor, layoutConfig, templateGroups, usableTemplateGroupNames,
			assistentFor, usableInContext);
	}

	private static Predicate<? super LayoutComponent> getUsableInContext(Element documentElement)
			throws ConfigurationException {
		String propertyName = "usable-in-context";
		String className = documentElement.getAttribute(propertyName);

		if (StringServices.isEmpty(className)) {
			return component -> true;
		}

		@SuppressWarnings("unchecked")
		Predicate<? super LayoutComponent> configuredPredicate =
			ConfigUtil.getInstanceMandatory(Predicate.class, propertyName, className);
		return configuredPredicate;
	}

	private static Optional<String> getAssistentFor(Element documentElement) {
		String assistentFor = documentElement.getAttribute("assistant-for");

		if (StringServices.isEmpty(assistentFor)) {
			return Optional.empty();
		}

		return Optional.of(assistentFor);
	}

	private static List<String> getBelongingTemplateGroups(Element documentElement) {
		String groupsAttribute = documentElement.getAttribute("groups");
		String[] groups = groupsAttribute.trim().split("\\s*,\\s*");
		return Arrays.asList(groups);
	}

	private static ConfigurationItem readDescriptor(Protocol log, Element configNode, String templateName) {
		BinaryContent content =
			DOMUtil.toBinaryContent(configNode, templateName + ":node:" + configNode.getLocalName() + "");
		DefaultInstantiationContext ctx = new DefaultInstantiationContext(log);
		ConfigurationReader reader = new ConfigurationReader(ctx, Collections.singletonMap(configNode.getLocalName(),
			TypedConfiguration.getConfigurationDescriptor(ConfigurationDescriptorConfig.class)));
		reader.setSource(content);
		ConfigurationItem config;
		try {
			config = reader.read();
		} catch (ConfigurationException ex) {
			config = null;
			log.error("Unable to read descriptor.", ex);
		}
		if (log.hasErrors()) {
			return null;
		}
		return config;
	}

}
