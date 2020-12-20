package com.example.modelwithsound;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    ModelAnimator modelAnimator;
    private int i =0;
    private SoundPool soundPool;
    private int sound;
    private Boolean modelPlaced=false;
    private ArFragment arFragment;
    private Button startBtn;
    private ModelRenderable renderable;
    private Anchor anchor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
//        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
//
//            createModel(hitResult.createAnchor(),arFragment);
//
//        });
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
        startBtn = findViewById(R.id.startBtn);


        loadSoundPool();
    }

    private void onUpdate(FrameTime frameTime) {

//        if (modelPlaced)
//            return;

        Frame frame=arFragment.getArSceneView().getArFrame();
        Collection<Plane> planes=frame.getUpdatedTrackables(Plane.class);

        for (Plane plane:planes){

            if (plane.getTrackingState()== TrackingState.TRACKING){

                anchor = plane.createAnchor(plane.getCenterPose());


                startBtn.setOnClickListener(v -> {

                    if(modelPlaced)
                        return;

                    createModel(anchor,arFragment);

                });


                break;
            }
        }
    }



    private void loadSoundPool() {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        sound = soundPool.load(this, R.raw.custom, 1);

    }

    private void createModel(Anchor anchor, ArFragment arFragment) {

        modelPlaced=true;

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("female.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    renderable = modelRenderable;
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    SkeletonNode skeletonNode = new SkeletonNode();
                    skeletonNode.setParent(anchorNode);
                    skeletonNode.setRenderable(renderable);
                    skeletonNode.setLocalRotation(Quaternion.axisAngle
                            (new Vector3(0, 1f, 0), 120f));
                    arFragment.getArSceneView().getScene().addChild(anchorNode);
                    animateModel(renderable);



                });
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       try {
                           Thread.sleep(4000);
                           soundPool.play(sound, 1f, 1f, 1, 0
                                   , 1f);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }

                   }
               }).start();





    }


    private void animateModel(ModelRenderable modelRenderable) {

        if (modelAnimator!=null && modelAnimator.isRunning())
            modelAnimator.end();

        int animateCount = modelRenderable.getAnimationDataCount();

        if (i==animateCount)
            i=0;

        AnimationData animationData = modelRenderable.getAnimationData(i);

        modelAnimator= new ModelAnimator(animationData,modelRenderable);
        modelAnimator.start();
        i++;





    }
}
