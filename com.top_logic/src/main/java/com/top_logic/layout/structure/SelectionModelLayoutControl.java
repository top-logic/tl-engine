/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.dynamic.DynamicLayoutContainer;
import com.top_logic.layout.layoutRenderer.FixedFlowLayoutRenderer;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutControl} for {@link DynamicLayoutContainer}s.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class SelectionModelLayoutControl extends ContainerControl<SelectionModelLayoutControl>
		implements OrientationAware {
    
    /**
     * Indicates the arrangement of the rendered children.
     */
	private Orientation _orientation;
    
    /**
     * The model provides the {@link LayoutComponent}s to be rendered.
     * 
     * The model must provide {@link LayoutComponent}s as objects,
     * the selected objects will be rendered.
     */
    private SelectionModel model;
    
    /**
     * Provides the {@link LayoutControl}s for the rendered {@link LayoutComponent}s
     */
	final LayoutFactory _controlProvider;
    
    /**
     * Mapping {@link LayoutComponent}s to the associated {@link LayoutControl}s
     */
	final Map<LayoutComponent, LayoutControl> _controls = new HashMap<>();

    private SelectionListener selectionChangedListener;

	public SelectionModelLayoutControl(SelectionModel aModel, LayoutFactory aControlProvider,
			Orientation orientation) {
		this(aModel, aControlProvider, orientation, Collections.<String, ControlCommand> emptyMap());
    }

	protected SelectionModelLayoutControl(SelectionModel aModel, LayoutFactory aControlProvider,
			Orientation orientation, Map commandsByName) {
        super(commandsByName);
		assert aControlProvider != null : "control provider required";
		this._controlProvider = aControlProvider;
		init(aModel, orientation);
    }
    
	@Override
	public SelectionModel getModel() {
		return model;
	}

	private void init(SelectionModel aModel, Orientation orientation) {
        assert aModel           != null : "selection model required";
        
		this._orientation = orientation;
        this.model           = aModel;
        
        this.selectionChangedListener = new SelectionListener() {

            /**
             * @see com.top_logic.layout.component.model.SelectionListener#notifySelectionChanged(com.top_logic.mig.html.SelectionModel, Set, Set)
             */
            @Override
			public void notifySelectionChanged(SelectionModel selectionModel, Set<?> aFormerlySelectedObjects,
					Set<?> aSelectedObjects) {
				Collection<?> theOldComps = new HashSet<>(aFormerlySelectedObjects);
				Collection<?> theNewComps = new HashSet<>(aSelectedObjects);
				Collection<?> theOldCopy = new HashSet<>(aFormerlySelectedObjects);
                theOldComps.removeAll(theNewComps);
                theNewComps.removeAll(theOldCopy);
                
				for (Iterator<?> theIt = theOldComps.iterator(); theIt.hasNext();) {
                    Object theObject = theIt.next();
					LayoutControl theControl = _controls.remove(theObject);
                    if (theControl != null) {
                        theControl.detach();
                    }
                }     
                
				dropChildren();
				for (Iterator<?> theIt = theNewComps.iterator(); theIt.hasNext();) {
                    LayoutComponent theObject = (LayoutComponent) theIt.next();
					LayoutControl theControl = _controls.get(theObject);
                    if (theControl == null) {
						theControl = LayoutControlAdapter.wrap(_controlProvider.createLayout(theObject));
						_controls.put(theObject, theControl);
                    }
					internalAddChild(theControl);
                }
            }
            
        };
    }

    @Override
	public Orientation getOrientation() {
		return _orientation;
    }

    /**
	 * @see #getOrientation()
	 */
	public void setOrientation(Orientation aHorizontal) {
		this._orientation = aHorizontal;
    }

    @Override
	protected void internalDetach() {
        super.internalDetach();
        this.model.removeSelectionListener(this.selectionChangedListener);
    }

    @Override
	protected void internalAttach() {
        super.internalAttach();

		Collection<?> theSelection = this.model.getSelection();
        if ( ! CollectionUtil.isEmptyOrNull(theSelection)) {
			dropChildren();
			for (Iterator<?> theIt = theSelection.iterator(); theIt.hasNext();) {
                LayoutComponent theObject = (LayoutComponent) theIt.next();
				LayoutControl theControl = _controls.get(theObject);
                if (theControl == null) {
					theControl = LayoutControlAdapter.wrap(_controlProvider.createLayout(theObject));
					_controls.put(theObject, theControl);
                }
				internalAddChild(theControl);
            }
        }

        this.model.addSelectionListener(this.selectionChangedListener);
    }

	final void dropChildren() {
		super.setChildren(Collections.<LayoutControl> emptyList());
	}

	final void internalAddChild(LayoutControl child) {
		super.addChild(child);
	}

    @Override
	public void setChildren(List<? extends LayoutControl> aChildren) {
        throw new IllegalArgumentException("children are determined via selection model");
    }
    
    @Override
	public void addChild(LayoutControl aLayout) {
        throw new UnsupportedOperationException("children are determined via selection model");
    }
    
	@Override
	protected ControlRenderer<? super SelectionModelLayoutControl> createDefaultRenderer() {
		return FixedFlowLayoutRenderer.INSTANCE;
	}

	@Override
	public SelectionModelLayoutControl self() {
		return this;
	}

}

