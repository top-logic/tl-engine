/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider;
import com.top_logic.util.Resources;


/**
 * Save the information needed for a tabber in a tabframe.
 * 
 * Since writing a Tabber is quite complex this is not an Adorner.
 *
 * @author  <a href="mailto:khanvh@top-logic.com"kha>nvh</a>
 */
public class TabInfo {
     
	/** Configuration interface of {@link TabInfo}. */
	public interface TabConfig extends ConfigurationItem {

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getImageSelected()}. */
		String IMAGE_SELECTED = "imageSelected";

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getDecorator()}. */
		String DECORATOR = "decorator";

		/** Configuration name for {@link #isRendered()}. */
		String RENDERED = "rendered";

		/** ID of a tab */
		@Name(ID)
		@Nullable
		String getId();

		/** @see #getId() */
		void setId(String id);

		/** Label/name for a tab. */
		@Name(LABEL)
		ResKey getLabel();

		/** @see #getLabel() */
		void setLabel(ResKey label);

		/** An icon for the selected state of a tab. */
		@Name(IMAGE_SELECTED)
		@Nullable
		ThemeImage getImageSelected();

		/** Icon for the tab. */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();

		/** Decorator for the tab. */
		@Name(DECORATOR)
		ComponentName getDecorator();

		/** Whether the tab should be rendered or not. */
		@Name(RENDERED)
		@BooleanDefault(true)
		boolean isRendered();

	}

	/**
	 * Creates a new {@link TabInfo} which just has a {@link TabInfo#getLabelKey()}.
	 */
    public static TabInfo newTabInfo(ResKey labelKey) {
		TabConfig tabConfig = TypedConfiguration.newConfigItem(TabConfig.class);
		tabConfig.setLabel(labelKey);
		return new TabInfo(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, tabConfig);
	}

	/** Component we are decorated by */
	private DecorationValueProvider _decorationProvider;

	private TabConfig _config;

	/** Creates a {@link TabInfo}. */
	public TabInfo(InstantiationContext context, TabConfig config) {
		_config = config;
        
		if (getLabelKey() == null && getImage() == null) {
			context.error("TabInfo needs either name or image: " + config.location());
        }
    }

	/** return some reasonable debug info */
    @Override
	public String toString() {
        return "TabInfo[" 
			+ ':' + getId()
			+ ':' + getImage() + "]";
    }

	/** Returns the name. */
    public String getLabel() {
		if (_decorationProvider != null) {
			Object[] decorations = _decorationProvider.getTabBarDecorationValues();
			return Resources.getInstance().getMessage(getLabelKey(), decorations);
		} else {
			return Resources.getInstance().getString(getLabelKey());
		}
    }

	/** Gets the {@link #_config}. */
	public TabConfig getConfig() {
		return _config;
	}

	/** Gets the label of a tab. */
	public ResKey getLabelKey() {
		return getConfig().getLabel();
	}

    /**
	 * An ID for this tab.
	 * 
	 * <p>
	 * Defaults to the resource key name of {@link #getLabel()}, if not specified.
	 * </p>
	 */
	public String getId() {
		String id = getConfig().getId();
		return id != null ? id : getLabelKey().hasKey() ? getLabelKey().getKey() : getLabelKey().toString();
    }

	/** Returns the path to the selected image. */
	public ThemeImage getImageSelected() {
		ThemeImage imageSelected = getConfig().getImageSelected();
		return imageSelected == null ? getImage() : imageSelected;
    }

	/** Returns the path to the unselected image. */
	public ThemeImage getImage() {
		return getConfig().getImage();
    }

	/** Returns the configured Decorator for this tab. */
	public ComponentName getDecorator() {
		return getConfig().getDecorator();
	}

	/** Set the decoration source. */
	public void setDecorated(DecorationValueProvider decorationProvider) {
		_decorationProvider = decorationProvider;
	}

} 
