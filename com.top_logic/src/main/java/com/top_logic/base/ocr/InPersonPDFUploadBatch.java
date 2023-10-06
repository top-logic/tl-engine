/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.ocr;

import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;


/**
 * {@link PDFUploadBatch} that executes the upload in a new context for a given person.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InPersonPDFUploadBatch extends PDFUploadBatch {

	final PersonManager _personManager;

	final String _personName;

	public InPersonPDFUploadBatch(String personName) {
		super("PDFUploadBatch for " + personName);
		_personName = personName;
		_personManager = PersonManager.getManager();
	}

	@Override
	protected void runInContext() {
		TLContextManager.inPersonContext(fetchPerson(), new InContext() {
			
			@Override
			public void inContext() {
				ocrContext = TLContext.getContext();
				InPersonPDFUploadBatch.this.executeJob();
			}
		});
	}

	private Person fetchPerson() {
		return ThreadContextManager.inSystemInteraction(InPersonPDFUploadBatch.class, new Computation<Person>() {

			@Override
			public Person run() {
				return Person.byName(_personName);
			}
		});
	}

}

