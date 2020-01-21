package com.example.androidble.models;

public class CharacteristicViewModel {

    private String name;
    private String uuid;

    public CharacteristicViewModel(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }
}
