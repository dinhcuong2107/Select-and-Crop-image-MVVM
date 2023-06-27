package com.example.myapplication;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.FileUtils;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.model.Users;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainVM extends ViewModel {
    private MutableLiveData<Uri> selectedImageUri = new MutableLiveData<>();
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private MutableLiveData<AppCompatActivity> context = new MutableLiveData<>();

    public MainVM(ActivityResultRegistry registry) {
        cropImageLauncher = registry.register("crop_image", new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                if (cropResult.isSuccessful()) {
                    Uri croppedUri = cropResult.getUri();
                    Log.d("MainVM", "croppedUri: " + croppedUri);
                    selectedImageUri.setValue(croppedUri);
                } else {
                    Exception error = cropResult.getError();
                    Log.d("MainVM", "cropError: " + error);
                }
            }
        });

        pickImageLauncher = registry.register("select_image", new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        startCropActivity(result);
                    }
                });
    }

    private void startCropActivity(Uri uri) {
        AppCompatActivity activity = context.getValue();
        if (activity == null) {
            Log.e("MainVM", "Context is null");
            return;
        }
        Intent cropIntent = CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .getIntent(activity.getApplicationContext());

        cropImageLauncher.launch(cropIntent);
    }

    public void launchImagePicker(View view) {
        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        context.setValue(activity);
        pickImageLauncher.launch("image/*");
    }

    @BindingAdapter({"android:src"})
    public static void setImageView(ImageView imageView, Uri imgUrl) {
        if (imgUrl == null) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Picasso.get().load(imgUrl).into(imageView);
        }
    }

    public LiveData<Uri> getSelectedImageUri() {
        return selectedImageUri;
    }
}