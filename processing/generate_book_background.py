#!/usr/bin/env python3
"""
Generate a book background texture for the Tutorial Wizard.
Design: iPad-like content area with book-style decorative border.
Dimensions: 512x320 pixels
"""

from PIL import Image, ImageDraw, ImageFilter
import math

# Dimensions
WIDTH = 512
HEIGHT = 320

# Colors
BORDER_LEATHER = (101, 67, 33)  # Dark brown leather
BORDER_LEATHER_LIGHT = (139, 90, 43)  # Lighter brown for highlights
GOLD = (218, 165, 32)  # Gold for corners
GOLD_DARK = (184, 134, 11)  # Darker gold for shadow
PARCHMENT = (250, 242, 218)  # Light parchment
PARCHMENT_DARK = (235, 225, 195)  # Slightly darker for subtle texture
SHADOW = (0, 0, 0, 60)  # Drop shadow

# Layout
BORDER_WIDTH = 18
CORNER_SIZE = 24
SHADOW_SIZE = 8
CORNER_RADIUS = 12


def create_book_background():
    # Create main image with alpha channel
    img = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Calculate inner area
    inner_x = BORDER_WIDTH
    inner_y = BORDER_WIDTH
    inner_width = WIDTH - 2 * BORDER_WIDTH
    inner_height = HEIGHT - 2 * BORDER_WIDTH

    # Draw drop shadow (offset rectangle with blur)
    shadow_img = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow_img)
    shadow_offset = SHADOW_SIZE
    shadow_rect = [
        shadow_offset,
        shadow_offset,
        WIDTH - 1 + shadow_offset,
        HEIGHT - 1 + shadow_offset,
    ]
    shadow_draw.rounded_rectangle(shadow_rect, radius=CORNER_RADIUS, fill=SHADOW)
    shadow_img = shadow_img.filter(ImageFilter.GaussianBlur(radius=4))

    # Composite shadow onto main image
    img = Image.alpha_composite(img, shadow_img)
    draw = ImageDraw.Draw(img)

    # Draw main border (leather) with rounded corners
    border_rect = [0, 0, WIDTH - 1, HEIGHT - 1]
    draw.rounded_rectangle(border_rect, radius=CORNER_RADIUS, fill=BORDER_LEATHER)

    # Add subtle leather texture lines
    for i in range(BORDER_WIDTH, WIDTH - BORDER_WIDTH, 3):
        for j in range(0, BORDER_WIDTH, 2):
            alpha = 20 + (j % 3) * 10
            draw.point((i, j), fill=(*BORDER_LEATHER_LIGHT, alpha))
            draw.point((i, HEIGHT - 1 - j), fill=(*BORDER_LEATHER_LIGHT, alpha))

    for j in range(BORDER_WIDTH, HEIGHT - BORDER_WIDTH, 3):
        for i in range(0, BORDER_WIDTH, 2):
            alpha = 20 + (i % 3) * 10
            draw.point((i, j), fill=(*BORDER_LEATHER_LIGHT, alpha))
            draw.point((WIDTH - 1 - i, j), fill=(*BORDER_LEATHER_LIGHT, alpha))

    # Draw inner content area (parchment)
    inner_rect = [
        inner_x,
        inner_y,
        inner_x + inner_width - 1,
        inner_y + inner_height - 1,
    ]
    draw.rounded_rectangle(inner_rect, radius=6, fill=PARCHMENT)

    # Add subtle parchment texture
    for i in range(inner_x, inner_x + inner_width):
        for j in range(inner_y, inner_y + inner_height):
            # Check if inside rounded rectangle
            if (
                (i < inner_x + 6 and j < inner_y + 6)
                or (i > inner_x + inner_width - 7 and j < inner_y + 6)
                or (i < inner_x + 6 and j > inner_y + inner_height - 7)
                or (i > inner_x + inner_width - 7 and j > inner_y + inner_height - 7)
            ):
                continue  # Skip corners

            # Add subtle noise
            noise = ((i * 7 + j * 13) % 15) - 7
            r = max(0, min(255, PARCHMENT[0] + noise))
            g = max(0, min(255, PARCHMENT[1] + noise))
            b = max(0, min(255, PARCHMENT[2] + noise))
            img.putpixel((i, j), (r, g, b, 255))

    # Draw gold corner decorations
    draw_gold_corners(draw)

    # Draw inner border line (gold)
    inner_border_rect = [
        inner_x - 2,
        inner_y - 2,
        inner_x + inner_width + 1,
        inner_y + inner_height + 1,
    ]
    draw.rounded_rectangle(inner_border_rect, radius=7, outline=GOLD, width=2)

    return img


def draw_gold_corners(draw):
    """Draw decorative gold corners on the border."""

    # Top-left corner
    draw_corner(draw, 4, 4, CORNER_SIZE, CORNER_SIZE, "tl")

    # Top-right corner
    draw_corner(draw, WIDTH - 4 - CORNER_SIZE, 4, CORNER_SIZE, CORNER_SIZE, "tr")

    # Bottom-left corner
    draw_corner(draw, 4, HEIGHT - 4 - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, "bl")

    # Bottom-right corner
    draw_corner(
        draw,
        WIDTH - 4 - CORNER_SIZE,
        HEIGHT - 4 - CORNER_SIZE,
        CORNER_SIZE,
        CORNER_SIZE,
        "br",
    )


def draw_corner(draw, x, y, w, h, position):
    """Draw a decorative corner piece."""

    # Determine direction based on position
    if position == "tl":
        # Diamond shape pointing outward
        points = [
            (x, y + h // 2),  # Left
            (x + w // 2, y),  # Top
            (x + w, y + h // 3),  # Right inner
            (x + w // 2, y + h // 2),  # Center
            (x + w // 3, y + h),  # Bottom inner
        ]
    elif position == "tr":
        points = [
            (x + w // 2, y),  # Top
            (x + w, y + h // 2),  # Right
            (x + w - w // 3, y + h),  # Bottom inner
            (x + w // 2, y + h // 2),  # Center
            (x, y + h // 3),  # Left inner
        ]
    elif position == "bl":
        points = [
            (x, y + h // 2),  # Left
            (x + w // 3, y),  # Top inner
            (x + w // 2, y + h // 2),  # Center
            (x + w, y + h - h // 3),  # Right inner
            (x + w // 2, y + h),  # Bottom
        ]
    else:  # 'br'
        points = [
            (x + w // 2, y + h),  # Bottom
            (x + w, y + h // 2),  # Right
            (x + w - w // 3, y),  # Top inner
            (x + w // 2, y + h // 2),  # Center
            (x, y + h - h // 3),  # Left inner
        ]

    # Draw filled polygon
    draw.polygon(points, fill=GOLD, outline=GOLD_DARK)

    # Add highlight
    if position == "tl":
        draw.line(
            [(x + 2, y + h // 2 - 2), (x + w // 2, y + 2)], fill=(255, 215, 0), width=1
        )
    elif position == "tr":
        draw.line(
            [(x + w - 2, y + h // 2 - 2), (x + w // 2, y + 2)],
            fill=(255, 215, 0),
            width=1,
        )
    elif position == "bl":
        draw.line(
            [(x + 2, y + h // 2 + 2), (x + w // 2, y + h - 2)],
            fill=(255, 215, 0),
            width=1,
        )
    else:  # 'br'
        draw.line(
            [(x + w - 2, y + h // 2 + 2), (x + w // 2, y + h - 2)],
            fill=(255, 215, 0),
            width=1,
        )


def main():
    print("Generating book background texture...")
    print(f"Dimensions: {WIDTH}x{HEIGHT}")

    img = create_book_background()

    # Save to output path
    output_path = "../src/main/resources/assets/not-riding-alert/textures/gui/tutorial/book_background.png"
    img.save(output_path, "PNG")
    print(f"Saved to: {output_path}")

    # Also save a preview in processing folder
    preview_path = "book_background_preview.png"
    img.save(preview_path, "PNG")
    print(f"Preview saved to: {preview_path}")

    print("Done!")


if __name__ == "__main__":
    main()
