---
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git commit:*), Bash(git diff:*), Bash(git log:*)
description: Create a git commit with project-specific conventions
---

Create a git commit following these project-specific conventions:

## Commit Message Format

```
Short summary line (imperative mood, max 50 chars)

Detailed explanation of what changed and why (wrap at 72 chars).
Can span multiple paragraphs if needed.

- Bullet points for key changes
- Another change

User prompts:
> Original user request that led to this change
> Additional user clarification or feedback
```

## Rules

- **DO NOT** add Claude signature lines like "Generated with Claude Code" or "Co-Authored-By: Claude"
- **DO** include the user prompts that produced the commit as the last section
- Use imperative mood in the summary ("Add feature" not "Added feature")
- Separate summary from body with a blank line
- Wrap body text at 72 characters
- Use bullet points for listing multiple changes

## Steps

1. Run `git status` and `git diff` to understand the changes
2. Run `git log --oneline -5` to see recent commit style
3. Stage relevant files (prefer specific files over `git add -A`)
4. Compose the commit message with the format above
5. Create the commit

## Example

```
Add module description to ModelModulesResource

Enhance the model modules resource to include module descriptions by
retrieving them from TLI18NKey annotations. This provides more context
for AI agents browsing the TopLogic model structure.

- Retrieve TLI18NKey annotation from each module
- Resolve the resource key to get the localized description
- Include description field in JSON output when available

User prompts:
> A TLModule actually has a description. Unfortunately this is not
> available as property or getter. In the UI it is possible to enter
> a description when creating a module. Use this description as
> additional information in the modules resource.
```
