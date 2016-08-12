package com.test.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.example.aidl.ITestAidlInterface;

import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by ubuntu on 16-7-18.
 */
public class TestAidlService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final ITestAidlInterface.Stub mBinder = new ITestAidlInterface.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public int add(int x, int y) throws RemoteException {
            return x+y;
        }
    };
}
