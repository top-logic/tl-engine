/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.config.AttributedTypeConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelConfigLoader;
import com.top_logic.model.config.EnumConfig;
import com.top_logic.model.config.EnumConfig.ClassifierConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.util.model.ModelService;

/**
 * Every type and attribute of this application's data model has a label, in both languages.
 *
 * <p>
 * A model part is labelled by a resource named after it. Nothing declares that resource - the model
 * file and the resource bundle are written independently - so a part added without its line of text
 * is a defect that starts the application, passes every other test and is found by somebody looking
 * at a screen. What that somebody sees is one of two things, depending on which key the view asks
 * for: the bracketed key, <code>[model.tl.ai.core.Skill.tools]</code>, with an error in the log; or,
 * where {@link com.top_logic.model.util.TLModelNamingConvention} falls back to the identifier, the
 * attribute's Java name in English - a German form field labelled "Owner" among translated ones.
 * The second is the worse of the two, because nothing about it looks broken.
 * </p>
 *
 * <p>
 * The same happens to a whole module at once when its bundle is registered nowhere: the file exists,
 * the keys are in it, and the application never reads it. This test asks the running
 * {@link ResourcesModule} rather than the files, so both failures look the same to it.
 * </p>
 *
 * <p>
 * The keys come from the model as the application loads it - {@link ModelConfigLoader} over the
 * declarations of the {@link ModelService} - so a type nobody declares is out of scope, and a newly
 * declared one is in scope the moment it is added. Of what is declared, the check holds the modules
 * this project ships, named {@value #OWN_MODULE_PREFIX}*. The two fixtures - "demo.events", the
 * event-management domain of the demo, and the "pm" model of the security test bed - are not held to
 * it: they furnish a demonstration, not a product.
 * </p>
 *
 * @see #KNOWN_GAPS
 */
public class TestModelResources extends BasicTestCase {

	/** The name prefix of the model modules this project ships. */
	private static final String OWN_MODULE_PREFIX = "tl.ai.";

	/** The prefix of every resource key that names a model part. */
	private static final String MODEL_PREFIX = "model.";

	/**
	 * What the i18n generator writes when it reads a key the application cannot resolve.
	 *
	 * <p>
	 * A regeneration round-trips those placeholders back into the bundle file, which turns a missing
	 * key into a present one holding the text "[missing:...]" - that is how the unregistered bundle
	 * of issue #229 stayed invisible. A lookup finds such a key, so the check has to reject the
	 * value as well as the absence.
	 * </p>
	 */
	private static final String GENERATOR_PLACEHOLDER = "[missing:";

	/** A key of a type that does not exist, for the positive control. */
	private static final String UNDEFINED_KEY = MODEL_PREFIX + OWN_MODULE_PREFIX + "core.NoSuchType";

	/** The languages the application ships. */
	private static final List<Locale> LANGUAGES = List.of(Locale.ENGLISH, Locale.GERMAN);

	/**
	 * The modules this test does not hold to the rule yet, and why.
	 *
	 * <p>
	 * The set is empty: every module named {@value #OWN_MODULE_PREFIX}* is labelled in both
	 * languages. It is here for the gap that cannot be closed in the change that finds it - parking
	 * the module with the ticket that closes it keeps the check running over everything else,
	 * instead of the check being switched off or left red.
	 * </p>
	 *
	 * <p>
	 * An entry here is a debt, not a licence: a module belongs in this set only while a ticket is
	 * open for it, and leaves it with the fix.
	 * </p>
	 */
	private static final Set<String> KNOWN_GAPS = Set.of();

	/** Every type and attribute of this project's model has a text of its own in both languages. */
	public void testEveryModelPartIsLabelled() throws ConfigurationException {
		List<String> missing = new ArrayList<>();
		for (ModuleConfig module : ownModules()) {
			if (KNOWN_GAPS.contains(module.getName())) {
				continue;
			}
			for (Locale language : LANGUAGES) {
				for (String key : labelKeys(module)) {
					String text = label(language, key);
					if (text == null) {
						missing.add(language.getLanguage() + ": " + key);
					} else if (text.startsWith(GENERATOR_PLACEHOLDER)) {
						missing.add(language.getLanguage() + ": " + key + " = " + text);
					}
				}
			}
		}

		assertEquals("Wherever the type or attribute is shown, the user interface renders the bracketed"
			+ " key or the English identifier instead of a label. Write the text into"
			+ " WEB-INF/conf/resources/model.<module>.messages_<language>.properties, and check that the"
			+ " bundle is registered with the ResourcesModule:\n  " + String.join("\n  ", missing),
			List.of(), missing);
	}

	/**
	 * The positive control: a key nothing defines really has no text.
	 *
	 * <p>
	 * Without it a lookup that answers every key - with a fallback text, or with the key itself -
	 * would let the check above pass while proving nothing.
	 * </p>
	 */
	public void testAnUndefinedKeyHasNoText() {
		for (Locale language : LANGUAGES) {
			assertNull("'" + UNDEFINED_KEY + "' must have no text in '" + language + "'.",
				label(language, UNDEFINED_KEY));
		}
	}

	/**
	 * Precondition: the model this test inspects was found.
	 *
	 * <p>
	 * A renamed module prefix or a model the service no longer declares would turn the check above
	 * into one that inspects nothing and passes.
	 * </p>
	 */
	public void testTheModelIsThere() throws ConfigurationException {
		Collection<ModuleConfig> modules = ownModules();
		assertTrue("Only " + modules.size() + " modules named '" + OWN_MODULE_PREFIX + "*' are declared."
			+ " Either the model moved or the ModelService no longer declares it.", modules.size() >= 5);

		int keys = 0;
		for (ModuleConfig module : modules) {
			keys += labelKeys(module).size();
		}
		assertTrue("Only " + keys + " label keys derived from the model; this test would check almost"
			+ " nothing.", keys > 100);
	}

	/** The resource keys that name the module, its types and their parts. */
	private static Set<String> labelKeys(ModuleConfig module) {
		Set<String> result = new TreeSet<>();
		String modulePrefix = MODEL_PREFIX + module.getName();
		result.add(modulePrefix);
		for (TypeConfig type : module.getTypes()) {
			String typePrefix = modulePrefix + '.' + type.getName();
			result.add(typePrefix);
			if (type instanceof AttributedTypeConfig attributed) {
				for (PartConfig part : attributed.getAttributes()) {
					result.add(typePrefix + '.' + part.getName());
				}
			}
			if (type instanceof EnumConfig enumeration) {
				for (ClassifierConfig classifier : enumeration.getClassifiers()) {
					result.add(typePrefix + '.' + classifier.getName());
				}
			}
		}
		return result;
	}

	/**
	 * The text of one resource in one language, without the fallback to another language.
	 *
	 * @return The text, or <code>null</code> if that language does not have it - which is exactly
	 *         what the fallback would hide.
	 */
	private static String label(Locale language, String key) {
		I18NBundleSPI bundle = (I18NBundleSPI) ResourcesModule.getInstance().getBundle(language);
		return bundle.lookup(key, false);
	}

	/** The model modules of this project, as the application declares them. */
	private static Collection<ModuleConfig> ownModules() throws ConfigurationException {
		List<ModuleConfig> result = new ArrayList<>();
		for (ModuleConfig module : declaredModel().getModules()) {
			if (module.getName().startsWith(OWN_MODULE_PREFIX)) {
				result.add(module);
			}
		}
		return result;
	}

	/** The data model of the application, combined from every declaration of the model service. */
	private static ModelConfig declaredModel() throws ConfigurationException {
		DynamicModelService.Config<?> serviceConfig = (DynamicModelService.Config<?>) ApplicationConfig
			.getInstance().getServiceConfiguration(ModelService.class);
		InstantiationContext context = new DefaultInstantiationContext(TestModelResources.class);
		ModelConfig result = new ModelConfigLoader().load(context, serviceConfig);
		context.checkErrors();
		return result;
	}

	/**
	 * The resource lookup needs the running {@link ResourcesModule}, and loading the model needs
	 * {@link Settings}; the application configuration both read from is what the
	 * {@link ModuleTestSetup} provides.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestModelResources.class,
			ResourcesModule.Module.INSTANCE, Settings.Module.INSTANCE));
	}

}
