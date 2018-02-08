package dnejad.marjan.browsermodule;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by M.DN
 * On 11/01/2018
 *
 *
 * this class populate in app browser with arrow of backward and forward
 */
public class BrowserMainActivity extends AppCompatActivity {

    private String postUrl = "http://google.com/";
    private WebView webView;
    private ProgressBar progressBar;
    private float m_downX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarBrowser);
        toolbar.setTitle("fake");
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        imgHeader = (ImageView) findViewById(R.id.backdrop);

        if (!TextUtils.isEmpty(getIntent().getStringExtra("postUrl"))) {
            postUrl = getIntent().getStringExtra("postUrl");
        }
        toolbar.setTitle(postUrl);

        initWebView();
//        initCollapsingToolbar();
        renderPost();
    }

    private void initWebView() {
        // Swipe to Refresh
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload(); // refreshes the WebView
                swipeLayout.setRefreshing(false);
            }
        });

        webView.setWebChromeClient(new MyWebChromeClient(this));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /*
                 * Check for the url, if the url is from same domain
                 * open the url in the same activity as new intent
                 * else pass the url to view_browser_menu activity
                 * */
                if (Utils.isSameDomain(postUrl, url)) {
                    webView.loadUrl(url);
                } else {
                    // launch in-app view_browser_menu i.e BrowserActivity
                    openInAppBrowser(url);
                }
                return true;

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                invalidateOptionsMenu();
            }
        });
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    //Multi touch detected
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // save the x
                        m_downX = event.getX();
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        // set x so that it doesn't move
                        event.setLocation(m_downX, event.getY());
                    }
                    break;

                }

                return false;
            }
        });
    }

    private void renderPost() {
        webView.loadUrl(postUrl);
    }

    private void openInAppBrowser(String url) {
        Intent intent = new Intent(BrowserMainActivity.this, BrowserActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_browser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!webView.canGoBack()) {
            menu.getItem(1).setEnabled(false);
            menu.getItem(1).getIcon().setAlpha(130);
        } else {
            menu.getItem(1).setEnabled(true);
            menu.getItem(1).getIcon().setAlpha(255);
        }

        if (!webView.canGoForward()) {
            menu.getItem(2).setEnabled(false);
            menu.getItem(2).getIcon().setAlpha(130);
        } else {
            menu.getItem(2).setEnabled(true);
            menu.getItem(2).getIcon().setAlpha(255);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        if (item.getItemId() == R.id.action_back) {
            back();
        }

        if (item.getItemId() == R.id.action_forward) {
            forward();
        }

        return super.onOptionsItemSelected(item);
    }
    private void back() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    private void forward() {
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }
    }
}