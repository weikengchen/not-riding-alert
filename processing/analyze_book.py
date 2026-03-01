#!/usr/bin/env python3
"""
Analyze book.png to find the drawable content area.
Detects the border vs inner content region by analyzing pixel colors.
"""

from PIL import Image
import os


def analyze_book_image(image_path):
    img = Image.open(image_path)
    width, height = img.size
    print(f"Image dimensions: {width}x{height}")

    # Convert to RGB if needed
    if img.mode == "RGBA":
        pixels = img.load()
    else:
        img = img.convert("RGBA")
        pixels = img.load()

    # Sample pixels to find border vs content
    # Strategy: Scan from edges inward to find where the "paper" content starts

    # Sample the center to get content color
    center_x, center_y = width // 2, height // 2
    center_color = pixels[center_x, center_y]
    print(f"Center pixel color: {center_color}")

    # Sample corners to get border color
    corner_colors = [
        pixels[10, 10],
        pixels[width - 10, 10],
        pixels[10, height - 10],
        pixels[width - 10, height - 10],
    ]
    print(f"Corner colors (sample at 10,10): {corner_colors[0]}")

    # Find content area by scanning from edges
    # Top edge
    top_border = 0
    for y in range(height):
        # Sample a few points across this row
        sample_colors = [
            pixels[width // 4, y],
            pixels[width // 2, y],
            pixels[3 * width // 4, y],
        ]
        # Check if this row looks like content (similar to center) or border
        avg_diff = (
            sum(abs(c[i] - center_color[i]) for c in sample_colors for i in range(3))
            / 9
        )
        if avg_diff < 50:  # Close enough to center color = content
            top_border = y
            break

    # Bottom edge
    bottom_border = height
    for y in range(height - 1, -1, -1):
        sample_colors = [
            pixels[width // 4, y],
            pixels[width // 2, y],
            pixels[3 * width // 4, y],
        ]
        avg_diff = (
            sum(abs(c[i] - center_color[i]) for c in sample_colors for i in range(3))
            / 9
        )
        if avg_diff < 50:
            bottom_border = y
            break

    # Left edge
    left_border = 0
    for x in range(width):
        sample_colors = [
            pixels[x, height // 4],
            pixels[x, height // 2],
            pixels[x, 3 * height // 4],
        ]
        avg_diff = (
            sum(abs(c[i] - center_color[i]) for c in sample_colors for i in range(3))
            / 9
        )
        if avg_diff < 50:
            left_border = x
            break

    # Right edge
    right_border = width
    for x in range(width - 1, -1, -1):
        sample_colors = [
            pixels[x, height // 4],
            pixels[x, height // 2],
            pixels[x, 3 * height // 4],
        ]
        avg_diff = (
            sum(abs(c[i] - center_color[i]) for c in sample_colors for i in range(3))
            / 9
        )
        if avg_diff < 50:
            right_border = x
            break

    content_width = right_border - left_border + 1
    content_height = bottom_border - top_border + 1

    print(f"\nContent area detection:")
    print(f"  Top border: {top_border}px")
    print(f"  Bottom border: {height - bottom_border - 1}px (from bottom)")
    print(f"  Left border: {left_border}px")
    print(f"  Right border: {width - right_border - 1}px (from right)")
    print(f"\n  Content area: {content_width}x{content_height}")
    print(f"  Content offset: ({left_border}, {top_border})")

    return {
        "width": width,
        "height": height,
        "content_x": left_border,
        "content_y": top_border,
        "content_width": content_width,
        "content_height": content_height,
        "border_top": top_border,
        "border_bottom": height - bottom_border - 1,
        "border_left": left_border,
        "border_right": width - right_border - 1,
    }


def copy_to_destination(src_path, dest_path):
    img = Image.open(src_path)
    os.makedirs(os.path.dirname(dest_path), exist_ok=True)
    img.save(dest_path)
    print(f"\nCopied to: {dest_path}")


if __name__ == "__main__":
    src = "../book.png"
    dest = "../src/main/resources/assets/not-riding-alert/textures/gui/tutorial/book_background.png"

    result = analyze_book_image(src)
    copy_to_destination(src, dest)

    print(f"\n=== Summary for WizardScreen.java ===")
    print(f"BOOK_WIDTH = {result['width']};")
    print(f"BOOK_HEIGHT = {result['height']};")
    print(f"CONTENT_X = {result['content_x']};  // offset from book left")
    print(f"CONTENT_Y = {result['content_y']};  // offset from book top")
    print(f"CONTENT_WIDTH = {result['content_width']};")
    print(f"CONTENT_HEIGHT = {result['content_height']};")
