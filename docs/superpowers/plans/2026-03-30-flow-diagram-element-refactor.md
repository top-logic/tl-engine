# FlowDiagramElement Refactor — Replace Builder with TL-Script + Input Channels

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `FlowChartBuilder` indirection in `FlowDiagramElement` with direct TL-Script expressions and input channel configuration, following the patterns from `TableElement` (channels) and `ScriptFlowChartBuilder` (TL-Script + handlers).

**Architecture:** `FlowDiagramElement.Config` gets `inputs` (positional channel refs), `createChart` (Expr), `observed` (Expr), `handlers` (named `DiagramHandler` map), and `selection` (output channel). The constructor compiles expressions and injects handler variables as lambda wrappers. `createControl()` resolves input channels, reads their values, executes `createChart` with channel values + handlers as `Args`, and creates a `FlowDiagramControl` with the resulting `Diagram`. The `FlowChartBuilder` interface, `ScriptFlowChartBuilder`, and `TestFlowChartBuilder` are deleted from `react.flow.server`.

**Tech Stack:** Java 17, TL-Script (SearchExpression / QueryExecutor), Maven

**Reference files (all paths relative to worktree `/home/bhu/devel/tl-engine/.worktrees/agent-a/`):**
- Pattern for channels: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TableElement.java`
- Pattern for TL-Script + handlers: `com.top_logic.graphic.blocks.server/src/main/java/com/top_logic/graphic/flow/server/ui/ScriptFlowChartBuilder.java`
- Current implementation: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/FlowDiagramElement.java`
- Control: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/control/FlowDiagramControl.java`
- Test view: `com.top_logic.react.flow.server/src/main/webapp/WEB-INF/views/test-flow-diagram.view.xml`

---

## File Structure

### Modified
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/FlowDiagramElement.java` — rewrite Config + constructor + createControl()

### Modified (test view)
- `com.top_logic.react.flow.server/src/main/webapp/WEB-INF/views/test-flow-diagram.view.xml` — replace builder with inline TL-Script

### Deleted
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/FlowChartBuilder.java`
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/TestFlowChartBuilder.java`
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/ScriptFlowChartBuilder.java` *(if it exists in react.flow.server — check first)*

### Not modified
- `FlowDiagramControl.java` — no changes needed; it already accepts a `Diagram` in its constructor

---

## Task 1: Rewrite FlowDiagramElement

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/FlowDiagramElement.java`

- [ ] **Step 1: Rewrite FlowDiagramElement.java**

Replace the entire file content with:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.react.flow.callback.DiagramHandler;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.server.control.FlowDiagramControl;
import com.top_logic.util.model.ModelService;

/**
 * {@link UIElement} for embedding a flow diagram in the React View framework.
 *
 * <p>
 * Combines the channel-based input pattern from
 * {@link com.top_logic.layout.view.element.TableElement} with the TL-Script + handler pattern from
 * {@link com.top_logic.graphic.flow.server.ui.ScriptFlowChartBuilder}.
 * </p>
 *
 * <p>
 * Usage in view.xml:
 * </p>
 *
 * <pre>
 * &lt;flow-diagram createChart="model -&gt; flowChart(...)" selection="selected"&gt;
 *   &lt;inputs&gt;
 *     &lt;input channel="myModel" /&gt;
 *   &lt;/inputs&gt;
 * &lt;/flow-diagram&gt;
 * </pre>
 *
 * <p>
 * The input channel values become positional arguments to the {@link Config#getCreateChart()
 * createChart} expression. The expression must return a {@link Diagram}.
 * </p>
 */
public class FlowDiagramElement implements UIElement {

	/**
	 * Configuration for {@link FlowDiagramElement}.
	 */
	@TagName("flow-diagram")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(FlowDiagramElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getCreateChart()}. */
		String CREATE_CHART = "createChart";

		/** Configuration name for {@link #getObserved()}. */
		String OBSERVED = "observed";

		/** Configuration name for {@link #getHandlers()}. */
		String HANDLERS = "handlers";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/**
		 * References to {@link ViewChannel}s whose current values become positional arguments to
		 * the {@link #getCreateChart() createChart} expression.
		 */
		@Name(INPUTS)
		@ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
		List<ChannelRef> getInputs();

		/**
		 * TL-Script function computing the {@link Diagram}.
		 *
		 * <p>
		 * Takes the input channel values as positional arguments (one per configured input) and
		 * must return a {@link Diagram}. Handler variables are available as implicitly defined
		 * variables (see {@link #getHandlers()}).
		 * </p>
		 */
		@Name(CREATE_CHART)
		@Mandatory
		@NonNullable
		@FormattedDefault("flowChart()")
		Expr getCreateChart();

		/**
		 * Optional TL-Script function that retrieves the business objects to observe for a given
		 * diagram element's user object.
		 *
		 * <p>
		 * The function receives the user object as first argument and the input channel values as
		 * further arguments.
		 * </p>
		 *
		 * <p>
		 * If not given, the default is to observe the user object of diagram elements.
		 * </p>
		 */
		@Name(OBSERVED)
		Expr getObserved();

		/**
		 * Named interaction handlers that are injected as implicitly defined variables into the
		 * {@link #getCreateChart() createChart} script.
		 *
		 * <p>
		 * A handler named {@code onClick} is available in the script as {@code $onClick}.
		 * </p>
		 */
		@Name(HANDLERS)
		@Key(HandlerDefinition.NAME_ATTRIBUTE)
		Map<String, HandlerDefinition<? extends DiagramHandler>> getHandlers();

		/**
		 * Optional reference to a {@link ViewChannel} to write the selected node's user object to.
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Configuration of a named {@link DiagramHandler}.
		 */
		@Abstract
		interface HandlerDefinition<T extends DiagramHandler>
				extends PolymorphicConfiguration<T>, NamedConfigMandatory {

			/**
			 * Unique name of the handler.
			 *
			 * <p>
			 * Available in the {@link Config#getCreateChart() createChart} script as
			 * {@code $name}.
			 * </p>
			 */
			@Override
			String getName();
		}
	}

	private final Config _config;

	private final QueryExecutor _createChart;

	private final QueryExecutor _getObserved;

	private final List<DiagramHandler> _handlers;

	/**
	 * Creates a {@link FlowDiagramElement} from configuration.
	 *
	 * <p>
	 * Expressions are compiled once here and shared across all sessions. Handler variables are
	 * injected as lambda wrappers around the {@link Config#getCreateChart() createChart}
	 * expression.
	 * </p>
	 */
	@CalledByReflection
	public FlowDiagramElement(InstantiationContext context, Config config) {
		_config = config;

		Map<String, DiagramHandler> handlerMap =
			TypedConfiguration.getInstanceMap(context, config.getHandlers());

		SearchExpression createChart =
			SearchBuilder.toSearchExpression(ModelService.getApplicationModel(), config.getCreateChart());

		// Inject handler variables as implicit lambda parameters, same as ScriptFlowChartBuilder.
		_handlers = new ArrayList<>();
		for (Entry<String, DiagramHandler> handler : handlerMap.entrySet()) {
			createChart = lambda(handler.getKey(), createChart);
			_handlers.add(handler.getValue());
		}

		_createChart = QueryExecutor.compile(createChart);
		_getObserved = QueryExecutor.compileOptional(config.getObserved());
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// 1. Resolve input channels.
		List<ChannelRef> inputRefs = _config.getInputs();
		List<ViewChannel> inputChannels = new ArrayList<>(inputRefs.size());
		for (ChannelRef ref : inputRefs) {
			inputChannels.add(context.resolveChannel(ref));
		}

		// 2. Read current channel values and build args.
		Object[] channelValues = readChannelValues(inputChannels);
		Diagram diagram = executeCreateChart(channelValues);

		// 3. Create control.
		FlowDiagramControl control = new FlowDiagramControl(context, diagram);

		// 4. Wire selection channel.
		ChannelRef selectionRef = _config.getSelection();
		if (selectionRef != null) {
			ViewChannel selectionChannel = context.resolveChannel(selectionRef);
			control.setSelectionChannel(selectionChannel);
		}

		return control;
	}

	private Diagram executeCreateChart(Object[] channelValues) {
		Args args = Args.none();
		// Channel values are positional arguments (rightmost = last input).
		for (int i = channelValues.length - 1; i >= 0; i--) {
			args = Args.cons(channelValues[i], args);
		}
		// Handler values are prepended (leftmost = first handler), same order as
		// ScriptFlowChartBuilder.
		for (DiagramHandler handler : _handlers) {
			args = Args.cons(handler, args);
		}

		Object result = _createChart.executeWith(args);
		if (result instanceof Diagram) {
			return (Diagram) result;
		}
		return Diagram.create();
	}

	private static Object[] readChannelValues(List<ViewChannel> channels) {
		Object[] values = new Object[channels.size()];
		for (int i = 0; i < channels.size(); i++) {
			values[i] = channels.get(i).get();
		}
		return values;
	}

}
```

Key design decisions:
- `Expr` import: Uses `com.top_logic.model.search.expr.config.dom.Expr` (same as `TableElement.Config.getRows()` and `ScriptFlowChartBuilder.Config.getCreateChart()`).
- Handler injection: Identical to `ScriptFlowChartBuilder` — `lambda()` wraps the expression, handlers are prepended as `Args.cons()`.
- Channel values: Passed as positional arguments after handlers, read left-to-right.
- Default `createChart`: `flowChart()` produces an empty diagram when no expression is configured (matches `ScriptFlowChartBuilder.Config` default).
- `_getObserved` is compiled but not yet wired into observation — that is a separate gap (Gap 8: Model Observation).

- [ ] **Step 2: Verify the `Expr` import resolves**

The `Config` interface uses `Expr` as a property type. This is `com.top_logic.model.search.expr.config.dom.Expr`. Verify it exists:

```bash
grep -r "public interface Expr" com.top_logic.model.search/src/main/java/com/top_logic/model/search/expr/config/dom/Expr.java | head -3
```

Expected: The `Expr` interface definition appears.

**Note:** The `Expr` type does not need an explicit import in the `Config` interface if it's used as a return type of a getter — TypedConfiguration resolves it via the format. However, `FlowDiagramElement.java` itself uses `Expr` in the constructor (indirectly via `config.getCreateChart()`), so the import `com.top_logic.model.search.expr.config.dom.Expr` is NOT needed in the element class — it flows through `SearchBuilder.toSearchExpression()` and `QueryExecutor.compileOptional()` which accept `Expr` directly.

Wait — actually `Config.getCreateChart()` returns `Expr`. The `Config` interface **does** need the import. Check `TableElement.Config` for how it imports `Expr`:

The `TableElement` imports `com.top_logic.model.search.expr.config.dom.Expr` at line 39. The `FlowDiagramElement` code above must do the same. This is already included in the import list in the code above.

- [ ] **Step 3: Commit**

```
Ticket #29108: Rewrite FlowDiagramElement with TL-Script + input channels.

Replace FlowChartBuilder indirection with direct TL-Script expression (createChart)
and input channel configuration, following TableElement and ScriptFlowChartBuilder patterns.
```

---

## Task 2: Delete Obsolete Builder Classes

**Files:**
- Delete: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/FlowChartBuilder.java`
- Delete: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/TestFlowChartBuilder.java`

Before deleting, verify no other files in `react.flow.server` reference these classes (besides the files being deleted and `FlowDiagramElement` which was just rewritten):

- [ ] **Step 1: Check for remaining references**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a && grep -r "FlowChartBuilder\|TestFlowChartBuilder" com.top_logic.react.flow.server/src/ --include="*.java" --include="*.xml" -l
```

Expected output should only list:
- `FlowChartBuilder.java` (being deleted)
- `TestFlowChartBuilder.java` (being deleted)
- Possibly `test-flow-diagram.view.xml` (will be updated in Task 3)

If any other files reference these classes, they must be updated first.

- [ ] **Step 2: Delete the files**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a
git rm com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/FlowChartBuilder.java
git rm com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/TestFlowChartBuilder.java
```

Also check if `ScriptFlowChartBuilder.java` exists in `react.flow.server` (it may have been copied during Phase 1):

```bash
ls com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/ScriptFlowChartBuilder.java 2>/dev/null && echo "EXISTS — delete it" || echo "Not present — skip"
```

If it exists, delete it too:

```bash
git rm com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/ui/ScriptFlowChartBuilder.java
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Remove obsolete FlowChartBuilder classes from react.flow.server.
```

---

## Task 3: Update Test View XML

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/webapp/WEB-INF/views/test-flow-diagram.view.xml`

The current view references `TestFlowChartBuilder` via a `<builder>` element. Replace with inline TL-Script that creates the same three-node tree diagram.

- [ ] **Step 1: Rewrite test-flow-diagram.view.xml**

Replace the entire file with:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <channels>
    <channel name="selected" />
  </channels>
  <split-panel>
    <start>
      <panel title="Flow Diagram">
        <flow-diagram
          createChart="
            flowChart(
              padding(20,
                tree(
                  list(
                    border(padding(5, text('Parent'))),
                    border(padding(5, text('Child A'))),
                    border(padding(5, text('Child B')))
                  ),
                  list(
                    treeConnection(treeConnector($nodes[0], 0.5), treeConnector($nodes[1], 0.5)),
                    treeConnection(treeConnector($nodes[0], 0.5), treeConnector($nodes[2], 0.5))
                  )
                )
              )
            )
          "
          selection="selected"
        />
      </panel>
    </start>
    <end>
      <panel title="Selection">
        <!-- The selected channel value would be displayed here when connected to a form or table. -->
      </panel>
    </end>
  </split-panel>
</view>
```

**Important:** The exact TL-Script function names for creating diagram elements (like `flowChart()`, `padding()`, `border()`, `text()`, `tree()`, `treeConnection()`, `treeConnector()`) must match the functions registered in `FlowFactory.java`. Check the `FlowFactory` source for the actual function names and signatures. The script above is a **placeholder** — adapt it to match the real API.

If the TL-Script API does not support inline diagram construction easily, a simpler alternative is a self-contained expression that doesn't need inputs:

```xml
<flow-diagram
  createChart="flowChart()"
  selection="selected"
/>
```

This creates an empty diagram, which is sufficient to verify the element wiring works. A more complex test diagram can be added once the TL-Script diagram API is verified.

- [ ] **Step 2: Verify build**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a && mvn -B install -DskipTests=true -pl com.top_logic.react.flow.server 2>&1 | tail -20
```

Expected: `BUILD SUCCESS`

If compilation fails because of missing imports or type issues in `FlowDiagramElement.java`, fix those first and rebuild.

- [ ] **Step 3: Commit**

```
Ticket #29108: Update test view to use inline TL-Script instead of builder class.
```

---

## Task 4: Remove `tl-graphic-blocks` Dependency (if possible)

**Files:**
- Modify: `com.top_logic.react.flow.server/pom.xml`

The `pom.xml` currently depends on `tl-graphic-blocks`. Check if this dependency is still needed after removing `FlowChartBuilder` (which extended `ModelBuilder` from `tl-ajax-server`).

- [ ] **Step 1: Check remaining references to graphic.blocks**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a && grep -r "com\.top_logic\.graphic" com.top_logic.react.flow.server/src/ --include="*.java" -l
```

If no files reference `com.top_logic.graphic.*`, the dependency can be removed.

- [ ] **Step 2: Remove dependency if unused**

If no references remain, remove from `pom.xml`:

```xml
        <dependency>
            <groupId>com.top-logic</groupId>
            <artifactId>tl-graphic-blocks</artifactId>
            <version>${project.version}</version>
        </dependency>
```

Also check if `tl-ajax-server` is still needed — `FlowChartBuilder` extended `ModelBuilder` from `tl-ajax-server`. The new `FlowDiagramElement` does not extend `ModelBuilder`, so the dependency on `tl-ajax-server` might be removable too (but it may be pulled in transitively or used by other classes like `FlowDiagramControl`).

- [ ] **Step 3: Verify build after dependency changes**

```bash
cd /home/bhu/devel/tl-engine/.worktrees/agent-a && mvn -B install -DskipTests=true -pl com.top_logic.react.flow.server 2>&1 | tail -20
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit (only if changes were made)**

```
Ticket #29108: Remove unused tl-graphic-blocks dependency from react.flow.server.
```

---

## Summary

| Task | Description | Complexity |
|------|-------------|------------|
| 1 | Rewrite FlowDiagramElement with TL-Script + channels | Medium |
| 2 | Delete obsolete builder classes | Low |
| 3 | Update test view XML | Low |
| 4 | Remove unused dependency (if applicable) | Low |
