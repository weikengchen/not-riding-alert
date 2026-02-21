import csv

with open("final_mapping.csv", "r", encoding="utf-8") as f:
    reader = csv.DictReader(f)
    data = list(reader)

print("    static {")
for row in data:
    unicode_str = row["unicode"]
    label = row["label"]
    color = row["color"]

    codepoint = int(unicode_str.replace("\\u", ""), 16)
    char = chr(codepoint)

    color_int = int(color.replace("#", ""), 16)

    print(
        f'        REPLACEMENT_TABLE.put("{char}", Component.literal("[{label}]").withStyle(Style.EMPTY.withColor(0x{color_int:06X})));'
    )
print("    }")
