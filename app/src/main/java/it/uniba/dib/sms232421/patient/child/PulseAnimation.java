package it.uniba.dib.sms232421.patient.child;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;


public class PulseAnimation extends ScaleAnimation {

    public PulseAnimation(View target) {
        super(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        setDuration(1000);
        setRepeatCount(Animation.INFINITE);
        setRepeatMode(Animation.REVERSE);
        target.startAnimation(this);
    }
}

