package click.dummer.yidkey;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.util.List;

import click.dummer.yidkey.uglykeyb.R;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    @Override
    public View onCreateInputView() {
        // get the KeyboardView and add our Keyboard layout to it
        KeyboardView keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        Keyboard keyboard = new Keyboard(this, R.xml.pad);
        List<Keyboard.Key> keys = keyboard.getKeys();

        for (Keyboard.Key key : keys) {
            if (key.codes[0] == -5) {
                ;
            }
        }
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection inputConnection = getCurrentInputConnection();

        if (inputConnection != null) {
            switch(primaryCode) {
                case Keyboard.KEYCODE_DELETE :
                    CharSequence selectedText = inputConnection.getSelectedText(0);

                    if (TextUtils.isEmpty(selectedText)) {
                        inputConnection.deleteSurroundingText(1, 0);
                    } else {
                        inputConnection.commitText("", 1);
                    }

                    break;
                case -10:
                    inputConnection.commitText("שׂ", 1);
                    break;
                case -11:
                    inputConnection.commitText("יִ", 1);
                    break;
                case -12:
                    inputConnection.commitText("אַ", 1);
                    break;
                case -13:
                    inputConnection.commitText("אָ", 1);
                    break;
                case -15:
                    inputConnection.commitText("כּ", 1);
                    break;
                case -18:
                    inputConnection.commitText("תּ", 1);
                    break;
                case -14:
                    inputConnection.commitText("בֿ", 1);
                    break;
                case -16:
                    inputConnection.commitText("פּ", 1);
                    break;
                case -17:
                    inputConnection.commitText("פֿ", 1);
                    break;
                case -19:
                    inputConnection.commitText("וּ", 1);
                    break;
                case -20:
                    inputConnection.commitText("ױ", 1);
                    break;
                case -21:
                    inputConnection.commitText("ײַ", 1);
                    break;

                case Keyboard.KEYCODE_DONE:
                    inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                default :
                    char code = (char) primaryCode;
                    inputConnection.commitText(String.valueOf(code), 1);
            }
        }
    }

    @Override
    public void onPress(int primaryCode) { }

    @Override
    public void onRelease(int primaryCode) { }

    @Override
    public void onText(CharSequence text) { }

    @Override
    public void swipeLeft() { }

    @Override
    public void swipeRight() { }

    @Override
    public void swipeDown() { }

    @Override
    public void swipeUp() { }

}