from PIL import Image, ImageDraw
import os
import math

src_img = Image.open('app/src/main/res/drawable/ic_mascot_art.jpg').convert('RGBA')
W, H = src_img.size

# Target brand color: #00A6E9 -> (0, 166, 233) or mascot blue (1, 157, 218)
BG_COLOR = (0, 166, 233, 255) # BrandPrimary

# Scale character down so height is ~66% of 1024 (i.e. ~675px)
target_char_h = int(1024 * 0.66)
# Original character spans roughly y: 77 to 949 (height ~872)
scale = target_char_h / 872.0
scaled_w = int(W * scale)
scaled_h = int(H * scale)

scaled_img = src_img.resize((scaled_w, scaled_h), Image.Resampling.LANCZOS)

# Create 1024x1024 canvas filled with BG_COLOR
canvas = Image.new('RGBA', (1024, 1024), BG_COLOR)
offset_x = (1024 - scaled_w) // 2
# Center character vertically: character center in original is (77 + 949) / 2 = 513
offset_y = (1024 - scaled_h) // 2

# We blend the edges of scaled_img into BG_COLOR smoothly so there is zero seam
# Sample border color of scaled_img
pixels = scaled_img.load()
sw, sh = scaled_img.size
for y in range(sh):
    for x in range(sw):
        # distance to edge of scaled_img
        dist_edge = min(x, sw - 1 - x, y, sh - 1 - y)
        if dist_edge < 30:
            factor = dist_edge / 30.0
            r, g, b, _ = pixels[x, y]
            nr = int(r * factor + BG_COLOR[0] * (1 - factor))
            ng = int(g * factor + BG_COLOR[1] * (1 - factor))
            nb = int(b * factor + BG_COLOR[2] * (1 - factor))
            pixels[x, y] = (nr, ng, nb, 255)

canvas.paste(scaled_img, (offset_x, offset_y))

# Save high-res foreground
os.makedirs('app/src/main/res/drawable-nodpi', exist_ok=True)
canvas.save('app/src/main/res/drawable-nodpi/ic_launcher_foreground.png')
canvas.save('app/src/main/res/drawable/ic_launcher_foreground.png')

# Generate mipmap densities for legacy fallback icons and adaptive foregrounds
densities = {
    'mipmap-mdpi': (48, 108),
    'mipmap-hdpi': (72, 162),
    'mipmap-xhdpi': (96, 216),
    'mipmap-xxhdpi': (144, 324),
    'mipmap-xxxhdpi': (192, 432),
}

for folder, (legacy_size, adaptive_size) in densities.items():
    dir_path = os.path.join('app/src/main/res', folder)
    os.makedirs(dir_path, exist_ok=True)
    
    # 1. Legacy ic_launcher.png (square with subtle rounded corners)
    legacy = canvas.resize((legacy_size, legacy_size), Image.Resampling.LANCZOS)
    legacy.save(os.path.join(dir_path, 'ic_launcher.png'))
    
    # 2. Legacy ic_launcher_round.png (circular mask)
    round_icon = legacy.copy()
    mask = Image.new('L', (legacy_size, legacy_size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, legacy_size, legacy_size), fill=255)
    round_icon.putalpha(mask)
    round_icon.save(os.path.join(dir_path, 'ic_launcher_round.png'))

print("Generated adaptive launcher icon and legacy icons across all mipmap folders!")
