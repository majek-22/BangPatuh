from PIL import Image
import os
import math
from collections import deque

sheet_path = 'app/src/main/assets/source/icon_sheet.jpg'
sheet = Image.open(sheet_path).convert('RGBA')

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

out_drawable = 'app/src/main/res/drawable'
out_xxhdpi = 'app/src/main/res/drawable-xxhdpi'
os.makedirs(out_drawable, exist_ok=True)
os.makedirs(out_xxhdpi, exist_ok=True)

def color_dist(c1, c2):
    return math.sqrt((c1[0]-c2[0])**2 + (c1[1]-c2[1])**2 + (c1[2]-c2[2])**2)

for x, y, w, h, name in crops:
    # 200x200 region containing badge
    badge_img = sheet.crop((x, y, x + 200, y + 200)).copy()
    bw, bh = badge_img.size
    pixels = badge_img.load()
    
    # 1. Clear everything outside the circular badge (center 100, 100, radius 82)
    cx, cy = 100.0, 100.0
    radius = 82.0
    for py in range(bh):
        for px in range(bw):
            dist = math.hypot(px - cx, py - cy)
            if dist > radius:
                pixels[px, py] = (0, 0, 0, 0)
            elif dist > radius - 1.5:
                alpha = int(255 * (radius - dist) / 1.5)
                r, g, b, _ = pixels[px, py]
                pixels[px, py] = (r, g, b, alpha)
                
    # 2. Find circle background color by sampling perimeter around r=70
    perimeter_colors = []
    for a_deg in range(0, 360, 8):
        rad = math.radians(a_deg)
        sx = int(cx + 70 * math.cos(rad))
        sy = int(cy + 70 * math.sin(rad))
        p = pixels[sx, sy]
        if p[3] > 200:
            perimeter_colors.append((sx, sy, p[:3]))
            
    # Cluster perimeter colors
    clusters = []
    for sx, sy, c in perimeter_colors:
        matched = False
        for cl in clusters:
            if color_dist(c, cl['center']) < 25:
                cl['points'].append((sx, sy))
                cl['colors'].append(c)
                matched = True
                break
        if not matched:
            clusters.append({'center': c, 'points': [(sx, sy)], 'colors': [c]})
            
    clusters.sort(key=lambda cl: len(cl['points']), reverse=True)
    badge_bg = clusters[0]
    target_c = badge_bg['center']
    
    # Flood-fill remove the flat badge background color
    visited = set()
    queue = deque()
    for sx, sy in badge_bg['points']:
        queue.append((sx, sy))
        visited.add((sx, sy))
        
    while queue:
        qx, qy = queue.popleft()
        p = pixels[qx, qy]
        if p[3] == 0:
            continue
        if color_dist(p[:3], target_c) < 32:
            pixels[qx, qy] = (0, 0, 0, 0)
            for nx, ny in [(qx+1, qy), (qx-1, qy), (qx, qy+1), (qx, qy-1)]:
                if 0 <= nx < bw and 0 <= ny < bh and (nx, ny) not in visited:
                    visited.add((nx, ny))
                    queue.append((nx, ny))
                    
    # Save to both drawable and drawable-xxhdpi
    badge_img.save(os.path.join(out_drawable, f'{name}.png'))
    badge_img.save(os.path.join(out_xxhdpi, f'{name}.png'))
    
    # Check stats
    bbox = badge_img.getbbox()
    opaque_count = sum(1 for p in badge_img.getdata() if p[3] > 0)
    print(f'{name:20}: target_c={target_c}, bbox={bbox}, count={opaque_count}')

print("Successfully generated all 15 transparent icon drawables!")
