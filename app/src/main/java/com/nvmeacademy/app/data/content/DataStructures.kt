package com.nvmeacademy.app.data.content

/**
 * The precise, byte/Dword-level data structures that make up every Submission
 * Queue Entry and Completion Queue Entry, transcribed from Base Spec 2.3 §4.1
 * and §4.2 (Figures 91-100). This is the structural complement to the
 * per-command Dword10-15 usage already covered in the Command Reference -
 * here you look up "what does Command Dword 0 look like for every command",
 * not "what does Get Log Page put in Dword 10".
 */
object AllDataStructures {
    val structures: List<DataStructureSeed> = listOf(
        DataStructureSeed(
            id = 9001,
            order = 1,
            category = "SQE",
            name = "Command Dword 0 (CDW0)",
            summary = "The first 4 bytes of every Submission Queue Entry - Admin, I/O, or Fabrics - defined identically for (almost) every command.",
            source = "Base Spec 2.3 §4.1, Figure 91",
            fields = listOf(
                StructureFieldSeed("07:00", "Opcode (OPC)", "The command's opcode. Bits 07:02 are the Function (FN) field; bits 01:00 are the Data Transfer Direction (DTD): 00b no data, 01b host-to-controller, 10b controller-to-host, 11b bidirectional."),
                StructureFieldSeed("09:08", "Fused Operation (FUSE)", "00b Normal operation, 01b First command of a fused operation, 10b Second command of a fused operation, 11b Reserved."),
                StructureFieldSeed("13:10", "Reserved", "-"),
                StructureFieldSeed("15:14", "PRP or SGL for Data Transfer (PSDT)", "00b PRPs used. 01b SGLs used, Metadata Pointer (MPTR) is a single contiguous buffer address. 10b SGLs used, MPTR is an address of an SGL segment containing exactly one SGL descriptor. 11b Reserved. PRPs are required for Admin commands over PCIe; SGLs are required for all commands over Fabrics."),
                StructureFieldSeed("31:16", "Command Identifier (CID)", "A host-assigned tag, unique among a Submission Queue's currently outstanding commands, used with the SQ Identifier to match a completion back to its command. FFFFh should not be used - the Error Information log page reserves it to mean 'no associated command'.")
            )
        ),
        DataStructureSeed(
            id = 9002,
            order = 2,
            category = "SQE",
            name = "Common Command Format (Admin & I/O)",
            summary = "The full 64-byte Submission Queue Entry layout shared by every Admin command and every I/O command across all I/O Command Sets.",
            source = "Base Spec 2.3 §4.1, Figure 92",
            fields = listOf(
                StructureFieldSeed("03:00", "Command Dword 0 (CDW0)", "Common to all commands - see the 'Command Dword 0' structure."),
                StructureFieldSeed("07:04", "Namespace Identifier (NSID)", "The namespace this command applies to; 0h if unused. FFFFFFFFh is a command-dependent broadcast value (scope varies - all attached namespaces, all namespaces in the subsystem, etc.). An inactive NSID aborts with Invalid Field in Command; an invalid NSID aborts with Invalid Namespace or Format, unless the command specifies otherwise."),
                StructureFieldSeed("11:08", "Command Dword 2 (CDW2)", "Command-specific Dword 2 (most commands leave this reserved)."),
                StructureFieldSeed("15:12", "Command Dword 3 (CDW3)", "Command-specific Dword 3 (most commands leave this reserved)."),
                StructureFieldSeed("23:16", "Metadata Pointer (MPTR)", "Present only when metadata isn't interleaved with user data. If PSDT=00b: a dword-aligned physical buffer address. If PSDT=01b: a physical buffer address (alignment per the Identify Controller MBA bit). If PSDT=10b: a qword-aligned address of an SGL segment containing exactly one SGL descriptor."),
                StructureFieldSeed("39:24", "Data Pointer (DPTR)", "If PSDT=00b: PRP Entry 1 (bytes 31:24, the first PRP entry or a PRP List pointer) and PRP Entry 2 (bytes 39:32, the second page's base address, or a PRP List pointer if the transfer spans more than one page boundary). If PSDT=01b/10b: SGL Entry 1, the first SGL segment for the command."),
                StructureFieldSeed("43:40", "Command Dword 10 (CDW10)", "Command-specific."),
                StructureFieldSeed("47:44", "Command Dword 11 (CDW11)", "Command-specific."),
                StructureFieldSeed("51:48", "Command Dword 12 (CDW12)", "Command-specific."),
                StructureFieldSeed("55:52", "Command Dword 13 (CDW13)", "Command-specific."),
                StructureFieldSeed("59:56", "Command Dword 14 (CDW14)", "Command-specific."),
                StructureFieldSeed("63:60", "Command Dword 15 (CDW15)", "Command-specific.")
            )
        ),
        DataStructureSeed(
            id = 9003,
            order = 3,
            category = "SQE",
            name = "Common Command Format - Vendor Specific (Optional)",
            summary = "An alternate 64-byte layout some controllers support for vendor-specific commands, adding explicit transfer-length dwords in place of CDW10/CDW11.",
            source = "Base Spec 2.3 §4.1, Figure 93",
            fields = listOf(
                StructureFieldSeed("03:00", "Command Dword 0 (CDW0)", "Same as the common format."),
                StructureFieldSeed("07:04", "Namespace Identifier (NSID)", "0h if unused; FFFFFFFFh applies the command to all namespaces attached to the controller unless otherwise specified. Behavior on an inactive NSID is vendor specific."),
                StructureFieldSeed("15:08", "Reserved", "-"),
                StructureFieldSeed("39:16", "Metadata and Data Pointers (MDPTR)", "Same definition as MPTR/DPTR in the Common Command Format."),
                StructureFieldSeed("43:40", "Number of Dwords in Data Transfer (NDT)", "Number of dwords in the data transfer."),
                StructureFieldSeed("47:44", "Number of Dwords in Metadata Transfer (NDM)", "Number of dwords in the metadata transfer."),
                StructureFieldSeed("51:48", "Command Dword 12 (CDW12)", "Command-specific."),
                StructureFieldSeed("55:52", "Command Dword 13 (CDW13)", "Command-specific."),
                StructureFieldSeed("59:56", "Command Dword 14 (CDW14)", "Command-specific."),
                StructureFieldSeed("63:60", "Command Dword 15 (CDW15)", "Command-specific.")
            )
        ),
        DataStructureSeed(
            id = 9004,
            order = 4,
            category = "SQE",
            name = "Fabrics Command - Submission Queue Entry",
            summary = "The SQE layout used for every Fabrics command (Connect, Property Get/Set, Authentication, Disconnect) - Opcode is always fixed to 7Fh.",
            source = "Base Spec 2.3 §4.1, Figure 94/95",
            fields = listOf(
                StructureFieldSeed("07:00", "Opcode (OPC)", "Always 7Fh - specifies a Fabrics command."),
                StructureFieldSeed("09:08", "Fused Operation (FUSE)", "Always 00b - there are no fused Fabrics commands."),
                StructureFieldSeed("15:14", "PRP or SGL for Data Transfer (PSDT)", "00b No Data Transferred (or SGLs used if data is transferred - a host should prefer 10b for that case). 01b Reserved. 10b Data Transferred, SGLs used."),
                StructureFieldSeed("31:16", "Command Identifier (CID)", "Same definition as the common Command Dword 0."),
                StructureFieldSeed("04 (of byte 04)", "Fabrics Command Type (FCTYPE)", "Selects which Fabrics command this capsule carries (Connect, Property Get/Set, Authentication Send/Receive, Disconnect) - bits 07:02 are a Function sub-field, bits 01:00 are the Data Transfer Direction, same encoding as OPC."),
                StructureFieldSeed("23:05", "Reserved", "-"),
                StructureFieldSeed("39:24", "SGL Descriptor 1 (SGL1)", "A Transport SGL Data Block or Keyed SGL Data Block descriptor describing the entire data transfer; reserved if the Fabrics command doesn't transfer data."),
                StructureFieldSeed("63:40", "Fabrics Command Type Specific (FCTS)", "Fabrics-command-type-specific fields (e.g. the Connect command's QID, SQSIZE, KATO).")
            )
        ),
        DataStructureSeed(
            id = 9005,
            order = 5,
            category = "CQE",
            name = "Common Completion Queue Entry Layout",
            summary = "The 16-byte completion structure posted for every Admin and I/O command, across all I/O Command Sets.",
            source = "Base Spec 2.3 §4.2, Figure 96/97/98",
            fields = listOf(
                StructureFieldSeed("Bytes 03:00 (DW0)", "Command Specific", "Meaning depends on which command is completing; reserved if the command doesn't use it."),
                StructureFieldSeed("Bytes 07:04 (DW1)", "Command Specific", "Meaning depends on which command is completing; reserved if the command doesn't use it."),
                StructureFieldSeed("31:16 (DW2)", "SQ Identifier (SQID)", "The Submission Queue this completion's command was issued to - lets a host tell completions apart when multiple SQs share one CQ. Reserved over NVMe over Fabrics."),
                StructureFieldSeed("15:00 (DW2)", "SQ Head Pointer (SQHD)", "The controller's SQ Head pointer at the moment this completion was created - tells the host which SQ slots are now free for reuse. May lag the controller's true current state by the time the host reads it."),
                StructureFieldSeed("31:17 (DW3)", "Status (STATUS)", "The command's completion status - see the 'Completion Queue Entry: Status Field' structure."),
                StructureFieldSeed("16 (DW3)", "Phase Tag (P)", "Flips value each time the controller posts a new entry into this slot, letting the host detect new completions without polling a register. Reserved over NVMe over Fabrics."),
                StructureFieldSeed("15:00 (DW3)", "Command Identifier (CID)", "The tag the host assigned at submission; combined with SQ Identifier, uniquely identifies the completing command. Up to 65,535 requests may be outstanding on one SQ at a time.")
            )
        ),
        DataStructureSeed(
            id = 9006,
            order = 6,
            category = "CQE",
            name = "Fabrics Response - Completion Queue Entry",
            summary = "The 16-byte completion structure posted for Fabrics commands, replacing SQ Identifier/Phase Tag with a Fabrics-specific layout.",
            source = "Base Spec 2.3 §4.2, Figure 99",
            fields = listOf(
                StructureFieldSeed("07:00", "Fabrics Response Type Specific (FRTS)", "Meaning depends on the Fabrics response type."),
                StructureFieldSeed("09:08", "SQ Head Pointer (SQHD)", "Same meaning as the common CQE; reserved if SQ flow control is disabled for this queue pair."),
                StructureFieldSeed("11:10", "Reserved", "-"),
                StructureFieldSeed("13:12", "Command Identifier (CID)", "Identifies the completing command."),
                StructureFieldSeed("15:01 (of bytes 15:14)", "Status (STATUS)", "Same Status field definition as the common CQE."),
                StructureFieldSeed("00 (of bytes 15:14)", "Reserved", "-")
            )
        ),
        DataStructureSeed(
            id = 9007,
            order = 7,
            category = "Status",
            name = "Completion Queue Entry - Status Field",
            summary = "The sub-structure inside CQE Dword 3 that reports success, error category, and retry guidance for a completed command.",
            source = "Base Spec 2.3 §4.2.3, Figure 100/101",
            fields = listOf(
                StructureFieldSeed("31", "Do Not Retry (DNR)", "If set, resubmitting the identical command is expected to fail again. If clear, a retry may succeed."),
                StructureFieldSeed("30", "More (M)", "If set, more detail about this error is available via the Error Information log page (Get Log Page)."),
                StructureFieldSeed("29:28", "Command Retry Delay (CRD)", "If DNR is clear and the host enabled Advanced Command Retry (ACRE), selects a suggested retry-wait time from Identify Controller: 00b immediate, 01b/10b/11b select CRDT1/CRDT2/CRDT3."),
                StructureFieldSeed("27:25", "Status Code Type (SCT)", "Which status table to consult: 0h Generic Command Status, 1h Command Specific Status, 2h Media and Data Integrity Errors, 3h Path Related Status, 4h-6h Reserved, 7h Vendor Specific."),
                StructureFieldSeed("24:17", "Status Code (SC)", "The specific status within the selected table - e.g. under Generic Command Status: 00h Successful Completion, 01h Invalid Command Opcode, 02h Invalid Field in Command, 03h Command ID Conflict, 04h Data Transfer Error, 06h Internal Error, 08h Command Aborted due to SQ Deletion, 0Bh Invalid Namespace or Format, and more.")
            )
        )
    )
}
