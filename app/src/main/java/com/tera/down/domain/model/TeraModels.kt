package com.tera.down.domain.model

import com.google.gson.annotations.SerializedName

data class TeraResponse(
    val ok: Boolean,
    val status: Int,
    val data: TeraData?
)

data class TeraData(
    val list: List<TeraFileItem>?
)

data class TeraFileItem(
    @SerializedName("fs_id") val fsId: String,
    val filename: String,
    val category: String, // "folder" or "video"
    @SerializedName("size_fmt") val sizeFmt: String,
    val thumb: String?,
    val links: TeraLinks?
)

data class TeraLinks(
    val proxy: String? // URL untuk streaming/download
)