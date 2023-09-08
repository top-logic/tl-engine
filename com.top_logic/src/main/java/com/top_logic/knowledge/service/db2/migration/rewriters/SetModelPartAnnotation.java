/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.ConfiguredRewritingEventVisitor;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.TLModelMigrationUtil;
import com.top_logic.knowledge.service.db2.migration.db.RequiresRowTransformation;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.transformers.ApplicationModelTransformer;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;

/**
 * {@link RewritingEventVisitor} setting the annotations for a certain {@link TLNamedPart}.
 * 
 * <p>
 * The rewrites ensures that for all time the annotations are the same, i.e. when the annotations
 * has been changed during the timelife of the application, the modifications are overridden.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SetModelPartAnnotation extends ConfiguredRewritingEventVisitor<SetModelPartAnnotation.Config>
		implements RequiresRowTransformation {

	private static final String ANNOTATIONS_ATTRIBUTE = PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE;

	/**
	 * Typed configuration interface definition for {@link SetModelPartAnnotation}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("set-part-annotation")
	public interface Config extends ConfiguredRewritingEventVisitor.Config<SetModelPartAnnotation> {

		/**
		 * Name of the {@link TLModule} containing the {@link TLStructuredTypePart}
		 */
		@Mandatory
		String getModule();

		/**
		 * Setter for {@link #getModule()}.
		 */
		void setModule(String module);

		/**
		 * Name of the {@link TLStructuredType} containing the {@link TLStructuredTypePart}.
		 */
		String getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(String type);

		/**
		 * Name of the {@link TLTypePart} for which the annotations must be changed.
		 */
		String getAttribute();

		/**
		 * Setter for {@link #getAttribute()}.
		 */
		void setAttribute(String attribute);

		/**
		 * New {@link TLAnnotation}s to set.
		 * 
		 * @see TLNamedPart#getAnnotations()
		 */
		AnnotationConfigs getAnnotations();

		/**
		 * Setter for {@link #getAnnotations()}.
		 */
		void setAnnotations(AnnotationConfigs annotations);

	}

	private ObjectKey _typeKey;

	private ObjectKey _moduleKey;

	private ObjectBranchId _modelPartToAdapt;

	private Log _log = new LogProtocol(SetModelPartAnnotation.class);

	/**
	 * Create a {@link SetModelPartAnnotation}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SetModelPartAnnotation(InstantiationContext context, Config config) {
		super(context, config);
		checkConfig(context, config);
	}

	private void checkConfig(InstantiationContext context, Config config) {
		String module = config.getModule();
		if (module.isEmpty()) {
			context.error("No module configured.");
		}
		String type = config.getType();
		String attribute = config.getAttribute();
		if (type.isEmpty()) {
			// No type configured, the module should be adapted.
			if (!attribute.isEmpty()) {
				context.error("Annotation for attribute '" + attribute + "' in module '" + module
					+ "' should be adapted, but no type is given.");
			}
		}
	}

	/**
	 * Sets the {@link Log} to write messages to.
	 */
	@Inject
	public void setLog(Log log) {
		_log = log;
	}

	@Override
	protected void processCreations(ChangeSet cs) {
		Config config = getConfig();
		if (_modelPartToAdapt != null) {
			return;
		} else if (_typeKey != null) {
			ObjectCreation creation =
				TLModelMigrationUtil.getTLTypePartCreation(cs, _typeKey, config.getAttribute());
			if (creation != null) {
				_modelPartToAdapt = creation.getObjectId();
				setAnnotations(creation);
				return;
			}
		} else if (_moduleKey != null) {
			ObjectCreation creation =
				TLModelMigrationUtil.getTLTypeCreation(cs, _moduleKey, config.getType());
			if (creation != null) {
				if (config.getAttribute().isEmpty()) {
					_modelPartToAdapt = creation.getObjectId();
					setAnnotations(creation);
					return;
				} else {
					_typeKey = creation.getObjectId().toCurrentObjectKey();
					processCreations(cs);
				}
			}
		} else {
			ObjectCreation creation =
				TLModelMigrationUtil.getTLModuleCreation(cs, config.getModule());
			if (creation != null) {
				if (config.getType().isEmpty()) {
					_modelPartToAdapt = creation.getObjectId();
					setAnnotations(creation);
					return;
				} else {
					_moduleKey = creation.getObjectId().toCurrentObjectKey();
					processCreations(cs);
				}
			}
		}
	}

	private void setAnnotations(ObjectCreation creation) {
		_log.info("Update annotations of " + creation);
		creation.getValues().put(ANNOTATIONS_ATTRIBUTE, getConfig().getAnnotations());
	}

	@Override
	public Object visitUpdate(ItemUpdate event, Void arg) {
		if (_modelPartToAdapt == null || !_modelPartToAdapt.equals(event.getObjectId())) {
			return super.visitUpdate(event, arg);
		}
		Map<String, Object> values = event.getValues();
		if (values.containsKey(ANNOTATIONS_ATTRIBUTE)) {
			values.remove(ANNOTATIONS_ATTRIBUTE);
			Map<String, Object> oldValues = event.getOldValues();
			if (oldValues != null) {
				oldValues.remove(ANNOTATIONS_ATTRIBUTE);
			}
		}
		return super.visitUpdate(event, arg);
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
				XPathExpression xPath;
				try {
					xPath = XPathFactory.newInstance().newXPath().compile(annotationsXPath());
				} catch (XPathExpressionException ex) {
					throw new RuntimeException("Invalid XPath: " + annotationsXPath(), ex);
				}
				Document applicationModel = DOMUtil.parse(serializedApplicationModel);

				NodeList oldAnnotationsNode;
				try {
					oldAnnotationsNode = (NodeList) xPath.evaluate(applicationModel, XPathConstants.NODESET);
				} catch (XPathExpressionException ex) {
					throw new RuntimeException("Unable to evaluate " + annotationsXPath() + " on " + applicationModel,
						ex);
				}
				Element newAnnotationsNode = newAttributesNode();
				for (int n = oldAnnotationsNode.getLength() - 1; n >= 0; n--) {
					Element annotationNode = (Element) oldAnnotationsNode.item(n);
					Node importedNode = applicationModel.importNode(newAnnotationsNode, true);
					annotationNode.getParentNode().replaceChild(importedNode, annotationNode);
				}

				return DOMUtil.toString(applicationModel);
			}

			private Element newAttributesNode() {
				String annotations = TypedConfiguration.toString(getConfig().getAnnotations());
				Element annotationsNode =
					(Element) DOMUtil.parse(annotations).getDocumentElement().getElementsByTagName("annotations")
						.item(0);
				return annotationsNode;
			}

			private String annotationsXPath() {
				List<String> modulePaths = new ArrayList<>();
				modulePaths.add("//module[@name='" + getConfig().getModule() + "']");
				List<String> paths = modulePaths;
				if (!getConfig().getType().isEmpty()) {
					List<String> typePaths = new ArrayList<>();
					for (String s : paths) {
						typePaths.add(s + "//interface[@name='" + getConfig().getType() + "']");
						typePaths.add(s + "//class[@name='" + getConfig().getType() + "']");
					}
					paths = typePaths;
					if (!getConfig().getAttribute().isEmpty()) {
						List<String> attributePaths = new ArrayList<>();
						for (String s : paths) {
							attributePaths.add(s + "//reference[@name='" + getConfig().getAttribute() + "']");
							attributePaths.add(s + "//property[@name='" + getConfig().getAttribute() + "']");
						}
						paths = attributePaths;
					}
				}
				return paths.stream().map(path -> path + "/annotations").collect(Collectors.joining(" | "));
			}
		};
	}


}
