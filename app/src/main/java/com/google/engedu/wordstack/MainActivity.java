package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

//TODO: Check whether the user found a valid solution besides the one picked.
//TODO: Allow dragging to undo and dragging tiles between word1 and word2.
//TODO: Come up with a different scrambling algorithm that avoids long runs of letters from the same word.

public class MainActivity extends AppCompatActivity {

    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private static boolean DEBUG_FLAG = false;
    private static int WORD_LENGTH = 3;
    private static Stack<LetterTile> placedTiles = new Stack<>();
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createWords();
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }
    private void createWords() {
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() == WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    protected boolean onStartGame(View view) {
        //Cleanup
        View word1LinearLayout = findViewById(R.id.word1);
        View word2LinearLayout = findViewById(R.id.word2);
        Button undoButton = (Button) findViewById(R.id.button);
        ((LinearLayout) word1LinearLayout).removeAllViews();
        ((LinearLayout) word2LinearLayout).removeAllViews();
        undoButton.setEnabled(true);
        stackedLayout.clear();
        placedTiles.clear();

        //Setup the game.
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        int randomInt0 = random.nextInt(words.size() + 1);
        int randomInt1 = random.nextInt(words.size() + 1);
        boolean randomBool = random.nextBoolean();
        int word1Counter = 0;
        int word2Counter = 0;
        word1 = words.get(randomInt0);
        word2 = words.get(randomInt1);
        String scrambled = "";

        while(word1Counter < WORD_LENGTH  && word2Counter < WORD_LENGTH ){
            if (randomBool) {
                    scrambled += word1.charAt(word1Counter);
                    word1Counter++;
            }
            else {
                scrambled += word2.charAt(word2Counter);
                word2Counter++;
            }
            randomBool = random.nextBoolean();
        }
        if(word1Counter == WORD_LENGTH){
            scrambled += word2.substring(word2Counter);
        }else scrambled += word1.substring(word1Counter);

        for (int i = scrambled.length() - 1; i >= 0; i--) {
            LetterTile tile = new LetterTile(view.getContext(), scrambled.charAt(i));
            stackedLayout.push(tile);
        }
        WORD_LENGTH++;
        words.clear();
        createWords();
        if (DEBUG_FLAG) {
            String answers = word1 + " " + word2;
            messageBox.setText(answers);
        }
        return true;
    }

    protected boolean onUndo(View view) {
        if (!placedTiles.empty()) {
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup(stackedLayout);
        }
        return true;
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText("Answers: " + word1 + " " + word2);
                        Button undoButton = (Button) findViewById(R.id.button);
                        undoButton.setEnabled(false);
                    }
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }
}
