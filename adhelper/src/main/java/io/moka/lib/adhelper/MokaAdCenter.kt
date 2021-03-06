package io.moka.lib.adhelper

import android.content.Context
import com.facebook.ads.*
import com.google.android.gms.ads.MobileAds
import com.tnkfactory.ad.TnkSession
import io.moka.lib.base.MokaBase

class AdItem(val position: Int, val key: String)

object MokaAdCenter {

    var ADMOB_TEST_CODE: String = ""

    var AUDIENCE_TEST_CODE: String = ""
        set(value) {
            field = value
            AdSettings.addTestDevice(value)
        }

    fun initialize(context: Context, admobAppKey: String? = "") {
        if (admobAppKey?.isNotEmpty() == true) {
            MobileAds.initialize(context, admobAppKey)
        }

        AudienceNetworkAds.initialize(context)
    }

    fun initializeTNK(context: Context) {
        TnkSession.initInstance(context)
    }

    var debuggable: Boolean
        get() = MokaBase.DEBUG
        set(value) {
            MokaBase.DEBUG = value
        }

    /**
     * MUST SET BEFORE LOAD
     *
     * facebook Key , admob Key
     */

    val audienceNativeAdHash: HashMap<Int, NativeAd> = hashMapOf()
    val audienceBannerAdHash: HashMap<Int, AdView> = hashMapOf()

    val admobAdHash: HashMap<Int, com.google.android.gms.ads.AdView> = hashMapOf()

    /*
    Facebook audience
     */

    fun loadFbAudienceNativeAd(context: Context, adItem: AdItem) {
        if (null == audienceNativeAdHash[adItem.position]) {
            audienceNativeAdHash[adItem.position] = NativeAd(context, adItem.key)
        }

        val audienceNative = audienceNativeAdHash[adItem.position]!!

        if (!audienceNative.isAdLoaded) {
            try {
                audienceNative.loadAd(NativeAdBase.MediaCacheFlag.ALL)
            }
            catch (ignore: Exception) {
            }
        }
    }

    fun reloadFbAudienceNativeAd(context: Context, adItem: AdItem) {
        val nativeAd = NativeAd(context, adItem.key)
        audienceNativeAdHash[adItem.position] = nativeAd

        try {
            nativeAd.loadAd(NativeAdBase.MediaCacheFlag.ALL)
        }
        catch (ignore: Exception) {
        }
    }

    fun makeFbAudienceBannerAd(context: Context, adItem: AdItem) {
        audienceBannerAdHash[adItem.position] = AdView(context, adItem.key, AdSize.BANNER_HEIGHT_50)
    }

    /*
    Google admob
     */

    fun makeAdmobAd(context: Context, adItem: AdItem) {
        admobAdHash[adItem.position] = com.google.android.gms.ads.AdView(context).apply {
            adSize = com.google.android.gms.ads.AdSize.SMART_BANNER
            adUnitId = adItem.key
        }
    }

}