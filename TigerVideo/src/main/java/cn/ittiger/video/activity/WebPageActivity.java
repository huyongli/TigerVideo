package cn.ittiger.video.activity;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.video.R;
import cn.ittiger.video.util.UIUtil;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * 加载网页
 * @author: laohu on 2016/12/27
 * @site: http://ittiger.cn
 */
public class WebPageActivity extends AppCompatActivity {
    @BindView(R.id.webView)
    WebView mWebView;
    @BindView(R.id.webViewLoadingBar)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webpage);
        ButterKnife.bind(this);
        configWebView();
        String url = getIntent().getData().toString();
        mWebView.loadUrl(url);
    }

    private void configWebView() {

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setMax(100);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

            super.onReceivedError(view, request, error);
            UIUtil.showToast(WebPageActivity.this, error.toString());
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

            handler.proceed();
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            super.onProgressChanged(view, newProgress);
            if(newProgress >= 0 || newProgress < 100) {
                mProgressBar.setProgress(newProgress);
            } else if(newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    };
}
