package com.Ejram.gameboost;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(GuardianEngine.class); // المحرك الحقيقي فقط
        super.onCreate(savedInstanceState);
    }
}
