package com.nvmeacademy.app.data.content

/** Part 1: Foundations - beginner on-ramp, no prior NVMe knowledge assumed. */
object Part1Foundations {
    val part = PartSeed(
        id = 1,
        order = 1,
        title = "Part 1 · Foundations",
        subtitle = "Start here if you're new to NVMe",
        chapters = listOf(
            ChapterSeed(
                id = 101,
                partId = 1,
                order = 1,
                title = "What is NVMe? History & Motivation",
                shortDescription = "Why NVMe replaced AHCI/SATA/SAS for solid-state storage",
                level = "Beginner",
                slides = listOf(
                    SlideSeed(
                        order = 1,
                        title = "From spinning disks to flash",
                        bullets = listOf(
                            "NVMe = Non-Volatile Memory Express, a command protocol built specifically for flash and other solid-state storage.",
                            "Predecessors (AHCI/SATA, SAS) were designed in an era of spinning disks with high seek latency.",
                            "AHCI supports only 1 command queue with 32 commands deep - a bottleneck for SSDs.",
                            "NVMe supports up to 65,535 queues, each up to 65,535 commands deep.",
                            "Commands travel directly over PCI Express - no legacy storage controller translation layer.",
                            "Designed from the ground up for parallelism, low latency, and multicore CPUs.",
                            "First ratified in 2011 by a consortium of storage/tech companies; now governed by NVM Express, Inc."
                        ),
                        notes = """Solid-state drives were originally bolted onto interfaces designed for mechanical hard drives - SATA using the AHCI command interface, or SAS. Those interfaces assumed millisecond-scale seek times, so a single command queue with a shallow queue depth (AHCI allows one queue of up to 32 commands) was perfectly adequate; the disk itself was always the bottleneck. Flash memory changed that: NAND-based SSDs could service requests in microseconds and in parallel across many independent flash dies, but AHCI's single queue and driver-mediated command path became the new bottleneck instead of the media. NVMe was created to remove that mismatch by defining a lightweight, streamlined command interface that talks to storage directly over PCI Express, avoiding the legacy SCSI/ATA translation layers. Its core design goals include avoiding uncacheable register reads on the hot path, needing at most one register write (a "doorbell") to submit a batch of commands, and supporting a very large number of independent, deep queues so that each CPU core (or virtual machine) can have its own private command path without lock contention. The result is a protocol that scales naturally with modern multicore, highly parallel storage media. Since its first ratification, NVMe has grown from a PCIe-only SSD interface into a full family of specifications that also covers network-attached ("fabric") storage, while keeping the same fundamental command and queueing model.
Note: this historical narrative (AHCI/SATA/SAS comparison, 2011 origin) is general industry background, not something asserted verbatim by the Base Specification itself.""",
                        source = "General industry background + NVMe Base Spec 2.3 §2 (Theory of Operation, key attributes)",
                        diagram = ChapterDiagramSeed(
                            caption = "Legacy vs NVMe queueing",
                            steps = listOf(
                                DiagramStepSeed("AHCI / SATA", "1 queue x 32 commands"),
                                DiagramStepSeed("NVMe", "65,535 queues x 65,535 commands")
                            )
                        )
                    )
                )
            ),
            ChapterSeed(
                id = 102,
                partId = 1,
                order = 2,
                title = "NVMe Spec Family & Version History",
                shortDescription = "How the Base Spec, Transport specs, Command Set specs, and MI spec fit together",
                level = "Beginner",
                slides = listOf(
                    SlideSeed(
                        order = 1,
                        title = "A family of specifications",
                        bullets = listOf(
                            "NVMe isn't one document - it's a family: Base Spec, Transport Specs, I/O Command Set Specs, MI Spec, Boot Spec.",
                            "The Base Specification defines the core protocol; Transport Specs (PCIe, RDMA, TCP, FC) bind it to physical links.",
                            "I/O Command Set Specs (NVM, Zoned Namespace, Key Value, ...) define per-namespace command behavior.",
                            "NVMe-MI is a separate, optional out-of-band management interface spec.",
                            "Revision 1.4 (2019) was PCIe-SSD-centric with Fabrics as a separate specification.",
                            "Revision 2.0 (2021) folded NVMe over Fabrics into the Base Spec and split out Transport/Command Set specs.",
                            "Revision 2.3 (2025) is the current spec studied here - same skeleton as 2.0, with more storage-management depth."
                        ),
                        notes = """The modern NVMe ecosystem is deliberately modular. The Base Specification defines the protocol elements that are common no matter how a host physically talks to the storage: the queueing model, controller architecture, data structures, and Admin/Fabrics commands. Separate NVMe Transport binding specifications (PCI Express, RDMA, TCP, Fibre Channel) describe how those abstract concepts map onto a specific physical/network layer. NVMe I/O Command Set specifications (the NVM Command Set, Zoned Namespace Command Set, Key Value Command Set, and others) define the data structures and commands specific to how a namespace's data is organized. NVMe-MI is an optional, independent out-of-band management protocol for the whole NVM subsystem, and a Boot Specification covers boot-time constructs. Comparing versions: 1.4 (2019) still contained its own PCIe register chapter and Controller Registers chapter, and treated NVMe over Fabrics as an entirely separate spec; 2.0 (2021) restructured everything into the modular family described above, merging Fabrics support directly into the Base Spec and pulling PCIe-specific register definitions out into a Transport Spec. Revision 2.3 (2025) keeps that same nine-chapter skeleton (Introduction, Theory of Operation, Architecture, Data Structures, Admin Command Set, Fabrics Command Set, I/O Commands, Extended Capabilities, Error Reporting) but adds several generations of new storage-management and reliability features on top.""",
                        source = "NVMe Base Spec 2.3 §1.1; structural comparison against Base Spec 1.4 and 2.0 tables of contents",
                        diagram = ChapterDiagramSeed(
                            caption = "The NVMe family of specifications",
                            connector = "none",
                            steps = listOf(
                                DiagramStepSeed("Base Spec", "Core protocol"),
                                DiagramStepSeed("Transport Specs", "PCIe, RDMA, TCP, FC"),
                                DiagramStepSeed("Command Set Specs", "NVM, ZNS, KV"),
                                DiagramStepSeed("MI Spec", "Out-of-band mgmt"),
                                DiagramStepSeed("Boot Spec", "Boot-time")
                            )
                        )
                    )
                )
            ),
            ChapterSeed(
                id = 103,
                partId = 1,
                order = 3,
                title = "Transport Models: PCIe vs Fabrics",
                shortDescription = "Memory-based (PCIe) vs message-based (RDMA/TCP/FC) transport",
                level = "Beginner",
                slides = listOf(
                    SlideSeed(
                        order = 1,
                        title = "Two ways to move commands and data",
                        bullets = listOf(
                            "Two transport models: memory-based (e.g., PCIe) and message-based (e.g., Fibre Channel, TCP, RDMA).",
                            "Memory-based: host and controller exchange data via direct memory reads/writes.",
                            "Message-based: host and controller exchange \"capsules\" containing a command or response.",
                            "PCIe queues can share: many Submission Queues can map to one Completion Queue.",
                            "NVMe over Fabrics queues are strictly 1:1 - one Submission Queue per Completion Queue.",
                            "Fabrics requires SGLs everywhere; PCIe requires PRPs for Admin commands and forbids SGLs there.",
                            "Fabrics has no hardware interrupt mechanism - the host fabric interface must raise interrupts itself."
                        ),
                        notes = """NVMe defines two fundamentally different ways a host and an NVM subsystem can exchange commands, responses, and data. In the memory-based transport model - used by PCI Express - the host and controller both have direct access to a shared address space, so Submission Queues, Completion Queues, and data buffers are just regions of host memory that the controller reads and writes directly. In the message-based transport model - used by NVMe over Fabrics transports like RDMA, TCP, and Fibre Channel - there is no shared memory; instead, "capsules" (self-contained units bundling a submission or completion queue entry, plus optionally data or scatter-gather lists) are sent as discrete messages. These models drive concrete protocol differences: PCIe permits many Submission Queues to share a single Completion Queue, while Fabrics strictly requires a 1:1 mapping. PCIe Admin commands must use Physical Region Pages (PRPs) and are forbidden from using Scatter Gather Lists (SGLs), whereas NVMe over Fabrics mandates SGLs for every command and does not support PRPs at all. Fabrics also has no notion of a hardware interrupt from controller to host; the underlying fabric interface is responsible for signaling completion to the host.""",
                        source = "NVMe Base Spec 2.3 §2, §2.1, §2.2",
                        diagram = ChapterDiagramSeed(
                            caption = "The common request path",
                            steps = listOf(
                                DiagramStepSeed("Host"),
                                DiagramStepSeed("Transport", "PCIe or Fabrics"),
                                DiagramStepSeed("Controller"),
                                DiagramStepSeed("NVM Media")
                            )
                        )
                    )
                )
            ),
            ChapterSeed(
                id = 104,
                partId = 1,
                order = 4,
                title = "NVM Storage Model",
                shortDescription = "Namespaces, NSIDs, Endurance Groups, and NVM Sets",
                level = "Beginner",
                slides = listOf(
                    SlideSeed(
                        order = 1,
                        title = "How storage is organized and addressed",
                        bullets = listOf(
                            "A namespace is a quantity of formatted non-volatile storage a host can address by Namespace ID (NSID).",
                            "Hierarchy (top to bottom): NVM Subsystem → Domain → Endurance Group → NVM Set / Reclaim Group → Namespace.",
                            "Every namespace lives in exactly one NVM Set, and every NVM Set lives in exactly one Endurance Group.",
                            "Each namespace is associated with exactly one I/O Command Set (NVM, Zoned Namespace, Key Value, ...) for life.",
                            "NSIDs can be allocated/unallocated (does a namespace exist?) and active/inactive (is it attached to this controller?).",
                            "Support for Endurance Groups, NVM Sets, and Reclaim Groups is all optional - a simple SSD may have just one of each."
                        ),
                        notes = """The NVM storage model describes how raw physical media is organized and exposed to hosts. At the top sits the NVM subsystem itself, which contains one or more domains. Within a domain, an Endurance Group is a management boundary for wear/endurance tracking; storage inside an Endurance Group can be further subdivided either into NVM Sets or into Reclaim Groups made of Reclaim Units. A namespace itself is "a formatted quantity of non-volatile memory that may be directly accessed by a host," identified by a Namespace ID (NSID). NSIDs have a two-axis lifecycle: an NSID is either allocated (a namespace with that ID currently exists) or unallocated, and independently, for any specific controller, an allocated NSID is either active (attached to and visible on that controller) or inactive. NSID 0h is never valid, and FFFFFFFFh is a reserved broadcast value meaning "all namespaces." Each namespace is tied to exactly one I/O Command Set for its entire lifetime - most commonly the NVM Command Set, but possibly Zoned Namespace or Key Value on controllers that support them. All of this richness is optional scaffolding: a simple consumer SSD may report just one domain, one Endurance Group, one NVM Set, and one namespace, while an enterprise array can use the full hierarchy to isolate performance or endurance characteristics between workloads.""",
                        source = "NVMe Base Spec 2.3 §2.3, §3.2",
                        diagram = ChapterDiagramSeed(
                            caption = "Storage containment hierarchy (top to bottom)",
                            orientation = "V",
                            steps = listOf(
                                DiagramStepSeed("NVM Subsystem"),
                                DiagramStepSeed("Domain"),
                                DiagramStepSeed("Endurance Group"),
                                DiagramStepSeed("NVM Set", "or Reclaim Group"),
                                DiagramStepSeed("Namespace")
                            )
                        )
                    )
                )
            )
        )
    )
}
