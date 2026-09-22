from PIL import Image
import math
from collections import deque
import os

crops = [
    'bribery', 'fraud', 'money_laundering', 'data_breach', 'systemic_corruption',
    'official_document', 'verified_approval', 'valid_partnership', 'certification', 'verified_invoice',
    'false_alarm', 'unverified_rumor', 'honest_mistake', 'compliance_shield', 'bonus_shield'
]

os.makedirs('processed_art', exist_ok=True)

def color_dist(c1, c2):
    return math.sqrt((c1[0]-c2[0])**2 + (c1[1]-c2[1])**2 + (c1[2]-c2[2])**2)

for name in crops:
    badge = Image.open(f'processed_badge/{name}.png').convert('RGBA')
    w, h = badge.size
    pixels = badge.load()
    
    perimeter_colors = []
    for a_deg in range(0, 360, 10):
        rad = math.radians(a_deg)
        sx = int(100 + 72 * math.cos(rad))
        sy = int(100 + 72 * math.sin(rad))
        p = pixels[sx, sy]
        if p[3] > 100:
            perimeter_colors.append((sx, sy, p[:3]))
            
    clusters = []
    for sx, sy, c in perimeter_colors:
        found = False
        for cl in clusters:
            if color_dist(c, cl['center']) < 25:
                cl['points'].append((sx, sy))
                cl['colors'].append(c)
                found = True
                break
        if not found:
            clusters.append({'center': c, 'points': [(sx, sy)], 'colors': [c]})
            
    clusters.sort(key=lambda cl: len(cl['points']), reverse=True)
    badge_bg_cluster = clusters[0]
    
    visited = set()
    queue = deque()
    
    target_color = badge_bg_cluster['center']
    for sx, sy in badge_bg_cluster['points']:
        queue.append((sx, sy))
        visited.add((sx, sy))
        
    while queue:
        x, y = queue.popleft()
        p = pixels[x, y]
        if p[3] == 0:
            continue
        if color_dist(p[:3], target_color) < 35:
            pixels[x, y] = (0, 0, 0, 0)
            for nx, ny in [(x+1, y), (x-1, y), (x, y+1), (x, y-1)]:
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in visited:
                    visited.add((nx, ny))
                    queue.append((nx, ny))
                    
    badge.save(f'processed_art/{name}.png')
    bbox = badge.getbbox()
    count = sum(1 for p in badge.getdata() if p[3] > 0)
    print(f'{name:20}: bbox={bbox}, count={count}')

