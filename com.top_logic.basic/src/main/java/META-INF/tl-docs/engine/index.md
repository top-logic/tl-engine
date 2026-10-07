---
description: Read when working on the TopLogic engine itself - creating engine modules, build/CI conformance, JavaDoc doclet warnings, msgbuf, the demo apps.
order: 90
---

# Extending the engine

The articles of this chapter concern the development of the engine repository itself rather than applications built on it. They describe what a new engine module needs, which CI gates a local build does not reveal, how to fix the warnings of the TLDoclet, how msgbuf message classes are generated, and how to run the demo applications for a manual check.

- [New module checklist](doc:engine/new-module-checklist) - the registration files a new module needs beyond its `pom.xml`, and the symptoms of a missing one.
- [New React control module](doc:engine/new-react-module) - the files, build and wiring of a module of its own for React controls.
- [Build conformance & CI gates](doc:engine/build-conformance) - the checks a local `mvn install` skips: class comments, normalized layouts, test compilation.
- [Fixing TLDoclet JavaDoc build warnings](doc:engine/javadoc-warnings) - the house rules for the doclet warnings that make the Jenkins build unstable.
- [Demo apps](doc:engine/demo-apps) - URLs and login of `tl-demo` and `tl-demo-react`, and notes for scripted tests.
- [msgbuf library](doc:engine/msgbuf) - the writer-type pitfall of `JsonWriter` and running the generator plugin.
