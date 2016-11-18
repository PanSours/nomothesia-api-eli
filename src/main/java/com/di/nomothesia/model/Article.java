
package com.di.nomothesia.model;

import java.util.ArrayList;
import java.util.List;

public class Article implements Fragment{
    private String title;
    private String id;
    private String uri;
    private List<Paragraph> paragraphs;
    private int status;
    private String type;
    
    public Article() {
        this.paragraphs = new ArrayList<>();
        status = 0;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int s) {
        this.status = s;
    }

    @Override
    public String getURI() {
        return uri;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }
    
    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }
    
    public void setParagraphs(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }
}
