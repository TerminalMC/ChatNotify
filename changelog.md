# Changelog

## 3.102.6

- Added trumpet sounds (rfin0)

## 3.102.5

- Reset `mc` version counter to 1 at mc1.0.0

## 3.2.4

- Fixed an issue causing regex inclusion triggers to always match
- Added `/say` to the default list of detection prefixes

## 3.2.3

- Added an option to restart the cooldown timer when a notification would be triggered if not
  already on cooldown.

## 3.2.2

- Fixed rendering of list items while dragging to reorder
- Added a debug command for parsing logged messages
- Added detection for non-root translation keys
- Added support for including `$` in custom messages using `$$`
- Replaced compile-time compat deps with reflective access

## 3.2.1

- Updated Russian translation (rfin0)
- Added French translation (Spipi1)
- Fixed a compat issue with ChatHeads causing all sent messages to trigger notifications

## 3.0.0

- Re-enabled ChatHeads compat

## 3.0.0-beta.2

- Updated to mc26.1
- Temporarily disabled ChatHeads compat
- Mod versioning scheme is now `major.mc.minor`:
  - `major` is incremented on 'significant' feature changes, or breaking API changes (if
    applicable).
  - `mc` is never reset, and is incremented on every MC release, irrespective of whether a mod
    update was required.
  - `minor` is reset when `major` is changed, and is incremented on every update that does not
    change either of the previous two numbers.
