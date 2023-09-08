/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MappedList;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.func.Identity;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapperImpl.Config.AttributesConfiguration;
import com.top_logic.knowledge.service.db2.migration.rewriters.IdMapperImpl.Config.TargetMapping;

/**
 * {@link Rewriter} that create target {@link TLID} from the source {@link TLID} and rewrites the
 * events by replacing the ID's.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ProvidesAPI(IdMapper.class)
public class IdMapperImpl extends Rewriter implements IdMapper {

	/**
	 * Configuration of an {@link IdMapperImpl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends Rewriter.Config {

		/** Name of {@link #getMappings()}. */
		String MAPPINGS = "mappings";

		/**
		 * Configured {@link TargetMapping} indexed by {@link TargetMapping#getType()}.
		 */
		@Name(MAPPINGS)
		@Key(TargetMapping.TYPE)
		Map<String, TargetMapping> getMappings();

		/**
		 * Configuration of the attributes whose values identify an object unique.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		interface TargetMapping extends ConfigurationItem {

			/** Name of {@link #getKeyAttributes()}. */
			String KEY_ATTRIBUTES = "key-attributes";

			/** Name of {@link #getAttributes()}. */
			String ATTRIBUTES = "attributes";

			/** Name of {@link #getType()}. */
			String TYPE = "type";

			/**
			 * Name of the type whose key attributes are configured.
			 */
			@Name(TYPE)
			@Mandatory
			String getType();

			/**
			 * Setter for {@link #getType()}.
			 */
			void setType(String type);

			/**
			 * List of attributes whose values for an object of the {@link #getType() given type}
			 * identify the object uniquely.
			 * 
			 * <p>
			 * This is a shortcut for {@link #getAttributes()} where
			 * {@link AttributesConfiguration#getSource() source} and
			 * {@link AttributesConfiguration#getTarget() target} are same.
			 * </p>
			 * 
			 * <p>
			 * Either this attribute or {@link #getAttributes()} must be set but not both.
			 * </p>
			 * 
			 * @see #getAttributes()
			 */
			@Name(KEY_ATTRIBUTES)
			@Format(CommaSeparatedStrings.class)
			List<String> getKeyAttributes();

			/**
			 * Setter for {@link #getKeyAttributes()}.
			 */
			void setKeyAttributes(List<String> keyAttributes);

			/**
			 * List of attributes whose values for an object of the {@link #getType() given type}
			 * identify the object uniquely.
			 * 
			 * <p>
			 * Either this attribute or {@link #getKeyAttributes()} must be set but not both.
			 * </p>
			 * 
			 * @see #getKeyAttributes()
			 */
			@Name(ATTRIBUTES)
			List<AttributesConfiguration> getAttributes();

		}

		/**
		 * Configuration of an attribute defining an item uniquely.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		interface AttributesConfiguration extends ConfigurationItem {

			/**
			 * Name of the attribute in the source database which identifies the item.
			 * 
			 * @see #getTarget() Corresponding attribute in target database.
			 */
			@Mandatory
			String getSource();

			/**
			 * Setter for {@link #getSource()}.
			 */
			void setSource(String value);

			/**
			 * Name of the attribute in the target database identifying the item unique.
			 * 
			 * @see #getSource() Corresponding attribute in source database.
			 */
			@Mandatory
			String getTarget();

			/**
			 * Setter for {@link #getTarget()}.
			 */
			void setTarget(String value);

			/**
			 * Mapping to use for the value of the {@link #getSource() source} attribute to identify
			 * with the value in the target database.
			 */
			@InstanceFormat
			@InstanceDefault(Identity.class)
			Mapping<Object, ?> getSourceMapping();

			/**
			 * Setter for {@link #getSourceMapping()}.
			 */
			void setSourceMapping(Mapping<Object, ?> value);

			/**
			 * Mapping to use for the value of the {@link #getTarget() target} attribute to identify
			 * with the value in the source database.
			 */
			@InstanceFormat
			@InstanceDefault(Identity.class)
			Mapping<Object, ?> getTargetMapping();

			/**
			 * Setter for {@link #getTargetMapping()}.
			 */
			void setTargetMapping(Mapping<Object, ?> value);

		}

	}

	private Map<TLID, TLID> _idMapping = new HashMap<>();

	private KnowledgeBase _kb;

	private Indexer _indexer;

	private final Config _config;

	private Map<String, Indexer.Index> _indexByTypeName;

	private Set<ObjectBranchId> _skippedCreations = new HashSet<>();

	/**
	 * Index of {@link ObjectKey}s in the target system by object type and their key values.
	 */
	private Map<MetaObject, Map<ValueKey, ObjectKey>> _idByTypeAndValueKey;

	/**
	 * Creates a new {@link IdMapperImpl}.
	 */
	public IdMapperImpl(InstantiationContext context, Config config) {
		super(context, config);
		_config = TypedConfiguration.copy(config);
		for (TargetMapping mapping : _config.getMappings().values()) {
			List<String> keyAttributes = mapping.getKeyAttributes();
			List<AttributesConfiguration> attributes = mapping.getAttributes();
			if (keyAttributes.isEmpty()) {
				if (attributes.isEmpty()) {
					context.error("Either attribute '" + TargetMapping.KEY_ATTRIBUTES + "' or '"
						+ TargetMapping.ATTRIBUTES + "' must be set.");
				}
			} else {
				if (!attributes.isEmpty()) {
					context.error("Not both attribute '" + TargetMapping.KEY_ATTRIBUTES + "' and '"
						+ TargetMapping.ATTRIBUTES + "' must be set.");
				} else {
					List<AttributesConfiguration> configs = mapping.getAttributes();
					for (String keyAttr : keyAttributes) {
						AttributesConfiguration attributeConfig =
							TypedConfiguration.newConfigItem(AttributesConfiguration.class);
						attributeConfig.setSource(keyAttr);
						attributeConfig.setTarget(keyAttr);
						configs.add(attributeConfig);
					}
				}
			}
		}
	}

	/**
	 * Initialises this {@link IdMapperImpl} with the {@link KnowledgeBase} to create new
	 * {@link TLID}s.
	 */
	@Inject
	public void initKnowledgeBase(KnowledgeBase kb) {
		_kb = kb;

		buildIndex();
	}

	private void buildIndex() {
		class ToBeResolved {

			private final ObjectKey _item;

			private final ValueKey _values;

			private final int _index;

			public ToBeResolved(ObjectKey item, ValueKey values, int index) {
				_item = item;
				_values = values;
				_index = index;
			}

			public void resolve(Map<ObjectKey, ValueKey> valueKeyByObjectKey) {
				ValueKey valueKey = valueKeyByObjectKey.get(_item);
				if (valueKey == null) {
					// Error, object cannot be resolved to value key.
					throw new ConfigurationError("Object '" + _item + "' cannot be resolved to value key.");
				}
				_values.set(_index, valueKey);
			}

		}

		_idByTypeAndValueKey = new HashMap<>();

		List<ToBeResolved> worklist = new ArrayList<>();
		Map<ObjectKey, ValueKey> valueKeyByObjectKey = new HashMap<>();
		for (Config.TargetMapping mapping : _config.getMappings().values()) {
			String type = mapping.getType();

			try {
				_idByTypeAndValueKey.put(_kb.getMORepository().getType(type), new HashMap<>());

				List<AttributesConfiguration> keyAttributes = mapping.getAttributes();

				SetExpression search = allOf(type);
				List<KnowledgeItem> result = _kb.search(queryUnresolved(search, KnowledgeItem.class));
				for (KnowledgeItem item : result) {
					ValueKey valueKey = new ValueKey(type, keyAttributes.size());

					for (int n = 0; n < keyAttributes.size(); n++) {
						AttributesConfiguration attribute = keyAttributes.get(n);
						Object value = attribute.getTargetMapping().map(item.getAttributeValue(attribute.getTarget()));
						if (value instanceof KnowledgeItem) {
							worklist.add(new ToBeResolved(((KnowledgeItem) value).tId(), valueKey, n));
						} else {
							valueKey.set(n, value);
						}
					}
					
					valueKeyByObjectKey.put(item.tId(), valueKey);
				}
			} catch (NoSuchAttributeException ex) {
				throw new ConfigurationError("Invalid mapping for type '" + type + "' in ID mapping.", ex);
			} catch (UnknownTypeException ex) {
				throw new ConfigurationError("Mapped target type '" + type + "' not known.", ex);
			}
		}

		for (ToBeResolved resolver : worklist) {
			resolver.resolve(valueKeyByObjectKey);
		}
		
		for (Entry<ObjectKey, ValueKey> entry : valueKeyByObjectKey.entrySet()) {
			ObjectKey id = entry.getKey();
			MetaObject type = id.getObjectType();
			ValueKey value = entry.getValue();
			ObjectKey clash = _idByTypeAndValueKey.get(type).put(value, id);
			if (clash != null) {
				throw new ConfigurationError("Multiple values for type '" + type + "' and keys '" + value
					+ "': " + clash + ", " + id);
			}
		}
	}

	/**
	 * Initialises this {@link IdMapperImpl} with the give {@link Indexer}.
	 */
	@Inject
	public void initIndexer(Indexer indexer) {
		_indexer = indexer;

		_indexByTypeName = new HashMap<>();
		for (Config.TargetMapping mapping : _config.getMappings().values()) {
			String type = mapping.getType();
			Indexer.Index index = _indexer.register(type,
				Collections.singletonList(Indexer.SELF_ATTRIBUTE ),
				new MappedList<>(new Mapping<AttributesConfiguration, String>() {

					@Override
					public String map(AttributesConfiguration input) {
						return input.getSource();
					}

				}, mapping.getAttributes()));
			_indexByTypeName.put(type, index);
		}
	}

	@Override
	protected void processCreations(ChangeSet cs) {
		Iterator<ObjectCreation> creations = cs.getCreations().iterator();
		while (creations.hasNext()) {
			ItemEvent creation = creations.next();
			ObjectBranchId dumpKey = creation.getObjectId();
			TLID dumpId = dumpKey.getObjectName();

			// Note: If the "same" object was created on different branches
			// independently (through a merge, not a branch operation), it must map
			// to
			// objects with the same ID in the target system, also.
			TLID targetId = mapId(dumpId);
			if (targetId == null) {
				MetaObject type = dumpKey.getObjectType();
				
				Indexer.Index index = _indexByTypeName.get(type.getName());
				if (index != null) {
					ValueKey valueKey;
					try {
						valueKey = toValueKey(dumpKey.toCurrentObjectKey());
					} catch (IllegalArgumentException ex) {
						throw new IllegalArgumentException("Cannot resolve value key for '" + dumpKey
							+ "' in revision " + cs.getRevision() + ".", ex);
					}
					
					/* Can not remove the entry, because the object can be deleted and recreated
					 * with the same functional key. In such case the original id must be
					 * found... */
					ObjectKey existingId = _idByTypeAndValueKey.get(type).get(valueKey);
					if (existingId != null) {
						targetId = existingId.getObjectName();
						_skippedCreations.add(dumpKey);
						creations.remove();
					}
				}
				
				if (targetId == null) {
					targetId = _kb.createID();
				}
				
				_idMapping.put(dumpId, targetId);
			}
		}

		super.processCreations(cs);
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		ObjectBranchId dumpKey = event.getObjectId();
		if (_skippedCreations.contains(dumpKey)) {
			// Creation was dropped as the represented object exists in target database. Drop all
			// further events for that type, because it is expected that the values in the target
			// database are correct.
			return SKIP_EVENT;
		}
		TLID dumpId = dumpKey.getObjectName();

		// Note: If the "same" object was created on different branches
		// independently (through a merge, not a branch operation), it must map
		// to
		// objects with the same ID in the target system, also.
		TLID targetId = mapId(dumpId);
		if (targetId == null) {
			migration().error("Event for unknown object: " + event);
			return SKIP_EVENT;
		}
		event.setObjectId(buildId(dumpKey, targetId));
		mapValues(event);

		return APPLY_EVENT;
	}

	private Object[] resolveObjectKeys(String type, Object[] values) {
		TargetMapping mapping = _config.getMappings().get(type);
		List<AttributesConfiguration> attributes = mapping.getAttributes();
		for (int n = 0, cnt = values.length; n < cnt; n++) {
			Object value = values[n];
			if (value instanceof ObjectKey) {
				values[n] = toValueKey((ObjectKey) value);
			} else {
				// The n-th attribute is used to get the n-th value from the index.
				values[n] = attributes.get(n).getSourceMapping().map(value);
			}
		}
		return values;
	}

	private ValueKey toValueKey(ObjectKey key) {
		String type = key.getObjectType().getName();
		Indexer.Index index = _indexByTypeName.get(type);
		if (index == null) {
			throw new IllegalArgumentException("Cannot resolve object key to key values: " + key);
		}
		Object[] values = index.getValues(key);
		if (values == null) {
			throw new IllegalArgumentException("Cannot resolve object key to key values: " + key);
		}
		return new ValueKey(type, resolveObjectKeys(type, values));
	}

	@Override
	protected Object processUpdate(ItemUpdate event) {
		Object result = super.processUpdate(event);
		if (result == SKIP_EVENT) {
			return SKIP_EVENT;
		}

		Map<String, Object> oldValues = event.getOldValues();
		if (oldValues != null) {
			mapValues(event, oldValues);
		}

		return result;
	}

	private void mapValues(ItemChange creation) {
		mapValues(creation, creation.getValues());
	}

	private void mapValues(ItemEvent event, Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof ObjectKey) {
				ObjectKey key = (ObjectKey) value;
				TLID targetId = mapId(key.getObjectName());
				if (targetId != null) {
					Object mappedKey =
						new DefaultObjectKey(key.getBranchContext(), key.getHistoryContext(),
							key.getObjectType(), targetId);
					entry.setValue(mappedKey);
				} else {
					migration().error("Unknown id in reference '" + entry.getKey() + "' in event " + event + ": "
						+ key.getObjectName());
					entry.setValue(null);
				}
			}
			else if (value instanceof TLID) {
				TLID targetId = mapId((TLID) value);
				if (targetId != null) {
					entry.setValue(targetId);
				} else {
					migration().error("Unknown id in '" + entry.getKey() + "' in event " + event + ": " + value);
					entry.setValue(null);
				}
			}
		}
	}

	@Override
	public TLID mapId(TLID dumpId) {
		TLID targetId = _idMapping.get(dumpId);
		return targetId;
	}

	@Override
	public TLID mapOrCreateId(TLID dumpId) {
		TLID targetId = mapId(dumpId);
		if (targetId != null) {
			return targetId;
		}
		TLID newId = _kb.createID();
		_idMapping.put(dumpId, newId);
		return newId;
	}


	private ObjectBranchId buildId(ObjectBranchId oldkey, TLID newId) {
		return new ObjectBranchId(oldkey.getBranchId(), oldkey.getObjectType(), newId);
	}

	@Override
	public String toString() {
		return "IdMapper: " + _idMapping;
	}

	/**
	 * The {@link ObjectBranchId}s of the skipped creations.
	 */
	public Set<ObjectBranchId> getSkippedCreatedObjects() {
		return _skippedCreations;
	}
}
