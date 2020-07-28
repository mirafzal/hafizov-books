package uz.mirafzal.hafizovbooks.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import uz.mirafzal.hafizovbooks.R;
import uz.mirafzal.hafizovbooks.adapters.FragmentsTabAdapter;
import uz.mirafzal.hafizovbooks.enums.Category;
import uz.mirafzal.hafizovbooks.enums.Type;
import uz.mirafzal.hafizovbooks.fragments.BooksFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final int RC_SIGN_IN = 123;
    public final static String SHARED_PREFERENCES_KEY = "uz.mirafzal.hafizovbooks.shf";

    FragmentsTabAdapter adapter;
    ArrayList<ArrayList<Fragment>> fragments = new ArrayList<>();
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        startActivity(new Intent(this, BookReaderActivity.class));
        setupNavigationDrawer();
        setNavigationViewItems();
        setupFragments();
        setupTabLayout(Category.MY_BOOKS);
        setupBottomNavigationView();
    }

    private void setupNavigationDrawer() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setNavigationViewItems() {
        Menu menu = navigationView.getMenu();
        menu.clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        if (isLogin()) {
            menu.removeGroup(R.id.menu_top_logged_out);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                menu.findItem(R.id.phone_number_item).setTitle(user.getPhoneNumber());
            }
        } else {
            menu.removeGroup(R.id.menu_top_logged_in);
        }
    }

    private void setupFragments() {
        for (Category category : Category.values()) {
            ArrayList<Fragment> tempFragments = new ArrayList<>();
            for (Type type : Type.values()) {
                tempFragments.add(BooksFragment.newInstance(category, type));
            }
            fragments.add(tempFragments);
        }
    }

    private void setupTabLayout(Category category) {
        adapter = new FragmentsTabAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs_main);
        for (Type type : Type.values()) {
            adapter.addFragment(fragments.get(category.ordinal()).get(type.ordinal()), getString(type.getTitleId()));
        }
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_books:
                        setupTabLayout(Category.MY_BOOKS);
                        return true;
                    case R.id.navigation_purchases:
                        setupTabLayout(Category.MY_PURCHASES);
                        return true;
                    case R.id.navigation_shop:
                        setupTabLayout(Category.SHOP);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                startLoginActivity();
                break;
            case R.id.phone_number_item:
                return false;
            case R.id.payments:
                startActivity(new Intent(this, PaymentsActivity.class));
                break;
            case R.id.logout:
                doLogout();
                break;
            case R.id.discounts:
                startActivity(new Intent(this, DiscountsActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.authors:
                startActivity(new Intent(this, AuthorsActivity.class));
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            default:
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    getPreferences(MODE_PRIVATE).edit().putBoolean("authorized", true).apply();
                    setNavigationViewItems();
                    toast(getString(R.string.you_have_successfully_entered_to_the_system));
                }
            } else {
                if (response != null) {
                    toast(response.getError().getErrorCode() + "");
                    toast(response.getError().getMessage());
                }
            }
        }

    }

    private void startLoginActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(
                                new AuthUI.IdpConfig.PhoneBuilder()
                                        .setDefaultCountryIso("uz")
                                        .build()
                                )
                        )
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    private void doLogout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        getPreferences(MODE_PRIVATE).edit().putBoolean("authorized", false).apply();
                        setNavigationViewItems();
                        toast(getString(R.string.you_have_successfully_logged_out_of_the_system));
                    }
                });
    }

    private boolean isLogin() {
        return getPreferences(MODE_PRIVATE).getBoolean("authorized", false);
    }

    private void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}