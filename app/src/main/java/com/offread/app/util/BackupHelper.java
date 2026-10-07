package com.offread.app.util;

import com.offread.app.data.Article;
import com.offread.app.data.ArticleRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BackupHelper {

    public static String exportToJson(List<Article> articles) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("exportedAt", System.currentTimeMillis());
        root.put("app", "OffRead");

        JSONArray array = new JSONArray();
        for (Article a : articles) {
            array.put(a.toJson());
        }
        root.put("articles", array);
        return root.toString(2);
    }

    public static List<Article> importFromJson(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);
        JSONArray array = root.getJSONArray("articles");
        List<Article> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Article a = Article.fromJson(obj);
            // Reset id so database auto-increments
            a.setId(0);
            list.add(a);
        }
        return list;
    }

    public static File writeBackupToFile(File directory, String jsonContent) throws Exception {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = "offread_backup_" + System.currentTimeMillis() + ".json";
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }

    public static String readBackupFromFile(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
