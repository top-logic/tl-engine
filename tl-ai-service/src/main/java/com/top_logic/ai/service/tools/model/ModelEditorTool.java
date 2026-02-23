/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.ai.service.tools.model.messages.BackwardReferenceResponse;
import com.top_logic.ai.service.tools.model.messages.ClassResponse;
import com.top_logic.ai.service.tools.model.messages.EnumClassifierInfo;
import com.top_logic.ai.service.tools.model.messages.EnumResponse;
import com.top_logic.ai.service.tools.model.messages.ModuleResponse;
import com.top_logic.ai.service.tools.model.messages.PropertyResponse;
import com.top_logic.ai.service.tools.model.messages.ReferenceResponse;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.ai.service.tools.util.ToolI18NUtil;
import com.top_logic.ai.service.tools.util.ToolI18NUtil.LocalizedTexts;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * <code>LangChain4j</code> tool for creating and updating TopLogic model elements.
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
	@Tool("Create a new TopLogic module or get an existing one by name")
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
		response.setTypeCount(module.getTypes().size());
		response.setCreated(created);
		response.setResultMessage(created
			? "Module '" + module.getName() + "' created successfully"
			: "Module '" + module.getName() + "' already existed");
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
	@Tool("Update internationalized labels and descriptions for an existing TopLogic module")
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
		response.setTypeCount(module.getTypes().size());
		response.setCreated(false);
		response.setResultMessage("Module '" + module.getName() + "' updated successfully");
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
	@Tool("Create a new TopLogic class in an existing module or get an existing one by name")
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

		List<String> generalizationNames = parseStringList(generalizations);

		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("Module '" + moduleName + "' does not exist");
		}

		List<TLClass> resolved = resolveGeneralizations(generalizationNames, module);

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
				tlClass.getGeneralizations().addAll(resolved);
				created = true;
				tx.commit();
			}

			ToolI18NUtil.applyIfPresent(tlClass, ToolI18NUtil.create(labelEn, labelDe, descEn, descDe));
		}

		ClassResponse response = ClassResponse.create();
		response.setSuccess(true);
		response.setName(tlClass.getName());
		response.setTid(tlClass.tId().asString());
		response.setModule(tlClass.getModule().getName());
		response.setPartCount(tlClass.getLocalClassParts().size());
		response.setCreated(created);
		response.setResultMessage(created
			? "Class '" + tlClass.getName() + "' created successfully in module '" + moduleName + "'"
			: "Class '" + tlClass.getName() + "' already existed in module '" + moduleName + "'");
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
	@Tool("Update properties, flags, and inheritance of an existing TopLogic class")
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

		List<String> generalizationNames = parseStringList(generalizations);
		List<TLClass> resolved =
			generalizationNames != null ? resolveGeneralizations(generalizationNames, module) : null;

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
		response.setModule(tlClass.getModule().getName());
		response.setPartCount(tlClass.getLocalClassParts().size());
		response.setCreated(false);
		response.setResultMessage("Class '" + tlClass.getName() + "' updated successfully");
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
	 *        Primitive type kind (STRING, INT, BOOLEAN, FLOAT, DATE, TRISTATE, BINARY, CUSTOM).
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
	@Tool("Create a new property on a TopLogic class with primitive types (STRING, INT, BOOLEAN, FLOAT, DATE, TRISTATE, BINARY, CUSTOM), or return an existing one by name")
	public String createProperty(
			@P("Name of the module containing the class") String moduleName,
			@P("Name of the class to add the property to") String className,
			@P("Name of the property") String propertyName,
			@P("Primitive type kind (e.g., STRING, INT, BOOLEAN, FLOAT, DATE, TRISTATE, BINARY, CUSTOM). Case-insensitive.") String propertyType,
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
		response.setOwner(property.getOwner().getModule().getName() + ":" + property.getOwner().getName());
		response.setType(property.getType().getName());
		response.setMandatory(property.isMandatory());
		response.setMultiple(property.isMultiple());
		response.setOrdered(property.isOrdered());
		response.setBag(property.isBag());
		response.setAbstract(property.isAbstract());
		response.setCreated(created);
		response.setResultMessage(created
			? "Property '" + property.getName() + "' created successfully in class '" + className + "'"
			: "Property '" + property.getName() + "' already existed in class '" + className + "'");
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
	@Tool("Update attributes and internationalization of an existing TopLogic class property")
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
		response.setOwner(existingPart.getOwner().getModule().getName() + ":" + existingPart.getOwner().getName());
		response.setType(existingPart.getType().getName());
		response.setMandatory(existingPart.isMandatory());
		response.setMultiple(existingPart.isMultiple());
		response.setOrdered(existingPart.isOrdered());
		response.setBag(existingPart.isBag());
		response.setAbstract(existingPart.isAbstract());
		response.setCreated(false);
		response.setResultMessage("Property '" + propertyName + "' updated successfully");
		return response;
	}

	// ===== Reference Operations =====

	/**
	 * Creates a new reference on a TopLogic class pointing to another class, or returns existing.
	 *
	 * @param moduleName
	 *        Name of the module containing the source class.
	 * @param className
	 *        Name of the source class.
	 * @param referenceName
	 *        Name of the reference.
	 * @param targetModuleName
	 *        Name of the module containing the target class.
	 * @param targetClassName
	 *        Name of the target class.
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
	 *        Deletion policy: CLEAR_REFERENCE, DELETE_OBJECT, STABILISE_REFERENCE, or VETO.
	 * @return JSON with reference details.
	 */
	@Tool("Create a new reference on a TopLogic class pointing to another class, or return an existing one by name")
	public String createReference(
			@P("Name of the module containing the source class") String moduleName,
			@P("Name of the source class to add the reference to") String className,
			@P("Name of the reference") String referenceName,
			@P("Name of the module containing the target class") String targetModuleName,
			@P("Name of the target class the reference points to") String targetClassName,
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
			@P("Deletion policy: CLEAR_REFERENCE (default), DELETE_OBJECT, STABILISE_REFERENCE, or VETO") String deletionPolicy) {

		try {
			ReferenceResponse response = ThreadContextManager.inSystemInteraction(
				ModelEditorTool.class,
				() -> doCreateReference(moduleName, className, referenceName,
					targetModuleName, targetClassName,
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
			String targetModuleName, String targetClassName,
			String labelEn, String labelDe, String descEn, String descDe,
			Boolean mandatory, Boolean multiple, Boolean ordered, Boolean bag, Boolean isAbstract,
			Boolean composite, Boolean aggregate, Boolean navigate,
			String historyType, String deletionPolicy) {

		requireNonEmpty(moduleName, "Module name");
		requireNonEmpty(className, "Class name");
		requireNonEmpty(referenceName, "Reference name");
		requireNonEmpty(targetModuleName, "Target module name");
		requireNonEmpty(targetClassName, "Target class name");
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

		TLModule targetModule = model.getModule(targetModuleName);
		if (targetModule == null) {
			throw new IllegalArgumentException("Target module '" + targetModuleName + "' does not exist");
		}
		TLType targetType = targetModule.getType(targetClassName);
		if (targetType == null) {
			throw new IllegalArgumentException(
				"Target class '" + targetClassName + "' does not exist in module '" + targetModuleName + "'");
		}
		if (!(targetType instanceof TLClass targetClass)) {
			throw new IllegalArgumentException(
				"Target type '" + targetClassName + "' is not a class");
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
			TLAssociationEnd forwardEnd = TLModelUtil.addEnd(association, "dest", targetClass);
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
	@Tool("Create a backward reference to an existing forward reference, or return an existing one by name")
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
			return buildBackwardReferenceResponse(backwardRef, forwardRef, false);
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

		return buildBackwardReferenceResponse(backwardRef, forwardRef, true);
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
	@Tool("Create a new TopLogic enumeration with optional classifiers, or get an existing one by name")
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
	@Tool("Update properties and classifiers of an existing TopLogic enumeration")
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

	@SuppressWarnings("unchecked")
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

	private static List<TLClass> resolveGeneralizations(List<String> names, TLModule module) {
		if (names == null) {
			return Collections.emptyList();
		}
		List<TLClass> result = new ArrayList<>();
		for (String name : names) {
			TLType type = module.getType(name);
			if (type == null) {
				throw new IllegalArgumentException(
					"Generalization class '" + name + "' does not exist in module '" + module.getName() + "'");
			}
			if (!(type instanceof TLClass tlClass)) {
				throw new IllegalArgumentException(
					"Type '" + name + "' in module '" + module.getName() + "' is not a class");
			}
			result.add(tlClass);
		}
		return result;
	}

	private static TLType resolvePrimitiveType(TLModel model, String typeKind) {
		try {
			TLPrimitive.Kind kind = TLPrimitive.Kind.valueOf(typeKind.toUpperCase());
			return TLCore.getPrimitiveType(model, kind);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Invalid primitive type kind: '" + typeKind + "'. Valid kinds are: STRING, INT, BOOLEAN, FLOAT, DATE, TRISTATE, BINARY, CUSTOM");
		}
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
		try {
			for (DeletionPolicy policy : DeletionPolicy.values()) {
				if (policy.getExternalName().equalsIgnoreCase(value)) {
					return policy;
				}
			}
			return DeletionPolicy.valueOf(value.toUpperCase().replace('-', '_'));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Invalid deletionPolicy: '" + value + "'. Valid values are: CLEAR_REFERENCE, DELETE_OBJECT, STABILISE_REFERENCE, VETO");
		}
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
		response.setOwner(reference.getOwner().getModule().getName() + ":" + reference.getOwner().getName());
		response.setTargetType(reference.getType().getModule().getName() + ":" + reference.getType().getName());
		response.setMandatory(reference.isMandatory());
		response.setMultiple(reference.isMultiple());
		response.setOrdered(reference.isOrdered());
		response.setBag(reference.isBag());
		response.setAbstract(reference.isAbstract());
		response.setComposite(reference.isComposite());
		response.setAggregate(reference.isAggregate());
		response.setNavigate(reference.getEnd().canNavigate());
		response.setHistoryType(reference.getHistoryType().getExternalName());
		response.setDeletionPolicy(reference.getDeletionPolicy().getExternalName());
		response.setCreated(created);
		response.setResultMessage(created
			? "Reference '" + reference.getName() + "' created successfully in class '" + reference.getOwner().getName() + "'"
			: "Reference '" + reference.getName() + "' already existed in class '" + reference.getOwner().getName() + "'");
		return response;
	}

	private static BackwardReferenceResponse buildBackwardReferenceResponse(
			TLReference backwardRef, TLReference forwardRef, boolean created) {
		BackwardReferenceResponse response = BackwardReferenceResponse.create();
		response.setSuccess(true);
		response.setName(backwardRef.getName());
		response.setTid(backwardRef.tId().asString());
		response.setOwner(backwardRef.getOwner().getModule().getName() + ":" + backwardRef.getOwner().getName());
		response.setTargetType(backwardRef.getType().getModule().getName() + ":" + backwardRef.getType().getName());
		response.setMandatory(backwardRef.isMandatory());
		response.setMultiple(backwardRef.isMultiple());
		response.setNavigate(backwardRef.getEnd().canNavigate());
		response.setForwardReferenceName(forwardRef.getName());
		response.setForwardReferenceOwner(
			forwardRef.getOwner().getModule().getName() + ":" + forwardRef.getOwner().getName());
		response.setCreated(created);
		response.setResultMessage(created
			? "Backward reference '" + backwardRef.getName() + "' created successfully in class '" + backwardRef.getOwner().getName() + "'"
			: "Backward reference '" + backwardRef.getName() + "' already existed in class '" + backwardRef.getOwner().getName() + "'");
		return response;
	}

	private static EnumResponse buildEnumResponse(TLEnumeration enumeration, boolean created) {
		EnumResponse response = EnumResponse.create();
		response.setSuccess(true);
		response.setName(enumeration.getName());
		response.setTid(enumeration.tId().asString());
		response.setModule(enumeration.getModule().getName());
		response.setClassifierCount(enumeration.getClassifiers().size());
		response.setCreated(created);

		for (TLClassifier classifier : enumeration.getClassifiers()) {
			EnumClassifierInfo info = EnumClassifierInfo.create();
			info.setName(classifier.getName());
			info.setDefault(classifier.isDefault());
			response.addClassifier(info);
		}

		response.setResultMessage(created
			? "Enumeration '" + enumeration.getName() + "' created successfully in module '" + enumeration.getModule().getName() + "'"
			: "Enumeration '" + enumeration.getName() + "' already existed in module '" + enumeration.getModule().getName() + "'");
		return response;
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
