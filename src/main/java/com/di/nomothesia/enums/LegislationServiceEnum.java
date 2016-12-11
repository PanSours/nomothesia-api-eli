package com.di.nomothesia.enums;

/**
 * Created by Panagiotis on 11/12/2016.
 *
 */
public enum LegislationServiceEnum {
    ID("id"),
    DOC_URI("documentURI"),
    ARTICLE("Article"),
    PARAGRAPH("Paragraph"),
    PASSAGE("Passage"),
    MODIFICATION("Modification"),
    CASE("Case");

    private String type;

    LegislationServiceEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
