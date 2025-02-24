package com.example.enums;

import com.documents4j.api.DocumentType;
import lombok.Getter;

import java.util.HashMap;

@Getter
public enum FileType {
    WORD("docx", DocumentType.MS_WORD),
    DOC("doc", DocumentType.DOC),
    TXT("txt", DocumentType.TEXT);

    private final String type;
    private final DocumentType documentType;

    private static final HashMap<String, FileType> MAP = new HashMap<>();

    static {
        for (FileType value : values()) {
            MAP.put(value.type, value);
        }
    }

    FileType(String type, DocumentType documentType) {
        this.type = type;
        this.documentType = documentType;
    }

    public static FileType fromString(String title) {
        return MAP.getOrDefault(title.toLowerCase(), null);
    }
}