import csv
from PIL import Image


def get_representative_color(image_path):
    img = Image.open(image_path).convert("RGBA")
    width, height = img.size
    middle_y = height // 2
    pixel = img.getpixel((0, middle_y))

    if pixel[3] < 128:
        return "#808080"

    return "#{:02X}{:02X}{:02X}".format(pixel[0], pixel[1], pixel[2])


def filename_to_label(filename):
    name = filename.replace("rank_", "").replace(".png", "")

    special_cases = {
        "dvcplus": "DVC+",
        "dvc": "DVC",
        "club33plus": "Club33+",
        "club33": "Club33",
        "d23plus": "D23+",
        "d23": "D23",
        "vipplus": "VIP+",
        "vip": "VIP",
        "passholderplus": "PassHolder+",
        "passholder": "PassHolder",
        "guestplusplusplus": "Guest+++",
        "guestplus": "Guest+",
        "guest": "Guest",
        "juniortourguide": "JuniorTourGuide",
        "tourguide": "TourGuide",
        "castmember": "CastMember",
        "pixelartist": "PixelArtist",
        "showtech": "ShowTech",
        "imagineer": "Imagineer",
        "developer": "Developer",
        "manager": "Manager",
        "character": "Character",
        "builder": "Builder",
        "operator": "Operator",
        "coordinator": "Coordinator",
        "trainee": "Trainee",
        "media": "Media",
        "discord": "Discord",
        "artist": "Artist",
        "director": "Director",
        "former": "Former",
    }

    if name in special_cases:
        return special_cases[name]

    parts = name.split("_")
    return "".join(part.capitalize() for part in parts)


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

    unicode_val = row["unicode"]
    label = filename_to_label(filename)
    color = get_representative_color(filename)

    results.append({"unicode": unicode_val, "label": label, "color": color})

with open("final_mapping.csv", "w", encoding="utf-8", newline="") as f:
    writer = csv.DictWriter(f, fieldnames=["unicode", "label", "color"])
    writer.writeheader()
    writer.writerows(results)

print(f"Generated final_mapping.csv with {len(results)} entries")
print("\nPreview:")
for r in results[:5]:
    print(f"  {r['unicode']}, {r['label']}, {r['color']}")
print("  ...")
