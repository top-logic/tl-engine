/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.util.TLTypeContext;
import com.top_logic.model.util.TypePartContext;
import com.top_logic.util.model.CompatibilityService;

/**
 * Default implementation of the {@link TableConfigModelInfo}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableConfigModelInfoImpl extends ColumnInfoFactory implements TableConfigModelInfo {

	private final Set<TLClass> _contentTypes;

	private final Set<TLClass> _commonSuperClasses;

	private final Set<String> _mainColumns;

	private final Map<String, ColumnInfo> _columnInfos;

	/**
	 * Creates a {@link TableConfigModelInfoImpl} for the given content classes.
	 * 
	 * @param classes
	 *        The content classes that may occur in the table, neither <code>null</code> nor empty.
	 */
	protected TableConfigModelInfoImpl(Collection<? extends TLClass> classes) {
		if (CollectionUtil.isEmptyOrNull(classes)) {
			throw new IllegalArgumentException("The given set of classes must not be empty or null.");
		}
		_contentTypes = unmodifiableSet(set(classes));
		_commonSuperClasses = unmodifiableSet(getCommonGeneralizations(classes));

		Map<TLClass, List<TLStructuredTypePart>> partsByType = partCalculator().calcLocalPartsByType(classes);
		LinkedHashMap<String, ColumnInfo> columnInfos = createColumnInfos(partsByType);
		List<String> mainColumns = new ArrayList<>(mainColumnCalculator(classes, partsByType).calcMainColumns());
		LinkedHashMap<String, ColumnInfo> columnInfosOrdered = sortToFront(columnInfos, mainColumns);

		_mainColumns = unmodifiableSet(new LinkedHashSet<>(mainColumns));
		_columnInfos = unmodifiableMap(columnInfosOrdered);
	}

	/**
	 * Algorithm computing visible parts.
	 */
	protected PartCalculator partCalculator() {
		return new PartCalculator();
	}

	/**
	 * Algorithm computing main properties of types.
	 */
	protected MainColumnCalculator mainColumnCalculator(Collection<? extends TLClass> classes,
			Map<TLClass, List<TLStructuredTypePart>> visibleLocalPartsByType) {
		return new MainColumnCalculator(classes, visibleLocalPartsByType);
	}

	/**
	 * Algorithm computing visible local parts per type.
	 */
	protected static class PartCalculator {

		/**
		 * Calculate the visible {@link TLStructuredTypePart}s for each of the given
		 * {@link TLClass}es.
		 * 
		 * @param contentTypes
		 *        Is not allowed to be null. Is allowed to be empty.
		 * @return A new, mutable and resizable {@link Map}. The values are immutable.
		 */
		public Map<TLClass, List<TLStructuredTypePart>> calcLocalPartsByType(
				Collection<? extends TLClass> contentTypes) {
			Map<TLClass, List<TLStructuredTypePart>> result = linkedMap();
			for (TLClass type : contentTypes) {
				for (TLClass specialization : getSpecializations(type)) {
					addGeneralizationParts(result, specialization);
				}
			}
			return result;
		}

		private Collection<TLClass> getSpecializations(TLClass baseType) {
			Set<TLClass> concreteTypes = getAllConcreteSubtypes(baseType);
			if (concreteTypes.isEmpty()) {
				return singletonList(baseType);
			}
			return concreteTypes;
		}

		private Set<TLClass> getAllConcreteSubtypes(TLClass baseType) {
			return TLModelUtil.getReflexiveTransitiveSpecializations(TLModelUtil.IS_CONCRETE, baseType);
		}

		private void addGeneralizationParts(Map<TLClass, List<TLStructuredTypePart>> result, TLClass tlClass) {
			boolean isNew = addLocalParts(result, tlClass);
			if (isNew) {
				for (TLClass superClass : tlClass.getGeneralizations()) {
					addGeneralizationParts(result, superClass);
				}
			}
		}

		private boolean addLocalParts(Map<TLClass, List<TLStructuredTypePart>> result, TLClass tlClass) {
			if (result.containsKey(tlClass)) {
				return false;
			}

			List<TLStructuredTypePart> visibleParts = unmodifiableList(calcParts(tlClass));
			result.put(tlClass, visibleParts);
			return true;
		}

		/**
		 * The visible {@link TLStructuredTypePart}s of the given {@link TLClass}.
		 * 
		 * @param tlClass
		 *        Never null.
		 * @return A new, mutable and resizable List.
		 */
		protected List<? extends TLStructuredTypePart> calcParts(TLClass tlClass) {
			return tlClass.getLocalParts();
		}
	}

	/**
	 * Algorithm computing main properties.
	 */
	protected static class MainColumnCalculator {

		private final Collection<? extends TLClass> _contentTypes;

		private final Map<TLClass, List<TLStructuredTypePart>> _visibleLocalPartsByType;

		/**
		 * Creates a {@link TableConfigModelInfoImpl.MainColumnCalculator}.
		 */
		public MainColumnCalculator(Collection<? extends TLClass> contentTypes,
				Map<TLClass, List<TLStructuredTypePart>> visibleLocalPartsByType) {
			_contentTypes = contentTypes;
			_visibleLocalPartsByType = visibleLocalPartsByType;
		}

		/**
		 * Create a {@link Map} with the main columns of the given {@link TLClass}es.
		 * 
		 * @return A new, mutable and resizable {@link Map}. The values are immutable.
		 */
		protected Map<TLClass, List<String>> calcMainColumnsByType() {
			Map<TLClass, List<String>> mainColumns = map();
			for (TLClass tlClass : _contentTypes) {
				mainColumns.put(tlClass, unmodifiableList(calcMainProperties(tlClass)));
			}
			return mainColumns;
		}

		/**
		 * The {@link MainProperties} of the {@link TLClass}.
		 * <p>
		 * If none are defined, those of the subclasses are returned, recursively. If that does not
		 * lead to any properties, all properties of the type are used.
		 * </p>
		 * <p>
		 * This algorithm has the following properties:
		 * <ol>
		 * <li>The main properties of the given type will always be used when they exist.
		 * Superclasses and subclasses won't override them.</li>
		 * <li>When there are no main properties for the given type, it is ensured that each
		 * subclass can be properly displayed: Either by using its main properties, recursively
		 * collecting the main properties for its subclasses, or by using all of its properties.
		 * That is important for classes with very diverse subclasses, for example "FooChild" types,
		 * which might not even have common properties at all.</li>
		 * <li>The nearest definition of main properties is used: When main properties are defined
		 * for a class, its subclasses are not searched.</li>
		 * <li>As long as the type has properties, the result is not empty. That is important to
		 * avoid displaying a table without columns, as such a table is always useless and its
		 * existence a bug.</li>
		 * <li>All subclasses are equally important when calculating the set of main properties. As
		 * their is no order between subclasses, there cannot be a "main" subclass. (Their names are
		 * used to establish a stable order, though.)</li>
		 * <li>The result has a stable order, as long as the model is not changed. Logouts and
		 * server restarts won't cause the properties to "jump around".</li>
		 * </ol>
		 * </p>
		 * 
		 * @param contentType
		 *        The {@link TLClass} whose main properties should be returned. Is not allowed to be
		 *        null.
		 * @return The main properties. If there are none, the
		 *         {@link #calcVisiblePartNames(TLClass)}. The {@link List} might be immutable.
		 *         Never null.
		 */
		protected List<String> calcMainProperties(TLClass contentType) {
			List<String> mainProperties = DisplayAnnotations.getMainProperties(contentType);
			if (!mainProperties.isEmpty()) {
				return mainProperties;
			}
			List<String> subtypeMainProperties = contentType
				.getSpecializations()
				.stream()
				.sorted(comparing(TLClass::getName)) // Ensure the order is stable.
				.map(this::calcMainProperties)
				.flatMap(Collection::stream)
				.distinct()
				.collect(toList());
			if (!subtypeMainProperties.isEmpty()) {
				return subtypeMainProperties;
			}
			return calcVisiblePartNames(contentType);
		}

		/**
		 * The names of all parts of the given type.
		 */
		public List<String> calcVisiblePartNames(TLClass type) {
			List<String> result = list();
			addVisiblePartNames(result, set(), type);
			return result;
		}

		private void addVisiblePartNames(List<String> result, Set<TLClass> seen, TLClass type) {
			boolean isNew = seen.add(type);
			if (isNew) {
				result.addAll(localNames(_visibleLocalPartsByType.get(type)));
				for (TLClass generalization : type.getGeneralizations()) {
					addVisiblePartNames(result, seen, generalization);
				}
			}
		}

		/**
		 * Calculate the "common main columns".
		 * 
		 * @return Never null. The order is the order in which they should be displayed, initially.
		 */
		public LinkedHashSet<String> calcMainColumns() {
			Map<TLClass, List<String>> mainColumnsByType = calcMainColumnsByType();

			LinkedHashSet<String> result = linkedSet();
			for (TLClass tlClass : _contentTypes) {
				if (result.isEmpty()) {
					result.addAll(mainColumnsByType.get(tlClass));
				} else {
					result.retainAll(mainColumnsByType.get(tlClass));
				}
			}
			checkStableMainColumnsOrder(mainColumnsByType, result);

			return result;
		}

		private void checkStableMainColumnsOrder(Map<TLClass, List<String>> mainColumnsByType,
				LinkedHashSet<String> commonMainColumns) {
			if (commonMainColumns.size() < 2) {
				return;
			}
			if (_contentTypes.size() < 2) {
				return;
			}
			for (TLClass tlClass : _contentTypes) {
				if (!CollectionUtil.haveCommonElementsSameOrder(commonMainColumns, mainColumnsByType.get(tlClass))) {
					errorInconsistentMainColumnOrder(commonMainColumns, _contentTypes);
				}
			}
		}

		private void errorInconsistentMainColumnOrder(Collection<String> commonMainColumns,
				Collection<? extends TLClass> contentTypes) {
			String message = "The common main properties of the following types have inconsistent orders."
				+ " Types: " + qualifiedNames(contentTypes) + ". Common main properties: " + commonMainColumns
				+ ". Which of these orders is picked is pure luck."
				+ " That means, each user might see another column order in those tables.";
			logError(message, new RuntimeException(message));
		}
	}

	/**
	 * Create the {@link ColumnInfo}s for the given {@link TLClass}es.
	 * 
	 * @param localPartsByType
	 *        The {@link TLStructuredTypePart}s to add to the given {@link Map} of
	 *        {@link ColumnInfo}s. Never null.
	 * 
	 * @return A new, mutable and resizable {@link LinkedHashMap}. Never null.
	 */
	private LinkedHashMap<String, ColumnInfo> createColumnInfos(
			Map<TLClass, List<TLStructuredTypePart>> localPartsByType) {
		LinkedHashMap<String, ColumnInfo> result = linkedMap();
		
		for (Entry<TLClass, List<TLStructuredTypePart>> localParts : localPartsByType.entrySet()) {
			addPartColumns(result, localParts.getValue());
		}
		return result;
	}

	/**
	 * Create the {@link ColumnInfo}s for the given {@link TLStructuredTypePart}s and adds them to
	 * the given {@link Map}.
	 * @param result
	 *        The {@link Map} in which the visible parts should be added.
	 * @param parts
	 *        The visible {@link TLStructuredTypePart}s to add to the given {@link Map} of
	 *        {@link ColumnInfo}s.
	 */
	protected void addPartColumns(Map<String, ColumnInfo> result, List<TLStructuredTypePart> parts) {
		for (TLStructuredTypePart typePart : parts) {
			String columnName = typePart.getName();
			ColumnInfo clash = result.get(columnName);
			if ((clash != null) && !isAttributeRelevant(typePart, clash)) {
				/* Keep the existing ColumnInfo, as only it is relevant for this table. */
				continue;
			}
			ColumnInfo info = createColumnInfo(typePart);
			result.put(columnName, info);
			if (clash != null) {
				// Note: The order is important here. the existing column `clash` is joined with the
				// new one keeping core attributes like the label. In case of an overridden
				// attribute, the overriding part is considered first therefore is the `clash` the
				// most specific attribute.
				info = clash.join(info);
				result.put(columnName, info);
			}
		}
	}

	private boolean isAttributeRelevant(TLStructuredTypePart newAttribute, ColumnInfo clash) {
		TLTypePart clashAttribute = clash.getTypeContext().getTypePart();
		if (clashAttribute == null) {
			/* The new attribute is relevant: If there is no attribute, this clash cannot be caused
			 * by an attribute override. And only when this is about an attribute override can the
			 * new attribute be irrelevant. */
			return true;
		}
		/* The cast is safe: Only StructuredTypes can have TypeParts. */
		TLStructuredType clashOwner = (TLStructuredType) clashAttribute.getOwner();
		TLStructuredType ownOwner = newAttribute.getOwner();
		if (!TLModelUtil.isGeneralization(ownOwner, clashOwner)) {
			/* This is not a simple attribute override. The new attribute is therefore relevant. */
			return true;
		}
		return isGeneralizationRelevant(clashOwner, ownOwner);
	}

	/**
	 * Whether the overridden attribute is relevant for this table.
	 * <p>
	 * That is the case, when the table can display subtypes of the original attribute's owner, that
	 * are not subtypes of the specialized attribute's owner.
	 * </p>
	 */
	private boolean isGeneralizationRelevant(TLStructuredType override, TLStructuredType original) {
		for (TLClass contentType : getContentTypes()) {
			if (TLModelUtil.isGeneralization(override, contentType)) {
				// This contentType uses the specialized attribute. The original attribute is not
				// relevant for this content type.
				continue;
			}
			if (TLModelUtil.isGeneralization(original, contentType)
				|| TLModelUtil.isGeneralization(contentType, original)) {
				/* There might be subclasses that use the original attribute, not the specialized
				 * one. The original attribute is therefore relevant. */
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a {@link LinkedHashMap} with the main columns sorted to the front.
	 * 
	 * @param allColumns
	 *        Has to contain each main column. Is not changed by this method. Is not allowed to be
	 *        null.
	 * @param mainColumns
	 *        Is not allowed to be null. Is allowed to be empty.
	 * @return Never null.
	 */
	protected LinkedHashMap<String, ColumnInfo> sortToFront(LinkedHashMap<String, ColumnInfo> allColumns,
			List<String> mainColumns) {
		LinkedHashMap<String, ColumnInfo> result = linkedMap();

		for (String mainColumn : mainColumns) {
			if (!allColumns.containsKey(mainColumn)) {
				if (mainColumn.equals(WrapperAccessor.SELF)) {
					/* The '_self' column was used as a special column in old TL versions. It was
					 * removed with #26872. But it can still appear in old, migrated systems. There
					 * is no special code for such old systems. They need to declare this column and
					 * the column order explicitly, if they want to keep it. */
					continue;
				}
				/* This cannot be checked at application startup, as for example the '_self' column
				 * is no type part. Other such legal columns without corresponding type part might
				 * exists. */
				String message = "The main column '" + mainColumn + "' does not exist. Columns: " + allColumns.keySet();
				logError(message, new RuntimeException(message));
				continue;
			}
			result.put(mainColumn, allColumns.get(mainColumn));
		}
		result.putAll(allColumns);

		return result;
	}

	/**
	 * Create the {@link ColumnInfo} for the given {@link TLStructuredTypePart}.
	 * 
	 * @param typePart
	 *        Is not allowed to be null.
	 * @return Never null.
	 * @throws UnsupportedOperationException
	 *         If it cannot create a column for the given type.
	 */
	@Override
	public ColumnInfo createColumnInfo(TLStructuredTypePart typePart) {
		TLTypeContext contentType = new TypePartContext(typePart);

		ColumnInfo info = createColumnInfo(contentType, headerI18N(typePart));
		if (TLModelUtil.isAccessAware(typePart)) {
			info.ensureSecurity();
		}
		assignPreloadOperations(info, typePart);
		return info;
	}

	/**
	 * The {@link ResKey} for the column header.
	 */
	protected ResKey headerI18N(TLStructuredTypePart typePart) {
		return CompatibilityService.getInstance().i18nKey(typePart);
	}

	private void assignPreloadOperations(ColumnInfo info, TLStructuredTypePart typePart) {
		PreloadContribution contribution = CompatibilityService.getInstance().preloadContribution(typePart);
		if (contribution != EmptyPreloadContribution.INSTANCE) {
			info.addPreloadContribution(contribution);
		}
	}

	static void logError(String message, RuntimeException exception) {
		Logger.error(message, exception, TableConfigModelInfoImpl.class);
	}

	@Override
	public Set<TLClass> getContentTypes() {
		if (_contentTypes == null) {
			throw new IllegalStateException("The \"content types set\" is not initialized, yet.");
		}
		return _contentTypes;
	}

	@Override
	public Set<TLClass> getCommonSuperClasses() {
		if (_commonSuperClasses == null) {
			throw new IllegalStateException("The \"common super classes set\" is not initialized, yet.");
		}
		return _commonSuperClasses;
	}

	@Override
	public Set<String> getMainColumns() {
		if (_mainColumns == null) {
			throw new IllegalStateException("The \"main columns set\" is not initialized, yet.");
		}
		return _mainColumns;
	}

	@Override
	public Map<String, ColumnInfo> getColumnInfos() {
		if (_columnInfos == null) {
			throw new IllegalStateException("The \"ColumnInfo map\" is not initialized, yet.");
		}
		return _columnInfos;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("content-types", qualifiedNames(getContentTypes()))
			.build();
	}

}
