package io.moka.lib.imagepicker.image.viewer.adapter;


import android.graphics.Bitmap;

import io.moka.lib.base.adapter.ItemData;


public class ImageItemData implements ItemData {

    private String imageUrl;
    private boolean checked = false;
    private Bitmap imageBitmap = null;

    public String getImageUrl() {

        return imageUrl;
    }

    public void setImagePath(String imagePath) {

        this.imageUrl = imagePath;
    }

    public boolean isChecked() {

        return checked;
    }

    public void setChecked(boolean checked) {

        this.checked = checked;
    }

    public void toggleCheckedState() {

        checked = !checked;
    }

    public void setImageBitmap(Bitmap imageBitmap) {

        this.imageBitmap = imageBitmap;
    }

    public Bitmap getImageBitmap() {

        return imageBitmap;
    }

}
