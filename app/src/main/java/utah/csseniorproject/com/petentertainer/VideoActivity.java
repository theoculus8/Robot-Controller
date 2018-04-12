package utah.csseniorproject.com.petentertainer;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

public class VideoActivity extends Activity implements IVLCVout.Callback {
    private static final String TAG = VideoActivity.class.getSimpleName();

    private final int VIDEO_PORT = 1235;

    private final double VIDEO_ASPECT_RATIO = 9.0 / 16.0;

    private SurfaceView mSurfaceView;
    private FrameLayout mSurfaceFrame;
    private SurfaceHolder mSurfaceHolder;
    private Surface mSurface = null;

    private LibVLC mLibVLC;

    private MediaPlayer mMediaPlayer;

    private String armAddress;
    private String chassisAddress;

    private ControllerView controllerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        armAddress = getIntent().getExtras().getString("armAddress");
        chassisAddress = getIntent().getExtras().getString("chassisAddress");

        try {
            controllerView = new ControllerView(this, armAddress, chassisAddress);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        controllerView.setEventListener(new ControllerView.ControllerSwitchListener() {
            @Override
            public void onEventOccurred(ControllerView.Device currentDevice) {
                switch (currentDevice) {
                    case arm: {
                        mMediaPlayer.stop();

                        Media media = new Media(mLibVLC, Uri.parse("http://jeremy:jeremy@" + armAddress + ":" + VIDEO_PORT + "/cam_pic_new.php?"));
                        media.setHWDecoderEnabled(true, false);
                        media.addOption(":network-caching=100");
                        media.addOption(":clock-jitter=0");
                        media.addOption(":clock-synchro=0");
                        //media.addOption(":transform-type=180");

                        mMediaPlayer.setMedia(media);
                        mMediaPlayer.play();
                        break;
                    }
                    case chassis: {
                        mMediaPlayer.stop();

                        Media media = new Media(mLibVLC, Uri.parse("http://jeremy:jeremy@" + chassisAddress + ":" + VIDEO_PORT + "/cam_pic_new.php?"));
                        media.setHWDecoderEnabled(true, false);
                        media.addOption(":network-caching=100");
                        media.addOption(":clock-jitter=0");
                        media.addOption(":clock-synchro=0");
                        //media.addOption(":transform-type=180");

                        mMediaPlayer.setMedia(media);
                        mMediaPlayer.play();
                        break;
                    }
                }
            }
        });

        mSurfaceFrame = findViewById(R.id.player_surface_frame);
        mSurfaceFrame.addView(controllerView);

        mLibVLC = new LibVLC(getApplicationContext());

        mMediaPlayer = new MediaPlayer(mLibVLC);

        IVLCVout vout = mMediaPlayer.getVLCVout();
        mSurfaceView = findViewById(R.id.player_surface);
        vout.setVideoView(mSurfaceView);
        vout.setWindowSize(Resources.getSystem().getDisplayMetrics().widthPixels, (int) (Resources.getSystem().getDisplayMetrics().widthPixels * VIDEO_ASPECT_RATIO));
        vout.attachViews();

        Media media = new Media(mLibVLC, Uri.parse("http://jeremy:jeremy@" + chassisAddress + ":" + VIDEO_PORT + "/cam_pic_new.php?"));//Change back to chassis
        //Media media = new Media(mLibVLC, Uri.parse("tcp/h264://" + armAddress + ":3333"));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=100");
        media.addOption(":clock-jitter=0");
        media.addOption(":clock-synchro=0");
        //media.addOption(":transform-type=180");

        mMediaPlayer.setMedia(media);
        mMediaPlayer.setAspectRatio("16:9");
        mMediaPlayer.play();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }

        if (controllerView != null) {
            controllerView.killThreads();
        }
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }
}
