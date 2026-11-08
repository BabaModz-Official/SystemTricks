package com.babamodz.darktricks;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.invokeOriginalMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "SystemTricks: ";
    private static final String PREF_PACKAGE = "com.babamodz.darktricks";
    private static final String PREF_FILE = "com.babamodz.darktricks_shared";
    public static final String ACTION_PREFS = "com.babamodz.darktricks.PREFERENCES";
    private static final int MSG_WAKE_UP = 100;

    // Feature settings
    private int mCursorControl = 0;
    private boolean mSkipTrack = false;
    private boolean mPowerTorch = false;
    private long mTorchTimeout = 0L;
    private boolean mProximityWakeUp = false;
    private boolean mDoubleTapToSleep = false;
    private int mQuickPulldown = 0;
    private int mGestureHeight = 0;
    private boolean mHideAdbNotification = false;
    private int mLessNotifications = 0;
    private boolean mScreenOffNotifications = false;
    private boolean mHideVpn = false;
    private boolean mHideCert = false;
    private boolean mHideNextAlarm = false;
    private boolean mShow4gForLte = false;
    private boolean mHideLtePlus = false;
    private boolean mHideBuildVersion = false;
    private String mCustomCarrierText = "";
    private boolean mPhoneRecorder = false;
    private boolean mOutlookPolicy = false;

    // Services & State
    private InputMethodService mImeService;
    private Context mContext;
    private Handler mPhoneWindowHandler;
    private Handler mPowerHandler;
    private AudioManager mAudioManager;
    private PowerManager mPowerManager;
    private CameraManager mCameraManager;
    private String mCameraId;
    private CameraManager.TorchCallback mTorchCallback;
    private SensorEventListener mListenerTorch;
    private SensorEventListener mListenerPower;
    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private TelephonyManager mTelephonyManager;
    private PowerManager.WakeLock mWakeLockPower;
    private PowerManager.WakeLock mWakeLockTorch;
    private boolean mTorchEnabled;
    private boolean mVolumeLongPress;
    private boolean mPowerLongPress;
    private Runnable mWakeUpRunnable;
    private Runnable mVolumeDownLongPress;
    private Runnable mVolumeUpLongPress;
    private Runnable mPowerDownLongPress;
    private Runnable mTorchTimeoutRunnable;

    private final ArrayMap<String, Long> mLastTimestamps = new ArrayMap<>();
    private Object mNotificationPanelViewController;
    private GestureDetector mDoubleTapGesture;
    private int mQuickQsOffsetHeight = 0;
    private Object mEdgeBackGestureHandler;
    private Object mUsbHandler;
    private Object mStatusBarSignalPolicy;
    private Object mPhoneStatusBarPolicy;
    private Object mQSFooterView;
    private Object mShadeCarrierGroupController;

    private XSharedPreferences prefs;
    private final IntentFilter mFilter = new IntentFilter(ACTION_PREFS);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePreferencesFromIntent(intent);
        }
    };

    @Override
    public void initZygote(StartupParam startupParam) {
        log(TAG + "SystemTricks Zygote Init");
        loadPreferences();

        // 1. Feature: Volume keys control cursor (InputMethodService)
        findAndHookMethodIfExists("android.inputmethodservice.InputMethodService", null, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mImeService = (InputMethodService) param.thisObject;
                try {
                    ContextCompat.registerReceiver(mImeService, mReceiver, mFilter, ContextCompat.RECEIVER_EXPORTED);
                } catch (Throwable t) {
                    try {
                        mImeService.registerReceiver(mReceiver, mFilter);
                    } catch (Throwable ignored) {}
                }
            }
        });

        findAndHookMethodIfExists("android.inputmethodservice.InputMethodService", null, "onDestroy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (mImeService == param.thisObject) {
                    try {
                        mImeService.unregisterReceiver(mReceiver);
                    } catch (Throwable ignored) {}
                    mImeService = null;
                }
            }
        });

        findAndHookMethodIfExists("android.inputmethodservice.InputMethodService", null, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                reloadPreferencesIfNeeded();
                if (mCursorControl > 0 && mImeService != null && mImeService.isInputViewShown()) {
                    int keyCode = ((KeyEvent) param.args[1]).getKeyCode();
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        int targetKey = (mCursorControl == 1) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
                        mImeService.sendDownUpKeyEvents(targetKey);
                        param.setResult(true);
                    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                        int targetKey = (mCursorControl == 1) ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
                        mImeService.sendDownUpKeyEvents(targetKey);
                        param.setResult(true);
                    }
                }
            }
        });

        findAndHookMethodIfExists("android.inputmethodservice.InputMethodService", null, "onKeyUp", int.class, KeyEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (mCursorControl > 0 && mImeService != null && mImeService.isInputViewShown()) {
                    int keyCode = ((KeyEvent) param.args[1]).getKeyCode();
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        param.setResult(true);
                    }
                }
            }
        });
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
        // Module active status hook for our own app
        if (PREF_PACKAGE.equals(param.packageName)) {
            findAndHookMethodIfExists("com.babamodz.darktricks.ModuleStatus", param.classLoader, "isModuleActive", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam hookParam) {
                    return true;
                }
            });
            return;
        }

        if ("android".equals(param.packageName)) {
            log(TAG + "Hooking System Server (android)");
            hookSystemServer(param);
        } else if ("com.android.systemui".equals(param.packageName)) {
            hookSystemUI(param);
        } else if ("com.google.android.dialer".equals(param.packageName)) {
            hookGoogleDialer(param);
        } else if ("com.microsoft.office.outlook".equals(param.packageName)) {
            hookOutlook(param);
        }
    }

    private void hookSystemServer(XC_LoadPackage.LoadPackageParam param) {
        // Hook PhoneWindowManager init (standard AOSP)
        findAndHookMethodIfExists("com.android.server.policy.PhoneWindowManager", param.classLoader, "init",
                Context.class, "android.view.IWindowManager", "com.android.server.policy.WindowManagerPolicy$WindowManagerFuncs", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam hookParam) {
                        ensurePhoneWindowInitialized(hookParam.thisObject);
                    }
                });

        // Hook PhoneWindowManager initKeyCombinationRules
        findAndHookMethodIfExists("com.android.server.policy.PhoneWindowManager", param.classLoader, "initKeyCombinationRules", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                ensurePhoneWindowInitialized(hookParam.thisObject);
            }
        });

        // Intercept keys (Skip Track & Power Torch)
        findAndHookMethodIfExists("com.android.server.policy.PhoneWindowManager", param.classLoader, "interceptKeyBeforeQueueing",
                KeyEvent.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam hookParam) {
                        handleKeyInterception(hookParam);
                    }
                });

        // Prevent accidental wake up: Hook PowerGroup wakeUpLocked across Android 11 to 17
        hookAllMethodsIfExists("com.android.server.power.PowerGroup", param.classLoader, "wakeUpLocked", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                handleWakeUpLocked(hookParam);
            }
        });

        // Hook PowerManagerService systemReady
        findAndHookMethodIfExists("com.android.server.power.PowerManagerService", param.classLoader, "systemReady", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                try {
                    Context context = (Context) getObjectField(hookParam.thisObject, "mContext");
                    mPowerHandler = (Handler) getObjectField(hookParam.thisObject, "mHandler");

                    if (mPowerManager == null && context != null) {
                        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    }
                    if (mSensorManager == null && context != null) {
                        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                    }
                    if (mSensorManager != null && mProximitySensor == null) {
                        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    }
                    if (mTelephonyManager == null && context != null) {
                        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    }
                    if (mWakeLockPower == null && mPowerManager != null) {
                        mWakeLockPower = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SystemTricks:WakeUp");
                    }
                } catch (Throwable ignored) {}
            }
        });

        findAndHookMethodIfExists("com.android.server.power.PowerManagerService$PowerManagerHandlerCallback", param.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                Message msg = (Message) hookParam.args[0];
                if (mProximityWakeUp && msg.what == MSG_WAKE_UP) {
                    if (mWakeLockPower != null) {
                        synchronized (mWakeLockPower) {
                            if (mWakeLockPower.isHeld()) mWakeLockPower.release();
                            if (mListenerPower != null && mSensorManager != null) {
                                mSensorManager.unregisterListener(mListenerPower);
                                mListenerPower = null;
                            }
                        }
                    }
                    if (mWakeUpRunnable != null) {
                        mWakeUpRunnable.run();
                    }
                }
            }
        });

        // Hide ADB notification
        findAndHookMethodIfExists("com.android.server.usb.UsbDeviceManager$UsbHandler", param.classLoader, "updateAdbNotification", boolean.class, new XC_MethodHook() {
            boolean connected;
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                try {
                    mUsbHandler = hookParam.thisObject;
                    connected = getBooleanField(hookParam.thisObject, "mConnected");
                    if (mHideAdbNotification) {
                        setBooleanField(hookParam.thisObject, "mConnected", false);
                    }
                } catch (Throwable ignored) {}
            }
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                try {
                    if (mHideAdbNotification) {
                        setBooleanField(hookParam.thisObject, "mConnected", connected);
                    }
                } catch (Throwable ignored) {}
            }
        });

        // Throttle notifications / screen-off notifications
        findAndHookMethodIfExists("com.android.server.notification.NotificationManagerService", param.classLoader, "shouldMuteNotificationLocked", "com.android.server.notification.NotificationRecord", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                if (Boolean.FALSE.equals(hookParam.getResult())) {
                    try {
                        Object lock = getObjectField(hookParam.thisObject, "mNotificationLock");
                        synchronized (lock) {
                            if (mScreenOffNotifications && getBooleanField(hookParam.thisObject, "mScreenOn")) {
                                hookParam.setResult(true);
                                return;
                            }
                            if (mLessNotifications > 0) {
                                StatusBarNotification sbn = (StatusBarNotification) getObjectField(hookParam.args[0], "sbn");
                                if (sbn != null) {
                                    String id = sbn.getPackageName() + "|" + sbn.getUid();
                                    Long lastTime = mLastTimestamps.get(id);
                                    long now = SystemClock.elapsedRealtime();
                                    if (lastTime == null || (now - lastTime > mLessNotifications)) {
                                        mLastTimestamps.put(id, now);
                                    } else {
                                        hookParam.setResult(true);
                                    }
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        });
    }

    private void ensurePhoneWindowInitialized(Object phoneWindowManager) {
        if (mContext == null) {
            try {
                mContext = (Context) getObjectField(phoneWindowManager, "mContext");
            } catch (Throwable ignored) {}
        }

        if (mPhoneWindowHandler == null) {
            try {
                mPhoneWindowHandler = (Handler) getObjectField(phoneWindowManager, "mHandler");
            } catch (Throwable ignored) {}
        }

        if (mContext != null) {
            try {
                ContextCompat.registerReceiver(mContext, mReceiver, mFilter, ContextCompat.RECEIVER_EXPORTED);
            } catch (Throwable t) {
                try {
                    mContext.registerReceiver(mReceiver, mFilter);
                } catch (Throwable ignored) {}
            }

            if (mPowerManager == null) mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            if (mSensorManager == null) mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            if (mProximitySensor == null && mSensorManager != null) mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (mAudioManager == null) mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (mTelephonyManager == null) mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (mWakeLockTorch == null && mPowerManager != null) {
                mWakeLockTorch = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SystemTricks:TorchLock");
            }
        }

        if (mTorchCallback == null) {
            mTorchCallback = new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    mTorchEnabled = enabled;
                    unregisterTorchSensor();
                    if (!enabled && mTorchTimeoutRunnable != null && mPhoneWindowHandler != null) {
                        mPhoneWindowHandler.removeCallbacks(mTorchTimeoutRunnable);
                    }
                }

                @Override
                public void onTorchModeUnavailable(String cameraId) {
                    mTorchEnabled = false;
                    unregisterTorchSensor();
                    if (mTorchTimeoutRunnable != null && mPhoneWindowHandler != null) {
                        mPhoneWindowHandler.removeCallbacks(mTorchTimeoutRunnable);
                    }
                }
            };
        }

        if (mVolumeUpLongPress == null) {
            mVolumeUpLongPress = () -> {
                mVolumeLongPress = true;
                if (mAudioManager != null) {
                    KeyEvent down = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                    mAudioManager.dispatchMediaKeyEvent(down);
                    KeyEvent up = KeyEvent.changeAction(down, KeyEvent.ACTION_UP);
                    mAudioManager.dispatchMediaKeyEvent(up);
                }
                performHaptic(phoneWindowManager);
            };
        }

        if (mVolumeDownLongPress == null) {
            mVolumeDownLongPress = () -> {
                mVolumeLongPress = true;
                if (mAudioManager != null) {
                    KeyEvent down = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
                    mAudioManager.dispatchMediaKeyEvent(down);
                    KeyEvent up = KeyEvent.changeAction(down, KeyEvent.ACTION_UP);
                    mAudioManager.dispatchMediaKeyEvent(up);
                }
                performHaptic(phoneWindowManager);
            };
        }
    }

    private void handleKeyInterception(XC_MethodHook.MethodHookParam hookParam) {
        KeyEvent event = (KeyEvent) hookParam.args[0];
        int keyCode = event.getKeyCode();

        if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_POWER) {
            return;
        }

        ensurePhoneWindowInitialized(hookParam.thisObject);
        reloadPreferencesIfNeeded();

        if (mContext == null) return;
        Handler handler = mPhoneWindowHandler;
        if (handler == null) {
            try {
                handler = (Handler) getObjectField(hookParam.thisObject, "mHandler");
                mPhoneWindowHandler = handler;
            } catch (Throwable ignored) {}
        }
        if (handler == null) return;

        boolean isInteractive = (mPowerManager != null && mPowerManager.isInteractive());

        // 2. Feature: Volume keys skip track
        if (mSkipTrack && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if ((event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0 &&
                    !isInteractive &&
                    mAudioManager != null && mAudioManager.isMusicActive()) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() == 0) {
                        mVolumeLongPress = false;
                        handler.postDelayed(keyCode == KeyEvent.KEYCODE_VOLUME_UP ? mVolumeUpLongPress : mVolumeDownLongPress,
                                ViewConfiguration.getLongPressTimeout());
                    }
                } else {
                    handler.removeCallbacks(mVolumeUpLongPress);
                    handler.removeCallbacks(mVolumeDownLongPress);

                    if (!mVolumeLongPress && mAudioManager != null) {
                        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                                keyCode == KeyEvent.KEYCODE_VOLUME_UP ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 0);
                    }
                }
                hookParam.setResult(0);
                return;
            }
        }

        // 3. Feature: Power key toggle torch (with proximity check)
        if (mPowerTorch && keyCode == KeyEvent.KEYCODE_POWER) {
            if (mCameraManager == null) {
                mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
                if (mCameraManager != null) {
                    mCameraManager.registerTorchCallback(mTorchCallback, handler);
                    try {
                        for (String id : mCameraManager.getCameraIdList()) {
                            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                            Boolean flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                            Integer lens = c.get(CameraCharacteristics.LENS_FACING);
                            if (flash != null && flash && lens != null && lens == CameraCharacteristics.LENS_FACING_BACK) {
                                mCameraId = id;
                                break;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }

            if (mPowerDownLongPress == null) {
                final Handler fHandler = handler;
                mPowerDownLongPress = () -> {
                    mPowerLongPress = true;

                    if (!mTorchEnabled) {
                        if (mSensorManager != null && mProximitySensor != null) {
                            if (mWakeLockTorch != null && !mWakeLockTorch.isHeld()) {
                                mWakeLockTorch.acquire(1500L);
                            }
                            mListenerTorch = new SensorEventListener() {
                                @Override
                                public void onSensorChanged(SensorEvent ev) {
                                    if (mWakeLockTorch != null && mWakeLockTorch.isHeld()) {
                                        mWakeLockTorch.release();
                                    }
                                    unregisterTorchSensor();

                                    if (ev.values[0] >= mProximitySensor.getMaximumRange()) {
                                        performHaptic(hookParam.thisObject);
                                        toggleTorch(true);
                                        scheduleTorchTimeout(fHandler);
                                    }
                                }

                                @Override
                                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                            };
                            mSensorManager.registerListener(mListenerTorch, mProximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                        } else {
                            performHaptic(hookParam.thisObject);
                            toggleTorch(true);
                            scheduleTorchTimeout(fHandler);
                        }
                    } else {
                        performHaptic(hookParam.thisObject);
                        toggleTorch(false);
                        if (mTorchTimeoutRunnable != null) {
                            fHandler.removeCallbacks(mTorchTimeoutRunnable);
                        }
                    }

                    if (mWakeLockTorch != null && mWakeLockTorch.isHeld()) {
                        mWakeLockTorch.release();
                    }
                };
            }

            if ((!isInteractive || mTorchEnabled) && event.getSource() != InputDevice.SOURCE_UNKNOWN) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() == 0) {
                        mPowerLongPress = false;
                        if (mWakeLockTorch != null && !mWakeLockTorch.isHeld()) {
                            mWakeLockTorch.acquire(1500L);
                        }
                        handler.removeCallbacks(mPowerDownLongPress);
                        handler.postDelayed(mPowerDownLongPress, ViewConfiguration.getLongPressTimeout());
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    handler.removeCallbacks(mPowerDownLongPress);
                    if (mPowerLongPress) {
                        mPowerLongPress = false;
                    } else {
                        handler.post(() -> injectPowerKey(mContext, event.getEventTime()));
                    }
                    if (mWakeLockTorch != null && mWakeLockTorch.isHeld()) {
                        mWakeLockTorch.release();
                    }
                }

                hookParam.setResult(0);
            }
        }
    }

    private void scheduleTorchTimeout(Handler handler) {
        if (mTorchTimeoutRunnable != null) {
            handler.removeCallbacks(mTorchTimeoutRunnable);
        }
        if (mTorchTimeout > 0) {
            mTorchTimeoutRunnable = () -> {
                if (mTorchEnabled) {
                    log(TAG + "Torch timeout reached. Turning torch off.");
                    toggleTorch(false);
                }
            };
            handler.postDelayed(mTorchTimeoutRunnable, mTorchTimeout);
        }
    }

    // 4. Feature: Prevent Accidental Wakeup (Pocket Mode)
    private void handleWakeUpLocked(XC_MethodHook.MethodHookParam hookParam) {
        reloadPreferencesIfNeeded();
        if (!mProximityWakeUp) return;

        mWakeUpRunnable = () -> {
            long ident = Binder.clearCallingIdentity();
            try {
                invokeOriginalMethod(hookParam.method, hookParam.thisObject, hookParam.args);
            } catch (Throwable ignored) {
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        };

        boolean isRinging = (mTelephonyManager != null && mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING);
        boolean isBiometric = (hookParam.args != null && hookParam.args.length > 1 && "android.policy:BIOMETRIC".equals(hookParam.args[1]));

        if (mPowerHandler != null && !isRinging && !isBiometric) {
            if (mPowerHandler.hasMessages(MSG_WAKE_UP)) {
                hookParam.setResult(null);
                return;
            }
            Message msg = mPowerHandler.obtainMessage(MSG_WAKE_UP);
            mPowerHandler.sendMessageDelayed(msg, 100);

            if (mWakeLockPower != null && mSensorManager != null && mProximitySensor != null) {
                synchronized (mWakeLockPower) {
                    mWakeLockPower.acquire(1000L);
                    mListenerPower = new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent ev) {
                            if (mWakeLockPower.isHeld()) mWakeLockPower.release();
                            if (mListenerPower != null) {
                                mSensorManager.unregisterListener(mListenerPower);
                                mListenerPower = null;
                            }
                            if (!mPowerHandler.hasMessages(MSG_WAKE_UP)) {
                                return;
                            }
                            mPowerHandler.removeMessages(MSG_WAKE_UP);

                            if (ev.values[0] >= mProximitySensor.getMaximumRange()) {
                                if (mWakeUpRunnable != null) mWakeUpRunnable.run();
                            }
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                    };
                }
                mSensorManager.registerListener(mListenerPower, mProximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                hookParam.setResult(null);
            }
        }
    }

    private void hookSystemUI(XC_LoadPackage.LoadPackageParam param) {
        findAndHookMethodIfExists("android.app.Instrumentation", param.classLoader, "newApplication",
                ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam hookParam) {
                        Context context = (Context) hookParam.args[2];
                        if (context != null) {
                            try {
                                ContextCompat.registerReceiver(context, mReceiver, mFilter, ContextCompat.RECEIVER_EXPORTED);
                            } catch (Throwable t) {
                                try {
                                    context.registerReceiver(mReceiver, mFilter);
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
                });

        // Double tap to sleep
        hookAllConstructorsIfExists("com.android.systemui.shade.NotificationPanelViewController", param.classLoader, new XC_MethodHook() {
            final Class<?> sysBarClass = findClassIfExists("com.android.internal.policy.SystemBarUtils", param.classLoader);
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                if (sysBarClass != null) {
                    mNotificationPanelViewController = hookParam.thisObject;
                    View view = (View) getObjectField(hookParam.thisObject, "mView");
                    if (view != null) {
                        try {
                            mQuickQsOffsetHeight = (int) callStaticMethod(sysBarClass, "getQuickQsOffsetHeight", view.getContext());
                        } catch (Throwable ignored) {}
                        if (mDoubleTapGesture == null) {
                            PowerManager pm = (PowerManager) view.getContext().getSystemService(Context.POWER_SERVICE);
                            mDoubleTapGesture = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
                                @Override
                                public boolean onDoubleTap(MotionEvent ev) {
                                    if (pm != null) callMethod(pm, "goToSleep", ev.getEventTime());
                                    return true;
                                }
                            });
                        }
                    }
                }
            }
        });

        findAndHookMethodIfExists("com.android.systemui.shade.NotificationPanelViewController$TouchHandler", param.classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                reloadPreferencesIfNeeded();
                if (mQuickQsOffsetHeight > 0 && mDoubleTapToSleep && mDoubleTapGesture != null && mNotificationPanelViewController != null) {
                    try {
                        MotionEvent ev = (MotionEvent) hookParam.args[0];
                        boolean isPulsing = getBooleanField(mNotificationPanelViewController, "mPulsing");
                        boolean isDozing = getBooleanField(mNotificationPanelViewController, "mDozing");
                        if (ev.getY() < mQuickQsOffsetHeight && !isPulsing && !isDozing) {
                            mDoubleTapGesture.onTouchEvent(ev);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        });

        // Quick Settings pulldown
        findAndHookMethodIfExists("com.android.systemui.shade.QuickSettingsController", param.classLoader, "isOpenQsEvent", MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                reloadPreferencesIfNeeded();
                if (mQuickPulldown > 0) {
                    try {
                        MotionEvent ev = (MotionEvent) hookParam.args[0];
                        Object mQs = getObjectField(hookParam.thisObject, "mQs");
                        int mBarState = getIntField(hookParam.thisObject, "mBarState");
                        View mView = (View) callMethod(mQs, "getView");
                        boolean isLayoutRtl = (boolean) callMethod(mView, "isLayoutRtl");
                        int w = (int) callMethod(mView, "getMeasuredWidth");
                        float x = ev.getX();
                        float region = w * 0.25f;
                        boolean showQsOverride = false;

                        if (mQuickPulldown == 1) {
                            showQsOverride = isLayoutRtl ? (x < region) : (w - region < x);
                        } else if (mQuickPulldown == 2) {
                            showQsOverride = isLayoutRtl ? (w - region < x) : (x < region);
                        }
                        if (showQsOverride && mBarState == 0) {
                            hookParam.setResult(true);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        });

        // Back Gesture Height
        hookAllConstructorsIfExists("com.android.systemui.navigationbar.gestural.EdgeBackGestureHandler", param.classLoader, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                mEdgeBackGestureHandler = hookParam.thisObject;
            }
        });

        findAndHookMethodIfExists("com.android.systemui.navigationbar.gestural.BackPanelController", param.classLoader, "onMotionEvent", MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                if (mGestureHeight > 0 && mEdgeBackGestureHandler != null) {
                    try {
                        MotionEvent ev = (MotionEvent) hookParam.args[0];
                        Point size = (Point) getObjectField(mEdgeBackGestureHandler, "mDisplaySize");
                        float bottomH = getFloatField(mEdgeBackGestureHandler, "mBottomGestureHeight");
                        if (size != null && ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            if ((int) ev.getY() < (size.y - bottomH) * (float) mGestureHeight / 100f) {
                                callMethod(mEdgeBackGestureHandler, "cancelGesture", ev);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        });

        // 4G for LTE & Hide LTE+
        findAndHookMethodIfExists("com.android.settingslib.mobile.MobileMappings$Config", param.classLoader, "readConfig", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                try {
                    Object res = hookParam.getResult();
                    if (res != null) {
                        setBooleanField(res, "hideLtePlus", mHideLtePlus);
                        setBooleanField(res, "show4gForLte", mShow4gForLte);
                    }
                } catch (Throwable ignored) {}
            }
        });

        // Hide Next Alarm
        findAndHookMethodIfExists("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", param.classLoader, "updateAlarm", new XC_MethodHook() {
            boolean setup;
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                try {
                    mPhoneStatusBarPolicy = hookParam.thisObject;
                    setup = getBooleanField(hookParam.thisObject, "mCurrentUserSetup");
                    if (mHideNextAlarm) setBooleanField(hookParam.thisObject, "mCurrentUserSetup", false);
                } catch (Throwable ignored) {}
            }
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                try {
                    if (mHideNextAlarm) setBooleanField(hookParam.thisObject, "mCurrentUserSetup", setup);
                } catch (Throwable ignored) {}
            }
        });

        // Hide VPN
        findAndHookMethodIfExists("com.android.systemui.statusbar.phone.StatusBarSignalPolicy", param.classLoader, "onStateChanged", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                try {
                    mStatusBarSignalPolicy = hookParam.thisObject;
                    Object iconController = getObjectField(hookParam.thisObject, "mIconController");
                    String slotVpn = (String) getObjectField(hookParam.thisObject, "mSlotVpn");
                    Handler handler = (Handler) getObjectField(hookParam.thisObject, "mHandler");
                    if (handler != null && iconController != null) {
                        handler.post(() -> callMethod(iconController, "setIconVisibility", slotVpn, !mHideVpn));
                    }
                    if (mHideVpn) hookParam.setResult(null);
                } catch (Throwable ignored) {}
            }
        });

        // Hide QS Footer Build text
        findAndHookMethodIfExists("com.android.systemui.qs.QSFooterView", param.classLoader, "setBuildText", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                try {
                    mQSFooterView = hookParam.thisObject;
                    if (mHideBuildVersion) setObjectField(hookParam.thisObject, "mShouldShowBuildText", false);
                } catch (Throwable ignored) {}
            }
        });

        // Custom Carrier Text
        findAndHookMethodIfExists("com.android.systemui.shade.carrier.ShadeCarrierGroupController", param.classLoader, "handleUpdateState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                try {
                    mShadeCarrierGroupController = hookParam.thisObject;
                    Object[] groups = (Object[]) getObjectField(hookParam.thisObject, "mCarrierGroups");
                    if (groups != null && groups.length > 0) {
                        TextView tv = (TextView) getObjectField(groups[0], "mCarrierText");
                        if (tv != null && mCustomCarrierText != null && !mCustomCarrierText.trim().isEmpty()) {
                            tv.setText(mCustomCarrierText.trim());
                        }
                    }
                } catch (Throwable ignored) {}
            }
        });
    }

    private void hookGoogleDialer(XC_LoadPackage.LoadPackageParam param) {
        hookAllMethods(Resources.class, "getString", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam hookParam) {
                try {
                    if (mPhoneRecorder && hookParam.args.length > 0 && hookParam.args[0] instanceof Integer) {
                        int resId = (Integer) hookParam.args[0];
                        Resources res = (Resources) hookParam.thisObject;
                        String entryName = res.getResourceEntryName(resId);
                        if ("call_recording_starting_voice".equals(entryName) || "call_recording_ending_voice".equals(entryName)) {
                            hookParam.setResult("");
                        }
                    }
                } catch (Throwable ignored) {}
            }
        });
    }

    private void hookOutlook(XC_LoadPackage.LoadPackageParam param) {
        findAndHookMethodIfExists("com.microsoft.office.outlook.olmcore.managers.mdm.DevicePolicy", param.classLoader, "requiresDeviceManagement", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                if (mOutlookPolicy) hookParam.setResult(false);
            }
        });
        findAndHookMethodIfExists("com.microsoft.office.outlook.olmcore.managers.mdm.DevicePolicy", param.classLoader, "isPolicyApplied", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam hookParam) {
                if (mOutlookPolicy) hookParam.setResult(true);
            }
        });
    }

    private void loadPreferences() {
        boolean loaded = false;
        if (mContext != null) {
            try {
                Bundle bundle = mContext.getContentResolver().call(
                        PreferencesProvider.CONTENT_URI,
                        PreferencesProvider.METHOD_GET_ALL, null, null);
                if (bundle != null && !bundle.isEmpty()) {
                    applyPreferencesFromBundle(bundle);
                    loaded = true;
                }
            } catch (Throwable ignored) {}
        }

        if (!loaded) {
            if (prefs == null) {
                try {
                    prefs = new XSharedPreferences(PREF_PACKAGE, PREF_FILE);
                } catch (Throwable ignored) {}
            } else {
                try { prefs.reload(); } catch (Throwable ignored) {}
            }
            applyLoadedPreferences();
        }
    }

    private void reloadPreferencesIfNeeded() {
        boolean reloaded = false;
        if (mContext != null) {
            try {
                Bundle bundle = mContext.getContentResolver().call(
                        PreferencesProvider.CONTENT_URI,
                        PreferencesProvider.METHOD_GET_ALL, null, null);
                if (bundle != null && !bundle.isEmpty()) {
                    applyPreferencesFromBundle(bundle);
                    reloaded = true;
                }
            } catch (Throwable ignored) {}
        }

        if (!reloaded) {
            if (prefs != null) {
                try { prefs.reload(); } catch (Throwable ignored) {}
                applyLoadedPreferences();
            } else {
                loadPreferences();
            }
        }
    }

    private void applyPreferencesFromBundle(Bundle bundle) {
        try {
            if (bundle.containsKey("trick_cursorControl"))
                mCursorControl = Integer.parseInt(bundle.getString("trick_cursorControl", "0"));
            if (bundle.containsKey("trick_skipTrack"))
                mSkipTrack = bundle.getBoolean("trick_skipTrack", false);
            if (bundle.containsKey("trick_powerTorch"))
                mPowerTorch = bundle.getBoolean("trick_powerTorch", false);
            if (bundle.containsKey("trick_torchTimeout"))
                mTorchTimeout = Long.parseLong(bundle.getString("trick_torchTimeout", "0"));
            if (bundle.containsKey("trick_proximityWakeUp"))
                mProximityWakeUp = bundle.getBoolean("trick_proximityWakeUp", false);
            if (bundle.containsKey("trick_doubleTapToSleep"))
                mDoubleTapToSleep = bundle.getBoolean("trick_doubleTapToSleep", false);
            if (bundle.containsKey("trick_quickPulldown"))
                mQuickPulldown = Integer.parseInt(bundle.getString("trick_quickPulldown", "0"));
            if (bundle.containsKey("trick_gestureHeight"))
                mGestureHeight = Integer.parseInt(bundle.getString("trick_gestureHeight", "0"));
            if (bundle.containsKey("trick_hideAdbNotification"))
                mHideAdbNotification = bundle.getBoolean("trick_hideAdbNotification", false);
            if (bundle.containsKey("trick_lessNotifications"))
                mLessNotifications = Integer.parseInt(bundle.getString("trick_lessNotifications", "0"));
            if (bundle.containsKey("trick_screenOffNotifications"))
                mScreenOffNotifications = bundle.getBoolean("trick_screenOffNotifications", false);
            if (bundle.containsKey("trick_hideVpn"))
                mHideVpn = bundle.getBoolean("trick_hideVpn", false);
            if (bundle.containsKey("trick_hideCert"))
                mHideCert = bundle.getBoolean("trick_hideCert", false);
            if (bundle.containsKey("trick_hideNextAlarm"))
                mHideNextAlarm = bundle.getBoolean("trick_hideNextAlarm", false);
            if (bundle.containsKey("trick_show4gForLte"))
                mShow4gForLte = bundle.getBoolean("trick_show4gForLte", false);
            if (bundle.containsKey("trick_hideLtePlus"))
                mHideLtePlus = bundle.getBoolean("trick_hideLtePlus", false);
            if (bundle.containsKey("trick_hideBuildVersion"))
                mHideBuildVersion = bundle.getBoolean("trick_hideBuildVersion", false);
            if (bundle.containsKey("trick_customCarrierText"))
                mCustomCarrierText = bundle.getString("trick_customCarrierText", "");
            if (bundle.containsKey("trick_PhoneRecorder"))
                mPhoneRecorder = bundle.getBoolean("trick_PhoneRecorder", false);
            if (bundle.containsKey("trick_OutlookPolicy"))
                mOutlookPolicy = bundle.getBoolean("trick_OutlookPolicy", false);
        } catch (Throwable ignored) {}
    }

    private void applyLoadedPreferences() {
        if (prefs == null) return;
        try {
            mCursorControl = Integer.parseInt(prefs.getString("trick_cursorControl", "0"));
            mSkipTrack = prefs.getBoolean("trick_skipTrack", false);
            mPowerTorch = prefs.getBoolean("trick_powerTorch", false);
            mTorchTimeout = Long.parseLong(prefs.getString("trick_torchTimeout", "0"));
            mProximityWakeUp = prefs.getBoolean("trick_proximityWakeUp", false);
            mDoubleTapToSleep = prefs.getBoolean("trick_doubleTapToSleep", false);
            mQuickPulldown = Integer.parseInt(prefs.getString("trick_quickPulldown", "0"));
            mGestureHeight = Integer.parseInt(prefs.getString("trick_gestureHeight", "0"));
            mHideAdbNotification = prefs.getBoolean("trick_hideAdbNotification", false);
            mLessNotifications = Integer.parseInt(prefs.getString("trick_lessNotifications", "0"));
            mScreenOffNotifications = prefs.getBoolean("trick_screenOffNotifications", false);
            mHideVpn = prefs.getBoolean("trick_hideVpn", false);
            mHideCert = prefs.getBoolean("trick_hideCert", false);
            mHideNextAlarm = prefs.getBoolean("trick_hideNextAlarm", false);
            mShow4gForLte = prefs.getBoolean("trick_show4gForLte", false);
            mHideLtePlus = prefs.getBoolean("trick_hideLtePlus", false);
            mHideBuildVersion = prefs.getBoolean("trick_hideBuildVersion", false);
            mCustomCarrierText = prefs.getString("trick_customCarrierText", "");
            mPhoneRecorder = prefs.getBoolean("trick_PhoneRecorder", false);
            mOutlookPolicy = prefs.getBoolean("trick_OutlookPolicy", false);
        } catch (Throwable ignored) {}
    }

    private void updatePreferencesFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        String key = extras.getString("preference");
        if (key == null) return;

        switch (key) {
            case "trick_cursorControl":
                mCursorControl = Integer.parseInt(extras.getString("value", "0"));
                break;
            case "trick_skipTrack":
                mSkipTrack = extras.getBoolean("value", false);
                break;
            case "trick_powerTorch":
                mPowerTorch = extras.getBoolean("value", false);
                break;
            case "trick_torchTimeout":
                mTorchTimeout = Long.parseLong(extras.getString("value", "0"));
                break;
            case "trick_proximityWakeUp":
                mProximityWakeUp = extras.getBoolean("value", false);
                break;
            case "trick_doubleTapToSleep":
                mDoubleTapToSleep = extras.getBoolean("value", false);
                break;
            case "trick_quickPulldown":
                mQuickPulldown = Integer.parseInt(extras.getString("value", "0"));
                break;
            case "trick_gestureHeight":
                mGestureHeight = Integer.parseInt(extras.getString("value", "0"));
                break;
            case "trick_hideAdbNotification":
                mHideAdbNotification = extras.getBoolean("value", false);
                if (mUsbHandler != null) {
                    try { callMethod(mUsbHandler, "updateAdbNotification", false); } catch (Throwable ignored) {}
                }
                break;
            case "trick_lessNotifications":
                mLessNotifications = Integer.parseInt(extras.getString("value", "0"));
                break;
            case "trick_screenOffNotifications":
                mScreenOffNotifications = extras.getBoolean("value", false);
                break;
            case "trick_hideVpn":
                mHideVpn = extras.getBoolean("value", false);
                if (mStatusBarSignalPolicy != null) {
                    try { callMethod(mStatusBarSignalPolicy, "onStateChanged"); } catch (Throwable ignored) {}
                }
                break;
            case "trick_hideCert":
                mHideCert = extras.getBoolean("value", false);
                break;
            case "trick_hideNextAlarm":
                mHideNextAlarm = extras.getBoolean("value", false);
                if (mPhoneStatusBarPolicy != null) {
                    try { callMethod(mPhoneStatusBarPolicy, "updateAlarm"); } catch (Throwable ignored) {}
                }
                break;
            case "trick_show4gForLte":
                mShow4gForLte = extras.getBoolean("value", false);
                break;
            case "trick_hideLtePlus":
                mHideLtePlus = extras.getBoolean("value", false);
                break;
            case "trick_hideBuildVersion":
                mHideBuildVersion = extras.getBoolean("value", false);
                if (mQSFooterView != null) {
                    try { callMethod(mQSFooterView, "setBuildText"); } catch (Throwable ignored) {}
                }
                break;
            case "trick_customCarrierText":
                mCustomCarrierText = extras.getString("value", "");
                if (mShadeCarrierGroupController != null) {
                    try { callMethod(mShadeCarrierGroupController, "handleUpdateState"); } catch (Throwable ignored) {}
                }
                break;
            case "trick_PhoneRecorder":
                mPhoneRecorder = extras.getBoolean("value", false);
                break;
            case "trick_OutlookPolicy":
                mOutlookPolicy = extras.getBoolean("value", false);
                break;
        }
        log(TAG + "Live preference updated: " + key);
    }

    private void toggleTorch(boolean enable) {
        if (mCameraManager != null && mCameraId != null) {
            try {
                mCameraManager.setTorchMode(mCameraId, enable);
                mTorchEnabled = enable;
            } catch (Throwable t) {
                log(TAG + "Failed to toggle torch: " + t.getMessage());
            }
        }
    }

    private void unregisterTorchSensor() {
        if (mListenerTorch != null && mSensorManager != null) {
            mSensorManager.unregisterListener(mListenerTorch);
            mListenerTorch = null;
        }
    }

    private void performHaptic(Object phoneWindowManager) {
        try {
            callMethod(phoneWindowManager, "performHapticFeedback",
                    new Class<?>[]{int.class, boolean.class, String.class},
                    HapticFeedbackConstants.LONG_PRESS, false, null);
        } catch (Throwable ignored) {}
    }

    private void injectPowerKey(Context context, long eventTime) {
        try {
            Object inputManager = context.getSystemService(Context.INPUT_SERVICE);
            KeyEvent down = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_POWER, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                    KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_UNKNOWN);
            KeyEvent up = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_POWER, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                    KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_UNKNOWN);
            callMethod(inputManager, "injectInputEvent", down, 0);
            callMethod(inputManager, "injectInputEvent", up, 0);
        } catch (Throwable t) {
            log(TAG + "Failed to inject power key: " + t.getMessage());
        }
    }

    private void findAndHookMethodIfExists(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            Method method = findMethodExactIfExists(className, classLoader, methodName, parameterTypesAndCallback);
            if (method != null) {
                findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
            }
        } catch (Throwable t) {
            log(TAG + "Hook skipped for " + className + "#" + methodName);
        }
    }

    private void hookAllConstructorsIfExists(String className, ClassLoader classLoader, XC_MethodHook callback) {
        try {
            Class<?> clazz = findClassIfExists(className, classLoader);
            if (clazz != null) {
                hookAllConstructors(clazz, callback);
            }
        } catch (Throwable t) {
            log(TAG + "Constructor hook skipped for " + className);
        }
    }

    private void hookAllMethodsIfExists(String className, ClassLoader classLoader, String methodName, XC_MethodHook callback) {
        try {
            Class<?> clazz = findClassIfExists(className, classLoader);
            if (clazz != null) {
                hookAllMethods(clazz, methodName, callback);
            }
        } catch (Throwable t) {
            log(TAG + "Methods hook skipped for " + className + "#" + methodName);
        }
    }
}
