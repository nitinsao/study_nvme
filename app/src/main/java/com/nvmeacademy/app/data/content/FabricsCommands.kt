package com.nvmeacademy.app.data.content

import com.nvmeacademy.app.data.db.entities.CommandSet

/** Fabrics Command Set reference entries (Base Spec 2.3 §6, dispatched via Admin opcode 7Fh). */
object FabricsCommands {
    val list: List<CommandSeed> = listOf(
        CommandSeed(
            id = 2501, opcode = "01h (FCTYPE)", name = "Connect", commandSet = CommandSet.FABRICS,
            summary = "Creates a Submission/Completion Queue pair over a Fabrics transport, establishing (for the Admin Queue) the host-controller association.",
            description = """The first command sent on any new Fabrics queue. On the Admin Queue it establishes the association between a Host NQN and an NVM Subsystem NQN and either requests a specific Controller ID (static model) or asks the subsystem to allocate one dynamically (Controller ID FFFFh). On an I/O Queue it must reuse the same Host NQN/Subsystem NQN/Controller ID as the Admin Queue. Also negotiates SQ flow control and, for the Admin Queue, sets the Keep Alive Timeout (KATO).""",
            mandatory = "Mandatory",
            source = "Base Spec 2.3 §6.3",
            fields = listOf(
                FieldSeed("QID", "Queue ID: 0h = Admin, 1-65534 = I/O."),
                FieldSeed("SQSIZE", "0's-based Submission Queue size."),
                FieldSeed("KATO", "Keep Alive Timeout, Admin Queue only."),
                FieldSeed("Data payload", "HOSTID, CNTLID, SUBNQN, HOSTNQN.")
            )
        ),
        CommandSeed(
            id = 2502, opcode = "08h (FCTYPE)", name = "Disconnect", commandSet = CommandSet.FABRICS,
            summary = "Deletes the I/O Queue on which it is submitted, ending Fabrics use of that queue pair.",
            description = """Can only be submitted on an I/O Queue (submitting it on the Admin Queue aborts with Invalid Queue Type). Its completion queue entry is guaranteed to be the last CQE the controller posts to that I/O Completion Queue. The underlying NVMe Transport connection is not itself torn down by Disconnect; that is a separate step once all queues on the connection are gone.""",
            mandatory = "Optional",
            source = "Base Spec 2.3 §6.4"
        ),
        CommandSeed(
            id = 2503, opcode = "04h (FCTYPE)", name = "Property Get", commandSet = CommandSet.FABRICS,
            summary = "Reads the value of a controller property (the Fabrics equivalent of a BAR0/PCIe register read, e.g. CAP, CC, CSTS).",
            description = """Returns the current value of a specified controller property at a given offset, in either 4-byte or 8-byte width selected by the Property Return Size (PRS) field. This is how Fabrics hosts read registers that would be memory-mapped in PCIe, since Fabrics transports have no BAR space. An invalid property or offset returns Invalid Field in Command.""",
            mandatory = "Mandatory",
            source = "Base Spec 2.3 §6.5",
            fields = listOf(FieldSeed("OFST", "Offset of the property to get."))
        ),
        CommandSeed(
            id = 2504, opcode = "00h (FCTYPE)", name = "Property Set", commandSet = CommandSet.FABRICS,
            summary = "Writes a value to a controller property (the Fabrics equivalent of a BAR0/PCIe register write, e.g. CC).",
            description = """Updates a specified controller property at a given offset with a host-supplied value, in either 4-byte or 8-byte width (Property Update Size). This is the mechanism a Fabrics host uses to, for example, set CC.EN to enable the controller, since there is no memory-mapped register space over Fabrics.""",
            mandatory = "Mandatory",
            source = "Base Spec 2.3 §6.6",
            fields = listOf(FieldSeed("OFST / VALUE", "Offset of property to set and value to write."))
        ),
        CommandSeed(
            id = 2505, opcode = "05h (FCTYPE)", name = "Authentication Send", commandSet = CommandSet.FABRICS,
            summary = "Transfers security-protocol data (e.g., DH-HMAC-CHAP authentication handshake messages) from host to controller.",
            description = """Carries a security-protocol-specific payload to the controller - in NVMe-oF this is primarily used to drive the NVMe In-Band Authentication (DH-HMAC-CHAP) handshake and optional secure-channel negotiation. Any status/data the controller needs to return is retrieved via a subsequent Authentication Receive command.""",
            mandatory = "Optional",
            source = "Base Spec 2.3 §6.2"
        ),
        CommandSeed(
            id = 2506, opcode = "06h (FCTYPE)", name = "Authentication Receive", commandSet = CommandSet.FABRICS,
            summary = "Retrieves the status/data result of a prior Authentication Send command (e.g., the next DH-HMAC-CHAP handshake message).",
            description = """Returns data and status that the security protocol defines as owed to the host following one or more Authentication Send commands - in practice, the next message of a DH-HMAC-CHAP authentication exchange. Results are not retained across communication loss or a Controller Level Reset.""",
            mandatory = "Optional",
            source = "Base Spec 2.3 §6.1"
        )
    )
}
