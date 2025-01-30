/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.importer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.bpe.bpml.display.FormProvider;
import com.top_logic.bpe.bpml.display.ProcessFormDefinition;
import com.top_logic.bpe.bpml.display.SpecializedForm;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.io.AttributeValueBinding;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.wysiwyg.storage.StructuredTextAttributeValueBinding;
import com.top_logic.util.Resources;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.handlers.Handler;

/**
 * {@link Handler} reading <i>TopLogic</i> BPML extension elements.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HandleTLExtensions implements Handler {

	private static final Property<Map<TLModelPart, AttributeValueBinding<?>>> BINDINGS =
		TypedAnnotatable.propertyMap("bindings");

	private Map<String, Upgrade> _upgrades;

	/**
	 * Creates a {@link HandleTLExtensions}.
	 */
	public HandleTLExtensions() {
		super();
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		readExtensions:
		while (true) {
			switch (in.next()) {
				case XMLStreamConstants.START_ELEMENT: {
					if (BPMLExporter.TL_EXTENSIONS_NS.equals(in.getNamespaceURI())) {
						importTLExtension(context, in);
					} else {
						XMLStreamUtil.skipUpToMatchingEndTag(in);
					}
					break;
				}
				case XMLStreamConstants.END_DOCUMENT:
				case XMLStreamConstants.END_ELEMENT: {
					break readExtensions;
				}
			}
		}
		return null;
	}

	private void importTLExtension(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();

		TLObject self = (TLObject) context.getVar(ImportContext.THIS_VAR);
		TLStructuredTypePart attribute = self.tType().getPart(localName);
		if (attribute == null) {
			// Upgrade from old versions.
			Upgrade upgrade = getUpgrade(context, self.tType(), localName);
			if (upgrade != null) {
				upgrade.handleUpgrade(context, in, self);
			} else {
				context.error(in.getLocation(), I18NConstants.INVALID_BPML_EXTENSION__NAME.fill(localName));
				XMLStreamUtil.skipUpToMatchingEndTag(in);
			}
		} else {
			AttributeValueBinding<?> binding = fetchBinding(context, attribute);
			if (binding == null) {
				context.error(in.getLocation(), I18NConstants.INVALID_BPML_EXTENSION__NAME.fill(localName));
				XMLStreamUtil.skipUpToMatchingEndTag(in);
			} else {
				Object value = binding.loadValue(context, in, attribute);
				self.tUpdate(attribute, value);
			}
		}

		assert in.getEventType() == XMLStreamConstants.END_ELEMENT : "Not at an end tag after reading extension '"
			+ localName + "': " + XMLStreamUtil.getEventName(in.getEventType());

		String namespace = in.getNamespaceURI();
		String localNameAfter = in.getLocalName();

		assert BPMLExporter.TL_EXTENSIONS_NS.equals(namespace)
			&& localName.equals(localNameAfter) : "Invalid state after reading extension '" + localName
				+ "', looking at tag: " + (namespace == null ? "" : (namespace + "/")) + localNameAfter;
	}

	private AttributeValueBinding<?> fetchBinding(ImportContext context, TLModelPart attribute) {
		Map<TLModelPart, AttributeValueBinding<?>> bindings = context.mkMap(BINDINGS);
		AttributeValueBinding<?> binding = bindings.get(attribute);
		if (binding == null && !bindings.containsKey(attribute)) {
			binding = AttributeSettings.getInstance()
				.getExportBinding(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, attribute);
			bindings.put(attribute, binding);
		}
		return binding;
	}

	private Upgrade getUpgrade(ImportContext context, TLStructuredType type, String localName) {
		if (_upgrades == null) {
			initUpgrades(context);
		}

		String name = TLModelUtil.qualifiedName(type) + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + localName;

		Upgrade upgrade = _upgrades.get(name);
		if (upgrade != null) {
			return upgrade;
		}

		if (type instanceof TLClass classType) {
			for (TLClass superType : classType.getGeneralizations()) {
				Upgrade inherited = getUpgrade(context, superType, localName);
				if (inherited != null) {
					addUpgrade(name, inherited);
					return inherited;
				}
			}
		}

		return null;
	}

	private void initUpgrades(ImportContext context) {
		_upgrades = new HashMap<>();

		Locale defaultLocale = ResourcesModule.getInstance().getDefaultLocale();

		AttributeValueBinding<?> internationalizeHtml = new AttributeValueBinding<I18NStructuredText>() {
			AttributeValueBinding<StructuredText> _htmlBindig = StructuredTextAttributeValueBinding.INSTANCE;
	
			@Override
			public I18NStructuredText loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute)
					throws XMLStreamException {
				StructuredText defaultValue = _htmlBindig.loadValue(log, in, attribute);
				if (defaultValue == null) {
					return null;
				}

				return new I18NStructuredText(Collections.singletonMap(defaultLocale, defaultValue));
			}
	
			@Override
			public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute,
					I18NStructuredText value)
					throws XMLStreamException {
				_htmlBindig.storeValue(log, out, attribute, value.localize(defaultLocale));
			}
		};

		AttributeValueBinding<?> internationalizeText = new AttributeValueBinding<ResKey>() {
			@SuppressWarnings("unchecked")
			AttributeValueBinding<String> _textBinding =
				(AttributeValueBinding<String>) fetchBinding(context, TLModelUtil.findType("tl.core:String"));

			@Override
			public ResKey loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute)
					throws XMLStreamException {
				String text = _textBinding.loadValue(log, in, attribute);
				if (StringServices.isEmpty(text)) {
					return null;
				}
				return ResKey.builder().add(defaultLocale, text).build();
			}

			@Override
			public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, ResKey value)
					throws XMLStreamException {
				_textBinding.storeValue(log, out, attribute, Resources.getInstance(defaultLocale).getString(value));
			}
		};

		AttributeValueBinding<ResKey> template2reskey = new AttributeValueBinding<>() {
			@SuppressWarnings("unchecked")
			AttributeValueBinding<String> _textBinding =
				(AttributeValueBinding<String>) fetchBinding(context, TLModelUtil.findType("tl.core:String"));

			@Override
			public ResKey loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute)
					throws XMLStreamException {
				String html = XMLStreamUtil.nextText(in);
				if (StringServices.isEmpty(html)) {
					return null;
				}

				Document document = Jsoup.parse(html);
				String text = document.text();
				return ResKey.builder().add(defaultLocale, text).build();
			}

			@Override
			public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, ResKey value)
					throws XMLStreamException {
				_textBinding.storeValue(log, out, attribute,
					TagUtil.encodeXML(Resources.getInstance(defaultLocale).getString(value)));
			}
		};

		AttributeValueBinding<ProcessFormDefinition> formUpgrade = new AttributeValueBinding<>() {

			@SuppressWarnings("unchecked")
			AttributeValueBinding<FormDefinition> _formBinding =
				(AttributeValueBinding<FormDefinition>) fetchBinding(context,
					TLModelUtil.findType("tl.bpe.bpml:DisplayDescription"));

			@Override
			public ProcessFormDefinition loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute)
					throws XMLStreamException {
				FormDefinition form = _formBinding.loadValue(log, in, attribute);
				if (form == null) {
					return null;
				}

				ProcessFormDefinition processFormDefinition =
					TypedConfiguration.newConfigItem(ProcessFormDefinition.class);
				SpecializedForm.Config<?> specializedForm =
					TypedConfiguration.newConfigItem(SpecializedForm.Config.class);
				specializedForm.setForm(form);
				processFormDefinition.setFormProvider(specializedForm);
				return processFormDefinition;
			}

			@Override
			public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute,
					ProcessFormDefinition value) throws XMLStreamException {
				PolymorphicConfiguration<? extends FormProvider> formProviderConfig = value.getFormProvider();
				if (formProviderConfig instanceof SpecializedForm.Config<?> specializedForm) {
					_formBinding.storeValue(log, out, attribute, specializedForm.getForm());
				}
			}
		};

		addUpgrade("tl.bpe.bpml:Described#description", "descriptionI18N", internationalizeHtml);
		addUpgrade("tl.bpe.bpml:Collaboration#myTasksTitle", "myTasksTitleI18N", internationalizeText);
		addUpgrade("tl.bpe.bpml:Collaboration#myTasksDescription", "myTasksDescriptionI18N", internationalizeHtml);
		addUpgrade("tl.bpe.bpml:Collaboration#myProcessesTitle", "myProcessesTitleI18N", internationalizeText);
		addUpgrade("tl.bpe.bpml:Collaboration#myProcessesDescription", "myProcessesDescriptionI18N",
			internationalizeHtml);
		addUpgrade("tl.bpe.bpml:Task#title", "titleI18N", template2reskey);
		addUpgrade("tl.bpe.bpml:ManualTask#displayDescription", "formDefinition", formUpgrade);
		addUpgrade("tl.bpe.bpml:Participant#taskTitle", new IgnoreUpgrade());
	}

	private void addUpgrade(String oldAttr, String newAttr, AttributeValueBinding<?> binding) {
		addUpgrade(oldAttr, new TransformUpgrade(oldAttr, newAttr, binding));
	}

	private void addUpgrade(String oldAttr, Upgrade upgrade) {
		_upgrades.put(oldAttr, upgrade);
	}

	private interface Upgrade {

		public void handleUpgrade(ImportContext context, XMLStreamReader in, TLObject self)
				throws XMLStreamException;

	}

	private final class IgnoreUpgrade implements Upgrade {

		@Override
		public void handleUpgrade(ImportContext context, XMLStreamReader in, TLObject self) throws XMLStreamException {
			// Ignore.
			XMLStreamUtil.skipUpToMatchingEndTag(in);
		}
	}

	private final class TransformUpgrade implements Upgrade {
		private final String _oldAttr;

		private final String _newAttr;

		private final AttributeValueBinding<Object> _compatibilityBinding;
	
		/**
		 * Creates a {@link TransformUpgrade}.
		 */
		private TransformUpgrade(String oldAttr, String newAttr, AttributeValueBinding<?> compatibilityBinding) {
			_oldAttr = oldAttr;
			_newAttr = newAttr;
	
			@SuppressWarnings("unchecked")
			AttributeValueBinding<Object> genericBinding = (AttributeValueBinding<Object>) compatibilityBinding;
			_compatibilityBinding = genericBinding;
		}
	
		@Override
		public void handleUpgrade(ImportContext context, XMLStreamReader in, TLObject self)
				throws XMLStreamException {
			TLStructuredTypePart attribute = self.tType().getPart(_newAttr);
			if (attribute != null) {
				Logger.info("Upgrading '" + _oldAttr + "' to '" + attribute + "'.", HandleTLExtensions.class);

				Object value = _compatibilityBinding.loadValue(context, in, attribute);
				self.tUpdate(attribute, value);
			} else {
				context.error(in.getLocation(), I18NConstants.INVALID_BPML_EXTENSION__NAME.fill(_oldAttr));
				XMLStreamUtil.skipUpToMatchingEndTag(in);
			}
		}
	}

}
