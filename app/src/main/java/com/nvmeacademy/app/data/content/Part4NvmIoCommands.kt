package com.nvmeacademy.app.data.content

/** Part 4: NVM I/O Command Set & Fabrics - the data-path commands. */
object Part4NvmIoCommands {
    val part = PartSeed(
        id = 4,
        order = 4,
        title = "Part 4 · NVM I/O Commands & Fabrics",
        subtitle = "Read, write, and data movement commands",
        chapters = listOf(
            ChapterSeed(
                id = 401, partId = 4, order = 1,
                title = "NVM Command Set Overview",
                shortDescription = "The default block-storage I/O command set almost every SSD implements",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Fixed logical blocks, addressed by LBA",
                    bullets = listOf(
                        "The NVM Command Set is the original/default NVMe I/O command set - block storage over fixed-size logical blocks (LBAs), the model almost every SSD implements.",
                        "Opcodes split cleanly: data-moving commands (Read, Write, Compare, Copy, Write Zeroes, Write Uncorrectable, Verify) vs management/advisory commands (Flush, Dataset Management) vs access-control commands (the Reservation family, Cancel).",
                        "Some I/O opcodes (Flush, Cancel, Reservation Register/Report/Acquire/Release, I/O Management Send/Receive) are actually defined once in the Base Specification and reused by every I/O command set - the NVM Command Set spec doesn't redefine them.",
                        "Every read/write-shaped command shares a common parameter vocabulary: SLBA (starting LBA), NLB (number of logical blocks, 0's-based), PRINFO (protection-info action/check), LR (Limited Retry), FUA (Force Unit Access).",
                        "Extended Capabilities (spec section 5) layer optional features on top: end-to-end data protection, reservations, sanitize, streams, namespace management, ANA reporting, and more.",
                        "Command Set Identifier (CSI) 00h designates \"NVM Command Set\" throughout Identify/Fabrics/etc., distinguishing it from other I/O command sets a subsystem might also expose.",
                        "Admin-side, the NVM Command Set mostly reuses Base Spec Admin commands as-is and adds CNS extensions rather than whole new commands - Get LBA Status is the one genuinely new Admin command it introduces."
                    ),
                    notes = """The NVM Command Set Specification (Rev 1.2) formalizes what most engineers picture when they hear "NVMe": fixed logical-block storage addressed by a 64-bit LBA, described in submission queue entries whose Command Dword 10-15 layout is opcode-specific. Its three main sections are I/O Commands (the block-data-path commands), Admin Commands for the NVM Command Set (mostly clarifying how generic Base Spec Admin commands behave under this command set, plus CNS extensions to Identify and the new Get LBA Status command), and Extended Capabilities (optional, separately-negotiated features layered on top: ANA reporting, end-to-end protection, Key Per I/O, LBA Format lists, LBA Migration Queues, Namespace Management granularity hints, media/data error handling nuances, Reservations, Sanitize, and Streams). A key architectural point: many "NVM I/O opcodes" a learner sees in the opcode table (Flush, Cancel, the four Reservation commands, I/O Management Send/Receive) are not actually specified in the NVM Command Set document at all - they live in the Base Specification because every I/O command set shares them verbatim. Distinguishing "defined here" (Compare, Copy, Dataset Management, Read, Verify, Write, Write Uncorrectable, Write Zeroes) from "inherited from Base Spec" is a useful mental model checkpoint.""",
                    source = "NVM Command Set Spec §1-§5 overview, Figure 21; Base Spec 2.3 §7"
                ))
            ),
            ChapterSeed(
                id = 402, partId = 4, order = 2,
                title = "Reservations & Persistent Reservations Explained",
                shortDescription = "How clustered hosts safely share a namespace",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Register, then acquire",
                    bullets = listOf(
                        "Persistent Reservations let multiple hosts share a namespace while restricting who may read/write it - the same access-control model as SCSI Persistent Reservations, adapted to NVMe.",
                        "Two-step model: a host first \"registers\" a reservation key with the namespace (Reservation Register), then a registrant may \"acquire\" a reservation of a chosen type on top of that registration (Reservation Acquire).",
                        "Six reservation types exist, combining exclusivity (Write Exclusive vs Exclusive Access) with registrant scope (holder-only, \"Registrants Only\", or \"All Registrants\").",
                        "Reservation Report returns the live registrant list plus a Generation counter that increments on every reservation-changing event.",
                        "Reservation Release ends the calling host's held reservation; Reservation Release with the Clear action wipes out the reservation and every registrant, a \"nuclear reset\" of sharing state.",
                        "Persist Through Power Loss (PTPL) state, toggled via Reservation Register's CPTPL field, decides whether reservations/registrants survive a power cycle - critical for cluster failover semantics.",
                        "Copy commands are checked twice: each source namespace as if by a Read, the destination namespace as if by a Write - so a Copy can conflict on either side independently."
                    ),
                    notes = """Persistent Reservations solve the classic clustered-storage problem: several hosts (e.g., nodes in a failover cluster) attach to the same namespace, but only the current cluster owner should be allowed to write, while standbys might still need read access, or fenced nodes need to be locked out entirely. Reservation Register establishes a "membership card" (a reservation key) for a host without granting any access rights by itself. Reservation Acquire then either creates the reservation (if none exists) or, using a supplied Preempt Reservation Key, forcibly displaces whoever currently holds it - with the "Preempt and Abort" variant also aborting that displaced host's in-flight commands, which is what makes fast, safe cluster failover possible. The six reservation types trade off exclusivity for shared-read convenience: "Write Exclusive" types let non-holders still read; "Exclusive Access" types lock out non-holders from both; the "Registrants Only" and "All Registrants" suffixes broaden who is treated as allowed beyond just the single current holder. Reservation Report's Generation counter is the idiomatic way a host polls for "did anything change" without re-parsing the whole registrant list every time. Reservation Release (Release action) is the polite way to give up a reservation; Clear is the forceful full reset, typically used in disaster-recovery/re-provisioning scenarios.""",
                    source = "Base Spec 2.3 §7.5-§7.8; NVM Command Set Spec §5.9"
                ))
            )
        )
    )
}
