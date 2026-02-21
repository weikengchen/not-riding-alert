import csv
import os

with open("mapping.csv", "r", encoding="utf-8") as f:
    reader = csv.DictReader(f)
    csv_filenames = set(row["filename"] for row in reader)

folder_files = set(
    f for f in os.listdir(".") if f.startswith("rank_") and f.endswith(".png")
)

in_csv_not_folder = csv_filenames - folder_files
in_folder_not_csv = folder_files - csv_filenames

print(f"Files in CSV: {len(csv_filenames)}")
print(f"Files in folder: {len(folder_files)}")
print()

if in_csv_not_folder:
    print(f"In CSV but not in folder ({len(in_csv_not_folder)}):")
    for f in sorted(in_csv_not_folder):
        print(f"  {f}")
else:
    print("No files in CSV missing from folder.")

print()

if in_folder_not_csv:
    print(f"In folder but not in CSV ({len(in_folder_not_csv)}):")
    for f in sorted(in_folder_not_csv):
        print(f"  {f}")
else:
    print("No files in folder missing from CSV.")
