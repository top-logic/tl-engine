/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.basic.ThemeImage;

/**
 * {@link MimeTypes} knowning icons for types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLMimeTypes extends MimeTypes {

	/** The map containing the mapping for images. */
	private Map<String, ThemeImage> imageMap;

	/**
	 * Mapping of pseudo MIME types to high-resolution icon references.
	 */
	private Map<String, ThemeImage> imageMapLarge;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link TLMimeTypes}.
	 */
	public TLMimeTypes(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		getImageMap();
	}

	@Override
	public boolean reload() {
		this.imageMap = null;
		this.imageMapLarge = null;
		return super.reload();
	}

	/**
	 * Check, whether the given name can be located in the list of known images.
	 *
	 * @param aName
	 *        The name to be looked up.
	 * @return true, if name can be found in list.
	 */
	public boolean hasDynamicImage(String aName) {
		return (this.getImageMap().containsKey(aName));
	}

	/**
	 * Returns the image for the given internal pseudo MIME type.
	 * 
	 * <p>
	 * The internal pseudo MIME type is either an official MIME type like <code>image/jpeg</code> or
	 * an internal type name for an application object like <code>Person</code>.
	 * </p>
	 * 
	 * @param pseudoMimeType
	 *        Must not be <code>null</code>.
	 * @return Never <code>null</code>.
	 * @throws NullPointerException
	 *         if mimeType is <code>null</code>.
	 * 
	 * @see #getMimeTypeImageLarge(String)
	 */
	public ThemeImage getMimeTypeImage(String pseudoMimeType) {
		return getMimeTypeImage(pseudoMimeType, Icons.UNKNOWN_TYPE);
	}

	/**
	 * Returns the image for the given internal pseudo MIME type.
	 * 
	 * <p>
	 * The internal pseudo MIME type is either an official MIME type like <code>image/jpeg</code> or
	 * an internal type name for an application object like <code>Person</code>.
	 * </p>
	 * 
	 * @param pseudoMimeType
	 *        Must not be <code>null</code>.
	 * @param defaultIcon
	 *        {@link ThemeImage} that serves as last fallback, if no icon was configured for the
	 *        given type.
	 * @return Never <code>null</code>.
	 * @throws NullPointerException
	 *         if mimeType is <code>null</code>.
	 * 
	 * @see #getMimeTypeImageLarge(String)
	 */
	public ThemeImage getMimeTypeImage(String pseudoMimeType, ThemeImage defaultIcon) {
		ThemeImage result = internalLookupIcon(getImageMap(), pseudoMimeType, null);
		if (result != null) {
			return result;
		}
		return ThemeImage.typeIcon(pseudoMimeType, defaultIcon);
	}

	/**
	 * Retrieve a high-resolution icon for the given pseudo MIME type.
	 * 
	 * @see #getMimeTypeImage(String)
	 */
	public ThemeImage getMimeTypeImageLarge(String pseudoMimeType) {
		return getMimeTypeImageLarge(pseudoMimeType, Icons.UNKNOWN_TYPE);
	}

	/**
	 * Retrieve a high-resolution icon for the given pseudo MIME type.
	 * 
	 * @see #getMimeTypeImage(String, ThemeImage)
	 */
	public ThemeImage getMimeTypeImageLarge(String pseudoMimeType, ThemeImage defaultIcon) {
		ThemeImage result = internalLookupIcon(getImageMapLarge(), pseudoMimeType, null);
		if (result != null) {
			return result;
		}
		return ThemeImage.typeIconLarge(pseudoMimeType, defaultIcon);
	}

	/**
	 * Retrieve the icon for an unknown type.
	 */
	public ThemeImage getUnknownImage() {
		return unknownIcon();
	}

	/**
	 * The icon for an unknown type.
	 */
	public static ThemeImage unknownIcon() {
		return Icons.UNKNOWN_TYPE;
	}

	private ThemeImage internalLookupIcon(Map<String, ThemeImage> iconsByType, String pseudoMimeType,
			ThemeImage unknownIcon) {
		ThemeImage result = iconsByType.get(pseudoMimeType);
		if (result == null) {
			return unknownIcon;
		}

		return (result);
	}

	/**
	 * Defines a dynamic mime type.
	 *
	 * @param pseudoMimeType
	 *        The type of the image to be stored.
	 * @param image
	 *        Icon reference for labels (low resolution).
	 * @param imageLarge
	 *        Icon reference for title bars (high resolution).
	 */
	public void putMimeType(String pseudoMimeType, ThemeImage image, ThemeImage imageLarge) {
		if (imageLarge == null) {
			// Default, if no large version is available.
			imageLarge = image;
		}

		if (!StringServices.isEmpty(image)) {
			this.imageMap.put(pseudoMimeType, image);
		}

		if (!StringServices.isEmpty(imageLarge)) {
			this.imageMapLarge.put(pseudoMimeType, imageLarge);
		}
	}

	/**
	 * Returns the map with the image names.
	 *
	 * @return The requested map.
	 */
	protected Map<String, ThemeImage> getImageMap() {
		if (this.imageMap == null) {
			loadImageMaps();
		}

		return (this.imageMap);
	}

	/**
	 * The internal mapping of pseudo MIME types to high-resolution image references.
	 */
	protected Map<String, ThemeImage> getImageMapLarge() {
		if (this.imageMapLarge == null) {
			loadImageMaps();
		}

		return (this.imageMapLarge);
	}

	private void loadImageMaps() {
		this.imageMap = new HashMap<>();
		this.imageMapLarge = new HashMap<>();

		IterableConfiguration legacyConfig = Configuration.getConfigurationByName("ImageMap");
		if (!legacyConfig.getProperties().isEmpty()) {
			throw new IllegalStateException(
				"Invalid legacy ImageMap configuration found: " + legacyConfig.getProperties().keySet());
		}
	}

	public static TLMimeTypes getInstance() {
		return (TLMimeTypes) MimeTypes.getInstance();
	}

}
