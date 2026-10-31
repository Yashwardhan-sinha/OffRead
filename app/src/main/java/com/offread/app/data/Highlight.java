package com.offread.app.data;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class Highlight implements Serializable {
    private static final long serialVersionUID = 1;
    private long articleId;
    private String color;
    private long createdAt;
    private long id;
    private String note;
    private String text;

    public Highlight() {
        this.color = "#F59E0B";
        this.createdAt = System.currentTimeMillis();
    }

    public Highlight(long j, long j2, String str, String str2, String str3, long j3) {
        this.id = j;
        this.articleId = j2;
        this.text = str;
        this.note = str2;
        this.color = str3;
        this.createdAt = j3;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long j) {
        this.id = j;
    }

    public long getArticleId() {
        return this.articleId;
    }

    public void setArticleId(long j) {
        this.articleId = j;
    }

    public String getText() {
        String str = this.text;
        return str == null ? "" : str;
    }

    public void setText(String str) {
        this.text = str;
    }

    public String getNote() {
        String str = this.note;
        return str == null ? "" : str;
    }

    public void setNote(String str) {
        this.note = str;
    }

    public String getColor() {
        String str = this.color;
        return str == null ? "#F59E0B" : str;
    }

    public void setColor(String str) {
        this.color = str;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long j) {
        this.createdAt = j;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("id", this.id);
        jSONObject.put("articleId", this.articleId);
        jSONObject.put("text", this.text);
        jSONObject.put("note", this.note);
        jSONObject.put("color", this.color);
        jSONObject.put("createdAt", this.createdAt);
        return jSONObject;
    }

    public static Highlight fromJson(JSONObject jSONObject) {
        Highlight highlight = new Highlight();
        highlight.id = jSONObject.optLong("id", 0L);
        highlight.articleId = jSONObject.optLong("articleId", 0L);
        highlight.text = jSONObject.optString("text", "");
        highlight.note = jSONObject.optString("note", "");
        highlight.color = jSONObject.optString("color", "#F59E0B");
        highlight.createdAt = jSONObject.optLong("createdAt", System.currentTimeMillis());
        return highlight;
    }
}
