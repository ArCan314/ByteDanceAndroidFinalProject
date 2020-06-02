package com.bytedance.AndroidFinal;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("_id")
    public String id;
    @SerializedName("feedurl")
    public String url;
    @SerializedName("nickname")
    public String nickname;
    @SerializedName("description")
    public String description;
    @SerializedName("likecount")
    public int likeCount;
    @SerializedName("avatar")
    public String avatarUrl;

    @Override
    public String toString() {
        return "Response{" +
                "id=" + id +
                ", url=" + url +
                ", nickname=" + nickname +
                ", description=" + description +
                ", likecount=" + String.valueOf(likeCount) +
                ", avatarUrl=" + avatarUrl +
                "}";
    }
}
