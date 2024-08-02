/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * Utility class for {@link PersonalConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PersonalConfigurationUtil {

	/**
	 * Default key to store homepage value.
	 * 
	 * @see #getHomepage(ValueProvider, MainLayout)
	 */
	static String HOMEPAGE = "homepage";

	static Homepage getHomepage(ValueProvider pc, MainLayout layout) throws ConfigurationException {
		String homepage = (String) pc.getValue(HOMEPAGE);
		if (homepage == null) {
			return null;
		}
		ConfigurationItem homepages = toHomepage(homepage);
		if (homepages instanceof Homepage) {
			/* Ticket #23311: Compatibility: There is currently exactly one homepage. */
			return (Homepage) homepages;
		}
		return ((Homepages) homepages).getPages().get(getKey(layout));
	}

	private static String getKey(MainLayout layout) {
		return layout.getLocation();
	}

	private static ConfigurationItem toHomepage(String homepageString) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = new MapBuilder<String, ConfigurationDescriptor>()
			.put("hp", TypedConfiguration.getConfigurationDescriptor(Homepage.class))
			.put("hps", TypedConfiguration.getConfigurationDescriptor(Homepages.class)).toMap();
		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors);
		reader.setSource(CharacterContents.newContent(homepageString));
		try {
			return reader.read();
		} catch (RuntimeException ex) {
			throw new ConfigurationException(I18NConstants.INVALID_HOMEPAGE__HOMEPAGE.fill(homepageString), HOMEPAGE,
				homepageString, ex);
		}
	}

	/**
	 * Sets the home page. That is the page displayed hen the user logs in.
	 * 
	 * @param layout
	 *        The {@link MainLayout} to set homepage for.
	 * @param homepage
	 *        The home page to show. May be <code>null</code>, to remove homepage for
	 *        {@link MainLayout}.
	 */
	static void setHomepage(ValueProvider pc, MainLayout layout, Homepage homepage) {
		String hpString = (String) pc.getValue(HOMEPAGE);
		Homepages pages;
		if (hpString == null) {
			if (homepage == null) {
				// No stored page, no homepage to store
				return;
			} else {
				pages = TypedConfiguration.newConfigItem(Homepages.class);
				pages.getPages().put(homepage.getMainLayout(), homepage);
			}
		} else {
			try {
				ConfigurationItem storedHp = toHomepage(hpString);
				if (storedHp instanceof Homepage) {
					/* Ticket #23311: Compatibility: There is currently exactly one homepage. */
					if (homepage == null) {
						// Remove stored homepage
						pages = null;
					} else {
						pages = TypedConfiguration.newConfigItem(Homepages.class);
						pages.getPages().put(homepage.getMainLayout(), homepage);
					}
				} else {
					pages = (Homepages) storedHp;
					if (homepage == null) {
						Homepage removed = pages.getPages().remove(getKey(layout));
						if (removed == null) {
							// No change.
							return;
						}
						if (pages.getPages().isEmpty()) {
							pages = null;
						}
					} else {
						pages.getPages().put(homepage.getMainLayout(), homepage);
					}
				}
			} catch (ConfigurationException ex) {
				// Invalid format.
				if (homepage == null) {
					pages = null;
				} else {
					pages = TypedConfiguration.newConfigItem(Homepages.class);
					pages.getPages().put(homepage.getMainLayout(), homepage);
				}
			}

		}

		storePages(pc, pages);
	}

	private static void storePages(ValueProvider pc, Homepages pages) {
		if (pages == null) {
			pc.setValue(HOMEPAGE, null);
			return;
		}
		try {
			StringWriter out = new StringWriter();
			try (ConfigurationWriter configWriter = new ConfigurationWriter(out)) {
				configWriter.write("hps", TypedConfiguration.getConfigurationDescriptor(Homepages.class), pages);
			}
			pc.setValue(HOMEPAGE, out.toString());
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
	}
}
