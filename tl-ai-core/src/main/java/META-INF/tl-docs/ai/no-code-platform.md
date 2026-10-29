---
description: Read to understand why agents on a model-based no-code platform configure rather than generate code, what that gains (smaller error space, immediate effect, guaranteed consistency, fewer hallucinations, traceability) and where it ends (the platform ceiling, missing prior knowledge, limits of the configuration language).
order: 40
---

# AI on a no-code platform

## Configuring instead of generating code

Where AI is used in software development today, it mostly generates source code: Java, TypeScript, SQL, HTML, CSS. That works astonishingly well - but generated code has to be compiled, tested and deployed before it runs.

On a model-based platform like TopLogic the approach is different in kind: the agent does not write code, it configures. It describes *what* it wants, not *how* that is implemented technically; the platform takes care of the how. The difference is subtle but important. The agent does not replace the developer by doing the same job (writing code) - the job itself disappears, because no code is needed.

## What it gains

**A smaller error space.** A configuration is valid or not, and the schema says so at once. Generated code can be syntactically correct and still wrong: security holes, timing problems, wrong data types. The platform rules out whole classes of errors that generated code can have.

**Immediate effect.** Changes to configuration and model take effect immediately. Generated code first has to be compiled, tested and put into operation - which takes time and is a source of errors of its own.

**Guaranteed consistency.** The platform keeps data model, persistence and user interface in step. In conventional development the agent would have to keep database schema, interfaces, user interface and tests consistent - a task that experienced developers fail at regularly.

**Fewer hallucinations.** The agent works with concrete schemas and gets structured answers. It does not invent interfaces, it uses the existing ones. Generating code, a model regularly invents functions and libraries that do not exist.

**Traceability.** Configuration changes are declarative: one sees what is set, not how it was implemented. Generated code is harder to review - was it correct before the change, and what exactly changed? With configuration that is obvious.

**Domain experts can judge it.** A domain expert reads a model description and says "yes, that is right" or "no, something is missing". Generated program code they cannot judge. The platform brings the agent closer to the language of the domain.

**Fewer decisions, fewer errors.** Instead of generating a thousand lines of code, the agent makes ten configuration decisions. The error rate per decision may be similar - but ten decisions instead of a thousand add up to fewer errors.

## Where it ends

**The platform ceiling.** What the platform cannot do, the agent cannot configure. Conventional development does not have this limit in principle. Very specific user interactions or integrations with other systems that the platform does not provide still need classic development - a Java tool, a view element, a service.

**Platform knowledge is needed.** The agent has to know the concepts and peculiarities of the platform. For general programming, knowledge of the common languages and frameworks suffices, and it is massively present in the training data. Platform-specific knowledge is rare - the tools, the configuration schemas, the skills and the [semantic index](doc:ai/semantic-memory) make up for it.

**Less prior knowledge.** For common languages and frameworks there are millions of examples in the training data; for configuring one specific platform there are practically none. The agent relies on tools, schemas and the index instead of learned knowledge, which makes the quality of these building blocks all the more important.

**Limits of the configuration language.** Some requirements are expressed more naturally in program code than in configuration. "Sort the list by a complex score of three weighted factors" is trivial in code and possibly awkward in configuration. TL-Script covers much of this, but not everything.
