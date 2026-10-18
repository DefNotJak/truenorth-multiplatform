# Compose App Store screenshots in the same style as the Play Store set:
# a red gradient ground, a letter-spaced TRUENORTH wordmark, a heavy white
# caption, and the device bleeding off the bottom edge.
#
#   powershell -File tools/make-store-screenshots.ps1
#
# Input  : icon-source/App Store Screenshots/      (raw 1206x2622 simulator captures)
# Output : icon-source/App Store Screenshots Framed/  (01.png .. 06.png)
#
# 1206x2622 is the "iPhone with Dynamic Island (medium display)" size, which
# since October 2026 is the only iPhone screenshot size Apple requires. Capture
# on an iPhone 17 Pro, NOT a Pro Max - the Pro Max is 1320x2868 and is rejected.

Add-Type -AssemblyName System.Drawing

$repo = Split-Path -Parent $PSScriptRoot
$src  = Join-Path $repo "icon-source\App Store Screenshots"
$out  = Join-Path $repo "icon-source\App Store Screenshots Framed"
New-Item -ItemType Directory -Force -Path $out | Out-Null

# Matched to the Play Store set by sampling its background.
$topColor    = [System.Drawing.Color]::FromArgb(224,51,42)   # #E0332A
$bottomColor = [System.Drawing.Color]::FromArgb(167,21,25)   # #A71519

# Each entry picks a source file by the timestamp in its name.
$picks = @(
  @{ f="17.58.18"; c="589 Real Citizenship Questions" },
  @{ f="17.58.37"; c="Flash Cards For Every Category" },
  @{ f="17.58.06"; c="Every Answer Explained" },
  @{ f="17.57.43"; c="Know When You're Ready" },
  @{ f="17.58.48"; c="See Exactly What Needs Work" },
  @{ f="17.59.09"; c="Know What Test Day Looks Like" }
)

# PowerShell variables are case-insensitive, so the canvas width must not be
# called $w - a loop variable of that name will silently overwrite it.
$canvasW = 1206; $canvasH = 2622
$capFamily = "Segoe UI Black"
if (-not ((New-Object System.Drawing.Text.InstalledFontCollection).Families.Name -contains $capFamily)) { $capFamily = "Segoe UI" }

$idx = 1
foreach ($pick in $picks) {
  $file = Get-ChildItem $src -File | Where-Object { $_.Name -like "*$($pick.f)*" } | Select-Object -First 1
  if (-not $file) { Write-Warning "no source matching $($pick.f)"; continue }
  $shot = New-Object System.Drawing.Bitmap($file.FullName)

  $bmp = New-Object System.Drawing.Bitmap($canvasW,$canvasH,[System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
  $gfx = [System.Drawing.Graphics]::FromImage($bmp)
  $gfx.SmoothingMode       = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $gfx.InterpolationMode   = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
  $gfx.PixelOffsetMode     = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
  $gfx.TextRenderingHint   = [System.Drawing.Text.TextRenderingHint]::AntiAlias

  $rect = New-Object System.Drawing.Rectangle(0,0,$canvasW,$canvasH)
  $gfx.FillRectangle((New-Object System.Drawing.Drawing2D.LinearGradientBrush($rect,$topColor,$bottomColor,90.0)),$rect)
  $white = [System.Drawing.Brushes]::White

  # wordmark, drawn per character to fake letter-spacing
  $markFont = New-Object System.Drawing.Font("Segoe UI",30,[System.Drawing.FontStyle]::Bold)
  $track = 16.0
  $totalW = 0.0
  foreach ($chr in "TRUENORTH".ToCharArray()) { $totalW += $gfx.MeasureString([string]$chr,$markFont).Width - 6 + $track }
  $penX = ($canvasW/2.0) - ($totalW/2.0)
  foreach ($chr in "TRUENORTH".ToCharArray()) {
    $gfx.DrawString([string]$chr,$markFont,$white,$penX,96)
    $penX += $gfx.MeasureString([string]$chr,$markFont).Width - 6 + $track
  }
  $markFont.Dispose()

  # caption, wrapped to at most two lines
  $capFont = New-Object System.Drawing.Font($capFamily,56,[System.Drawing.FontStyle]::Bold)
  $maxW = $canvasW*0.84
  $lines = New-Object System.Collections.ArrayList
  $cur = ""
  foreach ($word in $pick.c.Split(' ')) {
    $attempt = if ($cur -eq "") { $word } else { "$cur $word" }
    if ($gfx.MeasureString($attempt,$capFont).Width -le $maxW) { $cur = $attempt }
    else { [void]$lines.Add($cur); $cur = $word }
  }
  if ($cur -ne "") { [void]$lines.Add($cur) }
  $lineY = 185.0
  foreach ($line in $lines) {
    $lineW = $gfx.MeasureString($line,$capFont).Width
    $gfx.DrawString($line,$capFont,$white,(($canvasW-$lineW)/2.0),$lineY)
    $lineY += 92.0
  }
  $capFont.Dispose()

  # Device position is identical on every frame regardless of caption length,
  # so the carousel reads as one set rather than six separate images.
  $devW = [int]($canvasW*0.86)
  $devH = [int]($devW * ($shot.Height/[double]$shot.Width))
  $devX = [int](($canvasW-$devW)/2)
  $devY = 420
  $rad  = 56
  $path = New-Object System.Drawing.Drawing2D.GraphicsPath
  $path.AddArc($devX,$devY,($rad*2),($rad*2),180,90)
  $path.AddArc(($devX+$devW-$rad*2),$devY,($rad*2),($rad*2),270,90)
  $path.AddLine(($devX+$devW),($devY+$devH),$devX,($devY+$devH))
  $path.CloseFigure()
  $gfx.SetClip($path)
  $gfx.DrawImage($shot,(New-Object System.Drawing.Rectangle($devX,$devY,$devW,$devH)))
  $gfx.ResetClip()
  $pen = New-Object System.Drawing.Pen(([System.Drawing.Color]::FromArgb(90,255,255,255)),5)
  $gfx.DrawPath($pen,$path); $pen.Dispose(); $path.Dispose()

  $gfx.Dispose(); $shot.Dispose()
  $name = "{0:D2}.png" -f $idx
  $bmp.Save((Join-Path $out $name),[System.Drawing.Imaging.ImageFormat]::Png)
  $bmp.Dispose()
  "wrote $name  <- $($file.Name)"
  $idx++
}
