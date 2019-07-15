package com.example.androidparticlestarter;

import io.particle.android.sdk.cloud.ParticleDevice;

public class Particle {
    ParticleDevice particle;
    boolean hasVoted;
    String vote;
    int score;

    public Particle(ParticleDevice device){
        this.particle = device;
        hasVoted = false;
        vote = "";
        score = 0;
    }

    public ParticleDevice getParticle() {
        return particle;
    }

    public void setParticle(ParticleDevice device) {
        this.particle = device;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public String getVote() {
        return vote;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}