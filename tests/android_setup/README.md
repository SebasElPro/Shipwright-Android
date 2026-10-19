# Android extractor setup regression test

Run from the repository root with JDK 17:

```sh
javac -d /tmp/soh-setup-tests \
  Android/app/src/main/java/com/dishii/soh/ExtractorAssetState.java \
  tests/android_setup/ExtractorAssetStateTest.java
java -cp /tmp/soh-setup-tests com.dishii.soh.ExtractorAssetStateTest
```

The checks cover first setup, safe migration from the completed 9.3.1 support-2
installation, independent shader updates, changed extractor content, missing
assets, invalid markers, and retry after an interrupted setup.

On-device upgrade checks should also confirm that an unchanged extractor file
and the user's ROM archive retain their timestamps and checksums after both a
code-only update and a support-archive refresh. Test in the separate preview app
before replacing the released application.
