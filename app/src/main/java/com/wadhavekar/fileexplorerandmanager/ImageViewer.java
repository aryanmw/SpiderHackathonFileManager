package com.wadhavekar.fileexplorerandmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.barteksc.pdfviewer.PDFView;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ImageViewer extends AppCompatActivity {

    ImageView iv;
    PDFView pdfView;
    VideoView vv;
    TextView tv;
    Button start,stop,pause;
    MediaPlayer player;
    RelativeLayout buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        iv= findViewById(R.id.imageview);
        pdfView = findViewById(R.id.pdfView);
        tv = findViewById(R.id.tv_txt);
        vv = findViewById(R.id.videoView);
        buttons = findViewById(R.id.mediaPlayer_buttons);
        start=findViewById(R.id.button1);
        pause=findViewById(R.id.button2);
        stop=findViewById(R.id.button3);
        String iPath = getIntent().getStringExtra("image");
        //Toast.makeText(this, ""+iPath, Toast.LENGTH_SHORT).show();

        if (iPath.charAt(iPath.length()-1) == 'f'){
            //Toast.makeText(this, "PDF", Toast.LENGTH_SHORT).show();
            buttons.setVisibility(View.INVISIBLE);
            File f = new File(iPath);
            pdfView.setVisibility(View.VISIBLE);
            iv.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.INVISIBLE);
            vv.setVisibility(View.INVISIBLE);
            pdfView.fromFile(f)
                    .load();
        }
        else if (iPath.charAt(iPath.length()-1) == 'g' || iPath.substring(iPath.lastIndexOf('.')+1).equals("jgep")){
            iv.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "Image", Toast.LENGTH_SHORT).show();
            pdfView.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.INVISIBLE);
            vv.setVisibility(View.INVISIBLE);
            buttons.setVisibility(View.INVISIBLE);
            File f = new File(iPath);
            Picasso.Builder builder =new Picasso.Builder(getApplicationContext());
            builder.build().load(f).into(iv);
        }

        else if (iPath.charAt(iPath.length()-2) == 'x'){
            tv.setVisibility(View.VISIBLE);
            pdfView.setVisibility(View.INVISIBLE);
            iv.setVisibility(View.INVISIBLE);
            vv.setVisibility(View.INVISIBLE);
            buttons.setVisibility(View.INVISIBLE);

            tv.setText(readFile(iPath));
        }

        else if (iPath.charAt(iPath.length()-1) == '3'){
            buttons.setVisibility(View.VISIBLE);
            player = new MediaPlayer();

            try {
                player.setDataSource(iPath);
                player.prepare();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                //System.out.println("Exception of type : " + e.toString());
                e.printStackTrace();
            }

            player.start();
        }

        else{

        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stop();
            }
        });


    }

    private String readFile(String path){
        File file =new File(path);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null ){
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();

    }
}
