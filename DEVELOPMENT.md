# OffRead development guide

## Workspace

The recovered project is intended to live at:

~~~text
~/Projects/OffRead
~~~

The recovered worktree has been promoted to ~/Projects/OffRead. The untouched original GitHub clone is preserved at ~/Projects/OffRead-github-baseline.

## Termux prerequisites

Required commands:

- Java and javac (OpenJDK 21 currently installed; source compatibility is Java 17)
- aapt2
- apksigner
- python3
- keytool

The build also uses these local SDK files:

~~~text
~/Android/Sdk/platforms/android-35/android.jar
~/Android/Sdk/build-tools/35.0.0/d8
~/Android/Sdk/build-tools/35.0.0/lib/d8.jar
~~~

This is a deliberately minimal 42 MB SDK subset, separate from BuildLog's toolchain. The build script checks required paths before compiling.

## Build and test

Run from the repository root:

~~~bash
./build_apk.sh test
./build_apk.sh debug
./build_apk.sh release
./build_apk.sh all
./build_apk.sh clean
~~~

Build outputs and generated classes stay under build_cli/ and are ignored by Git.

## Signing

The first debug/release build creates:

- offread-dev.keystore
- .offread-dev-keypass

Both are local-only and must never be committed or uploaded. Keep them backed up privately if you need continuity for locally signed test builds.

The original installed APK has a different signing certificate. The original private signing key was not found in the inspected Termux home directory. Therefore, builds from this recovered workspace cannot update the installed app in place. Keep the installed copy. Do not uninstall it just to test a recovered build.

For a future production release, use a properly secured production signing key and a deliberate versioning/release process. The current release command is not a Play Store signing setup.

## Git workflow

- Recovery branch: recovery/apk-baseline
- Keep implementation changes in feature branches.
- Run ./build_apk.sh test before committing.
- Do not push recovered source automatically; inspect the diff and decide separately whether and when to publish.
- Keep the GitHub baseline bundle and APK backup in ~/Projects/OffRead-recovery/baseline/.

## Coding-agent workflow on this phone

Use one coding agent at a time. Preferred sequence: Antigravity for implementation, close/stop it, then Qoder for audit. Never run both concurrently on this device.

## Before a release

1. Run the complete test suite.
2. Build debug and release APKs.
3. Verify package/version/signature with aapt2 dump badging and apksigner verify.
4. Manually smoke-test on a separate test installation before replacing or migrating the installed app.
5. Verify backup export/import before any uninstall or data migration.
6. Confirm signing key continuity and APK alignment.
