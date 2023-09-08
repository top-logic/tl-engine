/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.attr.MetaObjectFormat;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOFunction;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.MOTuple;
import com.top_logic.dob.meta.TypeContext;

/**
 * Type description of a {@link DataObject}.
 * 
 * @see MOAttribute for the declaration of attributes.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Format(MetaObjectFormat.class)
public interface MetaObject extends MOPart {

	/**
	 * A classification of {@link MetaObject} types.
	 * 
	 * @see MetaObject#getKind()
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public enum Kind {
		
		/**
		 * A primitive type. 
		 * 
		 * <p>
		 * Only instances of {@link MOPrimitive} must have this kind.
		 * </p>
		 */
		primitive, 
		
		/**
		 * A tuple type. 
		 * 
		 * <p>
		 * The type must implement {@link MOTuple}.
		 * </p>
		 */
		tuple, 
		
		/**
		 * A structure type (object without identity). 
		 * 
		 * <p>
		 * The type must implement {@link MOStructure}.
		 * </p>
		 */
		struct, 
		
		/**
		 * An object type. 
		 * 
		 * <p>
		 * The type must implement {@link MOClass}.
		 * </p>
		 */
		item, 
		
		/**
		 * A collection type. 
		 * 
		 * <p>
		 * The type must implement {@link MOCollection}.
		 * </p>
		 */
		collection, 
		
		/**
		 * A function type.
		 * 
		 * <p>
		 * The type must implement {@link MOFunction}.
		 * </p>
		 */
		function,
		
		/**
		 * A pure summary type that provides alternative types.
		 * 
		 * <p>
		 * The type must implement {@link MOAlternative}.
		 * </p>
		 */
		alternative,
		
		/**
		 * Every type is is assignment compatible to this kind of type.
		 * 
		 * <p>
		 * Reserved for internal use.
		 * </p>
		 */
		ANY, 

		/**
		 * The type of the <code>null</code> value.
		 * 
		 * <p>
		 * Reserved for internal use.
		 * </p>
		 */
		NULL, 
		
		/**
		 * The error type during a compilation.
		 * 
		 * <p>
		 * Reserved for internal use.
		 * </p>
		 */
		INVALID;
	}

	/**
	 * The singleton {@link Kind#ANY} type.
	 */
	MetaObject ANY_TYPE = new MOInternal("<any>", Kind.ANY);
	
	/**
	 * The singleton {@link Kind#NULL} type.
	 */
	MetaObject NULL_TYPE = new MOInternal("<null>", Kind.NULL);
	
	/**
	 * The singleton {@link Kind#INVALID} type.
	 */
	MetaObject INVALID_TYPE = new MOInternal("<invalid>", Kind.INVALID);
	
	/**
	 * The {@link Kind} of this type.
	 */
	public Kind getKind();

    /**
	 * The resource name that defines this type.
	 */
	public String getDefiningResource();

	/**
	 * Initializes the resource name from which this type was loaded.
	 */
	public void setDefiningResource(String definingResource);

	/**
	 * Checks, whether this type is the same type or a sub type of the given
	 * type.
	 * 
	 * @param otherType
	 *        The type to check for being a sub-type of this type. Must not be <code>null</code>.
	 * @return Whether instances of this type are assignment compatible to the
	 *         given type.
	 */
    public boolean isSubtypeOf(MetaObject otherType);

	/**
	 * Same as {@link #isSubtypeOf(MetaObject)} if only the name of the type to
	 * check is given.
	 */
    public boolean isSubtypeOf(String aName);

	/**
	 * Creates an (unresolved) copy of this {@link MetaObject}.
	 * 
	 * @return A copy of this {@link MetaObject}.
	 * 
	 * @see #resolve(TypeContext)
	 */
	public MetaObject copy();

	/**
	 * Resolves this type in the given context.
	 * 
	 * <p>
	 * During resolve, all type references are replaced with their referenced types.
	 * </p>
	 * 
	 * @param context
	 *        The context in which to resolve.
	 * @return The resolved type (may be the same instance as this).
	 * 
	 * @throws DataObjectException
	 *         iff the object could not be resolved
	 */
	public MetaObject resolve(TypeContext context) throws DataObjectException;

}
