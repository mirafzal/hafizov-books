package uz.mirafzal.hafizovbooks.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Locale;

import uz.mirafzal.hafizovbooks.R;
import uz.mirafzal.hafizovbooks.activities.BookInfoActivity;
import uz.mirafzal.hafizovbooks.activities.BookReaderActivity;
import uz.mirafzal.hafizovbooks.activities.MainActivity;
import uz.mirafzal.hafizovbooks.models.Book;

public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<Book> books;
    private int listItemLayout;

    public ListViewAdapter(LayoutInflater layoutInflater, ArrayList<Book> books, int listItemLayout) {
        this.layoutInflater = layoutInflater;
        this.books = books;
        this.listItemLayout = listItemLayout;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(listItemLayout, parent, false);

        final Book book = getBook(position);

//        ((LinearLayout) view.findViewById(R.id.linearLayoutItem)).setBackgroundColor(Color.);
        ((ImageView) view.findViewById(R.id.bookImage)).setImageResource(book.photoId);
        ((TextView) view.findViewById(R.id.tvBookName)).setText(book.name);
        ((TextView) view.findViewById(R.id.tvBookAuthor)).setText(book.author);
        final ContentLoadingProgressBar progressBar = view.findViewById(R.id.downloadProgressBar);
        final TextView progressBarText = view.findViewById(R.id.downloadProgressBarText);
        switch (listItemLayout) {
            case R.layout.list_item_downloaded:
                ((Button) view.findViewById(R.id.btnRead)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), BookReaderActivity.class);
                        Log.d("mTag", book.id+"");
                        intent.putExtra("id", book.id);
                        intent.putExtra("path", book.path);
                        view.getContext().startActivity(intent);
                        Toast.makeText(view.getContext(), "Button " + book.name + " clicked!", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.layout.list_item_purchased:
                ((Button) view.findViewById(R.id.btnDownload)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadBook(view, book.path, book.id, progressBar, progressBarText);
                        ((Button)v).setEnabled(false);
                    }
                });
                break;
            case R.layout.list_item_shop:
                ((TextView) view.findViewById(R.id.tvBookPrice)).setText(view.getContext().getString(R.string.soum, book.price));
                ((LinearLayout) view.findViewById(R.id.linearLayoutItem)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Book book = getBook(position);
                        Log.d("mTag", "id: " + book.id);
                        Log.d("mTag", "path: " + book.path);
                        Intent intent = new Intent(view.getContext(), BookInfoActivity.class);
                        intent.putExtra("id", book.id);
                        intent.putExtra("path", book.path);
                        view.getContext().startActivity(intent);
                        Toast.makeText(view.getContext(), "Button " + book.name + " clicked!", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        return view;
    }

    Book getBook(int position) {
        return ((Book) getItem(position));
    }

    private void downloadBook(final View view, String bookPath, final int bookId, final ContentLoadingProgressBar progressBar, final TextView progressBarText) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final StorageReference booksRef = storageRef.child(bookPath);
        final File localFile;
        try {
            localFile = File.createTempFile("book", ".pdf");
            booksRef.getFile(localFile)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                            progressBar.setVisibility(View.GONE);
                            progressBarText.setVisibility(View.GONE);
                            try {
                                byte[] tempBook = FileUtils.readFileToByteArray(localFile);
                                Log.d("mTag", localFile.delete()+"");
                                SharedPreferences sharedPref = view.getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                                String encoded = Base64.encodeToString(tempBook, Base64.NO_WRAP);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                Log.d("mTag", bookId+"");
                                editor.putString(bookId + "", encoded);
                                editor.apply();
                                Toast.makeText(view.getContext(), R.string.book_has_been_downloaded_successfully, Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            progressBarText.setVisibility(View.GONE);
                            Toast.makeText(view.getContext(), R.string.book_has_not_been_downloaded, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                            if (taskSnapshot.getTotalByteCount() != -1) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressBar.setProgress((int) progress);
                                progressBar.setVisibility(View.VISIBLE);
                                String downloadedMegabytes = String.format(Locale.CANADA, "%.2f", ((float) taskSnapshot.getBytesTransferred()) / 1024 / 1024);
                                String totalMegabytes = String.format(Locale.CANADA, "%.2f", ((float) taskSnapshot.getTotalByteCount()) / 1024 / 1024);
                                progressBarText.setText(String.format("%s / %s mb", downloadedMegabytes, totalMegabytes));
                                progressBarText.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //                booksRef.getBytes(fileSize + 1024 * 1024)
//                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                            @Override
//                            public void onSuccess(byte[] bytes) {
//                SharedPreferences sharedPref = view.getContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
//                                String encoded = Base64.encodeToString(bytes, Base64.NO_WRAP);
//                                SharedPreferences.Editor editor = sharedPref.edit();
//                                editor.putString(bookId + "", encoded);
//                                editor.apply();
//                                Toast.makeText(view.getContext(), "Kitob muvaffaqiyatli yuklandi!", Toast.LENGTH_SHORT).show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        Toast.makeText(view.getContext(), "Firebase storage onFailure! " + exception.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                List<FileDownloadTask> tasks = booksRef.getActiveDownloadTasks();
//                Toast.makeText(view.getContext(), "Tasks count: " + tasks.size(), Toast.LENGTH_SHORT).show();
//
//                if (tasks.size() > 0) {
//                    // Get the task monitoring the download
//                    FileDownloadTask task = tasks.get(0);
//                    task.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                            ((ProgressBar) view.findViewById(R.id.downloadProgressBar)).setProgress((int)progress);
//                        }
//                    });
//                }
    }
}
