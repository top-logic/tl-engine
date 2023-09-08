/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.demo.layout.form.demo.model.DemoPerson;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A simple model builder for large tables
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class DemoLargeTableModelBuilder implements ListModelBuilder {

	private final static String[] femaleForenames = {"Brigitte", "Heike", "Steffi", "Gabriela",
												     "Sabine", "Barbara", "Claudia", "Maria",
												     "Sandra", "Monika"};
	private final static String[] maleForenames = {"Heinz", "Herbert", "Dirk", "Steffen",
												   "Siegfried", "Andreas", "Erik", "Christian",
												   "Matthias", "Alexander"};
	private final static String[] surNames = {"Schmidt", "Krause", "Schubert", "Klein",
											  "Rose", "Schill", "Vogler", "Ganter",
											  "Brunner", "Lehner", "H‰rtig", "Aﬂmann",
											  "Baader", "Spallek", "Nagel", "Hochberger",
											  "Pfitzmann", "Petersohn", "Flach", "Meiﬂner"};

	/**
	 * Singleton {@link DemoLargeTableModelBuilder} instance.
	 */
	public static final DemoLargeTableModelBuilder INSTANCE = new DemoLargeTableModelBuilder();

	private DemoLargeTableModelBuilder() {
		// Singleton constructor.
	}

	private List<DemoPerson> init() {
		int rowCount = 100000;
		List<DemoPerson> demoPersons = new ArrayList<>(rowCount);
		
		for(int i = 0; i < rowCount; i++) {
			float randomNumber = (float)Math.random();
			int surNameIndex = Math.round(randomNumber * 19);
			String surName = surNames[surNameIndex];
			String sex = randomNumber > 0.5 ? "male" : "female";
			int foreNameIndex = Math.round(randomNumber * 9);
			String foreName = "";
			if(sex.equals("male")) {
				foreName = maleForenames[foreNameIndex];
			}
			else {
				foreName = femaleForenames[foreNameIndex];
			}
			DemoPerson person = new DemoPerson(Integer.toString(i),
											   surName, foreName, "TL","", sex, "", "", "");
			demoPersons.add(person);
		}

		return demoPersons;
	}
	
	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent,
			Object anObject) {
		return null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent,
			Object anObject) {
		return anObject instanceof DemoPerson;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return init();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}
