package mgeier63.openpgpapilibdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.openintents.openpgp.util.OpenPgpApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {
    static final int NUM_TEST_THREADS = 100;
    private static final int REQUEST_CODE_SELECT_KEY = 66;
    private Api mApi;
    private boolean mStopLoad = false;
    private long mKeyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.button3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkServiceAllowedAndKeyIsThere();
            }


        });

        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    testStress(mApi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        });


        mApi = new Api(this);
    }

    private void checkServiceAllowedAndKeyIsThere() {
        if (!mApi.isServiceBound()) {
            setMessage("Error, service not bound!");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
       // intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, KEY_ID);

        Intent result = mApi.executeApi(intent, null, null);
        int resultCode =
                result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);

        if (resultCode == OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED) {

            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
            try {

                    this.startIntentSenderForResult(pi.getIntentSender(), REQUEST_CODE_SELECT_KEY,
                            null, 0, 0, 0);
            } catch (final IntentSender.SendIntentException ignored) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_CODE_SELECT_KEY) {
            mKeyId = data.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID,0);
            setMessage("OpenKeychain set up OK! \nusing key "+Long.toHexString(mKeyId));
        }
    }


    public void testStress(final IApi api) {
        if (!api.isServiceBound()) {
            setMessage("Error, service not bound!");
            return;
        }
        if (mKeyId==0) {
            setMessage("select a key first!");
            return;
        }
        setMessage("tests running...");

        generateLoad();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    WorkerThread[] threads = new WorkerThread[NUM_TEST_THREADS];
                    boolean[] result = new boolean[NUM_TEST_THREADS];
                    for (int i = 0; i < NUM_TEST_THREADS; i++) {
                        threads[i] = new WorkerThread(api, i);
                    }
                    for (int i = 0; i < NUM_TEST_THREADS; i++) {
                        threads[i].start();
                    }

                    int cntFailed = 0;
                    for (int i = 0; i < NUM_TEST_THREADS; i++) {
                        threads[i].join();
                        if (threads[i].failed) {
                            cntFailed++;
                        }
                    }

                    mStopLoad=true;
                    String msg = cntFailed == 0 ? "Everything OK, all tests passed!" : String.format("Gotcha: Data truncated on %d of %d invocations!", cntFailed, NUM_TEST_THREADS);

                    setMessage(msg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setMessage(ex.toString());
                }
            }
        }).start();

    }

    private void generateLoad() {
        mStopLoad = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!mStopLoad) {
                        new Object();
                    }
                }
            }).start();

    }


    public interface IApi {
        Intent executeApi(Intent intent, InputStream is, OutputStream os);

        boolean isServiceBound();
    }

    private class WorkerThread extends Thread {
        private final int mI;
        private final IApi mApi;
        boolean failed = false;

        public WorkerThread(IApi api, int i) {
            mApi = api;
            mI = i;
        }

        @Override
        public void run() {
            String asciiArmoredStuff = "";

            Intent intent = new Intent();
            intent.setAction(OpenPgpApi.ACTION_ENCRYPT);
            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
            intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, new long[]{mKeyId});
            try {

                ByteArrayInputStream is = new ByteArrayInputStream(new byte[234567]);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                Intent result = mApi.executeApi(intent, is, baos);

                asciiArmoredStuff = new String(baos.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (!asciiArmoredStuff.endsWith("-----END PGP MESSAGE-----\n")) {
                failed = true;
            }
        }
    }


    private void setMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.textView2)).setText(msg);
            }
        });
    }
}
