# Agent Skills System Design

**Date:** 2026-02-18
**Status:** Approved
**Branch:** Memory (builds on conversation memory work)

## Problem

Agent system prompts are monolithic strings (up to 467 lines) embedded in `@SystemMessage` Java annotations. This makes them:
- Hard to read and edit
- Impossible to reuse across agents (ModelExplorer and TLScriptAgent share "tool-first" and "zero-knowledge" rules but maintain separate copies)
- Untestable in isolation
- Rigid (no path to dynamic activation)

## Solution: Composable Skill Fragments

Decompose monolithic system prompts into **skills** — named, reusable prompt fragments stored as classpath resource files. A `SkillRegistry` maps skills to agents and composes them into system prompts via LangChain4j's `systemMessageProvider`.

## Core Abstractions

### Skill Record

```java
public record Skill(
    String name,        // e.g. "zero-knowledge"
    String description, // e.g. "Enforces tool-first behavior"
    String content      // Prompt text loaded from resource file
) {}
```

### SkillRegistry

```java
public class SkillRegistry {
    private final Map<String, Skill> skills;              // name -> Skill
    private final Map<Class<?>, List<String>> agentSkills; // agent -> ordered skill names

    public List<Skill> getSkillsFor(Class<?> agentClass) { ... }
    public String composeSystemMessage(Class<?> agentClass) { ... }
}
```

### Resource File Layout

```
src/main/resources/com/top_logic/ai/service/skills/
  core/
    tool-first-rule.md          -- shared by ModelExplorer, TLScriptAgent
    zero-knowledge.md           -- shared by ModelExplorer, TLScriptAgent
  explorer/
    identity.md                 -- "You are a TopLogic Model Explorer agent..."
    exploration-strategy.md     -- exploration approach and completion checks
    output-format.md            -- evidence/facts/next-query format
    forbidden-patterns.md       -- explicitly forbidden outputs
  orchestrator/
    identity.md                 -- "You are the Orchestrator Router..."
    delegation-rules.md         -- mandatory delegation, no own answers
    legacy-prefixes.md          -- /explore-model, /tlscript handling
  tlscript/
    identity.md                 -- "You are the TLScript specialist..."
    verification-rules.md       -- mandatory function verification
    grammar-rules.md            -- TLScript syntax rules
  editor/
    identity.md                 -- "You are the Model Editor..."
    creation-order.md           -- modules -> enums -> classes -> props -> refs
    naming-conventions.md       -- camelCase/PascalCase rules
```

## Integration Pattern

### Before (current)

```java
// Agent interface has @SystemMessage with 467 lines
@SystemMessage("""...""")
@Agent("Model Explorer")
String explore(@UserMessage String q);

// AgentInvoker builds without systemMessageProvider
AgenticServices.agentBuilder(ModelExplorer.class)
    .chatModel(model)
    .tools(...)
    .build();
```

### After (with skills)

```java
// Agent interface: @SystemMessage removed
@Agent("Model Explorer")
public interface ModelExplorer {
    String explore(@UserMessage String q);
}

// AgentInvoker uses systemMessageProvider
private static final SkillRegistry SKILLS = new SkillRegistry();

AgenticServices.agentBuilder(ModelExplorer.class)
    .chatModel(model)
    .systemMessageProvider(memoryId -> SKILLS.composeSystemMessage(ModelExplorer.class))
    .tools(...)
    .build();
```

## Skill Composition

Skills compose by concatenation in list order, separated by `\n\n---\n\n`. The composed result is identical to the original monolithic prompt.

### ModelExplorer skill list (composition order)

1. `explorer/identity` -- agent role and purpose
2. `core/tool-first-rule` -- shared: HARD GATE
3. `core/zero-knowledge` -- shared: no prior knowledge
4. `explorer/exploration-strategy` -- plan/acquire/verify cycle
5. `explorer/forbidden-patterns` -- what NOT to do
6. `explorer/output-format` -- evidence/facts/next-query

### TLScriptAgent skill list

1. `tlscript/identity` -- agent role
2. `core/tool-first-rule` -- shared
3. `core/zero-knowledge` -- shared
4. `tlscript/verification-rules` -- mandatory function verification
5. `tlscript/grammar-rules` -- TLScript syntax

### Agent-to-Skill Mapping (Java)

```java
Map.of(
    ModelExplorer.class, List.of(
        "explorer/identity", "core/tool-first-rule", "core/zero-knowledge",
        "explorer/exploration-strategy", "explorer/forbidden-patterns",
        "explorer/output-format"),
    TLScriptAgent.class, List.of(
        "tlscript/identity", "core/tool-first-rule", "core/zero-knowledge",
        "tlscript/verification-rules", "tlscript/grammar-rules"),
    OrchestratorRouter.class, List.of(
        "orchestrator/identity", "orchestrator/delegation-rules",
        "orchestrator/legacy-prefixes"),
    ModelEditor.class, List.of(
        "editor/identity", "editor/creation-order", "editor/naming-conventions")
)
```

## Testing Strategy

### Skill loading test
Verify every referenced skill resource file exists and is non-empty.

### Composition test
Verify composed prompt for each agent contains expected section markers.

### Regression test (migration)
Verify composed prompt matches original `@SystemMessage` content exactly (modulo whitespace normalization).

## Migration Path

### Phase 1: Extract (no behavior change)
1. Create `SkillRegistry` and resource file structure
2. Extract each agent's `@SystemMessage` into skill `.md` files
3. Wire `systemMessageProvider` in `AgentInvoker` and `OrchestratorAgent`
4. Remove `@SystemMessage` from agent interfaces
5. Regression test: composed prompt matches original

### Phase 2: Reuse (first benefit)
1. Identify shared sections across agents
2. Extract into `core/` skills
3. Both ModelExplorer and TLScriptAgent reference same files
4. Changes to shared skills auto-propagate

### Phase 3 (future): Dynamic activation
1. Extend `AgentWorkOrder` with optional skill overrides
2. Orchestrator specifies "activate skill X" when delegating
3. `systemMessageProvider` reads active skills from context

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Activation model | Always-on per agent (v1) | Simplest; dynamic activation is v2 |
| Storage | Java + resource files | Prompt text in .md, metadata in Java |
| Composition | Concatenation in list order | Simple, predictable, same result as original |
| No preamble/postamble | First skill = identity/preamble | YAGNI; skill model handles ordering |
| LangChain4j mechanism | `systemMessageProvider` | Native, designed for dynamic prompts |
