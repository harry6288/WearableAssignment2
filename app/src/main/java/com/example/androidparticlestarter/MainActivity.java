package com.example.androidparticlestarter;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

public class MainActivity extends AppCompatActivity {


    String[] fruit = {"Apple", "Oranges", "Cherry", "Jet Fruit", "Peach", "Banana"};

    String[] animal = {"Cat", "Dog", "Horse", "Crocodile", "Cow", "Donkey"};

    String displayWord = "";
    int gameRounds;
    int wordIn = 0;
    int g_flag;
    int g_index;
    TextView question;
    String dataFromParticle = "";
    String particleId = "";
    private final String TAG = "hardeep";
    // Data data = new Data();
    Random r = new Random();
    // MARK: Particle Account Info
    private final String PARTICLE_USERNAME = "hardeepwalia019@gmail.com";
    private final String PARTICLE_PASSWORD = "ahluwalia";

    // MARK: Particle Publish / Subscribe variables
    private long subscriptionId;

    // MARK: Particle device
    private List<ParticleDevice> mDevice;
    private List<Particle> devices = new LinkedList<>();
    TextView scorelbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        gameRounds = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        question = (TextView) findViewById(R.id.questionLabel);
        // 1. Initialize your connection to the Particle API
        ParticleCloudSDK.init(this.getApplicationContext());

        // 2. Setup your device variable
        getDeviceFromCloud();
        setWord();
        scorelbl = (TextView) findViewById(R.id.scoreLabel);


        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                subscriptionId = ParticleCloudSDK.getCloud().subscribeToAllEvents(
                        "answer",  // the first argument, "eventNamePrefix", is optional
                        new ParticleEventHandler() {
                            public void onEvent(String eventName, ParticleEvent event) {
                                dataFromParticle = "" + event.dataPayload;
                                particleId = "" + event.deviceId;
                                Log.i("12345", "Received event with payload: " + dataFromParticle + "Device Id = " + particleId);
                                for (int i = 0; i<devices.size();i++){
                                    if(devices.get(i).getParticle().getID().equals(particleId) && devices.get(i).isHasVoted() == false){
                                        devices.get(i).setHasVoted(true);
                                        devices.get(i).setVote(dataFromParticle);
                                    }
                                }
                                if(userStatus() == true && gameRounds <= 5){
                                    checkAnswer();
                                }

                                if(gameRounds > 5){
                                    Log.d("Game", "Game Over");
                                    scorelbl.setText("Game Over");
                                    question.setText("No More Questions");
                                }
                            }
                            public void onEventError(Exception e) {
                                Log.e(TAG, "Event error: ", e);
                            }
                        });
                return -1;
            }

            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "Successfully subscribed device to Cloud");
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                subscriptionId = ParticleCloudSDK.getCloud().subscribeToAllEvents(
                        "score",  // the first argument, "eventNamePrefix", is optional
                        new ParticleEventHandler() {
                            public void onEvent(String eventName, ParticleEvent event) {
                                if(event.dataPayload.equals("Show") || gameRounds > 5) {
                                    scorelbl.setText("");
                                    for (int i = 0; i < devices.size(); i++) {
                                        scorelbl.setText(scorelbl.getText() + "Player =" + devices.get(i).getParticle().getName() + " - Score = " + devices.get(i).getScore() + "\n");
                                    }
                                }
                            }
                            public void onEventError(Exception e) {
                                Log.e(TAG, "Event error: ", e);
                            }
                        });
                return -1;
            }

            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "Successfully subscribed device to Cloud");
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });
    }

    public boolean userStatus(){
        int uiCnt = 0;
        for(int i = 0; i < devices.size(); i++){
            if(devices.get(i).isHasVoted() ==true){
                uiCnt++;
            }
        }
        if(uiCnt == devices.size() && gameRounds <= 5){
            gameRounds++;
            Log.d("RRRR", ""+gameRounds);
        }
        return (uiCnt == devices.size());
    }


    public void setWord(){
        if(gameRounds <= 5) {
            Log.d("GameRound",""+gameRounds);
            Random r1 = new Random();
            g_flag = r1.nextInt(2);
            g_index = r1.nextInt(6);
            if (g_flag == 0) {
                question.setText(fruit[g_index]);
                displayWord = fruit[g_index];
            } else {
                question.setText(animal[g_index]);
                displayWord = animal[g_index];
            }
            Log.d("Word", displayWord);
        }
    }

    public void restart(View view){
        gameRounds = 0;
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                if(gameRounds >= 6){
                    question.setText("");
                } else {
                    setWord();
                    scorelbl.setText("");
                }
            }
        }));
        for(int i = 0; i<devices.size(); i++) {
            devices.get(i).setVote("0");
            devices.get(i).setHasVoted(false);
            devices.get(i).setScore(0);
        }
    }

    public void findWinner(View view){
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                TextView winner = (TextView)findViewById(R.id.lblWinner);
                if(gameRounds>=6) {
                    int maxSc = 0;
                    String win = "";
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getScore() > maxSc) {
                            maxSc = devices.get(i).getScore();
                        }
                    }

                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getScore() == maxSc) {
                            win = win + " | " + devices.get(i).getParticle().getName();
                            winner.setText("Winner: " + win);
                        }
                    }
                } else {
                    winner.setText("Game is in Progress...");
                }
            }
        }));

    }
    public void checkExistance(){
        if(Arrays.stream(fruit).anyMatch(displayWord::equals)){
            wordIn = 1;
        }
        if(Arrays.stream(animal).anyMatch(displayWord::equals)){
            wordIn = 2;
        }
    }
    public void checkAnswer(){
        String cmdts = "";
        checkExistance();
        Log.d("DataIn", "Display Word = " + displayWord);
        Log.d("DataIn", "Word In = " + wordIn);
        scorelbl.setText("");
        for (int i = 0; i < devices.size(); i++){
            int ans = Integer.parseInt(devices.get(i).getVote());
            if(ans == wordIn){
                devices.get(i).setScore(devices.get(i).getScore() + 1);
                Log.d("DataIn", devices.get(i).getParticle().getID() + "You got it right...");
                cmdts = "0,255,0";
            }
            else{
                Log.d("DataIn", devices.get(i).getParticle().getID() +"You got it wrong...");
                cmdts = "255,0,0";
            }

            devices.get(i).setVote("0");
            devices.get(i).setHasVoted(false);
            runOnUiThread(new Thread(new Runnable() {
                @Override
                public void run() {
                    if(gameRounds > 5){
                        scorelbl.setText("No More Questions");
                    } else {
                        setWord();
                    }
                }
            }));

            changeColorsPressed(cmdts, i);
        }



    }

    public void getDeviceFromCloud() {
        // This function runs in the background
        // It tries to connect to the Particle Cloud and get your device
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                particleCloud.logIn(PARTICLE_USERNAME, PARTICLE_PASSWORD);
                //mDevice = particleCloud.getDevice(DEVICE_ID);
                mDevice = particleCloud.getDevices();
                for (int i = 0; i<mDevice.size();i++){
                    devices.add(new Particle(mDevice.get(i)));
                    //Log.d("jenelle",mDevice.get(i).getID());
                }
                for (int i = 0; i<devices.size();i++){
                    Log.d("id particle", "Hello World!!! " + devices.get(i).getParticle().getID());
                }
                return -1;
            }
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "Successfully got device from Cloud");
            }
            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });
    }

    public void changeColorsPressed(String cmd, int d) {
        // logic goes here
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                // 2. build a list and put the r,g,b into the list
                List<String> functionParameters = new ArrayList<String>();
                functionParameters.add(cmd);
                try {
                    devices.get(d).getParticle().callFunction("colors", functionParameters);
                    //mDevice.callFunction("colors", functionParameters);
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                }
                return -1;
            }
            @Override
            public void onSuccess(Object o) {

                Log.d(TAG, "Sent colors command to device.");
            }
            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.d(TAG, exception.getBestMessage());
            }
        });
    }
}
