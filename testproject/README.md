# Test projects

This directory contains a set of test project to use when verifying the functionality of the plugin.

# Structure

Projects mud follow some rules:
 
- no access to the parent directory or other test projects. Test projects must be self-contained.
- If the scenario has expected failure cases, these must be declared in the test projects README.md. Any failure that
 isn't declared in the README.md or can be inferred from related automated tests will be considered a bug.
- If the scenario is expected to change the test project in any way, the test project is to be used as a template, and
 the current tests have to be carried out in a temporary directory. Ideally, this should always be the case, but it is
 not necessary when one is just testing that the gutter icons are displayed on the correct files.
- These projects are designed to be used in automated unit tests, where they will be copied to a temporary directory
 It is intended to also use them for some quick verification of minor changes as explained below. 
- When carrying out manual tests, one must always make sure the integrity of the automated tests isn't
 compromised. Primarily this means not allowing the plugin under test to change the template and making sure that
 manual changes do not conflict with expected behaviour (prefer to consult the related tests rather than the
  established features of the project, as some test projects may test experimental features not yet listed elsewhere.).
 
 # Content
 
Test projects may be any regular IDEA compatible project. Be it gradle builds, IntelliJJ modules or maven builds. Be
 they simple or very complex, with a single language or combining multiple language and frameworks. Any situation in
 which the plugin under test may be used is a valid test project. As of writing this, there is only one test project
 . A simple Gradle project with Java sources and tests in the standard Gradle source set configuration. In the future,
  will contain simple Kotlin sources and tests too and serves to test the very basic functionality of the plugin.
 
 Every single file in a directory has to be considered part of the test situation with one exception: Test projects are
  encouraged to contain a README.md file that explains the situations that are being tested and preferably a
  reference to any automated test cases that use it.
