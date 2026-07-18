package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/** NVMe-MI Command Set (MI Spec 2.1 §5) and PCIe Command Set (§7) reference entries. */
object MiCommands {
    val list: List<CommandSeed> = listOf(
        CommandSeed(
            id = 4001, opcode = "00h", name = "Read NVMe-MI Data Structure", commandSet = CommandSet.MI,
            summary = "Returns one of several discoverable data structures (NVM Subsystem Information, Port Information, Controller List, Controller Information, ...) that describe the NVM Subsystem, a port, or a Controller.",
            description = """The primary discovery command in NVMe-MI - how a Management Controller learns what a device is and what it supports before issuing anything else. The Data Structure Type (DTYP) field selects which structure to return: 00h NVM Subsystem Information, 01h Port Information, 02h Controller List, 03h Controller Information, 04h Optionally Supported Command List, 05h Management Endpoint Buffer Command Support List.""",
            mandatory = "Mandatory for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §5, Figure 68/69",
            fields = listOf(
                FieldSeed("DTYP", "Data Structure Type selector."),
                FieldSeed("PORTID / CTRLID", "Port Identifier and Controller Identifier scoping the request.")
            )
        ),
        CommandSeed(
            id = 4002, opcode = "01h", name = "NVM Subsystem Health Status Poll", commandSet = CommandSet.MI,
            summary = "Efficiently reports a summary \"composite\" health/status change indicator across all Controllers in the NVM Subsystem, optionally clearing the change flags.",
            description = """Lets a Management Controller (e.g., a BMC) poll a single lightweight command instead of querying every Controller individually. Returns Composite Controller Status Flags aggregating per-Controller health changes (critical warning, available spare, temperature change, firmware activated, etc.). The Clear Status (CS) bit controls whether flags are copied and then atomically cleared.""",
            mandatory = "Mandatory for NVMe Storage Device; Optional for NVMe Enclosure",
            source = "MI Spec 2.1 §5, Figure 68/69",
            fields = listOf(FieldSeed("CS (Dword1 bit 31)", "Clear Status."))
        ),
        CommandSeed(
            id = 4003, opcode = "02h", name = "Controller Health Status Poll", commandSet = CommandSet.MI,
            summary = "Returns a filtered list of per-Controller Health data structures based on selection criteria such as function type and changed-flag state.",
            description = """Where the NVM Subsystem Health Status Poll gives a composite summary, this drills into individual Controllers. Report All (ALL) can force all Controllers to be returned; selection can be limited by error-condition bits and function-type filters (SR-IOV Virtual/Physical Functions, PCI Functions). Maximum Response Entries and a Starting Controller ID allow paging through large Controller lists.""",
            mandatory = "Mandatory for NVMe Storage Device; Optional for NVMe Enclosure",
            source = "MI Spec 2.1 §5, §5.3.1",
            fields = listOf(
                FieldSeed("ALL / MAXRENT / SCTLID (Dword0)", "Report All, Maximum Response Entries, Starting Controller ID."),
                FieldSeed("CCF (Dword1)", "Clear Changed Flags.")
            )
        ),
        CommandSeed(
            id = 4004, opcode = "03h", name = "Configuration Set", commandSet = CommandSet.MI,
            summary = "Writes/updates one of four (plus vendor-specific) named Configuration Identifiers on the Responder: SMBus/I2C Frequency, Health Status Change, MCTP Transmission Unit Size, or Asynchronous Event configuration.",
            description = """The general-purpose "write a setting" command in NVMe-MI. The Configuration Identifier (CID) selects which configuration is being written: 01h SMBus/I2C Frequency, 02h Health Status Change, 03h MCTP Transmission Unit Size, 04h Asynchronous Event (arms/disarms AEM reporting - central to the BMC "subscribe to interrupts instead of polling" use case).""",
            mandatory = "Mandatory for NVMe Storage Device and NVMe Enclosure at the command level (per-identifier support varies)",
            source = "MI Spec 2.1 §5, Figure 75",
            fields = listOf(FieldSeed("CID (Dword0 07:00)", "Configuration Identifier."))
        ),
        CommandSeed(
            id = 4005, opcode = "04h", name = "Configuration Get", commandSet = CommandSet.MI,
            summary = "Reads the current value of one of the named Configuration Identifiers (mirror image of Configuration Set).",
            description = """Used to read back configuration state, most commonly to discover the currently negotiated MCTP Transmission Unit Size or which Asynchronous Events are currently enabled (used during BMC AEM setup). A Requester should not use this for the Health Status Change identifier - those bits are read via the Health Status Poll commands instead.""",
            mandatory = "Mandatory for NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §5",
            fields = listOf(FieldSeed("CID (Dword0 07:00)", "Configuration Identifier."))
        ),
        CommandSeed(
            id = 4006, opcode = "05h", name = "VPD Read", commandSet = CommandSet.MI,
            summary = "Reads a byte range of the Vital Product Data (VPD) / FRU information stored in the FRU Information Device.",
            description = """VPD is manufacturing/asset-tracking data (serial number, part number, manufacturer, etc.) formatted per the IPMI FRU Information Storage Definition. Lets a Management Controller retrieve all or part of this data over the out-of-band mechanism. Data Offset and Data Length select the byte range.""",
            mandatory = "Mandatory for NVMe Storage Device; Optional for NVMe Enclosure",
            source = "MI Spec 2.1 §5",
            fields = listOf(
                FieldSeed("DOFST (Dword0)", "Data Offset."),
                FieldSeed("DLEN (Dword1)", "Data Length.")
            )
        ),
        CommandSeed(
            id = 4007, opcode = "06h", name = "VPD Write", commandSet = CommandSet.MI,
            summary = "Writes/updates a byte range of the Vital Product Data in the FRU Information Device.",
            description = """Used during manufacturing or field service to program or update FRU/asset data. DOFST/DLEN specify where and how much; the data to write comes in the Request Data field. The spec recommends VPD be updatable at least 8 times.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §5",
            fields = listOf(FieldSeed("DOFST / DLEN", "Data Offset and Data Length."))
        ),
        CommandSeed(
            id = 4008, opcode = "07h", name = "Reset", commandSet = CommandSet.MI,
            summary = "Initiates a reset of the NVM Subsystem (the only currently defined Reset Type is \"Reset NVM Subsystem\").",
            description = """The out-of-band equivalent of toggling the NVM Subsystem Reset mechanism, letting a BMC reset a misbehaving device without host cooperation. Notably, on successful completion the device does NOT send a Success Response - the reset itself replaces the response. Best practice is to send Shutdown first to gracefully stop all Controllers.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure (Mandatory only if NSSR is supported)",
            source = "MI Spec 2.1 §5",
            fields = listOf(FieldSeed("RSTTYP (Dword0 31:24)", "Reset Type; only 00h Reset NVM Subsystem is defined."))
        ),
        CommandSeed(
            id = 4009, opcode = "08h", name = "SES Receive", commandSet = CommandSet.MI,
            summary = "Retrieves a SCSI Enclosure Services (SES-4) \"status type\" diagnostic page (e.g., enclosure status, element status) - the read half of enclosure management.",
            description = """NVMe-MI reuses the SES-4 standard's diagnostic page model for managing enclosure elements like power supplies, fans, and slot indicators. The Page Code field selects which SES status diagnostic page to retrieve; Allocation Length caps the returned size.""",
            mandatory = "Prohibited for NVMe Storage Device; Mandatory for NVMe Enclosure",
            source = "MI Spec 2.1 §5",
            fields = listOf(
                FieldSeed("PCODE (Dword0 07:00)", "Page Code."),
                FieldSeed("ALENGTH (Dword1)", "Allocation Length.")
            )
        ),
        CommandSeed(
            id = 4010, opcode = "09h", name = "SES Send", commandSet = CommandSet.MI,
            summary = "Transfers a SES-4 \"control type\" diagnostic page to the Enclosure Services Process to modify enclosure state (e.g., turn on a fault LED, power off a slot).",
            description = """The write/actuation half of enclosure management, mirroring the SCSI SEND DIAGNOSTIC command. Unlike SES Receive, the page code is embedded inside the control-page payload itself rather than in a dedicated field. Data Length specifies the Request Data size.""",
            mandatory = "Prohibited for NVMe Storage Device; Mandatory for NVMe Enclosure",
            source = "MI Spec 2.1 §5",
            fields = listOf(FieldSeed("DLEN (Dword1)", "Data Length."))
        ),
        CommandSeed(
            id = 4011, opcode = "0Ah", name = "Management Endpoint Buffer Read", commandSet = CommandSet.MI,
            summary = "Reads back the contents of the optional Management Endpoint Buffer, a device-side staging buffer used to move data larger than one NVMe-MI Message.",
            description = """Some Response Data (large SES pages, large data structures) can exceed the maximum size of a single NVMe-MI Message (4,224 bytes). The Management Endpoint Buffer is a scratch buffer letting a Requester page through large payloads using Data Offset/Data Length windows.""",
            mandatory = "Optional for NVMe Storage Device; Mandatory for NVMe Enclosure whenever a buffer is implemented",
            source = "MI Spec 2.1 §5",
            fields = listOf(
                FieldSeed("DOFST (Dword0)", "Data Offset."),
                FieldSeed("DLEN (Dword1)", "Data Length.")
            )
        ),
        CommandSeed(
            id = 4012, opcode = "0Bh", name = "Management Endpoint Buffer Write", commandSet = CommandSet.MI,
            summary = "Writes data into the optional Management Endpoint Buffer, used to stage large Request Data before it's consumed by another command.",
            description = """Because a single NVMe-MI Message is capped at 4,224 bytes, commands needing larger payloads (e.g., a big SES Send control page) can be preceded by one or more Management Endpoint Buffer Write commands that assemble the full payload in the buffer.""",
            mandatory = "Optional for NVMe Storage Device; Mandatory for NVMe Enclosure whenever a buffer is implemented",
            source = "MI Spec 2.1 §5",
            fields = listOf(
                FieldSeed("DOFST (Dword0)", "Data Offset."),
                FieldSeed("DLEN (Dword1)", "Data Length.")
            )
        ),
        CommandSeed(
            id = 4013, opcode = "0Ch", name = "Shutdown", commandSet = CommandSet.MI,
            summary = "Initiates a graceful (or abrupt) shutdown of all NVMe Controllers in the NVM Subsystem from a single Management Endpoint.",
            description = """Lets a BMC or management controller quiesce all Controllers before power removal or a subsequent Reset, without host OS involvement. Shutdown Type selects Normal NVM Subsystem Shutdown (00h, graceful notification sequence) or Abrupt. The command completes successfully only once all Controllers report shutdown-complete.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §5",
            fields = listOf(FieldSeed("SHDNTYP (Dword0 31:24)", "Shutdown Type: 00h Normal NVM Subsystem Shutdown."))
        ),
        CommandSeed(
            id = 4101, opcode = "00h", name = "PCIe Configuration Read", commandSet = CommandSet.MI_PCIE,
            summary = "Reads a byte range from the PCIe configuration space (4 KiB) of a targeted NVMe Controller.",
            description = """Lets a Management Controller inspect standard/extended PCI configuration registers of a Controller out-of-band, useful for diagnostics or inventory without host cooperation. Response Data is always rounded up to a whole number of dwords, zero-padded if Length isn't dword-aligned.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure (effectively required if any other PCIe Command is supported)",
            source = "MI Spec 2.1 §7, Figure 148",
            fields = listOf(
                FieldSeed("Length", "Bytes to read."),
                FieldSeed("Offset", "Offset into the 4 KiB configuration space.")
            )
        ),
        CommandSeed(
            id = 4102, opcode = "01h", name = "PCIe Configuration Write", commandSet = CommandSet.MI_PCIE,
            summary = "Writes a byte range into the PCIe configuration space of a targeted NVMe Controller.",
            description = """Companion write command to PCIe Configuration Read; used out-of-band to modify PCI configuration registers without host OS involvement. Offset+Length combinations outside the 4 KiB config space produce an Invalid Parameter Error Response.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §7",
            fields = listOf(FieldSeed("Length / Offset", "Same addressing model as PCIe Configuration Read."))
        ),
        CommandSeed(
            id = 4103, opcode = "02h", name = "PCIe Memory Read", commandSet = CommandSet.MI_PCIE,
            summary = "Reads a byte range from PCIe memory space (BAR-mapped) of a targeted NVMe Controller, with a full 64-bit offset.",
            description = """Lets a Management Controller peek at memory-mapped registers/structures behind a Controller's PCI Base Address Register (BAR) entirely out-of-band. The BAR field selects which BAR (0h-5h) is targeted; the offset spans two Dwords to support 64-bit BARs.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §7",
            fields = listOf(
                FieldSeed("BAR", "Selects BAR 0h-5h (offsets 10h-24h)."),
                FieldSeed("Offset Lower/Upper", "64-bit offset into the BAR.")
            )
        ),
        CommandSeed(
            id = 4104, opcode = "03h", name = "PCIe Memory Write", commandSet = CommandSet.MI_PCIE,
            summary = "Writes a byte range into PCIe memory space (BAR-mapped) of a targeted NVMe Controller (64-bit offset), the write counterpart to PCIe Memory Read.",
            description = """Same BAR/64-bit-offset addressing model as PCIe Memory Read, but transfers Request Data into the target's BAR-mapped memory rather than returning Response Data. Implementations may block specific address ranges (returns Access Denied) for security.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §7"
        ),
        CommandSeed(
            id = 4105, opcode = "04h", name = "PCIe I/O Read", commandSet = CommandSet.MI_PCIE,
            summary = "Reads a byte range from PCIe I/O space (BAR-mapped) of a targeted NVMe Controller.",
            description = """For legacy PCI I/O-space BARs, the out-of-band equivalent of an I/O port read. BAR field selects which BAR is being accessed; Offset is a single dword (I/O space doesn't need the 64-bit split memory space uses).""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §7"
        ),
        CommandSeed(
            id = 4106, opcode = "05h", name = "PCIe I/O Write", commandSet = CommandSet.MI_PCIE,
            summary = "Writes a byte range into PCIe I/O space (BAR-mapped) of a targeted NVMe Controller, the write counterpart to PCIe I/O Read.",
            description = """Same BAR/Offset/Length addressing as PCIe I/O Read but transfers Request Data into the target I/O BAR range instead of returning data.""",
            mandatory = "Optional for both NVMe Storage Device and NVMe Enclosure",
            source = "MI Spec 2.1 §7"
        )
    )
}
