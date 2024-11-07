/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import java.util.Map;

import com.top_logic.base.locking.service.ConfiguredLockService.Config.TypeConfig.OperationConfig;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation of {@link TLClass}es for specifying the lock strategy for that type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("locks")
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@AnnotationInheritance(Policy.REDEFINE)
@InApp
public interface TLLocks extends TLTypeAnnotation {

	/**
	 * The locking strategies for various abstract operations.
	 */
	@DefaultContainer
	@Name("operations")
	@Key(OperationConfig.NAME_ATTRIBUTE)
	Map<String, OperationConfig> getOperations();

}
