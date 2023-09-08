/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.BlockControl;
/**
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockTag extends CompositeControlTag {

	private boolean initiallyVisible = true;
	
	public void setInitiallyVisible(boolean initiallyVisible) {
		this.initiallyVisible = initiallyVisible;
	}
	
	@Override
	protected Control createControl() {
		BlockControl result = new BlockControl();
		initCompositeControl(result);
		return result;
	}
	
	
	@Override
	protected void initCompositeControl(AbstractCompositeControl result) {
		super.initCompositeControl(result);
		((BlockControl) result).setVisible(initiallyVisible);
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		
		this.initiallyVisible = true;
	}
}
