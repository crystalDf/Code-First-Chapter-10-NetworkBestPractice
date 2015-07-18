package com.star.networkbestpractice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity {

    public static final int SHOW_RESPONSE = 0;

//    public static final String URL_ADDRESS = "http://www.baidu.com";
    public static final String URL_ADDRESS = "http://192.168.1.5:8080/get_data/get_data.xml";

    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";

    public static final int TIME_OUT = 8000;

    private Button mSendRequestButton;

    private TextView mResponseTextView;

    private WebView mWebView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    mResponseTextView.setText(response);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendRequestButton = (Button) findViewById(R.id.send_request);
        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestWithHttpURLConnection();
            }
        });

        mResponseTextView = (TextView) findViewById(R.id.response);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.loadUrl(URL_ADDRESS);
    }

    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection = null;
                try {
                    URL url = new URL(URL_ADDRESS);

                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestMethod(GET_METHOD);
                    httpURLConnection.setConnectTimeout(TIME_OUT);
                    httpURLConnection.setReadTimeout(TIME_OUT);

                    InputStream inputStream = httpURLConnection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(inputStream));

                    StringBuilder response = new StringBuilder();

                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }

                    Message message = new Message();

                    message.what = SHOW_RESPONSE;
                    message.obj = response.toString();

                    mHandler.sendMessage(message);

                    httpURLConnection.disconnect();

                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    inputStream = httpURLConnection.getInputStream();

                    parseXMLWithDom(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void parseXMLWithDom(InputStream inputStream) {

        try {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(inputStream);

            Element element = document.getDocumentElement();

            NodeList nodeList = element.getElementsByTagName("app");

            if ((nodeList != null) && (nodeList.getLength() > 0)) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element app = (Element) nodeList.item(i);

                    Element id = (Element)
                            app.getElementsByTagName("id").item(0);
                    Element name = (Element)
                            app.getElementsByTagName("name").item(0);
                    Element version = (Element)
                            app.getElementsByTagName("version").item(0);

                    if (app != null && id != null && name != null && version != null) {
                        String idString = id.getFirstChild().getNodeValue();

                        String nameString = name.getFirstChild().getNodeValue();

                        String versionString = version.getFirstChild().getNodeValue();

                        Log.d("MainActivity", "id is " + idString);
                        Log.d("MainActivity", "name is " + nameString);
                        Log.d("MainActivity", "version is " + versionString);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
