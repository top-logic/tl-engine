/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.OnlySetIfUnset;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * {@link AttributeConfig} for a reference valued attribute in an {@link MetaObject}.
 * 
 * @see PrimitiveAttributeConfig
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReferenceAttributeConfig extends AttributeConfig {

	/**
	 * The type of values that can be stored in this reference.
	 * 
	 * <p>
	 * If this reference is {@link #isMonomorphic()}, only objects of exactly that type can be
	 * stored in this reference. A polymorphic reference in contrast can also store objects of
	 * subtypes of this type.
	 * </p>
	 * 
	 * @see MOReference#getMetaObject()
	 */
	@Mandatory
	@Name(DOXMLConstants.TARGET_TYPE_ATTRIBUTE)
	String getValueType();

	/**
	 * Cache strategy for this reference.
	 * 
	 * <p>
	 * A by-value reference stores the actual referenced object directly in the cache. A
	 * by-identifier reference only stores the {@link ObjectKey identifier} of the referenced
	 * object. By default, the system decides about the cache strategy.
	 * </p>
	 * 
	 * <p>
	 * Note: A by-value reference must only be used in rare cases, where maximum lookup-performance
	 * is required. A by value reference prevents the referenced object from being garbage
	 * collected, if the referring object is pinned. A cycle of by-value references effectively
	 * creates a memory leak because those object can never be garbage collected.
	 * </p>
	 * 
	 * @see MOFactory#createMOReference(String, MetaObject, Boolean)
	 */
	@Name(DOXMLConstants.BY_VALUE_ATTRIBUTE)
	Decision isReferenceByValue();
	
	/**
	 * Whether this reference can point to objects in other branches.
	 * 
	 * <p>
	 * A branch local reference can only point to objects of the branch the referring object lives
	 * in. Branch local references are more efficient in terms of time and space.
	 * </p>
	 * 
	 * @see MOReference#isBranchGlobal()
	 */
	@Name(DOXMLConstants.BRANCH_GLOBAL_ATTRIBUTE)
	@BooleanDefault(false)
	boolean isBranchGlobal();

	/**
	 * Whether this reference can point to historic objects.
	 * 
	 * <p>
	 * The most common references are of {@link HistoryType} {@link HistoryType#CURRENT}. Those
	 * references point always to object in the same time slice as the referring object lives in.
	 * References of type {@link HistoryType#CURRENT} are most efficient in therms of time and
	 * space.
	 * </p>
	 * 
	 * @see MOReference#getHistoryType()
	 */
	@Name(DOXMLConstants.HISTORIC_ATTRIBUTE)
	@NonNullable
	HistoryType getHistoryType();
	
	/**
	 * Whether this reference is monomorphic.
	 * 
	 * <p>
	 * A monomorphic reference can only point to objects of the exact type given in
	 * {@link #getValueType()}, not to subtypes thereof. Monomporphic references are more efficient
	 * in terms of time and space.
	 * </p>
	 * 
	 * @see MOReference#isMonomorphic()
	 */
	@Name(DOXMLConstants.MONOMORPHIC_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isMonomorphic();
	
	/**
	 * Whether the referenced object is the contents of the referring object. The contents of a
	 * container is deleted, if the referring object is deleted. A container propagates deletion to
	 * its contents.
	 * 
	 * @see MOReference#isContainer()
	 */
	@Name(DOXMLConstants.CONTAINER_ATTRIBUTE)
	@BooleanDefault(false)
	boolean isContainer();

	/**
	 * The policy that defines how to react on the deletion of the referenced object.
	 * 
	 * @see MOReference#getDeletionPolicy()
	 */
	@Name(DOXMLConstants.DELETION_POLICY_ATTRIBUTE)
	@NonNullable
	DeletionPolicy getDeletionPolicy();
	
	/**
	 * Whether a default index is created for this reference.
	 * 
	 * <p>
	 * For efficient deletion checks, an index is required for each reference. If no default index
	 * is created, a custom index must be defined that also allows efficient backwards navigation.
	 * </p>
	 * 
	 * @see MOReference#useDefaultIndex(boolean)
	 */
	@Constraint(value = OnlySetIfUnset.class, args = @Ref(OVERRIDE))
	@Name(DOXMLConstants.USE_DEFAULT_INDEX_ATTRIBUTE)
	@BooleanDefault(true)
	boolean isUseDefaultIndex();
}

