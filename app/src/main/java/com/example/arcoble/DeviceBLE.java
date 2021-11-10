package com.example.arcoble;

import android.bluetooth.BluetoothDevice;

//Clase DeviceBLE
public class DeviceBLE {

    private String name;
    private BluetoothDevice MAC;
    private int rssi;

    public DeviceBLE() {
    }

    public DeviceBLE(String name, BluetoothDevice MAC, int rssi) {
        this.name = name;
        this.MAC = MAC;
        this.rssi = rssi;
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

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return name;
    }
}