/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ButtonUIModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.FocusHandling;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.ExecutabilityModel.ExecutabilityListener;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Utils;

/**
 * {@link FormMember}, that represents a button within {@link FormContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractButtonField extends AbstractFormMember implements ButtonUIModel, ExecutabilityListener {

	@Inspectable
	private ThemeImage enabledImage, notExecutableImage, activeImage;

	@Inspectable
	private ResKey notExecutableReasonKey;

	@Inspectable
	private boolean isExecutable = true;

	@Inspectable
	private ResKey altText;

	@Inspectable
	private char _accessKey;

	@Inspectable
	private boolean _progress;

	/* If the command field based on a BoundCommand it is necessary to react on security of it.
	 * These variables enable to be informed about changes of the ExecutableState. */
	private ExecutabilityModel executabilityDelegate;

	public AbstractButtonField(String formMemberName) {
		this(formMemberName, ConstantExecutabilityModel.ALWAYS_EXECUTABLE);
	}

	public AbstractButtonField(String formMemberName, ExecutabilityModel executabilityDelegate) {
		super(formMemberName);
		this.executabilityDelegate = executabilityDelegate;
	}

	@Override
	public boolean showProgress() {
		return _progress;
	}

	@Override
	public void setShowProgress(boolean value) {
		_progress = value;
	}

	/**
	 * Lately initializes the {@link ExecutabilityModel}.
	 */
	protected void initExecutabilityDelegate(ExecutabilityModel executability) {
		this.executabilityDelegate = executability;
	}

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		boolean hasListeners = hasListeners();
		if (!hasListeners) {
			executabilityDelegate.addExecutabilityListener(this);
			// bring state in sync with delegate
			updateDisplayMode();
		}
		/* Must add listener *after* the display state is updated. Otherwise an event is fired which
		 * may lead to failures, e.g. if the listener is a control currently attaching, an update
		 * would cause incremental updates for the GUI which is not allowed, because the control is
		 * not yet rendered. */
		boolean addListener = super.addListener(type, listener);
		if (!hasListeners && !addListener) {
			// adding listener failed! Remove listener from delegate
			executabilityDelegate.removeExecutabilityListener(this);
		}
		return addListener;
	}

	@Override
	public void handleExecutabilityChange(ExecutabilityModel sender, ExecutableState oldState, ExecutableState newState) {
		updateDisplayMode();
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		boolean removingSuccedded = super.removeListener(type, listener);
		if (!hasListeners() && removingSuccedded) {
			executabilityDelegate.removeExecutabilityListener(this);
		}
		return removingSuccedded;

	}
	@Override
	public boolean isLocallyDisabled() {
		final boolean locallyDisabled = super.isLocallyDisabled() || !isExecutable;
		if (locallyDisabled) {
			return locallyDisabled;
		} else {
			final ExecutableState executability = executabilityDelegate.getExecutability();
			final boolean notExecutableByDelegate = !executability.isExecutable();
			if (notExecutableByDelegate) {
				setNotExecutableReasonKey(executability.getI18NReasonKey());
			}
			return notExecutableByDelegate;
		}
	}

	@Override
	public boolean isLocallyVisible() {
		final boolean locallyVisible = super.isLocallyVisible();
		if (locallyVisible) {
			final ExecutableState executability = executabilityDelegate.getExecutability();
			final boolean visibleByDelegate = executability.isVisible();
			if (!visibleByDelegate) {
				setNotExecutableReasonKey(executability.getI18NReasonKey());
			}
			return visibleByDelegate;
		} else {
			return locallyVisible;
		}
	}

	@Override
	public ThemeImage getNotExecutableImage() {
		return notExecutableImage;
	}

	/**
	 * If the disabled image changes, an event with index
	 * {@link ButtonUIModel#NOT_EXECUTABLE_IMAGE_PROPERTY} is fired.
	 * 
	 * @see ButtonUIModel#setNotExecutableImage(ThemeImage)
	 */
	@Override
	public void setNotExecutableImage(ThemeImage image) {
		if (!Utils.equals(image, this.notExecutableImage)) {
			ThemeImage oldDisabledImage = this.notExecutableImage;
			this.notExecutableImage = image;
			firePropertyChanged(CommandModel.NOT_EXECUTABLE_IMAGE_PROPERTY, self(), oldDisabledImage, image);
		}
	}

	@Override
	public ThemeImage getImage() {
		return enabledImage;
	}

	/**
	 * If the image changes, an event with index {@link ButtonUIModel#IMAGE_PROPERTY} is fired.
	 * 
	 * @see ButtonUIModel#setImage(ThemeImage)
	 */
	@Override
	public void setImage(ThemeImage image) {
		if (!Utils.equals(image, this.enabledImage)) {
			ThemeImage oldEnabledImage = this.enabledImage;
			this.enabledImage = image;
			firePropertyChanged(CommandModel.IMAGE_PROPERTY, self(), oldEnabledImage, image);
		}
	}
	
	@Override
	public ThemeImage getActiveImage() {
		return activeImage;
	}

	@Override
	public void setActiveImage(ThemeImage image) {
		if (!Utils.equals(image, this.activeImage)) {
			ThemeImage oldActiveImage = this.activeImage;
			this.activeImage = image;
			firePropertyChanged(CommandModel.ACTIVE_IMAGE_PROPERTY, self(), oldActiveImage, image);
		}
	}

	@Override
	public void clearConstraints() {
		// nothing to do here
	}

	@Override
	public void reset() {
		// nothing to do here
	}

	@Override
	public void setMandatory(boolean isMandatory) {
		// nothing to do here
	}

	@Override
	public boolean isChanged() {
		// nothing can change here
		return false;
	}

	/**
	 * If the reason for non executable state changes, an event with key
	 * {@link ButtonUIModel#NOT_EXECUTABLE_REASON_PROPERTY} is fired.
	 */
	public void setNotExecutableReasonKey(ResKey disableReason) {
		ResKey currentReason = getNotExecutableReasonKey();
		if (!Utils.equals(currentReason, disableReason)) {
			this.notExecutableReasonKey = disableReason;
			firePropertyChanged(NOT_EXECUTABLE_REASON_PROPERTY, self(), currentReason, disableReason);
		}
	}

	@Override
	public ResKey getNotExecutableReasonKey() {
		return this.notExecutableReasonKey;
	}

	/**
	 * This method return true iff this {@link AbstractButtonField} is visible and neither immutable
	 * nor disabled.
	 * 
	 * @see ButtonUIModel#isExecutable()
	 */
	@Override
	public boolean isExecutable() {
		if (!isVisible()) {
			return false;
		}
		if (isImmutable()) {
			return false;
		} else {
			return !isDisabled();
		}
	}

	@Override
	public void setExecutable() {
		isExecutable = true;
		updateDisplayMode();
	}

	@Override
	public void setNotExecutable(ResKey disabledReason) {
		isExecutable = false;
		setNotExecutableReasonKey(disabledReason);
		updateDisplayMode();
	}

	@Override
	protected void notifyDisplayModeChanged(int oldDisplayMode, int newDisplayMode) {
		super.notifyDisplayModeChanged(oldDisplayMode, newDisplayMode);
		if (oldDisplayMode == ACTIVE_MODE) {
			firePropertyChanged(EXECUTABLE_PROPERTY, self(), Boolean.TRUE, Boolean.FALSE);
		}
		if (newDisplayMode == ACTIVE_MODE) {
			firePropertyChanged(EXECUTABLE_PROPERTY, self(), Boolean.FALSE, Boolean.TRUE);
		}
	}

	@Override
	public ResKey setLabel(ResKey newLabel) {
		ResKey oldLabel = super.setLabel(newLabel);

		//altText uses the label as fallback. Changing the Label could change the altText.
		if ((altText == null) && !StringServices.equals(oldLabel, newLabel)) {
			fireAltTextChangedIfChangeIsVisible(oldLabel, newLabel);
		}
		return oldLabel;
	}

	@Override
	public ResKey getAltText() {
		if (altText != null) {
			return altText;
		}

		if (getResources().hasStringResource("altText")) {
			return getResources().getStringResource("altText");
		}

		if (getLabel() != null) {
			return getLabel();
		}

		return ResKey.EMPTY_TEXT;
	}

	@Override
	public void setAltText(ResKey newAltText) {
		ResKey oldAltTextRespectingFallbacks = getAltText();
		altText = newAltText;
		ResKey newTextRespectingFallbacks = getAltText();

		fireAltTextChangedIfChangeIsVisible(oldAltTextRespectingFallbacks, newTextRespectingFallbacks);
	}

	private void fireAltTextChangedIfChangeIsVisible(ResKey oldAltTextIncludingFallbacks,
			ResKey newTextIncludingFallbacks) {
		if (!StringServices.equals(newTextIncludingFallbacks, oldAltTextIncludingFallbacks)) {
			firePropertyChanged(ALT_TEXT_PROPERTY, self(), oldAltTextIncludingFallbacks, newTextIncludingFallbacks);
		}
	}

	@Override
	public char getAccessKey() {
		return this._accessKey;
	}

	@Override
	public void setAccessKey(char newAccessKey) {
		char oldAccessKey = _accessKey;
		this._accessKey = newAccessKey;
		if (newAccessKey != oldAccessKey) {
			firePropertyChanged(ACCESS_KEY_PROPERTY, self(), oldAccessKey, newAccessKey);
		}
	}

	@Override
	public boolean focus() {
		if (FocusHandling.focus(DefaultDisplayContext.getDisplayContext(), this)) {
			firePropertyChanged(FOCUS_PROPERTY, self(), Boolean.FALSE, Boolean.TRUE);
		}
		return true;
	}

	@Override
	protected AbstractButtonField self() {
		return this;
	}

}
