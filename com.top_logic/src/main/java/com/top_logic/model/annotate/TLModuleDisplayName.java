/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * {@link TLModuleAnnotation} to define the modules display name.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
@TagName(TLModuleDisplayName.TAG_NAME)
public interface TLModuleDisplayName extends TLModuleAnnotation, StringAnnotation {

	/**
	 * Custom tag to create a {@link TLModuleDisplayName} annotation.
	 */
	String TAG_NAME = "display-name";

}
