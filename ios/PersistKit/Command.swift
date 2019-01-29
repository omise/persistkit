//
//  Command.swift
//  PersistKit
//
//  Created by Nuttapol Laoticharoen on 22/1/19.
//  Copyright © 2019 Chakrit Wichian. All rights reserved.
//

import Foundation

public enum Command {
    case loadAll
    case load(_ identifier: String)
    case save(_ record: Record)
    case delete(_ identifier: String)
}

