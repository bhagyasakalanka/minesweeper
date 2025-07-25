# Minesweeper App - Release Build Information

## Build Details
- **Build Date**: July 25, 2025
- **Build Type**: Release
- **File Format**: Android App Bundle (AAB)
- **File Size**: 2.0MB
- **Location**: `app/build/outputs/bundle/release/app-release.aab`

## App Metadata
- **Package Name**: com.b1zt.minesweeper
- **Version Code**: 1
- **Version Name**: 1.0.0
- **Target SDK**: 36 (Android 16)
- **Minimum SDK**: 24 (Android 7.0)

## Signing Information
- **Keystore**: minesweeper-keystore.jks
- **Alias**: minesweeper-key
- **Algorithm**: RSA 2048-bit
- **Validity**: 10,000 days

## Optimizations Applied
- ✅ Code obfuscation (R8)
- ✅ Resource shrinking
- ✅ APK size optimization
- ✅ Unused code removal

## Google Play Ready
This AAB file is ready for upload to Google Play Console.

## Commands Used
```bash
# Clean build
./gradlew clean

# Generate signed release bundle
./gradlew bundleRelease
```

## File Verification
```bash
# Check file type
file app/build/outputs/bundle/release/app-release.aab
# Output: Zip archive data, at least v0.0 to extract

# Check file size
ls -lh app/build/outputs/bundle/release/app-release.aab
# Output: 2.0M
```

---
**Status**: ✅ Ready for Google Play deployment
