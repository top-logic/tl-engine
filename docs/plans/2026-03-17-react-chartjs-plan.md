# React Chart.js Integration - Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a React-based Chart.js control (`<chart>`) as a UIElement in a new Maven module `com.top_logic.layout.react.chartjs`, with TL-Script data sourcing, named click handlers, server-side tooltips, zoom/pan, and theme integration.

**Architecture:** New module with Java-side `ChartElement` (UIElement factory) + `ReactChartJsControl` (session-scoped ReactControl) + TypeScript `TLChart` component using `react-chartjs-2`. Data flows from TL-Script (JSON with embedded TL objects) through Java extraction (strips metadata, builds lookup tables) to clean JSON via SSE to the React component.

**Tech Stack:** Java 17, Maven, Chart.js 4.x, react-chartjs-2 5.x, chartjs-plugin-zoom 2.x, TypeScript 5.7, Vite 6.0, React 19 (via tl-react-bridge)

**Spec:** `docs/superpowers/specs/2026-03-17-react-chartjs-design.md`

---

## File Structure

### New Module: `com.top_logic.layout.react.chartjs/`

```
com.top_logic.layout.react.chartjs/
  pom.xml
  package.json
  tsconfig.json
  vite.config.ts
  react-src/
    chartjs-entry.ts                          # Bundle entry, registers TLChart
    controls/
      TLChart.tsx                             # Main React chart component
      chart/
        useChartCallbacks.ts                  # Hook: injects click/legend/tooltip callbacks
        useThemeDefaults.ts                   # Hook: merges theme colors into config
        ChartTooltip.tsx                      # External tooltip renderer (server-side HTML)
  src/main/java/
    com/top_logic/layout/react/chartjs/
      ChartElement.java                       # UIElement factory
      ReactChartJsControl.java               # ReactControl with SSE state management
      ChartConfigExtractor.java              # Walks config, extracts metadata + handler refs
      ClickHandlerConfig.java                # Named ViewCommand config interface
      TooltipProviderConfig.java             # Named TL-Script tooltip config interface
      I18NConstants.java                     # I18N error message keys
      Icons.java                              # ThemeVar declarations for chart colors
    META-INF/
      web-fragment.xml                        # Module name declaration (no servlets needed)
  src/main/webapp/
    WEB-INF/conf/
      tl-layout-react-chartjs.conf.config.xml   # JSFileCompiler registration
    WEB-INF/themes/core/
      theme.xml                               # CSS registration in theme system
    script/
      tl-react-chartjs.js                     # Vite output (generated, not committed)
    style/
      tl-react-chartjs.css                    # Chart CSS
```

### Modified Files

```
tl-parent-engine/pom.xml                      # Register new module
com.top_logic.demo/pom.xml                    # Add dependency
com.top_logic.demo/.../views/app.view.xml     # Add sidebar entry
com.top_logic.demo/.../views/demo/chart-demo.view.xml  # New demo view
```

---

## Chunk 1: Maven Module + npm Scaffold

### Task 1: Create Maven module pom.xml

**Files:**
- Create: `com.top_logic.layout.react.chartjs/pom.xml`

- [ ] **Step 1: Create the pom.xml**

```xml
<?xml version="1.0"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <relativePath>../tl-parent-core/internal</relativePath>
    <groupId>com.top-logic</groupId>
    <artifactId>tl-parent-core-internal</artifactId>
    <version>7.11.0-SNAPSHOT</version>
  </parent>

  <artifactId>tl-layout-react-chartjs</artifactId>
  <name>${project.artifactId}</name>
  <description>React-based Chart.js integration for TopLogic views.</description>
  <url>https://github.com/top-logic/tl-engine/</url>

  <dependencies>
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-view</artifactId>
    </dependency>

    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-basic</artifactId>
      <type>test-jar</type>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>1.15.4</version>

        <executions>
          <execution>
            <id>install-node</id>
            <goals>
              <goal>install-node-and-npm</goal>
            </goals>
            <configuration>
              <nodeVersion>v20.10.0</nodeVersion>
            </configuration>
          </execution>

          <execution>
            <id>npm-install</id>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>install</arguments>
            </configuration>
          </execution>

          <execution>
            <id>npm-build</id>
            <phase>generate-resources</phase>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>run build</arguments>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

**Note:** `tl-layout-view` transitively provides `tl-layout-react`, `tl-core`, and `tl-model-search`. No need to declare them separately.

- [ ] **Step 2: Register in tl-parent-engine/pom.xml**

Add `<module>../com.top_logic.layout.react.chartjs</module>` after the `../com.top_logic.layout.view` entry (around line 87).

- [ ] **Step 3: Create web-fragment.xml**

Create `com.top_logic.layout.react.chartjs/src/main/java/META-INF/web-fragment.xml`:

```xml
<web-fragment xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
    http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"
    version="3.0" metadata-complete="true">
  <name>tl-layout-react-chartjs</name>
</web-fragment>
```

- [ ] **Step 4: Create JSFileCompiler config**

Create `com.top_logic.layout.react.chartjs/src/main/webapp/WEB-INF/conf/tl-layout-react-chartjs.conf.config.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <services>
    <config service-class="com.top_logic.gui.JSFileCompiler">
      <instance>
        <additional-files>
          <file resource="tl-react-chartjs.js" type="module" />
        </additional-files>
      </instance>
    </config>
  </services>
</application>
```

- [ ] **Step 5: Commit**

```
Ticket #29108: Create Maven module scaffold for com.top_logic.layout.react.chartjs.
```

---

### Task 2: Create npm + Vite scaffold

**Files:**
- Create: `com.top_logic.layout.react.chartjs/package.json`
- Create: `com.top_logic.layout.react.chartjs/tsconfig.json`
- Create: `com.top_logic.layout.react.chartjs/vite.config.ts`
- Create: `com.top_logic.layout.react.chartjs/react-src/chartjs-entry.ts`

- [ ] **Step 1: Create package.json**

```json
{
  "name": "tl-react-chartjs",
  "version": "7.11.0",
  "private": true,
  "scripts": {
    "build": "vite build"
  },
  "dependencies": {
    "chart.js": "^4.4.7",
    "react-chartjs-2": "^5.2.0",
    "chartjs-plugin-zoom": "^2.2.0"
  },
  "devDependencies": {
    "@types/react": "^19.0.0",
    "@vitejs/plugin-react": "^4.3.0",
    "typescript": "^5.7.0",
    "vite": "^6.0.0"
  }
}
```

- [ ] **Step 2: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "jsx": "react-jsx",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "outDir": "dist",
    "paths": {
      "tl-react-bridge": ["../com.top_logic.layout.react/react-src/bridge-entry.ts"]
    }
  },
  "include": ["react-src"]
}
```

- [ ] **Step 3: Create vite.config.ts**

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    lib: {
      entry: 'react-src/chartjs-entry.ts',
      fileName: () => 'tl-react-chartjs.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
```

**Critical:** `external: ['tl-react-bridge']` prevents bundling a second React copy.

- [ ] **Step 4: Create minimal entry file**

Create `com.top_logic.layout.react.chartjs/react-src/chartjs-entry.ts`:

```typescript
// Chart.js control registration for the tl-react-chartjs bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';

// Placeholder — TLChart will be added in Task 5.
// import TLChart from './controls/TLChart';
// register('TLChart', TLChart);

console.log('tl-react-chartjs loaded');
```

- [ ] **Step 5: Build to verify**

Run from project root:
```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react.chartjs
```

Expected: BUILD SUCCESS. `src/main/webapp/script/tl-react-chartjs.js` generated.

- [ ] **Step 6: Commit**

```
Ticket #29108: Add npm/Vite scaffold for React Chart.js module.
```

---

## Chunk 2: Java Configuration Interfaces

### Task 3: Create ClickHandlerConfig and TooltipProviderConfig

**Files:**
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/ClickHandlerConfig.java`
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/TooltipProviderConfig.java`

- [ ] **Step 1: Create ClickHandlerConfig**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.command.ViewCommand;

/**
 * Configuration for a named click handler that executes a {@link ViewCommand}.
 *
 * <p>
 * The {@link #getName() name} is referenced from TL-Script dataset output via
 * {@code onClick} / {@code onLegendClick} keys to connect chart interactions
 * with server-side commands.
 * </p>
 */
public interface ClickHandlerConfig extends ConfigurationItem {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getAction()}. */
	String ACTION = "action";

	/**
	 * The handler name, referenced from TL-Script dataset {@code onClick} / {@code onLegendClick}.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * The command to execute when the click occurs. Receives the metadata TL object as argument.
	 */
	@Mandatory
	@Name(ACTION)
	PolymorphicConfiguration<? extends ViewCommand> getAction();
}
```

- [ ] **Step 2: Create TooltipProviderConfig**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration for a named tooltip provider.
 *
 * <p>
 * The {@link #getName() name} is referenced from TL-Script dataset output via the
 * {@code tooltip} key. When a tooltip is requested, the {@link #getExpr() expression}
 * is evaluated with the metadata TL object and returns an HTML string.
 * </p>
 */
public interface TooltipProviderConfig extends ConfigurationItem {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/**
	 * The tooltip name, referenced from TL-Script dataset {@code tooltip} key.
	 */
	@Mandatory
	@Name(NAME)
	String getName();

	/**
	 * TL-Script expression that receives the metadata TL object and returns an HTML string.
	 */
	@Mandatory
	@Name(EXPR)
	Expr getExpr();
}
```

- [ ] **Step 3: Compile to verify**

```bash
mvn compile -DskipTests=true -pl com.top_logic.layout.react.chartjs
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```
Ticket #29108: Add ClickHandlerConfig and TooltipProviderConfig interfaces.
```

---

### Task 4: Create I18NConstants and ChartElement

**Files:**
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/I18NConstants.java`
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/ChartElement.java`

- [ ] **Step 1: Create I18NConstants**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the Chart.js React integration.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid chart configuration: {0}
	 */
	public static ResKey1 ERROR_INVALID_CHART_CONFIG__DETAIL;

	/**
	 * @en Non-primitive object found at path {0} outside of datasets[].metadata.
	 */
	public static ResKey1 ERROR_OBJECT_OUTSIDE_METADATA__PATH;

	/**
	 * @en Unknown handler reference ''{0}''. Check that a handler with this name is configured.
	 */
	public static ResKey1 ERROR_UNKNOWN_HANDLER__NAME;

	/**
	 * @en Unknown tooltip reference ''{0}''. Check that a tooltip with this name is configured.
	 */
	public static ResKey1 ERROR_UNKNOWN_TOOLTIP__NAME;

	/**
	 * @en No data available.
	 */
	public static ResKey NO_DATA;

	static {
		initConstants(I18NConstants.class);
	}
}
```

- [ ] **Step 2: Create ChartElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link UIElement} that renders a Chart.js chart via the React control layer.
 *
 * <p>
 * The chart data is computed by a TL-Script expression ({@link Config#getData()}) that
 * receives the input channel values as arguments. The expression returns a Chart.js
 * configuration map that may contain TL objects in {@code datasets[].metadata}.
 * </p>
 *
 * @see ReactChartJsControl
 */
public class ChartElement implements UIElement {

	/**
	 * Configuration for {@link ChartElement}.
	 */
	@TagName("chart")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ChartElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getData()}. */
		String DATA = "data";

		/** Configuration name for {@link #getHandlers()}. */
		String HANDLERS = "handlers";

		/** Configuration name for {@link #getTooltips()}. */
		String TOOLTIPS = "tooltips";

		/** Configuration name for {@link #getZoomEnabled()}. */
		String ZOOM_ENABLED = "zoomEnabled";

		/** Configuration name for {@link #getCSSClass()}. */
		String CSS_CLASS = "cssClass";

		/** Configuration name for {@link #getNoDataMessage()}. */
		String NO_DATA_MESSAGE = "noDataMessage";

		/**
		 * Input channels whose values are passed as arguments to the {@link #getData()} function.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script expression producing the Chart.js configuration map.
		 *
		 * <p>
		 * Receives as many arguments as {@link #getInputs() input channels} are declared.
		 * Must return a map with keys {@code type}, {@code data}, and optionally {@code options}.
		 * Datasets may contain {@code metadata} arrays with TL objects, and {@code onClick},
		 * {@code onLegendClick}, {@code tooltip} string keys referencing configured handlers.
		 * </p>
		 */
		@Mandatory
		@Name(DATA)
		Expr getData();

		/**
		 * Named click handlers. Each handler has a {@link ClickHandlerConfig#getName() name}
		 * that is referenced from dataset {@code onClick} / {@code onLegendClick} keys.
		 */
		@Name(HANDLERS)
		@EntryTag("handler")
		List<ClickHandlerConfig> getHandlers();

		/**
		 * Named tooltip providers. Each provider has a {@link TooltipProviderConfig#getName() name}
		 * that is referenced from dataset {@code tooltip} keys.
		 */
		@Name(TOOLTIPS)
		@EntryTag("tooltip")
		List<TooltipProviderConfig> getTooltips();

		/**
		 * Whether zoom and pan interaction is enabled.
		 */
		@Name(ZOOM_ENABLED)
		@BooleanDefault(false)
		boolean getZoomEnabled();

		/**
		 * Additional CSS class for the chart container element.
		 */
		@Name(CSS_CLASS)
		@Nullable
		String getCSSClass();

		/**
		 * Message shown when the chart has no data. If {@code null}, an empty chart is shown.
		 */
		@Name(NO_DATA_MESSAGE)
		@Nullable
		ResKey getNoDataMessage();
	}

	private final QueryExecutor _dataFun;

	private final Map<String, ClickHandlerConfig> _handlers;

	private final Map<String, QueryExecutor> _tooltips;

	private final List<ChannelRef> _inputs;

	private final boolean _zoomEnabled;

	private final String _cssClass;

	private final ResKey _noDataMessage;

	/**
	 * Creates a new {@link ChartElement} from configuration.
	 */
	@CalledByReflection
	public ChartElement(InstantiationContext context, Config config) {
		_dataFun = QueryExecutor.compile(config.getData());
		_inputs = config.getInputs();
		_zoomEnabled = config.getZoomEnabled();
		_cssClass = config.getCSSClass();
		_noDataMessage = config.getNoDataMessage();

		_handlers = new HashMap<>();
		for (ClickHandlerConfig handler : config.getHandlers()) {
			_handlers.put(handler.getName(), handler);
		}

		_tooltips = new HashMap<>();
		for (TooltipProviderConfig tooltip : config.getTooltips()) {
			_tooltips.put(tooltip.getName(), QueryExecutor.compile(tooltip.getExpr()));
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<ViewChannel> channels = _inputs.stream()
			.map(ref -> context.resolveChannel(ref))
			.toList();

		Object[] inputValues = channels.stream()
			.map(ViewChannel::get)
			.toArray();

		ReactChartJsControl control = new ReactChartJsControl(
			context, _dataFun, _handlers, _tooltips,
			_zoomEnabled, _cssClass, _noDataMessage,
			inputValues);

		// Re-evaluate on channel changes.
		for (ViewChannel channel : channels) {
			ViewChannel.ChannelListener listener = (sender, oldVal, newVal) -> {
				Object[] values = channels.stream().map(ViewChannel::get).toArray();
				control.updateChartData(values);
			};
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}

		return control;
	}
}
```

**Note:** `@EntryTag` import: `com.top_logic.basic.config.annotation.EntryTag`.

- [ ] **Step 3: Compile to verify**

```bash
mvn compile -DskipTests=true -pl com.top_logic.layout.react.chartjs
```

Expected: Compilation errors for missing `ReactChartJsControl` — expected at this stage.

- [ ] **Step 4: Commit** (even with compile errors, the element is self-contained)

```
Ticket #29108: Add ChartElement UIElement and I18NConstants.
```

---

## Chunk 3: Chart Config Extraction + ReactControl

### Task 5: Create ChartConfigExtractor

**Files:**
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/ChartConfigExtractor.java`

This is the core logic: walks the TL-Script output, extracts metadata and handler references, validates, and produces clean JSON.

- [ ] **Step 1: Create ChartConfigExtractor**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Extracts TL objects and handler/tooltip references from a TL-Script-produced
 * Chart.js configuration, producing clean JSON for the client.
 *
 * <p>
 * The TL-Script data function may return datasets containing:
 * </p>
 * <ul>
 *   <li>{@code metadata} — array of {@link TLObject}s parallel to the data array</li>
 *   <li>{@code onClick} — string referencing a configured click handler name</li>
 *   <li>{@code onLegendClick} — string referencing a configured click handler name</li>
 *   <li>{@code tooltip} — string referencing a configured tooltip provider name</li>
 * </ul>
 *
 * <p>
 * After extraction, these keys are removed from the config. Any {@link TLObject} found
 * outside of {@code datasets[].metadata} causes a {@link TopLogicException}.
 * </p>
 */
public class ChartConfigExtractor {

	/** Keys extracted from datasets and removed before sending to client. */
	private static final Set<String> DATASET_EXTRACT_KEYS =
		Set.of("metadata", "onClick", "onLegendClick", "tooltip");

	private final Map<String, ClickHandlerConfig> _configuredHandlers;

	private final Set<String> _configuredTooltips;

	/** Extracted metadata: metadata[datasetIndex][dataIndex] -> TLObject. */
	private final List<List<TLObject>> _metadata = new ArrayList<>();

	/** Extracted handler mapping: handlerMap[datasetIndex] -> { onClick, onLegendClick }. */
	private final List<Map<String, String>> _handlerRefs = new ArrayList<>();

	/** Extracted tooltip mapping: tooltipRef[datasetIndex] -> tooltip name. */
	private final List<String> _tooltipRefs = new ArrayList<>();

	/** The cleaned config (no TL objects, no handler/tooltip keys). */
	private Map<String, Object> _cleanConfig;

	/**
	 * Creates a new extractor.
	 *
	 * @param configuredHandlers
	 *        The click handlers declared in the view configuration.
	 * @param configuredTooltips
	 *        The tooltip provider names declared in the view configuration.
	 */
	public ChartConfigExtractor(Map<String, ClickHandlerConfig> configuredHandlers,
			Set<String> configuredTooltips) {
		_configuredHandlers = configuredHandlers;
		_configuredTooltips = configuredTooltips;
	}

	/**
	 * Extracts metadata and handler references from the given raw chart config.
	 *
	 * @param rawConfig
	 *        The chart configuration as returned by the TL-Script function.
	 * @throws TopLogicException
	 *         if the config is invalid.
	 */
	@SuppressWarnings("unchecked")
	public void extract(Object rawConfig) {
		if (!(rawConfig instanceof Map)) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CHART_CONFIG__DETAIL.fill("Expected a map, got: " + rawConfig));
		}

		Map<String, Object> config = new HashMap<>((Map<String, Object>) rawConfig);

		Object type = config.get("type");
		if (!(type instanceof CharSequence)) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CHART_CONFIG__DETAIL.fill("Missing or invalid 'type' key."));
		}

		Object data = config.get("data");
		if (!(data instanceof Map)) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_CHART_CONFIG__DETAIL.fill("Missing or invalid 'data' key."));
		}

		Map<String, Object> dataMap = new HashMap<>((Map<String, Object>) data);
		Object datasets = dataMap.get("datasets");
		if (datasets instanceof List) {
			List<?> datasetList = (List<?>) datasets;
			List<Object> cleanDatasets = new ArrayList<>(datasetList.size());
			for (int i = 0; i < datasetList.size(); i++) {
				cleanDatasets.add(extractDataset(i, datasetList.get(i)));
			}
			dataMap.put("datasets", cleanDatasets);
		}

		config.put("data", dataMap);

		// Validate: no TL objects remaining anywhere in the config.
		validateNoTLObjects(config, "");

		_cleanConfig = config;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractDataset(int index, Object dataset) {
		if (!(dataset instanceof Map)) {
			return Map.of();
		}

		Map<String, Object> dsMap = new HashMap<>((Map<String, Object>) dataset);

		// Extract metadata.
		List<TLObject> metadataList = new ArrayList<>();
		Object metadata = dsMap.remove("metadata");
		if (metadata instanceof List) {
			for (Object item : (List<?>) metadata) {
				if (item instanceof TLObject) {
					metadataList.add((TLObject) item);
				} else if (item == null) {
					metadataList.add(null);
				} else {
					throw new TopLogicException(
						I18NConstants.ERROR_OBJECT_OUTSIDE_METADATA__PATH.fill(
							"datasets[" + index + "].metadata: expected TLObject, got " + item.getClass().getName()));
				}
			}
		}
		_metadata.add(metadataList);

		// Extract handler references.
		Map<String, String> handlers = new HashMap<>();
		extractHandlerRef(dsMap, "onClick", handlers);
		extractHandlerRef(dsMap, "onLegendClick", handlers);
		_handlerRefs.add(handlers);

		// Extract tooltip reference.
		String tooltipRef = null;
		Object tooltip = dsMap.remove("tooltip");
		if (tooltip instanceof String) {
			tooltipRef = (String) tooltip;
			if (!_configuredTooltips.contains(tooltipRef)) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_TOOLTIP__NAME.fill(tooltipRef));
			}
		}
		_tooltipRefs.add(tooltipRef);

		return dsMap;
	}

	private void extractHandlerRef(Map<String, Object> dsMap, String key, Map<String, String> handlers) {
		Object ref = dsMap.remove(key);
		if (ref instanceof String) {
			String name = (String) ref;
			if (!_configuredHandlers.containsKey(name)) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_HANDLER__NAME.fill(name));
			}
			handlers.put(key, name);
		}
	}

	@SuppressWarnings("unchecked")
	private void validateNoTLObjects(Object value, String path) {
		if (value instanceof TLObject) {
			throw new TopLogicException(
				I18NConstants.ERROR_OBJECT_OUTSIDE_METADATA__PATH.fill(path));
		}
		if (value instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				validateNoTLObjects(entry.getValue(), path + "." + entry.getKey());
			}
		}
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			for (int i = 0; i < list.size(); i++) {
				validateNoTLObjects(list.get(i), path + "[" + i + "]");
			}
		}
	}

	/** The cleaned Chart.js config (safe for client). */
	public Map<String, Object> getCleanConfig() {
		return _cleanConfig;
	}

	/** Metadata lookup: metadata[datasetIndex][dataIndex]. */
	public List<List<TLObject>> getMetadata() {
		return _metadata;
	}

	/** Handler references per dataset. */
	public List<Map<String, String>> getHandlerRefs() {
		return _handlerRefs;
	}

	/** Tooltip provider name per dataset (null if none). */
	public List<String> getTooltipRefs() {
		return _tooltipRefs;
	}

	/**
	 * Builds the interaction descriptor sent to the client.
	 */
	public Map<String, Object> buildInteractions() {
		List<Map<String, Object>> dsInteractions = new ArrayList<>();
		for (int i = 0; i < _handlerRefs.size(); i++) {
			Map<String, String> handlers = _handlerRefs.get(i);
			String tooltip = i < _tooltipRefs.size() ? _tooltipRefs.get(i) : null;
			dsInteractions.add(Map.of(
				"clickable", handlers.containsKey("onClick"),
				"legendClickable", handlers.containsKey("onLegendClick"),
				"hasTooltip", tooltip != null
			));
		}
		return Map.of("datasets", dsInteractions);
	}
}
```

- [ ] **Step 2: Compile to verify**

```bash
mvn compile -DskipTests=true -pl com.top_logic.layout.react.chartjs
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add ChartConfigExtractor for metadata and handler extraction.
```

---

### Task 6: Create ReactChartJsControl

**Files:**
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/ReactChartJsControl.java`

- [ ] **Step 1: Create ReactChartJsControl**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ReactControl} that renders a Chart.js chart via the {@code TLChart} React component.
 *
 * <p>
 * Manages the server-side state including metadata lookup tables, handler mappings,
 * and tooltip providers. Communicates with the client via SSE state/patch events.
 * </p>
 *
 * @see ChartElement
 * @see ChartConfigExtractor
 */
public class ReactChartJsControl extends ReactControl {

	private static final String CHART_CONFIG = "chartConfig";
	private static final String INTERACTIONS = "interactions";
	private static final String THEME_COLORS = "themeColors";
	private static final String ZOOM_ENABLED = "zoomEnabled";
	private static final String CSS_CLASS = "cssClass";
	private static final String ERROR = "error";
	private static final String NO_DATA_MESSAGE = "noDataMessage";
	private static final String TOOLTIP_CONTENT = "tooltipContent";

	private final QueryExecutor _dataFun;

	private final Map<String, ClickHandlerConfig> _handlers;

	private final Map<String, QueryExecutor> _tooltips;

	private final ResKey _noDataMessage;

	/** Current metadata lookup: metadata[datasetIndex][dataIndex]. */
	private List<List<TLObject>> _metadata = Collections.emptyList();

	/** Current handler mapping per dataset. */
	private List<Map<String, String>> _handlerRefs = Collections.emptyList();

	/** Current tooltip name per dataset. */
	private List<String> _tooltipRefs = Collections.emptyList();

	/**
	 * Creates a new {@link ReactChartJsControl}.
	 */
	public ReactChartJsControl(ReactContext context, QueryExecutor dataFun,
			Map<String, ClickHandlerConfig> handlers, Map<String, QueryExecutor> tooltips,
			boolean zoomEnabled, String cssClass, ResKey noDataMessage,
			Object[] initialInputValues) {
		super(context, null, "TLChart");

		_dataFun = dataFun;
		_handlers = handlers;
		_tooltips = tooltips;
		_noDataMessage = noDataMessage;

		putState(ZOOM_ENABLED, zoomEnabled);
		if (cssClass != null) {
			putState(CSS_CLASS, cssClass);
		}

		putState(THEME_COLORS, Icons.getChartThemeColors());

		evaluateAndSetChartData(initialInputValues);
	}

	/**
	 * Re-evaluates the TL-Script data function and updates the chart state.
	 * Called when input channels change.
	 */
	public void updateChartData(Object[] inputValues) {
		evaluateAndSetChartData(inputValues);
	}

	private void evaluateAndSetChartData(Object[] inputValues) {
		try {
			Object rawConfig = _dataFun.execute(inputValues);

			ChartConfigExtractor extractor =
				new ChartConfigExtractor(_handlers, _tooltips.keySet());
			extractor.extract(rawConfig);

			_metadata = extractor.getMetadata();
			_handlerRefs = extractor.getHandlerRefs();
			_tooltipRefs = extractor.getTooltipRefs();

			putState(CHART_CONFIG, extractor.getCleanConfig());
			putState(INTERACTIONS, extractor.buildInteractions());
			putState(ERROR, null);

			if (_noDataMessage != null) {
				putState(NO_DATA_MESSAGE,
					com.top_logic.util.Resources.getInstance().getString(_noDataMessage));
			}
		} catch (TopLogicException ex) {
			putState(ERROR, ex.getMessage());
			putState(CHART_CONFIG, null);
		}
	}

	/**
	 * Handles click events from the React component.
	 *
	 * @param arguments
	 *        Contains {@code datasetIndex} (int), {@code index} (int), and
	 *        {@code zone} ("datapoint" or "legend").
	 */
	@ReactCommand("elementClick")
	HandlerResult handleElementClick(Map<String, Object> arguments) {
		int datasetIndex = ((Number) arguments.get("datasetIndex")).intValue();
		int index = ((Number) arguments.get("index")).intValue();
		String zone = (String) arguments.get("zone");

		String handlerKey = "legend".equals(zone) ? "onLegendClick" : "onClick";

		if (datasetIndex < 0 || datasetIndex >= _handlerRefs.size()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		Map<String, String> dsHandlers = _handlerRefs.get(datasetIndex);
		String handlerName = dsHandlers.get(handlerKey);
		if (handlerName == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		ClickHandlerConfig handlerConfig = _handlers.get(handlerName);
		if (handlerConfig == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		TLObject targetObject = resolveMetadata(datasetIndex, index);

		ViewCommand command = com.top_logic.basic.config.SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(handlerConfig.getAction());
		return command.execute(getReactContext(), targetObject);
	}

	/**
	 * Handles tooltip requests from the React component.
	 *
	 * @param arguments
	 *        Contains {@code datasetIndex} (int) and {@code index} (int).
	 */
	@ReactCommand("tooltip")
	void handleTooltip(Map<String, Object> arguments) {
		int datasetIndex = ((Number) arguments.get("datasetIndex")).intValue();
		int index = ((Number) arguments.get("index")).intValue();

		if (datasetIndex < 0 || datasetIndex >= _tooltipRefs.size()) {
			return;
		}

		String tooltipName = _tooltipRefs.get(datasetIndex);
		if (tooltipName == null) {
			return;
		}

		QueryExecutor tooltipExpr = _tooltips.get(tooltipName);
		if (tooltipExpr == null) {
			return;
		}

		TLObject targetObject = resolveMetadata(datasetIndex, index);
		if (targetObject == null) {
			return;
		}

		Object html = tooltipExpr.execute(targetObject);
		if (html != null) {
			putState(TOOLTIP_CONTENT, Map.of(
				"html", html.toString(),
				"datasetIndex", datasetIndex,
				"index", index
			));
		}
	}

	private TLObject resolveMetadata(int datasetIndex, int dataIndex) {
		if (datasetIndex < 0 || datasetIndex >= _metadata.size()) {
			return null;
		}
		List<TLObject> dsMetadata = _metadata.get(datasetIndex);
		if (dataIndex < 0 || dataIndex >= dsMetadata.size()) {
			return null;
		}
		return dsMetadata.get(dataIndex);
	}
}
```

- [ ] **Step 2: Compile to verify the full Java stack**

```bash
mvn compile -DskipTests=true -pl com.top_logic.layout.react.chartjs
```

Expected: BUILD SUCCESS (all Java classes compile).

- [ ] **Step 3: Commit**

```
Ticket #29108: Add ReactChartJsControl with click handler and tooltip support.
```

---

## Chunk 4: React Component (TLChart)

### Task 7: Create TLChart React component

**Files:**
- Create: `com.top_logic.layout.react.chartjs/react-src/controls/TLChart.tsx`
- Create: `com.top_logic.layout.react.chartjs/react-src/controls/chart/useChartCallbacks.ts`
- Create: `com.top_logic.layout.react.chartjs/react-src/controls/chart/useThemeDefaults.ts`
- Create: `com.top_logic.layout.react.chartjs/react-src/controls/chart/ChartTooltip.tsx`
- Modify: `com.top_logic.layout.react.chartjs/react-src/chartjs-entry.ts`

- [ ] **Step 1: Create useThemeDefaults hook**

```typescript
// react-src/controls/chart/useThemeDefaults.ts
import { React } from 'tl-react-bridge';

interface ThemeColors {
  palette?: string[];
  gridColor?: string;
  textColor?: string;
  backgroundColor?: string;
}

/**
 * Merges theme color defaults into a Chart.js config.
 * Explicit colors in the config take precedence.
 */
export function useThemeDefaults(
  config: any,
  themeColors: ThemeColors
): any {
  return React.useMemo(() => {
    if (!config || !themeColors) return config;

    const merged = { ...config };
    const data = merged.data ? { ...merged.data } : {};

    // Apply palette to datasets that don't have explicit colors.
    if (themeColors.palette && data.datasets) {
      data.datasets = data.datasets.map((ds: any, i: number) => {
        const color = themeColors.palette![i % themeColors.palette!.length];
        const result = { ...ds };
        if (!result.backgroundColor) {
          result.backgroundColor = color + 'cc'; // 80% opacity
        }
        if (!result.borderColor) {
          result.borderColor = color;
        }
        return result;
      });
    }

    merged.data = data;

    // Apply theme to scales and grid.
    const options = { ...merged.options };
    if (themeColors.textColor || themeColors.gridColor) {
      const scales = { ...options.scales };
      for (const axis of Object.keys(scales)) {
        scales[axis] = { ...scales[axis] };
        if (themeColors.gridColor && !scales[axis].grid?.color) {
          scales[axis].grid = { ...scales[axis].grid, color: themeColors.gridColor };
        }
        if (themeColors.textColor && !scales[axis].ticks?.color) {
          scales[axis].ticks = { ...scales[axis].ticks, color: themeColors.textColor };
        }
      }
      options.scales = scales;
    }

    merged.options = options;
    return merged;
  }, [config, themeColors]);
}
```

- [ ] **Step 2: Create useChartCallbacks hook**

```typescript
// react-src/controls/chart/useChartCallbacks.ts
import { React, useTLCommand } from 'tl-react-bridge';

interface DatasetInteraction {
  clickable: boolean;
  legendClickable: boolean;
  hasTooltip: boolean;
}

interface Interactions {
  datasets: DatasetInteraction[];
}

/**
 * Creates Chart.js callback options based on the interaction descriptor.
 */
export function useChartCallbacks(interactions: Interactions | null) {
  const sendCommand = useTLCommand();

  const hasAnyClick = interactions?.datasets.some(d => d.clickable) ?? false;
  const hasAnyLegendClick = interactions?.datasets.some(d => d.legendClickable) ?? false;
  const hasAnyTooltip = interactions?.datasets.some(d => d.hasTooltip) ?? false;

  const onClick = React.useCallback((_event: any, elements: any[]) => {
    if (elements.length > 0) {
      const { datasetIndex, index } = elements[0];
      sendCommand('elementClick', { datasetIndex, index, zone: 'datapoint' });
    }
  }, [sendCommand]);

  const onLegendClick = React.useCallback((_event: any, legendItem: any, legend: any) => {
    // Preserve default toggle behavior.
    const chart = legend.chart;
    const index = legendItem.datasetIndex;
    if (chart.isDatasetVisible(index)) {
      chart.hide(index);
      legendItem.hidden = true;
    } else {
      chart.show(index);
      legendItem.hidden = false;
    }
    // Additionally send server command.
    sendCommand('elementClick', { datasetIndex: index, index: -1, zone: 'legend' });
  }, [sendCommand]);

  // Debounce tooltip requests.
  const tooltipTimer = React.useRef<number | null>(null);
  const onTooltip = React.useCallback((datasetIndex: number, dataIndex: number) => {
    if (tooltipTimer.current != null) {
      clearTimeout(tooltipTimer.current);
    }
    tooltipTimer.current = window.setTimeout(() => {
      sendCommand('tooltip', { datasetIndex, index: dataIndex });
      tooltipTimer.current = null;
    }, 200);
  }, [sendCommand]);

  return React.useMemo(() => {
    const callbacks: any = {};

    if (hasAnyClick) {
      callbacks.onClick = onClick;
    }

    if (hasAnyLegendClick || hasAnyTooltip) {
      callbacks.plugins = {};
      if (hasAnyLegendClick) {
        callbacks.plugins.legend = { onClick: onLegendClick };
      }
    }

    return { callbacks, hasAnyTooltip, onTooltip };
  }, [hasAnyClick, hasAnyLegendClick, hasAnyTooltip, onClick, onLegendClick, onTooltip]);
}
```

- [ ] **Step 3: Create ChartTooltip component**

```typescript
// react-src/controls/chart/ChartTooltip.tsx
import { React } from 'tl-react-bridge';

interface TooltipContent {
  html: string;
  datasetIndex: number;
  index: number;
}

interface ChartTooltipProps {
  content: TooltipContent | null;
  chartRef: React.RefObject<any>;
}

/**
 * Renders server-provided tooltip HTML positioned near the data point.
 */
const ChartTooltip: React.FC<ChartTooltipProps> = ({ content, chartRef }) => {
  if (!content || !content.html) return null;

  // Position tooltip near the data point.
  let left = 0;
  let top = 0;
  const chart = chartRef.current;
  if (chart) {
    const meta = chart.getDatasetMeta(content.datasetIndex);
    if (meta && meta.data && meta.data[content.index]) {
      const point = meta.data[content.index];
      left = point.x;
      top = point.y - 10;
    }
  }

  return (
    <div
      className="tlReactChart__tooltip"
      style={{ left: left + 'px', top: top + 'px' }}
      dangerouslySetInnerHTML={{ __html: content.html }}
    />
  );
};

export default ChartTooltip;
```

- [ ] **Step 4: Create TLChart main component**

```typescript
// react-src/controls/TLChart.tsx
import { React } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import {
  Chart as ChartJS,
  registerables,
} from 'chart.js';
import { Chart } from 'react-chartjs-2';
import zoomPlugin from 'chartjs-plugin-zoom';
import { useThemeDefaults } from './chart/useThemeDefaults';
import { useChartCallbacks } from './chart/useChartCallbacks';
import ChartTooltip from './chart/ChartTooltip';

// Register all chart types + zoom plugin.
ChartJS.register(...registerables, zoomPlugin);

const TLChart: React.FC<TLCellProps> = ({ controlId, state }) => {
  const chartRef = React.useRef<ChartJS | null>(null);

  const chartConfig = state.chartConfig as any;
  const interactions = state.interactions as any;
  const themeColors = state.themeColors as any;
  const zoomEnabled = state.zoomEnabled as boolean;
  const cssClass = state.cssClass as string | null;
  const error = state.error as string | null;
  const noDataMessage = state.noDataMessage as string | null;
  const tooltipContent = state.tooltipContent as any;

  // Merge theme defaults.
  const themedConfig = useThemeDefaults(chartConfig, themeColors);

  // Build callbacks from interaction descriptor.
  const { callbacks, hasAnyTooltip, onTooltip } = useChartCallbacks(interactions);

  // Merge options: config options + callbacks + zoom.
  const options = React.useMemo(() => {
    if (!themedConfig) return {};

    const base = { ...themedConfig.options, ...callbacks };
    base.responsive = true;
    base.maintainAspectRatio = false;

    // Zoom/pan.
    if (zoomEnabled) {
      base.plugins = {
        ...base.plugins,
        zoom: {
          zoom: { wheel: { enabled: true }, pinch: { enabled: true }, mode: 'xy' as const },
          pan: { enabled: true, mode: 'xy' as const },
        },
      };
    }

    // External tooltip for server-side content.
    if (hasAnyTooltip) {
      base.plugins = {
        ...base.plugins,
        tooltip: {
          ...base.plugins?.tooltip,
          enabled: false,
          external: (context: any) => {
            const tooltip = context.tooltip;
            if (tooltip.opacity > 0 && tooltip.dataPoints?.length > 0) {
              const point = tooltip.dataPoints[0];
              onTooltip(point.datasetIndex, point.dataIndex);
            }
          },
        },
      };
    }

    // Merge legend callbacks if present.
    if (callbacks.plugins?.legend) {
      base.plugins = {
        ...base.plugins,
        legend: { ...base.plugins?.legend, ...callbacks.plugins.legend },
      };
    }

    return base;
  }, [themedConfig, callbacks, zoomEnabled, hasAnyTooltip, onTooltip]);

  // Error state.
  if (error) {
    return (
      <div id={controlId} className={'tlReactChart tlReactChart--error ' + (cssClass || '')}>
        <div className="tlReactChart__error">{error}</div>
      </div>
    );
  }

  // No data.
  if (!themedConfig?.data?.datasets?.length && noDataMessage) {
    return (
      <div id={controlId} className={'tlReactChart tlReactChart--noData ' + (cssClass || '')}>
        <div className="tlReactChart__noData">{noDataMessage}</div>
      </div>
    );
  }

  // No config yet.
  if (!themedConfig) {
    return <div id={controlId} className={'tlReactChart ' + (cssClass || '')} />;
  }

  return (
    <div id={controlId} className={'tlReactChart ' + (cssClass || '')}>
      <Chart
        ref={chartRef}
        type={themedConfig.type}
        data={themedConfig.data}
        options={options}
      />
      {zoomEnabled && (
        <button
          className="tlReactChart__zoomReset"
          onClick={() => chartRef.current?.resetZoom()}
        >
          Reset Zoom
        </button>
      )}
      <ChartTooltip content={tooltipContent} chartRef={chartRef} />
    </div>
  );
};

export default TLChart;
```

- [ ] **Step 5: Update chartjs-entry.ts to register TLChart**

Replace the placeholder content:

```typescript
// Chart.js control registration for the tl-react-chartjs bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import TLChart from './controls/TLChart';

register('TLChart', TLChart);
```

- [ ] **Step 6: Build the full module**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react.chartjs
```

Expected: BUILD SUCCESS. Both Java compilation and npm/Vite build succeed. `src/main/webapp/script/tl-react-chartjs.js` generated with Chart.js bundled.

- [ ] **Step 7: Commit**

```
Ticket #29108: Add TLChart React component with callbacks, theme, and tooltip support.
```

---

## Chunk 5: CSS + Demo View

### Task 8: Create chart CSS, theme registration, and Icons.java

**Files:**
- Create: `com.top_logic.layout.react.chartjs/src/main/webapp/style/tl-react-chartjs.css`
- Create: `com.top_logic.layout.react.chartjs/src/main/webapp/WEB-INF/themes/core/theme.xml`
- Create: `com.top_logic.layout.react.chartjs/src/main/java/com/top_logic/layout/react/chartjs/Icons.java`

- [ ] **Step 1: Create CSS file**

```css
.tlReactChart {
    width: 100%;
    height: 100%;
    position: relative;
    min-height: 200px;
}

.tlReactChart--error,
.tlReactChart--noData {
    display: flex;
    align-items: center;
    justify-content: center;
}

.tlReactChart__error {
    color: var(--error-color, #dc2626);
    padding: 1rem;
    text-align: center;
}

.tlReactChart__noData {
    color: var(--text-secondary, #64748b);
    padding: 1rem;
    text-align: center;
    font-style: italic;
}

.tlReactChart__tooltip {
    position: absolute;
    pointer-events: none;
    z-index: 100;
    background: var(--surface-elevated, #fff);
    border: 1px solid var(--border-color, #e2e8f0);
    border-radius: 0.375rem;
    padding: 0.5rem 0.75rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    font-size: 0.8125rem;
}

.tlReactChart__zoomReset {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    z-index: 10;
    background: var(--surface, #fff);
    border: 1px solid var(--border-color, #e2e8f0);
    border-radius: 0.25rem;
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    cursor: pointer;
    color: var(--text-secondary, #64748b);
}

.tlReactChart__zoomReset:hover {
    background: var(--surface-hover, #f1f5f9);
}
```

- [ ] **Step 2: Create theme.xml for CSS registration**

Create `com.top_logic.layout.react.chartjs/src/main/webapp/WEB-INF/themes/core/theme.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<theme>
	<styles>
		<style name="/style/tl-react-chartjs.css"/>
	</styles>
</theme>
```

Without this file, the CSS will never be loaded by the browser.

- [ ] **Step 3: Create Icons.java with ThemeVar declarations**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.chartjs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;

/**
 * Theme variable declarations for Chart.js integration.
 */
public class Icons extends IconsBase {

	@DefaultValue("#3b82f6")
	public static ThemeVar<String> CHART_COLOR_1;

	@DefaultValue("#10b981")
	public static ThemeVar<String> CHART_COLOR_2;

	@DefaultValue("#f59e0b")
	public static ThemeVar<String> CHART_COLOR_3;

	@DefaultValue("#ef4444")
	public static ThemeVar<String> CHART_COLOR_4;

	@DefaultValue("#8b5cf6")
	public static ThemeVar<String> CHART_COLOR_5;

	@DefaultValue("#06b6d4")
	public static ThemeVar<String> CHART_COLOR_6;

	@DefaultValue("#f97316")
	public static ThemeVar<String> CHART_COLOR_7;

	@DefaultValue("#ec4899")
	public static ThemeVar<String> CHART_COLOR_8;

	@DefaultValue("#14b8a6")
	public static ThemeVar<String> CHART_COLOR_9;

	@DefaultValue("#6366f1")
	public static ThemeVar<String> CHART_COLOR_10;

	@DefaultValue("#e2e8f0")
	public static ThemeVar<String> CHART_GRID_COLOR;

	@DefaultValue("#64748b")
	public static ThemeVar<String> CHART_TEXT_COLOR;

	@DefaultValue("#ffffff")
	public static ThemeVar<String> CHART_BACKGROUND_COLOR;

	@DefaultValue("#e2e8f0")
	public static ThemeVar<String> CHART_BORDER_COLOR;

	/**
	 * Returns all chart theme colors as a map suitable for the React state.
	 */
	public static Map<String, Object> getChartThemeColors() {
		List<String> palette = new ArrayList<>();
		palette.add(CHART_COLOR_1.get());
		palette.add(CHART_COLOR_2.get());
		palette.add(CHART_COLOR_3.get());
		palette.add(CHART_COLOR_4.get());
		palette.add(CHART_COLOR_5.get());
		palette.add(CHART_COLOR_6.get());
		palette.add(CHART_COLOR_7.get());
		palette.add(CHART_COLOR_8.get());
		palette.add(CHART_COLOR_9.get());
		palette.add(CHART_COLOR_10.get());

		return Map.of(
			"palette", palette,
			"gridColor", CHART_GRID_COLOR.get(),
			"textColor", CHART_TEXT_COLOR.get(),
			"backgroundColor", CHART_BACKGROUND_COLOR.get(),
			"borderColor", CHART_BORDER_COLOR.get()
		);
	}
}
```

**Note:** `ThemeVar` and `IconsBase` are in `com.top_logic.layout.basic`. `@DefaultValue` sets the fallback when no theme overrides it. The actual `ThemeVar.get()` reads the resolved value from the current theme at runtime.

- [ ] **Step 4: Commit**

```
Ticket #29108: Add CSS, theme registration, and ThemeVar declarations for charts.
```

---

### Task 9: Create demo view and wire it up

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/chart-demo.view.xml`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml`
- Modify: `com.top_logic.demo/pom.xml`

- [ ] **Step 1: Add dependency to com.top_logic.demo/pom.xml**

Add in the `<dependencies>` section:

```xml
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-react-chartjs</artifactId>
    </dependency>
```

- [ ] **Step 2: Create chart-demo.view.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>

<view>
  <stack direction="column" gap="default">
    <grid gap="default" min-column-width="24rem">
      <card title="Bar Chart Demo">
        <chart data="${-> {
            'type': 'bar',
            'data': {
                'labels': ['January', 'February', 'March', 'April', 'May', 'June'],
                'datasets': [
                    {
                        'label': 'Revenue',
                        'data': [12, 19, 15, 25, 22, 30]
                    },
                    {
                        'label': 'Costs',
                        'data': [8, 14, 10, 18, 16, 22]
                    }
                ]
            },
            'options': {
                'maintainAspectRatio': false,
                'scales': {
                    'y': { 'beginAtZero': true }
                }
            }
        }}">
          <inputs/>
        </chart>
      </card>

      <card title="Doughnut Chart Demo">
        <chart data="${-> {
            'type': 'doughnut',
            'data': {
                'labels': ['Electronics', 'Clothing', 'Food', 'Books', 'Other'],
                'datasets': [{
                    'data': [35, 25, 20, 12, 8]
                }]
            },
            'options': {
                'maintainAspectRatio': false
            }
        }}">
          <inputs/>
        </chart>
      </card>
    </grid>

    <card title="Line Chart with Zoom">
      <chart zoomEnabled="true" data="${-> {
          'type': 'line',
          'data': {
              'labels': ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
              'datasets': [
                  {
                      'label': '2025',
                      'data': [12, 19, 15, 25, 22, 30, 28, 35, 32, 40, 38, 45],
                      'fill': true,
                      'tension': 0.3
                  },
                  {
                      'label': '2024',
                      'data': [10, 14, 12, 18, 16, 22, 20, 26, 24, 30, 28, 35],
                      'fill': true,
                      'tension': 0.3,
                      'borderDash': [5, 5]
                  }
              ]
          },
          'options': {
              'maintainAspectRatio': false,
              'interaction': { 'intersect': false, 'mode': 'index' }
          }
      }}">
        <inputs/>
      </chart>
    </card>

    <card title="Combined Bar + Line">
      <chart data="${-> {
          'type': 'bar',
          'data': {
              'labels': ['Q1', 'Q2', 'Q3', 'Q4'],
              'datasets': [
                  {
                      'type': 'bar',
                      'label': 'Revenue',
                      'data': [30, 50, 40, 60],
                      'order': 2
                  },
                  {
                      'type': 'line',
                      'label': 'Margin %',
                      'data': [15, 22, 18, 28],
                      'fill': false,
                      'tension': 0.3,
                      'yAxisID': 'y1',
                      'order': 1
                  }
              ]
          },
          'options': {
              'maintainAspectRatio': false,
              'scales': {
                  'y': { 'beginAtZero': true, 'title': { 'display': true, 'text': 'Revenue' } },
                  'y1': { 'beginAtZero': true, 'position': 'right', 'grid': { 'drawOnChartArea': false }, 'title': { 'display': true, 'text': 'Margin %' } }
              }
          }
      }}">
        <inputs/>
      </chart>
    </card>
  </stack>
</view>
```

- [ ] **Step 3: Add sidebar entry to app.view.xml**

Add after the "Input Controls" nav-item (after line 46):

```xml
				<nav-item id="chart-demo"
					icon="bi bi-bar-chart-line"
					label="Chart Demo"
				>
					<view-ref view="demo/chart-demo.view.xml"/>
				</nav-item>
```

- [ ] **Step 4: Build the demo app to verify**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react.chartjs && mvn compile -DskipTests=true -pl com.top_logic.demo
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```
Ticket #29108: Add chart demo view with bar, doughnut, line, and combined charts.
```

---

## Chunk 6: Integration Test

### Task 10: Manual integration test

- [ ] **Step 1: Start the demo app**

```bash
mvn jetty:run -pl com.top_logic.demo
```

- [ ] **Step 2: Open browser and navigate to Chart Demo**

Open `http://localhost:8080/` and log in with `root` / `root1234`. Click "Chart Demo" in the sidebar.

- [ ] **Step 3: Verify all four charts render**

Expected:
- Bar chart with Revenue/Costs datasets
- Doughnut chart with 5 categories
- Line chart with 2025/2024 comparison (zoom/pan should work)
- Combined bar + line chart with dual y-axes

- [ ] **Step 4: Verify zoom/pan on line chart**

Scroll wheel on the line chart should zoom. Drag should pan. "Reset Zoom" button should restore.

- [ ] **Step 5: Commit any fixes needed**

```
Ticket #29108: Fix integration issues found during manual testing.
```

---

## Summary

| Task | Description | Dependencies |
|------|-------------|-------------|
| 1 | Maven module scaffold | — |
| 2 | npm + Vite scaffold | Task 1 |
| 3 | ClickHandlerConfig + TooltipProviderConfig | Task 1 |
| 4 | I18NConstants + ChartElement | Task 3 |
| 5 | ChartConfigExtractor | Task 3 |
| 6 | ReactChartJsControl | Tasks 4, 5 |
| 7 | TLChart React component + hooks | Task 2 |
| 8 | Chart CSS + theme.xml + Icons.java | — |
| 9 | Demo view + wiring | Tasks 6, 7, 8 |
| 10 | Manual integration test | Task 9 |

**Parallelization:** Tasks 3-6 (Java) and Tasks 7-8 (TypeScript) can be worked on in parallel by separate agents.
