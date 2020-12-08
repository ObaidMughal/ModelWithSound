package com.example.modelwithsound;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    ModelAnimator modelAnimator;
    private int i =0;
    private SoundPool soundPool;
    private int sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            createModel(hitResult.createAnchor(),arFragment);

        });
        loadSoundPool();
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

        sound = soundPool.load(this, R.raw.welcome, 1);

    }

    private void createModel(Anchor anchor, ArFragment arFragment) {

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("skeletal.sfb"))
                .build()
                .thenAccept(modelRenderable -> {

                    AnchorNode anchorNode = new AnchorNode(anchor);

                    SkeletonNode skeletonNode = new SkeletonNode();
                    skeletonNode.setParent(anchorNode);
                    skeletonNode.setRenderable(modelRenderable);

                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                    Button startBtn = findViewById(R.id.startBtn);

                    startBtn.setOnClickListener(v -> {

                        animateModel(modelRenderable);
                        soundPool.play(sound, 1f, 1f, 1, 0
                                , 1f);

                    });



                });


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
