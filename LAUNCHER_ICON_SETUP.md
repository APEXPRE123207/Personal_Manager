# FLOW App Launcher Icon Setup

## Automatic Method (Using Android Studio - RECOMMENDED)

1. **Open Android Studio** and load the PersonalManager project

2. **Right-click** on `app/src/main/res` folder in the Project view

3. **Select**: New → Image Asset

4. **Configure the Asset Studio**:
   - **Icon Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: `ic_launcher`
   - **Foreground Layer**:
     - **Source Asset**: Image
     - **Path**: Click folder icon and browse to `w:\GIT\PersonalManager\logo.png`
     - **Resize**: Adjust the slider to fit your logo properly (usually 60-80%)
   - **Background Layer**:
     - **Source Asset**: Color
     - **Color**: Choose a solid color that complements your logo (or use Image if you want)
   - **Legacy Icon**: Enable "Legacy" tab and configure similarly

5. **Click "Next"** then **"Finish"**

Android Studio will automatically generate all required icon sizes:
- mipmap-hdpi (72x72)
- mipmap-mdpi (48x48)
- mipmap-xhdpi (96x96)
- mipmap-xxhdpi (144x144)
- mipmap-xxxhdpi (192x192)
- mipmap-anydpi-v26 (adaptive icons for Android 8.0+)

## Manual Method (If Android Studio is not available)

### Using Online Tool:
1. Go to: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
2. Upload `logo.png`
3. Adjust padding and background
4. Download the generated icon pack
5. Extract and copy all folders to `app/src/main/res/`

### Using ImageMagick (Command Line):
```powershell
# Install ImageMagick first if not available

# Generate different sizes
magick convert logo.png -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.png
magick convert logo.png -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.png
magick convert logo.png -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.png
magick convert logo.png -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.png
magick convert logo.png -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
```

## Verification

After generating the icons, verify:
1. All mipmap folders contain `ic_launcher.png` and `ic_launcher_round.png`
2. The `mipmap-anydpi-v26` folder has XML files for adaptive icons
3. Clean and rebuild the project
4. Run the app to see the new icon

## Current Status

✅ App name changed to "FLOW" in strings.xml
✅ Logo copied to drawable folder as app_logo.png
✅ Splash screen updated with new branding
✅ Dashboard updated with "FLOW" branding
⏳ Launcher icons need to be generated (follow steps above)

## Notes

- The logo.png is 633KB which is quite large - Android Studio will optimize it
- For best results, ensure your logo has a transparent background
- Adaptive icons (Android 8.0+) work best with simple, centered designs
- Consider creating a separate background layer for adaptive icons if needed
