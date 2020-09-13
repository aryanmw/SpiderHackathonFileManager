package com.wadhavekar.fileexplorerandmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.ACTIVITY_SERVICE;

public class MainActivity extends AppCompatActivity {

    ListView lv;
    SearchView searchView;
    TextAdapter adapter;
    RelativeLayout button_layout, withCopyLayout;
    ImageView delete, createFolder, backToParent, copy,rename,move,share;

    private File[] files;

    private File dir;
    int filesFoundCount;
    List<String> filesList;
    boolean[] selection;
    private String currentPath;
    TextView paste,moveHere,copyCancel,moveCancel,title;
    RelativeLayout pasteLayout,moveLayout;
    ArrayList<Integer> selectedItems;
    private int SELECT_PICTURE = 1;
    private String imagePath;
    TextView sharetv,renameTv;

    private ArrayList<String> multipleCopyPaths = new ArrayList<>();

    private static final int REQUEST_CODE = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_COUNT = 2;

    private boolean isFileManagerInitialized;
    private int selectedItemIndex;
    private String copyPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.listView1);
        button_layout = findViewById(R.id.ll_buttons);
    }

    @Override
    public void onBackPressed() {

        String rootPath = String.valueOf(Environment.getExternalStorageDirectory());

        if (!currentPath.equals(rootPath)) {
            currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
            dir = new File(currentPath);
            changeAdapterOnUpdate();

            if (currentPath.equals(rootPath)){
                title.setText("Internal Storage");
            }
            else{
                title.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
            }

//            for (int j = 0; j < selection.length; j++) {
//                selection[j] = false;
//            }
            button_layout.setVisibility(View.INVISIBLE);

            //showPath = showPath.substring(0, showPath.lastIndexOf('/'));

        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView = findViewById(R.id.search_view);
        sharetv = findViewById(R.id.tv_share);
        renameTv= findViewById(R.id.tv_rename);
        share = findViewById(R.id.button_share);
        paste = findViewById(R.id.button_paste);
        title = findViewById(R.id.title);
        move = findViewById(R.id.move_button);
        delete = findViewById(R.id.button_delete);
        copy = findViewById(R.id.button_copy);
        rename = findViewById(R.id.button_rename);
        createFolder = findViewById(R.id.createFolder);
        backToParent = findViewById(R.id.backToParent);
        pasteLayout = findViewById(R.id.layout_paste);
        moveLayout = findViewById(R.id.layout_move);
        moveHere = findViewById(R.id.button_move_here);
        moveCancel = findViewById(R.id.move_cancel);
        copyCancel = findViewById(R.id.copy_cancel);
        if (arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_CODE);
            return;
        }
        if (!isFileManagerInitialized) {
            final String rootPath = String.valueOf(Environment.getExternalStorageDirectory());
            currentPath = rootPath;
            dir = new File(rootPath);
            files = dir.listFiles();
            isFileManagerInitialized = true;
            filesFoundCount = files.length;

            filesList = new ArrayList<>();


            selection = new boolean[files.length];



            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//                    if (currentPath.equals("storage/emulated/0")){
//                        title.setText("Internal Storage");
//                        showPath = "Internal Storage";
//                    }
//                    else{
//                        showPath += currentPath.substring(currentPath.lastIndexOf('/')+1);
//                        title.setText(showPath);
//                    }


                    if (anyItemSelected(selection)) {

                        selection[i] = !selection[i];
                        adapter.setSelection(selection);
                        if (!anyItemSelected(selection)) {
                            button_layout.setVisibility(View.INVISIBLE);
                        }

                        if (!oneItemSelected(selection)) {
                            rename.setVisibility(View.INVISIBLE);
                            renameTv.setVisibility(View.INVISIBLE);

                            share.setVisibility(View.INVISIBLE);
                            sharetv.setVisibility(View.INVISIBLE);
                        } else {
                            rename.setVisibility(View.VISIBLE);
                            renameTv.setVisibility(View.VISIBLE);

                            share.setVisibility(View.VISIBLE);
                            sharetv.setVisibility(View.VISIBLE);
                            //selectedItemIndex = i;
                        }
                    } else {
                        button_layout.setVisibility(View.INVISIBLE);
                        if (files[i].isDirectory()) {
                            currentPath = files[i].getAbsolutePath();
                            dir = new File(currentPath);
                            changeAdapterOnUpdate();
                        } else {
                            imagePath = files[i].getAbsolutePath();
                            Intent intent = new Intent(MainActivity.this,ImageViewer.class);
                            intent.putExtra("image",imagePath);
                            startActivity(intent);
                            //Toast.makeText(MainActivity.this, "" + imagePath, Toast.LENGTH_SHORT).show();





                        }
                    }

                    if (new File(currentPath).isDirectory()) {
                        if (currentPath.equals(rootPath)){
                            title.setText("Internal Storage");
                        }
                        else{
                            title.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
                        }

                    }

                }
            });


            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    selection[i] = !selection[i];
                    selectedItemIndex = i;

                    if (files[i].isDirectory()){
                        share.setVisibility(View.INVISIBLE);
                        sharetv.setVisibility(View.INVISIBLE);
                        rename.setVisibility(View.VISIBLE);
                        renameTv.setVisibility(View.VISIBLE);
                    }
                    else{
                        share.setVisibility(View.VISIBLE);
                        sharetv.setVisibility(View.VISIBLE);
                        rename.setVisibility(View.VISIBLE);
                        renameTv.setVisibility(View.VISIBLE);


                    }

                    adapter.setSelection(selection);
                    if (oneItemSelected(selection)) {
                        button_layout.setVisibility(View.VISIBLE);
                        imagePath = files[i].getAbsolutePath();
                        move.setVisibility(View.VISIBLE);
                    } else {
                        button_layout.setVisibility(View.INVISIBLE);
                    }


                    return true;
                }
            });


            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //changeAdapterOnUpdate();
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("Delete");
                    deleteDialog.setMessage("Do you really want to delete it?");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //changeAdapterOnUpdate();
                            for (int j = 0; j < files.length; j++) {
                                if (selection[j]) {
                                    //Toast.makeText(MainActivity.this, ""+files[j], Toast.LENGTH_SHORT).show();
                                    deleteFileOrFolder(files[j]);
                                    //Toast.makeText(MainActivity.this, "Method shud be called "+files[j], Toast.LENGTH_SHORT).show();
//                                    selection[j] = false;
                                }
                            }
                            changeAdapterOnUpdate();

                        }
                    });

                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            button_layout.setVisibility(View.INVISIBLE);
                        }
                    });
                    deleteDialog.show();

                }
            });

            createFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder folderDialog = new AlertDialog.Builder(MainActivity.this);
                    folderDialog.setTitle("New Folder");
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    folderDialog.setView(input);
                    folderDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final File newFolder = new File(currentPath + "/" + input.getText());
                            if (!newFolder.exists()) {
                                newFolder.mkdir();
                                changeAdapterOnUpdate();
                            }
                        }
                    });

                    folderDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            button_layout.setVisibility(View.INVISIBLE);
                        }
                    });
                    folderDialog.show();
                }
            });

            backToParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!currentPath.equals(rootPath)) {
                        currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
                        dir = new File(currentPath);
                        changeAdapterOnUpdate();

                        if (currentPath.equals(rootPath)){
                            title.setText("Internal Storage");
                        }
                        else{
                            title.setText(currentPath.substring(currentPath.lastIndexOf('/')+1));
                        }

//                        for (int j = 0; j < selection.length; j++) {
//                            selection[j] = false;
//                        }
                        button_layout.setVisibility(View.INVISIBLE);

                        //showPath = showPath.substring(0, showPath.lastIndexOf('/'));

                    }

                }
            });

            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(MainActivity.this);
                    renameDialog.setTitle("Rename To:");
                    final EditText input = new EditText(MainActivity.this);
                    final String path = files[selectedItemIndex].getAbsolutePath();
                    input.setText(path.substring(path.lastIndexOf('/') + 1));
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    renameDialog.setView(input);
                    renameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!input.getText().toString().equals(path)) {
                                String s = new File(path).getParent() + '/' + input.getText();
                                File newFile = new File(s);
                                new File(path).renameTo(newFile);
                                changeAdapterOnUpdate();
                                button_layout.setVisibility(View.INVISIBLE);
                            } else {
                                Toast.makeText(MainActivity.this, "Please change the name!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            button_layout.setVisibility(View.INVISIBLE);
                        }
                    });
                    renameDialog.show();
                }
            });

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    button_layout.setVisibility(View.INVISIBLE);
                    for (int j = 0; j < selection.length; j++) {
                        selection[j] = false;
                    }
                    changeAdapterOnUpdate();
                    pasteLayout.setVisibility(View.VISIBLE);
                    multipleCopyPaths = new ArrayList<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        multipleCopyPaths.add(files[selectedItems.get(i)].getAbsolutePath());
                    }
                    //copyPath = files[selectedItemIndex].getAbsolutePath();
                }
            });

            paste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    for (int i = 0; i < multipleCopyPaths.size(); i++) {
                        String dstPath = currentPath + '/' + multipleCopyPaths.get(i).substring(multipleCopyPaths.get(i).lastIndexOf('/'));
                        copyFile(new File(multipleCopyPaths.get(i)), new File(dstPath));
                    }
//                    String dstPath = currentPath + '/' + copyPath.substring(copyPath.lastIndexOf('/'));
//                    copyFile(new File(copyPath), new File(dstPath));
                    pasteLayout.setVisibility(View.INVISIBLE);
                    changeAdapterOnUpdate();
                }
            });
            copyCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < selection.length; j++) {
                        selection[j] = false;
                    }

                    changeAdapterOnUpdate();
                    pasteLayout.setVisibility(View.INVISIBLE);
                }
            });

            move.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        multipleCopyPaths = new ArrayList<>();
                        for (int i = 0; i < selectedItems.size(); i++) {
                            multipleCopyPaths.add(files[selectedItems.get(i)].getAbsolutePath());
                        }

                        moveLayout.setVisibility(View.VISIBLE);
                        button_layout.setVisibility(View.INVISIBLE);
                    for (int j = 0; j < selection.length; j++) {
                        selection[j] = false;
                    }



                }
            });

            moveHere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < multipleCopyPaths.size(); i++) {
                        String dstPath = currentPath + '/' + multipleCopyPaths.get(i).substring(multipleCopyPaths.get(i).lastIndexOf('/'));
                        File file = new File(multipleCopyPaths.get(i));
                        file.renameTo(new File(dstPath));
                    }
                    move.setVisibility(View.INVISIBLE);
                    for (int j = 0; j < selection.length; j++) {
                        selection[j] = false;
                    }

                    changeAdapterOnUpdate();
                    moveLayout.setVisibility(View.INVISIBLE);
                }
            });

            moveCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < selection.length; j++) {
                        selection[j] = false;
                    }

                    changeAdapterOnUpdate();
                    moveLayout.setVisibility(View.INVISIBLE);
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri path = null;
                    try {
                        path = FileProvider.getUriForFile(MainActivity.this,"com.wadhavekar.FM",new File(imagePath));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM,path);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent,"Share.."));

                    Toast.makeText(MainActivity.this, ""+imagePath, Toast.LENGTH_SHORT).show();

                    button_layout.setVisibility(View.INVISIBLE);
                    selection = new boolean[files.length];
                    changeAdapterOnUpdate();
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                   adapter.filter(s);
                   return true;
                }
            });


            for (int i = 0; i < filesFoundCount; i++) {
                filesList.add(String.valueOf(files[i]));
            }

            adapter = new TextAdapter(filesList);
            lv.setAdapter(adapter);
        } else {
            changeAdapterOnUpdate();
        }


    }


    private boolean arePermissionsDenied() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int p = 0;
            while (p < PERMISSIONS_COUNT) {
                if (checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                p++;
            }
        }
        return false;
    }

    private void copyFile(File src, File destination) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                imagePath = getPath(selectedImageUri);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }




    private void changeAdapterOnUpdate(){
        files = dir.listFiles();
        if (files.length > 0){
            selection = new boolean[files.length];
        }

        filesList.clear();
        filesFoundCount = files.length;
        for (int z = 0 ; z < filesFoundCount ; z++){
            filesList.add(String.valueOf(files[z]));
        }
        adapter = new TextAdapter(filesList);
        lv.setAdapter(adapter);
    }

    private boolean anyItemSelected(boolean[] selection){
        for (int i = 0 ; i < selection.length ; i++){
            if (selection[i]){
                return true;
            }
        }
        return false;

    }

    private boolean oneItemSelected(boolean[] selection){
        int count = 0;
        selectedItems = new ArrayList<>();

        for (int i = 0 ; i < selection.length ; i++){
            if (selection[i]){
                count++;
                selectedItems.add(i);
                selectedItemIndex = i;
            }
        }
        if (count == 1){
            return true;
        }
        return false;
    }

    private void deleteFileOrFolder(File filerOrFolder){
        //Toast.makeText(this, "Delete method called", Toast.LENGTH_SHORT).show();
        if (filerOrFolder.isDirectory()){
            if (filerOrFolder.list().length == 0){
                filerOrFolder.delete();
                changeAdapterOnUpdate();
            }
            else{
                String file[] = filerOrFolder.list();
                for(String temp: file){
                    File fileToDelete = new File(filerOrFolder,temp);
                    deleteFileOrFolder(fileToDelete);

                }
                if (filerOrFolder.list().length == 0){
                    filerOrFolder.delete();
                    changeAdapterOnUpdate();
                }
                changeAdapterOnUpdate();
            }
        }
        else{
            if (filerOrFolder.delete()){
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            }
            changeAdapterOnUpdate();
        }

        button_layout.setVisibility(View.INVISIBLE);
        move.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE && grantResults.length > 0){
            if (arePermissionsDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }
            else{
                onResume();
            }
        }
    }


}

class TextAdapter extends BaseAdapter{

    private List<String> data;
    private ArrayList<String> arrayList;
    private boolean[] selection;

    void setSelection(boolean[] selection){
        if (selection != null){
            this.selection = new boolean[selection.length];
            for (int i = 0 ; i < selection.length ; i++){
                this.selection[i] = selection[i];
            }
            notifyDataSetChanged();
        }
    }

    public TextAdapter(List<String> data) {
        this.data = data;
        arrayList = new ArrayList<>();
        arrayList.addAll(data);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }



    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout,viewGroup,false);
            view.setTag(new ViewHolder((TextView) view.findViewById(R.id.tv_row),(ImageView) view.findViewById(R.id.iv_icon),(RelativeLayout) view.findViewById(R.id.cv_rr)));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        String item = getItem(i);
        File file = new File(item);
        if (file.isDirectory()){
            holder.icon.setImageResource(R.drawable.ic_folder_black_24dp);
        }
        else if (item.substring(item.lastIndexOf('.')+1).equals("pdf")){
            holder.icon.setImageResource(R.drawable.ic_picture_as_pdf_black_24dp);
        }
        else if (item.substring(item.lastIndexOf('.')+1).equals("mp3")){
            holder.icon.setImageResource(R.drawable.ic_music_note_black_24dp);
        }
        else if (item.substring(item.lastIndexOf('.')+1).equals("jpg") || item.substring(item.lastIndexOf('.')+1).equals("jpeg")){
            holder.icon.setImageResource(R.drawable.ic_image_black_24dp);
        }
        else if (item.substring(item.lastIndexOf('.')+1).equals("mkv") || item.substring(item.lastIndexOf('.')+1).equals("mp4")){
            holder.icon.setImageResource(R.drawable.ic_video_library_black_24dp);
        }
        else{
            holder.icon.setImageResource(R.drawable.ic_insert_drive_file_black_24dp);
        }

        if (item.substring(item.lastIndexOf('/')+1).length() > 20){
            if (!(new File(item).isDirectory())){
                String text = item.substring(item.lastIndexOf('/')+1).substring(0,15) + "..." + item.substring(item.lastIndexOf('.'));
                holder.info.setText(text);
            }
            else{
                String text = item.substring(item.lastIndexOf('/')+1).substring(0,15) + "...";
                holder.info.setText(text);
            }

        }
        else{
            holder.info.setText(item.substring(item.lastIndexOf('/')+1));
        }



        if (selection != null){
            if (selection[i]){
                holder.cv.setBackgroundColor(Color.parseColor("#36373B"));

            }
            else{
                holder.cv.setBackgroundColor(Color.parseColor("#202125"));
            }
        }
        return view;
    }

    public void filter(String charText){
        charText.toLowerCase(Locale.getDefault());
        data.clear();
        if (charText.length() == 0){
            data.addAll(arrayList);
        }
        else{
            for (String string: arrayList){
                if (string.toLowerCase(Locale.getDefault()).contains(charText)){
                    data.add(string);
                }
            }
        }
        notifyDataSetChanged();
    }




    class ViewHolder{
        TextView info;
        ImageView icon;
        RelativeLayout cv;

        public ViewHolder(TextView info,ImageView icon, RelativeLayout cv) {
            this.info = info;
            this.icon = icon;
            this.cv = cv;
        }
    }
}

