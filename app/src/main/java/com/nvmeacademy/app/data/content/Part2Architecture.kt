package com.nvmeacademy.app.data.content

/** Part 2: Architecture & Transport - controller, queues, command structure, error handling. */
object Part2Architecture {
    val part = PartSeed(
        id = 2,
        order = 2,
        title = "Part 2 · Architecture & Transport",
        subtitle = "Controllers, queues, and how commands travel",
        chapters = listOf(
            ChapterSeed(
                id = 201, partId = 2, order = 1,
                title = "NVM Controller Architecture",
                shortDescription = "I/O, Administrative, and Discovery controllers",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Three controller types, two lifecycle models",
                    bullets = listOf(
                        "A controller is the interface between a host and an NVM subsystem - the thing that actually executes commands.",
                        "Three controller types: I/O controller, Administrative controller, Discovery controller.",
                        "I/O controllers access user data and may also support management commands.",
                        "Administrative controllers manage the subsystem (namespaces, health, virtualization) but never touch I/O data.",
                        "Discovery controllers exist only for NVMe over Fabrics, to help hosts find other NVM subsystems.",
                        "Two controller lifecycle models: static (state persists across resets/re-associations) and dynamic (fresh state every time).",
                        "PCIe controllers must use the static model; Discovery controllers must use the dynamic model.",
                        "Every controller has exactly one Admin Submission/Completion Queue pair, regardless of type."
                    ),
                    notes = """A controller is formally "the interface between a host and an NVM subsystem," distinguished by the Controller Type (CNTRLTYPE) field in the Identify Controller data structure. An I/O controller supports I/O commands that read and write user data, and may also support management-style Admin commands. An Administrative controller supports NVM subsystem management (namespace management, virtualization management, NVMe-MI health polling, resetting/shutting down the subsystem) but does not support I/O Queues or I/O commands, and no namespace can ever be attached to it. A Discovery controller is specific to NVMe over Fabrics: it implements no I/O Queues and exposes no namespaces at all, existing solely to serve a Discovery log page. Separately, the spec defines two controller lifecycle models: in the static model, controller state survives a Controller Level Reset (and for Fabrics, re-association) - memory-based (PCIe) controllers are required to use only this model. In the dynamic model (usable only over Fabrics, mandatory for Discovery controllers), the subsystem allocates a controller on demand with no state carried over. Whatever the type or model, every controller implements exactly one Admin Submission Queue and one Admin Completion Queue.""",
                    source = "NVMe Base Spec 2.3 §3.1"
                ))
            ),
            ChapterSeed(
                id = 202, partId = 2, order = 2,
                title = "NVM Subsystem Entities",
                shortDescription = "Ports, controllers, shared namespaces, multi-path I/O",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Zooming out to the whole subsystem",
                    bullets = listOf(
                        "The NVM subsystem is the outermost container: it holds domains, controllers, ports, and namespaces.",
                        "Ports are the physical/protocol connection points; controllers connect to hosts through ports.",
                        "A namespace can be private (attached to one controller) or shared (attached to multiple controllers).",
                        "Namespace Management and Namespace Attachment commands create/delete and attach/detach namespaces.",
                        "NVM Sets group namespaces that inherit shared attributes (like optimal write size).",
                        "Multi-path I/O and namespace sharing both require 2+ controllers in the subsystem.",
                        "Concurrent access to a shared namespace from multiple hosts needs external coordination - the spec doesn't define it."
                    ),
                    notes = """The NVM subsystem is the overall container that ties everything together: it can expose one or more ports, one or more controllers reachable through those ports, and a pool of namespaces that controllers can be attached to. A simple single-namespace SSD looks like one port, one controller, one namespace. A more complex subsystem can have several controllers behind several ports, with some namespaces "private" (attached to only one controller) and others "shared" (attached to multiple controllers simultaneously). This is the basis for multi-path I/O and namespace sharing; both require the subsystem to contain at least two controllers, and the spec is explicit that coordinating concurrent access from multiple hosts to a shared namespace is outside its scope. Structurally, each namespace belongs to exactly one NVM Set, and each NVM Set belongs to exactly one Endurance Group. A host discovers and creates these entities with dedicated Admin commands: Namespace Management (create/delete a namespace), Namespace Attachment (attach/detach a namespace to a specific controller), and Capacity Management (create/delete NVM Sets and Endurance Groups).""",
                    source = "NVMe Base Spec 2.3 §3.2, §2.4.1"
                ))
            ),
            ChapterSeed(
                id = 203, partId = 2, order = 3,
                title = "Queueing Model: SQ, CQ, Doorbells",
                shortDescription = "Submission/Completion Queues, doorbells, phase tags",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Everything rides on paired queues",
                    bullets = listOf(
                        "Everything in NVMe is built on paired Submission Queues (SQ) and Completion Queues (CQ).",
                        "Host writes commands into an SQ; controller writes results into the associated CQ.",
                        "SQ/CQ are circular buffers; Tail = next free slot to write, Head = next slot to consume.",
                        "On PCIe, the host signals new commands by writing the SQ Tail Doorbell register.",
                        "On PCIe, one Completion Queue can serve multiple Submission Queues; on Fabrics, it's always strictly 1:1.",
                        "The Phase Tag (P) bit in each CQE flips value each time the CQ wraps, letting the host detect \"is this entry new?\" without polling a register.",
                        "A queue is \"Full\" when Head equals one more than Tail; \"Empty\" when Head equals Tail.",
                        "Every controller always has exactly one Admin SQ/CQ pair; I/O Queues are created/deleted on top of that."
                    ),
                    notes = """NVMe's entire command path rests on one mechanism: a Submission Queue (SQ), where the host places commands, paired with a Completion Queue (CQ), where the controller places results. Both are circular buffers of fixed-size slots (64 bytes per SQE) tracked with Head and Tail pointers; a queue is Empty when Head equals Tail, and Full when Head equals one more than Tail. On PCIe, after writing new commands the host writes the SQ Tail Doorbell register; after consuming completions, it writes the CQ Head Doorbell register. Rather than polling for new completions, the host watches the Phase Tag (P) bit inside each CQE: the controller inverts this bit's expected value every time it posts a new entry, and flips it again on the next full pass, so a host can tell "new" entries apart from stale leftovers purely by comparing against the expected phase. PCIe's memory-based model allows many Submission Queues to feed into one shared Completion Queue, while NVMe over Fabrics enforces a strict 1:1 SQ-to-CQ pairing. A host must create Completion Queues before their associated Submission Queues, and must delete Submission Queues before deleting their Completion Queue.""",
                    source = "NVMe Base Spec 2.3 §2.1, §3.3, §4.2.4"
                ))
            ),
            ChapterSeed(
                id = 204, partId = 2, order = 4,
                title = "Command Processing Flow",
                shortDescription = "Arbitration, fused operations, and ordering guarantees",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "From submission to completion",
                    bullets = listOf(
                        "Commands are submitted only when the controller reports itself ready (CSTS.RDY = 1).",
                        "Except for fused operations, commands have no required ordering - the controller may process them in any order.",
                        "Fused operations glue two adjacent commands into one atomic unit (e.g., compare-and-write style sequences).",
                        "Arbitration decides which Submission Queue gets serviced next: Round Robin (mandatory) or Weighted Round Robin with Urgent (optional).",
                        "Weighted Round Robin has 4 priority tiers: Admin (highest, strict) > Urgent (strict) > High/Medium/Low (weighted round robin).",
                        "The Arbitration Burst setting caps how many commands run from one queue before arbitration happens again.",
                        "Completion order is independent of submission order - a host must never assume commands finish in sequence."
                    ),
                    notes = """A host may only submit commands once the controller's CSTS.RDY bit is set to '1'. The controller then fetches submitted commands and, aside from fused operations, is explicitly permitted to process them "in any order" - if a host cares about ordering, it must enforce that itself. Fused operations are the structured exception: two adjacent commands in the same SQ can be marked via the FUSE field of CDW0 (01b = first, 10b = second) so the controller executes them back-to-back as a single atomic unit. To decide which SQ to service next, every controller must support round robin arbitration, and may optionally add Weighted Round Robin with Urgent Priority Class arbitration - a four-tier scheme (Admin strict-highest, Urgent strict-next, then High/Medium/Low weighted round-robin). The Arbitration Burst setting bounds how many commands the controller will launch from one queue in a row. Completions carry no ordering guarantee relative to submission order, so correlating a specific completion to its command relies on the SQ Identifier + Command Identifier pair carried in the CQE.""",
                    source = "NVMe Base Spec 2.3 §3.4"
                ))
            ),
            ChapterSeed(
                id = 205, partId = 2, order = 5,
                title = "Controller Initialization & Shutdown",
                shortDescription = "CC.EN, CSTS.RDY, and the shutdown state machine",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "The defined handshake",
                    bullets = listOf(
                        "Init (PCIe): wait CSTS.RDY=0 → configure Admin Queue (AQA/ASQ/ACQ) → set CC.EN=1 → wait CSTS.RDY=1 → Identify → create I/O Queues.",
                        "Init (Fabrics): connect via Fabrics Connect command → authenticate if required → configure → CC.EN=1 → wait CSTS.RDY=1 → Identify → Connect I/O Queues.",
                        "Two \"controller ready\" modes: Ready With Media (fully usable the instant RDY=1) vs Ready Independent of Media (media catches up within CRTO.CRWMT).",
                        "Shutdown has two flavors: normal (CC.SHN=01b, drain gracefully) and abrupt (CC.SHN=10b, stop fast).",
                        "CSTS.SHST tracks shutdown progress: 00b not started, 01b in progress, 10b complete.",
                        "NVM Subsystem Shutdown (NSSD) can shut down every controller in a domain/subsystem from one controller.",
                        "A controller is \"safe to power off\" only once CSTS.SHST reads 10b for the relevant scope."
                    ),
                    notes = """Over PCIe, the host waits for CSTS.RDY to read '0', configures the Admin Queue Attributes (AQA) plus Admin SQ/CQ base addresses (ASQ/ACQ), sets controller configuration bits, and only then sets CC.EN to '1' and waits for CSTS.RDY to flip to '1'. It then uses Identify to learn capabilities and creates I/O Completion Queues before I/O Submission Queues. Fabrics initialization starts with a Fabrics Connect command before any property configuration happens. The spec defines two "controller ready" modes: in "Ready With Media," by the moment CSTS.RDY flips to '1' the controller and all namespaces are fully able to process commands; in "Ready Independent of Media," CSTS.RDY can flip to '1' before media is ready - commands may be aborted with "Namespace Not Ready" for up to CRTO.CRWMT while media initialization finishes in the background. Shutdown mirrors initialization in reverse: normal shutdown (CC.SHN=01b) drains outstanding I/O gracefully; abrupt shutdown (CC.SHN=10b) stops immediately. CSTS.SHST progresses from 00b through 01b to 10b (safe to remove power). An NVM Subsystem Shutdown can cascade across every controller in a domain or subsystem from one control point.""",
                    source = "NVMe Base Spec 2.3 §3.5, §3.6"
                ))
            ),
            ChapterSeed(
                id = 206, partId = 2, order = 6,
                title = "Resets & Keep Alive",
                shortDescription = "Controller/subsystem resets and the Keep Alive watchdog",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Recovering from failure and detecting dead connections",
                    bullets = listOf(
                        "Controller Level Reset (CLR) is triggered by: NVM Subsystem Reset, Controller Reset (CC.EN 1→0), Cross-Controller Reset command, or transport-specific resets.",
                        "A CLR stops all outstanding commands, deletes all I/O Queues, and resets nearly all controller properties.",
                        "NVM Subsystem Reset (writing \"NVMe\" to NSSR.NSSRC) resets the entire subsystem/domain.",
                        "Queue Level Reset = delete + recreate a single I/O SQ/CQ pair - a lighter-weight recovery tool.",
                        "Keep Alive is a watchdog: host and controller each track a timer to detect a dead connection.",
                        "Command Based Keep Alive: host periodically sends an explicit Keep Alive command.",
                        "Traffic Based Keep Alive: any Admin/I/O command traffic resets the watchdog - no dedicated command needed.",
                        "Keep Alive Timeout (KATT) expiry sets CSTS.CFS (fatal status) - Fabrics also tears down the association."
                    ),
                    notes = """An NVM Subsystem Reset resets an entire NVM subsystem (or one domain), cascading into a Controller Level Reset on every controller in scope. A Controller Level Reset (CLR) can also be triggered independently: by clearing CC.EN from '1' to '0', by Cross-Controller Reset, or by a transport-specific reset. A CLR stops all outstanding commands, deletes every I/O queue, and resets virtually all controller properties. A Queue Level Reset lets a host delete and recreate just one I/O SQ/CQ pair. Keep Alive is a watchdog timer detecting silent loss of contact. If supported (KAS non-zero), the host configures a Keep Alive Timeout (KATO); the controller reports the operative timeout as KATT. Command Based Keep Alive requires the host to periodically submit an explicit Keep Alive Admin command; Traffic Based Keep Alive (TBKAS) resets the watchdog on any Admin/I/O command traffic. If KATT elapses with no qualifying activity, the controller must log an error, stop processing, and set CSTS.CFS; on Fabrics it also tears down the association entirely.""",
                    source = "NVMe Base Spec 2.3 §3.7, §3.9"
                ))
            ),
            ChapterSeed(
                id = 207, partId = 2, order = 7,
                title = "NVM Capacity Model",
                shortDescription = "Media Units, Channels, and capacity allocation",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "How physical storage is carved up",
                    bullets = listOf(
                        "The capacity model tracks how physical storage is carved up: NVM subsystem → domain → Endurance Group → NVM Set → namespace.",
                        "Media Units are the actual physical storage blocks; each belongs to exactly one Endurance Group (and one NVM Set, if used).",
                        "Channels connect Media Units to the controller; a Channel can serve multiple Media Units and vice versa.",
                        "Capacity Management (an Admin command) allocates domain capacity to Endurance Groups, and Endurance Group capacity to NVM Sets.",
                        "Namespace Management allocates capacity from an NVM Set to a specific namespace.",
                        "Different physical layouts trade bandwidth vs isolation: \"vertical\" isolates NVM Sets per channel; \"horizontal\" mixes SLC-fast and QLC-dense regions.",
                        "Capacity is reported at multiple levels (TNVMCAP/UNVMCAP, Domain List, Endurance Group Information log, NVM Set List), each with total and unallocated fields."
                    ),
                    notes = """At the physical layer, Media Units are the actual pieces of non-volatile storage; each belongs to exactly one Endurance Group (and, where used, exactly one NVM Set), and data moves over Channels, where one Channel can serve several Media Units. Capacity allocation flows top-down via two Admin commands: Capacity Management (allocates a domain's capacity to Endurance Groups, and within an Endurance Group to NVM Sets) and Namespace Management (allocates capacity out of an NVM Set to create a namespace). The spec describes illustrative physical layouts: a "simple" subsystem striping every namespace across all Media Units/Channels for bandwidth; a "vertically organized" subsystem dedicating each Channel to a separate NVM Set for isolation; and a "horizontally organized" subsystem splitting capacity between a fast small NVM Set (e.g., SLC-mode NAND) and a larger denser one (e.g., QLC-mode). Capacity is reported at several levels, each with total/unallocated pairs: Identify Controller's TNVMCAP/UNVMCAP, the Domain List, the Endurance Group Information log page, and the NVM Set List.""",
                    source = "NVMe Base Spec 2.3 §3.8"
                ))
            ),
            ChapterSeed(
                id = 208, partId = 2, order = 8,
                title = "Firmware Update Process",
                shortDescription = "Firmware Image Download and Firmware Commit",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Download, then commit",
                    bullets = listOf(
                        "Two-step core process: Firmware Image Download (send the image, possibly in chunks) then Firmware Commit (verify and activate).",
                        "Firmware Image Download offsets should follow the Firmware Update Granularity from Identify Controller, or the update may fail.",
                        "An image that doesn't start at offset zero, has gaps, or overlaps is considered invalid.",
                        "Firmware Commit can activate the new image several ways: via a later reset, or immediately (Commit Action 011b) with no reset at all.",
                        "Activating without a reset triggers a \"Firmware Activation Starting\" asynchronous event to affected hosts, if enabled.",
                        "If activation fails, specific status codes tell the host exactly which reset type is required.",
                        "If a new firmware image can't load successfully, the controller reverts to the last good image and reports a Firmware Image Load Error event.",
                        "Never interleave two firmware/boot-partition update sequences - behavior in that case is explicitly undefined."
                    ),
                    notes = """Updating firmware is a small state machine built from two Admin commands. First, the host issues one or more Firmware Image Download commands, each carrying a chunk of the new image at a specified byte offset, following the Firmware Update Granularity from Identify Controller; the resulting image must have no gaps, no overlaps, and must start at offset zero. Once fully downloaded, the host issues Firmware Commit, which verifies the image and commits it into a chosen firmware slot. Activation can happen "by reset" (the new image takes effect after a later Controller Level Reset, or in some cases requires a Conventional or NVM Subsystem Reset) or "without reset" (Commit Action 011b activates immediately, emitting a "Firmware Activation Starting" event first if enabled). Failure modes are well defined: an invalid image aborts with "Invalid Firmware Image"; if activation genuinely requires a reset the controller didn't get, it reports exactly which reset type is needed. If a downloaded image fails to load at boot, the controller falls back to the most recently active image and raises a Firmware Image Load Error event. The spec is explicit that a host should never interleave commands from two separate firmware/boot-partition update sequences.""",
                    source = "NVMe Base Spec 2.3 §3.11"
                ))
            ),
            ChapterSeed(
                id = 209, partId = 2, order = 9,
                title = "Submission Queue Entry (SQE) Structure",
                shortDescription = "CDW0, NSID, Data Pointer, and command-specific Dwords",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "64 bytes that describe any command",
                    bullets = listOf(
                        "Every SQE is exactly 64 bytes - fixed size, no matter the command.",
                        "Command Dword 0 (CDW0) is common to every command: Opcode, FUSE, PSDT, and Command Identifier (CID).",
                        "Opcode's low 2 bits are the Data Transfer Direction: 00b none, 01b host→controller, 10b controller→host, 11b bidirectional.",
                        "PSDT (bits 15:14 of CDW0) picks PRPs (00b) vs SGL variants (01b/10b) for the data transfer.",
                        "FUSE (bits 09:08 of CDW0) marks fused-operation membership: 00b normal, 01b first, 10b second.",
                        "Bytes 07:04 hold the Namespace Identifier (NSID) - 0h if unused, FFFFFFFFh is a command-dependent broadcast value.",
                        "Bytes 39:24 hold the Data Pointer - two PRP entries, or one SGL entry, depending on PSDT.",
                        "Command Dwords 10-15 (CDW10-CDW15) are entirely command-specific - their meaning depends on the opcode."
                    ),
                    notes = """Every SQE - Admin, I/O, or Fabrics - is exactly 64 bytes. The first four bytes are Command Dword 0 (CDW0): bits 07:00 are the Opcode (whose low two bits are the Data Transfer Direction); bits 09:08 are FUSE (Fused Operation); bits 15:14 are PSDT, selecting whether the Data Pointer should be interpreted as PRP entries (00b) or an SGL entry; bits 31:16 are the Command Identifier (CID), a host-assigned tag used to match a completion back to its command. Bytes 07:04 hold the Namespace Identifier (NSID), cleared to 0h if unused. Bytes 23:16 are the Metadata Pointer (MPTR). Bytes 39:24 are the Data Pointer (DPTR): if PSDT is 00b, this holds PRP Entry 1 and PRP Entry 2; if PSDT is 01b/10b, the same bytes hold the first SGL segment descriptor. Bytes 43:40 through 63:60 are Command Dwords 10 through 15 (CDW10-CDW15) - six dwords whose meaning is entirely defined by the specific command's opcode. Fabrics commands share the same CDW0 layout conceptually but fix Opcode to 7Fh and add a Fabrics Command Type (FCTYPE) field.""",
                    source = "NVMe Base Spec 2.3 §4.1"
                ))
            ),
            ChapterSeed(
                id = 210, partId = 2, order = 10,
                title = "Completion Queue Entry (CQE) Structure",
                shortDescription = "SQID, SQHD, Phase Tag, and the Status field",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "16 bytes that report the result",
                    bullets = listOf(
                        "Every CQE is at least 16 bytes (4 dwords): DW0, DW1, DW2, DW3.",
                        "DW0 and DW1 are command-specific - their meaning depends on which command is completing.",
                        "DW2 holds SQ Identifier (bits 31:16) and SQ Head Pointer / SQHD (bits 15:00).",
                        "SQ Identifier tells the host which Submission Queue this completion belongs to (useful when queues share a CQ).",
                        "DW3 holds Status (bits 31:17), Phase Tag / P (bit 16), and Command Identifier / CID (bits 15:00).",
                        "Status Field breaks down further into Status Code Type (SCT), Status Code (SC), Do Not Retry (DNR), and More (M).",
                        "Status Code Type 0h = Generic status; 1h = command-specific; 2h = media/data integrity; 3h = path-related."
                    ),
                    notes = """A completion queue entry tells the host everything it needs to know about a finished command, in at least 16 bytes across four dwords. Dword 0 and Dword 1 are command-specific: most leave them reserved, but some (Create I/O Completion Queue, Identify) return meaningful data there. Dword 2 is standardized: bits 31:16 are the SQ Identifier (SQID) and bits 15:00 are the SQ Head Pointer (SQHD), which the host uses to know which SQ slots have been freed. Dword 3 carries the fields developers reach for constantly: bit 31 is Do Not Retry (DNR), bit 30 is More (M, additional detail via the Error Information log page), bits 29:28 are Command Retry Delay (CRD), bits 27:25 are Status Code Type (SCT), bits 24:17 are Status Code (SC), bit 16 is the Phase Tag (P), and bits 15:00 are the Command Identifier (CID). SCT is the "which table do I look this up in" field: 0h Generic Command Status, 1h Command Specific Status, 2h Media and Data Integrity Errors, 3h Path Related Status; 4h-6h reserved, 7h vendor-specific.""",
                    source = "NVMe Base Spec 2.3 §4.2"
                ))
            ),
            ChapterSeed(
                id = 211, partId = 2, order = 11,
                title = "Data Pointers: PRPs and SGLs",
                shortDescription = "Two ways to describe where data lives in memory",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Physical Region Pages vs Scatter Gather Lists",
                    bullets = listOf(
                        "Two competing mechanisms describe \"where in memory is the data\": Physical Region Pages (PRPs) and Scatter Gather Lists (SGLs).",
                        "A PRP entry is just a Page Base Address plus an Offset - simple, fixed-size, page-granular.",
                        "PRP Entry 1 and PRP Entry 2 live inline in the SQE; more entries need a PRP List (a page full of more PRP entries).",
                        "SGLs are more flexible: an SGL segment holds an array of typed descriptors (Data Block, Bit Bucket, Segment, Last Segment, Keyed Data Block, Transport Data Block).",
                        "An SGL \"Segment\" descriptor chains to more descriptors; a \"Last Segment\" descriptor terminates the chain.",
                        "A \"Bit Bucket\" descriptor tells the controller to discard read data the host doesn't actually want.",
                        "PCIe Admin commands must use PRPs (SGLs are forbidden there); NVMe over Fabrics requires SGLs everywhere.",
                        "Data Pointer interpretation is selected by the PSDT field in Command Dword 0: 00b = PRPs, 01b/10b = SGL variants."
                    ),
                    notes = """Physical Region Pages (PRPs) are the simpler, older mechanism: a PRP entry is a 64-bit Page Base Address with a small Offset packed into its low bits. A command carries up to two PRP entries inline; if the transfer spans more pages, PRP Entry 2 instead becomes a pointer to a PRP List, chainable to further PRP List pages. PRPs require every entry after the first to be page-aligned. Scatter Gather Lists (SGLs) are more general and flexible - optional for PCIe I/O commands (but forbidden for PCIe Admin commands) and mandatory for every command over NVMe over Fabrics. An SGL is built from segments of typed descriptors: Data Block (address+length), Bit Bucket (discard unwanted read data), Segment (chain to next segment), Last Segment (terminates the chain), Keyed Data Block (adds a key for fabric memory access), and Transport Data Block (defers to transport-specific semantics). The combined length across a data SGL's descriptors must be at least the command's requested transfer length, or the controller aborts with Data SGL Length Invalid.""",
                    source = "NVMe Base Spec 2.3 §4.3"
                ))
            ),
            ChapterSeed(
                id = 212, partId = 2, order = 12,
                title = "Feature Values, Identifiers & NQN",
                shortDescription = "Get/Set Features, VID/SN/EUI64, and NVMe Qualified Names",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Naming and configuring the pieces",
                    bullets = listOf(
                        "Feature Values are read/set with Get Features / Set Features, organized by numbered Feature Identifiers.",
                        "Each Feature can have up to three values in play: default (fixed), saved (persists if saveable), and current (in effect now).",
                        "Feature scope varies - some are per-controller, some per-namespace, some subsystem/domain/Endurance-Group/NVM-Set-wide.",
                        "Identifiers like VID, SSVID, SN, MN, EUI64, NGUID, and UUID uniquely name vendors, controllers, and namespaces - with different byte orders.",
                        "NQN (NVMe Qualified Name) uniquely names a host or NVM subsystem, UTF-8 encoded, max 223 bytes, null-terminated.",
                        "Two NQN formats: a domain-name-based human-readable form, and a UUID-based form.",
                        "List Data Structures (Controller List, Namespace List) are a count header followed by an array of IDs, zero-filled past the count."
                    ),
                    notes = """Get Features and Set Features read and write settings identified by a numeric Feature Identifier; if the controller supports Save and Select Feature Support (SSFS), each Feature independently tracks a default, saved, and current value. Features also have a defined scope governing how the NSID field must be used. Separately, the spec defines exact byte layouts for identifiers with inconsistent byte orders by design: PCI Vendor ID (VID), PCI Subsystem Vendor ID (SSVID), and IEEE OUI are little-endian, while Serial Number (SN), Model Number (MN), EUI64, and NGUID are big-endian. The NVMe Qualified Name (NQN) uniquely names a host or subsystem: a UTF-8 string of at most 223 bytes, null-terminated, in one of two formats - a domain-name-based form (nqn.yyyy-mm.reverse.domain:suffix) or a UUID-based form. List Data Structures like the Controller List and Namespace List follow a simple shape: a count header followed by a packed array of fixed-size IDs, zero-filled past the count. UTF-8 strings including NQNs are compared and stored as raw binary byte sequences.""",
                    source = "NVMe Base Spec 2.3 §4.4, §4.5, §4.6, §4.7, §4.8"
                ))
            ),
            ChapterSeed(
                id = 213, partId = 2, order = 13,
                title = "Error Reporting & Recovery",
                shortDescription = "Status codes, controller fatal status, and recovery procedures",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "When things go wrong",
                    bullets = listOf(
                        "Command errors are reported via the Status field: Status Code Type (SCT) picks the category, Status Code (SC) picks the specific error.",
                        "Generic Command Status (SCT=0h) covers the basics: Successful Completion (00h), Invalid Command Opcode (01h), Invalid Field in Command (02h), Data Transfer Error (04h), Internal Error (06h), and more.",
                        "Serious Submission/Completion Queue errors call for deleting and recreating the affected queue(s), or resetting the whole controller for Admin Queue issues.",
                        "Media/data errors (like End-to-end Guard Check failures) complete the command with a status that identifies the specific failure type.",
                        "Controller Fatal Status (CSTS.CFS = 1) means the controller can't even talk back reliably - the host should reset and reinitialize.",
                        "If CSTS.CFS persists after a Controller Reset, escalate to an NVM Subsystem Reset (with caution - it can bounce PCIe links).",
                        "Communication-loss recovery needs care: retrying blindly risks corrupting data if the original command might still complete later."
                    ),
                    notes = """The Generic Command Status table (SCT 0h) defines everyday cases: 00h Successful Completion, 01h Invalid Command Opcode, 02h Invalid Field in Command, 03h Command ID Conflict, 04h Data Transfer Error, 05h Commands Aborted due to Power Loss Notification, 06h Internal Error, 07h Command Abort Requested, plus SGL- and namespace-related errors - with Command Specific (1h), Media/Data Integrity (2h), and Path Related (3h) types layered on top. For a serious error tied to a specific queue, the recommended recovery is to delete and recreate the affected queue(s); for serious Admin command errors, escalate to a full Controller Level Reset. Media/data errors are reported by completing the offending command with an appropriate status. The most serious controller-level condition is Controller Fatal Status (CSTS.CFS): the controller may be unable to reliably post completions at all - a host seeing repeated timeouts should check CSTS.CFS, issue a Controller Reset, and if that doesn't clear it, escalate to an NVM Subsystem Reset. When a host loses communication entirely, blindly retrying a command whose original completion might still arrive later risks data corruption; the host must first establish the controller can no longer be processing the original commands before retrying elsewhere.""",
                    source = "NVMe Base Spec 2.3 §4.2.3, §9"
                ))
            )
        )
    )
}
