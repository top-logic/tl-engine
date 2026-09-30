/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.kbbased.PersistentReference;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration options for a reference.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@Abstract
public interface EndAspect extends PartConfig {

	/** @see #isComposite() */
	String COMPOSITE_PROPERTY = PersistentReference.COMPOSITE_ATTR;

	/** @see #isAggregate() */
	String AGGREGATE_PROPERTY = PersistentReference.AGGREGATE_ATTR;

	/** @see #canNavigate() */
	String NAVIGATE_PROPERTY = PersistentReference.NAVIGATE_ATTR;

	/** @see #getHistoryType() */
	String HISTORY_TYPE_PROPERTY = "history-type";

	/** @see #getDeletionPolicy() */
	String DELETION_POLICY_PROPERTY = "deletion-policy";

	/**
	 * Whether the referenced object is a part of the refering object.
	 */
	@Name(COMPOSITE_PROPERTY)
	boolean isComposite();

	/** @see #isComposite() */
	void setComposite(boolean value);

	/**
	 * Whether the referring object is part of the reference object.
	 * 
	 * <p>
	 * Note: This property is only for documentation. During export, a back reference of a
	 * composition is marked "aggregate". During import, this property is not considered, because
	 * exactly back references of compositions are marked "aggregate".
	 * </p>
	 */
	@Hidden
	@Name(AGGREGATE_PROPERTY)
	boolean isAggregate();

	/** @see #isAggregate() */
	void setAggregate(boolean aggregate);

	/**
	 * Whether navigating this reference is efficient.
	 * 
	 * <p>
	 * A reference is "efficient" if there is e.g. only a simple access. Assume that every project
	 * has one project manager. To go from the project to the corresponding project manager is
	 * therefore a simple reference access.
	 * </p>
	 * 
	 * <p>
	 * The backward reference is essentially inefficient. If you want to know in which project you
	 * are the project manager then all projects are scanned and it is checked if you are the
	 * project manager of this project.
	 * </p>
	 */
	@Name(NAVIGATE_PROPERTY)
	boolean canNavigate();

	/** @see #canNavigate() */
	void setNavigate(boolean b);

	/**
	 * The type of history of the values. The values of this attribute can be either all current,
	 * historized, or a mixture of current and historized values.
	 */
	@Name(HISTORY_TYPE_PROPERTY)
	@Label("Historization")
	HistoryType getHistoryType();

	/**
	 * Setter for {@link #getHistoryType()}.
	 */
	void setHistoryType(HistoryType type);

	/**
	 * Specification how to handle the case that a referenced object stored in this reference gets
	 * deleted without the referring object being also deleted.
	 * 
	 * <p>
	 * Note: The deletion policy is only relevant for references of history type
	 * {@link HistoryType#CURRENT}.
	 * </p>
	 */
	@Name(DELETION_POLICY_PROPERTY)
	@DynamicMode(fun = CurrentOnly.class, args = @Ref(HISTORY_TYPE_PROPERTY))
	@Options(fun = DeletionPolicyOptions.class, args = @Ref(COMPOSITE_PROPERTY))
	DeletionPolicy getDeletionPolicy();

	/**
	 * @see #getDeletionPolicy()
	 */
	void setDeletionPolicy(DeletionPolicy value);

	/**
	 * {@link FieldMode} depending on a {@link HistoryType}.
	 */
	class CurrentOnly extends Function1<FieldMode, HistoryType> {
		@Override
		public FieldMode apply(HistoryType arg) {
			return arg == HistoryType.CURRENT ? FieldMode.ACTIVE : FieldMode.DISABLED;
		}
	}

	/**
	 * Option provider for {@link EndAspect#getDeletionPolicy()}.
	 */
	class DeletionPolicyOptions extends Function1<List<DeletionPolicy>, Boolean> {
		@Override
		public List<DeletionPolicy> apply(Boolean composition) {
			List<DeletionPolicy> result = Arrays.asList(DeletionPolicy.values());
			return Utils.isTrue(composition)
				? result.stream().filter(p -> p != DeletionPolicy.STABILISE_REFERENCE).toList()
				: result;
		}
	}
}

