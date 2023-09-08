/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link AttributeValueBinding} using configured key extractor and key resolver functions in
 * TL-Script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class AttributeValueBindingByExpression<C extends AttributeValueBindingByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements AttributeValueBinding<Object> {

	/**
	 * Configuration options for {@link AttributeValueBindingByExpression}.
	 */
	public interface Config<I extends AttributeValueBindingByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getReferenceTag()
		 */
		String REFEREMCE_TAG = "reference-tag";

		/**
		 * @see #getKeyAttributes()
		 */
		String KEY_ATTRIBUTES = "key-attributes";

		/**
		 * @see #getKeyFun()
		 */
		String KEY_FUN = "key-fun";

		/**
		 * @see #getResolveFun()
		 */
		String RESOLVER_FUN = "resolver-fun";

		/**
		 * The tag name to use for storing multiple references for a
		 * {@link TLStructuredTypePart#isMultiple() multiple} attribute.
		 */
		@Name(REFEREMCE_TAG)
		@StringDefault("ref")
		String getReferenceTag();

		/**
		 * The attribute names to store retrieved key values in.
		 * 
		 * <p>
		 * If more than one attribute name is configured, the {@link #getKeyFun()} is expected to
		 * compute a list with as many entries as key attribute names are given.
		 * </p>
		 * 
		 * <p>
		 * If only a single key attribute name is given, the {@link #getKeyFun()} may compute a
		 * single value to use as key.
		 * </p>
		 */
		@Name(KEY_ATTRIBUTES)
		@Format(CommaSeparatedStrings.class)
		@Mandatory
		List<String> getKeyAttributes();

		/**
		 * Function computing a key from a given object.
		 * 
		 * <p>
		 * The function must retrieve as many values as {@link #getKeyAttributes()} contains names.
		 * </p>
		 * 
		 * <p>
		 * If multiple key values are returned, they must be wrapped in a list. The result may
		 * contain key values of any type (including <code>null</code>), but only their canonical
		 * string-representation is stored and passed to {@link #getResolveFun()} later on for
		 * retrieval.
		 * </p>
		 */
		@Name(KEY_FUN)
		@Mandatory
		Expr getKeyFun();

		/**
		 * Function resolving an object given as many values as {@link #getKeyAttributes()} contains
		 * names.
		 * 
		 * <p>
		 * If {@link #getKeyAttributes()} contains a single name, a one-argument function is
		 * expected. If {@link #getKeyAttributes()} contains <code>n</code> names, a
		 * <code>n</code>-argument function is expected. All key arguments passed in are either of
		 * type {@link String} or <code>null</code>.
		 * </p>
		 */
		@Name(RESOLVER_FUN)
		@Mandatory
		Expr getResolveFun();

	}

	private QueryExecutor _keyFun;

	private QueryExecutor _resolveFun;

	/**
	 * Creates a {@link AttributeValueBindingByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeValueBindingByExpression(InstantiationContext context, C config) {
		super(context, config);

		_keyFun = QueryExecutor.compile(config.getKeyFun());
		_resolveFun = QueryExecutor.compile(config.getResolveFun());
	}

	@Override
	public Object loadValue(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute) throws XMLStreamException {
		List<String> keyAttributes = getConfig().getKeyAttributes();
		Object result;
		if (attribute.isMultiple()) {
			List<Object> listResult = new ArrayList<>();
			result = listResult;
			readEntries:
			while (true) {
				switch (in.next()) {
					case XMLStreamConstants.START_ELEMENT:
						if (in.getLocalName().equals(getConfig().getReferenceTag())) {
							listResult.add(resolveReference(log, in, attribute, keyAttributes));
						} else {
							log.error(in.getLocation(), I18NConstants.INVALID_REFERENCE_ELEMET__EXPECTED_FOUND_ATTR
								.fill(getConfig().getReferenceTag(), in.getLocalName(), attribute));
						}
						XMLStreamUtil.skipUpToMatchingEndTag(in);
						break;

					case XMLStreamConstants.CDATA:
					case XMLStreamConstants.SPACE:
					case XMLStreamConstants.CHARACTERS:
						String text = in.getText().trim();
						if (!text.isEmpty()) {
							log.error(in.getLocation(),
								I18NConstants.UNEXPECTED_TEXT_CONTENT__TEXT__ATTR.fill(text, attribute));
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						break readEntries;
				}
			}
		} else {
			result = resolveReference(log, in, attribute, keyAttributes);
			XMLStreamUtil.skipUpToMatchingEndTag(in);
		}
		return result;
	}

	private Object resolveReference(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute,
			List<String> keyAttributes) {
		Object resolverResult;
		Object key;
		if (keyAttributes.size() == 1) {
			key = in.getAttributeValue(null, keyAttributes.get(0));
			resolverResult = _resolveFun.execute(key);
		} else {
			Object[] keyArray = new Object[keyAttributes.size()];
			int n = 0;
			for (String keyAttr : keyAttributes) {
				keyArray[n++] = in.getAttributeValue(null, keyAttr);
			}
			key = keyArray;
			resolverResult = _resolveFun.execute(keyArray);
		}
		return singleton(log, in, attribute, key, resolverResult);
	}

	private Object singleton(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute, Object key, Object result) {
		if (result instanceof Collection<?>) {
			Collection<?> collectionResult = (Collection<?>) result;
			if (collectionResult.isEmpty()) {
				errorNothingFound(log, in, attribute, key);
				return null;
			} else if (collectionResult.size() > 1) {
				log.error(in.getLocation(), I18NConstants.AMBIGUOUS_RESULT_FROM_RESOLVER__KEY_RESULT_ATTR
					.fill(keyToString(key), result, attribute));
				return collectionResult.iterator().next();
			} else {
				return collectionResult.iterator().next();
			}
		} else if (result == null) {
			errorNothingFound(log, in, attribute, key);
			return null;
		} else {
			return result;
		}
	}

	private void errorNothingFound(XMLStreamLog log, XMLStreamReader in, TLStructuredTypePart attribute, Object key) {
		log.error(in.getLocation(), I18NConstants.CANNOT_RESOLVE_OBJECT__KEY__ATTR.fill(keyToString(key), attribute));
	}

	private String keyToString(Object key) {
		List<String> keyAttributes = getConfig().getKeyAttributes();
		if (key == null) {
			return keyAttributes.get(0) + "=" + "null";
		}
		if (key.getClass().isArray()) {
			StringBuilder buffer = new StringBuilder();
			int index = 0;
			Object[] values = (Object[]) key;
			for (String name : keyAttributes) {
				if (buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(name);
				buffer.append("=");
				buffer.append(values[index++]);
			}
			return buffer.toString();
		} else {
			return keyAttributes.get(0) + "=" + key.toString();
		}
	}

	@Override
	public void storeValue(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, Object value)
			throws XMLStreamException {
		if (isEmptyValue(value)) {
			return;
		}

		if (attribute.isMultiple()) {
			Collection<?> values = (Collection<?>) value;
			for (Object entry : values) {
				out.writeEmptyElement(getConfig().getReferenceTag());
				writeKeyAttributes(log, out, attribute, entry);
			}
		} else {
			writeKeyAttributes(log, out, attribute, value);
		}
	}

	private void writeKeyAttributes(Log log, XMLStreamWriter out, TLStructuredTypePart attribute, Object value) throws XMLStreamException {
		Object keyVal = _keyFun.execute(value);
		if (keyVal == null) {
			 log.error("No key produced for value '" + value + "' in attribute '" + attribute + "'.");
		} else {
			List<String> keyAttributes = getConfig().getKeyAttributes();
			if (keyVal instanceof Collection<?>) {
				Collection<?> keyCollection = (Collection<?>) keyVal;
				if (keyAttributes.size() != keyCollection.size()) {
					log.error("Expected key values for " + keyAttributes
						+ " in attribute '" + attribute + "', but key function produced wrong number of values (" +
						keyCollection + ") for reference '" + value + "'.");
				}
				Iterator<String> attrIt = keyAttributes.iterator();
				for (Iterator<?> keyIt = keyCollection.iterator(); attrIt.hasNext() && keyIt.hasNext();) {
					Object keyEntry = keyIt.next();
					if (keyEntry != null) {
						out.writeAttribute(attrIt.next(), keyEntry.toString());
					}
				}
			} else {
				if (keyAttributes.size() != 1) {
					log.error("Expected key values for " +
						keyAttributes + " in attribute '" + attribute
						+ "', but key function produced only a single value (" + keyVal
						+ ") for reference '" + value + "'.");
				}
				out.writeAttribute(keyAttributes.get(0), keyVal.toString());
			}
		}
	}

	@Override
	public boolean useEmptyElement(TLStructuredTypePart attribute, Object value) {
		return (!attribute.isMultiple()) || isEmptyValue(value);
	}

	private boolean isEmptyValue(Object value) {
		return value == null || (value instanceof Collection<?> && ((Collection<?>) value).isEmpty());
	}

}
