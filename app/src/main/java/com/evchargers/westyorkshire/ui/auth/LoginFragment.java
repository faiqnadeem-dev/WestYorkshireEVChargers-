package com.evchargers.westyorkshire.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.evchargers.westyorkshire.databinding.FragmentLoginBinding;
import androidx.navigation.Navigation;
import com.evchargers.westyorkshire.R;
import com.evchargers.westyorkshire.utils.FirebaseHelper;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.registerButton.setOnClickListener(v -> handleRegister());
        return binding.getRoot();
    }

    private void handleLogin() {
        String email = binding.emailInput.getText() != null ? binding.emailInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    firebaseHelper.isCurrentUserAdmin(isAdmin -> {
                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_loginFragment_to_mainFragment);
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleRegister() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_registerFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
