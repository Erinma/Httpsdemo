package com.example.multiesthreaddemo;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private EditText downloadpathText;
    private TextView resultView;
    private ProgressBar progressBar;

    /**
     * 当Handler被创建会关联到创建它的当前线程的消息队列，该类用于往消息队列发送消息
     * 消息队列中的消息由当前线程内部进行处理
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    progressBar.setProgress(msg.getData().getInt("size"));
                    float num = (float) progressBar.getProgress() / (float) progressBar.getMax();
                    int result = (int) (num * 100);
                    resultView.setText(result + "%");

                    if (progressBar.getProgress() == progressBar.getMax()) {
                        Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();
                    }
                    break;
                case -1:
                    Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadpathText = (EditText) this.findViewById(R.id.path);
        progressBar = (ProgressBar) this.findViewById(R.id.downloadbar);
        resultView = (TextView) this.findViewById(R.id.resultView);
        Button button = (Button) this.findViewById(R.id.button);
        downloadpathText.setText("http://dldir1.qq.com/weixin/android/weixin6522android1160.apk");
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String path = downloadpathText.getText().toString();
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    download(path, new File(Environment.getExternalStorageDirectory().getPath() + "/down"));
                } else {
                    Toast.makeText(MainActivity.this, R.string.sdcarderror, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //Content-Length:56993782
    //DownloadThread: Thread 1 start download from position 0
// I/DownloadThread: Thread 2 start download from position 18997928
    // I/DownloadThread: Thread 3 start download from position 37995856


    /**
     * 主线程(UI线程)
     * 对于显示控件的界面更新只是由UI线程负责，如果是在非UI线程更新控件的属性值，更新后的显示界面不会反映到屏幕上
     *
     * @param path
     * @param savedir
     */
    private void download(final String path, final File savedir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileDownloader loader = new FileDownloader(MainActivity.this, path, savedir, 3);
                progressBar.setMax(loader.getFileSize());//设置进度条的最大刻度为文件的长度

                try {
                    loader.download(new FileDownloader.DownloadProgressListener() {
                        @Override
                        public void onDownloadSize(int size) {//实时获知文件已经下载的数据长度
                            Message msg = new Message();
                            msg.what = 1;
                            msg.getData().putInt("size", size);
                            handler.sendMessage(msg);//发送消息
                        }
                    });
                } catch (Exception e) {
                    handler.obtainMessage(-1).sendToTarget();
                }
            }
        }).start();
    }

}
