# Config Value-to-Control Mapping Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the React configuration editor pick the right input control for every value type of a configuration property, and read and write typed values through the property's own format instead of assigning raw strings.

**Architecture:** The static `ConfigFieldDispatch.createPlainControl` is replaced by a configured service `ConfigControlService`, modelled on `com.top_logic.layout.view.form.FieldControlService`. It resolves a control in four steps: a `@ConfigControl` annotation on the property, an option list resolved through `Fields.optionProvider(…)`, a mapping by Java value type from the service configuration, and a built-in fallback by value type. Values whose type is neither string, number, boolean nor enum are edited as text through a new `ConfigFormatFieldModel` that parses and formats with the property's `ConfigurationValueProvider` and turns a parse failure into a field error.

**Tech Stack:** Java 21, TopLogic TypedConfiguration, JUnit 3-style tests (`junit.framework.TestCase` with a `suite()` method), Maven.

**Ticket:** #29462 — spec: `docs/superpowers/specs/2026-08-12-config-form-element-design.md` (section "1. Value-to-control mapping"). This plan covers that section only; the spec's sections 2–5 get their own plans, because each of them ships working software on its own and depends on this one.

## Global Constraints

- **Module under change:** `com.top_logic.layout.configedit`. Do not touch `com.top_logic.layout.view.designer` or the view designer's behaviour.
- **Member variables** are prefixed with `_` (e.g. `_property`). No `this.` prefix.
- **Every new `.java` file** needs the SPDX header used by the module:
  ```java
  /*
   * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
   *
   * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
   */
  ```
  plus a class-level JavaDoc comment.
- **JavaDoc references members with `{@link}`**, not `{@code}`. `{@code}` is only for literals (`{@code null}`), expressions, parameter names, and non-Java identifiers.
- **Build command** (run from the project root, never `cd` into a module; `target/` is owned by another user, hence `fakeroot`):
  ```bash
  HOME=/home/claude fakeroot mvn -B install -pl com.top_logic.layout.configedit \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
  ```
- **Test command** (`-Dtest` must be the fully qualified class name, otherwise Surefire's `test/TestAll.java` include wins):
  ```bash
  HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
    -Dtest=test.com.top_logic.layout.configedit.TestConfigFormatFieldModel \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
  ```
- **Never pass `-Dmaven.javadoc.skip=true`** — `TLDoclet` runs in the javadoc lifecycle and regenerates `messages_*.properties` from the new `I18NConstants` fields.
- **Commit messages:** `Ticket #29462: <description>.` No AI attribution lines.
- **English `messages_en.properties` are generated** during `mvn install` — never edit by hand. German `messages_de.properties` are hand-maintained; commit both when a new `I18NConstants` field is added.

## File Structure

| File | Responsibility |
|---|---|
| `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigFormatFieldModel.java` | **Create.** Field model for a plain property edited as text through its `ConfigurationValueProvider`: formats on read, parses on write, reports a parse failure as a field error. |
| `…/configedit/ConfigControlProvider.java` | **Create.** The SPI: creates a `ReactControl` for a `ConfigFieldModel`. |
| `…/configedit/ConfigControl.java` | **Create.** Java annotation naming a `ConfigControlProvider` for one property. |
| `…/configedit/ConfigControlService.java` | **Create.** The configured service holding the resolution chain and the built-in fallback. Takes over the body of `ConfigFieldDispatch`. |
| `…/configedit/ConfigPropertyOptions.java` | **Create.** Minimal `DeclarativeFormOptions` implementation so `Fields.optionProvider(…)` and `Fields.optionLabelsOrNull(…)` can be reused. |
| `…/configedit/ConfigFieldDispatch.java` | **Delete** at the end of Task 5; its logic lives in `ConfigControlService`. |
| `…/configedit/ConfigEditorControl.java:135` | **Modify.** Ask the service instead of the static dispatch, and let the service supply the field model. |
| `…/configedit/I18NConstants.java` | **Modify.** Add the parse-error key. |
| `com.top_logic.layout.configedit/src/main/webapp/WEB-INF/conf/tl-layout-configedit.conf.config.xml` | **Modify.** Register the service module and its type mappings. |
| `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigFormatFieldModel.java` | **Create.** Tests for formatting, parsing, and the error case. |
| `…/test/…/configedit/TestConfigControlService.java` | **Create.** Tests for the resolution chain. Replaces `TestConfigFieldDispatch.java` (**delete** in Task 5). |

---

### Task 1: `ConfigFormatFieldModel` — parse and format through the property's value provider

Today a text input writes the raw `String` it received into a typed property: `ConfigFieldModel.setValue` calls `_config.update(_property, value)` unchecked. For a property of type `Date`, `ThemeImage` or `TLModelPartRef` that either throws deep inside TypedConfiguration or stores a wrong-typed value. This task adds the field model that mediates through `PropertyDescriptor.getValueProvider()`.

> **Corrected after the task review.** The first version of Step 4 below overrode `getValue()` to
> return formatted text but left the inherited machinery in the typed domain. Two defects followed:
> `setDefaultValue(format(…))` in the constructor left `_defaultValue` formatted while `_value` stayed
> the raw value, so `isDirty()` was true for every freshly loaded non-null field; and delegating to
> `super.setValue(…)` defeated `ConfigFieldModel`'s redundant-write guard, whose unqualified
> `getValue()` dispatches to the formatted override and never compares equal. The code below carries
> the text domain through consistently instead: the constructor seeds cached value **and** default
> with the formatted text, the guard is formulated in that domain, the write goes through
> `getConfig().update(…)`, and `onChange` keeps the cached value in step. Steps 1 and 5 gain the two
> tests that pin this down.

**Files:**
- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigFormatFieldModel.java`
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/I18NConstants.java`
- Test: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigFormatFieldModel.java`

**Interfaces:**
- Consumes: `ConfigFieldModel(ConfigurationItem, PropertyDescriptor)` — existing; its `getValue()` returns `_config.value(_property)`, its `setValue(Object)` calls `_config.update(…)`, and its `onChange(ConfigurationChange)` fires `fireValueChanged(oldValue, newValue)` with the **typed** values.
- Produces: `ConfigFormatFieldModel(ConfigurationItem config, PropertyDescriptor property)`; `getValue()` returns the format specification as a `String` (or `null`); `setValue(Object)` accepts the text and writes the parsed value; `I18NConstants.ERROR_INVALID_VALUE__VALUE_PROPERTY` (a `ResKey2`).

- [ ] **Step 1: Write the failing test**

Create `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigFormatFieldModel.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;

/**
 * Tests for {@link ConfigFormatFieldModel}.
 */
public class TestConfigFormatFieldModel extends TestCase {

	/** Configuration with properties that need their value provider to be read and written. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getStart()}. */
		String START = "start";

		/** Property name for {@link #getDuration()}. */
		String DURATION = "duration";

		/**
		 * A point in time, written in the configuration's date format.
		 */
		@Name(START)
		Date getStart();

		/** @see #getStart() */
		void setStart(Date value);

		/**
		 * A duration in milliseconds, written as a human-readable amount of time.
		 */
		@Name(DURATION)
		@Format(MillisFormat.class)
		long getDuration();

		/** @see #getDuration() */
		void setDuration(long value);
	}

	private TestConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(TestConfig.class);
	}

	private ConfigFormatFieldModel model(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		return new ConfigFormatFieldModel(_config, property);
	}

	/**
	 * A typed value is handed out as the text its format produces, not as {@link Object#toString()}.
	 */
	public void testFormatsValueForDisplay() {
		_config.setDuration(90000);

		assertEquals("1min 30s", model(TestConfig.DURATION).getValue());
	}

	/**
	 * Entered text is parsed through the property's format before it reaches the configuration.
	 */
	public void testParsesEnteredText() {
		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		model.setValue("2min");

		assertEquals("The parsed value must reach the configuration.", 120000L, _config.getDuration());
		assertNull("A well-formed input leaves no error.", model.getInputError());
	}

	/**
	 * Unparsable text becomes a field error and leaves the configuration untouched.
	 */
	public void testRejectsUnparsableText() {
		_config.setDuration(90000);
		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		model.setValue("not a duration");

		assertEquals("The rejected input must not change the value.", 90000L, _config.getDuration());
		assertNotNull("The rejected input must be reported as an error.", model.getInputError());
	}

	/**
	 * A well-formed input after a rejected one clears the error.
	 */
	public void testClearsErrorAfterValidInput() {
		ConfigFormatFieldModel model = model(TestConfig.DURATION);
		model.setValue("not a duration");

		model.setValue("5s");

		assertNull("A well-formed input must clear the previous error.", model.getInputError());
		assertEquals(5000L, _config.getDuration());
	}

	/**
	 * Empty input clears the value instead of failing to parse.
	 */
	public void testEmptyInputClearsValue() {
		_config.setStart(new Date(0));
		ConfigFormatFieldModel model = model(TestConfig.START);

		model.setValue("");

		assertNull("Empty input must clear the value.", _config.getStart());
		assertNull("Empty input is not a parse error.", model.getInputError());
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration}.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigFormatFieldModel.class, TypeIndex.Module.INSTANCE);
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
  -Dtest=test.com.top_logic.layout.configedit.TestConfigFormatFieldModel \
  -Dmaven.repo.local=/home/claude/.m2/repository \
  -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
  -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
```

Expected: compile error, `cannot find symbol: class ConfigFormatFieldModel`.

If `MillisFormat` cannot be resolved, find the format class actually available for `long` durations with `grep -rn "class MillisFormat" com.top_logic.basic/src/main/java` and adjust the import; the test's assertions on `"1min 30s"` then need adjusting to that format's specification syntax. Do not drop the test — pick a property type whose value provider round-trips text.

- [ ] **Step 3: Add the error message key**

In `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/I18NConstants.java`, add the import and the field:

```java
import com.top_logic.basic.util.ResKey2;
```

```java
	/**
	 * @en Invalid value "{0}" for "{1}".
	 */
	public static ResKey2 ERROR_INVALID_VALUE__VALUE_PROPERTY;
```

Keep the existing `LIST_ELEMENT_EMPTY_TITLE__TYPE` field and the `static { initConstants(…); }` block unchanged.

- [ ] **Step 4: Write the field model**

Create `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigFormatFieldModel.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Objects;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.values.edit.Labels;

/**
 * {@link ConfigFieldModel} for a property that is edited as text but holds a typed value.
 *
 * <p>
 * The property's {@link PropertyDescriptor#getValueProvider() value provider} is the format between
 * the two: it turns the value into the text the input displays, and the entered text back into the
 * value the configuration stores. Text the format rejects becomes an
 * {@link #getInputError() input error} and leaves the configuration untouched, so the editor behaves
 * like a form field over a model attribute.
 * </p>
 */
public class ConfigFormatFieldModel extends ConfigFieldModel {

	/**
	 * Creates a {@link ConfigFormatFieldModel}.
	 *
	 * @param config
	 *        The configuration item to bind to.
	 * @param property
	 *        The property of the configuration item. Must have a
	 *        {@link PropertyDescriptor#getValueProvider() value provider}.
	 */
	public ConfigFormatFieldModel(ConfigurationItem config, PropertyDescriptor property) {
		super(config, property);
		// Both the cached value and the default value live in the text domain from here on -
		// ConfigFieldModel's constructor cached the raw typed value, which would otherwise never
		// compare equal to the formatted text this class hands out (breaking isDirty()).
		String text = format(config.value(property));
		setValueInternal(text);
		setDefaultValue(text);
	}

	@Override
	public Object getValue() {
		return format(super.getValue());
	}

	@Override
	public void setValue(Object value) {
		String text = value == null ? null : value.toString();
		if (StringServices.isEmpty(text)) {
			text = null;
		}

		if (Objects.equals(text, getValue())) {
			// The displayed text already matches the configuration's current value: nothing to
			// parse or write. (ConfigFieldModel's own redundant-write guard cannot see this,
			// since it compares in the typed domain against this class's formatted getValue().)
			return;
		}

		if (text == null) {
			setError(null);
			getConfig().update(getProperty(), null);
			return;
		}

		Object parsed;
		try {
			parsed = valueProvider().getValue(getProperty().getPropertyName(), text);
		} catch (ConfigurationException ex) {
			// Keep the value: the input control still shows the rejected text, the configuration
			// keeps the last accepted value - the same split a model attribute's field makes.
			setError(I18NConstants.ERROR_INVALID_VALUE__VALUE_PROPERTY.fill(text,
				Labels.propertyLabel(getProperty(), false)));
			return;
		}

		setError(null);
		// Write through the configuration API directly (not ConfigFieldModel#setValue): its
		// redundant-write guard operates in the typed domain and would never fire for this
		// class, and the value change notification (and this model's cached value) still needs
		// to go through onChange() below.
		getConfig().update(getProperty(), parsed);
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() == Kind.SET) {
			// The listener reports typed values, the control expects the formatted text.
			String oldText = format(change.getOldValue());
			String newText = format(change.getNewValue());
			setValueInternal(newText);
			fireValueChanged(oldText, newText);
		}
	}

	/**
	 * The given value as the text its format produces, or {@code null} for no value.
	 */
	private String format(Object value) {
		if (value == null) {
			return null;
		}
		return valueProvider().getSpecification(value);
	}

	@SuppressWarnings("unchecked")
	private ConfigurationValueProvider<Object> valueProvider() {
		return (ConfigurationValueProvider<Object>) getProperty().getValueProvider();
	}

}
```

- [ ] **Step 5: Run the test to verify it passes**

Same command as Step 2. Expected: PASS, 5 tests.

If `fireValueChanged` or `setError` are not visible, check `AbstractFieldModel` — both are public/protected there (`setError(ResKey)` at `AbstractFieldModel.java:237`, `fireValueChanged` at `:285`).

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigFormatFieldModel.java \
        com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/I18NConstants.java \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigFormatFieldModel.java
git commit -m "Ticket #29462: Read and write a configuration property through its own format."
```

The generated `messages_en.properties` and the seeded `messages_de.properties` land in the next full `mvn install`; add them in the commit of Task 5 once the whole module builds.

---

### Task 2: The `ConfigControlProvider` SPI and the `@ConfigControl` annotation

`FieldControlService` lets an attribute name its control with the `TLInputControl` annotation. Configuration properties carry **Java** annotations, not configuration items, so the counterpart is a plain annotation naming a provider class — the same shape `@PropertyEditor` uses for the classic editors.

**Files:**
- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControlProvider.java`
- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControl.java`

**Interfaces:**
- Consumes: `ConfigFieldModel` (Task 1's base class), `com.top_logic.layout.react.ReactContext`, `com.top_logic.layout.react.control.ReactControl`.
- Produces: `ConfigControlProvider.createControl(ReactContext context, ConfigFieldModel model)` returning `ReactControl`; the annotation `@ConfigControl(Class<? extends ConfigControlProvider>)` targeting a method (the property getter) or a type (all properties of that value type).

- [ ] **Step 1: Write the SPI**

Create `ConfigControlProvider.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Creates the {@link ReactControl} that edits a single configuration property.
 *
 * <p>
 * Implementations are picked by {@link ConfigControlService}, either through a {@link ConfigControl}
 * annotation on the property or through the service's value-type mapping. An implementation
 * referenced by {@link ConfigControl} must have a public constructor without arguments.
 * </p>
 *
 * @see com.top_logic.layout.view.form.ReactFieldControlProvider
 */
@FunctionalInterface
public interface ConfigControlProvider {

	/**
	 * Creates the input control for the property the given model is bound to.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model providing value, editability, and change notifications. Its
	 *        {@link ConfigFieldModel#getProperty() property} carries the metadata.
	 * @return The control for the field input widget.
	 */
	ReactControl createControl(ReactContext context, ConfigFieldModel model);

}
```

- [ ] **Step 2: Write the annotation**

Create `ConfigControl.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation naming the {@link ConfigControlProvider} that edits the annotated property.
 *
 * <p>
 * On a getter, it decides the control for that property. On a configuration interface, it decides
 * the control for properties whose value type is that interface. It is the configuration-side
 * counterpart of {@link com.top_logic.layout.view.form.TLInputControl} and takes precedence over
 * every other step of {@link ConfigControlService}'s resolution.
 * </p>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("config-control")
public @interface ConfigControl {

	/**
	 * The provider creating the control. Must have a public constructor without arguments.
	 */
	Class<? extends ConfigControlProvider> value();

}
```

- [ ] **Step 3: Compile**

```bash
HOME=/home/claude fakeroot mvn -B install -pl com.top_logic.layout.configedit \
  -Dmaven.repo.local=/home/claude/.m2/repository \
  -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
  -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
```

Expected: BUILD SUCCESS. The JavaDoc `{@link}` to `com.top_logic.layout.view.form.TLInputControl` will **not** resolve — `configedit` does not depend on `tl-layout-view` (the dependency runs the other way). Replace that reference with `{@code TLInputControl}` in both files and re-run; this is the one case where `{@code}` is correct, because the type is not on the module's classpath.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControlProvider.java \
        com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControl.java
git commit -m "Ticket #29462: Introduce the control provider SPI for configuration properties."
```

---

### Task 3: Option resolution for every option-bearing property

Today only enums get a select: `ConfigFieldDispatch.createEnumControl` builds the option list from `Class.getEnumConstants()`. A property annotated `@Options(fun = …)` gets a text field. `Fields.optionProvider(DeclarativeFormOptions)` already resolves `@Options` including its `fun`, `mapping` and `args`, plus the implementation lists of polymorphic properties; it only needs a `DeclarativeFormOptions` to work on.

**Files:**
- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigPropertyOptions.java`
- Test: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigPropertyOptions.java`

**Interfaces:**
- Consumes: `com.top_logic.layout.form.values.DeclarativeFormOptions` (methods `getProperty()`, `getCustomizations()`, plus `TypedAnnotatable`); `com.top_logic.layout.form.values.Fields.optionProvider(DeclarativeFormOptions)` returning `DerivedProperty<? extends Iterable<?>>` or `null`; `Fields.optionLabelsOrNull(DeclarativeFormOptions)` returning `LabelProvider` or `null`; `Fields.optionMapping(DerivedProperty)` returning `OptionMapping`; `DerivedProperty.get(ConfigurationItem)` returning the options; `com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations`.
- Produces: `ConfigPropertyOptions(PropertyDescriptor property)`, an implementation of `DeclarativeFormOptions`; the static helper `ConfigPropertyOptions.optionsFor(ConfigurationItem config, PropertyDescriptor property)` returning `List<?>` (empty when the property has no options).

- [ ] **Step 1: Write the failing test**

Create `TestConfigPropertyOptions.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigPropertyOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Tests for {@link ConfigPropertyOptions}.
 */
public class TestConfigPropertyOptions extends TestCase {

	/** Options function offering three fixed colours. */
	public static class Colors extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return Arrays.asList("red", "green", "blue");
		}
	}

	/** Configuration with an annotated option list and a property without options. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getColor()}. */
		String COLOR = "color";

		/** Property name for {@link #getText()}. */
		String TEXT = "text";

		/**
		 * One of the offered colours.
		 */
		@Name(COLOR)
		@Options(fun = Colors.class)
		String getColor();

		/** @see #getColor() */
		void setColor(String value);

		/**
		 * Free text without options.
		 */
		@Name(TEXT)
		String getText();

		/** @see #getText() */
		void setText(String value);
	}

	/**
	 * A property annotated with {@link Options} offers the function's values.
	 */
	public void testResolvesAnnotatedOptions() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COLOR);

		List<?> options = ConfigPropertyOptions.optionsFor(config, property);

		assertEquals(Arrays.asList("red", "green", "blue"), options);
	}

	/**
	 * A property without options offers none, rather than failing.
	 */
	public void testPlainPropertyHasNoOptions() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.TEXT);

		assertTrue("A property without options must offer none.",
			ConfigPropertyOptions.optionsFor(config, property).isEmpty());
	}

	/**
	 * Suite requiring {@link TypeIndex}: the option resolution looks up specializations.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigPropertyOptions.class, TypeIndex.Module.INSTANCE);
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
  -Dtest=test.com.top_logic.layout.configedit.TestConfigPropertyOptions \
  -Dmaven.repo.local=/home/claude/.m2/repository \
  -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
  -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
```

Expected: compile error, `cannot find symbol: class ConfigPropertyOptions`.

- [ ] **Step 3: Write the options adapter**

Create `ConfigPropertyOptions.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.LazyTypedAnnotatableMixin;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;

/**
 * {@link DeclarativeFormOptions} over a single configuration property.
 *
 * <p>
 * Exists so that the option resolution of the classic declarative form
 * ({@link Fields#optionProvider(DeclarativeFormOptions)}) can be reused: it resolves the
 * {@link com.top_logic.layout.form.values.edit.annotation.Options} annotation including its
 * function, mapping and argument references, and the implementation lists of polymorphic
 * properties.
 * </p>
 */
public class ConfigPropertyOptions extends LazyTypedAnnotatableMixin implements DeclarativeFormOptions {

	private static final AnnotationCustomizations NO_CUSTOMIZATIONS =
		new ConfiguredAnnotationCustomizations();

	private final PropertyDescriptor _property;

	/**
	 * Creates a {@link ConfigPropertyOptions}.
	 *
	 * @param property
	 *        The property whose options are resolved.
	 */
	public ConfigPropertyOptions(PropertyDescriptor property) {
		_property = property;
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	@Override
	public AnnotationCustomizations getCustomizations() {
		// No form-local customizations: the annotations on the property itself decide.
		return NO_CUSTOMIZATIONS;
	}

	/**
	 * The options offered for the given property, or an empty list if it has none.
	 *
	 * @param config
	 *        The configuration item holding the property; option functions may depend on its other
	 *        values.
	 * @param property
	 *        The property to resolve options for.
	 */
	public static List<?> optionsFor(ConfigurationItem config, PropertyDescriptor property) {
		DerivedProperty<? extends Iterable<?>> provider = optionProvider(property);
		if (provider == null) {
			return Collections.emptyList();
		}
		Iterable<?> options = provider.get(config);
		if (options == null) {
			return Collections.emptyList();
		}
		List<Object> result = new ArrayList<>();
		for (Object option : options) {
			result.add(option);
		}
		return result;
	}

	/**
	 * The option provider for the given property, or {@code null} if it is not edited by selecting.
	 */
	public static DerivedProperty<? extends Iterable<?>> optionProvider(PropertyDescriptor property) {
		return Fields.optionProvider(new ConfigPropertyOptions(property));
	}

	/**
	 * The {@link LabelProvider} for the options of the given property, or {@code null} for the
	 * default labels.
	 */
	public static LabelProvider optionLabels(PropertyDescriptor property) {
		return Fields.optionLabelsOrNull(new ConfigPropertyOptions(property));
	}

}
```

- [ ] **Step 4: Run the test to verify it passes**

Same command as Step 2. Expected: PASS, 2 tests.

Two things commonly need fixing here:
1. `LazyTypedAnnotatableMixin` may not exist under that name. `DeclarativeFormOptions` extends `TypedAnnotatable`; find the reusable base with `grep -rn "class LazyTypedAnnotatable" com.top_logic.basic/src/main/java` and extend that instead (`com.top_logic.basic.col.TypedAnnotatable` has a lazy subclass used by `DerivedProperty`).
2. `ConfiguredAnnotationCustomizations` may need a constructor argument. Check with `grep -n "public ConfiguredAnnotationCustomizations" com.top_logic.basic/src/main/java/com/top_logic/basic/config/customization/ConfiguredAnnotationCustomizations.java` and pass what it wants; an empty customizations instance is what we need.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigPropertyOptions.java \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigPropertyOptions.java
git commit -m "Ticket #29462: Resolve the options of a configuration property through the declarative form."
```

---

### Task 4: `ConfigControlService` — the resolution chain

> **Corrected after implementation and three review rounds.** Step 3's listing below is the shape
> the task started from; the code deviates from it deliberately, on the human's ruling:
>
> - **The ordering follows the classic form's veto, not the Java type.** `ValueEditor.addField` asks
>   first whether a property is *specialized* — options, an explicit `@Format`, or a value binding —
>   and only an unspecialized property reaches the type branches. `isSpecialized` mirrors that, with
>   `@Encrypted` as a fourth condition, because the password control is a text control. One extracted
>   predicate governs both `createModel` (value domain) and `fallback` (widget); every defect found in
>   this task's reviews was those two disagreeing.
> - **`dateKind` is gone.** An earlier attempt derived `TIME`/`DATE_TIME` from the value provider's
>   class. Under the veto a formatted `Date` never reaches the picker at all, so the distinction moved
>   to Task 7's format-provider map instead.
> - **The listing below silently dropped the enum branch.** `ConfigPropertyOptions` does not answer
>   for a plain enum property, so `TestConfig.MODE` would have landed in a text field. `isSelect`
>   treats `property.getType().isEnum()` as select-worthy on its own.
> - **`checkSupportedKind`** rejects any kind the service cannot put into one widget — accepted are
>   `PLAIN`, `REF`, and `COMPLEX` *with* a value provider — rather than trusting the caller's dispatch
>   order.
> - **The service registration** in `tl-layout-configedit.conf.config.xml`, which this plan put in
>   Task 5, landed here: a `ConfiguredManagedClass` does not start without it.

**Files:**
- Create: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControlService.java`
- Test: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigControlService.java`

**Interfaces:**
- Consumes: `ConfigControlProvider`, `ConfigControl`, `ConfigFieldModel`, `ConfigFormatFieldModel`, `ConfigSelectFieldModel(ConfigurationItem, PropertyDescriptor, List<?>, boolean)`, `ConfigPropertyOptions.optionsFor/optionLabels`, and the controls `ReactCheckboxControl(ReactContext, FieldModel)`, `ReactNumberInputControl(ReactContext, FieldModel, int decimals)`, `ReactTextInputControl(ReactContext, FieldModel)`, `ReactSelectFormFieldControl(ReactContext, SelectFieldModel, LabelProvider)`, `ReactDatePickerControl(ReactContext, FieldModel, TLPrimitive.Kind)`, `ReactPasswordInputControl` — verify the last one's name with `ls com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/`.
- Produces: `ConfigControlService.getInstance()`; `createModel(ConfigurationItem config, PropertyDescriptor property)` returning `ConfigFieldModel`; `createControl(ReactContext context, ConfigFieldModel model)` returning `ReactControl`; `ConfigControlService.Module.INSTANCE`.

- [ ] **Step 1: Write the failing test**

Create `TestConfigControlService.java`. It replaces `TestConfigFieldDispatch` and keeps its four cases, adding the new ones:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests for {@link ConfigControlService}.
 */
public class TestConfigControlService extends TestCase {

	/** Options function offering three fixed colours. */
	public static class Colors extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return Arrays.asList("red", "green", "blue");
		}
	}

	/** Configuration covering every value type the fallback distinguishes. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getText()}. */
		String TEXT = "text";

		/** Property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Property name for {@link #isFlag()}. */
		String FLAG = "flag";

		/** Property name for {@link #getRatio()}. */
		String RATIO = "ratio";

		/** Property name for {@link #getMode()}. */
		String MODE = "mode";

		/** Property name for {@link #getStart()}. */
		String START = "start";

		/** Property name for {@link #getColor()}. */
		String COLOR = "color";

		/** Property name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Property name for {@link #getSecret()}. */
		String SECRET = "secret";

		/** A mode to choose from. */
		enum Mode {
			/** The one mode. */
			ON,
			/** The other mode. */
			OFF;
		}

		/** Free text. */
		@Name(TEXT)
		String getText();

		/** A whole number. */
		@Name(COUNT)
		@IntDefault(0)
		int getCount();

		/** A flag. */
		@Name(FLAG)
		boolean isFlag();

		/** A fraction. */
		@Name(RATIO)
		double getRatio();

		/** One of the modes. */
		@Name(MODE)
		Mode getMode();

		/** A point in time. */
		@Name(START)
		Date getStart();

		/** One of the offered colours. */
		@Name(COLOR)
		@Options(fun = Colors.class)
		String getColor();

		/** An internationalized label. */
		@Name(LABEL)
		ResKey getLabel();

		/** A secret that must not be shown. */
		@Name(SECRET)
		@Encrypted
		String getSecret();
	}

	private TestConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(TestConfig.class);
	}

	private ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	private ReactControl control(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(_config, property);
		return ConfigControlService.getInstance().createControl(context(), model);
	}

	private ConfigFieldModel model(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		return ConfigControlService.getInstance().createModel(_config, property);
	}

	/** A string property keeps the text input. */
	public void testString() {
		assertTrue(control(TestConfig.TEXT) instanceof ReactTextInputControl);
	}

	/** A boolean property keeps the checkbox. */
	public void testBoolean() {
		assertTrue(control(TestConfig.FLAG) instanceof ReactCheckboxControl);
	}

	/** An integral property keeps the number input. */
	public void testInt() {
		assertTrue(control(TestConfig.COUNT) instanceof ReactNumberInputControl);
	}

	/** A floating-point property keeps the number input. */
	public void testDouble() {
		assertTrue(control(TestConfig.RATIO) instanceof ReactNumberInputControl);
	}

	/** An enum property keeps the select. */
	public void testEnum() {
		assertTrue(control(TestConfig.MODE) instanceof ReactSelectFormFieldControl);
	}

	/** A property with an options annotation gets a select, not a text input. */
	public void testOptionsProperty() {
		assertTrue("A property with options must be edited by selecting.",
			control(TestConfig.COLOR) instanceof ReactSelectFormFieldControl);
	}

	/** A date property gets the date picker instead of a text input. */
	public void testDate() {
		assertTrue("A date must be edited in a date picker.",
			control(TestConfig.START) instanceof ReactDatePickerControl);
	}

	/** A typed property that is edited as text gets the format-aware model. */
	public void testTypedPropertyUsesFormatModel() {
		assertTrue("A typed property must be parsed through its format.",
			model(TestConfig.LABEL) instanceof ConfigFormatFieldModel);
	}

	/** A string property needs no format model. */
	public void testStringUsesPlainModel() {
		assertFalse("A string property needs no format.",
			model(TestConfig.TEXT) instanceof ConfigFormatFieldModel);
	}

	/**
	 * Suite starting the service under test.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestConfigControlService.class,
			TypeIndex.Module.INSTANCE, ConfigControlService.Module.INSTANCE);
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
  -Dtest=test.com.top_logic.layout.configedit.TestConfigControlService \
  -Dmaven.repo.local=/home/claude/.m2/repository \
  -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
  -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
```

Expected: compile error, `cannot find symbol: class ConfigControlService`.

- [ ] **Step 3: Write the service**

Create `ConfigControlService.java`. Mirror the shape of `com.top_logic.layout.view.form.FieldControlService` (`ConfiguredManagedClass` with a `Module` singleton holder and a `@Key`-ed provider map), keyed by Java value type rather than by model type:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.model.TLPrimitive.Kind;

/**
 * Service resolving the input control for a configuration property.
 *
 * <p>
 * Resolution chain:
 * </p>
 * <ol>
 * <li>{@link ConfigControl} annotation on the property or on its value type.</li>
 * <li>The property's option list, resolved by {@link ConfigPropertyOptions} - such a property is
 * edited by selecting.</li>
 * <li>The value-type-to-provider map configured in this service.</li>
 * <li>The built-in fallback by value type.</li>
 * </ol>
 *
 * <p>
 * The configured map is what lets a module above this one contribute a control - the TL-Script
 * editor, for instance, lives in {@code tl-model-search-react}.
 * </p>
 */
public class ConfigControlService extends ConfiguredManagedClass<ConfigControlService.Config> {

	/**
	 * Configuration options for {@link ConfigControlService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ConfigControlService> {

		/**
		 * Value-type-to-provider mappings, keyed by the Java type of the property value.
		 */
		@Key(ProviderMapping.TYPE)
		Map<Class<?>, ProviderMapping> getProviders();

	}

	/**
	 * A single value-type-to-provider mapping entry.
	 */
	public interface ProviderMapping extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The Java type of the property value this mapping applies to.
		 */
		@Name(TYPE)
		@Mandatory
		Class<?> getType();

		/**
		 * The control provider to use for properties of this value type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ConfigControlProvider> getImpl();

	}

	private final InstantiationContext _context;

	private Map<Class<?>, ConfigControlProvider> _providerByType;

	/**
	 * Creates a {@link ConfigControlService} from configuration.
	 */
	@CalledByReflection
	public ConfigControlService(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		_providerByType = new HashMap<>();
		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			_providerByType.put(mapping.getType(), _context.getInstance(mapping.getImpl()));
		}
	}

	/**
	 * Creates the {@link ConfigFieldModel} for the given property.
	 *
	 * <p>
	 * A property edited by selecting gets a {@link ConfigSelectFieldModel}, a typed property edited
	 * as text a {@link ConfigFormatFieldModel}, and everything else the plain
	 * {@link ConfigFieldModel}.
	 * </p>
	 *
	 * @param config
	 *        The configuration item holding the property.
	 * @param property
	 *        The property to bind to.
	 */
	public ConfigFieldModel createModel(ConfigurationItem config, PropertyDescriptor property) {
		if (isSelect(config, property)) {
			List<?> options = ConfigPropertyOptions.optionsFor(config, property);
			return new ConfigSelectFieldModel(config, property, options, false);
		}
		if (needsFormat(property)) {
			return new ConfigFormatFieldModel(config, property);
		}
		return new ConfigFieldModel(config, property);
	}

	/**
	 * Resolves and creates the input control for the given field model.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model, created by {@link #createModel(ConfigurationItem, PropertyDescriptor)}.
	 */
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		PropertyDescriptor property = model.getProperty();

		// 1. Annotation on the property or on its value type.
		ConfigControl annotation = controlAnnotation(property);
		if (annotation != null) {
			return newProvider(annotation.value()).createControl(context, model);
		}

		// 2. Edited by selecting.
		if (model instanceof ConfigSelectFieldModel selectModel) {
			LabelProvider labels = ConfigPropertyOptions.optionLabels(property);
			return new ReactSelectFormFieldControl(context, selectModel, labels);
		}

		// 3. Configured provider by value type.
		ConfigControlProvider mapped = _providerByType.get(property.getType());
		if (mapped != null) {
			return mapped.createControl(context, model);
		}

		// 4. Built-in fallback.
		return fallback(context, model, property);
	}

	private ReactControl fallback(ReactContext context, ConfigFieldModel model, PropertyDescriptor property) {
		Class<?> type = property.getType();

		if (type == boolean.class || type == Boolean.class) {
			return new ReactCheckboxControl(context, model);
		}
		if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
			return new ReactNumberInputControl(context, model, 0);
		}
		if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
			return new ReactNumberInputControl(context, model, 2);
		}
		if (Date.class.isAssignableFrom(type)) {
			return new ReactDatePickerControl(context, model, Kind.DATE);
		}
		if (property.getAnnotation(Encrypted.class) != null) {
			return new ReactTextInputControl(context, model);
		}
		// String, and every typed value the format model turned into text.
		return new ReactTextInputControl(context, model);
	}

	/**
	 * Whether the given property is edited by selecting from options.
	 */
	private boolean isSelect(ConfigurationItem config, PropertyDescriptor property) {
		if (controlAnnotation(property) != null) {
			// An explicitly named control decides on its own.
			return false;
		}
		return ConfigPropertyOptions.optionProvider(property) != null;
	}

	/**
	 * Whether the given property holds a typed value that must be parsed and formatted when edited
	 * as text.
	 */
	private boolean needsFormat(PropertyDescriptor property) {
		Class<?> type = property.getType();
		if (type == String.class || type.isPrimitive() || Number.class.isAssignableFrom(type)
			|| type == Boolean.class || type.isEnum()) {
			return false;
		}
		return property.getValueProvider() != null;
	}

	private ConfigControl controlAnnotation(PropertyDescriptor property) {
		ConfigControl annotation = property.getAnnotation(ConfigControl.class);
		if (annotation != null) {
			return annotation;
		}
		return property.getType().getAnnotation(ConfigControl.class);
	}

	private ConfigControlProvider newProvider(Class<? extends ConfigControlProvider> providerClass) {
		try {
			return providerClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new IllegalArgumentException(
				"Cannot instantiate control provider '" + providerClass.getName()
					+ "'. A public constructor without arguments is required.",
				ex);
		}
	}

	/**
	 * The {@link ConfigControlService} singleton.
	 */
	public static ConfigControlService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link ConfigControlService}.
	 */
	public static final class Module extends TypedRuntimeModule<ConfigControlService> {

		/**
		 * Singleton {@link ConfigControlService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ConfigControlService> getImplementation() {
			return ConfigControlService.class;
		}

	}

}
```

The `Encrypted` branch currently returns a text input, which is what the test asserts nothing about — it exists so the next step has an obvious place to hook the password control.

- [ ] **Step 4: Run the test to verify it passes**

Same command as Step 2. Expected: PASS, 9 tests.

Likely fixes:
- `ResKey` needs a value provider for `testTypedPropertyUsesFormatModel` to hold. Verify with a scratch assertion that `descriptor().getProperty("label").getValueProvider() != null`. If `ResKey` properties have none, use a property type that does (e.g. `java.util.regex.Pattern` or `com.top_logic.basic.config.CommaSeparatedStrings`-formatted `List<String>`) and adjust the test's property.
- `ConfigSelectFieldModel` may require `SelectFieldModel`'s option list to be non-empty; if `ReactSelectFormFieldControl`'s constructor rejects a `null` label provider, pass `MetaLabelProvider.INSTANCE` instead.

- [ ] **Step 5: Give the encrypted property its password control**

Find the password control's class name:

```bash
ls com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/form/ | grep -i password
```

Add the test case to `TestConfigControlService`, using the class the listing shows (assumed `ReactPasswordInputControl` below):

```java
	/** An encrypted property is not shown in the clear. */
	public void testEncrypted() {
		assertTrue("An encrypted property must be edited in a password field.",
			control(TestConfig.SECRET) instanceof ReactPasswordInputControl);
	}
```

Then replace the `Encrypted` branch in `fallback`:

```java
		if (property.getAnnotation(Encrypted.class) != null) {
			return new ReactPasswordInputControl(context, model);
		}
```

Consult `com.top_logic.layout.view.form.PasswordInputControlProvider` for the exact constructor it uses.

- [ ] **Step 6: Run the test to verify it passes**

Same command as Step 2. Expected: PASS, 10 tests.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControlService.java \
        com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigControlService.java
git commit -m "Ticket #29462: Resolve the control of a configuration property in a configurable service."
```

---

### Task 5: Switch the editor over, register the service, retire the static dispatch

> **Two things implementation surfaced.** Retiring `ConfigFieldDispatch` also retires its
> `enumLabelProvider`, which resolved enum option labels from a resource key with an
> `@<constantName>` suffix; the service uses `MetaLabelProvider` and stays free of a `Resources`
> dependency. Enum labels in the editor therefore look different — accepted knowingly.
> And `ConfigEditorControl.isSupportedKind` still excludes `PropertyKind.COMPLEX`, so a `ResKey`
> property never reaches the service at all. Pre-existing, and the reason section 1 of the spec
> cannot deliver the I18N control yet; it belongs to spec section 2.

**Files:**
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java` (the `PLAIN`/`REF` branch, currently lines 135–150)
- Modify: `com.top_logic.layout.configedit/src/main/webapp/WEB-INF/conf/tl-layout-configedit.conf.config.xml`
- Delete: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigFieldDispatch.java`
- Delete: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigFieldDispatch.java`

**Interfaces:**
- Consumes: `ConfigControlService.getInstance().createModel(…)` and `.createControl(…)` from Task 4.
- Produces: nothing new; `ConfigEditorControl`'s constructor signatures stay as they are, so `ConfigEditorElement` and `DemoConfigEditorElement` keep compiling.

- [ ] **Step 1: Register the service module**

Replace the empty `<application>` element in `tl-layout-configedit.conf.config.xml` with:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<configs>
		<config config:interface="com.top_logic.basic.module.ModuleSystem$Config">
			<modules>
				<module key="com.top_logic.layout.configedit.ConfigControlService$Module"
					enabled="true"
				/>
			</modules>
		</config>
	</configs>

	<services>
		<config service-class="com.top_logic.layout.configedit.ConfigControlService">
			<instance class="com.top_logic.layout.configedit.ConfigControlService">
				<providers/>
			</instance>
		</config>
	</services>
</application>
```

Verify the module-registration syntax against the file that does the same for `FieldControlService`:

```bash
sed -n '1,30p' com.top_logic.layout.view/src/main/webapp/WEB-INF/conf/tl-layout-view.conf.config.xml
```

and copy its structure exactly — the `<modules>` wrapper's interface name must match.

- [ ] **Step 2: Point the editor at the service**

In `ConfigEditorControl.java`, replace the `PLAIN`/`REF` branch

```java
			ConfigFieldModel model = new ConfigFieldModel(config, property);
			addCleanupAction(model::detach);

			ReactControl input = ConfigFieldDispatch.createPlainControl(context, model);
```

with

```java
			ConfigFieldModel model = ConfigControlService.getInstance().createModel(config, property);
			addCleanupAction(model::detach);

			ReactControl input = ConfigControlService.getInstance().createControl(context, model);
```

and remove the now unused import of `ConfigFieldDispatch`. Leave the label, tooltip, `LabelPosition` and chrome handling below it untouched.

- [ ] **Step 3: Delete the static dispatch and its test**

```bash
git rm com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigFieldDispatch.java \
       com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigFieldDispatch.java
```

`ConfigFieldDispatch.enumLabelProvider` is package-visible and may have other callers. Check before deleting:

```bash
grep -rn "ConfigFieldDispatch" --include="*.java" com.top_logic.layout.configedit/src com.top_logic.layout.view/src com.top_logic.demo.react/src
```

Every remaining caller must move to the service. If `enumLabelProvider` is still needed, move the method into `ConfigPropertyOptions` (it resolves the label key with an `@<constantName>` suffix) rather than keeping the class alive.

- [ ] **Step 4: Build the module and run its whole test suite**

```bash
HOME=/home/claude fakeroot mvn -B install -pl com.top_logic.layout.configedit \
  -Dmaven.repo.local=/home/claude/.m2/repository \
  -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
  -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu 2>&1 | tee com.top_logic.layout.configedit/target/mvn-build.log
```

Expected: BUILD SUCCESS. Then each remaining test class in turn:

```bash
for t in TestConfigFieldModel TestConfigSelectFieldModel TestConfigEditorControl \
         TestConfigFormatFieldModel TestConfigPropertyOptions TestConfigControlService; do
  HOME=/home/claude fakeroot mvn -B test -DskipTests=false -pl com.top_logic.layout.configedit \
    -Dtest=test.com.top_logic.layout.configedit.$t \
    -Dmaven.repo.local=/home/claude/.m2/repository \
    -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
    -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu || echo "FAILED: $t"
done
```

Expected: no `FAILED:` line. `TestConfigEditorControl` exercises the branch changed in Step 2 and needs `ConfigControlService.Module.INSTANCE` in its `suite()` — add it there alongside the modules it already starts.

- [ ] **Step 5: Build the two dependent modules**

```bash
HOME=/home/claude fakeroot mvn -B install -pl com.top_logic.layout.view,com.top_logic.demo.react \
  -Dmaven.repo.local=/home/claude/.m2/repository \
  -Dmaven.repo.local.tail=/home/dbu/.m2/repository \
  -Daether.chainedLocalRepository.ignoreTailAvailability=true -nsu
```

Expected: BUILD SUCCESS. Do **not** pass `-am`; both modules are listed explicitly.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigEditorControl.java \
        com.top_logic.layout.configedit/src/main/webapp/WEB-INF/conf/tl-layout-configedit.conf.config.xml \
        com.top_logic.layout.configedit/src/main/java/META-INF/messages_en.properties \
        com.top_logic.layout.configedit/src/main/java/META-INF/messages_de.properties
git add -u
git commit -m "Ticket #29462: Let the configuration editor resolve its controls through the service."
```

If `messages_*.properties` do not exist under that path, find them with `find com.top_logic.layout.configedit/src -name "messages_*.properties"` and add the ones the build touched. Check the German file for the new key and correct its wording by hand — the build seeds it via machine translation but never overwrites it again.

---

### Task 6: Verify in the running application

The view designer is the existing user of the editor and the fastest way to see the change: it edits view configurations whose properties include typed values that previously landed in a plain text field.

**Files:** none — verification only.

- [ ] **Step 1: Start the React demo application**

Use the `tl-app` skill to start `com.top_logic.demo.react`. The module directory must be the working directory, otherwise the database is missing. Login is `root` / `root1234`; a freshly created database prints an initial password in the log instead.

- [ ] **Step 2: Verify the view designer**

Delegate the browser interaction to a sub-agent (see the `tl-app` skill's "Verifying in the browser" section) with this brief:

> Open the view designer in the React demo application. Select a node whose configuration has a property that is neither text, number, boolean nor enum — a date, an internationalized label, or a property with an `@Options` annotation. Report for each: which input control is rendered, whether the displayed value is the formatted value rather than a Java `toString()` result, whether entering a well-formed value is kept after a reload, and whether entering nonsense shows an error at the field instead of throwing. Take a screenshot of the property form.

- [ ] **Step 3: Record the outcome**

Write what the sub-agent reported into the ticket as a comment: which control each property type now gets, and anything that still falls back to a text input. A property type that still lands in text and should not is the input for the next plan.

- [ ] **Step 4: Commit nothing, report**

No code changes in this task. If Step 2 found a defect, fix it as a new commit in this plan's style (failing test first) before declaring the plan done.

---

### Task 7: A second key for the provider map — the value provider's class

**Runs after Task 4 and before Task 5.** Added after the plan was written, once the resolution
had been restructured to the classic ordering (see the note in Task 1 and the `isSpecialized`
predicate in `ConfigControlService`).

The classic ordering routes every *specialized* property — one with options, an explicit
`@Format`, or a value binding — to the format text field. That is correct and never wrong, but it
throws away information the format carries: `TimeOfDayAsDateValueProvider` says "this value is a
time of day", and the React layer has had `<input type="time">` since ticket #29448
(`ReactDatePickerControl.Kind.TIME`). Neither the classic model layer nor the classic config layer
could use that — the classic config layer has no time widget at all, and reaches its formatted
text field through `PlainEditor` — so this is a gain, not a restoration.

The service already has a configured provider map keyed by the property's **Java value type**
(`Config.getProviders()`). A `Date` property that means a time of day and one that means a date
share the same Java type, so that key cannot separate them. This task adds a second map keyed by
the **`ConfigurationValueProvider` class**, consulted in the specialized branch before the format
text field. The same mechanism is what lets a module above this one contribute a control from
outside — `tl-model-search-react` for TL-Script expressions — which is what the spec's step 3 asks
for.

**The trap, and the reason this is not a two-line change:** a control claimed this way needs the
**typed** value, not the format's text. `ReactDatePickerControl`'s constructor calls
`formatIso(model.getValue())` and expects a `Date`; handing it a `ConfigFormatFieldModel` would
give it the specification string. So the claim has to be visible to `createModel` as well, which
picks the value domain. `isSpecialized(property)` must therefore become "specialized **and not
claimed by a format mapping**", used by both callers exactly as it is today.

**Files:**
- Modify: `com.top_logic.layout.configedit/src/main/java/com/top_logic/layout/configedit/ConfigControlService.java`
- Modify: `com.top_logic.layout.configedit/src/main/webapp/WEB-INF/conf/tl-layout-configedit.conf.config.xml`
- Test: `com.top_logic.layout.configedit/src/test/java/test/com/top_logic/layout/configedit/TestConfigControlService.java`

**Interfaces:**
- Consumes: `Config.getProviders()` and `ProviderMapping` as they are today; `isSpecialized(PropertyDescriptor)`; `PropertyDescriptor.getValueProvider()`; `ReactDatePickerControl(ReactContext, FieldModel, Kind)`.
- Produces: a second configured map on `Config` keyed by the value-provider class, and a lookup that walks the provider's superclass chain so a mapping registered for a base provider also covers its specializations.

- [ ] **Step 1: Write the failing tests**

Three cases, in the style of the existing ones in `TestConfigControlService`:

1. A `Date` property annotated `@Format(TimeOfDayAsDateValueProvider.class)` gets a
   `ReactDatePickerControl` whose `inputType` state is `"time"`, **and** its field model is a plain
   `ConfigFieldModel`, not a `ConfigFormatFieldModel`. Assert both — the widget alone would pass
   even with the domains mismatched, and that mismatch is the defect this task can most easily
   introduce.
2. A `Date` property with some other explicit format still gets the text input over a
   `ConfigFormatFieldModel` — the unclaimed case keeps the classic behaviour.
3. A property whose value provider is a *subclass* of a registered one is claimed too, proving the
   superclass walk.

- [ ] **Step 2: Run the tests to verify they fail**

Use the module's test command from the Global Constraints. Expect failures on all three, the first
two on the assertion and the third on the missing mapping.

- [ ] **Step 3: Add the configured map**

Mirror the existing `ProviderMapping` shape: a `@Key`-ed map on `Config`, an entry type carrying
the value-provider class and a `PolymorphicConfiguration<? extends ConfigControlProvider>`, and a
resolved `Map<Class<?>, ConfigControlProvider>` built in `startUp()`. Look up the provider's
concrete class first, then walk up its superclasses.

- [ ] **Step 4: Thread the claim through both decisions**

`isSpecialized` gains the "not claimed" clause, and `createControl` consults the map in the
specialized branch before the format text field. Keep both callers reading the same predicate —
if the claim is visible to one and not the other, the value domain and the widget drift apart.

- [ ] **Step 5: Register the time-of-day format**

In `tl-layout-configedit.conf.config.xml`, map `TimeOfDayAsDateValueProvider` to a provider that
creates `ReactDatePickerControl` with `Kind.TIME`. This is the first real user of the new key and
the reason it exists; without it the mechanism is untested in the running application.

- [ ] **Step 6: Run the module's six test classes**

All must stay green.

- [ ] **Step 7: Commit**

```
Ticket #29462: Let a known configuration format claim its own input control.
```

---


### Task 8: A number arriving from a control reaches the property's own numeric type

**Came out of Task 6's verification, not from the original spec.** Entering `555` into an
`int`-typed property and applying threw `Value '555.0' (Double) is not of expected type 'Integer'`
and the write was rejected. `ReactNumberInputControl.parseClientValue` always returns
`Double.parseDouble(...)`, whatever decimal-place count it was constructed with.

Not a regression: the retired `ConfigFieldDispatch` routed `int`/`Integer`/`long`/`Long` to the same
control with the same zero-decimals argument, so such a property was equally unwritable before this
ticket — it never had a plain text field.

The fix belongs on the configuration side rather than in the control. The control knows only how many
decimals to display, not whether the target is an `Integer`, a `Long`, a `Short` or a `Byte`; and it
serves the model-attribute side too, so widening its contract would reach beyond this ticket.
`ConfigFieldModel` holds the `PropertyDescriptor` and therefore knows the exact type, so coercing an
incoming `Number` there fixes it for every control that hands back a number.

A fractional value for an integral property is **rejected** through the same field-error mechanism
`ConfigFormatFieldModel` uses for unparsable text, not silently truncated. `ConfigSelectFieldModel`
inherits the coercion harmlessly; `ConfigFormatFieldModel` bypasses it, writing through
`getConfig().update` itself.

Implemented in commit `ba0f745ba2`, with tests in `TestConfigFieldModel`.

---

## Self-Review

**Spec coverage** (section 1 of the design document):

| Spec requirement | Task |
|---|---|
| Configurable service modelled on `FieldControlService` | 4 |
| Step 1 — annotation on the property naming the control | 2 (annotation + SPI), 4 (resolution) |
| Step 2 — option list via `Fields.optionProvider`, not just enums | 3, 4 |
| Step 3 — configured mapping by Java value type, extensible from outside | 4 (`Config.getProviders()`), 5 (registration) |
| Step 4 — fallback by value type | 4 |
| `ConfigurationValueProvider` for parsing and formatting; parse failure as field error | 1 |
| `Date` → date/time picker | 4 |
| `ResKey` → I18N control | **gap, see below** |
| `Class` and instance-valued properties → implementation select | **gap, see below** |
| `@Encrypted` → password field | 4 (step 5) |
| Fixes the same defect in the view designer | 6 |

Two spec items are deliberately **not** in this plan, and the reason is the same for both: the control they need cannot be reached from `com.top_logic.layout.configedit`.

- **`ResKey` → I18N control.** The control is `ReactI18NStringInputControl`, used by `com.top_logic.layout.view.form.I18NStringControlProvider`. Confirm where it lives with `grep -rn "class ReactI18NStringInputControl" --include="*.java" .`. If it sits in `com.top_logic.layout.react`, add it to Task 4's fallback with its own test case. If it sits in `com.top_logic.layout.view` or above, it must be contributed from there through `Config.getProviders()` — a one-entry addition to `tl-layout-view.conf.config.xml`, which belongs in the plan for spec section 4 (entry points), because that is when `tl-layout-view` gains configuration for this service anyway. Until then a `ResKey` property is edited as text through its format, which is correct behaviour, just not the nicest.
- **`Class` and instance-valued properties → implementation select.** `PolymorphicOptions` already builds these option lists but its `Choices` type is shaped for `ITEM` properties. Whether a `PLAIN` property of type `Class` can reuse it needs reading `PolymorphicOptions` in full (215 lines). Task 3's option resolution already covers the case **if** `Fields.optionProvider` returns a provider for such a property — `TestConfigControlService` should be extended with a `Class`-valued property to find out, and the finding decides whether extra code is needed. Add that test case in Task 4 Step 1 and let its result stand: a passing assertion means the spec item is done, a failing one means it moves to its own task in the next plan.

**Placeholder scan:** no `TBD`, no "add error handling", no "similar to Task N". Every code step carries the code. The three "likely fixes" notes name the exact command to run and the exact decision to make, which is verification guidance, not a placeholder.

**Type consistency:** `ConfigFieldModel.getProperty()` is used in Tasks 1, 4; `ConfigControlProvider.createControl(ReactContext, ConfigFieldModel)` is defined in Task 2 and used in Task 4; `ConfigPropertyOptions.optionsFor(ConfigurationItem, PropertyDescriptor)` / `optionProvider(PropertyDescriptor)` / `optionLabels(PropertyDescriptor)` are defined in Task 3 and used with those signatures in Task 4; `ConfigControlService.createModel` / `createControl` / `getInstance` / `Module.INSTANCE` are defined in Task 4 and used in Tasks 4, 5. `ConfigFormatFieldModel(ConfigurationItem, PropertyDescriptor)` is defined in Task 1 and used in Task 4.
