/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dynamic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.util.Utils;

/**
 * The DynamicLayoutModel holds a current {@link LayoutComponent} based on a given object.
 * 
 * <p>
 * A list of {@link LayoutResolver}s is provide the proper layout for a given business model. The
 * fist resolver that {@link LayoutResolver#canResolve(Object)} a given business object determines
 * the layout. If no resolver {@link LayoutResolver#canResolve(Object)} a given business object, the
 * {@link #getDefaultLayoutName()} is used. if the business Object is <code>null</code>, the
 * {@link #getNullLayoutName()} is used.
 * </p>
 * 
 * <p>
 * The already resolved layouts are cached and reused if a new business object is resolved to a
 * cached layout.
 * </p>
 * 
 * <p>
 * Due to the fact that the layout gets resolved AFTER the business object is set, the business
 * component might not receive the model set event. So best, the business component gets its model
 * directly from the surrounding {@link DynamicLayoutContainer} via
 * {@link DynamicLayoutUtil#getEnclosingModel(LayoutComponent)}.
 * </p>
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DynamicLayoutModel implements SelectionModel {
    
    /**
	 * LayoutResolver that can provide the layout definitions for a given business model.
	 */
	private LayoutResolver layoutResolver;

    /** The name of the default layout xml file to use if no specific layout is provided */
    private String defaultLayoutName;
    
    /** The name of the null layout xml file to use if the given business model is null */
    private String nullLayoutName;    
    
    private Map<String, LayoutComponent> layouts = new HashMap<>();

	/**
	 * Immutable view to the values of {@link #layouts}.
	 */
	private List<LayoutComponent> _layoutsView = null;

    /**
	 * The currently active layout as determined by the last call to
	 * {@link #adaptModel(Object, DynamicLayoutContainer)}
	 */
    private LayoutComponent currentComponent;
    
    /**
     * The selection listeners
     */
    private Collection selectionListeners = new HashSet();

	private final SelectionModelOwner _owner;

	public DynamicLayoutModel(LayoutResolver layoutResolver, String defaultLayoutName, String nullLayoutName,
			SelectionModelOwner owner) {
        super();
		this.layoutResolver = layoutResolver;
        this.defaultLayoutName = defaultLayoutName;
        this.nullLayoutName = nullLayoutName;
		_owner = owner;
    }

	/**
	 * Layout to use for <code>null</code> objects.
	 */
	public String getNullLayoutName() {
		return nullLayoutName;
	}

	/**
	 * Layout to use for objects that have no matching resolvers.
	 * 
	 * @see LayoutResolver#canResolve(Object)
	 */
	public String getDefaultLayoutName() {
		return defaultLayoutName;
	}
    
	public void init(DynamicLayoutContainer aParent) {
		LayoutComponent theNullComponent = layouts.get(nullLayoutName);
		if (theNullComponent == null) {
			theNullComponent = getComponentFor(aParent, nullLayoutName);
			if (theNullComponent == null) {
				throw new ConfigurationError("Configured null layout could not be instantiated: nullLayout = "
					+ this.nullLayoutName);
			}
		}

		if (!layouts.containsKey(defaultLayoutName)) {
			LayoutComponent theDefaultComponent = getComponentFor(aParent, defaultLayoutName);
			if (theDefaultComponent == null) {
				throw new ConfigurationError("Configured default layout could not be instantiated: defaultLayout = "
					+ this.defaultLayoutName);
			}
		}

		switchToComponent(theNullComponent);
    }
    
    /**
     * @returna list of all components resolved so far
     */
	public List<LayoutComponent> getComponents() {
		if (_layoutsView == null) {
			_layoutsView = Collections.unmodifiableList(new ArrayList<>(layouts.values()));
		}
		return _layoutsView;
    }
    
	public int getSize() {
		return layouts.size();
	}

    /**
     * the currently active component
     */
    public LayoutComponent getCurrentComponent() {
        return this.currentComponent;
    }

	/**
	 * Adapts this model to find the current component for the given model in the given
	 * {@link DynamicLayoutContainer}.
	 * 
	 * @param model
	 *        The model to make component for visible.
	 * @param container
	 *        The container to adapt.
	 * 
	 * @return The new value of {@link #getCurrentComponent()}.
	 */
	public LayoutComponent adaptModel(Object model, DynamicLayoutContainer container) {
		LayoutComponent theComp = this.getComponentFor(container, this.resolveLayoutName(model));
        switchToComponent(theComp);
        return this.currentComponent;
	}

	private void switchToComponent(LayoutComponent newComponent) {
		if (!Utils.equals(this.currentComponent, newComponent)) {
			if (currentComponent != null) {
				currentComponent.setVisible(false);
			}
			if (newComponent != null) {
				newComponent.setVisible(true);
			}
            for (Iterator theIt = this.selectionListeners.iterator(); theIt.hasNext();) {
                SelectionListener theListener = (SelectionListener) theIt.next();
                theListener.notifySelectionChanged(this, 
                        Collections.singleton(this.currentComponent), 
					Collections.singleton(newComponent));
            }
			this.currentComponent = newComponent;
        }
	}

	private String resolveLayoutName(Object aModel) {
		if (aModel == null) {
			return this.nullLayoutName;           
		}
		if (layoutResolver.canResolve(aModel)) {
			return layoutResolver.getLayoutName(aModel);
		}
		return this.defaultLayoutName;
	}

	private LayoutComponent getComponentFor(DynamicLayoutContainer parent, String layoutName) {
		LayoutComponent component = this.layouts.get(layoutName);
        if (component == null) {
			component = this.createComponentFromLayoutXML(parent, layoutName);
			if (component != null) {
				List<LayoutComponent> oldChildren;
				if (_layoutsView == null) {
					oldChildren = Collections.emptyList();
				} else {
					oldChildren = new ArrayList<>(_layoutsView);
				}
				// Invalidate external view.
				_layoutsView = null;
				this.layouts.put(layoutName, component);
				parent.notifyNewChildren(oldChildren);
			}
        }
        return component;
	}

	private LayoutComponent createComponentFromLayoutXML(DynamicLayoutContainer aParent, String layoutName) {
        try {
			InstantiationContext instantiationContext = new DefaultInstantiationContext(DynamicLayoutContainer.class);
			Config config = LayoutStorage.getInstance().getOrCreateLayoutConfig(layoutName);
			LayoutComponent result =
				LayoutUtils.createComponentFromXML(instantiationContext, aParent, layoutName, true, config);
			instantiationContext.checkErrors();
			return result;
        } catch (IOException e) {
            Logger.error("failed to createComponent from XML: " + layoutName, e, this);
		} catch (ConfigurationException e) {
            Logger.error("failed to createComponent from XML: " + layoutName, e, this);
        }
        return null;
    }
    
    @Override
	public boolean addSelectionListener(SelectionListener aListener) {
        return this.selectionListeners.add(aListener);
    }
    
    @Override
	public boolean removeSelectionListener(SelectionListener aListener) {
        return this.selectionListeners.remove(aListener);
    }

    @Override
	public void clear() {
        throw new UnsupportedOperationException("Selection can not be set extenally.");
    }

    @Override
	public Set<?> getSelection() {
        if (this.currentComponent != null) {
            return Collections.singleton(this.currentComponent);
        } else {
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Always false, setting of selection is not supported.
     * 
     * @see com.top_logic.mig.html.SelectionModel#isSelectable(java.lang.Object)
     */
    @Override
	public boolean isSelectable(Object aObj) {
        return false;
    }

	@Override
	public boolean isMultiSelectionSupported() {
		return false;
	}

    @Override
	public boolean isSelected(Object aObj) {
        return this.currentComponent == aObj;
    }

    @Override
	public void setSelected(Object aObj, boolean aSelect) {
        throw new UnsupportedOperationException("Selection can not be set extenally.");
    }
    
	@Override
	public void setSelection(Set<?> newSelection) {
		throw new UnsupportedOperationException("Selection can not be set extenally.");
	}

    public static class DynamicCard implements Card {

        String key;
        LayoutComponent component;
        
        
        
        public DynamicCard(String aKey, LayoutComponent aComponent) {
            super();
            this.key = aKey;
            this.component = aComponent;
        }

        /**
         * @see com.top_logic.mig.html.layout.Card#getCardInfo()
         */
        @Override
		public Object getCardInfo() {
            return this.key;
        }

        /**
         * @see com.top_logic.mig.html.layout.Card#getContent()
         */
        @Override
		public Object getContent() {
            return this.component;
        }

        /**
         * @see com.top_logic.mig.html.layout.Card#getName()
         */
        @Override
		public String getName() {
            return this.key;
        }

        /**
         * @see com.top_logic.mig.html.layout.Card#writeCardInfo(com.top_logic.layout.DisplayContext, Appendable)
         */
        @Override
		public void writeCardInfo(DisplayContext aContext, Appendable aOut)
                throws IOException {
            aOut.append(this.key);
        }
        
    }

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public SelectionModelOwner getOwner() {
		return _owner;
	}

}

