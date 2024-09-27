/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Adaptor implementation for the {@link CommandModel} interface.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class CommandModelAdapter extends AbstractButtonUIModel implements WrappedCommandModel,
		GenericPropertyListener {

	@Inspectable
	private final CommandModel impl;
	
	/**
	 * Creates a {@link CommandModelAdapter}.
	 * 
	 * @param impl
	 *        The {@link CommandModel} to delegate all calls to.
	 */
	public CommandModelAdapter(CommandModel impl) {
		this.impl = impl;
	}

	/**
	 * The underlying {@link CommandModel} to dispatch to.
	 */
	@Override
	public CommandModel unwrap() {
		return impl;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		return impl.executeCommand(context);
	}
	
	@Override
	public CheckScope getCheckScope() {
		return impl.getCheckScope();
	}
	
	@Override
	public <T> T set(Property<T> property, T value) {
		return impl.set(property, value);
	}

	@Override
	public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
		if (!hasListeners()) {
			firstListenerAdded();
		}
		return super.addListener(type, listener);
	}

	/**
	 * Hook called, if this model starts being observed.
	 */
	protected void firstListenerAdded() {
		impl.addListener(GLOBAL_LISTENER_TYPE, this);
	}

	@Override
	public <T extends PropertyListener, S, V> boolean removeListener(EventType<T, S, V> type, T listener) {
		boolean removed = super.removeListener(type, listener);
		if (removed && !hasListeners()) {
			lastListenerRemoved();
		}
		return removed;
	}

	/**
	 * Hook called, if this model is no longer observed.
	 */
	protected void lastListenerRemoved() {
		impl.removeListener(GLOBAL_LISTENER_TYPE, this);
	}

	@Override
	public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
		forwardEvent(type, oldValue, newValue);
		return Bubble.BUBBLE;
	}

	/**
	 * Forwards the given property event to own listeners.
	 * 
	 * @param type
	 *        The type of event occurred.
	 * @param oldValue
	 *        The old property value.
	 * @param newValue
	 *        The new property value.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void forwardEvent(EventType<?, ?, ?> type, Object oldValue, Object newValue) {
		notifyListeners((EventType) type, this, oldValue, newValue);
	}

	@Override
	public String getCssClasses() {
		return impl.getCssClasses();
	}

	@Override
	public ThemeImage getImage() {
		return impl.getImage();
	}

	@Override
	public ThemeImage getActiveImage() {
		return impl.getActiveImage();
	}

	@Override
	public ResKey getLabel() {
		return impl.getLabel();
	}

	@Override
	public ThemeImage getNotExecutableImage() {
		return impl.getNotExecutableImage();
	}

	@Override
	public ResKey getNotExecutableReasonKey() {
		return impl.getNotExecutableReasonKey();
	}

	@Override
	public boolean isSet(Property<?> property) {
		return impl.isSet(property);
	}

	@Override
	public <T> T get(Property<T> property) {
		return impl.get(property);
	}

	@Override
	public ResKey getTooltip() {
		return impl.getTooltip();
	}

	@Override
	public ResKey getTooltipCaption() {
		return impl.getTooltipCaption();
	}

	@Override
	public ResKey getAltText() {
		return impl.getAltText();
	}

	@Override
	public char getAccessKey() {
		return impl.getAccessKey();
	}

	@Override
	public void setAccessKey(char accessKey) {
		impl.setAccessKey(accessKey);
	}

	@Override
	public boolean isExecutable() {
		return impl.isExecutable();
	}

	@Override
	public boolean isVisible() {
		return impl.isVisible();
	}

	@Override
	public <T> T reset(Property<T> property) {
		return impl.reset(property);
	}

	@Override
	public void setCssClasses(String newCssClasses) {
		impl.setCssClasses(newCssClasses);
	}

	@Override
	public void setExecutable() {
		impl.setExecutable();
	}

	@Override
	public void setNotExecutable(ResKey disabledReason) {
		impl.setNotExecutable(disabledReason);
	}

	@Override
	public void setImage(ThemeImage image) {
		impl.setImage(image);
	}

	@Override
	public void setActiveImage(ThemeImage image) {
		impl.setActiveImage(image);
	}

	@Override
	public ResKey setLabel(ResKey label) {
		return impl.setLabel(label);
	}

	@Override
	public void setNotExecutableImage(ThemeImage image) {
		impl.setNotExecutableImage(image);
	}

	@Override
	public void setTooltip(ResKey aTooltip) {
		impl.setTooltip(aTooltip);
	}

	@Override
	public void setTooltipCaption(ResKey aTooltip) {
		impl.setTooltipCaption(aTooltip);
	}

	@Override
	public void setAltText(ResKey anAltText) {
		impl.setAltText(anAltText);
	}

	@Override
	public void setVisible(boolean visible) {
		impl.setVisible(visible);
	}

	@Override
	public boolean focus() {
		return impl.focus();
	}

	@Override
	public void setShowProgress(boolean value) {
		impl.setShowProgress(value);
	}

	@Override
	public boolean showProgress() {
		return impl.showProgress();
	}

}
