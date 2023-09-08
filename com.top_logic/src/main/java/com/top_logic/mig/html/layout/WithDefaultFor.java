/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.resources.TLPartScopedResourceProvider;
import com.top_logic.model.util.AllClasses;

/**
 * Configuration fragment allowing to specify that a component is the default view for some types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface WithDefaultFor extends ConfigurationItem {

	/** @see #getDefaultFor() */
	String DEFAULT_FOR = "defaultFor";

	/**
	 * For the configured types, this component is the default view.
	 * 
	 * <p>
	 * The default view for a type is opened, whenever an object of that type is opened through a
	 * link.
	 * </p>
	 */
	@Name(DEFAULT_FOR)
	@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
	@ControlProvider(SelectionControlProvider.class)
	@OptionLabels(TLPartScopedResourceProvider.class)
	@Format(CommaSeparatedStrings.class)
	List<String> getDefaultFor();

}
