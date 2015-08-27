# Simple Demo App to demonstrate data truncation in current OpenPGPApi lib


### Build with Gradle

1. Clone the project from GitHub
2. Get all external submodules with ``git submodule update --init --recursive``
3. Execute ``./gradlew build``
4. This builds TWO apks, one _"orig"_ with the original, unpatched OpenPGPApi
5. and one _"fixed"_ using  [my patched OpenPGPApi](https://github.com/mgeier63/openpgp-api)

### Demonstrate the bug

1. Make sure OpenKeyChain is installed
2. Now install the _orig_ app with ``adb install -r OpenPgpApiLibDemobuild/app/build/outputs/apk/app-orig-debug.apk``
3. run the app, selecte a key and then tap "Test..."
4. you should see "Data Truncated on N of 100 invocations"

### Verify the fix
1. Build openkeychain after having merged [this pull request](https://github.com/open-keychain/openpgp-api/pull/3)
2. Nowinstall the _fixed_ app with ``adb install -r OpenPgpApiLibDemobuild/app/build/outputs/apk/app-fixed-debug.apk``
3. run the app, selecte a key and then tap "Test..."
4. no data truncation any more, relax!
