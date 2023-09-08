/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.kbbased.PersistentReference;

/**
 * Configuration options for a reference.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface EndAspect extends PartConfig {

	/** @see #isComposite() */
	String COMPOSITE_PROPERTY = PersistentReference.COMPOSITE_ATTR;

	/** @see #isAggregate() */
	String AGGREGATE_PROPERTY = PersistentReference.AGGREGATE_ATTR;

	/** @see #canNavigate() */
	String NAVIGATE_PROPERTY = PersistentReference.NAVIGATE_ATTR;

	/** @see #getHistoryType() */
	String HISTORY_TYPE_PROPERTY = "history-type";

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

}

