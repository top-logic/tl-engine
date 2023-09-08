/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.table.ConfigKey;

/**
 * Abstract implementation of {@link DialogModel} handling {@link #isResizable()},
 * {@link #hasCloseButton()}, and {@link #getHelpID()}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractDialogModel extends DefaultWindowModel implements DialogModel, LazyTypedAnnotatableMixin {

	@Inspectable
	private final boolean _resizable;

	@Inspectable
	private final boolean _closeButton;

	@Inspectable
	private final String _helpID;

	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	/** A unique {@link ConfigKey} for {@link PersonalConfiguration} */
	private ConfigKey _configKey;

	private static final String CONFIG_KEY_SIZE_SUFFIX = "dialogSize";

	/**
	 * Creates a new {@link AbstractDialogModel} without the possibility to create
	 * {@link PersonalConfiguration}s.
	 * 
	 * @param layoutData
	 *        see {@link #getLayoutData()}
	 * @param title
	 *        see {@link #getWindowTitle()}
	 * @param resizable
	 *        see {@link #isResizable()}
	 * @param closeButton
	 *        see {@link #hasCloseButton()}
	 * @param helpID
	 *        see {@link #getHelpID()}
	 */
	public AbstractDialogModel(LayoutData layoutData, HTMLFragment title, boolean resizable, boolean closeButton,
			String helpID) {
		super(layoutData, title);
		this._resizable = resizable;
		this._closeButton = closeButton;
		this._helpID = helpID;
		_configKey = ConfigKey.none();
	}

	/**
	 * Creates a new {@link AbstractDialogModel}.
	 * 
	 * @param layoutData
	 *        see {@link #getLayoutData()}
	 * @param title
	 *        see {@link #getWindowTitle()}
	 * @param resizable
	 *        see {@link #isResizable()}
	 * @param closeButton
	 *        see {@link #hasCloseButton()}
	 * @param helpID
	 *        see {@link #getHelpID()}
	 * @param configKey
	 *        see {@link #_configKey}. The {@link ConfigKey} will be derived by
	 *        {@link #CONFIG_KEY_SIZE_SUFFIX} before saving in {@link #_configKey}.
	 */
	public AbstractDialogModel(LayoutData layoutData, HTMLFragment title, boolean resizable, boolean closeButton,
			String helpID, ConfigKey configKey) {
		super(layoutData, title);
		this._resizable = resizable;
		this._closeButton = closeButton;
		this._helpID = helpID;
		_configKey = ConfigKey.derived(configKey, CONFIG_KEY_SIZE_SUFFIX);
	}

	@Override
	public boolean isResizable() {
		return _resizable;
	}

	@Override
	public boolean hasCloseButton() {
		return _closeButton;
	}

	@Override
	public String getHelpID() {
		return this._helpID;
	}

	@Override
	public void saveCustomizedSize(Dimension size) {
		String configKey = _configKey.get();
		if (configKey == null) {
			return;
		}
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		List<Integer> parameters = new ArrayList<>();
		parameters.add(size.width);
		parameters.add(size.height);
		config.setJSONValue(configKey, parameters);
	}

	@Override
	public boolean hasCustomizedSize() {
		String configKey = _configKey.get();
		if (configKey == null) {
			return false;
		}
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		if (config == null) {
			return false;
		}
		Object jsonValue = config.getJSONValue(configKey);
		return isValid(jsonValue);
	}

	@SuppressWarnings("rawtypes")
	private boolean isValid(Object jsonValue) {
		return jsonValue != null && jsonValue instanceof List && ((List) jsonValue).size() == 2;
	}

	@Override
	public Dimension getCustomizedSize() {
		PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
		List<Integer> jsonValue = (List<Integer>) config.getJSONValue(_configKey.get());
		return new Dimension(jsonValue.get(0), jsonValue.get(1));
	}

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

}

