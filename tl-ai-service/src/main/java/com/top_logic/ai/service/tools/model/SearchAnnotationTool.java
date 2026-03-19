/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo;
import com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse;
import com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult;
import com.top_logic.ai.service.tools.model.messages.ImplementationOption;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLClassifierAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.util.model.ModelService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for listing available annotation types for TopLogic model elements.
 * <p>
 * This tool allows AI agents to discover which annotation types can be applied to a given model
 * element. It enumerates all {@link InApp @InApp} annotation types filtered by the target element's
 * kind and type.
 * </p>
 *
 * @see ModelAnnotationBrowserTool
 * @see AnnotationDefinitionExplorerTool
 * @see ModelAnnotationEditorTool
 */
public class SearchAnnotationTool {

	/**
	 * Configuration for {@link SearchAnnotationTool}.
	 */
	public interface Config extends PolymorphicConfiguration<SearchAnnotationTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link SearchAnnotationTool}.
	 */
	public SearchAnnotationTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link SearchAnnotationTool} from configuration.
	 */
	@CalledByReflection
	public SearchAnnotationTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Lists all available annotation types for a given target model element.
	 *
	 * <p>
	 * Returns all {@link InApp @InApp} annotation types applicable to the target, filtered by
	 * marker interface and {@link TLTypeKind}.
	 * </p>
	 *
	 * @param targetRef
	 *        Target reference (e.g. "module", "module:Type", "module:Type#part").
	 * @return JSON object listing all applicable annotation types.
	 */
	@Tool("List all available annotation types for a given model element. "
		+ "Returns every applicable annotation that can be set on the target.")
	public String listAvailableAnnotations(
			@P("Target reference (e.g. 'module:Type#part')") String targetRef) {

		try {
			if (targetRef == null || targetRef.trim().isEmpty()) {
				Map<String, Object> args = new HashMap<>();
				args.put("targetRef", targetRef);
				return DataClassSerializer.errorToJson(
					"Target reference is required.",
					SearchAnnotationTool.class, "listAvailableAnnotations()", args);
			}

			AnnotationSearchResponse response = ThreadContextManager.inSystemInteraction(
				SearchAnnotationTool.class,
				() -> {
					TargetRefParser parser = TargetRefParser.parse(targetRef.trim());
					TLModelPart targetPart = parser.resolve(ModelService.getApplicationModel());
					String targetKind = TargetRefParser.determineKind(targetPart, parser);

					Class<? extends TLAnnotation> markerInterface = TLAnnotation.class;
					TLTypeKind targetTypeKind = null;

					if (targetPart instanceof TLStructuredTypePart) {
						markerInterface = TLAttributeAnnotation.class;
						targetTypeKind = TLTypeKind.getTLTypeKind((TLStructuredTypePart) targetPart);
					} else if (targetPart instanceof TLClassifier) {
						markerInterface = TLClassifierAnnotation.class;
					} else if (targetPart instanceof TLType) {
						markerInterface = TLTypeAnnotation.class;
						targetTypeKind = TLTypeKind.getTLTypeKind((TLType) targetPart);
					} else if (targetPart instanceof TLModule) {
						markerInterface = TLModuleAnnotation.class;
					}

					Collection<Class<?>> annotationTypes =
						TypeIndex.getInstance().getSpecializations(markerInterface, true, true, false);

					List<AnnotationSearchResult> results = new ArrayList<>();
					for (Class<?> annotationType : annotationTypes) {
						InApp inApp = annotationType.getAnnotation(InApp.class);
						if (inApp == null || !inApp.value()) {
							continue;
						}

						if (targetTypeKind != null) {
							TargetType targetType = annotationType.getAnnotation(TargetType.class);
							if (targetType != null) {
								boolean matchesKind = false;
								for (TLTypeKind kind : targetType.value()) {
									if (kind == targetTypeKind) {
										matchesKind = true;
										break;
									}
								}
								if (!matchesKind) {
									continue;
								}
							}
						}

						String tagName = getTagName(annotationType);
						String description = getDescription(annotationType);

						AnnotationSearchResult result = AnnotationSearchResult.create();
						result.setTagName(tagName);
						result.setConfigInterface(annotationType.getName());
						if (description != null) {
							result.setDescription(description);
						}

						for (AnnotationPropertyInfo propInfo : getPolymorphicProperties(annotationType, targetTypeKind)) {
							result.addImplementationOption(propInfo);
						}
						results.add(result);
					}

					AnnotationSearchResponse resp = AnnotationSearchResponse.create();
					resp.setTotalMatches(results.size());
					resp.setTargetRef(targetRef);
					resp.setTargetKind(targetKind);
					for (AnnotationSearchResult r : results) {
						resp.addResult(r);
					}
					return resp;
				});

			Map<String, Object> args = new HashMap<>();
			args.put("targetRef", targetRef);
			return DataClassSerializer.toJson(
				response, SearchAnnotationTool.class, "listAvailableAnnotations()", args);

		} catch (Exception ex) {
			Logger.error("listAvailableAnnotations() error: " + ex.getMessage(), ex, SearchAnnotationTool.class);
			Map<String, Object> args = new HashMap<>();
			args.put("targetRef", targetRef);
			return DataClassSerializer.errorToJson(
				"Error listing annotations: " + ex.getMessage(),
				SearchAnnotationTool.class,
				"listAvailableAnnotations()",
				args);
		}
	}

	/**
	 * Extracts the tag name from a configuration interface's {@link TagName} annotation.
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @return The tag name, or the simple class name in lowercase if no {@link TagName} annotation
	 *         is present.
	 */
	private static String getTagName(Class<?> annotationType) {
		TagName tagNameAnnotation = annotationType.getAnnotation(TagName.class);
		if (tagNameAnnotation != null) {
			return tagNameAnnotation.value();
		}
		return annotationType.getSimpleName().toLowerCase(Locale.ROOT);
	}

	/**
	 * Looks up a human-readable description for a configuration property from i18n resources.
	 *
	 * @param configType
	 *        The configuration interface class.
	 * @param property
	 *        The property descriptor.
	 * @return The description string, or {@code null} if no resource is available.
	 */
	private static String getPropertyDescription(Class<?> configType, PropertyDescriptor property) {
		ResKey labelKey = ResKey.forClass(configType).suffix(property.getPropertyName());
		String tooltip = Resources.getInstance().getString(labelKey.tooltip(), null);
		if (tooltip != null) {
			return tooltip;
		}
		return Resources.getInstance().getString(labelKey, null);
	}

	/**
	 * Looks up a human-readable description for the annotation type from i18n resources.
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @return The description string, or {@code null} if no resource is available.
	 */
	private static String getDescription(Class<?> annotationType) {
		ResKey labelKey = ResKey.forClass(annotationType);
		String tooltip = Resources.getInstance().getString(labelKey.tooltip(), null);
		if (tooltip != null) {
			return tooltip;
		}
		return Resources.getInstance().getString(labelKey, null);
	}

	/**
	 * Discovers polymorphic configuration properties and their available implementations.
	 *
	 * <p>
	 * For each property of kind {@link PropertyKind#ITEM} that has a non-null
	 * {@link PropertyDescriptor#getInstanceType() instance type}, enumerates all
	 * {@link InApp @InApp} specializations as implementation options.
	 * </p>
	 *
	 * @param annotationType
	 *        The annotation configuration interface class.
	 * @param targetTypeKind
	 *        The target element's type kind for classifier-based filtering. May be {@code null}.
	 * @return List of {@link AnnotationPropertyInfo} for polymorphic properties, empty if none.
	 */
	private static List<AnnotationPropertyInfo> getPolymorphicProperties(Class<?> annotationType,
			TLTypeKind targetTypeKind) {
		List<AnnotationPropertyInfo> propertyInfos = new ArrayList<>();

		ConfigurationDescriptor descriptor;
		try {
			descriptor = TypedConfiguration.getConfigurationDescriptor(annotationType);
		} catch (Exception ex) {
			Logger.warn("Cannot get descriptor for " + annotationType.getName(), ex, SearchAnnotationTool.class);
			return propertyInfos;
		}

		for (PropertyDescriptor property : descriptor.getPropertiesOrdered()) {
			if (property.kind() != PropertyKind.ITEM) {
				continue;
			}

			Class<?> instanceType = property.getInstanceType();
			if (instanceType == null) {
				continue;
			}

			List<ImplementationOption> options = findInAppConfigInterfaces(instanceType, targetTypeKind);

			if (options.isEmpty()) {
				continue;
			}

			AnnotationPropertyInfo info = AnnotationPropertyInfo.create();
			info.setName(property.getPropertyName());
			info.setRequired(property.isMandatory());

			String propertyDescription = getPropertyDescription(annotationType, property);
			if (propertyDescription != null) {
				info.setDescription(propertyDescription);
			}

			for (ImplementationOption option : options) {
				info.addImplementation(option);
			}

			propertyInfos.add(info);
		}

		return propertyInfos;
	}

	/**
	 * Finds configuration interfaces for concrete implementations of the given type.
	 *
	 * <p>
	 * Searches for concrete subclasses of the instance type, then looks for their inner
	 * configuration interfaces (the standard TopLogic pattern where {@code MyImpl} has an inner
	 * {@code Config} interface extending {@link PolymorphicConfiguration}). Filters by
	 * {@link InApp @InApp} on the implementation class and matches classifiers against the target's
	 * {@link TLTypeKind}.
	 * </p>
	 *
	 * @param instanceType
	 *        The base implementation type (e.g. {@code StorageImplementation.class}).
	 * @param targetTypeKind
	 *        The target element's type kind, used for classifier filtering. May be {@code null}.
	 * @return Sorted list of {@link ImplementationOption} for each matching config interface found.
	 */
	private static List<ImplementationOption> findInAppConfigInterfaces(Class<?> instanceType,
			TLTypeKind targetTypeKind) {
		List<ImplementationOption> options = new ArrayList<>();

		Set<String> targetClassifiers = getTargetClassifiers(targetTypeKind);

		Collection<Class<?>> implClasses =
			TypeIndex.getInstance().getSpecializations(instanceType, true, false, false);

		for (Class<?> implClass : implClasses) {
			InApp implInApp = implClass.getAnnotation(InApp.class);
			if (implInApp == null || !implInApp.value()) {
				continue;
			}

			String[] classifiers = implInApp.classifiers();
			if (classifiers.length > 0 && !targetClassifiers.isEmpty()) {
				boolean matches = false;
				for (String classifier : classifiers) {
					if (targetClassifiers.contains(classifier)) {
						matches = true;
						break;
					}
				}
				if (!matches) {
					continue;
				}
			}

			for (Class<?> inner : implClass.getDeclaredClasses()) {
				if (!inner.isInterface()) {
					continue;
				}
				if (!PolymorphicConfiguration.class.isAssignableFrom(inner)) {
					continue;
				}

				ImplementationOption option = ImplementationOption.create();
				option.setTagName(getTagName(inner));
				option.setConfigInterface(inner.getName());

				String description = getDescription(inner);
				if (description != null) {
					option.setDescription(description);
				}

				options.add(option);
			}
		}

		return options;
	}

	/**
	 * Determines the set of classifiers that match the given target type kind.
	 *
	 * @param targetTypeKind
	 *        The target element's type kind. May be {@code null}.
	 * @return Set of classifier strings matching the target, empty if no kind is specified.
	 */
	private static Set<String> getTargetClassifiers(TLTypeKind targetTypeKind) {
		if (targetTypeKind == null) {
			return Set.of();
		}
		switch (targetTypeKind) {
			case REF:
			case COMPOSITION:
				return Set.of("reference");
			default:
				return Set.of("primitive");
		}
	}

}
