/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

/**
 * Constants that formerly identified attribute implementations.
 * 
 * TODO #6121: Delete TL 5.8.0 deprecation
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class LegacyTypeCodes {

	/** Classification attribute. */
	public static final int TYPE_CLASSIFICATION = 0x0040;    // 64
	/** The type classification in String form */
	public static final String IMPLEMENTATION_CLASSIFICATION = "CLASSIFICATION";
	/** List of arbitrary wrappers attribute. */
	public static final int TYPE_LIST           = 0x0088;   // 136
	/** the type list */
	public static final String IMPLEMENTATION_LIST = "LIST";
	/** Composition attribute. */
	public static final int TYPE_COMPOSITION = 0x0013; // 19
	/** the type composition */
	public static final String IMPLEMENTATION_COMPOSITION = "COMPOSITION";
	/** Collection of typed wrappers attribute. */
	public static final int TYPE_TYPEDSET   	= 0x0100;   // 256
	/** The type typedset in String form */
	public static final String IMPLEMENTATION_TYPEDSET = "TYPEDSET";
	/** Single wrapper of a fixed type attribute. */
	public static final int TYPE_WRAPPER    	= 0x0020;    // 32
	/** The type typed wrapper in String form */
	public static final String IMPLEMENTATION_WRAPPER = "WRAPPER";
	/** binary attributes */
	public static final int TYPE_BINARY         = 0x8008; // 32776
	/** the type binary */
	public static final String IMPLEMENTATION_BINARY = "BINARY";
	/** Boolean/checkbox attribute. */
	public static final int TYPE_BOOLEAN    	= 0x0010;    // 16
	/** The type boolean in String form */
	public static final String IMPLEMENTATION_BOOLEAN = "BOOLEAN";
	/** Date attribute. */
	public static final int TYPE_DATE       	= 0x0002;     // 2
	/** The type date in String form */
	public static final String IMPLEMENTATION_DATE = "DATE";
	/** Floating point attribute. Is actually a DOUBLE ? */
	public static final int TYPE_FLOAT      	= 0x0008;     // 8
	/** The type float in String form (TODO KBU: this is actually Double ?) */
	public static final String IMPLEMENTATION_FLOAT = "FLOAT";
	/** Long number attribute. */
	public static final int TYPE_LONG       	= 0x0004;     // 4
	/** The type long in String form */
	public static final String IMPLEMENTATION_LONG = "LONG";
	/** Text attribute. */
	public static final int TYPE_STRING     	= 0x0001;     // 1
	/** The type String in String form */
	public static final String IMPLEMENTATION_STRING = "STRING";
	/** Check-list. */
	public static final int TYPE_CHECKLIST      = 0x8002; // 32770
	/** The type Checklist. */
	public static final String IMPLEMENTATION_CHECKLIST = "CHECKLIST";
	/** External contact attribute type. */
	public static final int TYPE_EXTERNAL_CONTACT = 0x8005;
	/** External contact attribute type. */
	public static final String IMPLEMENTATION_EXTERNAL_CONTACT = "EXTERNAL_CONTACT";
	/** Set of Strings. */
	public static final int TYPE_STRING_SET     = 0x4000; // 16384
	/** The type StringSet in String form */
	public static final String IMPLEMENTATION_STRING_SET = "STRING_SET";
	/** The type collection in String form */
	public static final String IMPLEMENTATION_COLLECTION = "COLLECTION";
	/** Collection of arbitrary wrappers attribute. */
	public static final int TYPE_COLLECTION 	= 0x0080;   // 128
	/** The type Structure in String form */
	public static final String IMPLEMENTATION_STRUCTURE = "STRUCTURE";
	/** Set of StructuredElements. */
	public static final int TYPE_STRUCTURE      = 0x0800;  // 2048
	/** Reference to historic Wrapper. */
	public static final int TYPE_HISTORIC_WRAPPER = 0x8888;
	/** the type binary */
	public static final String IMPLEMENTATION_HISTORIC_WRAPPER = "HISTORIC_WRAPPER";
	/** calculated attributes */
	public static final int TYPE_CALCULATED     = 0x8004; // 32772
	/** the type calculated */
	public static final String IMPLEMENTATION_CALCULATED = "CALCULATED";
	/** Set of SAP Objects. */
	public static final int TYPE_DAP_COLLECTION = 0x2000;  // 8192
	/** Set of DAP Objects. */
	public static final String IMPLEMENTATION_DAP_COLLECTION = "DAP_COLLECTION";
	/** DAP-Object will fall-back to local storage. */
	public static final int TYPE_DAP_FALLB      = 0x8000; // 32768
	/** Single DAP object with fallback to local storage */
	public static final String IMPLEMENTATION_DAP_FALLB = "DAP_FALLB";
	/** The type reference in String form */
	public static final String IMPLEMENTATION_SINGLE_REFERENCE = "SINGLE_REFERENCE";
	/** Reference to one other Wrapper. */
	public static final int TYPE_SINGLE_REFERENCE = 3;
	/** The type wrapper in String form */
	public static final String IMPLEMENTATION_SINGLEWRAPPER = "SINGLEWRAPPER";
	/** Single untyped wrapper attribute. */
	public static final int TYPE_SINGLEWRAPPER	= 0x0200;   // 512
	/** Set of StructuredElements. */
	public static final int TYPE_SINGLE_STRUCTURE = 0x0888; // 2184
	/** The type Structure in String form */
	public static final String IMPLEMENTATION_SINGLE_STRUCTURE = "SINGLE_STRUCTURE";
	/** External contact attribute single assignemt type. */
	public static final int TYPE_SINGLE_EXTERNAL_CONTACT = 0x8006;
	/** External contact attribute single assignemt type. */
	public static final String IMPLEMENTATION_SINGLE_EXTERNAL_CONTACT = "SINGLE_EXTERNAL_CONTACT";
	/** Single SAP object. */
	public static final int TYPE_DAP            = 0x1000;  // 4096
	/** Single DAP object. */
	public static final String IMPLEMENTATION_DAP = "DAP";
	/** an single attribute with a defined range of values */ 
	public static final int TYPE_COMPLEX        = 0x0011;    // 17
	/** The type boolean in String form */
	public static final String IMPLEMENTATION_COMPLEX = "COMPLEX";

	public static final int TYPE_GALLERY = 0x0012; // 18

	/** The type boolean in String form */
	public static final String IMPLEMENTATION_GALLERY = "GALLERY";

}
