/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventRewriter;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.model.TLModule;
import com.top_logic.model.impl.generated.TLTypeBase;
import com.top_logic.model.impl.generated.TLTypePartBase;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Index that allows to resolve names of model references and type names for objects during
 * migration.
 */
public class MigrationModelIndex extends AbstractConfiguredInstance<MigrationModelIndex.Config<?>>
		implements EventRewriter {

	private final Set<String> _indexedTypes;

	private final EventRewriter _rewriter;

	private final boolean _buildTypeIndex;

	private MetaObject _moduleTable;

	private Set<MetaObject> _typeTables;

	private MetaObject _attributeTable;

	private MetaObject _classifierTable;

	private final Map<ObjectBranchId, String> _modelNameOf = new HashMap<>();

	private final Map<ObjectBranchId, String> _typeOf = new HashMap<>();


	/**
	 * Configuration options for {@link MigrationModelIndex}.
	 */
	@TagName("model-index")
	public interface Config<I extends MigrationModelIndex> extends PolymorphicConfiguration<I> {

		/**
		 * Anchor property to allow resolving a {@link MigrationModelIndex} from the configuration
		 * context.
		 * 
		 * @see InstantiationContext#resolveReference(Object, Class,
		 *      com.top_logic.basic.config.ReferenceResolver)
		 */
		@Id
		@Nullable
		String getName();

		/**
		 * Fully qualified names of types, for which {@link MigrationModelIndex#getTypeOf(ObjectKey)
		 * object type resolution} should be supported.
		 */
		@Name("indexed-types")
		@ListBinding(attribute = "name", tag = "type")
		List<String> getIndexedTypes();

		/**
		 * Inner rewriters that can make use of the model index created.
		 * 
		 * @implNote
		 *           <p>
		 *           To access the model index, the inner {@link EventRewriter} must receive the
		 *           index in its configuration constructor:
		 *           </p>
		 * 
		 *           <pre>
		 *           context.resolveReference(InstantiationContext.OUTER, MigrationModelIndex.class, x -> _modelIndex = x);
		 *           </pre>
		 */
		@Name("rewriters")
		@DefaultContainer
		List<PolymorphicConfiguration<? extends EventRewriter>> getRewriters();
	}

	/**
	 * Creates a {@link MigrationModelIndex} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MigrationModelIndex(InstantiationContext context, Config<?> config) {
		super(context, config);

		_rewriter =
			StackedEventRewriter.getRewriter(TypedConfiguration.getInstanceList(context, config.getRewriters()));
		_indexedTypes = new HashSet<>(config.getIndexedTypes());
		_buildTypeIndex = !_indexedTypes.isEmpty();
	}

	/**
	 * The qualified name of the model element with the given ID.
	 * 
	 * <p>
	 * E.g. if the given value is the identifier of a type, its fully qualified type name is
	 * returned.
	 * </p>
	 * 
	 * @see TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart)
	 */
	public String getModelName(ObjectKey typeId) {
		return getModelName(id(typeId));
	}

	/**
	 * The qualified name of the model element with the given ID.
	 * 
	 * <p>
	 * E.g. if the given value is the identifier of a type, its fully qualified type name is
	 * returned.
	 * </p>
	 * 
	 * @see TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart)
	 */
	public String getModelName(ObjectBranchId id) {
		return _modelNameOf.get(id);
	}

	/**
	 * Resolves the fully qualified type name of the object with the given ID.
	 * 
	 * <p>
	 * Note: For the resolution to work, the type must be configured to be indexed.
	 * </p>
	 */
	public String getTypeOf(ObjectKey objectId) {
		return getTypeOf(id(objectId));
	}

	/**
	 * Resolves the fully qualified type name of the object with the given ID.
	 * 
	 * <p>
	 * Note: For the resolution to work, the type must be configured to be indexed.
	 * </p>
	 */
	public String getTypeOf(ObjectBranchId id) {
		return _typeOf.get(id);
	}

	private ObjectBranchId id(ObjectKey objectKey) {
		return ObjectBranchId.toObjectBranchId(objectKey);
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		before(cs);
		_rewriter.rewrite(cs, out);
		after(cs);
	}

	/**
	 * Sets the {@link MORepository} to resolve the target type.
	 */
	@Inject
	public void initMORepository(MORepository typeSystem) {
		_moduleTable = typeSystem.getMetaObject(TlModelFactory.KO_NAME_TL_MODULE);

		_typeTables = new HashSet<>();
		_typeTables.add(typeSystem.getMetaObject(TlModelFactory.KO_NAME_TL_CLASS));
		_typeTables.add(typeSystem.getMetaObject(TlModelFactory.KO_NAME_TL_ENUMERATION));
		_typeTables.add(typeSystem.getMetaObject(TlModelFactory.KO_NAME_TL_PRIMITIVE));

		_attributeTable = typeSystem.getMetaObject(TlModelFactory.KO_NAME_TL_PROPERTY);
		_classifierTable = typeSystem.getMetaObject(TlModelFactory.KO_NAME_TL_CLASSIFIER);
	}

	/**
	 * Called by the migration framework before custom {@link Rewriter}s receive the
	 * {@link ChangeSet}.
	 */
	private void before(ChangeSet cs) {
		processModuleCreations(cs);
		processTypeCreations(cs);
		processPartCreations(cs);
	}

	/**
	 * Called by the migration framework after all custom {@link Rewriter}s received the
	 * {@link ChangeSet}.
	 */
	private void after(ChangeSet cs) {
		processDeletes(cs);
	}

	private void processModuleCreations(ChangeSet cs) {
		for (ObjectCreation event : cs.getCreations()) {
			MetaObject table = event.getObjectType();
			if (table == _moduleTable) {
				_modelNameOf.put(event.getObjectId(), (String) event.getValues().get(TLModule.NAME_ATTR));
			}
		}
	}

	private void processTypeCreations(ChangeSet cs) {
		for (ObjectCreation event : cs.getCreations()) {
			MetaObject table = event.getObjectType();
			if (_typeTables.contains(table)) {
				Map<String, Object> values = event.getValues();
				ObjectKey moduleId = (ObjectKey) values.get(TLTypeBase.MODULE_ATTR);
				if (moduleId != null) {
					_modelNameOf.put(event.getObjectId(),
						getModelName(moduleId)
								+ TLModelUtil.QUALIFIED_NAME_SEPARATOR
								+ (String) values.get(TLTypeBase.NAME_ATTR));
				}
			}
		}
	}

	private void processPartCreations(ChangeSet cs) {
		for (ObjectCreation event : cs.getCreations()) {
			Map<String, Object> values = event.getValues();

			// Index type parts.
			MetaObject table = event.getObjectType();
			if (table == _attributeTable || table == _classifierTable) {
				ObjectKey typeId = (ObjectKey) values.get(TLTypePartBase.OWNER_ATTR);
				if (typeId != null) {
					_modelNameOf.put(event.getObjectId(),
						getModelName(typeId)
								+ TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR
								+ (String) values.get(TLTypePartBase.NAME_ATTR));
				}
			}
			
			if (_buildTypeIndex) {
				// Index object types.
				ObjectKey typeId = (ObjectKey) values.get(PersistentObject.T_TYPE_ATTR);
				if (typeId != null) {
					String typeName = getModelName(typeId);
					if (_indexedTypes.contains(typeName)) {
						_typeOf.put(event.getObjectId(), typeName);
					}
				}
			}
		}
	}

	private void processDeletes(ChangeSet cs) {
		for (ItemDeletion event : cs.getDeletions()) {
			ObjectBranchId id = event.getObjectId();
			_modelNameOf.remove(id);
		}
	}

}
