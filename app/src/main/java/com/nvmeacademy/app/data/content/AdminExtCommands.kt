package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/**
 * NVM-Command-Set-specific extensions layered onto Base Spec Admin commands
 * (mostly Identify CNS values), plus the one wholly new Admin command the
 * NVM Command Set Specification introduces (Get LBA Status).
 */
object AdminExtCommands {
    val list: List<CommandSeed> = listOf(
        CommandSeed(
            id = 2701, opcode = "86h (Admin)", name = "Get LBA Status", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Returns descriptors identifying either allocated LBAs or potentially-unrecoverable LBAs within a specified range of a namespace.",
            description = """The one wholly new Admin command the NVM Command Set defines. Requests information about LBAs in a range via the Action Type (ATYPE) field: 02h returns tracked-allocated LBAs (useful for minimizing data copied during migration/snapshot); 10h/11h scan for, or return already-tracked, Potentially Unrecoverable LBAs - blocks likely to fail with Unrecovered Read Error, so the host can proactively recover and rewrite them.""",
            mandatory = "Optional (Get LBA Status Supported bit in Identify Controller; ATYPE values 02h/10h/11h are each individually optional)",
            source = "NVM Command Set Spec §4.2/§4.2.1",
            fields = listOf(
                FieldSeed("SLBA (CDW10-11)", "64-bit Starting LBA."),
                FieldSeed("MNDW (CDW12)", "Maximum Number of Dwords to return, 0's-based."),
                FieldSeed("ATYPE (CDW13 31:24)", "02h Return Allocated LBAs, 10h/11h Potentially Unrecoverable LBA scan/report."),
                FieldSeed("RL (CDW13 15:00)", "Range Length in LBAs, 0h = to end of namespace.")
            )
        ),
        CommandSeed(
            id = 2702, opcode = "06h (CNS 01h ext.)", name = "Identify Controller - NVM Command Set Fields", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "NVM-Command-Set-specific fields layered into the standard Identify Controller data structure (CNS 01h), e.g. atomic write unit sizes.",
            description = """Rather than a separate command, a set of controller-wide capability fields added to the common Identify Controller data structure when accessed via the NVM Command Set: Atomic Write Unit Normal (AWUN) and Atomic Write Unit Power Fail (AWUPF) describe write sizes guaranteed atomic during normal operation and during a power/error event; Atomic Compare & Write Unit (ACWU) bounds the size of a Compare-and-Write fused operation that can be treated atomically.""",
            mandatory = "AWUN/AWUPF Mandatory; ACWU Optional/conditional on fused Compare-and-Write support",
            source = "NVM Command Set Spec §4.1.5.2",
            fields = listOf(
                FieldSeed("AWUN", "Atomic Write Unit Normal, 0's-based logical-block count."),
                FieldSeed("AWUPF", "Atomic Write Unit Power Fail; must be <= AWUN."),
                FieldSeed("ACWU", "Atomic Compare & Write Unit.")
            )
        ),
        CommandSeed(
            id = 2703, opcode = "06h (CNS 05h)", name = "I/O Command Set Specific Identify Namespace (by Format Index)", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Returns per-Format-Index namespace capability fields (e.g. protection info options, size limits) for a given LBA Format, independent of any specific namespace instance.",
            description = """Lets a host query the capabilities associated with a particular Format Index (as opposed to a live namespace) - fields flagged "Reported = Yes" are populated identically for every namespace using that Format Index. The Format Index to query is passed via the CNS Specific Identifier (FIDX) in Command Dword 11.""",
            mandatory = "Conditional (valid Format Index required; individual field M/O per underlying figure)",
            source = "NVM Command Set Spec §4.1.5.3",
            fields = listOf(FieldSeed("FIDX (CDW11 15:00)", "Format Index to query."))
        ),
        CommandSeed(
            id = 2704, opcode = "06h (CNS 06h)", name = "I/O Command Set Specific Identify Controller Data Structure", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Controller-wide NVM-Command-Set-specific capability structure - size limits for Verify, Write Zeroes, Write Uncorrectable, Dataset Management, plus Copy Descriptor Formats Supported, LBA Migration Queue Format, etc.",
            description = """The primary per-controller capability structure for the NVM Command Set, returned by Identify with CNS=06h and CSI=00h. Reports optional-command size limits (Verify Size Limit, Write Zeroes Size Limit and its Deallocate variant, Dataset Management Ranges/Range-Size/Size Limits), whether those limits are hard caps or soft recommendations, and other command-set-wide parameters.""",
            mandatory = "Field-dependent (each field carries its own O/M designation)",
            source = "NVM Command Set Spec §4.1.5.4",
            fields = listOf(
                FieldSeed("VSL", "Verify Size Limit - max/recommended Verify data size."),
                FieldSeed("WZSL", "Write Zeroes Size Limit.")
            )
        ),
        CommandSeed(
            id = 2705, opcode = "06h (CNS 09h)", name = "NVM Command Set Identify Namespace Data Structure", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Returns the standard (I/O-Command-Set-independent) Identify Namespace data structure's fields as they apply to a given Format Index rather than a live namespace.",
            description = """Complementary to the CNS 05h structure: returns the common Identify Namespace data structure populated for a specified Format Index (via the same FIDX field) instead of an actual namespace - used by hosts probing format capabilities before creating a namespace with that format.""",
            mandatory = "Conditional (valid only for a valid Format Index)",
            source = "NVM Command Set Spec §4.1.5.5",
            fields = listOf(FieldSeed("FIDX (CDW11 15:00)", "Format Index to query."))
        ),
        CommandSeed(
            id = 2706, opcode = "06h (CNS 0Ah)", name = "Identify I/O Command Set Specific Namespace (by Format Index)", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Like CNS 06h's controller structure but scoped to a Format Index - returns I/O-Command-Set-specific namespace capability fields for that format rather than for a live namespace.",
            description = """Returns the I/O Command Set specific Identify Namespace data structure for the NVM Command Set for the Format Index specified via CDW11, rather than for an actual namespace. Used together with CNS 09h to fully characterize a candidate LBA Format prior to formatting/creating a namespace with it.""",
            mandatory = "Conditional (valid Format Index required)",
            source = "NVM Command Set Spec §4.1.5.6",
            fields = listOf(FieldSeed("FIDX (CDW11 15:00)", "Format Index to query."))
        ),
        CommandSeed(
            id = 2707, opcode = "06h (CNS 11h)", name = "Identify Namespace Data Structure for an Allocated NSID", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Returns the common Identify Namespace data structure for any allocated NSID, even one not currently attached/active to this controller.",
            description = """Distinct from the "live" Identify Namespace (CNS 00h), which typically only reports data for namespaces attached to the requesting controller. CNS 11h returns data for any allocated NSID in the subsystem (zero-filled if merely unallocated, aborted if outright invalid or FFFFFFFFh), letting a host enumerate/inspect namespaces it hasn't attached yet.""",
            mandatory = "Conditional (behavior branches on allocated vs. unallocated vs. invalid NSID)",
            source = "NVM Command Set Spec §4.1.5.7"
        ),
        CommandSeed(
            id = 2708, opcode = "06h (CNS 16h)", name = "Namespace Granularity List", commandSet = CommandSet.NVM_ADMIN_EXT,
            summary = "Reports preferred namespace-size and namespace-capacity allocation granularities so a host can create namespaces that fully utilize allocated capacity.",
            description = """Returns up to 16 (or 64, if LBAFEE is enabled) Namespace Granularity Descriptors, each giving a Namespace Size Granularity (NSG) and Namespace Capacity Granularity (NCG) in bytes. If a Namespace Management create request's NSZE/NCAP are exact multiples of these granularities (and NSZE=NCAP), the resulting namespace is considered "fully provisioned". """,
            mandatory = "Optional (only present if controller supports reporting Namespace Granularity)",
            source = "NVM Command Set Spec §4.1.5.8"
        )
    )
}
