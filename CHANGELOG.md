# Changelog

## 2.3.5

- Updated Russian translation (rfin0)
- Fixed message text pre-conversion occurring even when style is disabled
- Fixed a bug in matching normal-type triggers introduced in v2.3.4
- Switched double-click to select-word and added triple-click for select-all

## 2.3.4

- Fixed restyle preview in trigger editor not working for regex style targets
- Added advanced option to override global self-notify control
- Fixed sound dropdown list scrollbar not working after selection
- Fixed color picker size and position inconsistency
- Prevented recent chat list in trigger editor from receiving new messages

## 2.3.3

- Fixed version metadata
- Fixed a bug causing exclusion triggers to be checked even when disabled
- Fixed a crash caused by processing invalid message structures
- Fixed trigger editor restyle preview not working for key-type triggers
- Added an option to use regex for style target
- Prevented notifications from being activated while editing
- Added an option to use ChatHeads for message sender detection

## 2.3.2

- Fixed an issue causing certain messages to be converted to blank strings

## 2.3.1

- Fixed an issue with checking notifications on a replacement message
- Added high contrast button textures
- Fixed sound automatically enabling itself on restart

## 2.3.0

- Redesigned options GUI
- Added trigger editor with recent chat message display
- Replaced debug copy options with access via trigger editor
- Improved message key selection
- Fixed normal triggers restyling beyond the trigger string
- Improved translatable message processing
- Improved format code processing
- Improved message component tree processing
- Added support for restyling multiple instances of style strings or triggers
- Added support for activating multiple notifications on a single message
- Added message replacement option
- Added status bar message option
- Adjusted title message option to match other custom message options
- Fixed a network protocol error caused by referencing an empty capturing group
- Updated Russian translation (rfin0)
- Special thanks to aquahonoredhi for helping with design and testing of this update

## 2.3.0-beta.16

- Removed unused translation keys
- Fixed a widget overflow bug on the main options screen

## 2.3.0-beta.15

- Fixed message blocking also disabling response and custom messages
- Removed multi-trigger restyling
- Adjusted display conditions of color and sound fields on main options screen

## 2.3.0-beta.14

- Improved color field and status button logic
- Fixed a network protocol error caused by referencing an empty capturing group

## 2.3.0-beta.13

- Updated Russian translation (rfin0)
- Fixed a bug breaking click-drag on dropdown text fields
- Fixed a button spacing issue on the notification options screen

## 2.3.0-beta.12

- Fixed a text field rendering bug introduced in beta.11
- Fixed overflow when resizing multiline text fields

## 2.3.0-beta.11

- Fixed a crash when adding a restyle string using the trigger editor
- Moved custom messages to advanced options screen
- Switched to multi-line text field for trigger editor

## 2.3.0-beta.10

- Fixed a few minor GUI issues

## 2.3.0-beta.9

- Overhauled top-level options GUI
- Added replacement message and status bar message options
- Moved title message option

## 2.3.0-beta.8

- Fixed processing of certain translatable message formats

## 2.3.0-beta.7

- Fixed notification status display

## 2.3.0-beta.6

- Improved format util error logging
- Fixed response message sending via chat screen
- Fixed inversion of response message modes
- Removed automatic notification enabling/disabling

## 2.3.0-beta.5

- Fixed translatable message processing

## 2.3.0-beta.4

- Restored old key selection screen

## 2.3.0-beta.3

- Fixed normal triggers restyling beyond the trigger string

## 2.3.0-beta.2

- Fixed style string restyle being overwritten
- Added option to change style color from within trigger editor

## 2.3.0-beta.1

- Added debug option to copy basic message text
- Updated Russian translation (rfin0)
- Fixed advanced settings reset
- Added enhanced trigger editor with chat message display
- Fixed handling of format codes
- Fixed handling of triggers spanning multiple message components
- Replaced debug copy with debug logging
- Added support for restyling multiple instances of style strings or triggers
- Added support for activating multiple notifications on a single message

## 2.2.0

- Fixed style target string field overlapping delete button
- Moved title text field to advanced options
- Added advanced option to block messages via notification
- Added option to change the way response messages are sent
- Switched response message delays from individual to cumulative
- Added CommandKeys integration for response messages
