package id.kostlab.stickerapp.ads;

import android.content.Context;

import com.apps.stickerapp.R;
import com.facebook.ads.AdSettings;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import id.kostlab.stickerapp.StickerPackListActivity;
import id.kostlab.stickerapp.StickerPackListAdapter;

/**
 * Implemented in below files
 *
 * See {@link StickerPackListAdapter}
 * See {@link StickerPackListActivity}
 */
public class AdResources {

    private InterstitialAd interstitialAd;
    private com.facebook.ads.InterstitialAd interstitialFbAd;
    public AdResources(){

    }

    public InterstitialAd getInterstitial(final Context context){
        if(null == interstitialAd){
            interstitialAd = new InterstitialAd(context);
        }
        // Test Ad Unit id
        //interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        interstitialAd.setAdUnitId( context.getResources()
                .getString(R.string.google_admob_interstitial_id));
        return interstitialAd;
    }

    public com.facebook.ads.InterstitialAd getFbInterstitial(final Context context){
        AdSettings.addTestDevice("1ca153f5-d24b-4828-8f7c-58a788ed805c");
        interstitialFbAd = new com.facebook.ads.InterstitialAd(context, "355553438827486_355681685481328");
        return interstitialFbAd;
    }

    public AdRequest newAdRequest(){
        return new AdRequest.Builder().build();
    }


}
