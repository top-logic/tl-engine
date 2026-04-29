/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.annotate.TLModuleDisplayGroup;
import com.top_logic.model.config.annotation.UnorderedEnum;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo;
import com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse;
import com.top_logic.tl.ai.core.tools.model.messages.ErrorResponse;
import com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo;
import com.top_logic.tl.ai.core.tools.model.messages.ModuleListResponse;
import com.top_logic.tl.ai.core.tools.model.messages.TypeInfo;
import com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse;
import com.top_logic.tl.ai.core.tools.model.messages.TypePartInfo;
import com.top_logic.tl.ai.core.tools.model.messages.TypePartListResponse;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Tool for browsing TopLogic model structure (modules, types, parts).
 *
 * <p>
 * This tool provides three methods that form the natural hierarchy for model exploration: listing
 * all modules, listing types within a module, and listing properties/references of a type. All
 * methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 *
 * @see #listModules(Boolean)
 * @see #listTypes(String)
 * @see #listTypeParts(String, String, Boolean)
 */
public class ModelBrowserTool {

	/**
	 * Configuration for {@link ModelBrowserTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ModelBrowserTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link ModelBrowserTool}.
	 */
	public ModelBrowserTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link ModelBrowserTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ModelBrowserTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Lists modules in the TopLogic application model.
	 *
	 * <p>
	 * Returns a JSON array of modules with their names, type counts, labels, and descriptions.
	 * Modules are sorted by name. Association types are excluded from type counts as they are
	 * internal implementation details.
	 * </p>
	 *
	 * <p>
	 * By default, only application-specific modules are returned. Framework/platform modules
	 * (tl.*, tl5.*) are excluded unless {@code includeFramework} is set to {@code true}.
	 * </p>
	 *
	 * @param includeFramework
	 *        Whether to include framework/platform modules. Default: {@code false}.
	 * @return JSON array of modules.
	 */
	@ToolMethod(description = "List modules in the TopLogic application model. By default, only application-specific "
		+ "modules are returned. Framework/platform modules (tl.*, tl5.*) are excluded unless "
		+ "includeFramework is set to true. Use includeFramework=true only when you need to "
		+ "discover framework base types for extending or referencing.", mode = ToolMode.READ)
	public String listModules(
			@P("Set to true to include framework/platform modules (tl.*, tl5.*). "
				+ "Default: false (application modules only).")
			Boolean includeFramework) {
		try {
			// System interaction returns data class
			ModuleListResponse response = ThreadContextManager.inSystemInteraction(
				ModelBrowserTool.class,
				() -> {
					// Get the application model and retrieve all modules
					TLModel model = ModelService.getApplicationModel();
					List<TLModule> tlModules = model.getModules().stream()
						.sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
						.collect(Collectors.toList());

					// Build response using data classes
					ModuleListResponse result = ModuleListResponse.create();
					Resources resources = Resources.getInstance();

					for (TLModule module : tlModules) {
						// Skip framework modules unless explicitly requested
						if (!Boolean.TRUE.equals(includeFramework) && isFrameworkModule(module)) {
							continue;
						}

						ModuleInfo moduleInfo = ModuleInfo.create();
						moduleInfo.setName(module.getName());

						// Count only non-association types
						long typeCount = module.getTypes().stream()
							.filter(type -> type.getModelKind() != ModelKind.ASSOCIATION)
							.count();
						moduleInfo.setTypeCount(typeCount);

						// Add label and description from module resource key
						ResKey resourceKey = LabelVisitor.getModuleResourceKey(module);
						String label = resources.getString(resourceKey, null);
						if (label != null && !label.isEmpty()) {
							moduleInfo.setLabel(label);
						}
						String description = resources.getString(resourceKey.tooltip(), null);
						if (description != null && !description.isEmpty()) {
							moduleInfo.setDescription(description);
						}

						// Determine if this is a framework module
						// Framework modules are grouped under "tl" in the model editor
						boolean isFramework = isFrameworkModule(module);
						moduleInfo.setFrameworkModule(isFramework);

						result.addModule(moduleInfo);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("includeFramework", includeFramework);
			return DataClassSerializer.toJson(response, ModelBrowserTool.class, "listModules()", args);

		} catch (Exception ex) {
			Logger.error("listModules() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("includeFramework", includeFramework);
			return DataClassSerializer.errorToJson(
				"Error listing modules: " + ex.getMessage(),
				ModelBrowserTool.class,
				"listModules()",
				args);
		}
	}

	/**
	 * Lists all types in a specific module.
	 *
	 * <p>
	 * Returns a JSON array of types (classes, enumerations, datatypes) with their names, qualified
	 * names, metadata, and inheritance information. Types are sorted by name. Association types
	 * are excluded as they are internal implementation details.
	 * </p>
	 *
	 * @param moduleName
	 *        The name of the module to list types from.
	 * @return JSON array of types, or error JSON if module not found.
	 */
	@ToolMethod(description = "List all types (classes, enumerations, datatypes) in a specific module with inheritance information", mode = ToolMode.READ)
	public String listTypes(@P("The name of the module to list types from") String moduleName) {
		try {
			// System interaction returns data class
			TypeListResponse response = ThreadContextManager.inSystemInteraction(
				ModelBrowserTool.class,
				() -> {
					// Validate parameter
					if (moduleName == null || moduleName.isEmpty()) {
						ErrorResponse error = ErrorResponse.create();
						error.setError("Module name is required");
						throw new IllegalArgumentException("Module name is required");
					}

					// Get the application model and find the requested module
					TLModel model = ModelService.getApplicationModel();
					TLModule module = model.getModule(moduleName);

					if (module == null) {
						throw new IllegalArgumentException("Module not found: " + moduleName);
					}

					// Get all types in the module, excluding associations
					List<TLType> tlTypes = module.getTypes().stream()
						.filter(type -> type.getModelKind() != ModelKind.ASSOCIATION)
						.sorted((t1, t2) -> t1.getName().compareTo(t2.getName()))
						.collect(Collectors.toList());

					// Build response using data classes
					TypeListResponse result = TypeListResponse.create();
					Resources resources = Resources.getInstance();

					for (TLType type : tlTypes) {
						TypeInfo typeInfo = TypeInfo.create();
						typeInfo.setName(type.getName());
						typeInfo.setQualifiedName(TLModelUtil.qualifiedName(type));

						// Add meta-type (class, enumeration, datatype)
						ModelKind modelKind = type.getModelKind();
						typeInfo.setMetaType(modelKind.name().toLowerCase());

						// Add classifier count for enumerations, part count for other structured types
						if (type instanceof TLEnumeration) {
							TLEnumeration enumeration = (TLEnumeration) type;
							typeInfo.setClassifierCount(enumeration.getClassifiers().size());
						} else if (type instanceof TLStructuredType) {
							TLStructuredType structuredType = (TLStructuredType) type;
							typeInfo.setPartCount(structuredType.getLocalParts().size());
						}

						// Add inheritance information for classes
						if (type instanceof TLClass) {
							TLClass tlClass = (TLClass) type;

							// Add generalizations (supertypes)
							for (TLClass generalization : tlClass.getGeneralizations()) {
								typeInfo.addGeneralization(TLModelUtil.qualifiedName(generalization));
							}

							// Add specializations (subtypes)
							for (TLClass specialization : tlClass.getSpecializations()) {
								typeInfo.addSpecialization(TLModelUtil.qualifiedName(specialization));
							}

							// Add abstract and final flags
							typeInfo.setAbstract(tlClass.isAbstract());
							typeInfo.setFinal(tlClass.isFinal());
						}

						// Add label and description from type resource key
						ResKey resourceKey = TLModelNamingConvention.getTypeLabelKey(type);
						String label = resources.getString(resourceKey, null);
						if (label != null && !label.isEmpty()) {
							typeInfo.setLabel(label);
						}
						String description = resources.getString(resourceKey.tooltip(), null);
						if (description != null && !description.isEmpty()) {
							typeInfo.setDescription(description);
						}

						result.addType(typeInfo);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			return DataClassSerializer.toJson(response, ModelBrowserTool.class, "listTypes()", args);

		} catch (Exception ex) {
			Logger.error("listTypes() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			return DataClassSerializer.errorToJson(
				"Error listing types: " + ex.getMessage(),
				ModelBrowserTool.class,
				"listTypes()",
				args);
		}
	}

	/**
	 * Lists all parts (properties and references) of a specific type.
	 *
	 * <p>
	 * Returns a JSON array of parts with their names, types, kinds (property/reference), and
	 * multiplicity constraints. By default, only local parts are included, but inherited parts can
	 * be included by setting {@code includeInherited} to true. Parts are sorted by name.
	 * </p>
	 *
	 * @param moduleName
	 *        The module containing the type.
	 * @param typeName
	 *        The name of the type.
	 * @param includeInherited
	 *        Whether to include inherited parts from supertypes (default: false).
	 * @return JSON array of parts, or error JSON if module/type not found or type is not
	 *         structured.
	 */
	@ToolMethod(description = "List all parts (properties and references) of a specific type with multiplicity and constraints. Set {@code includeInherited=true} to include inherited parts from supertypes.", mode = ToolMode.READ)
	public String listTypeParts(
			@P("The module containing the type") String moduleName,
			@P("The name of the type") String typeName,
			@P("Whether to include inherited parts from supertypes (default: false)") Boolean includeInherited) {

		try {
			// System interaction returns data class
			TypePartListResponse response = ThreadContextManager.inSystemInteraction(
				ModelBrowserTool.class,
				() -> {
					// Validate parameters
					if (moduleName == null || moduleName.isEmpty()) {
						throw new IllegalArgumentException("Module name is required");
					}
					if (typeName == null || typeName.isEmpty()) {
						throw new IllegalArgumentException("Type name is required");
					}

					// Get the application model and find the requested module
					TLModel model = ModelService.getApplicationModel();
					TLModule module = model.getModule(moduleName);

					if (module == null) {
						throw new IllegalArgumentException("Module not found: " + moduleName);
					}

					// Find the type in the module
					TLType type = module.getType(typeName);

					if (type == null) {
						throw new IllegalArgumentException("Type not found: " + typeName + " in module " + moduleName);
					}

					// Check if the type is structured (has parts)
					if (!(type instanceof TLStructuredType)) {
						throw new IllegalArgumentException("Type " + typeName + " is not a structured type and does not have parts");
					}

					// Check if the type is an enumeration (use {@link #listClassifiers(String, String)} instead)
					if (type instanceof TLEnumeration) {
						throw new IllegalArgumentException("Type " + typeName + " is an enumeration. Use listClassifiers() to retrieve its classifiers.");
					}

					TLStructuredType structuredType = (TLStructuredType) type;

					// Default to false if not specified
					boolean includeInheritedParts = includeInherited != null && includeInherited;

					// Get parts based on include inherited flag, sorted by name
					List<? extends TLStructuredTypePart> tlParts = (includeInheritedParts
						? structuredType.getAllParts()
						: structuredType.getLocalParts()).stream()
						.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
						.collect(Collectors.toList());

					// Build response using data classes
					TypePartListResponse result = TypePartListResponse.create();
					Resources resources = Resources.getInstance();

					for (TLStructuredTypePart part : tlParts) {
						TypePartInfo partInfo = TypePartInfo.create();
						partInfo.setName(part.getName());

						// Add part kind (property/reference)
						ModelKind partKind = part.getModelKind();
						partInfo.setKind(partKind.name().toLowerCase());

						// Add owner information (type that defines this part)
						TLStructuredType owner = part.getOwner();
						partInfo.setDefinedIn(TLModelUtil.qualifiedName(owner));
						partInfo.setInherited(!owner.equals(structuredType));

						// Add type information
						TLType partType = part.getType();
						if (partType != null) {
							partInfo.setType(TLModelUtil.qualifiedName(partType));
						}

						// Add multiplicity and collection characteristics
						partInfo.setMandatory(part.isMandatory());
						partInfo.setMultiple(part.isMultiple());
						partInfo.setOrdered(part.isOrdered());
						partInfo.setBag(part.isBag());

						// Add structural properties
						partInfo.setAbstract(part.isAbstract());
						partInfo.setDerived(part.isDerived());
						partInfo.setOverride(part.isOverride());

						// Add label and description from part resource key
						ResKey resourceKey = TLTypePartResourceProvider.labelKey(part);
						String label = resources.getString(resourceKey, null);
						if (label != null && !label.isEmpty()) {
							partInfo.setLabel(label);
						}
						String description = resources.getString(resourceKey.tooltip(), null);
						if (description != null && !description.isEmpty()) {
							partInfo.setDescription(description);
						}

						result.addPart(partInfo);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			args.put("typeName", typeName);
			args.put("includeInherited", includeInherited);
			return DataClassSerializer.toJson(response, ModelBrowserTool.class, "listTypeParts()", args);

		} catch (Exception ex) {
			Logger.error("listTypeParts() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			args.put("typeName", typeName);
			args.put("includeInherited", includeInherited);
			return DataClassSerializer.errorToJson(
				"Error listing type parts: " + ex.getMessage(),
				ModelBrowserTool.class,
				"listTypeParts()",
				args);
		}
	}

	/**
	 * Lists all classifiers in a TopLogic enumeration type.
	 *
	 * @param moduleName
	 *        The name of the module containing the enumeration
	 * @param enumName
	 *        The name of the enumeration type
	 * @return JSON string containing enumeration metadata and classifier list with annotations
	 */
	@ToolMethod(description = "List all classifiers in a TopLogic enumeration with annotations", mode = ToolMode.READ)
	public String listClassifiers(
			@P("The name of the module containing the enumeration") String moduleName,
			@P("The name of the enumeration type") String enumName) {

		try {
			// System interaction returns data class
			EnumerationResponse response = ThreadContextManager.inSystemInteraction(
				ModelBrowserTool.class,
				() -> {
					// Validate inputs
					if (moduleName == null || moduleName.trim().isEmpty()) {
						throw new IllegalArgumentException("Module name is required");
					}
					if (enumName == null || enumName.trim().isEmpty()) {
						throw new IllegalArgumentException("Enumeration name is required");
					}

					// Get model and module
					TLModel model = ModelService.getApplicationModel();
					TLModule module = model.getModule(moduleName);
					if (module == null) {
						throw new IllegalArgumentException("Module not found: " + moduleName);
					}

					// Get type
					TLType type = module.getType(enumName);
					if (type == null) {
						throw new IllegalArgumentException("Type not found: " + enumName + " in module " + moduleName);
					}

					// Verify it's an enumeration
					if (!(type instanceof TLEnumeration)) {
						throw new IllegalArgumentException("Type " + enumName + " is not an enumeration (it's a " + type.getModelKind() + ")");
					}

					TLEnumeration enumeration = (TLEnumeration) type;

					// Build response using data classes
					EnumerationResponse result = EnumerationResponse.create();
					Resources resources = Resources.getInstance();

					// Set enumeration metadata
					result.setEnumeration(TLModelUtil.qualifiedName(enumeration));
					result.setModule(moduleName);
					result.setName(enumName);
					result.setClassifierCount(enumeration.getClassifiers().size());

					// Check for UnorderedEnum annotation
					UnorderedEnum unorderedAnnotation = enumeration.getAnnotation(UnorderedEnum.class);
					result.setUnordered(unorderedAnnotation != null);

					// Add label and description
					ResKey enumResourceKey = TLModelNamingConvention.getTypeLabelKey(enumeration);
					String enumLabel = resources.getString(enumResourceKey, null);
					if (enumLabel != null && !enumLabel.isEmpty()) {
						result.setLabel(enumLabel);
					}
					String enumDescription = resources.getString(enumResourceKey.tooltip(), null);
					if (enumDescription != null && !enumDescription.isEmpty()) {
						result.setDescription(enumDescription);
					}

					// Add classifiers
					for (TLClassifier classifier : enumeration.getClassifiers()) {
						ClassifierInfo classifierInfo = ClassifierInfo.create();
						classifierInfo.setName(classifier.getName());
						classifierInfo.setDefault(classifier.isDefault());
						classifierInfo.setIndex(classifier.getIndex());

						// Check for TLI18NKey annotation
						TLI18NKey i18nKey = classifier.getAnnotation(TLI18NKey.class);
						if (i18nKey != null && i18nKey.getValue() != null) {
							classifierInfo.setCustomI18NKey(i18nKey.getValue().toString());
						}

						// Add label and description
						ResKey classifierResourceKey = TLModelNamingConvention.resourceKey(classifier);
						String classifierLabel = resources.getString(classifierResourceKey, null);
						if (classifierLabel != null && !classifierLabel.isEmpty()) {
							classifierInfo.setLabel(classifierLabel);
						}
						String classifierDescription = resources.getString(classifierResourceKey.tooltip(), null);
						if (classifierDescription != null && !classifierDescription.isEmpty()) {
							classifierInfo.setDescription(classifierDescription);
						}

						result.addClassifier(classifierInfo);
					}

					return result;
				});

			// Serialize to JSON using DataClassSerializer with tracking
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			args.put("enumName", enumName);
			return DataClassSerializer.toJson(response, ModelBrowserTool.class, "listClassifiers()", args);

		} catch (Exception ex) {
			Logger.error("listClassifiers() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("moduleName", moduleName);
			args.put("enumName", enumName);
			return DataClassSerializer.errorToJson(
				"Error listing classifiers: " + ex.getMessage(),
				ModelBrowserTool.class,
				"listClassifiers()",
				args);
		}
	}

	/**
	 * Determines if a module is a framework/platform module.
	 * 
	 * <p>
	 * Framework modules are identified by:
	 * <ul>
	 * <li>Having a {@link TLModuleDisplayGroup} annotation starting with "tl"</li>
	 * <li>Or having a technical name starting with "tl." or "tl5."</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * This matches the grouping logic in TopLogic's model editor, where framework modules
	 * are grouped under the "TopLogic" container (with display group "tl").
	 * </p>
	 * 
	 * @param module
	 *        The module to check
	 * @return true if this is a framework module, false if it's an application module
	 */
	private boolean isFrameworkModule(TLModule module) {
		// Check for TLModuleDisplayGroup annotation
		TLModuleDisplayGroup displayGroup = module.getAnnotation(TLModuleDisplayGroup.class);
		if (displayGroup != null) {
			String groupValue = displayGroup.getValue();
			if (groupValue != null && groupValue.startsWith("tl")) {
				return true;
			}
		}

		// Check technical module name
		String moduleName = module.getName();
		if (moduleName.startsWith("tl.") || moduleName.startsWith("tl5.")) {
			return true;
		}

		return false;
	}
}
