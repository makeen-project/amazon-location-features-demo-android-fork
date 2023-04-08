package com.aws.amazonlocation.utils.stickyHeaders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface StickyHeaderAdapter<T : RecyclerView.ViewHolder> {

    fun getHeaderId(position: Int): Long

    fun onCreateHeaderViewHolder(parent: ViewGroup): T

    fun onBindHeaderViewHolder(holder: T, position: Int)
}
