# RECOVERY: BSc DAS-on-Kademlia simulator (Lancaster SCC.300, 2025)

> **About this copy.** This document was written in the author's local recovery workspace. For publication, private mailbox details were removed and a few sentences were corrected to match this repository. The folders it mentions (`fragments/`, `decompiled/`, `upstream/`, `analysis/`, `logs/`) and the verification scripts exist only in that workspace. Two stray IDE zip files (`EDProtocol.java.zip`, `EDSimulator.java.zip`) and the build output are not part of this repository.

**TL;DR.** No original copy survives: none in mail, Trash, snapshots or cloud folders. The project was rebuilt in `simulator/` (branch `das-sampling` of this repository) from three sources:

- the upstream simulator at its exact base commit;
- Damir's VS Code Local History (3 original files, with comments);
- the VS Code Java build of 2025-10-04 (148 classes), decompiled for the 7 files he changed and no history exists for.

Bytecode proves which files he changed: exactly 8 of 143. The rebuilt tree compiles back to his build:

- 140/148 classes are byte-identical.
- In the 8 rebuilt classes, 132/139 methods have identical bytecode. The other 7 methods differ only in how local variables are laid out.

It builds offline with Maven and runs.

The source fragments were only copied, never changed.

## 1. Where the original was looked for (step 0)
| Place | Result |
|---|---|
| Mail clients (Outlook for Mac, Apple Mail) | No project attachment. |
| Spotlight, Trash, cloud-synced folders, APFS local snapshots | Nothing (only OS-update snapshots; no Time Machine). |
| iCloud Drive `Downloads/` | Reference papers only (DAS papers, the PeerSim manual, the Kademlia paper), not his work. Not copied. |
| Damir's GitHub account (37 repos at the time) | Not there. |
| Photos library | Screenshots of the code exist there; they were not used. They are the only remaining source of the original comments and formatting of the 7 decompiled files. |

## 2. Fragments used (copies in `fragments/`, originals untouched)
| Fragment (original location) | What it gave |
|---|---|
| `~/Library/Application Support/Code/User/workspaceStorage/6fdd9fe8ce45869d59478af02c776bf3/redhat.java/ss_ws/simulator_85048d5d/bin` | 148 `.class` files, Java 21 (major 65), built 2025-10-04 by VS Code's Java server. Also `peersim/tags` and 2 stray zips (`edsim/EDSimulator.java.zip`, `EDProtocol.java.zip`, dated 2022-11-12). |
| same workspaceStorage folder: `workspace.json`, `ss_ws/.metadata/.../simulator_85048d5d/.classpath`, `jdt_ws` Maven state, `state.vscdb` | Folder = `~/Downloads/KADEMLIAWORKING 4/simulator`. The build classpath was **JRE + `src/main/java` + `src/test/java` only, with no jars**. Maven state: `peersim:service-discovery:1.0-SNAPSHOT`, compiler level 1.8, `fmt-maven-plugin 2.18`, 66 resolved `~/.m2` jars. The editor state (last saved 2025-11-30) records `KademliaProtocol.java` = 1145 lines and `CustomDistribution.java` = 56 lines (upstream 55 + 1, i.e. unchanged). |
| VS Code Local History `~/Library/Application Support/Code/User/History/` | Found by scanning all 42 `entries.json` files; 3 files belong to the project: <br>- `-33cdc692`: `TrafficGeneratorPutGet.java`, 2 versions; latest `RTIf.java`, 2025-08-02 15:38Z. <br>- `-3608fddf`: `config/kademlia_putget.cfg`, 9 versions; latest `kaPk.cfg`, 2025-08-11 13:16Z. <br>- `5dd35c18`: `run.sh`, 1 version (`ADTp.sh`). <br>The manifest is `history-manifest.tsv`. |
| `~/.m2/repository` | All 66 dependency jars are present. `grpc` in `~/.m2` dates from Dec 2025 and is not from this project. |

## 3. Upstream (step a)
- **Repo:** https://github.com/datahop/kademlia-simulator (Sergi Rene / datahop). Lineage: PeerSim, then the Kademlia protocol by Daniele Furlan
  and Maurizio Bonani, then `harnen/p2p-service-discovery`, then this repo.
- **Base commit:** `2ef9f79279af`, "put/get implementation done", tip of branch `put_get`, 2022-11-11. Its tree `f351b5b` is identical to `main@3f62a7d`
  (PR #23, 2023-01-26) and `eac4d02` (2023-02-08). Damir's `KBucket` has none of the 2023-02-08 replacement-bucket changes, and his `peersim/tags` matches
  this tree byte for byte. The zips dated 2022-11-12, which no upstream commit or fork contains, suggest he received a folder snapshot made the day after
  `2ef9f79` rather than cloning. Another public repository (a 2024-25 Lancaster project) starts from the same snapshot; it was checked and ruled out as a base.
- **Licence:** the repo has **no LICENSE file**. The 121 vendored PeerSim files carry "Copyright (c) 2003-2005 The BISON Project", **LGPL v2** headers.
  The `kademlia/` files have only `@author Daniele Furlan, Maurizio Bonani` and no licence. The README credits both. The non-PeerSim code is therefore "all rights reserved" by default. This fork adds no licence; the upstream authors keep their rights in their code.

## 4. How the changed files were identified (steps b-c)
1. **Exact compiler found.** The Eclipse compiler bundled with VS Code (`redhat.java-1.54.0/.../org.eclipse.jdt.core.compiler.batch_3.46.0.jar`) was run with
   `-21 -g -preserveAllLocals` on the **JRE-only classpath the IDE really used**. It reproduces Damir's class files byte for byte.
2. **Upstream `2ef9f79` compiled that way: 139/148 classes byte-identical to his build.** That includes 4 classes that hold `Unresolved compilation
   problem` stubs in his build: `ConfigContainer`, `Operators` and `StringListParser` (missing JEP jar) and `FindOperation.visualize()` (missing graphstream).
   The stubs come from the IDE's missing jars, not from his source.
3. **Damir changed exactly 8 source files**, all in `peersim/kademlia`:
   - `KademliaCommonConfig`, `KademliaObserver`, `KademliaProtocol`, `Message`, `Timeout`, `TrafficGeneratorPutGet` and `Util`;
   - `operations/GetOperation`.

   He added no new classes.
4. `TrafficGeneratorPutGet.java` is the VS Code history file `RTIf.java`, **verbatim**. Compiled, it is byte-identical to his 2025-10-04 class, so it is
   the exact original with comments.
5. The other 7 were decompiled with **CFR 0.152** (`decompiled/` holds the raw output), then fixed by hand. Each fix is marked `// recovery:` in the source.
   - **3 decompiler slips** that blocked compilation: an `Object`-typed String in `Util.put0`, a merged local in `KademliaProtocol.handleResponse`, and
     an un-sugared try-with-resources in `KademliaObserver.writeMap`.
   - **4 idioms restored**, each proven by the original bytecode:
     - field-initialiser order in `Message` and `GetOperation`;
     - `Map` declared type in `Message.toMap`;
     - nested `if` in `KademliaObserver.writeOut`;
     - `x.nrHops++`.

   Result:
   - **132 of 139 methods compile back to identical bytecode.** The other 7 are `KademliaProtocol`'s constructor, `nodeIdtoNode`, `handleResponse`,
     `handleInit`, `chunkFileData` and `processEvent`, plus `KademliaObserver.execute`.
   - Those 7 differ only in local-variable slot order and block layout. Decompiling them again gives the same code, apart from where variables are
     declared and iterator versus for-each loops. Scripts: `analysis/scripts/verify_build.py`, `equiv_check.py`.
6. **`pom.xml`** is upstream, unchanged. `mvn dependency:build-classpath` resolves exactly the IDE's 66 `~/.m2` jars, plus the 2 system-scope jars in `lib/`.
   The coordinates, compiler 1.8 and fmt plugin also match the IDE's Maven state.

Provenance counts for `simulator/` in this repository: ORIGINAL 3, DECOMPILED 7, UPSTREAM-VERIFIED 136 (135 `.java` + `tags`), UPSTREAM 132 (non-Java upstream files with
no bytecode evidence: 123 committed upstream `.class` files, `lib/*.jar`, `config/kademlia.cfg`, `pom.xml`, `dependency-reduced-pom.xml`), ADDED 2. The full per-file list is in the Appendix.
Branch `das-sampling` is two commits on top of upstream `2ef9f79`:

- Damir's recovered files (the 8 Java files, `config/kademlia_putget.cfg`, `run.sh`). The 7 decompiled files were converted to CRLF line endings to match upstream's files.
- the README, these docs, `.gitignore` and the SIZE-500 test config.

Run `git diff 2ef9f79 das-sampling` to see everything Damir changed.

## 5. What Damir added (diff vs upstream)
Detail: the README section "What Damir added" (the full analysis stays in the recovery workspace). The comparison is CFR of upstream against CFR of his build, so decompiler noise cancels.

| File | Change (lines +/-) | What |
|---|---|---|
| KademliaProtocol | +366/-31 | Chunked PUT/GET, PUT ACKs, ping/pong liveness, timeout handling. |
| KademliaObserver | +42/-11 | GET/PUT/FIND timing stores, `reset()`, new stats line, CSV `writeMap` bug fixed. |
| TrafficGeneratorPutGet | +59/-43 (source; mostly comments) | Payload `"hello"` changed to 26,214,400 random hex chars (25 MiB); GET delay 9999 ms. |
| Util | +12 | `SHA256(byte[])`. |
| GetOperation | +10/-1 | `metadataHandled` flag. |
| Message | +3 | `MSG_GET_RESPONSE`, `MSG_ACK`, `MSG_PONG`. |
| Timeout | +3/-1 | `isEmptyPing`. |
| KademliaCommonConfig | +3/-3 | Defaults: K 16 to 36, MAXREPLACEMENT 10 to 20, FINDMODE 1 to 0. |

How it works:

- **PUT:**
  - The 25 MiB value is cut into 25,600 chunks of 1 KiB.
  - Each chunk key is SHA-256(offset + chunk).
  - A metadata list of all chunk keys is stored under SHA-256("t1").
  - Each chunk goes to the K=36 closest nodes, and PUT time is counted when all ACKs arrive.
- **GET:**
  - The client fetches the metadata, shuffles it and takes `subList(0, 1023)` (about 4%).
  - It looks up each of those chunks and retries replica after replica.
  - The GET time is recorded once 1023 chunks have arrived.
- **Liveness:** an unanswered request triggers a ping, and a peer that does not PONG is evicted from the routing table. This implements a timeout handler
  that upstream left commented out.

Only configured, not written by him:

- **Malicious nodes** (`isEvil` / `attackerID`): upstream code, never called and not enabled in any cfg.
- **Churn** (`Turbulence`): enabled through cfg.
- **Transport delays.**

Config history (9 snapshots, 2-11 Aug 2025): he varied only SIZE (5k / 25k / 50k) and the churn mix. K 25, NBUCKETS 256, 10 ms delays and FINDMODE 0
stay fixed in every snapshot.

## 6. Build and run (step d)
- **Build:** `cd simulator && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -o -q package -DskipTests -Dfmt.skip=true`
  passes offline in 13.7 s and produces `target/service-discovery-1.0-SNAPSHOT.jar` (12.3 MB, shaded).
  - `-Dfmt.skip=true` is needed because fmt-maven-plugin 2.18 (google-java-format 1.15) crashes on JDK 21. It would also reformat `src/` in place.
  - There are no tests: `src/test/java` was empty, since no test classes were in the build.
- **Run:** it works. The run used `config/kademlia_putget_small.cfg`, which is Damir's latest cfg with only `SIZE 25000` changed to `500`. The cfg uses
  CRLF line endings. The command was run.sh's classpath with `-Xmx8g -ea peersim.Simulator config/kademlia_putget_small.cfg`.
  - **Resources:** exit 0, 84 s wall time, 3.6 GB peak RSS.
  - **PUT:** 1 PUT of the 25 MiB value took 300 ms of simulated time ("[PUT] Full operation took 300 time units"). That is 25,600 chunks x 36
    replicas; by t=300 s the run had 924,792 FINDs and 2.77 M messages.
  - **GET:** each of the 11 GETs sampled 1023 chunks and finished in 580 ms. In that run all 11 were identical. In a later run on the published branch they took 400-480 ms (median 460), because the payload and the sampled chunks are unseeded (see the README, "Limits").
  - **Observer at t=3.5 M ms:**
    - N=506 nodes up (churn added 12 nodes);
    - HOP AVG 34, GET AVG 580 ms, FIND AVG 21 ms;
    - PUT AVG 0, because the per-GET `reset()` wipes it.
  - **Output file:** `simulator/logs/messages.csv` has 69,223 lines with header `dst,src,id,type,status`. The rows are well-formed, which confirms his
    CSV fix.
  - **Aborted run:** an earlier attempt ran at the full 25,000 nodes by mistake, because the first SIZE edit missed the CRLF line ending. It was aborted.
  - **Run on the published branch:** see the README and `docs/sample-run-500.log` (PUT 300 ms, GET 400-480 ms; 177 s and 3.8 GB peak on a loaded machine).

## 7. Assessment
What is solid:

- The 136 upstream files and his `TrafficGeneratorPutGet` are proven exact.
- His 7 other files are semantically his code: same fields, same methods and, in 132/139 methods, the same bytecode.

What is lost:

- Comments, Javadoc and his formatting in those 7 files. `KademliaProtocol` was 1145 lines and is now 684 in decompiler style.
- Any edit made after the 2025-10-04 build, and any cfg edit after 2025-08-11. The build matches the history file, and the 1145-line count fits its size,
  so little or nothing is likely missing.

On the work itself:

- It is **chunked content-addressed DHT storage plus timed retrieval of a random 4% of chunks by one client**. It is not data-availability sampling
  proper: no erasure coding, no commitments or proofs, one sampler, and no availability or failure metric.
- **The cfg's `K 25` never took effect.** The code reads `protocol.3kademlia.K`, so runs used the code default **K = 36**. The `K 25` line in the cfg is never applied.
- FIND time measures only the last lookup round.
- `KademliaObserver.reset()` on every GET wipes the PUT stats.
- `subList(0,1023)` throws on payloads under 1023 chunks.
- Runs are not reproducible, because `new Random()` and `Collections.shuffle` ignore the seed.

Real upstream bugs he fixed:

- the CSV `writeMap` bug;
- the missing timeout handling.

## 8. Recovery workspace (not part of this repository)
The work was done in a local workspace with these folders; none of them is published here:
- `fragments/`: copies of every source fragment (VS Code history, class files).
- `decompiled/`: raw CFR output of all 148 classes.
- `upstream/`: read-only clones of the upstream repositories.
- `analysis/`: the bytecode proof (`ecj-compare/`), the scripts, `damir-changes.md`, `provenance-appendix.md`.
- `logs/`: build and run logs.

## Appendix: every file in `simulator/` and its provenance
Counts: ADDED 2, DECOMPILED 7, ORIGINAL 3, UPSTREAM 132, UPSTREAM-VERIFIED 136 (total 280)

| File | Provenance | Evidence |
|---|---|---|
| `.gitignore` | ADDED | added by recovery |
| `config/kademlia.cfg` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `config/kademlia_putget.cfg` | ORIGINAL | VS Code Local History -3608fddf/kaPk.cfg (2025-08-11 13:16Z, latest of 9) |
| `config/kademlia_putget_small.cfg` | ADDED | added by recovery: SIZE 500 test copy of kademlia_putget.cfg |
| `dependency-reduced-pom.xml` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `lib/djep-1.0.0.jar` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `lib/gs-core-2.0.jar` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `lib/gs-ui-swing-2.0.jar` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `lib/jep-2.3.0.jar` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `lib/mbox2-1.0.jar` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `lib/pherd-1.0.jar` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `pom.xml` | UPSTREAM | upstream 2ef9f79; coordinates, compiler 1.8, fmt plugin and all 66 resolved ~/.m2 jars match the IDE's Maven state |
| `run.sh` | ORIGINAL | VS Code Local History 5dd35c18/ADTp.sh (2025-08-02 14:34Z) |
| `src/main/java/peersim/Simulator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/Simulator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/CDProtocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/CDProtocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/CDSimulator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/CDSimulator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/CDState.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/CDState.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/DaemonProtocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/DaemonProtocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/FullNextCycle.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/FullNextCycle.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/NextCycle.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/NextCycle.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/cdsim/Shuffle.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/cdsim/Shuffle.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/CheckConfig.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/CheckConfig.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/ClassFinder.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/ClassFinder.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/ConfigContainer.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/ConfigContainer.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/ConfigProperties.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/ConfigProperties.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/Configuration.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/Configuration.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/FastConfig.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/FastConfig.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/IllegalParameterException.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/IllegalParameterException.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/MissingParameterException.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/MissingParameterException.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/NullPrintStream.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/NullPrintStream.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/Operators.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/Operators.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/config/ParsedProperties.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/config/ParsedProperties.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Cleanable.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Cleanable.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/CommonState.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/CommonState.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Control.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Control.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Fallible.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Fallible.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/GeneralNode.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/GeneralNode.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/IdleProtocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/IdleProtocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Linkable.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Linkable.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/MaliciousProtocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/MaliciousProtocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/ModifiableNode.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/ModifiableNode.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Network.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Network.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Node.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Node.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/OracleIdleProtocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/OracleIdleProtocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/OverlayGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/OverlayGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Protocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Protocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/core/Scheduler.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/core/Scheduler.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/DynamicNetwork.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/DynamicNetwork.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/MethodInvoker.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/MethodInvoker.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/NodeInitializer.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/NodeInitializer.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/OscillatingNetwork.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/OscillatingNetwork.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/RandNI.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/RandNI.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/StarNI.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/StarNI.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireByMethod.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireByMethod.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireFromFile.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireFromFile.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireKOut.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireKOut.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireRegRootedTree.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireRegRootedTree.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireRingLattice.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireRingLattice.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireScaleFreeBA.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireScaleFreeBA.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireScaleFreeDM.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireScaleFreeDM.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireStar.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireStar.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/dynamics/WireWS.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/dynamics/WireWS.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/CDScheduler.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/CDScheduler.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/ControlEvent.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/ControlEvent.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/EDProtocol.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/EDProtocol.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/EDSimulator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/EDSimulator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/Heap.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/Heap.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/NextCycleEvent.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/NextCycleEvent.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/PriorityQ$Event.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/PriorityQ.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/PriorityQ.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 2 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/RandNextCycle.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/RandNextCycle.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/edsim/RegRandNextCycle.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/edsim/RegRandNextCycle.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/BitMatrixGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/BitMatrixGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/ConstUndirGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/ConstUndirGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/FastUndirGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/FastUndirGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/Graph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/Graph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/GraphAlgorithms.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/GraphAlgorithms.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/GraphFactory.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/GraphFactory.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/GraphIO.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/GraphIO.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/NeighbourListGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/NeighbourListGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/PrefixSubGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/PrefixSubGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/SubGraphEdges.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/SubGraphEdges.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/graph/UndirectedGraph.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/graph/UndirectedGraph.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/CustomDistribution.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/KBucket.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/KademliaCommonConfig.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/KademliaNode.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/KademliaObserver.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/KademliaProtocol.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/KeyValueStore.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 2 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/Message.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/RoutingTable.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/SimpleEvent.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/StateBuilder.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 2 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/Timeout.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/TrafficGenerator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/TrafficGeneratorPutGet.java` | ORIGINAL | VS Code Local History -33cdc692/RTIf.java (2025-08-02 15:38Z); class byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/Turbulence.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 2 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/UniformRandomGenerator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/Util.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/operations/FindOperation.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/operations/GetOperation.java` | DECOMPILED | CFR 0.152 from Damir's 2025-10-04 build (+ marked '// recovery:' fixes) |
| `src/main/java/peersim/kademlia/operations/Operation.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/kademlia/operations/PutOperation.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/rangesim/ProcessHandler.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/rangesim/ProcessHandler.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/rangesim/ProcessManager.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/rangesim/ProcessManager.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/rangesim/RangeSimulator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/rangesim/RangeSimulator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/rangesim/TaggedOutputStream.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/rangesim/TaggedOutputStream.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/BallExpansion.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/BallExpansion.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/Clustering.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/Clustering.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/ConnectivityObserver.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/ConnectivityObserver.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/DegreeStats.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/DegreeStats.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/GraphObserver.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/GraphObserver.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/GraphPrinter.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/GraphPrinter.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/GraphStats.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/GraphStats.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/MemoryObserver.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/MemoryObserver.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/reports/RandRemoval.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/reports/RandRemoval.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/tags` | UPSTREAM-VERIFIED | upstream; byte-identical to bin/peersim/tags |
| `src/main/java/peersim/transport/E2ENetwork.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/E2ENetwork.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/E2ETransport.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/E2ETransport.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/KingParser.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/KingParser.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/RouterInfo.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/RouterInfo.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/Transport.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/Transport.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/TriangularMatrixParser.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/TriangularMatrixParser.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/UniformRandomTransport.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/UniformRandomTransport.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/UniformRouterAssignment.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/UniformRouterAssignment.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/transport/UnreliableTransport.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/transport/UnreliableTransport.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/ExtendedRandom.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/ExtendedRandom.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/FileNameGenerator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/FileNameGenerator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/IncrementalFreq.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/IncrementalFreq.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/IncrementalStats.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/IncrementalStats.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/IndexIterator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/IndexIterator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/LinearIterator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/LinearIterator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/MedianStats.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/MedianStats.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/MomentStats.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/MomentStats.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/RandPermutation.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/RandPermutation.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/StringListParser.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/StringListParser.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/util/WeightedRandPerm.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/util/WeightedRandPerm.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/Getter.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/Getter.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/GetterSetterFinder.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/GetterSetterFinder.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/InitVectFromFile.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/InitVectFromFile.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/LinearDistribution.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/LinearDistribution.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/Normalizer.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/Normalizer.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/PeakDistribution.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/PeakDistribution.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/Setter.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/Setter.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/SingleValue.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/SingleValue.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/SingleValueComparator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/SingleValueComparator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/SingleValueHolder.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/SingleValueHolder.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/SingleValueObserver.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/SingleValueObserver.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/TestVectors.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/TestVectors.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/UniformDistribution.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/UniformDistribution.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/ValueDumper.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/ValueDumper.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/VectAngle.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/VectAngle.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/VectControl.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/VectControl.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/VectCopy.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/VectCopy.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/VectorComparator.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/VectorComparator.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
| `src/main/java/peersim/vector/VectorObserver.class` | UPSTREAM | upstream 2ef9f79, unchanged (no bytecode evidence for non-Java files) |
| `src/main/java/peersim/vector/VectorObserver.java` | UPSTREAM-VERIFIED | upstream 2ef9f79; 1 class(es) byte-identical to Damir's build |
