/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.ConcatenatedClosableIterator;
import com.top_logic.basic.col.EmptyClosableIterator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.shared.collection.map.MappedIterator;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.boundsec.attribute.AttributeClassifierManager;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.element.config.annotation.FolderType;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.config.annotation.TLConstraint;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.config.annotation.TLValidityCheck;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.FieldProviderAnnotation;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.ConfiguredAttributeImpl;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.PersistentObjectImpl;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.InstancesQueryBuilder;
import com.top_logic.knowledge.search.MonomorphicQueryBuilder;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.AssociationQueryUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.knowledge.service.xml.annotation.InstancesQueryAnnotation;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.layout.form.FormContextProxy;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.BooleanAnnotation;
import com.top_logic.model.annotate.RenderInputBeforeLabelAnnotation;
import com.top_logic.model.annotate.RenderWholeLineAnnotation;
import com.top_logic.model.annotate.TLFullTextRelevant;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.model.annotate.TLSearchRange;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.ui.BooleanDisplay;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.annotate.ui.OptionsDisplay;
import com.top_logic.model.annotate.ui.OptionsPresentation;
import com.top_logic.model.annotate.ui.ReferenceDisplay;
import com.top_logic.model.annotate.ui.ReferencePresentation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.error.TopLogicException;

/**
 * Utilities for executing {@link StorageImplementation} operations.
 * 
 * @see AttributeUtil
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeOperations {

	/** @see #getOptions(TLStructuredTypePart) */
	private static final String OPTIONS_ATTRIBUTE = "options";

	/** See definition of {@link KBBasedMetaAttribute#OBJECT_NAME} in ElementMeta.xml */
	private static final String CONSTRAINT_ATTRIBUTE = "constraint";

	private static final StorageImplementation NO_STORAGE = new StorageImplementation() {

		@Override
		public boolean supportsLiveCollections() {
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public void update(AttributeUpdate update) throws AttributeException {
			undefined(update);
		}

		private AttributeException undefined(AttributeUpdate update) {
			throw new AttributeException(
				"Access to undefined attribute '" + update.getAttribute() + "' in type '" + update.getType() + "'.");
		}

		@Override
		public void setAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
				throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
			undefined(object, attribute);
		}

		@Override
		public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
				throws NoSuchAttributeException, AttributeException {
			undefined(object, attribute);
		}

		private AttributeException undefined(TLObject object, TLStructuredTypePart attribute) {
			throw new AttributeException(
				"Access to undefined attribute '" + attribute + "' in type '" + object.tType() + "'.");
		}

		@Override
		public void init(TLStructuredTypePart attribute) {
			// Ignore.
		}

		@Override
		public Object getUpdateValue(AttributeUpdate update)
				throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
			throw undefined(update);
		}

		@Override
		public Collection<?> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
			throw undefined(object, attribute);
		}

		@Override
		public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
			return null;
		}

		@Override
		public void checkUpdate(AttributeUpdate update) throws TopLogicException {
			throw undefined(update);
		}

		@Override
		public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
				throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
			throw undefined(object, attribute);
		}

		@Override
		public PreloadContribution getPreload() {
			return EmptyPreloadContribution.INSTANCE;
		}

		@Override
		public PreloadContribution getReversePreload() {
			return EmptyPreloadContribution.INSTANCE;
		}

		@Override
		public Unimplementable unimplementable() {
			return null;
		}
	};

	/**
	 * @see StorageImplementation#getAttributeValue(TLObject, TLStructuredTypePart)
	 */
	public static Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		checkAlive(object, attribute);
		return getStorageImplementation(object, attribute).getAttributeValue(object, attribute);
	}

	/**
	 * @see StorageImplementation#checkUpdate(AttributeUpdate)
	 */
	public static void checkUpdate(AttributeUpdate update) throws TopLogicException {
		TLObject object = update.getObject();
		TLStructuredTypePart attribute = update.getAttribute();
		checkAlive(object, attribute);
		getStorageImplementation(object, attribute).checkUpdate(update);
	}

	/**
	 * @see StorageImplementation#update(AttributeUpdate)
	 */
	public static Object getUpdateValue(AttributeUpdate update) throws NoSuchAttributeException, IllegalArgumentException,
			AttributeException {
		TLObject object = update.getObject();
		TLStructuredTypePart attribute = update.getAttribute();
		checkAlive(object, attribute);
		return getStorageImplementation(object, attribute).getUpdateValue(update);
	}

	/**
	 * @see StorageImplementation#setAttributeValue(TLObject, TLStructuredTypePart, Object)
	 */
	public static void setAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		checkAlive(object, attribute);
		AttributeOperations.getStorageImplementation(object, attribute).setAttributeValue(object, attribute, aValues);
	}

	/**
	 * @see StorageImplementation#addAttributeValue(TLObject, TLStructuredTypePart, Object)
	 */
	public static void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue) {
		checkAlive(object, attribute);
		getStorageImplementation(object, attribute).addAttributeValue(object, attribute, aValue);
	}

	/**
	 * The (potentially) overridden version of the given attribute in the context of the given
	 * object.
	 * 
	 * @param obj
	 *        The object context. If it is <code>null</code>, the given attribute is considered not
	 *        to be overridden.
	 * @param attribute
	 *        An attribute of the given object or of a super class thereof.
	 * @return The (potentially) overridden version of the given attribute in the type of the given
	 *         object. If the given attribute does not belong to the type of the given object,
	 *         <code>null</code> is returned.
	 */
	public static TLStructuredTypePart getAttributeOverride(TLObject obj, TLStructuredTypePart attribute) {
		if (obj == null) {
			return attribute;
		} else {
			return getAttributeOverride(obj.tType(), attribute);
		}
	}

	/**
	 * The (potentially) overridden version of the given attribute in the context of the given type.
	 * 
	 * @param actualType
	 *        The concrete type of the context.
	 * @param attribute
	 *        An attribute of the given type or of a super class thereof.
	 * @return The (potentially) overridden version of the given attribute in the given type. If the
	 *         given attribute does not belong to the given type, <code>null</code> is returned.
	 */
	private static TLStructuredTypePart getAttributeOverride(TLStructuredType actualType,
			TLStructuredTypePart attribute) {
		TLStructuredTypePart attributeInObjectRev = WrapperHistoryUtils.getWrapper(actualType.tRevision(), attribute);
		if (attributeInObjectRev == null) {
			// Attribute does not exist at the time of the accessed type.
			return null;
		}

		TLStructuredType declaringType = attributeInObjectRev.getOwner();
		if (declaringType == null || !TLModelUtil.isGeneralization(declaringType, actualType)) {
			// Attribute of an unrelated type.
			return null;
		}
		if (actualType == declaringType) {
			// Short-cut for the most common case of a non-overridden attribute.
			return attributeInObjectRev;
		}

		// Lookup by name in the concrete type.
		return actualType.getPart(attributeInObjectRev.getName());
	}

	/**
	 * @see StorageImplementation#removeAttributeValue(TLObject, TLStructuredTypePart, Object)
	 */
	public static void removeAttributeValue(TLObject anAttributed, TLStructuredTypePart metaAttribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		checkAlive(anAttributed, metaAttribute);
		AttributeOperations.getStorageImplementation(metaAttribute).removeAttributeValue(anAttributed, metaAttribute, aValue);
	}

	/**
	 * Checks whether the given object and attribute are valid.
	 * 
	 * @throws WrapperRuntimeException
	 *         If something is invalid.
	 * 
	 * @see TLObject#tValid()
	 */
	public static void checkAlive(TLObject self, TLStructuredTypePart attribute) {
		// Note: During create, the target object may be null.
		if (self != null && !self.tValid()) {
			throw new WrapperRuntimeException("Target object is deleted: " + self);
		}
		if (!attribute.tValid()) {
			throw new WrapperRuntimeException("Attribute is deleted: " + attribute);
		}
	}

	/**
	 * Find the set of all {@link TLObject objects} that reference the given object through the
	 * given reference.
	 * 
	 * @param self
	 *        The object that searched for in all attribute values of this attribute.
	 * @param reference
	 *        The reference that is navigated backwards.
	 * @return The set of {@link TLObject} objects that refer to the given object by having it set
	 *         as value in this attribute.
	 * 
	 * @deprecated Use {@link TLObject#tReferers(TLReference)}.
	 */
	@Deprecated
	public static Set<? extends TLObject> getReferers(TLObject self, TLStructuredTypePart reference) {
		return self.tReferers((TLReference) reference);
	}

	/**
	 * Potential values that can be set to the given attribute.
	 * 
	 * <p>
	 * Deprecated: Use {@link #allOptions(EditContext)}, other parameters are unused.
	 * </p>
	 * 
	 * @param self
	 *        The base object.
	 * @param editContext
	 *        The current update context.
	 * @param form
	 *        The current form inputs.
	 * @return All available options.
	 * 
	 * @deprecated Use {@link #allOptions(EditContext)}.
	 */
	@Deprecated
	public static OptionModel<?> getOptions(TLObject self, EditContext editContext, FormContextProxy form) {
		return allOptions(editContext);
	}

	/**
	 * Adjusts the given options to the concrete attribute.
	 */
	public static List<?> adjustOptions(EditContext editContext, List<?> allOptions) {

		Filter<Object> filter = createOptionsFilter(editContext);
		return FilterUtil.filterList(filter, allOptions);
	}

	private static Filter<Object> createOptionsFilter(EditContext context) {
		AttributedValueFilter filter = context.getFilter();
		
		if (filter == null) {
			return FilterFactory.trueFilter();
		}

		return new Filter<>() {

			@Override
			public boolean accept(Object object) {
				return filter.accept(object, context);
			}

		};
	}

	/**
	 * Potential values that can be set in the given context.
	 * 
	 * @param editContext
	 *        The current update context.
	 * 
	 * @return All available options.
	 */
	public static OptionModel<?> allOptions(EditContext editContext) {
		Filter<Object> optionsFilter = createOptionsFilter(editContext);
		Generator generator = editContext.getOptions();
		if (generator == null) {
			TLType type = editContext.getValueType();
			if (type instanceof TLEnumeration) {
				return createFilteredOptionModel(optionsFilter, ((TLEnumeration) type).getClassifiers());
			} else if (type instanceof TLClass) {
				return createLazyListOptionModel(optionsFilter, () -> allInstances((TLClass) type, TLObject.class));
			} else {
				return new DefaultListOptionModel<>(Collections.emptyList());
			}

		} else {
			OptionModel<?> optionModel = generator.generate(editContext);
			if (optionModel instanceof ListOptionModel) {
				if (FilterFactory.isTrue(optionsFilter)) {
					return optionModel;
				} else {
					return createLazyListOptionModel(optionsFilter, () -> (List<?>) optionModel.getBaseModel());
				}
			} else {
				return optionModel;
			}
		}
	}

	private static ListOptionModel<?> createLazyListOptionModel(Filter<Object> optionsFilter,
			Supplier<List<?>> options) {
		LazyListUnmodifyable<?> lazyOptions = new LazyListUnmodifyable<>() {

			@Override
			protected List<?> initInstance() {
				return FilterUtil.filterList(optionsFilter, options.get());
			}

		};

		return new DefaultListOptionModel<>(lazyOptions);
	}

	private static ListOptionModel<?> createFilteredOptionModel(Filter<Object> optionsFilter, List<?> allOptions) {
		List<?> options;
		if (FilterFactory.isTrue(optionsFilter)) {
			options = allOptions;
		} else if (FilterFactory.isFalse(optionsFilter)) {
			options = Collections.emptyList();
		} else {
			options = new LazyListUnmodifyable<>() {

				@Override
				protected List<?> initInstance() {
					return FilterUtil.filterList(optionsFilter, allOptions);
				}

			};
		}

		return new DefaultListOptionModel<>(options);
	}

	static <T extends TLObject> CloseableIterator<T> allDirectInstances(TLClass type, Class<T> expectedType) {
		if (type == null || type.isAbstract()) {
			return EmptyClosableIterator.getInstance();
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		MORepository tableRepository = kb.getMORepository();
		String tableName = TLAnnotations.getTable(type);
		try {
			MOClass tableType = (MOClass) tableRepository.getType(tableName);
			return instancesInTable(kb, expectedType, tableType, Collections.singleton(type));
		} catch (UnknownTypeException ex) {
			Logger.error("Undefined table '" + tableName + "' for type '" + type + "'.", ex);
			return EmptyClosableIterator.getInstance();
		}
	}

	static <T extends TLObject> List<T> allInstances(TLClass type, final Class<T> expectedType) {
		ArrayList<T> buffer = new ArrayList<>();

		try (ConcatenatedClosableIterator<T> instances = allInstancesIterator(type, expectedType)){
			while (instances.hasNext()) {
				buffer.add(instances.next());
			}
		}

		return buffer;
	}

	/**
	 * All instances of of a certain type.
	 * 
	 * <p>
	 * Note: The resulting iterator must be closed after usage.
	 * </p>
	 * 
	 * @param type
	 *        The type to retrieve instances for.
	 * @param expectedType
	 *        The expected Java collection type.
	 * @return {@link CloseableIterator} of all instances of the given type and all its subclasses.
	 */
	public static <T extends TLObject> ConcatenatedClosableIterator<T> allInstancesIterator(TLClass type,
			final Class<T> expectedType) {
		Map<String, Set<TLClass>> typesByTable = typesByTableName(type);

		return new ConcatenatedClosableIterator<>(
			new MappedIterator<Entry<String, Set<TLClass>>, CloseableIterator<T>>(typesByTable.entrySet().iterator()) {

				KnowledgeBase _kb = PersistencyLayer.getKnowledgeBase();

				MORepository _tableRepository = _kb.getMORepository();

				@Override
				protected CloseableIterator<T> map(Entry<String, Set<TLClass>> tableEntry) {
					String tableName = tableEntry.getKey();
					Set<TLClass> types = tableEntry.getValue();

					try {
						MOClass tableType = (MOClass) _tableRepository.getType(tableName);

						return instancesInTable(_kb, expectedType, tableType, types);
					} catch (UnknownTypeException ex) {
						Logger.error("Undefined table '" + tableName + "' for type '" + type + "'.", ex);
						return EmptyClosableIterator.getInstance();
					}
				}
			});
	}

	/**
	 * All concrete subtypes of the given {@link TLClass} type partitioned by the table name the
	 * concrete type is stored in.
	 * 
	 * @param type
	 *        The (potentially) abstract type to analyze.
	 * @return Mapping of {@link MOClass#getName() table names} to concrete subtypes of the given
	 *         type stored in that table.
	 * 
	 * @see #tableQuery(MOClass, Set)
	 */
	public static Map<String, Set<TLClass>> typesByTableName(TLClass type) {
		Collection<TLClass> concreteTypes = getConcreteSpecializations(type);
		HashMap<String, Set<TLClass>> typesByTable = new HashMap<>();
		for (TLClass concreteClass : concreteTypes) {
			String tableName = TLAnnotations.getTable(concreteClass);
			MultiMaps.add(typesByTable, tableName, concreteClass);
		}
		return typesByTable;
	}

	static <T extends TLObject> CloseableIterator<T> instancesInTable(KnowledgeBase kb, Class<T> expectedType,
			MOClass tableType, Set<TLClass> types) {
		SetExpression query = tableQuery(tableType, types);
		return kb.<T>searchStream(queryResolved(query, expectedType));
	}

	/**
	 * {@link KnowledgeBase} query expression delivering all instances of the concrete
	 * {@link TLClass}es from the given table.
	 * 
	 * @param tableType
	 *        The {@link MOClass#getName() table} to look in.
	 * @param types
	 *        The concrete types of objects to resolve (excluding subtypes). See
	 *        {@link #typesByTableName(TLClass)}.
	 * @return {@link KnowledgeBase} query expression retrieving all instances of the given types
	 *         from the given table.
	 */
	public static SetExpression tableQuery(MOClass tableType, Set<TLClass> types) {
		if (tableType instanceof MOKnowledgeItem) {
			InstancesQueryBuilder builder = ((MOKnowledgeItem) tableType).getInstancesQueryBuilder();
			if (builder == null ) {
				InstancesQueryAnnotation annotation = tableType.getAnnotation(InstancesQueryAnnotation.class);
				if (annotation != null) {
					throw new IllegalStateException(
						"No " + InstancesQueryBuilder.class + " but annotation " + annotation + "  found for table " + tableType + ".");
				} else {
					throw new IllegalStateException(
						"No " + InstancesQueryBuilder.class + " and no " + InstancesQueryAnnotation.class + " annotation for table " + tableType + ".");
				}
			}
			return builder.createInstancesQuery(tableType, types);
		} else {
			return MonomorphicQueryBuilder.INSTANCE.createInstancesQuery(tableType, types);
		}
	}

	private static Collection<TLClass> getConcreteSpecializations(TLClass type) {
		ArrayList<TLClass> buffer = new ArrayList<>();
		fillConcreteSpecializations(buffer, new HashSet<>(), type);
		return buffer;
	}

	private static void fillConcreteSpecializations(List<TLClass> buffer, Set<TLClass> seen, TLClass type) {
		if (seen.contains(type)) {
			return;
		}
		seen.add(type);

		if (!type.isAbstract()) {
			buffer.add(type);
		}

		for (TLClass specialization : type.getSpecializations()) {
			fillConcreteSpecializations(buffer, seen, specialization);
		}
	}

	/**
	 * Fetches the classification list for a {@link #isClassificationAttribute(TLStructuredTypePart)
	 * classification attribute}.
	 */
	public static FastList getClassificationList(TLStructuredTypePart attribute) {
		return (FastList) attribute.getType();
	}

	public static BooleanPresentation getBooleanDisplay(TLModelPart modelPart) {
		return booleanDisplayValue(TLAnnotations.getAnnotation(modelPart, BooleanDisplay.class));
	}

	public static BooleanPresentation getBooleanDisplay(EditContext editContext) {
		return booleanDisplayValue(editContext.getAnnotation(BooleanDisplay.class));
	}

	/**
	 * The {@link BooleanPresentation} of the given annotation, or a default value, if the given
	 * annotation is <code>null</code>.
	 */
	private static BooleanPresentation booleanDisplayValue(BooleanDisplay displayAnnotation) {
		if (displayAnnotation != null) {
			return displayAnnotation.getPresentation();
		} else {
			return BooleanPresentation.CHECKBOX;
		}
	}

	public static Double getMaximum(TLModelPart modelPart) {
		return getMaximum(TLAnnotations.getAnnotation(modelPart, TLRange.class));
	}

	/**
	 * The configured maximum of the given range configuration.
	 *
	 * @param annotation
	 *        A configuration, or <code>null</code>.
	 */
	public static Double getMaximum(TLRange annotation) {
		if (annotation == null) {
			return null;
		}
		return annotation.getMaximum();
	}

	public static Double getMinimum(TLModelPart modelPart) {
		return getMinimum(TLAnnotations.getAnnotation(modelPart, TLRange.class));
	}

	/**
	 * The configured minimum of the given range configuration.
	 *
	 * @param annotation
	 *        A configuration, or <code>null</code>.
	 */
	public static Double getMinimum(TLRange annotation) {
		if (annotation == null) {
			return null;
		}
		return annotation.getMinimum();
	}

	public static int getUpperBound(TLModelPart modelPart) {
		TLSize annotation = TLAnnotations.getAnnotation(modelPart, TLSize.class);
		return getUpperBound(annotation);
	}

	/**
	 * The upper bound of the given size annotation, or some default value, if none is given.
	 */
	public static int getUpperBound(TLSize annotation) {
		if (annotation == null) {
			return 255;
		}
		return (int) annotation.getUpperBound();
	}

	public static int getLowerBound(TLModelPart modelPart) {
		return getLowerBound(TLAnnotations.getAnnotation(modelPart, TLSize.class));
	}

	/**
	 * The lower bound of the given {@link TLSize} annotation, or <code>0</code>, if none is given.
	 */
	public static int getLowerBound(TLSize annotation) {
		if (annotation == null) {
			return 0;
		}
		return (int) annotation.getLowerBound();
	}

	/**
	 * Returns the maximum length of the given {@link #isTextAttribute(TLStructuredTypePart) text
	 * attribute}.
	 * 
	 * @return The maxmimum length or <code>null</code> when none configured.
	 */
	public static Integer getLength(TLModelPart modelPart) {
		TLSize annotation = TLAnnotations.getAnnotation(modelPart, TLSize.class);
		if (annotation == null) {
			return null;
		}
		return (int) annotation.getUpperBound();
	}

	/**
	 * Whether the given attribut should be rendered mult-line.
	 */
	public static boolean isMultiline(TLModelPart modelPart) {
		return isMultiline(TLAnnotations.getAnnotation(modelPart, MultiLine.class));
	}

	/**
	 * Whether the input element at the given edit location should be rendered mult-line.
	 */
	public static boolean isMultiline(EditContext editContext) {
		return isMultiline(TLAnnotations.getAnnotation(editContext, MultiLine.class));
	}

	/**
	 * {@link MultiLine#getValue()}, or a default, if none is given.
	 */
	public static boolean isMultiline(MultiLine annotation) {
		if (annotation == null) {
			return false;
		}
		return annotation.getValue();
	}

	/**
	 * The {@link ReferencePresentation} value of the annotation {@link ReferenceDisplay} for the
	 * given {@link EditContext}.
	 */
	public static ReferencePresentation getPresentation(EditContext context) {
		ReferenceDisplay annotation = context.getAnnotation(ReferenceDisplay.class);
		if (annotation == null) {
			return ReferencePresentation.POP_UP;
		}
		return annotation.getValue();
	}

	/**
	 * The {@link OptionsPresentation} value of the annotation {@link OptionsDisplay} for the given
	 * model part.
	 * 
	 * @param modelPart
	 *        The model part to get {@link OptionsPresentation}.
	 * @return May be <code>null</code>, when no {@link OptionsDisplay} is set.
	 */
	public static OptionsPresentation getOptionsPresentation(TLStructuredTypePart modelPart) {
		return getOptionsPresentation(TLAnnotations.getAnnotation(modelPart, OptionsDisplay.class),
			modelPart.getType());
	}

	/**
	 * The {@link OptionsPresentation} value of the given annotation, or the default given by the
	 * type.
	 */
	public static OptionsPresentation getOptionsPresentation(OptionsDisplay annotation, TLType targetType) {
		if (annotation != null) {
			return annotation.getValue();
		}
		OptionsDisplay targetTypeAnnotation = TLAnnotations.getAnnotation(targetType, OptionsDisplay.class);
		if (targetTypeAnnotation != null) {
			return targetTypeAnnotation.getValue();
		}
		return null;
	}

	public static String getFolderType(TLModelPart modelPart) {
		FolderType annotation = TLAnnotations.getAnnotation(modelPart, FolderType.class);
		if (annotation == null) {
			return WebFolderFactory.STANDARD_FOLDER;
		}
		return annotation.getValue();
	}

	/**
	 * Service method to get all parts of the given type which are
	 * {@link #isWebFolderAttribute(TLStructuredTypePart) web folder attributes}.
	 */
	public static List<TLStructuredTypePart> getWebFolderAttributes(TLStructuredType type) {
		List<TLStructuredTypePart> folderParts = new ArrayList<>(2);
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (isWebFolderAttribute(part)) {
				folderParts.add(part);
			}
		}
		return folderParts;
	}

	public static boolean isWebFolderAttribute(TLStructuredTypePart attribute) {
		return WebFolder.WEB_FOLDER_TYPE.equals(TLModelUtil.qualifiedName(attribute.getType()));
	}

	public static boolean isTableAttribute(String expectedMetaObjectName, TLStructuredTypePart attribute) {
		String tableName = ApplicationObjectUtil.tableTypeName(expectedMetaObjectName);
		return storesInTable(attribute, tableName);
	}

	/**
	 * Whether the given type is the table type with the given table name.
	 */
	public static boolean isTableType(String expectedMetaObjectName, TLType type) {
		String tableName = ApplicationObjectUtil.tableTypeName(expectedMetaObjectName);
		return storesInTable(type, tableName);
	}

	public static boolean isInterfaceTableAttribute(String expectedMetaObjectName, TLStructuredTypePart attribute) {
		String tableName = ApplicationObjectUtil.iTableTypeName(expectedMetaObjectName);
		return storesInTable(attribute, tableName);
	}

	private static boolean storesInTable(TLStructuredTypePart attribute, String tableName) {
		if (attribute == null) {
			return false;
		}
		TLType type = attribute.getType();
		return storesInTable(type, tableName);
	}

	/**
	 * Whether the given type stores its instances in the table type with the given name.
	 */
	private static boolean storesInTable(TLType type, String tableName) {
		if (!(type instanceof TLClass)) {
			return false;
		}
		return type.getName().equals(tableName);
	}

	public static boolean allowsSearchRange(TLStructuredTypePart attribute) {
		if (attribute.isMultiple()) {
			return false;
		}
		TLSearchRange attributeAnnotation = attribute.getAnnotation(TLSearchRange.class);
		if (attributeAnnotation != null) {
			return attributeAnnotation.getValue();
		}
		return false;
	}

	public static Set<TLStructuredTypePart> getCheckListAttributes(final TLObject element,
			boolean includeSuperMetaElements, boolean includeSubMetaElements) {
		TLClass metaElement = (TLClass) element.tType();
		Set<TLStructuredTypePart> classificationAttributes = new HashSet<>(
			MetaElementUtil.getMetaAttributes(metaElement, LegacyTypeCodes.TYPE_CLASSIFICATION, includeSuperMetaElements, includeSubMetaElements));
		for (Iterator<TLStructuredTypePart> it = classificationAttributes.iterator(); it.hasNext();) {
			if (TLAnnotations.getClassificationPresentation(it.next()) != ClassificationPresentation.CHECKLIST) {
				it.remove();
			}
		}
		return classificationAttributes;
	}

	/**
	 * @deprecated Use {@link TLStructuredTypePart#getType()}.
	 */
	@Deprecated
	public static int getMetaAttributeType(TLStructuredTypePart metaAttribute) {
		return AttributeSettings.getInstance().getImplementationId(AttributeSettings.getConfigType(metaAttribute));
	}

	/**
	 * @deprecated Use {@link EditContext#getValueType()}.
	 */
	@Deprecated
	public static int getMetaAttributeType(EditContext editContext) {
		return AttributeSettings.getInstance().getImplementationId(AttributeSettings.getConfigType(editContext));
	}

	/**
	 * Whether the given attribute is a composition reference.
	 */
	public static boolean isComposition(TLTypePart attribute) {
		return attribute.getModelKind() == ModelKind.REFERENCE && ((TLReference) attribute).getEnd().isComposite();
	}

	/**
	 * Whether the given edit location edits a composition reference.
	 */
	public static boolean isComposition(EditContext editContext) {
		return editContext.isComposition();
	}

	/**
	 * Checks whether the given {@link TLStructuredTypePart} has a data type with the given
	 * qualified name.
	 * 
	 * @param expectedDataTypeName
	 *        Qualified name of a datatype.
	 * @param attribute
	 *        The attribute to check.
	 * 
	 * @see TypeSpec
	 * @see TypeSpec#DATE_TYPE
	 */
	public static boolean isDataTypeAttribute(String expectedDataTypeName, TLStructuredTypePart attribute) {
		if (attribute == null) {
			return false;
		}
		TLType type = attribute.getType();
	
		if (!isDataType(type)) {
			return false;
		}
	
		TLScope scope = type.getScope();
		if (!(scope instanceof TLModule)) {
			return false;
		}

		String dataTypeName = ((TLModule) scope).getName() + ":" + type.getName();
		return expectedDataTypeName.equals(dataTypeName);
	}

	/**
	 * Whether the given {@link TLType} is as data type.
	 */
	public static boolean isDataType(TLType type) {
		return type instanceof TLPrimitive;
	}

	public static boolean isClassificationAttribute(TLStructuredTypePart attribute) {
		if (attribute == null) {
			return false;
		}
		return attribute.getType() instanceof TLEnumeration;
	}

	public static boolean isDocumentAttribute(TLStructuredTypePart attribute) {
		return Document.DOCUMENT_TYPE.equals(TLModelUtil.qualifiedName(attribute.getType()));
	}

	public static boolean isNumberAttribute(TLStructuredTypePart attribute) {
		return isDataTypeAttribute(TypeSpec.LONG_TYPE, attribute)
				|| isDataTypeAttribute(TypeSpec.DOUBLE_TYPE, attribute);
	}

	/**
	 * The annotated {@link ScopeRef scope reference}.
	 */
	public static ScopeRef getScopeRef(TLType type) {
		ScopeRef annotation = TLAnnotations.getAnnotation(type, ScopeRef.class);
		if (annotation != null) {
			return annotation;
		}
		return globalTypeRef(type.getName());
	}

	/**
	 * The annotated scope reference.
	 * 
	 * @see ScopeRef
	 */
	public static ScopeRef getScopeRef(TLStructuredTypePart containment) {
		ScopeRef annotation = TLAnnotations.getAnnotation(containment, ScopeRef.class);
		if (annotation != null) {
			return annotation;
		}

		return globalTypeRef(containment.getType().getName());
	}

	public static ScopeRef globalTypeRef(String createType) {
		ScopeRef result = TypedConfiguration.newConfigItem(ScopeRef.class);
		result.setCreateType(createType);
		return result;
	}

	/**
	 * Whether the attribute contains boolean values.
	 */
	public static boolean isBooleanAttribute(TLStructuredTypePart attribute) {
		return isDataTypeAttribute(TypeSpec.BOOLEAN_TYPE, attribute);
	}

	/**
	 * Whether the attribute contains tristate values.
	 */
	public static boolean isTristateAttribute(TLStructuredTypePart attribute) {
		return isDataTypeAttribute(TypeSpec.TRISTATE_TYPE, attribute);
	}

	/**
	 * Whether the attribute contains binary values.
	 */
	public static boolean isBinaryAttribute(TLStructuredTypePart attribute) {
		return isDataTypeAttribute(TypeSpec.BINARY_TYPE, attribute);
	}

	/**
	 * Whether the attribute contains text.
	 */
	public static boolean isTextAttribute(TLStructuredTypePart attribute) {
		return isDataTypeAttribute(TypeSpec.STRING_TYPE, attribute);
	}

	/**
	 * Whether the attribute contains date.
	 */
	public static boolean isDateAttribute(TLStructuredTypePart attribute) {
		if (attribute == null) {
			return false;
		}
		TLType type = attribute.getType();

		if (!(type instanceof TLPrimitive)) {
			return false;
		}

		return ((TLPrimitive) type).getKind() == Kind.DATE;
	}

	/**
	 * The {@link TLValidityCheck#getValue()} of the given attribute, or <code>null</code>
	 * if no such annotation exists.
	 */
	public static String getValidityCheckSpec(TLStructuredTypePart attribute) {
		TLValidityCheck validityAnnotation = attribute.getAnnotation(TLValidityCheck.class);
		if (validityAnnotation == null) {
			return null;
		}
		return validityAnnotation.getValue();
	}

	/**
	 * The configured option provider.
	 * 
	 * @see TLOptions
	 */
	public static Generator getOptions(TLStructuredTypePart attribute) {
		return (Generator) attribute.tGetData(OPTIONS_ATTRIBUTE);
	}

	/**
	 * The configured constraint.
	 * 
	 * @see TLConstraint
	 */
	public static AttributedValueFilter getConstraint(TLStructuredTypePart attribute) {
		return (AttributedValueFilter) attribute.tGetData(CONSTRAINT_ATTRIBUTE);
	}

	/**
	 * Whether {@link #getClassifiers(TLStructuredTypePart)} exist.
	 */
	public static boolean isClassified(TLStructuredTypePart attribute) {
		return KBBasedMetaAttribute.USE_ATTRIBUTE_CLASSIFIERS && !AttributeOperations.getClassifiers(attribute).isEmpty();
	}

	/**
	 * Get a set of objects classifying the meta attribute.
	 * 
	 * @return never null;
	 */
	public static Set<TLClassifier> getClassifiers(TLStructuredTypePart attribute) {
		if (!KBBasedMetaAttribute.USE_ATTRIBUTE_CLASSIFIERS) {
			return Collections.emptySet();
		} else {
			return AssociationQueryUtil.resolveWrappers((KnowledgeObject) attribute.tHandle(), TLClassifier.class,
				KBBasedMetaAttribute.CLASSIFIER_QUERY);
		}
	}

	/**
	 * Sets the new {@link #getClassifiers(TLStructuredTypePart)}.
	 */
	public static void setClassifiers(TLStructuredTypePart attribute, Collection<? extends TLClassifier> classifiers) {
		KnowledgeObject handle = (KnowledgeObject) attribute.tHandle();
		KBUtils.deleteAllKI(handle.getOutgoingAssociations(AttributeClassifierManager.KA_CLASSIFIED_BY));
		if (classifiers != null) {
			for (TLClassifier classifier : classifiers) {
				AbstractWrapper.associateWith(attribute, classifier, AttributeClassifierManager.KA_CLASSIFIED_BY);
			}
		}
	}

	/**
	 * @deprecated Use {@link TLStructuredTypePart#getOwner()}
	 */
	@Deprecated
	public static TLClass getMetaElement(TLStructuredTypePart attribute) throws AttributeException {
		return (TLClass) attribute.getOwner();
	}

	/**
	 * Return the handler for validity checks and managing.
	 * 
	 * @return The requested ValidityCheck, never <code>null</code>.
	 * 
	 * @see TLValidityCheck
	 */
	public static ValidityCheck getValidityCheck(TLStructuredTypePart attribute) {
		return (ValidityCheck) attribute.tGetData(ConfiguredAttributeImpl.VALIDITY_CHECK_ATTRIBUTE);
	}

	/**
	 * @see TLFullTextRelevant
	 */
	public static boolean isFullTextRelevant(TLStructuredTypePart attribute) {
		return AttributeSettings.isFullTextRelevant(attribute);
	}

	/**
	 * Get the ids of all access rights on of the given to one of the given roles
	 * 
	 * @param roles
	 *        the roles to consider, may be null
	 * @return the set of access right ids, never null
	 */
	public static Set<BoundCommandGroup> getAccess(TLStructuredTypePart attribute, Collection<BoundRole> roles) {
		if (roles == null || roles.isEmpty()) {
			return Collections.emptySet();
		}
		
		Map<BoundedRole, Set<BoundCommandGroup>> rightsByRole = ElementAccessHelper.getAccessRights(attribute);

		Set<BoundCommandGroup> result = new HashSet<>();
		for (BoundRole theRole : roles) {
			Collection<BoundCommandGroup> rights = rightsByRole.get(theRole);
			if (rights != null) {
				result.addAll(rights);
		    }
		}
		return result;
	}

	/**
	 * The {@link StorageImplementation} of the given attribute in the context of the given object.
	 * 
	 * <p>
	 * In contrast to {@link #getStorageImplementation(TLStructuredTypePart)}, this method considers
	 * potential overrides of the given attribute in the concrete type of the given object.
	 * </p>
	 *
	 * @param object
	 *        The context that is accessed.
	 * @param attribute
	 *        The attribute to retrieve the storage implementation for.
	 * @return The {@link StorageImplementation} for the given attribute in the given concrete
	 *         context considering potential attribute overrides.
	 */
	public static StorageImplementation getStorageImplementation(TLObject object, TLStructuredTypePart attribute) {
		TLStructuredTypePart matchingOverride = getAttributeOverride(object, attribute);
		if (matchingOverride == null) {
			return NO_STORAGE;
		}
		return AttributeOperations.getStorageImplementation(matchingOverride);
	}

	/**
	 * Encapsulation of operational semantics of this attribute.
	 * 
	 * @see #getStorageImplementation(TLObject, TLStructuredTypePart)
	 */
	public static StorageImplementation getStorageImplementation(TLStructuredTypePart attribute) {
		return (StorageImplementation) attribute.getStorageImplementation();
	}

	/**
	 * Whether the value of this attribute may not be set neither by the user in the UI nor by the
	 * application with explicit calls to {@link TLObject#tUpdate(TLStructuredTypePart, Object)}.
	 * 
	 * <p>
	 * A meta attribute might be read-only e.g. because:
	 * </p>
	 * 
	 * <ul>
	 * <li>There is no storage location for its value, because the value is computed on demand.</li>
	 * 
	 * <li>The value is fetched from another system, where no write-back is possible</li>.
	 * </ul>
	 */
	public static boolean isReadOnly(TLStructuredTypePart metaAttribute) {
		return metaAttribute.isDerived();
	}

	/**
	 * Check if the attribute works with single values or collections of values (true).
	 * 
	 * @return true if the attribute works with collections of values
	 */
	public static boolean isCollectionValued(TLStructuredTypePart attribute) {
		return attribute.isMultiple();
	}

	/**
	 * Add a value to a collection-valued attribute.
	 * 
	 * <p>
	 * If the attribute is ordered and the value is added to the end of the current collection.
	 * </p>
	 * 
	 * @param aKey
	 *        the attribute name
	 * @param aValue
	 *        the value to add
	 */
	public static void addValue(TLObject attributed, String aKey, Object aValue) {
		PersistentObjectImpl.addValue(attributed, aKey, aValue);
	}

	/**
	 * Remove a value from a collection-valued attribute
	 * 
	 * @param aKey
	 *        the attribute name
	 * @param aValue
	 *        the value
	 */
	public static void removeValue(TLObject attributed, String aKey, Object aValue) {
		PersistentObjectImpl.removeValue(attributed, aKey, aValue);
	}

	/**
	 * Sets the last change date of the given attribute to current date. This only takes effect on
	 * attributes that have an active validity check.
	 *
	 * @param part
	 *        the attribute to "touch"
	 */
	public static void touch(TLObject attributed, TLStructuredTypePart part) {
		PersistentObjectImpl.touch(attributed, part);
	}

	/**
	 * Sets the last change date of the given attribute to the given date.
	 * 
	 * @param part
	 *        the attribute to "touch"
	 * @param aDate
	 *        the date to set the last changed date attribute
	 */
	public static void touch(TLObject attributed, TLStructuredTypePart part, Date aDate) {
		attributed.tSetData(PersistentObjectImpl.getTouchProperty(part), aDate);
	}

	/**
	 * Get the time stamp of the last touch to the given attribute. If there is no validityTime
	 * specified for the attribute, this method will return 0 (zero).
	 *
	 * @param part
	 *        the attribute
	 * @return the time the attribute was last touched (timestamp in milliseconds), 0 (zero) if no
	 *         validity time is specified for the attribute.
	 */
	public static Date getLastChangeDate(TLObject attributed, TLStructuredTypePart part) {
		return attributed.tGetDataDate(PersistentObjectImpl.getTouchProperty(part));
	}

	/**
	 * Returns whether the element is rendered over the whole line.
	 * 
	 * @param attribute
	 *        the attribute.
	 * @return true if the element is rendered over the whole line.
	 */
	public static boolean renderWholeLine(TLStructuredTypePart attribute, EditContext context) {
		RenderWholeLineAnnotation wholeLineAnnotation = context.getAnnotation(RenderWholeLineAnnotation.class);
		if (wholeLineAnnotation != null) {
			return wholeLineAnnotation.getValue();
		}
		ReferenceDisplay referenceDisplayAnnotation = context.getAnnotation(ReferenceDisplay.class);
		if (referenceDisplayAnnotation != null) {
			if (ReferencePresentation.TABLE == referenceDisplayAnnotation.getValue()) {
				return true;
			}
		}
		return getFieldProvider(attribute).renderWholeLine(context);
	}

	/**
	 * Determines the {@link FieldProvider} to create {@link FormMember} for the given attribute.
	 * 
	 * @param attribute
	 *        Attribute to get {@link FieldProvider} for.
	 * @return May be <code>null</code>, when no {@link FieldProvider} can be found.
	 */
	public static FieldProvider getFieldProvider(TLStructuredTypePart attribute) {
		FieldProviderAnnotation annotation = attribute.getAnnotation(FieldProviderAnnotation.class);
		if (annotation == null) {
			return null;
		}
		return TypedConfigUtil.createInstance(annotation.getImpl());
	}

	/**
	 * Returns whether the element is rendered over the whole line.
	 * 
	 * @param attribute
	 *        the attribute.
	 * @param update
	 *        Pending attribute updates container.
	 * @return true if the element is rendered over the whole line.
	 */
	public static boolean renderInputBeforeLabel(TLStructuredTypePart attribute, AttributeUpdate update) {
		if (update != null) {
			return inputBeforeLabel(attribute, update.getAnnotation(RenderInputBeforeLabelAnnotation.class));
		}

		return false;
	}

	/**
	 * Returns whether the element is rendered over the whole line.
	 * 
	 * @param attribute
	 *        the attribute.
	 * @return true if the element is rendered over the whole line.
	 */
	public static boolean renderInputBeforeLabel(TLStructuredTypePart attribute) {
		return inputBeforeLabel(attribute, attribute.getAnnotation(RenderInputBeforeLabelAnnotation.class));
	}

	private static boolean inputBeforeLabel(TLStructuredTypePart attribute, BooleanAnnotation annotation) {
		if (annotation != null) {
			return annotation.getValue();
		}

		if (isBooleanAttribute(attribute) || isTristateAttribute(attribute)) {
			if (getBooleanDisplay(attribute) == BooleanPresentation.CHECKBOX) {
				return true;
			}
		}

		return false;
	}
}
