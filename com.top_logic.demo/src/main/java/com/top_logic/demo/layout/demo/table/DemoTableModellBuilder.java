/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.Arrays;
import java.util.Collection;

import com.top_logic.demo.layout.form.demo.model.DemoPerson;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} creating some fixed {@link DemoPerson}s.
 * 
 * @author dna
 */
public class DemoTableModellBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link DemoTableModellBuilder} instance.
	 */
	public static final DemoTableModellBuilder INSTANCE = new DemoTableModellBuilder();

	private DemoTableModellBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		return Arrays.asList(
            new DemoPerson(""   , "Majunke" , "Tim", "Build/Ruby","single","male", "unknown", "00000 Unknown City, Unknown Street",
                    "Dies ist ain besöndärs < Übler Comment, bit allerlei wüst' \\ und / gebläk\" Ob dies & Das &amp; Sonstwas alle geht?"),
            new DemoPerson(""   , "Wittmann", "Thorsten", "","","", "", "", ""),
            new DemoPerson(""   , "Sattler" , "Theo", "EWE","single","male", "unknown", "00000 Unknown City, Unknown Street", ""),
            new DemoPerson(""   , "Halfmann", "Klaus", "Admin","married","male", "unknown", "00000 Unknown City, Unknown Street", 
                            "Zustimmung zur Würgkbetriebsaufnahme wird am 05.06.2204 erwartet, Gesamtstatus gelb wegen kommitteter Zackweg-Verschiebung auf Ende Juni 2217"),
            new DemoPerson("Dr.", "Mausz"   , "Frank", "Coms" ,"single","male", "unknown", "00000 Unknown City, Unknown Street", 
                            "kolportierte WBA-Versiebung auf Ende Juni 2009\n"
                         +  "(im Vorbericht falscher Quantensprung!)"),
            new DemoPerson("Dr.", "Haummacher", "Bernhard", "TopLogic" ,"married","male", "unknown", "00000 Unknown City, Unknown Street", 
                            "Dieser\nComment\nhat\nviele\neue\nZeilen\nmacht\ndas\nwas\naus?"),
            new DemoPerson("", "Gänsler", "Michael", "EWE","married","male", "unknown", "00000 Unknown City, Unknown Street", 
                            "Realisierung der Stufe 44 wurde mit Abschluss der Phase GURK qualitätsgesichert ausgesetzt.\n"
                          + "Die Reaquireae der Realisierung erfolgt im Sept. 08, der BzSIT ist zum Verladedatum 02/2009 geplant. "
                          + "Der Start der Migration „Format C:\\“ ist für Juni 09 vorgesehen.\n"
                          + "Die ZZK-Vorlage „Continue Realization“ wurde im PMG-Brett am 19.06.verschlossen.\n"
                          + "Mit der Vorlage wird die Verschiebung der Stufe 44 in die Container und der damit\n"
                          + "Überhang 2008/2009 überaus verlustfrei dokumentiert."),
            new DemoPerson("", "Weinlich", "Heike", "","single","female", "unknown", "00000 Unknown City, Unknown Street", ""),
            new DemoPerson("", "Bauer", "Thomas", "","single","male", "unknown", "00000 Unknown City, Unknown Street", ""),
            new DemoPerson("Dr.", "Borchard", "Holger", "","","", "", "", ""),
            new DemoPerson("", "Breitling", "Markus", "","","", "", "", ""),
            new DemoPerson("", "Connotte", "Jörg", "","","", "", "", ""),
            new DemoPerson("", "Dickhut", "Thomas", "","","", "", "", ""),
            new DemoPerson("", "Dilger", "Peter", "","","", "", "", ""),
            new DemoPerson("", "Dostert", "Guido", "","","", "", "", ""),
            new DemoPerson("", "Geppert", "Ingo", "","","", "", "", ""),
            new DemoPerson("Dr.", "Grill", "Thomas", "","","", "", "", 
                            "Projekt wieder im Plan!! Noch einige Verzögerungen, aber jetzt alles unter Kontrolle!"),
            new DemoPerson("", "Hiller", "Jochen", "","","", "", "", ""),
            new DemoPerson("", "Köhlhoff", "Dirk", "","","", "", "", ""),
            new DemoPerson("", "Kogge", "Dierk", "EWE","married","", "", "", ""),
            new DemoPerson("", "Kras", "Sylwester", "EWE","","", "", "", ""),
            new DemoPerson("", "Lehmann", "Randolf", "-blown away-","","", "", "", ""),
            new DemoPerson("", "Richter", "Thomas", "","single","male", "", "", 
                            "Die aktuelle Planung sieht eine WBA der SSt AL-PS - SMILE zeitgleich mit der WBA SMILE 6.500 (NGN B1a) zum 23.06.08 vor. Mit der Feinplanung zum Start der Migration „Löschen EG“ im Juli 08 wurde begonnen. Voraussetzungen : GetWell-Phase CRM-T 1.000ff abgeschlossen, Erweiterung der FnP für EG und SSt AL-PS – SMILE im Wirkbetrieb. Die Risiken und Abhängigkeiten bestehen aber kurz vor WB-Termin unverändert – die Eintrittswahrscheinlichkeit einer Terminverschiebung steigt."),
            new DemoPerson("Dr.", "Schwarber", "Olaf", "EWE","","", "", "", "")
		);
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object anObject) {
		return null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent component, Object anObject) {
		return anObject instanceof DemoPerson;
	}



}
