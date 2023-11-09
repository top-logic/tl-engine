/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.table.ConfigKey;

/**
 * {@link LayoutData} whose size value is linked to the {@link PersonalConfiguration}.
 * 
 * <p>
 * {@link PersonalizedLayoutData} can be loaded from the {@link PersonalConfiguration}, see
 * {@link #copyWithAppliedPersonalization()}. When the size value is adjusted (
 * {@link #resized(DisplayDimension, DisplayDimension)}), the new value is stored to the
 * {@link PersonalConfiguration}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersonalizedLayoutData extends DefaultLayoutData {

	private final ConfigKey _configKey;

	private final boolean _parentLayoutedHorizontally;

	/**
	 * Creates a {@link PersonalizedLayoutData}.
	 * 
	 * @param configKey
	 *        The {@link ConfigKey} under which the value is stored in the
	 *        {@link PersonalConfiguration}.
	 * @param parentLayoutedHorizontally
	 *        Whether the width or height is relevant.
	 */
	public PersonalizedLayoutData(ConfigKey configKey, boolean parentLayoutedHorizontally, DisplayDimension aWidth,
			int minWidth, int aMaxWidth, DisplayDimension aHeight,
			int minHeight, int aMaxHeight, Scrolling aScrollable) {
		super(aWidth, minWidth, aMaxWidth, aHeight, minHeight, aMaxHeight, aScrollable);
		_configKey = configKey;
		_parentLayoutedHorizontally = parentLayoutedHorizontally;
	}

	@Override
	public LayoutData resized(DisplayDimension newWidth, DisplayDimension newHeight) {
		PersonalizedLayoutData result = copyWithUpdatedSize(newWidth, newHeight);
		if (result.isNonMaximized()) {
			result.storeInPersonalConfiguration();
		}
		return result;
	}

	private PersonalizedLayoutData copyWithUpdatedSize(DisplayDimension newWidth, DisplayDimension newHeight) {
		PersonalizedLayoutData result =
			new PersonalizedLayoutData(_configKey, _parentLayoutedHorizontally, newWidth, this.getMinWidth(),
				this.getMaxWidth(), newHeight, this.getMinHeight(), this.getMaxHeight(), this.getScrollable());
		return result;
	}

	/**
	 * Whether one of the layout dimension sizes is smaller than 100%, false otherwise
	 */
	private boolean isNonMaximized() {
		DisplayDimension width = getWidthDimension();
		DisplayDimension height = getHeightDimension();
		return !(width.getValue() == 100 && width.getUnit() == DisplayUnit.PERCENT
			&& height.getValue() == 100 && height.getUnit() == DisplayUnit.PERCENT);
	}

	/**
	 * Stores layout model information in {@link PersonalConfiguration}.
	 */
	protected void storeInPersonalConfiguration() {
		DisplayDimension width = getWidthDimension();
		DisplayDimension height = getHeightDimension();
		String resolvedKey = _configKey.get();
		if (resolvedKey != null) {
			PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
			List<Object> jsonLayout = new ArrayList<>(5);
			jsonLayout.add(_parentLayoutedHorizontally);
			jsonLayout.add(width.getValue());
			jsonLayout.add(width.getUnit().getExternalName());
			jsonLayout.add(height.getValue());
			jsonLayout.add(height.getUnit().getExternalName());
			personalConfig.setJSONValue(resolvedKey, jsonLayout);
		}
	}

	/**
	 * Fetches the values from {@link PersonalConfiguration}.
	 * 
	 * @return {@link LayoutData} with values from the {@link PersonalConfiguration}, if a
	 *         personalization is found, this instance otherwise.
	 */
	public PersonalizedLayoutData copyWithAppliedPersonalization() {
		String resolvedKey = _configKey.get();
		if (resolvedKey != null) {
			PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
			List<?> jsonLayout = (List<?>) personalConfig.getJSONValue(resolvedKey);
			if (jsonLayout != null) {
				try {
					boolean isHorizontal = (Boolean) jsonLayout.get(0);
					float width = ((Number) jsonLayout.get(1)).floatValue();
					String widthUnit = (String) jsonLayout.get(2);
					float height = ((Number) jsonLayout.get(3)).floatValue();
					String heightUnit = (String) jsonLayout.get(4);
					DisplayDimension widthDimension = DisplayDimension.parseDimension(width + widthUnit);
					DisplayDimension heightDimension = DisplayDimension.parseDimension(height + heightUnit);
					if (isStoredConfigurationValid(isHorizontal, widthDimension, heightDimension)) {
						return copyWithUpdatedSize(widthDimension, heightDimension);
					}
				} catch (IndexOutOfBoundsException ex) {
					reportRestoreError(resolvedKey, ex);
				} catch (ClassCastException ex) {
					reportRestoreError(resolvedKey, ex);
				}
			}
		}

		return this;
	}

	private boolean isStoredConfigurationValid(boolean isHorizontal, DisplayDimension widthDimension,
			DisplayDimension heightDimension) {
		if (isHorizontal == _parentLayoutedHorizontally) {
			if (isHorizontal) {
				return widthDimension.getUnit() == getWidthUnit();
			} else {
				return heightDimension.getUnit() == getHeightUnit();
			}
		}
		return false;
	}

	private static void reportRestoreError(String configName, Exception ex) {
		if (Logger.isDebugEnabled(AbstractLayoutControl.class)) {
			Logger.debug("Could not restore personal configuration for layout '" +
				configName + "', due to stored configuration format " +
				"does not meet expectation. Layout restore will be omitted.", ex, AbstractLayoutControl.class);
		}
	}

}
