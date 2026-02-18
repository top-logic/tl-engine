# Agent Skills System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Decompose monolithic agent system prompts into composable, reusable skill fragments loaded from classpath resource files.

**Architecture:** A `Skill` record holds prompt fragment metadata. A `SkillRegistry` maps skills to agents and composes system prompts. Agents switch from `@SystemMessage` annotations to `systemMessageProvider` on `AgenticServices.agentBuilder()`. Resource `.md` files hold prompt text.

**Tech Stack:** Java 17 records, LangChain4j 1.10.0 `AgenticServices.agentBuilder().systemMessageProvider()`, classpath resource files (.md), JUnit 4 (TestCase).

**Critical constraint:** `@SystemMessage` takes precedence over `systemMessageProvider`. When switching an agent, the annotation MUST be removed first.

---

### Task 1: Create the Skill record

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/Skill.java`

**Step 1: Write the failing test**

Create file `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkill.java`:

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkill extends TestCase {

	public void testSkillCreation() {
		Skill skill = new Skill("test-skill", "A test skill", "You are a test agent.");
		assertEquals("test-skill", skill.name());
		assertEquals("A test skill", skill.description());
		assertEquals("You are a test agent.", skill.content());
	}

	public void testSkillContentNonEmpty() {
		Skill skill = new Skill("s", "d", "content");
		assertFalse(skill.content().isEmpty());
	}
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -pl tl-ai-service -Dtest=com.top_logic.ai.service.skills.TestSkill -DfailIfNoTests=false`
Expected: Compilation error — `Skill` class does not exist yet.

**Step 3: Write minimal implementation**

Create file `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/Skill.java`:

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * A reusable behavioral instruction fragment that can be composed into an agent's system prompt.
 *
 * <p>
 * Skills are loaded from classpath resource files ({@code .md}) and assembled by
 * {@link SkillRegistry} into complete system prompts for each agent.
 * </p>
 *
 * @param name
 *        Unique skill identifier (e.g. {@code "core/zero-knowledge"}).
 * @param description
 *        Human-readable description of what this skill enforces.
 * @param content
 *        The actual prompt text loaded from the resource file.
 */
public record Skill(String name, String description, String content) {
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -pl tl-ai-service -Dtest=com.top_logic.ai.service.skills.TestSkill`
Expected: PASS

**Step 5: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/skills/Skill.java \
       tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkill.java
git commit -m "Add Skill record for composable prompt fragments

Prompt: Create the Skill record as part of the agent skills system"
```

---

### Task 2: Create the SkillRegistry with resource loading

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillRegistry.java`
- Modify: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkill.java` (add registry tests)

**Step 1: Create a test resource file**

Create file `tl-ai-service/src/test/resources/com/top_logic/ai/service/skills/test/sample-skill.md`:

```markdown
### SAMPLE RULE

This is a test skill content.
```

**Step 2: Write the failing test**

Add to `TestSkill.java` (or create `TestSkillRegistry.java`):

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkillRegistry extends TestCase {

	public void testLoadSkillFromResource() {
		SkillRegistry registry = new SkillRegistry();
		Skill skill = registry.getSkill("test/sample-skill");
		assertNotNull(skill);
		assertTrue(skill.content().contains("SAMPLE RULE"));
	}

	public void testLoadNonExistentSkillThrows() {
		SkillRegistry registry = new SkillRegistry();
		try {
			registry.getSkill("nonexistent/skill");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	public void testComposeSystemMessage() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/sample-skill");
		String message = registry.composeSystemMessage(TestAgent.class);
		assertTrue(message.contains("SAMPLE RULE"));
	}

	public void testComposeMultipleSkills() {
		SkillRegistry registry = new SkillRegistry();
		// Use the same skill twice for simplicity (demonstrates ordering/concatenation)
		registry.registerAgentSkills(TestAgent.class, "test/sample-skill", "test/sample-skill");
		String message = registry.composeSystemMessage(TestAgent.class);
		int firstIdx = message.indexOf("SAMPLE RULE");
		int secondIdx = message.indexOf("SAMPLE RULE", firstIdx + 1);
		assertTrue("Should contain skill content twice", secondIdx > firstIdx);
		assertTrue("Should contain separator", message.contains("---"));
	}

	/** Dummy interface for testing. */
	interface TestAgent {
		String chat(String msg);
	}
}
```

**Step 3: Run test to verify it fails**

Run: `mvn test -pl tl-ai-service -Dtest=com.top_logic.ai.service.skills.TestSkillRegistry -DfailIfNoTests=false`
Expected: Compilation error — `SkillRegistry` does not exist yet.

**Step 4: Write minimal implementation**

Create file `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillRegistry.java`:

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that loads skill prompt fragments from classpath resources and composes them into
 * system prompts for agents.
 *
 * <p>
 * Skills are loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the classpath.
 * Each agent class is mapped to an ordered list of skill names. The
 * {@link #composeSystemMessage(Class)} method concatenates all skills for an agent in order,
 * separated by section dividers.
 * </p>
 */
public class SkillRegistry {

	private static final String RESOURCE_BASE = "com/top_logic/ai/service/skills/";

	private static final String SKILL_SEPARATOR = "\n\n---\n\n";

	private final Map<String, Skill> _skills = new LinkedHashMap<>();

	private final Map<Class<?>, List<String>> _agentSkills = new LinkedHashMap<>();

	/**
	 * Loads and returns the skill with the given name.
	 *
	 * <p>
	 * The skill is loaded from {@code com/top_logic/ai/service/skills/<name>.md} on the
	 * classpath. Skills are cached after first load.
	 * </p>
	 *
	 * @param name
	 *        The skill name (e.g. {@code "core/zero-knowledge"}).
	 * @return The loaded skill.
	 * @throws IllegalArgumentException
	 *         if the skill resource is not found.
	 */
	public Skill getSkill(String name) {
		return _skills.computeIfAbsent(name, this::loadSkill);
	}

	/**
	 * Registers the ordered list of skills for an agent class.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @param skillNames
	 *        Skill names in composition order.
	 */
	public void registerAgentSkills(Class<?> agentClass, String... skillNames) {
		_agentSkills.put(agentClass, Arrays.asList(skillNames));
	}

	/**
	 * Returns the ordered skills configured for the given agent class.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @return Ordered list of skills, or empty list if none configured.
	 */
	public List<Skill> getSkillsFor(Class<?> agentClass) {
		List<String> names = _agentSkills.getOrDefault(agentClass, Collections.emptyList());
		List<Skill> result = new ArrayList<>(names.size());
		for (String name : names) {
			result.add(getSkill(name));
		}
		return result;
	}

	/**
	 * Composes the system prompt for an agent by concatenating its skills in order.
	 *
	 * @param agentClass
	 *        The agent interface class.
	 * @return The composed system prompt string.
	 */
	public String composeSystemMessage(Class<?> agentClass) {
		List<Skill> skills = getSkillsFor(agentClass);
		StringBuilder sb = new StringBuilder();
		for (Skill skill : skills) {
			if (sb.length() > 0) {
				sb.append(SKILL_SEPARATOR);
			}
			sb.append(skill.content());
		}
		return sb.toString();
	}

	/**
	 * Returns all registered skill names.
	 */
	public List<String> allSkillNames() {
		return new ArrayList<>(_skills.keySet());
	}

	private Skill loadSkill(String name) {
		String resourcePath = RESOURCE_BASE + name + ".md";
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IllegalArgumentException("Skill resource not found: " + resourcePath);
			}
			String content = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
			return new Skill(name, name, content);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load skill: " + resourcePath, e);
		}
	}
}
```

**Step 5: Run test to verify it passes**

Run: `mvn test -pl tl-ai-service -Dtest=com.top_logic.ai.service.skills.TestSkillRegistry`
Expected: PASS

**Step 6: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillRegistry.java \
       tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillRegistry.java \
       tl-ai-service/src/test/resources/com/top_logic/ai/service/skills/test/sample-skill.md
git commit -m "Add SkillRegistry for composable prompt assembly

Loads skill .md files from classpath, caches them, and composes
system prompts by concatenating agent-specific skill lists.

Prompt: Create the SkillRegistry with resource loading and composition"
```

---

### Task 3: Extract ModelExplorer system prompt into skill files

**Files:**
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/identity.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/core/tool-first-rule.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/core/zero-knowledge.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/exhaustiveness-mandate.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/scope.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/exploration-strategy.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/context-resolution.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/fact-rules.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/explorer/output-format.md`

**Step 1: Analyze the ModelExplorer @SystemMessage**

The ModelExplorer system prompt (lines 24-467 of `ModelExplorer.java`) contains these sections, separated by `---`:

1. **Identity paragraph** (line 25) — "You are a TopLogic Model Explorer agent..."
2. **TOOL-FIRST RULE** (lines 29-37) — Hard gate requiring tool calls
3. **ZERO PRIOR KNOWLEDGE RULE** (lines 40-50) — No prior knowledge
4. **EXHAUSTIVENESS MANDATE** (lines 54-71) — Must call tools + forbidden patterns
5. **SCOPE** (lines 76-108) — Data layer focus + UI/behavior rules + module scope
6. **TOOLS** (lines 112-113) — Brief tools description
7. **EXPLORATION STRATEGY** (lines 117-139) — Explorer loop + completion check
8. **COMMON EXPLORATION FLOWS** (lines 143-161) — Tool recipes
9. **CONTEXT RESOLUTION** (lines 166-183) — Name resolution rules
10. **SCRIPT/TLSCRIPT BOUNDARY** (lines 187-193) — Script handling
11. **EXPLORER MODE: FACTS ONLY** (lines 196-213) — Facts-only output
12. **FACT GUARANTEE RULES** (lines 217-227) — Part/enum/usage token rules
13. **VERBATIM PART NAMES** (lines 231-233) — No renaming
14. **RELATIONSHIPS** (lines 237-240) — Evidence only
15. **ABSTRACT/CONCRETE** (lines 243-246) — No inference
16. **OUTPUT FORMAT** (lines 249-267) — Evidence/Facts/Next suggested query
17. **COMPLETENESS/NON-HALLUCINATION** (lines 270-279) — No new qualified names
18. **PERFORMANCE/EFFICIENCY** (lines 283-289) — Tool call efficiency
19. **EXAMPLES** (lines 292-466) — Example A through F

**Step 2: Extract into skill files**

Group logically related sections into skill files. Create each `.md` file with the exact text from the current `@SystemMessage`, preserving formatting.

The grouping:

| Skill file | Sections | Shared? |
|---|---|---|
| `explorer/identity.md` | 1 (identity) + 4 (scope) + 6 (tools) | No |
| `core/tool-first-rule.md` | 2 (tool-first rule) | Yes |
| `core/zero-knowledge.md` | 3 (zero prior knowledge) | Yes |
| `explorer/exhaustiveness-mandate.md` | 4 (exhaustiveness + forbidden patterns) | No |
| `explorer/exploration-strategy.md` | 7 (strategy) + 8 (flows) + 9 (context resolution) | No |
| `explorer/fact-rules.md` | 10 (script boundary) + 11 (facts only) + 12 (fact guarantee) + 13 (verbatim) + 14 (relationships) + 15 (abstract/concrete) + 17 (completeness) | No |
| `explorer/output-format.md` | 16 (output format) + 18 (performance) | No |
| `explorer/examples.md` | 19 (examples A-F) | No |

**Step 3: Create each file**

For each file: copy the exact section text from `ModelExplorer.java` lines 24-467. The text lives inside the `@SystemMessage("""...""")` text block — extract the content between the triple quotes, keeping all formatting intact.

Note: The `@UserMessage` annotation (lines 468-487) stays on the interface — it is NOT part of the skill system. Only `@SystemMessage` content becomes skills.

**Step 4: Write a regression test**

Add to `TestSkillRegistry.java`:

```java
public void testModelExplorerComposedPromptMatchesOriginal() {
    SkillRegistry registry = createProductionRegistry();
    String composed = registry.composeSystemMessage(ModelExplorer.class);
    // Verify key sections are present in correct order
    int identityIdx = composed.indexOf("TopLogic Model Explorer");
    int toolFirstIdx = composed.indexOf("TOOL-FIRST RULE");
    int zeroKnowledgeIdx = composed.indexOf("ZERO PRIOR KNOWLEDGE");
    int outputFormatIdx = composed.indexOf("OUTPUT FORMAT");

    assertTrue("identity comes first", identityIdx < toolFirstIdx);
    assertTrue("tool-first before zero-knowledge", toolFirstIdx < zeroKnowledgeIdx);
    assertTrue("output-format comes after", zeroKnowledgeIdx < outputFormatIdx);
}
```

**Step 5: Run test to verify it passes**

Run: `mvn test -pl tl-ai-service -Dtest=com.top_logic.ai.service.skills.TestSkillRegistry`
Expected: PASS

**Step 6: Commit**

```bash
git add tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/
git commit -m "Extract ModelExplorer system prompt into skill resource files

Split 467-line monolithic prompt into 8 composable skill files.
Shared core/tool-first-rule.md and core/zero-knowledge.md will be
reused by other agents.

Prompt: Extract ModelExplorer system prompt into skill files"
```

---

### Task 4: Create the production SkillConfig and wire ModelExplorer

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/ModelExplorer.java` — remove `@SystemMessage`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java:167-177` — use `systemMessageProvider`

**Step 1: Create SkillConfig**

Create `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java`:

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import com.top_logic.ai.service.agents.ModelExplorer;

/**
 * Production configuration mapping agents to their ordered skill lists.
 *
 * <p>
 * Each agent is mapped to a list of skill names in composition order.
 * The {@link SkillRegistry} loads the corresponding {@code .md} resource files and
 * concatenates them to produce the agent's system prompt.
 * </p>
 */
public final class SkillConfig {

	private static final SkillRegistry INSTANCE = createRegistry();

	/**
	 * Returns the singleton production skill registry.
	 */
	public static SkillRegistry getInstance() {
		return INSTANCE;
	}

	private static SkillRegistry createRegistry() {
		SkillRegistry registry = new SkillRegistry();

		registry.registerAgentSkills(ModelExplorer.class,
			"explorer/identity",
			"core/tool-first-rule",
			"core/zero-knowledge",
			"explorer/exhaustiveness-mandate",
			"explorer/scope",
			"explorer/exploration-strategy",
			"explorer/context-resolution",
			"explorer/fact-rules",
			"explorer/output-format",
			"explorer/examples");

		// Other agents will be added in subsequent tasks.

		return registry;
	}

	private SkillConfig() {
		// utility
	}
}
```

**Step 2: Remove @SystemMessage from ModelExplorer.java**

In `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/ModelExplorer.java`:

- Remove the entire `@SystemMessage("""...""")` annotation (lines 24-467)
- Keep `@UserMessage` (lines 468-487), `@Agent` (line 489), and the method signature (line 490)
- Remove the `import dev.langchain4j.service.SystemMessage;` import

The file should look like:

```java
package com.top_logic.ai.service.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agent for exploring the TopLogic application model.
 */
public interface ModelExplorer {
	/**
	 * Explores the TopLogic application model to answer a user question.
	 *
	 * @param userQuestion
	 *        The user's question about the model.
	 * @return The answer based on model exploration.
	 */
	@UserMessage("""
			...(keep existing @UserMessage content)...
			""")
	@Agent("Explores the TopLogic application model (read-only) to answer user questions with grounded model evidence")
	String explore(@V("userQuestion") String userQuestion);
}
```

**Step 3: Wire systemMessageProvider in AgentInvoker**

In `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java`, modify `buildModelExplorer()` (lines 167-178):

Before:
```java
private static ModelExplorer buildModelExplorer(ChatModel explorationModel) {
    ModelBrowserTool browserTool = new ModelBrowserTool();
    // ...
    return AgenticServices.agentBuilder(ModelExplorer.class)
        .chatModel(explorationModel)
        .tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool)
        .build();
}
```

After:
```java
private static ModelExplorer buildModelExplorer(ChatModel explorationModel) {
    ModelBrowserTool browserTool = new ModelBrowserTool();
    // ...
    String systemMessage = SkillConfig.getInstance().composeSystemMessage(ModelExplorer.class);
    return AgenticServices.agentBuilder(ModelExplorer.class)
        .chatModel(explorationModel)
        .systemMessageProvider(memoryId -> systemMessage)
        .tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool)
        .build();
}
```

Add import: `import com.top_logic.ai.service.skills.SkillConfig;`

**Step 4: Verify build compiles**

Run: `mvn compile -pl tl-ai-service`
Expected: BUILD SUCCESS

**Step 5: Run tests**

Run: `mvn test -pl tl-ai-service`
Expected: PASS

**Step 6: Commit**

```bash
git add tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/ModelExplorer.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java
git commit -m "Wire ModelExplorer to skill-composed system prompt

Remove @SystemMessage from ModelExplorer interface. System prompt is
now assembled by SkillConfig from resource files via systemMessageProvider.

Prompt: Create SkillConfig and wire ModelExplorer to skills"
```

---

### Task 5: Extract OrchestratorRouter system prompt into skill files

**Files:**
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/orchestrator/identity.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/orchestrator/delegation-rules.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/orchestrator/chained-delegation.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/orchestrator/work-order-discipline.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/orchestrator/behavior.md`

**Step 1: Analyze the OrchestratorRouter @SystemMessage**

The prompt (lines 29-143 of `OrchestratorRouter.java`) contains:

1. **Identity** (line 30) — "You are the Orchestrator..."
2. **CRITICAL: NO ANSWERS FROM OWN KNOWLEDGE** (lines 33-41) — Zero knowledge enforcement
3. **MANDATORY DELEGATION RULES** (lines 44-65) — When to delegate to which specialist
4. **CHAINED DELEGATION** (lines 68-79) — TLScript from model results
5. **THINK FIRST** (lines 82-87) — Planning steps
6. **SPECIALIST TOOLS** (lines 89-108) — Tool descriptions
7. **WORK ORDER DISCIPLINE** (lines 110-116) — Goal/task/context rules
8. **BEHAVIOR** (lines 119-127) — Flow summary
9. **ANTI-PATTERNS** (lines 129-137) — Forbidden patterns
10. **LEGACY PREFIXES** (lines 140-142) — /explore-model etc.

**Step 2: Create skill files**

| Skill file | Sections |
|---|---|
| `orchestrator/identity.md` | 1 + 2 (identity + zero-knowledge) |
| `orchestrator/delegation-rules.md` | 3 (mandatory delegation) |
| `orchestrator/chained-delegation.md` | 4 (chained delegation) |
| `orchestrator/work-order-discipline.md` | 5 + 6 + 7 (think first + tools + work orders) |
| `orchestrator/behavior.md` | 8 + 9 + 10 (behavior + anti-patterns + legacy) |

Extract the exact text from each section.

**Step 3: Register in SkillConfig**

Add to `SkillConfig.createRegistry()`:

```java
registry.registerAgentSkills(OrchestratorRouter.class,
    "orchestrator/identity",
    "orchestrator/delegation-rules",
    "orchestrator/chained-delegation",
    "orchestrator/work-order-discipline",
    "orchestrator/behavior");
```

**Step 4: Remove @SystemMessage from OrchestratorRouter.java**

Remove `@SystemMessage("""...""")` (lines 29-143). Keep `@UserMessage` and `@Agent`.
Remove `import dev.langchain4j.service.SystemMessage;`.

**Step 5: Wire systemMessageProvider in OrchestratorAgent.java**

In `OrchestratorAgent.java` (lines 54-61), change:

Before:
```java
OrchestratorRouter router = AgenticServices.agentBuilder(OrchestratorRouter.class)
    .chatModel(model)
    .chatMemory(chatMemory)
    .tools(...)
    .build();
```

After:
```java
String systemMessage = SkillConfig.getInstance().composeSystemMessage(OrchestratorRouter.class);
OrchestratorRouter router = AgenticServices.agentBuilder(OrchestratorRouter.class)
    .chatModel(model)
    .chatMemory(chatMemory)
    .systemMessageProvider(memoryId -> systemMessage)
    .tools(...)
    .build();
```

Add import: `import com.top_logic.ai.service.skills.SkillConfig;`

**Step 6: Add regression test**

Add to `TestSkillRegistry.java`:

```java
public void testOrchestratorComposedPromptContainsAllSections() {
    SkillRegistry registry = SkillConfig.getInstance();
    String composed = registry.composeSystemMessage(OrchestratorRouter.class);
    assertTrue(composed.contains("Orchestrator"));
    assertTrue(composed.contains("MANDATORY DELEGATION"));
    assertTrue(composed.contains("WORK ORDER DISCIPLINE"));
    assertTrue(composed.contains("ANTI-PATTERNS"));
}
```

**Step 7: Verify build and tests**

Run: `mvn test -pl tl-ai-service`
Expected: PASS

**Step 8: Commit**

```bash
git add tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/orchestrator/ \
       tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/OrchestratorRouter.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/OrchestratorAgent.java
git commit -m "Extract OrchestratorRouter system prompt into skill files

Split orchestrator prompt into 5 composable skills. Wired via
systemMessageProvider in OrchestratorAgent.

Prompt: Extract OrchestratorRouter system prompt into skill files"
```

---

### Task 6: Extract TLScriptAgent system prompt into skill files

**Files:**
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/tlscript/identity.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/tlscript/verification-rules.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/tlscript/grammar-rules.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/tlscript/response-rules.md`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/TLScriptAgent.java` — remove `@SystemMessage`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java:96-99` — use `systemMessageProvider`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java` — add TLScriptAgent mapping

**Step 1: Analyze the TLScriptAgent @SystemMessage**

The prompt (lines 30-205 of `TLScriptAgent.java`) contains:

1. **Identity** (lines 31-33)
2. **TOOL-FIRST RULE** (lines 36-43) — shared concept, but TLScript-specific wording
3. **ZERO PRIOR KNOWLEDGE RULE** (lines 46-54) — shared concept, TLScript-specific wording
4. **MANDATORY VERIFICATION RULES** (lines 57-69) — TLScript-specific
5. **SCRIPT AND CONTEXT RULES** (lines 72-76)
6. **TOOLS** (lines 79-87)
7. **WHEN TO USE REQUEST MODEL EXPLORATION** (lines 90-101)
8. **TLScript RULES** (lines 104-151) — Grammar
9. **RESPONSE RULES** (lines 153-161)
10. **EXAMPLES** (lines 163-204)

**Important decision for shared skills:** The TLScriptAgent's TOOL-FIRST RULE and ZERO PRIOR KNOWLEDGE sections have TLScript-specific wording (mentioning "Get TLScript function documentation" instead of generic tool calls). Two options:

- **Option A**: Use the same `core/tool-first-rule.md` and `core/zero-knowledge.md` (requires making the wording generic enough for both agents).
- **Option B**: Create `tlscript/tool-first-rule.md` and `tlscript/zero-knowledge.md` with TLScript-specific wording.

**Choose Option B for now** (preserve exact behavior), with a follow-up task to unify if desired.

**Step 2: Create skill files with exact text from current prompt**

| Skill file | Sections |
|---|---|
| `tlscript/identity.md` | 1 (identity) |
| `tlscript/tool-first-rule.md` | 2 (TLScript-specific tool-first) |
| `tlscript/zero-knowledge.md` | 3 (TLScript-specific zero-knowledge) |
| `tlscript/verification-rules.md` | 4 + 5 + 6 + 7 (verification + context + tools + when to use exploration) |
| `tlscript/grammar-rules.md` | 8 (TLScript grammar) |
| `tlscript/response-rules.md` | 9 + 10 (response rules + examples) |

**Step 3: Register in SkillConfig, remove @SystemMessage, wire systemMessageProvider**

Add to `SkillConfig`:
```java
registry.registerAgentSkills(TLScriptAgent.class,
    "tlscript/identity",
    "tlscript/tool-first-rule",
    "tlscript/zero-knowledge",
    "tlscript/verification-rules",
    "tlscript/grammar-rules",
    "tlscript/response-rules");
```

In `AgentInvoker.invokeTLScript()` (lines 96-99), add `systemMessageProvider`:
```java
String systemMessage = SkillConfig.getInstance().composeSystemMessage(TLScriptAgent.class);
final TLScriptAgent agent = AgenticServices.agentBuilder(TLScriptAgent.class)
    .chatModel(tlscriptModel)
    .systemMessageProvider(memoryId -> systemMessage)
    .tools(new TLScriptReferenceTool(), new AgentDelegationTools.Exploration("TLScriptAgent"), new TLScriptTool())
    .build();
```

**Step 4: Add regression test, verify build, commit**

Run: `mvn test -pl tl-ai-service`

```bash
git add tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/tlscript/ \
       tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/TLScriptAgent.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java
git commit -m "Extract TLScriptAgent system prompt into skill files

Split TLScript prompt into 6 composable skills. Uses TLScript-specific
tool-first and zero-knowledge variants (not shared core) to preserve
exact agent behavior.

Prompt: Extract TLScriptAgent system prompt into skill files"
```

---

### Task 7: Extract ModelEditor system prompt into skill files

**Files:**
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/editor/identity.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/editor/creation-rules.md`
- Create: `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/editor/output-format.md`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/ModelEditor.java` — remove `@SystemMessage`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java:180-184` — use `systemMessageProvider`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java` — add ModelEditor mapping

**Step 1: Analyze the ModelEditor @SystemMessage**

The prompt (lines 25-119 of `ModelEditor.java`) contains:

1. **Identity** (lines 26-27)
2. **TOOL-FIRST RULE** (lines 31-37)
3. **IDEMPOTENT CREATION** (lines 40-43)
4. **AVAILABLE TOOLS** (lines 46-58)
5. **MANDATORY CREATION ORDER** (lines 63-75)
6. **NAMING CONVENTIONS** (lines 79-86)
7. **I18N** (lines 90-95)
8. **OUTPUT FORMAT** (lines 99-118)

**Step 2: Create skill files**

| Skill file | Sections |
|---|---|
| `editor/identity.md` | 1 + 2 (identity + tool-first) |
| `editor/creation-rules.md` | 3 + 4 + 5 + 6 + 7 (idempotent + tools + order + naming + i18n) |
| `editor/output-format.md` | 8 (output format) |

**Step 3: Register, remove @SystemMessage, wire systemMessageProvider**

Add to `SkillConfig`:
```java
registry.registerAgentSkills(ModelEditor.class,
    "editor/identity",
    "editor/creation-rules",
    "editor/output-format");
```

In `AgentInvoker.buildModelEditor()` (lines 180-184):
```java
private static ModelEditor buildModelEditor(ChatModel creationModel) {
    String systemMessage = SkillConfig.getInstance().composeSystemMessage(ModelEditor.class);
    return AgenticServices.agentBuilder(ModelEditor.class)
        .chatModel(creationModel)
        .systemMessageProvider(memoryId -> systemMessage)
        .tools(new ModelEditorTool(), new AgentDelegationTools.TLScript("ModelEditor"))
        .build();
}
```

**Step 4: Verify build and tests, commit**

Run: `mvn test -pl tl-ai-service`

```bash
git add tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/editor/ \
       tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillConfig.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/ModelEditor.java \
       tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java
git commit -m "Extract ModelEditor system prompt into skill files

Split editor prompt into 3 composable skills. All four primary agents
now use skill-composed system prompts.

Prompt: Extract ModelEditor system prompt into skill files"
```

---

### Task 8: Verify all agents are wired and add comprehensive tests

**Files:**
- Modify: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillRegistry.java`

**Step 1: Write comprehensive tests**

```java
public void testAllAgentsHaveSkills() {
    SkillRegistry registry = SkillConfig.getInstance();
    // All four primary agents must have skills configured
    assertFalse(registry.getSkillsFor(ModelExplorer.class).isEmpty());
    assertFalse(registry.getSkillsFor(OrchestratorRouter.class).isEmpty());
    assertFalse(registry.getSkillsFor(TLScriptAgent.class).isEmpty());
    assertFalse(registry.getSkillsFor(ModelEditor.class).isEmpty());
}

public void testAllSkillResourcesExist() {
    SkillRegistry registry = SkillConfig.getInstance();
    // Force-load all skills to verify resources exist
    for (Class<?> agentClass : List.of(
            ModelExplorer.class, OrchestratorRouter.class,
            TLScriptAgent.class, ModelEditor.class)) {
        List<Skill> skills = registry.getSkillsFor(agentClass);
        for (Skill skill : skills) {
            assertNotNull("Skill content should not be null: " + skill.name(), skill.content());
            assertFalse("Skill content should not be empty: " + skill.name(), skill.content().isEmpty());
        }
    }
}

public void testNoAgentInterfaceHasSystemMessageAnnotation() {
    // Verify @SystemMessage was removed from all agent interfaces
    assertFalse(hasSystemMessageAnnotation(ModelExplorer.class));
    assertFalse(hasSystemMessageAnnotation(OrchestratorRouter.class));
    assertFalse(hasSystemMessageAnnotation(TLScriptAgent.class));
    assertFalse(hasSystemMessageAnnotation(ModelEditor.class));
}

private boolean hasSystemMessageAnnotation(Class<?> clazz) {
    for (var method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(dev.langchain4j.service.SystemMessage.class)) {
            return true;
        }
    }
    return false;
}
```

**Step 2: Run all tests**

Run: `mvn test -pl tl-ai-service`
Expected: ALL PASS

**Step 3: Commit**

```bash
git add tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillRegistry.java
git commit -m "Add comprehensive skill system verification tests

Tests verify all agents have skills configured, all resources load,
and no agent interface retains @SystemMessage annotation.

Prompt: Add comprehensive tests for the skill system"
```

---

### Task 9: Functional verification with run-and-verify

**Step 1: Start the application and verify chat works**

Use the `run-and-verify` skill to start the app and test that the chat interface still works correctly with the skill-composed prompts.

**Step 2: Test a model exploration query**

Send a query like "What modules exist?" and verify the ModelExplorer responds with tool-backed facts (not hallucination). This confirms the skill-composed prompt preserves the zero-knowledge and tool-first behaviors.

**Step 3: Test an orchestrator delegation**

Send a query that requires delegation and verify the orchestrator correctly routes to a specialist.

**Step 4: Final commit if any fixes needed**

---

## Summary

| Task | Description | Files touched |
|------|-------------|---------------|
| 1 | Skill record | 2 new (Skill.java + test) |
| 2 | SkillRegistry | 3 new (SkillRegistry.java + test + test resource) |
| 3 | Extract ModelExplorer prompt | ~8 new .md files |
| 4 | SkillConfig + wire ModelExplorer | 3 modified (SkillConfig new, ModelExplorer, AgentInvoker) |
| 5 | Extract + wire OrchestratorRouter | ~5 new .md + 3 modified |
| 6 | Extract + wire TLScriptAgent | ~6 new .md + 3 modified |
| 7 | Extract + wire ModelEditor | ~3 new .md + 3 modified |
| 8 | Comprehensive tests | 1 modified (test file) |
| 9 | Functional verification | 0 files (manual test) |
