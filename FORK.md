# DrivenByMoss — Iuri's fork

Fork of [git-moss/DrivenByMoss](https://github.com/git-moss/DrivenByMoss) with changes only to the
**Akai APC40 / APC40 mkII**. Everything else is identical to the original.

## What changes on the APC40

- **Session View only.** The Play, Drum, Sequencer and Raindrops views do not exist. The APC
  always starts in Session.
- **Shift is a plain modifier.** While holding Shift you get the native functions (solo/mute, clip
  length etc.). Releasing it no longer opens the mode selection view.
- **"Device knobs" option** (Settings → Controllers → APC card → *Device Control*):
  - *Fixed (map with Learn)* (default): the 8 device knobs are fixed Bitwig controls. To map
    them: right-click a parameter → *Learn Controller Assignment* → turn the knob. The mapping
    is stored in the project and shows up in the *Mappings* panel.
  - *Follow selected device*: original behaviour (the knobs follow the remote controls of the
    selected device).
- The scale, note repeat, play and drum sequencer settings were removed from the APC settings.
- **Groups:** Track Select on the selected group opens/closes it (upstream). Track Select on the
  selected track inside a group closes the group and selects it. A notification shows the new
  state.
- **Track Select LEDs:** closed group blinks slowly, open group blinks fast (also when selected),
  selected track is on.

Every change in the code is marked with `[fork]`. This makes resolving conflicts easier.

## Branches

| Branch          | What it is                                                         |
|-----------------|--------------------------------------------------------------------|
| `master`        | Exact mirror of upstream (updated by the sync Action)              |
| `iuri`          | `master` + the fork changes. **The build comes from here.**        |
| `upstream-sync` | Only exists when the automatic merge has conflicts (comes with a PR) |

## Updates from Moss

The **Sync with upstream** Action runs every Monday morning (or manually in *Actions*):

1. updates `master` with upstream;
2. merges upstream into `iuri`. Without conflicts it pushes directly and starts the build;
3. with conflicts it opens a PR `upstream-sync → iuri`. Resolve it locally:

```bash
git checkout iuri
git fetch origin && git merge origin/upstream-sync
# resolve the conflicts (look for [fork]), test, then:
git push origin iuri
```

## Build

- **GitHub Actions** (*Build extension*): on every push to `iuri`. Download
  `DrivenByMoss.bwextension` from the run's artifacts. A tag `iuri-<version>` also creates a
  Release.
- **Local:** `mvn clean install -Dbitwig.extension.directory=target` (Java 21 + Maven). It has to
  be `install`: the pom copies the jar to `DrivenByMoss.bwextension` in that phase.
- **Without Maven Central:** `fork-tools/build-patch.sh` recompiles only the APC packages and
  patches them into an official `.bwextension` of the **same version**.

Installation: copy `DrivenByMoss.bwextension` to `~/Documents/Bitwig Studio/Extensions/`.
