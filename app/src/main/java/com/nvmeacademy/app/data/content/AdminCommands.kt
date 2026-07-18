package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/**
 * Admin Command Set reference entries (Base Spec 2.3 §5.2/§5.3/§5.4).
 * Mandatory/Optional/Prohibited values are given primarily for the I/O controller
 * role (the common SSD case); Administrative/Discovery-controller differences are
 * noted in the mandatory field where they diverge.
 */
object AdminCommands {
    val list: List<CommandSeed> = listOf(
        CommandSeed(
            id = 1001, opcode = "08h", name = "Abort", commandSet = CommandSet.ADMIN,
            summary = "Requests that the controller abort a specific previously-submitted command.",
            description = """The Abort command asks the controller to cancel a command identified by its Submission Queue Identifier and Command Identifier, which may target either the Admin Submission Queue or an I/O Submission Queue. The controller is not guaranteed to succeed - the target command may already be complete, mid-execution, or too deep in the pipeline to cancel, so an Abort is best-effort, not a guarantee. If the abort happens before the target command's completion is posted ("immediate abort"), the controller must ensure no side effects of that command occur after the Abort's own completion; otherwise the abort is "deferred" and happens later. The Identify Controller data structure reports an Abort Command Limit (ACL) capping how many Abort commands may be outstanding at once.""",
            mandatory = "Mandatory (I/O controller); Optional (Administrative, Discovery)",
            source = "Base Spec 2.3 §5.2.1",
            fields = listOf(
                FieldSeed("CID (CDW10 31:16)", "Command Identifier of the command to abort."),
                FieldSeed("SQID (CDW10 15:00)", "Submission Queue Identifier the target command was submitted to."),
                FieldSeed("IANP (CQE Dword0 bit 0)", "Immediate Abort Not Performed flag.")
            )
        ),
        CommandSeed(
            id = 1002, opcode = "0Ch", name = "Asynchronous Event Request", commandSet = CommandSet.ADMIN,
            summary = "Arms the controller to asynchronously notify the host of status, error, and health events.",
            description = """A host keeps one or more Asynchronous Event Request (AER) commands outstanding on the Admin Submission Queue with no timeout; the controller only completes one when it has an event to report. Events are grouped into types - Error, SMART/Health status, Notice, Immediate, One-Shot, I/O Command specific status, and Vendor specific - and each type (except immediate/one-shot) is auto-masked after being reported until the host clears it, typically by reading the associated log page via Get Log Page. The number of AERs a host may keep outstanding is capped by the Asynchronous Event Request Limit in Identify Controller, and all outstanding AERs are silently aborted on a controller reset.""",
            mandatory = "Mandatory (I/O controller); mandatory if certain features implemented (Administrative); conditional (Discovery)",
            source = "Base Spec 2.3 §5.2.2",
            fields = listOf(
                FieldSeed("LID (CQE Dword0 23:16)", "Log Page Identifier to read to clear the event."),
                FieldSeed("AEI (CQE Dword0 15:08)", "Asynchronous Event Information."),
                FieldSeed("AET (CQE Dword0 02:00)", "Asynchronous Event Type: 000b Error, 001b SMART/Health, 010b Notice, 011b Immediate, 100b One-Shot, 110b I/O Command specific.")
            )
        ),
        CommandSeed(
            id = 1003, opcode = "20h", name = "Capacity Management", commandSet = CommandSet.ADMIN,
            summary = "Creates or deletes Endurance Groups and NVM Sets, or selects a pre-defined capacity configuration.",
            description = """Lets a host carve up NVM subsystem capacity into Endurance Groups and, within them, NVM Sets, either by picking one of the controller's pre-built "Capacity Configuration Descriptors" (Fixed Capacity Management) or by directly specifying a byte capacity for a new Endurance Group/NVM Set (Variable Capacity Management). Deleting an Endurance Group cascades to delete all NVM Sets and namespaces contained within it. This is a data-center-oriented capacity-provisioning command rather than something used in everyday I/O.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.3",
            fields = listOf(
                FieldSeed("Operation (CDW10 03:00)", "0h Select Capacity Configuration, 1h Create Endurance Group, 2h Delete Endurance Group, 3h Create NVM Set, 4h Delete NVM Set."),
                FieldSeed("Element Identifier (CDW10 31:16)", "Meaning depends on Operation."),
                FieldSeed("Capacity Lower/Upper (CDW11/CDW12)", "32-bit capacity value for create operations.")
            )
        ),
        CommandSeed(
            id = 1004, opcode = "45h", name = "Controller Data Queue", commandSet = CommandSet.ADMIN,
            summary = "Manages controller-posted data queues in host memory used to deliver specific types of controller-generated data.",
            description = """A management-operation-style command: its Select field chooses between "Create Controller Data Queue" and "Delete Controller Data Queue". The queue it manages is a host-memory buffer the controller uses to post specific categories of data proactively, separate from normal Completion Queue traffic. Works together with the Controller Data Queue Feature (FID 21h) and its associated one-shot asynchronous event.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.4",
            fields = listOf(FieldSeed("Select (CDW10 07:00)", "0h Create, 1h Delete, C0h-FFh Vendor Specific."))
        ),
        CommandSeed(
            id = 1005, opcode = "14h", name = "Device Self-test", commandSet = CommandSet.ADMIN,
            summary = "Starts, or aborts, a device self-test diagnostic operation on the controller and/or specified namespaces.",
            description = """Triggers one of several built-in diagnostics: a short self-test, an extended self-test, a Host-Initiated Refresh, a vendor-specific test, or aborts a self-test already in progress. The Namespace Identifier field controls scope - 0h tests only the controller, an individual NSID tests that namespace plus the controller, FFFFFFFFh tests all attached namespaces. Results are recorded in the Device Self-test log page (LID 06h).""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.5",
            fields = listOf(FieldSeed("Self-test Code (CDW10 03:00)", "1h short, 2h extended, 3h Host-Initiated Refresh, Eh vendor specific, Fh abort."))
        ),
        CommandSeed(
            id = 1006, opcode = "1Ah", name = "Directive Receive", commandSet = CommandSet.ADMIN,
            summary = "Retrieves a data buffer associated with a specific Directive Type (e.g., Streams, Data Placement) from the controller.",
            description = """The "read" counterpart to Directive Send in NVMe's generic Directives framework, used to query controller-side state for a supported Directive Type such as Identify (00h), Streams (01h), Data Placement (02h), or vendor-specific (0Fh). The Directive Type and a per-type Directive Operation are encoded in Command Dword 11 alongside an optional Directive-Specific value; the actual data returned depends on both fields.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.6",
            fields = listOf(
                FieldSeed("NUMD (CDW10)", "Number of Dwords, 0's based."),
                FieldSeed("DTYPE (CDW11 15:08)", "Directive Type: 00h Identify, 01h Streams, 02h Data Placement, 0Fh Vendor Specific."),
                FieldSeed("DOPER (CDW11 07:00)", "Directive Operation, meaning is per-DTYPE.")
            )
        ),
        CommandSeed(
            id = 1007, opcode = "19h", name = "Directive Send", commandSet = CommandSet.ADMIN,
            summary = "Transfers a data buffer that configures or acts on a specific Directive Type on the controller.",
            description = """The "write" counterpart to Directive Receive, sharing the identical Command Dword 10/11 layout. Its effect is entirely defined by the Directive Type - for Streams this enables/disables streams or releases stream identifiers; for Data Placement it configures placement-related parameters. Because both Directive commands share one generic envelope, a new Directive Type doesn't require a new Admin opcode.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.7",
            fields = listOf(
                FieldSeed("NUMD (CDW10)", "Number of Dwords."),
                FieldSeed("DTYPE (CDW11 15:08)", "Directive Type."),
                FieldSeed("DOPER (CDW11 07:00)", "Directive Operation.")
            )
        ),
        CommandSeed(
            id = 1008, opcode = "10h", name = "Firmware Commit", commandSet = CommandSet.ADMIN,
            summary = "Commits a previously-downloaded firmware image to a slot and optionally schedules or triggers its activation.",
            description = """Known as "Firmware Activate" before spec revision 1.2, this is the second half of the firmware update flow: Commit Action says what to do with the staged image - merely place it, place and activate at next Controller Level Reset, activate the existing image at next reset, or place-and-activate immediately. Can also target Boot Partitions. All controllers sharing a domain share firmware slots, so activation affects every controller in that domain at once.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.8",
            fields = listOf(
                FieldSeed("Commit Action (CDW10 05:03)", "000b place only, 001b place+activate at next reset, 010b activate existing image at next reset, 011b place+activate immediately, 110b/111b Boot Partition variants."),
                FieldSeed("Firmware Slot (CDW10 02:00)", "0h = controller chooses.")
            )
        ),
        CommandSeed(
            id = 1009, opcode = "11h", name = "Firmware Image Download", commandSet = CommandSet.ADMIN,
            summary = "Downloads all or part of a new firmware image into the controller ahead of a later Firmware Commit.",
            description = """Transfers one dword-aligned chunk of a firmware image at a time, identified by an Offset and a Number of Dwords; pieces may be submitted out of order (except for Boot Partition updates) as long as ranges don't overlap. The image is only staged, not activated - a subsequent Firmware Commit performs activation. The Firmware Update Granularity (FWUG) in Identify Controller specifies alignment requirements.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.9",
            fields = listOf(
                FieldSeed("NUMD (CDW10)", "Number of Dwords, 0's based."),
                FieldSeed("OFST (CDW11)", "Dword offset into the overall image, 0h for the first piece.")
            )
        ),
        CommandSeed(
            id = 1010, opcode = "80h", name = "Format NVM", commandSet = CommandSet.ADMIN,
            summary = "Performs a low-level format of NVM media, optionally combined with a secure erase, changing the active LBA data/metadata format.",
            description = """Re-initializes the media backing one namespace, all namespaces attached to the controller, or (depending on FNS/SENS) all namespaces in the subsystem. After a successful Format, the controller must never return user data that existed before the format. Options include User Data Erase (SES=001b) or Cryptographic Erase (SES=010b, erases the encryption key rather than raw bits). The LBA Format to apply is selected via LBAFL/LBAFU, indexing into the I/O Command Set's LBA Format table.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.10",
            fields = listOf(
                FieldSeed("LBAFL/LBAFU (CDW10)", "Selects target LBA Format index."),
                FieldSeed("SES (CDW10 11:09)", "Secure Erase Settings: 000b none, 001b User Data Erase, 010b Cryptographic Erase."),
                FieldSeed("PI/PIL/MSET (CDW10)", "Protection Information settings and Metadata Settings.")
            )
        ),
        CommandSeed(
            id = 1011, opcode = "0Ah", name = "Get Features", commandSet = CommandSet.ADMIN,
            summary = "Reads the current, default, saved, or supported-capabilities value of a named controller/namespace Feature.",
            description = """Retrieves the attributes of the specified Feature Identifier (FID), with Select choosing between Current (000b), Default (001b), Saved (010b), and Supported Capabilities (011b, reports changeable/saveable/namespace-scoped instead of the value). Depending on the FID, the returned attribute is either encoded directly in Dword 0 of the CQE (simple numeric features) or transferred through a Data Pointer buffer for larger structures (Host Memory Buffer, Autonomous Power State Transition). Shares the same Feature Identifier table with Set Features.""",
            mandatory = "Mandatory (I/O controller); Optional or mandatory-if-implemented (Administrative); conditional (Discovery)",
            source = "Base Spec 2.3 §5.2.11",
            fields = listOf(
                FieldSeed("Select (CDW10 10:08)", "000b Current, 001b Default, 010b Saved, 011b Supported Capabilities."),
                FieldSeed("FID (CDW10 07:00)", "Feature Identifier, e.g. 01h Arbitration, 02h Power Management, 07h Number of Queues, 08h Interrupt Coalescing, 0Dh Host Memory Buffer, 0Eh Timestamp, 0Fh Keep Alive Timer.")
            )
        ),
        CommandSeed(
            id = 1012, opcode = "02h", name = "Get Log Page", commandSet = CommandSet.ADMIN,
            summary = "Retrieves a named log page (error history, SMART/health, telemetry, commands-supported, etc.) from the controller.",
            description = """Returns a data buffer for the requested Log Page Identifier (LID), with amount/position controlled by Number of Dwords Lower/Upper and a Log Page Offset. A Retain Asynchronous Event (RAE) bit controls whether reading the page clears the associated outstanding AER. Dozens of standardized log pages exist: Supported Log Pages (00h), Error Information (01h), SMART/Health Information (02h), Firmware Slot Information (03h), Commands Supported and Effects (05h), Device Self-test (06h), Telemetry (07h/08h), Endurance Group Information (09h), Asymmetric Namespace Access (0Ch), Persistent Event Log (0Dh), and Discovery-specific pages (70h/71h) for Fabrics.""",
            mandatory = "Mandatory",
            source = "Base Spec 2.3 §5.2.12",
            fields = listOf(
                FieldSeed("LID (CDW10 07:00)", "Log Page Identifier."),
                FieldSeed("RAE (CDW10 bit 15)", "Retain Asynchronous Event."),
                FieldSeed("NUMDL/NUMDU (CDW10/CDW11)", "Number of Dwords Lower/Upper."),
                FieldSeed("Log Page Offset (CDW12/CDW13)", "Byte offset (or index) into the log.")
            )
        ),
        CommandSeed(
            id = 1013, opcode = "06h", name = "Identify", commandSet = CommandSet.ADMIN,
            summary = "Returns a 4 KiB data structure describing the NVM subsystem, domain, controller, or a namespace, selected by the CNS field.",
            description = """The primary discovery command in NVMe - nearly every other capability a host relies on is learned via Identify with the appropriate Controller or Namespace Structure (CNS) value. Common CNS values: 00h Identify Namespace, 01h Identify Controller, 02h Active Namespace ID list, 03h Namespace Identification Descriptor list (UUID/EUI64/NGUID), 05h/06h I/O Command Set specific Identify Namespace/Controller, 08h I/O Command Set Independent Identify Namespace. Other values cover NVM Set, Controller, Domain, Endurance Group, and Secondary Controller lists.""",
            mandatory = "Mandatory",
            source = "Base Spec 2.3 §5.2.13",
            fields = listOf(
                FieldSeed("CNS (CDW10 07:00)", "Controller or Namespace Structure selector."),
                FieldSeed("CNTID (CDW10 31:16)", "Controller Identifier, used by some CNS values."),
                FieldSeed("CSI (CDW11 31:24)", "Command Set Identifier, used by some CNS values.")
            )
        ),
        CommandSeed(
            id = 1014, opcode = "18h", name = "Keep Alive", commandSet = CommandSet.ADMIN,
            summary = "Restarts the controller's Keep Alive Timer to signal the host connection is still active.",
            description = """Has no command-specific fields at all - its only purpose is to reset the Keep Alive Timer so the controller knows the host/association is still alive, which matters most for NVMe over Fabrics where losing a transport connection should be detectable even without in-flight I/O. Paired with the Keep Alive Timer Feature (FID 0Fh) which configures the timeout interval.""",
            mandatory = "Mandatory if the Keep Alive Timer feature is supported (I/O and Administrative controllers); conditional (Discovery)",
            source = "Base Spec 2.3 §5.2.14",
            fields = listOf(FieldSeed("(none)", "All of CDW10-15 are reserved."))
        ),
        CommandSeed(
            id = 1015, opcode = "24h", name = "Lockdown", commandSet = CommandSet.ADMIN,
            summary = "Prohibits or allows execution of a specific Admin command opcode or Set Features Feature Identifier on specified interfaces.",
            description = """Implements the Command and Feature Lockdown security capability, letting an authorized entity restrict which commands/Features may be executed and on which interface (Admin SQ, out-of-band Management Endpoint, or both). The Scope field determines whether the target means an Admin opcode, a Set Features FID, an NVMe-MI opcode, or a PCIe Command Set opcode. Only lockdown-capable commands/Features can actually be prohibited. Effects apply NVM-subsystem-wide.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.15",
            fields = listOf(
                FieldSeed("OFI (CDW10 15:08)", "Opcode or Feature Identifier."),
                FieldSeed("Scope (CDW10 03:00)", "0h Admin opcode, 2h Set Features FID, 3h MI Command Set opcode, 4h PCIe Command Set opcode."),
                FieldSeed("PRHBT (CDW10 bit 04)", "Prohibit (1) or allow (0)."),
                FieldSeed("Interface (CDW10 06:05)", "00b Admin SQ, 01b Admin SQ + Mgmt Endpoint, 10b Mgmt Endpoint only.")
            )
        ),
        CommandSeed(
            id = 1016, opcode = "42h", name = "Migration Receive", commandSet = CommandSet.ADMIN,
            summary = "Retrieves controller-migration state data (e.g., a Controller State data structure) to support live migration of a controller.",
            description = """Used by a host managing controller migration to pull state describing a specified controller (by CNTLID) out of the controller processing the command. Its defined operation, "Get Controller State," returns a Controller State data structure whose format is selected via a Controller State Version Index. If the target controller is suspended for the entire read, the data is guaranteed internally consistent; otherwise fields may reflect different points in time.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.16",
            fields = listOf(
                FieldSeed("Select (CDW10 07:00)", "0h Get Controller State."),
                FieldSeed("CNTLID (CDW11 15:00)", "Controller Identifier to query."),
                FieldSeed("Offset / NUMD (CDW12/13, CDW15)", "Paging offset and length for the returned state data.")
            )
        ),
        CommandSeed(
            id = 1017, opcode = "41h", name = "Migration Send", commandSet = CommandSet.ADMIN,
            summary = "Suspends, resumes, or sets the state of a controller to support live migration.",
            description = """Drives the controller-migration lifecycle with three operations: Suspend (stop the target controller from fetching new commands), Resume (bring a suspended controller back to normal operation), and Set Controller State (push a previously-retrieved Controller State structure into a target controller). Suspend has a two-phase type: a "Suspend Notification" warning, and an actual Suspend. A suspended controller remains suspended until a matching Resume or a Controller Level Reset.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.17",
            fields = listOf(
                FieldSeed("Select (CDW10 07:00)", "0h Suspend, 1h Resume, 2h Set Controller State."),
                FieldSeed("Suspend Type / CNTLID (CDW11)", "0h Suspend Notification, 1h Suspend; target controller ID.")
            )
        ),
        CommandSeed(
            id = 1018, opcode = "1Eh", name = "NVMe-MI Receive", commandSet = CommandSet.ADMIN,
            summary = "Receives data associated with the NVMe Management Interface command set, tunneled through the Admin Submission Queue.",
            description = """The Base Specification only lists this opcode and defers its full command format to the NVM Express Management Interface Specification. It lets a host (or in-band agent) retrieve NVMe-MI Command Set responses without a separate out-of-band Management Endpoint, tunneling the NVMe-MI protocol over the normal Admin Submission Queue.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.18",
            fields = listOf(FieldSeed("(defined in MI Spec)", "Full Dword layout is defined in the NVM Express Management Interface Specification, not the Base Spec."))
        ),
        CommandSeed(
            id = 1019, opcode = "1Dh", name = "NVMe-MI Send", commandSet = CommandSet.ADMIN,
            summary = "Sends data associated with the NVMe Management Interface command set, tunneled through the Admin Submission Queue.",
            description = """Reserved and named in the Base Spec but fully defined in the NVM Express Management Interface Specification. The "send" counterpart used to deliver NVMe-MI Command Set requests in-band via the Admin Submission Queue rather than through a dedicated out-of-band Management Endpoint (e.g., SMBus/I2C or PCIe VDM).""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.19",
            fields = listOf(FieldSeed("(defined in MI Spec)", "Full Dword layout is defined in the NVM Express Management Interface Specification."))
        ),
        CommandSeed(
            id = 1020, opcode = "15h", name = "Namespace Attachment", commandSet = CommandSet.ADMIN,
            summary = "Attaches or detaches one or more controllers to/from a namespace, making it visible/usable to those controllers.",
            description = """Follows namespace creation (Namespace Management) and exposes a namespace to specific controllers, using Select to choose Controller Attach (0h) or Controller Detach (1h) and a Controller List naming the targets. Attach/detach state persists across all reset events. Limits (MAXDNA, MAXCNA) can cause an attach to be rejected with a "Namespace Attachment Limit Exceeded" status. """,
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.20",
            fields = listOf(
                FieldSeed("Select (CDW10 03:00)", "0h Controller Attach, 1h Controller Detach."),
                FieldSeed("Data Pointer", "References a Controller List data structure naming the controllers.")
            )
        ),
        CommandSeed(
            id = 1021, opcode = "0Dh", name = "Namespace Management", commandSet = CommandSet.ADMIN,
            summary = "Creates or deletes namespaces, independent of attaching them to any controller.",
            description = """Select chooses Create (0h) or Delete (1h). On Create, the host clears NSID and the controller picks/returns an available Namespace ID; the data buffer's Command Set Identifier plus I/O-Command-Set-specific payload determines the namespace's I/O Command Set and attributes. Creating does not attach it to any controller - a subsequent Namespace Attachment is required. On Delete, NSID identifies the target, or FFFFFFFFh deletes every namespace in the subsystem.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.21",
            fields = listOf(
                FieldSeed("Select (CDW10 03:00)", "0h Create, 1h Delete."),
                FieldSeed("CSI (CDW11 31:24)", "Command Set Identifier, Create only; 0h = NVM Command Set."),
                FieldSeed("NSID", "Reserved/0h for Create; target NSID or FFFFFFFFh for Delete.")
            )
        ),
        CommandSeed(
            id = 1022, opcode = "84h", name = "Sanitize", commandSet = CommandSet.ADMIN,
            summary = "Starts (or recovers from a failed) NVM-subsystem-wide sanitize operation that irreversibly erases all user data.",
            description = """The subsystem-scope, security-grade erase command, supporting Block Erase, Crypto Erase, and Overwrite as reported in SANICAP of Identify Controller. Runs in the background - the command completes once the operation has started, not once it's finished; progress is tracked via the Sanitize Status log page. Options include No-Deallocate After Sanitize and an optional post-sanitize Media Verification state. Prohibited while any Persistent Memory Region is enabled or any namespace is write-protected.""",
            mandatory = "Optional (prohibited for Exported NVM Subsystems); Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.22",
            fields = listOf(
                FieldSeed("SANACT (CDW10 02:00)", "001b Exit Failure Mode, 010b Block Erase, 011b Overwrite, 100b Crypto Erase, 101b Exit Media Verification State."),
                FieldSeed("AUSE / NDAS / EMVS (CDW10)", "Allow Unrestricted Sanitize Exit, No-Deallocate After Sanitize, Enter Media Verification State."),
                FieldSeed("OWPASS / OVRPAT (CDW10/CDW11)", "Overwrite Pass Count and Pattern, Overwrite only.")
            )
        ),
        CommandSeed(
            id = 1023, opcode = "8Ch", name = "Sanitize Namespace", commandSet = CommandSet.ADMIN,
            summary = "Starts (or recovers from a failed) sanitize operation scoped to a single namespace, using Crypto Erase only.",
            description = """The namespace-scoped counterpart to Sanitize, supporting only the Crypto Erase action (deleting the namespace's encryption key rather than physically overwriting media). Requires a valid, active, non-FFFFFFFFh, non-0h NSID. The Maximum Namespace Sanitize Operations In Progress (MNSOIP) field bounds concurrent operations subsystem-wide. Runs in the background, like Sanitize, and is rejected on write-protected namespaces.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.23",
            fields = listOf(
                FieldSeed("SANACT (CDW10 02:00)", "001b Exit Failure Mode, 100b Crypto Erase, 101b Exit Media Verification State."),
                FieldSeed("NSID", "Target namespace; FFFFFFFFh/0h/inactive not allowed.")
            )
        ),
        CommandSeed(
            id = 1024, opcode = "82h", name = "Security Receive", commandSet = CommandSet.ADMIN,
            summary = "Retrieves the status and data result of one or more prior Security Send commands, per the specified Security Protocol.",
            description = """The read-back half of NVMe's pass-through security channel (used for TCG Opal/SIIS-style self-encrypting-drive protocols, RPMB, and similar); the meaning of transferred data is fully delegated to the SPC-5 Security Protocol identified. Security Protocol 00h is special-cased as discovery, returning supported protocols. Security Protocol EAh is reserved for NVMe use (RPMB, CDP Authentication). Data may not survive a communication loss or Controller Level Reset.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.24",
            fields = listOf(
                FieldSeed("SECP (CDW10 31:24)", "Security Protocol per SPC-5; 00h discovery, EAh NVMe-specific."),
                FieldSeed("SPSP0/SPSP1 (CDW10)", "SP Specific fields."),
                FieldSeed("AL (CDW11)", "Allocation Length.")
            )
        ),
        CommandSeed(
            id = 1025, opcode = "81h", name = "Security Send", commandSet = CommandSet.ADMIN,
            summary = "Transfers security-protocol-specific command/data payloads to the controller, per SPC-5.",
            description = """Delivers a Security-Protocol-specific request to the controller; the response, if any, is retrieved afterward with a matching Security Receive per the rules of that protocol. Its interpretation is almost entirely delegated to SPC-5. This pairing is the standard tunnel used for TCG self-encrypting-drive management, RPMB access, and CDP authentication on NVMe devices.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.25",
            fields = listOf(
                FieldSeed("SECP (CDW10 31:24)", "Security Protocol."),
                FieldSeed("TL (CDW11)", "Transfer Length.")
            )
        ),
        CommandSeed(
            id = 1026, opcode = "09h", name = "Set Features", commandSet = CommandSet.ADMIN,
            summary = "Sets the attributes of a named controller/namespace Feature, optionally persisting the value across power/reset.",
            description = """The write counterpart to Get Features, sharing the same Feature Identifier space, plus a Save (SV) bit letting the host request the new value persist across power cycles - only honored if the controller supports Save/Select and the Feature is saveable. Depending on the Feature, the new value is carried directly in Command Dword 11 or via a larger data structure through the Data Pointer. Some Features are namespace-scoped, some Endurance-Group- or NVM-Set-scoped, some controller-wide.""",
            mandatory = "Mandatory (I/O controller); Optional or mandatory-if-implemented (Administrative); conditional (Discovery)",
            source = "Base Spec 2.3 §5.2.26",
            fields = listOf(
                FieldSeed("SV (CDW10 bit 31)", "Save - persist across power cycle/reset."),
                FieldSeed("FID (CDW10 07:00)", "Feature Identifier, same table as Get Features.")
            )
        ),
        CommandSeed(
            id = 1027, opcode = "3Eh", name = "Track Receive", commandSet = CommandSet.ADMIN,
            summary = "Retrieves host-memory-change information that a Track Send command previously enabled tracking for.",
            description = """Its defined operation, "Tracked Memory Changes," reports which regions of host memory a specified controller has written to since tracking was enabled or last reported - useful for migration scenarios needing to know exactly which memory pages a source controller touched. The response is a header plus Tracked Memory Changed Descriptors, with More-To-Report and Suspended flags. A successful read clears the reported ranges.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.27",
            fields = listOf(
                FieldSeed("Select (CDW10 07:00)", "0h Tracked Memory Changes."),
                FieldSeed("CNTLID (CDW11 15:00)", "Target controller.")
            )
        ),
        CommandSeed(
            id = 1028, opcode = "3Dh", name = "Track Send", commandSet = CommandSet.ADMIN,
            summary = "Enables or manages tracking of controller-side information (e.g., host memory writes) for later retrieval via Track Receive.",
            description = """The enabling/configuration half of the Track Send/Receive pair, using a management-operation pattern via Select. Its primary use is starting tracking of host memory modifications made by a specified controller, so a subsequent Track Receive can report exactly what changed - most relevant to controller migration, avoiding re-transferring an entire memory image.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.2.28",
            fields = listOf(FieldSeed("Select (CDW10)", "Management operation selector; CDW11/Data Pointer usage depends on the selected operation."))
        ),
        CommandSeed(
            id = 1029, opcode = "05h", name = "Create I/O Completion Queue", commandSet = CommandSet.ADMIN,
            summary = "Creates an I/O Completion Queue in host (or Controller Memory Buffer) memory.",
            description = """Allocates and registers a new I/O Completion Queue - every I/O Completion Queue except the Admin Completion Queue is created this way. The host supplies a Queue Size, an unused Queue Identifier, an interrupt vector/enable bit, and whether the queue's memory is a single contiguous buffer (PC=1) or PRP-List-described (PC=0). Any I/O Submission Queue posting to this CQ must be created afterward.""",
            mandatory = "Mandatory for NVMe over PCIe (Prohibited for NVMe over Fabrics); Prohibited (Administrative, Discovery)",
            source = "Base Spec 2.3 §5.3.1",
            fields = listOf(
                FieldSeed("QSIZE (CDW10 31:16)", "Queue Size, 0's based."),
                FieldSeed("QID (CDW10 15:00)", "Queue Identifier."),
                FieldSeed("PC / IEN / IV (CDW11)", "Physically Contiguous, Interrupts Enabled, Interrupt Vector.")
            )
        ),
        CommandSeed(
            id = 1030, opcode = "01h", name = "Create I/O Submission Queue", commandSet = CommandSet.ADMIN,
            summary = "Creates an I/O Submission Queue in host (or Controller Memory Buffer) memory, bound to an existing I/O Completion Queue.",
            description = """Allocates a new I/O Submission Queue with a Queue Size and Queue Identifier, referencing an already-created I/O Completion Queue via CQID - referencing an invalid or nonexistent CQID aborts the command. Supports assigning a static Queue Priority class (used with Weighted Round Robin with Urgent arbitration) and optionally associating the SQ with a specific NVM Set.""",
            mandatory = "Mandatory for NVMe over PCIe (Prohibited for NVMe over Fabrics); Prohibited (Administrative, Discovery)",
            source = "Base Spec 2.3 §5.3.2",
            fields = listOf(
                FieldSeed("QSIZE/QID (CDW10)", "Queue Size and Queue Identifier."),
                FieldSeed("CQID (CDW11 31:16)", "Completion Queue Identifier to bind to."),
                FieldSeed("QPRIO (CDW11 02:01)", "00b Urgent, 01b High, 10b Medium, 11b Low."),
                FieldSeed("PC (CDW11 bit 00)", "Physically Contiguous.")
            )
        ),
        CommandSeed(
            id = 1031, opcode = "04h", name = "Delete I/O Completion Queue", commandSet = CommandSet.ADMIN,
            summary = "Deletes a previously-created I/O Completion Queue.",
            description = """Frees an I/O Completion Queue by Queue Identifier; the Admin Completion Queue (QID 0h) can never be targeted. Any I/O Submission Queue still associated with the target CQ must be deleted first, or the command is rejected with an "Invalid Queue Deletion" status. """,
            mandatory = "Mandatory for NVMe over PCIe (Prohibited for NVMe over Fabrics); Prohibited (Administrative, Discovery)",
            source = "Base Spec 2.3 §5.3.3",
            fields = listOf(FieldSeed("QID (CDW10 15:00)", "Queue Identifier of the Completion Queue to delete (0h not permitted)."))
        ),
        CommandSeed(
            id = 1032, opcode = "00h", name = "Delete I/O Submission Queue", commandSet = CommandSet.ADMIN,
            summary = "Deletes a previously-created I/O Submission Queue.",
            description = """Frees an I/O Submission Queue by Queue Identifier; the Admin Submission Queue (QID 0h) can never be targeted. On completion, every command previously submitted to that SQ is guaranteed to have been completed or implicitly completed with "Command Aborted due to SQ Deletion." Must be done before deleting the associated Completion Queue.""",
            mandatory = "Mandatory for NVMe over PCIe (Prohibited for NVMe over Fabrics); Prohibited (Administrative, Discovery)",
            source = "Base Spec 2.3 §5.3.4",
            fields = listOf(FieldSeed("QID (CDW10 15:00)", "Queue Identifier of the Submission Queue to delete (0h not permitted)."))
        ),
        CommandSeed(
            id = 1033, opcode = "7Ch", name = "Doorbell Buffer Config", commandSet = CommandSet.ADMIN,
            summary = "Configures host-memory Shadow Doorbell and EventIdx buffers used to mirror controller doorbell writes, mainly for emulated/virtualized controllers.",
            description = """Hands the controller two PRP-pointed, page-aligned host-memory buffers - a Shadow Doorbell buffer the host updates in place of ringing real doorbells, and an EventIdx buffer the controller updates to tell the host when to look at the shadow values. Purely an optimization for virtualized/emulated controllers to avoid a VM exit per doorbell write. Settings do not survive a Controller Level Reset.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.3.5",
            fields = listOf(
                FieldSeed("PRP Entry 1", "Base address of the Shadow Doorbell buffer."),
                FieldSeed("PRP Entry 2", "Base address of the EventIdx buffer.")
            )
        ),
        CommandSeed(
            id = 1034, opcode = "1Ch", name = "Virtualization Management", commandSet = CommandSet.ADMIN,
            summary = "Manages SR-IOV Flexible Resource allocation and Online/Offline state for secondary (virtual function) controllers.",
            description = """Supported only by primary controllers implementing Virtualization Enhancements. Lets a primary controller move Flexible Resources (Virtual Queue/Interrupt resources) between itself and secondary controllers, and manage secondary controller lifecycle: Offline, Assign resources to an Offline controller, or bring Online. Action selects among these; Resource Type distinguishes VQ vs VI.""",
            mandatory = "Optional; Prohibited for Discovery controllers",
            source = "Base Spec 2.3 §5.3.6",
            fields = listOf(
                FieldSeed("CNTLID (CDW10 31:16)", "Controller Identifier."),
                FieldSeed("RT (CDW10 10:08)", "Resource Type: 000b VQ, 001b VI."),
                FieldSeed("ACT (CDW10 03:00)", "1h Primary Flexible Allocation, 7h Secondary Offline, 8h Secondary Assign, 9h Secondary Online.")
            )
        ),
        CommandSeed(
            id = 1035, opcode = "28h", name = "Clear Exported NVM Resource Configuration", commandSet = CommandSet.ADMIN,
            summary = "Deletes all Exported NVM resource configuration (all Exported NVM Subsystems, Namespaces, and Ports) on a Fabric-side controller.",
            description = """Wipes an Exported NVM configuration entirely without affecting the Underlying Namespaces or Underlying NVM Subsystems that back them. Must be issued only after every host has disconnected from every Exported NVM Subsystem, otherwise rejected with a Command Sequence Error. Not supported by Exported NVM Subsystems themselves.""",
            mandatory = "Prohibited for standard I/O controllers; Optional for Administrative controllers",
            source = "Base Spec 2.3 §5.4.1"
        ),
        CommandSeed(
            id = 1036, opcode = "2Ah", name = "Create Exported NVM Subsystem", commandSet = CommandSet.ADMIN,
            summary = "Creates a new Exported NVM Subsystem, initially empty, with a chosen access-restriction mode.",
            description = """Creates a Fabric-presented NVM subsystem backed by "Underlying" resources, with either unrestricted access or restricted access (Allowed Host List only). The newly created subsystem starts with an empty allowed-host list and no linked namespaces; its generated Subsystem NQN is returned.""",
            mandatory = "Prohibited for standard I/O controllers; Optional for Administrative controllers",
            source = "Base Spec 2.3 §5.4.2",
            fields = listOf(FieldSeed("RA (CDW10 bit 08)", "Restricted Access: 0 unrestricted, 1 restrict to Allowed Host List."))
        ),
        CommandSeed(
            id = 1037, opcode = "38h", name = "Cross-Controller Reset", commandSet = CommandSet.ADMIN,
            summary = "Requests that the NVM subsystem initiate a Controller Level Reset on a different (\"Impacted\") controller than the one the command is submitted to.",
            description = """Useful when a host has lost communication with a controller directly but can still reach the subsystem through a different path. If the reset completes before the command's own completion, the Immediate Reset Successful bit is set; otherwise an entry is recorded in the Cross-Controller Reset log page and an async event follows.""",
            mandatory = "Optional for message-based (Fabrics) controllers, Prohibited for memory-based (PCIe) controllers; Optional for Discovery controllers supporting persistent connections",
            source = "Base Spec 2.3 §5.4.3",
            fields = listOf(FieldSeed("ICID (CDW10 15:00)", "Impacted Controller ID."))
        ),
        CommandSeed(
            id = 1038, opcode = "21h", name = "Discovery Information Management", commandSet = CommandSet.ADMIN,
            summary = "Registers, de-registers, or updates host/NVM-subsystem discovery information entries on a Centralized or Direct Discovery Controller.",
            description = """A Task field selects Register (0h), De-register (1h), or Update (2h) of one or more discovery information entries, covering both host discovery entries and NVM subsystem discovery entries. Register/De-register can act on multiple entries at once; Update always operates atomically on exactly one entry.""",
            mandatory = "Mandatory for Centralized Discovery Controllers (CDCs); Prohibited for standard I/O and Administrative controllers",
            source = "Base Spec 2.3 §5.4.4",
            fields = listOf(FieldSeed("TAS (CDW10)", "Task: 0h Register, 1h De-register, 2h Update."))
        ),
        CommandSeed(
            id = 1039, opcode = "25h", name = "Fabric Zoning Lookup", commandSet = CommandSet.ADMIN,
            summary = "Looks up the key identifying a Fabric Zoning data structure held on a Centralized Discovery Controller.",
            description = """A Direct Discovery Controller uses this to resolve a Zoning Data Key for a zoning data structure on the CDC before retrieving it with Fabric Zoning Receive. Rejected if zoning is disabled, the ZoneGroup isn't accessible, the data structure is locked, or it doesn't exist.""",
            mandatory = "Mandatory for CDCs; Prohibited for standard I/O and Administrative controllers",
            source = "Base Spec 2.3 §5.4.5"
        ),
        CommandSeed(
            id = 1040, opcode = "22h", name = "Fabric Zoning Receive", commandSet = CommandSet.ADMIN,
            summary = "Reads (a fragment of) a Fabric Zoning data structure from a Centralized Discovery Controller.",
            description = """Transfers a chunk of a Zoning data structure identified by a Zoning Data Key or Transaction ID, starting at a byte offset. The completion's Last Fragment bit tells the requester whether more fragments remain. Implements, with Send/Lookup, the CDC-hosted Fabric Zoning database restricting which DDCs/hosts can see which zone groups.""",
            mandatory = "Mandatory for CDCs; Prohibited for standard I/O and Administrative controllers",
            source = "Base Spec 2.3 §5.4.6"
        ),
        CommandSeed(
            id = 1041, opcode = "29h", name = "Fabric Zoning Send", commandSet = CommandSet.ADMIN,
            summary = "Writes (a fragment of) a Fabric Zoning data structure to a Centralized Discovery Controller.",
            description = """The write counterpart to Fabric Zoning Receive, with a Last Fragment bit set on the final chunk of a multi-command upload. Used by DDCs (or the CDC's own management path) to push zoning configuration data into the CDC's zoning database.""",
            mandatory = "Mandatory for CDCs; Prohibited for standard I/O and Administrative controllers",
            source = "Base Spec 2.3 §5.4.7"
        ),
        CommandSeed(
            id = 1042, opcode = "31h", name = "Manage Exported Namespace", commandSet = CommandSet.ADMIN,
            summary = "Associates or disassociates an Exported Namespace ID with an Underlying Namespace and an Exported NVM Subsystem.",
            description = """Select chooses Associate Namespace (01h, links an Exported Namespace ID to an Underlying Namespace/Controller/Subsystem triple) or Disassociate Namespace (02h). Association alone does not attach the Exported Namespace to any controller - a separate Namespace Attachment is still required.""",
            mandatory = "Prohibited for standard I/O controllers; Optional for Administrative controllers",
            source = "Base Spec 2.3 §5.4.8",
            fields = listOf(FieldSeed("Select (CDW10 07:00)", "01h Associate Namespace, 02h Disassociate Namespace."))
        ),
        CommandSeed(
            id = 1043, opcode = "2Dh", name = "Manage Exported NVM Subsystem", commandSet = CommandSet.ADMIN,
            summary = "Deletes an Exported NVM Subsystem, or changes its access mode / allowed-host grants.",
            description = """Select chooses Delete (01h, rejected if it still has active controllers/namespaces/ports), Change Access Mode (02h), Grant Host Access (03h), or Revoke Host Access (04h). The primary lifecycle/ACL-management command for Exported NVM Subsystems in Fabric gateway/bridging deployments.""",
            mandatory = "Prohibited for standard I/O controllers; Optional for Administrative controllers",
            source = "Base Spec 2.3 §5.4.9",
            fields = listOf(FieldSeed("Select (CDW10 07:00)", "01h Delete, 02h Change Access Mode, 03h Grant Host Access, 04h Revoke Host Access."))
        ),
        CommandSeed(
            id = 1044, opcode = "35h", name = "Manage Exported Port", commandSet = CommandSet.ADMIN,
            summary = "Creates or deletes an Exported Port association between an Exported NVM Subsystem and an Underlying Port.",
            description = """Select chooses Create (01h, associates an Exported Port ID with an Underlying Port and a target Exported NVM Subsystem) or Delete (02h). Makes an Exported NVM Subsystem reachable through a specific physical/underlying NVMe-oF port and transport service ID.""",
            mandatory = "Prohibited for standard I/O controllers; Optional for Administrative controllers",
            source = "Base Spec 2.3 §5.4.10",
            fields = listOf(FieldSeed("Select (CDW10 07:00)", "01h Create, 02h Delete."))
        ),
        CommandSeed(
            id = 1045, opcode = "39h", name = "Send Discovery Log Page", commandSet = CommandSet.ADMIN,
            summary = "Pushes a discovery log page (Discovery, Host Discovery, or AVE Discovery) from a Centralized Discovery Controller to a pull-model Direct Discovery Controller.",
            description = """Used in the CDC-to-DDC direction to proactively deliver an updated discovery log page rather than waiting for polling. Only three log pages are permitted this way: Discovery (70h), Host Discovery (71h), AVE Discovery (72h); anything else is rejected with a "Not Allowed Log Page" status. """,
            mandatory = "Mandatory for CDCs; Prohibited for standard I/O and Administrative controllers",
            source = "Base Spec 2.3 §5.4.11",
            fields = listOf(FieldSeed("TLID (CDW10 07:00)", "Transferred Log Page Identifier: 70h Discovery, 71h Host Discovery, 72h AVE Discovery."))
        ),
        CommandSeed(
            id = 1046, opcode = "7Fh", name = "Fabrics Commands", commandSet = CommandSet.ADMIN,
            summary = "Dispatch opcode for the separate NVMe-over-Fabrics \"Fabrics Command Set\" (Connect, Property Get/Set, Disconnect, Authentication).",
            description = """Not a single command but an entry point into an entirely separate command set defined in Base Spec §6, used only over Fabrics transports (prohibited for NVMe over PCIe). A Fabrics Command Type sub-field distinguishes Property Set, Connect, Disconnect, Property Get, Authentication Send, and Authentication Receive - see the Fabrics chapter for those individual commands.""",
            mandatory = "Mandatory for the core subset (Connect, Property Get/Set) on Fabrics transports; Optional for Authentication/Disconnect; Prohibited for NVMe over PCIe",
            source = "Base Spec 2.3 §6"
        ),
        CommandSeed(
            id = 1047, opcode = "85h", name = "Load Program", commandSet = CommandSet.ADMIN,
            summary = "Loads a computational program onto the controller (Computational Programs capability).",
            description = """Figure 142 lists opcode 85h as "Load Program" but its detailed command format is deferred entirely to the NVM Express Computational Programs Command Set Specification, not the Base Spec itself. Part of the Computational Storage feature set, letting a host push executable program images to run on the controller against namespace data.""",
            mandatory = "Depends on the applicable I/O Command Set specification; Prohibited for standard Administrative/Discovery controllers",
            source = "Base Spec 2.3 Figure 142 (detail in NVM Express Computational Programs Command Set Specification)"
        ),
        CommandSeed(
            id = 1048, opcode = "86h", name = "Get LBA Status (Admin)", commandSet = CommandSet.ADMIN,
            summary = "Retrieves the status (e.g., deallocated vs. mapped, or potentially unrecoverable) of a range of LBAs in a namespace.",
            description = """Figure 142 references "NVM, ZNS" for this opcode - its full command format is defined in the NVM Command Set Specification. A "Get LBA Status Supported" (GLSS) capability bit in Identify Controller advertises support. See the NVM I/O Commands section for full field detail.""",
            mandatory = "Depends on the applicable I/O Command Set specification; does not support NSID = FFFFFFFFh",
            source = "Base Spec 2.3 Figure 142 (detail in NVM Command Set Specification §4.2)"
        ),
        CommandSeed(
            id = 1049, opcode = "88h", name = "Program Activation Management", commandSet = CommandSet.ADMIN,
            summary = "Activates or manages a previously loaded computational program (Computational Programs capability).",
            description = """Named in Figure 142 but its detailed format is deferred to the NVM Express Computational Programs Command Set Specification. The activation-management counterpart to Load Program in the Computational Storage feature set.""",
            mandatory = "Depends on the applicable I/O Command Set specification; Prohibited for standard Administrative/Discovery controllers",
            source = "Base Spec 2.3 Figure 142 (detail in NVM Express Computational Programs Command Set Specification)"
        ),
        CommandSeed(
            id = 1050, opcode = "89h", name = "Memory Range Set Management", commandSet = CommandSet.ADMIN,
            summary = "Manages memory range sets used by computational programs (Computational Programs capability).",
            description = """Deferred entirely to the NVM Express Computational Programs Command Set Specification. Presumably lets a host define/manage the memory ranges a loaded computational program is permitted to operate against.""",
            mandatory = "Depends on the applicable I/O Command Set specification; Prohibited for standard Administrative/Discovery controllers",
            source = "Base Spec 2.3 Figure 142 (detail in NVM Express Computational Programs Command Set Specification)"
        )
    )
}
