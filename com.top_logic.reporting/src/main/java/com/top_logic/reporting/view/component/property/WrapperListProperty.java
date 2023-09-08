/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.WrapperIDFormatter;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * {@link FilterProperty} for wrapper lists.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public abstract class WrapperListProperty extends FilterProperty {

	private boolean multiSelect;
	private Comparator<Object> comparator;

	/**
	 * Creates a new {@link WrapperListProperty} using the {@link LabelComparator} as option comparator.
	 */
	public WrapperListProperty(String name, List<? extends TLObject> initialValue, boolean multiSelect,
			BoundComponent aComponent) {
		this(name, initialValue, multiSelect, aComponent, LabelComparator.newCachingInstance());
	}
	
	/**
	 * Creates a new {@link WrapperListProperty}.
	 */
	public WrapperListProperty(String name, List<? extends TLObject> initialValue, boolean multiSelect,
			BoundComponent aComponent, Comparator<Object> comparator) {
		super(name, initialValue, aComponent);
		this.multiSelect = multiSelect;
		this.comparator = comparator;
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		List<? extends TLObject> options = getAllElements();
		Object initialValue = getInitialValue();
		SelectField field = FormFactory.newSelectField(getName(), options, multiSelect, checkInitialValue(initialValue, options), false);
		field.setOptionComparator(comparator);
		return field;
	}

	protected List<Object> checkInitialValue(Object initialValue, List<? extends TLObject> options) {
		if (initialValue == null) return null; 
		List<Object> initialList = new ArrayList<>((Collection<?>) initialValue);
		for (Iterator<Object> it = initialList.iterator(); it.hasNext();) { 
			if (!options.contains(it.next())) { 
				it.remove(); 
			} 
		} 
		return initialList; 
	}

	protected abstract List<? extends TLObject> getAllElements();

	@Override
	protected Object getValueForPersonalConfiguration(Object fieldValue) {
		return getIDsFromWrappers((List)fieldValue);
	}

	@Override
	protected Object getValueFromPersonalConfiguration(Object confValue) {
		try {
			String theIDs = (String) confValue;
			if (!StringServices.isEmpty(theIDs)) {
				return getWrappersFromIDs(theIDs);
			}
		} catch (ParseException ex) {
			Logger.error("Failed to read selected orders from personal configuration", ex, this);
		}

		return null;
	}

	protected String getIDsFromWrappers(List<? extends TLObject> someWrappers) {
		return CollectionUtil.isEmptyOrNull(someWrappers) ? null : StringServices.toString(someWrappers, ",",
			WrapperIDFormatter.INSTANCE);
	}

	protected List<? extends TLObject> getWrappersFromIDs(String theIDs) throws ParseException {
		String[] theArray = StringServices.toArray(theIDs, ',');
		List<TLObject> theResult = new ArrayList<>(theArray.length);

		for (int thePos = 0; thePos < theArray.length; thePos++) {
			TLObject theWrapper = (TLObject) WrapperIDFormatter.INSTANCE.parseObject(theArray[thePos]);

			if (theWrapper != null) {
				if ((theWrapper instanceof BoundObject && !(theWrapper instanceof FastListElement))
					&& !BoundHelper.getInstance().allowView((BoundObject) theWrapper,
						BoundHelper.getInstance().getRootChecker())) {
					continue;
				}

				theResult.add(theWrapper);
			}
		}
		return theResult;
	}

}
