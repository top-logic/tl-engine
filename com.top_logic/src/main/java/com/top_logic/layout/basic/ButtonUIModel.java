/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.VisibilityModel;


/**
 * UI model for UI-triggered actions.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ButtonUIModel extends Focusable, TypedAnnotatable, VisibilityModel {

	/**
	 * Event type to notify {@link ExecutableListener}.
	 * 
	 * @see ExecutableListener#handleExecutableChange(ButtonUIModel, Boolean, Boolean)
	 */
	EventType<ExecutableListener, ButtonUIModel, Boolean> EXECUTABLE_PROPERTY =
		new EventType<>("executable") {

			@Override
			public Bubble dispatch(ExecutableListener listener, ButtonUIModel sender, Boolean oldValue, Boolean newValue) {
				return listener.handleExecutableChange(sender, oldValue, newValue);
			}

		};

	/**
	 * Event type to notify {@link ExecutableListener}.
	 * 
	 * @see ExecutableListener#handleNotExecutableReasonChange(ButtonUIModel, ResKey, ResKey)
	 */
	EventType<ExecutableListener, ButtonUIModel, ResKey> NOT_EXECUTABLE_REASON_PROPERTY =
		new EventType<>("notExecutableReason") {

			@Override
			public Bubble dispatch(ExecutableListener listener, ButtonUIModel sender, ResKey oldValue, ResKey newValue) {
				return listener.handleNotExecutableReasonChange(sender, oldValue, newValue);
			}

		};

	/**
	 * Event type to notify {@link ExecutableListener}.
	 * 
	 * @see ExecutableListener#handleExecutableChange(ButtonUIModel, Boolean, Boolean)
	 */
	EventType<ExecutableListener, ButtonUIModel, Boolean> ACTIVE_PROPERTY =
		new EventType<>("active") {

			@Override
			public Bubble dispatch(ExecutableListener listener, ButtonUIModel sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleExecutableChange(sender, oldValue, newValue);
			}
		};
		
	/**
	 * Event type to notify {@link ButtonImageListener}.
	 * 
	 * @see ButtonImageListener#handleDisabledImageChanged(ButtonUIModel, ThemeImage, ThemeImage)
	 */
	EventType<ButtonImageListener, ButtonUIModel, ThemeImage> NOT_EXECUTABLE_IMAGE_PROPERTY =
		new EventType<>("notExecutableImage") {

			@Override
			public Bubble dispatch(ButtonImageListener listener, ButtonUIModel sender, ThemeImage oldValue,
					ThemeImage newValue) {
				return listener.handleDisabledImageChanged(sender, oldValue, newValue);
			}
		};

	/**
	 * Event type to notify {@link ButtonImageListener}.
	 * 
	 * @see ButtonImageListener#handleImageChanged(ButtonUIModel, ThemeImage, ThemeImage)
	 */
	EventType<ButtonImageListener, ButtonUIModel, ThemeImage> IMAGE_PROPERTY =
		new EventType<>("enabledImage") {

			@Override
			public Bubble dispatch(ButtonImageListener listener, ButtonUIModel sender, ThemeImage oldValue,
					ThemeImage newValue) {
				return listener.handleImageChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Event type to notify {@link ButtonImageListener}.
	 * 
	 * @see ButtonImageListener#handleImageChanged(ButtonUIModel, ThemeImage, ThemeImage)
	 */
	EventType<ButtonImageListener, ButtonUIModel, ThemeImage> ACTIVE_IMAGE_PROPERTY =
		new EventType<>("activeImage") {

			@Override
			public Bubble dispatch(ButtonImageListener listener, ButtonUIModel sender, ThemeImage oldValue,
					ThemeImage newValue) {
				return listener.handleImageChanged(sender, oldValue, newValue);
			}
		};

	/**
	 * Event type to notify {@link AltTextChangedListener}.
	 * 
	 * @see AltTextChangedListener
	 */
	EventType<AltTextChangedListener, Object, String> ALT_TEXT_PROPERTY =
		new EventType<>("altText") {

			@Override
			public Bubble dispatch(AltTextChangedListener listener, Object sender, String oldValue, String newValue) {
				return listener.handleAltTextChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Event type to notify {@link AccessKeyListener}.
	 * 
	 * @see AccessKeyListener
	 */
	EventType<AccessKeyListener, ButtonUIModel, Character> ACCESS_KEY_PROPERTY =
		new EventType<>("accessKey") {

			@Override
			public Bubble dispatch(AccessKeyListener listener, ButtonUIModel sender, Character oldValue,
					Character newValue) {
				return listener.handleAccessKeyChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Sets the image for being shown if this {@link ButtonUIModel} is executable. All
	 * {@link GenericPropertyListener}s which are currently added must be informed about a change.
	 * 
	 * @see ButtonUIModel#IMAGE_PROPERTY
	 */
	void setImage(ThemeImage image);

	/**
	 * The enabled image.
	 * 
	 * @return may be <code>null</code>, if no image was set.
	 */
	ThemeImage getImage();

	/**
	 * Sets the image for being shown if this {@link ButtonUIModel} is currently not executable. All
	 * {@link GenericPropertyListener}s which are currently added must be informed about a change.
	 * 
	 * @see ButtonUIModel#NOT_EXECUTABLE_IMAGE_PROPERTY
	 */
	void setNotExecutableImage(ThemeImage image);

	/**
	 * The image for being shown in non executable state.
	 * 
	 * @return may be <code>null</code>, if no image was set.
	 */
	ThemeImage getNotExecutableImage();

	/**
	 * Sets the image for being shown if this {@link ButtonUIModel} is active. All
	 * {@link GenericPropertyListener}s which are currently added must be informed about a change.
	 * 
	 * @see ButtonUIModel#ACTIVE_IMAGE_PROPERTY
	 */
	void setActiveImage(ThemeImage image);

	/**
	 * The image for being shown in active state.
	 * 
	 * @return may be <code>null</code>, if no image was set.
	 */
	ThemeImage getActiveImage();

	/**
	 * Sets the label of this {@link ButtonUIModel}. All {@link GenericPropertyListener} s which are
	 * currently added must be informed about a change.
	 * 
	 * Returns the old label.
	 */
	String setLabel(String label);

	/**
	 * The label for this {@link ButtonUIModel}.
	 * 
	 * @return must not be <code>null</code>.
	 */
	String getLabel();

	/**
	 * Tooltip for this {@link ButtonUIModel}.
	 * 
	 * @return may be <code>null</code>.
	 */
	String getTooltip();

	/**
	 * Sets the tooltip of this {@link ButtonUIModel}.
	 */
	void setTooltip(String aTooltip);

	/**
	 * Tooltip caption for this {@link ButtonUIModel}.
	 * 
	 * @return may be <code>null</code>.
	 */
	String getTooltipCaption();

	/**
	 * Sets the tooltip caption of this {@link ButtonUIModel}.
	 */
	void setTooltipCaption(String aTooltip);

	/**
	 * Alternative image text for this {@link ButtonUIModel}.
	 * 
	 * @return is never <code>null</code>.
	 */
	String getAltText();

	/**
	 * In adition to 'normal' text, this method allows <code>null</code> (meaning: use fallbacks)
	 * and <code>""</code> (meaning: intentionally no text, even if a fallback would have text)
	 * 
	 * @see ButtonUIModel#ALT_TEXT_PROPERTY
	 */
	void setAltText(String anAltText);

	/**
	 * The keyboard shortcut to trigger this command directly (ALT+&lt;accesskey&gt;).
	 */
	char getAccessKey();

	/**
	 * Sets the {@link #getAccessKey()} property.
	 * 
	 * @see ButtonUIModel#ACCESS_KEY_PROPERTY
	 */
	void setAccessKey(char accessKey);

	/**
	 * Sets whether the button is currently visible. All {@link GenericPropertyListener} s which are
	 * currently added must be informed about a change.
	 */
	@Override
	void setVisible(boolean visible);

	/**
	 * Returns whether this button is visible. {@link ButtonUIModel}s which are not visible are also
	 * not executable.
	 */
	@Override
	boolean isVisible();

	/**
	 * Sets this command model {@link #isExecutable() executable}.
	 * 
	 * @see CommandModel#setNotExecutable(ResKey)
	 * 
	 * @see ButtonUIModel#EXECUTABLE_PROPERTY
	 */
	void setExecutable();

	/**
	 * Sets this command model {@link #isExecutable() not executable}.
	 * 
	 * <p>
	 * Sets the model not executable for the given reason. The given reason may be the current
	 * reason (see {@link #getNotExecutableReasonKey()}) or a new one. It is strongly recommend that
	 * the reason is not <code>null</code>, because this may lead to failure if
	 * {@link #getNotExecutableReasonKey()} is called for a not executable model.
	 * </p>
	 * 
	 * @param disabledReason
	 *        New value of {@link #getNotExecutableReasonKey()}.
	 * 
	 * @see CommandModel#setExecutable()
	 * @see ButtonUIModel#EXECUTABLE_PROPERTY
	 * @see ButtonUIModel#NOT_EXECUTABLE_REASON_PROPERTY
	 */
	void setNotExecutable(ResKey disabledReason);

	/**
	 * Whether this button is currently executable.
	 * 
	 * @see #isVisible()
	 */
	boolean isExecutable();

	/**
	 * The I18N key with optionally encoded arguments that describes why this {@link ButtonUIModel}
	 * is currently not executable.
	 * 
	 * @return Key for an internationalized message for the reason why this button is currently not
	 *         executable, or <code>null</code> if this button is executable.
	 */
	ResKey getNotExecutableReasonKey();

	/**
	 * Whether this button is currently active.
	 */
	default boolean isLinkActive() {
		return false;
	}

	/**
	 * Space separated string of CSS classes which can be used if this {@link ButtonUIModel} is
	 * executable.
	 * 
	 * @return a blank separated String of CSS classes or <code>null</code> if no specific CSS
	 *         classes shall be used.
	 */
	String getCssClasses();

	/**
	 * Sets the CSS Classes returned by {@link #getCssClasses()}
	 * 
	 * @see #getCssClasses()
	 */
	void setCssClasses(String newCssClasses);

	/**
	 * Whether a progress slider is shown.
	 */
	boolean showProgress();

	/**
	 * Enables {@link #showProgress()}.
	 */
	default void setShowProgress() {
		setShowProgress(true);
	}

	/**
	 * @see #showProgress()
	 */
	void setShowProgress(boolean value);

}