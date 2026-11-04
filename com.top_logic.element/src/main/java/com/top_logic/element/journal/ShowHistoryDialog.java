/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.journal;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.model.ObjectTableModel;

/**
 * Dialog displaying the message journal of an object in a table.
 * 
 * @implNote Subclasses define the journal entries to display by overriding
 *           {@link #getJournalIdentifier()}, {@link #getJournalAttribute()},
 *           {@link #getJournalAttributeType()}, {@link #getJournalCause()}, and
 *           {@link #getColumns()}.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>Bill Tenz</a>
 */
public class ShowHistoryDialog extends FormComponent {

	/** Name of the table field displaying the journal. */
	public static final String JOURNAL_FIELD         = "journal";
	
	public ShowHistoryDialog(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = 
			new FormContext("root", ResPrefix.legacyPackage(ShowHistoryDialog.class));
		
		TableField journalField = FormFactory.newTableField(JOURNAL_FIELD);
		journalField.setSelectable(false);
		formContext.addMember(journalField);
		
		try {
			List theJournal = this.getJournalEntries();
			
			ObjectTableModel journalTable = 
				new ObjectTableModel(this.getColumns(), getTableConfiguration(JOURNAL_FIELD), theJournal);
			journalField.setTableModel(journalTable);
		} catch (SQLException ex) {
			Logger.error("Retreiving attribute journal failed.", ex, this);
		}
		
		return formContext;
	}

	private final List getJournalEntries() throws SQLException {
		return JournalManager.getInstance().getMessageJournal(
				getJournalIdentifier(),
				getJournalAttribute(), 
				getJournalAttributeType(),
				getJournalCause());
	}
	
	/**
	 * Identifier of the object whose journal is displayed.
	 */
	protected TLID getJournalIdentifier() {
		return null;
	}
	
	/**
	 * Name of the attribute whose journal is displayed.
	 */
	protected String getJournalAttribute() {
		return null;
	}
	
	/**
	 * Type of the attribute whose journal is displayed.
	 */
	protected String getJournalAttributeType() {
		return null;
	}
	
	/**
	 * The cause of the journal entries to display.
	 */
	protected String getJournalCause() {
		return null;
	}
	
	/**
	 * The columns of the journal table.
	 * 
	 * @see MessageJournalAccessor
	 */
	protected String[] getColumns() {
	    return null;
	}
	
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return true;
	}
	
	@Override
	protected void becomingInvisible() {
		// Make sure, the context is rebuilt upon opening the dialog again.
		this.setModel(null);
		
		removeFormContext();
		
		super.becomingInvisible();
	}

}
