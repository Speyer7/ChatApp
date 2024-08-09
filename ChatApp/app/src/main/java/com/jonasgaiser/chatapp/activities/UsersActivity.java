package com.jonasgaiser.chatapp.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jonasgaiser.chatapp.R;
import com.jonasgaiser.chatapp.adapters.UsersAdapter;
import com.jonasgaiser.chatapp.databinding.ActivityUsersBinding;
import com.jonasgaiser.chatapp.listeners.UserListener;
import com.jonasgaiser.chatapp.models.User;
import com.jonasgaiser.chatapp.utilities.Constants;
import com.jonasgaiser.chatapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUsers();
        setListeners();
        checkConnectionWithIsConnected();
    }

    private void checkConnectionWithIsConnected() {
        if (isConnected()) {

        } else {
            showNoWifiDialog();
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo()!= null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @SuppressLint("MissingInflatedId")
    private void showNoWifiDialog() {
        ImageButton noWifiImage;
        Button continueButton;
        Button closeAppButton;

        View alertCustomDialogNoWifi = LayoutInflater.from(UsersActivity.this).inflate(R.layout.custom_dialog_internet, null);
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(UsersActivity.this);

        alertDialog.setView(alertCustomDialogNoWifi);

        noWifiImage = (ImageButton) alertCustomDialogNoWifi.findViewById(R.id.imageNoWifi);
        continueButton = (Button) alertCustomDialogNoWifi.findViewById(R.id.continueButton_dialog_nowifi);
        closeAppButton = (Button) alertCustomDialogNoWifi.findViewById(R.id.closeApp_dialog_nowifi);

        final android.app.AlertDialog dialog = alertDialog.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        noWifiImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeToast("You have no internet connection");
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                warnUserFromErrors();
            }
        });

        closeAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                System.exit(0);
            }
        });
    }

    private void warnUserFromErrors(){
        Button continueButton;

        View alertCustomDialogNoWifi = LayoutInflater.from(UsersActivity.this).inflate(R.layout.custom_dialog_errors_no_wifi, null);
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(UsersActivity.this);

        alertDialog.setView(alertCustomDialogNoWifi);

        continueButton = (Button) alertCustomDialogNoWifi.findViewById(R.id.continueButton_dialog_errors_no_wifi);

        final android.app.AlertDialog dialog = alertDialog.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}