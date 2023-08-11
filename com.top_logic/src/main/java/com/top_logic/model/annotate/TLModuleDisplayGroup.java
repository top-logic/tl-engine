/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * {@link TLModuleAnnotation} to set an object name to group the underlying module in.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
@TagName(TLModuleDisplayGroup.TAG_NAME)
public interface TLModuleDisplayGroup extends TLModuleAnnotation, StringAnnotation {

	/**
	 * Custom tag to create a {@link TLModuleDisplayGroup} annotation.
	 */
	String TAG_NAME = "display-group";

}
