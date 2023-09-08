/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Implementation of {@link Indexer} that updates its internal cache due to visited
 * {@link ItemEvent}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ProvidesAPI(Indexer.class)
public class IndexerImpl extends Rewriter implements Indexer {

	private Map<String, List<IndexImpl>> _indexesByType = new HashMap<>();

	/** Delete event in the current revision. */
	private Set<ObjectBranchId> _deleteEvents = new HashSet<>();

	/**
	 * Creates a new {@link IndexerImpl}.
	 */
	public IndexerImpl(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		/* Update index due to deletions in previous revision. */
		processDeleteFromPreviousRevision();
		super.rewrite(cs, out);
	}

	@Override
	protected Object processCreateObject(ObjectCreation event) {
		ObjectBranchId createdObject = event.getObjectId();
		boolean removed = _deleteEvents.remove(createdObject);
		if (removed) {
			// Object was deleted in the same event. Delete immediately to be able to re-create it.
			processDelete(createdObject);
		}
		String typeName = createdObject.getObjectType().getName();
		List<IndexImpl> indexesForType = _indexesByType.get(typeName);
		if (indexesForType != null) {
			for (IndexImpl index : indexesForType) {
				index.register(event);

			}
		}
		return super.processCreateObject(event);
	}

	@Override
	protected Object processUpdate(ItemUpdate event) {
		List<IndexImpl> indexes = _indexesByType.get(event.getObjectType().getName());
		if (indexes != null) {
			for (IndexImpl item : indexes) {
				item.update(event);
			}
		}
		return super.processUpdate(event);
	}

	@Override
	protected Object processDelete(ItemDeletion event) {
		/* Do not process delete event direct, because the index should contain also values for now
		 * deleted object, because later rewriter my need the values during their procession. */
		_deleteEvents.add(event.getObjectId());
		return super.processDelete(event);
	}

	/** Processes the {@link ItemDeletion} from the previous event. */
	private void processDeleteFromPreviousRevision() {
		if (_deleteEvents.isEmpty()) {
			return;
		}
		for (ObjectBranchId id : _deleteEvents) {
			processDelete(id);
		}
		_deleteEvents.clear();
	}

	private void processDelete(ObjectBranchId objectId) {
		List<IndexImpl> indexes = _indexesByType.get(objectId.getObjectType().getName());
		if (indexes != null) {
			for (IndexImpl item : indexes) {
				item.delete(objectId);
			}
		}
	}

	@Override
	public Index register(String typeName, List<String> keyAttributes, List<String> valueAttributes) {
		List<Mapping<Object, ?>> keyValueMapping;
		int numberKeyAttributes = keyAttributes.size();
		switch (numberKeyAttributes) {
			case 0:
				keyValueMapping = Collections.emptyList();
				break;
			case 1:
				keyValueMapping = Collections.<Mapping<Object, ?>> singletonList(Mappings.identity());
				break;
			default:
				keyValueMapping = Collections.<Mapping<Object, ?>> nCopies(numberKeyAttributes, Mappings.identity());
				break;
		}
		return register(typeName, keyValueMapping, keyAttributes, valueAttributes);
	}

	@Override
	public Index register(String typeName, List<? extends Mapping<Object, ?>> keyValueMapping, List<String> keyAttributes,
			List<String> valueAttributes) {
		IndexImpl key = new IndexImpl(typeName, keyValueMapping, keyAttributes, valueAttributes);
		List<IndexImpl> registeredKeys = _indexesByType.get(typeName);
		if (registeredKeys == null) {
			registeredKeys = new ArrayList<>();
			_indexesByType.put(typeName, registeredKeys);
		}
		registeredKeys.add(key);
		return key;
	}


	private static class IndexImpl implements Indexer.Index {

		String _type;

		List<? extends Mapping<Object, ?>> _keyValueMapping;

		List<String> _keyAttributes;

		List<String> _valueAttributes;

		private Map<List<Object>, MultipleValues> _index = new HashMap<>();

		private Map<ObjectBranchId, List<Object>> _technicalIDs = new HashMap<>();

		IndexImpl(String type, List<? extends Mapping<Object, ?>> keyValueMapping, List<String> keyAttributes,
				List<String> valueAttributes) {
			if (keyValueMapping == null) {
				throw new NullPointerException("'keyValueMapping' must not be 'null'.");
			}
			if (keyAttributes == null) {
				throw new NullPointerException("'keyAttributes' must not be 'null'.");
			}
			if (keyValueMapping.size() < keyAttributes.size()) {
				throw new IllegalArgumentException(
					"Number of Key value mappings must not be less than number of key attributes: "
						+ keyValueMapping.size() + "<" + keyAttributes.size());
			}
			if (valueAttributes == null) {
				throw new NullPointerException("'valueAttributes' must not be 'null'.");
			}
			_type = type;
			_keyValueMapping = keyValueMapping;
			_keyAttributes = keyAttributes;
			_valueAttributes = valueAttributes;
		}

		public void delete(ObjectBranchId objectId) {
			List<Object> oldKeys = _technicalIDs.remove(objectId);
			if (oldKeys == null) {
				return;
			}
			removeValuesForKey(objectId, oldKeys, _index.get(oldKeys));
		}

		public void update(ItemUpdate update) {
			Map<String, Object> eventValues = update.getValues();
			ObjectBranchId objectId = update.getObjectId();
			List<Object> oldKeys = _technicalIDs.get(objectId);
			List<Object> newKeys = newKeyAttributeValues(eventValues, objectId, oldKeys);
			MultipleValues valuesForKey = _index.get(oldKeys);
			Object[] oldValues = valuesForKey.getValue(objectId);
			Object[] newValues = newValueAttributeValues(eventValues, objectId, oldValues);

			if (newKeys != null) {
				// keys have changed
				removeValuesForKey(objectId, oldKeys, valuesForKey);
				MultipleValues values = getOrCreateValues(newKeys);
				if (newValues != null) {
					// values have also changed
					values.register(objectId, newValues);
				} else {
					values.register(objectId, oldValues);
				}
				_technicalIDs.put(objectId, newKeys);
			} else {
				if (newValues != null) {
					// values have changed
					valuesForKey.update(objectId, newValues);
				} else {
					// nothing changed
				}
			}
		}

		private MultipleValues getOrCreateValues(List<Object> newKeys) {
			MultipleValues values = _index.get(newKeys);
			if (values == null) {
				values = new MultipleValuesList();
				_index.put(newKeys, values);
			}
			return values;
		}

		private void removeValuesForKey(ObjectBranchId objectId, List<Object> oldKeys, MultipleValues valuesForKey) {
			valuesForKey.removeId(objectId);
			if (valuesForKey.isEmpty()) {
				_index.remove(oldKeys);
			}
		}

		public void register(ObjectCreation event) {
			List<Object> keyValues = getKeyValues(event);
			Object[] valueValues = getValueValues(event);
			getOrCreateValues(keyValues).register(event.getObjectId(), valueValues);
			_technicalIDs.put(event.getObjectId(), keyValues);
		}

		Object[] getValueValues(ObjectCreation event) {
			Object[] valueValues = new Object[_valueAttributes.size()];
			for (int i = 0; i < valueValues.length; i++) {
				valueValues[i] = getAttributeValue(event.getValues(), _valueAttributes.get(i), event.getObjectId());
			}
			return valueValues;
		}

		private List<Object> getKeyValues(ItemChange event) {
			List<Object> keyValues = new ArrayList<>(_keyAttributes.size());
			for (int i = 0; i < _keyAttributes.size(); i++) {
				Object keyValue = getAttributeValue(event.getValues(), _keyAttributes.get(i), event.getObjectId());
				keyValues.add(_keyValueMapping.get(i).map(keyValue));
			}
			return keyValues;
		}

		Object[] newValueAttributeValues(Map<String, Object> newValues, ObjectBranchId self, Object[] oldValues) {
			Object[] newValueValues = null;
			for (int i = 0; i < _valueAttributes.size(); i++) {
				String valueAttr = _valueAttributes.get(i);

				if (newValues.containsKey(valueAttr)) {
					if (newValueValues == null) {
						newValueValues = new Object[_valueAttributes.size()];
						System.arraycopy(oldValues, 0, newValueValues, 0, i);
					}
					newValueValues[i] = getAttributeValue(newValues, valueAttr, self);
				} else {
					if (newValueValues != null) {
						newValueValues[i] = oldValues[i];
					}
				}
			}
			return newValueValues;
		}

		private List<Object> newKeyAttributeValues(Map<String, Object> eventValues, ObjectBranchId itemId,
				List<Object> oldKeys) {
			List<Object> newKeys = null;
			for (int i = 0; i < _keyAttributes.size(); i++) {
				String keyAttr = _keyAttributes.get(i);

				if (eventValues.containsKey(keyAttr)) {
					if (newKeys == null) {
						newKeys = new ArrayList<>(_keyAttributes.size());
						for (int j = 0; j < i; j++) {
							newKeys.add(oldKeys.get(j));
						}
					}
					Object newKeyValue = getAttributeValue(eventValues, keyAttr, itemId);
					newKeys.add(_keyValueMapping.get(i).map(newKeyValue));
				} else {
					if (newKeys != null) {
						newKeys.add(oldKeys.get(i));
					}
				}
			}
			return newKeys;
		}

		private Object getAttributeValue(Map<String, Object> values, String attribute, ObjectBranchId self) {
			if (SELF_ATTRIBUTE.equals(attribute)) {
				return self.toCurrentObjectKey();
			}
			return values.get(attribute);
		}

		@Override
		public Object getValue(Object... keyValues) {
			Object[] values = getValues(keyValues);
			if (values == null) {
				return null;
			}
			return values[0];
		}

		@Override
		public Object[] getValues(Object... keyValues) {
			return value(keyValues).getSingleValue();
		}

		@Override
		public List<Object[]> getMultiValues(Object... keyValues) {
			return value(keyValues).getMultiValues();
		}

		private MultipleValues value(Object... keyValues) {
			MultipleValues result = _index.get(Arrays.asList(keyValues));
			if (result == null) {
				return MultipleValues.NO_VALUE;
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + _keyAttributes.hashCode();
			result = prime * result + _keyValueMapping.hashCode();
			result = prime * result + ((_type == null) ? 0 : _type.hashCode());
			result = prime * result + _valueAttributes.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IndexImpl other = (IndexImpl) obj;
			if (!_keyAttributes.equals(other._keyAttributes))
				return false;
			if (_type == null) {
				if (other._type != null)
					return false;
			} else if (!_type.equals(other._type))
				return false;
			if (!_valueAttributes.equals(other._valueAttributes))
				return false;
			if (!_keyValueMapping.equals(other._keyValueMapping))
				return false;
			return true;
		}

	}

	static interface MultipleValues {

		public static final MultipleValues NO_VALUE = new MultipleValues() {

			@Override
			public boolean isEmpty() {
				return true;
			}

			@Override
			public Object[] getSingleValue() {
				return null;
			}

			@Override
			public List<Object[]> getMultiValues() {
				return Collections.emptyList();
			}

			@Override
			public Object[] removeId(ObjectBranchId id) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Object[] getValue(ObjectBranchId id) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void update(ObjectBranchId id, Object[] newValues) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void register(ObjectBranchId id, Object[] values) {
				throw new UnsupportedOperationException();
			}

		};

		void register(ObjectBranchId id, Object[] values);

		boolean isEmpty();

		void update(ObjectBranchId id, Object[] newValues);

		Object[] removeId(ObjectBranchId id);

		Object[] getValue(ObjectBranchId id);

		Object[] getSingleValue();

		List<Object[]> getMultiValues();

	}

	static class MultipleValuesList extends ArrayList<Object> implements MultipleValues {

		@Override
		public void register(ObjectBranchId id, Object[] values) {
			add(id);
			add(values);
		}
		
		@Override
		public void update(ObjectBranchId id, Object[] newValues) {
			for (int i = 0; i < size(); i = i + 2) {
				if (id.equals(get(i))) {
					set(i + 1, newValues);
				}
			}
		}

		@Override
		public Object[] getValue(ObjectBranchId id) {
			for (int i = 0 ; i< size();i = i+2) {
				if (id.equals(get(i))) {
					return (Object[]) get(i + 1);
				}
			}
			return null;
		}

		@Override
		public Object[] removeId(ObjectBranchId id) {
			for (int i = 0; i < size(); i = i + 2) {
				if (id.equals(get(i))) {
					Object value = remove(i + 1);
					// remove key
					remove(i);
					return (Object[]) value;
				}
			}
			return null;
		}

		@Override
		public Object[] getSingleValue() {
			switch (size()) {
				case 0:
					return null;
				case 2:
					return (Object[]) get(1);
				default:
					throw new IllegalStateException("More than one entry: " + toString(getMultiValues()));
			}
		}

		private String toString(List<Object[]> multiValues) {
			int size = multiValues.size();
			switch (size) {
				case 0:
					return "[]";
				case 1:
					return "[" + Arrays.deepToString(multiValues.get(0)) + "]";
				default:
					StringBuilder tmp = new StringBuilder("[");
					tmp.append(Arrays.deepToString(multiValues.get(0)));
					for (int i = 1; i < size; i++) {
						tmp.append(',');
						tmp.append(Arrays.deepToString(multiValues.get(i)));
					}
					tmp.append(']');
					return tmp.toString();
			}
		}

		@Override
		public List<Object[]> getMultiValues() {
			switch (size()) {
				case 0:
					return Collections.emptyList();
				case 2:
					return Collections.singletonList((Object[]) get(1));
				default:
					List<Object[]> multiValues = new ArrayList<>(size() / 2);
					for (int i = 0; i < size(); i = i + 2) {
						multiValues.add((Object[]) get(i + 1));
					}
					return multiValues;
			}
		}

	}

}

