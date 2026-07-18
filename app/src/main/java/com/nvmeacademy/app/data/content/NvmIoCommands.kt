package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/** NVM I/O Command Set reference entries (NVM Command Set Spec 1.2 §3.3, Base Spec 2.3 §7). */
object NvmIoCommands {
    val list: List<CommandSeed> = listOf(
        CommandSeed(
            id = 2001, opcode = "00h", name = "Flush", commandSet = CommandSet.NVM_IO,
            summary = "Requests that any volatile write cache contents be committed to non-volatile media.",
            description = """Commits data and metadata for the specified namespace(s) that were completed by the controller prior to the Flush being submitted; the controller may also flush additional data from other namespaces as a side effect. Behavior when NSID = FFFFFFFFh (apply to all attached namespaces) depends on the Flush Behavior field in Identify Controller's VWC field. If no volatile write cache is present or enabled, Flush is a no-op that still completes successfully.""",
            mandatory = "Mandatory (core I/O command)",
            source = "Base Spec 2.3 §7.2; NVM Command Set Spec Figure 21"
        ),
        CommandSeed(
            id = 2002, opcode = "01h", name = "Write", commandSet = CommandSet.NVM_IO,
            summary = "Writes host-supplied data (and metadata/protection information, if applicable) to the specified LBA range.",
            description = """Transfers data from the host to the controller for storage at the specified logical block range, optionally including metadata and end-to-end protection information per PRINFO and the supplied tag fields. Force Unit Access (FUA) forces the write (and its metadata) to be committed to non-volatile media before command completion; Limited Retry (LR) trades error-recovery thoroughness for speed.""",
            mandatory = "Mandatory (core I/O command)",
            source = "NVM Command Set Spec §3.3.6",
            fields = listOf(
                FieldSeed("SLBA (CDW10-11)", "64-bit starting Logical Block Address."),
                FieldSeed("NLB (CDW12 15:00)", "Number of Logical Blocks to transfer, 0's-based."),
                FieldSeed("LR / FUA (CDW12 31/30)", "Limited Retry, Force Unit Access."),
                FieldSeed("PRINFO (CDW12 29:26)", "Protection information action/check.")
            )
        ),
        CommandSeed(
            id = 2003, opcode = "02h", name = "Read", commandSet = CommandSet.NVM_IO,
            summary = "Reads data (and metadata/protection information, if applicable) from a namespace at the given LBA range into a host buffer.",
            description = """Transfers data and any associated metadata from the specified logical block range to the host. The host may set Protection Information (PRINFO) checking, and may supply expected tag values to be validated against media protection information. Limited Retry (LR) requests the controller favor speed over exhaustive error recovery; Force Unit Access (FUA) forces the controller to first commit any cached write data for the affected blocks before servicing the read.""",
            mandatory = "Mandatory (core I/O command)",
            source = "NVM Command Set Spec §3.3.4",
            fields = listOf(
                FieldSeed("SLBA (CDW10-11)", "64-bit starting LBA."),
                FieldSeed("NLB (CDW12 15:00)", "Number of Logical Blocks, 0's-based."),
                FieldSeed("LR / FUA (CDW12 31/30)", "Limited Retry, Force Unit Access."),
                FieldSeed("PRINFO (CDW12 29:26)", "Protection information action/check.")
            )
        ),
        CommandSeed(
            id = 2004, opcode = "04h", name = "Write Uncorrectable", commandSet = CommandSet.NVM_IO,
            summary = "Marks a range of logical blocks as invalid/unreadable (\"poisons\" them) until a subsequent write clears the condition.",
            description = """Does not write any data; instead it flags the specified LBA range so that any subsequent Read of those blocks fails with Unrecovered Read Error, simulating a media error for test or error-injection purposes. The uncorrectable/invalid marking is only cleared by a later write to that block. The Write Uncorrectable Size Limit (WUSL) field in Identify Controller reports a recommended or hard maximum range size.""",
            mandatory = "Optional (gated by WUSL/NVMWUSV in Identify Controller)",
            source = "NVM Command Set Spec §3.3.7",
            fields = listOf(FieldSeed("SLBA (CDW10-11)", "64-bit starting LBA to mark uncorrectable."))
        ),
        CommandSeed(
            id = 2005, opcode = "05h", name = "Compare", commandSet = CommandSet.NVM_IO,
            summary = "Reads logical blocks from media and compares them against a host-supplied buffer without returning data to the host.",
            description = """Reads the logical blocks specified by SLBA/NLB and compares the data read to a comparison buffer supplied by the host. If the data matches with no miscompares, the command completes successfully; any miscompare causes the command to fail with a Compare Failure status. It is most commonly fused with a Write command (Compare-and-Write) to implement an atomic compare-and-swap semantic, bounded by the Atomic Compare & Write Unit (ACWU).""",
            mandatory = "Optional (commonly gated by ONCS \"Compare\" support bit)",
            source = "NVM Command Set Spec §3.3.1",
            fields = listOf(
                FieldSeed("SLBA (CDW10-11)", "64-bit starting LBA."),
                FieldSeed("NLB (CDW12 15:00)", "Number of Logical Blocks, 0's-based."),
                FieldSeed("Data Pointer", "Data buffer holding the comparison data.")
            )
        ),
        CommandSeed(
            id = 2006, opcode = "08h", name = "Write Zeroes", commandSet = CommandSet.NVM_IO,
            summary = "Clears a range of logical blocks (or, with NSZ, an entire namespace) to zero without transferring data over the interface.",
            description = """Causes subsequent reads of the affected range to return all-zero data until a later write occurs, and updates protection information per PRACT/PRINFO. If Deallocate (DEAC) is set, the host is also requesting the controller deallocate the specified blocks. The Namespace Zeroes (NSZ) bit, when supported, requests the controller zero the entire namespace by deallocation, ignoring SLBA/NLB.""",
            mandatory = "Optional (ONCS/NSZS-gated)",
            source = "NVM Command Set Spec §3.3.8",
            fields = listOf(
                FieldSeed("SLBA (CDW10-11)", "64-bit starting LBA, ignored if NSZ set."),
                FieldSeed("DEAC (CDW12 bit 25)", "Deallocate."),
                FieldSeed("NSZ (CDW12 bit 23)", "Namespace Zeroes - zero the whole namespace.")
            )
        ),
        CommandSeed(
            id = 2007, opcode = "09h", name = "Dataset Management", commandSet = CommandSet.NVM_IO,
            summary = "Advisory command that lets the host describe intended access patterns (e.g., deallocate/TRIM, sequential hints) for one or more LBA ranges.",
            description = """An advisory, non-binding command: the host supplies up to 256 ranges (each with a Starting LBA, Length, and Context Attributes) and the controller may optimize storage/performance based on that information, but is never required to act on it. The most operationally significant use is the Attribute-Deallocate (AD) bit, which requests the controller deallocate (TRIM) the specified ranges.""",
            mandatory = "Optional (ONCS-gated \"Dataset Management Support\")",
            source = "NVM Command Set Spec §3.3.3",
            fields = listOf(
                FieldSeed("NR (CDW10 07:00)", "Number of Ranges, 0's-based, max 256."),
                FieldSeed("AD (CDW11 bit 02)", "Attribute-Deallocate (TRIM).")
            )
        ),
        CommandSeed(
            id = 2008, opcode = "0Ch", name = "Verify", commandSet = CommandSet.NVM_IO,
            summary = "Confirms the integrity of stored data/metadata for an LBA range by reading and checking it internally, without transferring any data to the host.",
            description = """Performs the same integrity-checking work as a Read but never transfers data or metadata across the interface - only a completion status is returned. Used to proactively scrub/validate media without consuming host DMA bandwidth. The Verify Size Limit (VSL) field in Identify Controller indicates a recommended or mandatory maximum data size for a single Verify command.""",
            mandatory = "Optional (gated by NVMVFYS/VSL in Identify Controller)",
            source = "NVM Command Set Spec §3.3.5",
            fields = listOf(FieldSeed("SLBA / NLB (CDW10-12)", "64-bit starting LBA and Number of Logical Blocks."))
        ),
        CommandSeed(
            id = 2009, opcode = "19h", name = "Copy", commandSet = CommandSet.NVM_IO,
            summary = "Copies user data from one or more source LBA ranges (possibly in different namespaces) into a single contiguous destination LBA range, entirely within the controller.",
            description = """Lets a host move data from one or more source ranges (each potentially in a different namespace) to a single consecutive destination range in the namespace specified by NSID, without transferring the data through the host. The command carries a list of Source Range entries; the Descriptor Format field selects a Copy Descriptor Format, with some formats supporting cross-namespace copies via a Source Namespace Identifier (SNSID).""",
            mandatory = "Optional (ONCS-gated; controller advertises supported descriptor formats)",
            source = "NVM Command Set Spec §3.3.2",
            fields = listOf(
                FieldSeed("SDLBA (CDW10-11)", "64-bit starting destination LBA."),
                FieldSeed("NR (CDW12 07:00)", "Number of Ranges, 0's-based, up to 256."),
                FieldSeed("DESFMT (CDW12 11:08)", "Copy Descriptor Format.")
            )
        ),
        CommandSeed(
            id = 2010, opcode = "0Dh", name = "Reservation Register", commandSet = CommandSet.NVM_IO,
            summary = "Registers, unregisters, or replaces a host's reservation key on a namespace (the entry point into the persistent-reservation model).",
            description = """Manages a host's registration (its reservation key) with a namespace, the prerequisite for acquiring or being affected by a reservation. The Reservation Register Action (RREGA) field selects Register (create a new key), Unregister (remove registration), or Replace (swap the current key for a new one). Change Persist Through Power Loss State (CPTPL) can toggle whether reservation state survives a power cycle.""",
            mandatory = "Optional (part of the optional Reservations capability)",
            source = "Base Spec 2.3 §7.6",
            fields = listOf(FieldSeed("RREGA (CDW10 02:00)", "000b Register, 001b Unregister, 010b Replace."))
        ),
        CommandSeed(
            id = 2011, opcode = "0Eh", name = "Reservation Report", commandSet = CommandSet.NVM_IO,
            summary = "Returns the current reservation type and full list of registrants for a namespace.",
            description = """Reads back a Reservation Status data structure describing whether a reservation is currently held (and its type), a Generation counter that increments on any reservation-state-changing command, the Persist Through Power Loss State, and a list of Registrant data structures - one per host currently registered against the namespace.""",
            mandatory = "Optional (part of the optional Reservations capability)",
            source = "Base Spec 2.3 §7.8",
            fields = listOf(FieldSeed("EDS (CDW11 bit 00)", "Extended Data Structure selector (64-bit vs 128-bit Host Identifier)."))
        ),
        CommandSeed(
            id = 2012, opcode = "11h", name = "Reservation Acquire", commandSet = CommandSet.NVM_IO,
            summary = "Acquires a new reservation on a namespace, or preempts (optionally with abort) an existing reservation held by another registrant.",
            description = """Used to take a reservation of a specified type (Write Exclusive, Exclusive Access, and their Registrants-Only/All-Registrants variants) on a namespace, or to preempt a reservation currently held by a different registrant by supplying that registrant's Preempt Reservation Key. The "Preempt and Abort" action also causes the controller to abort outstanding commands that conflict with the newly acquired reservation.""",
            mandatory = "Optional (part of the optional Reservations capability)",
            source = "Base Spec 2.3 §7.5",
            fields = listOf(
                FieldSeed("RTYPE (CDW10 15:08)", "Reservation Type: 1h Write Exclusive, 2h Exclusive Access, 3h-6h Registrants-Only/All-Registrants variants."),
                FieldSeed("RACQA (CDW10 02:00)", "000b Acquire, 001b Preempt, 010b Preempt and Abort.")
            )
        ),
        CommandSeed(
            id = 2013, opcode = "15h", name = "Reservation Release", commandSet = CommandSet.NVM_IO,
            summary = "Releases the reservation currently held by the requesting host, or clears the reservation and all registrants entirely.",
            description = """Either releases (ends) the reservation held by the requesting registrant - requiring the Reservation Type field to match the currently-held type - or, via the Clear action, removes the reservation and unregisters every registrant on the namespace, fully resetting reservation state.""",
            mandatory = "Optional (part of the optional Reservations capability)",
            source = "Base Spec 2.3 §7.7",
            fields = listOf(FieldSeed("RRELA (CDW10 02:00)", "000b Release, 001b Clear."))
        ),
        CommandSeed(
            id = 2014, opcode = "18h", name = "Cancel", commandSet = CommandSet.NVM_IO,
            summary = "Requests the controller abort one specific command, or all commands, submitted to the same I/O Submission Queue.",
            description = """Targets commands already submitted on the same SQ the Cancel command itself is submitted to; it may target a single command by Command Identifier (Single Command Cancel) or every other outstanding command on that SQ for a given namespace (Multiple Command Cancel). Aborting an Admin command instead requires the Abort command.""",
            mandatory = "Optional",
            source = "Base Spec 2.3 §7.1",
            fields = listOf(
                FieldSeed("CID/SQID (CDW10)", "Command Identifier and Submission Queue Identifier of the target."),
                FieldSeed("ACODE (CDW11 01:00)", "00b Single Command Cancel, 01b Multiple Command Cancel.")
            )
        ),
        CommandSeed(
            id = 2015, opcode = "12h", name = "I/O Management Receive", commandSet = CommandSet.NVM_IO,
            summary = "Retrieves I/O-management information from the controller (currently: Flexible Data Placement Reclaim Unit Handle status).",
            description = """Behavior is entirely determined by the Management Operation (MO) field. The only currently-defined non-vendor operation, Reclaim Unit Handle Status, returns one Reclaim Unit Handle Status Descriptor per Reclaim Group for each Placement Handle accessible to the namespace - part of the Flexible Data Placement (FDP) feature.""",
            mandatory = "Optional (tied to the optional Flexible Data Placement feature)",
            source = "Base Spec 2.3 §7.3",
            fields = listOf(FieldSeed("MO (CDW10 07:00)", "00h No action, 01h Reclaim Unit Handle Status, FFh Vendor specific."))
        ),
        CommandSeed(
            id = 2016, opcode = "1Dh", name = "I/O Management Send", commandSet = CommandSet.NVM_IO,
            summary = "Sends I/O-management instructions to the controller (currently: Flexible Data Placement Reclaim Unit Handle updates).",
            description = """Like I/O Management Receive, behavior is dictated by the Management Operation field. The defined operation, Reclaim Unit Handle Update, supplies a list of Placement Identifiers and asks the controller to re-point each one at an empty Reclaim Unit - used to explicitly retire/rotate FDP Reclaim Units.""",
            mandatory = "Optional (tied to the optional Flexible Data Placement feature)",
            source = "Base Spec 2.3 §7.4",
            fields = listOf(FieldSeed("MO (CDW10 07:00)", "00h No action, 01h Reclaim Unit Handle Update, FFh Vendor specific."))
        )
    )
}
