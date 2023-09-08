/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.xml;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBIndex;

/** 
 * Constants used to export import Dataobjects to XML.
 *
 * @author   <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public interface DOXMLConstants {

	/** Generic name attribute */
	public static final String NAME_ATTRIBUTE = "name";

	/** Tag designating a {@link MetaObject} definition */
	public static final String META_OBJECT_ELEMENT = "metaobject";

	/** Attribute of the {@link #META_OBJECT_ELEMENT} describing the type of the object. */
	public static final String MO_TYPE_ATTRIBUTE = "object_type";

	/** Value for {@link #MO_TYPE_ATTRIBUTE} describing a {@link MOStructure} */
	public static final String MO_STRUCTURE_TYPE_VALUE = "MOStructure";

	/** Value for {@link #MO_TYPE_ATTRIBUTE} describing a {@link MOClass} */
	public static final String MO_CLASS_TYPE_VALUE = "MOClass";

	/** Attribute of {@link #META_OBJECT_ELEMENT} giving the name of the Object. */
	public static final String MO_NAME_ATTRIB = "object_name";

	/** Attribute of {@link #META_OBJECT_ELEMENT} naming the SuperClass */
	public static final String SUPERCLASS_ATTRIBUTE = "super_class";

	/** Attribute of {@link #META_OBJECT_ELEMENT} indicating that it is abstract */
	public static final String ABSTRACT_ATTRIBUTE = "abstract";

	/** Tag for a {@link MOAttribute}  */
	public String MO_ATTRIBUTE_ELEMENT = "mo_attribute";

	/** Name attribute of a {@link #MO_ATTRIBUTE_ELEMENT} giving the {@link MOAttribute#getName()} */
	public static final String ATT_NAME_ATTRIBUTE = "att_name";

	/** Type attribute of a {@link #MO_ATTRIBUTE_ELEMENT} */
	public static final String ATT_TYPE_ATTRIBUTE = "att_type";

	/** Value attribute of a do_attribute */
	public static final String ATT_VALUE_ATTRIBUTE = "att_value";

	/** Entry type attribute of a {@link #MO_ATTRIBUTE_ELEMENT} */
	public static final String ENTRY_TYPE_ATTRIBUTE = "entry_type";

	/** Mandatory flag for a {@link #MO_ATTRIBUTE_ELEMENT}, default true */
	public static final String MANDATORY_ATTRIBUTE = "mandatory";

	/** Immutable flag for a {@link #MO_ATTRIBUTE_ELEMENT}, default false */
	public static final String IMMUTABLE_ATTRIBUTE = "immutable";

	/** Initial flag for a {@link #MO_ATTRIBUTE_ELEMENT}, default false */
	public static final String INITIAL_ATTRIBUTE = "initial";

	/** Tag describing an {@link MOIndex} */
	public static final String MO_INDEX_ELEMENT = "mo_index";

	/** Unique flag of {@link #MO_INDEX_ELEMENT}, default true */
	public static final String UNIQUE_ATTRIBUTE = "unique";

	/** Configuration option for {@link DBIndex#isCustom()}. */
	public static final String CUSTOM_ATTRIBUTE = "custom";

	/** In-memory flag of {@link #MO_INDEX_ELEMENT}, default false */
	public static final String IN_MEMORY_ATTRIBUTE = "inMemory";

	/** Tag for a part of an {@link MOIndex}. */
	public String INDEX_PART_ELEMENT = "index_part";

	/** Attribute to define part of the {@link MOReference} to include in {@link MOIndex}. */
	public static final String PART_ATTRIBUTE = "part";

	/** Object Type attribute for the DataObject */
	public final static String OBJECT_TYPE_ATTRIBUTE = "object_type";

	/** Name (id) attribute for the Dataobject */
	public final static String OBJECT_NAME_ATTRIBUTE = "object_name";

	// DB Specific attributes 

	/** Name of an object to be used in a database */
	public final static String DB_NAME_ATTRIBUTE = "db_name";

	/** Type to be used for the attribute in the DB. */
	public final static String DB_TYPE_ATTRIBUTE = "db_type";

	/** Size of column to be used for the attribute in the DB */
	public final static String DB_SIZE_ATTRIBUTE = "db_size";

	/** Store along primary key in Database? */
	public final static String DB_PKS_ATTRIBUTE = "db_PKeyStorage";

    /** Compressprimary key in Database? */
    public final static String DB_COMPRESS_ATTRIBUTE = "db_compress";

	/** Value of {@link #DB_COMPRESS_ATTRIBUTE} if no compression is requested. */
	int NO_DB_COMPRESS = 0;

    /** Name of the attribute for specifying the {@link DBAttribute#isBinary()} property. */
    /** Precision of column to be used for the attribute in the DB */
    public final static String DB_PREC_ATTRIBUTE = "db_prec";

    public final static String BINARY_ATTRIBUTE = "binary";
	
	/**
	 * Element that produces a {@link MOReference}
	 */
	public static final String REFERENCE_ELEMENT = "reference";
	
	/**
	 * The Attribute describing the target type of the reference.
	 * 
	 * @see MOReference#getMetaObject()
	 */
	public static final String TARGET_TYPE_ATTRIBUTE = "target-type";
	
	/**
	 * Attribute describing whether the reference is branch global.
	 * 
	 * @see MOReference#isBranchGlobal()
	 */
	public static final String BRANCH_GLOBAL_ATTRIBUTE = "branch-global";
	
	/**
	 * Attribute describing whether the reference is a historic one.
	 * 
	 * @see MOReference#getHistoryType()
	 */
	public static final String HISTORIC_ATTRIBUTE = "history-type";
	
	/**
	 * Attribute describing the deletion policy of the reference.
	 * 
	 * @see MOReference#getDeletionPolicy()
	 */
	public static final String DELETION_POLICY_ATTRIBUTE = "deletion-policy";
	
	/**
	 * Attribute describing whether the reference is monomorph.
	 * 
	 * @see MOReference#isMonomorphic()
	 */
	public static final String MONOMORPHIC_ATTRIBUTE = "monomorphic";
	
	/**
	 * whether the reference is a container
	 * 
	 * @see MOReference#isContainer()
	 */
	public static final String CONTAINER_ATTRIBUTE = "is-container";
	
	/**
	 * Boolean attribute to configure whether the reference holds the id or the whole object in
	 * attribute cache.
	 */
	public static final String BY_VALUE_ATTRIBUTE = "by-value";

	/** Boolean attribute to configure whether a reference attribute must install its default index. */
	public static final String USE_DEFAULT_INDEX_ATTRIBUTE = "use-default-index";

	/** @see MOAttribute#isHidden() */
	public static final String HIDDEN_ATTRIBUTE = "hidden";

}
