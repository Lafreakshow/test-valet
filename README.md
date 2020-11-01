# Test Valet

<!PLUGIN DESCRIPTION START>
Improves ease of navigation between test and source files.

This is a continuation/extension/rework of the [Test Navigator IntelliJ Plugin](https://github.com/stacherzakp/test-navigator-plugin).
Even though it shares no code, the author still deserves credit for all the inspiration and help I got from the original source.

# Features

- gutter Markers for sources:
    - green [T]: a test was found
    - yellow [T]: a test was found but doesn't contain test cases
    - red [T]: no test was found
- gutter Markers for tests:
    - green [S]: a source was found
    - red [S]: no source was found
- locates test/source based on fully qualified name with configurable suffixes
- handles test in the same module and tests in separate module (e.g. imported gradle source sets)
- supports Java classes and interfaces
- supports Kotlin (data, sealed, enum) classes, objects and interfaces
<!PLUGIN DESCRIPTION END>

 # Upcoming features
 
 ## Timeline: soon-ish
 - support for kotlin top-level functions and properties
 - option to use the original Test Navigator Icons
 
 ## Timeline: eventually
 - support for more test frameworks and languages as demand arises
 - smarter detection of location of test/source files
 - link multiple tests/sources per target
 - detection of magic `!Tests [symbol]` comments to link tests and sources without requiring a suffix

 
 # Availability
 
 As of writing this, Test Valet is not available for download in binary format. This is because it is still very
  experimental and not ready for a formal release. If you want to try it out anyway, you will have to build the
  plugin yourself, which in theory should be as simple as cloning the repo and running `./gradlew buildPlugin
  ` (`.\gradlew.bat buildPlugin` on windows).

## Comparison to Test Navigator

- Both plugins provide five separate icons, one respectively for source with missing test, source with existing test but
 no test cases, source with existing test with cases, tests with existing source files and tests with no
  associated source file.
- Test Navigator doe not support Kotlin and fails to find tests in a separate module
- Test Navigator supports Groovy, Test Valet does not (yet)
- Test Navigator will find Junit and spock tests, Test Valet only supports Junit 4 and 5 (for now)
- Test Valets Icons are in SVG format (vs png in Navigator) which may look better on very high dpi screens
- Test Valet comes with custom but extremely minimalistic icons. Some may find Test Navigators Icons more appealing.

# Whats up with the name?

I wanted something different but still similar to Test Navigator and this was the first thing that popped into my
 mind. The Americans in the audience may understand the relationship but for those more acquainted with the French
 meaning of the word "Valet" and wonder about the connection between unit tests and clothes: In the US a Valet is
 sometimes employed by hotels and has the job to take a guest's car from the entrance to the hotels garage and back
 when the guest wants to leave. Test Valet finds your tests and sources and brings them into your editor when you
 need them.
 
# But Lafreakshow! Technically Test Valet takes you to the test, not the other way around!

Yes. 

# Contributing to Test Valet

Test Valets API has been designed to make it straight forward to add support for new languages (as long as they
 support the PSI). The entire code base is also documented with comments which hopefully makes it easy to understand
 I welcome anyone to tinker with the code and improve upon it. If you intend to contribute changes back to this repo
 please make sure to use a descriptively named branch and don't commit to Master. This makes contributions easier to
 manage. 
 
 For Suggestions or problems you can always create an Issue. When you do, try to provide some context and example if
  possible. I'd rather have too much information than too little. 
