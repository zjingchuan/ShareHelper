package com.sharehelper.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.utils.Log;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    public static final String WEIXIN_APP_ID = "wx63f0dc4f17604a0c";
    public static final String WEIXIN_APP_SECTET = "f5f1ef522941d118e5acf7f727c0d4a5";
    private static final String TAG = "sharehelper";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.d(TAG, "action:" + action);
        Log.d(TAG, "type:" + type);
        final Bundle extras = intent.getExtras();
        if (extras != null)
            for (final String key : extras.keySet()) {
                Log.d(TAG, "key:" + key + ",value:" + extras.get(key));
            }
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {

        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String sharedTitle = null;
        if (intent.getExtras().containsKey(Intent.EXTRA_TITLE))
            sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
        else if (intent.getExtras().containsKey(Intent.EXTRA_SUBJECT))
            sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            share(sharedTitle, sharedText, null);
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.d(TAG, imageUri.toString());
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            for (final Uri imageUri : imageUris) {
                Log.d(TAG, imageUri.toString());
            }
            // Update UI to reflect multiple images being shared

        }
    }


    void share(String title, String text, Uri imageUri) {
        Log.d(TAG, "title:" + title + ",text:" + text + ",image:" + (imageUri == null ? "null" : imageUri.toString()));
        UMSocialService controller = UMServiceFactory.getUMSocialService("com.umeng.share");
        // 添加微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(this, WEIXIN_APP_ID, WEIXIN_APP_SECTET);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
        CircleShareContent content = new CircleShareContent();
        content.setAppWebSite(text);
        content.setShareContent(text);
        content.setShareImage(new UMImage(this, R.drawable.ic_launcher));
        if (text != null && text.startsWith("http://"))
            content.setTargetUrl(text);
        if (!TextUtils.isEmpty(title))
            content.setTitle(title);
        controller.setShareMedia(content);
        controller.directShare(this, SHARE_MEDIA.WEIXIN_CIRCLE, new SocializeListeners.SnsPostListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(final SHARE_MEDIA share_media, final int i, final SocializeEntity socializeEntity) {

            }
        });
    }
}
