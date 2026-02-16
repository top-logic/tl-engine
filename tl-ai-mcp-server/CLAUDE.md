# Claude Code Guidelines for TL AI MCP Server

## Commit Message Convention

**IMPORTANT**: Do NOT add the Claude Code signature or Co-Authored-By lines to commits.

Instead, add relevant user prompts as the last section of the commit message.

### Format

```
<Title: Brief summary of the change>

<Body: Detailed description of what was changed and why>
- Bullet points for key changes
- Technical details and approach

<Optional: Additional context about the implementation>

User prompts:
- "<First user instruction or guidance>"
- "<Second user instruction or guidance>"
- "<Any corrections or clarifications provided>"
```

### Example

```
Implement real type usage discovery in TypeUsagesResource

Replaced stub implementation with meta-model based type usage discovery:
- Use TLReference.getReferers() to find properties using a type as value type
- Use TLClass.getSpecializations() to find subclasses inheriting from a type
- Use Java 17 pattern matching for instanceof to simplify code

The implementation leverages TopLogic's reflexive meta-model where types
themselves are instances. To find property usages, we locate the
tl.model:TLStructuredTypePart meta-type, get its 'type' reference, and
use getReferers() to find all property instances pointing to the target type.

User prompts:
- "OK, to actually find the usages of a type, you have to use the TopLogic
  meta-model..."
- "sorry, the method is called getReferers()"
- "Use Java 17 style instanceof with variable declaration to avoid explicit casts."
```

## Code Style

- Use Java 17 features including pattern matching for instanceof
- Avoid unnecessary casts when the type system can infer types
- Use TLObject instead of Wrapper when sufficient
- Package-private methods (no modifier) for utilities that need testing

## Testing

- Test production code directly rather than duplicating logic in tests
- Make utility methods package-private to enable testing without exposing public API
