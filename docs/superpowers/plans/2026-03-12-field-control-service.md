# FieldControlService Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the static `FieldControlFactory` with a configurable `FieldControlService` that resolves attribute-to-control mappings via TLAnnotations, a global service map, and a primitive-kind fallback.

**Architecture:** A `ConfiguredManagedClass` service with a `TLModelPartRef`-keyed provider map. A new `TLInputControl` annotation allows per-attribute and per-type overrides. Six stateless `ReactFieldControlProvider` implementations wrap existing React controls.

**Tech Stack:** Java 17, TopLogic TypedConfiguration, TopLogic TLAnnotation framework, React controls via `com.top_logic.layout.react`.

**Spec:** `docs/superpowers/specs/2026-03-12-field-control-service-design.md`

---

## File Map

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `com.top_logic.layout.view/.../form/ReactFieldControlProvider.java` | Functional interface |
| Create | `com.top_logic.layout.view/.../form/TLInputControl.java` | TLAnnotation |
| Create | `com.top_logic.layout.view/.../form/FieldControlService.java` | Service with resolution chain |
| Create | `com.top_logic.layout.view/.../form/CheckboxControlProvider.java` | BOOLEAN provider |
| Create | `com.top_logic.layout.view/.../form/NumberInputControlProvider.java` | INT/FLOAT provider |
| Create | `com.top_logic.layout.view/.../form/DatePickerControlProvider.java` | DATE provider |
| Create | `com.top_logic.layout.view/.../form/TextInputControlProvider.java` | STRING/default provider |
| Create | `com.top_logic.layout.view/.../form/ColorInputControlProvider.java` | Color provider |
| Create | `com.top_logic.layout.view/.../form/IconSelectControlProvider.java` | ThemeImage/Icon provider |
| Modify | `com.top_logic.layout.view/.../form/AttributeFieldControl.java` | Switch to service |
| Modify | `com.top_logic.layout.view/.../conf/tl-layout-view.conf.config.xml` | Register service + module |
| Delete | `com.top_logic.layout.view/.../form/FieldControlFactory.java` | Replaced by service |

All `...` paths expand to `src/main/java/com/top_logic/layout/view`.

---

## Task 1: Create ReactFieldControlProvider interface

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ReactFieldControlProvider.java`

- [ ] **Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Creates a {@link ReactControl} for editing a model attribute.
 *
 * <p>
 * Implementations are instantiated by {@link FieldControlService} based on the
 * {@link TLInputControl} annotation or the service's global type-to-provider map.
 * </p>
 */
@FunctionalInterface
public interface ReactFieldControlProvider {

	/**
	 * Creates an input control for the given attribute.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute whose type can be inspected for additional metadata.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @return A React control for the field input widget.
	 */
	ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model);

}
```

- [ ] **Step 2: Verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ReactFieldControlProvider.java
git commit -m "Ticket #29108: Add ReactFieldControlProvider functional interface."
```

---

## Task 2: Create TLInputControl annotation

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TLInputControl.java`

**Reference:** `com.top_logic.element/src/main/java/com/top_logic/element/config/annotation/TLOptions.java` for the `@InApp` + `PolymorphicConfiguration` pattern.

- [ ] **Step 1: Create the annotation**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAttributeAnnotation} configuring the React input control for a model attribute or type.
 *
 * <p>
 * When set on an attribute, it overrides the default control selection. When set on a type, it
 * provides the default control for all attributes of that type (via
 * {@link DefaultStrategy.Strategy#VALUE_TYPE}).
 * </p>
 *
 * @see FieldControlService
 */
@TagName("input-control")
@InApp
@DefaultStrategy(Strategy.VALUE_TYPE)
public interface TLInputControl extends TLAttributeAnnotation, TLTypeAnnotation {

	/** @see #getImpl() */
	String IMPL_PROPERTY = "impl";

	/**
	 * The control provider implementation.
	 */
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();

	/** @see #getImpl() */
	void setImpl(PolymorphicConfiguration<? extends ReactFieldControlProvider> value);

}
```

- [ ] **Step 2: Verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TLInputControl.java
git commit -m "Ticket #29108: Add TLInputControl annotation for per-attribute/type control configuration."
```

---

## Task 3: Create six built-in provider implementations

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CheckboxControlProvider.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/NumberInputControlProvider.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/DatePickerControlProvider.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TextInputControlProvider.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ColorInputControlProvider.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/IconSelectControlProvider.java`

- [ ] **Step 1: Create CheckboxControlProvider**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for boolean attributes.
 */
public class CheckboxControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactCheckboxControl(context, model);
	}

}
```

- [ ] **Step 2: Create NumberInputControlProvider**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;

/**
 * {@link ReactFieldControlProvider} for integer and floating-point attributes.
 *
 * <p>
 * Inspects the attribute's {@link TLPrimitive.Kind} to determine the number of decimal places:
 * {@link TLPrimitive.Kind#INT} uses 0, {@link TLPrimitive.Kind#FLOAT} uses 2.
 * </p>
 */
public class NumberInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		int decimalPlaces = 2;
		TLType type = part.getType();
		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			if (primitive.getKind() == TLPrimitive.Kind.INT) {
				decimalPlaces = 0;
			}
		}
		return new ReactNumberInputControl(context, model, decimalPlaces);
	}

}
```

- [ ] **Step 3: Create DatePickerControlProvider**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for date attributes.
 */
public class DatePickerControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactDatePickerControl(context, model);
	}

}
```

- [ ] **Step 4: Create TextInputControlProvider**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for string, tristate, binary, and other text-representable
 * attributes.
 *
 * <p>
 * Also serves as the ultimate fallback when no other provider matches.
 * </p>
 */
public class TextInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactTextInputControl(context, model);
	}

}
```

- [ ] **Step 5: Create ColorInputControlProvider**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactColorInputControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for color attributes.
 */
public class ColorInputControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactColorInputControl(context, model);
	}

}
```

- [ ] **Step 6: Create IconSelectControlProvider**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactIconSelectControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for icon/theme-image attributes.
 */
public class IconSelectControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		return new ReactIconSelectControl(context, model);
	}

}
```

- [ ] **Step 7: Verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/CheckboxControlProvider.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/NumberInputControlProvider.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/DatePickerControlProvider.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/TextInputControlProvider.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/ColorInputControlProvider.java \
       com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/IconSelectControlProvider.java
git commit -m "Ticket #29108: Add six built-in ReactFieldControlProvider implementations."
```

---

## Task 4: Create FieldControlService

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlService.java`

**Reference:** `com.top_logic/src/main/java/com/top_logic/model/annotate/util/AttributeSettings.java` for `ConfiguredManagedClass` + `TypedRuntimeModule` pattern.

- [ ] **Step 1: Create the service**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Service that resolves the appropriate {@link ReactFieldControlProvider} for a model attribute.
 *
 * <p>
 * Resolution chain:
 * </p>
 * <ol>
 * <li>{@link TLInputControl} annotation on the attribute or its type (via
 * {@link com.top_logic.model.annotate.DefaultStrategy.Strategy#VALUE_TYPE}).</li>
 * <li>Global type-to-provider map configured in this service.</li>
 * <li>Built-in fallback based on {@link TLPrimitive.Kind}.</li>
 * </ol>
 */
public class FieldControlService extends ConfiguredManagedClass<FieldControlService.Config> {

	/**
	 * Configuration options for {@link FieldControlService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<FieldControlService> {

		/**
		 * Global type-to-provider mappings keyed by model type reference.
		 */
		@Key(ProviderMapping.TYPE)
		Map<TLModelPartRef, ProviderMapping> getProviders();

	}

	/**
	 * A single type-to-provider mapping entry.
	 */
	public interface ProviderMapping extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/**
		 * The model type this mapping applies to.
		 */
		@Name(TYPE)
		TLModelPartRef getType();

		/**
		 * The control provider to use for attributes of this type.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();

	}

	private final InstantiationContext _context;

	private Map<String, ReactFieldControlProvider> _providerByTypeName;

	private final ReactFieldControlProvider _checkboxProvider = new CheckboxControlProvider();

	private final ReactFieldControlProvider _numberProvider = new NumberInputControlProvider();

	private final ReactFieldControlProvider _dateProvider = new DatePickerControlProvider();

	private final ReactFieldControlProvider _textProvider = new TextInputControlProvider();

	/**
	 * Creates a {@link FieldControlService} from configuration.
	 */
	@CalledByReflection
	public FieldControlService(InstantiationContext context, Config config) {
		super(context, config);
		_context = context;
	}

	@Override
	protected void startUp() {
		super.startUp();

		_providerByTypeName = new HashMap<>();
		for (ProviderMapping mapping : getConfig().getProviders().values()) {
			TLModelPartRef typeRef = mapping.getType();
			if (typeRef != null) {
				TLType type = typeRef.resolveType();
				if (type != null) {
					ReactFieldControlProvider provider = _context.getInstance(mapping.getImpl());
					_providerByTypeName.put(type.getName(), provider);
				}
			}
		}
	}

	/**
	 * Resolves and creates the appropriate input control for the given attribute.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @return A React control for the field input widget.
	 */
	public ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		// 1. Annotation on attribute (includes type-level default via VALUE_TYPE strategy).
		TLInputControl annotation = part.getAnnotation(TLInputControl.class);
		if (annotation != null) {
			ReactFieldControlProvider provider = _context.getInstance(annotation.getImpl());
			return provider.createControl(context, part, model);
		}

		// 2. Global service map by type name.
		TLType type = part.getType();
		ReactFieldControlProvider mapped = _providerByTypeName.get(type.getName());
		if (mapped != null) {
			return mapped.createControl(context, part, model);
		}

		// 3. Built-in primitive-kind fallback.
		return primitiveFallback(context, part, model);
	}

	private ReactControl primitiveFallback(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		TLType type = part.getType();
		if (type instanceof TLPrimitive) {
			TLPrimitive primitive = (TLPrimitive) type;
			switch (primitive.getKind()) {
				case BOOLEAN:
					return _checkboxProvider.createControl(context, part, model);
				case INT:
				case FLOAT:
					return _numberProvider.createControl(context, part, model);
				case DATE:
					return _dateProvider.createControl(context, part, model);
				case STRING:
				case TRISTATE:
				case BINARY:
				case CUSTOM:
				default:
					return _textProvider.createControl(context, part, model);
			}
		}
		return _textProvider.createControl(context, part, model);
	}

	/**
	 * The {@link FieldControlService} singleton.
	 */
	public static FieldControlService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link FieldControlService}.
	 */
	public static final class Module extends TypedRuntimeModule<FieldControlService> {

		/**
		 * Singleton {@link FieldControlService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<FieldControlService> getImplementation() {
			return FieldControlService.class;
		}

	}

}
```

- [ ] **Step 2: Verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlService.java
git commit -m "Ticket #29108: Add FieldControlService with annotation/map/fallback resolution chain."
```

---

## Task 5: Register the service and configure default providers

**Files:**
- Modify: `com.top_logic.layout.view/src/main/webapp/WEB-INF/conf/tl-layout-view.conf.config.xml`

**Reference:** `com.top_logic.layout.react/src/main/webapp/WEB-INF/conf/tl-layout-react.conf.config.xml` for the ModuleSystem registration pattern.

- [ ] **Step 1: Update config XML to register module and configure default providers**

Replace the current content of `tl-layout-view.conf.config.xml` with:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<services>
		<config service-class="com.top_logic.basic.module.ModuleSystem">
			<instance class="com.top_logic.basic.module.ModuleSystem">
				<modules>
					<module key="com.top_logic.layout.view.form.FieldControlService$Module"
						value="true"
					/>
				</modules>
			</instance>
		</config>

		<config service-class="com.top_logic.layout.view.form.FieldControlService">
			<instance>
				<providers>
					<provider type="tl.core:Icon">
						<impl class="com.top_logic.layout.view.form.IconSelectControlProvider"/>
					</provider>
					<provider type="tl.core:Color">
						<impl class="com.top_logic.layout.view.form.ColorInputControlProvider"/>
					</provider>
				</providers>
			</instance>
		</config>
	</services>

	<configs>
		<config config:interface="com.top_logic.base.accesscontrol.ApplicationPages$Config">
			<startPage>/view/</startPage>
		</config>
	</configs>
</application>
```

- [ ] **Step 2: Commit**

```
git add com.top_logic.layout.view/src/main/webapp/WEB-INF/conf/tl-layout-view.conf.config.xml
git commit -m "Ticket #29108: Register FieldControlService module and default type-to-provider mappings."
```

---

## Task 6: Wire up AttributeFieldControl and remove FieldControlFactory

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java`
- Delete: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java`

- [ ] **Step 1: Update AttributeFieldControl to use FieldControlService**

In `AttributeFieldControl.java`, replace the two calls to `FieldControlFactory.createFieldControl`:

Line 104: `_innerControl = FieldControlFactory.createFieldControl(_context, part, _model);`
Replace with: `_innerControl = FieldControlService.getInstance().createFieldControl(_context, part, _model);`

Line 137: `_innerControl = FieldControlFactory.createFieldControl(_context, part, _model);`
Replace with: `_innerControl = FieldControlService.getInstance().createFieldControl(_context, part, _model);`

Also remove the import of `FieldControlFactory`. Keep the `ReactTextInputControl` import — it is still used in the placeholder model case (line 89).

- [ ] **Step 2: Delete FieldControlFactory.java**

```
git rm com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/FieldControlFactory.java
```

- [ ] **Step 3: Verify compilation**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn compile -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/form/AttributeFieldControl.java
git commit -m "Ticket #29108: Switch AttributeFieldControl to FieldControlService; remove FieldControlFactory."
```

---

## Task 7: Smoke test in running demo app

**Files:** None (manual verification)

- [ ] **Step 1: Build the layout.view module**

Run: `cd /home/bhu/devel/tl-engine/.worktrees/agent-b/com.top_logic.layout.view && mvn install -DskipTests=true`
Expected: BUILD SUCCESS

- [ ] **Step 2: Restart the demo app**

Use the `tl-app` skill to restart the demo application.

- [ ] **Step 3: Verify existing controls still work**

Navigate to http://localhost:8080, log in (root/root1234), open a form with attributes (string, boolean, number, date, color fields). Verify each renders with the correct input control.

- [ ] **Step 4: Verify icon select control is now active**

Find an attribute of type `tl.core:Icon` (or configure one). Verify it renders with the icon select popup picker instead of a plain text input.
