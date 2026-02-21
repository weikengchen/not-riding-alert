import csv
import os
from PIL import Image


def get_representative_color(image_path):
    img = Image.open(image_path).convert("RGBA")
    pixel = img.getpixel((0, 0))

    if pixel[3] < 128:
        return (128, 128, 128)

    return (pixel[0], pixel[1], pixel[2])


def rgb_to_hex(rgb):
    return "#{:02X}{:02X}{:02X}".format(rgb[0], rgb[1], rgb[2])


with open("mapping.csv", "r", encoding="utf-8") as f:
    reader = csv.DictReader(f)
    data = list(reader)

results = []
seen = set()
for row in data:
    filename = row["filename"]
    if filename in seen:
        continue
    seen.add(filename)

    color = get_representative_color(filename)
    hex_color = rgb_to_hex(color)

    results.append(
        {
            "filename": filename,
            "char": row["char"],
            "unicode": row["unicode"],
            "rgb": color,
            "hex": hex_color,
        }
    )

with open("colors.html", "w", encoding="utf-8") as f:
    f.write("""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PNG Color Extraction</title>
    <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        h1 { margin-bottom: 20px; }
        .item {
            display: flex;
            align-items: center;
            margin: 10px 0;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 8px;
        }
        .img-container {
            width: 80px;
            height: 32px;
            background: #333;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 4px;
        }
        .img-container img {
            max-width: 100%;
            max-height: 100%;
        }
        .color-box {
            width: 60px;
            height: 32px;
            margin: 0 15px;
            border-radius: 4px;
            border: 1px solid #000;
        }
        .info {
            flex: 1;
        }
        .filename { font-weight: bold; }
        .char { font-size: 1.5em; margin: 0 10px; }
        .unicode { color: #666; font-family: monospace; }
        .rgb { color: #666; margin-left: 10px; }
    </style>
</head>
<body>
    <h1>PNG Color Extraction</h1>
""")

    for r in results:
        f.write(f'''
    <div class="item">
        <div class="img-container">
            <img src="{r["filename"]}" alt="{r["filename"]}">
        </div>
        <div class="color-box" style="background-color: {r["hex"]};" title="{r["hex"]}"></div>
        <div class="info">
            <span class="filename">{r["filename"]}</span>
            <span class="char">{r["char"]}</span>
            <span class="unicode">{r["unicode"]}</span>
            <span class="rgb">RGB: {r["rgb"][0]}, {r["rgb"][1]}, {r["rgb"][2]}</span>
        </div>
    </div>
''')

    f.write("""
</body>
</html>
""")

print(f"Generated colors.html with {len(results)} entries")
print("\nPreview:")
for r in results[:5]:
    print(f"  {r['filename']}: {r['hex']} (RGB: {r['rgb']})")
print("  ...")
