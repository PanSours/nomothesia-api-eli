
package com.di.nomothesia.model;

import java.util.ArrayList;
import java.util.List;

public class Chapter implements Fragment {
    private String title;
    private int id;
    private String uri;
    private List<Article> articles;
    private int status;
    private String type;
    
    public Chapter() {
        this.articles = new ArrayList<>();
        status = 0;
    }

    @Override
    public void setType(String t) {
        this.type = t;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int s) {
        this.status = s;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }
    
    public List<Article> getArticles() {
        return articles;
    }
    
    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}

