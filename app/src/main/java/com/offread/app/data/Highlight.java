package com.offread.app.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Highlight implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long articleId;
    private String text;
    private String note;
    private String color;
    private long createdAt;

    public Highlight() {
        this.color = "#F59E0B";
        this.createdAt = System.currentTimeMillis();
    }

    public Highlight(long id, long articleId, String text, String note, String color, long createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.text = text;
        this.note = note;
        this.color = color;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getArticleId() { return articleId; }
    public void setArticleId(long articleId) { this.articleId = articleId; }

    public String getText() { return text == null ? "" : text; }
    public void setText(String text) { this.text = text; }

    public String getNote() { return note == null ? "" : note; }
    public void setNote(String note) { this.note = note; }

    public String getColor() { return color == null ? "#F59E0B" : color; }
    public void setColor(String color) { this.color = color; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("articleId", articleId);
        obj.put("text", text);
        obj.put("note", note);
        obj.put("color", color);
        obj.put("createdAt", createdAt);
        return obj;
    }

    public static Highlight fromJson(JSONObject obj) {
        Highlight h = new Highlight();
        h.id = obj.optLong("id", 0);
        h.articleId = obj.optLong("articleId", 0);
        h.text = obj.optString("text", "");
        h.note = obj.optString("note", "");
        h.color = obj.optString("color", "#F59E0B");
        h.createdAt = obj.optLong("createdAt", System.currentTimeMillis());
        return h;
    }
}
