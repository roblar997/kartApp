package com.example.kartapp.database.models;

import java.util.Objects;

public class Severdighet {
    Double lat;
    Double lng;
    String gateadresse;
    String beskrivelse;

    public Severdighet(Double lat, Double lng, String gateadresse, String beskrivelse){
        this.lat = lat;
        this.lng = lng;
        this.gateadresse = gateadresse;
        this.beskrivelse = beskrivelse;
    }
    public Severdighet(){

    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Severdighet that = (Severdighet) o;
        return Objects.equals(lat, that.lat) && Objects.equals(lng, that.lng) && Objects.equals(gateadresse, that.gateadresse) && Objects.equals(beskrivelse, that.beskrivelse);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getGateadresse() {
        return gateadresse;
    }

    public void setGateadresse(String gateadresse) {
        this.gateadresse = gateadresse;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng, gateadresse, beskrivelse);
    }
}
