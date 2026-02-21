import csv

with open("mapping.csv", "r", encoding="utf-8") as f:
    reader = csv.DictReader(f)
    all_ok = True
    for row in reader:
        filename = row["filename"]
        char = row["char"]
        unicode_str = row["unicode"]

        expected_codepoint = int(unicode_str.replace("\\u", ""), 16)
        actual_codepoint = ord(char)

        if expected_codepoint == actual_codepoint:
            print(f"OK: {filename} - '{char}' -> U+{actual_codepoint:04X}")
        else:
            print(
                f"MISMATCH: {filename} - '{char}' expected U+{expected_codepoint:04X}, got U+{actual_codepoint:04X}"
            )
            all_ok = False

print()
if all_ok:
    print("All mappings verified successfully!")
else:
    print("Some mappings have errors.")
