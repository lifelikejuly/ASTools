package com.julyyu.asplugins.utils;

import com.google.gson.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.julyyu.asplugins.tools.dartjsonformat.model.ClsEntity;
import com.julyyu.asplugins.tools.dartjsonformat.model.ClsType;
import com.julyyu.asplugins.tools.dartjsonformat.model.MustacheEntity;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author shiki
 */
public class ClsUtils {
    private Gson gson;
    public ClsUtils() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public MustacheEntity jsonElementToMustacheEntity(String fileName, String jsonText) {
        return jsonElementToMustacheEntity(fileName, tojsonElement(jsonText));
    }

    public MustacheEntity jsonElementToMustacheEntity(String fileName, JsonElement jsonElement) {
        if (jsonElement != null && !TextUtils.isEmpty(fileName)) {
            String clzName = null;
            if (!TextUtils.isEmpty(fileName)) {
                fileName = fileName.replace(".dart", "");
                clzName = TextUtils.formatClassName(fileName);
            }
            Set<ClsEntity> clsEntityList = new LinkedHashSet<>();
            if (jsonElement.isJsonObject()) { // JSON对象
                jsonElementToClsEntity(null, clzName, ClsType.DYNAMIC_TYPE, jsonElement.getAsJsonObject(), clsEntityList);
            } else if (jsonElement.isJsonArray()) { // JSON数组
                jsonElementToClsEntity(null, clzName, ClsType.LIST_TYPE, jsonElement.getAsJsonArray(), clsEntityList);
            }
            return new MustacheEntity(fileName, clsEntityList); // 创建模版化对象
        }
        return null;
    }

    public void jsonElementToClsEntity(ClsEntity parent, String name, String clsType, JsonElement value, Set<ClsEntity> clsEntityList) { // 递归解析每层字段
        ClsEntity clsEntity = new ClsEntity();
        clsEntity.setName(name);
        clsEntity.setClassName(TextUtils.formatClassName(name));
        clsEntity.setPropName(TextUtils.formatPropertyName(name));
        clsEntity.setClsType(clsType);
        clsEntity.setValue(toJsonText(value));
        clsEntity.setFromJson(fromJson(clsEntity));
        clsEntity.setClsTypeName(clsTypeName(clsEntity));
        if (value.isJsonObject()) {
            clsEntityList.add(clsEntity);
            for (Map.Entry entry : value.getAsJsonObject().entrySet()) {
                JsonElement element = value.getAsJsonObject().get(entry.getKey().toString());
                jsonElementToClsEntity(clsEntity, entry.getKey().toString(), toClsType(entry.getKey().toString(), element), element, clsEntityList);
            }
        } else if (value.isJsonArray()) {
            int size = value.getAsJsonArray().size();
            for (int i = 0; i < size; i++) {
                JsonElement v = value.getAsJsonArray().get(i);
                jsonElementToClsEntity(clsEntity, name, toClsType(name, v), v, clsEntityList);
            }
        }
        if (parent != null) {
            parent.getProperties().add(clsEntity);
        }
    }

    public String clsTypeName(ClsEntity clsEntity) {
        if (clsEntity.getClsType() == ClsType.DYNAMIC_TYPE) {
            return clsEntity.getClassName();
        } else if (clsEntity.getClsType() == ClsType.LIST_TYPE) {
            return clsEntity.getClsType() + "<" + clsEntity.getClassName() + ">";
        }
        return clsEntity.getClsType();
    }

    public String fromJson(ClsEntity clsEntity) {
        if (clsEntity.getClsType() == ClsType.DYNAMIC_TYPE) {
            return "json['" + clsEntity.getName() + "'] == null\n" +
                    "        ? null\n" +
                    "        : " + clsEntity.getClassName() + ".fromJson(\n" +
                    "            json['" + clsEntity.getName() + "'] as Map<String, dynamic>)";
        } else if (clsEntity.getClsType() == ClsType.LIST_TYPE) {
            return " (json['" + clsEntity.getName() + "'] as List)\n" +
                    "        ?.map((e) => e == null\n" +
                    "            ? null\n" +
                    "            : " + clsEntity.getClassName() + ".fromJson(e as Map<String, dynamic>))\n" +
                    "        ?.toList()";
        }
        return "json['" + clsEntity.getName() + "'] as " + clsEntity.getClsType();
    }

    public String toClsType(String key, JsonElement value) {
        if (value.isJsonPrimitive()) {
            if (value.getAsJsonPrimitive().isBoolean()) {
                return ClsType.BOOL_TYPE;
            } else if (value.getAsJsonPrimitive().isString()) {
                return ClsType.STRING_TYPE;
            } else if (value.getAsJsonPrimitive().isNumber()) {
                String v = value.getAsString();
                if (TextUtils.isInt(v)) {
                    return ClsType.INT_TYPE;
                } else if (TextUtils.isDouble(v)) {
                    return ClsType.DOUBLE_TYPE;
                }
                return ClsType.NUM_TYPE;
            }
        } else if (value.isJsonArray()) {
            return ClsType.LIST_TYPE;
        }
        return ClsType.DYNAMIC_TYPE;

    }

    public JsonElement tojsonElement(String jsonText) throws JsonIOException {
        if (TextUtils.isEmpty(jsonText)) {
            return null;
        }
        return JsonParser.parseString(jsonText);
    }

    public String toJsonText(JsonElement jsonElement) {
        return gson.toJson(jsonElement);
    }

    public String getClsDir(PsiFile file) {
        PsiDirectory psiDirectory = file.getParent();
        String path = psiDirectory.getVirtualFile().getCanonicalPath();
        return path;
    }
}
