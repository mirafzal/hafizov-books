package uz.mirafzal.hafizovbooks.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import uz.mirafzal.hafizovbooks.enums.Category;
import uz.mirafzal.hafizovbooks.R;
import uz.mirafzal.hafizovbooks.adapters.ListViewAdapter;
import uz.mirafzal.hafizovbooks.enums.Type;
import uz.mirafzal.hafizovbooks.models.Book;

public class BooksFragment extends Fragment {

    private static final String CATEGORY = "category";
    private static final String TYPE = "type";

    private Category category;
    private Type type;

    public BooksFragment() {
        // Required empty public constructor
    }

    public static BooksFragment newInstance(Category category, Type type) {
        BooksFragment fragment = new BooksFragment();
        Bundle args = new Bundle();
        args.putSerializable(CATEGORY, category);
        args.putSerializable(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = (Category) getArguments().get(CATEGORY);
            type = (Type) getArguments().get(TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_books, container, false);
        ListView listView = view.findViewById(R.id.listView);
        ArrayList<Book> books = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            books.add(new Book(0, R.drawable.book_cover,
                    getString(category.getTitleId()) + " " + getString(type.getTitleId()) + " " + (i + 1),
                    "Hafizov Sardor Boborajabovich", i * 1000, "books/book2.pdf"));
        }
        int listItemLayout;
        switch (category) {
            case MY_BOOKS:
                listItemLayout = R.layout.list_item_downloaded;
                break;
            case MY_PURCHASES:
                listItemLayout = R.layout.list_item_purchased;
                break;
            case SHOP:
                listItemLayout = R.layout.list_item_shop;
                break;
            default:
                listItemLayout = R.layout.list_item_downloaded;
        }
        ListViewAdapter adapter = new ListViewAdapter(inflater, books, listItemLayout);
        listView.setAdapter(adapter);
        return view;

    }
}