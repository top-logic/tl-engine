/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.io.IOException;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.SetBuilder;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.Focusable;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.field.BusinessObjectFieldRef;
import com.top_logic.util.css.CssUtil;


/**
 * A {@link FormMember} represents an entity within a form.
 * 
 * A {@link FormMember} is either a {@link FormField}, which represents a field
 * with a value, or a {@link FormContainer}, which groups multiple form members
 * together.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormMember extends FormContextProxy, Focusable, VisibilityModel,
		TypedAnnotatable, NamedModel {
	
	/**
	 * Type of the event that is fired when this {@link FormMember} will be removed from its
	 * {@link #getParent() parent}, i.e. during completing this event the parent is the old one.
	 * After the event is finished, the parent is <code>null</code>.
	 * 
	 * @see RemovedListener
	 */
	EventType<RemovedListener, FormMember, FormContainer> REMOVED_FROM_PARENT =
		new EventType<>("removedFromParent") {

			@Override
			public Bubble dispatch(RemovedListener listener, FormMember sender, FormContainer oldValue,
					FormContainer newValue) {
				return listener.handleRemovedFromParent(sender, oldValue);
			}

		};
    
	/**
	 * Type of the event that is fired when this {@link FormMember} is added to its
	 * {@link #getParent() parent}.
	 * 
	 * @see AddedListener
	 */
	EventType<AddedListener, FormMember, FormContainer> ADDED_TO_PARENT =
		new EventType<>("addedToParent") {

			@Override
			public Bubble dispatch(AddedListener listener, FormMember sender, FormContainer oldValue,
					FormContainer newValue) {
				return listener.handleAddedToParent(sender, oldValue);
			}

		};

	/**
	 * If a {@link FormMember} needs special handling to create a stable id to it (or it's children,
	 * in case it has children), this property can contains further information.
	 */
	static final Property<Object> STABLE_ID_SPECIAL_CASE_MARKER_PROPERTY =
		TypedAnnotatable.property(Object.class, "stableIdSpecialCaseMarker");

	/** Context of a {@link FormMember} that needs a stable ID in special cases */
	static final Property<Object> STABLE_ID_SPECIAL_CASE_CONTEXT_PROPERTY =
		TypedAnnotatable.property(Object.class, "context");

	/**
	 * Type of the <code>immutable</code> property.
	 * 
	 * The value transmitted to the client in response to a change of this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#BOOLEAN_ELEMENT}.
	 * 
	 * @see #isImmutable()
	 * @see ImmutablePropertyListener
	 */
	EventType<ImmutablePropertyListener, FormMember, Boolean> IMMUTABLE_PROPERTY =
		new EventType<>("immutable") {

			@Override
			public Bubble dispatch(ImmutablePropertyListener listener, FormMember sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleImmutableChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Type of the <code>disabled</code> property.
	 * 
	 * The value transmitted to the client in response to a change of this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#BOOLEAN_ELEMENT}.
	 * 
	 * @see #isDisabled()
	 * @see DisabledPropertyListener
	 */
	EventType<DisabledPropertyListener, FormMember, Boolean> DISABLED_PROPERTY =
		new EventType<>("disabled") {

			@Override
			public Bubble dispatch(DisabledPropertyListener listener, FormMember sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleDisabledChanged(sender, oldValue, newValue);
			}
		};
	
	/**
	 * Type of the <code>class</code> property.
	 * 
	 * @see #getCssClasses()
	 * @see CSSClassListener
	 */
	EventType<CSSClassListener, Object, String> CLASS_PROPERTY = new EventType<>(
		"class") {

		@Override
		public Bubble dispatch(CSSClassListener listener, Object sender, String oldValue, String newValue) {
			return listener.handleCSSClassChange(sender, oldValue, newValue);
		}

	};

	/**
	 * Type of the <code>label</code> property.
	 * 
	 * @see #getLabel()
	 * @see LabelChangedListener
	 */
	EventType<LabelChangedListener, Object, String> LABEL_PROPERTY =
		new EventType<>("label") {

			@Override
			public Bubble dispatch(LabelChangedListener listener, Object sender, String oldValue, String newValue) {
				return listener.handleLabelChanged(sender, oldValue, newValue);
			}
		};
	
	/**
	 * Type of the <code>tooltip</code> property.
	 * 
	 * @see TooltipChangedListener
	 */
	EventType<TooltipChangedListener, Object, String> TOOLTIP_PROPERTY =
		new EventType<>("tooltip") {

			@Override
			public Bubble dispatch(TooltipChangedListener listener, Object sender, String oldValue, String newValue) {
				return listener.handleTooltipChanged(sender, oldValue, newValue);
			}
		};
	
	/**
	 * Type of the <code>isChanged</code> property.
	 * 
	 * @see ChangeStateListener
	 * @see FormMember#isChanged()
	 */
	EventType<ChangeStateListener, FormMember, Boolean> IS_CHANGED_PROPERTY =
		new EventType<>("isChanged") {

			@Override
			public Bubble dispatch(ChangeStateListener listener, FormMember sender, Boolean oldValue, Boolean newValue) {
				return listener.handleChangeStateChanged(sender, oldValue, newValue);
			}

		};
	
	/**
	 * Type of the <code>isTransient</code> property.
	 * 
	 * @see TransientStateListener
	 */
	EventType<TransientStateListener, FormMember, Boolean> TRANSIENT_STATE_CHANGED =
		new EventType<>("transientStateChanged") {

			@Override
			public Bubble dispatch(TransientStateListener listener, FormMember sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleTransientStateChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Property holding the business model, from which this {@link FormMember} was created.
	 */
	public static final Property<Object> BUSINESS_MODEL_PROPERTY = TypedAnnotatable.property(Object.class, "baseModel");
    
	/**
	 * {@link EventType} requesting all containers of this {@link FormMember} to expand.
	 * 
	 * @see VisibilityRequestListener
	 */
	EventType<VisibilityRequestListener, FormMember, Boolean> VISIBILITY_REQUEST =
		new EventType<>("visibilityRequested") {

			@Override
			public Bubble dispatch(VisibilityRequestListener listener, FormMember sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleVisibilityRequested(sender);
			}

		};

	/** {@link EventType}s that are supported by all {@link FormMember}. */
	Set<EventType<?, ?, ?>> FORM_MEMBER_EVENT_TYPES = new SetBuilder<EventType<?, ?, ?>>()
		.add(VISIBILITY_REQUEST)
		.add(TRANSIENT_STATE_CHANGED)
		.add(IS_CHANGED_PROPERTY)
		.add(TOOLTIP_PROPERTY)
		.add(LABEL_PROPERTY)
		.add(CLASS_PROPERTY)
		.add(DISABLED_PROPERTY)
		.add(IMMUTABLE_PROPERTY)
		.add(ADDED_TO_PARENT)
		.add(REMOVED_FROM_PARENT)
		.add(Focusable.FOCUS_PROPERTY)
		.add(VisibilityModel.VISIBLE_PROPERTY)
		.toSet();

	/**
	 * Mapping of {@link FormMember} to their {@link #getQualifiedName()}.
	 */
	Mapping<FormMember, String> QUALIFIED_NAME_MAPPING = new Mapping<>() {
		@Override
		public String map(FormMember input) {
			return input.getQualifiedName();
		}
	};

	/**
	 * Suffix of the {@link FormMember}'s resource key that builds the resource key for the tool-tip
	 * caption to be displayed.
	 */
	String TOOLTIP_CAPTION_SUFFIX = "tooltipCaption";

	/**
	 * The name identifies a form member in its context.
	 * 
	 * The name must be unique within that context. The context of a form member
	 * might be either the top-level {@link FormContext}, or a
	 * {@link FormContainer} within the {@link FormContext}.
	 * 
	 * The name property must be immutable during the lifetime of this form
	 * member. This guarantee is necessary for the invariants of
	 * {@link FormContext} and {@link FormContainer}.
	 * 
	 * @return the name of this form member.
	 */
    public String getName();

    /**
	 * The qualified name of a form member identifies this member within the
	 * top-level {@link FormContext}. The qualified name consists of the
	 * {@link #getName() name} of this form member prefixed with all names of
	 * ancestor form groups (see {@link #getParent()}.
	 */
    public String getQualifiedName();

    /**
	 * Optimized implementation of appending {@link #getQualifiedName()} to the given writer.
	 * 
	 * @param writer
	 *        the {@link Appendable} to append to.
	 * @throws IOException
	 *         If appending fails.
	 */
	public void appendQualifiedName(Appendable writer) throws IOException;

	/**
	 * The identifier to be used for the input field in the UI.
	 * 
	 * @param scope
	 *        Scope used to create a new ID, when no such ID is required before. May be
	 *        <code>null</code>, when the ID was requested before.
	 * @param usage
	 *        How the requested identifier will be used. Requesting an identifier twice for the same
	 *        usage creates a new identifier.
	 * 
	 * @return The identifier to use for the input element.
	 */
	String uiIdentifier(IdentifierSource scope, IDUsage usage);

	/**
	 * Compute the relative name of this {@link FormMember} relative to the given
	 * {@link FormContainer} ancestor.
	 * 
	 * @param ancestor
	 *        An ancestor of this {@link FormMember} or <code>null</code>.
	 * 
	 * @return If the given ancestor is not <code>null</code>, the name that can be passed to
	 *         {@link FormGroup#getMemberByRelativeName(FormMember, String)} for finding this
	 *         {@link FormMember} relative to the given ancestor. If the given ancestor is
	 *         <code>null</code>, the same as {@link #getQualifiedName()}.
	 * 
	 * @throws IllegalArgumentException,
	 *         if the given {@link FormContainer} is not an ancestor of this {@link FormMember}.
	 * 
	 * @see FormGroup#getMemberByRelativeName(FormMember, String) for the inverse operation.
	 */
	public String getRelativeName(FormContainer ancestor);
    
    //
    // Do not even think about adding a method setName(String) to this
    // interface. The name property of form members must be immutable to
    // keep intact the invariants of classes relying on this guarantee (e.g.
	// ConstraintGroup, FormContext).
    //
    // @see #getName()
    //
    // public void setName(String aName);
    //

    /**
	 * The label of this form member. The label is a textual description of this form member that is
	 * intended to be presented on the user interface.
	 * 
	 * <p>
	 * If no label is set explicitly, the label is looked up from the {@link #getResources()
	 * resources} of the {@link #getParent() parent} container using the {@link #getName() name} of
	 * this member as key.
	 * </p>
	 * 
	 * @see FormMember#hasLabel()
	 */
    public String getLabel();
    
    /**
	 * Explicitly sets a label for this member.
	 * 
	 * By default, the label of a member is looked up from the
	 * {@link #getResources() resouces} of the {@link #getParent() parent}
	 * container using the {@link #getName() name} of this member as key. By
	 * explicitly setting a key on this member, the default mechanism is
	 * overridden. The default may be re-established by invoking this method
	 * with the <code>null</code> argument.
	 * 
	 * @return the old label
	 * 
	 * @see #getLabel()
	 */
	public String setLabel(String label);
	
	/**
	 * Whether this {@link FormMember} has an explicitly set label or the default mechanism leads to
	 * an internationalised text.
	 * 
	 * @see FormMember#getLabel()
	 */
	boolean hasLabel();

	/**
	 * HTML fragment to render inside a tooltip on the label of this field.
	 */
	String getTooltip();

	/**
	 * @see #getTooltip()
	 */
	void setTooltip(String newTooltip);

	/**
	 * HTML fragment to render inside the tooltip caption, see {@link #getTooltip()}.
	 */
	String getTooltipCaption();

	/***
	 * @see #getTooltipCaption()
	 */
	void setTooltipCaption(String aTooltipCaption);

	/**
	 * The current custom CSS class names of this {@link FormMember}.
	 * 
	 * @return the (space-separated) CSS classes associated with this member, or
	 *         <code>null</code>, if there are no custom CSS classes for this
	 *         member.
	 * 
	 * @see #CLASS_PROPERTY for the property event being fired, if this
	 *      property changes.
	 */
	public String getCssClasses();

	/**
	 * Updates the {@link #getCssClasses()} of this {@link FormMember}. 
	 */
	public void setCssClasses(String newCssClasses);

	/**
	 * Adds a single CSS class to the {@link #getCssClasses()} of this {@link FormMember}.
	 *
	 * @param newClasses
	 *        The new CSS class to add (must not contain spaces).
	 * @return Whether the given CSS class was newly added (was not already set).
	 */
	default boolean addCssClass(String newClasses) {
		if (StringServices.isEmpty(newClasses)) {
			return false;
		}

		String before = getCssClasses();
		String after = CssUtil.joinCssClassesUnique(before, newClasses);
		if (StringServices.equals(before, after)) {
			return false;
		}

		setCssClasses(after);
		return true;
	}

	/**
	 * Removes a single CSS class from the {@link #getCssClasses()} of this {@link FormMember}.
	 *
	 * @param removedClass
	 *        The single CSS class to remove (must not contain spaces).
	 * @return Whether the given CSS class was set on this member before (was actually removed).
	 */
	default boolean removeCssClass(String removedClass) {
		if (StringServices.isEmpty(removedClass)) {
			return false;
		}

		String before = getCssClasses();
		String after = CssUtil.removeCssClasses(before, removedClass);
		if (StringServices.equals(before, after)) {
			return false;
		}

		setCssClasses(after);
		return true;
	}
    
    /**
     * The direct parent group of this form member. 
     */
    public FormContainer getParent();

	/**
	 * The effective display mode of this member.
	 * 
	 * @see #isVisible()
	 * @see #isDisabled()
	 * @see #isImmutable()
	 * @see FormField#isBlocked()
	 */
	FieldMode getMode();

	/**
	 * Sets the {@link FieldMode} of this member.
	 * 
	 * <p>
	 * Note: The {@link FieldMode#BLOCKED} is only applicable for {@link FormField}s.
	 * </p>
	 * 
	 * @see #getMode()
	 */
	void setMode(FieldMode value);

	/**
	 * Report, whether this member is visible without considering the state of
	 * its parent.
	 */
	public boolean isLocallyVisible();

	/**
	 * If a field is immutable, its contents cannot be edited on the user
	 * interface.
	 * 
	 * A view that displays an immutable fied must render it in a
	 * display-only fashion (e.g. a simple text instead of an text edit box). An
	 * immutable field should give the impression to the user, that the
	 * displayed information cannot (under no circumstances) be edited.
	 * 
	 * @see #isDisabled()
	 */
    public boolean isImmutable();

	/**
	 * Report, whether this member is immutable without considering the state of
	 * its parent.
	 */
	public boolean isLocallyImmutable();

    /**
	 * Recursively set the <code>immutable</code> property on this form
	 * member.
	 * 
	 * @see FormField#isImmutable()
	 */
    public void setImmutable(boolean isWriteProtected) ;

    /**
	 * If a field is disabled, its contents cannot be edited on the user
	 * interface.
	 * 
	 * {@link #isImmutable()} overrides {@link #isDisabled()}.
	 * 
	 * A view that displays a field, which is disabled, but also not immutable,
	 * may give the impression to the user that this field theoretically could
	 * be edited, but is only temporarily disabled. To achieve this effect, a
	 * view may render a edit box with gray background around the field's
	 * content.
	 * 
	 * @see #isImmutable()
	 */
    public boolean isDisabled();
    
	/**
	 * Report, whether this member is disabled without considering the state of
	 * its parent.
	 */
	public boolean isLocallyDisabled();

    /**
	 * Recursively set the {@link FormField#isDisabled() disabled} property on
	 * this form member.
	 * 
	 * @see FormField#isDisabled()
	 */
    public void setDisabled(boolean isDisabled);
    
	/**
	 * A {@link FormMember} is active, if it's {@link #isVisible() visible},
	 * not {@link #isImmutable() immutable}, and not
	 * {@link #isDisabled() disabled}.
	 */
    public boolean isActive();
    
    /**
	 * Set the <code>mandatory</code> property on this form member.
	 * 
	 * <p>
	 * Implementations may decide to consistently add and remove
	 * {@link Constraint}s with the modification of the mandatory property.
	 * </p>
	 * 
	 * @see FormField#isMandatory()
	 */
    public void setMandatory(boolean isMandatory);
    
	/**
	 * Whether the display mode {@link #isImmutable()}, {@link #isDisabled()},
	 * is inherited from the parent container and can only be further restricted
	 * by this member.
	 */
	public boolean getInheritDeactivation();

    
	/**
	 * Recursively remove all constraints
	 * {@link FormField#addConstraint(Constraint) added} to this member or its
	 * descendants.
	 */
	public void clearConstraints();
    
    /**
	 * Recursively resets descendant {@link FormField}s to its their initial
	 * state (where the value is the field's
	 * {@link FormField#setDefaultValue(Object) default value}).
	 */
    public void reset();
	
	/**
	 * Return a view of the resources of this field.
	 */
	public ResourceView getResources();

	/**
	 * Accepts the given visitor.
	 * 
	 * <p>
	 * Starts the visit by invoking the an appropriate visit choice
	 * corresponding to the concrete type of this {@link FormMember} and passes
	 * the given argument to the visit.
	 * </p>
	 * 
	 * @param v The visitor that performs the visit. 
	 * @param arg The argument to pass to the visit. 
	 * 
	 * @return The result produced by the visit. 
	 */     
	public <R, A> R visit(FormMemberVisitor<R, A> v, A arg);
    
    /**
	 * This method returns whether the something about the inner state of this {@link FormMember}
	 * has changed.
	 * 
	 * @see FormField#isChanged()
	 * @see FormContainer#isChanged()
	 */
	public boolean isChanged();
	
	/**
	 * Determines whether this {@link FormMember} is transient, i.e. whether it must be regarded
	 * when the {@link FormContext} this member belongs to becomes persistent.
	 */
	public boolean isTransient();

	/**
	 * You can use this method to ask whether this FormMember needs special handling to create a
	 * stable id to it (or it's children, in case it has children), if someone stored that
	 * information with the corresponding setter. <br/>
	 * <code>false</code> means: This is not a known and handled special case. <br/>
	 * <code>true</code> means: This is a known special case that should be handled.
	 * 
	 * @see BusinessObjectFieldRef
	 */
	public boolean hasStableIdSpecialCaseMarker();

	/**
	 * If this FormMember needs special handling to create a stable id to it (or it's children, in
	 * case it has children), you can get the necessary information here, after someone used the
	 * corresponding getter to store them.
	 * 
	 * @see BusinessObjectFieldRef
	 * 
	 * @return May be <code>null</code>.
	 */
	public Object getStableIdSpecialCaseMarker();

	/**
	 * If this FormMember needs special handling to create a stable id to it (or it's children, in
	 * case it has children), you can store the necessary information here. You can also use it as a
	 * marker that this is a special case.
	 * 
	 * @see BusinessObjectFieldRef
	 */
	public void setStableIdSpecialCaseMarker(Object stableIdSpecialCaseMarker);

	/**
	 * Context for a {@link FormMember} that needs a stable ID for special cases.
	 * 
	 * @see #getStableIdSpecialCaseMarker()
	 */
	public Object getStableIdSpecialCaseContext();

	/** @see FormMember#getStableIdSpecialCaseContext() */
	public void setStableIdSpecialCaseContext(Object context);

	/**
	 * The {@link ControlProvider} that should be used to render this {@link FormMember}.
	 */
	public ControlProvider getControlProvider();

	/**
	 * Sets the {@link #getControlProvider()} property.
	 */
	public void setControlProvider(ControlProvider value);

	/**
	 * Expands all collapsed groups, this {@link FormMember} is displayed in.
	 */
	public void makeVisible();

	/**
	 * The {@link ContextMenuProvider} for this {@link FormMember}.
	 * 
	 * <p>
	 * The context object of the resulting {@link ContextMenuProvider} (the argument to
	 * {@link ContextMenuProvider#getContextMenu(Object)}) is always this {@link FormMember}
	 * instance.
	 * </p>
	 */
	public ContextMenuProvider getContextMenu();

	/**
	 * @see #getContextMenu()
	 */
	public void setContextMenu(ContextMenuProvider value);

	/**
	 * Usage classification of the {@link FormMember#uiIdentifier(IdentifierSource, IDUsage) UI
	 * identifier}.
	 * 
	 * @see FormMember#uiIdentifier(IdentifierSource, IDUsage)
	 */
	enum IDUsage {
		/**
		 * The ID is requested for an input element.
		 */
		INPUT,

		/**
		 * The ID is requested for referencing an input element from a label.
		 */
		LABEL,

		/**
		 * The ID is requested for some other purpose.
		 */
		OTHER
	}
}
