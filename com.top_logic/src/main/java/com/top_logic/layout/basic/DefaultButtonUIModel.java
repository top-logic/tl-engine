/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.InlineMap;
import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyEventUtil;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormMember;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Utils;

/**
 * Default implementation of {@link ButtonUIModel} with all properties stored locally.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultButtonUIModel extends AbstractButtonUIModel implements LazyTypedAnnotatableMixin {

	@Inspectable
	private ThemeImage image;

	@Inspectable
	private ThemeImage notExecutableImage;

	@Inspectable
	private ThemeImage activeImage;

	@Inspectable
	private ResKey label;

	@Inspectable
	private String cssClasses;

	@Inspectable
	private ResKey tooltip;

	@Inspectable
	ResKey tooltipCaption;

	@Inspectable
	private ResKey altText;

	@Inspectable
	private boolean _progress;

	/**
	 * Map that hold the properties accessed by {@link #set(Property, Object)},
	 * {@link #reset(Property)}, and {@link #get(Property)}.
	 */
	@Inspectable
	private InlineMap<Property<?>, Object> _properties = InlineMap.empty();
	
	private boolean _observed;

	/**
	 * When {@link #_observed}, the last executability state returned.
	 * 
	 * @see #state()
	 */
	private ExecutableState _lastState = null;

	/**
	 * The visibility aspect of {@link #_lastState}.
	 * 
	 * @see #computeState()
	 */
	private ExecutableState _localVisibility = ExecutableState.EXECUTABLE;

	/**
	 * The executability aspect of {@link #_lastState}.
	 * 
	 * @see #computeState()
	 */
	private ExecutableState _localExecutability = ExecutableState.EXECUTABLE;

	@Inspectable
	private char _accessKey;

	@Override
	public ThemeImage getImage() {
		return this.image;
	}

	/**
	 * If the image changes, an event with index {@link ButtonUIModel#IMAGE_PROPERTY} is fired.
	 * 
	 * @see ButtonUIModel#setImage(ThemeImage)
	 */
	@Override
	public void setImage(ThemeImage image) {
		ThemeImage currentImage = getImage();
		if (!Utils.equals(currentImage, image)) {
			this.image = image;
			notifyListeners(ButtonUIModel.IMAGE_PROPERTY, this, currentImage, image);
		}
	}

	@Override
	public ThemeImage getNotExecutableImage() {
		return this.notExecutableImage;
	}

	/**
	 * If the image for non executable state changes, an event with index
	 * {@link ButtonUIModel#NOT_EXECUTABLE_IMAGE_PROPERTY} is fired.
	 * 
	 * @see ButtonUIModel#setNotExecutableImage(ThemeImage)
	 */
	@Override
	public void setNotExecutableImage(ThemeImage image) {
		ThemeImage currentImage = getNotExecutableImage();
		if (!Utils.equals(currentImage, image)) {
			this.notExecutableImage = image;
			notifyListeners(ButtonUIModel.NOT_EXECUTABLE_IMAGE_PROPERTY, this, currentImage, image);
		}
	}

	@Override
	public ThemeImage getActiveImage() {
		return this.activeImage;
	}

	/**
	 * If the image for active state changes, an event with index
	 * {@link ButtonUIModel#ACTIVE_IMAGE_PROPERTY} is fired.
	 * 
	 * @see ButtonUIModel#setActiveImage(ThemeImage)
	 */
	@Override
	public void setActiveImage(ThemeImage image) {
		ThemeImage currentImage = getActiveImage();
		if (!Utils.equals(currentImage, image)) {
			this.activeImage = image;
			notifyListeners(ButtonUIModel.ACTIVE_IMAGE_PROPERTY, this, currentImage, image);
		}
	}

	@Override
	public ResKey getLabel() {
		return this.label;
	}

	/**
	 * If the label changes, an event with index {@link AbstractFormMember#LABEL_PROPERTY} is fired.
	 */
	@Override
	public ResKey setLabel(ResKey newLabel) {
		ResKey oldLabelRespectingFallbacks = getLabel();
		ResKey oldAltTextRespectingFallbacks = getAltText();
		label = newLabel;
		ResKey newLabelRespectingFallbacks = getLabel();
		ResKey newAltTextRespectingFallbacks = getAltText();

		//Don't fire at invisible changes.
		if (!StringServices.equals(oldLabelRespectingFallbacks, newLabelRespectingFallbacks)) {
			notifyListeners(AbstractFormMember.LABEL_PROPERTY, this, oldLabelRespectingFallbacks,
				newLabelRespectingFallbacks);
		}

		//altText uses the label as fallback. Changing the Label could change the altText.
		fireAltTextChangedIfChangeIsVisible(oldAltTextRespectingFallbacks, newAltTextRespectingFallbacks);

		return oldLabelRespectingFallbacks;
	}

    @Override
	public ResKey getTooltip() {
        return this.tooltip;
    }
    
    /**
	 * If the tooltip changes, an event with index* {@link FormMember#TOOLTIP_PROPERTY} is fired.
	 */
    @Override
	public void setTooltip(ResKey aTooltip) {
		ResKey currentTooltip = getTooltip();
        if (!Utils.equals(currentTooltip, aTooltip)) {
            this.tooltip = aTooltip;
			notifyListeners(AbstractFormMember.TOOLTIP_PROPERTY, this, currentTooltip, aTooltip);
        }
    }
	
    @Override
	public ResKey getTooltipCaption() {
        return this.tooltipCaption;
    }
    
	/**
	 * If the tooltip caption changes, a event with index
	 * {@link AbstractFormMember#TOOLTIP_PROPERTY} is fired.
	 */
    @Override
	public void setTooltipCaption(ResKey aTooltip) {
		ResKey currentTooltip = getTooltipCaption();
        if (!Utils.equals(currentTooltip, aTooltip)) {
            this.tooltipCaption = aTooltip;
			notifyListeners(AbstractFormMember.TOOLTIP_PROPERTY, this, currentTooltip, aTooltip);
        }
    }

	/**
	 * @see CommandModel#getAltText()
	 */
	@Override
	public ResKey getAltText() {
		if (altText != null) {
			return altText;
		}

		if (getLabel() != null) {
			return getLabel();
		}

		return ResKey.text("");
	}

	/**
	 * If the altText changes, a event with index {@link ButtonUIModel#ALT_TEXT_PROPERTY} is fired.
	 */
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
			notifyListeners(ALT_TEXT_PROPERTY, this, oldAltTextIncludingFallbacks, newTextIncludingFallbacks);
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
			notifyListeners(ACCESS_KEY_PROPERTY, this, oldAccessKey, newAccessKey);
		}
	}
    
	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		boolean firstListener = !hasListeners();
		if (firstListener) {
			firstListenerAdded();
		}
		return super.addListener(type, listener);
	}

	/**
	 * Hook called when the first {@link PropertyListener} is added.
	 */
	protected void firstListenerAdded() {
		_observed = true;
		updateState();
	}

	@Override
	public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
		boolean removingSuccedded = super.removeListener(type, listener);
		if (removingSuccedded && !hasListeners()) {
			lastListenerRemoved();
		}
		return removingSuccedded;
	}

	/**
	 * Hook called when the last {@link PropertyListener} is removed.
	 */
	protected void lastListenerRemoved() {
		_observed = false;
		_lastState = null;
	}

	@Override
	public final boolean isVisible() {
		return state().isVisible();
	}

	/**
	 * If visibility changes, an event with index {@link FormMember#VISIBLE_PROPERTY} is fired,
	 * moreover maybe an event with index {@link ButtonUIModel#EXECUTABLE_PROPERTY} is fired if also
	 * the executable state changes.
	 * 
	 * @see CommandModel#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		_localVisibility = visible ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
		updateState();
	}

	@Override
	public final boolean isExecutable() {
		return state().isExecutable();
	}

	@Override
	public void setExecutable() {
		_localExecutability = ExecutableState.EXECUTABLE;
		updateState();
	}

	@Override
	public void setNotExecutable(ResKey disableReason) {
		_localExecutability = ExecutableState.createDisabledState(disableReason);
		updateState();
	}

	private ExecutableState state() {
		if (!isObserved()) {
			return computeState();
		}
		return _lastState;
	}

	/**
	 * Updates the {@link #_lastState state} of this {@link CommandModel} and fires necessary
	 * events.
	 */
	protected final void updateState() {
		if (!isObserved()) {
			return;
		}
		ExecutableState oldState = _lastState;
		ExecutableState newState = computeState();
		_lastState = newState;
		if (oldState != null) {
			// Do not bother listeners with initial internal null state.
			notifyStateChanged(this, oldState, newState);
		}
	}

	/**
	 * Computes the new executable state of the properties {@link #isVisible()},
	 * {@link #isExecutable()}, and {@link #getNotExecutableReasonKey()}.
	 * 
	 * <p>
	 * This method is called after each potential state change. Sub classes may override to include
	 * dynamically observed executability state.
	 * </p>
	 */
	protected ExecutableState computeState() {
		return _localVisibility.combine(_localExecutability);
	}

	/**
	 * Called after changing the state of this command models from <code>oldState</code> to
	 * <code>newState</code>, therefore the given states are different.
	 * 
	 * @param oldState
	 *        the state before the change.
	 * @param newState
	 *        the state after the change
	 */
	public static void notifyStateChanged(AbstractButtonUIModel sender, ExecutableState oldState, ExecutableState newState) {
		fireVisibleChange(sender, oldState, newState);
		fireExecutableChange(sender, oldState, newState);
		fireReasonKeyChange(sender, oldState, newState);
	}

	private static boolean fireVisibleChange(AbstractButtonUIModel sender, ExecutableState oldState, ExecutableState newState) {
		boolean oldVisible = oldState.isVisible();
		boolean newVisible = newState.isVisible();
		boolean changed = oldVisible != newVisible;
		if (changed) {
			PropertyEventUtil.notifyListeners(sender, VISIBLE_PROPERTY, Boolean.valueOf(oldVisible),
				Boolean.valueOf(newVisible));
		}
		return changed;
	}

	private static boolean fireExecutableChange(AbstractButtonUIModel sender, ExecutableState oldState, ExecutableState newState) {
		boolean oldExecutable = oldState.isExecutable();
		boolean newExecutable = newState.isExecutable();
		boolean changed = oldExecutable != newExecutable;
		if (changed) {
			PropertyEventUtil.notifyListeners(sender, 
				CommandModel.EXECUTABLE_PROPERTY, Boolean.valueOf(oldExecutable), Boolean.valueOf(newExecutable));
		}
		return changed;
	}

	private static boolean fireReasonKeyChange(AbstractButtonUIModel sender, ExecutableState oldState, ExecutableState newState) {
		ResKey oldKey = oldState.getI18NReasonKey();
		ResKey newKey = newState.getI18NReasonKey();
		boolean changed = !Utils.equals(oldKey, newKey);
		if (changed) {
			PropertyEventUtil.notifyListeners(sender, NOT_EXECUTABLE_REASON_PROPERTY, oldKey, newKey);
		}
		return changed;
	}

	@Override
	public ResKey getNotExecutableReasonKey() {
		return state().getI18NReasonKey();
	}

	@Override
	public String getCssClasses() {
		return this.cssClasses;
	}

	@Override
	public void setCssClasses(String newCssClasses) {
		if (StringServices.isEmpty(newCssClasses)) {
			newCssClasses = null;
		}
		String currentCssClasses = getCssClasses();
		if (!Utils.equals(currentCssClasses, newCssClasses)) {
			this.cssClasses = newCssClasses;
			notifyListeners(AbstractFormMember.CLASS_PROPERTY, this, currentCssClasses, newCssClasses);
		}
	}
	
	@Override
	public boolean showProgress() {
		return _progress;
	}

	@Override
	public void setShowProgress(boolean value) {
		_progress = value;
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
	 * Return the I18N name of the given command.
	 * 
	 * @param aCommand
	 *        The command to get the I18N for, must not be <code>null</code>.
	 * @return The requested I18N.
	 */
	protected static ResKey getCommandI18N(ControlCommand aCommand) {
		return aCommand.getI18NKey();
	}

	@Override
	public String toString() {
		StringBuilder toStringBuilder = new StringBuilder();
		toStringBuilder.append(getClass().getName()).append('[');
		appendToStringValues(toStringBuilder);
		toStringBuilder.append(']');
		return toStringBuilder.toString();
	}
	
	/**
	 * Hook for sub classes to add more information to the {@link #toString()} representation of an
	 * object.
	 * 
	 * @param toStringBuilder
	 *        The buffer to append.
	 */
	protected void appendToStringValues(StringBuilder toStringBuilder) {
		toStringBuilder.append("executability:").append(state()).append(',');
		toStringBuilder.append("label:").append(getLabel()).append(',');
	}

	@Override
	public boolean focus() {
		if (FocusHandling.focus(DefaultDisplayContext.getDisplayContext(), this)) {
			notifyListeners(FOCUS_PROPERTY, this, Boolean.FALSE, Boolean.TRUE);
		}
		return false;
	}

	/**
	 * Whether this {@link DefaultButtonUIModel} is currently observed.
	 */
	protected boolean isObserved() {
		return _observed;
	}

}
