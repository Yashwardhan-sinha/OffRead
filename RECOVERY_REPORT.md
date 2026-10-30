# OffRead recovery report

## Recovery scope

Recover the most complete available development source from the GitHub repository and the working OffRead APK installed on the Vivo Y73, then establish a repeatable Termux build and test pipeline.

No changes were made to the installed app, its private data, or the original GitHub clone during the audit.

## Preserved baselines

| Item | Result |
|---|---|
| GitHub repository | Yashwardhan-sinha/OffRead |
| GitHub HEAD | 8a6ff2885929ad162e8a3b78775e226673e65106 |
| Original Git commits | 2 |
| Original tracked files | 83 |
| Installed package | com.offread.app |
| Installed version | 1.0.0-rot1 (version code 2) |
| Minimum / target SDK | 24 / 33 |
| Installed APK size | 188,614 bytes |
| Installed APK SHA-256 | 12f88eaf0570c408cc4a2fc06219fcac1836833f74b9571f9e907d9dd1d3bcf3 |
| Original APK certificate SHA-256 | eb03ff986be45b9138c819968d8477690506ead6965c50ce900c293b11ee5045 |

The uploaded APK and the installed package's base.apk were byte-for-byte identical by SHA-256.

## Recovered source and resources

- 46 application Java source files reconstructed from the APK.
- 59 Android resource files decoded from the APK.
- Original GitHub unit tests retained.
- APK manifest decoded and used as the recovered manifest.
- The decompiler could not reconstruct one large ReadabilityEngine.formatNode method. That method was restored from the original GitHub source while the APK-recovered scoring, extraction and image helper methods were retained.
- Additional decompiler artifacts were corrected in the isolated recovery worktree, including HTML tree traversal, JSON string escaping, URL decompression handling, URL host parsing, image redirects, and Java variable/lambda reconstruction.

These are source reconstructions, not a byte-for-byte restoration of the original developer's source formatting or lost local history.

## Build environment

A minimal Android SDK subset was created at ~/Android/Sdk (about 42 MB), containing the Android API 35 platform jar and D8 compiler files. The build uses Termux aapt2, apksigner, Java, javac and Python.

The old GitHub build script pointed at a missing API 33 SDK path. The recovered CLI build script now uses the minimal local SDK and explicit version metadata.

## Verification

- JVM test suite: **28 passed, 0 failed**.
- Debug APK: built and signature-verified.
- Release APK: built and signature-verified.
- Both builds report package com.offread.app, version code 2, version name 1.0.0-rot1, min SDK 24 and target SDK 33.
- The original installed app remains installed and untouched.

## Signing limitation

A new local development keystore was generated for the recovered project. Its certificate SHA-256 is:

82a7e15f89e12d8e97181d7ea2f220ec1770b0f2e8718a8060e4a440d1d10427

This differs from the installed APK's certificate. No original OffRead keystore was found in the inspected Termux home directory. Therefore, the recovered APK cannot be installed as an in-place update over the existing installation with the current key.

## Backup locations

~~~text
~/Projects/OffRead-recovery/baseline/OffRead-installed-baseline.apk
~/Projects/OffRead-recovery/baseline/OffRead-source-history.bundle
~/Projects/OffRead-recovery/baseline/OffRead-source-main.tar.gz
~/Projects/OffRead-recovery/baseline/SHA256SUMS
~~~

The APK is backed up. The app's private database/data directory was not extracted; it remains with the installed application on the phone.

## Remaining validation

See KNOWN_LIMITATIONS.md and FEATURE_MATRIX.md. In particular, manual UI testing on a separate test installation, export/import testing, and signing-key decisions remain before replacing the installed app or calling this a production release.
