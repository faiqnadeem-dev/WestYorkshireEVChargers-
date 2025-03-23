package com.evchargers.westyorkshire.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.evchargers.westyorkshire.databinding.FragmentRegisterBinding;
import com.evchargers.westyorkshire.utils.FirebaseHelper;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseHelper = new FirebaseHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        binding.registerButton.setOnClickListener(v -> handleRegister());
        return binding.getRoot();
    }

    private void handleRegister() {
        String email = binding.emailInput.getText() != null ? binding.emailInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        boolean isAdmin = binding.adminCheckbox.isChecked();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.createUserWithRole(email, password, isAdmin, success -> {
            if (success) {
                Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
