package com.eattle.phoket.host;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eattle.phoket.AlbumFullActivity;
import com.eattle.phoket.CONSTANT;
import com.eattle.phoket.FileSystem;
import com.eattle.phoket.MainActivity;
import com.eattle.phoket.device.BlockDevice;
import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.device.CachedUsbMassStorageBlockDevice;
import com.eattle.phoket.device.MyUsbSerialDevice;
import com.eattle.phoket.device.UsbMassStorageBlockDevice;
import com.eattle.phoket.device.UsbSerialDevice;
import com.eattle.phoket.host.BlockDeviceApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hyeonguk on 15. 2. 20..
 */
public class UsbDeviceHost {
    private final static List<UsbDevice> mDevices = new ArrayList<UsbDevice>();
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    int isAsked = 0;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("usb", "onReceive 호출");
            String action = intent.getAction();
            Log.d("usb", "CONSTANT.ISUSBCONNECTED : " + CONSTANT.ISUSBCONNECTED);
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    //if(device.getVendorId()!= 0){//fake
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d("usb", "vendor-id : " + device.getVendorId() + " product-id : " + device.getProductId() + " class : " + device.getDeviceClass() + " subclass : " + device.getDeviceSubclass() + " protocol : " + device.getDeviceProtocol());
                            UsbDeviceConnection connection = mUsbManager.openDevice(device);
                            UsbInterface usbInterface = device.getInterface(0);
                            connection.claimInterface(usbInterface, true);

                            usbInterface.getInterfaceSubclass();
                            UsbEndpoint readEndpoint = usbInterface.getEndpoint(0);
                            UsbEndpoint writeEndpoint = usbInterface.getEndpoint(1);

                            if (readEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                                UsbEndpoint temp = readEndpoint;
                                readEndpoint = writeEndpoint;
                                writeEndpoint = temp;
                            }

                            UsbSerialDevice serialDevice = new MyUsbSerialDevice(connection, readEndpoint, writeEndpoint);
                            BlockDevice blockDevice = new UsbMassStorageBlockDevice(serialDevice);

                            Log.i("getLastLBA", String.valueOf(blockDevice.getLastLogicalBlockAddress()));
                            Log.i("getBlockLength", String.valueOf(blockDevice.getBlockLength()));
/*
                            stop(); // 기존의 리시버를 해제, 새로운 리시버 등록
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                            activity.registerReceiver(mUsbReceiverFinal, filter);
*/

                            app.onConnected(blockDevice);
                        }
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                Log.d("usb", "ACTION_USB_DEVICE_ATTACHED 진입 ");
                CONSTANT.ISUSBCONNECTED = 1;
                stop();
                start(activity, MainActivity.paramBlock);


/*
                synchronized (this) {
                    CONSTANT.ISUSBCONNECTED = 1;
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(device.getVendorId() == 1086) {//fake
                        Log.d("usb", "hasPermission : " + ACTION_USB_PERMISSION.equals(action));
                        //mUsbManager.requestPermission(device, mPermissionIntent);

                        //if(mUsbManager.hasPermission(device)){
                        //if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //if(ACTION_USB_PERMISSION.equals(action)){
                        if (device != null) {

                            UsbDeviceConnection connection = mUsbManager.openDevice(device);
                            UsbInterface usbInterface = device.getInterface(0);
                            connection.claimInterface(usbInterface, true);

                            usbInterface.getInterfaceSubclass();
                            UsbEndpoint readEndpoint = usbInterface.getEndpoint(0);
                            UsbEndpoint writeEndpoint = usbInterface.getEndpoint(1);

                            if (readEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                                UsbEndpoint temp = readEndpoint;
                                readEndpoint = writeEndpoint;
                                writeEndpoint = temp;
                            }

                            UsbSerialDevice serialDevice = new MyUsbSerialDevice(connection, readEndpoint, writeEndpoint);
                            BlockDevice blockDevice = new UsbMassStorageBlockDevice(serialDevice);

                            Log.i("getLastLBA", String.valueOf(blockDevice.getLastLogicalBlockAddress()));
                            Log.i("getBlockLength", String.valueOf(blockDevice.getBlockLength()));

                            app.onConnected(blockDevice);
                        }
                    }
                }*/
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Log.d("usb", "ACTION_USB_DEVICE_DETACHED 진입 ");
                CONSTANT.ISUSBCONNECTED = 0;
                /*
                UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null) {
                    // call your method that cleans up and closes communication with the accessory

                }*/
            }

        }
    };

    public final BroadcastReceiver mUsbReceiverFinal = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);


            String message = "action:" + action + " device:" + device.getProductId() + " Vendor:" + device.getVendorId();
            Log.d("onReceive", message);

            if (device.getVendorId() == 1086) {
                if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    CONSTANT.ISUSBCONNECTED = 1;
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.d("usb", "hasPermission : " + ACTION_USB_PERMISSION.equals(action));
                    //mUsbManager.requestPermission(device, mPermissionIntent);

                    //if(mUsbManager.hasPermission(device)){
                    //if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    //if(ACTION_USB_PERMISSION.equals(action)){
                    if (device != null) {
                        Log.d("usb", "ACTION_USB_DEVICE_ATTACHED 진입 ");

                        CONSTANT.ISUSBCONNECTED = 1;


                            /*
                            MainActivity.fileSystem = FileSystem.getInstance();

                            AlbumFullActivity.fileSystem = FileSystem.getInstance();

                            MainActivity.fileSystem.incaseSearchTable(CONSTANT.BLOCKDEVICE);
                            AlbumFullActivity.fileSystem.incaseSearchTable(CONSTANT.BLOCKDEVICE);
*/


                    }
                } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    Log.d("usb", "ACTION_USB_DEVICE_DETACHED 진입 ");
                    CONSTANT.ISUSBCONNECTED = 0;


                    //MainActivity.fileSystem = null;
                    //AlbumFullActivity.fileSystem = null;
                }
            }
        }
    };
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private BlockDeviceApp app;
    private Activity activity;

    public void start(Activity activity, BlockDeviceApp blockDeviceApp) {
        Log.d("usb", "start 호출");
        mUsbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        activity.registerReceiver(mUsbReceiver, filter);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            mUsbManager.requestPermission(device, mPermissionIntent);

        }

        app = blockDeviceApp;
        this.activity = activity;
    }

    public void stop() {
        activity.unregisterReceiver(mUsbReceiver);
    }
}