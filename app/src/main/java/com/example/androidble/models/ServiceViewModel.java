package com.example.androidble.models;

import java.util.ArrayList;
import java.util.List;

public class ServiceViewModel {

    private String name;
    private String uuid;
    private List<CharacteristicViewModel>characteristics;

    public ServiceViewModel(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
        this.characteristics = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public List<CharacteristicViewModel> getCharacteristics() {
        return characteristics;
    }

    public void addCharacteristic(CharacteristicViewModel characteristic){
        this.characteristics.add(characteristic);
    }
}
