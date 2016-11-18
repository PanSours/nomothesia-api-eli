
package com.di.nomothesia.model;

public class Signer {
    private String fullName;
    private String title;
    
    public Signer() {
        //Empty Constructor
    }
    
    //Setters-Getters for Signer
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
