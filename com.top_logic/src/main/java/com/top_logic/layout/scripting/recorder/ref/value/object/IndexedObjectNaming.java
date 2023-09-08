/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexedObjectNaming.GlobalConfig.TypeSetting;
import com.top_logic.layout.scripting.recorder.ref.value.object.IndexedObjectNaming.Name.AttributeAssignment;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link VersionedObjectNaming} referencing a persistent object through assignments to properties
 * of a unique index.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IndexedObjectNaming extends VersionedObjectNaming<TLObject, IndexedObjectNaming.Name> {

	private final GlobalConfig _config;

	/**
	 * Configuration options for {@link IndexedObjectNaming}.
	 */
	public interface GlobalConfig extends PolymorphicConfiguration<IndexedObjectNaming> {

		/**
		 * Configuration of key attributes by fully qualified type.
		 */
		@Key(TypeSetting.NAME_ATTRIBUTE)
		Map<String, TypeSetting> getTypes();

		/**
		 * Index settings for a type with qualified name {@link #getName()}.
		 */
		interface TypeSetting extends NamedConfigMandatory {

			/**
			 * List of attributes unambiguously identifying an object of type {@link #getName()}.
			 */
			@Format(CommaSeparatedStrings.class)
			@Mandatory
			List<String> getAttributes();

		}

	}

	/**
	 * Creates a {@link IndexedObjectNaming}.
	 */
	@CalledByReflection
	public IndexedObjectNaming() {
		_config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
	}

	/**
	 * Identifier for a persistent object using values of a unique index.
	 */
	public interface Name extends VersionedObjectNaming.Name {
		/**
		 * The name of the object's {@link MOClass}.
		 */
		String getTable();

		/**
		 * @see #getTable()
		 */
		void setTable(String value);

		/**
		 * The dynamic type of the identified object.
		 */
		ModelName getType();

		/**
		 * @see #getType()
		 */
		void setType(ModelName value);

		/**
		 * {@link AttributeAssignment}s that describe values of a unique index of the referenced
		 * object.
		 */
		@Key(AttributeAssignment.NAME_ATTRIBUTE)
		Map<String, AttributeAssignment> getAttributes();

		/**
		 * Sets the {@link #getAttributes()} property.
		 */
		void setAttributes(Map<String, AttributeAssignment> keyValues);

		/**
		 * Assignment of a single attribute {@link #getName()} to the value {@link #getValue()}.
		 */
		interface AttributeAssignment extends NamedConfiguration {

			/**
			 * The value that is assigned to the attribute {@link #getName()}.
			 */
			ModelName getValue();

			/**
			 * @see #getValue()
			 */
			void setValue(ModelName value);

		}
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<TLObject> getModelClass() {
		return TLObject.class;
	}

	private List<String> getKeyAttributes(TLObject obj) {
		TLType type = obj.tType();
		List<String> attributes;
		if (type != null) {
			attributes = getConfiguredKeyAttributes(type);
		} else {
			attributes = configuredAttributes(ApplicationObjectUtil.tableTypeQName(obj.tTable()));
		}
		if (attributes != null) {
			return attributes;
		}

		// Fall-back to table-defined indices.
		return getStaticKeyAttributes((MOClass) obj.tHandle().tTable());
	}

	private List<String> getConfiguredKeyAttributes(TLType type) {
		String typeName = TLModelUtil.qualifiedName(type);
		List<String> configuredAttributes = configuredAttributes(typeName);
		if (configuredAttributes != null) {
			return configuredAttributes;
		}

		if (type.getModelKind() == ModelKind.CLASS) {
			List<TLClass> generalizations = ((TLClass) type).getGeneralizations();
			for (TLClass generalization : generalizations) {
				List<String> superTypeAttributes = getConfiguredKeyAttributes(generalization);
				if (superTypeAttributes != null) {
					return superTypeAttributes;
				}
			}
		}

		return null;
	}

	private List<String> configuredAttributes(String typeName) {
		TypeSetting settings = _config.getTypes().get(typeName);
		if (settings != null) {
			return settings.getAttributes();
		}
		return null;
	}

	private List<String> getStaticKeyAttributes(MOClass table) {
		for (MOIndex moIndex : table.getIndexes()) {
			if (table.getPrimaryKey() == moIndex) {
				// The primary key could be generated.
				// But we search something stable and not generated.
				continue;
			}

			if (!moIndex.isUnique()) {
				continue;
			}

			return getAttributeNames(moIndex);
		}
		return null;
	}

	private static List<String> getAttributeNames(MOIndex moIndex) {
		List<String> attributeNames = new ArrayList<>();
		for (MOAttribute attribute : moIndex.getAttributes()) {
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	@Override
	public Maybe<Name> buildName(TLObject model) {
		if (isIncompatible(model)) {
			return Maybe.none();
		}

		List<String> keyAttributes = getKeyAttributes(model);

		if (keyAttributes == null) {
			return Maybe.none();
		}

		Name name = createName();
		setVersionContext(name, model);
		name.setTable(model.tHandle().tTable().getName());
		name.setType(ModelResolver.buildModelName(model.tType()));
		for (String attributeName : keyAttributes) {
			Object value = getValue(model, attributeName);
			if (value instanceof KnowledgeItem) {
				value = ((KnowledgeItem) value).getWrapper();
			}

			AttributeAssignment assignment = TypedConfiguration.newConfigItem(AttributeAssignment.class);
			assignment.setName(attributeName);
			assignment.setValue(ModelResolver.buildModelName(value));
			name.getAttributes().put(assignment.getName(), assignment);
		}
		return Maybe.some(name);
	}

	private boolean isIncompatible(TLObject model) {
		try {
			model.tType();
		} catch (RuntimeException ex) {
			// transient object or not existing type
			return true;
		}
		return model.tHandle() == null;
	}

	static Object getValue(TLObject model, String attributeName) {
		TLStructuredType type = model.tType();
		TLStructuredTypePart attribute = type.getPart(attributeName);
		if (attribute != null) {
			return model.tValue(attribute);
		}

		try {
			Object result = model.tHandle().getAttributeValue(attributeName);
			if (result instanceof KnowledgeItem) {
				return ((KnowledgeItem) result).getWrapper();
			}
			return result;
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("Cannot access attribute '" + attributeName + "' of '" + model
				+ "'.", ex);
		}
	}

	@Override
	public TLObject locateModel(ActionContext context, Name name) {
		try {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			MOClass table = (MOClass) kb.getMORepository().getMetaObject(name.getTable());

			Filter<? super TLObject> filter = TrueFilter.INSTANCE;

			SetExpression search = ExpressionFactory.allOf(table);

			if (name.getType() != null) {
				TLType type = (TLType) ModelResolver.locateModel(context, name.getType());

				MOAttribute typeRef = table.getAttributeOrNull(PersistentObject.TYPE_REF);
				if (typeRef != null && typeRef instanceof MOReference) {
					search = filter(search, eqBinary(reference((MOReference) typeRef), literal(type.tHandle())));
				} else {
					filter = object -> object.tType() == type;
				}
			}

			for (AttributeAssignment assignment : name.getAttributes().values()) {
				Object searchedValue = ModelResolver.locateModel(context, assignment.getValue());
				String attributeName = assignment.getName();
				MOAttribute attribute = table.getAttributeOrNull(attributeName);
				if (attribute != null) {
					Object searchValue;
					if (searchedValue instanceof TLObject) {
						searchValue = ((TLObject) searchedValue).tHandle();
					} else {
						searchValue = searchedValue;
					}
					Expression ref;
					if (attribute instanceof MOReference) {
						ref = reference((MOReference) attribute);
					} else {
						ref = attribute(attribute);
					}
					search = filter(search, searchValue == null ? isNull(ref) : eqBinary(ref, literal(searchValue)));
				} else {
					Filter<? super TLObject> outer = filter;
					filter = object -> isSearchedObject(object, attributeName, searchedValue) && outer.accept(object);
				}
			}

			Branch branch = getBranch(context, name);
			Revision revision = getRevision(context, name);
			List<TLObject> results =
				kb.search(
					queryResolved(search, TLObject.class),
					revisionArgs().setRequestedBranch(branch).setRequestedRevision(revision.getCommitNumber()));

			TLObject uniqueResult = null;
			for (TLObject result : results) {
				if (!filter.accept(result)) {
					continue;
				}

				if (uniqueResult != null) {
					throw ApplicationAssertions.fail(name, "Referenced object is ambiguous: " + uniqueResult + ", vs. "
						+ result);
				}

				uniqueResult = result;
			}

			return uniqueResult;
		} catch (UnknownTypeException ex) {
			throw ApplicationAssertions.fail(name, "Cannot resolve object, unknown table '" + name.getTable() + "'.");
		}
	}

	private boolean isSearchedObject(TLObject object, String attributeName, Object searchedValue) {
		return Objects.equals(getValue(object, attributeName), searchedValue);
	}

}
