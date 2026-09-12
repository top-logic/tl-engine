# `<switch>`-View-Element Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ein neues, deklaratives `<switch>`-Element für die `.view.xml`-Schicht, das abhängig von einem Kanalwert genau eine von mehreren konfigurierten Sichten anzeigt — und dessen Nutzung im Modell-Editor-Details-Panel.

**Architecture:** `SwitchElement` (`@TagName("switch")`) im Modul `com.top_logic.layout.view` ist ein zustandsloser `UIElement`-Factory analog zu `TabBarElement`/`AdaptiveDetailElement`. Es hält eine geordnete Liste von `<case>`-Fällen (jeweils eine TL-Script-`Expr` plus Inhalt) und optional `<default>`. Zur Laufzeit erzeugt es ein `ReactDeckPaneControl` (zeigt genau ein Kind, lazy + gecacht) und registriert einen Listener am `input`-Kanal, der bei Wertänderung den ersten passenden Fall berechnet und per `selectChild(index)` umschaltet.

**Tech Stack:** Java, TopLogic TypedConfiguration (`@TagName`/`@DefaultContainer`/`@TreeProperty`), TL-Script (`QueryExecutor`/`Expr`), TopLogic React-View-Schicht (`ReactControl`/`ReactDeckPaneControl`/`ReactStackControl`), JUnit 3/4 (`TestCase` + `suite()`), Playwright (manuelle UI-Verifikation).

## Global Constraints

- Member-Variablen (Instanzfelder) beginnen mit Unterstrich `_` (kein `this.`).
- Bauen immer vom Projekt-Root mit `-pl <modul-dir>`; **kein** `-am`; nie in ein Modul-Verzeichnis `cd`-en.
- Tests: `-DskipTests=false`, `-Dtest=<voll qualifizierter Klassenname>` (einfacher Klassenname funktioniert nicht).
- `.view.xml`-Dateien verwenden **Tab**-Einrückung (bestehende Formatierung von `model-editor.view.xml` beibehalten).
- I18N: neue englische `messages_en.properties` werden generiert, nicht editiert. In diesem Plan entstehen **keine** neuen I18N-Keys (das `<switch>`/`<case>` trägt keine Labels).
- **Commits:** Vor jedem `git commit` zuerst Diff/Status zeigen und auf ausdrückliche Freigabe des Nutzers warten (globale Nutzer-Regel). Commit-Nachricht-Format: `Ticket #<nr>: <Beschreibung>.` — keine AI-Attribution.
- React-UI-Features werden in `com.top_logic.demo.react` mit Playwright verifiziert (nicht in `tl-demo`). Demo-Login: `root` / `root1234`.

---

## File Structure

- **Neu:** `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SwitchElement.java`
  — Element + `Config` (`@TagName("switch")`) + `CaseConfig` (`@TagName("case")`). Einzige Verantwortung: Fallauswahl über Kanalwert und Delegation an ein Deck-Control.
- **Neu:** `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-switch.view.xml`
  — Test-Fixture mit einem `<switch>` (ein `<case>` + `<default>`).
- **Neu:** `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestSwitchElement.java`
  — Parse- und Instanziierungs-Test (analog `TestFormElement`).
- **Geändert:** `com.top_logic.dev.tools/src/main/webapp/WEB-INF/views/admin/model/model-editor.view.xml`
  — Details-Panel-Inhalt (aktuell Zeilen ~410–415) durch ein `<switch input="selectedPart">` ersetzt.

---

## Task 1: `<switch>`-Element (Config, Implementierung, Parse-/Instanziierungs-Test)

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SwitchElement.java`
- Test (create): `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestSwitchElement.java`
- Test-Fixture (create): `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-switch.view.xml`

**Interfaces:**
- Consumes:
  - `UIElement` / `UIElement.Config`, `ViewContext#resolveChannel(ChannelRef)`, `ViewContext#childContext(String)`, `ViewContext#withChildSlotPath(String)`.
  - `ChannelRef` + `@Format(ChannelRefFormat.class)`, `ViewChannel` (`get()`, `addListener`, `removeListener`), `ViewChannel.ChannelListener`.
  - `QueryExecutor.compile(Expr)` und `QueryExecutor#execute(Object)` (Truthiness: `Boolean.TRUE.equals(...)`, siehe `VisibleIf`).
  - `ReactControl#addCleanupAction(Runnable)`, `ReactDeckPaneControl(ReactContext, List<ChildFactory>, int)` + `selectChild(int)`, `ReactDeckPaneControl.ChildFactory` (`ReactControl create()`), `ReactStackControl(ReactContext, List<? extends ReactControl>)`.
- Produces:
  - `SwitchElement implements UIElement` mit `createControl(ViewContext): IReactControl`.
  - `SwitchElement.Config extends UIElement.Config` (`@TagName("switch")`): `ChannelRef getInput()`, `List<CaseConfig> getCases()`, `List<PolymorphicConfiguration<? extends UIElement>> getDefault()`.
  - `SwitchElement.CaseConfig extends ConfigurationItem` (`@TagName("case")`): `Expr getTest()`, `List<PolymorphicConfiguration<? extends UIElement>> getContent()`.

- [ ] **Step 1: Test-Fixture anlegen**

Create `com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-switch.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="selectedPart"/>
  </channels>

  <switch input="selectedPart">
    <case test="p -> $p != null &amp;&amp; $p.instanceOf(`tl.model:TLClass`)">
      <form input="selectedPart">
        <field attribute="name"/>
      </form>
    </case>
    <default>
      <form input="selectedPart">
        <field attribute="name"/>
      </form>
    </default>
  </switch>
</view>
```

- [ ] **Step 2: Fehlschlagenden Test schreiben**

Create `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestSwitchElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.SwitchElement;

/**
 * Tests parsing and instantiation of {@link SwitchElement} and its {@code <case>} children.
 */
public class TestSwitchElement extends TestCase {

	private ViewElement.Config parseTestView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSwitchElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestSwitchElement.class, "test-switch.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/**
	 * Tests that a view XML with {@code <switch>}, {@code <case>} and {@code <default>} parses.
	 */
	public void testParseSwitchView() throws Exception {
		ViewElement.Config config = parseTestView();

		assertTrue("Content should be SwitchElement config",
			config.getContent() instanceof SwitchElement.Config);
		SwitchElement.Config switchConfig = (SwitchElement.Config) config.getContent();

		assertNotNull("Input should be set", switchConfig.getInput());
		assertEquals("Input channel name", "selectedPart", switchConfig.getInput().getChannelName());

		assertEquals("Should have one case", 1, switchConfig.getCases().size());
		SwitchElement.CaseConfig caseConfig = switchConfig.getCases().get(0);
		assertNotNull("Case test should be parsed", caseConfig.getTest());
		assertEquals("Case should have one content element", 1, caseConfig.getContent().size());
		PolymorphicConfiguration<? extends UIElement> caseContent = caseConfig.getContent().get(0);
		assertTrue("Case content should be FormElement config", caseContent instanceof FormElement.Config);

		assertEquals("Should have one default content element", 1, switchConfig.getDefault().size());
		assertTrue("Default content should be FormElement config",
			switchConfig.getDefault().get(0) instanceof FormElement.Config);
	}

	/**
	 * Tests that the parsed configuration instantiates into a UIElement tree (compiles the case
	 * test expression via {@link com.top_logic.model.search.expr.query.QueryExecutor}).
	 */
	public void testInstantiateSwitchElement() throws Exception {
		ViewElement.Config config = parseTestView();

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSwitchElement.class);
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestSwitchElement.class, TypeIndex.Module.INSTANCE);
	}
}
```

- [ ] **Step 3: Test ausführen, Fehlschlag verifizieren**

Run:
```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view \
  -Dtest=test.com.top_logic.layout.view.element.TestSwitchElement \
  2>&1 | tee com.top_logic.layout.view/target/test-switch.log
```
Expected: **Kompilierfehler** — `SwitchElement` / `SwitchElement.Config` / `SwitchElement.CaseConfig` existieren nicht (`cannot find symbol`). Das ist der erwartete Fehlschlag.

- [ ] **Step 4: `SwitchElement` implementieren**

Create `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SwitchElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactDeckPaneControl;
import com.top_logic.layout.react.control.layout.ReactDeckPaneControl.ChildFactory;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * UIElement that shows exactly one of several configured content views, chosen by evaluating a
 * TL-Script predicate per {@code <case>} against the value of an {@code input} channel.
 *
 * <p>
 * The cases are tested in configuration order; the first whose {@link CaseConfig#getTest() test}
 * returns {@code true} is shown. If none matches, the {@code <default>} content is shown (or nothing
 * if no default is configured). The switch re-evaluates whenever the input channel changes and only
 * swaps the visible view when the matching case actually changes - within a single case the content
 * (bound to its own channels) updates itself.
 * </p>
 *
 * <p>
 * Backed by {@link ReactDeckPaneControl}, so each case's content is created lazily on first
 * activation and cached afterwards (e.g. a form's edit mode survives switching away and back).
 * </p>
 */
public class SwitchElement implements UIElement {

	/**
	 * Configuration for {@link SwitchElement}.
	 */
	@TagName("switch")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getCases()}. */
		String CASES = "cases";

		/** Configuration name for {@link #getDefault()}. */
		String DEFAULT = "default";

		@Override
		@ClassDefault(SwitchElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel whose current value is passed to every {@code <case>} test predicate to choose
		 * the visible content.
		 */
		@Name(INPUT)
		@Format(ChannelRefFormat.class)
		@Mandatory
		ChannelRef getInput();

		/**
		 * The cases, evaluated in order; the first matching case is shown.
		 *
		 * @implNote Written directly as {@code <case>} children of the {@code <switch>}.
		 */
		@Name(CASES)
		@DefaultContainer
		@TreeProperty
		List<CaseConfig> getCases();

		/**
		 * Content shown when no {@code <case>} matches; empty for no fallback content.
		 */
		@Name(DEFAULT)
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getDefault();
	}

	/**
	 * Configuration for a single {@code <case>} of a {@link SwitchElement}.
	 */
	@TagName("case")
	public interface CaseConfig extends ConfigurationItem {

		/** Configuration name for {@link #getTest()}. */
		String TEST = "test";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * TL-Script predicate called with the current value of the switch's
		 * {@link Config#getInput() input} channel; this case is chosen when it returns {@code true}.
		 */
		@Name(TEST)
		@Mandatory
		@NonNullable
		Expr getTest();

		/**
		 * The content elements shown when this case is chosen.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final ChannelRef _inputRef;

	private final List<CaseEntry> _cases;

	private final List<UIElement> _default;

	/**
	 * Creates a new {@link SwitchElement} from configuration.
	 */
	@CalledByReflection
	public SwitchElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_cases = new ArrayList<>();
		for (CaseConfig caseConfig : config.getCases()) {
			QueryExecutor test = QueryExecutor.compile(caseConfig.getTest());
			List<UIElement> content = caseConfig.getContent().stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
			_cases.add(new CaseEntry(test, content));
		}
		_default = config.getDefault().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = context.resolveChannel(_inputRef);

		List<ChildFactory> factories = new ArrayList<>();
		for (int i = 0; i < _cases.size(); i++) {
			CaseEntry entry = _cases.get(i);
			int index = i;
			factories.add(() -> buildContent(entry._content, context, index));
		}
		// Trailing fallback pane: the <default> content, or an empty pane when no default is
		// configured. Chosen whenever no case matches.
		int fallbackIndex = factories.size();
		factories.add(() -> buildContent(_default, context, fallbackIndex));

		ReactDeckPaneControl deck =
			new ReactDeckPaneControl(context, factories, selectIndex(input.get(), fallbackIndex));

		// Re-choose the visible case on input change. selectChild() is a no-op when the index is
		// unchanged, so selecting a different object of the same case does not rebuild; and it
		// detaches (does not dispose) the previous pane, so cached case content keeps its state -
		// no ChannelNotificationScope deferral needed here.
		ChannelListener listener =
			(sender, oldValue, newValue) -> deck.selectChild(selectIndex(newValue, fallbackIndex));
		input.addListener(listener);
		deck.addCleanupAction(() -> input.removeListener(listener));

		return deck;
	}

	private int selectIndex(Object value, int fallbackIndex) {
		for (int i = 0; i < _cases.size(); i++) {
			if (Boolean.TRUE.equals(_cases.get(i)._test.execute(value))) {
				return i;
			}
		}
		return fallbackIndex;
	}

	private static ReactControl buildContent(List<UIElement> elements, ViewContext context, int index) {
		ViewContext childContext = context.childContext("switch").withChildSlotPath(String.valueOf(index));
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(childContext);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(childContext))
			.collect(Collectors.toList());
		return new ReactStackControl(childContext, children);
	}

	private record CaseEntry(QueryExecutor _test, List<UIElement> _content) {
	}
}
```

- [ ] **Step 5: Modul bauen und Test ausführen, Erfolg verifizieren**

Run:
```bash
mvn -B install -pl com.top_logic.layout.view \
  -DskipTests=false \
  -Dtest=test.com.top_logic.layout.view.element.TestSwitchElement \
  2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
```
Expected: `BUILD SUCCESS`; im Log `Tests run: 2, Failures: 0, Errors: 0`.

Falls `-Dtest` beim `install`-Lifecycle nicht greift (Surefire-Include-Konfiguration), stattdessen erst installieren, dann gezielt testen:
```bash
mvn -B install -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log
mvn -B test -DskipTests=false -pl com.top_logic.layout.view \
  -Dtest=test.com.top_logic.layout.view.element.TestSwitchElement \
  2>&1 | tee com.top_logic.layout.view/target/test-switch.log
```
Expected: `Tests run: 2, Failures: 0, Errors: 0`.

- [ ] **Step 6: Diff zeigen und nach Freigabe committen**

```bash
git add \
  com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SwitchElement.java \
  com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/element/TestSwitchElement.java \
  com.top_logic.layout.view/src/test/resources/test/com/top_logic/layout/view/element/test-switch.view.xml
git status
git diff --staged
```
Diff dem Nutzer zeigen; **erst nach ausdrücklicher Freigabe** committen:
```bash
git commit -m "Ticket #<nr>: Add declarative <switch> view element for input-dependent detail views."
```

---

## Task 2: `<switch>` im Modell-Editor verdrahten + Playwright-Verifikation

**Files:**
- Modify: `com.top_logic.dev.tools/src/main/webapp/WEB-INF/views/admin/model/model-editor.view.xml` (Details-Pane, aktuell Zeilen ~405–416)

**Interfaces:**
- Consumes: das in Task 1 registrierte `<switch>`/`<case>`/`<default>`-Element sowie die im View bereits deklarierten Kanäle (`selectedPart`).
- Produces: keine (Endverbraucher; UI-Verhalten).

- [ ] **Step 1: Details-Pane auf `<switch>` umstellen**

In `com.top_logic.dev.tools/src/main/webapp/WEB-INF/views/admin/model/model-editor.view.xml` den bisherigen Inhalt des Details-`<panel>`:

```xml
				<panel>
					<title>
						<en>Details</en>
						<de>Details</de>
					</title>
					<form
						input="selectedPart"
						withEditMode="true"
					>
						<field attribute="name"/>
					</form>
				</panel>
```

ersetzen durch (Tab-Einrückung wie in der Datei beibehalten):

```xml
				<panel>
					<title>
						<en>Details</en>
						<de>Details</de>
					</title>
					<switch input="selectedPart">
						<case test="p -> $p != null &amp;&amp; $p.instanceOf(`tl.model:TLClass`)">
							<form
								input="selectedPart"
								withEditMode="true"
							>
								<field attribute="name"/>
							</form>
						</case>
						<default>
							<form
								input="selectedPart"
								withEditMode="true"
							>
								<field attribute="name"/>
							</form>
						</default>
					</switch>
				</panel>
```

- [ ] **Step 2: Betroffene Module bauen**

`com.top_logic.layout.view` ist bereits aus Task 1 installiert. Für die im Modell-Editor genutzte, in `com.top_logic.dev.tools` liegende View-Datei das dev-tools-Modul (neu-)installieren, damit die geänderte `.view.xml` in die App-Ressourcen gelangt:
```bash
mvn -B install -pl com.top_logic.layout.view,com.top_logic.dev.tools \
  2>&1 | tee com.top_logic.dev.tools/target/mvn-build.log
```
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Demo-React-App starten**

Die React-View-Datei wird in `com.top_logic.demo.react` verifiziert (nicht `tl-demo`). App über die `tl-app`-Skill starten:

Run: `Skill(tl-app)` und den Anweisungen der Skill folgen, um `com.top_logic.demo.react` zu bauen/starten. Login: `root` / `root1234`.

- [ ] **Step 4: Mit Playwright verifizieren**

Im Browser (Playwright):
1. Anmelden (`root` / `root1234`), zum Admin-Bereich navigieren und den **Model editor** öffnen (React-View unter `/view/`).
2. Ein Modul in der Modul-Tabelle (links) selektieren, sodass das Klassendiagramm erscheint.
3. Im Diagramm eine **Klasse** selektieren → prüfen: das Details-Panel zeigt den `TLClass`-Fall (Formular des `<case>`-Zweigs).
4. Ein **Attribut** einer Klasse selektieren → prüfen: das Details-Panel zeigt den `<default>`-Zweig.
5. Editiermodus-Erhalt: bei selektierter Klasse in den Bearbeitungsmodus des Formulars wechseln, dann ein Attribut selektieren und wieder dieselbe Klasse — prüfen, dass der `TLClass`-Fall seinen Zustand (Bearbeitungsmodus) behalten hat (Deck-Caching).

Expected: Umschalten funktioniert; kein „useState is null"/Konsolenfehler; das jeweils passende Formular wird gezeigt.

Falls die Änderung nicht sichtbar ist: einmal ab- und wieder anmelden (Views werden pro Session geladen, siehe CLAUDE.md „View Configuration Reloading").

- [ ] **Step 5: Diff zeigen und nach Freigabe committen**

```bash
git add com.top_logic.dev.tools/src/main/webapp/WEB-INF/views/admin/model/model-editor.view.xml
git status
git diff --staged
```
Diff dem Nutzer zeigen; **erst nach ausdrücklicher Freigabe** committen:
```bash
git commit -m "Ticket #<nr>: Use <switch> for input-dependent detail view in the model editor."
```

---

## Self-Review

**1. Spec coverage:**
- „Neues `<switch>`-Element mit `<case test>`/`<default>`, erster Treffer gewinnt, sonst Default, sonst leer" → Task 1 (`Config`/`CaseConfig`, `selectIndex()` mit Fallback-Pane). ✅
- „Lazy + gecacht, Zustandserhalt beim Umschalten" → `ReactDeckPaneControl` in `createControl` (Kommentar begründet detach-statt-dispose). ✅
- „Umschalten nur bei Fallwechsel; innerhalb eines Falls aktualisiert sich das Formular selbst" → `selectChild()`-No-op bei gleichem Index. ✅
- „`input` nur für Tests; Fall-Inhalte binden Kanäle selbst" → `input` nur in `selectIndex`; Inhalte via eigene `input`-Attribute. ✅
- „Konkrete Verdrahtung: TLClass-Fall + Default, beide einfache Form" → Task 2. ✅
- „Verifikation: Build + Playwright in demo.react" → Task 2, Steps 2–4. ✅

**2. Placeholder scan:** Keine TBD/TODO/„handle edge cases"; vollständiger Code in jedem Code-Schritt; Test-Code ausgeschrieben. ✅

**3. Type consistency:**
- `SwitchElement.Config#getInput(): ChannelRef` (Test liest `getChannelName()` — `ChannelRef`-API, konsistent mit `FormElement`-Test). ✅
- `getCases(): List<CaseConfig>`, `CaseConfig#getTest(): Expr`, `getContent(): List<PolymorphicConfiguration<? extends UIElement>>` — im Impl und im Test identisch verwendet. ✅
- `getDefault(): List<PolymorphicConfiguration<? extends UIElement>>` — Impl und Test konsistent. ✅
- Laufzeit: `ReactDeckPaneControl(ReactContext, List<ChildFactory>, int)` + `selectChild(int)`; `ChildFactory#create(): ReactControl`; `ReactStackControl(ReactContext, List<? extends ReactControl>)` — exakt wie im Quellcode. ✅

**Hinweis zur Test-Grenze:** Die Laufzeit-Fallauswahl (`selectIndex` mit echter Expr-`execute`) wird bewusst nicht als Unit-Test abgebildet — `QueryExecutor#execute` benötigt die App-Services (PersistencyLayer/ModelService/SearchBuilder), wie in `TestDerivedChannelConfig` dokumentiert. Automatisiert getestet werden Parsing + Instanziierung (Compile der Expr, unter `TypeIndex.Module`); das Laufzeitverhalten deckt Playwright in Task 2 ab.
