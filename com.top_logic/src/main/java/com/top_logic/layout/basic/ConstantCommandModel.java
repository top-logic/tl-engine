/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.check.CheckScope;

/**
 * Immutable {@link CommandModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConstantCommandModel extends PropertyObservableBase implements CommandModel {

	@Override
	public final void setCssClasses(String newCssClasses) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setExecutable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNotExecutable(ResKey disabledReason) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setImage(ThemeImage image) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final String setLabel(String label) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setNotExecutableImage(ThemeImage image) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setActiveImage(ThemeImage image) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setTooltip(String aTooltip) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setTooltipCaption(String aTooltip) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setAltText(String anAltText) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void setVisible(boolean visible) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T> T set(Property<T> property, T value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T> T reset(Property<T> property) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isSet(Property<?> property) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isExecutable() {
		return true;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public String getCssClasses() {
		return null;
	}
	
	@Override
	public String getLabel() {
		return null;
	}
	
	@Override
	public ThemeImage getImage() {
		return null;
	}
	
	@Override
	public ThemeImage getNotExecutableImage() {
		return null;
	}
	
	@Override
	public ThemeImage getActiveImage() {
		return null;
	}

	@Override
	public ResKey getNotExecutableReasonKey() {
		return null;
	}
	
	@Override
	public <T> T get(Property<T> property) {
		return property.getDefaultValue(this);
	}
	
	@Override
	public boolean showProgress() {
		return false;
	}

	@Override
	public void setShowProgress(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTooltip() {
		return null;
	}
	
	@Override
	public String getTooltipCaption() {
		return null;
	}

	@Override
	public CheckScope getCheckScope() {
		return null;
	}
	
	@Override
	public String getAltText() {
		return null;
	}

	@Override
	public char getAccessKey() {
		return 0;
	}

	@Override
	public void setAccessKey(char accessKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean focus() {
		if (FocusHandling.focus(DefaultDisplayContext.getDisplayContext(), this)) {
			notifyListeners(FOCUS_PROPERTY, this, Boolean.FALSE, Boolean.TRUE);
		}
		return false;
	}

}
