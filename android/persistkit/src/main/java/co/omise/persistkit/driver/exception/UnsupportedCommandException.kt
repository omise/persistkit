package co.omise.persistkit.driver.exception

import co.omise.persistkit.Command

class UnsupportedCommandException(command: Command) : Exception("Command ${command::class.qualifiedName} is not supported")
