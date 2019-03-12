package co.omise.persistkit

sealed class Command {
    object LoadAll : Command()
    class Load(val identifier: String) : Command()
    class Save(val record: Record) : Command()
    class Delete(val identifier: String) : Command()
}
