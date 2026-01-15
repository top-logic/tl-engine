/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.ai.service.tools.util.JsonToolResponseBuilder;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.common.json.gstream.JsonWriter;
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
import com.top_logic.model.config.annotation.UnorderedEnum;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for browsing TopLogic model structure (modules, types, parts).
 *
 * <p>
 * This tool provides three methods that form the natural hierarchy for model exploration:
 * <ul>
 * <li>{@link #listModules()} - Lists all modules in the application</li>
 * <li>{@link #listTypes(String)} - Lists types within a specific module</li>
 * <li>{@link #listTypeParts(String, String, Boolean)} - Lists properties/references of a type</li>
 * </ul>
 * </p>
 *
 * <p>
 * All methods return JSON-formatted strings for consumption by AI agents.
 * </p>
 */
public class ModelBrowserTool {

	/**
	 * Lists all modules in the TopLogic application model.
	 *
	 * <p>
	 * Returns a JSON array of modules with their names, type counts, labels, and descriptions.
	 * Modules are sorted by name. Association types are excluded from type counts as they are
	 * internal implementation details.
	 * </p>
	 *
	 * @return JSON array of modules.
	 */
	@Tool("List all modules in the TopLogic application model with type counts and descriptions")
	public String listModules() {
		return ThreadContextManager.inSystemInteraction(
			ModelBrowserTool.class,
			() -> {
				try {
					// Get the application model and retrieve all modules
					TLModel model = ModelService.getApplicationModel();
					List<TLModule> modules = model.getModules().stream()
						.sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
						.collect(Collectors.toList());

					// Build JSON array with module information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginArray();

						for (TLModule module : modules) {
							json.beginObject();
							json.name("name").value(module.getName());

							// Count only non-association types
							long typeCount = module.getTypes().stream()
								.filter(type -> type.getModelKind() != ModelKind.ASSOCIATION)
								.count();
							json.name("typeCount").value(typeCount);

							// Add label and description from module resource key
							JsonToolResponseBuilder.writeLabelAndDescription(json,
								LabelVisitor.getModuleResourceKey(module));

							json.endObject();
						}

						json.endArray();
					}

					String result = buffer.toString();
					Logger.info("listModules() result: " + result, ModelBrowserTool.class);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to list modules: " + ex.getMessage());
					Logger.error("listModules() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listModules() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error accessing model: " + ex.getMessage());
					Logger.error("listModules() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listModules() error: " + error);
					return error;
				}
			});
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
	@Tool("List all types (classes, enumerations, datatypes) in a specific module with inheritance information")
	public String listTypes(@P("The name of the module to list types from") String moduleName) {
		return ThreadContextManager.inSystemInteraction(
			ModelBrowserTool.class,
			() -> {
				try {
					// Validate parameter
					if (moduleName == null || moduleName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Module name is required");
					}

					// Get the application model and find the requested module
					TLModel model = ModelService.getApplicationModel();
					TLModule module = model.getModule(moduleName);

					if (module == null) {
						return JsonToolResponseBuilder.buildErrorJson("Module not found: " + moduleName);
					}

					// Get all types in the module, excluding associations
					List<TLType> types = module.getTypes().stream()
						.filter(type -> type.getModelKind() != ModelKind.ASSOCIATION)
						.sorted((t1, t2) -> t1.getName().compareTo(t2.getName()))
						.collect(Collectors.toList());

					// Build JSON array with type information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginArray();

						for (TLType type : types) {
							json.beginObject();
							json.name("name").value(type.getName());
							json.name("qualifiedName").value(TLModelUtil.qualifiedName(type));

							// Add meta-type (class, enumeration, datatype)
							ModelKind modelKind = type.getModelKind();
							json.name("metaType").value(modelKind.name().toLowerCase());

							// Add classifier count for enumerations, part count for other structured types
							if (type instanceof TLEnumeration) {
								TLEnumeration enumeration = (TLEnumeration) type;
								int classifierCount = enumeration.getClassifiers().size();
								json.name("classifierCount").value(classifierCount);
							} else if (type instanceof TLStructuredType) {
								TLStructuredType structuredType = (TLStructuredType) type;
								int partCount = structuredType.getLocalParts().size();
								json.name("partCount").value(partCount);
							}

							// Add inheritance information for classes
							if (type instanceof TLClass) {
								TLClass tlClass = (TLClass) type;

								// Add generalizations (supertypes)
								List<TLClass> generalizations = tlClass.getGeneralizations();
								if (!generalizations.isEmpty()) {
									json.name("generalizations").beginArray();
									for (TLClass generalization : generalizations) {
										json.value(TLModelUtil.qualifiedName(generalization));
									}
									json.endArray();
								}

								// Add specializations (subtypes)
								Collection<TLClass> specializations = tlClass.getSpecializations();
								if (!specializations.isEmpty()) {
									json.name("specializations").beginArray();
									for (TLClass specialization : specializations) {
										json.value(TLModelUtil.qualifiedName(specialization));
									}
									json.endArray();
								}
							}

							// Add label and description from type resource key
							JsonToolResponseBuilder.writeLabelAndDescription(json,
								TLModelNamingConvention.getTypeLabelKey(type));

							json.endObject();
						}

						json.endArray();
					}

					String result = buffer.toString();
					Logger.info("listTypes() result: " + result, ModelBrowserTool.class);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to list types: " + ex.getMessage());
					Logger.error("listTypes() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listTypes() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error accessing module: " + ex.getMessage());
					Logger.error("listTypes() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listTypes() error: " + error);
					return error;
				}
			});
	}

	/**
	 * Lists all parts (properties and references) of a specific type.
	 *
	 * <p>
	 * Returns a JSON array of parts with their names, types, kinds (property/reference), and
	 * multiplicity constraints. By default, only local parts are included, but inherited parts
	 * can be included by setting includeInherited to true. Parts are sorted by name.
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
	@Tool("List all parts (properties and references) of a specific type with multiplicity and constraints. Set includeInherited=true to include inherited parts from supertypes.")
	public String listTypeParts(
			@P("The module containing the type") String moduleName,
			@P("The name of the type") String typeName,
			@P("Whether to include inherited parts from supertypes (default: false)") Boolean includeInherited) {

		return ThreadContextManager.inSystemInteraction(
			ModelBrowserTool.class,
			() -> {
				try {
					// Validate parameters
					if (moduleName == null || moduleName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Module name is required");
					}
					if (typeName == null || typeName.isEmpty()) {
						return JsonToolResponseBuilder.buildErrorJson("Type name is required");
					}

					// Get the application model and find the requested module
					TLModel model = ModelService.getApplicationModel();
					TLModule module = model.getModule(moduleName);

					if (module == null) {
						return JsonToolResponseBuilder.buildErrorJson("Module not found: " + moduleName);
					}

					// Find the type in the module
					TLType type = module.getType(typeName);

					if (type == null) {
						return JsonToolResponseBuilder.buildErrorJson(
							"Type not found: " + typeName + " in module " + moduleName);
					}

					// Check if the type is structured (has parts)
					if (!(type instanceof TLStructuredType)) {
						return JsonToolResponseBuilder.buildErrorJson(
							"Type " + typeName + " is not a structured type and does not have parts");
					}

					TLStructuredType structuredType = (TLStructuredType) type;

					// Default to false if not specified
					boolean includeInheritedParts = includeInherited != null && includeInherited;

					// Get parts based on includeInherited flag, sorted by name
					List<? extends TLStructuredTypePart> parts = (includeInheritedParts
						? structuredType.getAllParts()
						: structuredType.getLocalParts()).stream()
						.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
						.collect(Collectors.toList());

					// Build JSON array with part information
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginArray();

						for (TLStructuredTypePart part : parts) {
							json.beginObject();
							json.name("name").value(part.getName());

							// Add part kind (property/reference)
							ModelKind partKind = part.getModelKind();
							json.name("kind").value(partKind.name().toLowerCase());

							// Add owner information (type that defines this part)
							TLStructuredType owner = part.getOwner();
							json.name("definedIn").value(TLModelUtil.qualifiedName(owner));
							json.name("inherited").value(!owner.equals(structuredType));

							// Add type information
							TLType partType = part.getType();
							if (partType != null) {
								json.name("type").value(TLModelUtil.qualifiedName(partType));
							}

							// Add multiplicity and collection characteristics
							json.name("mandatory").value(part.isMandatory());
							json.name("multiple").value(part.isMultiple());

							if (part.isMultiple()) {
								// Only relevant for multiple-valued parts
								json.name("ordered").value(part.isOrdered());
								json.name("bag").value(part.isBag());
							}

							// Add structural properties
							json.name("abstract").value(part.isAbstract());
							json.name("derived").value(part.isDerived());
							json.name("override").value(part.isOverride());

							// Add label and description from part resource key
							JsonToolResponseBuilder.writeLabelAndDescription(json,
								TLTypePartResourceProvider.labelKey(part));

							json.endObject();
						}

						json.endArray();
					}

					String result = buffer.toString();
					Logger.info("listTypeParts() result: " + result, ModelBrowserTool.class);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to list type parts: " + ex.getMessage());
					Logger.error("listTypeParts() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listTypeParts() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error accessing type: " + ex.getMessage());
					Logger.error("listTypeParts() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listTypeParts() error: " + error);
					return error;
				}
			});
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
	@Tool("List all classifiers in a TopLogic enumeration with annotations")
	public String listClassifiers(
			@P("The name of the module containing the enumeration") String moduleName,
			@P("The name of the enumeration type") String enumName) {

		return ThreadContextManager.inSystemInteraction(
			ModelBrowserTool.class,
			() -> {
				try {
					// Build JSON response with enumeration metadata and classifier list
					StringWriter buffer = new StringWriter();
					try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
						json.beginObject();

						// Validate inputs
						if (moduleName == null || moduleName.trim().isEmpty()) {
							json.name("error").value("Module name is required");
							json.endObject();
							return buffer.toString();
						}
						if (enumName == null || enumName.trim().isEmpty()) {
							json.name("error").value("Enumeration name is required");
							json.endObject();
							return buffer.toString();
						}

						// Get model and module
						TLModel model = ModelService.getApplicationModel();
						TLModule module = model.getModule(moduleName);
						if (module == null) {
							json.name("error").value("Module not found: " + moduleName);
							json.endObject();
							return buffer.toString();
						}

						// Get type
						TLType type = module.getType(enumName);
						if (type == null) {
							json.name("error").value("Type not found: " + enumName + " in module " + moduleName);
							json.endObject();
							return buffer.toString();
						}

						// Verify it's an enumeration
						if (!(type instanceof TLEnumeration)) {
							json.name("error").value("Type " + enumName + " is not an enumeration (it's a " + type.getModelKind() + ")");
							json.endObject();
							return buffer.toString();
						}

						TLEnumeration enumeration = (TLEnumeration) type;

						// Build enumeration metadata
						json.name("enumeration").value(TLModelUtil.qualifiedName(enumeration));
						json.name("module").value(moduleName);
						json.name("name").value(enumName);
						json.name("classifierCount").value(enumeration.getClassifiers().size());

						// Check for UnorderedEnum annotation
						UnorderedEnum unorderedAnnotation = enumeration.getAnnotation(UnorderedEnum.class);
						json.name("unordered").value(unorderedAnnotation != null);

						// Add label and description
						JsonToolResponseBuilder.writeLabelAndDescription(json,
							TLModelNamingConvention.getTypeLabelKey(enumeration));

						// Write classifiers array
						json.name("classifiers").beginArray();
						for (TLClassifier classifier : enumeration.getClassifiers()) {
							json.beginObject();
							json.name("name").value(classifier.getName());
							json.name("default").value(classifier.isDefault());
							json.name("index").value(classifier.getIndex());

							// Check for TLI18NKey annotation
							TLI18NKey i18nKey = classifier.getAnnotation(TLI18NKey.class);
							if (i18nKey != null && i18nKey.getValue() != null) {
								json.name("customI18NKey").value(i18nKey.getValue().toString());
							}

							// Add label and description
							JsonToolResponseBuilder.writeLabelAndDescription(json,
								TLModelNamingConvention.resourceKey(classifier));

							json.endObject();
						}
						json.endArray();

						json.endObject();
					}

					String result = buffer.toString();
					Logger.info("listClassifiers() result: " + result, ModelBrowserTool.class);
					return result;

				} catch (IOException ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Failed to list classifiers: " + ex.getMessage());
					Logger.error("listClassifiers() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listClassifiers() error: " + error);
					return error;
				} catch (Exception ex) {
					String error = JsonToolResponseBuilder.buildErrorJson("Error accessing enumeration: " + ex.getMessage());
					Logger.error("listClassifiers() error: " + ex.getMessage(), ex, ModelBrowserTool.class);
					System.err.println("ModelBrowserTool.listClassifiers() error: " + error);
					return error;
				}
			});
	}
}
