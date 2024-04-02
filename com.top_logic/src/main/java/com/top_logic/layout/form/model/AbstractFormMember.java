/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.Listeners;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.resources.NestedResourceView;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.util.Utils;

/**
 * Abstract base class for all {@link FormMember}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormMember extends PropertyObservableBase implements FormMember, LazyTypedAnnotatableMixin {

	/**
	 * Property to store the UI identifier.
	 * 
	 * @see #uiIdentifier(IdentifierSource, IDUsage)
	 */
	private static final Property<String> UI_IDENTIFIER = TypedAnnotatable.property(String.class, "uiIdentifier");

	private static final Property<IDUsage> UI_IDENTIFIER_CREATION =
		TypedAnnotatable.property(IDUsage.class, "uiIdentifierCreation", IDUsage.OTHER);

	/** @see #displayMode */
	protected static final int INVISIBLE_MODE = 1;

	/** @see #displayMode */
	protected static final int IMMUTABLE_MODE = 2;

	/** @see #displayMode */
	protected static final int DISABLED_MODE = 3;

	/** @see #displayMode */
	protected static final int ACTIVE_MODE = 4;

	/** @see AbstractFormMember#getControlProvider() */
	private static final Property<ControlProvider> CONTROL_PROVIDER_PROPERTY =
		TypedAnnotatable.property(ControlProvider.class, "controlProvider");

	private static final Property<ContextMenuProvider> CONTEXT_MENU =
		TypedAnnotatable.property(ContextMenuProvider.class, "contextMenu", NoContextMenuProvider.INSTANCE);
	
	/** @see FormMember#getName() */
	@Inspectable
	private final String name;
	
	@Inspectable
	private FormContainer parent;
	
	@Inspectable
	private String label = null;
	
	/**
	 * A tooltip can be set via the I18N resource-key + ".tooltip".
	 * 
	 * E.g. the resource key of the field is "example.booleanField", then the tooltip would be
	 * "example.booleanField.tooltip". If this resource key doesn't exist no tooltip will be generated.
	 */
	@Inspectable
	private String tooltip = null;
	
	@Inspectable
	private String tooltipCaption = null;
	
	/**
	 * The display mode of a {@link FormMember} describes its current effective
	 * visualization.
	 * 
	 * <p>
	 * The display mode is the aggregation of the properties
	 * {@link #isLocallyVisible()}, {@link #isLocallyImmutable()}, and
	 * {@link #isLocallyDisabled()} of this {@link FormMember} and the
	 * properties {@link #isVisible()}, {@link #isImmutable()}, and
	 * {@link #isDisabled()} of its {@link #getParent()}.
	 * </p>
	 * 
	 * <p>
	 * Valid values are {@link #INVISIBLE_MODE}, {@link #IMMUTABLE_MODE},
	 * {@link #DISABLED_MODE}, and {@link #ACTIVE_MODE}.
	 * </p>
	 */
	private int displayMode;

	/**
	 * Decides, whether this field is visible. An invisible field is not shown
	 * on the user interface.
	 */
	@Inspectable
	private boolean visible = true;

	/**
	 * Decides, whether this field is displayed in a display-only fashion, e.g.
	 * as label instead of input field.
	 */
	@Inspectable
	private boolean immutable;

	/**
	 * Decides, whether this field does not accept input, even if it is
	 * displayed as input field.
	 */
	@Inspectable
	private boolean disabled = false;
	
	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();

	private ResourceView lazyResources;

    /**
     * @see #getCssClasses()
     */
	@Inspectable
	private String customCssClasses;

	/**
	 * @see #getInheritDeactivation()
	 */
	@Inspectable
	private boolean inheritDeactivation = true;

	/**
	 * @see #isTransient()
	 */
	@Inspectable
	private boolean isTransient;

	/**
	 * Listeners object holding listeners that are not {@link PropertyListener}.
	 */
	@Inspectable
	private Listeners _listeners;

	/**
	 * Creates a new form member with the given name.
	 * 
	 * @param name See {@link FormMember#getName()}.
	 */
	public AbstractFormMember(String name) {
		this(name, false);
	}

	public AbstractFormMember(String name, boolean immutable) {
		assert name != null;
		assert name.length() > 0;
		
		// Check, whether the given name string is a legal name.
		for (int cnt = name.length(), n = 0; n < cnt; n++) {
			char testChar = name.charAt(n);
			if (testChar == '.') {
				throw new IllegalArgumentException(
					"Illegal character '" + testChar + "' inside a form member name: " + name);
			}
		}
		
		this.name = name;
		this.immutable = immutable;
		
		this.displayMode = immutable ? IMMUTABLE_MODE : ACTIVE_MODE;
	}

	/**
	 * Final because any other implementation will break the FormContext.
	 * 
	 * @return name of the field in the form.
	 */
	@Override
	public final String getName() {
	    return name;
	}
	
    @Override
	public final String getQualifiedName() {
    	// TODO: Delegate to getRelativeName(null).
    	if (parent == null) {
    		return name;
    	} else {
			StringBuilder buffer = new StringBuilder();
			try {
				appendQualifiedName(buffer);
			} catch (IOException ex) {
				throw new UnreachableAssertion("Appending to a string builder must not fail.", ex);
			}
			return buffer.toString();
    	}
    }

	@Override
	public final void appendQualifiedName(Appendable writer) throws IOException {
		if (parent != null) {
			parent.appendQualifiedName(writer);
			writer.append('.');
		}
		writer.append(name);
	}

	@Override
	public final String uiIdentifier(IdentifierSource scope, IDUsage usage) {
		String existingIdentifier = get(UI_IDENTIFIER);
		if (existingIdentifier != null) {
			if (usage == IDUsage.OTHER) {
				return existingIdentifier;
			}

			IDUsage creation = get(UI_IDENTIFIER_CREATION);
			if (creation == IDUsage.OTHER) {
				// Now consider the identifier to be created for the current usage.
				set(UI_IDENTIFIER_CREATION, usage);
				return existingIdentifier;
			} else if (creation == usage) {
				// Do not re-use the same ID for multiple inputs or labels. Create new
				// identifier.
			} else {
				return existingIdentifier;
			}
		}

		String newIdentifier = scope.createNewID();
		set(UI_IDENTIFIER, newIdentifier);
		set(UI_IDENTIFIER_CREATION, usage);
		return newIdentifier;
	}

    @Override
	public String getRelativeName(FormContainer ancestor) {
    	// Check parent == ancestor first, to handle ancestor == null correctly.
    	if (parent == ancestor) {
    		return name;
    	} else if (parent == null) {
    		throw new IllegalArgumentException("Not a descendant of the given ancestor '" + ancestor.getQualifiedName() + "'");
    	} else {
    		return parent.getRelativeName(ancestor) + '.' + name;
    	}
    }
    

	@Override
	public final boolean getInheritDeactivation() {
		return this.inheritDeactivation;
	}

	/**
	 * @see #getInheritDeactivation()
	 */
	public void setInheritDeactivation(boolean inheritDeactivation) {
		this.inheritDeactivation = inheritDeactivation;
		
		updateDisplayMode();
	}

	@Override
	public FieldMode getMode() {
		switch (displayMode) {
			case ACTIVE_MODE:
				return FieldMode.ACTIVE;
			case DISABLED_MODE:
				return FieldMode.DISABLED;
			case IMMUTABLE_MODE:
				return FieldMode.IMMUTABLE;
			case INVISIBLE_MODE:
				return FieldMode.INVISIBLE;
			default:
				throw new UnreachableAssertion("No such display mode: " + displayMode);
		}
	}

	@Override
	public void setMode(FieldMode mode) {
		switch (mode) {
			case LOCALLY_IMMUTABLE:
				// Note: A group cannot be directly set to "locally immutable". For the group itself
				// this is equivalent with setting all contents to active mode. A plain group has no
				// input elements. Only groups bound to configuration items in declarative forms
				// evaluate this "locally immutable" mode.
			case ACTIVE: {
				setDisabled(false);
				setImmutable(false);
				setBlocked(false);
				setVisible(true);
				break;
			}
			case DISABLED: {
				setDisabled(true);
				setImmutable(false);
				setBlocked(false);
				setVisible(true);
				break;
			}
			case IMMUTABLE: {
				setImmutable(true);
				setBlocked(false);
				setVisible(true);
				setDisabled(false);
				break;
			}
			case INVISIBLE: {
				setVisible(false);
				setBlocked(false);
				setDisabled(false);
				setImmutable(false);
				break;
			}
			case BLOCKED: {
				setBlocked(true);
				setVisible(true);
			}
		}
	}

	/**
	 * Hook for updating the blocked property in sub-classes.
	 */
	protected void setBlocked(boolean value) {
		// Only relevant for fields.
	}

	@Override
	public final boolean isVisible() {
		return displayMode != INVISIBLE_MODE;
	}

	private boolean computeVisible() {
		return this.isLocallyVisible() && (isRoot() || getParent().isVisible());
	}

	@Override
	public boolean isLocallyVisible() {
		return this.visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;

		updateDisplayMode();
	}

	@Override
	public final boolean isImmutable() {
		return displayMode == IMMUTABLE_MODE;
	}

	/* package protected */boolean computeImmutable() {
		boolean locallyImmutable = this.isLocallyImmutable();
		if (getInheritDeactivation()) {
			return locallyImmutable || ((!isRoot()) && getParent().isImmutable());
		}
		return locallyImmutable;
	}

	@Override
	public boolean isLocallyImmutable() {
	    return this.immutable;
	}

	@Override
	public void setImmutable(boolean immutable) {
	    this.immutable = immutable;
	    
		updateDisplayMode();
	}

	@Override
	public final boolean isDisabled() {
		return displayMode == DISABLED_MODE;
	}

	/* package protected */boolean computeDisabled() {
		boolean locallyDisabled = this.isLocallyDisabled();
		if (getInheritDeactivation()) {
			return locallyDisabled || ((!isRoot()) && getParent().isDisabled());
		}
		return locallyDisabled;
	}

	@Override
	public boolean isLocallyDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
	    this.disabled = disabled;
	    
		updateDisplayMode();
	}
	
    @Override
	public final boolean isActive() {
    	return displayMode == ACTIVE_MODE;
    }
    
	/*package protected*/ void updateDisplayMode() {
		int newDisplayMode = ACTIVE_MODE;
		if (! computeVisible()) {
			newDisplayMode = INVISIBLE_MODE;
		} else if (computeImmutable()) {
			newDisplayMode = IMMUTABLE_MODE;
		} else if (computeDisabled()) {
			newDisplayMode = DISABLED_MODE;
		} // else newDisplayMode = ACTIVE_MODE;

		int oldDisplayMode = this.displayMode;
		this.displayMode = newDisplayMode;
		
		// For compatibility, translate back into simple "normalized" property
		// transitions.
		if (newDisplayMode != oldDisplayMode) {
			notifyDisplayModeChanged(oldDisplayMode, newDisplayMode);
		}
	}

	protected void notifyDisplayModeChanged(int oldDisplayMode, int newDisplayMode) {
		fireDisplayModeEvent(oldDisplayMode, false);
		fireDisplayModeEvent(newDisplayMode, true);
	}

	private void fireDisplayModeEvent(int mode, boolean isNewMode) {
		switch (mode) {
		case INVISIBLE_MODE: {
				firePropertyChanged(VISIBLE_PROPERTY, self(), Boolean.valueOf(isNewMode), Boolean.valueOf(!isNewMode));
			break;
		}
		case IMMUTABLE_MODE: {
				firePropertyChanged(IMMUTABLE_PROPERTY, self(), Boolean.valueOf(!isNewMode), Boolean.valueOf(isNewMode));
			break;
		}
		case DISABLED_MODE: {
				firePropertyChanged(DISABLED_PROPERTY, self(), Boolean.valueOf(!isNewMode), Boolean.valueOf(isNewMode));
			break;
		}
		default: {
			assert mode == ACTIVE_MODE;
			break;
		}
		}
	}
	
	@Override
	public String getTooltip() {
		if(this.tooltip != null) {
			return this.tooltip;
		}

		return this.getResources().getStringResource("tooltip", null);
	}
    
	@Override
	public void setTooltip(String newTooltip) {
	    String oldTooltip = this.tooltip;
		if (! StringServices.equals(oldTooltip, newTooltip)) {
            this.tooltip = newTooltip;
			firePropertyChanged(TOOLTIP_PROPERTY, self(), oldTooltip, newTooltip);
        }
	}
	
	@Override
	public String getTooltipCaption() {
		if(this.tooltipCaption != null) {
			return this.tooltipCaption;
		}
		
		return this.getResources().getStringResource(TOOLTIP_CAPTION_SUFFIX, null);
	}
	
	@Override
	public void setTooltipCaption(String aTooltipCaption) {
		this.tooltipCaption = aTooltipCaption;
	}

	@Override
	public String getLabel() {
		if (this.label != null)
			return this.label;
		
		// The label of a form member is defined by its parent.
		FormContainer myParent = getParent();
		if (myParent != null) {
			return myParent.getResources().getStringResource(this.getName());
		}
		return null;
	}

	@Override
	public String setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;

		if (!StringServices.equals(oldLabel, newLabel)) {
			firePropertyChanged(LABEL_PROPERTY, self(), oldLabel, newLabel);
		}

		return oldLabel;
	}

	@Override
	public boolean hasLabel() {
		if (this.label != null)
			return true;

		// The label of a form member is defined by its parent.
		FormContainer myParent = getParent();
		if (myParent != null) {
			return myParent.getResources().hasStringResource(this.getName());
		}
		return false;
	}

	@Override
	public String getCssClasses() {
		return this.customCssClasses;
	}

	@Override
	public void setCssClasses(String newCssClasses) {
		if (newCssClasses != null && newCssClasses.length() == 0) {
			// Normalize.
			newCssClasses = null;
		}
		
		String oldCssClasses = this.customCssClasses;
		if (Utils.equals(newCssClasses, oldCssClasses)) {
			return;
		}
		
		this.customCssClasses = newCssClasses;
		
		firePropertyChanged(CLASS_PROPERTY, self(), oldCssClasses, newCssClasses);
	}
	
	@Override
	public ResourceView getResources() {
		if (lazyResources == null) {
			assert parent != null : "'parent' of member '"+ this.getName() +"' must not be null"; 
			this.lazyResources = new NestedResourceView(parent.getResources(), name);
		}
		return this.lazyResources;
	}

	public void setResources(ResourceView resources) {
		this.lazyResources = resources;
	}

	@Override
	public FormContext getFormContext() {
	    FormContainer theParent = getParent();
	    if (theParent != null) {
	        return theParent.getFormContext();
	    }
	    return null;
	}

	@Override
	public FormContainer getParent() {
		return this.parent;
	}

	/**
	 * Report, whether this member is the top-level member, or whether it has a
	 * parent member.
	 * 
	 * @return <code>true</code>, if this is the top-level member,
	 *     <code>false</code> otherwise.
	 */
	public final boolean isRoot() {
		return getParent() == null;
	}

	/**
	 * Set the parent group of this form member.
	 * 
	 * This method may only be called by the (new) parent of this form member after
	 * it is added to the parents child set.
	 * 
	 * <strong>Note:</strong> This method is package protected to ensure that both
	 * associations {@link FormMember#getParent()} and
	 * {@link FormContainer#getMembers()} stay in sync. 
	 * 
	 * @param container
	 *     The (new) parent of this member. If the argument is <code>null</code>, 
	 *     the current parent is removed. 
	 */
	/*package protected*/
	void setParent(FormContainer container) {
		FormContainer oldContainer = this.parent;

		if ((container != null) && (oldContainer != null)) 
			throw new IllegalStateException("form member "+this.getQualifiedName()+" is still a member of another group");
		
		if (container == null) {
			firePropertyChanged(REMOVED_FROM_PARENT, self(), oldContainer, container);

			// Remove parent last, to allow observing the event also from the parent container.
			this.parent = container;
		} else {
			// Set parent first, to allow observing the event also from the parent container.
			this.parent = container;

			firePropertyChanged(ADDED_TO_PARENT, self(), oldContainer, container);
		}

		// Since the parent's display mode decides about this member's display
		// mode, the display mode must be adjusted, if the parent relationship
		// changes.
		updateDisplayMode();
	}

	/**
	 * Notifies all listeners of property events about a change in the given property.
	 * 
	 * @param sender
	 *        This {@link FormMember}.
	 * @param propertyType
	 *        The type of the property that has changed.
	 * @param oldValue
	 *        The old value of the property.
	 * @param newValue
	 *        The new value of the property.
	 */
	protected final <T extends PropertyListener, S, V> void firePropertyChanged(
			EventType<T, ? super S, V> propertyType, S sender, V oldValue, V newValue) {
		
		firePropertyChangedBubleUp(propertyType, sender, oldValue, newValue);
	}

	/**
	 * Internal method to notify all listeners about a change in the given property and lets the
	 * event bubble over the parent hierarchy.
	 */
	protected <T extends PropertyListener, S, V> void firePropertyChangedBubleUp(
			EventType<T, ? super S, V> propertyType, S sender, V oldValue, V newValue) {
		Bubble bubble = notifyListeners(propertyType, sender, oldValue, newValue);
		if (!propertyType.canBubble() || bubble == Bubble.CANCEL_BUBBLE)
			return;

		FormContainer theParent = getParent();
		if (theParent != null) {
			((AbstractFormMember) theParent).firePropertyChangedBubleUp(propertyType, sender, oldValue, newValue);
		}
	}

	/**
	 * Create a string representation if this object that is intended for
	 * debugging.
	 * 
	 * @return The string representation of this instance.
	 */
    @Override
	public final String toString() {
		String nameAndType = getQualifiedName() + " : " + getClass().getSimpleName();
		if (hasLabel()) {
			return getLabel() + " (" + nameAndType + ")";
		} else {
			return nameAndType;
		}
    }

	@Override
	public InlineMap<Property<?>, Object> internalAccessLazyPropertiesStore() {
		return _properties;
	}

	@Override
	public void internalUpdateLazyPropertiesStore(InlineMap<Property<?>, Object> newProperties) {
		_properties = newProperties;
	}

	/**
	 * Sets the transient state of this {@link FormMember} to <code>isTransient</code>.
	 */
	public void setTransient(boolean isTransient) {
		if (this.isTransient != isTransient) {
			boolean changedBefore = isChanged();
			this.isTransient = isTransient;
			firePropertyChanged(FormMember.TRANSIENT_STATE_CHANGED, self(), Boolean.valueOf(!isTransient),
				Boolean.valueOf(isTransient));
			if (changedBefore != isChanged()) {
				firePropertyChanged(FormMember.IS_CHANGED_PROPERTY, self(), Boolean.valueOf(changedBefore),
					Boolean.valueOf(!changedBefore));
			}
		}
	}

	/**
	 * <code>false</code> by default if {@link #setTransient(boolean)} was not called.
	 * 
	 * @see FormMember#isTransient()
	 */
	@Override
	public boolean isTransient() {
		return isTransient;
	}

	@Override
	public boolean hasStableIdSpecialCaseMarker() {
		return getStableIdSpecialCaseMarker() != null;
	}

	@Override
	public Object getStableIdSpecialCaseMarker() {
		return get(STABLE_ID_SPECIAL_CASE_MARKER_PROPERTY);
	}

	@Override
	public void setStableIdSpecialCaseMarker(Object stableIdSpecialCaseMarker) {
		set(STABLE_ID_SPECIAL_CASE_MARKER_PROPERTY, stableIdSpecialCaseMarker);
	}

	@Override
	public Object getStableIdSpecialCaseContext() {
		return get(STABLE_ID_SPECIAL_CASE_CONTEXT_PROPERTY);
	}

	@Override
	public void setStableIdSpecialCaseContext(Object context) {
		set(STABLE_ID_SPECIAL_CASE_CONTEXT_PROPERTY, context);
	}

	/**
	 * Setter for {@link #getStableIdSpecialCaseMarker()} and
	 * {@link #getStableIdSpecialCaseContext()}.
	 */
	public void setStableIdSpecialCaseMarker(Object stableIdSpecialCaseMarker, Object context) {
		setStableIdSpecialCaseMarker(stableIdSpecialCaseMarker);
		setStableIdSpecialCaseContext(context);
	}

	@Override
	public ControlProvider getControlProvider() {
		return get(CONTROL_PROVIDER_PROPERTY);
	}

	@Override
	public void setControlProvider(ControlProvider value) {
		set(CONTROL_PROVIDER_PROPERTY, value);
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public void makeVisible() {
		firePropertyChanged(VISIBILITY_REQUEST, self(), Boolean.FALSE, Boolean.TRUE);
	}

	@Override
	public ContextMenuProvider getContextMenu() {
		return get(CONTEXT_MENU);
	}

	@Override
	public void setContextMenu(ContextMenuProvider value) {
		set(CONTEXT_MENU, value);
	}

	/**
	 * @see Listeners#has(Class, Object)
	 */
	protected <T> boolean removeListener(Class<T> listenerInterface, T listener) {
		if (_listeners == null) {
			return false;
		}
		return _listeners.remove(listenerInterface, listener);
	}

	/**
	 * @see Listeners#has(Class)
	 */
	protected boolean hasListeners(Class<?> listenerInterface) {
		if (_listeners == null) {
			return false;
		}
		return _listeners.has(listenerInterface);
	}

	/**
	 * @see Listeners#get(Class)
	 */
	protected <T> List<T> getListeners(Class<T> listenerInterface) {
		if (_listeners == null) {
			return Collections.emptyList();
		}
		return _listeners.get(listenerInterface);
	}

	/**
	 * @see Listeners#getDirect(Class)
	 */
	protected <T> Collection<T> getListenersDirect(Class<T> listenerInterface) {
		if (_listeners == null) {
			return Collections.emptyList();
		}
		return _listeners.getDirect(listenerInterface);
	}

	/**
	 * @see Listeners#add(Class, Object)
	 */
	protected <T> boolean addListener(Class<T> listenerInterface, T listener) {
		if (_listeners == null) {
			_listeners = new Listeners();
		}
		return _listeners.add(listenerInterface, listener);
	}
	
	/**
	 * The sender to use in events.
	 */
	protected FormMember self() {
		return this;
	}

}
