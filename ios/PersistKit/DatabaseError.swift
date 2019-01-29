//
//  DatabaseError.swift
//  PersistKit
//
//  Created by Nuttapol Laoticharoen on 22/1/19.
//  Copyright © 2019 Chakrit Wichian. All rights reserved.
//

public enum DatabaseError: Error {
    case cannotConvertToRecord
    case cannotConvertToRecordable
    case driverError(reason: String)
}
