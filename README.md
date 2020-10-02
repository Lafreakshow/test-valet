# Test Valet

This is a continuation/extension/rework of the [Test Navigator IntelliJ Plugin](https://github.com/stacherzakp/test-navigator-plugin)

I originally just wanted to make some quick changes, but it became apparent very quickly that it would turn into a
 major effort. So major, the version you see now probably has nothing in common with the original plugin anymore
 except for some features. However, I started out with the original code, so I found it appropriate to credit
 the original author.

As of writing this line, Test Valet doesn't share any files with Test Navigator, but I still consider it to be based
 off of it.
 
 # Availability
 
 As of writing this, Test Valet is not available for download in binary format. This is because it is still very
  experimental and not ready for a formal release. If you want to try it out anyway, you will have to build the
  plugin yourself, which in theory should be as simple as cloning the repo and running `./gradlew buildPlugin
  ` (`.\gradlew.bat buildPlugin` on windows).

# Features

- provides gutter marker with navigation to test/source for Java Classes, Kotlin Classes and Kotlin objects.
- finds test/source based on test file suffixes, configurable in settings.
- automagically finds test/source file in separate modules (confirmed to work only for modules imported from default
 "main"/"test" gradle source sets. Should theoretically work with any setup as long as IntelliJ picks up which
  modules relate to each other, still very experimental)

- (TODO) gutter icons for kotlin top-level functions.

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

# I dislike your style/mentality

That is your right. However, Test Valet is a hobby project, and I sort of consider it my baby since it is the first
 Project I actually took somewhat seriously. I always appreciate feedback and suggestions (I want Test Valet to be
 helpful to more than just myself, but I cannot know what features other people need) But please understand that I
 will always choose my style over whatever you may prefer. I will respectfully consider any criticism but in exchange I
 expect that you respectfully accept my decision. 
 
 Well, you can always scream at me and move on, that is fine with me too.

# Contributing to Test Valet

Test Valets API has been designed to make it straight forward to add support for new languages (as long as they
 support the PSI). The entire code base is also documented with comments which hopefully makes it easy to understand
 I welcome anyone to tinker with the code and improve upon it. If you intend to contribute changes back to this repo
 please make sure to use a descriptively named branch and don't commit to Master. This makes contributions easier to
 manage. 
 
 For Suggestions or problems you can always create an Issue. When you do, try to provide some context and example if
  possible. I'd rather have too much information than too little. 
