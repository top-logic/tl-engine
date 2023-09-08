/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.service.db2.AbstractFlexDataManager.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.ConstantMapMapping;
import com.top_logic.basic.col.ConstantMapping;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.db2.SerializingTransformer.Config.TypeStrategy;
import com.top_logic.knowledge.service.db2.SerializingTransformer.Config.TypeStrategy.AttributeStrategy;
import com.top_logic.knowledge.service.db2.TransformingFlexDataManager.DataTransformation;

/**
 * {@link DataTransformation} that stores all primitive data into a single {@link BinaryData}
 * attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SerializingTransformer implements DataTransformation {

	/**
	 * Strategy selection for attributes.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public enum Strategy {
		/**
		 * Include the attribute in the serialization.
		 */
		include,

		/**
		 * Exclude the attribute from the serialization.
		 */
		exclude;
	}

	/**
	 * Configuration of {@link SerializingTransformer}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * The default {@link Strategy} for all types.
		 */
		Strategy getStrategy();

		/**
		 * {@link TypeStrategy} for some specific types.
		 */
		@Key(TypeStrategy.TYPE_NAME_PROPERTY)
		Map<String, TypeStrategy> getTypes();

		/**
		 * Configuration for a specific object type.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface TypeStrategy extends ConfigurationItem {

			String TYPE_NAME_PROPERTY = "name";

			/**
			 * The name of the configured type.
			 */
			@Name(TYPE_NAME_PROPERTY)
			String getTypeName();

			/**
			 * The default {@link Strategy} for all attributes of the {@link #getTypeName()}.
			 */
			Strategy getStrategy();

			/**
			 * Configuration for some attributes of {@link #getTypeName()}.
			 */
			@Key(AttributeStrategy.ATTRIBUTE_NAME_PROPERTY)
			Map<String, AttributeStrategy> getAttributes();

			/**
			 * Configuration for a specific attribute.
			 * 
			 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
			 */
			public interface AttributeStrategy extends ConfigurationItem {
				String ATTRIBUTE_NAME_PROPERTY = "name";

				/**
				 * The name of the attribute.
				 */
				@Name(ATTRIBUTE_NAME_PROPERTY)
				String getAttributeName();

				/**
				 * The {@link Strategy} for the {@link #getAttributeName()}.
				 */
				Strategy getStrategy();
			}
		}
	}

	private static final String ENCODED_ATTRIBUTE = "_";

	/**
	 * {@link Mapping} of type names to attribute name {@link Filter}s.
	 */
	private final Mapping<? super String, Filter<? super String>> _attributeSelection;

	/**
	 * Creates a {@link SerializingTransformer} from configuration.
	 */
	public SerializingTransformer(Config config) {
		_attributeSelection = instantiate(config);
	}

	private static Mapping<? super String, Filter<? super String>> instantiate(Config config) {
		Filter<? super String> defaultFilter = getDefaultFilter(config);

		Map<String, Filter<? super String>> typeMap = new HashMap<>();
		for (Entry<String, TypeStrategy> entry : config.getTypes().entrySet()) {
			Filter<? super String> attributeFilter = instantiate(entry.getValue());

			if (attributeFilter != defaultFilter) {
				typeMap.put(entry.getKey(), attributeFilter);
			}
		}

		if (typeMap.isEmpty()) {
			return new ConstantMapping<>(defaultFilter);
		} else {
			return new ConstantMapMapping<>(typeMap, defaultFilter);
		}
	}

	private static Filter<? super String> getDefaultFilter(Config config) {
		Filter<? super String> defaultFilter;
		if (config.getStrategy() == Strategy.include) {
			defaultFilter = FilterFactory.trueFilter();
		} else {
			defaultFilter = FilterFactory.falseFilter();
		}
		return defaultFilter;
	}

	private static Filter<? super String> instantiate(TypeStrategy typeStrategy) {
		Set<String> exceptionalAttributes = new HashSet<>();

		Strategy defaultStrategy = typeStrategy.getStrategy();
		for (Entry<String, AttributeStrategy> entry : typeStrategy.getAttributes().entrySet()) {
			AttributeStrategy attributeStrategy = entry.getValue();
			if (attributeStrategy.getStrategy() != defaultStrategy) {
				exceptionalAttributes.add(entry.getKey());
			}
		}

		Filter<Object> exceptionAttributeFilter;
		if (exceptionalAttributes.isEmpty()) {
			exceptionAttributeFilter = FilterFactory.falseFilter();
		} else {
			exceptionAttributeFilter = new SetFilter(exceptionalAttributes);
		}

		if (defaultStrategy == Strategy.include) {
			return FilterFactory.not(exceptionAttributeFilter);
		} else {
			return exceptionAttributeFilter;
		}
	}

	@Override
	public FlexData encode(String typeName, FlexData values) {
		Filter<? super String> filter = _attributeSelection.map(typeName);

		Values ownValues = (Values) values;
		{
			FlexData proxyValues = ownValues.getProxyValues();

			Encoder encoder = new Encoder();
			for (String attribute : ownValues.getAttributes()) {
				Object value = ownValues.getAttributeValue(attribute);
				Object proxyValue;
				if (value instanceof BinaryData || !filter.accept(attribute)) {
					proxyValue = value;
				} else {
					if (value != null) {
						encoder.encode(attribute, value);
					}

					proxyValue = null;
				}

				/* The attribute must be set in the proxy under all circumstances:
				 * 
				 * 1. The value is an unencoded binary data that must be forwarded.
				 * 
				 * 2. The value is null:
				 * 
				 * 2a) An unencoded binary data attribute was cleared and must be removed in the
				 * proxy.
				 * 
				 * 2b) An unencoded binary data attribute was set to an encoded primitive value.
				 * The binary attribute must be cleared in the proxy.
				 * 
				 * 2c) An encoded primitive attribute was cleared. In that case, clearing the
				 * attribute in the proxy is superfluous but this case cannot be detected. */
				proxyValues.setAttributeValue(attribute, proxyValue);
			}

			proxyValues.setAttributeValue(ENCODED_ATTRIBUTE, encoder.getData());

			return proxyValues;
		}
	}

	@Override
	public FlexData decode(FlexData proxyValues, boolean mutable) {
		{
			Decoder decoder = new Decoder();
			for (String attribute : proxyValues.getAttributes()) {
				Object proxyValue = proxyValues.getAttributeValue(attribute);
				if (ENCODED_ATTRIBUTE.equals(attribute)) {
					decoder.decode(BinaryData.cast(proxyValue));
				} else {
					decoder.storeUnencodedAttribute(attribute, proxyValue);
				}
			}
			return new Values(proxyValues, decoder.getDecodedAttributes(), mutable);
		}
	}

	class Encoder {
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	
		private DataOutput serializer = new DataOutputStream(buffer);
	
		public void encode(String attribute, Object value) {
			try {
				encodeValue(value);
				serializer.writeUTF(attribute);
			} catch (IOException ex) {
				throw errorUnreachableForBufferedOperation(ex);
			}
		}
	
		public BinaryData getData() {
			if (buffer.size() == 0) {
				return null;
			} else {
				return BinaryDataFactory.createBinaryData(buffer.toByteArray());
			}
		}
	
		private void encodeValue(Object value) throws IOException {
			if (value instanceof String) {
				serializer.write(STRING_TYPE);
				serializer.writeUTF((String) value);
			} else if (value instanceof Number) {
				Number number = (Number) value;
				if (value instanceof Double) {
					serializer.write(DOUBLE_TYPE);
					serializer.writeDouble(number.doubleValue());
				} else if (value instanceof Float) {
					serializer.write(FLOAT_TYPE);
					serializer.writeFloat(number.floatValue());
				} else if (value instanceof Long) {
					serializer.write(LONG_TYPE);
					serializer.writeLong(number.longValue());
				} else {
					serializer.write(INTEGER_TYPE);
					serializer.writeInt(number.intValue());
				}
			} else if (value instanceof Date) {
				serializer.write(DATE_TYPE);
				serializer.writeLong(((Date) value).getTime());
			} else if (value instanceof Boolean) {
				if (((Boolean) value).booleanValue()) {
					serializer.write(BOOLEAN_TRUE);
				} else {
					serializer.write(BOOLEAN_FALSE);
				}
			} else if (value instanceof TLID) {
				serializer.write(TL_ID_TYPE);
				serializer.writeUTF(IdentifierUtil.toExternalForm((TLID) value));
			} else if (value instanceof ExtID) {
				serializer.write(EXT_ID_TYPE);
				serializer.writeLong(((ExtID) value).systemId());
				serializer.writeLong(((ExtID) value).objectId());
			} else {
				throw new UnsupportedOperationException(
					"Cannot store object of type '" + value.getClass().getName() + "'.");
			}
		}
	}

	class Decoder {
		private final Map<String, Object> _decodedAttributes = new HashMap<>();

		public void decode(BinaryData proxyData) {
			try {
				InputStream in = proxyData.getStream();
				try {
					DataInputStream decoder = new DataInputStream(in);
	
					while (true) {
						int typeCode = decoder.read();
						if (typeCode < 0) {
							break;
						}
	
						Object value = decodeValue(decoder, typeCode);
						String attribute = decoder.readUTF();
						storeUnencodedAttribute(attribute, value);
					}
				} finally {
					in.close();
				}
			} catch (IOException ex) {
				throw errorUnreachableForBufferedOperation(ex);
			}
		}
	
		private Object decodeValue(DataInput decoder, int typeCode) throws IOException {
			switch (typeCode) {
				case STRING_TYPE:
					return decoder.readUTF();
				case INTEGER_TYPE:
					return Integer.valueOf(decoder.readInt());
				case DOUBLE_TYPE:
					return Double.valueOf(decoder.readDouble());
				case FLOAT_TYPE:
					return Float.valueOf(decoder.readFloat());
				case LONG_TYPE:
					return Long.valueOf(decoder.readLong());
				case DATE_TYPE:
					return new Date(decoder.readLong());
				case BOOLEAN_TRUE:
					return Boolean.TRUE;
				case BOOLEAN_FALSE:
					return Boolean.FALSE;
				case TL_ID_TYPE: {
					return IdentifierUtil.fromExternalForm(decoder.readUTF());
				}
				case EXT_ID_TYPE: {
					long systemId = decoder.readLong();
					long objectId = decoder.readLong();
					return new ExtID(systemId, objectId);
				}
				default:
					throw new UnsupportedOperationException("Unsupported type code: " + typeCode);
			}
		}
	
		public void storeUnencodedAttribute(String attribute, Object value) {
			_decodedAttributes.put(attribute, value);
		}
	
		public Map<String, Object> getDecodedAttributes() {
			return _decodedAttributes;
		}
	}

	private AssertionError errorConversionFailed(DataObjectException ex) {
		return (AssertionError) new AssertionError("Failed to conert.").initCause(ex);
	}

	static UnreachableAssertion errorUnreachableForBufferedOperation(IOException ex) {
		return new UnreachableAssertion("Buffered operation.", ex);
	}

	private static class Values implements FlexData {
	
		private final FlexData _proxyValues;
	
		private final Map<String, Object> _data;
	
		private final boolean _mutable;
	
		public Values(FlexData proxyValues, Map<String, Object> data, boolean mutable) {
			_proxyValues = proxyValues;
			_data = data;
			_mutable = mutable;
		}

		@Override
		public long lastModified(String attributeName) {
			return _proxyValues.lastModified(attributeName);
		}
	
		@Override
		public Collection<String> getAttributes() {
			return _data.keySet();
		}

		@Override
		public boolean hasAttribute(String attributeName) {
			return _data.containsKey(attributeName);
		}
	
		@Override
		public Object getAttributeValue(String attributeName) {
			return _data.get(attributeName);
		}
	
		@Override
		public Object setAttributeValue(String attributeName, Object value) throws DataObjectException {
			if (!_mutable) {
				throw new IllegalStateException("Immutable values.");
			}
			return _data.put(attributeName, value);
		}
	
		public FlexData getProxyValues() {
			return _proxyValues;
		}

	}
}