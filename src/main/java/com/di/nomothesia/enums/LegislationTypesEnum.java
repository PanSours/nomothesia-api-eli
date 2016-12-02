package com.di.nomothesia.enums;

/**
 * Created by psour on 22/11/2016.
 */
public enum LegislationTypesEnum {
    PD("PresidentialDecree"),
    LAW("Law"),
    AMC("ActOfMinisterialCabinet"),
    MD("MinisterialDecision"),
    RP("RegulatoryProvision"),
    LA("LegislativeAct");

    private String type;

    LegislationTypesEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
