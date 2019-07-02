// swift-tools-version:5.0
import PackageDescription


let package = Package(
  name: "PersistKit",
  platforms: [.iOS(.v8), .macOS(.v10_10), .tvOS(.v9), .watchOS(.v2)],
  products: [
    .library(name: "PersistKit", targets: ["PersistKit"]),
  ],
  targets: [
    .target(name: "PersistKit",
            path: "ios/PersistKit"),
    .testTarget(name: "PersistKitTests", dependencies: ["PersistKit"],
                path: "ios/PersistKitTests"),
  ]
)

