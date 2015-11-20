package com.sharehelper.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final String WEIXIN_APP_ID = "wx63f0dc4f17604a0c";
    public static final String WEIXIN_APP_SECTET = "f5f1ef522941d118e5acf7f727c0d4a5";
    private static final String TAG = "sharehelper";
    private IWXAPI wxapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wxapi = WXAPIFactory.createWXAPI(this, WEIXIN_APP_ID, true);
        wxapi.registerApp(WEIXIN_APP_ID);

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
            } else if (type.startsWith("imageList/")) {
                handleSendImage(intent); // Handle single imageList being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("imageList/")) {
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
            // Update UI to reflect imageList being shared
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


    void share(final String title, final String text, final Uri imageUri) {
        Log.d(TAG, "title:" + title + ",text:" + text + ",imageList:" + (imageUri == null ? "null" : imageUri.toString()));

        new AsyncTask<Void, Void, ShareContent>() {
            @Override
            protected ShareContent doInBackground(Void... params) {
                String url = text.substring(text.indexOf("http"), text.length());

                Document document = null;
                try {
                    document = Jsoup.connect(url).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ShareContent content = new ShareContent();
                content.setUrl(url);
                content.setTitle(document.title());
                Elements images = document.getElementsByTag("img");
                for (Element image : images) {
                    String src = image.attr("src");
                    KLog.v(src);
                    if (src.startsWith("http")) {
                        content.addImage(src);
                    }
                }
                content.setContent(text);
                KLog.d(content.toString());
                return content;
            }

            @Override
            protected void onPostExecute(ShareContent shareContent) {
                WXImageObject imageObject = new WXImageObject();
//                imageObject.setImagePath(shareContent.getImageList());
                WXMediaMessage message = new WXMediaMessage();
                message.mediaObject = imageObject;
                SendMessageToWX.Req request = new SendMessageToWX.Req();
                request.message = message;
                wxapi.sendReq(request);
            }
        }.execute();

//
    }

    public static class ShareContent {
        List<String> imageList;
        String title;
        String content;
        String url;

        @Override
        public String toString() {
            return "ShareContent{" +
                    "imageList='" + imageList + '\'' +
                    ", title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getImageList() {
            return imageList;
        }

        public void setImageList(List<String> imageList) {
            this.imageList = imageList;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void addImage(String src) {
            if (imageList==null)
                imageList = new ArrayList<String>();
            imageList.add(src);
        }
    }
}
