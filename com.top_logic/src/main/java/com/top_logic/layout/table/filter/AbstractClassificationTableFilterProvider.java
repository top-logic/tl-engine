/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.list.FastListElementComparator;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.transform.ModelLayout;
import com.top_logic.util.TLCollator;
import com.top_logic.util.Utils;

/**
 * Abstract superclass for filters based on OrderedLists of FastListElements.
 * The standard behaviour is to render a Filter for every entry in the Ordered List.
 * The concrete classes have to define the name of the FastList and the default column, where
 * the Filter provider should be attached.
 * 
 * @author     <a href="mailto:jco@top-logic.com">jco</a>
 */
public abstract class AbstractClassificationTableFilterProvider implements TableFilterProvider {

	private List<TLEnumeration> _classifications;

	/**
	 * Creates a {@link AbstractClassificationTableFilterProvider}.
	 *
	 */
	public AbstractClassificationTableFilterProvider(List<TLEnumeration> classifications) {
		_classifications = classifications;
	}
	
	/**
	 * Utility to lookup the given {@link TLEnumeration}s by name.
	 *
	 * @param names
	 *        The names of {@link TLEnumeration}s.
	 * @return The resolved {@link TLEnumeration}s.
	 */
	protected static List<TLEnumeration> loadClassifications(List<String> names) {
		List<TLEnumeration> result = new ArrayList<>(names.size());
		for (String name : names) {
			result.add(resolveEnumeration(name));
		}
		return result;
	}

	private static TLEnumeration resolveEnumeration(String name) {
		int sepIndex = name.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR);
		if (sepIndex < 0) {
			return (TLEnumeration) TLModelUtil.findType(ModelLayout.TL5_ENUM_MODULE, name);
		} else {
			return (TLEnumeration) TLModelUtil.findType(name.substring(0, sepIndex), name.substring(sepIndex + 1));
		}
	}

	protected abstract boolean isMulti();

	/**
	 * Whether the "no value" option should be hidden.
	 */
	protected boolean isMandatory() {
		return false;
	}

	@Override
	public TableFilter createTableFilter(TableViewModel aTableModel, String filterPosition) {
		return buildClassificationFilters(aTableModel);
	}

	/**
	 * The implementation of the builder method for the classification filter assumes that 
	 * for every entry in the FastList a DPMTableSingleFLElementFilter is build.  
	 * other behavior may be implemented by overwriting this method.
	 */
	protected TableFilter buildClassificationFilters(TableViewModel aTableModel) {
		// Create table filter
		PopupDialogModel dialogModel =
			new DefaultPopupDialogModel(
				new ResourceText(PopupFilterDialogBuilder.STANDARD_TITLE),
				new DefaultLayoutData(
					dim(ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.FILTER_DIALOG_WIDTH), PIXEL), 100,
					ZERO,
					100, Scrolling.NO),
				1);
		FilterDialogBuilder manager = new PopupFilterDialogBuilder(dialogModel);
		TableFilter tableFilter = new TableFilter(manager, true);
		List<TLEnumeration> classificationList = getClassificationList();
		if (classificationList.size() == 1) {
			tableFilter.addSubFilterGroup(getElementFilters(classificationList.get(0)),
				ResKey.NONE);

		} else {
			for (TLEnumeration classification : classificationList) {
				tableFilter.addSubFilterGroup(getElementFilters(classification),
					getFastListI18N(classification));
			}
		}
		if (!isMandatory()) {
			tableFilter.addSubFilter(TableFilterProviderUtil.createNoValueFilter(), true);
		}

		return tableFilter;
	}

	private List<ConfiguredFilter> getElementFilters(TLEnumeration classificationList) {
		List<ConfiguredFilter> configuredFilters = new ArrayList<>();
		{
			List<TLClassifier> elements = getElements(classificationList);
			for (TLClassifier currentClassification : elements) {
				configuredFilters.add(createElementFilter(currentClassification));
			}
		}
		return configuredFilters;
	}

	private List<TLClassifier> getElements(TLEnumeration classificationList) {
		if (classificationList.isOrdered()) {
			return classificationList.getClassifiers();
		} else {
			List<TLClassifier> elements = new ArrayList<>(classificationList.getClassifiers());
			Collections.sort(elements, new FastListElementComparator(new TLCollator()));
			return elements;
		}
	}

	private ResKey getFastListI18N(TLEnumeration classification) {
		return TLModelNamingConvention.enumKey(classification);
	}

	protected List<TLEnumeration> getClassificationList() {
		return _classifications;
	}

	protected ConfiguredFilter createElementFilter(TLClassifier currentClassification) {
		AbstractTableFLElementFilter theFEFilter = null;
		if (isMulti()) {
			theFEFilter = new TableMultiFLElementFilter(currentClassification);
		}
		else {
			theFEFilter = new TableFLElementFilter(currentClassification);
		}
		ResKey theName =
			currentClassification != null ? FastListElementLabelProvider.labelKey(currentClassification) : ResKey
				.legacy("layout.table.filterEmpty");

		return new StaticFilterWrapper(theFEFilter, new ResourceText(theName));
	}

	/**
	 * Compare objects unversioned.
	 */
	public static abstract class HistoricPattern<T extends TLObject> {

		/**
		 * Check if single object matches this pattern.
		 */
		public abstract boolean matches(T object);

		/**
		 * Check if at least one object of the given collection matches this pattern.
		 */
		public abstract boolean matchesAny(Collection<? extends T> objects);

		/**
		 * Return the {@link ObjectReference} for an object.
		 */
		protected ObjectReference id(T object) {
			if (object != null) {
				return WrapperHistoryUtils.getUnversionedIdentity(object);
			}
			return null;
		}

		/**
		 * Return the {@link ObjectReference} for all objects.
		 */
		protected Set<ObjectReference> ids(Collection<? extends T> objects) {
			Set<ObjectReference> ids = CollectionUtil.newSet(objects.size());
			for (T object : objects) {
				if (object != null) {
					ids.add(id(object));
				}
			}
			return ids;
		}
	}

	public static class SingleMatcher extends HistoricPattern<TLClassifier> {
		private final ObjectReference _id;

		/**
		 * Create new {@link SingleMatcher}.
		 */
		public SingleMatcher(TLClassifier pattern) {
			_id = id(pattern);
		}

		@Override
		public boolean matches(TLClassifier object) {
			return Utils.equals(_id, id(object));
		}

		@Override
		public boolean matchesAny(Collection<? extends TLClassifier> objects) {
			if (_id != null) {
				return ids(objects).contains(_id);
			}
			return false;
		}
	}

	public static class MultiMatcher extends HistoricPattern<TLClassifier> {
		private final Set<ObjectReference> _ids;

		/**
		 * Create new {@link MultiMatcher}
		 */
		public MultiMatcher(Collection<? extends TLClassifier> patterns) {
			_ids = ids(patterns);
		}

		@Override
		public boolean matches(TLClassifier needle) {
			return _ids.contains(id(needle));
		}

		@Override
		public boolean matchesAny(Collection<? extends TLClassifier> objects) {
			return CollectionUtil.containsAny(_ids, ids(objects));
		}
	}
}
