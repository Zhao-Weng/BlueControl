package com.anapp.bluecontrol;

/**
 * Created by apple on 3/7/17.
 */

public class handleThread extends Thread {
    private char[] chars = {};
    handleThread(){
    }
    handleThread(char[]chars){
        this.chars = chars;
    }

    public void run(){
        String s = new String(this.chars);
        System.out.println("The Data from Arduino is: " + s);

    }

}
