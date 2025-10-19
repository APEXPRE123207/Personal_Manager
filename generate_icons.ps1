# PowerShell Script to Generate Android Launcher Icons from logo.png
# Run this script from the project root directory

$logoPath = "logo.png"
$resPath = "app\src\main\res"

# Check if logo exists
if (-Not (Test-Path $logoPath)) {
    Write-Host "Error: logo.png not found in current directory!" -ForegroundColor Red
    exit 1
}

Write-Host "Found logo.png - Generating launcher icons..." -ForegroundColor Green

# Define icon sizes for different densities
$iconSizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

# Check if ImageMagick is installed
$magickInstalled = $false
try {
    $magickVersion = magick -version 2>$null
    if ($LASTEXITCODE -eq 0) {
        $magickInstalled = $true
        Write-Host "ImageMagick detected - will generate icons automatically" -ForegroundColor Green
    }
} catch {
    Write-Host "ImageMagick not found - you'll need to use Android Studio's Image Asset Studio" -ForegroundColor Yellow
}

if ($magickInstalled) {
    # Generate icons for each density
    foreach ($density in $iconSizes.Keys) {
        $size = $iconSizes[$density]
        $outputDir = Join-Path $resPath $density
        
        # Create directory if it doesn't exist
        if (-Not (Test-Path $outputDir)) {
            New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
        }
        
        $outputFile = Join-Path $outputDir "ic_launcher.png"
        $outputRoundFile = Join-Path $outputDir "ic_launcher_round.png"
        
        Write-Host "Generating ${density} (${size}x${size})..." -ForegroundColor Cyan
        
        # Generate regular launcher icon
        magick convert $logoPath -resize "${size}x${size}" -background none -gravity center -extent "${size}x${size}" $outputFile
        
        # Generate round launcher icon
        magick convert $logoPath -resize "${size}x${size}" -background none -gravity center -extent "${size}x${size}" `
            \( +clone -alpha extract -draw "fill black polygon 0,0 0,$size $size,$size $size,0" -blur 0x8 \) `
            -compose CopyOpacity -composite $outputRoundFile
    }
    
    Write-Host "`nLauncher icons generated successfully!" -ForegroundColor Green
    Write-Host "Note: You may still want to use Android Studio's Image Asset Studio for adaptive icons (Android 8.0+)" -ForegroundColor Yellow
    
} else {
    Write-Host "`n=== ImageMagick Not Installed ===" -ForegroundColor Yellow
    Write-Host "To generate icons automatically, install ImageMagick:"
    Write-Host "1. Download from: https://imagemagick.org/script/download.php#windows" -ForegroundColor Cyan
    Write-Host "2. Run the installer and make sure 'Add to PATH' is checked"
    Write-Host "3. Restart PowerShell and run this script again"
    Write-Host "`nAlternatively, use Android Studio's Image Asset Studio:" -ForegroundColor Yellow
    Write-Host "1. Right-click on app/src/main/res in Android Studio" -ForegroundColor Cyan
    Write-Host "2. Select: New > Image Asset"
    Write-Host "3. Choose logo.png as the source"
    Write-Host "4. Click 'Next' then 'Finish'"
    Write-Host "`nSee LAUNCHER_ICON_SETUP.md for detailed instructions" -ForegroundColor Green
}

Write-Host "`nPress any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
