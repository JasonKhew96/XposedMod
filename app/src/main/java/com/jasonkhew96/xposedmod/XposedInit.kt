package com.jasonkhew96.xposedmod

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.util.Log
import dalvik.system.BaseDexClassLoader
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.iacn.biliroaming.utils.DexHelper

class XposedInit : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        XposedHelpers.findAndHookMethod(Instrumentation::class.java,
            "callApplicationOnCreate",
            Application::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    try {
                        if (lpparam?.processName == "eu.faircode.email") {
                            hookFairMail(lpparam.classLoader)
                        }
                        if (lpparam?.processName == "onlymash.flexbooru.play") {
                            hookFlexbooru(lpparam.classLoader)
                        }
                        if (lpparam?.processName == "com.google.android.apps.youtube.music") {
                            hookYouTubeMusic(lpparam.classLoader)
                        }
                        if (lpparam?.processName == "com.google.android.youtube") {
                            hookYouTube(lpparam.classLoader)
                        }
                        if (lpparam?.processName == "studio.scillarium.ottnavigator") {
                            hookOttNavigator(lpparam.classLoader)
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Failed to hook ${lpparam?.processName}", t)
                    }
                }
            })
    }

    companion object {
        const val TAG = "XposedMod"
        fun hookFairMail(classLoader: ClassLoader) {
            Log.d(TAG, "Hooking FairMail")
            XposedHelpers.findAndHookMethod("eu.faircode.email.ActivityBilling",
                classLoader,
                "isPro",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        param?.result = true
                    }
                })
        }

        fun hookFlexbooru(classLoader: ClassLoader) {
            Log.d(TAG, "Hooking Flexbooru")
            try {
                System.loadLibrary("xposedmod")
            } catch (e: Throwable) {
                Log.d(TAG, "Unable to load library" + e.message)
                return
            }
            val dexHelper = DexHelper(classLoader.findDexClassLoader() ?: return)
            val isOrderSuccessMethod = dexHelper.findMethodUsingString(
                "order_success",
                false,
                dexHelper.encodeClassIndex(Boolean::class.java),
                0,
                null,
                -1,
                null,
                null,
                null,
                true
            ).asSequence().firstNotNullOfOrNull {
                dexHelper.decodeMethodIndex(it)
            } ?: return
            val isOrderSuccessMethodName = isOrderSuccessMethod.name
            val settingsClassName = isOrderSuccessMethod.declaringClass.name
            XposedHelpers.findAndHookMethod(
                settingsClassName,
                classLoader,
                isOrderSuccessMethodName,
                XC_MethodReplacement.returnConstant(true)
            )
        }

        fun hookYouTubeMusic(classLoader: ClassLoader) {
            Log.d(TAG, "Hooking YouTube Music")
            try {
                System.loadLibrary("xposedmod")
            } catch (e: Throwable) {
                Log.d(TAG, "Unable to load library" + e.message)
                return
            }
            val dexHelper = DexHelper(classLoader.findDexClassLoader() ?: return)
            val checkBackgroundPausedMethodIndex = dexHelper.findMethodUsingString(
                "MUSIC_BACKGROUND_PAUSED_UPSELL",
                false,
                dexHelper.encodeClassIndex(Void.TYPE),
                0,
                null,
                dexHelper.encodeClassIndex(classLoader.loadClass("com.google.android.apps.youtube.music.watch.WatchFragment")),
                null,
                null,
                null,
                true
            ).asSequence().firstOrNull() ?: return
            val isBackgroundAllowedMethod = dexHelper.findMethodInvoking(
                checkBackgroundPausedMethodIndex,
                dexHelper.encodeClassIndex(Boolean::class.java),
                1,
                null,
                -1,
                null,
                null,
                null,
                true
            ).asSequence().firstNotNullOfOrNull {
                dexHelper.decodeMethodIndex(it)
            } ?: return
            XposedBridge.hookMethod(
                isBackgroundAllowedMethod, XC_MethodReplacement.returnConstant(true)
            )

            val showVideoAdsMethodIndex = dexHelper.findMethodUsingString(
                "loadVideo() called on LocalDirector in wrong state",
                false,
                -1,
                2,
                null,
                -1,
                null,
                null,
                null,
                true
            ).firstOrNull() ?: return
            val shouldShowVideoAdsMethod = dexHelper.findMethodInvoking(
                showVideoAdsMethodIndex,
                dexHelper.encodeClassIndex(Void.TYPE),
                1,
                null,
                -1,
                longArrayOf(dexHelper.encodeClassIndex(Boolean::class.java)),
                null,
                null,
                true,
            ).asSequence().firstNotNullOfOrNull {
                dexHelper.decodeMethodIndex(it)
            } ?: return
            XposedBridge.hookMethod(
                shouldShowVideoAdsMethod, XC_MethodReplacement.returnConstant(false)
            )
        }

        fun hookYouTube(classLoader: ClassLoader) {
            Log.d(TAG, "Hooking YouTube")
            try {
                System.loadLibrary("xposedmod")
            } catch (e: Throwable) {
                Log.d(TAG, "Unable to load library" + e.message)
                return
            }
            val dexHelper = DexHelper(classLoader.findDexClassLoader() ?: return)
            val showVideoAdsMethodIndex = dexHelper.findMethodUsingString(
                "loadVideo() called on LocalDirector in wrong state",
                false,
                -1,
                2,
                null,
                -1,
                null,
                null,
                null,
                true
            ).firstOrNull() ?: return
            val shouldShowVideoAdsMethod = dexHelper.findMethodInvoking(
                showVideoAdsMethodIndex,
                dexHelper.encodeClassIndex(Void.TYPE),
                1,
                null,
                -1,
                longArrayOf(dexHelper.encodeClassIndex(Boolean::class.java)),
                null,
                null,
                true,
            ).asSequence().firstNotNullOfOrNull {
                dexHelper.decodeMethodIndex(it)
            } ?: return
            XposedBridge.hookMethod(
                shouldShowVideoAdsMethod, XC_MethodReplacement.returnConstant(false)
            )
        }
        
        fun hookOttNavigator(classLoader: ClassLoader) {
            Log.d(TAG, "Hooking OTT Navigator")
            try {
                System.loadLibrary("xposedmod")
            } catch (e: Throwable) {
                Log.d(TAG, "Unable to load library" + e.message)
                return
            }
            val dexHelper = DexHelper(classLoader.findDexClassLoader() ?: return)
            val premiumClass = dexHelper.findMethodUsingString(
                "cmp3",
                false,
                dexHelper.encodeClassIndex(Void.TYPE),
                0,
                null,
                -1,
                null,
                null,
                null,
                true
            ).asSequence().firstNotNullOfOrNull { 
                dexHelper.decodeMethodIndex(it)?.declaringClass
            } ?: return
            for (method in premiumClass.declaredMethods) {
                if (method.returnType == Int::class.java) {
                    XposedBridge.hookMethod(
                        method, XC_MethodReplacement.returnConstant(2)
                    )
                }
                if (method.returnType == Boolean::class.java && method.parameterTypes.isEmpty()) {
                    XposedBridge.hookMethod(
                        method, XC_MethodReplacement.returnConstant(true)
                    )
                }
            }
        }
    }
}


fun ClassLoader.findDexClassLoader(): BaseDexClassLoader? {
    var classLoader = this
    while (classLoader !is BaseDexClassLoader) {
        if (classLoader.parent != null) classLoader = classLoader.parent
        else return null
    }
    return classLoader
}
