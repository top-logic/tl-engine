/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;

/**
 * Creation of a {@link TLClassifier}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("create-classifier")
public interface CreateClassifier extends CreatePart {

	/**
	 * Description of the {@link TLClassifier} to add to {@link #getType()}.
	 */
	@DefaultContainer
	@Mandatory
	ClassifierConfig getClassifierConfig();
	
	/** @see #getClassifierConfig() */
	void setClassifierConfig(ClassifierConfig value);

	/**
	 * The name of an existing {@link TLClassifier} of {@link #getType()} before which to insert the
	 * new one.
	 * 
	 * <p>
	 * If the value is <code>null</code>, the new {@link TLClassifier} is appended to the list of
	 * classifiers.
	 * </p>
	 */
	@Nullable
	String getBefore();

	/** @see #getBefore() */
	void setBefore(String value);
}
