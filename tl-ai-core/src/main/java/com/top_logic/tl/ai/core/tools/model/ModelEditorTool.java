/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.tools.model.messages.BackwardReferenceResponse;
import com.top_logic.tl.ai.core.tools.model.messages.ClassResponse;
import com.top_logic.tl.ai.core.tools.model.messages.DeleteResponse;
import com.top_logic.tl.ai.core.tools.model.messages.DeletionBlocker;
import com.top_logic.tl.ai.core.tools.model.messages.EnumResponse;
import com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse;
import com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse;
import com.top_logic.tl.ai.core.tools.model.messages.ReferenceResponse;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.core.tools.util.ToolI18NUtil;
import com.top_logic.tl.ai.core.tools.util.ToolI18NUtil.LocalizedTexts;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.util.model.ModelService;

/**
 * Tool for creating and updating TopLogic model elements.
 *
 * <p>
 * This tool provides methods for creating and updating modules, classes, properties, references,
 * backward references, and enumerations in the TopLogic application model.
 * </p>
 *
 * <p>
 * All methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 */
public class ModelEditorTool {

	/**
	 * Configuration for {@link ModelEditorTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelEditorTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link ModelEditorTool}.
	 */
	public ModelEditorTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link ModelEditorTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ModelEditorTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9_]*$");

	private static final Pattern PROPERTY_NAME_PATTERN = Pattern.compile(
		"^[\\p{L}_](?:[\\p{L}0-9._-]*[\\p{L}0-9_-])?$");

	private static final Pattern ENUM_NAME_PATTERN = Pattern.compile(
		"^\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:\\.\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*$");

	private static final Pattern CLASSIFIER_NAME_PATTERN = Pattern.compile(
		"^\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:[\\.\\-]\\p{javaJavaIdentifierPart}+)*$");

	// ===== Module Operations =====

	/**
	 * Creates a new TopLogic module or returns an existing one by name.
	 *
	 * @param moduleName
	 *        Name of the module to create.
	 * @param labelEn
	 *        English label for the module (optional).
	 * @param labelDe
	 *        German label for the module (optional).
	 * @param descEn
	 *        English description for the module (optional).
	 * @param descDe
	 *        German description for the module (optional).
	 * @return JSON with module details.
	 */
	@ToolMethod(description = "Create a new TopLogic module or get an existing one by name")
	public String createModule(
			@P("Name of the module to create") String moduleName,
			@P("English label for the module") String labelEn,
			@P("German label for the module") String labelDe,
			@P("English description for the module") String descEn,
			@P("German description for the module") String descDe) {

		try {
			ModuleResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateModule(moduleName, labelEn, labelDe, descEn, descDe));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "createModule()",
				buildArgs("moduleName", moduleName));
		} catch (Exception ex) {
			Logger.error("createModule() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create module: " + ex.getMessage(),
				ModelEditorTool.class, "createModule()",
				buildArgs("moduleName", moduleName));
		}
	}

	private ModuleResponse doCreateModule(String moduleName, String labelEn, String labelDe,
			String descEn, String descDe) {

		if (moduleName == null || moduleName.trim().isEmpty()) {
			throw new IllegalArgumentException("Module name is required");
		}

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		boolean created = false;

		if (module == null) {
			LocalizedTexts i18n = ToolI18NUtil.create(labelEn, labelDe, descEn, descDe);

			try (Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Module"))) {
				module = TLModelUtil.addModule(model, moduleName);
				created = true;
				tx.commit();
			}

			ToolI18NUtil.applyIfPresent(module, i18n);
		}

		ModuleResponse response = ModuleResponse.create();
		response.setSuccess(true);
		response.setName(module.getName());
		response.setTid(module.tId().asString());
		response.setCreated(created);
		return response;
	}

	/**
	 * Updates internationalized labels and descriptions for an existing TopLogic module.
	 *
	 * @param moduleName
	 *        Name of the module to update.
	 * @param labelEn
	 *        English label for the module (optional).
	 * @param labelDe
	 *        German label for the module (optional).
	 * @param descEn
	 *        English description for the module (optional).
	 * @param descDe
	 *        German description for the module (optional).
	 * @return JSON with updated module details.
	 */
	@ToolMethod(description = "Update internationalized labels and descriptions for an existing TopLogic module")
	public String updateModule(
			@P("Name of the module to update") String moduleName,
			@P("English label for the module") String labelEn,
			@P("German label for the module") String labelDe,
			@P("English description for the module") String descEn,
			@P("German description for the module") String descDe) {

		try {
			ModuleResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doUpdateModule(moduleName, labelEn, labelDe, descEn, descDe));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "updateModule()",
				buildArgs("moduleName", moduleName));
		} catch (Exception ex) {
			Logger.error("updateModule() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to update module: " + ex.getMessage(),
				ModelEditorTool.class, "updateModule()",
				buildArgs("moduleName", moduleName));
		}
	}

	private ModuleResponse doUpdateModule(String moduleName, String labelEn, String labelDe,
			String descEn, String descDe) {

		if (moduleName == null || moduleName.trim().isEmpty()) {
			throw new IllegalArgumentException("Module name is required");
		}

		LocalizedTexts i18n = ToolI18NUtil.create(labelEn, labelDe, descEn, descDe);
		if (!i18n.hasAny()) {
			throw new IllegalArgumentException(
				"No update data provided. At least one of label or description must be specified");
		}

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}

		ToolI18NUtil.applyIfPresent(module, i18n);

		ModuleResponse response = ModuleResponse.create();
		response.setSuccess(true);
		response.setName(module.getName());
		response.setTid(module.tId().asString());
		response.setCreated(false);
		return response;
	}

	// ===== Class Operations =====

	/**
	 * Creates a new TopLogic class in an existing module or returns an existing one by name.
	 *
	 * @param moduleName
	 *        Name of the module where the class will be created.
	 * @param className
	 *        Name of the class. Must start with uppercase.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param isAbstract
	 *        Whether abstract (default: false).
	 * @param isFinal
	 *        Whether final (default: false).
	 * @param generalizations
	 *        Comma-separated names of parent classes (optional).
	 * @return JSON with class details.
	 */
	@ToolMethod(description = "Create a new TopLogic class in an existing module or get an existing one by name")
	public String createClass(
			@P("Name of the module where the class will be created") String moduleName,
			@P("Name of the class to create. Must start with an uppercase letter.") String className,
			@P("English label for the class") String labelEn,
			@P("German label for the class") String labelDe,
			@P("English description for the class") String descEn,
			@P("German description for the class") String descDe,
			@P("Whether the class is abstract (default: false)") Boolean isAbstract,
			@P("Whether the class is final (default: false)") Boolean isFinal,
			@P("Names of the classes this class directly extends, as a JSON array string") String generalizations) {

		try {
			ClassResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateClass(moduleName, className, labelEn, labelDe, descEn, descDe,
					isAbstract, isFinal, generalizations));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "createClass()",
				buildArgs("moduleName", moduleName, "className", className));
		} catch (Exception ex) {
			Logger.error("createClass() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create class: " + ex.getMessage(),
				ModelEditorTool.class, "createClass()",
				buildArgs("moduleName", moduleName, "className", className));
		}
	}

	private ClassResponse doCreateClass(String moduleName, String className,
			String labelEn, String labelDe, String descEn, String descDe,
			Boolean isAbstract, Boolean isFinal, String generalizations) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");
		validateClassName(className);

		boolean abstractFlag = isAbstract != null && isAbstract;
		boolean finalFlag = isFinal != null && isFinal;

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}

		TLType existingType = module.getType(className);
		TLClass tlClass;
		boolean created = false;

		if (existingType != null) {
			if (!(existingType instanceof TLClass)) {
				throw new IllegalArgumentException(
					"Type '" + className + "' exists in module '" + moduleName + "' but is not a class");
			}
			tlClass = (TLClass) existingType;
		} else {
			try (Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Class"))) {
				tlClass = TLModelUtil.addClass(module, className);
				tlClass.setAbstract(abstractFlag);
				tlClass.setFinal(finalFlag);
				List<TLClass> finalGeneralizations =
					resolveAndEnsureGeneralizations(generalizations, module, model, tlClass);
				tlClass.getGeneralizations().addAll(finalGeneralizations);
				created = true;
				tx.commit();
			}

			ToolI18NUtil.applyIfPresent(tlClass, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));
		}

		ClassResponse response = ClassResponse.create();
		response.setSuccess(true);
		response.setName(tlClass.getName());
		response.setTid(tlClass.tId().asString());
		response.setCreated(created);
		return response;
	}

	/**
	 * Updates properties, flags, and inheritance of an existing TopLogic class.
	 *
	 * @param moduleName
	 *        Name of the module containing the class.
	 * @param className
	 *        Name of the class to update.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param isAbstract
	 *        Whether abstract (optional).
	 * @param isFinal
	 *        Whether final (optional).
	 * @param generalizations
	 *        Comma-separated names of parent classes (optional).
	 * @return JSON with updated class details.
	 */
	@ToolMethod(description = "Update properties, flags, and inheritance of an existing TopLogic class")
	public String updateClass(
			@P("Name of the module containing the class") String moduleName,
			@P("Name of the class to update") String className,
			@P("English label for the class") String labelEn,
			@P("German label for the class") String labelDe,
			@P("English description for the class") String descEn,
			@P("German description for the class") String descDe,
			@P("Whether the class is abstract") Boolean isAbstract,
			@P("Whether the class is final") Boolean isFinal,
			@P("Names of the classes this class directly extends, as a JSON array string") String generalizations) {

		try {
			ClassResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doUpdateClass(moduleName, className, labelEn, labelDe, descEn, descDe,
					isAbstract, isFinal, generalizations));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "updateClass()",
				buildArgs("moduleName", moduleName, "className", className));
		} catch (Exception ex) {
			Logger.error("updateClass() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to update class: " + ex.getMessage(),
				ModelEditorTool.class, "updateClass()",
				buildArgs("moduleName", moduleName, "className", className));
		}
	}

	private ClassResponse doUpdateClass(String moduleName, String className,
			String labelEn, String labelDe, String descEn, String descDe,
			Boolean isAbstract, Boolean isFinal, String generalizations) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}

		TLType existingType = module.getType(className);
		if (existingType == null) {
			throw new IllegalArgumentException(
				"Class '" + className + "' does not exist in module '" + moduleName + "'");
		}
		if (!(existingType instanceof TLClass tlClass)) {
			throw new IllegalArgumentException(
				"Type '" + className + "' exists in module '" + moduleName + "' but is not a class");
		}

		List<TLClass> resolved = null;
		if (generalizations != null && !generalizations.trim().isEmpty()) {
			resolved = resolveAndEnsureGeneralizations(generalizations, module, model, tlClass);
		}

		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Update Class"))) {

			if (isAbstract != null) {
				tlClass.setAbstract(isAbstract);
			}
			if (isFinal != null) {
				tlClass.setFinal(isFinal);
			}
			if (resolved != null) {
				tlClass.getGeneralizations().clear();
				tlClass.getGeneralizations().addAll(resolved);
			}

			tx.commit();
		}

		ToolI18NUtil.applyIfPresent(tlClass, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));

		ClassResponse response = ClassResponse.create();
		response.setSuccess(true);
		response.setName(tlClass.getName());
		response.setTid(tlClass.tId().asString());
		response.setCreated(false);
		return response;
	}

	// ===== Property Operations =====

	/**
	 * Creates a new property on a TopLogic class with primitive types, or returns an existing one.
	 *
	 * @param moduleName
	 *        Name of the module containing the class.
	 * @param className
	 *        Name of the class.
	 * @param propertyName
	 *        Name of the property.
	 * @param propertyType
	 *        The datatype name (e.g. String, Text, Integer, Long, Double, Boolean, Date).
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param mandatory
	 *        Whether required (default: false).
	 * @param multiple
	 *        Whether multi-valued (default: false).
	 * @param ordered
	 *        Whether ordered (default: false).
	 * @param bag
	 *        Whether duplicates allowed (default: false).
	 * @param isAbstract
	 *        Whether abstract (default: false).
	 * @return JSON with property details.
	 */
	@ToolMethod(description = "Create a new PRIMITIVE property on a TopLogic class, or return an existing one by name. Supports all datatypes available in the model (e.g. String, Text, Integer, Double, Date, Color, URL). NOT for enumerations or references to other types — use createReference for those.")
	public String createProperty(
			@P("Name of the module containing the class") String moduleName,
			@P("Name of the class to add the property to") String className,
			@P("Name of the property") String propertyName,
			@P("The datatype name (e.g. String, Text, Integer, Long, Double, Boolean, Date, Day, DateTime, Color, URL). Supports all datatypes from any module. Use qualified name (e.g. tl.util:Color) if ambiguous. Aliases: int=Integer, bool=Boolean. Case-insensitive. NOT for enumerations or references — use createReference instead.") String propertyType,
			@P("English label for the property") String labelEn,
			@P("German label for the property") String labelDe,
			@P("English description for the property") String descEn,
			@P("German description for the property") String descDe,
			@P("Whether the property is required (default: false)") Boolean mandatory,
			@P("Whether the property can hold multiple values (default: false)") Boolean multiple,
			@P("Whether order matters for multiple-valued properties (default: false)") Boolean ordered,
			@P("Whether duplicates are allowed for multiple-valued properties (default: false)") Boolean bag,
			@P("Whether this attribute must be overridden in specific classes (default: false)") Boolean isAbstract) {

		try {
			PropertyResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateProperty(moduleName, className, propertyName, propertyType,
					labelEn, labelDe, descEn, descDe, mandatory, multiple, ordered, bag, isAbstract));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "createProperty()",
				buildArgs("moduleName", moduleName, "className", className, "propertyName", propertyName));
		} catch (Exception ex) {
			Logger.error("createProperty() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create property: " + ex.getMessage(),
				ModelEditorTool.class, "createProperty()",
				buildArgs("moduleName", moduleName, "className", className, "propertyName", propertyName));
		}
	}

	private PropertyResponse doCreateProperty(String moduleName, String className,
			String propertyName, String propertyType, String labelEn, String labelDe,
			String descEn, String descDe, Boolean mandatory, Boolean multiple,
			Boolean ordered, Boolean bag, Boolean isAbstract) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");
		requireNonEmpty(propertyName, "Property name");
		requireNonEmpty(propertyType, "Property type");
		validatePropertyName(propertyName);

		boolean mandatoryFlag = mandatory != null && mandatory;
		boolean multipleFlag = multiple != null && multiple;
		boolean orderedFlag = ordered != null && ordered;
		boolean bagFlag = bag != null && bag;
		boolean abstractFlag = isAbstract != null && isAbstract;

		TLModel model = ModelService.getApplicationModel();
		TLClass tlClass = resolveClass(model, moduleName, className);

		TLStructuredTypePart existingPart = tlClass.getPart(propertyName);
		TLClassProperty property;
		boolean created = false;

		if (existingPart != null) {
			if (!(existingPart instanceof TLClassProperty)) {
				throw new IllegalArgumentException(
					"Part '" + propertyName + "' already exists on class '" + className + "' but is not a property");
			}
			property = (TLClassProperty) existingPart;
		} else {
			TLType resolvedType = resolvePrimitiveType(model, propertyType);

			try (Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Property"))) {
				property = TLModelUtil.addProperty(tlClass, propertyName, resolvedType);
				property.setMandatory(mandatoryFlag);
				property.setMultiple(multipleFlag);
				property.setOrdered(orderedFlag);
				property.setBag(bagFlag);
				property.setAbstract(abstractFlag);
				created = true;
				tx.commit();
			}

			ToolI18NUtil.applyIfPresent(property, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));
		}

		PropertyResponse response = PropertyResponse.create();
		response.setSuccess(true);
		response.setName(property.getName());
		response.setTid(property.tId().asString());
		response.setCreated(created);
		return response;
	}

	/**
	 * Updates attributes and internationalization of an existing TopLogic class property.
	 *
	 * @param moduleName
	 *        Name of the module containing the class.
	 * @param className
	 *        Name of the class containing the property.
	 * @param propertyName
	 *        Name of the property to update.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param mandatory
	 *        Whether required (optional).
	 * @param multiple
	 *        Whether multi-valued (optional).
	 * @param ordered
	 *        Whether ordered (optional).
	 * @param bag
	 *        Whether duplicates allowed (optional).
	 * @param isAbstract
	 *        Whether abstract (optional).
	 * @return JSON with updated property details.
	 */
	@ToolMethod(description = "Update attributes and internationalization of an existing TopLogic class property")
	public String updateProperty(
			@P("Name of the module containing the class") String moduleName,
			@P("Name of the class containing the property") String className,
			@P("Name of the property to update") String propertyName,
			@P("English label for the property") String labelEn,
			@P("German label for the property") String labelDe,
			@P("English description for the property") String descEn,
			@P("German description for the property") String descDe,
			@P("Whether the property is required") Boolean mandatory,
			@P("Whether the property can hold multiple values") Boolean multiple,
			@P("Whether order matters for multiple-valued properties") Boolean ordered,
			@P("Whether duplicates are allowed for multiple-valued properties") Boolean bag,
			@P("Whether this attribute must be overridden in specific classes") Boolean isAbstract) {

		try {
			PropertyResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doUpdateProperty(moduleName, className, propertyName,
					labelEn, labelDe, descEn, descDe, mandatory, multiple, ordered, bag, isAbstract));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "updateProperty()",
				buildArgs("moduleName", moduleName, "className", className, "propertyName", propertyName));
		} catch (Exception ex) {
			Logger.error("updateProperty() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to update property: " + ex.getMessage(),
				ModelEditorTool.class, "updateProperty()",
				buildArgs("moduleName", moduleName, "className", className, "propertyName", propertyName));
		}
	}

	private PropertyResponse doUpdateProperty(String moduleName, String className, String propertyName,
			String labelEn, String labelDe, String descEn, String descDe,
			Boolean mandatory, Boolean multiple, Boolean ordered, Boolean bag, Boolean isAbstract) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");
		requireNonEmpty(propertyName, "Property name");

		TLModel model = ModelService.getApplicationModel();
		TLClass tlClass = resolveClass(model, moduleName, className);

		TLStructuredTypePart existingPart = tlClass.getPart(propertyName);
		if (existingPart == null) {
			throw new IllegalArgumentException(
				"Property '" + propertyName + "' does not exist in class '" + className + "'");
		}

		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Update Property"))) {

			if (mandatory != null) {
				existingPart.setMandatory(mandatory);
			}
			if (multiple != null) {
				existingPart.setMultiple(multiple);
			}
			if (ordered != null) {
				existingPart.setOrdered(ordered);
			}
			if (bag != null) {
				existingPart.setBag(bag);
			}
			if (isAbstract != null) {
				existingPart.setAbstract(isAbstract);
			}

			tx.commit();
		}

		ToolI18NUtil.applyIfPresent(existingPart, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));

		PropertyResponse response = PropertyResponse.create();
		response.setSuccess(true);
		response.setName(existingPart.getName());
		response.setTid(existingPart.tId().asString());
		response.setCreated(false);
		return response;
	}

	// ===== Reference Operations =====

	/**
	 * Creates a new reference on a TopLogic class pointing to another class or enumeration, or
	 * returns existing.
	 *
	 * @param moduleName
	 *        Name of the module containing the source class.
	 * @param className
	 *        Name of the source class.
	 * @param referenceName
	 *        Name of the reference.
	 * @param targetModule
	 *        Name of the module containing the target class.
	 * @param targetClass
	 *        Name of the target class or enumeration.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param mandatory
	 *        Whether required (default: false).
	 * @param multiple
	 *        Whether multi-valued (default: false).
	 * @param ordered
	 *        Whether ordered (default: false).
	 * @param bag
	 *        Whether duplicates allowed (default: false).
	 * @param isAbstract
	 *        Whether abstract (default: false).
	 * @param composite
	 *        Whether composite part (default: false).
	 * @param aggregate
	 *        Whether aggregate part (default: false).
	 * @param navigate
	 *        Whether navigable (default: true).
	 * @param historyType
	 *        History type: CURRENT, HISTORIC, or MIXED (default: CURRENT).
	 * @param deletionPolicy
	 *        Deletion policy: CLEAR_REFERENCE, DELETE_REFERER, STABILISE_REFERENCE, or VETO.
	 * @return JSON with reference details.
	 */
	@ToolMethod(description = "Create a new reference on a TopLogic class pointing to another class or enumeration, or return an existing one by name")
	public String createReference(
			@P("Name of the module containing the source class") String moduleName,
			@P("Name of the source class to add the reference to") String className,
			@P("Name of the reference") String referenceName,
			@P("Name of the module containing the target class or enumeration") String targetModule,
			@P("Name of the target class or enumeration the reference points to") String targetClass,
			@P("English label for the reference") String labelEn,
			@P("German label for the reference") String labelDe,
			@P("English description for the reference") String descEn,
			@P("German description for the reference") String descDe,
			@P("Whether the reference is required (default: false)") Boolean mandatory,
			@P("Whether the reference can hold multiple values (default: false)") Boolean multiple,
			@P("Whether order matters for multiple-valued references (default: false)") Boolean ordered,
			@P("Whether duplicates are allowed (default: false)") Boolean bag,
			@P("Whether this reference must be overridden (default: false)") Boolean isAbstract,
			@P("Whether this reference points to a composite part (default: false)") Boolean composite,
			@P("Whether this reference points to an aggregate part (default: false)") Boolean aggregate,
			@P("Whether navigation is efficient for this reference (default: true)") Boolean navigate,
			@P("Type of history: CURRENT (default), HISTORIC, or MIXED") String historyType,
			@P("Deletion policy: CLEAR_REFERENCE (default), DELETE_REFERER, STABILISE_REFERENCE, or VETO") String deletionPolicy) {

		try {
			ReferenceResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateReference(moduleName, className, referenceName,
					targetModule, targetClass,
					labelEn, labelDe, descEn, descDe,
					mandatory, multiple, ordered, bag, isAbstract,
					composite, aggregate, navigate, historyType, deletionPolicy));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "createReference()",
				buildArgs("moduleName", moduleName, "className", className, "referenceName", referenceName));
		} catch (Exception ex) {
			Logger.error("createReference() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create reference: " + ex.getMessage(),
				ModelEditorTool.class, "createReference()",
				buildArgs("moduleName", moduleName, "className", className, "referenceName", referenceName));
		}
	}

	private ReferenceResponse doCreateReference(String moduleName, String className, String referenceName,
			String targetModule, String targetClass,
			String labelEn, String labelDe, String descEn, String descDe,
			Boolean mandatory, Boolean multiple, Boolean ordered, Boolean bag, Boolean isAbstract,
			Boolean composite, Boolean aggregate, Boolean navigate,
			String historyType, String deletionPolicy) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");
		requireNonEmpty(referenceName, "Reference name");
		requireNonEmpty(targetModule, "Target module name");
		requireNonEmpty(targetClass, "Target class name");
		validatePropertyName(referenceName);

		boolean mandatoryFlag = mandatory != null && mandatory;
		boolean multipleFlag = multiple != null && multiple;
		boolean orderedFlag = ordered != null && ordered;
		boolean bagFlag = bag != null && bag;
		boolean abstractFlag = isAbstract != null && isAbstract;
		boolean compositeFlag = composite != null && composite;
		boolean aggregateFlag = aggregate != null && aggregate;
		boolean navigateFlag = navigate == null || navigate;

		HistoryType resolvedHistoryType = resolveHistoryType(historyType);
		DeletionPolicy resolvedDeletionPolicy = resolveDeletionPolicy(deletionPolicy);

		TLModel model = ModelService.getApplicationModel();
		TLClass sourceClass = resolveClass(model, moduleName, className);

		TLModule resolvedTargetModule = model.getModule(targetModule);
		if (resolvedTargetModule == null) {
			throw new IllegalArgumentException("Target module '" + targetModule + "' does not exist");
		}
		TLType targetType = resolvedTargetModule.getType(targetClass);
		if (targetType == null) {
			throw new IllegalArgumentException(
				"Target class '" + targetClass + "' does not exist in module '" + targetModule + "'");
		}
		if (!(targetType instanceof TLClass) && !(targetType instanceof TLEnumeration)) {
			throw new IllegalArgumentException(
				"Target type '" + targetClass + "' is not a class or enumeration");
		}

		// Check if reference already exists
		TLStructuredTypePart existingPart = sourceClass.getPart(referenceName);
		if (existingPart != null) {
			if (!(existingPart instanceof TLReference existingRef)) {
				throw new IllegalArgumentException(
					"Part '" + referenceName + "' already exists on class '" + className + "' but is not a reference");
			}
			return buildReferenceResponse(existingRef, false);
		}

		TLReference reference;
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Reference"))) {

			// Create association with backward end first (Index 0), forward end second (Index 1)
			String associationName = sourceClass.getName() + "$" + referenceName;
			TLAssociation association = TLModelUtil.addAssociation(sourceClass.getModule(), associationName);

			// Backward end (Index 0) - non-navigable by default
			TLAssociationEnd backwardEnd = TLModelUtil.addEnd(association, "source", sourceClass);
			backwardEnd.setNavigate(false);
			backwardEnd.setMultiple(true);

			// Forward end (Index 1)
			TLAssociationEnd forwardEnd = TLModelUtil.addEnd(association, "dest", targetType);
			forwardEnd.setNavigate(navigateFlag);
			forwardEnd.setMandatory(mandatoryFlag);
			forwardEnd.setMultiple(multipleFlag);
			forwardEnd.setOrdered(orderedFlag);
			forwardEnd.setBag(bagFlag);
			forwardEnd.setAbstract(abstractFlag);
			forwardEnd.setComposite(compositeFlag);
			forwardEnd.setAggregate(aggregateFlag);
			forwardEnd.setHistoryType(resolvedHistoryType);
			forwardEnd.setDeletionPolicy(resolvedDeletionPolicy);

			// Create TLReference on source class pointing to forward end
			reference = TLModelUtil.addReference(sourceClass, referenceName, forwardEnd);

			tx.commit();
		}

		ToolI18NUtil.applyIfPresent(reference, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));

		return buildReferenceResponse(reference, true);
	}

	// ===== Backward Reference Operations =====

	/**
	 * Creates a backward reference to an existing forward reference.
	 *
	 * @param moduleName
	 *        Name of the module containing the source class that has the forward reference.
	 * @param className
	 *        Name of the source class that has the forward reference.
	 * @param forwardReferenceName
	 *        Name of the existing forward reference.
	 * @param backwardReferenceName
	 *        Name for the new backward reference.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param navigate
	 *        Whether navigable (default: true).
	 * @return JSON with backward reference details.
	 */
	@ToolMethod(description = "Create a backward reference to an existing forward reference, or return an existing one by name")
	public String createBackwardReference(
			@P("Name of the module containing the source class (that has the forward reference)") String moduleName,
			@P("Name of the source class that has the forward reference") String className,
			@P("Name of the existing forward reference") String forwardReferenceName,
			@P("Name for the new backward reference") String backwardReferenceName,
			@P("English label for the backward reference") String labelEn,
			@P("German label for the backward reference") String labelDe,
			@P("English description for the backward reference") String descEn,
			@P("German description for the backward reference") String descDe,
			@P("Whether the backward reference is navigable (default: true)") Boolean navigate) {

		try {
			BackwardReferenceResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateBackwardReference(moduleName, className,
					forwardReferenceName, backwardReferenceName,
					labelEn, labelDe, descEn, descDe, navigate));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "createBackwardReference()",
				buildArgs("moduleName", moduleName, "className", className,
					"forwardReferenceName", forwardReferenceName,
					"backwardReferenceName", backwardReferenceName));
		} catch (Exception ex) {
			Logger.error("createBackwardReference() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create backward reference: " + ex.getMessage(),
				ModelEditorTool.class, "createBackwardReference()",
				buildArgs("moduleName", moduleName, "className", className));
		}
	}

	private BackwardReferenceResponse doCreateBackwardReference(String moduleName, String className,
			String forwardReferenceName, String backwardReferenceName,
			String labelEn, String labelDe, String descEn, String descDe, Boolean navigate) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");
		requireNonEmpty(forwardReferenceName, "Forward reference name");
		requireNonEmpty(backwardReferenceName, "Backward reference name");

		boolean navigateFlag = navigate == null || navigate;

		TLModel model = ModelService.getApplicationModel();
		TLClass sourceClass = resolveClass(model, moduleName, className);

		// Find forward reference
		TLStructuredTypePart existingPart = sourceClass.getPart(forwardReferenceName);
		if (existingPart == null) {
			throw new IllegalArgumentException(
				"Forward reference '" + forwardReferenceName + "' does not exist in class '" + className + "'");
		}
		if (!(existingPart instanceof TLReference forwardRef)) {
			throw new IllegalArgumentException(
				"'" + forwardReferenceName + "' exists in class '" + className + "' but is not a reference");
		}

		// Get target class
		TLAssociationEnd forwardEnd = forwardRef.getEnd();
		TLType targetType = forwardEnd.getType();
		if (!(targetType instanceof TLClass targetClass)) {
			throw new IllegalArgumentException(
				"Target type of forward reference must be a class");
		}

		// Check if backward reference already exists
		TLStructuredTypePart existingBackwardPart = targetClass.getPart(backwardReferenceName);
		if (existingBackwardPart != null) {
			if (!(existingBackwardPart instanceof TLReference backwardRef)) {
				throw new IllegalArgumentException(
					"Part '" + backwardReferenceName + "' already exists on class '" + targetClass.getName()
						+ "' but is not a reference");
			}
			return buildBackwardReferenceResponse(backwardRef, false);
		}

		// Get backward end
		TLAssociationEnd backwardEnd = TLModelUtil.getOtherEnd(forwardEnd);

		if (backwardEnd.getReference() != null) {
			throw new IllegalArgumentException(
				"Backward end already has a reference: " + backwardEnd.getReference().getName());
		}

		if (backwardEnd.getType() != sourceClass) {
			throw new IllegalArgumentException(
				"Backward end does not point to source class. Expected: " + sourceClass.getName()
					+ ", but points to: " + backwardEnd.getType().getName());
		}

		TLReference backwardRef;
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Backward Reference"))) {

			backwardRef = TLModelUtil.addReference(targetClass, backwardReferenceName, backwardEnd);
			backwardEnd.setNavigate(navigateFlag);

			// Copy multiplicity from forward reference
			backwardRef.setMandatory(forwardRef.isMandatory());
			backwardRef.setMultiple(forwardRef.isMultiple());
			backwardRef.setOrdered(forwardRef.isOrdered());
			backwardRef.setBag(forwardRef.isBag());
			backwardRef.setAbstract(forwardRef.isAbstract());

			tx.commit();
		}

		ToolI18NUtil.applyIfPresent(backwardRef, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));

		return buildBackwardReferenceResponse(backwardRef, true);
	}

	// ===== Enumeration Operations =====

	/**
	 * Creates a new TopLogic enumeration with optional classifiers, or returns an existing one.
	 *
	 * @param moduleName
	 *        Name of the module.
	 * @param enumName
	 *        Name of the enumeration.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param classifiersJson
	 *        JSON array of classifier objects. Each with: <code>classifierName</code>, default (boolean),
	 *        label (object with en/de), description (object with en/de). Pass null for none.
	 * @return JSON with enumeration details.
	 */
	@ToolMethod(description = "Create a new TopLogic enumeration with optional classifiers, or get an existing one by name")
	public String createEnumeration(
			@P("Name of the module where the enumeration will be created") String moduleName,
			@P("Name of the enumeration. Must be a valid Java identifier and may contain dots.") String enumName,
			@P("English label for the enumeration") String labelEn,
			@P("German label for the enumeration") String labelDe,
			@P("English description for the enumeration") String descEn,
			@P("German description for the enumeration") String descDe,
			@P("JSON array of classifiers, each with: classifierName (required), default (boolean), label.en, label.de, description.en, description.de") String classifiersJson) {

		try {
			EnumResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateEnumeration(moduleName, enumName, labelEn, labelDe, descEn, descDe, classifiersJson));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "createEnumeration()",
				buildArgs("moduleName", moduleName, "enumName", enumName));
		} catch (Exception ex) {
			Logger.error("createEnumeration() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create enumeration: " + ex.getMessage(),
				ModelEditorTool.class, "createEnumeration()",
				buildArgs("moduleName", moduleName, "enumName", enumName));
		}
	}

	@SuppressWarnings("unchecked")
	private EnumResponse doCreateEnumeration(String moduleName, String enumName,
			String labelEn, String labelDe, String descEn, String descDe,
			String classifiersJson) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(enumName, "Enumeration name");
		validateEnumName(enumName);

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}

		// Check if already exists
		TLType existingType = module.getType(enumName);
		if (existingType != null) {
			if (!(existingType instanceof TLEnumeration existingEnum)) {
				throw new IllegalArgumentException(
					"Type '" + enumName + "' exists but is not an enumeration");
			}
			return buildEnumResponse(existingEnum, false);
		}

		// Parse classifiers from JSON
		List<Map<String, Object>> classifierDataList = new ArrayList<>();
		if (classifiersJson != null && !classifiersJson.trim().isEmpty()) {
			try {
				Object parsed = com.top_logic.basic.json.JSON.fromString(classifiersJson);
				if (parsed instanceof List<?> list) {
					for (Object item : list) {
						if (item instanceof Map) {
							classifierDataList.add((Map<String, Object>) item);
						}
					}
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid classifiers JSON: " + ex.getMessage());
			}
		}

		// Validate classifiers
		Set<String> classifierNames = new HashSet<>();
		String defaultName = null;
		for (Map<String, Object> data : classifierDataList) {
			String name = (String) data.get("classifierName");
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Classifier name is required");
			}
			validateClassifierName(name);
			if (!classifierNames.add(name)) {
				throw new IllegalArgumentException("Duplicate classifier name: '" + name + "'");
			}
			Object isDefault = data.get("default");
			if (Boolean.TRUE.equals(isDefault)) {
				if (defaultName != null) {
					throw new IllegalArgumentException(
						"Only one classifier can be marked as default. Found: '" + defaultName + "' and '" + name + "'");
				}
				defaultName = name;
			}
		}

		TLEnumeration enumeration;
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Create Enumeration"))) {

			enumeration = TLModelUtil.addEnumeration(module, enumName);

			for (Map<String, Object> data : classifierDataList) {
				String name = (String) data.get("classifierName");
				TLClassifier classifier = TLModelUtil.addClassifier(enumeration, name);
				Object isDefault = data.get("default");
				if (Boolean.TRUE.equals(isDefault)) {
					classifier.setDefault(true);
				}
			}

			tx.commit();
		}

		ToolI18NUtil.applyIfPresent(enumeration, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));

		// Apply I18N to classifiers
		for (Map<String, Object> data : classifierDataList) {
			String name = (String) data.get("classifierName");
			TLClassifier classifier = enumeration.getClassifier(name);
			if (classifier != null) {
				String cLabelEn = extractNested(data, "label", "en");
				String cLabelDe = extractNested(data, "label", "de");
				String cDescEn = extractNested(data, "description", "en");
				String cDescDe = extractNested(data, "description", "de");
				ToolI18NUtil.applyIfPresent(classifier, ToolI18NUtil.create(cLabelEn, cLabelDe, cDescEn, cDescDe));
			}
		}

		return buildEnumResponse(enumeration, true);
	}

	/**
	 * Updates properties and classifiers of an existing TopLogic enumeration.
	 *
	 * @param moduleName
	 *        Name of the module.
	 * @param enumName
	 *        Name of the enumeration.
	 * @param labelEn
	 *        English label (optional).
	 * @param labelDe
	 *        German label (optional).
	 * @param descEn
	 *        English description (optional).
	 * @param descDe
	 *        German description (optional).
	 * @param addClassifiersJson
	 *        JSON array of new classifiers to add.
	 * @param updateClassifiersJson
	 *        JSON array of existing classifiers to update.
	 * @param defaultClassifier
	 *        Name of classifier to set as default.
	 * @return JSON with updated enumeration details.
	 */
	@ToolMethod(description = "Update properties and classifiers of an existing TopLogic enumeration")
	public String updateEnumeration(
			@P("Name of the module containing the enumeration") String moduleName,
			@P("Name of the enumeration to update") String enumName,
			@P("English label for the enumeration") String labelEn,
			@P("German label for the enumeration") String labelDe,
			@P("English description for the enumeration") String descEn,
			@P("German description for the enumeration") String descDe,
			@P("JSON array of new classifiers to add, each with: classifierName (required), default (boolean), label.en, label.de, description.en, description.de") String addClassifiersJson,
			@P("JSON array of existing classifiers to update, each with: classifierName (required), default (boolean), label.en, label.de, description.en, description.de") String updateClassifiersJson,
			@P("Name of the classifier that should be set as the default") String defaultClassifier) {

		try {
			EnumResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doUpdateEnumeration(moduleName, enumName, labelEn, labelDe, descEn, descDe,
					addClassifiersJson, updateClassifiersJson, defaultClassifier));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "updateEnumeration()",
				buildArgs("moduleName", moduleName, "enumName", enumName));
		} catch (Exception ex) {
			Logger.error("updateEnumeration() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to update enumeration: " + ex.getMessage(),
				ModelEditorTool.class, "updateEnumeration()",
				buildArgs("moduleName", moduleName, "enumName", enumName));
		}
	}

	private EnumResponse doUpdateEnumeration(String moduleName, String enumName,
			String labelEn, String labelDe, String descEn, String descDe,
			String addClassifiersJson, String updateClassifiersJson, String defaultClassifier) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(enumName, "Enumeration name");

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}

		TLType existingType = module.getType(enumName);
		if (!(existingType instanceof TLEnumeration enumeration)) {
			if (existingType == null) {
				throw new IllegalArgumentException(
					"Enumeration '" + enumName + "' not found in module '" + moduleName + "'");
			}
			throw new IllegalArgumentException("Type '" + enumName + "' exists but is not an enumeration");
		}

		// Parse classifiers
		List<Map<String, Object>> addList = parseJsonArray(addClassifiersJson);
		List<Map<String, Object>> updateList = parseJsonArray(updateClassifiersJson);

		LocalizedTexts i18n = ToolI18NUtil.create(labelEn, labelDe, descEn, descDe);

		boolean hasUpdates = i18n.hasAny() || !addList.isEmpty() || !updateList.isEmpty()
			|| (defaultClassifier != null && !defaultClassifier.trim().isEmpty());

		if (!hasUpdates) {
			throw new IllegalArgumentException(
				"No update data provided. At least one of label, description, addClassifiers, updateClassifiers, or defaultClassifier must be specified");
		}

		String defClassifier = (defaultClassifier != null && !defaultClassifier.trim().isEmpty())
			? defaultClassifier.trim() : null;

		// Validate add classifiers
		Set<String> addNames = new HashSet<>();
		for (Map<String, Object> data : addList) {
			String name = (String) data.get("classifierName");
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Classifier name is required in addClassifiers");
			}
			validateClassifierName(name);
			if (!addNames.add(name)) {
				throw new IllegalArgumentException("Duplicate classifier name in addClassifiers: " + name);
			}
		}

		// Validate existing classifiers
		Set<String> existingNames = new LinkedHashSet<>();
		for (TLClassifier c : enumeration.getClassifiers()) {
			existingNames.add(c.getName());
		}

		for (Map<String, Object> data : updateList) {
			String name = (String) data.get("classifierName");
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Classifier name is required in updateClassifiers");
			}
			if (!existingNames.contains(name)) {
				throw new IllegalArgumentException(
					"Classifier '" + name + "' does not exist in enumeration '" + enumName + "'");
			}
		}

		for (Map<String, Object> data : addList) {
			String name = (String) data.get("classifierName");
			if (existingNames.contains(name)) {
				throw new IllegalArgumentException(
					"Cannot add classifier '" + name + "' because it already exists. Use updateClassifiers instead.");
			}
		}

		// Apply in transaction
		try (Transaction tx =
			PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Update Enumeration"))) {

			if (i18n.hasAny()) {
				ToolI18NUtil.applyIfPresent(enumeration, i18n);
			}

			for (Map<String, Object> data : addList) {
				String name = (String) data.get("classifierName");
				TLClassifier classifier = TLModelUtil.addClassifier(enumeration, name);
				Object isDefault = data.get("default");
				if (Boolean.TRUE.equals(isDefault)) {
					classifier.setDefault(true);
				}
				String cLabelEn = extractNested(data, "label", "en");
				String cLabelDe = extractNested(data, "label", "de");
				String cDescEn = extractNested(data, "description", "en");
				String cDescDe = extractNested(data, "description", "de");
				ToolI18NUtil.applyIfPresent(classifier, ToolI18NUtil.create(cLabelEn, cLabelDe, cDescEn, cDescDe));
			}

			for (Map<String, Object> data : updateList) {
				String name = (String) data.get("classifierName");
				TLClassifier classifier = enumeration.getClassifier(name);
				if (classifier == null) {
					continue;
				}
				if (data.containsKey("default")) {
					Object isDefault = data.get("default");
					classifier.setDefault(Boolean.TRUE.equals(isDefault));
				}
				String cLabelEn = extractNested(data, "label", "en");
				String cLabelDe = extractNested(data, "label", "de");
				String cDescEn = extractNested(data, "description", "en");
				String cDescDe = extractNested(data, "description", "de");
				ToolI18NUtil.applyIfPresent(classifier, ToolI18NUtil.create(cLabelEn, cLabelDe, cDescEn, cDescDe));
			}

			if (defClassifier != null) {
				for (TLClassifier c : enumeration.getClassifiers()) {
					c.setDefault(false);
				}
				TLClassifier dc = enumeration.getClassifier(defClassifier);
				if (dc != null) {
					dc.setDefault(true);
				}
			}

			tx.commit();
		}

		return buildEnumResponse(enumeration, false);
	}

	// ===== Helper Methods =====

	private static void requireNonEmpty(String value, String label) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(label + " is required");
		}
	}

	private static void validateClassName(String className) {
		if (!CLASS_NAME_PATTERN.matcher(className).matches()) {
			throw new IllegalArgumentException(
				"Class name must start with an uppercase letter and contain only letters, numbers, and underscores");
		}
	}

	private static void validatePropertyName(String propertyName) {
		if (!PROPERTY_NAME_PATTERN.matcher(propertyName).matches()) {
			throw new IllegalArgumentException(
				"Property name must start with a letter or underscore, may only contain letters, digits, dots, underscores, and minus signs, and must not end with a dot.");
		}
	}

	private static void validateEnumName(String enumName) {
		if (!ENUM_NAME_PATTERN.matcher(enumName).matches()) {
			throw new IllegalArgumentException(
				"Enumeration name must be a valid Java identifier and may contain dots");
		}
	}

	private static void validateClassifierName(String name) {
		if (!CLASSIFIER_NAME_PATTERN.matcher(name).matches()) {
			throw new IllegalArgumentException(
				"Invalid classifier name '" + name + "'. Must start with a Java identifier character.");
		}
	}

	private static TLClass resolveClass(TLModel model, String moduleName, String className) {
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}
		TLType type = module.getType(className);
		if (type == null) {
			throw new IllegalArgumentException(
				"Class '" + className + "' does not exist in module '" + moduleName + "'");
		}
		if (!(type instanceof TLClass tlClass)) {
			throw new IllegalArgumentException(
				"Type '" + className + "' exists in module '" + moduleName + "' but is not a class");
		}
		return tlClass;
	}

	private static List<TLClass> resolveGeneralizations(List<String> names, TLModule module, TLModel model) {
		if (names == null) {
			return new ArrayList<>();
		}
		LinkedHashSet<String> uniqueNames = new LinkedHashSet<>(names);
		List<TLClass> result = new ArrayList<>();
		for (String name : uniqueNames) {
			result.add(resolveType(name, module, model));
		}
		return result;
	}

	private static TLClass resolveType(String name, TLModule module, TLModel model) {
		TLType type;

		if (name.contains(":")) {
			// Qualified name: "module:TypeName"
			try {
				type = TLModelUtil.findType(model, name);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException(
					"Cannot resolve qualified type '" + name + "': " + e.getMessage(), e);
			}
		} else {
			// Unqualified name: search current module first, then all modules
			type = module.getType(name);
			if (type == null) {
				type = findTypeAcrossModules(name, model);
			}
		}

		if (!(type instanceof TLClass tlClass)) {
			throw new IllegalArgumentException(
				"Type '" + name + "' is not a class");
		}
		if (tlClass.isFinal()) {
			throw new IllegalArgumentException(
				"Class '" + name + "' is final and cannot be used as a supertype");
		}
		return tlClass;
	}

	private static TLType findTypeAcrossModules(String name, TLModel model) {
		List<TLType> matches = new ArrayList<>();
		for (TLModule mod : model.getModules()) {
			TLType type = mod.getType(name);
			if (type != null) {
				matches.add(type);
			}
		}
		if (matches.isEmpty()) {
			throw new IllegalArgumentException(
				"Type '" + name + "' does not exist in any module");
		}
		if (matches.size() > 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ambiguous type '").append(name).append("'. Did you mean: ");
			for (int i = 0; i < matches.size(); i++) {
				if (i > 0) sb.append(", ");
				sb.append(TLModelUtil.qualifiedName(matches.get(i)));
			}
			sb.append("?");
			throw new IllegalArgumentException(sb.toString());
		}
		return matches.get(0);
	}

	private static List<TLClass> ensureTLObjectSuperType(TLClass tlClass, List<TLClass> generalizations) {
		TLClass tlObjectType = TLModelUtil.tlObjectType(tlClass.getModel());
		switch (generalizations.size()) {
			case 0:
				return new ArrayList<>(Collections.singletonList(tlObjectType));
			case 1:
				return generalizations;
			default:
				generalizations.remove(tlObjectType);
				return generalizations;
		}
	}

	private static List<TLClass> resolveAndEnsureGeneralizations(
			String generalizations, TLModule module, TLModel model, TLClass tlClass) {
		List<String> names = parseStringList(generalizations);
		List<TLClass> resolved = resolveGeneralizations(names, module, model);
		return ensureTLObjectSuperType(tlClass, resolved);
	}

	private static final Map<String, String> TYPE_ALIASES = Map.of(
		"int", "Integer",
		"bool", "Boolean"
	);

	private static TLType resolvePrimitiveType(TLModel model, String typeName) {
		String normalized = typeName.trim();

		// Check aliases first (case-insensitive).
		String alias = TYPE_ALIASES.get(normalized.toLowerCase());
		if (alias != null) {
			normalized = alias;
		}

		// Support qualified names like "tl.util:Color" via TLModelUtil.
		if (normalized.contains(":")) {
			try {
				return TLModelUtil.findType(model, normalized);
			} catch (RuntimeException ex) {
				throw new IllegalArgumentException(
					"Unknown datatype: '" + typeName + "'. " + ex.getMessage());
			}
		}

		// Search all modules for a matching datatype (case-insensitive).
		TLPrimitive match = null;
		for (TLModule module : model.getModules()) {
			for (TLPrimitive datatype : module.getDatatypes()) {
				if (datatype.getName().equalsIgnoreCase(normalized)) {
					if (match != null) {
						throw new IllegalArgumentException(
							"Ambiguous datatype name: '" + typeName + "'. Found in multiple modules: "
								+ TLModelUtil.qualifiedName(match) + " and " + TLModelUtil.qualifiedName(datatype)
								+ ". Use a qualified name (e.g. '" + TLModelUtil.qualifiedName(match) + "') to disambiguate.");
					}
					match = datatype;
				}
			}
		}

		if (match != null) {
			return match;
		}

		// Build available types list for error message.
		List<String> availableTypes = new ArrayList<>();
		for (TLPrimitive datatype : TLModelUtil.getDataTypes(model)) {
			availableTypes.add(TLModelUtil.qualifiedName(datatype));
		}
		Collections.sort(availableTypes);

		throw new IllegalArgumentException(
			"Unknown datatype: '" + typeName + "'. Available datatypes: " + String.join(", ", availableTypes));
	}

	private static HistoryType resolveHistoryType(String value) {
		if (value == null || value.trim().isEmpty()) {
			return HistoryType.CURRENT;
		}
		try {
			return HistoryType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Invalid historyType: '" + value + "'. Valid values are: CURRENT, HISTORIC, MIXED");
		}
	}

	private static DeletionPolicy resolveDeletionPolicy(String value) {
		if (value == null || value.trim().isEmpty()) {
			return DeletionPolicy.CLEAR_REFERENCE;
		}
		for (DeletionPolicy policy : DeletionPolicy.values()) {
			if (policy.getExternalName().equalsIgnoreCase(value)
				|| policy.name().equalsIgnoreCase(value)) {
				return policy;
			}
		}
		throw new IllegalArgumentException(
			"Invalid deletionPolicy: '" + value + "'. Valid values are: CLEAR_REFERENCE, DELETE_REFERER, STABILISE_REFERENCE, VETO");
	}

	private static List<String> parseStringList(String jsonOrCommaSeparated) {
		if (jsonOrCommaSeparated == null || jsonOrCommaSeparated.trim().isEmpty()) {
			return null;
		}
		String trimmed = jsonOrCommaSeparated.trim();
		if (trimmed.startsWith("[")) {
			// Parse as JSON array
			try {
				Object parsed = com.top_logic.basic.json.JSON.fromString(trimmed);
				if (parsed instanceof List<?> list) {
					List<String> result = new ArrayList<>();
					for (Object item : list) {
						if (item instanceof String s) {
							result.add(s);
						}
					}
					return result;
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid JSON array: " + ex.getMessage());
			}
		}
		// Parse as comma-separated
		String[] parts = trimmed.split(",");
		List<String> result = new ArrayList<>();
		for (String part : parts) {
			String p = part.trim();
			if (!p.isEmpty()) {
				result.add(p);
			}
		}
		return result.isEmpty() ? null : result;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> parseJsonArray(String json) {
		if (json == null || json.trim().isEmpty()) {
			return Collections.emptyList();
		}
		try {
			Object parsed = com.top_logic.basic.json.JSON.fromString(json);
			if (parsed instanceof List<?> list) {
				List<Map<String, Object>> result = new ArrayList<>();
				for (Object item : list) {
					if (item instanceof Map) {
						result.add((Map<String, Object>) item);
					}
				}
				return result;
			}
			return Collections.emptyList();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid JSON array: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static String extractNested(Map<String, Object> data, String objectKey, String languageKey) {
		Object nested = data.get(objectKey);
		if (nested instanceof Map) {
			Object value = ((Map<String, Object>) nested).get(languageKey);
			if (value != null) {
				String s = value.toString().trim();
				return s.isEmpty() ? null : s;
			}
		}
		return null;
	}

	private static ReferenceResponse buildReferenceResponse(TLReference reference, boolean created) {
		ReferenceResponse response = ReferenceResponse.create();
		response.setSuccess(true);
		response.setName(reference.getName());
		response.setTid(reference.tId().asString());
		response.setCreated(created);
		return response;
	}

	private static BackwardReferenceResponse buildBackwardReferenceResponse(
			TLReference backwardRef, boolean created) {
		BackwardReferenceResponse response = BackwardReferenceResponse.create();
		response.setSuccess(true);
		response.setName(backwardRef.getName());
		response.setTid(backwardRef.tId().asString());
		response.setCreated(created);
		return response;
	}

	private static EnumResponse buildEnumResponse(TLEnumeration enumeration, boolean created) {
		EnumResponse response = EnumResponse.create();
		response.setSuccess(true);
		response.setName(enumeration.getName());
		response.setTid(enumeration.tId().asString());
		response.setCreated(created);
		return response;
	}

	// ===== Delete Operations =====

	/**
	 * Deletes a model element from the TopLogic application model.
	 *
	 * <p>
	 * Performs safety checks before deletion and returns detailed blockers on failure. The element
	 * type is automatically inferred from the resolved model part.
	 * </p>
	 *
	 * @param qualifiedName
	 *        Qualified name of the element: {@code 'moduleName'} for modules,
	 *        {@code 'moduleName:TypeName'} for classes/enums,
	 *        {@code 'moduleName:TypeName#partName'} for properties/references/classifiers.
	 * @return JSON with deletion result including any blockers or cascade-deleted parts.
	 */
	@ToolMethod(description = "Delete a model element from the TopLogic application model. "
		+ "Performs safety checks before deletion — returns detailed blockers on failure. "
		+ "Qualified name format: 'moduleName' for modules, 'moduleName:TypeName' for classes/enums, "
		+ "'moduleName:TypeName#partName' for properties/references/classifiers.")
	public String deleteModelElement(
			@P("Qualified name of the element to delete")
			String qualifiedName) {

		try {
			DeleteResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doDeleteModelElement(qualifiedName));

			return DataClassSerializer.toJson(response, ModelEditorTool.class, "deleteModelElement()",
				buildArgs("qualifiedName", qualifiedName));
		} catch (Exception ex) {
			Logger.error("deleteModelElement() error: " + ex.getMessage(), ex, ModelEditorTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to delete element: " + ex.getMessage(),
				ModelEditorTool.class, "deleteModelElement()",
				buildArgs("qualifiedName", qualifiedName));
		}
	}

	private DeleteResponse doDeleteModelElement(String qualifiedName)
			throws KnowledgeBaseException {
		requireNonEmpty(qualifiedName, "qualifiedName");

		TLModelPart resolved = TLModelUtil.resolveModelPart(qualifiedName);
		ModelKind kind = resolved.getModelKind();
		String kindName = kind.name().toLowerCase();

		// Check for deletion blockers
		List<DeletionBlocker> blockers = checkDeletionBlockers(resolved);
		if (!blockers.isEmpty()) {
			DeleteResponse response = DeleteResponse.create();
			response.setSuccess(false);
			response.setName(getPartName(resolved));
			response.setQualifiedName(qualifiedName);
			response.setElementType(kind.name());
			response.setResultMessage(
				"Cannot delete " + kindName + " '" + qualifiedName + "': blocked by dependencies");
			for (DeletionBlocker blocker : blockers) {
				response.addBlocker(blocker);
			}
			return response;
		}

		// Capture name before deletion (object becomes invalid after deleteRecursive)
		String partName = getPartName(resolved);
		// Collect parts that will be cascade-deleted BEFORE deletion
		List<String> deletedParts = collectDeletedParts(resolved);

		// Perform deletion
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
				.beginTransaction(ResKey.text("Delete " + kind.name() + ": " + qualifiedName))) {
			TLModelUtil.deleteRecursive(resolved);
			tx.commit();
		}

		DeleteResponse response = DeleteResponse.create();
		response.setSuccess(true);
		response.setName(partName);
		response.setQualifiedName(qualifiedName);
		response.setElementType(kind.name());
		response.setResultMessage("Successfully deleted " + kindName + " '" + qualifiedName + "'");
		for (String part : deletedParts) {
			response.addDeletedPart(part);
		}
		return response;
	}

	private List<DeletionBlocker> checkDeletionBlockers(TLModelPart part) {
		TLModel model = ModelService.getApplicationModel();
		List<DeletionBlocker> blockers = new ArrayList<>();

		// Check delete-protection annotation
		if (DisplayAnnotations.isDeleteProtected(part)) {
			DeletionBlocker blocker = DeletionBlocker.create();
			blocker.setReason("DELETE_PROTECTED");
			blocker.setDetails("Element is marked as delete-protected");
			blockers.add(blocker);
		}

		if (part instanceof TLClass) {
			TLClass tlClass = (TLClass) part;

			// Check for direct instances
			if (!MetaElementUtil.getAllDirectInstancesOf(tlClass, TLObject.class).isEmpty()) {
				DeletionBlocker blocker = DeletionBlocker.create();
				blocker.setReason("HAS_INSTANCES");
				blocker.setDetails("Class has direct instances that must be deleted first");
				blockers.add(blocker);
			}

			// Check for specializations (subtypes)
			if (!tlClass.getSpecializations().isEmpty()) {
				StringBuilder sb = new StringBuilder("Subtypes: ");
				boolean first = true;
				for (TLClass spec : tlClass.getSpecializations()) {
					if (!first) sb.append(", ");
					sb.append(spec.getModule().getName()).append(":").append(spec.getName());
					first = false;
				}
				DeletionBlocker blocker = DeletionBlocker.create();
				blocker.setReason("HAS_SPECIALIZATIONS");
				blocker.setDetails(sb.toString());
				blockers.add(blocker);
			}

			// Check for usage by type parts in other types
			checkTypeUsage(model, tlClass, blockers);
		}

		if (part instanceof TLEnumeration) {
			TLEnumeration tlEnum = (TLEnumeration) part;
			checkTypeUsage(model, tlEnum, blockers);
		}

		if (part instanceof TLModule) {
			TLModule module = (TLModule) part;
			for (TLType type : new ArrayList<>(module.getTypes())) {
				if (type instanceof TLClass) {
					TLClass tlClass = (TLClass) type;
					if (!MetaElementUtil.getAllDirectInstancesOf(tlClass, TLObject.class).isEmpty()) {
						DeletionBlocker blocker = DeletionBlocker.create();
						blocker.setReason("HAS_INSTANCES");
						blocker.setDetails("Class '" + module.getName() + ":" + tlClass.getName()
							+ "' has direct instances");
						blockers.add(blocker);
					}
					for (TLClass spec : tlClass.getSpecializations()) {
						if (spec.getModule() != module) {
							DeletionBlocker blocker = DeletionBlocker.create();
							blocker.setReason("HAS_SPECIALIZATIONS");
							blocker.setDetails("Class '" + module.getName() + ":" + tlClass.getName()
								+ "' has subtype in another module: "
								+ spec.getModule().getName() + ":" + spec.getName());
							blockers.add(blocker);
						}
					}
					checkTypeUsageExcludingModule(model, tlClass, module, blockers);
				}
				if (type instanceof TLEnumeration) {
					checkTypeUsageExcludingModule(model, type, module, blockers);
				}
			}
		}

		return blockers;
	}

	private void checkTypeUsage(TLModel model, TLType type, List<DeletionBlocker> blockers) {
		Collection<TLStructuredTypePart> usages = TLModelUtil.getUsage(model, type);
		for (TLStructuredTypePart usage : usages) {
			// Skip internal association types (e.g. Task$priority) — they are
			// implementation details managed by the framework, not user-visible dependencies.
			if (usage.getOwner().getModelKind() == ModelKind.ASSOCIATION) {
				continue;
			}
			String ownerQName = usage.getOwner().getModule().getName() + ":" + usage.getOwner().getName();
			DeletionBlocker blocker = DeletionBlocker.create();
			blocker.setReason("USED_BY_TYPEPART");
			blocker.setDetails("Referenced by: " + ownerQName + "#" + usage.getName());
			blockers.add(blocker);
		}
	}

	private void checkTypeUsageExcludingModule(TLModel model, TLType type, TLModule excludeModule,
			List<DeletionBlocker> blockers) {
		Collection<TLStructuredTypePart> usages = TLModelUtil.getUsage(model, type);
		for (TLStructuredTypePart usage : usages) {
			if (usage.getOwner().getModelKind() == ModelKind.ASSOCIATION) {
				continue;
			}
			if (usage.getOwner().getModule() != excludeModule) {
				String ownerQName = usage.getOwner().getModule().getName() + ":" + usage.getOwner().getName();
				DeletionBlocker blocker = DeletionBlocker.create();
				blocker.setReason("USED_BY_TYPEPART");
				blocker.setDetails("Type '" + type.getModule().getName() + ":" + type.getName()
					+ "' referenced by: " + ownerQName + "#" + usage.getName());
				blockers.add(blocker);
			}
		}
	}

	private List<String> collectDeletedParts(TLModelPart part) {
		List<String> deletedParts = new ArrayList<>();

		if (part instanceof TLClass) {
			TLClass tlClass = (TLClass) part;
			String prefix = tlClass.getModule().getName() + ":" + tlClass.getName() + "#";
			for (TLStructuredTypePart localPart : tlClass.getLocalParts()) {
				String kind = (localPart instanceof TLReference) ? "reference" : "property";
				deletedParts.add(kind + ": " + prefix + localPart.getName());
			}
			for (TLClass spec : tlClass.getSpecializations()) {
				deletedParts.add("subtype: " + spec.getModule().getName() + ":" + spec.getName());
			}
		} else if (part instanceof TLEnumeration) {
			TLEnumeration tlEnum = (TLEnumeration) part;
			String prefix = tlEnum.getModule().getName() + ":" + tlEnum.getName() + "#";
			for (TLClassifier classifier : tlEnum.getClassifiers()) {
				deletedParts.add("classifier: " + prefix + classifier.getName());
			}
		} else if (part instanceof TLModule) {
			TLModule module = (TLModule) part;
			for (TLType type : module.getTypes()) {
				if (type instanceof TLClass) {
					deletedParts.add("class: " + module.getName() + ":" + type.getName());
				} else if (type instanceof TLEnumeration) {
					deletedParts.add("enumeration: " + module.getName() + ":" + type.getName());
				}
			}
		} else if (part instanceof TLReference) {
			TLReference ref = (TLReference) part;
			TLAssociationEnd end = ref.getEnd();
			if (end != null) {
				TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(end);
				if (otherEnd != null) {
					TLReference inverseRef = otherEnd.getReference();
					if (inverseRef != null && inverseRef != ref) {
						String ownerQName = inverseRef.getOwner().getModule().getName()
							+ ":" + inverseRef.getOwner().getName();
						deletedParts.add("inverse reference: " + ownerQName + "#" + inverseRef.getName());
					}
				}
			}
		}

		return deletedParts;
	}

	private String getPartName(TLModelPart part) {
		if (part instanceof TLModule) {
			return ((TLModule) part).getName();
		} else if (part instanceof TLType) {
			return ((TLType) part).getName();
		} else if (part instanceof TLStructuredTypePart) {
			return ((TLStructuredTypePart) part).getName();
		} else if (part instanceof TLClassifier) {
			return ((TLClassifier) part).getName();
		}
		return part.toString();
	}

	private static Map<String, Object> buildArgs(String... keyValues) {
		Map<String, Object> args = new HashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			if (i + 1 < keyValues.length) {
				args.put(keyValues[i], keyValues[i + 1]);
			}
		}
		return args;
	}
}
