#!/usr/bin/env python3
"""
Flag Hash Generator for MobileShepherd
Generates SHA-256 hashes for all flags to populate FlagValidator.java
"""

import hashlib

# Define all flags from CHALLENGE_SOLUTIONS.txt
FLAGS = {
    # Reverse Engineering
    'RE_LESSON': 'FLAG{R3v3rs3_Eng1n33r1ng_M4st3r_2024}',
    'RE_CHALLENGE_1': 'OWASP{Simple_Flag_Easy_To_Find}',
    'RE_CHALLENGE_2': 'OWASP{Base64_Encoded_Flag}',
    'RE_CHALLENGE_3': 'OWASP{Obfuscated_Hard_Challenge}',
    
    # Insecure Data Storage
    'IDS_LESSON': 'TheSnailsGoMeowAndTheTurtlesSayMoo',
    'IDS_CHALLENGE_1': 'letmein2024',
    'IDS_CHALLENGE_2': 'OWASP{FileSt0rage_Insecure_2024}',
    'IDS_CHALLENGE_3': 'SecureFlag{WeakXOR_Crypto_2024}',
    
    # Poor Authentication
    'POOR_AUTH_LESSON': 'P00rAuthL3ss0nFl4g2024',
    'POOR_AUTH_CHALLENGE': 'OWASP{P00r_Auth_Weak_Questions_2024}',
    
    # Insecure Authorization
    'INSECURE_AUTH_LESSON': 'Shepherd{Pr1v1l3g3_Esc4l4t10n_Pwn3d}',
    
    # Supply Chain
    'SUPPLY_CHAIN_LESSON': 'SupplyCh41nS3cur1tyM4tt3rs',
    'SUPPLY_CHAIN_CHALLENGE': 'OWASP{Suppl7_Ch41n_C0mpr0m1s3d}',
    
    # Insecure Communication
    'INSECURE_COMM_LESSON': 'Pl41nT3xtTr4ff1cL34k5',
    'INSECURE_COMM_CHALLENGE': 'OWASP{N3tw0rk_Sn1ff3d}',
    
    # Insufficient Cryptography
    'INSUFFICIENT_CRYPTO_LESSON': 'W34kCrypt0L34dsT0Bre4ch',
    'INSUFFICIENT_CRYPTO_CHALLENGE': 'OWASP{ECB_M0d3_Vuln3r4bl3}',
    
    # Security Misconfiguration
    'SECURITY_MISCONFIG_LESSON': 'M1sc0nf1gur4t10nR1sks',
    'SECURITY_MISCONFIG_CHALLENGE_2': 'OWASP{B4ckup_D4t4_3xtr4ct3d}',
    'SECURITY_MISCONFIG_CHALLENGE_3': 'OWASP{3xp0rt3d_C0mp0n3nt_Pwn}',
    
    # Input Validation
    'INPUT_VALIDATION_LESSON': 'FLAG{1nput_V4l1d4t10n_Byp4ss3d}',
    'XSS_CHALLENGE': 'FLAG{XSS_W3bV13w_Pwn3d_2024}',
    
    # Privacy Controls
    'PRIVACY_LESSON': 'OWASP{3x1f_M3t4d4t4_L34k5_L0c4t10n}',
    
    # Client-Side Injection
    'CLIENT_SIDE_INJECTION_LESSON': 'FLAG{CL13NT_S1D3_SQL_1NJ3CT10N}',
    'CLIENT_SIDE_INJECTION_CHALLENGE_1': 'SourHatsAndAngryCats',
    'CLIENT_SIDE_INJECTION_CHALLENGE_2': 'BurpingChimneys',
}

def sha256_hash(text):
    """Generate SHA-256 hash of the input text"""
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def generate_hashes():
    """Generate and print all flag hashes"""
    print("// SHA-256 hashes of correct flags")
    print("// Generated using flag_hash_generator.py")
    print("private static final Map<Module, String> FLAG_HASHES = new HashMap<Module, String>() {{")
    
    # Group by category
    categories = {
        'Reverse Engineering': ['RE_LESSON', 'RE_CHALLENGE_1', 'RE_CHALLENGE_2', 'RE_CHALLENGE_3'],
        'Insecure Data Storage': ['IDS_LESSON', 'IDS_CHALLENGE_1', 'IDS_CHALLENGE_2', 'IDS_CHALLENGE_3'],
        'Poor Authentication': ['POOR_AUTH_LESSON', 'POOR_AUTH_CHALLENGE'],
        'Insecure Authorization': ['INSECURE_AUTH_LESSON'],
        'Supply Chain': ['SUPPLY_CHAIN_LESSON', 'SUPPLY_CHAIN_CHALLENGE'],
        'Insecure Communication': ['INSECURE_COMM_LESSON', 'INSECURE_COMM_CHALLENGE'],
        'Insufficient Cryptography': ['INSUFFICIENT_CRYPTO_LESSON', 'INSUFFICIENT_CRYPTO_CHALLENGE'],
        'Security Misconfiguration': ['SECURITY_MISCONFIG_LESSON', 'SECURITY_MISCONFIG_CHALLENGE_2', 'SECURITY_MISCONFIG_CHALLENGE_3'],
        'Input Validation': ['INPUT_VALIDATION_LESSON', 'XSS_CHALLENGE'],
        'Privacy Controls': ['PRIVACY_LESSON'],
        'Client-Side Injection': ['CLIENT_SIDE_INJECTION_LESSON', 'CLIENT_SIDE_INJECTION_CHALLENGE_1', 'CLIENT_SIDE_INJECTION_CHALLENGE_2'],
    }
    
    for category, modules in categories.items():
        print(f"    // {category}")
        for module in modules:
            if module in FLAGS:
                flag = FLAGS[module]
                hash_val = sha256_hash(flag)
                print(f'    put(Module.{module}, "{hash_val}");')
        print()
    
    print("}};")
    print()
    
    # Print verification table
    print("\n// Verification Table (for reference/testing)")
    print("// Module | Flag | SHA-256 Hash")
    print("// " + "-" * 120)
    for module, flag in FLAGS.items():
        hash_val = sha256_hash(flag)
        print(f"// {module:40} | {flag:50} | {hash_val}")

def verify_hash(flag_text, expected_hash):
    """Verify a hash matches"""
    actual = sha256_hash(flag_text)
    match = actual == expected_hash
    print(f"Flag: {flag_text}")
    print(f"Expected: {expected_hash}")
    print(f"Actual:   {actual}")
    print(f"Match: {'✓ YES' if match else '✗ NO'}")
    return match

if __name__ == "__main__":
    print("=" * 120)
    print("MobileShepherd Flag Hash Generator")
    print("=" * 120)
    print()
    
    generate_hashes()
    
    print("\n" + "=" * 120)
    print("Example Hash Verification:")
    print("=" * 120)
    print()
    verify_hash('SourHatsAndAngryCats', sha256_hash('SourHatsAndAngryCats'))
    print()
    verify_hash('BurpingChimneys', sha256_hash('BurpingChimneys'))
