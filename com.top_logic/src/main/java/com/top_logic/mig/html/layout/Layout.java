/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * A Layout is a horizontal or vertical List of LayoutComponents.
 * 
 * The model for this class is the list of its child components.
 * 
 * @author  <a href="mailto:kha@top-logic.com">kha</a>
 */
public class Layout extends LayoutList {

	/**
	 * Configuration options for {@link Layout}.
	 */
	public interface Config extends LayoutList.Config {

		@Name(HORIYONTAL_ATT)
		boolean getHorizontal();

		/** @see #getHorizontal() */
		void setHorizontal(boolean horizontal);

		@Name(RESIZE_MODE_ATT)
		String getResizeMode();

		/**
		 * Flag to mark the layout as resizable, so that the content components can be resized by
		 * dragging an additional displayed bar.
		 */
		@Name(RESIZABLE_ATT)
		@BooleanDefault(false)
		boolean getResizable();

	}

	public static final String HORIYONTAL_ATT = "horizontal";

	public static final String RESIZABLE_ATT = "resizable";

	public static final String RESIZE_MODE_ATT = "resizeMode";

	/** Indicates that Layout is Horizontal. */
    public final static boolean HORIZONTAL = true;

    /** Indicates that Layout is Vertical. */
    public final static boolean VERTICAL = false;

    /** true when Componentes are horizontally aligned. */
    protected boolean horizontal;
    
	private boolean isResizable;
	private LayoutResizeMode resizeMode;

    /** Construct a Layout from (XML-)Attributes. */
	public Layout(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        horizontal   = atts.getHorizontal();
		isResizable = atts.getResizable();
		resizeMode = setupResizeMode(parseResizeMode(context, atts));
	}

	private LayoutResizeMode parseResizeMode(InstantiationContext context, Config atts) throws ConfigurationException {
		String rawValue = StringServices.nonEmpty(atts.getResizeMode());
		return ConfigUtil.getEnumValue(RESIZE_MODE_ATT, rawValue, LayoutResizeMode.class, null);
	}
    
	private LayoutResizeMode setupResizeMode(LayoutResizeMode resizeMode) {
		if (resizeMode == null) {
			return LayoutResizeMode.INSTANT;
		} else {
			return resizeMode;
		}
	}

    /**
     * Creates an ArrayList that will hold LayoutComponents as children.
     * 
     * Overwrite to fit for subclass list types (e.g. TabRowLayout). Since the
     * model variable is not private (as opposed to componentLayoutConstraint),
     * creation of it must be variable as well.
     * 
     * @return List
     */
    protected List createChildList(int size) {
        return new ArrayList(size);
    }

    /**
     * Add a component to the layout.
     * @param lc the LayoutComponent to add.
     */
    public final void addComponent(LayoutComponent lc) {
		addComponent(getChildCount(), lc);
    }

    /**
     * Add a component to the layout. The components contained in this Layout
     * get a new id.
     * Makes this be the parent of the component.
     * 
     * @param    anIndex    The index at which to add the given component.
     * @param    aComp      The LayoutComponent to add.
     * @throws   IllegalArgumentException    If the given component contains 
     *                                       or is contained by this component.
     */
	public void addComponent(int anIndex, LayoutComponent aComp) {
        if ((aComp == this)|| this.containsComponent(aComp, /* visble Only */ false)
            || ((aComp instanceof LayoutContainer) 
             && ((LayoutContainer) aComp).containsComponent(this, /* visble Only */ false))) {
            throw new IllegalArgumentException("Layout " + getName() + " must not recursiveliy nested.");
        }

		addChild(anIndex, aComp);

        MainLayout theMain = this.getMainLayout();

        if (theMain != null) {
			Protocol log = new LogProtocol(Layout.class);
			theMain.setupRelations(log);
			log.checkErrors();
        }
    }

	/**
	 * Removes the given component from the sub-component list.
	 * 
	 * @param oldChild
	 *        The component to remove.
	 * @return Whether the given component was part of this component before.
	 */
	public final boolean removeComponent(LayoutComponent oldChild) {
		int index = getIndexOfChild(oldChild);
		if (index < 0) {
			return false;
		}

		removeChild(index);
		Protocol log = new LogProtocol(Layout.class);
		getMainLayout().setupRelations(log);
		log.checkErrors();
		return true;
	}

	@Override
	protected void onAdd(int index, LayoutComponent newChild) {
		super.onAdd(index, newChild);
		newChild.setVisible(_isVisible());
	}

    /**
     * Gets the index of the given LayoutComponent.
     * @param lc the LayoutComponent to search for.
     * @return int the index of the given LayoutComponent in this list, or -1 if
     * this list does not contain this LayoutComponent.
     */
    protected int getComponentIndex(LayoutComponent lc) {
		return getChildList().indexOf(lc);
    }

    @Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return getChildList();
    }

    /**
     * Returns the horizontal.
     * @return boolean
     */
    public boolean isHorizontal() {
        return horizontal;
    }

	/**
	 * whether this layout can be adjusted by user, or not
	 */
	public boolean isResizable() {
		return isResizable;
	}

	/**
	 * the way a resizable layout will be resized
	 */
	public LayoutResizeMode getResizeMode() {
		return resizeMode;
	}

    /**
     * Resolve a List of Component Names against the components.
     * 
     * @return aList for all names are resolved to theire components.
     *         null when aList was empty or no components could be resolved.
     */
    public List<LayoutComponent> resolveComponents(Collection<ComponentName> aCol) {
        if (aCol == null)
            return null;
        int  size   = aCol.size();
		List<LayoutComponent> result = new ArrayList<>(size);
		for (Iterator<ComponentName> theIt = aCol.iterator(); theIt.hasNext();) {
			ComponentName theName = theIt.next();
            LayoutComponent theComp = this.getComponentByName(theName);
            if (theComp == null) {
                Logger.info("Failed to resolve ' " + theName + "' , ignored" , this);
                continue;
            } 
            result.add(theComp);
        }
        if (result.size() == 0)
            return null;
        return result;
    }

    /**
     * Set scrolling callback in given List of LayoutComponents.
     * 
     * @throws IOException in case one of the aimed component is invisible
     *          (The Reference would be invalid)
     */
    protected void setScrollCallback(List aList) throws IOException {
        if (aList == null)
            return;
        int size = aList.size();
        for (int i=0; i < size; i++) {
            LayoutComponent theComp = (LayoutComponent) aList.get(i);
            String parentRef = LayoutUtils.getFrameReference(theComp.getEnclosingFrameScope(), this.getEnclosingFrameScope());
            theComp.addOnscroll(parentRef + ".syncScroll(window);");
        }
    }
  
    /**
     * We only write the outer frameset when not in another Layout.
     */
    @Override
	public boolean isOuterFrameset() {
        return (!(this.getParent() instanceof Layout));
    }

    /** 
     * Try to make the given child component visible.
     * 
     * Since we dont hide anything, we just try to make ourself visible.
     *      
     * @return true when child component is visible after the call.
     */
    @Override
	public boolean makeVisible(LayoutComponent child) {
        return makeVisible();
    }

    /**
	 * Forward visibility to all children
	 * 
	 * @see com.top_logic.mig.html.layout.LayoutContainer#setChildVisibility(boolean)
	 */
	@Override
	protected void setChildVisibility(boolean aVisible) {
		if (getChildList() == null)
			return;
		int s = getChildList().size();
		for (int i = 0; i < s; i++) {
			getChildList().get(i).setVisible(aVisible);
		}
	}

	public static enum LayoutResizeMode implements ExternallyNamed {
		INSTANT("instant"),
		PREVIEW("preview");

		private String externalName;

		private LayoutResizeMode(String externalName) {
			this.externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return externalName;
		}
	}
}

