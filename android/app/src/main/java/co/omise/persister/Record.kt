package co.omise.persister

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class Record(
    @PrimaryKey @ColumnInfo(name = "_id") var identifier: String,
    var kind: String,
    var flags: Int,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) var content: ByteArray
)