package com.nvmeacademy.app.data.content

/** Part 5: Advanced Topics - the NVM Command Set's Extended Capabilities (§5.1-§5.11). */
object Part5AdvancedTopics {
    val part = PartSeed(
        id = 5,
        order = 5,
        title = "Part 5 · Advanced Topics",
        subtitle = "Security, data protection, and extended capabilities",
        chapters = listOf(
            ChapterSeed(
                id = 501, partId = 5, order = 1,
                title = "Asymmetric Namespace Access (ANA) Reporting",
                shortDescription = "Multipath accessibility states and their effect on Features/Identify",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Optimized, Non-Optimized, Inaccessible, Persistent Loss, Change",
                    bullets = listOf(
                        "ANA lets a controller tell a host, per namespace-and-path, whether that path is Optimized, Non-Optimized, Inaccessible, Persistent Loss, or in a Change transition - the NVMe analog of SCSI ALUA.",
                        "The NVM Command Set layers extra rules on top of the Base Spec's generic ANA mechanism, specifically around which Feature Identifiers remain usable in each ANA state.",
                        "In ANA Inaccessible, Persistent Loss, or Change states, the NVM-Command-Set-specific Error Recovery feature (Feature ID 05h) becomes unavailable to Get/Set Features.",
                        "In ANA Inaccessible or Persistent Loss states, namespace capacity fields returned by Identify Namespace are cleared to 0h.",
                        "Commands that try to use an unavailable feature/log page abort with a state-specific status: Asymmetric Access Inaccessible, Persistent Loss, or Transition.",
                        "Set Features additionally disables feature-saving while in the affected ANA states, preventing a stale/inaccessible-path value from being persisted as the saved default."
                    ),
                    notes = """ANA is fundamentally a Base Specification multipath feature (a namespace can be reached via several controllers/ports, each in a different accessibility state at any moment), and the NVM Command Set spec's contribution is narrow but important: it pins down exactly which of its own command-set-specific Feature Identifiers stop working when a path degrades, and what capacity-reporting behavior a degraded path should exhibit. A learner reading only the Base Spec's ANA section would not know that Error Recovery (Feature 05h) specifically is blacklisted in three of the four non-optimal ANA states, or that Identify Namespace zeroes out capacity fields under Inaccessible/Persistent Loss - these are command-set-specific refinements layered onto the generic mechanism.""",
                    source = "NVM Command Set Spec §5.1"
                ))
            ),
            ChapterSeed(
                id = 502, partId = 5, order = 2,
                title = "Get LBA Status & I/O Performance Hints",
                shortDescription = "Finding allocated/unrecoverable LBAs, and alignment hints for throughput",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Correctness vs performance: two different kinds of hint",
                    bullets = listOf(
                        "This capability bundles two related but distinct things: the Get LBA Status command's underlying model, and a set of I/O alignment/performance hint attributes.",
                        "Get LBA Status answers two questions cheaply: \"which LBAs are actually allocated\" (sparse-namespace migration/snapshot without copying unwritten space) and \"which LBAs are Potentially Unrecoverable\" (proactive detection of failing media).",
                        "LBA Status Information Alert async events notify the host proactively when Tracked LBAs cross a reporting threshold or a component failure occurs.",
                        "Namespace I/O Boundaries (NOIOB) mark internal alignment boundaries; commands that cross them may see reduced performance.",
                        "Namespace Atomic Boundary Size Normal (NABSN) and Offset (NABO) describe similar alignment guidance specifically for Write/Write-Uncorrectable/Write-Zeroes atomicity.",
                        "Namespace Preferred Write/Read Granularity and Alignment (NPWG/NPWA/NPRG/NPRA) are the concrete size/alignment values a host should round I/O requests to for best throughput."
                    ),
                    notes = """Get LBA Status is presented here as a capability rather than purely a command because its value comes from the surrounding machinery: the LBA Status Information log page, the LBA Status Information Attributes Feature, and the async event notice that tells a host when to bother reading that log page - the Get LBA Status command itself is just the final step of fetching detail once the host knows where to look. The performance-hint half of this section is squarely advisory, not correctness-affecting: I/O that crosses a NOIOB, ignores NABSN/NABO, or isn't sized/aligned to NPWG/NPWA/NPRG/NPRA will still complete correctly - it just may run slower. This correctness-vs-performance distinction is worth calling out explicitly, since it's easy to conflate performance hints with mandatory alignment requirements (which do exist elsewhere, e.g., Key Per I/O's KPIODAAG granularity).""",
                    source = "NVM Command Set Spec §5.2, §5.2.1, §5.2.2"
                ))
            ),
            ChapterSeed(
                id = 503, partId = 5, order = 3,
                title = "End-to-End Data Protection",
                shortDescription = "Guard, Application Tag, Reference Tag - detecting corruption in transit",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Guard, App Tag, Reference Tag",
                    bullets = listOf(
                        "End-to-end data protection adds a per-logical-block Guard (CRC), Application Tag, and Reference Tag to metadata, letting corruption be detected anywhere between the application and the media.",
                        "Three protection information sizes exist: 16b Guard (CRC-16, classic SCSI DIF/DIX-compatible), 32b Guard, and 64b Guard.",
                        "Two placement models, equivalent to SCSI's DIF vs DIX: protection info interleaved directly with logical block data vs stored in a separate metadata buffer - selected by namespace format, not by command.",
                        "Protection Information Type (Type 1/2/3, per SBC-4) controls how strictly the Reference Tag ties a logical block to its address, guarding against misdirected or out-of-order writes/reads.",
                        "PRINFO (a 4-bit action+check field) appears in nearly every I/O command to control whether protection info is generated/checked and which parts are validated.",
                        "Copy commands carry independent PRINFOR (read-side) and PRINFOW (write-side) fields since a Copy's source and destination namespaces may use different protection formats."
                    ),
                    notes = """This chapter is the technical core behind why NVMe commands carry so many "tag" fields. The 64-bit CRC option (64b Guard) is expanded machinery relative to older NVMe generations and includes a fully worked CRC parameter set and test vectors in the spec for implementers to validate against. The DIF-vs-DIX distinction is purely about metadata buffer placement (contiguous vs separate), not about the protection algorithm itself - a common point of confusion worth calling out directly. PRINFO's presence in Read/Write/Compare/Verify (with PRACT forced to 0 for Compare/Verify, since there's no "generate protection info" concept when you're not writing) versus Copy's split PRINFOR/PRINFOW versus Write Zeroes' PRCHK-forced-to-zero (you can't "check" the protection info of a block you're about to erase) is a good comparative table for the app's UI.""",
                    source = "NVM Command Set Spec §5.3, §5.3.1.1-§5.3.1.3, §5.3.2",
                    diagram = ChapterDiagramSeed(
                        caption = "The three parts of protection information",
                        connector = "none",
                        steps = listOf(
                            DiagramStepSeed("Guard", "CRC over data"),
                            DiagramStepSeed("Application Tag", "host-defined"),
                            DiagramStepSeed("Reference Tag", "ties block to its LBA")
                        )
                    )
                ))
            ),
            ChapterSeed(
                id = 504, partId = 5, order = 4,
                title = "Key Per I/O",
                shortDescription = "Per-command encryption context via CETYPE/CEV",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Encryption context on a per-command basis",
                    bullets = listOf(
                        "Key Per I/O lets each individual I/O command carry (or reference) its own encryption key context, rather than relying purely on a namespace-wide encryption key.",
                        "It builds on the Trusted Computing Group's Security Subsystem Key Per I/O specification - NVMe supplies the plumbing (CETYPE/CEV fields), TCG defines the security semantics.",
                        "CETYPE (Command Extension Type) and CEV (Command Extension Value), present in Read/Write/Compare/Verify/Write-Zeroes/Copy's Command Dword 12/13, are the generic mechanism.",
                        "Key Per I/O commands must respect an alignment/granularity constraint reported by the namespace's KPIODAAG field - both SLBA and range length must be aligned to it, or the command aborts.",
                        "This is one of the few places in the NVM Command Set where an alignment requirement is mandatory (command-failing) rather than merely a performance hint."
                    ),
                    notes = """Key Per I/O is a compact but important extension because it's the mechanism that makes per-command (rather than per-namespace) encryption context practical in NVMe - each command can be tagged to indicate which key domain it should be encrypted/decrypted under, which matters for multi-tenant SSDs where different tenants' data must never share a key even within the same namespace. The CETYPE/CEV fields it introduces are reused generically across almost every data-path command, so this is a good place to explain the shared mechanism once rather than repeating it per-command. The KPIODAAG alignment requirement is worth flagging as a genuine hard constraint versus the softer NOIOB-style guidance elsewhere.""",
                    source = "NVM Command Set Spec §5.4"
                ))
            ),
            ChapterSeed(
                id = 505, partId = 5, order = 5,
                title = "LBA Format List Structure",
                shortDescription = "How Format Index selects block size and metadata layout",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Choosing a format by index, not by inline parameters",
                    bullets = listOf(
                        "Namespaces are formatted according to an \"LBA Format,\" identified by a small integer Format Index, not by directly specifying block size/protection settings inline.",
                        "The format list splits into two groups: NLBAF (\"green\") formats sharing one set of common capabilities, and NULBAF (\"orange\") formats each with unique attributes.",
                        "Up to 16 total LBA Formats are supported normally, expanding to 64 if the host has enabled the LBA Format Extension Enable (LBAFEE) field.",
                        "A Format Index is valid if it's less than NLBAF+NULBAF combined; a defined-but-currently-unavailable format is signaled by an LBA Data Size of 0h.",
                        "Different Identify CNS values (00h/05h/08h vs 09h/0Ah) determine whether returned data reflects only the NLBAF-referenced formats or both NLBAF- and NULBAF-referenced formats."
                    ),
                    notes = """This is fundamentally a data-modeling/indexing chapter: before you can Format NVM or create a namespace (Namespace Management), you must choose a Format Index, and this section explains how that index space is organized and how much of it a controller can expose. The NLBAF/NULBAF split exists so that controllers with many logical-block-size options aren't forced to duplicate a large shared-capability block for every variant - "green" formats share capability metadata, "orange" formats each get their own. A good exercise: "given NLBAF=4 and NULBAF=2, which CNS values return unique data for Format Index 5?" (answer: only 09h/0Ah, since Format Index 5 falls in the NULBAF-unique range).""",
                    source = "NVM Command Set Spec §5.5, Figure 175/176"
                ))
            ),
            ChapterSeed(
                id = 506, partId = 5, order = 6,
                title = "LBA Migration Queue",
                shortDescription = "Tracking changed LBAs for efficient incremental replication",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "A change log for efficient migration",
                    bullets = listOf(
                        "The LBA Migration Queue is the NVM-Command-Set-specific instantiation of the Base Spec's generic \"User Data Migration Queue\" concept - a controller-maintained log of which logical blocks changed.",
                        "Entries record: data changed, data may have changed, blocks were deallocated, or blocks may have been deallocated - a deliberately loose \"may have\" category.",
                        "Logging is started/stopped via the Base Spec's Track Send command; the LBA Migration Queue itself only structures the resulting entries for the NVM Command Set.",
                        "Special sentinel entries mark queue lifecycle events: first entry after logging starts/resumes, last entry after a stop request, last entry due to suspension, and last entry because the queue became full.",
                        "A controller may coalesce multiple commands' effects into a single entry, so entry count is not a 1:1 proxy for command count.",
                        "The primary use case is efficient incremental replication/migration: copy only the LBAs that actually changed since the last sync."
                    ),
                    notes = """This capability targets live-migration and replication tooling - think vMotion-style namespace migration, or asynchronous replication to a backup target - where re-reading an entire large namespace on every sync interval would be prohibitively expensive. Because a Write command's effect can be observed by a Read even before that Write's own completion is posted, the queue effectively offers a lower-latency signal of "this data is now readable at the new value" than waiting on the write's CQE - a subtlety worth surfacing since it inverts the naive assumption that a queue entry implies the command already completed. """,
                    source = "NVM Command Set Spec §5.6, Figure 177"
                ))
            ),
            ChapterSeed(
                id = 507, partId = 5, order = 7,
                title = "Namespace Management: Granularity Extensions",
                shortDescription = "Sizing namespaces to avoid stranded capacity",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Fully provisioned vs stranded capacity",
                    bullets = listOf(
                        "Namespace Management (create/delete namespaces) is a Base Spec Admin command; this chapter covers only the NVM-Command-Set-specific additions layered on top.",
                        "The main addition is Namespace Granularity reporting: hints for Namespace Size Granularity and Namespace Capacity Granularity so hosts can size namespaces to avoid stranding unaddressable capacity.",
                        "A namespace is \"fully provisioned\" when NSZE and NCAP are each exact multiples of their respective granularity and NSZE equals NCAP.",
                        "Granularity mismatches are hints, not hard errors - the controller must still accept an otherwise-valid create request even if it isn't granularity-aligned."
                    ),
                    notes = """The practical lesson here is capacity efficiency: SSD controllers often internally allocate storage in fixed-size chunks (e.g., erase blocks or larger), so a namespace whose requested size doesn't align to those chunks can end up with allocated-but-unaddressable space. Namespace Granularity reporting exists purely to let a capacity-conscious host avoid that waste by rounding its NSZE/NCAP choices in Namespace Management's create payload to the reported granularities. This pairs well with the LBA Format List chapter, since both are about giving hosts enough metadata to make efficient namespace-creation decisions before committing storage.""",
                    source = "NVM Command Set Spec §5.7"
                ))
            ),
            ChapterSeed(
                id = 508, partId = 5, order = 8,
                title = "Media and Data Error Handling",
                shortDescription = "What a failed write leaves behind, precisely",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Atomicity after a failed write",
                    bullets = listOf(
                        "Extends the Base Spec's generic media/data error handling with NVM-Command-Set-specific write-failure semantics.",
                        "If a write fails and the write size was within the Atomic Write Unit Power Fail (AWUPF) size, subsequent reads of those logical blocks are guaranteed to return the previous successful write's data.",
                        "If the failed write was larger than AWUPF, subsequent reads may return either the previous data or the (failed) new write's data - no atomicity guarantee beyond the AWUPF boundary.",
                        "The controller's error-recovery effort in the failure path is still influenced by the command's Limited Retry (LR) bit, same as for successful-path reads/writes."
                    ),
                    notes = """This chapter formalizes exactly what "atomic write" means operationally when a write fails partway: atomicity here isn't about all-or-nothing success across the whole command, but about read consistency afterward - a small (within-AWUPF) failed write can't leave a torn/mixed-data logical block visible to subsequent reads, while a larger failed write offers no such guarantee. This is a precise, testable definition worth contrasting against a naive "atomic = succeeds or fails as a whole" mental model that learners often bring in from database transaction contexts.""",
                    source = "NVM Command Set Spec §5.8"
                ))
            ),
            ChapterSeed(
                id = 509, partId = 5, order = 9,
                title = "Reservations: Command Behavior Rules",
                shortDescription = "The Allowed/Conflict lookup table for every reservation type",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Read group vs write group, per reservation type",
                    bullets = listOf(
                        "Reservations themselves (Register/Acquire/Release/Report) are Base Spec commands; this section adds the NVM-Command-Set-specific rulebook for how each data command behaves in the presence of a reservation.",
                        "Commands are grouped into a Read Command Group (Compare, Copy-as-source, Read, Verify) and a Write Command Group (Copy-as-destination, Dataset Management, Write, Write Uncorrectable, Write Zeroes).",
                        "A lookup table cross-references each group against each of the six reservation types and each caller role (non-registrant, registrant, holder), yielding a simple Allowed/Conflict verdict per cell.",
                        "A command that loses the check aborts with a Reservation Conflict status rather than executing.",
                        "Copy is special-cased: its source namespace(s) are checked as if read by the calling host, and its destination namespace is independently checked as if written by the calling host.",
                        "\"Write Exclusive\" reservation types still allow non-holders into the Read group while blocking the Write group; \"Exclusive Access\" types block non-holders from both groups."
                    ),
                    notes = """This chapter is the concrete, mechanical payoff of the broader Reservations concept (see the "Reservations & Persistent Reservations Explained" overview chapter for the acquire/register/preempt lifecycle) - once a reservation of a given type is held, the command-behavior table is the lookup table that determines, command-by-command, whether a given host's I/O actually executes or is rejected. An excellent structured-quiz source: "Host X is a non-registrant; namespace has an Exclusive Access - Registrants Only reservation; Host X submits a Read - allowed or conflict?" The Copy special-case (independent source/destination checks) is a frequently-missed detail worth its own explicit callout, since every other command in the table is checked against only one namespace.""",
                    source = "NVM Command Set Spec §5.9, Figure 178"
                ))
            ),
            ChapterSeed(
                id = 510, partId = 5, order = 10,
                title = "Sanitize Operations: Post-Erase Read Behavior",
                shortDescription = "Block Erase, Crypto Erase, Overwrite, and Media Verification",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "What reads return after a sanitize",
                    bullets = listOf(
                        "Sanitize (the Base Spec Admin command for irreversibly destroying all user data) gets NVM-Command-Set-specific rules for exactly what post-sanitize reads return.",
                        "Three sanitize operation types yield different \"erased\" data: Block Erase returns a vendor-specific value, Crypto Erase returns an indeterminate value, Overwrite follows the Base Spec's overwrite-mechanism rules.",
                        "A Media Verification state exists after sanitize completes, during which specially-flagged Read commands (no protection-info checking requested) are allowed to read raw (possibly garbage) media without triggering Unrecovered Read Error.",
                        "Read commands that do request protection-info checking during Media Verification are rejected outright with Invalid Field in Command.",
                        "The host can request sanitized logical blocks not be deallocated via the No-Deallocate After Sanitize bit."
                    ),
                    notes = """The Media Verification state is the most distinctive NVM-Command-Set contribution here: it lets an auditor (or the host itself) explicitly probe what a freshly-sanitized drive's media actually contains - including reading successive different "garbage" values from the same LBA on purpose (used to demonstrate the media was actually overwritten with unpredictable data, not just marked as invalid) - without every such read blowing up as an error. This is a niche but important compliance/audit feature (data-destruction verification for regulated environments), and the strict split between "no PI checking = allowed, returns whatever's there" vs "PI checking requested = rejected" is a clean two-branch decision tree well suited to a quiz-style app feature.""",
                    source = "NVM Command Set Spec §5.10, Figure 179"
                ))
            ),
            ChapterSeed(
                id = 511, partId = 5, order = 11,
                title = "Streams",
                shortDescription = "Grouping related writes to reduce write amplification",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "One NVM-Command-Set-specific refinement",
                    bullets = listOf(
                        "Streams (a Base Spec feature letting a host tag writes with a Stream Identifier so the controller can group related data together) gets one NVM-Command-Set-specific refinement.",
                        "The Stream Write Size (SWS) field's unit of granularity is specified, for this command set, to be logical blocks - i.e., SWS is expressed in the namespace's own LBA size rather than raw bytes."
                    ),
                    notes = """This is a short but easy-to-miss clarification: Streams itself is entirely defined in the Base Specification (directive-based write grouping to reduce controller-side write amplification, especially valuable for QLC/TLC NAND and other write-amplification-sensitive media), and the NVM Command Set spec's only contribution is nailing down that SWS is measured in logical blocks for this command set specifically - worth calling out precisely because a different I/O command set (e.g., one with variable-size objects rather than fixed LBAs) might define that unit differently.""",
                    source = "NVM Command Set Spec §5.11"
                ))
            )
        )
    )
}
