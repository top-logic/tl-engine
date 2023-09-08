/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.listener.EventType;
import com.top_logic.layout.basic.component.AJAXComponent;

/**
 * A Component that may have subcomponents.
 *
 * @author <a href="mailto:kha@top-logic.com"kha>Klaus Halfmann</a>
 * @author nvh
 */
public abstract class LayoutContainer extends AJAXComponent {

	/**
	 * Configuration of a {@link LayoutContainer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface Config extends AJAXComponent.Config {

		/**
		 * Configuration of all possible children in this {@link LayoutContainer}.
		 * 
		 * @see LayoutContainer#getChildList()
		 */
		@Abstract
		List<? extends LayoutComponent.Config> getChildConfigurations();

	}

	/**
	 * {@link EventType} to be informed about a change of the value {@link #getChildList()}.
	 * 
	 * @see #addListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 * @see #removeListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 */
	public static final EventType<ChildrenChangedListener, LayoutContainer, List<LayoutComponent>> CHILDREN_PROPERTY =
		new EventType<>("childrenChange") {
			@Override
			public Bubble dispatch(ChildrenChangedListener listener, LayoutContainer sender,
					List<LayoutComponent> oldValue, List<LayoutComponent> newValue) {
				return listener.notifyChildrenChanged(sender, oldValue, newValue);
			}
		};

	/**
	 * Creates a {@link LayoutContainer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param atts
	 *        The configuration.
	 */
	@CalledByReflection
    public LayoutContainer(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }
    
    /**
	 * Recursively goes through this layouts subcomponents and checks if the
	 * given LayoutComponent is identical (not just equal) to one of them.
	 * 
	 * TODO: x.containsComponent(l, true) should be equivalent to (l.isVisible() && x.containsComponent(l, false))
	 * 
	 * @param component
	 *        the Component to find.
	 * @param visibleOnly
	 *        only visible components will be considered when set.
	 * @return boolean <code>true</code> if the given component was found.
	 */
    public boolean containsComponent(LayoutComponent component, boolean visibleOnly) {
		for (LayoutComponent child : getChildList()) {
			// TODO: Since a component can only be found once, within the
			// component *tree*, the following seems to be nonsense (BHU):
            if (child == component && (!visibleOnly || child.isVisible())) {
                return true;
            } else {
                if ((child instanceof LayoutContainer)
                    && ((LayoutContainer) child).containsComponent(component, visibleOnly)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Look at the Layout's components and return a visible component.
     * 
     * The excat implemenation may vary. It is usually called when some
     * component became invisble and looks for some other component to
     * be shown.
     * 
     * @param skipLc if not <code>null</code>, the first visible component is
     * returned "after" skipLc.
     * @return  null when no such component can be found.
     */
	public final LayoutComponent getFirstVisibleChild(LayoutComponent skipLc) {
		boolean foundSkipLc = skipLc == null;

		for (LayoutComponent child : getVisibleChildren()) {
			if (foundSkipLc && child.isVisible()) {
				return child;
			}
			if (!foundSkipLc && (child == skipLc)) {
				foundSkipLc = true;
			}
		}
		return null;
	}
    
	/**
	 * Report all visible children of this {@link LayoutContainer} assuming this container itself is
	 * visible.
	 * 
	 * <p>
	 * This method may also be called when this container itself is invisible, but must report the
	 * children that would become visible, if this container would become visible.
	 * </p>
	 */
	public abstract Iterable<? extends LayoutComponent> getVisibleChildren();

    /** Try to make the given child component visible.
     *      
     * @return true when child component is visible after the call.
     */
    abstract public boolean makeVisible(LayoutComponent child);
    
    /**
	 * override {@link #setChildVisibility(boolean)} to set the visibility of the children.
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutComponent#setVisible(boolean)
	 */
	@Override
	public final void setVisible(boolean visible) {
		boolean changed = visible != isVisible();
		if (! changed) {
			return;
		}

		if (visible) {
			// Make sure, that the parent becomes visible, before the children
			// become visible. Otherwise, children may ignore the event, because
			// they de facto stay invisible after the event, because the parent
			// is still invisible.
			super.setVisible(visible);
			
			setChildVisibility(visible);
		} else {
			// Make sure, that children become invisible, before the parent
			// becomes invisible. Otherwise, children could assume that nothing
			// has changed, because they are de facto invisible at the time the
			// event occurs.
		    setChildVisibility(visible);
		    
		    super.setVisible(visible);
		}
	}
    
    /**
	 * Sets the visibility of the child components. This {@link LayoutContainer} already has the new
	 * visibility.
	 * <p>
	 * If this {@link LayoutContainer} is set to be not visible, all children must also be not
	 * visible.
	 * </p>
	 * 
	 * @param visible
	 *        the new visibility of the this {@link LayoutContainer}.
	 */
	protected abstract void setChildVisibility(boolean visible);

    /**
	 * All children components.
	 * 
	 * <p>
	 * The result must be safe for iterating even if the sub components change while iterating. In
	 * such a case, a copy-on-write strategy must be implemented on the returned buffer.
	 * </p>
	 * 
	 * @return An unmodifiable view of sub components.
	 */
	public abstract List<LayoutComponent> getChildList();

	/**
	 * Sets the new children for this container.
	 * 
	 * @param newChildren
	 *        New value for {@link #getChildList()}.
	 */
	public abstract void setChildren(List<LayoutComponent> newChildren);

	/**
	 * Decides whether this LayoutContainer is a top level frameset for several 
	 * frames or framesets.
	 * 
	 * a top level frameset is a frameset specified in an own file referenced by another frameset.
	 * a component generating a new frameset, but nested in a parent frameset must return <code>false</code>.
	 */
	public abstract boolean isOuterFrameset();

    /**
     * the number of children of this container.
     */
	public abstract int getChildCount();

    /**
     * Gets the first child component matching the given name.
     * Iterates recursively through the this layout's list to find the
     * component.
     * @param skipSet a Set of LayoutComponents that are not returned.
     * @param aName the Name of the component searched for. 
     * @return a LayoutComponent with the given name or <code>null</code>, if
     * none was found.
     */
    @Override
	public LayoutComponent getComponentByName(String aName, Set skipSet) {
        
        LayoutComponent theResult;
        
        theResult = super.getComponentByName(aName, skipSet);
        if(theResult != null) {
        	return theResult;
        }
        
		List<LayoutComponent> childList = getChildList();
		for (int n = 0, cnt = childList.size(); n < cnt; n++) {
			LayoutComponent child = childList.get(n);

			if (CollectionUtil.equals(aName, child.getName()) && !skipSet.contains(child)) {
				return child;
            }
			theResult = child.getComponentByName(aName, skipSet);
            if(theResult != null) {
            	return theResult;
            }
        }
        return null;    // nothing found
    }
    
    @Override
	protected void registerMainLayout(MainLayout newMainLayout) {
		super.registerMainLayout(newMainLayout);

		for (LayoutComponent theComponent : getChildList()) {
			theComponent.registerMainLayout(newMainLayout);
		}
	}

	@Override
	protected void initDialog(LayoutComponent newDialog) {
		super.initDialog(newDialog);

		for (LayoutComponent theComponent : getChildList()) {
			theComponent.initDialog(newDialog);
		}
	}

    /**
     * delegate to childs.
     */
    @Override
	protected void componentsResolved(InstantiationContext context) {
    	super.componentsResolved(context);

		List<LayoutComponent> childList = getChildList();
		for (int n = 0, cnt = childList.size(); n < cnt; n++) {
			LayoutComponent child = childList.get(n);

			child.resolveComponent(context);
    	}
	}

    /** 
     * all childs are also valid when container was rendered...
     */ 
    @Override
	public void markAsValid() {
        super.markAsValid();
		Collection<LayoutComponent> theChilds = this.getChildList();
        if(theChilds != null) {
			for (LayoutComponent lc : theChilds) {
                lc.markAsValid();
            }
        }
    }
    
    @Override
	protected void internalBroadcastModelEvent(Object aModel, Object changedBy, int eventType) {
		super.internalBroadcastModelEvent(aModel, changedBy, eventType);
    
		List<LayoutComponent> childList = getChildList();
		for (int n = 0, cnt = childList.size(); n < cnt; n++) {
			LayoutComponent child = childList.get(n);

			child.internalBroadcastModelEvent(aModel, changedBy, eventType);
        }
    }

    /** reset the forReload flag ususally reset by parent components */
    @Override
	public void resetForReload() {
        super.resetForReload();
		for (LayoutComponent theChild : getChildList()) {
            if (theChild.isVisible()) {
                theChild.resetForReload();
            }
        }
    }

    /**
	 * Overriden to implement type-specific visiting for {@link LayoutContainer}s.
	 * 
	 * @see LayoutComponent#acceptVisitor(LayoutComponentVisitor)
	 */
	@Override
	public boolean acceptVisitor(LayoutComponentVisitor aVisitor) {
		return aVisitor.visitLayoutContainer(this);
	}
    
    /**
	 * Recursively visit all children of this component.
	 * 
	 * @param aVisitor
	 *     The visitor who "must" visit the component.
	 */
    @Override
	public void visitChildrenRecursively(LayoutComponentVisitor aVisitor) {
        super.visitChildrenRecursively(aVisitor);

		List<LayoutComponent> childList = getChildList();
		for (int n = 0, cnt = childList.size(); n < cnt; n++) {
			LayoutComponent child = childList.get(n);

			child.acceptVisitorRecursively(aVisitor);
        }
    }

	/**
	 * Informs all listener about the changed of {@link #getChildList()}.
	 * 
	 * @param oldChildren
	 *        Old value of {@link #getChildList()} before the change.
	 * 
	 * @see #CHILDREN_PROPERTY
	 */
	protected void fireChildrenChanged(List<LayoutComponent> oldChildren) {
		firePropertyChanged(LayoutContainer.CHILDREN_PROPERTY, this, oldChildren, getChildList());
	}

}