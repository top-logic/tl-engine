/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import java.util.Objects;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationProperty;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLTypePart;

/**
 * {@link BranchIdType} representing a {@link TLTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TypePart extends BranchIdType {

	/**
	 * Kind of a {@link TypePart}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	enum Kind {
		/** A {@link TLClassProperty}. */
		CLASS_PROPERTY,
		/** A {@link TLAssociationProperty}. */
		ASSOCIATION_PROPERTY,
		/** A {@link TLAssociationEnd} */
		ASSOCIATION_END,
		/** A {@link TLReference} */
		REFERENCE,
		/** A {@link TLClassifier} */
		CLASSIFIER;
	}

	/**
	 * Name of the represented {@link TLTypePart}.
	 */
	@Mandatory
	String getPartName();

	/**
	 * Setter for {@link #getPartName()}.
	 */
	void setPartName(String name);

	/**
	 * Owner {@link Type} of the represented {@link TLTypePart}.
	 */
	@Mandatory
	Type getOwner();

	/**
	 * Setter for {@link #getOwner()}.
	 */
	void setOwner(Type type);

	/**
	 * ID of the definition for this {@link TypePart}.
	 */
	@Mandatory
	TLID getDefinition();

	/**
	 * Setter for {@link #getDefinition()}.
	 */
	void setDefinition(TLID definition);

	/**
	 * The order of this part within it's {@link #getOwner()}.
	 */
	@Mandatory
	int getOrder();

	/**
	 * Setter for {@link #getOrder()}.
	 */
	void setOrder(int value);

	/**
	 * The kind of a {@link TypePart}.
	 */
	@Mandatory
	TypePart.Kind getKind();

	/**
	 * Setter for {@link #getKind()}.
	 */
	void setKind(TypePart.Kind value);

	/**
	 * Creates a new instance of the given {@link TypePart}.
	 */
	static TypePart newInstance(long branch, TLID id, String table, TypePart.Kind kind, Type owner,
			String partName, TLID definition, int order) {
		return TypePart.newInstance(TypePart.class, branch, id, table, kind, owner, partName, definition, order);
	}

	/**
	 * Creates a new instance of the given {@link TypePart}.
	 */
	static <T extends TypePart> T newInstance(Class<T> configType, long branch, TLID id, String table,
			TypePart.Kind kind, Type owner,
			String partName, TLID definition, int order) {
		T typePart = BranchIdType.newInstance(configType, branch, id, table);
		typePart.setKind(Objects.requireNonNull(kind));
		typePart.setOwner(Objects.requireNonNull(owner));
		typePart.setPartName(Objects.requireNonNull(partName));
		typePart.setDefinition(Objects.requireNonNull(definition));
		typePart.setOrder(order);
		return typePart;
	}

}

