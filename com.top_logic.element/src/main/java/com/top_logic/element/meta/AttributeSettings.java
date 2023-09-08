/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.model.imagegallery.GalleryImage;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLFullTextRelevant;
import com.top_logic.model.annotate.TLSearchRelevant;
import com.top_logic.model.io.AttributeValueBinding;
import com.top_logic.model.io.annotation.TLExportBinding;

/**
 * Service providing access to global {@link TLStructuredTypePart} settings.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeSettings extends com.top_logic.model.annotate.util.AttributeSettings {

	/**
	 * Configuration options for {@link AttributeSettings}.
	 */
	public interface Config extends com.top_logic.model.annotate.util.AttributeSettings.Config {

		/**
		 * Attribute implementation settings.
		 */
		@Key(AttributeSetting.CONFIG_NAME)
		Map<String, AttributeSetting> getAttributeSettings();
		
	}

	/**
	 * Implementation setting for a certain {@link TLStructuredTypePart} type.
	 */
	public interface AttributeSetting extends AnnotatedConfig<TLAnnotation> {

		/** Property name of {@link #getConfigName()}. */
		String CONFIG_NAME = "config-name";

		/** Property name of {@link #getLegacyTypeCode()}. */
		String LEGACY_TYPE_CODE = "legacy-type-code";

		/**
		 * The implementation type name.
		 */
		@Name(CONFIG_NAME)
		@Mandatory
		String getConfigName();

		/**
		 * The legacy implementation identifier.
		 * 
		 * @deprecated Can be removed, after
		 *             {@link AttributeOperations#getMetaAttributeType(TLStructuredTypePart)} is
		 *             deleted.
		 */
		@Deprecated
		@Name(LEGACY_TYPE_CODE)
		@NullDefault
		Integer getLegacyTypeCode();

		/**
		 * @see com.top_logic.model.annotate.AnnotatedConfig#getAnnotations()
		 */
		@Override
		@DefaultContainer
		Collection<TLAnnotation> getAnnotations();

	}

	/**
	 * {@link AttributeSetting} by its {@link AttributeSetting#getLegacyTypeCode()}.
	 */
	private final Map<Integer, AttributeSetting> _settingsById;

	/**
	 * Legacy implementation ID its {@link AttributeSetting#getLegacyTypeCode()}.
	 */
	private final Map<String, Integer> _implementationIdByName;

	/**
	 * Creates a {@link AttributeSettings} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeSettings(InstantiationContext context, Config config) {
		super(context, config);

		_settingsById = new HashMap<>();
		_implementationIdByName = new HashMap<>();

		for (AttributeSetting setting : getConfig().getAttributeSettings().values()) {
			Class<?> constantsClass = LegacyTypeCodes.class;

			int id;
			if (setting.getLegacyTypeCode() == null) {
				try {
					id = (Integer) constantsClass.getField("TYPE_" + setting.getConfigName()).get(null);
				} catch (IllegalArgumentException ex) {
					throw noImplementationId(setting, ex);
				} catch (SecurityException ex) {
					throw noImplementationId(setting, ex);
				} catch (IllegalAccessException ex) {
					throw noImplementationId(setting, ex);
				} catch (ClassCastException ex) {
					throw noImplementationId(setting, ex);
				} catch (NoSuchFieldException ex) {
					throw noImplementationId(setting, ex);
				}

				String implNameConstant = "IMPLEMENTATION_" + setting.getConfigName();
				Object implementationName;
				try {
					implementationName = constantsClass.getField(implNameConstant).get(null);
				} catch (IllegalArgumentException ex) {
					throw noImplementationName(setting, ex);
				} catch (SecurityException ex) {
					throw noImplementationName(setting, ex);
				} catch (IllegalAccessException ex) {
					throw noImplementationName(setting, ex);
				} catch (NoSuchFieldException ex) {
					throw noImplementationName(setting, ex);
				}
				if (!setting.getConfigName().equals(implementationName)) {
					throw new ConfigurationError("Invalid implementation name constant '" + implNameConstant
						+ "' in class '" + constantsClass.getName() + "': Expected value '"
							+ setting.getConfigName() + "'.");
				}
				
				AttributeSetting clash = _settingsById.put(id, setting);
				
				if (clash != null) {
					throw new ConfigurationError("Duplicate ID for attribute implementation '"
							+ setting.getConfigName()
							+ "' and '" + clash.getConfigName() + "'.");
				}
			} else {
				id = setting.getLegacyTypeCode();
			}
			
			_implementationIdByName.put(setting.getConfigName(), id);
		}
	}

	private ConfigurationError noImplementationId(AttributeSetting setting, Exception ex) {
		return new ConfigurationError("Cannot resolve attribute implementation ID for type '"
				+ setting.getConfigName()
			+ "' in '" + LegacyTypeCodes.class + "'.", ex);
	}

	private ConfigurationError noImplementationName(AttributeSetting setting, Exception ex) {
		return new ConfigurationError("Cannot resolve attribute implementation name constant for type '"
				+ setting.getConfigName()
			+ "' in '" + LegacyTypeCodes.class + "'.", ex);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * This method returns the type of an attribute as a String
	 * 
	 * @param implementationId
	 *        the type of an attribute
	 * @return The type as string if it is a type.
	 */
	public final String getTypeAsString(int implementationId) {
		return _settingsById.get(implementationId).getConfigName();
	}

	/**
	 * The registered {@link AttributeSetting} for the given legacy implementation ID.
	 */
	protected final AttributeSetting getSetting(int implementationId) {
		AttributeSetting result = _settingsById.get(implementationId);
		if (result == null) {
			throw new IllegalArgumentException("Type '" + implementationId + "' is not registered as an attribute");
		}
		return result;
	}

	/**
	 * The implementation ID for the implementation name.
	 */
	public final int getImplementationId(String implementationName) {
		Integer id = _implementationIdByName.get(implementationName);
		if (id != null) {
			return id;
		}

		// Legacy handling: Type code as name.
		int typeCode;
		try {
			if (implementationName.startsWith("0x")) {
				typeCode = Integer.parseInt(implementationName.substring(2), 16);
			} else {
				typeCode = Integer.parseInt(implementationName);
			}
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("No legacy type code for config type '" + implementationName + "'.");
		}

		// Make sure that the code is a valid one.
		getSetting(typeCode);
		return typeCode;
	}

	/**
	 * The registered {@link AttributeSetting} for the given
	 * {@link AttributeSetting#getConfigName()}.
	 */
	public final AttributeSetting getSetting(String implementationName) {
		AttributeSetting result = getSettingOrNull(implementationName);
		if (result == null) {
			throw new IllegalArgumentException("Invalid attribute implementation name '" + implementationName + "'.");
		}
		return result;
	}

	/**
	 * The registered {@link AttributeSetting} for the given
	 * {@link AttributeSetting#getConfigName()}, or <code>null</code>, if the given type name was
	 * not registered.
	 */
	protected final AttributeSetting getSettingOrNull(String implementationName) {
		return getConfig().getAttributeSettings().get(implementationName);
	}

	/**
	 * All registered {@link AttributeSetting}s.
	 */
	protected final Collection<AttributeSetting> getSettings() {
		return getConfig().getAttributeSettings().values();
	}

	/**
	 * The {@link AttributeValueBinding} for {@link TLStructuredTypePart}s with the given type, or
	 * <code>null</code> if none is defined for the given type.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to instantiate the binding, if a custom one is
	 *        annotated at the given attribute.
	 */
	public AttributeValueBinding<?> getExportBinding(InstantiationContext context, TLStructuredTypePart attribute) {
		TLExportBinding bindingAnnotation = attribute.getAnnotation(TLExportBinding.class);
		if (bindingAnnotation != null) {
			return context.getInstance(bindingAnnotation.getImpl());
		}

		return null;
	}

	private AttributeSetting getSettingOrNull(TLType type) {
		String configType = getConfigType(type);
		if (configType == null) {
			return null;
		}
		return getSettingOrNull(configType);
	}
	
	/**
	 * Whether the given attribute should be indexed for full-text search.
	 */
	public static boolean isFullTextRelevant(TLStructuredTypePart typePart) {
		TLFullTextRelevant annotation = typePart.getAnnotation(TLFullTextRelevant.class);
		if (annotation != null) {
			return annotation.getValue();
		}
	
		return true;
	}

	/**
	 * Whether the given attribute should be visible in search contexts.
	 */
	public static boolean isSearchRelevant(TLStructuredTypePart typePart) {
		TLSearchRelevant annotation = typePart.getAnnotation(TLSearchRelevant.class);
		if (annotation != null) {
			return annotation.getValue();
		}

		return true;
	}

	/**
	 * Name for which additional unversioned configuration is provided by {@link AttributeSettings}.
	 */
	public static String getConfigType(TLStructuredTypePart attribute) {
		ConfigType attributeAnnotation = configTypeAnnotation(attribute);
		if (attributeAnnotation != null) {
			return attributeAnnotation.getValue();
		}
	
		TLType type = attribute.getType();
		boolean composite = AttributeOperations.isComposition(attribute);
		boolean multiple = attribute.isMultiple();
		boolean ordered = attribute.isOrdered();
		boolean bag = attribute.isBag();

		return getConfigType(type, composite, multiple, ordered, bag);
	}

	/**
	 * Name for which additional unversioned configuration is provided by {@link AttributeSettings}.
	 */
	public static String getConfigType(EditContext editContext) {
		ConfigType attributeAnnotation = configTypeAnnotation(editContext);
		if (attributeAnnotation != null) {
			return attributeAnnotation.getValue();
		}

		TLType type = editContext.getType();
		boolean composite = editContext.isComposition();
		boolean multiple = editContext.isMultiple();
		boolean ordered = editContext.isOrdered();
		boolean bag = editContext.isBag();

		return getConfigType(type, composite, multiple, ordered, bag);
	}

	/**
	 * Name of the unversioned configuration of an attribute with the given type and properties.
	 */
	public static String getConfigType(TLType type, boolean composite, boolean multiple, boolean ordered,
			boolean bag) {
		String typeAnnotation = getConfigType(type);
		if (typeAnnotation != null) {
			return typeAnnotation;
		}
	
		if (type.getModelKind() == ModelKind.CLASS) {
			if (composite) {
				return LegacyTypeCodes.IMPLEMENTATION_COMPOSITION;
			}
			if (multiple) {
				if (ordered) {
					if (type.getName().equals(GalleryImage.GALLERY_IMAGE_TYPE)) {
						return LegacyTypeCodes.IMPLEMENTATION_GALLERY;
					}
					if (bag) {
						return LegacyTypeCodes.IMPLEMENTATION_LIST;
					} else {
						return LegacyTypeCodes.IMPLEMENTATION_TYPEDSET;
					}
				} else {
					return LegacyTypeCodes.IMPLEMENTATION_TYPEDSET;
				}
			} else {
				return LegacyTypeCodes.IMPLEMENTATION_WRAPPER;
			}
		} else {
			return LegacyTypeCodes.IMPLEMENTATION_COMPLEX;
		}
	}

	private static String getConfigType(TLType type) {
		ConfigType typeAnnotation = type.getAnnotation(ConfigType.class);
		if (typeAnnotation != null) {
			return typeAnnotation.getValue();
		}
		if (type.getModelKind() == ModelKind.ENUMERATION) {
			return LegacyTypeCodes.IMPLEMENTATION_CLASSIFICATION;
		}
		return null;
	}

	@Override
	public <T extends TLAnnotation> T getConfiguredPartAnnotation(Class<T> annotationInterface, TLStructuredTypePart part) {
		if (annotationInterface == ConfigType.class) {
			// Avoid infinite recursion; ConfigType is a valid annotation at TLModelPart and is used
			// to identify configuration section.
			return null;
		}
		ConfigType attributeAnnotation = configTypeAnnotation(part);
		if (attributeAnnotation != null) {
			String configType = attributeAnnotation.getValue();
			AttributeSetting setting = getSettingOrNull(configType);
			if (setting != null) {
				return setting.getAnnotation(annotationInterface);
			}
		}
		return super.getConfiguredPartAnnotation(annotationInterface, part);
	}

	private static ConfigType configTypeAnnotation(TLStructuredTypePart part) {
		return part.getAnnotation(ConfigType.class);
	}

	private static ConfigType configTypeAnnotation(EditContext editContext) {
		return editContext.getAnnotation(ConfigType.class);
	}

	@Override
	public <T extends TLAnnotation> T getConfiguredTypeAnnotation(Class<T> annotationInterface, TLType type) {
		if (annotationInterface == ConfigType.class) {
			// Avoid infinite recursion; ConfigType is a valid annotation at TLModelPart and is used
			// to identify configuration section.
			return null;
		}
		AttributeSetting setting = getSettingOrNull(type);
		if (setting != null) {
			return setting.getAnnotation(annotationInterface);
		}
		return super.getConfiguredTypeAnnotation(annotationInterface, type);
	}

	/**
	 * The {@link AttributeSettings} service.
	 */
	public static AttributeSettings getInstance() {
		return (AttributeSettings) com.top_logic.model.annotate.util.AttributeSettings.getInstance();
	}

}
