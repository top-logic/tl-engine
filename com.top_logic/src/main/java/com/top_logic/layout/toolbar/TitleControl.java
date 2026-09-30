/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.toolbar;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.structure.TitleChangedListener;
import com.top_logic.layout.structure.WindowModel;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link Control} rendering the title of a title model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TitleControl extends ConstantControl<HTMLFragment> implements TitleChangedListener {

	private PropertyObservable _titleModel;

	/**
	 * Creates a new {@link TitleControl}.
	 * 
	 * @param titleModel
	 *        The model where the given title was got from. This control attaches on that model as
	 *        {@link TitleChangedListener} for the {@link WindowModel#TITLE_PROPERTY title event}
	 *        type.
	 * @param title
	 *        The initial title rendered by this control.
	 */
	public TitleControl(PropertyObservable titleModel, HTMLFragment title) {
		super(title);
		_titleModel = titleModel;
	}

	@Override
	protected void internalAttach() {
		_titleModel.addListener(WindowModel.TITLE_PROPERTY, this);
		super.internalAttach();
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();
		_titleModel.removeListener(WindowModel.TITLE_PROPERTY, this);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			getModel().write(context, out);
		}
		out.endTag(HTMLConstants.SPAN);
	}

	@Override
	protected String getTypeCssClass() {
		return "tl-title";
	}

	@Override
	public void handleTitleChanged(Object sender, HTMLFragment oldTitle, HTMLFragment newTitle) {
		setModel(newTitle);
	}

}
