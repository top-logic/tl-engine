/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;

/**
 * Encodes "arbitrary" values to XML for exchange with client-side JavaScript.
 * 
 * @see #encode(Object) for the main entry point for encoding objects.
 * 
 * TODO KHA/BHU Fix inconsitant behaviour with Long-encoding.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLValueEncoder {
    
    /** Use for calling read acessor vi aBeanInfo */
	private static final Object[] NO_ARGS = new Object[] {};
	
	/**
	 * The writer, to which this encoder writes the encoded value.
	 */
	TagWriter out;
	
	/**
	 * Repository of {@link Encoder}s for a concrete {@link Class}. 
	 */
	private static final HashMap knownClasses;

	/**
	 * The identifier, which is assigned to the next started object.
	 */
	int nextID = 0;

	/**
	 * History of all encoded objects during one pass.
	 */
	HashMap/*<Object,Integer>*/ seenObjects = new HashMap/*<Object,Integer>*/();
	
	/**
	 * Creates a new {@link XMLValueEncoder} that writes to the given writer.
	 */
	public XMLValueEncoder(TagWriter out) {
		this.out = out;
	}

	/**
	 * Encodes the given object and writes the resulting XML to the writer of
	 * this encoder.
	 * 
	 * @param obj
	 *     The object to encode.
	 */
	public void encode(Object obj) throws IOException {
		internalEncode(obj, /* addNamespace */ true);
	}

	/**
	 * Recursive implementation of {@link #encode(Object)}.
	 * 
	 * @param obj
	 *      The object to encode.
	 * @param addNamespace
	 *      Whether adding a namespace declaration on the next created
	 *      element is necessary. This encoder declares
	 *      {@link XMLValueConstants#XML_VALUE_NS} as the default
	 *      namespace. Therefore, adding a namespace declaration is only
	 *      required on the outermost created element.
	 */
	protected void internalEncode(Object obj, boolean addNamespace) throws IOException {
		if (obj == null) {
			out.beginBeginTag(XMLValueConstants.NULL_ELEMENT);
			if (addNamespace) encodeNamespace();
			out.endEmptyTag();
			return;
		}
		
		Integer knownID = (Integer) seenObjects.get(obj);
		if (knownID != null) {
			out.beginBeginTag(XMLValueConstants.REF_ELEMENT);
			if (addNamespace) encodeNamespace();
			out.writeAttribute(XMLValueConstants.ID_ATTRIBUTE, knownID.intValue());
			out.endEmptyTag();
			return;
		}

		Class objClass = obj.getClass();
		Encoder encoder = (Encoder) knownClasses.get(objClass);
		if (encoder != null) {
			encoder.encode(this, obj, addNamespace);
			return;
		}
		
		int objectID = nextID++;
		seenObjects.put(obj, Integer.valueOf(objectID));

		// Encode arrays by reading them reflectively.
		if (objClass.isArray()) {
			out.beginBeginTag(XMLValueConstants.ARRAY_ELEMENT);
			if (addNamespace) encodeNamespace();
			out.endBeginTag();
			for (int cnt = Array.getLength(obj), n = 0; n < cnt; n++) {
				internalEncode(Array.get(obj, n), false);
			}
			out.endTag(XMLValueConstants.ARRAY_ELEMENT);
			return;
		}
		
		// Special handling for maps: Encode, as if an object were transmitted
		// that contained a property for each keys in the map. 
		if (obj instanceof Map) {
			Map map = (Map) obj;
			out.beginBeginTag(XMLValueConstants.OBJECT_ELEMENT);
			if (addNamespace) encodeNamespace();
			out.endBeginTag();
			for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = (Entry) it.next();
				out.beginTag(XMLValueConstants.PROPERTY_ELEMENT);
				{
                    Object key = entry.getKey();
                    if (!(key instanceof String)) {
                        throw new IOException("Unsupportet " 
                                + (key == null ? null : key.getClass())
                                + " key for encoding Map");
                    } else {
                        out.beginTag(XMLValueConstants.NAME_ELEMENT);
                        out.writeText((String) key);
                        out.endTag(XMLValueConstants.NAME_ELEMENT);
					
                        internalEncode(entry.getValue(), false);
                    }
				}
				out.endTag(XMLValueConstants.PROPERTY_ELEMENT);
			}
			out.endTag(XMLValueConstants.OBJECT_ELEMENT);
			return;
		}

		// Special handling for collections. Encode as if an array were 
		// transmitted with an entry for each collection entry.
		if (obj instanceof Collection) {
			Collection collection = (Collection) obj;
			out.beginBeginTag(XMLValueConstants.ARRAY_ELEMENT);
			if (addNamespace) encodeNamespace();
			out.endBeginTag();
			for (Iterator it = collection.iterator(); it.hasNext(); ) {
				internalEncode(it.next(), false);
			}
			out.endTag(XMLValueConstants.ARRAY_ELEMENT);
			return;
		}
		
		// Default handling for all other JavaBean objects through inspection.
		beanEncode(obj, addNamespace, objClass);
			
		return;
	}

    /** 
     * Encode all Attributes of obj via {@link BeanInfo} of objClass.
     */
    private void beanEncode(Object obj, boolean addNamespace, Class objClass)
            throws IOException {
        try {
			BeanInfo objInfo = Introspector.getBeanInfo(objClass);
			PropertyDescriptor[] properties = objInfo.getPropertyDescriptors();

			out.beginBeginTag(XMLValueConstants.OBJECT_ELEMENT);
			if (addNamespace) encodeNamespace();
			out.endBeginTag();
			for (int cnt = properties.length, n = 0; n < cnt; n++) {
			    PropertyDescriptor pDesc      = properties[n];
			    String             pName      = pDesc.getName();
			    Method             readMethod = pDesc.getReadMethod();
                if (readMethod == null) {
                    continue; // write only Attributes
                }

			    if ("class".equals(pName)) { // do not follow getClass ;-)
			        Logger.warn("Enconding " + objClass + " via BeanInfo", this);
			        continue;
			    }
				out.beginTag(XMLValueConstants.PROPERTY_ELEMENT);
				out.beginTag(XMLValueConstants.NAME_ELEMENT);
				out.writeText(pName);
				out.endTag(XMLValueConstants.NAME_ELEMENT);
				internalEncode(readMethod.invoke(obj, NO_ARGS), /* addNamespace */ false);
				out.endTag(XMLValueConstants.PROPERTY_ELEMENT);
			}
			out.endTag(XMLValueConstants.OBJECT_ELEMENT);
		} catch (Exception ex) {
			throw (IOException) new IOException("XML encoding failed.").initCause(ex);
		}
    }
	
    /*package protected*/ final void encodeNamespace() throws IOException {
		out.writeAttribute("xmlns", XMLValueConstants.XML_VALUE_NS);
	}

	/**
	 * Resets this encoder for a new encoding.
	 */
	public final void reset() {
		this.nextID = 0;
		this.seenObjects.clear();
	}
	
	/**
	 * Sub-encoder for a concrete type.
	 */
	private interface Encoder {
		/**
		 * Encodes the given object in the context of the given top-level
		 * encoder.
		 * 
		 * @param encoder
		 *     The top-level encoder.
		 * @param obj
		 *     The object to encode.
		 * @param addNamespace
		 *     See {@link XMLValueEncoder#internalEncode(Object, boolean)}.
		 */
		void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) 
			throws IOException;
	}
	
	static {
		knownClasses = new HashMap(); // KHA, will result in two hash-collisions :-/

		knownClasses.put(String.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.STRING_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText((String) obj);
				out.endTag(XMLValueConstants.STRING_ELEMENT);
			}
		});

		knownClasses.put(Boolean.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.BOOLEAN_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Boolean) obj).toString());
				out.endTag(XMLValueConstants.BOOLEAN_ELEMENT);
			}
		});

		knownClasses.put(Character.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.STRING_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Character) obj).toString());
				out.endTag(XMLValueConstants.STRING_ELEMENT);
			}
		});

        knownClasses.put(Byte.class, new Encoder() {
            @Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
                TagWriter out = encoder.out;
                out.beginBeginTag(XMLValueConstants.INT_ELEMENT);
                if (addNamespace) encoder.encodeNamespace();
                out.endBeginTag();
                out.writeText(((Byte) obj).toString());
                out.endTag(XMLValueConstants.INT_ELEMENT);
            }
        });

		knownClasses.put(Short.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.INT_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Short) obj).toString());
				out.endTag(XMLValueConstants.INT_ELEMENT);
			}
		});

        knownClasses.put(Integer.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.INT_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Integer) obj).toString());
				out.endTag(XMLValueConstants.INT_ELEMENT);
			}
		});

        // TODO KHA/BHU either drop this or use FLOAT_ELEMENT
		knownClasses.put(Long.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.INT_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Long) obj).toString());
				out.endTag(XMLValueConstants.INT_ELEMENT);
			}
		});

		knownClasses.put(Float.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.FLOAT_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Float) obj).toString());
				out.endTag(XMLValueConstants.FLOAT_ELEMENT);
			}
		});

		knownClasses.put(Double.class, new Encoder() {
			@Override
			public void encode(XMLValueEncoder encoder, Object obj, boolean addNamespace) throws IOException {
				TagWriter out = encoder.out;
				out.beginBeginTag(XMLValueConstants.FLOAT_ELEMENT);
				if (addNamespace) encoder.encodeNamespace();
				out.endBeginTag();
				out.writeText(((Double) obj).toString());
				out.endTag(XMLValueConstants.FLOAT_ELEMENT);
			}
		});

		// TODO BHU: Add "the others"...
	}

}
