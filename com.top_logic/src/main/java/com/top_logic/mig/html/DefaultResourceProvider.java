/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.layout.provider.icon.StaticIconProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLMimeTypes;

/**
 * A {@link com.top_logic.layout.ResourceProvider} implementation that transforms model objects into
 * textual representations by Java-built-in mechanisms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultResourceProvider extends AbstractResourceProvider {

	/**
	 * Singleton {@link DefaultResourceProvider} instance.
	 */
	public static final DefaultResourceProvider INSTANCE = new DefaultResourceProvider();

	/**
	 * Creates a {@link DefaultResourceProvider}.
	 */
	protected DefaultResourceProvider() {
		// Singleton constructor that allows sub-classes.
	}
	
	@Override
	public String getType(Object object) {
		if (object instanceof TLObject) {
			TLStructuredType type = getModelType((TLObject) object);
			if (type != null) {
				return typeKey(type);
			}
		}
		if (object instanceof ConfigurationItem) {
			Class<?> type = ((ConfigurationItem) object).descriptor().getConfigurationInterface();
			return type.getName();
		}
		if (object == null) {
            return Object.class.getName();
        }
		return object.getClass().getName();
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object instanceof TLObject) {
			TLStructuredType type = getModelType((TLObject) object);
			if (type == null) {
				return null;
			}
			IconProvider provider = getIconProvider(type);
			return provider.getIcon(object, flavor);
		}
		else if (object == null) {
			return null;
		} else {
			return getTypeImage(getType(object), flavor, defaultImage());
		}
	}

	private static IconProvider getIconProvider(TLType type) {
		return TLModelCacheService.getOperations().getIconProvider(type);
	}

	/**
	 * The icon to use, if none is configured otherwise.
	 */
	protected ThemeImage defaultImage() {
		return ThemeImage.none();
	}

	/**
	 * {@link TLObject#tType()} for valid {@link TLObject}s, <code>null</code> otherwise.
	 */
	protected static TLStructuredType getModelType(TLObject object) {
		if (!object.tValid()) {
			return null;
		}
		return object.tType();
	}

	/**
	 * The {@link ThemeImage} associated with the given type.
	 */
	public static ThemeImage getTypeImage(TLType type, Flavor flavor) {
		return getTypeImage(type, flavor, ThemeImage.none());
	}

	private static ThemeImage getTypeImage(TLType type, Flavor flavor, ThemeImage defaultIcon) {
		if (type == null) {
			return null;
		}
		IconProvider provider = getIconProvider(type);
		ThemeImage annotatedImage = provider.getIcon(null, flavor);
		if (annotatedImage != null) {
			return annotatedImage;
		}

		return getTypeImage(typeKey(type), flavor, defaultIcon);
	}

	private static String typeKey(TLType type) {
		return TLModelUtil.qualifiedNameDotted(type);
	}

	/**
	 * Returns the file link for the type image of the given mime-type in the given flavour.
	 * 
	 * <p>
	 * If no image is found for the given flavour, the image for the {@link Flavor#DEFAULT} is
	 * returned.
	 * </p>
	 * 
	 * @param mimeType
	 *        The mime-type to get image for.
	 * @param flavor
	 *        The flavour in which the image is requested.
	 * 
	 * @return The link for the requested icon in the current theme or <code>null</code> if nothing
	 *         is found.
	 */
	public static ThemeImage getTypeImage(String mimeType, Flavor flavor) {
		return getTypeImage(mimeType, flavor, ThemeImage.none());
	}

	/**
	 * Returns the file link for the type image of the given mime-type in the given flavour.
	 * 
	 * <p>
	 * If no image is found for the given flavour, the image for the {@link Flavor#DEFAULT} is
	 * returned.
	 * </p>
	 * 
	 * @param mimeType
	 *        The mime-type to get image for.
	 * @param flavor
	 *        The flavour in which the image is requested.
	 * 
	 * @return The link for the requested icon in the current theme or <code>null</code> if nothing
	 *         is found.
	 */
	public static ThemeImage getTypeImage(String mimeType, Flavor flavor, ThemeImage defaultIcon) {
		TLMimeTypes mimeTypes = TLMimeTypes.getInstance();

		ThemeImage result;
		if (StaticIconProvider.isEnlarged(flavor)) {
			result = mimeTypes.getMimeTypeImageLarge(mimeType, defaultIcon);
		} else {
			result = mimeTypes.getMimeTypeImage(mimeType, defaultIcon);
		}
		return result;
	}

	/**
	 * The default implementation is to use {@link String#valueOf(Object)} as textual representation
	 * for the given object.
	 * 
	 * @see ResourceProvider#getLabel(Object)
	 */
	@Override
	public String getLabel(Object object) {
		return (object == null) ? "" : object.toString();
	}

}
