#!/usr/bin/env python3
"""
Generate SHA-256 hashes for all Mobile Shepherd flags
All flags now standardized to OWASP{} format
"""

import hashlib

# Define all flags with OWASP{} format
FLAGS = {
    # Lessons (11 total)
    'RE_LESSON': 'OWASP{R3v3rs3_Eng1n33r1ng_M4st3r}',
    'IDS_LESSON': 'OWASP{1ns3cur3_D4t4_St0r4g3_L3ss0n}',
    'POOR_AUTH_LESSON': 'OWASP{P00r_4uth3nt1c4t10n_L3ss0n}',
    'INSECURE_AUTH_LESSON': 'OWASP{1ns3cur3_4uth0r1z4t10n_L3ss0n}',
    'SUPPLY_CHAIN_LESSON': 'OWASP{Suppl7_Ch41n_S3cur1ty_L3ss0n}',
    'INSECURE_COMM_LESSON': 'OWASP{1ns3cur3_C0mmun1c4t10n_L3ss0n}',
    'INSUFFICIENT_CRYPTO_LESSON': 'OWASP{1nsuff1c13nt_Crypt0_L3ss0n}',
    'SECURITY_MISCONFIG_LESSON': 'OWASP{S3cur1ty_M1sc0nf1g_L3ss0n}',
    'INPUT_VALIDATION_LESSON': 'OWASP{1nput_V4l1d4t10n_L3ss0n}',
    'PRIVACY_LESSON': 'OWASP{Pr1v4cy_C0ntr0ls_L3ss0n}',
    'CLIENT_SIDE_INJECTION_LESSON': 'OWASP{CL13NT_S1D3_1NJ3CT10N_L3ss0n}',
    
    # Challenges (15 total)
    'RE_CHALLENGE_1': 'OWASP{S1mpl3_Fl4g_34sy_T0_F1nd}',
    'RE_CHALLENGE_2': 'OWASP{B4s364_3nc0d3d_Fl4g}',
    'RE_CHALLENGE_3': 'OWASP{0bfusc4t3d_H4rd_Ch4ll3ng3}',
    
    'IDS_CHALLENGE_1': 'OWASP{MD5_H4sh3d_P4ssw0rd}',
    'IDS_CHALLENGE_2': 'OWASP{Sh4r3dPr3fs_Vuln3r4bl3}',
    'IDS_CHALLENGE_3': 'OWASP{W34k_X0R_Crypt0_2024}',
    
    'POOR_AUTH_CHALLENGE': 'OWASP{P00r_Auth_W34k_Qu3st10ns}',
    
    'SUPPLY_CHAIN_CHALLENGE': 'OWASP{Suppl7_Ch41n_C0mpr0m1s3d}',
    
    'INSECURE_COMM_CHALLENGE': 'OWASP{N3tw0rk_Sn1ff3d_Tr4ff1c}',
    
    'INSUFFICIENT_CRYPTO_CHALLENGE': 'OWASP{3CB_M0d3_Vuln3r4bl3}',
    
    'SECURITY_MISCONFIG_CHALLENGE_2': 'OWASP{B4ckup_D4t4_3xtr4ct3d}',
    'SECURITY_MISCONFIG_CHALLENGE_3': 'OWASP{3xp0rt3d_C0mp0n3nt_Pwn}',
    
    'XSS_CHALLENGE': 'OWASP{XSS_W3bV13w_Pwn3d}',
    
    'CLIENT_SIDE_INJECTION_CHALLENGE_1': 'OWASP{SQL_1nj3ct10n_4dm1n_Pwn}',
    'CLIENT_SIDE_INJECTION_CHALLENGE_2': 'OWASP{UN10N_SQL_1nj3ct10n_M4st3r}',
}

def generate_hash(flag_text):
    """Generate SHA-256 hash of flag text"""
    return hashlib.sha256(flag_text.encode('utf-8')).hexdigest()

def main():
    print("=" * 80)
    print("Mobile Shepherd - Standardized Flag Hashes (OWASP{} Format)")
    print("=" * 80)
    print()
    
    # Generate hashes
    hashes = {}
    for module, flag in FLAGS.items():
        hashes[module] = generate_hash(flag)
    
    # Print Java code for FlagValidator.java
    print("// Copy this into FlagValidator.java FLAG_HASHES map:")
    print()
    
    print("// Lessons (11)")
    for module in ['RE_LESSON', 'IDS_LESSON', 'POOR_AUTH_LESSON', 'INSECURE_AUTH_LESSON',
                   'SUPPLY_CHAIN_LESSON', 'INSECURE_COMM_LESSON', 'INSUFFICIENT_CRYPTO_LESSON',
                   'SECURITY_MISCONFIG_LESSON', 'INPUT_VALIDATION_LESSON', 'PRIVACY_LESSON',
                   'CLIENT_SIDE_INJECTION_LESSON']:
        print(f'put(Module.{module}, "{hashes[module]}");')
    
    print()
    print("// Challenges (15)")
    for module in ['RE_CHALLENGE_1', 'RE_CHALLENGE_2', 'RE_CHALLENGE_3',
                   'IDS_CHALLENGE_1', 'IDS_CHALLENGE_2', 'IDS_CHALLENGE_3',
                   'POOR_AUTH_CHALLENGE', 'SUPPLY_CHAIN_CHALLENGE', 'INSECURE_COMM_CHALLENGE',
                   'INSUFFICIENT_CRYPTO_CHALLENGE', 'SECURITY_MISCONFIG_CHALLENGE_2',
                   'SECURITY_MISCONFIG_CHALLENGE_3', 'XSS_CHALLENGE',
                   'CLIENT_SIDE_INJECTION_CHALLENGE_1', 'CLIENT_SIDE_INJECTION_CHALLENGE_2']:
        print(f'put(Module.{module}, "{hashes[module]}");')
    
    print()
    print("=" * 80)
    print("Verification Table")
    print("=" * 80)
    print(f"{'Module':<40} {'Flag':<45} {'Hash (first 16 chars)'}")
    print("-" * 105)
    
    for module, flag in sorted(FLAGS.items()):
        hash_preview = hashes[module][:16] + "..."
        print(f"{module:<40} {flag:<45} {hash_preview}")
    
    print()
    print("=" * 80)
    print(f"Total Modules: {len(FLAGS)} (11 lessons + 15 challenges)")
    print("All flags now use OWASP{} format!")
    print("=" * 80)

if __name__ == '__main__':
    main()
