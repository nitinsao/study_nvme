# NVMe Academy

An offline Android app for learning the NVMe protocol from first principles to
advanced spec detail, built with Kotlin, Jetpack Compose, and Room.

Content is sourced from the actual specifications in `spec_files/`:
- NVM Express Base Specification, Revisions 1.4, 2.0, and 2.3
- NVM Express NVM Command Set Specification, Revision 1.2
- NVM Express Management Interface Specification, Revision 2.1

## Structure

- **Learn tab** - chapters grouped into six parts (Foundations, Architecture &
  Transport, Admin Command Set, NVM I/O Commands & Fabrics, Advanced Topics,
  NVMe-MI). Each chapter is a swipeable slide deck; tap "Show detailed notes"
  on any slide for a deeper explanation with a spec citation.
- **Reference tab** - search any command by name or opcode across the Admin,
  NVM I/O, Fabrics, and NVMe-MI command sets, or browse the glossary.

## Building

This project was scaffolded and its content authored in a sandboxed
environment without access to Google's Maven repository (`dl.google.com`), so
the Android Gradle Plugin and AndroidX/Compose/Room dependencies could not be
downloaded or compiled there. Open this project in Android Studio (or run
`./gradlew assembleDebug` from a machine with normal internet access) to build
it — dependency resolution and compilation have not been verified end-to-end
outside that constraint. Requires JDK 17+, compileSdk 34, minSdk 26.

## Content notes

Command and chapter content is authored in Kotlin under
`app/src/main/java/com/nvmeacademy/app/data/content/`, extracted and
paraphrased directly from the spec text in `spec_files/` (not copied
verbatim, to respect the specs' copyright). A handful of entries are
explicitly marked in code comments or descriptions where the source text was
ambiguous (e.g. OCR artifacts from PDF-to-text conversion) — these are noted
inline rather than guessed. This is a first pass covering the full Admin
Command Set (50 opcodes), the full NVM I/O Command Set, Fabrics commands, and
the full NVMe-MI command set; some optional/advanced areas (e.g. Zoned
Namespaces, Key Value Command Set — separate specs not included in
`spec_files/`) are not yet covered and can be added in a follow-up pass.
