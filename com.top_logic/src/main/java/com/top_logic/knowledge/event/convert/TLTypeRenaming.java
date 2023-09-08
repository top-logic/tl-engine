/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.db.RequiresRowTransformation;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.transformers.ApplicationModelTransformer;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AbstractMappingRewriter} rewriting renaming a {@link TLType}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLTypeRenaming extends AbstractMappingRewriter
		implements RequiresRowTransformation, ConfiguredInstance<TLTypeRenaming.Config> {

	private static final String TTYPE_NAME_ATTRIBUTE = "name";

	private static final String MODULE_NAME_ATTRIBUTE = "name";

	/**
	 * Typed configuration interface definition for {@link TLTypeRenaming}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("ttype-renaming")
	public interface Config extends PolymorphicConfiguration<TLTypeRenaming> {

		/**
		 * Module of the {@link TLClass} to rename.
		 */
		@Mandatory
		String getModule();

		/**
		 * Name of the {@link TLClass} to rename.
		 */
		@Mandatory
		String getOldTypeName();

		/**
		 * New name of the {@link TLClass}.
		 */
		@Mandatory
		String getNewTypeName();

	}

	private final Config _config;

	private ObjectKey _moduleID;

	private ObjectBranchId _typeID;

	private Log _log;

	/**
	 * Create a {@link TLTypeRenaming}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TLTypeRenaming(InstantiationContext context, Config config) {
		_config = config;
	}

	/**
	 * Sets the {@link Log} to write messages to.
	 */
	@Inject
	public void setLog(Log log) {
		_log = log;
	}

	@Override
	protected ChangeSet mapChangeSet(ChangeSet input) {
		fetchModuleId(input);
		renameTType(input);
		return input;
	}

	private void renameTType(ChangeSet input) {
		if (_typeID != null) {
			// type was found
			return;
		}
		if (_moduleID == null) {
			return;
		}
		_typeID = renameTType(input.getCreations());
	}

	private ObjectBranchId renameTType(List<ObjectCreation> creations) {
		for (ObjectCreation event : creations) {
			if (!ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE.equals(event.getObjectType().getName())) {
				continue;
			}
			Map<String, Object> values = event.getValues();
			if (!_config.getOldTypeName().equals(values.get(TTYPE_NAME_ATTRIBUTE))) {
				continue;
			}
			if (_moduleID.equals(values.get(ApplicationObjectUtil.META_ELEMENT_MODULE_REF))) {
				values.put(TTYPE_NAME_ATTRIBUTE, _config.getNewTypeName());
				_log.info("Renamed '" + oldQName() + "' to '" + newQName() + "'.");
				return event.getObjectId();
			}
		}
		return null;
	}

	String newTypeName() {
		return getConfig().getNewTypeName();
	}

	String module() {
		return getConfig().getModule();
	}

	String oldTypeName() {
		return getConfig().getOldTypeName();
	}

	String oldQName() {
		return TLModelUtil.qualifiedName(module(), oldTypeName());
	}

	String newQName() {
		return TLModelUtil.qualifiedName(module(), newTypeName());
	}

	private void fetchModuleId(ChangeSet input) {
		if (_moduleID != null) {
			return;
		}
		_moduleID = fetchModuleID(input.getCreations());
	}

	private ObjectKey fetchModuleID(List<ObjectCreation> creations) {
		for (ObjectCreation event : creations) {
			if (!ApplicationObjectUtil.MODULE_OBJECT_TYPE.equals(event.getObjectType().getName())) {
				continue;
			}
			Object name = event.getValues().get(MODULE_NAME_ATTRIBUTE);
			if (_config.getModule().equals(name)) {
				return event.getObjectId().toCurrentObjectKey();
			}
		}
		return null;
	}

	@Override
	public RowTransformer getRequiredTransformations() {
		return new ApplicationModelTransformer() {

			@Override
			protected Log log() {
				return _log;
			}

			@Override
			protected String updateApplicationModel(String serializedApplicationModel) {
				Document applicationModel = DOMUtil.parse(serializedApplicationModel);
				
				adaptLocalOccurrences(applicationModel);
				adaptGlobalOccurrences(applicationModel);

				return DOMUtil.toString(applicationModel);
			}

			private void adaptGlobalOccurrences(Document applicationModel) {
				XPath newXPath = XPathFactory.newInstance().newXPath();
				String globalOccurences = globalOccurences();
				NodeList typeAttrs;
				try {
					typeAttrs = (NodeList) newXPath.compile(globalOccurences).evaluate(applicationModel,
						XPathConstants.NODESET);
				} catch (XPathExpressionException ex) {
					throw new RuntimeException(
						"Unable to evaluate " + globalOccurences + " on application model " + applicationModel, ex);
				}
				String newQName = newQName();
				for (int n = typeAttrs.getLength() - 1; n >= 0; n--) {
					Attr attr = (Attr) typeAttrs.item(n);
					attr.setValue(newQName);
				}
			}

			private String globalOccurences() {
				return "//*[@type='" + oldQName() + "']/@type";
			}

			private void adaptLocalOccurrences(Document applicationModel) {
				XPath newXPath = XPathFactory.newInstance().newXPath();
				String localOccurences = localOccurences();
				NodeList typeAttrs;
				try {
					typeAttrs =
						(NodeList) newXPath.compile(localOccurences).evaluate(applicationModel, XPathConstants.NODESET);
				} catch (XPathExpressionException ex) {
					throw new RuntimeException(
						"Unable to evaluate " + localOccurences + " on application model " + applicationModel, ex);
				}
				for (int n = typeAttrs.getLength() - 1; n >= 0; n--) {
					Attr attr = (Attr) typeAttrs.item(n);
					attr.setValue(newTypeName());
				}
			}

			private String localOccurences() {
				String module = module();
				String oldTypeName = oldTypeName();
				String localTypeName = "//module[@name='" + module + "']//interface[@name='" + oldTypeName + "']/@name";
				localTypeName += "|";
				localTypeName += "//module[@name='" + module + "']//class[@name='" + oldTypeName + "']/@name";
				localTypeName += "|";
				localTypeName += "//module[@name='" + module + "']//*[@type='" + oldTypeName + "']/@type";
				return localTypeName;
			}
		};
	}

	/**
	 * @see com.top_logic.basic.config.ConfiguredInstance#getConfig()
	 */
	@Override
	public Config getConfig() {
		return _config;
	}


}

