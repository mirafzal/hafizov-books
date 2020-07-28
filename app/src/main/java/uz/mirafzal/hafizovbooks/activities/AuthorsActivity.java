package uz.mirafzal.hafizovbooks.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import uz.mirafzal.hafizovbooks.R;
import uz.mirafzal.hafizovbooks.adapters.FragmentsTabAdapter;
import uz.mirafzal.hafizovbooks.enums.Category;
import uz.mirafzal.hafizovbooks.enums.Type;
import uz.mirafzal.hafizovbooks.fragments.AuthorsFragment;
import uz.mirafzal.hafizovbooks.fragments.BooksFragment;

public class AuthorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authors);
        setupToolbar();
        setupTabLayout();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupTabLayout() {
        FragmentsTabAdapter adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs_main);
        for (Type type : Type.values()) {
            adapter.addFragment(AuthorsFragment.newInstance(null, null), getString(type.getTitleId()));
        }
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}