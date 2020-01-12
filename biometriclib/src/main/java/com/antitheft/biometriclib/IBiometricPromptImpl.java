package com.antitheft.biometriclib;

import android.os.CancellationSignal;
import androidx.annotation.NonNull;

/**
 * Created by Jacky.ao on 2019/11/12.
 */
interface IBiometricPromptImpl {

    void authenticate(@NonNull CancellationSignal cancel,
                      @NonNull BiometricPromptManager.OnBiometricIdentifyCallback callback);

}
