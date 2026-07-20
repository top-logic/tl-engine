/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.constraint;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} ensuring that the value of the annotated attribute is unique among all
 * instances of the attribute's owner type (including instances of subtypes).
 *
 * <p>
 * Additional attributes may participate in the constraint. In that case, the value of the
 * annotated attribute must be unique only among those objects that share the values of all
 * additional attributes. This expresses uniqueness within a scope: A name that must be unique
 * within its container declares the container reference as additional attribute.
 * </p>
 *
 * <p>
 * An unset value participates in the constraint like any other value: At most one object may have
 * the empty (combination of) value(s).
 * </p>
 *
 * <p>
 * Uniqueness is checked among the objects alive in the current branch. Historic states are not
 * affected: A value freed by a deletion may be used again.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class UniqueConstraint extends AbstractConfiguredInstance<UniqueConstraint.Config<?>>
		implements ConstraintCheck {

	/**
	 * Configuration options for {@link UniqueConstraint}.
	 */
	@TagName("unique")
	public interface Config<I extends UniqueConstraint> extends PolymorphicConfiguration<I> {

		/** @see #getAdditionalAttributes() */
		String ADDITIONAL_ATTRIBUTES = "additional-attributes";

		/**
		 * Names of additional attributes of the annotated type that participate in the uniqueness
		 * constraint.
		 *
		 * <p>
		 * If given, the value of the annotated attribute must be unique only among those objects
		 * that share the values of all these attributes. Each additional attribute must be a
		 * property or a to-one reference.
		 * </p>
		 */
		@Name(ADDITIONAL_ATTRIBUTES)
		@Format(CommaSeparatedStrings.class)
		List<String> getAdditionalAttributes();

		/**
		 * Whether a violation is reported as an error that prevents saving, or only as a warning
		 * displayed in the form.
		 */
		ConstraintType getType();

	}

	private final ConstraintType _type;

	/**
	 * Creates a {@link UniqueConstraint} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UniqueConstraint(InstantiationContext context, Config<?> config) {
		super(context, config);

		_type = config.getType();
	}

	@Override
	public ConstraintType type() {
		return _type;
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		TLObject conflict = findConflict(object, attribute);
		if (conflict == null) {
			return null;
		}
		return I18NConstants.ERROR_VALUE_NOT_UNIQUE__CONFLICT.fill(conflict);
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		TLStructuredType objectType = object.tType();
		for (String name : getConfig().getAdditionalAttributes()) {
			trace.add(Pointer.create(object, resolvePart(objectType, name)));
		}
	}

	/**
	 * Searches for an object that collides with the given object on the constrained value
	 * combination.
	 *
	 * <p>
	 * The given object may be a transient form overlay providing the values currently entered. The
	 * persistent object being edited (if any) is excluded from the search. Objects created or
	 * modified in the current transaction are found with their pending values.
	 * </p>
	 *
	 * @param object
	 *        The object being checked.
	 * @param attribute
	 *        The attribute carrying this constraint.
	 * @return A conflicting object, or <code>null</code> if the constrained value combination is
	 *         unique.
	 */
	public TLObject findConflict(TLObject object, TLStructuredTypePart attribute) {
		TLClass scope = (TLClass) attribute.getOwner();
		List<String> additionalNames = getConfig().getAdditionalAttributes();

		Object anchorValue = object.tValue(attribute);

		TLStructuredType objectType = object.tType();
		List<TLStructuredTypePart> additionalParts = new ArrayList<>(additionalNames.size());
		List<Object> additionalValues = new ArrayList<>(additionalNames.size());
		for (String name : additionalNames) {
			additionalParts.add(resolvePart(scope, name));
			additionalValues.add(object.tValue(resolvePart(objectType, name)));
		}

		TLObject self = editedObject(object);

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		MORepository repository = kb.getMORepository();
		for (Entry<String, Set<TLClass>> tableEntry : AttributeOperations.typesByTableName(scope).entrySet()) {
			MOClass table;
			try {
				table = (MOClass) repository.getType(tableEntry.getKey());
			} catch (UnknownTypeException ex) {
				throw new ConfigurationError(
					"Undefined table '" + tableEntry.getKey() + "' for type '" + scope + "'.", ex);
			}

			SimpleQuery<TLObject> query =
				SimpleQuery.queryResolved(TLObject.class, table, candidateFilter(table, attribute, anchorValue));
			for (TLObject candidate : kb.compileSimpleQuery(query).search()) {
				if (!tableEntry.getValue().contains(candidate.tType())) {
					continue;
				}
				if (candidate.equals(self) || candidate.equals(object)) {
					continue;
				}
				if (matches(candidate, attribute, anchorValue, additionalParts, additionalValues)) {
					return candidate;
				}
			}
		}
		return null;
	}

	/**
	 * Filter expression pre-selecting potentially conflicting rows of the given table.
	 *
	 * <p>
	 * If the constrained attribute is stored in a column of the table, the equality test is
	 * performed by the database. Otherwise, all rows are retrieved and compared in memory by
	 * {@link #matches(TLObject, TLStructuredTypePart, Object, List, List)}.
	 * </p>
	 */
	private Expression candidateFilter(MOClass table, TLStructuredTypePart attribute, Object anchorValue) {
		if (anchorValue != null) {
			StorageDetail storage = attribute.getStorageImplementation();
			if (storage instanceof ColumnStorage) {
				MOAttribute column = table.getAttributeOrNull(((ColumnStorage) storage).getStorageAttribute());
				if (column != null && attribute.getType() instanceof TLPrimitive) {
					Object storageValue =
						((TLPrimitive) attribute.getType()).getStorageMapping().getStorageObject(anchorValue);
					if (storageValue != null) {
						return eqBinary(attribute(column), literal(storageValue));
					}
				}
			}
		}
		return literal(Boolean.TRUE);
	}

	private boolean matches(TLObject candidate, TLStructuredTypePart attribute, Object anchorValue,
			List<TLStructuredTypePart> additionalParts, List<Object> additionalValues) {
		if (!Utils.equals(candidate.tValue(attribute), anchorValue)) {
			return false;
		}
		for (int n = 0, cnt = additionalParts.size(); n < cnt; n++) {
			if (!Utils.equals(candidate.tValue(additionalParts.get(n)), additionalValues.get(n))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The persistent object to exclude from the conflict search.
	 *
	 * <p>
	 * When checking a form overlay, the edited persistent object must not be reported as conflict
	 * of itself. In a create form, there is no such object yet.
	 * </p>
	 */
	private static TLObject editedObject(TLObject object) {
		if (object instanceof TLFormObjectBase) {
			return ((TLFormObjectBase) object).getEditedObject();
		}
		return object;
	}

	private static TLStructuredTypePart resolvePart(TLStructuredType type, String name) {
		TLStructuredTypePart part = type.getPart(name);
		if (part == null) {
			throw new ConfigurationError("Type '" + type + "' has no attribute '" + name
				+ "' referenced by a unique constraint.");
		}
		if (part.isMultiple()) {
			throw new ConfigurationError("To-many attribute '" + name + "' of type '" + type
				+ "' cannot participate in a unique constraint.");
		}
		return part;
	}

}
