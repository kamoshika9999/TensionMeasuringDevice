/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

/**
 *
 * @author Z002VDJE
 */
public class HX711 {

    private final GpioPinDigitalOutput pinCLK;
    private final GpioPinDigitalInput pinDAT;
    private int gain;

    public long emptyValue = 0;
    public double emptyWeight = 0.0d;
    public long calibrationValue = 0;
    public double calibrationWeight = 9.0d;
    public double resolution;

    public double fullCupWeight = 200.0d;

    public double weight = 0.0d;
    public long value = 0;

    public HX711(GpioPinDigitalInput pinDAT, GpioPinDigitalOutput pinSCK, int gain) {
        this.pinCLK = pinSCK;
        this.pinDAT = pinDAT;
        setGain(gain);
    }

    public boolean read() {
        pinCLK.setState(PinState.LOW);
        int timeOutCnt = 0;
        while (!isReady()) {
            sleep(1);
			timeOutCnt++;
            if(timeOutCnt>1000) {
            	System.out.println("HX711 TIMEOUT");
            	return false;//1秒以上pinDTがHeightならばタイムアウト
            }
        }

        long count = 0;
        for (int i = 0; i < this.gain; i++) {
            pinCLK.setState(PinState.HIGH);
            count = count << 1;
            pinCLK.setState(PinState.LOW);
            if (pinDAT.isHigh()) {
                count++;
            }
        }
        pinCLK.setState(PinState.HIGH);
        count = count ^ 0x800000;//最上位Bitをキャンセルしている(なぜか？)
        pinCLK.setState(PinState.LOW);

        if( count < 8700000 ) {
        	System.out.println("count < 870000" + String.valueOf(count));
        	value = -1;
        }else {
        	value = count;
        }

        if( calibrationWeight == 0.0 ) {
        	weight = 0.0;
        }else {
        	weight = (value-emptyValue)/resolution*-1;
        }
        if( value < 0 ) {
        	return false;
        }else {
        	return true;
        }
    }

    public void setGain(int gain) {
        switch (gain) {
            case 128:       // channel A, gain factor 128
                this.gain = 24;
                break;
            case 64:        // channel A, gain factor 64
                this.gain = 26;
                break;
            case 32:        // channel B, gain factor 32
                this.gain = 25;
                break;
        }

        pinCLK.setState(PinState.LOW);
        read();
    }

    public boolean isReady() {
        return (pinDAT.isLow());
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception ex) {
        }
    }
}