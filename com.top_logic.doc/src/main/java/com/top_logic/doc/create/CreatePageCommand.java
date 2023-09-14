/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.create;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.doc.model.Page;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Command} to create a new {@link Page}.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class CreatePageCommand implements Command {

	private static final String STRING_WHITESPACES = "\\s+";
	
	/** @see #getDialog() */
	private CreatePageDialog _dialog;

	/** @see #getSelectedPage() */
	private Page _selectedPage;

	/** @see #isChild() */
	private boolean _isChild;

	/** @see #getComponent() */
	private LayoutComponent _component;

	/**
	 * Creates an {@link CreatePageCommand}.
	 */
	public CreatePageCommand(LayoutComponent component, CreatePageDialog dialog, Page page, boolean isChild) {
		_component = component;
		_dialog = dialog;
		_selectedPage = page;
		_isChild = isChild;
	}

	/**
	 * The {@link FormContext} of the dialog.
	 */
	protected FormContext getFormContext() {
		return getDialog().getFormContext();
	}
	

	/**
	 * The {@link LayoutComponent} of the dialog.
	 */
	protected final LayoutComponent getComponent() {
		return (_component);
	}

	/**
	 * The dialog containing the title and helpid field to create a new {@link Page}.
	 */
	protected final CreatePageDialog getDialog() {
		return _dialog;
	}

	/** The currently selected {@link Page} of the {@link LayoutComponent} */
	protected final Page getSelectedPage() {
		return _selectedPage;
	}

	/**
	 * <code>true</code> if the new {@link Page} shall be created as a child of
	 * {@link #_selectedPage}. When <code>false</code> the new {@link Page} will be created as the
	 * next sibling of {@link #_selectedPage}
	 */
	protected final boolean isChild() {
		return _isChild;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		FormField titleField = getFormContext().getField(CreatePageDialog.PAGE_TITLE);
		FormField helpIdField = getFormContext().getField(CreatePageDialog.HELP_ID);
		if (titleField instanceof StringField && helpIdField instanceof StringField) {
			String title = ((StringField) titleField).getAsString();
			String helpId = ((StringField) helpIdField).getAsString();
			Page newPage = createPage(helpId, title);
			// Ensure that components have received the model
			// created event, before the new object is used in
			// explicit communication with the components.
			// Otherwise, selection would not work, if the new
			// object was not included into component models.
			// See AbstractCreateCommandHandler#afterCommit().
			getComponent().getMainLayout().processGlobalEvents();
			((Selectable) getComponent()).setSelected(newPage);
			EditMode editMode = (EditMode) CollectionUtil.getFirst(getComponent().getSlaves());
			editMode.setEditMode();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates a new {@link Page} in the {@link #_component}.
	 * 
	 * @param helpId
	 *        Helpid of the new {@link Page}.
	 * @param title
	 *        Title of the new {@link Page}.
	 * 
	 * @return Newly created {@link Page}.
	 */
	private Page createPage(String helpId, String title) {
		return KBUtils.inTransaction(() -> createPageInTransaction(helpId, title));
	}

	/**
	 * Creates a {@link Page} in the database.
	 * 
	 * @param helpId
	 *        Helpid of the new {@link Page}.
	 * @param title
	 *        Title of the new {@link Page}
	 * 
	 * @return Newly created {@link Page}.
	 */
	private Page createPageInTransaction(String helpId, String title) {
		Page selected = getSelectedPage();
		if (helpId.isEmpty()) {
			helpId = toCamelCase(title);
		}
		Page newPage;
		if(isChild()) {
			newPage = (Page) selected.createChild(helpId.strip(), Page.PAGE_TYPE);
		} else {
			Page parent = selected.getParent();
			newPage = (Page) parent.createChild(helpId.strip(), Page.PAGE_TYPE);
			Page nextSibling = getNextSiblingOfPage(selected);
			if (nextSibling != newPage) {
				parent.move(newPage, nextSibling);
			}
		}
		newPage.setTitle(ResKey.text(title.strip()));
		newPage.setImportSource(selected.getImportSource());
		return newPage;
	}

	/**
	 * The next sibling of the given {@link Page}.
	 * 
	 * @param page
	 *        The {@link Page} whose next sibling is wanted.
	 * @return Next sibling {@link Page}. <code>null</code> if the given {@link Page} is the last
	 *         element of the children of its parent.
	 */
	private Page getNextSiblingOfPage(Page page) {
		List<StructuredElement> children = page.getParent().getChildrenModifiable();
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == page) {
				return (Page) children.get(i + 1);
			}
		}
		return null;
	}

	/**
	 * Converts a given string into camel case string.
	 * 
	 * @param string
	 *        A string with spaces to convert to a camel case string.
	 * @return The camel case string
	 */
	static String toCamelCase(String string) {
		String[] parts = string.split(STRING_WHITESPACES);
		String camelCaseString = "";
		for (String part : parts) {
			camelCaseString = camelCaseString + toProperCase(part);
		}
		if (camelCaseString.length() > 0) {
			camelCaseString = camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1);
		}
		return camelCaseString;
	}

	/**
	 * Sets the first character of the given string to upper case and the rest to lower case.
	 * 
	 * @param string
	 *        String to convert.
	 * @return The proper case string.
	 */
	static String toProperCase(String string) {
		return string.substring(0, 1).toUpperCase() +
			string.substring(1).toLowerCase();
	}

}
