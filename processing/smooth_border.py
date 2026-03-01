#!/usr/bin/env python3
"""
Smooth image borders by refining alpha channel.
Removes semi-transparent artifacts and applies edge smoothing.
"""

from PIL import Image, ImageFilter


def smooth_border(
    input_path, output_path, alpha_threshold=20, blur_radius=1.5, final_threshold=128
):
    """
    Smooth image borders using alpha channel refinement.

    Args:
        input_path: Path to input PNG image
        output_path: Path to output PNG image
        alpha_threshold: Remove pixels with alpha below this value (removes halos)
        blur_radius: Gaussian blur radius for edge smoothing
        final_threshold: Re-threshold blurred alpha for crisp edges
    """
    img = Image.open(input_path).convert("RGBA")
    r, g, b, a = img.split()

    # Step 1: Threshold alpha - remove semi-transparent artifacts (pink halos)
    a = a.point(lambda x: 0 if x < alpha_threshold else x)

    # Step 2: Gaussian blur for smooth edges
    a = a.filter(ImageFilter.GaussianBlur(radius=blur_radius))

    # Step 3: Re-threshold for crisp edges
    a = a.point(lambda x: 0 if x < final_threshold else 255)

    # Recombine and save
    result = Image.merge("RGBA", (r, g, b, a))
    result.save(output_path, "PNG")
    print(f"Saved smoothed image to: {output_path}")


if __name__ == "__main__":
    input_file = "background/v7_pink_screen_1024x640_remove_bg_w1024_h640.png"
    output_file = "background/out1.png"

    print(f"Processing: {input_file}")
    print(f"  alpha_threshold=20, blur_radius=1.5, final_threshold=128")

    smooth_border(input_file, output_file)
    print("Done!")
