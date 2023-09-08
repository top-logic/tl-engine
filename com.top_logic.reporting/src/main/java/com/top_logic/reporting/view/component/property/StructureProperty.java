/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.core.util.FilteredVisitor;
import com.top_logic.element.layout.structured.StructuredElementTreeModel;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.layout.CachedLabelProvider;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * {@link WrapperListProperty} based on a tree of {@link StructuredElement}s.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class StructureProperty extends WrapperListProperty {

	private StructuredElementWrapper root;

	/**
	 * Creates a new {@link StructureProperty}.
	 */
	public StructureProperty(String name, List<? extends StructuredElementWrapper> initialValue,
			BoundComponent aComponent, StructuredElementWrapper aRoot) {
		super(name, initialValue, true, aComponent);
		root = aRoot;
	}
	
	@Override
	protected AbstractFormField createNewFormMember() {
		return newStructureField(getName(), false, null);
	}
	
	@Override
	protected List<? extends StructuredElementWrapper> getAllElements() {
    	final StructuredElement theRoot = this.getRoot();
		FilteredVisitor filteredVisitor = new FilteredVisitor(AllowViewFilter.INSTANCE);
    	TraversalFactory.traverse(theRoot, filteredVisitor, TraversalFactory.DEPTH_FIRST);
    	return CollectionUtil.dynamicCastView(StructuredElementWrapper.class, filteredVisitor.getResult());
	}
	
	protected StructuredElementWrapper getRoot() {
		return root;
	}

	private SelectField newStructureField(String name, boolean isDisabled, Collection<StructuredElement> someElts) {
    	boolean    isMandatory = false;
    	boolean    isMultiple  = true;
    	
    	final StructuredElement theRoot = getRoot();
    	final List<? extends StructuredElementWrapper> allowedWrappers = getAllElements();
    	List<StructuredElementWrapper> navigationWrappers = new ArrayList<>();
    	for (StructuredElementWrapper theWrapper : allowedWrappers) {
			StructuredElementWrapper theParent = (StructuredElementWrapper) theWrapper.getParent();
			while(theParent != null) {
				if (!allowedWrappers.contains(theParent) && !navigationWrappers.contains(theParent)) {
					navigationWrappers.add(theParent);
				}
				theParent = (StructuredElementWrapper) theParent.getParent();
			}
		}
    	final List<StructuredElementWrapper> possibleWrappers = new ArrayList<>(allowedWrappers);
    	possibleWrappers.addAll(navigationWrappers);
    	
    	SelectField selectField = FormFactory.newSelectField(name, new StructuredElementTreeModel(theRoot, new SetFilter(possibleWrappers)),
    		isMultiple, (List)getInitialValue(), isMandatory, isDisabled, null);
    	selectField.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
    	selectField.setFixedOptions(navigationWrappers);
    	
        if (someElts != null) {
        	selectField.initializeField(CollectionUtil.toList(someElts));
        }
    	
		selectField.setOptionLabelProvider(CachedLabelProvider.newInstance(MetaLabelProvider.INSTANCE));
        
        return selectField;
    }
}