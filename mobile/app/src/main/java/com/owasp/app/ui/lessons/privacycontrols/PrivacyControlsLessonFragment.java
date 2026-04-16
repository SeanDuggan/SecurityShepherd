package com.owasp.app.ui.lessons.privacycontrols;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.owasp.app.R;
import com.owasp.app.databinding.FragmentPrivacyControlsLessonBinding;
import com.owasp.app.utils.FlagValidator;
import com.owasp.app.utils.ProgressTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrivacyControlsLessonFragment extends Fragment {

    private FragmentPrivacyControlsLessonBinding binding;
    private Uri currentPhotoUri;
    private String currentPhotoPath;
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    
    // Flag hidden in preloaded image EXIF data
    private static final String PRELOADED_IMAGE = "privacy_sample.jpg";
    private static final String FLAG = "KEY{3x1f_M3t4d4t4_L34k5_L0c4t10n}";

    private final ActivityResultLauncher<Uri> takePictureLauncher = 
        registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && currentPhotoUri != null) {
                displayImage(currentPhotoPath);
                Toast.makeText(getContext(), "Photo saved without metadata stripping!", Toast.LENGTH_SHORT).show();
            }
        });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                launchCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrivacyControlsLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> toggleFabExpansion(fab, fabCommandRef, fabOwaspLink));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }
        if (fabOwaspLink != null) {
            fabOwaspLink.setOnClickListener(v -> {
                openOwaspTop10Link();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        // Load preloaded image with flag in EXIF
        loadPreloadedImage();

        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
        binding.btnLoadSample.setOnClickListener(v -> loadPreloadedImage());

        return root;
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                    "com.owasp.app.fileprovider", photoFile);
            takePictureLauncher.launch(currentPhotoUri);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "PRIVACY_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void loadPreloadedImage() {
        try {
            // Copy preloaded image from assets to app storage
            File outputFile = new File(requireContext().getFilesDir(), PRELOADED_IMAGE);
            
            if (!outputFile.exists()) {
                // Create image with EXIF metadata containing flag
                createPreloadedImageWithFlag(outputFile);
            }
            
            currentPhotoPath = outputFile.getAbsolutePath();
            displayImage(currentPhotoPath);
            
            binding.tvImageInfo.setText("Sample image loaded. This image contains hidden metadata with location, device info, and more!");
            Toast.makeText(getContext(), "Analyze the metadata to find the flag", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error loading sample image", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPreloadedImageWithFlag(File outputFile) throws IOException {
        // Create a simple bitmap
        Bitmap bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFF2196F3); // Blue background
        
        // Save bitmap to file
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
        }
        
        // Add EXIF metadata with flag
        ExifInterface exif = new ExifInterface(outputFile.getAbsolutePath());
        exif.setAttribute(ExifInterface.TAG_MAKE, "OWASP");
        exif.setAttribute(ExifInterface.TAG_MODEL, "Privacy Trainer");
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "37.7749");
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "-122.4194");
        exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, FLAG);
        exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "This metadata should be stripped!");
        exif.setAttribute(ExifInterface.TAG_ARTIST, "Security Researcher");
        exif.setAttribute(ExifInterface.TAG_DATETIME, "2024:01:15 14:30:00");
        exif.saveAttributes();
        
        bitmap.recycle();
    }

    private void displayImage(String imagePath) {
        if (imagePath != null && new File(imagePath).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            binding.ivPhoto.setImageBitmap(bitmap);
            binding.ivPhoto.setVisibility(View.VISIBLE);
        }
    }

    private void analyzeCurrentImage() {
        if (currentPhotoPath == null || !new File(currentPhotoPath).exists()) {
            Toast.makeText(getContext(), "No image to analyze. Load sample or take photo first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ExifInterface exif = new ExifInterface(currentPhotoPath);
            StringBuilder metadata = new StringBuilder();
            metadata.append("IMAGE METADATA ANALYSIS\n\n");
            
            // Extract various EXIF tags
            addMetadataIfExists(metadata, "Make", exif.getAttribute(ExifInterface.TAG_MAKE));
            addMetadataIfExists(metadata, "Model", exif.getAttribute(ExifInterface.TAG_MODEL));
            addMetadataIfExists(metadata, "DateTime", exif.getAttribute(ExifInterface.TAG_DATETIME));
            addMetadataIfExists(metadata, "GPS Latitude", exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            addMetadataIfExists(metadata, "GPS Longitude", exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            addMetadataIfExists(metadata, "Description", exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
            addMetadataIfExists(metadata, "User Comment", exif.getAttribute(ExifInterface.TAG_USER_COMMENT));
            addMetadataIfExists(metadata, "Artist", exif.getAttribute(ExifInterface.TAG_ARTIST));
            addMetadataIfExists(metadata, "Software", exif.getAttribute(ExifInterface.TAG_SOFTWARE));
            
            String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (orientation != null) {
                metadata.append("Orientation: ").append(orientation).append("\n");
            }
            
            metadata.append("\nWARNING: This metadata can reveal:\n");
            metadata.append("• Device type and model\n");
            metadata.append("• Exact location (GPS coordinates)\n");
            metadata.append("• When photo was taken\n");
            metadata.append("• Software/app used\n");
            metadata.append("• User information\n");
            metadata.append("• Custom data (like flags!)\n");

            new AlertDialog.Builder(requireContext())
                    .setTitle("Metadata Analysis")
                    .setMessage(metadata.toString())
                    .setPositiveButton("Close", null)
                    .show();

        } catch (IOException e) {
            Toast.makeText(getContext(), "Error reading metadata", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMetadataIfExists(StringBuilder sb, String label, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (fab2 != null) fab2.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1, fab2);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }

    private void openOwaspTop10Link() {
        String url = "https://owasp.org/www-project-mobile-top-10/2023-risks/m2-insecure-data-storage";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        android.widget.TextView introText = dialogView.findViewById(R.id.intro_text);
        // android.widget.TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        android.widget.TextView hintsText = dialogView.findViewById(R.id.hints_text);
        
        introText.setText(R.string.privacy_lesson_intro);
        // vulnerabilitiesText.setText(R.string.privacy_lesson_vulnerabilities);
        hintsText.setText(R.string.privacy_lesson_hints);
        dialogView.findViewById(R.id.hints_section).setVisibility(View.VISIBLE);
        
        // Add metadata analysis as additional info if image is loaded
        if (currentPhotoPath != null && new File(currentPhotoPath).exists()) {
            dialogView.findViewById(R.id.additional_section).setVisibility(View.VISIBLE);
            android.widget.TextView additionalTitle = dialogView.findViewById(R.id.additional_title);
            android.widget.TextView additionalText = dialogView.findViewById(R.id.additional_text);
            additionalTitle.setText("Current Image Metadata");
            additionalText.setText(getImageMetadata());
        } else {
            dialogView.findViewById(R.id.additional_section).setVisibility(View.GONE);
        }

        boolean isCompleted = progressTracker.isCompleted(FlagValidator.Module.PRIVACY_LESSON);
        String buttonText = isCompleted ? "Mark as Incomplete" : "Mark as Complete";

        new AlertDialog.Builder(requireContext())
                .setTitle("Inadequate Privacy Controls")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .setNeutralButton(buttonText, (d, which) -> {
                    boolean nowCompleted = progressTracker.toggleCompleted(FlagValidator.Module.PRIVACY_LESSON);
                    String message = nowCompleted ? "✓ Marked as complete!" : "○ Marked as incomplete";
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    
    private String getImageMetadata() {
        try {
            ExifInterface exif = new ExifInterface(currentPhotoPath);
            StringBuilder metadata = new StringBuilder();
            metadata.append("IMAGE METADATA:\n\n");
            
            addMetadataIfExists(metadata, "Make", exif.getAttribute(ExifInterface.TAG_MAKE));
            addMetadataIfExists(metadata, "Model", exif.getAttribute(ExifInterface.TAG_MODEL));
            addMetadataIfExists(metadata, "DateTime", exif.getAttribute(ExifInterface.TAG_DATETIME));
            addMetadataIfExists(metadata, "GPS Latitude", exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            addMetadataIfExists(metadata, "GPS Longitude", exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            addMetadataIfExists(metadata, "Description", exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
            addMetadataIfExists(metadata, "User Comment", exif.getAttribute(ExifInterface.TAG_USER_COMMENT));
            addMetadataIfExists(metadata, "Artist", exif.getAttribute(ExifInterface.TAG_ARTIST));
            addMetadataIfExists(metadata, "Software", exif.getAttribute(ExifInterface.TAG_SOFTWARE));
            
            return metadata.toString();
        } catch (Exception e) {
            return "Error reading metadata: " + e.getMessage();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
