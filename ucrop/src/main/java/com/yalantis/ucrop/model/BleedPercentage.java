package com.yalantis.ucrop.model;

/**
 * Used to determine the distance of bleed from the crop view rect.
 * Each variable represents the percentage size of the width (not length) of the bleed rectangle relative to the crop
 * view rect's height (for top/bottom bleed) or width (for left/right bleed).
 */
public class BleedPercentage {

    public final float left;
    public final float top;
    public final float right;
    public final float bottom;

    public BleedPercentage(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}
