# PersistKit

PersistKit is a small-ish library that provides a simple Key-Value interface with the
following functions:

1. Get/Set binary blobs.
2. Perform an ordered range scan of keys.
3. Perform a filtered range scan of keys based on key prefixes.
4. Serialization to/from binary format.

The goal of this project is to provide a minimal and normalized cross-platform KV store
interface that is easy to guarantee consistent functionality between iOS and Android.

**Goals:**

* Make porting code from iOS to Android and back dead simple.
* Fast.
* Must not require schema management.

**Non-Goals:**

* Database compatibility between Android and iOS. Each platform use the best and most
  performant format available to them.
