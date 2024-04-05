/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.impl.generated.TLPrimitiveBase;

/**
 * A data type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLPrimitive extends DBColumnType, TLPrimitiveBase {

	/**
	 * Enumeration of all built-in data types.
	 * 
	 * @see TLTypeKind
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	enum Kind implements ExternallyNamed {
		/**
		 * A boolean value.
		 */
		BOOLEAN("Boolean"),

		/**
		 * A boolean value plus <code>null</code>.
		 */
		TRISTATE("Tristate"),

		/**
		 * A floating point number.
		 * 
		 * <p>
		 * No precision e.g. <code>float</code>, or <code>double</code> is
		 * implied.
		 * </p>
		 */
		FLOAT("Float"),
		
		/**
		 * An integer number.
		 * 
		 * <p>
		 * No size constraint e.g. <code>int</code>, or <code>long</code> is
		 * implied.
		 * </p>
		 */
		INT("Integer"),
		
		/**
		 * A string of characters.
		 */
		STRING("String"),
		
		/**
		 * A date value.
		 */
		DATE("Date"),
		
		/**
		 * A string of <code>byte</code> values.
		 */
		BINARY("Binary"),

		/**
		 * An application-defined data type.
		 */
		CUSTOM("Custom");

		private String _externalName;

		private Kind(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

		private static final Map<String, Kind> _byExternalName = new HashMap<>();
		static {
			for (Kind kind : values()) {
				_byExternalName.put(kind.getExternalName(), kind);
			}
		}

		public static Kind byExternalName(String name) {
			return _byExternalName.get(name);
		}
	}

	/**
	 * The kind of this primitive type.
	 */
	Kind getKind();

	/**
	 * The {@link StorageMapping} that converts application values back and forth to storage values.
	 */
	StorageMapping<?> getStorageMapping();

	/**
	 * Updates the {@link #getStorageMapping()}.
	 * 
	 * <p>
	 * Note: This operation is only safe during migrations. Under no circumstances, this method must
	 * be used during production.
	 * </p>
	 */
	void setStorageMapping(StorageMapping<?> value);

	@Override
	default ModelKind getModelKind() {
		return ModelKind.DATATYPE;
	}

	@Override
	default <R, A> R visitType(TLTypeVisitor<R, A> v, A arg) {
		return v.visitPrimitive(this, arg);
	}

}
