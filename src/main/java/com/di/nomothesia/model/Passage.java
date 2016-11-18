
package com.di.nomothesia.model;

public class Passage implements Fragment {
    private String uri;
    private String text;
    private int id;
    private int status;
    private String type;
    private Modification modification;

    public Passage() {
        //Empty Constructor
    }

    @Override
    public int getStatus() {
        return status;
    }
    
    @Override
    public void setStatus(int status) {
        this.status = status;
    }
    
    @Override
    public String getURI() {
        return uri;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
    
    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Modification getModification() {
        return modification;
    }

    public void setModification(Modification modification) {
        this.modification = modification;
    }
}
