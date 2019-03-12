package co.omise.persistkit.driver

import co.omise.persistkit.Command
import co.omise.persistkit.Record
import co.omise.persistkit.driver.exception.UnsupportedCommandException

interface Driver {
    @Throws (UnsupportedCommandException::class)
    fun query(command: Command): List<Record>

    @Throws (UnsupportedCommandException::class)
    fun execute(command: Command): Int
}
