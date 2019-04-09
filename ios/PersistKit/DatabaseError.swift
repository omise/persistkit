public enum DatabaseError: Error {
  case cannotConvertToRecord
  case cannotConvertToRecordable
  case driverError(reason: String)
}
