/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.onnx;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.index.embed.AbstractEmbedder;
import com.top_logic.tl.ai.index.lucene.Vectors;

/**
 * The in-process model finds the English translation of a German sentence: the property the whole
 * meaning side of the index rests on.
 *
 * <p>
 * The 30 pairs are the scout's (consulting and engineering topics); both default models matched 30 of
 * 30 there. Behind {@link OnnxModelE2e}, because the model is not part of the build.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestOnnxEmbedder extends TestCase {

	private static final String[] DE = {
		"Die Grundplatte muss aus eloxiertem Aluminium gefertigt werden.",
		"Der Kunde wünscht eine Exportfunktion nach Excel für alle Berichte.",
		"Das Projektteam trifft sich jeden Dienstag zur Abstimmung der offenen Punkte.",
		"Die Lieferzeit für Sonderanfertigungen beträgt derzeit zwölf Wochen.",
		"Im Interview nannte die Leiterin der Fertigung fehlende Ersatzteile als größtes Risiko.",
		"Die Montage der Halterung erfolgt mit vier Schrauben M6.",
		"Der Datenschutzbeauftragte verlangt, dass personenbezogene Daten in der EU bleiben.",
		"Die bestehende Software wird seit zehn Jahren nicht mehr gewartet.",
		"Für die Migration der Altdaten ist ein eigenes Teilprojekt geplant.",
		"Die Anwender beklagen, dass die Suche zu viele irrelevante Treffer liefert.",
		"Das Budget für das erste Jahr ist auf zweihunderttausend Euro begrenzt.",
		"Die Freigabe einer Zeichnung erfordert zwei Unterschriften.",
		"Der Vertrieb möchte Angebote direkt aus dem Teilekatalog erzeugen.",
		"Die Prüfung der Schweißnähte erfolgt durch einen externen Gutachter.",
		"Wir haben vereinbart, die Schnittstelle zum ERP-System zuerst umzusetzen.",
		"Der Kaffeeautomat im dritten Stock ist seit Montag defekt.",
		"Die Abteilung Einkauf bestellt Normteile nur bei zwei Lieferanten.",
		"Das neue System soll auch auf Tablets in der Werkhalle laufen.",
		"Die Schulung der Mitarbeiter ist für das zweite Quartal vorgesehen.",
		"Die Toleranz der Bohrung beträgt plus minus fünf Hundertstel Millimeter.",
		"Ein Mitarbeiter schlug vor, die Stückliste automatisch zu prüfen.",
		"Die Geschäftsführung hat das Projekt im September genehmigt.",
		"Die Konstruktionsabteilung arbeitet mit zwei verschiedenen CAD-Systemen.",
		"Die Dokumentation der Prozesse liegt nur auf Papier vor.",
		"Der Wartungsvertrag läuft Ende des Jahres aus.",
		"Kunden reklamieren häufig falsch gelieferte Varianten.",
		"Die Rechte der Benutzer sollen pro Projekt vergeben werden.",
		"Das Lager hat keinen Überblick über die Bestände der Außenlager.",
		"Die Abnahme des Systems erfolgt nach einem vierwöchigen Testbetrieb.",
		"Der Betriebsrat muss der Einführung des Systems zustimmen."
	};

	private static final String[] EN = {
		"The base plate must be made of anodised aluminium.",
		"The customer wants an export to Excel for all reports.",
		"The project team meets every Tuesday to align on the open items.",
		"The delivery time for custom-made parts is currently twelve weeks.",
		"In the interview, the head of production named missing spare parts as the biggest risk.",
		"The bracket is mounted with four M6 screws.",
		"The data protection officer requires that personal data stays in the EU.",
		"The existing software has not been maintained for ten years.",
		"A separate subproject is planned for migrating the legacy data.",
		"Users complain that the search returns too many irrelevant hits.",
		"The budget for the first year is limited to two hundred thousand euros.",
		"Releasing a drawing requires two signatures.",
		"Sales wants to create quotes directly from the parts catalogue.",
		"The welds are inspected by an external expert.",
		"We agreed to implement the interface to the ERP system first.",
		"The coffee machine on the third floor has been broken since Monday.",
		"The purchasing department orders standard parts from only two suppliers.",
		"The new system should also run on tablets on the shop floor.",
		"Staff training is planned for the second quarter.",
		"The tolerance of the bore is plus or minus five hundredths of a millimetre.",
		"An employee suggested checking the bill of materials automatically.",
		"Management approved the project in September.",
		"The design department works with two different CAD systems.",
		"The process documentation exists only on paper.",
		"The maintenance contract expires at the end of the year.",
		"Customers often complain about wrongly delivered variants.",
		"User rights should be assigned per project.",
		"The warehouse has no overview of the stock in the external warehouses.",
		"The system is accepted after a four-week trial operation.",
		"The works council must agree to the introduction of the system."
	};

	/** At least 28 of 30 German sentences have their English translation as nearest passage. */
	public void testGermanFindsEnglish() throws Exception {
		OnnxModelE2e.assumeEnabled();
		try (OnnxEmbedder embedder = embedder()) {
			List<float[]> english = embedder.embedPassages(List.of(EN));
			int top1 = 0;
			for (int i = 0; i < DE.length; i++) {
				float[] query = embedder.embedQuery(DE[i]);
				int best = -1;
				float bestScore = -2;
				for (int j = 0; j < english.size(); j++) {
					float score = Vectors.dot(query, english.get(j));
					if (score > bestScore) {
						bestScore = score;
						best = j;
					}
				}
				if (best == i) {
					top1++;
				}
			}
			System.out.println("German to English, top 1: " + top1 + " of " + DE.length + " (" + embedder.modelId() + ")");
			assertTrue("Top 1 was " + top1 + " of " + DE.length, top1 >= 28);
		}
	}

	/** A text embedded in a batch, padded to a longer neighbour, gets the vector it gets alone. */
	public void testBatchEqualsSingle() throws Exception {
		OnnxModelE2e.assumeEnabled();
		try (OnnxEmbedder embedder = embedder()) {
			List<String> texts = new ArrayList<>(List.of(EN));
			List<float[]> batch = embedder.embedPassages(texts);
			for (int i = 0; i < 3; i++) {
				float[] single = embedder.embedPassages(List.of(texts.get(i))).get(0);
				assertEquals(1.0, Vectors.dot(single, batch.get(i)), 1e-3);
			}
		}
	}

	/** A model directory without the files is refused when the embedder is created. */
	public void testMissingModelIsRefused() throws Exception {
		Path empty = Files.createTempDirectory("tl-ai-index-onnx-empty");
		try {
			new OnnxEmbedder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				config(empty.toString(), "model.onnx", "CLS", 8, "", ""));
			fail("A model directory without a model must be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("model.onnx"));
		} finally {
			Files.delete(empty);
		}
	}

	/**
	 * A native library that does not load on the loader thread - ONNX Runtime on a musl image, say -
	 * reaches the thread that creates the embedder.
	 */
	public void testNativeLibraryThatDoesNotLoad() throws Exception {
		UnsatisfiedLinkError failure = new UnsatisfiedLinkError("libonnxruntime.so: Error loading shared library");
		try {
			OnnxEmbedder.onLoaderThread(() -> {
				throw failure;
			});
			fail("The failure of the loader thread must reach the caller.");
		} catch (UnsatisfiedLinkError ex) {
			assertSame(failure, ex);
		}
	}

	private static OnnxEmbedder embedder() {
		String dir = System.getProperty("tl.ai.index.model.dir");
		assertNotNull("Name the model directory with -Dtl.ai.index.model.dir=...", dir);
		return new OnnxEmbedder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			config(dir, System.getProperty("tl.ai.index.model.file", "model_int8.onnx"),
				System.getProperty("tl.ai.index.model.pooling", "CLS"),
				Integer.getInteger("tl.ai.index.model.dimensions", 768),
				System.getProperty("tl.ai.index.model.query-prefix", "query: "),
				System.getProperty("tl.ai.index.model.passage-prefix", "")));
	}

	@SuppressWarnings("unchecked")
	private static OnnxEmbedder.Config<OnnxEmbedder> config(String dir, String file, String pooling, int dimensions,
			String queryPrefix, String passagePrefix) {
		OnnxEmbedder.Config<OnnxEmbedder> config = TypedConfiguration.newConfigItem(OnnxEmbedder.Config.class);
		config.update(config.descriptor().getProperty(OnnxEmbedder.Config.MODEL_DIR), dir);
		config.update(config.descriptor().getProperty(OnnxEmbedder.Config.MODEL_FILE), file);
		config.update(config.descriptor().getProperty(OnnxEmbedder.Config.POOLING),
			OnnxEmbedder.Pooling.valueOf(pooling));
		config.update(config.descriptor().getProperty(AbstractEmbedder.Config.DIMENSIONS), dimensions);
		config.update(config.descriptor().getProperty(AbstractEmbedder.Config.QUERY_PREFIX), queryPrefix);
		config.update(config.descriptor().getProperty(AbstractEmbedder.Config.PASSAGE_PREFIX), passagePrefix);
		config.update(config.descriptor().getProperty(AbstractEmbedder.Config.BATCH_SIZE), 16);
		return config;
	}

	/** The alias manager resolves the model directory. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.withThreadContext(new TestSuite(TestOnnxEmbedder.class)));
	}

}
