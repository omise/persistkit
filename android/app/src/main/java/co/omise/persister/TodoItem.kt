package co.omise.persister

import java.io.Serializable

interface Identifiable {
    val identifier: String
}

data class TodoItem(
    override val identifier: String,
    val description: String,
    val completed: Boolean
) : Identifiable, Serializable
