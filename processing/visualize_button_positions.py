#!/usr/bin/env python3
"""
Visualize button positions on the book background.
Edit the values below and run to see where buttons will be placed.
"""

from PIL import Image, ImageDraw, ImageFont

# Book dimensions (original texture size)
BOOK_WIDTH = 1024
BOOK_HEIGHT = 640

# ============================================================
# EDIT THESE VALUES TO DEFINE THE CONTENT AREA
# ============================================================

# Content area - where text and title appear
CONTENT_X = 160
CONTENT_Y = 90
CONTENT_WIDTH = 700
CONTENT_HEIGHT = 450
TEXT_MARGIN = 20

# Close button (X) - relative to book top-left
CLOSE_X = BOOK_WIDTH - 140
CLOSE_Y = 40
CLOSE_WIDTH = 30
CLOSE_HEIGHT = 30

# PageButton arrows (scaled proportionally from 23x13)
ARROW_WIDTH = 48
ARROW_HEIGHT = 27

# Back arrow position
BACK_X = CONTENT_X + TEXT_MARGIN
BACK_Y = CONTENT_Y + CONTENT_HEIGHT - 40

# Next arrow position
NEXT_X = CONTENT_X + CONTENT_WIDTH - TEXT_MARGIN - ARROW_WIDTH
NEXT_Y = CONTENT_Y + CONTENT_HEIGHT - 40

# ============================================================


def main():
    # Load book background
    book_path = "src/main/resources/assets/not-riding-alert/textures/gui/tutorial/book_background.png"
    try:
        img = Image.open(book_path)
    except FileNotFoundError:
        # Create a blank image if book doesn't exist
        img = Image.new("RGBA", (BOOK_WIDTH, BOOK_HEIGHT), (200, 180, 140, 255))

    draw = ImageDraw.Draw(img)

    # Draw content area (light overlay)
    content_rect = [
        CONTENT_X,
        CONTENT_Y,
        CONTENT_X + CONTENT_WIDTH,
        CONTENT_Y + CONTENT_HEIGHT,
    ]
    draw.rectangle(content_rect, outline=(255, 0, 0, 200), width=3)

    # Draw close button (red)
    close_rect = [
        CLOSE_X,
        CLOSE_Y,
        CLOSE_X + CLOSE_WIDTH,
        CLOSE_Y + CLOSE_HEIGHT,
    ]
    draw.rectangle(close_rect, fill=(255, 0, 0, 180), outline=(0, 0, 0, 255), width=2)

    # Draw back arrow (blue)
    back_rect = [
        BACK_X,
        BACK_Y,
        BACK_X + ARROW_WIDTH,
        BACK_Y + ARROW_HEIGHT,
    ]
    draw.rectangle(back_rect, fill=(0, 100, 255, 180), outline=(0, 0, 0, 255), width=2)

    # Draw next arrow (green)
    next_rect = [
        NEXT_X,
        NEXT_Y,
        NEXT_X + ARROW_WIDTH,
        NEXT_Y + ARROW_HEIGHT,
    ]
    draw.rectangle(next_rect, fill=(0, 200, 100, 180), outline=(0, 0, 0, 255), width=2)

    # Save visualization
    output_path = "button_positions_visual.png"
    img.save(output_path)
    print(f"Saved visualization to: {output_path}")
    print()
    print("Current values (edit in script to adjust):")
    print(f"  CONTENT_X = {CONTENT_X}")
    print(f"  CONTENT_Y = {CONTENT_Y}")
    print(f"  CONTENT_WIDTH = {CONTENT_WIDTH}")
    print(f"  CONTENT_HEIGHT = {CONTENT_HEIGHT}")
    print(f"  CLOSE_X = {CLOSE_X}")
    print(f"  CLOSE_Y = {CLOSE_Y}")
    print(f"  BACK_X = {BACK_X}")
    print(f"  BACK_Y = {BACK_Y}")
    print(f"  NEXT_X = {NEXT_X}")
    print(f"  NEXT_Y = {NEXT_Y}")


if __name__ == "__main__":
    main()
