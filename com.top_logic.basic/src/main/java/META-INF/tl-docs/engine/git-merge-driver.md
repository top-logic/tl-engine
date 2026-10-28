---
description: Read when setting up a clone or the Gitea server of the engine repository, or when a merge conflicts in META-INF/messages_*.properties or WEB-INF/conf/resources/*.properties - the merge driver tl-properties (tools/git/merge-properties.sh) that merges these resource bundles key by key, its registration with git config, and its installation for Gitea.
order: 40
---

# Merging resource bundles

The resource bundles of the engine (`META-INF/messages_*.properties`, `WEB-INF/conf/resources/*.properties`) are files of `key = value` lines, one line per key, sorted by key. Most branches add keys to the same bundles, so two branches that add different keys next to each other conflict in a text merge although their changes are independent.

The merge driver `tl-properties` (`tools/git/merge-properties.sh`) merges these files key by key:

- A key that only one side added, changed or removed takes that side's line.
- A key that both sides changed to the same line takes that line.
- A key that the two sides changed to different lines is a real conflict.

The result is sorted by key in byte order, the order of the generated files. On a real conflict the driver falls back to the text merge of `git merge-file`, which writes the usual conflict markers into the file. It falls back as well for a file that has a line without `=` or keys out of order. A conflict of the generated `messages_en.properties` is resolved by taking either side and running `mvn install` on the module, which regenerates the file.

`.gitattributes` assigns the driver to the files (`merge=tl-properties`). Git runs a driver only once it is registered in the Git configuration; without the registration, Git merges these files as text, as before.

## Registering the driver in a clone

Once per clone, in its top-level directory. The configuration of a clone holds for all its worktrees:

```bash
git config merge.tl-properties.name "TopLogic resource bundle merge"
git config merge.tl-properties.driver 'f=tools/git/merge-properties.sh; if test -f $f; then sh $f %O %A %B %P; else git merge-file %A %O %B; fi'
```

Git runs the driver in the top-level directory of the worktree, so the relative path finds the script of the checked-out branch. In a checkout of a branch that does not contain the script yet, the command falls back to the text merge. Without that fallback, the missing script would make Git report a conflict with the file left at the own side and without conflict markers. Every merge then uses the driver: `git merge`, `git pull`, `git rebase`, `git cherry-pick` and `git merge-tree`. On Windows, Git for Windows provides `sh`, `awk` and `sort`.

To check the registration, run `git config --get merge.tl-properties.driver`. With the driver registered, `git merge-tree --write-tree --name-only origin/master <branch>` lists only real conflicts, and no longer the bundles that merely received keys on both sides.

## Installing the driver for Gitea

Gitea merges a pull request with Git in a temporary repository and reads the merge drivers from its own Git configuration. Installing the driver there lets the merge button merge a pull request whose only conflicts are in the bundles:

1. Copy `tools/git/merge-properties.sh` to a path outside the repositories that the Gitea user can read. With the Docker image, that is a path in the data volume, for example `/data/gitea/merge-properties.sh`. Gitea runs the driver with its own `sh`, `awk`, `sort` and `git`; the Alpine-based image provides them through BusyBox.
2. Register the driver in the section `[git.config]` of `app.ini`. Gitea writes these entries into the Git configuration that it uses for all its Git commands:

   ```ini
   [git.config]
   merge.tl-properties.name = TopLogic resource bundle merge
   merge.tl-properties.driver = sh /data/gitea/merge-properties.sh %O %A %B %P
   ```

3. Restart Gitea.
4. Check that the driver is in place:
   - Open two test pull requests that add different keys at the same place of one bundle.
   - Merge the first.
   - The second must still be shown as mergeable, and must merge without a conflict.

   Gitea checks whether a pull request is mergeable separately from the merge itself. If its check reports the second pull request as conflicting, that check does not run the configured drivers. Then merge the branch's master locally (with the registered driver) and push it, so that the check sees no conflict.

When the script changes, copy the new version to the server again.
