/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.schema.ElementSchemaConstants;

/**
 * Definition of a reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(ElementSchemaConstants.REFERENCE_ELEMENT)
public interface ReferenceConfig extends EndAspect {

	/**
	 * Classification of a reference in forwards and backwards.
	 * 
	 * @see ReferenceConfig#getKind()
	 */
	public enum ReferenceKind implements ExternallyNamed {
		
		/**
		 * A reference with an explicitly defined association.
		 */
		NONE(Names.NONE_NAME),
		
		/**
		 * A "forwards" reference.
		 */
		FORWARDS(Names.FORWARDS_NAME),
		
		/**
		 * A "backwards" reference.
		 */
		BACKWARDS(Names.BACKWARDS_NAME);


		private ReferenceKind(String name) {
			_externalName = name;
		}
		
		private final String _externalName;
		
		@Override
		public String getExternalName() {
			return _externalName;
		}

		/**
		 * External names of enum literals.
		 */
		public interface Names {
			/**
			 * @see ReferenceKind#NONE
			 */
			String NONE_NAME = "none";

			/**
			 * @see ReferenceKind#FORWARDS
			 */
			String FORWARDS_NAME = "forwards";

			/**
			 * @see ReferenceKind#BACKWARDS
			 */
			String BACKWARDS_NAME = "backwards";
		}
	}
	
	/** @see #getEndName() */
	String END = "end";

	/** @see #getInverseReference() */
	String INVERSE_REFERENCE = "inverse-reference";
	
	/** @see #getKind() */
	String KIND = "kind";
	
	/**
	 * Name of the association end, that implements this reference.
	 */
	@Name(END)
	String getEndName();

	/** @see ReferenceConfig#getEndName() */
	void setEndName(String endName);
	
	/**
	 * In a reference with implicit association, whether the corresponding association end of the
	 * implicitly created association should be the {@link ReferenceKind#FORWARDS forwards}, (index
	 * 1) or {@link ReferenceKind#BACKWARDS backwards} (index 0) end.
	 * 
	 * <p>
	 * {@link ReferenceKind#NONE} for a reference with explicit association, see
	 * {@link #getEndName()}.
	 * </p>
	 * 
	 * <p>
	 * If the value is {@link ReferenceKind#NONE}, but {@link #getEndName()} is not set, the
	 * following holds: If {@link #getInverseReference()} is not set, the reference is considered to
	 * be {@link ReferenceKind#FORWARDS}, {@link ReferenceKind#BACKWARDS} otherwise.
	 * </p>
	 */
	ReferenceKind getKind();
	
	/** @see #getKind() */
	void setKind(ReferenceKind value);

	/**
	 * Name of the reference in the {@link #getTypeSpec() reference target type} that represents the
	 * inverse navigation.
	 * 
	 * <p>
	 * Can only be set for references of two-ended associations.
	 * </p>
	 */
	@Name(INVERSE_REFERENCE)
	String getInverseReference();
	
	/** @see ReferenceConfig#getEndName() */
	void setInverseReference(String value);
	
}
