# Dynamic Skill Loading System

## Problem

Skills are currently composed into a static system prompt at agent build time. Every skill registered for an agent is concatenated and baked in, regardless of whether the current task needs it. This wastes context window tokens and prevents the AI from selectively loading specialized knowledge.

## Solution

A two-tier skill system where core skills remain in the system prompt and on-demand skills are discoverable via a `SkillTool` that every agent receives. Mirrors the Claude Code pattern: short catalog in context, AI loads full skill content when needed.

## Architecture

### Skill Tiers

**Core** — Always composed into the system prompt (unchanged from today):

| Agent | Core Skills |
|-------|-------------|
| ModelExplorer | `explorer/identity`, `core/tool-first-rule`, `core/zero-knowledge`, `explorer/exhaustiveness-mandate`, `explorer/fact-rules` |
| OrchestratorRouter | `orchestrator/identity`, `orchestrator/delegation-rules`, `orchestrator/chained-delegation`, `orchestrator/work-order-discipline` |
| TLScriptAgent | `tlscript/identity`, `tlscript/tool-first-rule`, `tlscript/zero-knowledge`, `tlscript/verification-rules` |
| ModelEditor | `editor/identity`, `editor/creation-rules` |

**On-demand** — Discoverable via `SkillTool`:

| Skill | Example description (from frontmatter) |
|-------|----------------------------------------|
| `explorer/scope` | Defines what is in/out of scope for model exploration |
| `explorer/exploration-strategy` | Step-by-step strategy for systematic model exploration |
| `explorer/context-resolution` | How to resolve ambiguous type/module references |
| `explorer/output-format` | Output formatting rules for exploration results |
| `explorer/examples` | Example exploration interactions |
| `orchestrator/behavior` | Orchestrator behavioral guidelines and conversation style |
| `tlscript/grammar-rules` | TLScript grammar reference and syntax rules |
| `tlscript/response-rules` | How to format TLScript responses |
| `editor/output-format` | Output formatting rules for model creation results |

### YAML Frontmatter in Skill Files

Each `.md` skill file gets a YAML frontmatter header. This is the single source of truth for skill metadata:

```markdown
---
description: Step-by-step strategy for systematic model exploration
tier: on-demand
---
# Exploration Strategy
...actual skill content...
```

For core skills:

```markdown
---
description: Agent identity and role definition
tier: core
---
# You are the Model Explorer
...
```

`listAvailableSkills()` returns data parsed directly from these frontmatter blocks. No hardcoded catalog.

### SkillTool

A `@Tool`-annotated class added to every agent:

```java
public class SkillTool {

    @Tool("Lists all on-demand skills available. Returns skill names with descriptions. Call to discover specialized knowledge you can load for the current task.")
    public String listAvailableSkills() {
        // Iterates on-demand skills registered for this agent
        // Returns: "- explorer/scope: Defines what is in/out of scope...\n- explorer/strategy: ..."
        // Descriptions come from parsed YAML frontmatter
    }

    @Tool("Loads a skill by name, returning its full content. Call after listAvailableSkills to load instructions you need.")
    public String loadSkill(@P("Skill name from the catalog") String skillName) {
        // Returns the full .md content (without frontmatter)
    }
}
```

The `SkillTool` is scoped per agent — it only lists on-demand skills registered for that agent's class.

### System Prompt Composition

System prompt = core skills only (composed by `SkillRegistry.composeSystemMessage()` as today). No catalog appendix needed — the AI discovers on-demand skills by calling `listAvailableSkills()`.

### SkillRegistry Changes

- **Parse YAML frontmatter** from `.md` files to extract `description` and `tier`
- **New method:** `getOnDemandSkillsFor(Class<?>)` — returns on-demand skills for an agent
- **Existing method:** `composeSystemMessage(Class<?>)` — now only uses core-tier skills
- **Skill record:** `description` field now populated from frontmatter (currently duplicates `name`)

### SkillConfig Changes

- `registerAgentSkills` now registers both core and on-demand skills
- New: `registerOnDemandSkills(Class<?>, String...)` or a single registration with tier info

### Agent Building Integration

```java
// In AgentInvoker.buildModelExplorer():
String systemMessage = SkillConfig.getInstance().composeSystemMessage(ModelExplorer.class);
SkillTool skillTool = new SkillTool(SkillConfig.getInstance(), ModelExplorer.class);

return AgenticServices.agentBuilder(ModelExplorer.class)
    .chatModel(explorationModel)
    .systemMessageProvider(memoryId -> systemMessage)
    .tools(browserTool, instanceTool, ..., skillTool)  // skillTool added
    .build();
```

## Data Flow

```
User message -> Agent built with:
  1. System prompt = core skills only (identity, hard rules)
  2. Tools = domain tools + SkillTool(agent-scoped)

Agent processes message -> Decides it needs specialized knowledge
  -> Calls listAvailableSkills()
  -> Sees: "explorer/exploration-strategy - Step-by-step strategy for..."
  -> Calls loadSkill("explorer/exploration-strategy")
  -> Gets full skill content as tool result
  -> Follows the loaded instructions for the current task
```

## Key Decisions

- **Core vs on-demand classification:** Hard rules and identity are core; reference material, examples, and formatting guides are on-demand.
- **YAML frontmatter as source of truth:** Skill metadata lives in the `.md` file itself, not in Java config. `listAvailableSkills()` reads from parsed frontmatter.
- **Agent-scoped catalog:** Each agent only sees on-demand skills relevant to its domain.
- **Classpath storage:** Skills remain as `.md` files on the classpath, versioned with code.
