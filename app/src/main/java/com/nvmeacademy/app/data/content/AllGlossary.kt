package com.nvmeacademy.app.data.content

/** Glossary of NVMe terminology, searchable alongside commands. */
object AllGlossary {
    val terms: List<GlossarySeed> = listOf(
        GlossarySeed("NVM Subsystem", "The collection of one or more NVM controllers, namespaces, and the storage medium(s) that connects them to one or more hosts."),
        GlossarySeed("Namespace", "A quantity of non-volatile memory formatted into logical blocks, addressable by a host as an independently formatted storage device."),
        GlossarySeed("Submission Queue (SQ)", "A circular buffer the host uses to submit commands to a controller for execution."),
        GlossarySeed("Completion Queue (CQ)", "A circular buffer the controller uses to post completion entries back to the host."),
        GlossarySeed("Doorbell", "A controller register the host writes to signal a new submission queue entry or that completion entries have been consumed."),
        GlossarySeed("PRP (Physical Region Page)", "A data pointer mechanism describing physical memory pages used for a command's data transfer."),
        GlossarySeed("SGL (Scatter Gather List)", "An alternative data pointer mechanism describing one or more, possibly non-contiguous, memory regions for a command's data transfer."),
        GlossarySeed("NSID (Namespace Identifier)", "The identifier a host uses to address a specific namespace; 0h is never valid and FFFFFFFFh is a command-dependent broadcast value."),
        GlossarySeed("Phase Tag (P)", "A single bit in each completion queue entry that flips value every time the completion queue wraps, letting the host detect new entries without polling a register."),
        GlossarySeed("Fused Operation", "Two adjacent commands in the same Submission Queue marked to execute back-to-back as a single atomic unit, most commonly Compare-and-Write."),
        GlossarySeed("Feature Identifier (FID)", "A numeric identifier naming a configurable attribute of the controller, namespace, or subsystem, read and written via Get Features / Set Features."),
        GlossarySeed("NQN (NVMe Qualified Name)", "A UTF-8 string of at most 223 bytes that uniquely names a host or NVM subsystem, used in NVMe over Fabrics discovery and authentication."),
        GlossarySeed("Persistent Reservation", "An access-control mechanism letting multiple hosts share a namespace while restricting which hosts may read or write it, modeled on SCSI Persistent Reservations."),
        GlossarySeed("Endurance Group", "A management boundary within an NVM subsystem used for wear/endurance tracking, containing one or more NVM Sets."),
        GlossarySeed("NVM Set", "A grouping of namespaces that share attributes such as optimal write size; every NVM Set belongs to exactly one Endurance Group."),
        GlossarySeed("Controller Fatal Status (CSTS.CFS)", "A controller register bit indicating the controller may be unable to reliably post completions at all, signaling the host should reset and reinitialize."),
        GlossarySeed("Keep Alive Timeout (KATO/KATT)", "The interval within which a host must send Keep Alive traffic (or an explicit Keep Alive command) or the controller treats the association as dead."),
        GlossarySeed("MCTP", "Management Component Transport Protocol - the transport NVMe-MI messages ride on, running over PCIe Vendor Defined Messages or SMBus/I2C/I3C."),
        GlossarySeed("VPD (Vital Product Data)", "Manufacturing/asset-tracking data (serial number, part number, manufacturer) stored on a device and readable via NVMe-MI VPD Read even without a host OS.")
    )
}
