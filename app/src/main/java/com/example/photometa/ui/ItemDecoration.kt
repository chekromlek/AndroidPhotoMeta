package com.example.photometa.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class GridSpaceItemDecoration(space: Int) : RecyclerView.ItemDecoration() {

    private val space: Int = space

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(space, space, space, space)
    }

}