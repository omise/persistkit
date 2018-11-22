package co.omise.persister

import co.omise.persistkit.Identifiable
import java.io.Serializable

data class TodoItem(
    override val identifier: String,
    val description: String,
    val completed: Boolean
) : Identifiable, Serializable
