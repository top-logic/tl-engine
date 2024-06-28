/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.form.control.InfoControl;
import com.top_logic.layout.form.control.IntegerInputControl;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Summary of constants that describe CSS classes and JavaScript class and
 * package names used in form field views.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormConstants {

	/**
	 * Default CSS class for elements that should have <code>overflow: auto;</code>
	 */
	public static final String OVERFLOW_AUTO_CLASS = "tl-overflow--auto";

	/** Default CSS class of a from body {@link HTMLConstants#DIV}. */
	public static final String FORM_BODY_CSS_CLASS = "frmBody";

	/**
	 * CSS class that identifies the UI of an {@link InfoControl}.
	 */
	public static final String IS_INFO_CSS_CLASS   = "is-info";
	
	public static final String IS_RADIO_CSS_CLASS   = "is-radio";
	public static final String IS_CHECKBOX_CSS_CLASS   = "is-checkbox";

	/**
	 * CSS class added to <code>input</code> elements of image buttons
	 */
    public static final String INPUT_IMAGE_CSS_CLASS = "input-image";
    
	/**
	 * CSS class added to <code>img</code> elements of type image of labels.
	 */
	public static final String TYPE_IMAGE_CSS_CLASS = "type-image";

	public static final String TREE_TYPE_IMAGE_CSS_CLASS = "treeType";

    /** Prefix for the {@link IntegerInputControl}'s plus button style class */
    public static final String BUTTON_PLUS_CSS_CLASS = "button_plus";
    
    /** Prefix for the {@link IntegerInputControl}'s minus button style class */
    public static final String BUTTON_MINUS_CSS_CLASS = "button_minus";

	/**
	 * CSS class that describes a top-level HTML element of a conditionally
	 * visible view. A {@link #IS_ERROR_CSS_CLASS} part of an on-error view
	 * is only visible, if the associated form field has an error.
	 */
	public static final String IS_ON_ERROR_CSS_CLASS = "is-onerror";

	/**
	 * CSS class that describes a sub-element of a
	 * {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control} element, which is the input part of
	 * the view.
	 *
	 * @see #IS_DISPLAY_CSS_CLASS for the corresponding display element.
	 */
	public static final String IS_INPUT_CSS_CLASS = "is-input";

	/**
	 * CSS class for {@link HTMLConstants#INPUT} fields of type
	 * {@link HTMLConstants#FILE_TYPE_VALUE}.
	 */
	public static final String IS_UPLOAD_CSS_CLASS = "is-upload";

	/**
	 * CSS class that describes a sub-element of a
	 * {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control} element, which is the display part
	 * of the view.
	 *
	 * <p>
	 * Both classes, {@link #IS_DISPLAY_CSS_CLASS} and
	 * {@link #IS_INPUT_CSS_CLASS} are used to make the corresponding element
	 * visible, if the field is
	 * {@link FormField#isImmutable() immutable} or mutable.
	 * </p>
	 *
	 * @see #IS_INPUT_CSS_CLASS for the corresponding input element.
	 */
	public static final String IS_DISPLAY_CSS_CLASS = "is-display";

	/**
	 * CSS class that marks a GUI element that represents a value that is
	 * blocked by an access rule.
	 */
	public static final String IS_BLOCKED_CSS_CLASS = "is-blocked";

	/**
	 * CSS class that describes a sub-element of an
	 * {@link #IS_ON_ERROR_CSS_CLASS} element, which is only visible, if the
	 * form field associated with its view has an error.
	 *
	 * <p>
	 * An element with this class displays the error message that occured during
	 * {@link FormField#check() checking} the associated form field.
	 * </p>
	 *
	 * <p>
	 * Elements with this class may be rendered visually different.
	 * </p>
	 */
	public static final String IS_ERROR_CSS_CLASS = "is-error";

	/**
	 * CSS class that describes a sub-element of a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control} element,
	 * which represents an active part of the view.
	 */
	public static final String IS_ACTION_CSS_CLASS = "is-action";

	/**
	 * CSS class that describes a sub-element of a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control} element,
	 * which represents an active part of the view and is displayed by an
	 * {@link HTMLConstants#INPUT} of type {@link HTMLConstants#IMAGE_TYPE_VALUE}.
	 */
	public static final String INPUT_IMAGE_ACTION_CSS_CLASS = INPUT_IMAGE_CSS_CLASS + ' ' + IS_ACTION_CSS_CLASS;

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, it it represents the view of a popup input field. This
	 * class is for visual formatting only.
	 */
	public static final String IS_POPUP_CSS_CLASS = "is-popup";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field has an error.
	 *
	 * @see #NO_ERROR_CSS_CLASS
	 */
	public static final String ERROR_CSS_CLASS = "error";
	
	/**
	 * CSS class that can be set to {@link FormField}s that actually have no error but which should
	 * look like having error.
	 * 
	 * @see FormConstants#ERROR_CSS_CLASS
	 */
	public static final String ERROR_ANNOTATION_CSS_CLASS = "error-annotation";

	public static final String WARNING_CSS_CLASS = "warning";

	/**
	 * CSS class that can be set to {@link FormField}s that actually have no warnings but which
	 * should look like having warnings.
	 * 
	 * @see FormConstants#WARNING_CSS_CLASS
	 */
	public static final String WARNING_ANNOTATION_CSS_CLASS = "warning-annoation";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field has no error.
	 *
	 * @see #ERROR_CSS_CLASS
	 */
	public static final String NO_ERROR_CSS_CLASS = "no-error";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field
	 * {@link FormField#isMandatory() is mandatory}.
	 */
	public static final String MANDATORY_CSS_CLASS = "mandatory";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field is
	 * {@link FormField#isImmutable() is not immutable}.
	 */
	public static final String CAN_EDIT_CSS_CLASS = "can-edit";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field is
	 * {@link FormField#isImmutable() is immutable}.
	 */
	public static final String IMMUTABLE_CSS_CLASS = "cannot-edit";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field is invisible.
	 */
	public static final String INVISIBLE_CSS_CLASS = "invisible";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field is
	 * {@link FormField#isDisabled() disabled}.
	 */
	public static final String DISABLED_CSS_CLASS = "disabled";

	/**
	 * CSS class that is annotated to a {@link AbstractControlBase#IS_CONTROL_CSS_CLASS control}
	 * HTML element, if the corresponding field is {@link FormField#isActive()}.
	 */
	public static final String ACTIVE_CSS_CLASS = "active";

	public static final String COLLAPSED_CLASS = "collapsed";


	public static final String EXPANDED_CLASS = "expanded";

	/**
     * Annotates an (invisible) JSON config string to the client-side display of a
     * {@link Control}.
     */
	public static final String CONFIG_CLASS = "config";

	/**
	 * CSS class that is annotated to an element showing an access key of a HTML
	 * label element.
	 */
	public static final String ACCESSKEY_CSS_CLASS = "is-accesskey";

	/**
	 * JavaScript package for client-side form controls.
	 */
	public static final String FORM_PACKAGE = "services.form";

	/**
	 * JavaScript class of a text input control with server-side logic.
	 */
	public static final String TEXT_INPUT_CONTROL_CLASS = FORM_PACKAGE + ".TextInputControl";
	
	/**
     * JavaScript class of a integer input control with server-side logic.
     */
	public static final String INTEGER_INPUT_CONTROL_CLASS = FORM_PACKAGE + ".IntegerInputControl";

	/**
	 * JavaScript class of a choice control.
	 */
	public static final String BEACON_HANDLER_CLASS = FORM_PACKAGE + ".BeaconControl";

	/**
	 * JavaScript class of a boolean choice control.
	 */
	public static final String BOOLEAN_CHOICE_CONTROL_CLASS = FORM_PACKAGE + ".BooleanChoiceControl";

	/**
	 * JavaScript class of a checkbox control with three states.
	 */
	public static final String TRISTATE_CHECKBOX_HANDLER_CLASS = FORM_PACKAGE + ".TristateControl";

	/**
	 * JavaScript class of a checkbox control.
	 */
	public static final String CHECKBOX_HANDLER_CLASS = FORM_PACKAGE + ".Checkbox";

	/**
	 * JavaScript class of a date input control with calendar button.
	 */
	public static final String CALENDAR_HANDLER_CLASS = FORM_PACKAGE + ".DateInputControl";
	
	/**
	 * JavaScript class of a calendar control.
	 */
	public static final String CALENDAR_DIALOG_HANDLER_CLASS = FORM_PACKAGE + ".CalendarControl";

	/**
	 * JavaScript class of a time control.
	 */
	public static final String CLOCK_HANDLER_CLASS = FORM_PACKAGE + ".TimeInputControl";

	public static final String SELECT_CONTROL_CLASS = FORM_PACKAGE + ".SelectControl";

	public static final String CHOICE_CONTROL_CLASS = FORM_PACKAGE + ".ChoiceControl";

	public static final String ICON_SELECT_CONTROL_CLASS = FORM_PACKAGE + ".IconSelectControl";

	public static final String SELECT_OPTION_CONTROL_CLASS = FORM_PACKAGE + ".SelectOptionControl";

	public static final String SELECTION_PART_CONTROL_CLASS = FORM_PACKAGE + ".SelectionPartControl";

	/**
	 * JavaScript class of a icon input control with selected icon button.
	 */
	public static final String ICON_HANDLER_CLASS = FORM_PACKAGE + ".IconInputControl";

	/**
	 * CSS class that creates a fixed size box (at the left of the flexible box) within a flexible
	 * table cell layout.
	 * 
	 * @see #FLEXIBLE_CSS_CLASS
	 * @see #FIXED_RIGHT_CSS_CLASS
	 */
	public static final String FIXED_LEFT_CSS_CLASS = "lFixedLeft";

	/**
	 * CSS class that creates a flexible box that consumes all available space within a flexible
	 * table cell layout.
	 * 
	 * @see #FIXED_LEFT_CSS_CLASS
	 * @see #FIXED_RIGHT_CSS_CLASS
	 */
	public static final String FLEXIBLE_CSS_CLASS = "lFlexible";

	/**
	 * CSS class that creates a fixed size box (at the right of the flexible box) within a flexible
	 * table cell layout.
	 * 
	 * @see #FLEXIBLE_CSS_CLASS
	 * @see #FIXED_LEFT_CSS_CLASS
	 */
	public static final String FIXED_RIGHT_CSS_CLASS = "lFixedRight";

	/**
	 * CSS class attached to all input elements that open a (popup-) menu.
	 */
	public static final String MENU_BUTTON_CSS_CLASS = "fMenu";

	/**
	 * CSS class attached to all input elements that clear/reset a view.
	 */
	public static final String CLEAR_BUTTON_CSS_CLASS = "fClear";

	/**
	 * CSS class attached to all input elements that trigger an upload or download.
	 */
	public static final String DOWNLOAD_BUTTON_CSS_CLASS = "fDownload";

	/**
	 * CSS class attached to all input elements that switch a view to a different mode.
	 */
	public static final String TOGGLE_BUTTON_CSS_CLASS = "fToggle";

	/**
	 * CSS class attached to all input elements that trigger a selection.
	 */
	public static final String CHOOSE_BUTTON = "fChoose";

	/**
	 * CSS class for buttons that may be pushed or not pushed, i.e the button reflects two different
	 * states of the application.
	 */
	public static final String PUSHED_CSS_CLASS = "pushed";
}
