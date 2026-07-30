package com.nvmeacademy.app.data.content

/** Part 3: Admin Command Set - the control-plane protocol. Deep command details live in the searchable Command Reference. */
object Part3AdminCommands {
    val part = PartSeed(
        id = 3,
        order = 3,
        title = "Part 3 · Admin Command Set",
        subtitle = "Controller & namespace management commands",
        chapters = listOf(
            ChapterSeed(
                id = 301, partId = 3, order = 1,
                title = "Admin Command Set Overview",
                shortDescription = "The control-plane protocol: 50 opcodes across §5.2/§5.3/§5.4",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Managing the controller, not moving data",
                    bullets = listOf(
                        "The Admin Command Set is the control-plane protocol for an NVMe controller - issued only on the Admin Submission/Completion Queue pair, never on I/O queues.",
                        "Admin commands configure and monitor the controller/NVM subsystem: queue creation, feature configuration, log/telemetry retrieval, namespace lifecycle, firmware update, security, and sanitize/format operations.",
                        "Every Admin command shares a common 64-byte SQE layout; only Command Dwords 10-15 (and sometimes the Data Pointer) are command-specific.",
                        "Commands are grouped by transport applicability: §5.2 Common Admin Commands (all transports), §5.3 Memory-Based (PCIe-specific), §5.4 Message-Based (Fabrics-specific).",
                        "Figure 28 in the spec defines, per controller role (I/O, Administrative, Discovery), which commands are Mandatory, Optional, or Prohibited - the same opcode can be mandatory on one controller type and prohibited on another.",
                        "A handful of opcodes (Load Program 85h, Get LBA Status 86h, Program Activation Management 88h, Memory Range Set Management 89h) are reserved in the Base Spec's opcode table but fully defined in companion specs.",
                        "Revision 2.3 assigns 50 Admin opcodes in total - growth from Migration, Track, Controller Data Queue, and Fabrics zoning/exported-resource commands added since 2.0."
                    ),
                    notes = """The NVMe Admin Command Set exists to separate "managing the device" from "moving data." While I/O commands are defined per I/O Command Set and submitted to per-CPU/per-core I/O queues, Admin commands always funnel through a single Admin Submission Queue per controller (SQ ID 0) and its paired Admin Completion Queue (CQ ID 0). This gives the host one well-known channel to discover what the device supports (Identify, Get Log Page), change device behavior (Set Features, Firmware Commit, Sanitize, Format NVM), manage the resources I/O commands will use (Create/Delete I/O SQ/CQ, Namespace Management/Attachment), and get asynchronous notifications about device health and state changes (Asynchronous Event Request). A key architectural point: the SQE format is identical across all Admin commands for the first 10 Dwords; Command Dwords 10-15 are the "payload" and their meaning is entirely dependent on which opcode is being issued. Admin commands are also where the spec defines the escalating management model for NVMe over Fabrics: Discovery controllers support a restricted subset (mostly Get Log Page, Identify, Keep Alive, plus Fabrics Zoning/Discovery Information Management), while Administrative controllers support a broader subset excluding I/O-queue-lifecycle commands but including Namespace Management, Firmware Commit, Sanitize, and Virtualization Management. Command support is not just Mandatory/Optional at the command level - many commands gate further optional behavior through a Select or Operation sub-field in Command Dword 10 (e.g., Migration Send's Select, Capacity Management's Operation), a recurring idiom worth teaching as a single pattern rather than unrelated commands.""",
                    source = "NVMe Base Spec 2.3 §5.2, §5.3, §5.4, Figure 28, Figure 142"
                ))
            ),
            ChapterSeed(
                id = 302, partId = 3, order = 2,
                title = "Queue & Feature Management Commands",
                shortDescription = "Create/Delete SQ/CQ ordering, Get/Set Features, Namespace lifecycle",
                level = "Intermediate",
                slides = listOf(SlideSeed(
                    order = 1,
                    title = "Two-phase patterns: create-then-attach, create-then-configure",
                    bullets = listOf(
                        "I/O Submission/Completion Queues are never implicit - the host must explicitly create Create I/O Completion Queue (05h) before Create I/O Submission Queue (01h), because every SQ must reference an existing CQ.",
                        "Deletion is the mirror image: all I/O SQs referencing a CQ must be deleted (00h) before that CQ can be deleted (04h); the Admin SQ/CQ pair can never be deleted.",
                        "Get Features (0Ah) / Set Features (09h) share one Feature Identifier space covering Arbitration, Power Management, Number of Queues, Interrupt Coalescing, Temperature Threshold, Host Memory Buffer, Keep Alive Timer, and many others.",
                        "Every Feature has three \"current setting\" views selectable via Select on Get Features: Current, Default, Saved, plus a fourth \"Supported Capabilities\" view.",
                        "The Number of Queues feature (FID 07h) is special: it must be set exactly once, early, before any I/O queues are created.",
                        "Doorbell Buffer Config (7Ch) and Virtualization Management (1Ch) are PCIe/SR-IOV-specific queue-adjacent commands for virtualized controllers.",
                        "Namespace Management (0Dh) / Namespace Attachment (15h) form the namespace equivalent of the queue create/attach pattern."
                    ),
                    notes = """Queue management in NVMe is explicitly two-phase and asymmetric between completion queues and submission queues. A Completion Queue is a pure landing zone for command results; a Submission Queue must additionally declare which Completion Queue its results will land on, so the CQ must already exist. The reverse holds at teardown: you cannot delete a Completion Queue while a Submission Queue still points at it. The Feature/Attribute model (Get/Set Features) is NVMe's general-purpose configuration mechanism: a Feature Identifier names a configurable attribute of the controller, NVM subsystem, Endurance Group, NVM Set, or namespace, and the same FID is used by both Get Features and Set Features. Some Features carry their value entirely in Command Dword 11 of Set Features (Arbitration, Power Management, Temperature Threshold), while others transfer a larger data structure through the Data Pointer (Host Memory Buffer, Autonomous Power State Transition, Host Behavior Support). Every Feature can independently be marked saveable and changeable, which is why Get Features has a fourth Select value ("Supported Capabilities") purely to ask about those properties instead of the value itself. Namespace lifecycle mirrors the queue lifecycle pattern conceptually: Namespace Management creates or deletes the namespace's existence and capacity allocation, but a newly created namespace is not usable by any controller until a separate Namespace Attachment command attaches it - deliberately splitting "does this namespace exist" from "can this controller see it," which matters in multi-controller/multi-host subsystems and NVMe-oF fan-out scenarios.""",
                    source = "NVMe Base Spec 2.3 §5.2.11, §5.2.20, §5.2.21, §5.2.26, §5.3.1-§5.3.6",
                    diagram = ChapterDiagramSeed(
                        caption = "I/O queue lifecycle: order matters",
                        steps = listOf(
                            DiagramStepSeed("Create CQ"),
                            DiagramStepSeed("Create SQ"),
                            DiagramStepSeed("Use for I/O"),
                            DiagramStepSeed("Delete SQ"),
                            DiagramStepSeed("Delete CQ")
                        )
                    )
                ))
            )
        )
    )
}
