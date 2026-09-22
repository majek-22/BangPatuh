from PIL import Image
import os
import math

sheet = Image.open('app/src/main/assets/source/icon_sheet.jpg').convert('RGBA')

crops = [
    (15, 55, 200, 245, 'bribery'),
    (215, 55, 200, 245, 'fraud'),
    (415, 55, 200, 245, 'money_laundering'),
    (615, 55, 200, 245, 'data_breach'),
    (815, 55, 200, 245, 'systemic_corruption'),
    (15, 325, 200, 275, 'official_document'),
    (215, 325, 200, 275, 'verified_approval'),
    (415, 325, 200, 275, 'valid_partnership'),
    (615, 325, 200, 275, 'certification'),
    (815, 325, 200, 275, 'verified_invoice'),
    (15, 615, 200, 245, 'false_alarm'),
    (215, 615, 200, 245, 'unverified_rumor'),
    (415, 615, 200, 245, 'honest_mistake'),
    (615, 615, 200, 245, 'compliance_shield'),
    (815, 615, 200, 245, 'bonus_shield')
]

os.makedirs('processed_art', exist_ok=True)
os.makedirs('processed_badge', exist_ok=True)

for x, y, w, h, name in crops:
    # 200x200 square for the badge area (excluding bottom text label)
    badge_img = sheet.crop((x, y, x + 200, y + 200)).copy()
    w_b, h_b = badge_img.size
    cx, cy = w_b / 2.0, h_b / 2.0
    radius = 82.0 # circular badge radius ~82px
    
    # 1. Circular badge version: perfectly circular mask around (cx, cy)
    circ_img = badge_img.copy()
    circ_pixels = circ_img.load()
    for py in range(h_b):
        for px in range(w_b):
            dist = math.hypot(px - cx, py - cy)
            if dist > radius:
                circ_pixels[px, py] = (0, 0, 0, 0)
            elif dist > radius - 1.5:
                # anti-aliasing
                alpha = int(255 * (radius - dist) / 1.5)
                r, g, b, _ = circ_pixels[px, py]
                circ_pixels[px, py] = (r, g, b, alpha)
    circ_img.save(f'processed_badge/{name}.png')

print("Created 15 circular badges!")
