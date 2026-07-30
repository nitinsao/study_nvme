package com.nvmeacademy.app.data.content

/** Part 6: NVMe Management Interface (NVMe-MI) - out-of-band management. */
object Part6NvmeMi {
    val part = PartSeed(
        id = 6,
        order = 6,
        title = "Part 6 · NVMe-MI",
        subtitle = "Out-of-band management interface",
        chapters = listOf(
            ChapterSeed(
                id = 601, partId = 6, order = 1,
                title = "What is NVMe-MI? Out-of-Band Management Overview",
                shortDescription = "Managing hardware when you can't trust or don't have a host OS",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "A sibling spec for management, not I/O",
                    bullets = listOf(
                        "NVMe-MI is a sibling spec to the NVM Express Base Specification - part of the \"NVMe Family of Specifications\" alongside I/O Command Set specs and the Boot Specification.",
                        "Solves a real problem: the host OS/driver stack can be dead, hung, or absent, but a BMC still needs to check drive health, read asset tags, or reset the device.",
                        "Two distinct mechanisms: (1) out-of-band - a Management Controller (e.g., BMC) talks to the device over a side channel (SMBus/I2C/I3C or PCIe VDM) independent of the host; (2) in-band tunneling - the host wraps NVMe-MI commands inside NVMe Admin Commands \"NVMe-MI Send\"/\"NVMe-MI Receive\".",
                        "A device can implement out-of-band only, in-band tunneling only, or both.",
                        "Two device categories are managed: the \"NVMe Storage Device\" (the SSD itself) and the \"NVMe Enclosure\" (the chassis/backplane holding one or more drives) - many commands have different mandatory/optional/prohibited rules for each.",
                        "Key capabilities: discovery, health/temperature monitoring, VPD (asset data) read/write, enclosure element control (fans, power, LEDs via SES), and secure out-of-band access that preserves data-at-rest security."
                    ),
                    notes = """NVMe-MI's scope statement frames it as defining "an architecture and command set for out-of-band and in-band management of an NVMe Storage Device as well as an architecture and mechanisms for monitoring and controlling the elements of an NVMe Enclosure." For Storage Devices this includes discovery/capability negotiation, storing host-environment data for later query, health/temperature monitoring, multiple concurrent commands (so a slow command can't block monitoring), a host-agnostic out-of-band mechanism, a standard VPD format, and preserving data-at-rest security. For Enclosures it adds discovering enclosures/capabilities, managing/sensing enclosure elements (power supplies, cooling, displays, indicators), and discovering which storage devices occupy which enclosure slots.""",
                    source = "MI Spec 2.1 §1 (Introduction)",
                    diagram = ChapterDiagramSeed(
                        caption = "Two management paths, same device",
                        connector = "none",
                        steps = listOf(
                            DiagramStepSeed("Out-of-Band", "BMC <-> MCTP <-> Device"),
                            DiagramStepSeed("In-Band Tunneling", "Host -> NVMe-MI Send/Receive")
                        )
                    )
                ))
            ),
            ChapterSeed(
                id = 602, partId = 6, order = 2,
                title = "MCTP Transport & Physical Layer",
                shortDescription = "SMBus/I2C, PCIe VDM, and message size limits",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Two physical layers, one transport protocol",
                    bullets = listOf(
                        "NVMe-MI messages ride on top of MCTP (Management Component Transport Protocol) - a standard transport that itself can run over multiple physical links.",
                        "Two physical layers defined: PCI Express (MCTP over PCIe Vendor Defined Messages, \"VDM\") and 2-Wire (SMBus/I2C, optionally I3C).",
                        "PCIe VDM path: a PCIe port hosting a Management Endpoint must support MCTP-over-PCIe-VDM; the endpoint is normally tied to PCIe Function 0.",
                        "2-Wire path: default is SMBus mode after power-on; can negotiate up to I3C for higher speed/lower power.",
                        "Fixed 2-Wire addresses matter: FRU Information Device at A6h/A4h, 2-Wire Management Endpoint at 3Ah, 2-Wire Mux at E8h, legacy Basic Management Command at D4h.",
                        "Message size limits: max NVMe-MI Message out-of-band is 4,224 bytes (4 KiB + 128 B) split across MCTP packets; in-band tunneling instead caps at the NVMe Maximum Data Transfer Size."
                    ),
                    notes = """The physical layer is the concrete "wiring" story underneath everything else. PCIe VDM reuses the same physical link the host uses for I/O, layering MCTP messages as vendor-defined messages - attractive when a BMC has PCIe visibility but no separate management fabric. The 2-Wire (SMBus/I2C/I3C) path is the classic "sideband" seen in servers and JBOFs: a dedicated low-speed bus wired to a BMC that keeps working even if the PCIe link or host is down. Understanding this chapter is prerequisite to everything else because message size limits (4,224 B for out-of-band vs MDTS for in-band) directly explain why big structures like SES pages or large data structures need the Management Endpoint Buffer commands as a workaround.""",
                    source = "MI Spec 2.1 §2, §3",
                    diagram = ChapterDiagramSeed(
                        caption = "Out-of-band transport path",
                        steps = listOf(
                            DiagramStepSeed("Management Controller", "e.g. a BMC"),
                            DiagramStepSeed("MCTP"),
                            DiagramStepSeed("PCIe VDM", "or SMBus/I2C/I3C"),
                            DiagramStepSeed("Management Endpoint")
                        )
                    )
                ))
            ),
            ChapterSeed(
                id = 603, partId = 6, order = 3,
                title = "Message Servicing Model",
                shortDescription = "Requests, Responses, and Asynchronous Event Messages",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "The protocol grammar",
                    bullets = listOf(
                        "Every exchange is Requester -> Responder: out-of-band it's Management Controller -> Management Endpoint; in-band it's host -> NVMe Controller.",
                        "Three message categories: Request Messages, Response Messages, and Asynchronous Event Messages (AEMs).",
                        "A Request Message is either a Command Message (an NVMe-MI Command, an NVMe Admin Command, or a PCIe Command) or a Control Primitive.",
                        "A Response Message is either a Success Response or an Error Response, carrying a Status field plus a command-specific Response Body.",
                        "AEMs are one-way, Management-Endpoint-initiated notifications sent after Asynchronous Events occur (out-of-band only), tied to the Asynchronous Event Configuration Identifier (04h).",
                        "Command Slots let a Management Endpoint service multiple outstanding commands concurrently rather than serializing everything behind one long-latency operation."
                    ),
                    notes = """The NMIMT (NVMe-MI Message Type) field is what lets one transport carry four very different command families: Control Primitive, MI Command, tunneled NVMe Admin Command, or PCIe Command. Response Messages always echo a Status field that distinguishes plain success from various error conditions. The AEM model is what turns NVMe-MI from a pure-polling model into an interrupt-driven one - a BMC arms specific events once, then only wakes up when the device pushes an AEM, instead of continuously polling health status. Command Slots exist to keep a slow/blocking command from starving a concurrent health poll - directly traceable to the spec's stated goal of preventing a long-latency command from blocking monitoring operations.""",
                    source = "MI Spec 2.1 §4"
                ))
            ),
            ChapterSeed(
                id = 604, partId = 6, order = 4,
                title = "NVMe-MI Command Set Walkthrough",
                shortDescription = "13 opcodes grouped into five functional buckets",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Discovery, configuration, asset data, lifecycle, enclosure I/O",
                    bullets = listOf(
                        "13 defined opcodes (00h-0Ch) plus reserved (0Dh-BFh) and vendor-specific (C0h-FFh) ranges.",
                        "Discovery & health: Read NVMe-MI Data Structure (00h), NVM Subsystem Health Status Poll (01h), Controller Health Status Poll (02h).",
                        "Configuration: Configuration Set (03h) / Get (04h) - four Configuration Identifiers.",
                        "Asset data: VPD Read (05h) / Write (06h) - Field-Replaceable-Unit info.",
                        "Lifecycle control: Reset (07h), Shutdown (0Ch) - out-of-band power/reset control independent of host.",
                        "Enclosure management: SES Receive (08h) / Send (09h) - reuse SCSI Enclosure Services-4 diagnostic pages.",
                        "Large-transfer helper: Management Endpoint Buffer Read (0Ah) / Write (0Bh) - stage data too big for one NVMe-MI Message.",
                        "Mandatory/Optional/Prohibited status differs sharply between NVMe Storage Device and NVMe Enclosure."
                    ),
                    notes = """The command set groups cleanly into five functional buckets: discovery/health (00h-02h), configuration (03h-04h), asset data (05h-06h), lifecycle (07h, 0Ch), and enclosure I/O (08h-09h, plus buffer helpers 0Ah-0Bh). The M/O/P matrix is the single most load-bearing table for anyone implementing or auditing a device: a bare SSD must support Read NVMe-MI Data Structure, both health polls, both Configuration commands, and VPD Read, but must NOT implement SES Receive/Send - while an "NVMe Enclosure" must support SES Receive/Send, Configuration Set/Get, and the buffer commands, but health polls and VPD are only Optional. A device that is simultaneously an Enclosure and a Storage Device must satisfy both columns' mandatory sets.""",
                    source = "MI Spec 2.1 §5, Figure 68/69"
                ))
            ),
            ChapterSeed(
                id = 605, partId = 6, order = 5,
                title = "Enclosure Management & SES Commands",
                shortDescription = "Reusing SCSI Enclosure Services-4 for chassis control",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Byte-for-byte SES pages over a new transport",
                    bullets = listOf(
                        "NVMe-MI doesn't reinvent enclosure management - it adopts SCSI Enclosure Services-4 (SES-4) diagnostic pages wholesale and just changes the transport.",
                        "SES Send = the SCSI SEND DIAGNOSTIC equivalent - pushes a \"control type\" page to change enclosure state (fan speed, LED state, power a slot on/off).",
                        "SES Receive = the SCSI RECEIVE DIAGNOSTIC RESULTS equivalent - pulls a \"status type\" page with sensor/health/fault data.",
                        "An \"Enclosure Services Process,\" logically part of the NVMe Enclosure, actually services SES Send/Receive requests and maintains state.",
                        "NVMe-MI keeps one single global state for the whole enclosure regardless of which Requester or path issued the command.",
                        "Each enclosure slot has its own SES element so software can address/identify individual drive bays."
                    ),
                    notes = """This chapter matters because it explains why NVMe-MI didn't invent a bespoke enclosure protocol: SES-4 already solved chassis management for SCSI/SAS enclosures with a mature page format for power supplies, cooling devices, displays, and per-slot status/control - reusing it means BMC vendors already familiar with SES tooling for SAS backplanes can largely reuse that knowledge for NVMe backplanes. SES Receive/Send are Prohibited on a bare NVMe Storage Device and Mandatory on an NVMe Enclosure, reflecting that "enclosure" is the actual chassis/backplane role, not the drive itself. Because SES pages can be large, both commands require Management-Endpoint-Buffer support when implemented out-of-band.""",
                    source = "MI Spec 2.1 §1, §5"
                ))
            ),
            ChapterSeed(
                id = 606, partId = 6, order = 6,
                title = "Real-World Use Cases",
                shortDescription = "BMC health monitoring, VPD/FRU reads, out-of-band firmware/config",
                level = "Advanced",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Why data centers implement NVMe-MI at all",
                    bullets = listOf(
                        "BMC health monitoring: poll NVM Subsystem Health Status Poll periodically for a cheap composite signal, or arm Asynchronous Event and let the device push AEMs instead of polling.",
                        "Inventory/asset management: VPD Read pulls serial number/part number/manufacturer data for asset databases and RMA workflows without needing the OS or drivers up.",
                        "Out-of-band firmware management: tunnel the standard Firmware Image Download / Firmware Commit NVMe Admin Commands over MCTP to stage and activate firmware on a drive that's unresponsive or has no host driver loaded.",
                        "Out-of-band configuration: Configuration Set to tune SMBus/I2C Frequency or MCTP Transmission Unit Size for the sideband link itself.",
                        "Enclosure/chassis control: SES Send/Receive to light drive-bay fault LEDs, monitor fan/power-supply health, or power a slot down before a hot-swap.",
                        "Recovery/lifecycle actions: Shutdown then Reset to gracefully quiesce and reset a hung NVM Subsystem entirely out-of-band.",
                        "Low-level diagnostics: PCIe Command Set lets a BMC directly inspect a Controller's PCIe config space or BAR-mapped registers, independent of host PCIe enumeration state."
                    ),
                    notes = """These use cases are why data-center and enterprise-storage vendors implement NVMe-MI at all - it is the mechanism that lets a BMC/service processor treat NVMe drives like any other manageable FRU, on par with power supplies and fans, even when the host is powered off, hung, or running an untrusted OS. The AEM-driven monitoring pattern is explicitly the design solution to "don't want to constantly poll." VPD is the backbone of most data-center asset-tracking/RMA tooling since it's readable at very low power states - meaning inventory scans can happen before a drive is even fully powered on. Out-of-band firmware management is particularly valuable for fleet operations: a management network can push firmware to thousands of drives without needing each host OS to cooperate.""",
                    source = "MI Spec 2.1 §1, §4, §5, §6, §7, §8"
                ))
            )
        )
    )
}
