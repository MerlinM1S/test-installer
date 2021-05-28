package com.Default.testinstaller

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


public interface IProgressListener {
    fun onProgress(text:String);
}

class MainActivity : AppCompatActivity(), IProgressListener, Thread.UncaughtExceptionHandler {
    private var textDisplay: TextView? = null;
    private var downloader: Downloader = Downloader();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textDisplay = findViewById(R.id.textDisplay);

        Thread.setDefaultUncaughtExceptionHandler(this);

        downloader.onCreate(this);
        // downloader.startDownload("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", this);
        downloader.startDownload("https://github.com/MerlinM1S/improved-eureka-release/raw/main/Android.apk", this);
    }

    override fun onDestroy() {
        super.onDestroy()

        downloader.onDestroy();
    }

    override fun onProgress(text: String) {
        var previousText = textDisplay?.text;
        if(previousText.isNullOrBlank()) {
            textDisplay?.text = text;
        } else {
            textDisplay?.text = previousText.toString() + "\r\n" + text;
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        TODO("Not yet implemented")
    }
}