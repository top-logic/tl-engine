/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.reflect;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Run-time information about an indexed type.
 * 
 * @see TypeIndex#getSpecializations(Class, boolean, boolean, boolean)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ClassInfo implements TypeDescriptor {

	private boolean _public;

	private boolean _abstract;

	private boolean _interface;

	private final String _className;

	private String _tagName;

	private ClassInfo _configuration;

	private List<ClassInfo> _specializations = ImmutableList.of();

	private List<ClassInfo> _configurationOptions = ImmutableList.of();

	public ClassInfo(String className) {
		_className = className;
	}

	@Override
	public ClassInfo getConfiguration() {
		return _configuration;
	}

	/**
	 * @see #getConfiguration()
	 */
	public void setConfiguration(ClassInfo configuration) {
		_configuration = configuration;
	}

	@Override
	public String getTagName() {
		return _tagName;
	}

	/**
	 * @see #getTagName()
	 */
	public void setTagName(String tagName) {
		_tagName = tagName;
	}

	@Override
	public String getClassName() {
		return _className;
	}

	@Override
	public boolean isAbstract() {
		return _abstract;
	}

	public void setAbstract(boolean value) {
		_abstract = value;
	}

	@Override
	public boolean isInterface() {
		return _interface;
	}

	public void setInterface(boolean value) {
		_interface = value;
	}

	@Override
	public boolean isPublic() {
		return _public;
	}

	public void setPublic(boolean value) {
		_public = value;
	}

	@Override
	public List<ClassInfo> getSpecializations() {
		return _specializations;
	}

	void addSpecialization(ClassInfo specialization) {
		if (_specializations.isEmpty()) {
			_specializations = new ArrayList<>();
		}
		_specializations.add(specialization);
	}

	@Override
	public List<ClassInfo> getConfigurationOptions() {
		return _configurationOptions;
	}

	/**
	 * Adds the given type to {@link #getConfigurationOptions()}
	 * 
	 * @see #getConfigurationOptions()
	 */
	public void addConfigurationOption(ClassInfo classInfo) {
		if (_configurationOptions.isEmpty()) {
			_configurationOptions = new ArrayList<>();
		}
		_configurationOptions.add(classInfo);
	}

	void optimize() {
		_specializations = ImmutableList.copyOf(_specializations);
		_configurationOptions = ImmutableList.copyOf(_configurationOptions);
	}

}
