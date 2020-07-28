package uz.mirafzal.hafizovbooks.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import uz.mirafzal.hafizovbooks.R;

public class BookReaderActivity extends AppCompatActivity {

    private PDFView pdfView;
    private final String KEYWORD_NIGHT_MODE = "uz.mirafzal.hafizovbooks.nigtmode";
    private int bookId;
    private boolean isFinished = false;
    private boolean isLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reader);
        pdfView = findViewById(R.id.pdfView);
        setCurrentMode();
        Intent intent = getIntent();
        String bookPath = intent.getStringExtra("path");
        bookId = intent.getIntExtra("id", -1);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String encodedBookContent = sharedPref.getString(bookId + "", null);
        getPdfBook(bookPath);
        if (encodedBookContent != null) {
            byte[] bookContent = Base64.decode(encodedBookContent, Base64.NO_WRAP);
            loadPdfBook(bookContent);
        } else {
            getPdfBook(bookPath);
        }
    }

    private void getPdfBook(final String bookName) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference booksRef = storageRef.child(bookName);

        booksRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                float fileSize = (float) storageMetadata.getSizeBytes();
            }
        });
        final long ONE_MEGABYTE = 1024 * 1024;
        booksRef.getBytes(30 * ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(bookId + "", encoded);
                        editor.apply();
                        bytes = Base64.decode(encoded, Base64.NO_WRAP);
                        loadPdfBook(bytes);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Firebase storage onFailure! " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        List<FileDownloadTask> tasks = booksRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = tasks.get(0);

            task.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                }
            });
        }
    }

    private void loadPdfBook(byte[] bookBytes) {
        final OnLoadCompleteListener onLoadCompleteListener = new OnLoadCompleteListener() {
            @Override
            public void loadComplete(int nbPages) {
                if (isFinished) {
                    finish();
                } else {
                    isLoaded = true;
                }
                Toast.makeText(getApplicationContext(), "loaded!", Toast.LENGTH_SHORT).show();
            }
        };
        final OnErrorListener onErrorListener = new OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "onError!", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        };
        final OnPageErrorListener onPageErrorListener = new OnPageErrorListener() {
            @Override
            public void onPageError(int page, Throwable t) {
                Toast.makeText(getApplicationContext(), "onPageError!", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        };
        final OnRenderListener onRenderListener = new OnRenderListener() {
            @Override
            public void onInitiallyRendered(int nbPages) {
                Log.d("mTag", "rendered: " + nbPages);
            }
        };
        pdfView.setVisibility(View.VISIBLE);
        pdfView.fromBytes(bookBytes)
                .onLoad(onLoadCompleteListener)
                .onError(onErrorListener)
                .onPageError(onPageErrorListener)
                .onRender(onRenderListener)
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .scrollHandle(new DefaultScrollHandle(getApplicationContext()))
                .nightMode(getPreferences(Context.MODE_PRIVATE).getBoolean(KEYWORD_NIGHT_MODE, false))
                .load();


//        final InputStream[] input = new InputStream[1];
//
//        final String url = "https://file-examples-com.github.io/uploads/2017/10/file-example_PDF_1MB.pdf";
//        new AsyncTask<Void, Void, Void>() {
//            @SuppressLint({"WrongThread", "StaticFieldLeak"})
//            @Override
//            protected Void doInBackground(Void... voids) {
//                try {
//                    input[0] = new URL(url).openStream();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                pdfView.fromStream(input[0])
//                        .onLoad(onLoadCompleteListener)
//                        .onError(onErrorListener)
//                        .onPageError(onPageErrorListener)
////                        .onRender(onRenderListener)
//                        .swipeHorizontal(true)
//                        .pageSnap(true)
//                        .autoSpacing(true)
//                        .pageFling(true)
//                        .scrollHandle(new DefaultScrollHandle(getApplicationContext()))
//                        .nightMode(getPreferences(Context.MODE_PRIVATE).getBoolean(KEYWORD_NIGHT_MODE, false))
//                        .load();
//            }
//        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_day_night:
                switchNightMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setCurrentMode() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (!sharedPref.getBoolean(KEYWORD_NIGHT_MODE, false)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            }
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            pdfView.setNightMode(false);
            editor.putBoolean(KEYWORD_NIGHT_MODE, false);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorToolbarNight)));
            }
            getWindow().setStatusBarColor(Color.BLACK);
            pdfView.setNightMode(true);
            editor.putBoolean(KEYWORD_NIGHT_MODE, true);
        }
        pdfView.jumpTo(pdfView.getCurrentPage());
        editor.apply();
    }

    private void switchNightMode() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (sharedPref.getBoolean(KEYWORD_NIGHT_MODE, false)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            }
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            pdfView.setNightMode(false);
            editor.putBoolean(KEYWORD_NIGHT_MODE, false);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorToolbarNight)));
            }
            getWindow().setStatusBarColor(Color.BLACK);
            pdfView.setNightMode(true);
            editor.putBoolean(KEYWORD_NIGHT_MODE, true);
        }
        pdfView.jumpTo(pdfView.getCurrentPage());
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (isLoaded) {
            super.onBackPressed();
        } else {
            isFinished = true;
        }
    }
}