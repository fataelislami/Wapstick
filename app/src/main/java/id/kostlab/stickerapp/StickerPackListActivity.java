/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package id.kostlab.stickerapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.stickerapp.BuildConfig;
import com.apps.stickerapp.R;

import id.kostlab.stickerapp.ads.AdResources;
import id.kostlab.stickerapp.analytics.Analytics;
import id.kostlab.stickerapp.reviews.AppReview;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class StickerPackListActivity extends BaseActivity {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private static final String TAG = "StickerPackList";
    private static final String INTENT_ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK";
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;
    private StickerPackListAdapter allStickerPacksListAdapter;
    WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    ArrayList<StickerPack> stickerPackList;
    private com.facebook.ads.AdView fbAdView;
    private InterstitialAd interstitialAd;
    private com.facebook.ads.InterstitialAd interstitialFbAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);

        setToolBar();

        packRecyclerView = findViewById(R.id.sticker_pack_list);

        stickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
        showStickerPackList(stickerPackList);
//        ADS CONFIG
        AdResources adResources = new AdResources();
//        ADMOB CONFIG
         String admobOptListInter=getString(R.string.admobOptListInter);
         String admobOptListBanner=getString(R.string.admobOptListBanner);
        if(admobOptListBanner.equals("true")){
            admobAdinit();
        }else{
            AdView adView=findViewById(R.id.adView);
            adView.setVisibility(View.GONE);
        }
        if(admobOptListInter.equals("true")){
            interstitialAd = adResources.getInterstitial(this);
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
//        FACEBOOK ADS CONFIG
        String facebookOptListBanner=getString(R.string.facebookOptListBanner);
        String facebookOptListInter=getString(R.string.facebookOptListInter);
        if(facebookOptListBanner.equals("true")){
            facebookAdInit();
        }else{
            LinearLayout adContainer = (LinearLayout) findViewById(R.id.fbAdView);
            adContainer.setVisibility(View.GONE);
        }
        if(facebookOptListInter.equals("true")){
            interstitialFbAd = adResources.getFbInterstitial(this);
        }

        // https://developers.google.com/admob/android/interstitial

        Analytics.getInstance(this).logAppStart();

        AppReview.getInstance().init(this);
    }

    private void admobAdinit() {
//        Admob Init
        MobileAds.initialize(this, initializationStatus -> {});
        AdView mAdView = findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    private void facebookAdInit(){
        //        Fb Init
        AdSettings.addTestDevice(getString(R.string.facebookTestDevice));
        AudienceNetworkAds.initialize(this);
        fbAdView = new com.facebook.ads.AdView(this, getString(R.string.facebook_ads_banner), AdSize.BANNER_HEIGHT_50);
        // Find the Ad Container
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.fbAdView);

        // Add the ad view to your activity layout
        adContainer.addView(fbAdView);

        // Request an ad
        fbAdView.loadAd();
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        //noinspection unchecked
        whiteListCheckAsyncTask.execute(stickerPackList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
            whiteListCheckAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        String facebookOptListBanner=getString(R.string.facebookOptListBanner);
        if(facebookOptListBanner.equals("true")){
            if (fbAdView != null) {
                fbAdView.destroy();
            }
        }
        super.onDestroy();
    }

    private void showStickerPackList(List<StickerPack> stickerPackList) {
        allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList, onAddButtonClickedListener, this);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);
        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // setDividerForRecyclerView();
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private void setDividerForRecyclerView() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                packRecyclerView.getContext(),
                packLayoutManager.getOrientation()
        );
        packRecyclerView.addItemDecoration(dividerItemDecoration);
    }


    private StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> {
        String admobOptListInter=getString(R.string.admobOptListInter);
        if(admobOptListInter.equals("true")){
            if(interstitialAd.isLoaded()){
                interstitialAd.show();
            } else {
                Toast.makeText(this, "Ad not loaded", Toast.LENGTH_SHORT).show();
            }
        }
        String facebookOptListInter=getString(R.string.facebookOptListInter);
        if(facebookOptListInter.equals("true")){
            InterstitialAdListener interstitialAdListener=new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {

                }

                @Override
                public void onError(Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    interstitialFbAd.show();
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            interstitialFbAd.loadAd(
                    interstitialFbAd.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build());
        }

        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION_ENABLE_STICKER_PACK);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, pack.identifier);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_NAME, pack.name);
        try {
            startActivityForResult(intent, StickerPackDetailsActivity.ADD_PACK);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(StickerPackListActivity.this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show();
        }
    };

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int max = Math.max(viewHolder.imageRowView.getMeasuredWidth() / previewSize, 1);
            int numColumns = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            allStickerPacksListAdapter.setMaxNumberOfStickersInARow(numColumns);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StickerPackDetailsActivity.ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED && data != null) {
                final String validationError = data.getStringExtra("validation_error");
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        //validation error should be shown to developer only, not users.
                        MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(getSupportFragmentManager(), "validation error");
                    }
                    Log.e(TAG, "Validation failed:" + validationError);
                }
            }
        }
    }


    static class WhiteListCheckAsyncTask extends AsyncTask<List<StickerPack>, Void, List<StickerPack>> {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        @SafeVarargs
        @Override
        protected final List<StickerPack> doInBackground(List<StickerPack>... lists) {
            List<StickerPack> stickerPackList = lists[0];
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity == null) {
                return stickerPackList;
            }
            for (StickerPack stickerPack : stickerPackList) {
                stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
            }
            return stickerPackList;
        }

        @Override
        protected void onPostExecute(List<StickerPack> stickerPackList) {
            final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
            if (stickerPackListActivity != null) {
                stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
            }
        }
    }
}
