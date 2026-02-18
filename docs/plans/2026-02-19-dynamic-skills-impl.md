# Dynamic Skill Loading — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Two-tier skill system where core skills remain in the system prompt and on-demand skills are discoverable via a `SkillTool` that every agent receives.

**Architecture:** Add YAML frontmatter to all `.md` skill files with `description` and `tier`. Extend `Skill` record with `SkillTier`. `SkillRegistry` parses frontmatter and exposes tier-aware methods. New `SkillTool` class with `@Tool`-annotated `listAvailableSkills()` and `loadSkill()` methods is wired into every agent.

**Tech Stack:** Java 21, LangChain4j 1.10.0 (`@Tool`, `@P`), JUnit (TestCase), Maven

**Design doc:** `docs/plans/2026-02-19-dynamic-skills-design.md`

---

### Task 1: Add `SkillTier` Enum

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillTier.java`
- Test: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillTier.java`

**Step 1: Write the failing test**

File: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillTier.java`

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkillTier extends TestCase {

	public void testFromStringCore() {
		assertEquals(SkillTier.CORE, SkillTier.fromString("core"));
	}

	public void testFromStringOnDemand() {
		assertEquals(SkillTier.ON_DEMAND, SkillTier.fromString("on-demand"));
	}

	public void testFromStringCaseInsensitive() {
		assertEquals(SkillTier.CORE, SkillTier.fromString("CORE"));
		assertEquals(SkillTier.ON_DEMAND, SkillTier.fromString("ON-DEMAND"));
	}

	public void testFromStringInvalid() {
		try {
			SkillTier.fromString("unknown");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	public void testDefaultIsCoreWhenNull() {
		assertEquals(SkillTier.CORE, SkillTier.fromString(null));
	}
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillTier -DfailIfNoTests=false`
Expected: Compilation failure — `SkillTier` does not exist.

**Step 3: Write minimal implementation**

File: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillTier.java`

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * Classification of skills into loading tiers.
 *
 * <p>
 * {@link #CORE} skills are always included in the system prompt.
 * {@link #ON_DEMAND} skills are discoverable via {@link SkillTool} and loaded by the AI when needed.
 * </p>
 */
public enum SkillTier {

	/** Always included in the agent's system prompt. */
	CORE("core"),

	/** Discoverable via tool; loaded on demand by the AI. */
	ON_DEMAND("on-demand");

	private final String _key;

	SkillTier(String key) {
		_key = key;
	}

	/**
	 * Parses a tier string from YAML frontmatter.
	 *
	 * @param value
	 *        The frontmatter value (e.g. {@code "core"} or {@code "on-demand"}).
	 *        {@code null} defaults to {@link #CORE}.
	 * @return The matching tier.
	 * @throws IllegalArgumentException
	 *         if the value does not match any tier.
	 */
	public static SkillTier fromString(String value) {
		if (value == null) {
			return CORE;
		}
		String normalized = value.strip().toLowerCase();
		for (SkillTier tier : values()) {
			if (tier._key.equals(normalized)) {
				return tier;
			}
		}
		throw new IllegalArgumentException("Unknown skill tier: " + value);
	}
}
```

**Step 4: Run test to verify it passes**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillTier`
Expected: All 5 tests PASS.

**Step 5: Commit**

```
feat(skills): add SkillTier enum for core vs on-demand classification
```

---

### Task 2: Update `Skill` Record with Tier

**Files:**
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/Skill.java`
- Modify: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkill.java`

**Step 1: Update the test to expect a tier field**

Add to `TestSkill.java`:

```java
public void testSkillWithTier() {
    Skill skill = new Skill("test-skill", "A test skill", SkillTier.ON_DEMAND, "You are a test agent.");
    assertEquals("test-skill", skill.name());
    assertEquals("A test skill", skill.description());
    assertEquals(SkillTier.ON_DEMAND, skill.tier());
    assertEquals("You are a test agent.", skill.content());
}

public void testCoreSkillTier() {
    Skill skill = new Skill("core/rule", "A core rule", SkillTier.CORE, "HARD GATE...");
    assertEquals(SkillTier.CORE, skill.tier());
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkill -DfailIfNoTests=false`
Expected: Compilation failure — `Skill` constructor doesn't accept 4 args.

**Step 3: Update Skill record**

File: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/Skill.java`

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

/**
 * A reusable behavioral instruction fragment that can be composed into an agent's system prompt
 * or loaded on demand by the AI via {@link SkillTool}.
 *
 * <p>
 * Skills are loaded from classpath resource files ({@code .md}) with YAML frontmatter
 * and assembled by {@link SkillRegistry}.
 * </p>
 *
 * @param name
 *        Unique skill identifier (e.g. {@code "core/zero-knowledge"}).
 * @param description
 *        Human-readable description from YAML frontmatter.
 * @param tier
 *        Whether this skill is {@link SkillTier#CORE} or {@link SkillTier#ON_DEMAND}.
 * @param content
 *        The actual prompt text (frontmatter stripped).
 */
public record Skill(String name, String description, SkillTier tier, String content) {
}
```

**Step 4: Fix compilation in existing code**

Update `SkillRegistry.loadSkill()` to pass `SkillTier.CORE` as a temporary default (will be replaced in Task 3 by frontmatter parsing):

```java
// In SkillRegistry.loadSkill(), change:
return new Skill(name, name, content);
// to:
return new Skill(name, name, SkillTier.CORE, content);
```

Update existing `TestSkill.testSkillCreation()` and `testSkillContentNonEmpty()` to use the 4-arg constructor:

```java
public void testSkillCreation() {
    Skill skill = new Skill("test-skill", "A test skill", SkillTier.CORE, "You are a test agent.");
    assertEquals("test-skill", skill.name());
    assertEquals("A test skill", skill.description());
    assertEquals(SkillTier.CORE, skill.tier());
    assertEquals("You are a test agent.", skill.content());
}

public void testSkillContentNonEmpty() {
    Skill skill = new Skill("s", "d", SkillTier.CORE, "content");
    assertFalse(skill.content().isEmpty());
}
```

**Step 5: Run all tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkill,TestSkillRegistry,TestSkillTier`
Expected: All tests PASS.

**Step 6: Commit**

```
feat(skills): add tier field to Skill record
```

---

### Task 3: Implement YAML Frontmatter Parsing in `SkillRegistry`

**Files:**
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillRegistry.java`
- Modify: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillRegistry.java`
- Create: `tl-ai-service/src/test/resources/com/top_logic/ai/service/skills/test/frontmatter-skill.md`
- Create: `tl-ai-service/src/test/resources/com/top_logic/ai/service/skills/test/on-demand-skill.md`

**Step 1: Create test skill files with frontmatter**

File: `tl-ai-service/src/test/resources/com/top_logic/ai/service/skills/test/frontmatter-skill.md`

```markdown
---
description: A test skill with frontmatter
tier: core
---
### FRONTMATTER RULE

This skill has YAML frontmatter.
```

File: `tl-ai-service/src/test/resources/com/top_logic/ai/service/skills/test/on-demand-skill.md`

```markdown
---
description: An on-demand test skill
tier: on-demand
---
### ON-DEMAND CONTENT

This is on-demand skill content.
```

**Step 2: Write failing tests for frontmatter parsing**

Add to `TestSkillRegistry.java`:

```java
public void testFrontmatterParsing() {
    SkillRegistry registry = new SkillRegistry();
    Skill skill = registry.getSkill("test/frontmatter-skill");
    assertEquals("A test skill with frontmatter", skill.description());
    assertEquals(SkillTier.CORE, skill.tier());
    assertTrue(skill.content().contains("FRONTMATTER RULE"));
    assertFalse("Frontmatter should be stripped from content", skill.content().contains("---\ndescription:"));
}

public void testOnDemandFrontmatter() {
    SkillRegistry registry = new SkillRegistry();
    Skill skill = registry.getSkill("test/on-demand-skill");
    assertEquals("An on-demand test skill", skill.description());
    assertEquals(SkillTier.ON_DEMAND, skill.tier());
    assertTrue(skill.content().contains("ON-DEMAND CONTENT"));
}

public void testSkillWithoutFrontmatterDefaultsToCore() {
    SkillRegistry registry = new SkillRegistry();
    Skill skill = registry.getSkill("test/sample-skill");
    assertEquals(SkillTier.CORE, skill.tier());
    // Description defaults to name when no frontmatter
    assertEquals("test/sample-skill", skill.description());
}
```

**Step 3: Run tests to verify they fail**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillRegistry#testFrontmatterParsing`
Expected: FAIL — frontmatter not parsed yet, description still equals name.

**Step 4: Implement frontmatter parsing in `SkillRegistry.loadSkill()`**

Replace the `loadSkill` method in `SkillRegistry.java`:

```java
private static final String FRONTMATTER_DELIMITER = "---";

private Skill loadSkill(String name) {
    String resourcePath = RESOURCE_BASE + name + ".md";
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
        if (is == null) {
            throw new IllegalArgumentException("Skill resource not found: " + resourcePath);
        }
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        return parseSkill(name, raw);
    } catch (IOException e) {
        throw new RuntimeException("Failed to load skill: " + resourcePath, e);
    }
}

private Skill parseSkill(String name, String raw) {
    if (!raw.startsWith(FRONTMATTER_DELIMITER)) {
        return new Skill(name, name, SkillTier.CORE, raw);
    }

    int endIndex = raw.indexOf(FRONTMATTER_DELIMITER, FRONTMATTER_DELIMITER.length());
    if (endIndex < 0) {
        return new Skill(name, name, SkillTier.CORE, raw);
    }

    String frontmatter = raw.substring(FRONTMATTER_DELIMITER.length(), endIndex).trim();
    String content = raw.substring(endIndex + FRONTMATTER_DELIMITER.length()).trim();

    String description = name;
    SkillTier tier = SkillTier.CORE;

    for (String line : frontmatter.split("\n")) {
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            continue;
        }
        String key = line.substring(0, colonIndex).trim();
        String value = line.substring(colonIndex + 1).trim();
        switch (key) {
            case "description":
                description = value;
                break;
            case "tier":
                tier = SkillTier.fromString(value);
                break;
        }
    }
    return new Skill(name, description, tier, content);
}
```

**Step 5: Run tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillRegistry`
Expected: All tests PASS.

**Step 6: Commit**

```
feat(skills): parse YAML frontmatter from skill .md files
```

---

### Task 4: Add Tier-Aware Methods to `SkillRegistry`

**Files:**
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillRegistry.java`
- Modify: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillRegistry.java`

**Step 1: Write failing tests**

Add to `TestSkillRegistry.java`:

```java
public void testGetOnDemandSkillsFor() {
    SkillRegistry registry = new SkillRegistry();
    registry.registerAgentSkills(TestAgent.class,
        "test/frontmatter-skill", "test/on-demand-skill");
    List<Skill> onDemand = registry.getOnDemandSkillsFor(TestAgent.class);
    assertEquals(1, onDemand.size());
    assertEquals("test/on-demand-skill", onDemand.get(0).name());
    assertEquals(SkillTier.ON_DEMAND, onDemand.get(0).tier());
}

public void testComposeSystemMessageOnlyIncludesCoreSkills() {
    SkillRegistry registry = new SkillRegistry();
    registry.registerAgentSkills(TestAgent.class,
        "test/frontmatter-skill", "test/on-demand-skill");
    String message = registry.composeSystemMessage(TestAgent.class);
    assertTrue("Should contain core skill content", message.contains("FRONTMATTER RULE"));
    assertFalse("Should NOT contain on-demand skill content", message.contains("ON-DEMAND CONTENT"));
}
```

Add import at top of `TestSkillRegistry.java`:

```java
import java.util.List;
```

**Step 2: Run tests to verify they fail**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillRegistry#testGetOnDemandSkillsFor`
Expected: Compilation failure — `getOnDemandSkillsFor` does not exist.

**Step 3: Implement tier-aware methods**

Add to `SkillRegistry.java`:

```java
/**
 * Returns the on-demand skills configured for the given agent class.
 *
 * @param agentClass
 *        The agent interface class.
 * @return On-demand skills in registration order.
 */
public List<Skill> getOnDemandSkillsFor(Class<?> agentClass) {
    return getSkillsFor(agentClass).stream()
        .filter(skill -> skill.tier() == SkillTier.ON_DEMAND)
        .toList();
}
```

Update `composeSystemMessage` to filter core-only:

```java
public String composeSystemMessage(Class<?> agentClass) {
    List<Skill> skills = getSkillsFor(agentClass);
    StringBuilder sb = new StringBuilder();
    for (Skill skill : skills) {
        if (skill.tier() != SkillTier.CORE) {
            continue;
        }
        if (sb.length() > 0) {
            sb.append(SKILL_SEPARATOR);
        }
        sb.append(skill.content());
    }
    return sb.toString();
}
```

Add the stream import at the top of `SkillRegistry.java` (if not already present — it uses `stream().filter().toList()` which requires no extra import on Java 16+; just ensure the method is there).

**Step 4: Run tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillRegistry`
Expected: All tests PASS.

**Important:** Some existing tests like `testModelExplorerPromptContainsKeySections` check for content that is now in on-demand skills (e.g. `OUTPUT FORMAT`). These tests will fail until Task 5 adds frontmatter to the production skill files. We'll fix them in Task 7. For now, verify only the new tests and the ones that don't depend on on-demand content:

Run: `mvn test -pl tl-ai-service -Dtest="TestSkillRegistry#testLoadSkillFromResource+testLoadNonExistentSkillThrows+testComposeSystemMessage+testComposeMultipleSkills+testAllAgentsHaveSkills+testAllSkillResourcesLoadSuccessfully+testFrontmatterParsing+testOnDemandFrontmatter+testSkillWithoutFrontmatterDefaultsToCore+testGetOnDemandSkillsFor+testComposeSystemMessageOnlyIncludesCoreSkills"`

**Step 5: Commit**

```
feat(skills): add tier-aware getOnDemandSkillsFor and filter composeSystemMessage to core-only
```

---

### Task 5: Add YAML Frontmatter to All Production Skill Files

**Files:**
- Modify: All 24 `.md` files under `tl-ai-service/src/main/resources/com/top_logic/ai/service/skills/`

This task adds frontmatter to each file. The existing content is preserved exactly — only a `---\ndescription: ...\ntier: ...\n---` block is prepended.

**Core skills** (tier: core):

| File | description |
|------|-------------|
| `explorer/identity.md` | Model Explorer agent identity and role definition |
| `core/tool-first-rule.md` | Hard gate requiring at least one tool call before any answer |
| `core/zero-knowledge.md` | Zero prior knowledge rule — all facts must come from tools |
| `explorer/exhaustiveness-mandate.md` | Mandatory tool calls before making any claim |
| `explorer/fact-rules.md` | Fact guarantee rules, verbatim part names, relationship evidence |
| `orchestrator/identity.md` | Orchestrator agent identity — plans and delegates, never answers directly |
| `orchestrator/delegation-rules.md` | Mandatory delegation rules for model, TLScript, and creation questions |
| `orchestrator/chained-delegation.md` | Rules for chaining TLScript specialist after model exploration |
| `orchestrator/work-order-discipline.md` | Work order discipline and specialist tool descriptions |
| `tlscript/identity.md` | TLScript Specialist agent identity and role |
| `tlscript/tool-first-rule.md` | Hard gate for TLScript — verify functions before using them |
| `tlscript/zero-knowledge.md` | Zero knowledge rule for TLScript functions and model elements |
| `tlscript/verification-rules.md` | Mandatory verification, script/context rules, and tool descriptions |
| `editor/identity.md` | Model Creator agent identity with tool-first hard gate |
| `editor/creation-rules.md` | Idempotent creation, available tools, creation order, naming, i18n |

**On-demand skills** (tier: on-demand):

| File | description |
|------|-------------|
| `explorer/scope.md` | Scope rules for model exploration — application vs framework modules |
| `explorer/exploration-strategy.md` | Step-by-step exploration loop with completion checks and tool recipes |
| `explorer/context-resolution.md` | Context resolution rules for ambiguous type and module references |
| `explorer/output-format.md` | Strict output format — Evidence, Facts, Next suggested query |
| `explorer/examples.md` | Example exploration interactions demonstrating correct tool-first behavior |
| `orchestrator/behavior.md` | Orchestrator behavioral guidelines, anti-patterns, and legacy prefix handling |
| `tlscript/grammar-rules.md` | TLScript grammar reference — expressions, bindings, variables, model literals, chaining |
| `tlscript/response-rules.md` | TLScript response formatting rules and mandatory validation examples |
| `editor/output-format.md` | Strict output format for model creation results |

**Step 1: Add frontmatter to each file**

For each file, prepend the frontmatter block. Example for `explorer/scope.md` — the file currently starts with `---` as a section divider, so replace the leading `---` with the frontmatter block:

Before:
```
---

### SCOPE
...
```

After:
```
---
description: Scope rules for model exploration — application vs framework modules
tier: on-demand
---

### SCOPE
...
```

For files that do NOT start with `---` (like `explorer/identity.md`), insert the frontmatter block before the existing content:

Before:
```
You are a **TopLogic Model Explorer** agent...
```

After:
```
---
description: Model Explorer agent identity and role definition
tier: core
---
You are a **TopLogic Model Explorer** agent...
```

Apply this pattern to all 24 files using the descriptions from the tables above.

**Step 2: Verify all skills still load**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillRegistry#testAllSkillResourcesLoadSuccessfully`
Expected: PASS — all skills load with frontmatter parsed.

**Step 3: Commit**

```
feat(skills): add YAML frontmatter to all skill files
```

---

### Task 6: Create `SkillTool`

**Files:**
- Create: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillTool.java`
- Create: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillTool.java`

**Step 1: Write the failing test**

File: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillTool.java`

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkillTool extends TestCase {

	private SkillRegistry _registry;

	@Override
	protected void setUp() {
		_registry = new SkillRegistry();
		_registry.registerAgentSkills(TestAgent.class,
			"test/frontmatter-skill", "test/on-demand-skill");
	}

	public void testListAvailableSkillsReturnsOnDemandOnly() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.listAvailableSkills();
		assertTrue("Should list on-demand skill", result.contains("test/on-demand-skill"));
		assertTrue("Should include description", result.contains("An on-demand test skill"));
		assertFalse("Should NOT list core skill", result.contains("test/frontmatter-skill"));
	}

	public void testLoadSkillReturnsContent() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.loadSkill("test/on-demand-skill");
		assertTrue("Should contain skill content", result.contains("ON-DEMAND CONTENT"));
		assertFalse("Should NOT contain frontmatter", result.contains("description:"));
	}

	public void testLoadSkillRejectsUnregisteredSkill() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.loadSkill("nonexistent/skill");
		assertTrue("Should return error message", result.contains("not available"));
	}

	public void testLoadSkillRejectsCoreSkill() {
		SkillTool tool = new SkillTool(_registry, TestAgent.class);
		String result = tool.loadSkill("test/frontmatter-skill");
		assertTrue("Should reject core skill", result.contains("not available")
			|| result.contains("already loaded"));
	}

	public void testListAvailableSkillsEmptyWhenNoOnDemand() {
		SkillRegistry registry = new SkillRegistry();
		registry.registerAgentSkills(TestAgent.class, "test/frontmatter-skill");
		SkillTool tool = new SkillTool(registry, TestAgent.class);
		String result = tool.listAvailableSkills();
		assertTrue("Should indicate no skills available",
			result.contains("No on-demand skills") || result.trim().isEmpty());
	}

	interface TestAgent {
		String chat(String msg);
	}
}
```

**Step 2: Run test to verify it fails**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillTool -DfailIfNoTests=false`
Expected: Compilation failure — `SkillTool` does not exist.

**Step 3: Write minimal implementation**

File: `tl-ai-service/src/main/java/com/top_logic/ai/service/skills/SkillTool.java`

```java
/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import java.util.List;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * LangChain4j tool that allows agents to discover and load on-demand skills at runtime.
 *
 * <p>
 * Each instance is scoped to a specific agent class, exposing only the on-demand skills
 * registered for that agent. Core skills are excluded — they are already in the system prompt.
 * </p>
 */
public class SkillTool {

	private final SkillRegistry _registry;

	private final Class<?> _agentClass;

	/**
	 * Creates a skill tool scoped to the given agent.
	 *
	 * @param registry
	 *        The skill registry.
	 * @param agentClass
	 *        The agent class whose on-demand skills are exposed.
	 */
	public SkillTool(SkillRegistry registry, Class<?> agentClass) {
		_registry = registry;
		_agentClass = agentClass;
	}

	/**
	 * Lists all on-demand skills available for this agent.
	 *
	 * @return Formatted skill catalog with names and descriptions.
	 */
	@Tool("Lists all on-demand skills available. Returns skill names with descriptions. Call to discover specialized knowledge you can load for the current task.")
	public String listAvailableSkills() {
		List<Skill> skills = _registry.getOnDemandSkillsFor(_agentClass);
		if (skills.isEmpty()) {
			return "No on-demand skills available for this agent.";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Available skills (use loadSkill to load one):\n");
		for (Skill skill : skills) {
			sb.append("- ").append(skill.name()).append(": ").append(skill.description()).append("\n");
		}
		return sb.toString();
	}

	/**
	 * Loads a skill by name, returning its full content.
	 *
	 * @param skillName
	 *        The skill name from the catalog (e.g. {@code "explorer/scope"}).
	 * @return The full skill content, or an error message if not available.
	 */
	@Tool("Loads a skill by name, returning its full content. Call after listAvailableSkills to load instructions you need for the current task.")
	public String loadSkill(@P("Skill name from the catalog, e.g. 'explorer/scope'") String skillName) {
		List<Skill> onDemand = _registry.getOnDemandSkillsFor(_agentClass);
		for (Skill skill : onDemand) {
			if (skill.name().equals(skillName)) {
				return skill.content();
			}
		}
		return "Skill '" + skillName + "' is not available as an on-demand skill for this agent.";
	}
}
```

**Step 4: Run tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillTool`
Expected: All 5 tests PASS.

**Step 5: Commit**

```
feat(skills): add SkillTool for dynamic on-demand skill loading
```

---

### Task 7: Wire `SkillTool` into Agent Builders

**Files:**
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/AgentInvoker.java`
- Modify: `tl-ai-service/src/main/java/com/top_logic/ai/service/agents/OrchestratorAgent.java`

**Step 1: Add SkillTool to OrchestratorAgent**

In `OrchestratorAgent.chat()`, add a `SkillTool` instance to the tools list:

```java
// Add import at top:
import com.top_logic.ai.service.skills.SkillTool;

// In chat() method, after the systemMessage line, add:
SkillTool skillTool = new SkillTool(SkillConfig.getInstance(), OrchestratorRouter.class);

// Change .tools(...) to include skillTool:
.tools(
    new AgentDelegationTools.Exploration("Orchestrator"),
    new AgentDelegationTools.Creation("Orchestrator"),
    new AgentDelegationTools.TLScript("Orchestrator"),
    skillTool)
```

**Step 2: Add SkillTool to AgentInvoker.buildModelExplorer()**

```java
// Add import at top:
import com.top_logic.ai.service.skills.SkillTool;

// In buildModelExplorer(), after the systemMessage line, add:
SkillTool skillTool = new SkillTool(SkillConfig.getInstance(), ModelExplorer.class);

// Change .tools(...) to include skillTool:
.tools(browserTool, instanceTool, analysisTool, searchTool, annotationTool, definitionTool, skillTool)
```

**Step 3: Add SkillTool to AgentInvoker.invokeTLScript()**

```java
// In invokeTLScript(), after the systemMessage line, add:
SkillTool skillTool = new SkillTool(SkillConfig.getInstance(), TLScriptAgent.class);

// Change .tools(...) to include skillTool:
.tools(new TLScriptReferenceTool(), new AgentDelegationTools.Exploration("TLScriptAgent"), new TLScriptTool(), skillTool)
```

**Step 4: Add SkillTool to AgentInvoker.buildModelEditor()**

```java
// In buildModelEditor(), after the systemMessage line, add:
SkillTool skillTool = new SkillTool(SkillConfig.getInstance(), ModelEditor.class);

// Change .tools(...) to include skillTool:
.tools(new ModelEditorTool(), new AgentDelegationTools.TLScript("ModelEditor"), skillTool)
```

**Step 5: Verify compilation**

Run: `mvn compile -pl tl-ai-service`
Expected: BUILD SUCCESS.

**Step 6: Commit**

```
feat(skills): wire SkillTool into all agent builders
```

---

### Task 8: Update Existing Tests

**Files:**
- Modify: `tl-ai-service/src/test/java/com/top_logic/ai/service/skills/TestSkillRegistry.java`

The existing tests that check `composeSystemMessage` content need updating because some content is now in on-demand skills (excluded from system prompt).

**Step 1: Update `testModelExplorerPromptContainsKeySections`**

The `OUTPUT FORMAT` section is now on-demand. Update the test:

```java
public void testModelExplorerPromptContainsKeySections() {
    String prompt = SkillConfig.getInstance().composeSystemMessage(ModelExplorer.class);
    assertTrue(prompt.contains("TopLogic Model Explorer"));
    assertTrue(prompt.contains("TOOL-FIRST RULE"));
    assertTrue(prompt.contains("ZERO PRIOR KNOWLEDGE"));
    // OUTPUT FORMAT is now on-demand, so NOT in system prompt
    assertFalse("OUTPUT FORMAT should be on-demand",
        prompt.contains("OUTPUT FORMAT"));
}
```

**Step 2: Update `testOrchestratorPromptContainsKeySections`**

The `BEHAVIOR` section is now on-demand:

```java
public void testOrchestratorPromptContainsKeySections() {
    String prompt = SkillConfig.getInstance().composeSystemMessage(OrchestratorRouter.class);
    assertTrue(prompt.contains("Orchestrator"));
    assertTrue(prompt.contains("MANDATORY DELEGATION"));
    assertTrue(prompt.contains("WORK ORDER DISCIPLINE"));
    // BEHAVIOR is now on-demand
    assertFalse("BEHAVIOR should be on-demand",
        prompt.contains("ANTI-PATTERNS (FORBIDDEN)"));
}
```

**Step 3: Update `testTLScriptPromptContainsKeySections`**

The `TLScript RULES` section (grammar-rules) is now on-demand:

```java
public void testTLScriptPromptContainsKeySections() {
    String prompt = SkillConfig.getInstance().composeSystemMessage(TLScriptAgent.class);
    assertTrue(prompt.contains("TLScript Specialist"));
    assertTrue(prompt.contains("TOOL-FIRST RULE"));
    // Grammar rules and response rules are now on-demand
    assertFalse("Grammar rules should be on-demand",
        prompt.contains("TLScript is expression-based"));
}
```

**Step 4: Update `testModelEditorPromptContainsKeySections`**

The output format is now on-demand:

```java
public void testModelEditorPromptContainsKeySections() {
    String prompt = SkillConfig.getInstance().composeSystemMessage(ModelEditor.class);
    assertTrue(prompt.contains("TopLogic Model Creator"));
    assertTrue(prompt.contains("MANDATORY CREATION ORDER"));
    // OUTPUT FORMAT is now on-demand
    assertFalse("OUTPUT FORMAT should be on-demand",
        prompt.contains("Creation Summary"));
}
```

**Step 5: Add new test for on-demand skill availability**

```java
public void testOnDemandSkillsAvailableForAllAgents() {
    SkillRegistry registry = SkillConfig.getInstance();
    assertFalse("ModelExplorer should have on-demand skills",
        registry.getOnDemandSkillsFor(ModelExplorer.class).isEmpty());
    assertFalse("OrchestratorRouter should have on-demand skills",
        registry.getOnDemandSkillsFor(OrchestratorRouter.class).isEmpty());
    assertFalse("TLScriptAgent should have on-demand skills",
        registry.getOnDemandSkillsFor(TLScriptAgent.class).isEmpty());
    assertFalse("ModelEditor should have on-demand skills",
        registry.getOnDemandSkillsFor(ModelEditor.class).isEmpty());
}

public void testAllSkillDescriptionsAreNotJustNames() {
    SkillRegistry registry = SkillConfig.getInstance();
    Class<?>[] agents = {ModelExplorer.class, OrchestratorRouter.class,
        TLScriptAgent.class, ModelEditor.class};
    for (Class<?> agentClass : agents) {
        for (Skill skill : registry.getSkillsFor(agentClass)) {
            assertFalse("Skill " + skill.name() + " should have a real description (not just its name)",
                skill.name().equals(skill.description()));
        }
    }
}
```

**Step 6: Run all tests to verify they pass**

Run: `mvn test -pl tl-ai-service -Dtest=TestSkillRegistry,TestSkillTool,TestSkillTier,TestSkill`
Expected: All tests PASS.

**Step 7: Commit**

```
feat(skills): update tests for two-tier skill system
```

---

### Task 9: Final Verification

**Step 1: Run full module build**

Run: `mvn clean install -pl tl-ai-service`
Expected: BUILD SUCCESS with all tests passing.

**Step 2: Verify system prompt sizes reduced**

Add a quick sanity check (can be run manually, not committed):

```java
// In any test:
SkillRegistry registry = SkillConfig.getInstance();
String before = registry.getSkillsFor(ModelExplorer.class).stream()
    .map(Skill::content).reduce("", (a, b) -> a + b);
String after = registry.composeSystemMessage(ModelExplorer.class);
System.out.println("ModelExplorer full: " + before.length() + " chars, core-only: " + after.length() + " chars");
```

Expected: Core-only prompt is significantly shorter than full prompt.

**Step 3: Commit final state**

```
feat(skills): complete dynamic skill loading system — two-tier with SkillTool
```
