package com.example.arcoble;

import android.bluetooth.BluetoothDevice;

public class DeviceBLE {

    private String name;
    private BluetoothDevice MAC;

    public DeviceBLE() {
    }

    public DeviceBLE(String name, BluetoothDevice MAC) {
        this.name = name;
        this.MAC = MAC;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BluetoothDevice getMAC() {
        return MAC;
    }

    public void setMAC(BluetoothDevice MAC) {
        this.MAC = MAC;
    }

    @Override
    public String toString() {
        return name;
    }
}